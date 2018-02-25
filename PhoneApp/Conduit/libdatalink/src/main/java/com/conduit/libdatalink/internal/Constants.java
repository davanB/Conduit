package com.conduit.libdatalink.internal;

public class Constants {

    public static final int ARDUINO_SERIAL_RX_BUFFER_SIZE = 64;

    public static final byte CONTROL_START_OF_PACKET = 1;
    public static final byte CONTROL_END_OF_PACKET = 4;

    public static final byte COMMAND_DEBUG_LED_BLINK = 33;
    public static final byte COMMAND_DEBUG_ECHO = 34;
    public static final byte COMMAND_OPEN_WRITING_PIPE = 40;
    public static final byte COMMAND_OPEN_READING_PIPE = 41;
    public static final byte COMMAND_WRITE = 42;
    public static final byte COMMAND_READ = 43;

//
//    public static final byte ERROR_INVALID_COMMAND = 1;
//    public static final byte ERROR_TX_FAIL = 110;
//    public static final byte ERROR_ACK_MISS = 111;

}
