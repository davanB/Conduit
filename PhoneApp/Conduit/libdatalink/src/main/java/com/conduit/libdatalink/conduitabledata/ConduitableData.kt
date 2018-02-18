package com.conduit.libdatalink.conduitabledata

import java.nio.ByteBuffer

abstract class ConduitableData {
    var originAddress: Int = 0
    abstract fun getPayload(): ByteBuffer
    abstract fun populateFromPayload(payload: ByteBuffer)
    abstract val payloadType: ConduitableDataTypes
}
