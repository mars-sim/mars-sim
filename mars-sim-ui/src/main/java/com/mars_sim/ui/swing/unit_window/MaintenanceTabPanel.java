/*
 * Mars Simulation Project
 * MaintenanceTabPanel.java
 * @date 2025-08-07
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.resource.MaintenanceScope;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.PartConfig;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.PercentageTableCellRenderer;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The MaintenanceTabPanel is a tab panel for maintenance information.
 */
@SuppressWarnings("serial")
public class MaintenanceTabPanel extends TabPanelTable {
    private static final String SPANNER_ICON = "maintenance";
	private static final String REPAIR_PARTS_NEEDED = "Parts Needed: ";
	private static final String AGO = " ago";

	private static final String[] COLUMN_TOOL_TIPS = {
		    "The Part name", 
		    "The System Function",
		    "The # of Parts",
		    "The Probability that Triggers Maintenance"};
	
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The malfunction manager instance. */
	private MalfunctionManager manager;
	
	private JProgressBar wearCondition;
	private JProgressBar currentInspection;
	
	private JLabel inspectionWinLabel;
	private JLabel lastCompletedLabel;
	private JLabel baseWorkTimeLabel;
	private JLabel partsLabel;
	private JLabel malPLabel;
	private JLabel maintPLabel;
	private JLabel numMaintLabel;
	
	/** The parts table model. */
	private PartTableModel tableModel;

	private static PartConfig partConfig = SimulationConfig.instance().getPartConfiguration();

	/**
	 * Constructor.
	 * 
	 * @param malfunctionable the malfunctionable instance of the unit
	 * @param desktop         The main desktop
	 */
	public MaintenanceTabPanel(Malfunctionable malfunctionable, MainDesktopPane desktop) {
		super(
			Msg.getString("MaintenanceTabPanel.title"), 
			ImageLoader.getIconByName(SPANNER_ICON), 
			Msg.getString("MaintenanceTabPanel.tooltip"),             
			desktop
		);

		// Initialize data members.
		manager = malfunctionable.getMalfunctionManager();

        tableModel = new PartTableModel(manager);

		setHeaderToolTips(COLUMN_TOOL_TIPS);
		setTableTitle(Msg.getString("MaintenanceTabPanel.tableBorder"));
	}

	@Override
	protected TableModel createModel() {
		return tableModel;
	}

	/**
	 * Builds the UI.
	 */
	@Override
	protected JPanel createInfoPanel() {
	
		JPanel topPanel = new JPanel(new BorderLayout());

		AttributePanel labelPanel = new AttributePanel(7, 1);
		topPanel.add(labelPanel, BorderLayout.NORTH);
		
		Dimension barSize = new Dimension(100, 15);

		wearCondition = new JProgressBar();
		wearCondition.setStringPainted(true);
		wearCondition.setToolTipText(Msg.getString("MaintenanceTabPanel.wear.toolTip"));
		wearCondition.setMaximumSize(barSize);
		labelPanel.addLabelledItem(Msg.getString("MaintenanceTabPanel.wearCondition"), wearCondition);

		lastCompletedLabel = labelPanel.addTextField(Msg.getString("MaintenanceTabPanel.lastCompleted"), "", 
												null);
		inspectionWinLabel = labelPanel.addRow(Msg.getString("MaintenanceTabPanel.inspectionWin"), "");
		
		baseWorkTimeLabel = labelPanel.addRow(Msg.getString("MaintenanceTabPanel.baseWorkTime"), "");
		
		currentInspection = new JProgressBar();
		currentInspection.setStringPainted(true);		
		currentInspection.setMaximumSize(barSize);
		currentInspection.setToolTipText(Msg.getString("MaintenanceTabPanel.current.toolTip"));
		labelPanel.addLabelledItem(Msg.getString("MaintenanceTabPanel.currentInspection"), currentInspection);

		numMaintLabel = labelPanel.addRow(Msg.getString("MaintenanceTabPanel.numMaint"), "",
				Msg.getString("MaintenanceTabPanel.numMaint.toolTip"));
		
		partsLabel = labelPanel.addTextField(Msg.getString("MaintenanceTabPanel.partsNeeded"), "", null);
		
		topPanel.add(new JPanel(), BorderLayout.CENTER);
		
		AttributePanel dataPanel = new AttributePanel(2, 1);
		topPanel.add(dataPanel, BorderLayout.SOUTH);
	
		malPLabel = dataPanel.addTextField(
				Msg.getString("MaintenanceTabPanel.malfunctionChance"), 
				"", "The percentage of chance of getting a malfunction");	
		maintPLabel = dataPanel.addTextField(
				Msg.getString("MaintenanceTabPanel.maintenanceChance"), 
				"", "The percentage of chance of requiring a maintenance");
		
		return topPanel;
	}


	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {

        columnModel.getColumn(0).setPreferredWidth(140);
        columnModel.getColumn(1).setPreferredWidth(140);
		columnModel.getColumn(2).setPreferredWidth(25);
		columnModel.getColumn(3).setPreferredWidth(30);	
		
		// Add percentage format
		columnModel.getColumn(3).setCellRenderer(new PercentageTableCellRenderer(false));
	}

