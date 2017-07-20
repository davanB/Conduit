package datalinktestapp.conduit.com.datalinktestapp;


public interface DataLinkInterface {
    public void debugLEDBlink(byte numBlinks);

    public void debugEcho(byte value);

    public void openWritingPipe(byte address);

    public void openReadingPipe(byte pipeNumber, byte address);

    public void write(byte payload[]);
}
