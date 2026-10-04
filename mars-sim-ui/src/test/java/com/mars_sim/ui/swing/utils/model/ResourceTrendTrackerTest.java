package com.mars_sim.ui.swing.utils.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.ui.swing.components.TrendValue.Trend;

class ResourceTrendTrackerTest {

	private static final int OXYGEN = 1;
	private static final int WATER = 2;

	@Test
	void unknownResourceIsSame() {
		var tracker = new ResourceTrendTracker<String>();
		assertEquals(Trend.SAME, tracker.getTrend("Alpha", OXYGEN));
	}

	@Test
	void firstValueIsSame() {
		var tracker = new ResourceTrendTracker<String>();
		tracker.update("Alpha", OXYGEN, 100D);
		assertEquals(Trend.SAME, tracker.getTrend("Alpha", OXYGEN));
	}

	@Test
	void increaseAndDecrease() {
		var tracker = new ResourceTrendTracker<String>();
		tracker.update("Alpha", OXYGEN, 100D);

		tracker.update("Alpha", OXYGEN, 110D);
		assertEquals(Trend.UP, tracker.getTrend("Alpha", OXYGEN));

		tracker.update("Alpha", OXYGEN, 90D);
		assertEquals(Trend.DOWN, tracker.getTrend("Alpha", OXYGEN));
	}

	@Test
	void trendSurvivesRepeatedReads() {
		var tracker = new ResourceTrendTracker<String>();
		tracker.update("Alpha", OXYGEN, 100D);
		tracker.update("Alpha", OXYGEN, 110D);

		// Repaints only read, so the trend must not change
		for (int i = 0; i < 5; i++) {
			assertEquals(Trend.UP, tracker.getTrend("Alpha", OXYGEN));
		}
	}

	@Test
	void smallChangesAccumulate() {
		var tracker = new ResourceTrendTracker<String>();
		double step = ResourceTrendTracker.THRESHOLD / 4;
		tracker.update("Alpha", OXYGEN, 100D);

		tracker.update("Alpha", OXYGEN, 100D - step);
		assertEquals(Trend.SAME, tracker.getTrend("Alpha", OXYGEN), "Below threshold");

		tracker.update("Alpha", OXYGEN, 100D - 5 * step);
		assertEquals(Trend.DOWN, tracker.getTrend("Alpha", OXYGEN), "Accumulated past threshold");
	}

	@Test
	void ownersAndResourcesAreIndependent() {
		var tracker = new ResourceTrendTracker<String>();
		tracker.update("Alpha", OXYGEN, 100D);
		tracker.update("Alpha", WATER, 100D);
		tracker.update("Beta", OXYGEN, 100D);

		tracker.update("Alpha", OXYGEN, 120D);
		tracker.update("Alpha", WATER, 80D);

		assertEquals(Trend.UP, tracker.getTrend("Alpha", OXYGEN));
		assertEquals(Trend.DOWN, tracker.getTrend("Alpha", WATER));
		assertEquals(Trend.SAME, tracker.getTrend("Beta", OXYGEN));
	}

	@Test
	void removeForgetsOwner() {
		var tracker = new ResourceTrendTracker<String>();
		tracker.update("Alpha", OXYGEN, 100D);
		tracker.update("Alpha", OXYGEN, 120D);
		tracker.update("Beta", OXYGEN, 100D);
		tracker.update("Beta", OXYGEN, 120D);

		tracker.remove("Alpha");

		assertEquals(Trend.SAME, tracker.getTrend("Alpha", OXYGEN));
		assertEquals(Trend.UP, tracker.getTrend("Beta", OXYGEN));
	}
}
