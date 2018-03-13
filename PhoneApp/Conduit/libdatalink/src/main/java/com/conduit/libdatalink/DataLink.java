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

//    private static final int MAX_PACKETS_IN_FLIGHT = ARDUINO_SERIAL_RX_BUFFER_SIZE / SerialPacket.PACKET_SIZE;
    private static final int MAX_PACKETS_IN_FLIGHT = 1;

    private UsbDriverInterface usbDriver;
    private List<DataLinkListener> dataLinkObservers = new ArrayList<DataLinkListener>();

    private BlockingQueue<SerialPacket> processingQueue = new LinkedBlockingQueue<SerialPacket>();
    private SerialPacket inFlightPacket = null;
    private SerialPacket retryPacket = null;
    private SerialPacketParser serialPacketParser = new SerialPacketParser();
    private Map<Byte, NetworkPacketParser> networkPacketParsers = new HashMap<Byte, NetworkPacketParser>();


    final Semaphore txOkSem = new Semaphore(MAX_PACKETS_IN_FLIGHT);
    private QueueConsumer queueConsumer = new QueueConsumer();
    private Thread consumerThread = new Thread(queueConsumer);

    private int groupAddress = -1;

    private StatsCollector statsCollector = new StatsCollector();

    public DataLink(UsbDriverInterface usbDriver) {
        this.usbDriver = usbDriver;
        this.usbDriver.setReadListener(usbSerialListener);
        this.consumerThread.start();
    }

    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("Queue Length: \t").append(processingQueue.size()).append('\n');
        sb.append(statsCollector.getStats());
        return sb.toString();
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

        // Create a new packetparser for remote CLIENT
        byte lsb = (byte)(address & 0x0000000F);
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
                    if (retryPacket != null) {
                        System.out.println("Retrying SerialPacket!");
                        consume(retryPacket);
                        retryPacket = null;
                    } else {
                        consume(processingQueue.take());
                    }
                }
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }

        void consume(SerialPacket packet) {
            statsCollector.serialPacketTx(packet);
            inFlightPacket = packet;
            usbDriver.sendBuffer(packet.getPacketByteBuffer().array());
        }
    }

    @Override
    public void addReadListener(DataLinkListener listener) {
        if(!dataLinkObservers.contains(listener)){
            dataLinkObservers.add(listener);
        }
    }

    @Override
    public void removeReadListener(DataLinkListener listener) {
        if(dataLinkObservers.contains(listener)){
            dataLinkObservers.remove(listener);
        }
    }

    private void notifyDataLinkObservers(int originAddress, byte payloadType, ByteBuffer payload) {
        for(DataLinkListener observer: dataLinkObservers) {
            if(observer != null ) {
                observer.OnReceiveData(originAddress, payloadType, payload);
            }
        }
    }

    private void notifyDataLinkObservers(byte commandId, byte[] payload) {
        for(DataLinkListener observer: dataLinkObservers) {
            if(observer != null ) {
                observer.OnSerialError(commandId, payload);
            }
        }
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

                    System.out.println("[DataLink] SerialPacket Ready: \n" + serialPacket.toString());

                    if (serialPacket.getCommandId() == COMMAND_READ) {
                        System.out.println("[DataLink] Packet Source: Radio");

                        // This packet came from the radio - get the appropriate NetworkPacketParser
                        NetworkPacketParser networkPacketParser = networkPacketParsers.get(serialPacket.getSource());

                        if (networkPacketParser == null) {
                            System.out.println("[DataLink] [Error] Received data from unexpected address " + String.format(
                                    "0x%08X",
                                    (0xFFFFFF00 & groupAddress) | (0x0000000F & serialPacket.getSource())
                            ));
                            return;
                        }

                        networkPacketParser.addBytes(payload);

                        if (networkPacketParser.isPacketReady()) {
                            System.out.println("[DataLink] NetworkPacket ready");

                            NetworkPacket networkPacket = networkPacketParser.getPacket();
                            notifyDataLinkObservers(
                                    (0xFFFFFF00 & groupAddress) | (0x0000000F & serialPacket.getSource()), // Combine group and client id to get address
                                    networkPacket.getPayloadType(),
                                    networkPacket.getPacketPayload()
                            );
                        }

                    } else {
                        // This packet came from the Arduino
                        statsCollector.serialPacketAck(serialPacket);

                        // Action on packet status
                        if (serialPacket.getStatus() == STATUS_SUCCESS) {

                            switch (serialPacket.getCommandId()) {
                                case COMMAND_WRITE:
                                    ByteBuffer serialPacketPayload = serialPacket.getPacketPayload();
                                    statsCollector.networkTxComplete(serialPacketPayload.getInt());
                                    serialPacketPayload.getShort(); // ACK value from remote
                                    break;
                            }

                        } else if (serialPacket.getStatus() == STATUS_FAILURE) {

                            switch (serialPacket.getCommandId()) {
                                case COMMAND_WRITE:
                                    // Apply serial packet retry policy
                                    // TODO: This only works with MAX_PACKETS_IN_FLIGHT == 1
                                    retryPacket = inFlightPacket;
                                    inFlightPacket = null;
                                    break;
                            }

                            // We only notify on failed operations - success have too much noise
                            notifyDataLinkObservers(serialPacket.getCommandId(), payload);
                        }

                        // This is an ACK SerialPacket for some command - OK to send next command
                        txOkSem.release();

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
