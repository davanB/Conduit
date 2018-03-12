package mock;

import com.conduit.libdatalink.UsbDriverInterface;
import com.conduit.libdatalink.UsbSerialListener;
import com.conduit.libdatalink.internal.Constants;
import com.conduit.libdatalink.internal.SerialPacket;

import java.util.ArrayList;
import java.util.List;

import static com.conduit.libdatalink.internal.SerialPacket.*;

public class EchoBackMockUsbDriver implements UsbDriverInterface {

    private UsbSerialListener listener;
    private List<Byte> pipes = new ArrayList<Byte>();

    @Override
    public void sendBuffer(byte[] buf) {
        // HACK: Rewrite the command id from WRITE -> READ - this mocks a round trip on the network
        if (buf[SerialPacket.INDEX_COMMAND] == COMMAND_WRITE) {
            buf[SerialPacket.INDEX_COMMAND] = COMMAND_READ;
            // Send a successful operation ACK packet
            SerialPacket packetSuccess = new SerialPacket(COMMAND_WRITE, STATUS_SUCCESS, (byte)0, new byte[32]);
            listener.OnReceiveData(packetSuccess.getPacketByteBuffer().array());
        }

        // HACK: Save opened pipe addresses and replay them to mock a round trip
        if (buf[SerialPacket.INDEX_COMMAND] == COMMAND_OPEN_READING_PIPE) pipes.add(buf[SerialPacket.INDEX_PAYLOAD + 4]);
        if (buf[SerialPacket.INDEX_COMMAND] == COMMAND_READ && pipes.size() > 0) buf[SerialPacket.INDEX_SOURCE] = pipes.remove(0);

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
