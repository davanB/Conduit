package com.conduit.libdatalink.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.conduit.libdatalink.internal.Constants.*;

/**
 * NetworkPacketParser parses SerialPackets from a discontinuous stream of bytes
 *
 * In the WAITING State, bytes are dumped into an accumulator list until the basic packet information is received.
 * Once this has occured, the parser creates the intermediate packet structure and toggles to the ACCUMULATE state
 * In the ACCUMULATE State, bytes are directly dumped into the NetworkPacket's ByteBuffer to minimize unnecessary copying
 */
public class NetworkPacketParser {

    public enum State {
        WAITING,
        ACCUMULATING
    }

    private State state = State.WAITING;

    List<Byte> accumulator = new ArrayList<Byte>();

    private NetworkPacket currentPacket = null;
    private Queue<NetworkPacket> output = new LinkedList<NetworkPacket>();

    public void addBytes(byte[] data) {

        if (state == State.WAITING) {

            // Fill accumulator
            for (int i = 0; i < data.length; i++) {
                accumulator.add(data[i]);
            }

            // Need to accumulate header info for current packet
            int start = accumulator.indexOf(CONTROL_START_OF_PACKET);
            if (start == -1) return;
            if (accumulator.size() < start + NetworkPacket.HEADER_SIZE) return;

            if (start != 0) System.out.println("WARNING: Accumulator had residual data!!");

            // Consume header and partially build incoming packet
            accumulator.remove(start);
            byte commandId = accumulator.remove(start);
            int payloadSize = Utils.bytesToInt(new byte[] {
                    accumulator.remove(start),
                    accumulator.remove(start),
                    accumulator.remove(start),
                    accumulator.remove(start)
            });

            // Build our packet with the header information received
            currentPacket = new NetworkPacket(commandId, payloadSize);
            state = State.ACCUMULATING;

            System.out.println("[NetworkPacketParser] expecting packet with payload size: " + payloadSize);

            // Handle the case where a portion of the payload is received - we need to move from accumulator to the packet
            if (accumulator.size() != 0) {

                int remaining = currentPacket.getPacketByteBuffer().remaining();
                for (int i = 0; i < remaining; i++) {
                    if (accumulator.size() == 0) break;
                    if (start > accumulator.size()) break; // This should never happen

                    // Fill up current packet buffer
                    currentPacket.getPacketByteBuffer().put(accumulator.remove(start));
                }

                remaining = currentPacket.getPacketByteBuffer().remaining();

                if (remaining == 0) {
                    output.add(currentPacket);
                    currentPacket = null;
                    state = State.WAITING;
                }
            }

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

    public NetworkPacket getPacket() {
        return output.remove();
    }

}
