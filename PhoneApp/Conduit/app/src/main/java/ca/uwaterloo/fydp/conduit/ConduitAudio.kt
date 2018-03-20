package ca.uwaterloo.fydp.conduit

import android.util.Log
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import java.nio.ByteBuffer
import kotlin.properties.Delegates


class ConduitAudio() : ConduitableData() {
    override val payloadType: ConduitableDataTypes = ConduitableDataTypes.AUDIO

    var audio: ByteArrayMediaDataSource by Delegates.notNull()

    constructor(audio: ByteArray) : this(){
        this.audio = ByteArrayMediaDataSource(audio)
    }

    override fun populateFromPayload(payload: ByteBuffer) {
        val payloadBytes = ByteArray(payload.remaining())
        payload.get(payloadBytes)
        this.audio = ByteArrayMediaDataSource(payloadBytes)
    }


    override fun getPayload(): ByteBuffer {
        Log.i("YEET", "AUDIO SIZE: " + this.audio.data.size)
        return ByteBuffer.wrap(this.audio.data)
    }
}
