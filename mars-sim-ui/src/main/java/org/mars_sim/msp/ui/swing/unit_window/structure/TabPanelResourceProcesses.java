/*
 * Mars Simulation Project
 * TabPanelResourceProcesses.java
 * @date 2021-12-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * A tab panel for displaying all of the resource processes in a settlement.
 */
@SuppressWarnings("serial")
public class TabPanelResourceProcesses
extends TabPanel {

	/** The Settlement instance. */
	private Settlement settlement;
	
	private List<Building> buildings;
	private JPanel processListPanel;
	private JCheckBox overrideCheckbox;

	private BuildingManager mgr;

	private int size;

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
			null,
			Msg.getString("TabPanelResourceProcesses.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;
	}
	
	@Override
	protected void buildUI(JPanel content) {
		mgr = settlement.getBuildingManager();
		buildings = mgr.getBuildings(FunctionType.RESOURCE_PROCESSING);
		size = buildings.size();

		// Prepare process list panel.
		processListPanel = new JPanel(new GridLayout(0, 1, 5, 2));
		processListPanel.setAlignmentY(TOP_ALIGNMENT);
		processListPanel.setBorder(new MarsPanelBorder());
		content.add(processListPanel, BorderLayout.CENTER);
		populateProcessList();

		// Create override check box panel.
		JPanel overrideCheckboxPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		content.add(overrideCheckboxPane, BorderLayout.NORTH);

		// Create override check box.
		overrideCheckbox = new JCheckBox(Msg.getString("TabPanelResourceProcesses.checkbox.overrideResourceProcessToggling")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelResourceProcesses.tooltip.overrideResourceProcessToggling")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(e ->
			setResourceProcessOverride(overrideCheckbox.isSelected())
		);
		overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.RESOURCE_PROCESS));
		overrideCheckboxPane.add(overrideCheckbox);
	}

	/**
	 * Populates the process list panel with all building processes.
	 */
	private void populateProcessList() {
		// Clear the list.
		processListPanel.removeAll();

		// Add a label for each process in each processing building.
		Iterator<Building> i = buildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			ResourceProcessing processing = building.getResourceProcessing();
			Iterator<ResourceProcess> j = processing.getProcesses().iterator();
			while (j.hasNext()) {
				ResourceProcess process = j.next();
				processListPanel.add(new ResourceProcessPanel(process, building));
			}
		}
	}

	@Override
	public void update() {
		// Check if building list has changed.
		List<Building> newBuildings = selectBuildings();
		int newSize = buildings.size();
		if (size != newSize) {
			size = newSize;
			buildings = selectBuildings();
			Collections.sort(buildings);
			populateProcessList();
		}
		else if (!buildings.equals(newBuildings)) {
			buildings = newBuildings;
			Collections.sort(buildings);
			populateProcessList();
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

	private List<Building> selectBuildings() {
		return mgr.getBuildings(FunctionType.RESOURCE_PROCESSING);
	}

	/**
	 * Sets the settlement resource process override flag.
	 * @param override the resource process override flag.
	 */
	private void setResourceProcessOverride(boolean override) {
		settlement.setProcessOverride(OverrideType.RESOURCE_PROCESS, override);
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
			toggleButton.addActionListener(e -> {
					ResourceProcess p = getProcess();
					p.setProcessRunning(!p.isProcessRunning());
					update();
			});
			toggleButton.setToolTipText(Msg.getString("TabPanelResourceProcesses.tooltip.toggleButton")); //$NON-NLS-1$
			add(toggleButton);
			label = new JLabel(Msg.getString("TabPanelResourceProcesses.processLabel", building.getNickName(), process.getProcessName())); //$NON-NLS-1$
			add(label);

			// Load green and red dots.
			dotGreen = ImageLoader.getIcon(Msg.getString("img.dotGreen_full")); //$NON-NLS-1$
			dotRed = ImageLoader.getIcon(Msg.getString("img.dotRed")); //$NON-NLS-1$

			if (process.isProcessRunning()) toggleButton.setIcon(dotGreen);
			else toggleButton.setIcon(dotRed);

			setToolTipText(getToolTipString(building));
		}

		// NOTE: internationalize the resource processes' dynamic tooltip
		// Align text to improved tooltip readability (for English Locale only)
		private String getToolTipString(Building building) {
			StringBuilder result = new StringBuilder("<html>");
			result.append("&emsp;&nbsp;Process:&emsp;").append(process.getProcessName()).append("<br>");
			result.append("&emsp;&nbsp;Building:&emsp;").append(building.getNickName()).append("<br>");
			result.append("Power Req:&emsp;").append(decFormatter.format(process.getPowerRequired())).append(" kW<br>");
			result.append("&emsp;&emsp;&nbsp;Inputs:&emsp;");
			Iterator<Integer> i = process.getInputResources().iterator();
			String ambientStr = "";
			int ii = 0;
			while (i.hasNext()) {
				if (ii!=0)	result.append("&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;");
				Integer resource = i.next();
				double rate = process.getMaxInputResourceRate(resource) * 1000D;
				String rateString = decFormatter.format(rate);
				//result.append("&nbsp;&nbsp;&emsp;");
				if (process.isAmbientInputResource(resource)) ambientStr = "*";
				result.append(Conversion.capitalize(ResourceUtil.findAmountResource(resource).getName()))
					.append(ambientStr).append(" @ ")
					.append(rateString).append(" kg/sol<br>");
				ii++;
			}
			result.append("&emsp;&nbsp;&nbsp;Outputs:&emsp;");
			Iterator<Integer> j = process.getOutputResources().iterator();
			int jj = 0;
			while (j.hasNext()) {
				if (jj!=0) result.append("&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;");
				Integer resource = j.next();
				double rate = process.getMaxOutputResourceRate(resource) * 1000D;
				String rateString = decFormatter.format(rate);
				result.append(Conversion.capitalize(ResourceUtil.findAmountResource(resource).getName()))
					.append(" @ ").append(rateString).append(" kg/sol<br>");
				jj++;
			}
			// Added a note to denote an ambient input resource
			if (ambientStr == "*")
				result.append("&emsp;<i>Note:  * denotes an ambient resource</i>");
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
	
	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		buildings = null;
		processListPanel = null;
		overrideCheckbox = null;
		settlement = null;
		mgr = null;
	}	
}
