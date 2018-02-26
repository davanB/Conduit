package com.conduit.libdatalink;

/**
 * Created by Navjot on 2/25/2018.
 */

public class ConduitGroupHelper {

    public static int getFullAddress(int baseAddress, int clientId) {
        return (0xFFFFFF00 & baseAddress) | (0x000000FF & clientId);
    }

}
