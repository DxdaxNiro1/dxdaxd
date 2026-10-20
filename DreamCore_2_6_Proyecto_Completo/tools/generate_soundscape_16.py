"""Original 1.6 scores and effects, synthesized without recordings or network inputs.
Each world uses a different phrase, timbre, event spacing and harmonic progression.
Requires numpy and ffmpeg. OGG artifacts are decoded and checked after encoding.
"""
from pathlib import Path
import numpy as np
import subprocess, json, wave, tempfile, hashlib
RATE=44100
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'app/src/main/res/raw'
OUT.mkdir(parents=True,exist_ok=True)

def note(m):return 440*2**((m-69)/12)
def event(out,start,midi,kind,level,pan=0.,length=9.,reverse=False):
    n=int(length*RATE);t=np.arange(n)/RATE;f=note(midi)
    if kind=='piano':
        env=(1-np.exp(-t/.009))*np.exp(-t/1.8)
        v=np.sin(2*np.pi*f*t)+.28*np.sin(2*np.pi*f*2.001*t)*np.exp(-t/1.2)+.12*np.sin(2*np.pi*f*3.002*t)*np.exp(-t/.4)
    elif kind=='harp':
        env=(1-np.exp(-t/.003))*np.exp(-t/1.25)
        v=sum(np.sin(2*np.pi*f*k*t)/(k*k) for k in range(1,7))
    elif kind=='glass':
        env=(1-np.exp(-t/.05))*np.exp(-t/3.2)
        v=np.sin(2*np.pi*f*t+.35*np.sin(2*np.pi*f*2.76*t)*np.exp(-t/.9))+.17*np.sin(2*np.pi*f*4.12*t)*np.exp(-t/2)
    elif kind=='choir':
        env=(1-np.exp(-t/.9))*np.exp(-t/3.2)
        v=np.sin(2*np.pi*f*t+.2*np.sin(t*3.8))+.2*np.sin(2*np.pi*f*2*t)+.10*np.sin(2*np.pi*f*3*t)
    elif kind=='flute':
        env=(1-np.exp(-t/.3))*np.exp(-t/2.5)
        v=np.sin(2*np.pi*f*t+.1*np.sin(t*4.1))+.06*np.sin(2*np.pi*f*2*t)
    elif kind=='organ':
        env=(1-np.exp(-t/.65))*np.exp(-t/3.5)
        v=np.sin(2*np.pi*f*t)+.32*np.sin(2*np.pi*f*2*t)+.12*np.sin(2*np.pi*f*.5*t)
    else:
        env=(1-np.exp(-t/.08))*np.exp(-t/2.5)
        v=np.sin(2*np.pi*f*t+.65*np.sin(2*np.pi*f*1.003*t)*np.exp(-t/1.2))
    env*=np.minimum(1,(length-t)/.6).clip(0,1)
    v=v*env*level
    if reverse:v=v[::-1]
    idx=(int(start*RATE)+np.arange(n))%len(out)
    out[idx,0]+=v*np.sqrt((1-pan)*.5);out[idx,1]+=v*np.sqrt((1+pan)*.5)

def ambience(seconds,seed,kind):
    rng=np.random.default_rng(seed);n=seconds*RATE;t=np.arange(n)/RATE
    noise=rng.standard_normal(n);spec=np.fft.rfft(noise);freq=np.fft.rfftfreq(n,1/RATE)
    high={'wind':850,'room':400,'water':1800,'garden':1100}[kind]
    shape=np.exp(-(freq/high)**2)*(1-np.exp(-(freq/90)**2))
    air=np.fft.irfft(spec*shape,n=n);air/=max(np.sqrt(np.mean(air*air)),1e-9)
    env=.65+.2*np.sin(2*np.pi*t/seconds)+.12*np.sin(6*np.pi*t/seconds+.5)
    out=np.column_stack((air*env,np.roll(air,int(.013*RATE))*env))*.025
    if kind=='water':
        for i in range(9):event(out,i*seconds/9+1,86+(i%4)*3,'glass',.018,np.sin(i),2.)
    if kind=='garden':
        for i in range(4):event(out,3+i*5.7,90-i*5,'flute',.022,np.sin(i*1.4),2.8)
    return out

def finish(out,rms=.09):
    # Circular reverb with different stereo delays, no cropped note tails.
    source=out.copy()
    for seconds,gain in ((.137,.21),(.313,.16),(.677,.11),(1.17,.075),(2.31,.05)):
        out+=np.roll(source,int(seconds*RATE),axis=0)[:,::-1]*gain
    out-=out.mean(axis=0)
    out*=rms/max(np.sqrt(np.mean(out*out)),1e-12)
    out*=min(1,.61/max(np.max(np.abs(out)),1e-9))
    # Smoothly join both sides at silence; long natural pause between phrases.
    fade=min(RATE//2,len(out)//10)
    out[:fade]*=np.linspace(0,1,fade)[:,None]
    out[-fade:]*=np.linspace(1,0,fade)[:,None]
    return out

def export(name,out,description,loop=True):
    if not loop:out=finish(out,.08)
    with tempfile.TemporaryDirectory() as td:
        path=Path(td)/'sound.wav'
        with wave.open(str(path),'wb') as w:
            w.setnchannels(2);w.setsampwidth(2);w.setframerate(RATE)
            w.writeframes((np.clip(out,-.99,.99)*32767).astype('<i2').tobytes())
        dest=OUT/(name+'.ogg')
        subprocess.run(['ffmpeg','-v','error','-y','-i',str(path),'-c:a','libvorbis','-q:a','4','-metadata','COMMENT=Original synthesis; no reference recordings',str(dest)],check=True)
        dec=subprocess.check_output(['ffmpeg','-v','error','-i',str(dest),'-f','f32le','-ac','2','-ar',str(RATE),'-'])
        sig=np.frombuffer(dec,dtype='<f4').reshape(-1,2)
        metrics=dict(seconds=round(len(sig)/RATE,3),peak=float(abs(sig).max()),rms=float(np.sqrt(np.mean(sig**2))),seam=float(abs(sig[0]-sig[-1]).max()),clipped=int((abs(sig)>=.999).sum()))
        assert metrics['clipped']==0 and metrics['peak']<.9
        if loop:assert metrics['seam']<.012
        return dict(description=description,**metrics,sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),bytes=dest.stat().st_size)

