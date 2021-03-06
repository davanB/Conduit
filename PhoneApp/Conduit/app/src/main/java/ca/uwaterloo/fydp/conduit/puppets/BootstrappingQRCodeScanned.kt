package ca.uwaterloo.fydp.conduit.puppets

import ca.uwaterloo.fydp.conduit.flow.master.GroupData
import ca.uwaterloo.fydp.conduit.qr.HandShakeData
import com.conduit.libdatalink.ConduitGroup
import me.dm7.barcodescanner.zxing.ZXingScannerView
import com.google.zxing.Result

class BootstrappingQRCodeScanned(val qrResultHandler: ZXingScannerView.ResultHandler, group: ConduitGroup) : PuppetShow(group) {
    override fun writeScript(){
        script.clear()
        delay(5000)
        val mockHandshakeData = HandShakeData(GroupData.START_ADDRESS, 0x00000003, "Cool Friends", 6,"sneaky")
        val result = Result(mockHandshakeData.toString(), null, null, null)
        qrResultHandler.handleResult(result)
    }
}