package com.conduit.libdatalink.stats;

import com.conduit.libdatalink.internal.SerialPacket;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import static com.conduit.libdatalink.internal.Constants.*;

public class StatsCollector {
    public static final int STATS_BUCKET_LATENCY = 0;
    public static final int STATS_BUCKET_THROUGHPUT = 1;

    // need synchronization
    Hashtable<SerialPacket, Long> queueTime = new Hashtable<SerialPacket, Long>();

    StatsHolder statsQueueWait = new StatsHolder("ms");
    StatsHolder statsSerialRoundTripLatency = new StatsHolder("ms");
    StatsHolder statsSerialBandwidth = new StatsHolder("B/s");
    StatsHolder statsNetworkRoundTripLatency = new StatsHolder("ms");
    StatsHolder statsNetworkBandwidth = new StatsHolder("B/s");

    byte currentPacketCommand = 0;
    long currentPacketTxStart = -1;

    public void enqueuePacket(SerialPacket packet) {
        queueTime.put(packet, System.currentTimeMillis());
    }

    public void enqueuePackets(Collection<SerialPacket> c) {
        for (SerialPacket packet : c) enqueuePacket(packet);
    }

    public void packetTx(SerialPacket packet) {
        currentPacketTxStart = System.currentTimeMillis();
        currentPacketCommand = packet.getCommandId();

        if (!queueTime.contains(packet)) System.out.println("Not in Queue :(");

        long enqueued = queueTime.remove(packet);
        statsQueueWait.addMeasure(currentPacketTxStart - enqueued);
    }

    public void packetAck(SerialPacket packet) {
        long now = System.currentTimeMillis();

        float delta = now - currentPacketTxStart;

        if (currentPacketCommand == COMMAND_WRITE) {
            // Sent over the air
            statsNetworkRoundTripLatency.addMeasure(delta);
            statsNetworkBandwidth.addMeasure((float)SerialPacket.PAYLOAD_SIZE * 1000.0f / delta); //TODO: Send Tx time from Arduino (in packet payload)
        } else {
            // Sent to Arduino only
            statsSerialRoundTripLatency.addMeasure(delta);
            statsSerialBandwidth.addMeasure((float)SerialPacket.PACKET_SIZE * 2.0f * 1000.0f / delta);
        }

    }

    public void printStats() {
        System.out.println("Queue Wait Time");
        statsQueueWait.printStats();

        System.out.println("Serial Round Trip Latency");
        statsSerialRoundTripLatency.printStats();

        System.out.println("Serial Round Trip Bandwidth");
        statsSerialBandwidth.printStats();
    }
}
