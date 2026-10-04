/*
 * Mars Simulation Project
 * TrendValue.java
 * @date 2026-09-28
 * @author Shalini H R
 */
package com.mars_sim.ui.swing.components;

import java.io.Serializable;

/**
 * An immutable numeric value that also records the direction of the last change.
 * The rendering of the trend is handled by {@link TrendCellRenderer}.
 *
 * @param value Current value
 * @param trend Direction of the last change; null is treated as SAME
 */
public record TrendValue(double value, Trend trend)
		implements Comparable<TrendValue>, Serializable {

	/**
	 * Direction of the last significant change of the value.
	 */
	public enum Trend { UP, DOWN, SAME }

	public TrendValue {
		if (trend == null) {
			trend = Trend.SAME;
		}
	}

	public float floatValue() {
		return (float) value;
	}

	public int intValue() {
		return (int) value;
	}

	public long longValue() {
		return (long) value;
	}

	/**
	 * Values are ordered on the number first so sorting a column follows the amount.
	 * The trend is only used as a tie-breaker to stay consistent with equals.
	 */
	@Override
	public int compareTo(TrendValue o) {
		int result = Double.compare(value, o.value);
		return (result != 0) ? result : trend.compareTo(o.trend);
	}
}
