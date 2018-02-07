package com.conduit.libdatalink;

import com.conduit.libdatalink.internal.SerialPacket;
import com.conduit.libdatalink.internal.SerialPacketParser;
import com.conduit.libdatalink.internal.Utils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.conduit.libdatalink.internal.Constants.*;

public class DataLink implements DataLinkInterface{

    private UsbDriverInterface usbDriver;
    private DataLinkListener dataLinkListener;

    private BlockingQueue<SerialPacket> processingQueue = new LinkedBlockingQueue<SerialPacket>();
    private SerialPacketParser serialPacketParser = new SerialPacketParser();

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

    public void debugEcho(String value) {
        if (value.getBytes().length > SerialPacket.PAYLOAD_SIZE) throw new IllegalArgumentException("string too big");
        processingQueue.add(new SerialPacket(
                COMMAND_DEBUG_ECHO,
                value.getBytes()
        ));
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
        throw new NotImplementedException();
        //TODO: generate serialpackets from payload
//        processingQueue.add(new SerialPacket(
//                COMMAND_WRITE,
//                payload
//        ));
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

        void consume(SerialPacket packet) {
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
            serialPacketParser.addBytes(data);

            if (serialPacketParser.isPacketReady()) {
                SerialPacket packet = serialPacketParser.getPacket();
                byte[] payload = new byte[packet.getPayloadSize()];
                packet.getPacketPayload(payload);

                System.out.println("Packet Ready: " + Arrays.toString(payload));
                System.out.println("Packet Ready: " + new String(payload));

                // TODO: Update this to return generic byte[] data as well
                dataLinkListener.OnReceiveData(new String(payload));
            }
        }
    };

}
