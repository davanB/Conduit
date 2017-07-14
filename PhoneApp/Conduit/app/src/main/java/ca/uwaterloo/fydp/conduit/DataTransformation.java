package ca.uwaterloo.fydp.conduit;

/**
 * Created by davanb on 2017-07-14.
 */

public class DataTransformation {

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
