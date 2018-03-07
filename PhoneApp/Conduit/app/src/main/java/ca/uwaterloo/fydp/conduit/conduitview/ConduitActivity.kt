package ca.uwaterloo.fydp.conduit.conduitview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import ca.uwaterloo.fydp.conduit.R
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager
import ca.uwaterloo.fydp.conduit.puppets.PuppetMaster
import ca.uwaterloo.fydp.conduit.puppets.WhereYouAtConversation
import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import kotlin.properties.Delegates

class ConduitActivity : AppCompatActivity() {
    private val conduitDataReceived = mutableListOf<ConduitableData>()
    private var conduitGroup: ConduitGroup by Delegates.notNull()
    private var conduitStatusView: ConduitView by Delegates.notNull()
    private var conduitListView: ConduitView by Delegates.notNull()
    private var conduitSendView: ConduitSendView by Delegates.notNull()
    private val subscribedDataTypes = listOf(
            ConduitableDataTypes.MESSAGE,
            ConduitableDataTypes.GPS_COORDS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conduit)

        conduitListView = findViewById<ConduitListView>(R.id.conduit_list_view)
        conduitListView.data = conduitDataReceived

        conduitStatusView = findViewById<ConduitStatusView>(R.id.conduit_status_view)
        conduitStatusView.data = conduitDataReceived

        conduitSendView = findViewById<ConduitSendView>(R.id.conduit_send_view)
        conduitSendView.sendDelegate = {conduitSend(it)}

        conduitGroup = ConduitManager.getConduitGroup(ConduitManager.getLedger())

        subscribedDataTypes.forEach{
            conduitGroup.addConduitableDataListener(it, {onConduitDataReceived(it)})
        }

        val puppetMaster = PuppetMaster()
        val show = WhereYouAtConversation(conduitGroup)
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
        conduitGroup.send(0, data)
        onConduitDataReceived(data)
    }


}
