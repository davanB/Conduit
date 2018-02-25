package com.conduit.libdatalink.internal;

import java.nio.ByteBuffer;

public class SerialPacket {

    public static final byte STATUS_DONT_CARE = 0;
    public static final byte STATUS_SUCCESS = 100;
    public static final byte STATUS_FAILURE = 101;


    public static final int HEADER_SIZE = 1 + 1 + 1; // COMMAND_ID, STATUS, SOURCE

    public static final int INDEX_HEADER = 0;
    public static final int INDEX_COMMAND = INDEX_HEADER;
    public static final int INDEX_STATUS = INDEX_COMMAND + 1;
    public static final int INDEX_SOURCE = INDEX_STATUS + 1;
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
    protected SerialPacket(byte commandId, byte status, byte source) {
        // Call incremental constructor then append the payload
        this();

        packetData.put(commandId)
                .put(status)
                .put(source);
    }

    /**
     * Initialize a new SerialPacket; intended for creating a complete packet
     * @param commandId
     * @param source
     * @param payload
     */
    public SerialPacket(byte commandId, byte status, byte source, byte[] payload) {
        // Call incremental constructor then append the payload
        this();

        packetData.put(commandId)
                .put(status)
                .put(source)
                .put(payload);
    }

    /**
     * Convenience constructor
     */
    public SerialPacket(byte commandId, byte[] payload) {
        // Call incremental constructor then append the payload
        this(commandId, STATUS_DONT_CARE, (byte) 0, payload);
    }

    public byte getCommandId() {
        return packetData.get(INDEX_COMMAND);
    }

    public byte getStatus() {
        return packetData.get(INDEX_STATUS);
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


    /**
     * Get a ByteBuffer containing the packet payload.
     *
     * WARNING: ByteBuffer.array() DOES NOT honor the ByteBuffer position!
     * @return
     */
    public ByteBuffer getPacketPayload() {
        ByteBuffer payload = packetData.duplicate();
        payload.rewind()
                .position(INDEX_PAYLOAD)
                .limit(getPacketSize());
        return payload;
    }

    public ByteBuffer getPacketByteBuffer() {
        return packetData;
    }
}
