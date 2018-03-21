package ca.uwaterloo.fydp.conduit.connectionutils

import ca.uwaterloo.fydp.conduit.*
import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.ConduitGroupHelper
import com.conduit.libdatalink.DataLinkInterface
import com.conduit.libdatalink.conduitabledata.ConduitGroupData
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes

class AndroidConduitGroup(dataLink: DataLinkInterface, baseAddress: Int, currentClientId: Int, groupSize: Int) : ConduitGroup(dataLink, baseAddress, currentClientId, groupSize) {
    override fun getClassForPayloadType(payloadType: Byte): ConduitableData {
        if (payloadType == ConduitableDataTypes.IMAGE.flag) {
            return ConduitImage()
        }
        if (payloadType == ConduitableDataTypes.MESSAGE.flag) {
            return ConduitMessage()
        }
        if (payloadType == ConduitableDataTypes.AUDIO.flag) {
            return ConduitAudio()
        }
        return super.getClassForPayloadType(payloadType)
    }
}