import com.conduit.libdatalink.internal.NetworkPacket;
import com.conduit.libdatalink.internal.NetworkPacketParser;
import com.conduit.libdatalink.internal.Utils;
import org.junit.Test;

import static com.conduit.libdatalink.internal.Constants.*;
import static com.conduit.libdatalink.internal.SerialPacket.*;
import static org.junit.Assert.*;

public class NetworkPacketParserTest {

    @Test
    public void testBasicPacketParse(){

        byte[] payload = "Test Payload".getBytes();

        NetworkPacketParser parser = new NetworkPacketParser();
        assertFalse(parser.isPacketReady());

        // Begin receiving bytes - packet should not be ready
        parser.addBytes(new byte[]{ CONTROL_START_OF_PACKET });
        parser.addBytes(new byte[]{ COMMAND_READ });
        assertFalse(parser.isPacketReady());

        parser.addBytes(Utils.intToBytes(payload.length));
        assertFalse(parser.isPacketReady());

        parser.addBytes(payload);
        assertFalse(parser.isPacketReady());

        // Packet should only be ready once the end signal is received
        parser.addBytes(new byte[]{ CONTROL_END_OF_PACKET });
        assertTrue(parser.isPacketReady());

        // Ensure parsed data is equal
        NetworkPacket packet = parser.getPacket();
        byte[] parsedPayload = new byte[packet.getPayloadSize()];
        packet.getPacketPayload(parsedPayload);
        assertArrayEquals(payload, parsedPayload);
    }

    @Test
    public void testIncrementalBinaryPacketParse(){
        // The packet parser should handle an incremental build of packets with termination signals in the payload

        byte[] payload = { 12, CONTROL_END_OF_PACKET, 56, 26, 58, CONTROL_END_OF_PACKET };

        NetworkPacketParser parser = new NetworkPacketParser();
        assertFalse(parser.isPacketReady());

        // Begin receiving bytes - packet should not be ready
        parser.addBytes(new byte[]{ CONTROL_START_OF_PACKET });
        parser.addBytes(new byte[]{ COMMAND_READ });
        assertFalse(parser.isPacketReady());

        parser.addBytes(Utils.intToBytes(payload.length));
        assertFalse(parser.isPacketReady());

        parser.addBytes(payload);
        assertFalse(parser.isPacketReady());

        // Packet should only be ready once the end signal is received
        parser.addBytes(new byte[]{ CONTROL_END_OF_PACKET });
        assertTrue(parser.isPacketReady());
    }

    @Test
    public void testIncrementalMultiplePacketParse(){
        // The packet parser should correctly handle incremental receive mutiple consecutive packets and return them in order

        byte[] payload1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit".getBytes();
        byte[] payload2 = "Mauris lobortis posuere turpis, in volutpat lorem fringilla ac".getBytes();

        NetworkPacketParser parser = new NetworkPacketParser();

        parser.addBytes(new byte[]{ CONTROL_START_OF_PACKET });
        parser.addBytes(new byte[]{ COMMAND_READ });
        parser.addBytes(Utils.intToBytes(payload1.length));
        parser.addBytes(payload1);
        parser.addBytes(new byte[]{ CONTROL_END_OF_PACKET });
        assertTrue(parser.isPacketReady());

        parser.addBytes(new byte[]{ CONTROL_START_OF_PACKET });
        parser.addBytes(new byte[]{ COMMAND_READ });
        parser.addBytes(Utils.intToBytes(payload2.length));
        parser.addBytes(payload2);
        parser.addBytes(new byte[]{ CONTROL_END_OF_PACKET });
        assertTrue(parser.isPacketReady());

        NetworkPacket packet1 = parser.getPacket();
        byte[] parsedPayload1 = new byte[packet1.getPayloadSize()];
        packet1.getPacketPayload(parsedPayload1);
        assertArrayEquals(payload1, parsedPayload1);

        NetworkPacket packet2 = parser.getPacket();
        byte[] parsedPayload2 = new byte[packet2.getPayloadSize()];
        packet2.getPacketPayload(parsedPayload2);
        assertArrayEquals(payload2, parsedPayload2);
    }

    @Test
    public void testCompleteBinaryPacketParse(){
        // The packet parser should handle receiving an entire packet at once
        final byte[] PAYLOAD = "Hello World".getBytes();

        NetworkPacket inPacket = new NetworkPacket(COMMAND_OPEN_READING_PIPE, PAYLOAD);

        NetworkPacketParser parser = new NetworkPacketParser();
        assertFalse(parser.isPacketReady());

        // Receive all bytes at once
        parser.addBytes(inPacket.getPacketByteBuffer().array());
        assertTrue(parser.isPacketReady());

        // Validate payload
        NetworkPacket outPacket = parser.getPacket();
        byte[] parsedPayload = new byte[outPacket.getPayloadSize()];
        outPacket.getPacketPayload(parsedPayload);
        assertArrayEquals(PAYLOAD, parsedPayload);
    }

}
