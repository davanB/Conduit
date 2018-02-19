package com.conduit.desktop;

import com.conduit.libdatalink.DataLink;
import com.conduit.libdatalink.DataLinkListener;
import com.fazecast.jSerialComm.SerialPort;

import javax.xml.crypto.Data;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static final int ACTION_CONNECT = 0;
    private static final int ACTION_SEND_STRING = 1;
    private static final int ACTION_SERIAL_THROUGHPUT = 2;
    private static final int ACTION_STATS = 9;

    private static Scanner in = new Scanner(System.in);

    private static List<String> serialPorts;
    private static int portNumber = -1;

    private static DataLink dataLink;

    private static int remote = -1;

    public static void main(String[] args) {

        serialPorts = getSerialPortList();
        System.out.println("Choose a serial port: ");
        for (int i = 0; i < serialPorts.size(); i++) {
            System.out.println(String.format("[%d] %s",
                    i,
                    serialPorts.get(i)
            ));
        }

        // Dynamically choose port if not set at compile time
        if (portNumber == -1) portNumber = in.nextInt();

        dataLink = new DataLink(new UsbDriver(serialPorts.get(portNumber)));
        dataLink.setReadListener(new DataLinkListener() {
            @Override
            public void OnReceiveData(int originAddress, byte payloadType, ByteBuffer payload) {
                byte[] buf = new byte[payload.remaining()];
                payload.get(buf);
                System.out.println(String.format("0x%08X %1s", originAddress, new String(buf)));
            }
        });

        connect();
        sendString();

//        while(true) {
//            System.out.println("Choose Action: ");
//            System.out.println(String.format("[%d] Connect", ACTION_CONNECT));
//            System.out.println(String.format("[%d] Send String", ACTION_SEND_STRING));
//            System.out.println(String.format("[%d] Serial Throughput", ACTION_SERIAL_THROUGHPUT));
//            System.out.println(String.format("[%d] Dump Stats", ACTION_STATS));
//
//            int action = in.nextInt();
//            switch (action) {
//                case ACTION_CONNECT:
//                    connect();
//                    break;
//                case ACTION_SEND_STRING:
//                    sendString();
//                    break;
//                case ACTION_SERIAL_THROUGHPUT:
//                    serialThroughput();
//                    dataLink.statsCollector.printStats();
//                    break;
//                case ACTION_STATS:
//                    dataLink.statsCollector.printStats();
//                    break;
//            }
//
//        }
    }

    private static void connect() {
        // Only the last byte should differ
        int addrA = 0xCDABCD71;
        int addrB = 0xCDABCD69;

        // Assuming ports are off by one
        if (portNumber % 2 == 0) {
            System.out.println("This is radio A");
            dataLink.openWritingPipe(addrA);
            dataLink.openReadingPipe((byte)1, addrB);
            remote = addrB;
        } else {
            System.out.println("This is radio B");
            dataLink.openWritingPipe(addrB);
            dataLink.openReadingPipe((byte)1, addrA);
            remote = addrA;
        }
        System.out.println("Ready to Tx/Rx");
    }

    private static void sendString() {
        String line = "";
        while (!line.equals("exit")) {
            System.out.println("Type text to send (or exit)");
            line = in.nextLine();
            if (line.length() < 1) continue;
            System.out.println("Transmitting: " + line);
            dataLink.write((byte) 1, line.getBytes());
        }
    }

    private static void serialThroughput() {
        byte[] PAYLOAD = new byte[1024 * 5];
        new Random().nextBytes(PAYLOAD);
        dataLink.debugEcho(PAYLOAD);
    }

    private static List<String> getSerialPortList() {
        List<String> ports = new ArrayList<String>();

        SerialPort serialPorts[] = SerialPort.getCommPorts();

        for (SerialPort serialPort : serialPorts) {
            String name = serialPort.getSystemPortName();
            if (name.contains("cu.usbmodem") || name.contains("cu.usbserial")){
                ports.add(serialPort.getSystemPortName());
            }
        }
        return ports;
    }
}
