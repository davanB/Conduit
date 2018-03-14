package ca.uwaterloo.fydp.conduit.conduitview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import ca.uwaterloo.fydp.conduit.ConduitImage
import ca.uwaterloo.fydp.conduit.R
import ca.uwaterloo.fydp.conduit.R.attr.direction
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation
import com.conduit.libdatalink.conduitabledata.ConduitMessage
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import kotlinx.android.synthetic.main.conduit_status_message_view.view.*



class ConduitStatusMessageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyle, defStyleRes) {

    init{
        var direction = 0
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ConduitStatusMessageView,
                0, 0)

        try {
            direction = a.getInteger(R.styleable.ConduitStatusMessageView_direction, 0)
        } finally {
            a.recycle()
        }
        val layoutId = when(direction){
            1 -> R.layout.conduit_status_message_view_left
            else -> R.layout.conduit_status_message_view
        }
        inflate(getContext(), layoutId, this)

//        message.visibility = View.GONE
//        message_image.visibility = View.GONE
    }

    var data: ConduitableData? = null
        set(value){
            displayData(value)
        }

    private fun displayData(value: ConduitableData?) {
        //this.visibility = View.VISIBLE
        when(value?.payloadType){
            ConduitableDataTypes.MESSAGE -> {
                value as ConduitMessage
                message.text = value.message
                message.visibility = View.VISIBLE
                message_image.visibility = View.GONE
            }
            ConduitableDataTypes.GPS_COORDS -> {
                value as ConduitGpsLocation
                message.text = "GPS " + value.latitude + ", " + value.longitude
                message.visibility = View.VISIBLE
                message_image.visibility = View.GONE
            }
            ConduitableDataTypes.IMAGE -> {
                value as ConduitImage
                message_image.setImageBitmap(value.image)
                message.visibility = View.GONE
                message_image.visibility = View.VISIBLE
            }
            null -> {
                //this.visibility = View.INVISIBLE
            }
            else ->{

            }
        }
    }
}