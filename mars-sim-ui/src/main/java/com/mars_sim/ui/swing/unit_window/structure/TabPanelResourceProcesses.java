/*
 * Mars Simulation Project
 * TabPanelResourceProcesses.java
 * @date 2022-09-25
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.ResourceProcessing;
import com.mars_sim.core.logging.SimLogger;
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
 * A tab panel for displaying all of the resource processes in a settlement.
 */
class TabPanelResourceProcesses extends EntityTabPanel<Settlement>
		implements ActionListener, TemporalComponent {
	
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(TabPanelResourceProcesses.class.getName());

	private static final String AVERAGE_DUTY_CYCLE = "Average Duty Cycle: ";
	private static final String _PERCENT = " %";
	private static final String ICON = "resource";
	private static final String[] PERCENT_EFFORT = {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"}; 
	private static final String ONE_HUNDRED = "100";
	
	private JComboBox<String> percentEffortComboBox;
	
	private JLabel dutyCycleLabel;
	/** Track the percentage of effort. */ 
	private double percent = 100;
	
	private ResourceProcessPanel processPanel;

	/**
	 * Constructor.
	 * 
	 * @param settlement The settlement to display.
	 * @param context The UI context.
	 */
	public TabPanelResourceProcesses(Settlement settlement, UIContext context) {

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelResourceProcesses.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(ICON), null,
			context, settlement
		);
	}
	
	@Override
	protected void buildUI(JPanel content) {
		var settlement = getEntity();
			
		double sum = 0;
		double size = 0;
		BuildingManager mgr = settlement.getBuildingManager();
		Map<Building, List<ResourceProcess>> processes = new HashMap<>();
		for (Building building : mgr.getBuildings(FunctionType.RESOURCE_PROCESSING)) {
			ResourceProcessing processing = building.getResourceProcessing();
			size++;
			sum += processing.getOverallPercentDuty();
			processes.put(building, processing.getProcesses());
		}
		if (size == 0) 
			size = 1;
		double averagePercentDuty = sum / size;
		
		// Prepare process list panel
		processPanel = new ResourceProcessPanel(processes, getContext());
		processPanel.setPreferredSize(new Dimension(160, 120));
		content.add(processPanel, BorderLayout.CENTER);
		
		// Create override check box panel.
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		content.add(topPanel, BorderLayout.NORTH);
		
		JPanel gridPanel = new JPanel(new GridLayout(1, 3, 2, 2));
		gridPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		topPanel.add(gridPanel);
				
		// Create level panel.
		JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		gridPanel.add(levelPanel);
		
		JLabel levelLabel = new JLabel("Percentage of Effort:");
		levelLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		levelLabel.setToolTipText("The percentage of effort devoted to producing output resources. Apply to all processes.");
		levelPanel.add(levelLabel);
			
		// Prepare percentage of effort combo box
		percentEffortComboBox = new JComboBox<>(PERCENT_EFFORT);
		percentEffortComboBox.setAlignmentY(Component.TOP_ALIGNMENT);
		percentEffortComboBox.setPrototypeDisplayValue(ONE_HUNDRED);
		percentEffortComboBox.setSelectedItem(ONE_HUNDRED);
		percentEffortComboBox.addActionListener(this);
        
		levelPanel.add(percentEffortComboBox);
		
		// Create duty cycle panel.
		JPanel dutyCyclePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		dutyCyclePanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		gridPanel.add(dutyCyclePanel);
		
		dutyCycleLabel = new JLabel(AVERAGE_DUTY_CYCLE + Math.round(averagePercentDuty * 10.0)/10.0 + _PERCENT);
		dutyCycleLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		dutyCyclePanel.add(dutyCycleLabel);
		
		// Create override check box panel.
		JPanel overrideCheckboxPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		overrideCheckboxPane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		gridPanel.add(overrideCheckboxPane);
	
		// Create override check box.
		JCheckBox overrideCheckbox = new JCheckBox(Msg.getString("TabPanelResourceProcesses.checkbox.overrideResourceProcessToggling")); //$NON-NLS-1$
		overrideCheckbox.setAlignmentY(Component.TOP_ALIGNMENT);
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelResourceProcesses.tooltip.overrideResourceProcessToggling")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(e ->
			setResourceProcessOverride(overrideCheckbox.isSelected())
		);
		overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.RESOURCE_PROCESS));
		
		overrideCheckboxPane.add(overrideCheckbox);
	}


	/**
	 * Action event occurs.
	 *
	 * @param event {@link ActionEvent} the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox) {
			int newPercent = Integer.parseInt((String)percentEffortComboBox.getSelectedItem());
			if (percent != newPercent) {
				percent = newPercent;
				processPanel.setPercentEffort(newPercent);
				processPanel.update();
				logger.info(getEntity(), "Manually changed to " + newPercent + " % as the overall output effort in all resource processes.");
			}
		}
	}	

	/**
	 * Sets the settlement resource process override flag.
	 * 
	 * @param override the resource process override flag.
	 */
	private void setResourceProcessOverride(boolean override) {
		getEntity().setProcessOverride(OverrideType.RESOURCE_PROCESS, override);
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		processPanel.update();
		
		double sum = 0;
		double size = 0;
		BuildingManager mgr = getEntity().getBuildingManager();
		for (Building building : mgr.getBuildings(FunctionType.RESOURCE_PROCESSING)) {
			ResourceProcessing processing = building.getResourceProcessing();
			size++;
			sum += processing.getOverallPercentDuty();
		}
		if (size == 0) 
			size = 1;
		double averagePercentDuty = sum / size;
		
		dutyCycleLabel.setText(AVERAGE_DUTY_CYCLE + Math.round(averagePercentDuty * 10.0)/10.0 + _PERCENT);
	}
}
