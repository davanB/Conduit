package com.conduit.libdatalink.conduitabledata

import com.conduit.libdatalink.internal.Constants
import java.nio.ByteBuffer
import kotlin.properties.Delegates


class ConduitMessage() : ConduitableData() {
    override val payloadType: ConduitableDataTypes = ConduitableDataTypes.MESSAGE

    var message: String by Delegates.notNull()

    constructor(message: String) : this(){
        this.message = message
    }

    override fun populateFromPayload(payload: ByteBuffer) {
        val payloadBytes = payload.array()
        message = String(payloadBytes)
    }

    override fun getPayload(): ByteBuffer {
        return ByteBuffer.wrap(message.toByteArray())
    }
}
