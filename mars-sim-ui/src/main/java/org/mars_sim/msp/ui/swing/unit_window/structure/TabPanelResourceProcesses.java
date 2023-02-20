/*
 * Mars Simulation Project
 * TabPanelResourceProcesses.java
 * @date 2022-09-25
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
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

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.ResourceProcessPanel;

/**
 * A tab panel for displaying all of the resource processes in a settlement.
 */
@SuppressWarnings("serial")
public class TabPanelResourceProcesses extends TabPanel implements ActionListener {
	
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(TabPanelResourceProcesses.class.getName());

	private static final String CHEMICAL_ICON = "chemical";
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	private JComboBox<String> levelComboBox;


	private int level;

	private ResourceProcessPanel processPanel;

	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelResourceProcesses(Unit unit, MainDesktopPane desktop) {

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelResourceProcesses.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(CHEMICAL_ICON),
			Msg.getString("TabPanelResourceProcesses.title"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;
	}
	
	@Override
	protected void buildUI(JPanel content) {
		BuildingManager mgr = settlement.getBuildingManager();
		Map<Building,List<ResourceProcess>> processes = new HashMap<>();
		for(Building building : mgr.getBuildings(FunctionType.RESOURCE_PROCESSING)) {
			ResourceProcessing processing = building.getResourceProcessing();
			processes.put(building, processing.getProcesses());
		}

		// Prepare process list panel.n
		processPanel = new ResourceProcessPanel(processes);
		processPanel.setPreferredSize(new Dimension(160, 120));
		content.add(processPanel, BorderLayout.CENTER);
		
		// Create override check box panel.
		JPanel topPanel = new JPanel(new GridLayout(1, 2, 0, 0));
		content.add(topPanel, BorderLayout.NORTH);
		
		// Create override check box panel.
		JPanel overrideCheckboxPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		overrideCheckboxPane.setAlignmentY(BOTTOM_ALIGNMENT);
		topPanel.add(overrideCheckboxPane);
	
		// Create override check box.
		JCheckBox overrideCheckbox = new JCheckBox(Msg.getString("TabPanelResourceProcesses.checkbox.overrideResourceProcessToggling")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelResourceProcesses.tooltip.overrideResourceProcessToggling")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(e ->
			setResourceProcessOverride(overrideCheckbox.isSelected())
		);
		overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.RESOURCE_PROCESS));
		
		overrideCheckboxPane.add(overrideCheckbox);
		
		// Create level panel.
		JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		levelPanel.setAlignmentY(TOP_ALIGNMENT);
		topPanel.add(levelPanel);
		
		JLabel levelLabel = new JLabel("Overall Effort Level :");
		levelPanel.add(levelLabel);
			
		// Prepare level combo box
		String[] level = {"1", "2", "3", "4", "5"}; 
		levelComboBox = new JComboBox<>(level);
		levelComboBox.setSelectedItem("2");
		levelComboBox.addActionListener(this);
        
		levelPanel.add(levelComboBox);
	}


	/**
	 * Action event occurs.
	 *
	 * @param event {@link ActionEvent} the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox) {
			int newLevel = Integer.parseInt((String)levelComboBox.getSelectedItem());
			if (newLevel != level) {
				level = newLevel;
				processPanel.update();
				logger.info("The Overall Effort Level of resource processing is now at " + newLevel + ".");
			}
		}
	}	

	@Override
	public void update() {
		processPanel.update();
	}
	/**
	 * Sets the settlement resource process override flag.
	 * 
	 * @param override the resource process override flag.
	 */
	private void setResourceProcessOverride(boolean override) {
		settlement.setProcessOverride(OverrideType.RESOURCE_PROCESS, override);
	}
}
