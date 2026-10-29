package android.os
class Looper {companion object{fun getMainLooper()=Looper()}}
class Handler(looper:Looper){fun postDelayed(f:()->Unit,delay:Long){};fun removeCallbacksAndMessages(token:Any?){} }
