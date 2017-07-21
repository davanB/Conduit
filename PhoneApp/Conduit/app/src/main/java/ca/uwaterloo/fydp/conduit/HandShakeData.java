package ca.uwaterloo.fydp.conduit;

/**
 * Created by davanb on 2017-07-20.
 */

public class HandShakeData {
    int[] friends;

    public HandShakeData(int[] friends) {
        this.friends = friends;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < friends.length; i++) {
            result.append(friends[i]);
            result.append(',');
        }
        result.deleteCharAt(result.length());
        return result.toString();
    }
}
