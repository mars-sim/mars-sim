package com.mars_sim.ui.swing.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.ui.swing.components.TrendValue.Trend;

class TrendValueTest {

	@Test
	void behavesAsNumber() {
		Number n = new TrendValue(12.7D, Trend.UP);
		assertEquals(12.7D, n.doubleValue());
		assertEquals(12, n.intValue());
	}

	@Test
	void comparesOnValueOnly() {
		var low = new TrendValue(5D, Trend.UP);
		var high = new TrendValue(10D, Trend.DOWN);
		assertTrue(low.compareTo(high) < 0);
		assertEquals(0, low.compareTo(new TrendValue(5D, Trend.DOWN)));
	}

	@Test
	void nullTrendIsSame() {
		assertEquals(Trend.SAME, new TrendValue(1D, null).getTrend());
	}
}
