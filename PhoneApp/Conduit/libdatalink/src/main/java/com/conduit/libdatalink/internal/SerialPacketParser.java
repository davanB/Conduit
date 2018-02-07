package com.conduit.libdatalink.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * SerialPacketParser parses incoming SerialPackets from the UART port
 *
 * SerialPackets are a fixed size, thus parsing simply interprets a fixed size block as a packet
 */
public class SerialPacketParser {

    List<Byte> accumulator = new ArrayList<Byte>();

    public void addBytes(byte[] data) {
        // Fill accumulator
        for (int i = 0; i < data.length; i++) {
            accumulator.add(data[i]);
        }
    }

    public boolean isPacketReady() {
        return accumulator.size() >= SerialPacket.PACKET_SIZE;
    }

    public SerialPacket getPacket() {
        if (!isPacketReady()) return null;

        SerialPacket packet = new SerialPacket();

        for (int i = 0; i < SerialPacket.PACKET_SIZE; i++) {
            packet.getPacketByteBuffer().put(accumulator.remove(0));
        }

        return packet;
    }
}
