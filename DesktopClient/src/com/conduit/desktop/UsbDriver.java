package com.conduit.desktop;

import com.conduit.libdatalink.*;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.util.Arrays;

public class UsbDriver implements UsbDriverInterface {

    private SerialPort comPort;
    private UsbSerialListener usbSerialListener;

    public UsbDriver(String port) {

        if (port == null) {
            SerialPort ports[] = SerialPort.getCommPorts();
            comPort = ports[ports.length - 2];
        } else {
            comPort = SerialPort.getCommPort(port);
        }

        comPort.setBaudRate(9600);

        System.out.println(comPort.getSystemPortName());

        comPort.openPort();
        comPort.addDataListener(serialPortDataListener);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private SerialPortDataListener serialPortDataListener = new SerialPortDataListener() {

        @Override
        public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;

            byte[] newData = new byte[comPort.bytesAvailable()];
            int numRead = comPort.readBytes(newData, newData.length);
            System.out.println("[USBDRIVER] Read " + numRead + " bytes: " + Arrays.toString(newData) + " " + new String(newData));
            if (usbSerialListener != null) usbSerialListener.OnReceiveData(newData);
        }
    };

    @Override
    public void sendBuffer(byte[] buf) {
        System.out.println("[USBDRIVER] Sending: " + Arrays.toString(buf));
        comPort.writeBytes(buf, buf.length);
    }

    @Override
    public void setReadListener(UsbSerialListener listener) {
        this.usbSerialListener = listener;
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
