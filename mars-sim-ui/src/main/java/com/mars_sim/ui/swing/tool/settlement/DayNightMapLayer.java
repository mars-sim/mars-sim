/**
 * Mars Simulation Project
 * DayNightMapLayer.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;

import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.structure.Settlement;

/**
 * The DayNightMapLayer is a graphics layer to display twilight and night time shading of the settlement
 */
public class DayNightMapLayer implements SettlementMapLayer {
   
    private static final int LIGHT_THRESHOLD = 196;
    
    private int opacity;
    
	private SettlementMapPanel mapPanel;
	private SurfaceFeatures surfaceFeatures;

    public DayNightMapLayer(SettlementMapPanel mapPanel) {
		// Initialize data members.
		this.mapPanel = mapPanel;
		this.surfaceFeatures = mapPanel.getSettlementWindow().getDesktop().getSimulation().getSurfaceFeatures();
    }

    public int getOpacity() {
    	return opacity;
    }
    
	@Override
	public void displayLayer(Settlement settlement, MapViewPoint viewpoint) {

		if (mapPanel.isDaylightTrackingOn()) {

			// NOTE: whenever the user uses the combobox to switch to another settlement in Settlement Map Tool,
			// the corresponding location instance of the new settlement will be reloaded
			// in order to get the correct day light effect.

	        // sunlight normalized between 0 and 1 
	        double sunlight = surfaceFeatures.getSunlightRatio(settlement.getCoordinates());
            int sunlightInt = (int) (LIGHT_THRESHOLD * sunlight);
 
            if (sunlight < 0.85) { 
            	opacity = LIGHT_THRESHOLD - sunlightInt;

				var g2d = viewpoint.graphics();
	            g2d.setColor(new Color(5, 0, 0, opacity)); 
	            g2d.fillRect(0, 0, viewpoint.mapWidth(), viewpoint.mapHeight());
    
            }
		}
	}

	@Override
	public void destroy() {
		mapPanel = null;
	}
}
