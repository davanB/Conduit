package com.conduit.libdatalink.conduitabledata

import com.conduit.libdatalink.internal.Constants
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.ByteBuffer
import java.util.*
import kotlin.properties.Delegates

class ConduitGroupData() : ConduitableData() {
    override val payloadType: ConduitableDataTypes = ConduitableDataTypes.GROUP_DATA

    var groupName: String by Delegates.notNull()
    var numClients: Int by Delegates.notNull()
    var clientNames: List<String> by Delegates.notNull()

    constructor(groupName: String, numClients: Int, clientNames: List<String>) : this(){
        this.groupName = groupName
        this.numClients = numClients
        this.clientNames = clientNames
    }

    override fun populateFromPayload(payload: ByteBuffer) {
        numClients = payload.int

        val payloadBytes = Arrays.copyOfRange(payload.array(), payload.position(), payload.capacity())
        val byteIn = ByteArrayInputStream(payloadBytes)
        val `in` = ObjectInputStream(byteIn)
        clientNames = `in`.readObject() as ArrayList<String>
        groupName = clientNames[0]
        clientNames = clientNames.drop(1)
    }

    override fun getPayload(): ByteBuffer {
        val numClientsBuf = ByteBuffer.allocate(4).putInt(numClients).array()

        val byteOut = ByteArrayOutputStream()
        val out = ObjectOutputStream(byteOut)
        val list:List<String> = listOf(groupName) + clientNames
        out.writeObject(list)
        val namesBuf = byteOut.toByteArray()

        val res = ByteBuffer.allocate(numClientsBuf.size + namesBuf.size)
        res.put(numClientsBuf)
        res.put(namesBuf)
        return res
    }
}