package ca.uwaterloo.fydp.conduit.connectionutils

import com.conduit.libdatalink.DataLink
import com.conduit.libdatalink.UsbDriverInterface
import com.conduit.libdatalink.UsbSerialListener

/**
 * Created by Navjot on 1/17/2018.
 */

class MockUsbDriver : UsbDriverInterface {
    private var mockListener: UsbSerialListener? = null
    override fun isConnected(): Boolean = true

    override fun sendBuffer(buf: ByteArray?) {
        if( buf != null) {
            // just a rough modification to relay any write messages back to the client
            mockListener?.OnReceiveData(buf)
        }
    }

    override fun setReadListener(listener: UsbSerialListener?) {
        mockListener = listener
    }
}