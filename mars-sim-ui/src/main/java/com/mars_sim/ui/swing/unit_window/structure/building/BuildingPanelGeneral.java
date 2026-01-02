/*
 * Mars Simulation Project
 * BuildingPanelGeneral.java
 * @date 2024-07-10
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The BuildingPanelGeneral class is a building function panel showing
 * the general status of a settlement building.
 */
@SuppressWarnings("serial")
class BuildingPanelGeneral extends EntityTabPanel<Building> {
		
	/**
	 * Constructor.
	 * @param building the building
	 * @param context the UI context
	 */
	public BuildingPanelGeneral(Building building, UIContext context) {
		super(GENERAL_TITLE,
			ImageLoader.getIconByName(GENERAL_ICON),		
			GENERAL_TOOLTIP,
			context, building
		);
	}

	/**
	 * Build the UI elements
	 */
	@Override
	protected void buildUI(JPanel center) {

		JPanel topPanel = new JPanel(new BorderLayout());
		center.add(topPanel, BorderLayout.NORTH);

		var building = getEntity();

		// Add SVG Image loading for the building
		JPanel svgPanel = SVGMapUtil.createBuildingPanel(building.getBuildingType().toLowerCase(), 220, 110);
		topPanel.add(svgPanel, BorderLayout.NORTH);

		var labelPanel = SwingHelper.createTextBlock(Msg.getString("Entity.description"), building.getDescription());
		topPanel.add(labelPanel, BorderLayout.CENTER);
		
		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel();
		topPanel.add(infoPanel, BorderLayout.SOUTH);

		infoPanel.addRow(Msg.getString("Building.type"), building.getBuildingType());
		infoPanel.addRow(Msg.getString("Building.category"), building.getCategory().getName());
		infoPanel.addRow(Msg.getString("Building.construction"), building.getConstruction().name());

		// Prepare dimension label
		infoPanel.addRow(Msg.getString("Entity.internalPosn"), building.getPosition().getShortFormat(), 
				"The center x and y coordinates of this building, according to the Settlement Map");
		infoPanel.addRow(Msg.getString("Entity.dimension"), building.getLength() + " m x " + building.getWidth() 
			+ " m x 2.5 m", "Length x Width x Height");
		infoPanel.addRow("Floor Area", StyleManager.DECIMAL_M2.format(building.getFloorArea()),
				"The floor area in square meters");
	}
}
