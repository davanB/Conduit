package datalinktestapp.conduit.com.datalinktestapp;

/**
 * Created by Navjot on 7/14/2017.
 */
public interface DataLinkInterface {
    public void debugLEDBlink(byte numBlinks);

    public void debugEcho(byte value);

    public void openWritingPipe();

    public void openReadingPipe();

    public void write();
}
