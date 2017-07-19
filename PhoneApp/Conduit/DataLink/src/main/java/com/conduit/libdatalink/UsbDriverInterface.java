package com.conduit.libdatalink;

public interface UsbDriverInterface {
    void sendBuffer(byte[] buf);
    void setReadListener(DataLinkListener listener);
}
