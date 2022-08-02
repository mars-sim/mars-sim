/*
 * Mars Simulation Project
 * TabPanelMaintenance.java
 * @date 2022-08-01
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
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.apache.commons.collections.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.MalfunctionPanel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
//import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

@SuppressWarnings("serial")
public class TabPanelMaintenance extends TabPanel {

	private static final String SPANNER_ICON = Msg.getString("icon.spanner"); //$NON-NLS-1$
	private static final String REPAIR_WORK_REQUIRED = "Repair Work Required :";

	/** The Settlement instance. */
	private Settlement settlement;
	
	private WebScrollPane maintenanceScrollPane;
	private WebScrollPane malfunctionsScrollPane;
	
	private WebPanel malfunctionsListPanel;
	private WebPanel maintenanceListPanel;

	private List<Building> buildingsList;
	private List<Malfunction> malfunctionsList;
	
	private static final Font FONT_16 = new Font("Serif", Font.BOLD, 16);
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
		malfunctionsList = new ArrayList<>();
	}

	@Override
	protected void buildUI(JPanel content) {
		
		// Create topPanel.
		WebPanel topPanel = new WebPanel(new GridLayout(2, 1));
		content.add(topPanel, BorderLayout.CENTER);

		// Create maintenance panel.
		WebPanel maintenancePanel = new WebPanel(new BorderLayout());
		topPanel.add(maintenancePanel);

		// Create scroll pane for maintenance list panel.
		maintenanceScrollPane = new WebScrollPane();
		// increase vertical mousewheel scrolling speed for this one
		maintenanceScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		maintenanceScrollPane.setPreferredSize(new Dimension(200, 75));
		maintenancePanel.add(maintenanceScrollPane, BorderLayout.CENTER);

		// Prepare maintenance list panel.
		maintenanceListPanel = new WebPanel(new GridLayout(0, 1, 0, 0));
		maintenanceListPanel.setPadding(5);
//		maintenanceListPanel.setBorder(new MarsPanelBorder());
		maintenanceScrollPane.setViewportView(maintenanceListPanel);
		populateMaintenanceList();

		// Create malfunctions panel.
		WebPanel malfunctionsPanel = new WebPanel(new BorderLayout());
		topPanel.add(malfunctionsPanel);

		// Create malfunctions label.
		WebLabel malfunctionsLabel = new WebLabel("Building Malfunctions", SwingConstants.CENTER);
		malfunctionsLabel.setFont(FONT_16);
		// malfunctionsLabel.setForeground(new Color(102, 51, 0)); // dark brown
		malfunctionsPanel.add(malfunctionsLabel, BorderLayout.NORTH);

		// Create scroll panel for malfunctions list panel.
		malfunctionsScrollPane = new WebScrollPane();
		// increase vertical mousewheel scrolling speed for this one
		malfunctionsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		malfunctionsScrollPane.setPreferredSize(new Dimension(200, 75));
		malfunctionsPanel.add(malfunctionsScrollPane, BorderLayout.CENTER);

		// Prepare malfunctions outer list panel.
		WebPanel malfunctionsOuterListPanel = new WebPanel(new BorderLayout(0, 0));
//		malfunctionsOuterListPanel.setBorder(new MarsPanelBorder());
		malfunctionsScrollPane.setViewportView(malfunctionsOuterListPanel);

		// Prepare malfunctions list panel.
		malfunctionsListPanel = new WebPanel();
		malfunctionsListPanel.setPadding(5);
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
		buildingsList = settlement.getBuildingManager().getSortedBuildings();// getACopyOfBuildings()
		Iterator<Building> i = buildingsList.iterator();
		while (i.hasNext()) {
			WebPanel panel = new BuildingMaintenancePanel(i.next());
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
		malfunctionsList.clear();
		Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();// getACopyOfBuildings().iterator();.getACopyOfBuildings().iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Iterator<Malfunction> j = building.getMalfunctionManager().getMalfunctions().iterator();
			while (j.hasNext()) {
				Malfunction malfunction = j.next();
				if (!malfunctionsList.contains(malfunction)) {
					malfunctionsList.add(malfunction);
					WebPanel panel = new BuildingMalfunctionPanel(malfunction, building);
					malfunctionsListPanel.add(panel);
				}
			}
		}
	}

	/**
	 * Update the tab panel.
	 */
	@Override
	public void update() {

		// Check if building list has changed.
		List<Building> tempBuildings = ((Settlement) getUnit()).getBuildingManager().getSortedBuildings();
		if (!tempBuildings.equals(buildingsList)) {
			// Populate maintenance list.
			populateMaintenanceList();
			maintenanceScrollPane.validate();
		} else {
			// Update all building maintenance panels.
			Component[] components = maintenanceListPanel.getComponents();
			for (Component component : components)
				((BuildingMaintenancePanel) component).update();
		}
		
		// Create temporary malfunctions list.
		List<Malfunction> tempMalfunctions = new ArrayList<Malfunction>();
		Iterator<Building> i = tempBuildings.iterator();
		while (i.hasNext()) {
			Iterator<Malfunction> j = i.next().getMalfunctionManager().getMalfunctions().iterator();
			while (j.hasNext()) {
				tempMalfunctions.add(j.next());
			}
		}

		// Check if malfunctions list has changed.
		if (!CollectionUtils.isEqualCollection(malfunctionsList, tempMalfunctions)) {		
			// Populate malfunctions list.
			populateMalfunctionsList();
//			malfunctionsListPanel.validate();
			malfunctionsScrollPane.validate();
		} 
		
		else {
			// Update all building malfunction panels.
			Component[] components = malfunctionsListPanel.getComponents();
			for (Component component : components) {
				((BuildingMalfunctionPanel) component).updateMalfunctionPanel();
//					BuildingMalfunctionPanel panel = ((BuildingMalfunctionPanel) component);
//					if (panel.isChanged)
//						panel.update();
			}
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
			progressBar.setPreferredSize(new Dimension(240, 15));
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
				lastLabel.setText("Last completed : " + lastCompletedCache + " sols ago");
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
	 * Inner class for building malfunction panel.
	 */
	private class BuildingMalfunctionPanel extends WebPanel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		public int progressCache = 0;
		public String partsCache = "";
		public String workCache = "";
		
		/** The malfunction label. */
		private WebLabel malfunctionLabel;
		/** The work label. */
		private WebLabel workLabel;
		/** The repair parts label. */
		private WebLabel partsLabel;
		
		/** The repair bar model. */
		private BoundedRangeModel repairBarModel;
		
		/** The malfunction. */
		private Malfunction malfunction;

		/**
		 * Constructor.
		 * 
		 * @param malfunction the malfunction for the panel.
		 * @param building    the building the malfunction is in.
		 */
		public BuildingMalfunctionPanel(Malfunction malfunction, Building building) {
			// Use WebPanel constructor
			super();

			// Initialize data members
			this.malfunction = malfunction;

			// Set layout and border.
			setLayout(new GridLayout(5, 1, 0, 0));
			setBorder(new MarsPanelBorder());
			setOpaque(false);
			setBackground(new Color(0,0,0,128));
			
			// Prepare the building label.
			WebLabel buildingLabel = new WebLabel(building.getNickName(), SwingConstants.LEFT);
			buildingLabel.setFont(FONT_14);
			add(buildingLabel);

			// Prepare the malfunction label.
			malfunctionLabel = new WebLabel(malfunction.getName(), SwingConstants.CENTER);
			malfunctionLabel.setForeground(Color.red);
			add(malfunctionLabel);

			workLabel = new WebLabel("", SwingConstants.CENTER);
			workLabel.setForeground(Color.blue);
			add(workLabel);
			
			// Progress bar panel.
			WebPanel repairPane = new WebPanel(new BorderLayout(0, 0));
			add(repairPane, BorderLayout.CENTER);

			// Prepare progress bar.
			JProgressBar repairBar = new JProgressBar();
			repairBarModel = repairBar.getModel();
			repairBar.setStringPainted(true);
			repairPane.add(repairBar, BorderLayout.CENTER);

			// Set initial value for repair progress bar.
			repairBarModel.setValue((int)malfunction.getPercentageFixed());

			// Prepare parts label.
			partsLabel = new WebLabel(MalfunctionPanel.getPartsString(malfunction.getRepairParts(), false), SwingConstants.CENTER);
			partsLabel.setPreferredSize(new Dimension(-1, -1));
			add(partsLabel);

			// Add tooltip.
			TooltipManager.setTooltip(this, MalfunctionPanel.getToolTipString(malfunction), TooltipWay.right);
			
			updateMalfunctionPanel();
		}

		/**
		 * Updates the malfunction panel.
		 */
		void updateMalfunctionPanel() {

			String work = REPAIR_WORK_REQUIRED;
			String text = "";
			
			for (MalfunctionRepairWork workType : MalfunctionRepairWork.values()) {
				if (malfunction.getWorkTime(workType) > 0) {
					text += "  [" + workType + "]";
				}
			}
			
			if (!workCache.equalsIgnoreCase(text)) {
				workCache = text;
				workLabel.setText(work + text);
			}
			
			// Update progress bar.
			int percentComplete = (int)malfunction.getPercentageFixed();
			if (progressCache != percentComplete) {
				progressCache = percentComplete;
				repairBarModel.setValue(percentComplete);
			}
			
			// Update parts label.
			String parts = MalfunctionPanel.getPartsString(malfunction.getRepairParts(), false);
			if (partsCache.equalsIgnoreCase(parts)) {
				partsCache = parts;
				partsLabel.setText(parts);
			}
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		settlement = null;
		buildingsList = null;
		maintenanceScrollPane = null;
		maintenanceListPanel = null;
		malfunctionsList = null;
		malfunctionsScrollPane = null;
		malfunctionsListPanel = null;
	}

}
