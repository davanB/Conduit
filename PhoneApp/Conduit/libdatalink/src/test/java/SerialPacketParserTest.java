import com.conduit.libdatalink.internal.SerialPacket;
import com.conduit.libdatalink.internal.SerialPacketParser;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class SerialPacketParserTest {

    @Test
    public void testBasicPacketParse(){

        byte[] PAYLOAD = "Test Payload".getBytes();

        SerialPacketParser parser = new SerialPacketParser();
        assertFalse(parser.isPacketReady());

        // Begin receiving bytes - packet should not be ready
        for (int i = 0; i < SerialPacket.HEADER_SIZE; i++) parser.addBytes(new byte[]{ 2 });
        assertFalse(parser.isPacketReady());

        parser.addBytes(PAYLOAD);
        assertFalse(parser.isPacketReady());

        // Packet should only be ready once SerialPacket.PACKET_SIZE bytes are received
        for (int i = SerialPacket.HEADER_SIZE + PAYLOAD.length; i < SerialPacket.PACKET_SIZE; i++) {
            parser.addBytes(new byte[]{ 12 });
        }
        assertTrue(parser.isPacketReady());

        // Ensure parsed data is equal
        SerialPacket packet = parser.getPacket();
        byte[] parsedPayload = new byte[packet.getPayloadSize()];
        packet.getPacketPayload(parsedPayload);

        // Expect payload to match; remainder to be 12 (garbage data we added earlier)
        for (int i = 0; i < parsedPayload.length; i++) {
            if (i < PAYLOAD.length) {
                assertEquals(PAYLOAD[i], parsedPayload[i]);
            } else {
                assertEquals((byte) 12, parsedPayload[i]);
            }
        }
    }

}
