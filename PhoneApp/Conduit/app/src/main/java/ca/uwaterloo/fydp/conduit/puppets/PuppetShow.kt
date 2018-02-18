package ca.uwaterloo.fydp.conduit.puppets

import com.conduit.libdatalink.ConduitGroup

abstract class PuppetShow (val group: ConduitGroup){
    val script = ArrayList<(()->Unit)>()
    abstract fun getScript() :  List<() -> Unit>
    protected fun delay(time: Long) {
        script.add { Thread.sleep(time)  }
    }
}