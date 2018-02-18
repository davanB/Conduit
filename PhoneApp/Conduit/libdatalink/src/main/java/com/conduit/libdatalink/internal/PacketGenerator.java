package com.conduit.libdatalink.internal;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class PacketGenerator {
    public static List<SerialPacket> generateSerialPackets(byte serialCommand, NetworkPacket packet) {
        List<SerialPacket> serialPackets = new LinkedList<SerialPacket>();

        //TODO: I'm assuming infinite memory (copying entire NetworkPacket buffer into SerialPackets)

        int numSerialPackets = (int) Math.ceil((double) packet.getPacketSize() / (double) SerialPacket.PAYLOAD_SIZE);

        // Create a duplicate view of NetworkPacket buffer
        ByteBuffer txPayload = packet.getPacketByteBuffer().duplicate();
        txPayload.rewind();

        int start = 0;
        int end = 0;

        for (int i = 0; i < numSerialPackets; i++) {
            end = start + SerialPacket.PAYLOAD_SIZE;

            // Set to payload limit
            if (end > packet.getPacketSize()) end = packet.getPacketSize();

            txPayload.position(start);
            txPayload.limit(end);

            // Copy the buffer and add to list
            SerialPacket serialPacket = new SerialPacket(serialCommand, (byte) 0);
            serialPacket.getPacketByteBuffer().put(txPayload);
            serialPackets.add(serialPacket);

            start += SerialPacket.PAYLOAD_SIZE;
        }

        return serialPackets;
    }
}
