package com.conduit.libdatalink.internal;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class PacketGenerator {
    public static List<SerialPacket> generateSerialPackets(byte serialCommand, NetworkPacket packet) {
        List<SerialPacket> serialPackets = new LinkedList<SerialPacket>();

        //TODO: I'm assuming infinite memory (copying entire NetworkPacket buffer into SerialPackets)

        int numSerialPackets = (int) Math.ceil((double) packet.getPayloadSize() / (double) SerialPacket.PAYLOAD_SIZE);

        // Create a duplicate view of NetworkPacket buffer
        ByteBuffer txPayload = packet.getPacketByteBuffer().duplicate();
        txPayload.rewind();

        int offset = NetworkPacket.INDEX_PAYLOAD;
        int limit = 0;

        for (int i = 0; i < numSerialPackets; i++) {
            limit = offset + SerialPacket.PAYLOAD_SIZE;

            // Set to payload limit
            if (limit > packet.getPayloadSize()) limit = packet.getPacketByteBuffer().capacity() - NetworkPacket.FOOTER_SIZE;

            txPayload.position(offset);
            txPayload.limit(limit);

            // Copy the buffer and add to list
            SerialPacket serialPacket = new SerialPacket(serialCommand, (byte) 0);
            serialPacket.getPacketByteBuffer().put(txPayload);
            serialPackets.add(serialPacket);

            offset += SerialPacket.PAYLOAD_SIZE;
        }

        return serialPackets;
    }
}
