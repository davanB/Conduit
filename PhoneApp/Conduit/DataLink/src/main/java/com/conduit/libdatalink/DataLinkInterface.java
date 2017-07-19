package com.conduit.libdatalink;

public interface DataLinkInterface {
    void debugLEDBlink(byte numBlinks);

    void debugEcho(byte value);

    void openWritingPipe(byte address);

    void openReadingPipe(byte pipeNumber, byte address);

    void write(byte payload[]);

    void setReadListener(DataLinkListener listener);
}
