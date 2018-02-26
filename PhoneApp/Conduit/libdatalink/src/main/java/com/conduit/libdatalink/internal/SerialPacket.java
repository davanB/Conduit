package com.conduit.libdatalink.internal;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class SerialPacket {

    public static final byte STATUS_DONT_CARE = 0;
    public static final byte STATUS_SUCCESS = 100;
    public static final byte STATUS_FAILURE = 101;

    public static final byte COMMAND_DEBUG_LED_BLINK = 33;
    public static final byte COMMAND_DEBUG_ECHO = 34;
    public static final byte COMMAND_OPEN_WRITING_PIPE = 40;
    public static final byte COMMAND_OPEN_READING_PIPE = 41;
    public static final byte COMMAND_WRITE = 42;
    public static final byte COMMAND_READ = 43;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\tCommand: ");
        switch (getCommandId()) {
            case COMMAND_DEBUG_LED_BLINK:
                sb.append("DEBUG_LED_BLINK");
                break;
            case COMMAND_DEBUG_ECHO:
                sb.append("DEBUG_ECHO");
                break;
            case COMMAND_OPEN_WRITING_PIPE:
                sb.append("OPEN_WRITING_PIPE");
                break;
            case COMMAND_OPEN_READING_PIPE:
                sb.append("OPEN_READING_PIPE");
                break;
            case COMMAND_WRITE:
                sb.append("WRITE");
                break;
            case COMMAND_READ:
                sb.append("READ");
                break;
        }
        sb.append(String.format(" (%d)", (int)(getCommandId() & 0xFF)));
        sb.append("\n");

        sb.append("\tStatus:  ");
        switch (getStatus()) {
            case STATUS_SUCCESS:
                sb.append("SUCCESS");
                break;
            case STATUS_FAILURE:
                sb.append("FAILURE");
                break;
            case STATUS_DONT_CARE:
                sb.append("DON'T CARE");
                break;
            default:
                sb.append("UNKNOWN");
                break;
        }
        sb.append(String.format(" (%d)", (int)(getStatus() & 0xFF)));
        sb.append("\n");

        sb.append("\tSource: ");
        sb.append(String.format(" %02x", (int)(getSource() & 0xFF)));
        sb.append("\n");

        byte[] payload = new byte[getPayloadSize()];
        getPacketPayload(payload);
        sb.append("\tPayload: ");
        sb.append(new String(payload));
        sb.append("\n");
        sb.append("\tPayload: ");
        sb.append(Arrays.toString(payload));
        sb.append("\n");

        return sb.toString();
    }
}
