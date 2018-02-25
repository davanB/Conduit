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

    public void printStats() {
        System.out.println("\tMin:   " + min + " " + units);
        System.out.println("\tMax:   " + max + " " + units);
        System.out.println("\tLast:  " + last + " " + units);
        System.out.println("\tAvg:   " + average + " " + units);
        System.out.println("\tCount: " + count);
    }
}
