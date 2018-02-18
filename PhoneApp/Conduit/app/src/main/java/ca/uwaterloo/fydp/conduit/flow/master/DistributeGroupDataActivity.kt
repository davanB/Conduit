package ca.uwaterloo.fydp.conduit.flow.master

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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

class DistributeGroupDataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distribute_group_data)

        val responsesReceived = BooleanArray(ConduitManager.getLedger().groupSize)
        responsesReceived[ConduitManager.getLedger().currentUserId] = true
        val conduitGroup = ConduitManager.getConduitGroup(ConduitManager.getLedger())

        val respoTest = findViewById<TextView>(R.id.connection_response_evts)
        conduitGroup.addConduitableDataListener(ConduitableDataTypes.CONNECTION_EVENT) { conduitableData ->
            runOnUiThread {
                val response = conduitableData as ConduitConnectionEvent
                val clientId = response.connectedClientId
                responsesReceived[clientId] = true

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
        val puppetMaster = PuppetMaster()
        val simulateConduitResponseEvents = BootstrappingDataReceivedEventsIncoming(conduitGroup)
        puppetMaster.startShow(simulateConduitResponseEvents)

    }

    private fun nextScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
