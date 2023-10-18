/*
 * Mars Simulation Project
 * BuildingPanelGeneral.java
 * @date 2022-07-10
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.batik.gvt.GraphicsNode;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.svg.SVGGraphicNodeIcon;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The BuildingPanelGeneral class is a building function panel showing
 * the general status of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelGeneral extends BuildingFunctionPanel {

	private static final String ID_ICON = "info";
	
	/**
	 * Constructor.
	 * @param The panel for the Fishery
	 * @param The main desktop
	 */
	public BuildingPanelGeneral(Building building, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelGeneral.title"),
			ImageLoader.getIconByName(ID_ICON), 
			building, desktop
		);
	}

	/**
	 * Build the UI elements
	 */
	@Override
	protected void buildUI(JPanel center) {

		JPanel topPanel = new JPanel(new BorderLayout());
		center.add(topPanel, BorderLayout.NORTH);

		// Add SVG Image loading for the building
		GraphicsNode svg = SVGMapUtil.getBuildingSVG(building.getBuildingType().toLowerCase());
		SVGGraphicNodeIcon svgIcon = new SVGGraphicNodeIcon(svg, 220, 110, true);
		JLabel svgLabel = new JLabel(svgIcon);
		JPanel svgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		svgPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		svgPanel.add(svgLabel);
		topPanel.add(svgPanel, BorderLayout.NORTH);
		
		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel(6);
		topPanel.add(infoPanel, BorderLayout.CENTER);

		infoPanel.addTextField("Building Type", building.getBuildingType(), null);
		infoPanel.addTextField("Category", building.getCategory().getName(), null);
		infoPanel.addTextField("Construction", building.getConstruction().name(), null);

		// Prepare dimension label
		infoPanel.addTextField("Position", building.getPosition().getShortFormat(), "According to the Settlement x[m] x y[m]");
		infoPanel.addTextField("Dimension", building.getLength() + " x " + building.getWidth() + " x 2.5", "Length[m] x Width[m] x Height[m]");

		// Prepare mass label
		infoPanel.addTextField("Base Mass", StyleManager.DECIMAL_KG.format(building.getBaseMass()), "The base mass of this building");
	}
}
