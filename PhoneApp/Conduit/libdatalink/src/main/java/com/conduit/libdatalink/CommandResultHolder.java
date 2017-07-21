package com.conduit.libdatalink;


import java.util.Arrays;
import java.util.List;

public class CommandResultHolder {
    public final byte COMMAND_ID;
    public final byte STATUS;
    public final byte[] PAYLOAD;

    public CommandResultHolder(List<Byte> data) {
        this.COMMAND_ID = data.remove(0);
        this.STATUS = data.remove(0);
        this.PAYLOAD = new byte[data.size()];

        for (int i = 0; i < data.size(); i++) {
            this.PAYLOAD[i] = data.remove(0);
        }
    }

    public String toString() {
        String out = "";
        out += "Command: " + COMMAND_ID + "\n";
        out += "Status : " + STATUS + "\n";
        out += "Payload: " + Arrays.toString(PAYLOAD) + "\n";
        return out;
    }
}
