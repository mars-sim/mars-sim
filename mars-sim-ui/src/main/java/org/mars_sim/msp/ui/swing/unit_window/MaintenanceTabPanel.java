/*
 * Mars Simulation Project
 * MaintenanceTabPanel.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

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
import org.mars_sim.msp.ui.swing.utils.AttributePanel;
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
	
	private JProgressBar wearCondition;
	private JLabel lastCompletedLabel;
	private JLabel partsLabel;
	private JProgressBar currentMaintenance;

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
	
		AttributePanel labelPanel = new AttributePanel(4, 1);
		center.add(labelPanel, BorderLayout.NORTH);
		
		Dimension barSize = new Dimension(100, 15);

		wearCondition = new JProgressBar();
		wearCondition.setStringPainted(true);
		wearCondition.setToolTipText(Msg.getString("MaintenanceTabPanel.wear.toolTip"));
		wearCondition.setMaximumSize(barSize);
		labelPanel.addLabelledItem(Msg.getString("MaintenanceTabPanel.wearCondition"), wearCondition);

		lastCompletedLabel = labelPanel.addTextField(Msg.getString("MaintenanceTabPanel.lastCompleted"), "", 
												null);
		currentMaintenance = new JProgressBar();
		currentMaintenance.setStringPainted(true);		
		currentMaintenance.setMaximumSize(barSize);
		currentMaintenance.setToolTipText(Msg.getString("MaintenanceTabPanel.current.toolTip"));
		labelPanel.addLabelledItem(Msg.getString("MaintenanceTabPanel.currentMaintenance"), currentMaintenance);

		partsLabel = labelPanel.addTextField(Msg.getString("MaintenanceTabPanel.partsNeeded"), "", 
						   null);


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
		columnModel.getColumn(3).setCellRenderer(new PercentageCellRenderer(false));

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
		wearCondition.setValue((int) manager.getWearCondition());

		// Update last completed label.
		StringBuilder text = new StringBuilder();
		text.append(StyleManager.DECIMAL_SOLS.format(manager.getTimeSinceLastMaintenance()/1000D));
		text.append(" (cycle ");
		text.append(StyleManager.DECIMAL_SOLS.format(manager.getMaintenancePeriod()/1000D));
		text.append(")");

		lastCompletedLabel.setText(text.toString());

		// Update progress bar.
		double completed = manager.getMaintenanceWorkTimeCompleted();
		double total = manager.getMaintenanceWorkTime();
		currentMaintenance.setValue((int)(100.0 * completed / total));

		// Update parts label.
		partsLabel.setText(Integer.toString(manager.getMaintenanceParts().size()));

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
