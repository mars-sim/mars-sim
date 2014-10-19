/**
 * Mars Simulation Project
 * TabPanelThermalSystem.java
 * @version 3.07 2014-10-17
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.ThermalSystem;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
import org.mars_sim.msp.core.structure.building.function.HeatMode;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/** 
 * This is a tab panel for settlement's Thermal System information.
 */
public class TabPanelThermalSystem
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// default logger.
	private static Logger logger = Logger.getLogger(TabPanelThermalSystem.class.getName());

	// Data Members
	/** The total heat generated label. */
	private JLabel heatGeneratedLabel;
	/** The total heat used label. */
	private JLabel heatUsedLabel;
	/** The total heat storage capacity label. */
	private JLabel heatStorageCapacityLabel;
	/** The total heat stored label. */
	private JLabel heatStoredLabel;
	/** Table model for heat info. */
	private HeatTableModel heatTableModel;
	/** The settlement's Heating System */
	private ThermalSystem thermalSystem;

	// Data cache
	/** The total heat generated cache. */
	private double heatGeneratedCache;
	/** The total heat used cache. */
	private double heatUsedCache;
	/** The total heat storage capacity cache. */
	private double heatStorageCapacityCache;
	/** The total heat stored cache. */
	private double heatStoredCache;

	private DecimalFormat formatter = new DecimalFormat(Msg.getString("TabPanelThermalSystem.decimalFormat")); //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelThermalSystem(Unit unit, MainDesktopPane desktop) { 

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelThermalSystem.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelThermalSystem.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Settlement settlement = (Settlement) unit;
		thermalSystem = settlement.getThermalSystem();

		// Prepare heating System label panel.
		JPanel thermalSystemLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(thermalSystemLabelPanel);

		// Prepare heating System label.
		JLabel thermalSystemLabel = new JLabel(Msg.getString("TabPanelThermalSystem.label"), JLabel.CENTER); //$NON-NLS-1$
		thermalSystemLabelPanel.add(thermalSystemLabel);

		// Prepare heat info panel.
		JPanel heatInfoPanel = new JPanel(new GridLayout(4, 1, 0, 0));
		heatInfoPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(heatInfoPanel);

		// Prepare heat generated label.
		heatGeneratedCache = thermalSystem.getGeneratedHeat();
		heatGeneratedLabel = new JLabel(Msg.getString("TabPanelThermalSystem.totalHeatGenerated", formatter.format(heatGeneratedCache)), JLabel.CENTER); //$NON-NLS-1$
		heatInfoPanel.add(heatGeneratedLabel);

		// Prepare heat used label.
		heatUsedCache = thermalSystem.getRequiredHeat();
		heatUsedLabel = new JLabel(Msg.getString("TabPanelThermalSystem.totalHeatUsed", formatter.format(heatUsedCache)), JLabel.CENTER); //$NON-NLS-1$
		heatInfoPanel.add(heatUsedLabel);

		// Prepare heat storage capacity label.
		heatStorageCapacityCache = thermalSystem.getStoredHeatCapacity();
		heatStorageCapacityLabel = new JLabel(Msg.getString("TabPanelThermalSystem.heatStorageCapacity", formatter.format(heatStorageCapacityCache)), JLabel.CENTER); //$NON-NLS-1$
		heatInfoPanel.add(heatStorageCapacityLabel);

		// Prepare heat stored label.
		heatStoredCache = thermalSystem.getStoredHeat();
		heatStoredLabel = new JLabel(Msg.getString("TabPanelThermalSystem.totalHeatStored", formatter.format(heatStoredCache)), JLabel.CENTER); //$NON-NLS-1$
		heatInfoPanel.add(heatStoredLabel);

		// Create scroll panel for the outer table panel.
		JScrollPane heatScrollPane = new JScrollPane();
		heatScrollPane.setPreferredSize(new Dimension(257, 230));
		// increase vertical mousewheel scrolling speed for this one
		heatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		centerContentPanel.add(heatScrollPane,BorderLayout.CENTER);

		// Prepare outer table panel.
		JPanel outerTablePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		outerTablePanel.setBorder(new MarsPanelBorder());
		heatScrollPane.setViewportView(outerTablePanel);

		// Prepare heat table panel.
		JPanel heatTablePanel = new JPanel(new BorderLayout(0, 0));
		outerTablePanel.add(heatTablePanel);
		// heatScrollPanel.setViewportView(heatTablePanel);

		// Prepare heat table model.
		heatTableModel = new HeatTableModel(settlement);

		// Prepare heat table.
		JTable heatTable = new JTable(heatTableModel);
		heatTable.setCellSelectionEnabled(false);
		heatTable.setDefaultRenderer(Double.class, new NumberCellRenderer());
		heatTable.getColumnModel().getColumn(0).setPreferredWidth(18);
		heatTable.getColumnModel().getColumn(1).setPreferredWidth(90);
		heatTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		heatTable.getColumnModel().getColumn(3).setPreferredWidth(50);
		heatTable.getColumnModel().getColumn(4).setPreferredWidth(50);
		heatTablePanel.add(heatTable.getTableHeader(), BorderLayout.NORTH);
		heatTablePanel.add(heatTable, BorderLayout.CENTER);
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {

		// Update heat generated label.
		if (heatGeneratedCache != thermalSystem.getGeneratedHeat()) {
			heatGeneratedCache = thermalSystem.getGeneratedHeat();
			heatGeneratedLabel.setText(
				Msg.getString(
					"TabPanelThermalSystem.totalHeatGenerated", //$NON-NLS-1$
					formatter.format(heatGeneratedCache)
				)
			);
		}

		// Update heat used label.
		if (heatUsedCache != thermalSystem.getRequiredHeat()) {
			heatUsedCache = thermalSystem.getRequiredHeat();
			heatUsedLabel.setText(Msg.getString("TabPanelThermalSystem.totalHeatUsed",formatter.format(heatUsedCache))); //$NON-NLS-1$
		}

		// Update heat storage capacity label.
		if (heatStorageCapacityCache != thermalSystem.getStoredHeatCapacity()) {
			heatStorageCapacityCache = thermalSystem.getStoredHeatCapacity();
			heatStorageCapacityLabel.setText(Msg.getString(
				"TabPanelThermalSystem.heatStorageCapacity", //$NON-NLS-1$
				formatter.format(heatStorageCapacityCache)
			));
		}

		// Update heat stored label.
		if (heatStoredCache != thermalSystem.getStoredHeat()) {
			heatStoredCache = thermalSystem.getStoredHeat();
			heatStoredLabel.setText(Msg.getString(
				"TabPanelThermalSystem.totalHeatStored", //$NON-NLS-1$
				formatter.format(heatStoredCache)
			));
		}

		// Update heat table.
		heatTableModel.update();
	}

	/** 
	 * Internal class used as model for the heat table.
	 */
	private static class HeatTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Settlement settlement;
		private java.util.List<Building> buildings;
		private ImageIcon dotRed;
		private ImageIcon dotYellow;
		private ImageIcon dotGreen;

		private HeatTableModel(Settlement settlement) {
			this.settlement = settlement;
			buildings = settlement.getBuildingManager().getBuildings();
			dotRed = ImageLoader.getIcon(Msg.getString("img.dotRed")); //$NON-NLS-1$
			dotYellow = ImageLoader.getIcon(Msg.getString("img.dotYellow")); //$NON-NLS-1$
			dotGreen = ImageLoader.getIcon(Msg.getString("img.dotGreen")); //$NON-NLS-1$
		}

		public int getRowCount() {
			return buildings.size();
		}

		public int getColumnCount() {
			return 5;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = ImageIcon.class;
			else if (columnIndex == 1) dataType = String.class;
			else if (columnIndex == 2) dataType = Double.class;
			else if (columnIndex == 3) dataType = Double.class;
			else if (columnIndex == 4) dataType = Double.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelThermalSystem.column.s"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelThermalSystem.column.building"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelThermalSystem.column.generated"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelThermalSystem.column.used"); //$NON-NLS-1$
			else if (columnIndex == 4) return Msg.getString("TabPanelThermalSystem.column.temperature"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {

			Building building = buildings.get(row);
			HeatMode heatMode = building.getHeatMode();

			if (column == 0) {
				if (heatMode == HeatMode.FULL_POWER) { 
					return dotGreen;
				}
				else if (heatMode == HeatMode.POWER_DOWN) {
					return dotYellow;
				}
				else if (heatMode == HeatMode.NO_POWER) {
					return dotRed;
				}
				else return null;
			}
			else if (column == 1) return buildings.get(row);
			else if (column == 2) {
				double generated = 0D;
				if (building.hasFunction(BuildingFunction.THERMAL_GENERATION)) {
					try {
						ThermalGeneration generator = (ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
						generated = generator.getGeneratedHeat();
					}
					catch (Exception e) {}
				}
				return generated;
			}
			else if (column == 3) {
				double used = 0D;
				if (heatMode == HeatMode.FULL_POWER)
					used = building.getFullHeatRequired();
				else if (heatMode == HeatMode.POWER_DOWN)
					used = building.getPoweredDownHeatRequired();
				return used;
			}
			else if (column == 4) 
				// return temperature of the building;
				return building.getTemperature();
				
			else return null;
		}

		public void update() {
			if (!buildings.equals(settlement.getBuildingManager().getBuildings())) 
				buildings = settlement.getBuildingManager().getBuildings();

			fireTableDataChanged();
		}
	}
}