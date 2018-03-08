package com.conduit.libdatalink

import com.conduit.libdatalink.conduitabledata.*
import org.omg.CORBA.Object
import java.nio.ByteBuffer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ConduitGroup internal constructor(private val dataLink: DataLinkInterface, val baseAddress: Int, val currentClientId: Int) {
    val conduitableListeners: MutableMap<Byte ,((ConduitableData) -> Unit)> = HashMap()

    init {
        openInitialReadPipes(baseAddress, currentClientId)
        dataLink.addReadListener(object:DataLinkListener {
            override fun OnReceiveData(originAddress: Int, payloadType: Byte, payload: ByteBuffer?) {
                onDataReadListener(originAddress, payloadType, payload)
            }

            override fun OnSerialError(commandId: Byte, payload: ByteArray?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    fun sendAll(data: ConduitableData) {
        val allClientIds:ArrayList<Int> = ArrayList()
        allClientIds += 0..5
        val clientIdsToSendto = allClientIds.filter{ it != currentClientId }
        clientIdsToSendto.forEach{
            send(it, data)
        }
    }

    fun send(clientId: Int, data: ConduitableData) {
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

    private fun getClassForPayloadType(payloadType: Byte): ConduitableData = when(payloadType) {
        ConduitableDataTypes.MESSAGE.flag -> ConduitMessage()
        ConduitableDataTypes.GPS_COORDS.flag -> ConduitGpsLocation()
        ConduitableDataTypes.CONNECTION_EVENT.flag -> ConduitConnectionEvent()
        ConduitableDataTypes.GROUP_DATA.flag -> ConduitGroupData()
        else -> ConduitMessage()
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
        allClientIds += 0..5

        // filter self out
        val clientIdsToReadFrom = allClientIds.filter{ it != currentClientId }

        // pipes should be numbered from 1 to 5
        (1..5).zip(clientIdsToReadFrom).forEach{
            (pipeNumber, clientId) ->
            run {
                dataLink.openReadingPipe(pipeNumber.toByte(), ConduitGroupHelper.getFullAddress(baseAddress, currentClientId, clientId))
//                System.out.println("YEET opening read pipe: " + ConduitGroupHelper.getFullAddress(baseAddress, clientId))
            }
        }
    }


}
