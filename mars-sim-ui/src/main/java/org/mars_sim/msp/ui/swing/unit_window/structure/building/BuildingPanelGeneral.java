/**
 * Mars Simulation Project
 * BuildingPanelGeneral.java
 * @date 2021-10-07
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;

/**
 * The BuildingPanelGeneral class is a building function panel showing
 * the general status of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelGeneral
extends BuildingFunctionPanel {


	/**
	 * Constructor.
	 * @param The panel for the Fishery
	 * @param The main desktop
	 */
	public BuildingPanelGeneral(Building building, MainDesktopPane desktop) {
		super("General", building, desktop);
	}

	/**
	 * Build the UI elements
	 */
	@Override
	protected void buildUI(JPanel center) {

		JPanel topPanel = new JPanel(new BorderLayout());
		center.add(topPanel, BorderLayout.NORTH);

		// Add SVG Image loading for the building
		Dimension dim = new Dimension(110, 110);
		Settlement settlement = building.getSettlement();
		SettlementMapPanel mapPanel = new SettlementMapPanel(settlement, building);
		mapPanel.setPreferredSize(dim);

		JPanel svgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		svgPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		svgPanel.add(mapPanel);
		topPanel.add(svgPanel, BorderLayout.NORTH);
		
		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new GridLayout(5, 2, 3, 1));
		topPanel.add(infoPanel, BorderLayout.CENTER);

		addTextField(infoPanel, "Building Type:", building.getBuildingType(), null);
		addTextField(infoPanel, "Category:", building.getCategory().getName(), null);

		// Prepare dimension label
		addTextField(infoPanel, "Position:", building.getPosition().getShortFormat(), "According to the Settlement x[m] x y[m]");
		addTextField(infoPanel, "Dimension:", building.getLength() + " x " + building.getWidth() + " x 2.5", "Length[m] x Width[m] x Height[m]");

		// Prepare mass label
		addTextField(infoPanel, "Base Mass:", building.getBaseMass() + " kg", "The base mass of this building");
	}
}
