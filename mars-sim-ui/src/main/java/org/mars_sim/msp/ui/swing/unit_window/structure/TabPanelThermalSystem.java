/**
 * Mars Simulation Project
 * TabPanelThermalSystem.java
 * @version 3.07 2014-11-02
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.ThermalSystem;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
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
	//private static Logger logger = Logger.getLogger(TabPanelThermalSystem.class.getName());

	// Data Members
	// 2014-10-25  Changed label name to heatGenCapacityLabel
	/** The total heat generated label. */
	private JLabel heatGenCapacityLabel;
	/** The total heat used label. */
	//private JLabel heatGenLabel;
	/** The total heat storage capacity label. */
	private JLabel thermalStorageCapacityLabel;
	/** The total heat stored label. */
	private JLabel heatStoredLabel;
	/** Table model for heat info. */
	private HeatTableModel heatTableModel;
	/** The settlement's Heating System */
	private ThermalSystem thermalSystem;

	//private Building building ;
	//private HeatMode heatMode ;
	
	// Data cache
	/** The total heat generated cache. */
	// 2014-10-25  Changed names of variables to heatGenCapacityCache, heatGenCache
	private double heatGenCapacityCache;
	/** The total heat used cache. */
	//private double heatGenCache;
	/** The total thermal storage capacity cache. */
	private double thermalStorageCapacityCache;
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
		heatGenCapacityCache = thermalSystem.getGeneratedHeat();
		heatGenCapacityLabel = new JLabel(Msg.getString("TabPanelThermalSystem.totalHeatGenCapacity", formatter.format(heatGenCapacityCache)), JLabel.CENTER); //$NON-NLS-1$
		heatInfoPanel.add(heatGenCapacityLabel);

		/*
		// Prepare heat used label.
		heatGenCache = thermalSystem.getRequiredHeat();
		heatGenLabel = new JLabel(Msg.getString("TabPanelThermalSystem.totalHeatGen", formatter.format(heatGenCache)), JLabel.CENTER); //$NON-NLS-1$
		heatInfoPanel.add(heatGenLabel);
		*/
		
		// Prepare heat storage capacity label.
		thermalStorageCapacityCache = thermalSystem.getStoredHeatCapacity();
		thermalStorageCapacityLabel = new JLabel(Msg.getString("TabPanelThermalSystem.heatStorageCapacity", formatter.format(thermalStorageCapacityCache)), JLabel.CENTER); //$NON-NLS-1$
		heatInfoPanel.add(thermalStorageCapacityLabel);

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

		// Prepare thermal control table panel.
		JPanel heatTablePanel = new JPanel(new BorderLayout(0, 0));
		outerTablePanel.add(heatTablePanel);
		//heatScrollPanel.setViewportView(heatTablePanel);

		// Prepare thermal control table model.
		heatTableModel = new HeatTableModel(settlement);

		// Prepare thermal control table.
		JTable heatTable = new JTable(heatTableModel);
		heatTable.setCellSelectionEnabled(false);
		heatTable.setDefaultRenderer(Double.class, new NumberCellRenderer());
		heatTable.getColumnModel().getColumn(0).setPreferredWidth(25);
		heatTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		heatTable.getColumnModel().getColumn(2).setPreferredWidth(60);
		heatTable.getColumnModel().getColumn(3).setPreferredWidth(60);
		heatTable.getColumnModel().getColumn(4).setPreferredWidth(55);
		heatTablePanel.add(heatTable.getTableHeader(), BorderLayout.NORTH);
		heatTablePanel.add(heatTable, BorderLayout.CENTER);
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		// NOT working ThermalGeneration heater = (ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
		// SINCE thermalSystem is a singleton. heatMode always = null not helpful: HeatMode heatMode = building.getHeatMode();	
		//System.out.println("TabPanelThermalSystem : update() : old heatGenCapacityCache is "+ heatGenCapacityCache);	
		//heatGenCapacityCache = thermalSystem.getGeneratedCapacity();
		//System.out.println("TabPanelThermalSystem : update() : new heatGenCache is "+ thermalSystem.getGeneratedHeat());
		//System.out.println("TabPanelThermalSystem : update() : old heatGenCapacityCache is "+ heatGenCapacityCache);
		//System.out.println("TabPanelThermalSystem : update() : new heatGenCapacityCache is "+ thermalSystem.getGeneratedCapacity());
		
		// Check if the old heatGenCapacityCache is different from the latest .
		if (heatGenCapacityCache != thermalSystem.getGeneratedHeat()) {
				heatGenCapacityCache = thermalSystem.getGeneratedHeat();		
			heatGenCapacityLabel.setText(
				Msg.getString(
					"TabPanelThermalSystem.totalHeatGenCapacity", //$NON-NLS-1$
					formatter.format(heatGenCapacityCache)
				)
			);
		}
				
		/*
		 * CANNOT USE thermalSystem class to compute the individual building heat usage 
		 * NOT possible to know individual building's HeatMode (FULL_POWER or POWER_OFF) by calling thermalSystem
		// Update heat Gen label.
		 * 
		 * //System.out.println("TabPanelThermalSystem : heatGenCache is " + heatGenCache);
		//System.out.println("TabPanelThermalSystem : thermalSystem.getGeneratedHeat() is " + thermalSystem.getGeneratedHeat());
		if (heatGenCache != thermalSystem.getGeneratedHeat()) {
			heatGenCache = thermalSystem.getGeneratedHeat();
		System.out.println("TabPanelThermalSystem : YES, they are different");
			heatGenLabel.setText(Msg.getString("TabPanelThermalSystem.totalHeatGen",formatter.format(heatGenCache))); //$NON-NLS-1$
		}
		*/	

		// Update heat storage capacity label.
		if (thermalStorageCapacityCache != thermalSystem.getStoredHeatCapacity()) {
			thermalStorageCapacityCache = thermalSystem.getStoredHeatCapacity();
			thermalStorageCapacityLabel.setText(Msg.getString(
				"TabPanelThermalSystem.heatStorageCapacity", //$NON-NLS-1$
				formatter.format(thermalStorageCapacityCache)
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

		// Update thermal control table.
		heatTableModel.update();
	}

	/** 
	 * Internal class used as model for the thermal control table.
	 */
	private static class HeatTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Settlement settlement;
		// Make sure it's from java.util.List, not java.awt.List
		private List<Building> buildings; // java.util.List, not java.awt.List
		private List<Building> buildingsWithThermal = new ArrayList<Building>();;
		private ImageIcon dotRed;
		private ImageIcon dotYellow;
		private ImageIcon dotGreen;
		//private int n;

		private HeatTableModel(Settlement settlement) {
			this.settlement = settlement;
			
			//2014-11-02 Included only buildings having Thermal control system
			selectBuildings();
			
			dotRed = ImageLoader.getIcon(Msg.getString("img.dotRed")); //$NON-NLS-1$
			dotYellow = ImageLoader.getIcon(Msg.getString("img.dotYellow")); //$NON-NLS-1$
			dotGreen = ImageLoader.getIcon(Msg.getString("img.dotGreen")); //$NON-NLS-1$
		}
		
		//2014-11-02 Created selectBuildings() 
		// Included only buildings having Thermal control system
		public void selectBuildings() {
				BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
			buildings = settlement.getBuildingManager().getBuildings();
			Iterator<Building> i = buildings.iterator();
			//buildingsWithThermal.clear();
			//System.out.println("buildingsWithThermal.clear() : " + buildingsWithThermal.size());
			buildingsWithThermal.removeAll(buildingsWithThermal);
				//System.out.println("buildingsWithThermal.removeAll() : " + buildingsWithThermal.size());

			//n = 0;
			while (i.hasNext()) {
				Building b = i.next();
				String buildingType = b.getBuildingType();
					//System.out.println("buildingType is " + buildingType);
				if (config.hasThermalGeneration(buildingType)) {
					//n++;
					buildingsWithThermal.add(b);
					//System.out.println("buildingsWithThermal.size() : " + buildingsWithThermal.size());
				}
			}
	
		}
		
		//2014-11-02 Included only buildings having Thermal control system
		public int getRowCount() {
			//return buildings.size();
				//System.out.println("getRowCount() : returning buildingsWithThermal.size() : " + buildingsWithThermal.size());
			return buildingsWithThermal.size();
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
			else if (columnIndex == 2) return Msg.getString("TabPanelThermalSystem.column.capacity"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelThermalSystem.column.generated"); //$NON-NLS-1$
			else if (columnIndex == 4) return Msg.getString("TabPanelThermalSystem.column.temperature"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {

			Building building = buildingsWithThermal.get(row);
			HeatMode heatMode = building.getHeatMode();
			//System.out.println("TabPanelThermalSystem : getValueAt() : heatMode is "+ heatMode);	
			BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
			
			// if the building has thermal control system, display columns
			if (building.hasFunction(BuildingFunction.THERMAL_GENERATION)) {	
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
				else if (column == 1) return buildingsWithThermal.get(row);
				else if (column == 2) {
					double generatedCapacity = 0D;
					if (building.hasFunction(BuildingFunction.THERMAL_GENERATION)) {
						try {
							ThermalGeneration heater = (ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
							// 2014-10-25  Changed to calling getGeneratedCapacity()
							generatedCapacity = heater.getGeneratedCapacity();
						}
						catch (Exception e) {}
					}
						//System.out.println("TabPanelThermalSystem : getValueAt() : getGeneratedCapacity() is "+ generatedCapacity);
					return generatedCapacity;
				}
				
				else if (column == 3) {
					double generated = 0D;
					if (building.hasFunction(BuildingFunction.THERMAL_GENERATION)) {
						
						if (heatMode == HeatMode.FULL_POWER) { 
							try {
								ThermalGeneration heater = (ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
								generated = heater.getGeneratedHeat();
							}
							catch (Exception e) {}	
								//System.out.println("TabPanelThermalSystem : getValueAt() : getGeneratedHeat() is "+ generated);
							return generated;
							
						} 
						else if (heatMode == HeatMode.POWER_DOWN) {
							//System.out.println("TabPanelThermalSystem : getValueAt() : getGeneratedHeat() is "+ generated);
							return generated;
						}
					}
					/*
					 * 
					double used = 0D;
					if (heatMode == HeatMode.FULL_POWER)	used = building.getFullHeatRequired();
					else if (heatMode == HeatMode.POWER_DOWN)	used = building.getPoweredDownHeatRequired();
					return used;
					*/
				} 
				else if (column == 4) 
					// return temperature of the building;
					return building.getTemperature();		
			}
			return null;
		}

		public void update() {
			//List<Building> b = settlement.getBuildingManager().getBuildings();
			//2014-11-02 Included only buildings having Thermal control system
			List<Building> cache = buildingsWithThermal;
			selectBuildings();
			//buildingsWithThermal just got updated
			if (!buildingsWithThermal.equals(cache)) 
				buildingsWithThermal = cache;
			fireTableDataChanged();
		}
	}
}