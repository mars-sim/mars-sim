/*
 * Mars Simulation Project
 * MiningMissionCustomInfoPanel.java
 * @date 2022-10-01
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;

/**
 * A panel for displaying mining mission information.
 */
@SuppressWarnings("serial")
public class MiningMissionCustomInfoPanel extends MissionCustomInfoPanel {

	// Data members
	private Mining mission;
	private MainDesktopPane desktop;
	private JButton luvButton;
	private ConcentrationTableModel concentrationTableModel;
	private ExcavationTableModel excavationTableModel;

	private SurfaceFeatures surfaceFeatures;
	
	/**
	 * Constructor
	 * 
	 * @param desktop the main desktop.
	 */
	MiningMissionCustomInfoPanel(MainDesktopPane desktop) {
		// Use JPanel constructor
		super();

		// Set the layout.
		setLayout(new BorderLayout());

		// Initialize data members.
		this.desktop = desktop;
		surfaceFeatures = desktop.getSimulation().getSurfaceFeatures();

		// Create LUV panel.
		JPanel luvPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(luvPane, BorderLayout.NORTH);

		// Create LUV label.
		JLabel luvLabel = new JLabel("Light Utility Vehicle: ");
		luvPane.add(luvLabel);

		// Create LUV button.
		luvButton = new JButton("   ");
		luvButton.setVisible(false);
		luvButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Open window for light utility vehicle.
				LightUtilityVehicle luv = mission.getLightUtilityVehicle();
				if (luv != null)
					getDesktop().openUnitWindow(luv, false);
			}
		});
		luvPane.add(luvButton);

		// Create center panel.
		JPanel centerPane = new JPanel(new GridLayout(2, 1));
		add(centerPane, BorderLayout.CENTER);

		// Create concentration panel.
		JPanel concentrationPane = new JPanel(new BorderLayout());
		concentrationPane.setBorder(BorderFactory.createTitledBorder("Mineral Concentrations  "));
		centerPane.add(concentrationPane);

		// Create concentration scroll panel.
		JScrollPane concentrationScrollPane = new JScrollPane();
		concentrationScrollPane.setPreferredSize(new Dimension(-1, -1));
		concentrationPane.add(concentrationScrollPane, BorderLayout.CENTER);

		// Create concentration table.
		concentrationTableModel = new ConcentrationTableModel();
		JTable concentrationTable = new JTable(concentrationTableModel);
		concentrationTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2));
		concentrationScrollPane.setViewportView(concentrationTable);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		concentrationTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		concentrationTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		
		// Create excavation panel.
		JPanel excavationPane = new JPanel(new BorderLayout());
		excavationPane.setBorder(BorderFactory.createTitledBorder("Minerals Excavated - Aboard Vehicle  "));
		centerPane.add(excavationPane);

		// Create excavation scroll panel.
		JScrollPane excavationScrollPane = new JScrollPane();
		excavationScrollPane.setPreferredSize(new Dimension(-1, -1));
		excavationPane.add(excavationScrollPane, BorderLayout.CENTER);

		// Create excavation table.
		excavationTableModel = new ExcavationTableModel();
		JTable excavationTable = new JTable(excavationTableModel);
		excavationTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2));
		excavationScrollPane.setViewportView(excavationTable);
		
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		excavationTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
	}

	/**
	 * Gets the main desktop.
	 * 
	 * @return desktop.
	 */
	private MainDesktopPane getDesktop() {
		return desktop;
	}

	@Override
	public void updateMission(Mission mission) {
		if (mission instanceof Mining) {
			this.mission = (Mining) mission;
			updateLUVButton();
			concentrationTableModel.updateTable();
			excavationTableModel.updateTable();
		}
	}

	@Override
	public void updateMissionEvent(MissionEvent e) {
		if (e.getType() == MissionEventType.EXCAVATE_MINERALS_EVENT
				|| e.getType() == MissionEventType.COLLECT_MINERALS_EVENT)
			excavationTableModel.updateTable();
	}

	/**
	 * Updates the LUV button.
	 */
	private void updateLUVButton() {
		LightUtilityVehicle luv = mission.getLightUtilityVehicle();
		luvButton.setText(luv.getName());
		luvButton.setVisible(true);
	}

	/**
	 * Concentration table model.
	 */
	private class ConcentrationTableModel extends AbstractTableModel {

		// Data members.
		protected Map<String, Double> estimatedConcentrationMap;
		protected Map<String, Double> actualConcentrationMap;

		/**
		 * Constructor
		 */
		private ConcentrationTableModel() {
			// Use AbstractTableModel constructor.
			super();

			// Initialize concentration maps.
			estimatedConcentrationMap = new HashMap<>();
			actualConcentrationMap = new HashMap<>();
		}

		/**
		 * Returns the number of rows in the model.
		 * 
		 * @return number of rows.
		 */
		public int getRowCount() {
			return estimatedConcentrationMap.size();
		}

		/**
		 * Returns the number of columns in the model.
		 * 
		 * @return number of columns.
		 */
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return "Mineral";
			else if (columnIndex == 1)
				return "Estimated %";
			else
				return "Actual %";
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0)
				dataType = String.class;
			else if (columnIndex == 1)
				dataType = Double.class;
			else if (columnIndex == 2)
				dataType = Double.class;
			return dataType;
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param row    the row whose value is to be queried.
		 * @param column the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		public Object getValueAt(int row, int column) {
			Object result = "unknown";

			String[] minerals = estimatedConcentrationMap.keySet()
					.toArray(new String[estimatedConcentrationMap.size()]);
			if ((row >= 0) && (row < minerals.length)) {
				if (column == 0) {
					result = Conversion.capitalize(minerals[row]);
				} else if (column == 1) {
					result = Math.round(estimatedConcentrationMap.get(minerals[row]) * 100.0)/100.0;
				} else if (column == 2) {
					if (actualConcentrationMap.containsKey(minerals[row])) {
						result = Math.round(actualConcentrationMap.get(minerals[row]) * 100.0)/100.0;
					} else {
						result = Double.valueOf(0D);
					}
				}
			}

			return result;
		}

		/**
		 * Updates the table data.
		 */
		private void updateTable() {
			if (mission.getMiningSite() != null) {
				estimatedConcentrationMap = mission.getMiningSite().getEstimatedMineralConcentrations();
				actualConcentrationMap = surfaceFeatures.getMineralMap()
						.getAllMineralConcentrations(mission.getMiningSite().getLocation());
			} else {
				estimatedConcentrationMap.clear();
				actualConcentrationMap.clear();
			}
			fireTableDataChanged();
		}
	}

	/**
	 * Excavation table model.
	 */
	private class ExcavationTableModel extends AbstractTableModel {

		// Data members.
		protected Map<AmountResource, Double> excavationMap;

		/**
		 * Constructor
		 */
		private ExcavationTableModel() {
			// Use AbstractTableModel constructor.
			super();

			// Initialize excavation map.
			excavationMap = new HashMap<>();
		}

		/**
		 * Returns the number of rows in the model.
		 * 
		 * @return number of rows.
		 */
		public int getRowCount() {
			return excavationMap.size();
		}

		/**
		 * Returns the number of columns in the model.
		 * 
		 * @return number of columns.
		 */
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return "Mineral";
			else
				return "Excavated [kg]";
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0)
				dataType = String.class;
			else if (columnIndex == 1)
				dataType = Double.class;
			return dataType;
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param row    the row whose value is to be queried.
		 * @param column the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		public Object getValueAt(int row, int column) {
			Object result = "unknown";

			AmountResource[] minerals = excavationMap.keySet().toArray(new AmountResource[excavationMap.size()]);
			if ((row >= 0) && (row < minerals.length)) {
				if (column == 0)
					result = Conversion.capitalize(minerals[row].getName());
				else
					result = Math.round(excavationMap.get(minerals[row]) * 100.0)/100.0;
			}

			return result;
		}

		/**
		 * Updates the table data.
		 */
		private void updateTable() {
			excavationMap.clear();
			String[] mineralNames = surfaceFeatures.getMineralMap()
					.getMineralTypeNames();
			for (String mineralName : mineralNames) {
				AmountResource mineral = ResourceUtil.findAmountResource(mineralName);
				double amount = mission.getTotalMineralExcavatedAmount(mineral);
				if (amount > 0D)
					excavationMap.put(mineral, amount);
			}

			fireTableDataChanged();
		}
	}
}
