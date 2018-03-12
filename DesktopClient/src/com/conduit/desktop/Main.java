package com.conduit.desktop;

import com.conduit.libdatalink.ConduitGroup;
import com.conduit.libdatalink.DataLink;
import com.conduit.libdatalink.DataLinkListener;
import com.conduit.libdatalink.conduitabledata.*;
import com.fazecast.jSerialComm.SerialPort;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

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
    private static final int ACTION_CONDUIT_CONNECTION_EVENT = 5;
    private static final int ACTION_CONDUIT_GROUP_CONNECTION_FLOW_M = 6;
    private static final int ACTION_CONDUIT_GROUP_CONNECTION_FLOW_S = 7;
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

//        connect();
//        sendString();

        while(true) {
            System.out.println("Choose Action: ");
            System.out.println(String.format("[%d] Connect", ACTION_CONNECT));
            System.out.println(String.format("[%d] Send String", ACTION_SEND_STRING));
            System.out.println(String.format("[%d] Send Image", ACTION_SEND_IMAGE));
            System.out.println(String.format("[%d] Serial Throughput", ACTION_SERIAL_THROUGHPUT));
            System.out.println(String.format("[%d] Network Throughput", ACTION_NETWORK_THROUGHPUT));
            System.out.println(String.format("[%d] CCE", ACTION_CONDUIT_CONNECTION_EVENT));
            System.out.println(String.format("[%d] Conduit Group Connection Flow (M)", ACTION_CONDUIT_GROUP_CONNECTION_FLOW_M));
            System.out.println(String.format("[%d] Conduit Group Connection Flow (S)", ACTION_CONDUIT_GROUP_CONNECTION_FLOW_S));
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
                case ACTION_CONDUIT_CONNECTION_EVENT:
                    conduitConnectionEvent();
                    break;
                case ACTION_CONDUIT_GROUP_CONNECTION_FLOW_M:
                    conduitGroupFlow(true);
                    break;
                case ACTION_CONDUIT_GROUP_CONNECTION_FLOW_S:
                    conduitGroupFlow(false);
                    break;
                case ACTION_STATS:
                    dataLink.statsCollector.printStats();
                    break;
            }

        }
    }

    private static void connect() {
        // Only the last byte should differ
//        int addrA = 0xABCDEF01; // Me
//        int addrB = 0xABCDEF04;

        int addrA = 0xABCDEF00; // Me
        int addrB = 0xABCDEF20;

//        int addrA = 0xABCDEF27; // Me
//        int addrB = 0xABCDEF13;

        // Assuming ports are off by one
        if (portNumber % 2 == 0) {
            System.out.println("This is radio A");
//            dataLink.openReadingPipe((byte)1, 0xABCDEF00);
            dataLink.openReadingPipe((byte)1, 0xABCDEF01);
            dataLink.openReadingPipe((byte)2, 0xABCDEF02);
            dataLink.openReadingPipe((byte)3, 0xABCDEF03);
            dataLink.openReadingPipe((byte)4, 0xABCDEF04);
            dataLink.openReadingPipe((byte)5, 0xABCDEF05);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            dataLink.openWritingPipe(0xABCDEF20);
            remote = addrB;
        } else {
            System.out.println("This is radio B");
            dataLink.openReadingPipe((byte)1, 0xABCDEF20);
            dataLink.openReadingPipe((byte)2, 0xABCDEF21);
            dataLink.openReadingPipe((byte)3, 0xABCDEF23);
            dataLink.openReadingPipe((byte)4, 0xABCDEF24);
            dataLink.openReadingPipe((byte)5, 0xABCDEF25);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            dataLink.openWritingPipe(0xABCDEF01);
//            dataLink.openWritingPipe(0xABCDEF02); // TODO: WHY? This breaks.
            remote = addrA;
        }
//        System.out.println("This is radio A");
//        dataLink.openWritingPipe(0xABCDEF01);
//        dataLink.openReadingPipe((byte)1, 0xABCDEF10);
//        remote = addrA;

        System.out.println("Ready to Tx/Rx");
        addDataLinkListener();
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

    static int clientId = 1;
    private static void conduitConnectionEvent() {
        System.out.println("Sending Conduit Connection Event");
        ConduitConnectionEvent cce = new ConduitConnectionEvent(clientId, "Hello");
        ByteBuffer buff = cce.getPayload();
        dataLink.write((byte) 3, buff.array());
        clientId++;
    }

    private static void conduitGroupFlow(boolean master) {
        int masterClientId = 0;
        int slaveClientId = 1;

        if (master) {

            System.out.println("[Master] I am Master");

            ConduitGroup conduitGroup = new ConduitGroup(dataLink, 0xABCDEF00, masterClientId, 2);
            conduitGroup.addConduitableDataListener(ConduitableDataTypes.CONNECTION_EVENT, new Function1<ConduitableData, Unit>() {
                @Override
                public Unit invoke(ConduitableData conduitableData) {

                    System.out.println("[Master] Got client message");
                    conduitGroup.send(slaveClientId, new ConduitMessage("Hello, got ur message!"));

                    conduitGroup.addConduitableDataListener(ConduitableDataTypes.MESSAGE, new Function1<ConduitableData, Unit>() {
                        @Override
                        public Unit invoke(ConduitableData conduitableData) {

                            System.out.println("[Master] Flow complete!");

                            return null;
                        }
                    });

                    return null;
                }
            });


        } else {

            System.out.println("[Client] I am Client");

            ConduitGroup conduitGroup = new ConduitGroup(dataLink, 0xABCDEF00, slaveClientId, 2);
            conduitGroup.addConduitableDataListener(ConduitableDataTypes.MESSAGE, new Function1<ConduitableData, Unit>() {
                @Override
                public Unit invoke(ConduitableData conduitableData) {

                    System.out.println("[Client] Got Ledger");

                    ConduitMessage data = (ConduitMessage) conduitableData;
//                    conduitableData

                    // let master know that we're ready to go!
                    conduitGroup.send(0, new ConduitConnectionEvent(slaveClientId, "Aaron"));

                    return null;
                }
            });

            System.out.println("[Client] Sent Connection Event");
            conduitGroup.send(masterClientId, new ConduitConnectionEvent(slaveClientId, "Aaron"));
//            conduitGroup.send(masterClientId, new ConduitConnectionEvent(slaveClientId, "Aaron"));
        }

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

    private static void addDataLinkListener() {
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

            @Override
            public void OnSerialError(byte commandId, byte[] payload) {
                System.out.println("[Main] Serial Packet Error");
            }
        });
    }
}
