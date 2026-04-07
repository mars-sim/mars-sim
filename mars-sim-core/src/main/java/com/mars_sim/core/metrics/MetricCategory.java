/*
 * Mars Simulation Project
 * MetricCategory.java
 * @date 2026=01-08
 * @author Barry Evans
 */
package com.mars_sim.core.metrics;

import java.io.Serializable;

import com.mars_sim.core.Named;

/**
 * Represents a category for metrics, defining how data points are handled.
 * It defines the behviour for replacing existing data points when new data points
 * with the same timestamp are added.
 * Equality is based on name only.
 */
public class MetricCategory implements Comparable<MetricCategory>, Named, Serializable {
    private String name;
    private boolean replaceExist;

    /**
     * Creates a MetricCategory with the given name. The replaceExist flag is set to false, i.e. duplicate
     * data points are summed.
     * @param name Name of category
     */
    public MetricCategory(String name) {
        this(name, false);
    }  

    /**
     * Creates a MetricCategory with the given name and replaceExist flag.
     * @param name Name of category
     * @param replaceExist If true, existing DataPoints in a Metric using this Category are replaced
     */
    public MetricCategory(String name, boolean replaceExist) {
        this.name = name;
        this.replaceExist = replaceExist;
    }


    @Override
    public String getName() {
        return name;
    }

    /**
     * Indicates whether existing DataPoints in a Metric using this Category are replaced.
     */
    public boolean replaceExist() {
        return replaceExist;
    }

    /**
     * Compare this MetricCategory to another based on name
     * @param o Other MetricCategory
     */
    @Override
    public int compareTo(MetricCategory o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetricCategory other = (MetricCategory) obj;
        return name.equals(other.name);
    }
}
