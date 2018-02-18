package com.conduit.libdatalink.conduitabledata

import java.nio.ByteBuffer


class ConduitMessage() : ConduitableData() {
    var message: String? = null

    constructor(message: String?) : this(){
        this.message = message
    }

    override fun populateFromPayload(payload: ByteBuffer) {
        val payloadBytes = payload.array()
        message = String(payloadBytes)
    }

    override fun getPayload(): ByteBuffer? {
        return ByteBuffer.wrap(message?.toByteArray())
    }
}
