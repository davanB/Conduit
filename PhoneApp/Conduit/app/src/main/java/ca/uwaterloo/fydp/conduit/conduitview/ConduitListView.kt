package ca.uwaterloo.fydp.conduit.conduitview

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.widget.RelativeLayout
import ca.uwaterloo.fydp.conduit.R
import com.conduit.libdatalink.conduitabledata.ConduitableData
import kotlin.properties.Delegates

class ConduitListView@JvmOverloads constructor(
        context: Context,
        override var data: List<ConduitableData>,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyle, defStyleRes), ConduitView {
    var recyclerView: RecyclerView by Delegates.notNull()
    var viewAdapter: RecyclerView.Adapter<*> by Delegates.notNull()
    var viewManager: RecyclerView.LayoutManager by Delegates.notNull()

    init{
        inflate(getContext(), R.layout.conduit_list_view, this)
        viewManager = LinearLayoutManager(getContext())

        viewAdapter = ConduitListAdapter(data)
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
    }

    override fun notifyDataReceived() {
        viewAdapter.notifyDataSetChanged()
        recyclerView.smoothScrollToPosition(data.size - 1)
    }
}