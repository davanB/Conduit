package ca.uwaterloo.fydp.conduit.connectionutils

import android.content.Context
import android.hardware.usb.UsbManager

import com.conduit.libdatalink.DataLink

import ca.uwaterloo.fydp.conduit.UsbDriver
import com.conduit.libdatalink.DataLinkInterface
import com.conduit.libdatalink.UsbDriverInterface

class ConduitDataLink(context: Context) {
    val driver: UsbDriverInterface
    private val dataLink: DataLinkInterface
    private var genericConduitListener: ((String)->(Unit))? = null
    private val DEBUG_MODE = false

    init {
        if (DEBUG_MODE) {
            driver = MockUsbDriver()
        } else {
            val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            driver = UsbDriver(manager)
        }

        dataLink = DataLink(driver)
        dataLink.setReadListener { data -> sortAndDeliverReceiveData(data) }
    }

    fun write(data: String?) {
        if (data != null) {
            dataLink.write(data.toByteArray())
        }
    }

    private fun sortAndDeliverReceiveData(data: String) {
        genericConduitListener?.invoke(data)
    }

    fun setGenericConduitListener(genericConduitListener: ((String) -> (Unit))?) {
        this.genericConduitListener = genericConduitListener
    }

    fun openWritingPipe(addr: Int) {
        dataLink.openWritingPipe(addr)
    }

    fun openReadingPipe(pipeNumber: Int, addr: Int) {
        dataLink.openReadingPipe(pipeNumber.toByte(), addr)
    }
}
