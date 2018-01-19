package com.conduit.libdatalink.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.conduit.libdatalink.internal.Constants.*;

/**
 * SerialPacketParser parses SerialPackets from a discontinuous stream of bytes
 *
 * In the WAITING State, bytes are dumped into an accumulator list until the basic packet information is received.
 * Once this has occured, the parser creates the intermediate packet structure and toggles to the ACCUMULATE state
 * In the ACCUMULATE State, bytes are directly dumped into the SerialPacket's ByteBuffer to minimize unnecessary copying
 */
public class SerialPacketParser {

    public enum State {
        WAITING,
        ACCUMULATING
    }

    private State state = State.WAITING;

    List<Byte> accumulator = new ArrayList<Byte>();

    private SerialPacket currentPacket = null;
    private Queue<SerialPacket> output = new LinkedList<SerialPacket>();

    public void addBytes(byte[] data) {

        if (state == State.WAITING) {

            // Fill accumulator
            for (int i = 0; i < data.length; i++) {
                accumulator.add(data[i]);
            }

            // Need to accumulate header info for current packet
            int start = accumulator.indexOf(CONTROL_START_OF_PACKET);
            if (start == -1) return;
            if (accumulator.size() < start + SerialPacket.HEADER_SIZE) return;

            // Consume header and partially build incoming packet
            accumulator.remove(start);
            byte commandId = accumulator.remove(start);
            int payloadSize = Utils.bytesToInt(new byte[] {
                    accumulator.remove(start),
                    accumulator.remove(start),
                    accumulator.remove(start),
                    accumulator.remove(start)
            });

            if (accumulator.size() != 0) {
                // TODO: Handle this error condition properly
                System.out.println("WARNING: Accumulator not empty!");
                accumulator.clear();
            }

            currentPacket = new SerialPacket(commandId, payloadSize);
            state = State.ACCUMULATING;

        } else if (state == State.ACCUMULATING) {

            // Fill up current packet buffer
            int remaining = currentPacket.getPacketByteBuffer().remaining();

            if (data.length <= remaining) {
                currentPacket.getPacketByteBuffer().put(data);
            } else {
                // Add leftover bytes to the accumulator for the next packet
                currentPacket.getPacketByteBuffer().put(data, 0, remaining);
                for (int i = remaining; i < data.length; i++) {
                    accumulator.add(data[i]);
                }
            }

            remaining = currentPacket.getPacketByteBuffer().remaining();

            if (remaining == 0) {
                output.add(currentPacket);
                currentPacket = null;
                state = State.WAITING;
            }

        }
    }

    public boolean isPacketReady() {
        return output.size() > 0;
    }

    public SerialPacket getPacket() {
        return output.remove();
    }

}
