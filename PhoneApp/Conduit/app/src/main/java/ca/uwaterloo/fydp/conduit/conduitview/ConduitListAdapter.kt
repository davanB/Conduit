package ca.uwaterloo.fydp.conduit.conduitview

import android.media.Image
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import ca.uwaterloo.fydp.conduit.ConduitAudio
import ca.uwaterloo.fydp.conduit.ConduitImage
import ca.uwaterloo.fydp.conduit.ConduitMessage
import ca.uwaterloo.fydp.conduit.R
import ca.uwaterloo.fydp.conduit.R.id.mapview
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation
import com.conduit.libdatalink.conduitabledata.ConduitableData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes
import kotlinx.android.synthetic.main.activity_map_view.*
import kotlinx.android.synthetic.main.content_main.view.*
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Created by alvin on 2018-03-08.
 */
class ConduitListAdapter(private val data: List<ConduitableData>) : RecyclerView.Adapter<ConduitListAdapter.BaseViewHolder>() {

    abstract class BaseViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val nameTextView = rootView.findViewById<TextView>(R.id.user_name)
        val dateTextView = rootView.findViewById<TextView>(R.id.time_stamp)
        val iconView = rootView.findViewById<ConduitStatusIconView>(R.id.icon_view)
        open fun bind(conduitableData: ConduitableData) {
            val userName = ConduitManager.getLedger().getUserNameForId(conduitableData.originAddress and 0x000000FF)
            nameTextView.text = userName
            dateTextView.text = "10:24 PM"
            iconView.setInformation(userName)
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
        val mapview = rootView.findViewById<MapView>(R.id.gps_coord)
        override fun bind(conduitableData: ConduitableData) {
            super.bind(conduitableData)
            val gps = (conduitableData as ConduitGpsLocation)

            mapview.setUseDataConnection(false)
            mapview.setTileSource(XYTileSource(
                    "4uMaps",
                    1,
                    15,
                    256,
                    ".png",
                    arrayOf()
            ))
            val loc = org.osmdroid.util.GeoPoint(gps.latitude, gps.longitude)
            mapview.setMaxZoomLevel(17.0)
            mapview.getController().setCenter(loc)
            mapview.getController().setZoom(16.0)

            val startMarker = Marker(mapview)
            startMarker.setPosition(loc)
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapview.getOverlays().add(startMarker)
        }
    }
    class ImageViewHolder(rootView: View) : BaseViewHolder(rootView){
        val imageView = rootView.findViewById<ImageView>(R.id.image)
        override fun bind(conduitableData: ConduitableData) {
            super.bind(conduitableData)
            imageView.setImageBitmap((conduitableData as ConduitImage).image)
        }
    }
    class AudioViewHolder(rootView: View) : BaseViewHolder(rootView){
        val audioView = rootView.findViewById<View>(R.id.audio_play)
        val audioRecord = AudioRecord()
        override fun bind(conduitableData: ConduitableData) {
            super.bind(conduitableData)
            (conduitableData as ConduitAudio)
            audioView.setOnClickListener {
                audioRecord.onPlay(true, conduitableData.audio)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ConduitListAdapter.BaseViewHolder {
        val layoutId = when (viewType) {
            ConduitableDataTypes.AUDIO.flag.toInt() -> R.layout.conduit_list_audio_view
            ConduitableDataTypes.GPS_COORDS.flag.toInt() -> R.layout.conduit_list_gps_view
            ConduitableDataTypes.IMAGE.flag.toInt() -> R.layout.conduit_list_image_view
            else -> R.layout.conduit_list_message_view
        }
        val rootView = LayoutInflater.from(parent.context)
                .inflate(layoutId, parent, false)
        val viewHolder = when(viewType) {
            ConduitableDataTypes.AUDIO.flag.toInt() -> AudioViewHolder(rootView)
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