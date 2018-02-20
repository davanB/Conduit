package ca.uwaterloo.fydp.conduit.flow.master;
import java.util.ArrayList;
import ca.uwaterloo.fydp.conduit.qr.HandShakeData;

/**
 * Created by davanb on 2018-02-07.
 */

/*
Holds all the group data. Each HandShakeData holds the data that is sent to each member of group.
The master phone will need to keep track of all this before sending all the data to everyone else.
*/
public class GroupData {
    private static final int START_ADDRESS = 0x00000000;

    // TODO: account for the REAL number of max users, as should be set in the group creation screen!
    private static final int FINAL_ADDRESS = 0x00000005; // max 6 users

    private int mCurrentAddress;
    private int mMasterAddress;
    private String mGroupName;
    private String mPassword;

    private ArrayList<HandShakeData> mHandShakeDataList;

    public GroupData(String groupName, String password) {
        this.mGroupName = groupName;
        this.mPassword = password;
        mHandShakeDataList = new ArrayList<>();
        this.mCurrentAddress = START_ADDRESS;
        this.mMasterAddress = START_ADDRESS;
    }

    public int getBaseAddress() {
        return mMasterAddress;
    }

    public int getCurrentAddress() { return mCurrentAddress; }

    public boolean isFinishedHandshakes() {
        // rollover => done
        return mCurrentAddress == FINAL_ADDRESS + 1;
    }

    //Simply increment a counter for now, this ensures every addr is unique.
    private int generateAddress() {
        return ++(this.mCurrentAddress);
    }

    public ArrayList<HandShakeData> getUserList() {
        return mHandShakeDataList;
    }

    public HandShakeData generateHandShakeData() {
        int friendAddr = generateAddress();
        HandShakeData friendData = new HandShakeData(this.mMasterAddress, friendAddr, this.mGroupName, this.mPassword);
        mHandShakeDataList.add(friendData);
        return friendData;
    }
}
