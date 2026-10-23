package com.dreamcore.game
import android.opengl.GLES20 as G
fun main(){
 var checks=0;fun verify(v:Boolean,label:String){check(v){label};checks++}
 fun clean(){verify(G.liveTextures.isEmpty()&&G.liveFrames.isEmpty(),"all probe handles deleted");verify(G.restored&&G.depthClear==1f&&G.currentProgram==0,"default render state restored")}
 G.reset();val full=DreamGl29.probe({_,_->77},true);verify(full.color&&full.depth,"healthy color and actual depth sample");verify(G.depthDraws==1,"depth sampled only after detach");clean()
 G.reset();G.corruptColor=true;val green=DreamGl29.probe({_,_->77},true);verify(!green.color&&!green.depth,"green channel mismatch rejected");verify(G.depthDraws==0,"invalid color target not reused");clean()
 G.reset();G.depthSupported=false;val missing=DreamGl29.probe({_,_->77},true);verify(missing.color&&!missing.depth,"advertised but unusable UINT depth rejected");clean()
 G.reset();G.corruptDepth=true;val bad=DreamGl29.probe({_,_->77},true);verify(bad.color&&!bad.depth,"incorrect depth value rejected");clean()
 G.reset();val stable=DreamGl29.probe({_,_->error("must not compile")},false);verify(stable.color&&!stable.depth&&G.depthDraws==0,"stable driver skips optional depth reads");clean()
 G.reset();val failed=DreamGl29.probe({_,_->error("synthetic driver compilation error")},true);verify(failed.color&&!failed.depth,"shader rejection preserves color path");clean()
 G.extensions="GL_OES_depth_texture_other GL_OES_depth24";verify(!DreamGl29.capabilities().depthTexture,"extension matching is token exact")
 G.extensions="GL_OES_depth_texture GL_OES_depth24";val c=DreamGl29.capabilities();verify(c.depthTexture&&c.textureUnits==8&&c.viewportHeight==4096,"real capability query fields")
 println("PASS $checks runtime capability checks")
}
