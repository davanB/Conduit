package com.conduit.desktop;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.util.Arrays;

public class DataLink {

    private static final byte COMMAND_HEADER = 16;

    private static final byte COMMAND_DEBUG_LED_BLINK = 100;
    private static final byte COMMAND_DEBUG_ECHO = 101;
    private static final byte COMMAND_OPEN_WRITING_PIPE = 125;
    private static final byte COMMAND_OPEN_READING_PIPE = 126;
    private static final byte COMMAND_WRITE = 127;

    private static final byte ERROR_INVALID_COMMAND = 1;

    private static final byte COMMAND_TERMINATOR = '0';


    SerialPort comPort;


    DataLinkListener dataLinkListener;

    public DataLink() {
        SerialPort ports[] = SerialPort.getCommPorts();
        comPort = ports[ports.length - 2];

        System.out.println(comPort.getSystemPortName());

        comPort.openPort();
        comPort.addDataListener(serialPortDataListener);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    void debugLEDBlink(byte numBlinks) {
        byte buf[] = { COMMAND_HEADER, COMMAND_DEBUG_LED_BLINK, numBlinks };
        System.out.println(Arrays.toString(buf));
        comPort.writeBytes(buf, buf.length);
    }

    void debugEcho(byte value) {
        byte buf[] = { COMMAND_HEADER, COMMAND_DEBUG_ECHO, value };
        System.out.println(Arrays.toString(buf));
        comPort.writeBytes(buf, buf.length);
    }

    void openWritingPipe(byte address) {
        byte buf[] = { COMMAND_HEADER, COMMAND_OPEN_WRITING_PIPE, address };
        System.out.println(Arrays.toString(buf));
        comPort.writeBytes(buf, buf.length);
    }

    void openReadingPipe(byte pipeNumber, byte address) {
        byte buf[] = { COMMAND_HEADER, COMMAND_OPEN_READING_PIPE, pipeNumber, address };
        System.out.println(Arrays.toString(buf));
        comPort.writeBytes(buf, buf.length);
    }

    void write(byte[] payload) {
        byte buf[] = {COMMAND_HEADER,COMMAND_WRITE};
        buf = concatBuffers(buf, payload);
        buf = concatBuffers(buf, new byte[]{COMMAND_TERMINATOR});
        System.out.println("Buffer length:" + buf.length);
        System.out.println("Buffer:" + Arrays.toString(buf));
        comPort.writeBytes(buf, buf.length);
    }

    public void setDataLinkListener(DataLinkListener dataLinkListener) {
        this.dataLinkListener = dataLinkListener;
    }

    private SerialPortDataListener serialPortDataListener = new SerialPortDataListener() {
        @Override
        public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_DATA_WRITTEN; }
        @Override
        public void serialEvent(SerialPortEvent event)
        {
            if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN) {
                System.out.println("All bytes were successfully transmitted!");
            }

            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                return;
            byte[] newData = new byte[comPort.bytesAvailable()];
            int numRead = comPort.readBytes(newData, newData.length);
//            System.out.println("Read " + numRead + " bytes: " + Arrays.toString(newData) + " " + new String(newData));
            System.out.print(new String(newData));
        }
    };

    public byte[] concatBuffers(byte[] first, byte[] second) {
        int aLen = first.length;
        int bLen = second.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(first, 0, c, 0, aLen);
        System.arraycopy(second, 0, c, aLen, bLen);
        return c;
    }
}
