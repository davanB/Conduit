package com.conduit.libdatalink.conduitabledata

import com.conduit.libdatalink.internal.Constants
import java.nio.ByteBuffer
import java.util.*
import kotlin.properties.Delegates

class ConduitConnectionEvent() : ConduitableData() {
    override val payloadType: ConduitableDataTypes = ConduitableDataTypes.CONNECTION_EVENT

    var connectedClientId: Int by Delegates.notNull()
    var connectedClientName: String by Delegates.notNull()

    constructor(clientId: Int, clientName: String): this() {
        connectedClientId = clientId
        connectedClientName = clientName
    }

    override fun populateFromPayload(payload: ByteBuffer) {
        connectedClientId = payload.int

        val payloadBytes = Arrays.copyOfRange(payload.array(), payload.position(), payload.capacity())
        connectedClientName = String(payloadBytes)
    }

    override fun getPayload(): ByteBuffer {
        val idBuf = ByteBuffer.allocate(4).putInt(connectedClientId).array()
        val nameBuf = connectedClientName.toByteArray()

        val res = ByteBuffer.allocate(idBuf.size + nameBuf.size)
        res.put(idBuf)
        res.put(nameBuf)
        return res
    }
}