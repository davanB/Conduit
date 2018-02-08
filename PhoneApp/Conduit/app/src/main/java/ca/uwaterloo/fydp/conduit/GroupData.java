package ca.uwaterloo.fydp.conduit;
import java.util.ArrayList;
import ca.uwaterloo.fydp.conduit.HandShakeData;

/**
 * Created by davanb on 2018-02-07.
 */

/*
Holds all the group data. Each HandShakeData holds the data that is sent to each member of group.
The master phone will need to keep track of all this before sending all the data to everyone else.
*/
public class GroupData {
    private static final int START_ADDRESS = 0x00000001; // whats the actual start?
    private static final int FINAL_ADDRESS = 0xFFFFFFFF; // actual end?

    private int currentAddress;
    private int masterAddress;
    private String groupName;

    private ArrayList<HandShakeData> handShakeDataList;

    public GroupData(String groupName) {
        this.groupName = groupName;
        handShakeDataList = new ArrayList<HandShakeData>();
        this.currentAddress = START_ADDRESS;
        this.masterAddress = START_ADDRESS;
    }

    //Simply increment a counter for now, this ensures every addr is unique.
    private int GenerateAddress() {
        return ++(this.currentAddress);
    }

    public HandShakeData GenerateHandShakeData() {
        int friendAddr = GenerateAddress();
        HandShakeData friendData = new HandShakeData(this.masterAddress, friendAddr, this.groupName);
        handShakeDataList.add(friendData);
        return friendData;
    }
}
