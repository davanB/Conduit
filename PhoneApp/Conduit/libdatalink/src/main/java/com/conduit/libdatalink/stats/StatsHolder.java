package com.conduit.libdatalink.stats;

public class StatsHolder {

    //min, max, last, average
    float min = Float.POSITIVE_INFINITY;
    float max = Float.NEGATIVE_INFINITY;
    float last;
    float average;
    int count;

    String units;

    public StatsHolder(String units) {
        this.units = units;
    }

    public void addMeasure(float value) {
        count++;
        last = value;
        min = Math.min(min, value);
        max = Math.max(max, value);

        // https://dsp.stackexchange.com/a/1187
        average = average + (value-average)/(float) count;
    }

    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("\tMin:   \t" ).append(min).append(" ").append(units).append("\n");
        sb.append("\tMax:   \t" ).append(max).append(" ").append(units).append("\n");
        sb.append("\tLast:  \t" ).append(last).append(" ").append(units).append("\n");
        sb.append("\tAvg:   \t" ).append(average).append(" ").append(units).append("\n");
        sb.append("\tCount: \t" ).append(count).append(" ").append("\n");
        return sb.toString();
    }
}
