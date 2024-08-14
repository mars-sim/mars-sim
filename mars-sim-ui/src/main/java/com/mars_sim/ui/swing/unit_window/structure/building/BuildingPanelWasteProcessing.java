/*
 * Mars Simulation Project
 * BuildingPanelWasteProcessing.java
 * @date 2022-07-26
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.mars_sim.core.structure.building.function.WasteProcessing;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;

/**
 * The BuildingPanelWasteProcessing class is a building function panel representing
 * the waste processes of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelWasteProcessing extends BuildingFunctionPanel {

	private static final String RECYCLE_ICON = "recycle";

	/** Is UI constructed. */
	private boolean uiDone = false;

	// Data members
	private WasteProcessing processor;
	private ResourceProcessPanel processPanel;
	
	/**
	 * Constructor.
	 * 
	 * @param processor the waste processing building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelWasteProcessing(WasteProcessing processor, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelWasteProcessing.title"),
			ImageLoader.getIconByName(RECYCLE_ICON),
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
