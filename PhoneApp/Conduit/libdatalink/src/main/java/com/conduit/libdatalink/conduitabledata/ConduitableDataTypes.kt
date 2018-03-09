package com.conduit.libdatalink.conduitabledata

enum class ConduitableDataTypes(val flag: Byte) {
    MESSAGE(0x01),
    GPS_COORDS(0x02),
    CONNECTION_EVENT(0x03),
    GROUP_DATA(0x04),
    IMAGE(0x05)
}

