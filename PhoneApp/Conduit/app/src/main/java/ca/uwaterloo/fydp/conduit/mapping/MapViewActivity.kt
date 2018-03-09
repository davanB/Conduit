package ca.uwaterloo.fydp.conduit.mapping

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import ca.uwaterloo.fydp.conduit.R

import kotlinx.android.synthetic.main.activity_map_view.*
import org.osmdroid.tileprovider.tilesource.XYTileSource



class MapViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_view)
        setSupportActionBar(toolbar)

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }

        mapview.setUseDataConnection(false)
        mapview.setTileSource(XYTileSource(
                "4uMaps",
                1,
                15,
                256,
                ".png",
                arrayOf()
        ))
        mapview.setMaxZoomLevel(17.0)
        mapview.getController().setCenter(org.osmdroid.util.GeoPoint(43.470113, -80.530086))
        mapview.getController().setZoom(13.0)

    }

}
