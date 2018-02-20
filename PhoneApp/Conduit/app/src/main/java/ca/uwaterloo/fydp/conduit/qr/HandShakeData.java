package ca.uwaterloo.fydp.conduit.qr;

import java.io.Serializable;

/**
 * Created by davanb on 2017-07-20.
 */

// should simply hold data that is transfered via QR
public class HandShakeData implements Serializable {
    public int mMasterAddress;
    public int mFriendAddress;
    public String mGroupName;
    public String mPassword;

    public HandShakeData(int masterAddress, int friendAddress, String groupName,
                         String password) {
        this.mMasterAddress = masterAddress;
        this.mFriendAddress = friendAddress;
        this.mGroupName = groupName;
        this.mPassword = password;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.mMasterAddress + ",");
        result.append(this.mFriendAddress + ",");
        result.append(this.mGroupName + ",");
        result.append(this.mPassword);
        return result.toString();
    }
}
