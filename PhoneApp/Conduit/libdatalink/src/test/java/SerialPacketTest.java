import com.conduit.libdatalink.internal.Constants;
import com.conduit.libdatalink.internal.SerialPacket;
import org.junit.Test;

import static org.junit.Assert.*;

public class SerialPacketTest {
    @Test
    public void testSimplePacketCreation() {

        final byte[] PAYLOAD = "Hello World!".getBytes();
        SerialPacket packet = new SerialPacket(Constants.COMMAND_DEBUG_ECHO, (byte) 12, PAYLOAD);

        assertEquals(SerialPacket.PAYLOAD_SIZE, packet.getPayloadSize());
        assertEquals(Constants.COMMAND_DEBUG_ECHO, packet.getCommandId());
        assertEquals((byte) 12, packet.getSource());
        assertEquals(SerialPacket.HEADER_SIZE + SerialPacket.PAYLOAD_SIZE, packet.getPacketSize());

        byte[] payload = new byte[packet.getPayloadSize()];
        packet.getPacketPayload(payload);

        // Expect payload to match; remainder to be zero-padded
        for (int i = 0; i < payload.length; i++) {
            if (i < PAYLOAD.length) {
                assertEquals(PAYLOAD[i], payload[i]);
            } else {
                assertEquals((byte) 0, payload[i]);
            }
        }
    }

    @Test
    public void testImmutablePosition() {
        // Ensure the underlying ByteBuffer position does not change with packet operations
        final byte[] PAYLOAD = "Hello World!".getBytes();
        SerialPacket packet = new SerialPacket(Constants.COMMAND_DEBUG_ECHO, PAYLOAD);

        int oldPos = packet.getPacketByteBuffer().position();

        // Read payload
        byte[] payload = new byte[packet.getPayloadSize()];
        packet.getPacketPayload(payload);
        int newPos = packet.getPacketByteBuffer().position();
        assertEquals(oldPos, newPos);

        packet.getPayloadSize();
        newPos = packet.getPacketByteBuffer().position();
        assertEquals(oldPos, newPos);

        packet.getCommandId();
        newPos = packet.getPacketByteBuffer().position();
        assertEquals(oldPos, newPos);
    }
}
