package ca.uwaterloo.fydp.conduit.puppets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ca.uwaterloo.fydp.conduit.ConduitImage
import ca.uwaterloo.fydp.conduit.R
import ca.uwaterloo.fydp.conduit.R.mipmap.ic_launcher
import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation
import ca.uwaterloo.fydp.conduit.ConduitMessage
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes

class WhereYouAtConversation(val context: Context, group: ConduitGroup) : PuppetShow(group) {
    override fun writeScript(){
        script.clear()
        for (i in 0..10) {
            delay(1000)
            val msg0 = ConduitMessage("Oma wa mou Shindeiru")
            msg0.originAddress = 0x00000000
            script.add { group.conduitableListeners[ConduitableDataTypes.MESSAGE.flag]?.invoke(msg0)}

            val msglel = ConduitMessage("lmao u weeb")
            msglel.originAddress = 0x00000004
            script.add { group.conduitableListeners[ConduitableDataTypes.MESSAGE.flag]?.invoke(msglel)}

            val msg1 = ConduitMessage("Where are you?")
            msg1.originAddress = 0x00000002
            script.add { group.conduitableListeners[ConduitableDataTypes.MESSAGE.flag]?.invoke(msg1)}
            delay(2000)
            val msg2 = ConduitMessage("Let me drop a pin")
            msg2.originAddress = 0x00000001
            script.add { group.conduitableListeners[ConduitableDataTypes.MESSAGE.flag]?.invoke(msg2)}
            delay(2000)
            val pin = ConduitGpsLocation()
            pin.originAddress = 0x00000001
            pin.latitude = 45.4
            pin.longitude = 45.2
            script.add { group.conduitableListeners[ConduitableDataTypes.GPS_COORDS.flag]?.invoke(pin)}
            delay(2000)
            val pic = ConduitImage(BitmapFactory.decodeResource(context.resources,
                    R.mipmap.ic_launcher))
            pic.originAddress = 0x00000001
            script.add { group.conduitableListeners[ConduitableDataTypes.IMAGE.flag]?.invoke(pic)}
        }
    }
}