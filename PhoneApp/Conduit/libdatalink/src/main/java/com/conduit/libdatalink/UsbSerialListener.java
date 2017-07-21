package com.conduit.libdatalink;

/*
This listener should only be implemented in classes which provide low-level Serial I/O.
The raw data returned in this listener will contain control signals and should never be exposed in a user interface
*/
public interface UsbSerialListener {
    void OnReceiveData(byte[] data);
}
