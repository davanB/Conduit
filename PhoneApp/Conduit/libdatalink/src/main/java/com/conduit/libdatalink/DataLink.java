package com.conduit.libdatalink;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataLink implements DataLinkInterface{

    public static final byte CONTROL_START_OF_HEADING = 1;
    public static final byte CONTROL_START_OF_TEXT = 2;
    public static final byte CONTROL_END_OF_TEXT = 3;
    public static final byte CONTROL_END_OF_TRANSMISSION = 4;

    public static final byte COMMAND_DEBUG_LED_BLINK = 33;
    public static final byte COMMAND_DEBUG_ECHO = 34;
    public static final byte COMMAND_OPEN_WRITING_PIPE = 40;
    public static final byte COMMAND_OPEN_READING_PIPE = 41;
    public static final byte COMMAND_WRITE = 42;
    public static final byte COMMAND_READ = 43;

    public static final byte STATUS_SUCCESS = 100;
    public static final byte STATUS_FAILURE = 101;

    public static final byte ERROR_INVALID_COMMAND = 1;
    public static final byte ERROR_TX_FAIL = 110;
    public static final byte ERROR_ACK_MISS = 111;

    private UsbDriverInterface usbDriver;
    private DataLinkListener dataLinkListener;

    public DataLink(UsbDriverInterface usbDriver) {
        this.usbDriver = usbDriver;
        this.usbDriver.setReadListener(usbSerialListener);
    }

    public void debugLEDBlink(byte numBlinks) {
        byte buf[] = {CONTROL_START_OF_HEADING, COMMAND_DEBUG_LED_BLINK, numBlinks};
        usbDriver.sendBuffer(buf);
    }

    public void debugEcho(byte value) {
        byte buf[] = {CONTROL_START_OF_HEADING, COMMAND_DEBUG_ECHO, value};
        usbDriver.sendBuffer(buf);
    }

    public void openWritingPipe(int address) {
        byte buf[] = {CONTROL_START_OF_HEADING, COMMAND_OPEN_WRITING_PIPE};
        byte addressBuf[] = intToBytes(address);
        buf = concatBuffers(buf, addressBuf);
        usbDriver.sendBuffer(buf);
    }

    public void openReadingPipe(byte pipeNumber, int address) {
        byte buf[] = {CONTROL_START_OF_HEADING, COMMAND_OPEN_READING_PIPE, pipeNumber};
        byte addressBuf[] = intToBytes(address);
        buf = concatBuffers(buf, addressBuf);
        usbDriver.sendBuffer(buf);
    }

    public void write(byte[] payload) {
        byte buf[] = {CONTROL_START_OF_HEADING, COMMAND_WRITE, CONTROL_START_OF_TEXT};
        buf = concatBuffers(buf, payload);
        buf = concatBuffers(buf, new byte[]{CONTROL_END_OF_TEXT});
        usbDriver.sendBuffer(buf);
    }

    public void setReadListener(DataLinkListener listener) {
        dataLinkListener = listener;
    }


    List<Byte> accumulator = new ArrayList<Byte>();

    private UsbSerialListener usbSerialListener = new UsbSerialListener() {
        @Override
        public void OnReceiveData(byte[] data) {
            // Persist all bytes
            for (int i = 0; i < data.length; i++) accumulator.add(data[i]);

            while (accumulator.contains(CONTROL_START_OF_HEADING) && accumulator.contains(CONTROL_END_OF_TRANSMISSION)) {
                // Full packet(s); find and parse
                CommandResultHolder result = parseCommand(accumulator);
                System.out.println(result);

                if (dataLinkListener != null) {
                    if (result.COMMAND_ID == COMMAND_READ){
                        dataLinkListener.OnReceiveData(new String(result.PAYLOAD));
                    }
                }
            }

        }
    };

    public static CommandResultHolder parseCommand(List<Byte> data) {
        CommandResultHolder result = null;

        int head_idx = -1;
        int term_idx = -1;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) == CONTROL_START_OF_HEADING) head_idx = i;
            if (data.get(i) == CONTROL_END_OF_TRANSMISSION) term_idx = i;

            if (head_idx >= 0 && term_idx >= 0 && head_idx < term_idx) {

                // Found a good packet, remove header and terminator
                data.remove(term_idx);
                data.remove(head_idx);

                // Parse packet
                result = new CommandResultHolder(data.subList(head_idx, term_idx - 1));
            }
        }

        return result;
    }

    public byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(x);
        return buffer.array();
    }

    private byte[] concatBuffers(byte[] first, byte[] second) {
        int aLen = first.length;
        int bLen = second.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(first, 0, c, 0, aLen);
        System.arraycopy(second, 0, c, aLen, bLen);
        return c;
    }
}
