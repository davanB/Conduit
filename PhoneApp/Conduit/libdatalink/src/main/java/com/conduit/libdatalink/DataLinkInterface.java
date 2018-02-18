package com.conduit.libdatalink;

public interface DataLinkInterface {
    void debugLEDBlink(byte numBlinks);

    void debugEcho(byte value);

    void openWritingPipe(int address);

    void openReadingPipe(byte pipeNumber, int address);

    void write(byte payloadType, byte payload[]);

    void setReadListener(DataLinkListener listener);
}
