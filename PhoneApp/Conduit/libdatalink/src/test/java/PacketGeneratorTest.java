import com.conduit.libdatalink.internal.Constants;
import com.conduit.libdatalink.internal.NetworkPacket;
import com.conduit.libdatalink.internal.PacketGenerator;
import com.conduit.libdatalink.internal.SerialPacket;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class PacketGeneratorTest {
    @Test
    public void testSerialPacketGeneration() {
        // Ensure that the entire payload of the NetworkPacket is correctly split into SerialPackets

        byte[] PAYLOAD = new byte[64];
        new Random().nextBytes(PAYLOAD);

        NetworkPacket networkPacket = new NetworkPacket(Constants.COMMAND_READ, PAYLOAD);

        List<SerialPacket> serialPackets = PacketGenerator.generateSerialPackets(Constants.COMMAND_WRITE, networkPacket);

        // Ensure we have generated the correct number of SerialPackets
        int expectedPackets = (int) Math.ceil((double) PAYLOAD.length / (double) SerialPacket.PAYLOAD_SIZE);
        assertEquals(expectedPackets, serialPackets.size());

        // Match n-1 SerialPacket payloads with the corresponding bytes from the NetworkPacket
        int networkPayloadIndex = 0;
        for (int i = 0; i < serialPackets.size() - 1; i++) {

            SerialPacket sPacket = serialPackets.get(i);

            byte[] sPayload = new byte[sPacket.getPayloadSize()];
            sPacket.getPacketPayload(sPayload);

            for (int j = 0; j < sPayload.length; j++, networkPayloadIndex++) {
                assertEquals(PAYLOAD[networkPayloadIndex], sPayload[j]);
            }
        }

        // Verify the payload bytes in the last packet (ignore padding)
        int remainingBytes = networkPayloadIndex % SerialPacket.PAYLOAD_SIZE;

        SerialPacket sPacket = serialPackets.get(serialPackets.size() - 1);
        byte[] sPayload = new byte[sPacket.getPayloadSize()];
        sPacket.getPacketPayload(sPayload);

        for (int j = 0; j < remainingBytes; j++, networkPayloadIndex++) {
            assertEquals(PAYLOAD[networkPayloadIndex], sPayload[j]);
        }
    }

    @Test
    public void testImmutablePosition() {
        // Ensure that the NetworkPacket's ByteBuffer position is not modified after packet generation

        byte[] PAYLOAD = new byte[2048];
        new Random().nextBytes(PAYLOAD);

        NetworkPacket packet = new NetworkPacket(Constants.COMMAND_READ, PAYLOAD);
        int oldPos = packet.getPacketByteBuffer().position();

        List<SerialPacket> serialPackets = PacketGenerator.generateSerialPackets(Constants.COMMAND_WRITE, packet);
        int newPos = packet.getPacketByteBuffer().position();

        assertEquals(oldPos, newPos);
    }

}
