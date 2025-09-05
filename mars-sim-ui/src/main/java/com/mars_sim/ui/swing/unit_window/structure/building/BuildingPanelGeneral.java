/*
 * Mars Simulation Project
 * BuildingPanelGeneral.java
 * @date 2024-07-10
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.batik.gvt.GraphicsNode;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.svg.SVGGraphicNodeIcon;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;
import com.mars_sim.ui.swing.utils.AttributePanel;

import io.github.parubok.text.multiline.MultilineLabel;

/**
 * The BuildingPanelGeneral class is a building function panel showing
 * the general status of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelGeneral extends BuildingFunctionPanel {

	private static final String ID_ICON = "info";
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	private JLabel airMassLabel;
	
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
		int facing = (int)building.getFacing();
		boolean toTurn = false;
		if (facing == 90 || facing == 270)
			toTurn = true;
		GraphicsNode svg = SVGMapUtil.getBuildingSVG(building.getBuildingType().toLowerCase());
		SVGGraphicNodeIcon svgIcon = new SVGGraphicNodeIcon(svg, 220, 110, toTurn);
		JLabel svgLabel = new JLabel(svgIcon);
		JPanel svgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		svgPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		svgPanel.add(svgLabel);
		topPanel.add(svgPanel, BorderLayout.NORTH);
		
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		addBorder(labelPanel, "Description");
		var label = new MultilineLabel();
		labelPanel.add(label);
		String text = building.getDescription().replace("\n", " ").replace("\t", "");
		label.setText(text);
		label.setPreferredWidthLimit(430);
		label.setLineSpacing(1.2f);
		label.setMaxLines(30);
		label.setBorder(new EmptyBorder(5, 5, 5, 5));
		label.setSeparators(Set.of(' ', '/', '|', '(', ')'));
		topPanel.add(labelPanel, BorderLayout.CENTER);
		
		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel();
		topPanel.add(infoPanel, BorderLayout.SOUTH);

		infoPanel.addRow("Building Type", building.getBuildingType());
		infoPanel.addRow("Category", building.getCategory().getName());
		infoPanel.addRow("Construction", building.getConstruction().name());

		// Prepare dimension label
		infoPanel.addRow("Position", building.getPosition().getShortFormat(), 
				"The center x and y coordinates of this building, according to the Settlement Map");
		infoPanel.addRow("Dimension", building.getLength() + " m x " + building.getWidth() 
			+ " m x 2.5 m", "Length x Width x Height");
		infoPanel.addRow("Floor Area", StyleManager.DECIMAL_M2.format(building.getFloorArea()),
				"The floor area in square meters");
		
		// Prepare air mass label
		LifeSupport ls = building.getLifeSupport();
		if (ls != null)
			airMassLabel = infoPanel.addRow("Air Mass", StyleManager.DECIMAL_KG2.format(
				ls.getAir().getTotalMass()), "The mass of the air in kg");
		else
			airMassLabel = infoPanel.addRow("Air Mass", 0 + "", "The mass of the air in kg");
	}
	
	/**
	 * Updates this panel with latest values.
	 */
	@Override
	public void update() {	
		if (!uiDone)
			initializeUI();
		
		LifeSupport ls = building.getLifeSupport();
		if (ls != null)
			airMassLabel.setText(StyleManager.DECIMAL_KG2.format(
				ls.getAir().getTotalMass()));
	}
}
