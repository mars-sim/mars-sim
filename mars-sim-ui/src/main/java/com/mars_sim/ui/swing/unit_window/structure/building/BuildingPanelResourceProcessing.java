/*
 * Mars Simulation Project
 * BuildingPanelResourceProcessing.java
 * @date 2022-07-26
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.mars_sim.core.building.function.ResourceProcessing;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;

/**
 * The BuildingPanelResourceProcessing class is a building function panel representing
 * the resource processes of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelResourceProcessing extends BuildingFunctionPanel {

	private static final String ICON = "resource";
	
	/** Is UI constructed. */
	private boolean uiDone = false;

	// Data members
	private ResourceProcessing processor;
	private ResourceProcessPanel processPanel;

	/**
	 * Constructor.
	 * 
	 * @param processor the resource processing building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelResourceProcessing(ResourceProcessing processor, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelResourceProcessing.title"),
			ImageLoader.getIconByName(ICON),
			processor.getBuilding(), 
			desktop
		);

		// Initialize variables.
		this.processor = processor;
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {
		processPanel = new ResourceProcessPanel(processor.getBuilding(), processor.getProcesses());
		processPanel.setPreferredSize(new Dimension(160, 120));

		center.add(processPanel, BorderLayout.CENTER);	
	}
	
	@Override
	public void update() {	
		if (!uiDone)
			initializeUI();
		
		processPanel.update();
	}
}
