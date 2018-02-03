package com.conduit.libdatalink;

import com.conduit.libdatalink.internal.NetworkPacket;
import com.conduit.libdatalink.internal.NetworkPacketParser;
import com.conduit.libdatalink.internal.Utils;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.conduit.libdatalink.internal.Constants.*;

public class DataLink implements DataLinkInterface{

    private UsbDriverInterface usbDriver;
    private DataLinkListener dataLinkListener;

    private BlockingQueue<NetworkPacket> processingQueue = new LinkedBlockingQueue<NetworkPacket>();
    private NetworkPacketParser packetParser = new NetworkPacketParser();

    private QueueConsumer queueConsumer = new QueueConsumer();
    private Thread consumerThread = new Thread(queueConsumer);

    public DataLink(UsbDriverInterface usbDriver) {
        this.usbDriver = usbDriver;
        this.usbDriver.setReadListener(usbSerialListener);
        this.consumerThread.start();
    }

    public void debugLEDBlink(byte numBlinks) {
        processingQueue.add(new NetworkPacket(
                COMMAND_DEBUG_LED_BLINK,
                new byte[] {numBlinks}
        ));
    }

    public void debugEcho(byte value) {
        processingQueue.add(new NetworkPacket(
                COMMAND_DEBUG_ECHO,
                new byte[] {value}
        ));
    }

    public void openWritingPipe(int address) {
        processingQueue.add(new NetworkPacket(
                COMMAND_OPEN_WRITING_PIPE,
                Utils.intToBytes(address)
        ));
    }

    public void openReadingPipe(byte pipeNumber, int address) {
        processingQueue.add(new NetworkPacket(
                COMMAND_OPEN_READING_PIPE,
                Utils.intToBytes(address)
        ));
    }

    public void write(byte[] payload) {
        processingQueue.add(new NetworkPacket(
                COMMAND_WRITE,
                payload
        ));
    }

    class QueueConsumer implements Runnable {
        public void run() {
            System.out.println("Starting Datalink Queue Consumer");
            try {
                while (true) {
                    consume(processingQueue.take());
                }
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }

        void consume(NetworkPacket packet) {
            // TODO: Send packets in chunks (128B at a time)
            usbDriver.sendBuffer(packet.getPacketByteBuffer().array());
        }
    }

    public void setReadListener(DataLinkListener listener) {
        dataLinkListener = listener;
    }

    private UsbSerialListener usbSerialListener = new UsbSerialListener() {
        @Override
        public void OnReceiveData(byte[] data) {
            System.out.println("Got Data");
            System.out.println(Arrays.toString(data));
            packetParser.addBytes(data);

            if (packetParser.isPacketReady()) {
                System.out.println("Packet Ready!");
                NetworkPacket packet = packetParser.getPacket();
                byte[] payload = new byte[packet.getPayloadSize()];
                packet.getPacketPayload(payload);

                // TODO: Update this to return generic byte[] data as well
                dataLinkListener.OnReceiveData(new String(payload));
            }
        }
    };

}
