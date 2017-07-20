package com.conduit.desktop;

import com.conduit.libdatalink.DataLink;
import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static Scanner in = new Scanner(System.in);

    private static List<String> serialPorts;
    private static int portNumber = -1;

    public static void main(String[] args) {

        // Dynamically choose port if not set at compile time
        if (portNumber == -1) {
            serialPorts = getSerialPortList();
            System.out.println("Choose a serial port: ");
            for (int i = 0; i < serialPorts.size(); i++) {
                System.out.println(String.format("[%d] %s",
                    i,
                    serialPorts.get(i)
                ));
            }

            portNumber = in.nextInt();
        }

        DataLink dataLink = new DataLink(new UsbDriver(serialPorts.get(portNumber)));

        // Only the last byte should differ
        int addrA = 0xCDABCD71;
        int addrB = 0xCDABCD69;

        int remote = 0;

        // Assuming ports are off by one
        if (portNumber % 2 == 0) {
            System.out.println("This is radio A");
            dataLink.openWritingPipe((byte)addrA);
            dataLink.openReadingPipe((byte)1, (byte)addrB);
            remote = addrB;
        } else {
            System.out.println("This is radio B");
            dataLink.openWritingPipe((byte)addrB);
            dataLink.openReadingPipe((byte)1, (byte)addrA);
            remote = addrA;
        }

        System.out.println("Ready to Tx/Rx");

        while(true) {
            String line = in.nextLine();
            if (line.length() > 1) {
                System.out.println("Transmitting: " + line);
//                dataLink.openWritingPipe(remote);
                dataLink.write(line.getBytes());
//                dataLink.openReadingPipe((byte)1, remote);
            }
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
}
