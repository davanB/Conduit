package com.conduit.libdatalink.conduitabledata

import java.nio.ByteBuffer


class ConduitGpsLocation : ConduitableData() {
    var latiutde: Long? = null
    var longitude: Long? = null

    override fun populateFromPayload(payload: ByteBuffer) {
        longitude = payload.long
        latiutde = payload.long
    }

    override fun getPayload(): ByteBuffer? {
        // todo: replace the 8*2 with something... nicer?
        val payload = ByteBuffer.allocate(8*2)
        latiutde?.let { payload.putLong(it) }
        longitude?.let { payload.putLong(it) }
        return payload
    }
}
