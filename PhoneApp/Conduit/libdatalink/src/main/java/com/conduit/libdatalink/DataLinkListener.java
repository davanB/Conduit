package com.conduit.libdatalink;

/*
This listener can be implemented in any class which needs high-level data. Control signals will be removed.
*/
public interface DataLinkListener {
    void OnReceiveData(String data);
}
