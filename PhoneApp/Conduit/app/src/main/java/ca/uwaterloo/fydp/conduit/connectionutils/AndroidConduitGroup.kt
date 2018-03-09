package ca.uwaterloo.fydp.conduit.connectionutils

import ca.uwaterloo.fydp.conduit.ConduitImage
import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.DataLinkInterface
import com.conduit.libdatalink.conduitabledata.ConduitGroupData
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes

/**
 * Created by Navjot on 3/8/2018.
 */
class AndroidConduitGroup(dataLink: DataLinkInterface, baseAddress: Int, currentClientId: Int) : ConduitGroup(dataLink, baseAddress, currentClientId) {
    override fun getClassForPayloadType(payloadType: Byte): ConduitableData {
        if (payloadType == ConduitableDataTypes.IMAGE.flag) {
            return ConduitImage()
        }
        return super.getClassForPayloadType(payloadType)
    }
}