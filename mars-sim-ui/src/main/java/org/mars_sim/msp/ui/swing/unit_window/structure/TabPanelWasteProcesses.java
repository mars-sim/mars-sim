/*
 * Mars Simulation Project
 * TabPanelWasteProcesses.java
 * @date 2022-06-15
 * @author Manny Kung
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
import org.mars_sim.msp.core.structure.building.function.WasteProcess;
import org.mars_sim.msp.core.structure.building.function.WasteProcessing;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * A tab panel for displaying all of the waste processes in a settlement.
 */
@SuppressWarnings("serial")
public class TabPanelWasteProcesses
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
	public TabPanelWasteProcesses(Unit unit, MainDesktopPane desktop) {

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelWasteProcesses.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelWasteProcesses.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;
	}
	
	@Override
	protected void buildUI(JPanel content) {
		mgr = settlement.getBuildingManager();
		buildings = mgr.getBuildings(FunctionType.WASTE_PROCESSING);
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
		overrideCheckbox = new JCheckBox(Msg.getString("TabPanelWasteProcesses.checkbox.overrideWasteProcessToggling")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelWasteProcesses.tooltip.overrideWasteProcessToggling")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(e ->
			setWasteProcessesOverride(overrideCheckbox.isSelected())
		);
		overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.WASTE_PROCESSING));
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
			WasteProcessing processing = building.getWasteProcessing();
			Iterator<WasteProcess> j = processing.getProcesses().iterator();
			while (j.hasNext()) {
				WasteProcess process = j.next();
				processListPanel.add(new WasteProcessPanel(process, building));
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
				WasteProcessPanel panel = (WasteProcessPanel) component;
				panel.update();
			}
		}
	}

	private List<Building> selectBuildings() {
		return mgr.getBuildings(FunctionType.WASTE_PROCESSING);
	}

	/**
	 * Sets the settlement waste process override flag.
	 * @param override the waste process override flag.
	 */
	private void setWasteProcessesOverride(boolean override) {
		settlement.setProcessOverride(OverrideType.WASTE_PROCESSING, override);
	}

	/**
	 * An internal class for a waste process panel.
	 */
	private static class WasteProcessPanel
	extends JPanel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		private WasteProcess process;
		private JLabel label;
		private JButton toggleButton;
		private ImageIcon dotGreen;
		private ImageIcon dotRed;
		private DecimalFormat decFormatter = new DecimalFormat(Msg.getString("TabPanelWasteProcesses.decimalFormat")); //$NON-NLS-1$

		/**
		 * Constructor.
		 * @param process the waste process.
		 * @param building the building the process is in.
		 */
		WasteProcessPanel(WasteProcess process, Building building) {
			// Use JPanel constructor.
			super();

			setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

			this.process = process;

			toggleButton = new JButton();
			toggleButton.setMargin(new Insets(0, 0, 0, 0));
			toggleButton.addActionListener(e -> {
				WasteProcess p = getProcess();
				p.setProcessRunning(!p.isProcessRunning());
				update();
			});
			toggleButton.setToolTipText(Msg.getString("TabPanelWasteProcesses.tooltip.toggleButton")); //$NON-NLS-1$
			add(toggleButton);
			label = new JLabel(Msg.getString("TabPanelWasteProcesses.processLabel", building.getNickName(), process.getProcessName())); //$NON-NLS-1$
			add(label);

			// Load green and red dots.
			dotGreen = ImageLoader.getIcon(Msg.getString("img.dotGreen_full")); //$NON-NLS-1$
			dotRed = ImageLoader.getIcon(Msg.getString("img.dotRed")); //$NON-NLS-1$

			if (process.isProcessRunning()) toggleButton.setIcon(dotGreen);
			else toggleButton.setIcon(dotRed);

			setToolTipText(getToolTipString(building));
		}

		// NOTE: internationalize the waste processes' dynamic tooltip
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
				double rate = process.getMaxInputRate(resource) * 1000D;
				String rateString = decFormatter.format(rate);
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
				double rate = process.getMaxOutputRate(resource) * 1000D;
				String rateString = decFormatter.format(rate);
				result.append(Conversion.capitalize(ResourceUtil.findAmountResource(resource).getName()))
					.append(" @ ").append(rateString).append(" kg/sol<br>");
				jj++;
			}
			// Added a note to denote an ambient input resource
			if (ambientStr.equals("*"))
				result.append("&emsp;<i>Note: * denotes an ambient resource</i>");
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

		private WasteProcess getProcess() {
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
