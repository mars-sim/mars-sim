/**
 * Mars Simulation Project
 * DayNightMapLayer.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.JMenuItem;

import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.UIConfig;

/**
 * The DayNightMapLayer is a graphics layer to display twilight and night time shading of the settlement
 */
public class DayNightMapLayer extends AbstractMapLayer {
	static final String DAYLIGHT_LAYER_PROP = "DAYLIGHT_LAYER";
   
    private static final int LIGHT_THRESHOLD = 196;
    
    private int opacity;
    
	private SettlementMapPanel mapPanel;
	private SurfaceFeatures surfaceFeatures;
	private boolean showDaylightLayer;

    public DayNightMapLayer(SettlementMapPanel mapPanel, SurfaceFeatures surface, Properties userSettings) {
		// Initialize data members.
		this.mapPanel = mapPanel;
		this.surfaceFeatures = surface;
		this.showDaylightLayer = UIConfig.extractBoolean(userSettings, DAYLIGHT_LAYER_PROP, false);
    }

    public int getOpacity() {
    	return opacity;
    }
    
	@Override
	public Collection<? extends MapHotspot<?>> displayLayer(Settlement settlement, MapViewPoint viewpoint) {

		if (showDaylightLayer) {

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

		return Collections.emptyList();
	}

	public boolean isVisible() {
		return showDaylightLayer;
	}

	@Override
	public List<JMenuItem> getFilterControls() {
		return List.of(createDisplayToggle("daylight_layer", showDaylightLayer,
				selected -> {
					showDaylightLayer = selected;
					mapPanel.repaint();
					return null;
				}));
	}

	@Override
	public void saveUIProperties(Properties props) {
		props.setProperty(DAYLIGHT_LAYER_PROP, Boolean.toString(showDaylightLayer));
	}

	@Override
	public void destroy() {
		mapPanel = null;
		surfaceFeatures = null;
	}
}