# name,duration,instrument,tonic,melody(relative semitones),phrase intervals,secondary instrument.
THEMES=[
 ('station',44,'glass',57,[12,19,16,14],[2,9,18,29],'choir'),
 ('pools',54,'glass',62,[12,7,14,9,4],[3,10,21,34,46],'flute'),
 ('crater',64,'choir',38,[19,14,17,12],[5,23,38,53],'glass'),
 ('tunnels',47,'flute',55,[12,14,7,10,5,12],[2,7,17,25,34,41],'harp'),
 ('garden',58,'harp',60,[16,19,23,14,21,12,9],[2,5,13,20,31,40,49],'glass'),
 ('meadow',63,'piano',57,[12,16,19,14,9,16,11,7],[2,5,14,19,30,37,47,55],'flute'),
 ('house_meadow',52,'piano',53,[12,19,11,8],[7,20,31,44],'glass'),
 ('aquarium',59,'flute',50,[12,19,24,17,14],[4,16,27,39,51],'choir'),
 ('chess',48,'harp',58,[12,7,10,14,5,9],[1,9,14,25,33,42],'piano'),
 ('cloud_hotel',61,'glass',61,[12,16,23,19,11],[3,14,29,43,54],'choir'),
 ('road',57,'organ',45,[12,19,15,10,7],[4,15,26,40,49],'glass'),
 ('house_crater',68,'piano',55,[12,19,16,11,14,7],[3,15,28,43,55,62],'choir'),
 ('dark_fantasy',66,'organ',38,[12,19,15,10,7],[6,21,35,48,58],'choir')]

def main():
    reports={}
    for j,(name,seconds,instrument,tonic,melody,starts,secondary) in enumerate(THEMES):
        out=ambience(seconds,1600+j,'room')*.13
        for i,(m,second) in enumerate(zip(melody,starts)):
            event(out,second,tonic+m,instrument,.13,np.sin(i*1.7)*.45,10,reverse=name in ('house_meadow','aquarium') and i%3==1)
            if name in ('garden','chess','meadow') and i%2==0:
                event(out,second+1.9,tonic+m-5,instrument,.050,-np.sin(i*1.7)*.5,7)
        # Different sparse harmonic gestures, rather than the same continuous pad.
        for i in range(3):
            start=5+i*(seconds-12)/3
            event(out,start,tonic+(-2 if i==1 else 0),secondary,.050,.2,13)
            if i!=1:event(out,start+.7,tonic+7,secondary,.032,-.2,11)
        if name=='road':
            event(out,seconds*.63,tonic+31,'glass',.027,.7,9,True)
        out=finish(out,.095 if name!='crater' else .08)
        reports['music_'+name+'_16']=export('music_'+name+'_16',out,f'{instrument}, {secondary}; {seconds}s; {len(starts)} main events; tonic MIDI {tonic}')
        print(name,flush=True)
    for i,(key,kind) in enumerate([('wind','wind'),('room','room'),('water','water'),('garden','garden')]):
        signal=finish(ambience(24,1700+i,kind),.055)
        reports['air_'+key+'_16']=export('air_'+key+'_16',signal,kind)
    rng=np.random.default_rng(166)
    for j,key in enumerate(['grass','stone','tile','wood','water','chime']):
        seconds=2.8 if key=='chime' else .65
        n=int(seconds*RATE);t=np.arange(n)/RATE
        mono=rng.standard_normal(n)
        if key=='grass':mono=np.convolve(mono,np.ones(8)/8,mode='same')*np.exp(-t*18)
        elif key=='water':mono=np.convolve(mono,np.ones(12)/12,mode='same')*np.exp(-t*10)+.12*np.sin(2*np.pi*(1100*t+60*np.sin(t*20)))*np.exp(-t*16)
        elif key=='wood':mono=mono*.15*np.exp(-t*30)+np.sin(2*np.pi*150*t)*np.exp(-t*32)
        else:mono=mono*.4*np.exp(-t*32)+np.sin(2*np.pi*90*t)*np.exp(-t*38)
        sig=np.column_stack((mono,mono))
        if key=='chime':
            sig*=0;event(sig,0,86,'glass',.25,0.,2.8)
        sig*=np.minimum(t/.008,1)[:,None]
        reports['fx_'+key+'_16']=export('fx_'+key+'_16',sig,key,False)
    (ROOT/'tools/audio_validation_16.json').write_text(json.dumps(reports,indent=2)+'\n')
    print('Validated',len(reports),'decoded OGG files',flush=True)
if __name__=='__main__':main()
