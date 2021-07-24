/**
 * Mars Simulation Project
 * DayNightMapLayer.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Graphics2D;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * The DayNightMapLayer is a graphics layer to display twilight and night time shading of the settlement
 */
public class DayNightMapLayer implements SettlementMapLayer {

//    private static final Logger logger = Logger.getLogger(DayNightMapLayer.class.getName());
    
    private static final int LIGHT_THRESHOLD = 196;
    
    private int opacity;
    
	private SettlementMapPanel mapPanel;

    public DayNightMapLayer(SettlementMapPanel mapPanel) {
		// Initialize data members.
		this.mapPanel = mapPanel;
    }

    public int getOpacity() {
    	return opacity;
    }
    
	@Override
	public void displayLayer(Graphics2D g2d, Settlement settlement,
			Building building, double xPos, double yPos, int width,
			int height, double rotation, double scale) {

		// TODO: overlay a red/orange mask if having local dust storm

		if (mapPanel.isDaylightTrackingOn()) {

			// NOTE: whenever the user uses the combobox to switch to another settlement in Settlement Map Tool,
			// the corresponding location instance of the new settlement will be reloaded
			// in order to get the correct day light effect.

	        // sunlight normalized between 0 and 1 
	        double sunlight = surfaceFeatures.getSunlightRatio(settlement.getCoordinates());
            int sunlightInt = (int) (LIGHT_THRESHOLD * sunlight);
 
            if (sunlight < 0.85) { 
            	opacity = LIGHT_THRESHOLD - sunlightInt;
	            g2d.setColor(new Color(5, 0, 0, opacity)); //(0, 0, 0, 196));
	            g2d.fillRect(0, 0, width, height);
    
            }
		}
	}

	@Override
	public void destroy() {
		mapPanel = null;
	}
}
