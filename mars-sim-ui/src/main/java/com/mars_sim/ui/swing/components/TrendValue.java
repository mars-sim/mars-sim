/*
 * Mars Simulation Project
 * TrendValue.java
 * @date 2026-09-26
 * @author Shalini H R
 */
package com.mars_sim.ui.swing.components;

/**
 * An immutable numeric value that also records the direction of the last change.
 * It extends Number so existing consumers, e.g. charts and sorting, still treat it as a number.
 * The rendering of the trend is handled by {@link TrendCellRenderer}.
 */
public final class TrendValue extends Number implements Comparable<TrendValue> {

	private static final long serialVersionUID = 1L;

	/**
	 * Direction of the last significant change of the value.
	 */
	public enum Trend { UP, DOWN, SAME }

	private final double value;
	private final Trend trend;

	/**
	 * Constructor.
	 *
	 * @param value Current value
	 * @param trend Direction of the last change; null is treated as SAME
	 */
	public TrendValue(double value, Trend trend) {
		this.value = value;
		this.trend = (trend != null ? trend : Trend.SAME);
	}

	public Trend getTrend() {
		return trend;
	}

	@Override
	public double doubleValue() {
		return value;
	}

	@Override
	public float floatValue() {
		return (float) value;
	}

	@Override
	public int intValue() {
		return (int) value;
	}

	@Override
	public long longValue() {
		return (long) value;
	}

	/**
	 * Values are compared on the number only so sorting a column is unaffected by the trend.
	 */
	@Override
	public int compareTo(TrendValue o) {
		return Double.compare(value, o.value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return (obj instanceof TrendValue other)
				&& Double.compare(value, other.value) == 0
				&& trend == other.trend;
	}

	@Override
	public int hashCode() {
		return 31 * Double.hashCode(value) + trend.hashCode();
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}
}
