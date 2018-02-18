package com.conduit.libdatalink.internal;

import java.nio.ByteBuffer;

import static com.conduit.libdatalink.internal.Constants.*;

public class NetworkPacket {

    public static final int HEADER_SIZE = 1 + 1 + 4; // SOP, PAYLOAD_TYPE, PAYLOAD_SIZE
    public static final int FOOTER_SIZE = 1; // EOP

    private static final int INDEX_HEADER = 0;
    private static final int INDEX_PAYLOAD_TYPE = 1;
    private static final int INDEX_PAYLOAD_SIZE = 2;
    protected static final int INDEX_PAYLOAD = 6;

    private ByteBuffer packetData;

    /**
     * Initialize a new NetworkPacket; intended for incrementally building a packet
     * @param payloadType
     * @param payloadSize
     */
    protected NetworkPacket(byte payloadType, int payloadSize) {
        packetData = ByteBuffer.allocate(HEADER_SIZE + payloadSize + FOOTER_SIZE);
        packetData.put(CONTROL_START_OF_PACKET)
                .put(payloadType)
                .putInt(payloadSize);
    }

    /**
     * Initialize a new NetworkPacket; intended for creating a complete packet
     * @param payloadType
     * @param payload
     */
    public NetworkPacket(byte payloadType, byte[] payload) {
        // Call incremental constructor then append the payload
        this(payloadType, payload.length);

        packetData.put(payload)
                .put(CONTROL_END_OF_PACKET);
    }

    public byte getPayloadType() {
        return packetData.get(INDEX_PAYLOAD_TYPE);
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
                .limit(getPacketSize() - FOOTER_SIZE);
        return payload;
    }

    public ByteBuffer getPacketByteBuffer() {
        return packetData;
    }
}
