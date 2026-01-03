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

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.WasteProcessing;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;

/**
 * The BuildingPanelWasteProcessing class is a building function panel representing
 * the waste processes of a building.
 */
@SuppressWarnings("serial")
class BuildingPanelWasteProcessing extends EntityTabPanel<Building>
	implements TemporalComponent {

	private static final String RECYCLE_ICON = "recycle";

	// Data members
	private WasteProcessing processor;
	private ResourceProcessPanel processPanel;
	
	/**
	 * Constructor.
	 * 
	 * @param processor the waste processing building this panel is for.
	 * @param context the UI context
	 */
	public BuildingPanelWasteProcessing(WasteProcessing processor, UIContext context) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelWasteProcessing.title"),
			ImageLoader.getIconByName(RECYCLE_ICON), null,
			context, processor.getBuilding() 
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
	public void clockUpdate(ClockPulse pulse) {
		processPanel.update();
	}
}
