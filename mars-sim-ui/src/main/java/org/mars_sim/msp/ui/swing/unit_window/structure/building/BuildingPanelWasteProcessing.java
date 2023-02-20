/*
 * Mars Simulation Project
 * BuildingPanelWasteProcessing.java
 * @date 2022-07-26
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.WasteProcessing;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The BuildingPanelWasteProcessing class is a building function panel representing
 * the waste processes of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelWasteProcessing extends BuildingFunctionPanel {

	private static final String RECYCLE_ICON = "recycle";
	
	// Data members
	private WasteProcessing processor;
	private ResourceProcessPanel processPanel;
	
	/**
	 * Constructor.
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
