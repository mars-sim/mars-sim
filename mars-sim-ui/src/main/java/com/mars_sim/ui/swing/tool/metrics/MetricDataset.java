package com.mars_sim.ui.swing.tool.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jfree.data.xy.AbstractXYDataset;

import com.mars_sim.core.metrics.DataPoint;
import com.mars_sim.core.metrics.Metric;
import com.mars_sim.core.metrics.MetricKey;

class MetricDataset extends AbstractXYDataset {

    private transient List<Metric> metrics = new ArrayList<>();

    @Override
    public int getItemCount(int series) {
        return metrics.get(series).getSize();
    }

    @Override
    public Number getX(int series, int item) {
        DataPoint dp = metrics.get(series).getDataPoint(item);
        return dp.getWhen().getTotalMillisols();
    }

    @Override
    public Number getY(int series, int item) {
        DataPoint dp = metrics.get(series).getDataPoint(item);
        return dp.getValue();
    }

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

    public boolean isAligned(MetricKey source) {
        return true;
    }
    
    public void addMetric(Metric values) {
        metrics.add(values);
        fireDatasetChanged();
    }

    public Metric getMetric(int series) {
        return metrics.get(series);
    }

    /**
     * Create the best title description for the Metrics shown
     * @return
     */
    public String getTitle() {
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
                return String.format("%s - %s", entities.iterator().next(), categories.iterator().next());
            }
            // Single entity, multiple categories
            return entities.iterator().next();
        }
        else if (categories.size() == 1) {
            // Multiple entities, single category
            return categories.iterator().next();
        }
        
        // Multiple entities, multiple categories
        return "Multiple Metrics";
    }
}
