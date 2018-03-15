package ca.uwaterloo.fydp.conduit.conduitview

import android.media.Image
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.uwaterloo.fydp.conduit.ConduitImage
import ca.uwaterloo.fydp.conduit.ConduitMessage
import ca.uwaterloo.fydp.conduit.R
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes

/**
 * Created by alvin on 2018-03-08.
 */
class ConduitListAdapter(private val data: List<ConduitableData>) : RecyclerView.Adapter<ConduitListAdapter.BaseViewHolder>() {

    abstract class BaseViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val nameTextView = rootView.findViewById<TextView>(R.id.user_name)
        val dateTextView = rootView.findViewById<TextView>(R.id.time_stamp)
        open fun bind(conduitableData: ConduitableData) {
            nameTextView.text = ConduitManager.getLedger().getUserNameForId(conduitableData.originAddress and 0x000000FF)
            dateTextView.text = "10:24 PM"
        }
    }
    class MessageViewHolder(val rootView: View) : BaseViewHolder(rootView){
        val messageTextView = rootView.findViewById<TextView>(R.id.message)

        override fun bind(conduitableData: ConduitableData) {
            super.bind(conduitableData)
            messageTextView.text = (conduitableData as ConduitMessage).message
        }
    }
    class GPSViewHolder(val rootView: View) : BaseViewHolder(rootView){
        val coord = rootView.findViewById<TextView>(R.id.gps_coord)
        override fun bind(conduitableData: ConduitableData) {
            super.bind(conduitableData)
            val gps = (conduitableData as ConduitGpsLocation)
            coord.text = gps.latitude.toString() + ", " + gps.longitude
        }
    }
    class ImageViewHolder(rootView: View) : BaseViewHolder(rootView){
        val imageView = rootView.findViewById<ImageView>(R.id.image)
        override fun bind(conduitableData: ConduitableData) {
            super.bind(conduitableData)
            imageView.setImageBitmap((conduitableData as ConduitImage).image)
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ConduitListAdapter.BaseViewHolder {
        val layoutId = when (viewType) {
            ConduitableDataTypes.GPS_COORDS.flag.toInt() -> R.layout.conduit_list_gps_view
            ConduitableDataTypes.IMAGE.flag.toInt() -> R.layout.conduit_list_image_view
            else -> R.layout.conduit_list_message_view
        }
        val rootView = LayoutInflater.from(parent.context)
                .inflate(layoutId, parent, false)
        val viewHolder = when(viewType) {
            ConduitableDataTypes.GPS_COORDS.flag.toInt() -> GPSViewHolder(rootView)
            ConduitableDataTypes.IMAGE.flag.toInt() -> ImageViewHolder(rootView)
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