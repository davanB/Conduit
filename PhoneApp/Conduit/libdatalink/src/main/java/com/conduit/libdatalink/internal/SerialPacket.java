package com.conduit.libdatalink.internal;

import java.nio.ByteBuffer;

public class SerialPacket {

    public static final int HEADER_SIZE = 1 + 1; // COMMAND_ID, SOURCE

    public static final int INDEX_HEADER = 0;
    public static final int INDEX_COMMAND = INDEX_HEADER;
    public static final int INDEX_SOURCE = INDEX_COMMAND + 1;
    public static final int INDEX_PAYLOAD = INDEX_SOURCE + 1;

    public static final int PAYLOAD_SIZE = 32;
    public static final int PACKET_SIZE = HEADER_SIZE + PAYLOAD_SIZE;

    private ByteBuffer packetData;

    /**
     * Initialize a new SerialPacket; intended for incrementally building a packet
     */
    protected SerialPacket() {
        packetData = ByteBuffer.allocate(PACKET_SIZE);
    }

    /**
     * Initialize a new SerialPacket; intended for incrementally building a packet
     */
    protected SerialPacket(byte commandId, byte source) {
        // Call incremental constructor then append the payload
        this();

        packetData.put(commandId)
                .put(source);
    }

    /**
     * Initialize a new SerialPacket; intended for creating a complete packet
     * @param commandId
     * @param source
     * @param payload
     */
    public SerialPacket(byte commandId, byte source, byte[] payload) {
        // Call incremental constructor then append the payload
        this();

        packetData.put(commandId)
                .put(source)
                .put(payload);
    }

    /**
     * Convenience constructor
     */
    public SerialPacket(byte commandId, byte[] payload) {
        // Call incremental constructor then append the payload
        this(commandId, (byte) 0, payload);
    }

    public byte getCommandId() {
        return packetData.get(INDEX_COMMAND);
    }

    public byte getSource() {
        return packetData.get(INDEX_SOURCE);
    }

    public int getPayloadSize() {
        return PAYLOAD_SIZE;
    }

    public int getPacketSize() {
        return packetData.capacity();
    }

    public void getPacketPayload(byte[] buffer) {
        // Create a duplicate view of our buffer
        ByteBuffer tmp = packetData.duplicate();
        tmp.rewind();
        tmp.position(INDEX_PAYLOAD);
        tmp.limit(packetData.capacity());

        // Copy the payload into the provided array
        tmp.get(buffer);
    }

    public ByteBuffer getPacketByteBuffer() {
        return packetData;
    }
}
