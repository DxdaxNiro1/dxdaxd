package android.content
class Resources {fun getIdentifier(name:String,kind:String,pkg:String):Int=if(name=="fx_fall_wind36")999 else if(name.startsWith("fx_attack_"))1000+(name.hashCode() and 32767)else 0}
class Context {val resources=Resources();val packageName="com.dreamcore.game"}
