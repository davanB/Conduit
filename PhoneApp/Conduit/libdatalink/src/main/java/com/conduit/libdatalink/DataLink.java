package com.conduit.libdatalink;

import java.nio.ByteBuffer;

public class DataLink implements DataLinkInterface{

    private static final byte CONTROL_START_OF_HEADING = 1;
    private static final byte CONTROL_START_OF_TEXT = 2;
    private static final byte CONTROL_END_OF_TEXT = 3;

    private static final byte COMMAND_DEBUG_LED_BLINK = 33;
    private static final byte COMMAND_DEBUG_ECHO = 34;
    private static final byte COMMAND_OPEN_WRITING_PIPE = 40;
    private static final byte COMMAND_OPEN_READING_PIPE = 41;
    private static final byte COMMAND_WRITE = 42;
    private static final byte COMMAND_READ = 43;

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

    private UsbSerialListener usbSerialListener = new UsbSerialListener() {
        @Override
        public void OnReceiveData(byte[] data) {
            // TODO: Accumulate data and parse out control signals
            if (dataLinkListener != null) {
                dataLinkListener.OnReceiveData(new String(data));
            }
        }
    };

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
