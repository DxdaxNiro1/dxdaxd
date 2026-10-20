"""Authored, original ambient score collection. No reference samples or downloaded audio.

Four-section arrangements, unique motifs/harmony/pulse and seeded performances.
Render offline; the Android app only streams compressed audio. Requires numpy/ffmpeg.
"""
from pathlib import Path
import argparse, hashlib, json, subprocess, tempfile, wave
import numpy as np

ROOT=Path(__file__).resolve().parents[1]
RATE=32000
RAW=ROOT/'app/src/main/res/raw'
ALBUM=ROOT/'Banda_sonora'
RHYTHMS=[[1,.5,1.5,.75,1.25],[.75,1.25,1,.5,1.5],[1.5,.5,1,.75,.75],[1,1,.5,1.75,.75],[.5,1.5,.75,1.25,1],[1.25,.75,1.5,.5,1]]

def add(out,at,signal,pan):
    n=len(signal);start=int(at*RATE)%len(out)
    stereo=np.column_stack((signal*np.sqrt((1-pan)*.5),signal*np.sqrt((1+pan)*.5))).astype(np.float32)
    cut=min(n,len(out)-start);out[start:start+cut]+=stereo[:cut]
    if cut<n:out[:n-cut]+=stereo[cut:]

def voice(out,at,midi,kind,level,length,pan,rng,reverse=False):
    t=np.arange(int(length*RATE),dtype=np.float32)/RATE
    f=440*2**((midi-69)/12)
    # Slow tape drift, not a pitch-shifted copy of another recording.
    phase=2*np.pi*f*t+.018*np.sin(t*(2.1+rng.random()))
    if kind in ('felt','water','guitar','harp','marimba','musicbox','glass','tape'):
        attack={'felt':.018,'water':.06,'guitar':.012,'harp':.015,'marimba':.025,'musicbox':.03,'glass':.12,'tape':.3}[kind]
        decay={'felt':2.7,'water':4.8,'guitar':2.3,'harp':3.4,'marimba':1.9,'musicbox':3.1,'glass':5.2,'tape':5.5}[kind]
        env=(1-np.exp(-t/attack))*np.exp(-t/decay)
        if kind=='water':v=np.sin(phase+.22*np.sin(phase*2.003)*np.exp(-t/1.7))+.12*np.sin(phase*3.01)*np.exp(-t/2)
        elif kind=='marimba':v=np.sin(phase)+.18*np.sin(phase*3.99)*np.exp(-t/.45)
        elif kind=='musicbox':v=np.sin(phase)+.13*np.sin(phase*2.76)*np.exp(-t/1.4)+.035*np.sin(phase*5.4)*np.exp(-t/.5)
        elif kind=='glass':v=np.sin(phase+.14*np.sin(phase*2.71)*np.exp(-t/3))+.06*np.sin(phase*4.08)*np.exp(-t/2)
        elif kind=='tape':v=np.sin(phase)+.16*np.sin(phase*2+.05*np.sin(t*2))+.06*np.sin(phase*3)
        else:
            v=np.sin(phase)+.20*np.sin(phase*2.001)*np.exp(-t/1.6)+.06*np.sin(phase*3.003)*np.exp(-t/.7)
            if kind=='guitar':v+=.10*np.sin(phase*4.002)*np.exp(-t/.25)
    else:
        env=(1-np.exp(-t/(1.5 if kind=='bow' else .8)))*np.exp(-t/(length*.48))
        if kind=='choir':v=np.sin(phase+.09*np.sin(t*3.2))+.14*np.sin(phase*2)+.07*np.sin(phase*3)
        elif kind=='organ':v=np.sin(phase)+.25*np.sin(phase*2)+.11*np.sin(phase*.5)
        elif kind=='bow':v=np.sin(phase)+.17*np.sin(phase*2+.12*np.sin(t*3.7))+.065*np.sin(phase*3)+.022*np.sin(phase*5)
        else:v=np.sin(phase+.12*np.sin(t*4.3))+.04*np.sin(phase*2)
    env*=np.clip((length-t)/min(3.,length*.3),0,1)
    signal=(v*env*level).astype(np.float32)
    if reverse:signal=signal[::-1]
    add(out,at,signal,pan)

