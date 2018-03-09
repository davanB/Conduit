package ca.uwaterloo.fydp.conduit.conduitview

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ca.uwaterloo.fydp.conduit.R
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation
import com.conduit.libdatalink.conduitabledata.ConduitMessage
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes

/**
 * Created by alvin on 2018-03-08.
 */
class ConduitListAdapter(private val data: List<ConduitableData>) : RecyclerView.Adapter<ConduitListAdapter.BaseViewHolder>() {

    abstract class BaseViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        abstract fun bind(conduitableData: ConduitableData)
    }
    class MessageViewHolder(val rootView: View) : BaseViewHolder(rootView){
        val nameTextView = rootView.findViewById<TextView>(R.id.user_name)
        val messageTextView = rootView.findViewById<TextView>(R.id.message)

        override fun bind(conduitableData: ConduitableData) {
            messageTextView.text = (conduitableData as ConduitMessage).message
            nameTextView.text = ConduitManager.getLedger().getUserNameForId(conduitableData.originAddress and 0x000000FF) + ": "
        }
    }
    class GPSViewHolder(val rootView: View) : BaseViewHolder(rootView){
        val coord = rootView.findViewById<TextView>(R.id.gps_coord)
        val nameTextView = rootView.findViewById<TextView>(R.id.user_name)
        override fun bind(conduitableData: ConduitableData) {
            coord.text = (conduitableData as ConduitGpsLocation).latitude.toString()
            nameTextView.text = ConduitManager.getLedger().getUserNameForId(conduitableData.originAddress and 0x000000FF) + ": "
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ConduitListAdapter.BaseViewHolder {
        val layoutId = when (viewType) {
            ConduitableDataTypes.GPS_COORDS.flag.toInt() -> R.layout.conduit_list_gps_view
            else -> R.layout.conduit_list_message_view
        }
        val rootView = LayoutInflater.from(parent.context)
                .inflate(layoutId, parent, false)
        val viewHolder = when(viewType) {
            ConduitableDataTypes.GPS_COORDS.flag.toInt() -> GPSViewHolder(rootView)
            else -> MessageViewHolder(rootView)
        }
        return viewHolder
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].payloadType.flag.toInt()
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ConduitListAdapter.BaseViewHolder, position: Int) {
        holder.bind(data[position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = data.size
}