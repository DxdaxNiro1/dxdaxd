"""Soft shoe-contact effects: separate heel, sole, toe and surface friction, three variations.
All synthesis is original. No recordings or reference-video audio are used.
"""
from pathlib import Path
import numpy as np
import subprocess,tempfile,wave,json,hashlib
from generate_soundscape_16 import ambience,event,finish,export
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'app/src/main/res/raw';RATE=44100

def band(rng,n,low,high):
    f=np.fft.rfftfreq(n,1/RATE)
    shape=np.exp(-(f/high)**4)*(1-np.exp(-(f/max(low,1))**4))
    v=np.fft.irfft(np.fft.rfft(rng.standard_normal(n))*shape,n=n)
    return v/max(np.sqrt(np.mean(v*v)),1e-9)

def bump(t,at,attack,decay):
    u=np.maximum(t-at,0)
    return (1-np.exp(-u/attack))*np.exp(-u/decay)*(t>=at)

def shoe(surface,variant):
    rng=np.random.default_rng(17000+['grass','stone','tile','wood','water'].index(surface)*100+variant)
    n=int(RATE*(.46+rng.uniform(-.025,.025)));t=np.arange(n)/RATE
    sole=band(rng,n,35,300);grit=band(rng,n,450,2200)
    heel=bump(t,.008,.004,.032)
    toe=bump(t,.090+rng.uniform(-.008,.012),.009,.045)
    v=sole*(heel*.48+toe*.27)+grit*(heel*.065+toe*.055)
    if surface=='grass':
        v=v*.6+band(rng,n,180,1700)*bump(t,.025,.020,.068)*.20
    elif surface=='stone':
        for at in (.036,.112,.163):v+=grit*bump(t,at,.004,.012)*rng.uniform(.035,.065)
    elif surface=='tile':
        v+=band(rng,n,1300,2900)*(heel*.060+toe*.018)
    elif surface=='wood':
        for f,level in [(117,.08),(247,.035),(391,.018)]:
            v+=np.sin(2*np.pi*f*(1+rng.uniform(-.04,.04))*t)*heel*level
    elif surface=='water':
        v=v*.45+band(rng,n,160,1900)*bump(t,.028,.015,.082)*.23
        for i in range(3):
            u=np.maximum(t-(.06+i*.037),0)
            v+=np.sin(2*np.pi*((450+i*270)*u+120*np.log1p(u*20)))*bump(t,.06+i*.037,.003,.017)*.016
    v*=np.minimum(t/.006,1)*np.minimum((len(v)/RATE-t)/.025,1)
    v-=v.mean();v*=.045/max(np.sqrt(np.mean(v*v)),1e-9)
    v*=min(1,.29/max(abs(v).max(),1e-9))
    return v

def export_shoe(name,signal):
    with tempfile.TemporaryDirectory() as td:
        wav=Path(td)/'sole.wav'
        with wave.open(str(wav),'wb') as w:
            w.setnchannels(1);w.setsampwidth(2);w.setframerate(RATE);w.writeframes((signal*32767).astype('<i2').tobytes())
        target=OUT/(name+'.ogg')
        subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','5',str(target)],check=True)
        data=subprocess.check_output(['ffmpeg','-v','error','-i',str(target),'-f','f32le','-ac','1','-ar',str(RATE),'-'])
        v=np.frombuffer(data,dtype='<f4');spectrum=abs(np.fft.rfft(v))**2;freq=np.fft.rfftfreq(len(v),1/RATE)
        metrics=dict(peak=float(abs(v).max()),rms=float(np.sqrt(np.mean(v*v))),seconds=len(v)/RATE,
            spectral_centroid_hz=float((spectrum*freq).sum()/spectrum.sum()),clipped=int((abs(v)>=.999).sum()),
            bytes=target.stat().st_size,sha256=hashlib.sha256(target.read_bytes()).hexdigest())
        assert metrics['peak']<.36 and metrics['clipped']==0 and metrics['spectral_centroid_hz']<2400
        return metrics

def main():
    reports={}
    for surface in ['grass','stone','tile','wood','water']:
        for variant in range(3):
            name=f'fx_{surface}_{variant}_17';reports[name]=export_shoe(name,shoe(surface,variant))
    for i,name in enumerate(['air_moon_17','air_eclipse_17']):
        out=ambience(32,1721+i,'room')*.45
        for j,(at,note) in enumerate([(1,38),(10,45),(20,41),(27,50)]):
            event(out,at,note,'choir' if i==0 else 'organ',.028,-.25+j*.16,9,reverse=j%2==0)
        reports[name]=export(name,finish(out,.045 if i==0 else .05),'Original quiet lunar layer; no loud transient')
    (ROOT/'tools/footstep_validation_17.json').write_text(json.dumps(reports,indent=2)+'\n')
    print('Validated',len(reports),'new audio assets')
if __name__=='__main__':main()
