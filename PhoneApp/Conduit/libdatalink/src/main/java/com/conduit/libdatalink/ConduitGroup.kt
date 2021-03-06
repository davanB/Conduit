package com.conduit.libdatalink

import com.conduit.libdatalink.conduitabledata.*
import java.nio.ByteBuffer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

open class ConduitGroup constructor(dataLink: DataLinkInterface, val baseAddress: Int, val currentClientId: Int, val groupSize: Int) {
    val conduitableListeners: MutableMap<Byte ,((ConduitableData) -> Unit)> = HashMap()

    var dataLink: DataLinkInterface
        private set

    init {
        this.dataLink = dataLink
        openInitialReadPipes(baseAddress, currentClientId)
        dataLink.addReadListener(object:DataLinkListener {
            override fun OnReceiveData(originAddress: Int, payloadType: Byte, payload: ByteBuffer?) {
                onDataReadListener(originAddress, payloadType, payload)
            }

            override fun OnSerialError(commandId: Byte, payload: ByteArray?) {
            }
        })
    }

    fun sendAll(data: ConduitableData) {
        val allClientIds:ArrayList<Int> = ArrayList()
        allClientIds += 0..(groupSize-1)
        val clientIdsToSendto = allClientIds.filter{ it != currentClientId }
        clientIdsToSendto.forEach{
            send(it, data)
        }
    }

    open fun send(clientId: Int, data: ConduitableData) {
        val address: Int = ConduitGroupHelper.getFullAddress(baseAddress, clientId, currentClientId)
        dataLink.openWritingPipe(address)
//        System.out.println("YEET opening write pipe: " + ConduitGroupHelper.getFullAddress(baseAddress, clientId))

        dataLink.write(data.payloadType.flag, data.getPayload().array())
    }

    fun addConduitableDataListener(payloadType: ConduitableDataTypes, listener: ((ConduitableData) -> Unit)) {
        conduitableListeners[payloadType.flag] = listener
    }

    // After Conduit calls us back with data
    // Figure out where the data should go, and what payloadType it should be casted to
    // then call the correct listener
    private fun onDataReadListener(origin: Int, payloadType: Byte, payload : ByteBuffer?) {
        val payloadObject = getClassForPayloadType(payloadType)
        if (payload != null) {
            payloadObject.populateFromPayload(payload)
        }
        payloadObject.originAddress = origin
        conduitableListeners[payloadType]?.invoke(payloadObject)
    }

    open fun getClassForPayloadType(payloadType: Byte): ConduitableData = when(payloadType) {
        ConduitableDataTypes.GPS_COORDS.flag -> ConduitGpsLocation()
        ConduitableDataTypes.CONNECTION_EVENT.flag -> ConduitConnectionEvent()
        ConduitableDataTypes.GROUP_DATA.flag -> ConduitGroupData()
        else -> ConduitGpsLocation()
        // TODO: maybe throw an exception here for unrecognized data?
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
        allClientIds += 0..(groupSize-1)

        // filter self out
        val clientIdsToReadFrom = allClientIds.filter{ it != currentClientId }

        // pipes should be numbered from 1 to 5
        (1..(groupSize - 1)).zip(clientIdsToReadFrom).forEach{
            (pipeNumber, clientId) ->
            run {
                dataLink.openReadingPipe(pipeNumber.toByte(), ConduitGroupHelper.getFullAddress(baseAddress, currentClientId, clientId))
//                System.out.println("YEET opening read pipe: " + ConduitGroupHelper.getFullAddress(baseAddress, clientId))
            }
        }
    }


}
