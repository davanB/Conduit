package ca.uwaterloo.fydp.conduit;

/**
 * Created by davanb on 2017-07-20.
 */

// should simply hold data that is transfered via QR
public class HandShakeData {
    private int mMasterAddress;
    private int mFriendAddress;
    private String mGroupName;

    public HandShakeData(int masterAddress, int friendAddress, String groupName) {
        this.mMasterAddress = masterAddress;
        this.mFriendAddress = friendAddress;
        this.mGroupName = groupName;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.mMasterAddress + ",");
        result.append(this.mFriendAddress + ",");
        result.append(this.mGroupName);
        return result.toString();
    }
}
