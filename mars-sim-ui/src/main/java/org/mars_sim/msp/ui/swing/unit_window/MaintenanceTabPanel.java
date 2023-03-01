/*
 * Mars Simulation Project
 * MaintenanceTabPanel.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.resource.MaintenanceScope;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.PartConfig;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.PercentageCellRenderer;

/**
 * The MaintenanceTabPanel is a tab panel for unit maintenance information.
 */
@SuppressWarnings("serial")
public class MaintenanceTabPanel extends TabPanel {
    private static final String SPANNER_ICON = "maintenance";
	private static final String REPAIR_PARTS_NEEDED = "Parts Needed:";


	/** The malfunction manager instance. */
	private MalfunctionManager manager;
	
	/** The wear condition label. */
	private JLabel wearConditionLabel;
	/** The last completed label. */
	private JLabel lastCompletedLabel;
	/** Label for parts. */
	private JLabel partsLabel;

	/** The progress bar model. */
	private BoundedRangeModel progressBarModel;
	/** The parts table model. */
	private PartTableModel tableModel;

	private static PartConfig partConfig = SimulationConfig.instance().getPartConfiguration();

	/**
	 * Constructor.
	 * 
	 * @param malfunctionable the malfunctionable building the panel is for.
	 * @param desktop         The main desktop.
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
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
	
		JPanel labelPanel = new JPanel(new GridLayout(4, 1, 2, 1));
		center.add(labelPanel, BorderLayout.NORTH);
		
		// Create wear condition label.
		wearConditionLabel = new JLabel(Msg.getString("MaintenanceTabPanel.wearCondition", ""),
				JLabel.CENTER);
		wearConditionLabel.setToolTipText(Msg.getString("MaintenanceTabPanel.wear.toolTip"));
		labelPanel.add(wearConditionLabel);

		// Create lastCompletedLabel.
		lastCompletedLabel = new JLabel(Msg.getString("MaintenanceTabPanel.lastCompleted", ""),
				JLabel.CENTER);
		labelPanel.add(lastCompletedLabel);

		// Create maintenance progress bar panel.
		JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		labelPanel.add(progressPanel);
		progressPanel.setOpaque(false);

		// Prepare progress bar.
		JProgressBar progressBar = new JProgressBar();
		progressBarModel = progressBar.getModel();
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(300, 15));
		progressPanel.add(progressBar);

		// Prepare maintenance parts label.
		partsLabel = new JLabel(getPartsString(false), JLabel.CENTER);
		partsLabel.setPreferredSize(new Dimension(-1, -1));
		labelPanel.add(partsLabel);
		
		// Create the parts panel
		JScrollPane partsPane = new JScrollPane();
		partsPane.setPreferredSize(new Dimension(160, 80));
		center.add(partsPane, BorderLayout.CENTER);
		addBorder(partsPane, Msg.getString("MaintenanceTabPanel.tableBorder"));

		// Create the parts table
		JTable table = new JTable(tableModel);
		table.setRowSelectionAllowed(true);
		partsPane.setViewportView(table);

		TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(160);
        columnModel.getColumn(2).setPreferredWidth(120);
		columnModel.getColumn(2).setPreferredWidth(40);
		columnModel.getColumn(3).setPreferredWidth(40);		
		columnModel.getColumn(3).setCellRenderer(new PercentageCellRenderer());

		// Added sorting
		table.setAutoCreateRowSorter(true);

        // Set=up values
        update();
	}

	/**
	 * Update this panel
	 */
	@Override
	public void update() {

		// Update the wear condition label.
		wearConditionLabel.setText(Msg.getString("MaintenanceTabPanel.wearCondition",
                                    StyleManager.DECIMAL_PLACES0.format(manager.getWearCondition())));

		// Update last completed label.
		lastCompletedLabel.setText(Msg.getString("MaintenanceTabPanel.lastCompleted",
                                    StyleManager.DECIMAL_PLACES1.format(manager.getTimeSinceLastMaintenance()/1000D)));

		// Update progress bar.
		double completed = manager.getMaintenanceWorkTimeCompleted();
		double total = manager.getMaintenanceWorkTime();
		double percentDone = Math.round(100.0 * completed / total * 100.0)/100.0;
		progressBarModel.setValue((int)percentDone);

		// Update parts label.
		partsLabel.setText(getPartsString(false));
		// Update tool tip.
		partsLabel.setToolTipText("<html>" + getPartsString(true) + "</html>");
	}

	/**
	 * Gets the parts string.
	 * 
	 * @return string.
	 */
	private String getPartsString(boolean useHtml) {
		return MalfunctionTabPanel.getPartsString(REPAIR_PARTS_NEEDED, manager.getMaintenanceParts(), useHtml).toString();
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
                functions.add(maintenance.getName());
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
