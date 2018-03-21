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

    public void enqueueSerialPacket(SerialPacket packet) {
        queueTime.put(packet, System.currentTimeMillis());
    }

    public void enqueueSerialPackets(Collection<SerialPacket> c) {
        for (SerialPacket packet : c) enqueueSerialPacket(packet);
    }

    public void serialPacketTx(SerialPacket packet) {
        currentPacketTxStart = System.currentTimeMillis();
        currentPacketCommand = packet.getCommandId();

        if (!queueTime.containsKey(packet)){
            System.out.println("[StatsCollector] Packet not found on queue. CommandId: " + packet.getCommandId());
            return;
        }

        long enqueued = queueTime.remove(packet);
        statsQueueWait.addMeasure(currentPacketTxStart - enqueued);
    }

    public void serialPacketAck(SerialPacket packet) {
        float delta = System.currentTimeMillis() - currentPacketTxStart;

        // Sent to Arduino only
        statsSerialRoundTripLatency.addMeasure(delta);
        statsSerialBandwidth.addMeasure((float)SerialPacket.PACKET_SIZE * 2.0f * 1000.0f / delta);
    }

    public void networkTxComplete(float us) {
        statsNetworkBandwidth.addMeasure((float)SerialPacket.PAYLOAD_SIZE * 1e6f / us);
        statsNetworkRoundTripLatency.addMeasure(us / 1000f);
    }

    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("Queue Wait Time\n");
        sb.append(statsQueueWait.getStats());
        sb.append("\n");

        sb.append("Serial Round Trip Latency\n");
        sb.append(statsSerialRoundTripLatency.getStats());
        sb.append("\n");

        sb.append("Serial Round Trip Bandwidth\n");
        sb.append(statsSerialBandwidth.getStats());
        sb.append("\n");

        sb.append("Network Round Trip Latency\n");
        sb.append(statsNetworkRoundTripLatency.getStats());
        sb.append("\n");

        sb.append("Network Round Trip Bandwidth\n");
        sb.append(statsNetworkBandwidth.getStats());
        sb.append("\n");

        return sb.toString();
    }
}
