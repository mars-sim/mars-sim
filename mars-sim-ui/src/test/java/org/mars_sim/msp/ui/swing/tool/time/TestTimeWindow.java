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
    	//TimeWindow.ratioatmid = 500D;	
        assertEquals(.01D, TimeWindow.calculateTimeRatioFromSlider(1));
        assertEquals(500D, TimeWindow.calculateTimeRatioFromSlider(50));
        assertEquals(10800D, TimeWindow.calculateTimeRatioFromSlider(100));
    }
    
    /**
     * Test the calculateSliderValue method.
     */
    public void testCalculateSliderValue() {
        
        assertEquals(1, TimeWindow.calculateSliderValue(.01D));
        assertEquals(50, TimeWindow.calculateSliderValue(500D));
        assertEquals(100, TimeWindow.calculateSliderValue(10800D));
    }
    
    /**
     * Test that a slider value can be calculated into a time ratio, which can then
     * be inversely calculated back into the same slider value.
     */
    public void testCalculateInverse() {
        
        for (int x = 1; x <= 100; x++) {
            double timeRatio = TimeWindow.calculateTimeRatioFromSlider(x);
            int sliderValue = TimeWindow.calculateSliderValue(timeRatio);
            assertEquals(x, sliderValue);
        }
    }
}