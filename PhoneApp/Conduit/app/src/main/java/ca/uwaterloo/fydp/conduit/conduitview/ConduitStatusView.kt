package ca.uwaterloo.fydp.conduit.conduitview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import ca.uwaterloo.fydp.conduit.R
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation
import com.conduit.libdatalink.conduitabledata.ConduitMessage
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import kotlinx.android.synthetic.main.conduit_status_view.view.*
import kotlinx.android.synthetic.main.content_main.view.*
import java.security.AccessController.getContext
import kotlin.properties.Delegates

class ConduitStatusView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyle, defStyleRes), ConduitView {
    override var data: List<ConduitableData> by Delegates.notNull()

    //var textView: TextView by Delegates.notNull()
    private var idToMessageView: (List<Pair<Int, ConduitStatusMessageView>>) by Delegates.notNull()
    init{
        inflate(getContext(), R.layout.conduit_status_view, this)
        val iconViews = listOf(
                status_icon_top,
                status_icon_right,
                status_icon_bottom,
                status_icon_left
        )

        val messageViews = listOf(
                status_message_top,
                status_message_right,
                status_message_bottom,
                status_message_left
        )

        val ledger = ConduitManager.getLedger()
        iconViews.zip(ledger.getGroupMemberNamesList().filter{it != ledger.currentUserName}).forEach{
            (view, name) -> view.setInformation(name)
        }

        idToMessageView = (0..4).filter { it != ledger.currentUserId }.zip(messageViews)
    }

    override fun notifyDataReceived() {
        idToMessageView.forEach{
            (id, view) -> view.data = data.findLast { it.originAddress == id }
        }
    }
}