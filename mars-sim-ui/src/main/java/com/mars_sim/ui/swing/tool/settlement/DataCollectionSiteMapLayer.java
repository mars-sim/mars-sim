/**
 * Mars Simulation Project
 * DataCollectionSiteMapLayer.java
 * @date 2026-08-31
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;

import com.mars_sim.core.data.collection.DataCollectionSite;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.tool.settlement.SettlementMapPanel.DisplayOption;

/**
 * A settlement map layer for displaying data collection sites.
 */
public class DataCollectionSiteMapLayer extends AbstractMapLayer {
    
    private static final Color SITE_COLOR = Color.LIGHT_GRAY; // Color(119, 59, 0).brighter().brighter(); // dark orange
    private static final Color SITE_SELECTED_COLOR = Color.YELLOW.brighter().brighter(); //new Color(119, 85, 0); // dark orange

    private static final Font LABEL_FONT = new Font(Font.SERIF, Font.PLAIN, 7); // Note size doesn't matter

	private static final ColorChoice COLOR_CHOICE = new ColorChoice(new Color(237, 114, 38), Color.WHITE);// Color(0, 0, 0, 150));
        
    private SettlementMapPanel mapPanel;
    
    /**
     * Constructor 1.
     * 
     * @param mapPanel the settlement map panel.
     */
    public DataCollectionSiteMapLayer(SettlementMapPanel mapPanel) {

        // Initialize data members.
        this.mapPanel = mapPanel;
    }

    @Override
    public void displayLayer(Settlement settlement, MapViewPoint viewpoint) {

        // Save original graphics transforms.
        AffineTransform saveTransform = viewpoint.prepareGraphics();

        // Draw all construction sites.
        boolean labels = mapPanel.isOptionDisplayed(DisplayOption.DATA_COLLECTION_SITE_LABELS);
        for (DataCollectionSite c : settlement.getLocalSiteLists()) {
            drawSite(c, labels, viewpoint);
        }

	    // Restore original graphic transforms.
	    viewpoint.graphics().setTransform(saveTransform);
    }

    /**
     * Draws a site on the map.
     * 
     * @param site
     * @param showLabel
     * @param viewpoint
     */
    private void drawSite(DataCollectionSite site, boolean showLabel, MapViewPoint viewpoint) {
    	
     	// Check if it's drawing the mouse-picked building 
        Color selectedColor = SITE_SELECTED_COLOR; //(site.equals(mapPanel.getSelectedDataSite()) ? SITE_SELECTED_COLOR : null);
    	
        drawRectangle(site, SITE_COLOR, selectedColor, viewpoint);
        
        if (showLabel) {
            String words = site.getName();

            drawCenteredLabel(words, LABEL_FONT, site.getPosition(),
                                    COLOR_CHOICE, 15, viewpoint);
        }

    }
    
	@Override
	public void destroy() {
		super.destroy();
		mapPanel = null;
	}
}
