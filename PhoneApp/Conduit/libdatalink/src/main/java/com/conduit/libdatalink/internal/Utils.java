package com.conduit.libdatalink.internal;


import java.nio.ByteBuffer;

public class Utils {
    public static byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(x);
        return buffer.array();
    }

    public static int bytesToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
}
