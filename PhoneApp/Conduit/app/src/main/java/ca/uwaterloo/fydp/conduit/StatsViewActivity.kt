package ca.uwaterloo.fydp.conduit

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager
import com.conduit.libdatalink.ConduitGroup

import kotlinx.android.synthetic.main.activity_stats_view.*
import kotlin.properties.Delegates


class StatsViewActivity : AppCompatActivity() {

    private var conduitGroup: ConduitGroup by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats_view)
        setSupportActionBar(toolbar)

        conduitGroup = ConduitManager.getConduitGroup(ConduitManager.getLedger())

        val handler = Handler()
        val r = object : Runnable {
            override fun run() {
                handler.postDelayed(this, 250)
                textViewStats.text = conduitGroup.dataLink.stats
            }
        }
        handler.postDelayed(r, 0)

    }

}
