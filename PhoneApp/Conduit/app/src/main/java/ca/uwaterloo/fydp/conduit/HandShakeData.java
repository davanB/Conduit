package ca.uwaterloo.fydp.conduit;

/**
 * Created by davanb on 2017-07-20.
 */

// should simply hold data that is transfered via QR
public class HandShakeData {
    private int masterAddress;
    private int friendAddress;
    private String groupName;

    public HandShakeData(int masterAddress, int friendAddress, String groupName) {
        this.masterAddress = masterAddress;
        this.friendAddress = friendAddress;
        this.groupName = groupName;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.masterAddress + ",");
        result.append(this.friendAddress + ",");
        result.append(this.groupName);
        return result.toString();
    }
}
