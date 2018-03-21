package ca.uwaterloo.fydp.conduit.puppets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ca.uwaterloo.fydp.conduit.ConduitAudio
import ca.uwaterloo.fydp.conduit.ConduitImage
import ca.uwaterloo.fydp.conduit.R
import ca.uwaterloo.fydp.conduit.R.mipmap.ic_launcher
import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation
import ca.uwaterloo.fydp.conduit.ConduitMessage
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

class WhereYouAtConversation(val context: Context, group: ConduitGroup) : PuppetShow(group) {
    override fun writeScript(){
        script.clear()
        for (i in 0..10) {
            delay(1500)
            val msg0 = ConduitMessage("Hey guys when should we meet up")
            msg0.originAddress = 0x00000000
            script.add { group.conduitableListeners[ConduitableDataTypes.MESSAGE.flag]?.invoke(msg0)}

            delay(1500)
            val msglel = ConduitMessage("How about around 4pm?")
            msglel.originAddress = 0x00000004
            script.add { group.conduitableListeners[ConduitableDataTypes.MESSAGE.flag]?.invoke(msglel)}

            delay(1500)
            val msg1 = ConduitMessage("Okay cool, where should we meet?")
            msg1.originAddress = 0x00000002
            script.add { group.conduitableListeners[ConduitableDataTypes.MESSAGE.flag]?.invoke(msg1)}
            delay(1500)

            val msg2 = ConduitMessage("I know a good spot, let me drop a pin")
            msg2.originAddress = 0x00000001
            script.add { group.conduitableListeners[ConduitableDataTypes.MESSAGE.flag]?.invoke(msg2)}
            delay(3000)
            val pin = ConduitGpsLocation()
            pin.originAddress = 0x00000001
            pin.latitude = 43.472382
            pin.longitude = -80.542040
            script.add { group.conduitableListeners[ConduitableDataTypes.GPS_COORDS.flag]?.invoke(pin)}
            delay(3000)
            val pic = ConduitImage(BitmapFactory.decodeResource(context.resources,
                    R.drawable.sunset))
            pic.originAddress = 0x00000002
            script.add { group.conduitableListeners[ConduitableDataTypes.IMAGE.flag]?.invoke(pic)}
            delay(3000)
            val stream = context.assets.open("test.3gp")
            val audioBytes = stream.readBytes(4096)
            val audio = ConduitAudio(audioBytes)
            pic.originAddress = 0x00000000
            script.add { group.conduitableListeners[ConduitableDataTypes.AUDIO.flag]?.invoke(audio)}

            delay(7000)
        }
    }
}