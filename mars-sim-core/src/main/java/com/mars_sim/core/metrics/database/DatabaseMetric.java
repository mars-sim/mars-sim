/*
 * Mars Simulation Project
 * DatabaseMetric.java
 * @date 2026-06-18
 * @author Barry Evans
 */
package com.mars_sim.core.metrics.database;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;

import com.mars_sim.core.metrics.Calculator;
import com.mars_sim.core.metrics.DataPoint;
import com.mars_sim.core.metrics.Metric;
import com.mars_sim.core.metrics.MetricKey;
import com.mars_sim.core.time.MarsTime;

/**
 * A {@link Metric} implementation that persists data points in a H2
 * in-memory database via Spring JDBC.
 * <p>
 * The parent metric identity is stored in the {@code METRIC} table; each
 * {@link DataPoint} is stored as a row in the {@code DATA_POINT} table with a
 * foreign key back to the parent {@code METRIC} row.
 * </p>
 * <p>
 * The {@link JdbcTemplate} is marked {@code transient} because H2 in-memory
 * databases do not survive JVM serialisation.
 * </p>
 */
class DatabaseMetric extends Metric {

    private static final long serialVersionUID = 1L;

    /** Primary key of the owning row in the {@code METRIC} table. */
    private final long metricId;

    /** Parent used to manage Data points on this metric. */
    private DatabaseMetricManager parent;

    /**
     * Package-private constructor – instances are created exclusively by
     * {@link DatabaseMetricFactory}.
     *
     * @param key          the metric key
     * @param metricId     primary key of the corresponding row in the METRIC table
     * @param parent       parent used to manage Data points on this metric
     */
    DatabaseMetric(MetricKey key, long metricId, DatabaseMetricManager parent) {
        super(key);
        this.metricId = metricId;
        this.parent = parent;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the JDBC template, throwing if it has been lost through deserialisation.
     */
    private JdbcTemplate template() {
        if (parent == null) {
            throw new IllegalStateException(
                "DatabaseMetricManager is not available – DatabaseMetric must be recreated via "
                + "DatabaseMetricFactory after deserialisation.");
        }
        return parent.getJdbcTemplate();
    }

    // -------------------------------------------------------------------------
    // Metric abstract method implementations
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>
     * If a data point with the same {@link MarsTime} (i.e. the same
     * {@code total_millisols} value) already exists for this metric, the value
     * is either replaced or accumulated depending on
     * {@link com.mars_sim.core.metrics.MetricCategory#isAbsolute()}.
     * </p>
     */
    @Override
    protected void addDataPoint(int sol, DataPoint dataPoint) {
        double totalMillisols = dataPoint.getWhen().getTotalMillisols();

        List<Map<String, Object>> existing = template().queryForList(
            "SELECT id, metric_value FROM DATA_POINT WHERE metric_id = ? AND total_millisols = ?",
            metricId, totalMillisols);

        if (!existing.isEmpty()) {
            long existingId = ((Number) existing.get(0).get("ID")).longValue();
            double newValue;
            if (getKey().category().isAbsolute()) {
                newValue = dataPoint.getValue();
            } else {
                double existingValue = ((Number) existing.get(0).get("METRIC_VALUE")).doubleValue();
                newValue = existingValue + dataPoint.getValue();
            }
            template().update("UPDATE DATA_POINT SET metric_value = ? WHERE id = ?", newValue, existingId);
        } else {
            template().update(
                "INSERT INTO DATA_POINT (metric_id, sol, total_millisols, metric_value) VALUES (?, ?, ?, ?)",
                metricId, sol, totalMillisols, dataPoint.getValue());
        }
        template().execute("commit;");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Queries all data points for the given Sol in ascending time order and
     * passes each to the calculator.
     * </p>
     */
    @Override
    protected void applyCalculator(Integer sol, Calculator evaluator) {
        template().query(
            "SELECT total_millisols, metric_value FROM DATA_POINT "
            + "WHERE metric_id = ? AND sol = ? ORDER BY total_millisols",
            rs -> {
                DataPoint dp = new DataPoint(
                    new MarsTime(rs.getDouble("total_millisols")),
                    rs.getDouble("metric_value"));
                evaluator.accept(dp);
            },
            metricId, sol);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the distinct Sol numbers for which at least one data point exists.
     * </p>
     */
    @Override
    public Set<Integer> getSolRange() {
        return new HashSet<>(template().queryForList(
            "SELECT DISTINCT sol FROM DATA_POINT WHERE metric_id = ?",
            Integer.class, metricId));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the total number of data points stored for this metric.
     * </p>
     */
    @Override
    public int getSize() {
        Integer count = template().queryForObject(
            "SELECT COUNT(*) FROM DATA_POINT WHERE metric_id = ?",
            Integer.class, metricId);
        return count != null ? count : 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the {@code item}-th data point when all points are ordered by
     * {@code (sol, total_millisols)} ascending (zero-based index).
     * Returns {@code null} if {@code item} is out of range.
     * </p>
     */
    @Override
    public DataPoint getDataPoint(int item) {
        List<DataPoint> result = template().query(
            "SELECT total_millisols, metric_value FROM DATA_POINT "
            + "WHERE metric_id = ? ORDER BY sol, total_millisols "
            + "OFFSET ? ROWS FETCH NEXT 1 ROWS ONLY",
            (rs, rowNum) -> new DataPoint(
                new MarsTime(rs.getDouble("total_millisols")),
                rs.getDouble("metric_value")),
            metricId, item);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (metricId ^ (metricId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DatabaseMetric other = (DatabaseMetric) obj;
        return (metricId == other.metricId);
    }
}
