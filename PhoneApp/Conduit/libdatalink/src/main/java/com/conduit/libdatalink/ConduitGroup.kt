package com.conduit.libdatalink

import com.conduit.libdatalink.conduitabledata.ConduitMessage
import com.conduit.libdatalink.conduitabledata.ConduitableData
import java.nio.ByteBuffer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ConduitGroup internal constructor(private val dataLink: DataLinkInterface, val baseAddress: Int, currentClientId: Int) {

    companion object {
        val PAYLOAD_TYPE_MESSAGE: Byte = 0x01
        val PAYLOAD_TYPE_GPS_COORDS: Byte = 0x02
    }

    private val conduitableListeners: MutableMap<Byte ,((ConduitableData) -> Unit)> = HashMap()

    init {
        openInitialReadPipes(baseAddress, currentClientId)
        dataLink.setReadListener{
            originAddress, payloadType, payload ->  onDataReadListener(originAddress, payloadType, payload)
        }
    }

    fun send(clientId: Int, data: ConduitableData) {
        val address: Int = getFullAddress(baseAddress, clientId)
        dataLink.openWritingPipe(address)
        dataLink.write(PAYLOAD_TYPE_MESSAGE, data.payload.array())
    }

    fun addConduitableDataListener(payloadType: Byte, listener: ((ConduitableData) -> Unit)) {
        conduitableListeners[payloadType] = listener
    }

    // After Conduit calls us back with data
    // Figure out where the data should go, and what payloadType it should be casted to
    // then call the correct listener
    private fun onDataReadListener(origin: Int, payloadType: Byte, payload : ByteBuffer) {
        val payloadObject = getClassForPayloadType(payloadType)
        payloadObject.populateFromPayload(payload)
        payloadObject.originAddress = origin
        conduitableListeners[payloadType]?.invoke(payloadObject)
    }

    private fun getClassForPayloadType(payloadType: Byte): ConduitableData = when(payloadType) {
        PAYLOAD_TYPE_MESSAGE -> ConduitMessage()
        else -> ConduitMessage()
    }

    /**
     * openInitialReadPipes
     *
     * @param baseAddress a 3 byte base address that is shared across all users
     * @param currentClientId 1 byte that is prepended to the base address to create a unique address for the current user
     */
    private fun openInitialReadPipes(baseAddress: Int, currentClientId: Int) {

        // list of all possible clientIds
        val allClientIds:ArrayList<Int> = ArrayList()
        allClientIds += 0..5

        // filter self out
        val clientIdsToReadFrom = allClientIds.filter{ it != currentClientId }

        // pipes should be numbered from 1 to 5
        (1..5).zip(clientIdsToReadFrom).forEach{
            (pipeNumber, clientId) -> dataLink.openReadingPipe(pipeNumber.toByte(), getFullAddress(baseAddress, clientId))
        }
    }

    // should result in 4 bytes: [clientId, baseAddress] where clientId is 1 byte, and baseAddress is 3
    private fun getFullAddress(baseAddress: Int, clientId: Int) = ((0xFF and clientId) shl 3) and baseAddress

}
