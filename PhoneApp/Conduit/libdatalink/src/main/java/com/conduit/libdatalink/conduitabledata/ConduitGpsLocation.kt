package com.conduit.libdatalink.conduitabledata

import com.conduit.libdatalink.internal.Constants
import java.nio.ByteBuffer
import kotlin.properties.Delegates


class ConduitGpsLocation() : ConduitableData() {
    override val payloadType: ConduitableDataTypes = ConduitableDataTypes.GPS_COORDS

    var latitude: Double by Delegates.notNull()
    var longitude: Double by Delegates.notNull()

    constructor(latitude: Double, longitude: Double) : this(){
        this.latitude = latitude
        this.longitude = longitude
    }

    override fun populateFromPayload(payload: ByteBuffer) {
        latitude = payload.double
        longitude = payload.double
    }

    override fun getPayload(): ByteBuffer {
        val payload = ByteBuffer.allocate(8*2)
        payload.putDouble(latitude)
        payload.putDouble(longitude)
        return payload
    }
}
