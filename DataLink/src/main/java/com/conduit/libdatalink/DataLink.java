package com.conduit.libdatalink;

public class DataLink implements DataLinkInterface{

    private static final byte COMMAND_HEADER = 16;

    private static final byte COMMAND_DEBUG_LED_BLINK = 100;
    private static final byte COMMAND_DEBUG_ECHO = 101;
    private static final byte COMMAND_OPEN_WRITING_PIPE = 125;
    private static final byte COMMAND_OPEN_READING_PIPE = 126;
    private static final byte COMMAND_WRITE = 127;

    private static final byte COMMAND_TERMINATOR = '0';

    private UsbDriverInterface usbDriver;

    public DataLink(UsbDriverInterface usbDriver) {
        this.usbDriver = usbDriver;
    }

    public void debugLEDBlink(byte numBlinks) {
        byte buf[] = {COMMAND_HEADER, COMMAND_DEBUG_LED_BLINK, numBlinks};
        usbDriver.sendBuffer(buf);
    }

    public void debugEcho(byte value) {
        byte buf[] = {COMMAND_HEADER, COMMAND_DEBUG_ECHO, value};
        usbDriver.sendBuffer(buf);
    }

    public void openWritingPipe(byte address) {
        byte buf[] = {COMMAND_HEADER, COMMAND_OPEN_WRITING_PIPE, address};
        usbDriver.sendBuffer(buf);
    }

    public void openReadingPipe(byte pipeNumber, byte address) {
        byte buf[] = {COMMAND_HEADER, COMMAND_OPEN_READING_PIPE, pipeNumber, address};
        usbDriver.sendBuffer(buf);
    }

    public void write(byte[] payload) {
        byte buf[] = {COMMAND_HEADER,COMMAND_WRITE};
        buf = concatBuffers(buf, payload);
        buf = concatBuffers(buf, new byte[]{COMMAND_TERMINATOR});
        usbDriver.sendBuffer(buf);
    }

    public void setReadListener(DataLinkListener listener) {
        usbDriver.setReadListener(listener);
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
