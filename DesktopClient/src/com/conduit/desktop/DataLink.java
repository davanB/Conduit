package com.conduit.desktop;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.util.Arrays;

public class DataLink {

    private static final byte COMMAND_HEADER = 16;

    private static final byte COMMAND_DEBUG_LED_BLINK = 100;
    private static final byte COMMAND_OPEN_WRITING_PIPE = 125;
    private static final byte COMMAND_OPEN_READING_PIPE = 126;
    private static final byte COMMAND_WRITE = 127;

    private static final byte ERROR_INVALID_COMMAND = 1;


    SerialPort comPort;


    DataLinkListener dataLinkListener;

    public DataLink() {
//        comPort = SerialPort.getCommPort("cu.usbmodem1421");
//
//        for (SerialPort port : SerialPort.getCommPorts()) {
//            System.out.println(port.getSystemPortName());
//        }
//
        SerialPort ports[] = SerialPort.getCommPorts();
        comPort = ports[ports.length - 2];

        comPort.openPort();
        comPort.addDataListener(serialPortDataListener);
    }


    void debugLEDBlink(byte numBlinks) {
        byte buf[] = { COMMAND_HEADER, COMMAND_DEBUG_LED_BLINK, numBlinks };
        System.out.println(Arrays.toString(buf));
        comPort.writeBytes(buf, buf.length);
    }

    void openWritingPipe() {
    }

    void openReadingPipe() {
    }

    void write() {
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
            System.out.println("Read " + numRead + " bytes.");
        }
    };
}
