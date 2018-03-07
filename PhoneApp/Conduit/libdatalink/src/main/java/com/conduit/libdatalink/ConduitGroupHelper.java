package com.conduit.libdatalink;

/**
 * Created by Navjot on 2/25/2018.
 */

public class ConduitGroupHelper {

    public static int getFullAddress(int baseAddress, int majorClientID, int minorClientID) {
        return (0xFFFFFF00 & baseAddress) | ((0x0000000F & majorClientID) << 4) | (0x0000000F & minorClientID);
    }

}
