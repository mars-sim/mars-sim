/*
 * Mars Simulation Project
 * BuildingPanelResourceProcessing.java
 * @date 2022-07-26
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The BuildingPanelResourceProcessing class is a building function panel representing
 * the resource processes of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelResourceProcessing extends BuildingFunctionPanel {

	private static final String CHEMICAL_ICON = "chemical";
	
	// Data members
	private ResourceProcessing processor;
	private ResourceProcessPanel processPanel;

	/**
	 * Constructor.
	 * @param processor the resource processing building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelResourceProcessing(ResourceProcessing processor, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelResourceProcessing.title"),
			ImageLoader.getIconByName(CHEMICAL_ICON),
			processor.getBuilding(), 
			desktop
		);

		// Initialize variables.
		this.processor = processor;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
		processPanel = new ResourceProcessPanel(processor.getBuilding(), processor.getProcesses());
		processPanel.setPreferredSize(new Dimension(160, 120));

		center.add(processPanel, BorderLayout.CENTER);	
	}
	
	@Override
	public void update() {
		processPanel.update();
	}
}
