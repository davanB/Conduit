package com.conduit.libdatalink.conduitabledata;

import java.nio.ByteBuffer;

public abstract class ConduitableData {
    public int originAddress;
    public abstract void populateFromPayload(ByteBuffer payload);
    public abstract ByteBuffer getPayload();
}
