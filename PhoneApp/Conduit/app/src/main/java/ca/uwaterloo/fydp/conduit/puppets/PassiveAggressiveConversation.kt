package ca.uwaterloo.fydp.conduit.puppets

import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.conduitabledata.ConduitMessage

class PassiveAggressiveConversation(group: ConduitGroup) : PuppetShow(group) {
    override fun getScript(): List<() -> Unit> {
        val script = ArrayList<(()->Unit)>()
        for (i in 0..100) {
            script.add { Thread.sleep(1000)  }
            script.add { group.conduitableListeners[ConduitGroup.PAYLOAD_TYPE_MESSAGE]?.invoke(ConduitMessage("I'm not mad..."))}
            script.add { Thread.sleep(2000)  }
            script.add { group.conduitableListeners[ConduitGroup.PAYLOAD_TYPE_MESSAGE]?.invoke(ConduitMessage("I just find it funny how..."))}
        }
        return script
    }
}