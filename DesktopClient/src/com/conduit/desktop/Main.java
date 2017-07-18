package com.conduit.desktop;

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

        DataLink dataLink = new DataLink(serialPorts.get(portNumber));

        // Assuming ports are off by one
        if (portNumber % 2 == 0) {
            System.out.println("This is radio A");
            dataLink.openReadingPipe((byte)1, (byte)'b');
            dataLink.openWritingPipe((byte)'a');
        } else {
            System.out.println("This is radio B");
            dataLink.openReadingPipe((byte)1, (byte)'a');
            dataLink.openWritingPipe((byte)'b');
        }

        System.out.println("Ready to Tx/Rx");

        while(true) {
            String line = in.nextLine();
            if (line.length() > 1) {
                System.out.println("Transmitting: " + line);
                dataLink.write(line.getBytes());
            }
        }
    }

    private static List<String> getSerialPortList() {
        List<String> ports = new ArrayList<String>();

        SerialPort serialPorts[] = SerialPort.getCommPorts();

        for (SerialPort serialPort : serialPorts) {
            String name = serialPort.getSystemPortName();
            if (name.contains("cu.usbmodem")){
                ports.add(serialPort.getSystemPortName());
            }
        }

        return ports;
    }
}
