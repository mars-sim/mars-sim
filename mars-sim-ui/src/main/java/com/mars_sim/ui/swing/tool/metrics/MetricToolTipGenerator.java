/*
 * Mars Simulation Project
 * MetricToolTipGenerator.java
 * @date 2025-10-25
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.metrics;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

import com.mars_sim.core.time.MarsTimeFormat;

/**
 * Tool tip generator for Metric datasets. It shows the MetricKey display, value, and timestamp.
 */
public class MetricToolTipGenerator implements XYToolTipGenerator {

    @Override
    public String generateToolTip(XYDataset dataset, int series, int item) {
        MetricDataset metricsDataset = (MetricDataset) dataset;

        var m = metricsDataset.getMetric(series);
        var dp = m.getDataPoint(item);

        return m.getKey().getDisplay() + ": " + dp.getValue() + " @ " + MarsTimeFormat.getDateTimeStamp(dp.getWhen());
    }
}
