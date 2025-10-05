package com.mars_sim.core.metrics;

/**
 * Calculator that counts and sums the elements
 */
public class Total implements Calculator {

    private int count;
    private double sum;

    public int getCount() {
        return count;
    }

    public double getSum() {
        return sum;
    }

    @Override
    public void accept(DataPoint value) {
        sum += value.getValue();
        count++;
    }

}
