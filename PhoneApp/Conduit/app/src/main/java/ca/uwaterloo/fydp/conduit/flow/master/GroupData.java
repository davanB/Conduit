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
    public static final int START_ADDRESS = 0xCDABCD00;

    private int mCurrentAddress;
    private int mMasterAddress;
    private String mGroupName;
    private String mPassword;
    private int mGroupSize;

    private ArrayList<HandShakeData> mHandShakeDataList;

    public GroupData(String groupName, String password, int groupsize) {
        this.mGroupName = groupName;
        this.mPassword = password;
        mHandShakeDataList = new ArrayList<>();
        this.mCurrentAddress = START_ADDRESS;
        this.mMasterAddress = START_ADDRESS;
        this.mGroupSize = groupsize;
    }

    public int getBaseAddress() {
        return mMasterAddress;
    }

    public int getCurrentAddress() { return mCurrentAddress; }

    public boolean isFinishedHandshakes() {
        // rollover => done
        return mCurrentAddress == mGroupSize;
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
