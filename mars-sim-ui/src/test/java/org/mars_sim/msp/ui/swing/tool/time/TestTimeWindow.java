package org.mars_sim.msp.ui.swing.tool.time;

import junit.framework.TestCase;

/**
 * Unit test suite for the TimeWindow class.
 */
public class TestTimeWindow extends TestCase {

	/**
	 * Test the calculateTimeRatioFromSlider method.
	 */
	public void testCalculateTimeRatioFromSlider() {
		// require setting TimeWindow.ratioatmid = 500D;
//        assertEquals(.01D, TimeWindow.calculateTimeRatioFromSlider(1));
//        assertEquals(500D, TimeWindow.calculateTimeRatioFromSlider(50));
//        assertEquals(10800D, TimeWindow.calculateTimeRatioFromSlider(100));
		assertEquals(1D, TimeWindow.calculateTimeRatioFromSlider(0));
		assertEquals(256D, TimeWindow.calculateTimeRatioFromSlider(8));
		assertEquals(8192D, TimeWindow.calculateTimeRatioFromSlider(13));
	}

	/**
	 * Test the calculateSliderValue method.
	 */
	public void testCalculateSliderValue() {
//        assertEquals(1, TimeWindow.calculateSliderValue(.01D));
//        assertEquals(50, TimeWindow.calculateSliderValue(500D));
//        assertEquals(100, TimeWindow.calculateSliderValue(10800D));
		assertEquals(0, TimeWindow.calculateSliderValue(1D));
		assertEquals(8, TimeWindow.calculateSliderValue(256D));
		assertEquals(13, TimeWindow.calculateSliderValue(8192D));
	}

	/**
	 * Test that a slider value can be calculated into a time ratio, which can then
	 * be inversely calculated back into the same slider value.
	 */
	public void testCalculateInverse() {

		for (int x = 0; x <= 13; x++) {
			double timeRatio = TimeWindow.calculateTimeRatioFromSlider(x);
			int sliderValue = TimeWindow.calculateSliderValue(timeRatio);
			assertEquals(x, sliderValue);
		}
	}
}