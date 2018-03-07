package ca.uwaterloo.fydp.conduit.conduitview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import ca.uwaterloo.fydp.conduit.R
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation
import com.conduit.libdatalink.conduitabledata.ConduitMessage
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import java.security.AccessController.getContext
import kotlin.properties.Delegates

class ConduitStatusView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyle, defStyleRes), ConduitView {
    override var data: List<ConduitableData> by Delegates.notNull()

    var textView: TextView by Delegates.notNull()
    init{
        inflate(getContext(), R.layout.conduit_list_view, this)
        textView = findViewById<TextView>(R.id.textview_test)
    }

    override fun notifyDataReceived() {
        val sb = StringBuilder()
        data.forEach{
            when(it.payloadType) {
                ConduitableDataTypes.MESSAGE -> sb.append((it as ConduitMessage).message)
                ConduitableDataTypes.GPS_COORDS -> {
                    it as ConduitGpsLocation
                    sb.append(it.latitude)
                    sb.append(it.longitude)
                }
                else -> {}
            }
            sb.append("\n")
        }
        textView.text = sb.toString()
    }
}