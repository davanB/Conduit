package ca.uwaterloo.fydp.conduit.puppets

import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.conduitabledata.ConduitConnectionEvent
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes

class BootstrappingConnectionEventsIncoming(group: ConduitGroup) : PuppetShow(group) {
    override fun writeScript(){
        script.clear()
        addUser(1, "Aaron")
        addUser(2, "Alvin")
        addUser(3, "Navjot")
        addUser(4, "Shadmaan")
        addUser(5, "Davan")
    }

    private fun addUser(clientId: Int, clientName: String) {
        delay(2000)
        val event = ConduitConnectionEvent()
        event.connectedClientId = clientId
        event.connectedClientName = clientName
        script.add { group.conduitableListeners[ConduitableDataTypes.CONNECTION_EVENT.flag]?.invoke(event)}
    }
}