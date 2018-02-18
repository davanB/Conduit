package ca.uwaterloo.fydp.conduit.puppets

import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.conduitabledata.ConduitMessage
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes

class PassiveAggressiveConversation(group: ConduitGroup) : PuppetShow(group) {
    override fun writeScript(){
        script.clear()
        for (i in 0..100) {
            delay(1000)
            script.add { group.conduitableListeners[ConduitableDataTypes.MESSAGE.flag]?.invoke(ConduitMessage("I'm not mad..."))}
            delay(2000)
            script.add { group.conduitableListeners[ConduitableDataTypes.MESSAGE.flag]?.invoke(ConduitMessage("I just find it funny how..."))}
        }
    }
}