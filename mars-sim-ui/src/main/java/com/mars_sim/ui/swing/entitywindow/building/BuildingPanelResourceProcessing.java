/*
 * Mars Simulation Project
 * BuildingPanelResourceProcessing.java
 * @date 2025-07-23
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.ResourceProcessing;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.entitywindow.structure.ResourceProcessPanel;

/**
 * The BuildingPanelResourceProcessing class is a building function panel representing
 * the resource processes of a building.
 */
@SuppressWarnings("serial")
class BuildingPanelResourceProcessing extends EntityTabPanel<Building>
 implements TemporalComponent {

	private static final String ICON = "resource";

	// Data members
	private ResourceProcessing processor;
	private ResourceProcessPanel processPanel;

	/**
	 * Constructor.
	 * 
	 * @param processor the resource processing building this panel is for.
	 * @param context the UI context
	 */
	public BuildingPanelResourceProcessing(ResourceProcessing processor, UIContext context) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelResourceProcessing.title"),
			ImageLoader.getIconByName(ICON), null,
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
