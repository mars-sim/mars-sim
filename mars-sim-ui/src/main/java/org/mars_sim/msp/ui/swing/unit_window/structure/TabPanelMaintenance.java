/*
 * Mars Simulation Project
 * TabPanelMaintenance.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.MalfunctionPanel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

@SuppressWarnings("serial")
public class TabPanelMaintenance extends TabPanel {

	private static final String SPANNER_ICON = Msg.getString("icon.spanner"); //$NON-NLS-1$
	private static final String REPAIR_PARTS_NEEDED = "Parts Needed : ";

	/** The Settlement instance. */
	private Settlement settlement;

	private JPanel maintenanceListPanel;

	private List<Building> buildingsList;
	
	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelMaintenance(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(null, ImageLoader.getNewIcon(SPANNER_ICON), "Maintenance", unit, desktop);

		settlement = (Settlement) unit;
	}

	@Override
	protected void buildUI(JPanel content) {
		
		// Create maintenance panel.
		JPanel maintenancePanel = new JPanel(new BorderLayout());
		content.add(maintenancePanel, BorderLayout.CENTER);

		// Prepare maintenance list panel.
		maintenanceListPanel = new JPanel(new GridLayout(0, 1, 0, 0));
		//maintenanceListPanel.setPadding(5);
		maintenancePanel.add(maintenanceListPanel);
	
		populateMaintenanceList();
	}

	/**
	 * Populates the maintenance list.
	 */
	private void populateMaintenanceList() {
		// Clear the list.
		maintenanceListPanel.removeAll();

		// Populate the list.
		buildingsList = settlement.getBuildingManager().getSortedBuildings();
		Iterator<Building> i = buildingsList.iterator();
		while (i.hasNext()) {
			JPanel panel = new BuildingMaintenancePanel(i.next());
			maintenanceListPanel.add(panel);
		}
	}

	/**
	 * Updates the tab panel.
	 */
	@Override
	public void update() {
		// Check if building list has changed.
		if (!settlement.getBuildingManager().getSortedBuildings().equals(buildingsList)) {
			// Populate maintenance list.
			populateMaintenanceList();
		} else {
			// Update all building maintenance panels.
			Component[] components = maintenanceListPanel.getComponents();
			for (Component component : components)
				((BuildingMaintenancePanel) component).update();
		}
	}


	/**
	 * Inner class for the building maintenance panel.
	 */
	private class BuildingMaintenancePanel extends JPanel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private double lastCompletedCache;
		private double wearConditionCache;
		
		private BoundedRangeModel progressBarModel;
		private JLabel lastLabel;
		private JLabel partsLabel;
		private JLabel wearConditionLabel;
		
		private MalfunctionManager manager;

		/**
		 * Constructor.
		 * 
		 * @param building the building to display.
		 */
		public BuildingMaintenancePanel(Building building) {
			// User JPanel constructor.
			super();

			manager = building.getMalfunctionManager();

			setLayout(new GridLayout(4, 1, 0, 0));

			JLabel buildingLabel = new JLabel(building.getNickName(), SwingConstants.LEFT);
			StyleManager.applySubHeading(buildingLabel);
			add(buildingLabel);

			// Add wear condition cache and label.
			wearConditionCache = Math.round(manager.getWearCondition() * 100.0)/100.0;
			wearConditionLabel = new JLabel(
					Msg.getString("BuildingPanelMaintenance.wearCondition", wearConditionCache),
					SwingConstants.RIGHT);
			wearConditionLabel.setToolTipText(
					Msg.getString("BuildingPanelMaintenance.wear.toolTip"));

			JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
			add(mainPanel);

			lastCompletedCache = Math.round(manager.getTimeSinceLastMaintenance() / 1000D * 10.0)/10.0;
			lastLabel = new JLabel("Last completed : " + lastCompletedCache + " sols ago", SwingConstants.LEFT);
			mainPanel.add(lastLabel, BorderLayout.WEST);
			lastLabel.setToolTipText(getToolTipString());

			// Prepare progress bar panel.
			JPanel progressBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			add(progressBarPanel);
			
			mainPanel.add(wearConditionLabel, BorderLayout.CENTER);
			
			// Prepare progress bar.
			JProgressBar progressBar = new JProgressBar();
			progressBarModel = progressBar.getModel();
			progressBar.setStringPainted(true);
			progressBar.setPreferredSize(new Dimension(300, 15));
			progressBarPanel.add(progressBar);

			// Set initial value for progress bar.
			double completed = manager.getMaintenanceWorkTimeCompleted();
			double total = manager.getMaintenanceWorkTime();
			double percentDone = Math.round(100D * completed / total * 10.0)/10.0;
			progressBarModel.setValue((int)percentDone);

			// Prepare parts label.
			Map<Integer, Integer> parts = manager.getMaintenanceParts();
			partsLabel = new JLabel(MalfunctionPanel.getPartsString(REPAIR_PARTS_NEEDED, parts, false), SwingConstants.CENTER);
			partsLabel.setPreferredSize(new Dimension(-1, -1));
			add(partsLabel);

			partsLabel.setToolTipText(MalfunctionPanel.getPartsString(REPAIR_PARTS_NEEDED, parts, false));
		}

		/**
		 * Updates this panel.
		 */
		void update() {
			// Update progress bar.
			double completed = manager.getMaintenanceWorkTimeCompleted();
			double total = manager.getMaintenanceWorkTime();
			double percentDone = Math.round(100D * completed / total * 10.0)/10.0;
			progressBarModel.setValue((int)percentDone);

			// Add wear condition cache and label
			double wearCondition = Math.round(manager.getWearCondition() * 100.0)/100.0;
			if (wearCondition != wearConditionCache) {
				wearConditionCache = wearCondition;
				wearConditionLabel.setText(Msg.getString("BuildingPanelMaintenance.wearCondition", wearConditionCache));
			}

			// Update last completed.
			double lastCompleted = Math.round(manager.getTimeSinceLastMaintenance() / 1000D * 10.0)/10.0;
			if (lastCompleted != lastCompletedCache) {
				lastCompletedCache = lastCompleted;
				lastLabel.setText("Last Completed : " + lastCompletedCache + " sols ago");
			}

			Map<Integer, Integer> parts = manager.getMaintenanceParts();

			// Update parts label.
			partsLabel.setText(MalfunctionPanel.getPartsString(REPAIR_PARTS_NEEDED, parts, false));
		}

		private String getToolTipString() {
			StringBuilder result = new StringBuilder("<html>");
			result.append("The last completed maintenance was done ")
				.append(lastCompletedCache).append(" sols ago.");
			result.append("</html>");
			return result.toString();
		}
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		settlement = null;
		buildingsList = null;
		maintenanceListPanel = null;
	}
}