	/**
	 * Updates this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
		
		// Update the wear condition label.
		wearCondition.setValue((int) manager.getWearCondition());

		// Update last completed label.
		double timeSinceLastMaint = manager.getTimeSinceLastMaintenance()/1000;
		lastCompletedLabel.setText(StyleManager.DECIMAL1_SOLS.format(timeSinceLastMaint) + AGO);

		// Update inspection window label.
		double window = manager.getStandardInspectionWindow()/1000D;
		inspectionWinLabel.setText(StyleManager.DECIMAL1_SOLS.format(window));

		// Update inspection work time.
		double baseWorkTime = manager.getBaseMaintenanceWorkTime()/1000;
		baseWorkTimeLabel.setText(StyleManager.DECIMAL3_SOLS.format(baseWorkTime));
		
		// Update progress bar.
		double completed = manager.getInspectionWorkTimeCompleted();
		double total = manager.getBaseMaintenanceWorkTime();
		currentInspection.setValue((int)(100.0 * completed / total));

		int newNumMaint = manager.getNumberOfMaintenances();
		numMaintLabel.setText(newNumMaint + "");
		
		// Future: need to compare what parts are missing and what parts are 
		// available for swapping out (just need time)
		Map<Integer, Integer> parts = manager.getMaintenanceParts();
		int size = 0; 
		
		if (parts != null)
			size = parts.size();
		else
			parts = Collections.emptyMap();
		
		// Update parts label.
		partsLabel.setText(Integer.toString(size));
		// Generate tool tip.
		String tooltip = "<html>" + getPartsString(parts, true) + "</html>";
		// Update tool tip.
		partsLabel.setToolTipText(tooltip);
		
		malPLabel.setText(Math.round(manager.getMalfunctionProbability() 
				* 1_000_000_000.0)/1_000_000_000.0 + " % on each check");
		
		maintPLabel.setText(Math.round(manager.getMaintenanceProbability() 
				* 1_000_000_000.0)/1_000_000_000.0 + " % on each check");
	}

	/**
	 * Gets the parts string.
	 * 
	 * @return string.
	 */
	private String getPartsString(Map<Integer, Integer> parts, boolean useHtml) {
		return MalfunctionTabPanel.getPartsString(REPAIR_PARTS_NEEDED, parts, useHtml).toString();
	}

	/**
	 * Internal class used as model for the equipment table.
	 */
	private static class PartTableModel extends AbstractTableModel {
		
		private List<Part> parts = new ArrayList<>();
		private List<String> functions = new ArrayList<>();
		private List<Integer> max = new ArrayList<>();
		private List<Double> probability = new ArrayList<>();

		/**
		 * hidden constructor.
		 */
		private PartTableModel(MalfunctionManager mm) {
            // Find parts for each scope
            for (MaintenanceScope maintenance : partConfig.getMaintenance(mm.getScopes())) {

                parts.add(maintenance.getPart());
                functions.add(Conversion.capitalize(maintenance.getName()));
                max.add(maintenance.getMaxNumber());
                probability.add(maintenance.getProbability());
            }	
		}

		public int getRowCount() {
			return parts.size();
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
            case 0:
				return String.class;
			case 1:
				return String.class;
			case 2:
				return Integer.class;
			case 3:
				return Double.class;
			default:
                return String.class;
            }
		}

		public String getColumnName(int columnIndex) {
            switch(columnIndex) {
			case 0:
				return Msg.getString("MaintenanceTabPanel.header.part"); //$NON-NLS-1$
			case 1:
				return Msg.getString("MaintenanceTabPanel.header.function"); //$NON-NLS-1$
			case 2:
				return Msg.getString("MaintenanceTabPanel.header.max"); //$NON-NLS-1$
			case 3:
				return Msg.getString("MaintenanceTabPanel.header.probability"); //$NON-NLS-1$
			default:
				return "unknown";
            }
		}

		public Object getValueAt(int row, int column) {
			if (row >= 0 && row < parts.size()) {
                switch(column) {
				case 0:
					return parts.get(row).getName();
				case 1:
					return functions.get(row);
				case 2:
					return max.get(row);
				case 3:
					return probability.get(row);
                }
			}
			return "unknown";
		}
	}
}
