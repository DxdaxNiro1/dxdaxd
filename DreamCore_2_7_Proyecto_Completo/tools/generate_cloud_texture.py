#!/usr/bin/env python3
"""Deterministic, periodic cumulus density data for the GLES2 cloud sky.

Requires Python 3, numpy and Pillow. No external artwork or training data.
R = density; G/B = signed derivatives in +U/+V, encoded around 128.
Decode derivative = (channel * 255 - 128) / 127 * DERIVATIVE_SCALE.
Derivatives are density per complete texture UV, not per texel.
The image is linear numerical data; upload without a color-space conversion.

Example:
    python tools/generate_cloud_texture.py
    python tools/generate_cloud_texture.py --preview /tmp/cloud-density-preview.png
"""
from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
from PIL import Image

SIZE = 1024
SEED = 0xD4EA4C0
DERIVATIVE_SCALE = 64.0


def smoothstep(low: float, high: float, field: np.ndarray) -> np.ndarray:
    t = np.clip((field - low) / (high - low), 0.0, 1.0)
    return t * t * (3.0 - 2.0 * t)


def generate(size: int = SIZE) -> tuple[np.ndarray, np.ndarray]:
    """Synthesize a tile on a torus: there are no hard image boundaries."""
    rng = np.random.default_rng(SEED)
    freq = np.fft.fftfreq(size, 1.0 / size)
    fx, fy = np.meshgrid(freq, freq)
    frequency2 = fx * fx + fy * fy
    yy, xx = np.mgrid[:size, :size].astype(np.float64) / size

    def noise(band: float) -> np.ndarray:
        # Isotropic low-pass noise avoids directional/horizontal banding.
        spectrum = np.fft.fft2(rng.standard_normal((size, size)))
        filtered = np.fft.ifft2(
            spectrum * np.exp(-frequency2 / (2.0 * band * band))
        ).real
        filtered -= filtered.mean()
        return filtered / max(float(filtered.std()), 1e-8)

    broad = noise(2.2)
    warp_x, warp_y = noise(4.0), noise(4.0)
    # Two independent gentle curl-like distortions break circular repetitions.
    wx = np.mod(xx + 0.018 * warp_x, 1.0)
    wy = np.mod(yy + 0.018 * warp_y, 1.0)
    envelope = np.zeros_like(xx)
    billows = np.zeros_like(xx)
    for _ in range(23):
        cx, cy = rng.random(2)
        radius = rng.uniform(0.055, 0.125)
        dx = (wx - cx + 0.5) % 1.0 - 0.5
        dy = (wy - cy + 0.5) % 1.0 - 0.5
        parent = np.exp(-(dx * dx + dy * dy) / (radius * radius))
        envelope += (parent * rng.uniform(0.8, 1.15)) ** 4
        # Unequal overlapping lobes give rounded cumulus silhouettes.
        for _ in range(int(rng.integers(4, 8))):
            angle = rng.uniform(0.0, np.pi * 2.0)
            spread = radius * rng.uniform(0.2, 0.8)
            lx = cx + np.cos(angle) * spread
            ly = cy + np.sin(angle) * spread
            lobe_radius = radius * rng.uniform(0.30, 0.62)
            dx = (wx - lx + 0.5) % 1.0 - 0.5
            dy = (wy - ly + 0.5) % 1.0 - 0.5
            lobe = np.exp(-(dx * dx + dy * dy) / (lobe_radius * lobe_radius))
            billows += (lobe * rng.uniform(0.45, 0.85)) ** 4

    # Smooth maxima preserve rounded lobes without hard derivative seams.
    envelope = envelope ** 0.25
    billows = billows ** 0.25
    medium, small, fine = noise(9.0), noise(23.0), noise(57.0)
    mass = 0.60 * envelope + 0.43 * billows + 0.065 * broad
    # Fractal erosion affects the silhouette and interior without coarse grain.
    mass += 0.050 * medium + 0.024 * small + 0.010 * fine
    density = 1.0 - np.exp(-2.5 * np.maximum(mass - 0.24, 0.0))
    # A soft opacity ramp leaves clear sky between fields and keeps thin wisps.
    density = density ** 1.05
    density = np.clip(density, 0.0, 1.0)
    du = (np.roll(density, -1, 1) - np.roll(density, 1, 1)) * (size * 0.5)
    dv = (np.roll(density, -1, 0) - np.roll(density, 1, 0)) * (size * 0.5)
    rgb = np.empty((size, size, 3), dtype=np.uint8)
    rgb[..., 0] = np.rint(density * 255.0).astype(np.uint8)
    for channel, derivative in ((1, du), (2, dv)):
        rgb[..., channel] = np.rint(
            128.0 + 127.0 * np.clip(derivative / DERIVATIVE_SCALE, -1.0, 1.0)
        ).astype(np.uint8)
    return rgb, density


def preview(rgb: np.ndarray, density: np.ndarray, path: Path) -> None:
    """A diagnostic daylight composite, not an additional game texture."""
    size = density.shape[0]
    du = (rgb[..., 1].astype(float) - 128.0) / 127.0
    dv = (rgb[..., 2].astype(float) - 128.0) / 127.0
    light = np.clip(0.90 + (-du * 0.26 - dv * 0.18), 0.67, 1.03)
    cloud = np.stack((light, light * 0.985, light * 1.015), axis=-1)
    cloud = np.minimum(cloud, 1.0)
    y = np.linspace(0.0, 1.0, size)[:, None, None]
    sky = np.broadcast_to((1.0 - y) * np.array([0.40, 0.63, 0.88]) +
                          y * np.array([0.79, 0.80, 0.92]), (size, size, 3))
    alpha = smoothstep(0.005, 0.78, density)[..., None]
    image = np.rint(np.clip(sky * (1.0 - alpha) + cloud * alpha, 0.0, 1.0) * 255)
    Image.fromarray(image.astype(np.uint8), 'RGB').save(path)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=Path(__file__).resolve().parents[1] /
                        'app/src/main/res/drawable-nodpi/dream_cloud_density.png')
    parser.add_argument('--preview', type=Path)
    args = parser.parse_args()
    rgb, density = generate()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    Image.fromarray(rgb, 'RGB').save(args.output, optimize=True)
    if args.preview:
        args.preview.parent.mkdir(parents=True, exist_ok=True)
        preview(rgb, density, args.preview)
    print(f'Wrote {args.output}: {SIZE}x{SIZE} RGB8, {args.output.stat().st_size} bytes')
    print(f'R=density; G/B=128+127*derivativeUV/{DERIVATIVE_SCALE}; periodic REPEAT')
    print(f'Coverage density>0.10={np.mean(density > .10):.1%}; max={density.max():.3f}')


if __name__ == '__main__':
    main()
