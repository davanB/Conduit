package ca.uwaterloo.fydp.conduit.puppets

import com.conduit.libdatalink.ConduitGroup

abstract class PuppetShow (val group: ConduitGroup){
    abstract fun getScript() :  List<() -> Unit>
}