package com.conduit.libdatalink;

import com.conduit.libdatalink.internal.*;
import com.conduit.libdatalink.stats.StatsCollector;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import static com.conduit.libdatalink.internal.Constants.*;
import static com.conduit.libdatalink.internal.SerialPacket.*;

public class DataLink implements DataLinkInterface {

    private static final int MAX_PACKETS_IN_FLIGHT = ARDUINO_SERIAL_RX_BUFFER_SIZE / SerialPacket.PACKET_SIZE;

    private UsbDriverInterface usbDriver;
    private DataLinkListener dataLinkListener;

    private BlockingQueue<SerialPacket> processingQueue = new LinkedBlockingQueue<SerialPacket>();
    private SerialPacketParser serialPacketParser = new SerialPacketParser();
    private Map<Byte, NetworkPacketParser> networkPacketParsers = new HashMap<Byte, NetworkPacketParser>();


    final Semaphore txOkSem = new Semaphore(MAX_PACKETS_IN_FLIGHT);
    private QueueConsumer queueConsumer = new QueueConsumer();
    private Thread consumerThread = new Thread(queueConsumer);

    private int groupAddress = -1;
    public StatsCollector statsCollector = new StatsCollector();

    public DataLink(UsbDriverInterface usbDriver) {
        this.usbDriver = usbDriver;
        this.usbDriver.setReadListener(usbSerialListener);
        this.consumerThread.start();
    }

    public void debugLEDBlink(byte numBlinks) {
        SerialPacket packet = new SerialPacket(
                COMMAND_DEBUG_LED_BLINK,
                new byte[] {numBlinks}
        );
        statsCollector.enqueueSerialPacket(packet);
        processingQueue.add(packet);
    }

    public void debugEcho(byte value) {
        SerialPacket packet = new SerialPacket(
                COMMAND_DEBUG_ECHO,
                new byte[] {value}
        );
        statsCollector.enqueueSerialPacket(packet);
        processingQueue.add(packet);
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
        statsCollector.enqueueSerialPackets(packets);
        processingQueue.addAll(packets);
    }

    public void openWritingPipe(int address) {
        SerialPacket packet = new SerialPacket(
                COMMAND_OPEN_WRITING_PIPE,
                Utils.intToBytes(address)
        );
        statsCollector.enqueueSerialPacket(packet);
        processingQueue.add(packet);
    }

    public void openReadingPipe(byte pipeNumber, int address) {
        // Validate address
        if (groupAddress != -1) {
            if (groupAddress != (address & 0xFFFFFF00)) {
                //TODO: Surface this error correctly
                System.out.println("[DataLink] [ERROR] Invalid Address");
                return;
            }
        } else {
            groupAddress = address & 0xFFFFFF00;
            System.out.println("[DataLink] Registered Group Address " + String.format("0x%08X", groupAddress));
        }

        // Create a new packetparser for this address
        byte lsb = (byte)(address & 0x000000FF);
        networkPacketParsers.put(lsb, new NetworkPacketParser());

        byte a[] = Utils.intToBytes(address);
        SerialPacket packet = new SerialPacket(
                COMMAND_OPEN_READING_PIPE,
                new byte[] {pipeNumber, a[0], a[1], a[2], a[3]}
        );

        statsCollector.enqueueSerialPacket(packet);
        processingQueue.add(packet);
    }

    public void write(byte payloadType, byte[] payload) {
        List<SerialPacket> packets = PacketGenerator.generateSerialPackets(
                COMMAND_WRITE,
                new NetworkPacket(payloadType, payload)
        );

        System.out.println("[DATALINK] Enqueued " + packets.size() + " SerialPackets");
        statsCollector.enqueueSerialPackets(packets);
        processingQueue.addAll(packets);
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
            statsCollector.serialPacketTx(packet);
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

                    SerialPacket serialPacket = serialPacketParser.getPacket();
                    byte[] payload = new byte[serialPacket.getPayloadSize()];
                    serialPacket.getPacketPayload(payload);
                    statsCollector.serialPacketAck(serialPacket);

                    // Allow consumer to TX the next packet
                    txOkSem.release();

                    System.out.println("[DataLink] SerialPacket Ready: \n" + serialPacket.toString());

                    if (serialPacket.getCommandId() == COMMAND_READ) {
                        System.out.println("[DataLink] Packet Source: Radio");

                        // This packet came from the radio - get the appropriate NetworkPacketParser
                        NetworkPacketParser networkPacketParser = networkPacketParsers.get(serialPacket.getSource());

                        if (networkPacketParser == null) {
                            System.out.println("[DataLink] [Error] Received data from unexpected address " + String.format(
                                    "0x%08X",
                                    (0xFFFFFF00 & groupAddress) | (0x000000FF & serialPacket.getSource())
                            ));
                            return;
                        }

                        networkPacketParser.addBytes(payload);

                        if (networkPacketParser.isPacketReady()) {
                            System.out.println("[DataLink] NetworkPacket ready");

                            NetworkPacket networkPacket = networkPacketParser.getPacket();

                            if (dataLinkListener != null) {
                                dataLinkListener.OnReceiveData(
                                        (0xFFFFFF00 & groupAddress) | (0x000000FF & serialPacket.getSource()), // Combine group and source to get address
                                        networkPacket.getPayloadType(),
                                        networkPacket.getPacketPayload()
                                );
                            }
                        }

                    } else {
                        // This packet came from the Arduino
                        switch (serialPacket.getCommandId()) {
                            case COMMAND_WRITE:
                                ByteBuffer serialPacketPayload = serialPacket.getPacketPayload();
                                statsCollector.networkTxComplete(serialPacketPayload.getInt());
                                serialPacketPayload.getShort(); // ACK value from remote
                                break;
                        }

                        // TODO: Handle SerialPackets from Arduino
                        // TODO: Update this to return generic byte[] data as well
                        // if (dataLinkListener != null) dataLinkListener.OnReceiveData(0, (byte)0, ByteBuffer.wrap(payload));
                    }
                }

            } catch (Exception e) {
                // We MUST capture all exceptions here - otherwise errors are bubbled up to the UsbDriver and
                // silently squashed by the serial library
                System.out.println("[DATALINK] Exception Occurred " + e.toString());
                e.printStackTrace();
            }
        }
    };

}
