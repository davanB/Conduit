package ca.uwaterloo.fydp.conduit.conduitview

import com.conduit.libdatalink.conduitabledata.ConduitableData
import kotlin.properties.Delegates

interface ConduitView {
    var data: List<ConduitableData>
    fun notifyDataReceived()
}