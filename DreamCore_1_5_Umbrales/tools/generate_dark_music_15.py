"""Original Dark Fantasy theme: slow minor organ pad and far-off spectral bells."""
from pathlib import Path
import json,hashlib
import numpy as np
from generate_world_music_14 import pad,event,choose_loop_boundary
from generate_abyss_audio import finish,export,OUTPUT,ROOT

def main():
    theme=dict(seconds=48,pad=.045,chords=[[38,45,50,53],[34,41,46,50],[31,38,46,53],[33,40,45,52]])
    signal=pad(theme,seed=15001)
    for i,(second,note) in enumerate([(2,74),(9,69),(17,77),(26,72),(35,70),(43,65)]):
        event(signal,second,note,'glass_low',.45*np.sin(i*1.9),.052,4.5)
    # An organ-like octave anchors the minor harmony, with periodic long breaths.
    seconds=48;t=np.arange(len(signal))/44100
    frequency=round(36.7081*seconds)/seconds
    organ=np.sin(2*np.pi*frequency*t)+.18*np.sin(2*np.pi*frequency*2*t)
    breath=.012*(.7+.3*np.cos(2*np.pi*t/seconds))
    signal[:,0]+=organ*breath;signal[:,1]+=organ*breath
    signal=choose_loop_boundary(finish(signal,.12))
    report=export('music_dark_fantasy_15',signal)
    report['sha256']=hashlib.sha256((OUTPUT/'music_dark_fantasy_15.ogg').read_bytes()).hexdigest()
    report['description']='Composición original: acordes menores de órgano sintético, campanas graves y reverberación. No contiene audio del video.'
    (ROOT/'tools/audio_validation_15.json').write_text(json.dumps(report,indent=2,ensure_ascii=False)+'\n')
    print(report)
if __name__=='__main__':main()
