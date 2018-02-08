package com.conduit.libdatalink;

import com.conduit.libdatalink.internal.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import static com.conduit.libdatalink.internal.Constants.*;

public class DataLink implements DataLinkInterface{

    private static final int MAX_PACKETS_IN_FLIGHT = ARDUINO_SERIAL_RX_BUFFER_SIZE / SerialPacket.PACKET_SIZE;

    private UsbDriverInterface usbDriver;
    private DataLinkListener dataLinkListener;

    private BlockingQueue<SerialPacket> processingQueue = new LinkedBlockingQueue<SerialPacket>();
    private SerialPacketParser serialPacketParser = new SerialPacketParser();
    private NetworkPacketParser networkPacketParser = new NetworkPacketParser();

    final Semaphore txOkSem = new Semaphore(MAX_PACKETS_IN_FLIGHT);
    private QueueConsumer queueConsumer = new QueueConsumer();
    private Thread consumerThread = new Thread(queueConsumer);

    public DataLink(UsbDriverInterface usbDriver) {
        this.usbDriver = usbDriver;
        this.usbDriver.setReadListener(usbSerialListener);
        this.consumerThread.start();
    }

    public void debugLEDBlink(byte numBlinks) {
        processingQueue.add(new SerialPacket(
                COMMAND_DEBUG_LED_BLINK,
                new byte[] {numBlinks}
        ));
    }

    public void debugEcho(byte value) {
        processingQueue.add(new SerialPacket(
                COMMAND_DEBUG_ECHO,
                new byte[] {value}
        ));
    }

    public void debugEcho(byte[] payload) {
        List<SerialPacket> packets = PacketGenerator.generateSerialPackets(
                COMMAND_DEBUG_ECHO,
                new NetworkPacket(
                        (byte) 1,
                        payload
                )
        );

        System.out.println("[DATALINK] Enqueued " + packets.size() + " SerialPackets");
        processingQueue.addAll(packets);
    }

    public void openWritingPipe(int address) {
        processingQueue.add(new SerialPacket(
                COMMAND_OPEN_WRITING_PIPE,
                Utils.intToBytes(address)
        ));
    }

    public void openReadingPipe(byte pipeNumber, int address) {
        processingQueue.add(new SerialPacket(
                COMMAND_OPEN_READING_PIPE,
                Utils.intToBytes(address)
        ));
    }

    public void write(byte[] payload) {
        processingQueue.addAll(PacketGenerator.generateSerialPackets(
                COMMAND_WRITE,
                new NetworkPacket((byte) 1, payload)
        ));
    }

    class QueueConsumer implements Runnable {
        public void run() {
            System.out.println("Starting Datalink Queue Consumer");
            try {
                while (true) {
                    // wait till safe to send, then send single packet
                    txOkSem.acquire();
                    consume(processingQueue.take());
                }
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }

        void consume(SerialPacket packet) {
            usbDriver.sendBuffer(packet.getPacketByteBuffer().array());
        }
    }

    public void setReadListener(DataLinkListener listener) {
        dataLinkListener = listener;
    }

    private UsbSerialListener usbSerialListener = new UsbSerialListener() {
        @Override
        public void OnReceiveData(byte[] data) {
            try {
                serialPacketParser.addBytes(data);

                if (serialPacketParser.isPacketReady()) {

                    // Allow consumer to TX the next packet
                    txOkSem.release();

                    SerialPacket packet = serialPacketParser.getPacket();
                    byte[] payload = new byte[packet.getPayloadSize()];
                    packet.getPacketPayload(payload);

                    System.out.println("[DataLink] SerialPacket Ready: " + Arrays.toString(payload));
                    System.out.println("[DataLink] SerialPacket Ready: " + new String(payload));

                    if (packet.getCommandId() == COMMAND_READ) {
                        System.out.println("[DataLink] Packet Source: Radio");

                        // TODO: Handle packets from multiple remote radios
                        // This packet came from the radio
                        networkPacketParser.addBytes(payload);

                        if (networkPacketParser.isPacketReady()) {
                            System.out.println("[DataLink] NetworkPacket ready");

                            NetworkPacket networkPacket = networkPacketParser.getPacket();
                            byte[] networkPayload = new byte[networkPacket.getPayloadSize()];
                            networkPacket.getPacketPayload(networkPayload);

                            // TODO: Update this to return generic byte[] data as well
                            dataLinkListener.OnReceiveData(new String(networkPayload));
                        }

                    } else {
                        // This packet came from the Arduino
                        // TODO: Update this to return generic byte[] data as well
                        dataLinkListener.OnReceiveData(new String(payload));
                    }
                }

            } catch (Exception e) {
                // We MUST capture all exceptions here - otherwise errors are bubbled up to the UsbDriver and
                // silently squashed by the serial library
                System.out.println("[DATALINK] Exception Occurred " + e.toString());
            }
        }
    };

}
