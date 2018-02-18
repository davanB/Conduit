package ca.uwaterloo.fydp.conduit.puppets

import com.conduit.libdatalink.ConduitGroup

abstract class PuppetShow (val group: ConduitGroup){
    val script = ArrayList<(()->Unit)>()
    var showEndTime: Long = 0
    abstract fun writeScript()
    fun delay(time: Long) {
        script.add { Thread.sleep(time)  }
        showEndTime += time
    }
}