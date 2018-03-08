package ca.uwaterloo.fydp.conduit.conduitview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import ca.uwaterloo.fydp.conduit.R
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation
import com.conduit.libdatalink.conduitabledata.ConduitMessage
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import java.security.AccessController.getContext
import kotlin.properties.Delegates

class ConduitSendView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyle, defStyleRes) {

    // trigger this when the user hits send
    var sendDelegate: ((ConduitableData)->Unit)? = null

    init{
        inflate(getContext(), R.layout.conduit_send_view, this)
        findViewById<Button>(R.id.send_view_button).setOnClickListener {
            val editText = findViewById<EditText>(R.id.send_view_text)
            val textToSend = editText.text.toString()
            sendDelegate?.invoke(ConduitMessage(textToSend))
        }

    }

}