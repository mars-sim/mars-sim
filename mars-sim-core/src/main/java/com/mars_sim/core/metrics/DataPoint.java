/*
 * Mars Simulation Project
 * DataPoint.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.metrics;

import com.mars_sim.core.time.MarsTime;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a single data point in a metric time series, containing
 * a timestamp and a numeric value.
 * This equality of this object is based only on the 'when'. This means two data points at the
 * same time are considered equal, even if their values differ.
 */
public class DataPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MarsTime when;
    private final double value;
    
    /**
     * Creates a new DataPoint.
     * 
     * @param when The timestamp when this data point was recorded
     * @param value The numeric value of this data point
     */
    public DataPoint(MarsTime when, double value) {
        this.when = Objects.requireNonNull(when, "Timestamp cannot be null");
        this.value = value;
    }
    
    /**
     * Gets the timestamp of this data point.
     * 
     * @return The MarsTime timestamp
     */
    public MarsTime getWhen() {
        return when;
    }
    
    /**
     * Gets the value of this data point.
     * 
     * @return The numeric value
     */
    public double getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DataPoint dataPoint = (DataPoint) obj;
        return Objects.equals(when, dataPoint.when);
    }
    
    @Override
    public int hashCode() {
        return when.hashCode();
    }

    @Override
    public String toString() {
        return String.format("DataPoint{when=%s, value=%.2f}", when, value);
    }
}