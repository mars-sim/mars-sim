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

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.construction.ConstructionSite;
import com.mars_sim.core.structure.construction.ConstructionStage;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.tool.settlement.SettlementMapPanel.DisplayOption;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;

/**
 * A settlement map layer for displaying construction sites.
 */
public class ConstructionMapLayer extends AbstractMapLayer {
    
    private static final Color CONST_COLOR = new Color(119, 59, 0); // dark orange
    private static final Color CONST_SELECTED_COLOR = new Color(119, 85, 0); // dark orange

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
        // Use SVG image for construction site if available.
        GraphicsNode svg = null;
        ConstructionStage stage = site.getCurrentConstructionStage();
        String stageName = null;
        if (stage != null) {
            stageName = stage.getInfo().getName().toLowerCase();
            svg = SVGMapUtil.getConstructionSiteSVG(stageName);
        }
        
        Color selectedColor = (site.isMousePicked() ? CONST_SELECTED_COLOR : null);
        if (svg != null) {
            // Determine construction site pattern SVG image if available.
            GraphicsNode patternSVG = SVGMapUtil
                    .getConstructionSitePatternSVG(stageName);

            drawStructure(site, svg, patternSVG, selectedColor, viewpoint);
        }
        else {
            drawRectangle(site, CONST_COLOR, selectedColor, viewpoint);
        }

        
        if (showLabel) {
            String[] words = getConstructionLabel(site).split(" ");

            drawCenteredMultiLabel(words, LABEL_FONT, site.getPosition(),
                                    CONSTRUCTION_COLOR, viewpoint);
        }
    }

    /**
	 * Gets the label for a construction site.
	 * 
	 * @param site the construction site.
	 * @return the construction label.
	 */
	public static String getConstructionLabel(ConstructionSite site) {
		String label = ""; //$NON-NLS-1$
		ConstructionStage stage = site.getCurrentConstructionStage();
		if (stage != null) {
			if (site.isUndergoingConstruction()) {
				label = Msg.getString("LabelMapLayer.constructing", stage.getInfo().getName()); //$NON-NLS-1$
			} else if (site.isUndergoingSalvage()) {
				label = Msg.getString("LabelMapLayer.salvaging", stage.getInfo().getName()); //$NON-NLS-1$
			} else if (site.hasUnfinishedStage()) {
				if (stage.isSalvaging()) {
					label = Msg.getString("LabelMapLayer.salvagingUnfinished", stage.getInfo().getName()); //$NON-NLS-1$
				} else {
					label = Msg.getString("LabelMapLayer.constructingUnfinished", stage.getInfo().getName()); //$NON-NLS-1$
				}
			} else {
				label = Msg.getString("LabelMapLayer.completed", stage.getInfo().getName()); //$NON-NLS-1$
			}
		} else {
			label = Msg.getString("LabelMapLayer.noConstruction"); //$NON-NLS-1$
		}
		return label;
	}
}
