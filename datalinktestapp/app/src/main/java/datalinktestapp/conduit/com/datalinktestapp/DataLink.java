package datalinktestapp.conduit.com.datalinktestapp;

import android.hardware.usb.UsbManager;


public class DataLink extends SerialWrapper implements DataLinkInterface{

    private static final byte COMMAND_HEADER = 16;

    private static final byte COMMAND_DEBUG_LED_BLINK = 100;
    private static final byte COMMAND_DEBUG_ECHO = 101;
    private static final byte COMMAND_OPEN_WRITING_PIPE = 125;
    private static final byte COMMAND_OPEN_READING_PIPE = 126;
    private static final byte COMMAND_WRITE = 127;

    public DataLink(UsbManager manager) {
        super(manager);
    }

    public void debugLEDBlink(byte numBlinks) {
        byte buf[] = {COMMAND_HEADER, COMMAND_DEBUG_LED_BLINK, numBlinks};
        sendBuffer(buf);
    }

    public void debugEcho(byte value) {
        byte buf[] = {COMMAND_HEADER, COMMAND_DEBUG_ECHO, value};
        sendBuffer(buf);
    }

    public void openWritingPipe() {
    }

    public void openReadingPipe() {
    }

    public void write() {
    }

}
