package ca.uwaterloo.fydp.conduit.puppets

import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.conduitabledata.ConduitConnectionEvent
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes

class BootstrappingConnectionEventsIncoming(group: ConduitGroup) : PuppetShow(group) {
    override fun writeScript(){
        script.clear()
        addUser(1, "Bob")
        addUser(2, "Joe")
        addUser(3, "Smith")
        addUser(4, "Fred")
        addUser(5, "Menma")
    }

    private fun addUser(clientId: Int, clientName: String) {
        delay(5000)
        val event = ConduitConnectionEvent()
        event.connectedClientId = clientId
        event.connectedClientName = clientName
        script.add { group.conduitableListeners[ConduitableDataTypes.CONNECTION_EVENT.flag]?.invoke(event)}
    }
}