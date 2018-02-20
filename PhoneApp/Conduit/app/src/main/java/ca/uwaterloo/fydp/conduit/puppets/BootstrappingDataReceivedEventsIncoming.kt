package ca.uwaterloo.fydp.conduit.puppets

import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.conduitabledata.ConduitConnectionEvent
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes

class BootstrappingDataReceivedEventsIncoming(group: ConduitGroup) : PuppetShow(group) {
    override fun writeScript(){
        script.clear()
        addUser(1, "")
        addUser(2, "")
        addUser(3, "")
        addUser(4, "")
        addUser(5, "")
    }

    private fun addUser(clientId: Int, clientName: String) {
        delay((2000* Math.random() + 5000).toLong())
        val event = ConduitConnectionEvent()
        event.connectedClientId = clientId
        event.connectedClientName = clientName
        script.add { group.conduitableListeners[ConduitableDataTypes.CONNECTION_EVENT.flag]?.invoke(event)}
    }
}