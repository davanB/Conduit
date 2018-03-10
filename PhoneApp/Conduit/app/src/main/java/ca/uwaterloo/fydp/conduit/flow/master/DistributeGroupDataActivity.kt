package ca.uwaterloo.fydp.conduit.flow.master

import android.content.ClipData
import android.content.Intent
import android.media.Image
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import android.widget.Toast
import ca.uwaterloo.fydp.conduit.MainActivity
import ca.uwaterloo.fydp.conduit.R
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitLedger
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager
import ca.uwaterloo.fydp.conduit.puppets.BootstrappingConnectionEventsIncoming
import ca.uwaterloo.fydp.conduit.puppets.BootstrappingDataReceivedEventsIncoming
import ca.uwaterloo.fydp.conduit.puppets.PuppetMaster
import com.conduit.libdatalink.conduitabledata.ConduitConnectionEvent
import com.conduit.libdatalink.conduitabledata.ConduitGroupData
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import ca.uwaterloo.fydp.conduit.AppConstants
import ca.uwaterloo.fydp.conduit.conduitview.ConduitActivity
import kotlin.properties.Delegates


class DistributeGroupDataActivity : AppCompatActivity() {
    private var pendingConnectionsView: RecyclerView by Delegates.notNull()
    private var pendingConnectionsAdapter: PendingConnectionsAdapter by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distribute_group_data)

        val responsesReceived = BooleanArray(ConduitManager.getLedger().groupSize)
        responsesReceived[ConduitManager.getLedger().currentUserId] = true
        val conduitGroup = ConduitManager.getConduitGroup(ConduitManager.getLedger())

        val respoTest = findViewById<TextView>(R.id.connection_response_evts)


        pendingConnectionsView = findViewById<RecyclerView>(R.id.pending_connections)
        pendingConnectionsView.layoutManager = LinearLayoutManager(this)
        pendingConnectionsAdapter = PendingConnectionsAdapter(responsesReceived)
        pendingConnectionsView.adapter = pendingConnectionsAdapter

        conduitGroup.addConduitableDataListener(ConduitableDataTypes.CONNECTION_EVENT) { conduitableData ->
            runOnUiThread {
                val response = conduitableData as ConduitConnectionEvent
                val clientId = response.connectedClientId
                responsesReceived[clientId] = true
                pendingConnectionsAdapter.data = responsesReceived
                pendingConnectionsAdapter.notifyDataSetChanged()

                respoTest.append(ConduitManager.getLedger().getUserNameForId(clientId) + " has connected\n")

                val finished = responsesReceived.all { it}
                if (finished) {
                    nextScreen()
                }
            }
        }

        for (i in 0..ConduitManager.getLedger().groupSize) {
            val ledger: ConduitLedger = ConduitManager.getLedger()
            val groupData = ConduitGroupData(ledger.groupName, ledger.groupSize, ledger.getGroupMemberNamesList())
            conduitGroup.send(i, groupData)
        }



        // TODO: The following code is being used for debug purposes
        if(AppConstants.PUPPET_MASTER_ENABLED) {
            val puppetMaster = PuppetMaster()
            val simulateConduitResponseEvents = BootstrappingDataReceivedEventsIncoming(conduitGroup)
            puppetMaster.startShow(simulateConduitResponseEvents)
        }

    }

    private fun nextScreen() {
        val intent = Intent(this, ConduitActivity::class.java)
        startActivity(intent)
    }

    inner class PendingConnectionsAdapter(var data: BooleanArray) : RecyclerView.Adapter<PendingConnectionsAdapter.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.connection_status_list_item, parent, false))

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(ConduitManager.getLedger().getUserNameForId(position), data[position])

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var indicatorView: ImageView by Delegates.notNull()
            var nameView: TextView by Delegates.notNull()

            init {
                indicatorView = itemView.findViewById(R.id.connection_status_indicator_imageview)
                nameView = itemView.findViewById(R.id.connection_status_username)
            }

            fun bind(name: String, status: Boolean) = with(itemView) {
                nameView.text = name
                val imageId = if (status) android.R.drawable.ic_media_play else R.drawable.connect_animation
                indicatorView.setImageResource(imageId)
            }
        }
    }
}
