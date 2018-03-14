package ca.uwaterloo.fydp.conduit.conduitview

import android.content.Context
import android.util.AttributeSet
import android.widget.*
import ca.uwaterloo.fydp.conduit.R
import ca.uwaterloo.fydp.conduit.R.id.icon_view_name
import kotlinx.android.synthetic.main.conduit_status_icon_view.view.*

class ConduitStatusIconView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyle, defStyleRes) {

    init{
        inflate(getContext(), R.layout.conduit_status_icon_view, this)
    }

    fun setInformation(name: String) {
        icon_view_name.text = name.substring(0..0)
    }



}