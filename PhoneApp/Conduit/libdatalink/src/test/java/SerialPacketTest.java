import com.conduit.libdatalink.internal.SerialPacket;
import com.conduit.libdatalink.internal.SerialPacketParser;
import com.conduit.libdatalink.internal.Utils;
import org.junit.Test;

import static com.conduit.libdatalink.internal.Constants.*;
import static org.junit.Assert.*;

public class SerialPacketTest {

    @Test
    public void testSimplePacketCreation() {
        SerialPacket packet = new SerialPacket(COMMAND_DEBUG_ECHO, new byte[] {42});
        assertEquals(1, packet.getPayloadSize());
        assertEquals(COMMAND_DEBUG_ECHO, packet.getCommandId());
        assertEquals(SerialPacket.HEADER_SIZE + SerialPacket.FOOTER_SIZE + packet.getPayloadSize(), packet.getPacketSize());

        byte[] payload = new byte[packet.getPayloadSize()];
        packet.getPacketPayload(payload);

        assertEquals((byte) 42, payload[0]);
    }

    @Test
    public void testImmutablePosition() {
        // Ensure the underlying ByteBuffer position does not change with packet operations
        SerialPacket packet = new SerialPacket(COMMAND_DEBUG_ECHO, new byte[] {24, 32, 12});

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
