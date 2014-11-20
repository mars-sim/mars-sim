/**
 * Mars Simulation Project
 * ResourceProcessTabTabPanel.java
 * @version 3.07 2014-10-14
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * A tab panel for displaying all of the resource processes in a settlement.
 */
public class TabPanelResourceProcesses
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private List<Building> processingBuildings;
	private JScrollPane processesScrollPane;
	private JPanel processListPanel;
	private JCheckBox overrideCheckbox;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelResourceProcesses(Unit unit, MainDesktopPane desktop) { 

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelResourceProcesses.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelResourceProcesses.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Settlement settlement = (Settlement) unit;
		processingBuildings = settlement.getBuildingManager().getBuildings(BuildingFunction.RESOURCE_PROCESSING);

		// Prepare resource processes label panel.
		JPanel resourceProcessesLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(resourceProcessesLabelPanel);

		// Prepare esource processes label.
		JLabel resourceProcessesLabel = new JLabel(Msg.getString("TabPanelResourceProcesses.label"), JLabel.CENTER); //$NON-NLS-1$
		resourceProcessesLabelPanel.add(resourceProcessesLabel);

		// Create scroll panel for the outer table panel.
		processesScrollPane = new JScrollPane();
		processesScrollPane.setPreferredSize(new Dimension(220, 280));
		// increase vertical mousewheel scrolling speed for this one
		processesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		centerContentPanel.add(processesScrollPane,BorderLayout.CENTER);         

		// Prepare process list panel.
		processListPanel = new JPanel(new GridLayout(0, 1, 5, 2));
		processListPanel.setBorder(new MarsPanelBorder());
		processesScrollPane.setViewportView(processListPanel);
		populateProcessList();

		// Create override check box panel.
		JPanel overrideCheckboxPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(overrideCheckboxPane,BorderLayout.SOUTH);

		// Create override check box.
		overrideCheckbox = new JCheckBox(Msg.getString("TabPanelResourceProcesses.checkbox.overrideResourceProcessToggling")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelResourceProcesses.tooltip.overrideResourceProcessToggling")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setResourceProcessOverride(overrideCheckbox.isSelected());
			}
		});
		overrideCheckbox.setSelected(settlement.getManufactureOverride());
		overrideCheckboxPane.add(overrideCheckbox);
	}

	/**
	 * Populates the process list panel with all building processes.
	 */
	private void populateProcessList() {
		// Clear the list.
		processListPanel.removeAll();

		//    	try {
		// Add a label for each process in each processing building.
		Iterator<Building> i = processingBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			ResourceProcessing processing = (ResourceProcessing) building.getFunction(BuildingFunction.RESOURCE_PROCESSING);
			Iterator<ResourceProcess> j = processing.getProcesses().iterator();
			while (j.hasNext()) {
				ResourceProcess process = j.next();
				processListPanel.add(new ResourceProcessPanel(process, building));
			}
		}
		//    	}
		//    	catch (BuildingException e) {
		//    		e.printStackTrace(System.err);
		//    	}
	}

	@Override
	public void update() {
		// Check if building list has changed.
		Settlement settlement = (Settlement) unit;
		List<Building> tempBuildings = settlement.getBuildingManager().getBuildings(BuildingFunction.RESOURCE_PROCESSING);
		if (!tempBuildings.equals(processingBuildings)) {
			// Populate process list.
			processingBuildings = tempBuildings;
			populateProcessList();
			processesScrollPane.validate();
		}
		else {
			// Update process list.
			Component[] components = processListPanel.getComponents();
			for (Component component : components) {
				ResourceProcessPanel panel = (ResourceProcessPanel) component;
				panel.update();
			}
		}
	}

	/**
	 * Sets the settlement resource process override flag.
	 * @param override the resource process override flag.
	 */
	private void setResourceProcessOverride(boolean override) {
		Settlement settlement = (Settlement) unit;
		settlement.setResourceProcessOverride(override);
	}

	/**
	 * An internal class for a resource process panel.
	 */
	private static class ResourceProcessPanel
	extends JPanel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		private ResourceProcess process;
		private JLabel label;
		private JButton toggleButton;
		private ImageIcon dotGreen;
		private ImageIcon dotRed;
		private DecimalFormat decFormatter = new DecimalFormat(Msg.getString("TabPanelResourceProcesses.decimalFormat")); //$NON-NLS-1$

		/**
		 * Constructor.
		 * @param process the resource process.
		 * @param building the building the process is in.
		 */
		ResourceProcessPanel(ResourceProcess process, Building building) {
			// Use JPanel constructor.
			super();

			setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

			this.process = process;

			toggleButton = new JButton();
			toggleButton.setMargin(new Insets(0, 0, 0, 0));
			toggleButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					ResourceProcess process = getProcess();
					process.setProcessRunning(!process.isProcessRunning());
					update();
				}
			});
			toggleButton.setToolTipText(Msg.getString("TabPanelResourceProcesses.tooltip.toggleButton")); //$NON-NLS-1$
			add(toggleButton);
			// 2014-11-17 Changed building.getName() to building.getNickName()
			label = new JLabel(Msg.getString("TabPanelResourceProcesses.processLabel", building.getNickName(), process.getProcessName())); //$NON-NLS-1$
			add(label);

			// Load green and red dots.
			dotGreen = ImageLoader.getIcon(Msg.getString("img.dotGreen")); //$NON-NLS-1$
			dotRed = ImageLoader.getIcon(Msg.getString("img.dotRed")); //$NON-NLS-1$

			if (process.isProcessRunning()) toggleButton.setIcon(dotGreen);
			else toggleButton.setIcon(dotRed);

			setToolTipText(getToolTipString(building));
		}

		// TODO internationalize the resource processes' dynamic tooltip
		private String getToolTipString(Building building) {
			StringBuilder result = new StringBuilder("<html>");
			result.append("Resource Process: ").append(process.getProcessName()).append("<br>");
			// 2014-11-17 Changed building.getName() to building.getNickName()
			result.append("Building: ").append(building.getNickName()).append("<br>");
			result.append("Power Required: ").append(decFormatter.format(process.getPowerRequired())).append(" kW<br>");
			result.append("Process Inputs:<br>");
			Iterator<AmountResource> i = process.getInputResources().iterator();
			while (i.hasNext()) {
				AmountResource resource = i.next();
				double rate = process.getMaxInputResourceRate(resource) * 1000D;
				String rateString = decFormatter.format(rate);
				result.append("&nbsp;&nbsp;");
				if (process.isAmbientInputResource(resource)) result.append("* ");
				result.append(resource.getName()).append(": ").append(rateString).append(" kg/sol<br>");
			}
			result.append("Process Outputs:<br>");
			Iterator<AmountResource> j = process.getOutputResources().iterator();
			while (j.hasNext()) {
				AmountResource resource = j.next();
				double rate = process.getMaxOutputResourceRate(resource) * 1000D;
				String rateString = decFormatter.format(rate);
				result.append("&nbsp;&nbsp;").append(resource.getName()).append(": ").append(rateString).append(" kg/sol<br>");
			}
			result.append("</html>");
			return result.toString();
		}

		/**
		 * Update the label.
		 */
		void update() {
			if (process.isProcessRunning()) toggleButton.setIcon(dotGreen);
			else toggleButton.setIcon(dotRed);
		}

		private ResourceProcess getProcess() {
			return process;
		}
	}
}