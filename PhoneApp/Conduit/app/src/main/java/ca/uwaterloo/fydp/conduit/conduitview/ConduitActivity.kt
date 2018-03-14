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
import android.content.Intent
import android.support.v4.app.ActivityCompat
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.R.attr.bitmap
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileNotFoundException
import java.io.IOException
import android.R.attr.bitmap
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import ca.uwaterloo.fydp.conduit.StatsViewActivity
import ca.uwaterloo.fydp.conduit.flow.master.QRGenerationActivity


class ConduitActivity : AppCompatActivity() {
    private val conduitDataReceived = mutableListOf<ConduitableData>()
    private var conduitGroup: ConduitGroup by Delegates.notNull()
    private var conduitStatusView: ConduitStatusView by Delegates.notNull()
    private var conduitListView: ConduitListView by Delegates.notNull()
    private var conduitSendView: ConduitSendView by Delegates.notNull()
    private var viewPager: ViewPager by Delegates.notNull()
    private val subscribedDataTypes = listOf(
            ConduitableDataTypes.MESSAGE,
            ConduitableDataTypes.GPS_COORDS,
            ConduitableDataTypes.IMAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conduit)

        conduitListView = ConduitListView(this, conduitDataReceived)
//        conduitListView.data = conduitDataReceived

        conduitStatusView = ConduitStatusView(this)
        conduitStatusView.data = conduitDataReceived

        conduitSendView = findViewById<ConduitSendView>(R.id.conduit_send_view)
        conduitSendView.sendDelegate = {conduitSend(it)}
        conduitSendView.requestGalleryImageDelegate = {requestGalleryImage()}
        conduitSendView.requestCameraImageDelegate = {requestCameraImage()}

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

        data.originAddress = ConduitManager.getLedger().currentUserId
        onConduitDataReceived(data)
    }


    val IMAGE_REQUEST_GALLERY = 1
    fun requestGalleryImage() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, IMAGE_REQUEST_GALLERY)
    }

    val IMAGE_REQUEST_CAMERA = 2
    fun requestCameraImage() {
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), IMAGE_REQUEST_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {

        if(resultCode == Activity.RESULT_OK && intentData != null){
            when(requestCode){
                IMAGE_REQUEST_CAMERA ->{
                    val bitmap = intentData.extras.get("data") as Bitmap
                    conduitSendView.imageSelected(bitmap)
                }
                IMAGE_REQUEST_GALLERY ->{
                    val stream = contentResolver.openInputStream(
                            intentData.data)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    stream!!.close()
                    conduitSendView.imageSelected(bitmap)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, intentData)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_stats) {
            val intent = Intent(this, StatsViewActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
