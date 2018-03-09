package ca.uwaterloo.fydp.conduit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import java.nio.ByteBuffer
import kotlin.properties.Delegates
import ca.uwaterloo.fydp.conduit.R.mipmap.ic_launcher
import java.io.ByteArrayOutputStream


class ConduitImage() : ConduitableData() {
    override val payloadType: ConduitableDataTypes = ConduitableDataTypes.IMAGE

    var image: Bitmap by Delegates.notNull()

    constructor(image: Bitmap) : this(){
        this.image = image
    }

    override fun populateFromPayload(payload: ByteBuffer) {
        val payloadBytes = ByteArray(payload.remaining())
        payload.get(payloadBytes)
        image = BitmapFactory.decodeByteArray(payloadBytes, 0, payloadBytes.size)
    }


    override fun getPayload(): ByteBuffer {
        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        return ByteBuffer.wrap(byteArray)
    }
}
