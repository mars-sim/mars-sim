package com.mars_sim.ui.swing.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.ui.swing.components.TrendValue.Trend;

class TrendValueTest {

	@Test
	void numericAccessors() {
		var v = new TrendValue(12.7D, Trend.UP);
		assertEquals(12.7D, v.value());
		assertEquals(12, v.intValue());
		assertEquals(12L, v.longValue());
		assertEquals(12.7F, v.floatValue());
	}

	@Test
	void ordersOnValueFirst() {
		var low = new TrendValue(5D, Trend.UP);
		var high = new TrendValue(10D, Trend.DOWN);
		assertTrue(low.compareTo(high) < 0);
		assertTrue(high.compareTo(low) > 0);
	}

	@Test
	void compareToConsistentWithEquals() {
		var up = new TrendValue(5D, Trend.UP);
		var down = new TrendValue(5D, Trend.DOWN);

		assertNotEquals(up, down);
		assertNotEquals(0, up.compareTo(down));

		var same = new TrendValue(5D, Trend.UP);
		assertEquals(up, same);
		assertEquals(0, up.compareTo(same));
	}

	@Test
	void nullTrendIsSame() {
		assertEquals(Trend.SAME, new TrendValue(1D, null).trend());
	}
}
