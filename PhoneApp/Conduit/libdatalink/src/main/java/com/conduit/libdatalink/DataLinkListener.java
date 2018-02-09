package com.conduit.libdatalink;

import java.nio.ByteBuffer;

/*
This listener can be implemented in any class which needs high-level data. Control signals will be removed.
*/
public interface DataLinkListener {
    void OnReceiveData(int originAddress, byte payloadType, ByteBuffer payload);
}
