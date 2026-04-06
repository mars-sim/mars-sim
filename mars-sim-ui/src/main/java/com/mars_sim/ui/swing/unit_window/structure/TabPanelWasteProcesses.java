/*
 * Mars Simulation Project
 * TabPanelWasteProcesses.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.WasteProcessing;
import com.mars_sim.core.resourceprocess.ResourceProcess;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.entitywindow.structure.ResourceProcessPanel;

/**
 * A tab panel for displaying all of the waste processes in a settlement.
 */
@SuppressWarnings("serial")
class TabPanelWasteProcesses extends EntityTabPanel<Settlement>
			implements TemporalComponent {
	
	private static final String RECYCLE_ICON = "recycle";

	private ResourceProcessPanel processPanel;

	/**
	 * Constructor.
	 * 
	 * @param settlement The settlement to display.
	 * @param context The UI context.
	 */
	public TabPanelWasteProcesses(Settlement settlement, UIContext context) {

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelWasteProcesses.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(RECYCLE_ICON), null,
			context, settlement
		);
	}
	
	@Override
	protected void buildUI(JPanel content) {
		BuildingManager mgr = getEntity().getBuildingManager();
		Map<Building, List<ResourceProcess>> processes = new HashMap<>();
		for (Building building : mgr.getBuildings(FunctionType.WASTE_PROCESSING)) {
			WasteProcessing processing = building.getWasteProcessing();
			processes.put(building, processing.getProcesses());
		}

		// Prepare process list panel.n
		processPanel = new ResourceProcessPanel(processes, getContext());
		processPanel.setPreferredSize(new Dimension(160, 120));
		content.add(processPanel, BorderLayout.CENTER);

		// Create override check box panel.
		JPanel overrideCheckboxPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		content.add(overrideCheckboxPane, BorderLayout.NORTH);

		// Create override check box.
		JCheckBox overrideCheckbox = new JCheckBox(Msg.getString("TabPanelWasteProcesses.checkbox.overrideWasteProcessToggling")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelWasteProcesses.tooltip.overrideWasteProcessToggling")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(e ->
			setWasteProcessesOverride(overrideCheckbox.isSelected())
		);
		overrideCheckbox.setSelected(getEntity().getProcessOverride(OverrideType.WASTE_PROCESSING));
		overrideCheckboxPane.add(overrideCheckbox);
	}

	/**
	 * Sets the settlement waste process override flag.
	 * @param override the waste process override flag.
	 */
	private void setWasteProcessesOverride(boolean override) {
		getEntity().setProcessOverride(OverrideType.WASTE_PROCESSING, override);
	}

	/**
	 * Update status of processes
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		processPanel.update();
	}
}
