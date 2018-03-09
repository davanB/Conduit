package ca.uwaterloo.fydp.conduit.conduitview

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import ca.uwaterloo.fydp.conduit.R
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager
import ca.uwaterloo.fydp.conduit.puppets.PuppetMaster
import ca.uwaterloo.fydp.conduit.puppets.WhereYouAtConversation
import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import kotlin.properties.Delegates
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.widget.RelativeLayout


class ConduitActivity : AppCompatActivity() {
    private val conduitDataReceived = mutableListOf<ConduitableData>()
    private var conduitGroup: ConduitGroup by Delegates.notNull()
    private var conduitStatusView: ConduitStatusView by Delegates.notNull()
    private var conduitListView: ConduitListView by Delegates.notNull()
    private var conduitSendView: ConduitSendView by Delegates.notNull()
    private var viewPager: ViewPager by Delegates.notNull()
    private val subscribedDataTypes = listOf(
            ConduitableDataTypes.MESSAGE,
            ConduitableDataTypes.GPS_COORDS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conduit)

        conduitListView = ConduitListView(this)
        conduitListView.data = conduitDataReceived

        conduitStatusView = ConduitStatusView(this)
        conduitStatusView.data = conduitDataReceived

        conduitSendView = findViewById<ConduitSendView>(R.id.conduit_send_view)
        conduitSendView.sendDelegate = {conduitSend(it)}

        viewPager = findViewById(R.id.view_pager)
        val viewPagerAdapter = ViewPagerAdapter()
        viewPagerAdapter.conduitListView = this.conduitListView
        viewPagerAdapter.conduitStatusView = this.conduitStatusView
        viewPager.adapter = viewPagerAdapter

        conduitGroup = ConduitManager.getConduitGroup(ConduitManager.getLedger())

        subscribedDataTypes.forEach{
            conduitGroup.addConduitableDataListener(it, {onConduitDataReceived(it)})
        }

        val puppetMaster = PuppetMaster()
        val show = WhereYouAtConversation(this, conduitGroup)
        puppetMaster.startShow(show)
    }

    private fun onConduitDataReceived(data: ConduitableData) {
        runOnUiThread{
            conduitDataReceived.add(data)
            conduitListView.notifyDataReceived()
            conduitStatusView.notifyDataReceived()
        }
    }

    private fun conduitSend(data: ConduitableData) {
        conduitGroup.sendAll(data)
        onConduitDataReceived(data)
    }

}
