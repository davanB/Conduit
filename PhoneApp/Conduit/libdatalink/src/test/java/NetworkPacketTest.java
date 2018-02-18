import com.conduit.libdatalink.internal.NetworkPacket;
import org.junit.Test;

import java.nio.ByteBuffer;

import static com.conduit.libdatalink.internal.Constants.*;
import static org.junit.Assert.*;

public class NetworkPacketTest {

    @Test
    public void testSimplePacketCreation() {
        NetworkPacket packet = new NetworkPacket(COMMAND_DEBUG_ECHO, new byte[] {42});
        assertEquals(1, packet.getPayloadSize());
        assertEquals(COMMAND_DEBUG_ECHO, packet.getPayloadType());
        assertEquals(NetworkPacket.HEADER_SIZE + NetworkPacket.FOOTER_SIZE + packet.getPayloadSize(), packet.getPacketSize());

        // Test payload via ByteBuffer
        ByteBuffer payloadBuffer = packet.getPacketPayload();
        assertEquals((byte) 42, payloadBuffer.get());

        // Test payload via byte[]
        byte[] payload = new byte[packet.getPayloadSize()];
        packet.getPacketPayload(payload);

        assertEquals((byte) 42, payload[0]);
    }

    @Test
    public void testImmutablePosition() {
        // Ensure the underlying ByteBuffer position does not change with packet operations
        NetworkPacket packet = new NetworkPacket(COMMAND_DEBUG_ECHO, new byte[] {24, 32, 12});

        int oldPos = packet.getPacketByteBuffer().position();

        // Read payload
        byte[] payload = new byte[packet.getPayloadSize()];
        packet.getPacketPayload(payload);
        int newPos = packet.getPacketByteBuffer().position();
        assertEquals(oldPos, newPos);

        packet.getPayloadSize();
        newPos = packet.getPacketByteBuffer().position();
        assertEquals(oldPos, newPos);

        packet.getPayloadType();
        newPos = packet.getPacketByteBuffer().position();
        assertEquals(oldPos, newPos);
    }
}
