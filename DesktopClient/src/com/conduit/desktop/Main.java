package com.conduit.desktop;

import com.conduit.libdatalink.DataLink;
import com.conduit.libdatalink.DataLinkListener;
import com.fazecast.jSerialComm.SerialPort;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static final int ACTION_CONNECT = 0;
    private static final int ACTION_SEND_STRING = 1;
    private static final int ACTION_SEND_IMAGE = 2;
    private static final int ACTION_SERIAL_THROUGHPUT = 3;
    private static final int ACTION_NETWORK_THROUGHPUT = 4;
    private static final int ACTION_STATS = 9;

    private static final byte PACKET_TYPE_DEBUG_JUNK = 1;
    private static final byte PACKET_TYPE_DEBUG_IMAGE = 3;

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
        dataLink.addReadListener(new DataLinkListener() {
            @Override
            public void OnReceiveData(int originAddress, byte payloadType, ByteBuffer payload) {
                byte[] buf = new byte[payload.remaining()];
                payload.get(buf);

                switch (payloadType) {
                    case PACKET_TYPE_DEBUG_IMAGE:
                        File fi = new File("tmp/" + System.currentTimeMillis() + ".jpg");
                        fi.getParentFile().mkdirs();
                        try {
                            Files.write(fi.toPath(), buf);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        System.out.println(String.format("0x%08X %1s", originAddress, new String(buf)));
                        break;
                }
            }
        });

        connect();
//        sendString();

        while(true) {
            System.out.println("Choose Action: ");
            System.out.println(String.format("[%d] Connect", ACTION_CONNECT));
            System.out.println(String.format("[%d] Send String", ACTION_SEND_STRING));
            System.out.println(String.format("[%d] Send Image", ACTION_SEND_IMAGE));
            System.out.println(String.format("[%d] Serial Throughput", ACTION_SERIAL_THROUGHPUT));
            System.out.println(String.format("[%d] Network Throughput", ACTION_NETWORK_THROUGHPUT));
            System.out.println(String.format("[%d] Dump Stats", ACTION_STATS));

            int action = in.nextInt();
            switch (action) {
                case ACTION_CONNECT:
                    connect();
                    break;
                case ACTION_SEND_STRING:
                    sendString();
                    break;
                case ACTION_SEND_IMAGE:
                    sendImage();
                    break;
                case ACTION_SERIAL_THROUGHPUT:
                    serialThroughput();
                    dataLink.statsCollector.printStats();
                    break;
                case ACTION_NETWORK_THROUGHPUT:
                    networkThroughput();
                    dataLink.statsCollector.printStats();
                    break;
                case ACTION_STATS:
                    dataLink.statsCollector.printStats();
                    break;
            }

        }
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

    private static void sendImage() {
        try {
            File fi = new File("res/img-9k.jpg");
            byte[] fileContent = Files.readAllBytes(fi.toPath());
            dataLink.write(PACKET_TYPE_DEBUG_IMAGE, fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void serialThroughput() {
        byte[] PAYLOAD = new byte[1024 * 2];
        new Random().nextBytes(PAYLOAD);
        dataLink.debugEcho(PAYLOAD);
    }

    private static void networkThroughput() {
        byte[] PAYLOAD = new byte[1024 * 2];
        new Random().nextBytes(PAYLOAD);
        dataLink.write((byte) 1, PAYLOAD);
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
