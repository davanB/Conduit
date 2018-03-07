package ca.uwaterloo.fydp.conduit.puppets

import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation
import com.conduit.libdatalink.conduitabledata.ConduitMessage
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes

class WhereYouAtConversation(group: ConduitGroup) : PuppetShow(group) {
    override fun writeScript(){
        script.clear()
        for (i in 0..10) {
            delay(1000)
            script.add { group.conduitableListeners[ConduitableDataTypes.MESSAGE.flag]?.invoke(ConduitMessage("Where are you?"))}
            delay(2000)
            script.add { group.conduitableListeners[ConduitableDataTypes.MESSAGE.flag]?.invoke(ConduitMessage("Let me drop a pin"))}
            delay(2000)
            val pin = ConduitGpsLocation()
            pin.latitude = 45.4
            pin.longitude = 45.2
            script.add { group.conduitableListeners[ConduitableDataTypes.GPS_COORDS.flag]?.invoke(pin)}
        }
    }
}