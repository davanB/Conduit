package mock;

import com.conduit.libdatalink.UsbDriverInterface;
import com.conduit.libdatalink.UsbSerialListener;

public class EchoBackMockUsbDriver implements UsbDriverInterface {

    private UsbSerialListener listener;

    @Override
    public void sendBuffer(byte[] buf) {
        listener.OnReceiveData(buf);
    }

    @Override
    public void setReadListener(UsbSerialListener listener) {
        this.listener = listener;
    }

}
