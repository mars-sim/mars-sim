/**
 * Mars Simulation Project
 * MaintenanceTabPanel.java
 * @version 3.07 2015-03-06
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

public class TabPanelMaintenance
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private Settlement settlement;
	private List<Building> buildingsList;
	private JScrollPane maintenanceScrollPane;
	private JPanel maintenanceListPanel;
	private List<Malfunction> malfunctionsList;
	private JScrollPane malfunctionsScrollPane;
	private JPanel malfunctionsListPanel;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelMaintenance(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			"Maint",
			null,
			"Maintenance",
			unit, desktop
		);

		settlement = (Settlement) unit;

		// Create topPanel.
		JPanel topPanel = new JPanel(new GridLayout(2, 1));
		centerContentPanel.add(topPanel);

		// Create maintenance panel.
		JPanel maintenancePanel = new JPanel(new BorderLayout());
		topPanel.add(maintenancePanel);

		// Create maintenance label.
		JLabel titleLabel = new JLabel("Building Maintenance", JLabel.CENTER);
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		maintenancePanel.add(titleLabel, BorderLayout.NORTH);

		// Create scroll pane for maintenance list panel.
		maintenanceScrollPane = new JScrollPane();
		// increase vertical mousewheel scrolling speed for this one
		maintenanceScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		maintenanceScrollPane.setPreferredSize(new Dimension(200, 75));
		maintenancePanel.add(maintenanceScrollPane, BorderLayout.CENTER);

		// Prepare maintenance list panel.
		maintenanceListPanel = new JPanel(new GridLayout(0, 1, 0, 0));
		maintenanceListPanel.setBorder(new MarsPanelBorder());
		maintenanceScrollPane.setViewportView(maintenanceListPanel);
		populateMaintenanceList();

		// Create malfunctions panel.
		JPanel malfunctionsPanel = new JPanel(new BorderLayout());
		topPanel.add(malfunctionsPanel);

		// Create malfunctions label.
		JLabel malfunctionsLabel = new JLabel("Building Malfunctions", JLabel.CENTER);
		malfunctionsLabel.setFont(new Font("Serif", Font.BOLD, 16));
		malfunctionsLabel.setForeground(new Color(102, 51, 0)); // dark brown
		malfunctionsPanel.add(malfunctionsLabel, BorderLayout.NORTH);

		// Create scroll panel for malfunctions list panel.
		malfunctionsScrollPane = new JScrollPane();
		// increase vertical mousewheel scrolling speed for this one
		malfunctionsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		malfunctionsScrollPane.setPreferredSize(new Dimension(200, 75));
		malfunctionsPanel.add(malfunctionsScrollPane, BorderLayout.CENTER);

		// Prepare malfunctions outer list panel.
		JPanel malfunctionsOuterListPanel = new JPanel(new BorderLayout(0, 0));
		malfunctionsOuterListPanel.setBorder(new MarsPanelBorder());
		malfunctionsScrollPane.setViewportView(malfunctionsOuterListPanel);

		// Prepare malfunctions list panel.
		malfunctionsListPanel = new JPanel();
		malfunctionsListPanel.setLayout(new BoxLayout(malfunctionsListPanel, BoxLayout.Y_AXIS));
		malfunctionsOuterListPanel.add(malfunctionsListPanel, BorderLayout.NORTH);

		populateMalfunctionsList();
	}

	/**
	 * Populates the maintenance list.
	 */
	private void populateMaintenanceList() {
		// Clear the list.
		maintenanceListPanel.removeAll();

		// Populate the list.
		buildingsList = settlement.getBuildingManager().getSortedBuildings();//getACopyOfBuildings()
		Iterator<Building> i = buildingsList.iterator();
		while (i.hasNext()) {
			JPanel panel = new BuildingMaintenancePanel(i.next());
			maintenanceListPanel.add(panel);
		}
	}

	/**
	 * Populates the malfunctions list.
	 */
	private void populateMalfunctionsList() {
		// Clear the list.
		malfunctionsListPanel.removeAll();

		// Populate the list.
		if (malfunctionsList == null) malfunctionsList = new ArrayList<Malfunction>();
		else malfunctionsList.clear();
		Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();//getACopyOfBuildings().iterator();.getACopyOfBuildings().iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Iterator<Malfunction> j = building.getMalfunctionManager().getMalfunctions().iterator();
			while (j.hasNext()) {
				Malfunction malfunction = j.next();
				malfunctionsList.add(malfunction);
				JPanel panel = new BuildingMalfunctionPanel(malfunction, building);
				malfunctionsListPanel.add(panel);
			}
		}
	}

	/**
	 * Update the tab panel.
	 */
	public void update() {

		// Check if building list has changed.
		List<Building> tempBuildings = ((Settlement) unit).getBuildingManager().getSortedBuildings();
		if (!tempBuildings.equals(buildingsList)) {
			// Populate maintenance list.
			populateMaintenanceList();
			maintenanceScrollPane.validate();
		}
		else {
			// Update all building maintenance panels.
			Component[] components = maintenanceListPanel.getComponents();
			for (Component component : components) ((BuildingMaintenancePanel) component).update();
		}

		// Create temporary malfunctions list.
		List<Malfunction> tempMalfunctions = new ArrayList<Malfunction>();
		Iterator<Building> i = tempBuildings.iterator();
		while (i.hasNext()) {
			Iterator<Malfunction> j = i.next().getMalfunctionManager().getMalfunctions().iterator();
			while (j.hasNext()) malfunctionsList.add(j.next());
		}

		// Check if malfunctions list has changed.
		if (!tempMalfunctions.equals(malfunctionsList)) {
			// Populate malfunctions list.
			populateMalfunctionsList();
			malfunctionsListPanel.validate();
			malfunctionsScrollPane.validate();
		}
		else {
			// Update all building malfunction panels.
			Component[] components = malfunctionsListPanel.getComponents();
			for (Component component : components) ((BuildingMalfunctionPanel) component).update();
		}
	}

	/**
	 * Gets the parts string.
	 * @return string.
	 */
	private String getPartsString(Map<Part, Integer> parts, boolean useHtml) {
		/*
		StringBuilder buf = new StringBuilder("Parts: ");
		if (parts.size() > 0) {
			Iterator<Part> i = parts.keySet().iterator();
			while (i.hasNext()) {
				Part part = i.next();
				int number = parts.get(part);
				// 2014-11-20 Capitalized part.getName()
				buf.append(number).append(" ").append(Conversion.capitalize(part.getName()));
				if (i.hasNext()) buf.append(", ");
			}
		}
		else buf.append("None.");
		return buf.toString();
		*/
        StringBuilder buf = new StringBuilder("Needed Parts: ");
    	//Map<Part, Integer> parts = malfunctionable.getMalfunctionManager().getMaintenanceParts();
    	if (parts.size() > 0) {
    		Iterator<Part> i = parts.keySet().iterator();
    		while (i.hasNext()) {
    			Part part = i.next();
    			int number = parts.get(part);
				if (useHtml) buf.append("<br>");
				buf.append(number).append(" ").append(Conversion.capitalize(part.getName()));
				if (i.hasNext()) buf.append(", ");
				else {
					buf.append(".");
					if (useHtml) buf.append("<br>");
				}
      		}
    	}
    	else buf.append("None.");

    	return buf.toString();
	}

	/**
	 * Inner class for the building maintenance panel.
	 */
	private class BuildingMaintenancePanel
	extends JPanel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private MalfunctionManager manager;
		private int lastCompletedCache;
		private int wearConditionCache;
		private BoundedRangeModel progressBarModel;
		private JLabel lastLabel;
		private JLabel partsLabel;
		private JLabel wearConditionLabel;

		/**
		 * Constructor.
		 * @param building the building to display.
		 */
		public BuildingMaintenancePanel(Building building) {
			// User JPanel constructor.
			super();

			manager = building.getMalfunctionManager();

			setLayout(new GridLayout(4, 1, 0, 0));
			setBorder(new MarsPanelBorder());

			JLabel buildingLabel = new JLabel(building.getNickName(), JLabel.LEFT);
			add(buildingLabel);

			// 2015-03-12 Added wear condition cache and label.
			wearConditionCache = (int) Math.round(manager.getWearCondition());
			wearConditionLabel = new JLabel(Msg.getString("BuildingPanelMaintenance.wearCondition",
					wearConditionCache), JLabel.CENTER);
			wearConditionLabel.setToolTipText(Msg.getString("BuildingPanelMaintenance.toolTip"));
			add(wearConditionLabel);

			JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
			add(mainPanel);

			lastCompletedCache = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
			lastLabel = new JLabel("Last completed : " + lastCompletedCache + " sols ago", JLabel.LEFT);
			mainPanel.add(lastLabel, BorderLayout.WEST);

			// Prepare progress bar panel.
			JPanel progressBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			mainPanel.add(progressBarPanel, BorderLayout.CENTER);

			// Prepare progress bar.
			JProgressBar progressBar = new JProgressBar();
			progressBarModel = progressBar.getModel();
			progressBar.setStringPainted(true);
			progressBar.setPreferredSize(new Dimension(80, 15));
			progressBarPanel.add(progressBar);

			// Set initial value for progress bar.
			double completed = manager.getMaintenanceWorkTimeCompleted();
			double total = manager.getMaintenanceWorkTime();
			int percentDone = (int) (100D * (completed / total));
			progressBarModel.setValue(percentDone);

			// Prepare parts label.
			partsLabel = new JLabel(getPartsString(manager.getMaintenanceParts(), false), JLabel.CENTER);
			partsLabel.setPreferredSize(new Dimension(-1, -1));
			add(partsLabel);

			// Add tooltip.
			lastLabel.setToolTipText(getToolTipString());
		}

		/**
		 * Update this panel.
		 */
		void update() {
			// Update progress bar.
			double completed = manager.getMaintenanceWorkTimeCompleted();
			double total = manager.getMaintenanceWorkTime();
			int percentDone = (int) (100D * (completed / total));
			progressBarModel.setValue(percentDone);

			// // 2015-03-12 Added wear condition cache and label
			int wearCondition = (int) Math.round(manager.getWearCondition());
			if (wearCondition != wearConditionCache) {
				wearConditionCache = wearCondition;
				wearConditionLabel.setText(Msg.getString("BuildingPanelMaintenance.wearCondition",
						wearConditionCache));
			}

			// Update last completed.
			int lastCompleted = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
			if (lastCompleted != lastCompletedCache) {
				lastCompletedCache = lastCompleted;
				lastLabel.setText("Last completed : " + lastCompletedCache + " sols ago");
			}

			Map<Part, Integer> parts = manager.getMaintenanceParts();

			// Update parts label.
			partsLabel.setText(getPartsString(parts, false));

			// Update tool tip.
			lastLabel.setToolTipText(getToolTipString());

	        // Update tool tip.
			partsLabel.setToolTipText("<html>" + getPartsString(parts, true) + "</html>");

		}

		/**
		 * Creates multi-line tool tip text.

		private String getToolTipString() {
			StringBuilder result = new StringBuilder("<html>");
			int maintSols = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
			result.append("Last completed maintenance: ").append(maintSols).append(" Sols<br>");
			result.append("Repair ").append(getPartsString(manager.getMaintenanceParts()).toLowerCase());
			result.append("</html>");

			return result.toString();
		}
		*/
		private String getToolTipString() {
			StringBuilder result = new StringBuilder("<html>");
			result.append("The Last Complete Maintenance Was Done ").append(lastCompletedCache).append(" Sols Ago<br>");
			result.append("</html>");
			return result.toString();
		}
	}

	/**
	 * Inner class for building malfunction panel.
	 */
	private class BuildingMalfunctionPanel
	extends JPanel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		private Malfunction malfunction;
		private JLabel malfunctionLabel;
		private BoundedRangeModel progressBarModel;
		private JLabel partsLabel;

		/**
		 * Constructor.
		 * @param malfunction the malfunction for the panel.
		 * @param building the building the malfunction is in.
		 */
		public BuildingMalfunctionPanel(Malfunction malfunction, Building building) {
			// Use JPanel constructor
			super();

			// Initialize data members
			this.malfunction = malfunction;

			// Set layout and border.
			setLayout(new GridLayout(4, 1, 0, 0));
			setBorder(new MarsPanelBorder());

			// Prepare the building label.
			JLabel buildingLabel = new JLabel(building.getNickName(), JLabel.LEFT);
			add(buildingLabel);

			// Prepare the malfunction label.
			malfunctionLabel = new JLabel(malfunction.getName(), JLabel.LEFT);
			if (malfunction.getCompletedEmergencyWorkTime() < malfunction.getEmergencyWorkTime()) {
				malfunctionLabel.setText(malfunction.getName() + " - Emergency");
				malfunctionLabel.setForeground(Color.red);
			}
			add(malfunctionLabel);

			// Progress bar panel.
			JPanel progressBarPanel = new JPanel(new BorderLayout(0, 0));
			add(progressBarPanel, BorderLayout.CENTER);

			// Prepare progress bar.
			JProgressBar progressBar = new JProgressBar();
			progressBarModel = progressBar.getModel();
			progressBar.setStringPainted(true);
			progressBarPanel.add(progressBar, BorderLayout.CENTER);

			// Set initial value for repair progress bar.
			double totalRequiredWork = malfunction.getEmergencyWorkTime() + malfunction.getWorkTime()
					+ malfunction.getEVAWorkTime();
			double totalCompletedWork = malfunction.getCompletedEmergencyWorkTime() +
					malfunction.getCompletedWorkTime() + malfunction.getCompletedEVAWorkTime();
			int percentComplete = 0;
			if (totalRequiredWork > 0D) percentComplete = (int) (100D * (totalCompletedWork / totalRequiredWork));
			progressBarModel.setValue(percentComplete);

			// Prepare parts label.
			partsLabel = new JLabel(getPartsString(malfunction.getRepairParts(), false), JLabel.CENTER);
			partsLabel.setPreferredSize(new Dimension(-1, -1));
			add(partsLabel);

			// Add tooltip.
			setToolTipText(getToolTipString());
		}

		/**
		 * Update the panel.
		 */
		void update() {
			// Update name label.
			if (malfunction.getCompletedEmergencyWorkTime() < malfunction.getEmergencyWorkTime()) {
				malfunctionLabel.setText(malfunction.getName() + " - Emergency");
				malfunctionLabel.setForeground(Color.red);
			}
			else {
				malfunctionLabel.setText(malfunction.getName());
				malfunctionLabel.setForeground(Color.black);
			}

			// Update progress bar.
			double totalRequiredWork = malfunction.getEmergencyWorkTime() + malfunction.getWorkTime()
					+ malfunction.getEVAWorkTime();
			double totalCompletedWork = malfunction.getCompletedEmergencyWorkTime() +
					malfunction.getCompletedWorkTime() + malfunction.getCompletedEVAWorkTime();
			int percentComplete = 0;
			if (totalRequiredWork > 0D) percentComplete = (int) (100D * (totalCompletedWork / totalRequiredWork));
			progressBarModel.setValue(percentComplete);

			// Update parts label.
			partsLabel.setText(getPartsString(malfunction.getRepairParts(), false));

			// Update tool tip.
			setToolTipText(getToolTipString());
		}

		/**
		 * Creates multi-line tool tip text.
		 */
		private String getToolTipString() {
			StringBuilder result = new StringBuilder("<html>");
			result.append(malfunction.getName()).append("<br>");
			result.append("General Repair Time: ").append((int) malfunction.getWorkTime()).append(" millisols<br>");
			result.append("EVA Repair Time: ").append((int) malfunction.getEVAWorkTime()).append(" millisols<br>");
			result.append("Emergency Repair Time: ").append((int) malfunction.getEmergencyWorkTime()).append(" millisols<br>");
			result.append("Repair ").append(getPartsString(malfunction.getRepairParts(), false).toLowerCase());
			result.append("</html>");

			return result.toString();
		}
	}
}