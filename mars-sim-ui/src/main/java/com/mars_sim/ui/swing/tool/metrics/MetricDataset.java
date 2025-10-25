/*
 * Mars Simulation Project
 * MetricDataset.java
 * @date 2025-10-25
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.TableXYDataset;

import com.mars_sim.core.metrics.DataPoint;
import com.mars_sim.core.metrics.Metric;

/**
 * Dataset for Metrics to be used in JFreeChart that binds a number of Metrics to series'.
 */
class MetricDataset extends AbstractXYDataset implements TableXYDataset {

    private transient List<Metric> metrics = new ArrayList<>();
    private String title = "No Metrics";
    private List<double[]> cumulativeValues = null;

    /**
     * Get the number of items for the given series
     */
    @Override
    public int getItemCount(int series) {
        return metrics.get(series).getSize();
    }

    /**
     * Get the X value for the given series and item
     */
    @Override
    public Number getX(int series, int item) {
        DataPoint dp = metrics.get(series).getDataPoint(item);
        return dp.getWhen().getTotalMillisols();
    }

    /**
     * Get the Y value for the given series and item
     */
    @Override
    public Number getY(int series, int item) {
        if (cumulativeValues != null) {
            return cumulativeValues.get(series)[item];
        }
        DataPoint dp = metrics.get(series).getDataPoint(item);
        return dp.getValue();
    }

    /**
     * Get the number of series in the dataset
     */
    @Override
    public int getSeriesCount() {
        return metrics.size();
    }

    /**
     * Get the key of this series based on the MetricKey display value.
     */
    @Override
    public Comparable getSeriesKey(int series) {
        return metrics.get(series).getKey().getDisplay();    
    }
    
    /**
     * Add a Metric to the dataset to belong a series
     * @param values
     * @return true if added, false if already present
     */
    public boolean addMetric(Metric values) {
        if (metrics.contains(values)) {
            return false;
        }
        metrics.add(values);
        calculateTitle();
        fireDatasetChanged();
        return true;
    }

    /**
     * Get the Metric for the given series index
     * @param series
     * @return
     */
    public Metric getMetric(int series) {
        return metrics.get(series);
    }

    /**
     * Set the dataset to return cumulative values
     */
    public void setCumulative(boolean cumulative) {
        // Currently no-op as metrics are always cumulative
        if (cumulative) {
            cumulativeValues = new ArrayList<>();
            for(int j = 0; j < metrics.size(); j++) {
                var m = metrics.get(j);
                var totals = new double[m.getSize()];
                var runningTotal = 0.0;
                for (int i = 0; i < totals.length; i++) {
                    runningTotal += m.getDataPoint(i).getValue();
                    totals[i] = runningTotal;
                }
                cumulativeValues.add(totals);
            }
        }
        else {
            cumulativeValues = null;
        }
        fireDatasetChanged();
    }

    /**
     * Create the best title description for the Metrics shown
     * @return
     */
    public String getTitle() {
        return title;
    }

    private void calculateTitle() {
        if (metrics.size() == 1) {
            title = metrics.get(0).getKey().getDisplay();
            return;
        }   
        
        Set<String> entities = new TreeSet<>();
        Set<String> categories = new TreeSet<>();

        for(var m : metrics) {
            entities.add(m.getKey().asset().getName());
            categories.add(m.getKey().category());
        }

        // See which parts we have
        if (entities.size() == 1) {
            if (categories.size() == 1) {
                // Single entity, single category
                title = String.format("%s - %s", entities.iterator().next(), categories.iterator().next());
            }
            else {
                // Single entity, multiple categories
                title = entities.iterator().next();
            }
        }
        else if (categories.size() == 1) {
            // Multiple entities, single category
            title = categories.iterator().next();
        }
        else {
            // Multiple entities, multiple categories
            title = "Multiple Metrics";
        }
    }

    @Override
    public int getItemCount() {
        return metrics.stream().mapToInt(Metric::getSize).sum();
    }
}
