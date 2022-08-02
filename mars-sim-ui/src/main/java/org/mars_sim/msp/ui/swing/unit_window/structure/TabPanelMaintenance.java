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
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoundedRangeModel;
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
import org.mars_sim.msp.ui.swing.unit_window.MalfunctionPanel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

@SuppressWarnings("serial")
public class TabPanelMaintenance extends TabPanel {

	private static final String SPANNER_ICON = Msg.getString("icon.spanner"); //$NON-NLS-1$

	/** The Settlement instance. */
	private Settlement settlement;

	private WebPanel maintenanceListPanel;

	private List<Building> buildingsList;

	private static final Font FONT_14 = new Font("Serif", Font.BOLD, 14);
	
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
		WebPanel maintenancePanel = new WebPanel(new BorderLayout());
		content.add(maintenancePanel, BorderLayout.CENTER);

		// Prepare maintenance list panel.
		maintenanceListPanel = new WebPanel(new GridLayout(0, 1, 0, 0));
		maintenanceListPanel.setPadding(5);
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
			WebPanel panel = new BuildingMaintenancePanel(i.next());
			maintenanceListPanel.add(panel);
		}
	}

	/**
	 * Updates the tab panel.
	 */
	@Override
	public void update() {

		// Check if building list has changed.
		List<Building> tempBuildings = settlement.getBuildingManager().getSortedBuildings();
		if (!tempBuildings.equals(buildingsList)) {
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
	private class BuildingMaintenancePanel extends WebPanel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private MalfunctionManager manager;
		private int lastCompletedCache;
		private int wearConditionCache;
		private BoundedRangeModel progressBarModel;
		private WebLabel lastLabel;
		private WebLabel partsLabel;
		private WebLabel wearConditionLabel;

		/**
		 * Constructor.
		 * 
		 * @param building the building to display.
		 */
		public BuildingMaintenancePanel(Building building) {
			// User WebPanel constructor.
			super();

			manager = building.getMalfunctionManager();

			setLayout(new GridLayout(4, 1, 0, 0));
//			setBorder(new MarsPanelBorder());

			WebLabel buildingLabel = new WebLabel(building.getNickName(), SwingConstants.LEFT);
			buildingLabel.setFont(FONT_14);
			add(buildingLabel);

			// Add wear condition cache and label.
			wearConditionCache = (int) Math.round(manager.getWearCondition());
			wearConditionLabel = new WebLabel(
					Msg.getString("BuildingPanelMaintenance.wearCondition", wearConditionCache),
					SwingConstants.RIGHT);
			TooltipManager.setTooltip(wearConditionLabel, 
					Msg.getString("BuildingPanelMaintenance.wear.toolTip"),
					TooltipWay.down);

			WebPanel mainPanel = new WebPanel(new BorderLayout(0, 0));
			add(mainPanel);

			lastCompletedCache = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
			lastLabel = new WebLabel("Last completed : " + lastCompletedCache + " sols ago", SwingConstants.LEFT);
			mainPanel.add(lastLabel, BorderLayout.WEST);;
			TooltipManager.setTooltip(lastLabel, getToolTipString(), TooltipWay.down);

			// Prepare progress bar panel.
			WebPanel progressBarPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
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
			int percentDone = (int) (100D * (completed / total));
			progressBarModel.setValue(percentDone);

			// Prepare parts label.
			Map<Integer, Integer> parts = manager.getMaintenanceParts();
			partsLabel = new WebLabel(MalfunctionPanel.getPartsString(parts, false), SwingConstants.CENTER);
			partsLabel.setPreferredSize(new Dimension(-1, -1));
			add(partsLabel);

			TooltipManager.setTooltip(partsLabel, MalfunctionPanel.getPartsString(parts, false), TooltipWay.down);
		}

		/**
		 * Updates this panel.
		 */
		void update() {
			// Update progress bar.
			double completed = manager.getMaintenanceWorkTimeCompleted();
			double total = manager.getMaintenanceWorkTime();
			int percentDone = (int) (100D * (completed / total));
			progressBarModel.setValue(percentDone);

			// Add wear condition cache and label
			int wearCondition = (int) Math.round(manager.getWearCondition());
			if (wearCondition != wearConditionCache) {
				wearConditionCache = wearCondition;
				wearConditionLabel.setText(Msg.getString("BuildingPanelMaintenance.wearCondition", wearConditionCache));
			}

			// Update last completed.
			int lastCompleted = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
			if (lastCompleted != lastCompletedCache) {
				lastCompletedCache = lastCompleted;
				lastLabel.setText("Last Completed : " + lastCompletedCache + " sols ago");
			}

			Map<Integer, Integer> parts = manager.getMaintenanceParts();

			// Update parts label.
			partsLabel.setText(MalfunctionPanel.getPartsString(parts, false));
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
