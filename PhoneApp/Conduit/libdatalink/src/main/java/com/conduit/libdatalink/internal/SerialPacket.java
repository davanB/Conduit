package com.conduit.libdatalink.internal;

import java.nio.ByteBuffer;

import static com.conduit.libdatalink.internal.Constants.*;

public class SerialPacket {

    public static final int HEADER_SIZE = 1 + 1 + 4; // SOP, COMMAND_ID, PAYLOAD_SIZE
    public static final int FOOTER_SIZE = 1; // EOP

    private static final int INDEX_HEADER = 0;
    private static final int INDEX_COMMAND = 1;
    private static final int INDEX_PAYLOAD_SIZE = 2;
    private static final int INDEX_PAYLOAD = 6;

    private ByteBuffer packetData;

    /**
     * Initialize a new SerialPacket; intended for incrementally building a packet
     * @param commandId
     * @param payloadSize
     */
    protected SerialPacket(byte commandId, int payloadSize) {
        packetData = ByteBuffer.allocate(HEADER_SIZE + payloadSize + FOOTER_SIZE);
        packetData.put(CONTROL_START_OF_PACKET)
                .put(commandId)
                .putInt(payloadSize);
    }

    /**
     * Initialize a new SerialPacket; intended for creating a complete packet
     * @param commandId
     * @param payload
     */
    public SerialPacket(byte commandId, byte[] payload) {
        // Call incremental constructor then append the payload
        this(commandId, payload.length);

        packetData.put(payload)
                .put(CONTROL_END_OF_PACKET);
    }

    public byte getCommandId() {
        return packetData.get(INDEX_COMMAND);
    }

    public int getPayloadSize() {
        return packetData.getInt(INDEX_PAYLOAD_SIZE);
    }

    public int getPacketSize() {
        return packetData.capacity();
    }

    public void getPacketPayload(byte[] buffer) {
        // Create a duplicate view of our buffer
        ByteBuffer tmp = packetData.duplicate();
        tmp.rewind();
        tmp.position(INDEX_PAYLOAD);
        tmp.limit(packetData.capacity() - FOOTER_SIZE); // don't include the footer

        // Copy the payload into the provided array
        tmp.get(buffer);
    }

    public ByteBuffer getPacketByteBuffer() {
        return packetData;
    }
}
