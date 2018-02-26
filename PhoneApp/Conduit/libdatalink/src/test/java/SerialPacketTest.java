import com.conduit.libdatalink.internal.Constants;
import com.conduit.libdatalink.internal.SerialPacket;
import org.junit.Test;

import java.nio.ByteBuffer;

import static com.conduit.libdatalink.internal.SerialPacket.*;
import static org.junit.Assert.*;

public class SerialPacketTest {
    @Test
    public void testSimplePacketCreation() {

        final byte[] PAYLOAD = "Hello World!".getBytes();
        SerialPacket packet = new SerialPacket(COMMAND_DEBUG_ECHO, STATUS_SUCCESS, (byte) 12, PAYLOAD);

        assertEquals(SerialPacket.PAYLOAD_SIZE, packet.getPayloadSize());
        assertEquals(COMMAND_DEBUG_ECHO, packet.getCommandId());
        assertEquals(STATUS_SUCCESS, packet.getStatus());
        assertEquals((byte) 12, packet.getSource());
        assertEquals(SerialPacket.HEADER_SIZE + SerialPacket.PAYLOAD_SIZE, packet.getPacketSize());

        // Test payload via ByteBuffer
        ByteBuffer payloadBuffer = packet.getPacketPayload();

        byte[] payload = new byte[packet.getPayloadSize()];
        packet.getPacketPayload(payload);

        // Expect payload to match; remainder to be zero-padded
        for (int i = 0; i < payload.length; i++) {
            if (i < PAYLOAD.length) {
                assertEquals(PAYLOAD[i], payload[i]);
                assertEquals(PAYLOAD[i], payloadBuffer.get());
            } else {
                assertEquals((byte) 0, payload[i]);
            }
        }
    }

    @Test
    public void testImmutablePosition() {
        // Ensure the underlying ByteBuffer position does not change with packet operations
        final byte[] PAYLOAD = "Hello World!".getBytes();
        SerialPacket packet = new SerialPacket(COMMAND_DEBUG_ECHO, PAYLOAD);

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
