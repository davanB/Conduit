package ca.uwaterloo.fydp.conduit

import android.provider.ContactsContract
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import java.nio.ByteBuffer
import kotlin.properties.Delegates


class ConduitMessage() : ConduitableData() {
    override val payloadType: ConduitableDataTypes = ConduitableDataTypes.MESSAGE

    var message: String by Delegates.notNull()

    constructor(message: String) : this(){
        this.message = message
    }

    override fun populateFromPayload(payload: ByteBuffer) {
        val payloadBytes = ByteArray(payload.remaining())
        payload.get(payloadBytes)
        if(AppConstants.TRANSFORMATIONS_ENABLED) {
            message = String(DataTransformation.decompressAndDecrypt(payloadBytes))
        } else {
            message = String(payloadBytes)
        }
    }

    override fun getPayload(): ByteBuffer {
        if(AppConstants.TRANSFORMATIONS_ENABLED) {
            return ByteBuffer.wrap(DataTransformation.compressAndEncrypt(message))
        }
        return ByteBuffer.wrap(message.toByteArray())
    }
}
