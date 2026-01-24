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

import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.resource.MaintenanceScope;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.components.JIntegerLabel;
import com.mars_sim.ui.swing.components.PercentageTableCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The MaintenanceTabPanel is a tab panel for maintenance information.
 */
@SuppressWarnings("serial")
public class MaintenanceTabPanel extends EntityTableTabPanel<Malfunctionable> implements TemporalComponent {
    private static final String SPANNER_ICON = "maintenance";
	private static final String REPAIR_PARTS_NEEDED = "Parts Needed: ";
	
	/** The malfunction manager instance. */
	private MalfunctionManager manager;
	
	private JProgressBar wearCondition;
	private JProgressBar currentInspection;
	
	private JDoubleLabel inspectionWinLabel;
	private JDoubleLabel lastCompletedLabel;
	private JDoubleLabel baseWorkTimeLabel;
	private JLabel partsLabel;
	private JLabel malPLabel;
	private JLabel maintPLabel;
	private JIntegerLabel numMaintLabel;
	
	/** The parts table model. */
	private PartTableModel tableModel;

	/**
	 * Constructor.
	 * 
	 * @param malfunctionable the malfunctionable instance of the unit
	 * @param context         The UI context
	 */
	public MaintenanceTabPanel(Malfunctionable malfunctionable, UIContext context) {
		super(
			Msg.getString("MaintenanceTabPanel.title"), 
			ImageLoader.getIconByName(SPANNER_ICON), 
			Msg.getString("MaintenanceTabPanel.tooltip"),   
			malfunctionable, context
		);

		// Initialize data members.
		manager = malfunctionable.getMalfunctionManager();

        tableModel = new PartTableModel(manager);

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

		AttributePanel labelPanel = new AttributePanel();
		topPanel.add(labelPanel, BorderLayout.NORTH);
		
		Dimension barSize = new Dimension(100, 15);

		wearCondition = new JProgressBar();
		wearCondition.setStringPainted(true);
		wearCondition.setToolTipText(Msg.getString("MaintenanceTabPanel.wear.toolTip"));
		wearCondition.setMaximumSize(barSize);
		labelPanel.addLabelledItem(Msg.getString("MaintenanceTabPanel.wearCondition"), wearCondition);

		lastCompletedLabel = new JDoubleLabel(StyleManager.DECIMAL2_SOLS);
		labelPanel.addLabelledItem(Msg.getString("MaintenanceTabPanel.lastCompleted"), lastCompletedLabel);
		inspectionWinLabel = new JDoubleLabel(StyleManager.DECIMAL2_SOLS);
		labelPanel.addLabelledItem(Msg.getString("MaintenanceTabPanel.inspectionWin"), inspectionWinLabel);
		baseWorkTimeLabel = new JDoubleLabel(StyleManager.DECIMAL3_SOLS);
		labelPanel.addLabelledItem(Msg.getString("MaintenanceTabPanel.baseWorkTime"), baseWorkTimeLabel);
		
		currentInspection = new JProgressBar();
		currentInspection.setStringPainted(true);		
		currentInspection.setMaximumSize(barSize);
		currentInspection.setToolTipText(Msg.getString("MaintenanceTabPanel.current.toolTip"));
		labelPanel.addLabelledItem(Msg.getString("MaintenanceTabPanel.currentInspection"), currentInspection);

		numMaintLabel = new JIntegerLabel();
		labelPanel.addLabelledItem(Msg.getString("MaintenanceTabPanel.numMaint"), numMaintLabel,
				Msg.getString("MaintenanceTabPanel.numMaint.toolTip"));
		
		partsLabel = labelPanel.addTextField(Msg.getString("MaintenanceTabPanel.partsNeeded"), "", null);
		
		topPanel.add(new JPanel(), BorderLayout.CENTER);
		
		AttributePanel dataPanel = new AttributePanel();
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

        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(100);
		columnModel.getColumn(2).setPreferredWidth(25);
		columnModel.getColumn(3).setPreferredWidth(40);	
		columnModel.getColumn(4).setPreferredWidth(40);	
		columnModel.getColumn(5).setPreferredWidth(35);	
		// Add percentage format
		columnModel.getColumn(5).setCellRenderer(new PercentageTableCellRenderer(false));
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {

		// Update the wear condition label.
		wearCondition.setValue((int) manager.getWearCondition());

		// Update last completed label.
		lastCompletedLabel.setValue(manager.getEffectiveTimeSinceLastMaintenance()/1000);
		inspectionWinLabel.setValue(manager.getStandardInspectionWindow()/1000D);
		baseWorkTimeLabel.setValue(manager.getBaseMaintenanceWorkTime()/1000);
		
		// Update progress bar.
		double completed = manager.getInspectionWorkTimeCompleted();
		double total = manager.getBaseMaintenanceWorkTime();
		currentInspection.setValue((int)(100.0 * completed / total));

		int newNumMaint = manager.getNumberOfMaintenances();
		numMaintLabel.setValue(newNumMaint);
		
		// Future: need to compare what parts are missing and what parts are 
		// available for swapping out (just need time)
		Map<MaintenanceScope, Integer> parts = manager.getMaintenanceParts();
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
	private String getPartsString(Map<MaintenanceScope, Integer> parts, boolean useHtml) {
		return MalfunctionTabPanel.getPartsString(REPAIR_PARTS_NEEDED, parts, useHtml).toString();
	}

	/**
	 * Internal class used as the table model for the parts.
	 */
	private static class PartTableModel extends AbstractTableModel {
		
		private List<Part> parts = new ArrayList<>();
		private List<String> systems = new ArrayList<>();
		private List<Integer> max = new ArrayList<>();
		private List<Double> fatigue = new ArrayList<>();
		private List<Double> failure = new ArrayList<>();
		private List<Double> probability = new ArrayList<>();

		/**
		 * hidden constructor.
		 */
		private PartTableModel(MalfunctionManager mm) {
            // Find parts for each scope
            for (MaintenanceScope maintenance : mm.getMaintenanceScopeCollection()) {

                parts.add(maintenance.getPart());
                systems.add(Conversion.capitalize(maintenance.getScope()));          
                max.add(maintenance.getMaxNumber());
                failure.add(maintenance.getPart().getFailureRate());
                fatigue.add(maintenance.getFatigue());
                probability.add(maintenance.getProbability());
            }	
		}

		public int getRowCount() {
			return parts.size();
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
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
			case 4:
				return Double.class;
			case 5:
				return Double.class;
			default:
                return String.class;
            }
		}

		@Override
		public String getColumnName(int columnIndex) {
            switch(columnIndex) {
			case 0:
				return Msg.getString("MaintenanceTabPanel.header.part"); //$NON-NLS-1$
			case 1:
				return Msg.getString("MaintenanceTabPanel.header.system"); //$NON-NLS-1$
			case 2:
				return Msg.getString("MaintenanceTabPanel.header.num"); //$NON-NLS-1$
			case 3:
				return Msg.getString("MaintenanceTabPanel.header.fatigue"); //$NON-NLS-1$
			case 4:
				return Msg.getString("MaintenanceTabPanel.header.failure"); //$NON-NLS-1$
			case 5:
				return Msg.getString("MaintenanceTabPanel.header.probability"); //$NON-NLS-1$
			default:
				return "";
            }
		}

		public Object getValueAt(int row, int column) {
			if (row >= 0 && row < parts.size()) {
                switch(column) {
				case 0:
					return parts.get(row).getName();
				case 1:
					return systems.get(row);
				case 2:
					return max.get(row);
				case 3:
					return fatigue.get(row);
				case 4:
					return failure.get(row);
				case 5:
					return probability.get(row);
				default:
					return "unknown";
                }
			}
			return "unknown";
		}
	}
}
