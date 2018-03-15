package ca.uwaterloo.fydp.conduit.conduitview

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.*
import ca.uwaterloo.fydp.conduit.R
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import java.security.AccessController.getContext
import kotlin.properties.Delegates
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.content.Intent.CATEGORY_OPENABLE
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.Bitmap
import ca.uwaterloo.fydp.conduit.ConduitImage
import ca.uwaterloo.fydp.conduit.ConduitMessage


class ConduitSendView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyle, defStyleRes) {

    // trigger this when the user hits send
    var sendDelegate: ((ConduitableData)->Unit)? = null
    var requestEmojiInsert: (() -> Unit)? = null
    var requestLocationDelegate: (() -> Unit)? = null
    var requestGalleryImageDelegate: (() -> Unit)? = null
    var requestCameraImageDelegate: (() -> Unit)? = null

    init{
        inflate(getContext(), R.layout.conduit_send_view, this)
        findViewById<ImageButton>(R.id.send_view_button).setOnClickListener {
            val editText = findViewById<EditText>(R.id.send_view_text)
            val textToSend = editText.text.toString()
            sendDelegate?.invoke(ConduitMessage(textToSend))
        }

        findViewById<ImageButton>(R.id.insert_emoji_button).setOnClickListener {
            requestEmojiInsert?.invoke()
        }

        findViewById<ImageButton>(R.id.send_location_button).setOnClickListener {
            requestLocationDelegate?.invoke()
        }

        findViewById<ImageButton>(R.id.send_gallery_image_button).setOnClickListener {
            requestGalleryImageDelegate?.invoke()
        }

        findViewById<ImageButton>(R.id.send_camera_image_button).setOnClickListener {
            requestCameraImageDelegate?.invoke()
        }
    }

    fun imageSelected(bitmap: Bitmap) {
        sendDelegate?.invoke(ConduitImage(bitmap))
    }


}