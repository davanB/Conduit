package mock;

import com.conduit.libdatalink.UsbDriverInterface;
import com.conduit.libdatalink.UsbSerialListener;
import com.conduit.libdatalink.internal.Constants;
import com.conduit.libdatalink.internal.SerialPacket;

import static com.conduit.libdatalink.internal.Constants.*;

public class EchoBackMockUsbDriver implements UsbDriverInterface {

    private UsbSerialListener listener;

    @Override
    public void sendBuffer(byte[] buf) {
        // HACK: Rewrite the command id from WRITE -> READ - this mocks a round trip on the network
        if (buf[SerialPacket.INDEX_COMMAND] == COMMAND_WRITE) buf[SerialPacket.INDEX_COMMAND] = COMMAND_READ;
        listener.OnReceiveData(buf);
    }

    @Override
    public void setReadListener(UsbSerialListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

}
