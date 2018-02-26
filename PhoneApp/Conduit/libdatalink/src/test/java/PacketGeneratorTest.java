import com.conduit.libdatalink.internal.*;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import static com.conduit.libdatalink.internal.SerialPacket.*;
import static org.junit.Assert.*;

public class PacketGeneratorTest {
    @Test
    public void testSerialPacketGeneration() {
        // Ensure that the entire NetworkPacket is correctly split into SerialPackets

        byte[] PAYLOAD = new byte[64];
        new Random().nextBytes(PAYLOAD);

        NetworkPacket networkPacket = new NetworkPacket(COMMAND_READ, PAYLOAD);
        ByteBuffer networkPacketBuff = networkPacket.getPacketByteBuffer().duplicate();
        networkPacketBuff.rewind();

        List<SerialPacket> serialPackets = PacketGenerator.generateSerialPackets(COMMAND_WRITE, networkPacket);

        // Ensure we have generated the correct number of SerialPackets
        int expectedPackets = (int) Math.ceil((double) networkPacket.getPacketSize() / (double) SerialPacket.PAYLOAD_SIZE);
        assertEquals(expectedPackets, serialPackets.size());

        // Match n-1 SerialPacket payloads with the corresponding bytes from the NetworkPacket
        int networkPacketIndex = 0;
        for (int i = 0; i < serialPackets.size() - 1; i++) {

            SerialPacket sPacket = serialPackets.get(i);

            byte[] sPayload = new byte[sPacket.getPayloadSize()];
            sPacket.getPacketPayload(sPayload);

            for (int j = 0; j < sPayload.length; j++, networkPacketIndex++) {
                assertEquals(networkPacketBuff.get(networkPacketIndex), sPayload[j]);
            }
        }

        // Verify the payload bytes in the last packet (ignore padding)
        int remainingBytes = networkPacketIndex % SerialPacket.PAYLOAD_SIZE;

        SerialPacket sPacket = serialPackets.get(serialPackets.size() - 1);
        byte[] sPayload = new byte[sPacket.getPayloadSize()];
        sPacket.getPacketPayload(sPayload);

        for (int j = 0; j < remainingBytes; j++, networkPacketIndex++) {
            assertEquals(networkPacketBuff.get(networkPacketIndex), sPayload[j]);
        }
    }

    @Test
    public void testImmutablePosition() {
        // Ensure that the NetworkPacket's ByteBuffer position is not modified after packet generation

        byte[] PAYLOAD = new byte[2048];
        new Random().nextBytes(PAYLOAD);

        NetworkPacket packet = new NetworkPacket(COMMAND_READ, PAYLOAD);
        int oldPos = packet.getPacketByteBuffer().position();

        List<SerialPacket> serialPackets = PacketGenerator.generateSerialPackets(COMMAND_WRITE, packet);
        int newPos = packet.getPacketByteBuffer().position();

        assertEquals(oldPos, newPos);
    }

    @Test
    public void testNetworkPacketReconstruction() {
        // Ensure that the NetworkPacket is correctly reconstructed

        byte[] PAYLOAD = "Hello World".getBytes();

        NetworkPacket in = new NetworkPacket(COMMAND_READ, PAYLOAD);
        List<SerialPacket> serialPackets = PacketGenerator.generateSerialPackets(COMMAND_WRITE, in);

        NetworkPacketParser networkPacketParser = new NetworkPacketParser();

        // Accumulate all serial packet payloads
        for (SerialPacket serialPacket : serialPackets) {
            byte[] payload = new byte[serialPacket.getPayloadSize()];
            serialPacket.getPacketPayload(payload);
            networkPacketParser.addBytes(payload);
        }

        assertTrue(networkPacketParser.isPacketReady());

        NetworkPacket out = networkPacketParser.getPacket();
        byte[] payloadOut = new byte[out.getPayloadSize()];
        out.getPacketPayload(payloadOut);

        assertArrayEquals(PAYLOAD, payloadOut);
    }

}