def compose(c):
    rng=np.random.default_rng(c['seed']);n=c['seconds']*RATE
    out=np.zeros((n,2),dtype=np.float32)
    dark=c['world'].startswith('dark') or c['world'] in ['backrooms_01','preview_eyes','pocket_call_house','dream_03','dream_07','echo_10']
    third=3 if dark else 4;root=c['root'];quarter=c['seconds']/4
    pad='bow' if dark else ('organ' if c['lead'] in ['musicbox','marimba','guitar'] else 'choir')
    # Distinct harmonic movement, four overlapping voicings, sparse bass changes.
    for section,shift in enumerate(c['harmony']):
        at=section*quarter-2.5
        chord=[0,third,7,13 if dark and section==2 else 14]
        for j,interval in enumerate(chord):
            voice(out,at+j*.37,root+shift+interval-12,pad,.020/(1+j*.35),quarter+6,(-1 if j%2 else 1)*.42,rng)
        voice(out,at+1,root+shift-24,'organ',.012,quarter+3,0,rng)
    # Each world has its own authored phrase; later sections answer or interrupt it.
    rhythm=RHYTHMS[(c['number']-1)%len(RHYTHMS)]
    for section in range(4):
        order=list(c['motif'])
        if section==1:order=order[2:]+order[:2]
        if section==2:order=order[::2][::-1]
        if section==3:order=order[:-1]+[0]
        at=section*quarter+2.8+(c['number']%4)*.55
        shift=c['harmony'][section]
        for j,interval in enumerate(order):
            if at>=(section+1)*quarter-2:break
            length=8.5 if c['lead'] in ['marimba','felt','guitar','musicbox','harp'] else 14.
            pitch=root+interval+shift+(12 if c['lead']=='musicbox' else 0)
            level=.052 if section!=2 else .030
            pan=np.sin(j*1.27+section*.8+c['number'])*.48
            voice(out,at,pitch,c['lead'],level*(.84+rng.random()*.25),length,pan,rng,reverse=c['lead']=='tape' and j%3==1)
            if (j+section+c['number'])%3==0:
                answer='felt' if c['lead'] in ['choir','bow','organ','flute'] else 'flute'
                voice(out,at+c['pulse']*.43,pitch-12,answer,.012,10.,-pan,rng)
            at+=c['pulse']*rhythm[(j+section)%len(rhythm)]*(.68 if section==1 else 1.)
    # One quiet, uneasy memory; no jump scare, voice or rhythm taken from the upload.
    voice(out,c['seconds']*.61,root+13 if dark else root+18,'glass',.006,17.,.6,rng,True)
    source=out.copy()
    for delay,gain in [(.193,.24),(.437,.19),(.811,.13),(1.697,.09),(3.127,.055),(5.31,.026)]:
        out+=np.roll(source,int(delay*RATE),axis=0)[:,::-1]*gain
    # Gentle low-pass rounding preserves air while taming brittle high harmonics.
    for ch in range(2):
        freq=np.fft.rfftfreq(n,1/RATE)
        spectrum=np.fft.rfft(out[:,ch]);spectrum*=1/(1+(freq/6400)**6)
        out[:,ch]=np.fft.irfft(spectrum,n).astype(np.float32)
    out-=out.mean(axis=0)
    out*=.079/max(float(np.sqrt(np.mean(out*out))),1e-9)
    out*=min(1.,.68/max(float(np.max(np.abs(out))),1e-9))
    # Quiet loop join with long tails and no DC step; natural breath every two minutes.
    fade=int(2.5*RATE);out[:fade]*=np.linspace(0,1,fade,dtype=np.float32)[:,None]
    out[-fade:]*=np.linspace(1,0,fade,dtype=np.float32)[:,None]
    return out

def decode_metrics(path):
    raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(path),'-f','f32le','-ac','2','-ar',str(RATE),'-'])
    sig=np.frombuffer(raw,dtype='<f4').reshape(-1,2)
    blocks=[float(np.sqrt(np.mean(b*b))) for b in np.array_split(sig,8)]
    return dict(seconds=round(len(sig)/RATE,3),peak=float(abs(sig).max()),rms=float(np.sqrt(np.mean(sig*sig))),clipped=int((abs(sig)>=.999).sum()),seam=float(abs(sig[0]-sig[-1]).max()),section_rms=blocks,sha256=hashlib.sha256(path.read_bytes()).hexdigest(),bytes=path.stat().st_size)

def main():
    a=argparse.ArgumentParser();a.add_argument('--only',type=int);args=a.parse_args()
    RAW.mkdir(parents=True,exist_ok=True);(ALBUM/'Musica').mkdir(parents=True,exist_ok=True)
    catalog=json.loads((ROOT/'tools/music_catalog_24.json').read_text())
    reportpath=ROOT/'tools/audio_validation_24.json'
    report=json.loads(reportpath.read_text()) if reportpath.exists() else {}
    for c in catalog:
        if args.only and c['number']!=args.only:continue
        out=compose(c)
        with tempfile.TemporaryDirectory() as td:
            wav=Path(td)/'score.wav'
            with wave.open(str(wav),'wb') as f:
                f.setnchannels(2);f.setsampwidth(2);f.setframerate(RATE);f.writeframes((np.clip(out,-.99,.99)*32767).astype('<i2').tobytes())
            ogg=RAW/(c['resource']+'.ogg');mp3=ALBUM/'Musica'/c['file']
            meta=['-metadata','title='+c['title'],'-metadata','album=DreamCore - Resonancias 2.4','-metadata','artist=DreamCore - composiciones originales','-metadata','track='+str(c['number']),'-metadata','comment=Original procedural composition; no uploaded recording samples']
            subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','1',*meta,str(ogg)],check=True)
            subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libmp3lame','-q:a','5',*meta,str(mp3)],check=True)
        metrics=decode_metrics(ogg);mp3metrics=decode_metrics(mp3)
        for m in (metrics,mp3metrics):
            assert m['clipped']==0 and m['peak']<.9 and m['seam']<.012 and abs(m['seconds']-c['seconds'])<.1
            assert .025<m['rms']<.1 and min(m['section_rms'])>.012
        report[c['resource']]=dict(ogg=metrics,mp3=mp3metrics,world=c['world'],title=c['title'],authored_brief=c)
        reportpath.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
        print(f'{c["number"]:02}: {c["title"]} | {metrics["seconds"]}s | peak {metrics["peak"]:.3f}',flush=True)

if __name__=='__main__':main()
