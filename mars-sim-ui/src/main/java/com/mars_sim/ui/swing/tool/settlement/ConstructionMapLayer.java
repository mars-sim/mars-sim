/**
 * Mars Simulation Project
 * ConstructionMapLayer.java
 * @date 2023-12-13
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;

import org.apache.batik.gvt.GraphicsNode;

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.tool.settlement.SettlementMapPanel.DisplayOption;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;

/**
 * A settlement map layer for displaying construction sites.
 */
public class ConstructionMapLayer extends AbstractMapLayer {
    
    private static final Color CONST_COLOR = new Color(119, 59, 0); // dark orange
    private static final Color CONST_SELECTED_COLOR = Color.WHITE; // Color(119, 85, 0); // dark orange

    private static final Font LABEL_FONT = new Font(Font.SERIF, Font.PLAIN, 10); // Note size doesn;t matter

	private static final ColorChoice CONSTRUCTION_COLOR = new ColorChoice(new Color(237, 114, 38), new Color(0, 0, 0, 150));
        
    private SettlementMapPanel mapPanel;
    
    /**
     * Constructor 1.
     * 
     * @param mapPanel the settlement map panel.
     */
    public ConstructionMapLayer(SettlementMapPanel mapPanel) {

        // Initialize data members.
        this.mapPanel = mapPanel;
    }

    @Override
    public void displayLayer(Settlement settlement, MapViewPoint viewpoint) {

        // Save original graphics transforms.
        AffineTransform saveTransform = viewpoint.prepareGraphics();

        // Draw all construction sites.
        boolean constLabels = mapPanel.isOptionDisplayed(DisplayOption.CONSTRUCTION_LABELS);
        for(ConstructionSite c : settlement.getConstructionManager()
                                .getConstructionSites()) {
            drawConstructionSite(c, constLabels, viewpoint);
        }

	    // Restore original graphic transforms.
	    viewpoint.graphics().setTransform(saveTransform);
    }

    /**
     * Draws a construction site on the map.
     * 
     * @param site the construction site.
     */
    private void drawConstructionSite(ConstructionSite site, boolean showLabel, MapViewPoint viewpoint) {
    	
     	// Check if it's drawing the mouse-picked building 
        Color selectedColor = (site.equals(mapPanel.getSelectedSite()) ? CONST_SELECTED_COLOR : null);
    	
        // Use SVG image for construction site if available.
        GraphicsNode svg = null;
        ConstructionStage stage = site.getCurrentConstructionStage();
        String imageName = null;
        if (stage != null) {
            imageName = stage.getInfo().getImageName();
            svg = SVGMapUtil.getConstructionSiteSVG(imageName);
        }
        
        if (svg != null) {
            // Determine construction site pattern SVG image if available.
            GraphicsNode patternSVG = SVGMapUtil.getConstructionSitePatternSVG(imageName);

            drawStructure(site, svg, patternSVG, selectedColor , viewpoint);
        }
        else {
            drawRectangle(site, CONST_COLOR, selectedColor, viewpoint);
        }

        
        if (showLabel) {
            String[] words = site.getStatusDescription().split(" ");

            drawCenteredMultiLabel(words, LABEL_FONT, site.getPosition(),
                                    CONSTRUCTION_COLOR, viewpoint);
        }

    }
    
	@Override
	public void destroy() {
		super.destroy();
		mapPanel = null;
	}
}
