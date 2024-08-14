/*
 * Mars Simulation Project
 * TabPanelPowerGrid.java
 * @date 2024-06-28
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.utility.power.PowerGrid;
import com.mars_sim.core.structure.building.utility.power.PowerMode;
import com.mars_sim.core.structure.building.utility.power.PowerSource;
import com.mars_sim.core.structure.building.utility.power.PowerStorage;
import com.mars_sim.core.structure.building.utility.power.SolarPowerSource;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_window.TabPanelTable;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityModel;


/**
 * This is a tab panel for a settlement's power grid information.
 */
@SuppressWarnings("serial")
public class TabPanelPowerGrid extends TabPanelTable {

	private static final String POWER_ICON = "power";
	
	private static final String PERCENT_PER_SOL = " % per sol";
	private static final String SLASH = " / ";
	private static final String OPEN_PARA = " (";
	private static final String CLOSE_PARA = ")";
	
	private static final String[] TOOLTIPS = {"Power Status", "Building Name",
			"kW Power Generated","kWh Energy Stored in Battery"};
	
	// Data Members
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	// Data cache
	/** The total power generated cache. */
	private double powerGenCache;
	/** The total power used cache. */
	private double powerLoadCache;
	/** The total power storage capacity cache. */
	private double energyCapacityCache;
	/** The total power stored cache. */
	private double energyStoredCache;
	/** The total solar cell efficiency cache. */
	private double solarCellEfficiencyCache;
	
	private double percentPowerUsage;

	private double percentEnergyUsage;

	
	/** The Settlement instance. */
	private Settlement settlement;

	private JLabel solarCellEfficiencyTF;
	private JLabel percentPowerUsageLabel;
	private JLabel percentEnergyUsageLabel;

	private JRadioButton r0;
	private JRadioButton r1;
	private JRadioButton r2;
	private JRadioButton r3;
	
	/** Table model for power info. */
	private PowerTableModel powerTableModel;
	/** The settlement's power grid. */
	private PowerGrid powerGrid;

	private BuildingManager manager;

	private List<Building> buildings;

	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelPowerGrid(Settlement unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(POWER_ICON),
			Msg.getString("TabPanelPowerGrid.title"), //$NON-NLS-1$
			desktop
		);
		settlement = unit;

		setHeaderToolTips(TOOLTIPS);
	}
	
	@Override
	protected JPanel createInfoPanel() {
		powerGrid = settlement.getPowerGrid();
		manager = settlement.getBuildingManager();
		buildings = manager.getBuildingsF1NoF2F3(
				FunctionType.POWER_GENERATION, FunctionType.LIFE_SUPPORT, FunctionType.RESOURCE_PROCESSING);

		JPanel topContentPanel = new JPanel(new BorderLayout());

		// Prepare spring layout power info panel.
		AttributePanel powerInfoPanel = new AttributePanel(4);
		topContentPanel.add(powerInfoPanel);

		// Prepare power generated tf.
		powerGenCache = powerGrid.getGeneratedPower();
		// Prepare power used tf.
		powerLoadCache = powerGrid.getRequiredPower();
		// Prepare the power usage percent
		percentPowerUsage = Math.round(powerGenCache/powerLoadCache * 100 * 10.0)/10.0;
		
		percentPowerUsageLabel = powerInfoPanel.addTextField(Msg.getString("TabPanelPowerGrid.powerUsage"),
				StyleManager.DECIMAL1_PERC.format(percentPowerUsage) + OPEN_PARA 
				+ StyleManager.DECIMAL_KW.format(powerLoadCache) 
				+ SLASH + StyleManager.DECIMAL_KW.format(powerGenCache) + CLOSE_PARA,
				Msg.getString("TabPanelPowerGrid.powerUsage.tooltip"));
		
		// Prepare power storage capacity tf.
		energyCapacityCache = powerGrid.getStoredEnergyCapacity();
		// Prepare power stored tf.
		energyStoredCache = powerGrid.getStoredEnergy();
		// Prepare the energy usage percent
		percentEnergyUsage = Math.round(energyStoredCache/energyCapacityCache * 100 * 10.0)/10.0;

		percentEnergyUsageLabel = powerInfoPanel.addTextField(Msg.getString("TabPanelPowerGrid.energyUsage"),
				StyleManager.DECIMAL1_PERC.format(percentEnergyUsage) + OPEN_PARA 
				+ StyleManager.DECIMAL_KWH.format(energyStoredCache) 
				+ SLASH + StyleManager.DECIMAL_KWH.format(energyCapacityCache) + CLOSE_PARA,
				Msg.getString("TabPanelPowerGrid.energyUsage.tooltip"));
		
		// Create solar cell eff tf
		solarCellEfficiencyCache = getAverageEfficiency();
		solarCellEfficiencyTF = powerInfoPanel.addTextField(Msg.getString("TabPanelPowerGrid.solarPowerEfficiency"),
											 StyleManager.DECIMAL1_PERC.format(solarCellEfficiencyCache * 100D),
											 Msg.getString("TabPanelPowerGrid.solarPowerEfficiency.tooltip"));

		// Create degradation rate tf.
		double solarPowerDegradRate = SolarPowerSource.DEGRADATION_RATE_PER_SOL;
		powerInfoPanel.addTextField(Msg.getString("TabPanelPowerGrid.solarPowerDegradRate"),
									StyleManager.DECIMAL_PLACES2.format(solarPowerDegradRate * 100D) 
									+ PERCENT_PER_SOL,
									Msg.getString("TabPanelPowerGrid.solarPowerDegradRate.tooltip"));

		// Create a button panel
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
		topContentPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		buttonPanel.setBorder(BorderFactory.createTitledBorder("Choose Buildings"));
		buttonPanel.setToolTipText("Select the type of buildings");

		ButtonGroup group0 = new ButtonGroup();

		r0 = new JRadioButton("Power Bldgs", true);
		r1 = new JRadioButton("Bldgs w/ Gen");
		r2 = new JRadioButton("Bldgs w/o Gen");
		r3 = new JRadioButton("All");

		group0.add(r0);
		group0.add(r1);
		group0.add(r2);
		group0.add(r3);
		
		buttonPanel.add(r0);
		buttonPanel.add(r1);
		buttonPanel.add(r2);
		buttonPanel.add(r3);

		PolicyRadioActionListener actionListener = new PolicyRadioActionListener();
		r0.addActionListener(actionListener);
		r1.addActionListener(actionListener);
		r2.addActionListener(actionListener);
		r3.addActionListener(actionListener);
		
		return topContentPanel;
	}

	@Override
	protected TableModel createModel() {
		// Prepare power table model.
		powerTableModel = new PowerTableModel(settlement);
		return powerTableModel;
	}

	@Override
	protected void setColumnDetails(TableColumnModel powerColumns) {

		powerColumns.getColumn(0).setPreferredWidth(10);
		powerColumns.getColumn(1).setPreferredWidth(100);
		powerColumns.getColumn(2).setPreferredWidth(50);
		powerColumns.getColumn(3).setPreferredWidth(50);
		powerColumns.getColumn(4).setPreferredWidth(50);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		powerColumns.getColumn(2).setCellRenderer(renderer);
		powerColumns.getColumn(3).setCellRenderer(renderer);
		powerColumns.getColumn(4).setCellRenderer(renderer);
	}


	class PolicyRadioActionListener implements ActionListener {
	    @Override
	    public void actionPerformed(ActionEvent event) {
	        JRadioButton button = (JRadioButton) event.getSource();

			if (button == r0) {
				buildings = manager.getBuildingsF1NoF2F3(FunctionType.POWER_GENERATION, 
						FunctionType.LIFE_SUPPORT, FunctionType.RESOURCE_PROCESSING);
			}
			else if (button == r1) {
				buildings = getBuildingsWithPowerGeneration();
			}
			else if (button == r2) {
				buildings = manager.getBuildingsNoF1F2(FunctionType.POWER_GENERATION, 
						FunctionType.THERMAL_GENERATION);
			}
			else if (button == r3) {
				buildings = manager.getSortedBuildings();
			}

			powerTableModel.fireTableDataChanged();
	    }
	}

	/**
	 * Gets a list of settlement's buildings with power generation function.
	 *
	 * @return list of buildings
	 */
	private List<Building> getBuildingsWithPowerGeneration() {
		return manager.getBuildings(FunctionType.POWER_GENERATION);
	}

	public double getAverageEfficiency() {
		double eff = 0;
		int i = 0;
		for(Building building : getBuildingsWithPowerGeneration()) {
			for(PowerSource powerSource : building.getPowerGeneration().getPowerSources()) {
				if (powerSource instanceof SolarPowerSource solarPowerSource) {
					i++;
					eff += solarPowerSource.getElectricEfficiency();
				}
			}
		}
		// get the average eff
		if (i > 0) {
			eff = eff / i;
		}
		return eff;
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();

		// Update power generated TF
		double gen = powerGrid.getGeneratedPower();
		// Update power used TF.
		double req = powerGrid.getRequiredPower();

		if (powerGenCache != gen || powerLoadCache != req) {
			powerGenCache = gen;
			powerLoadCache = req;
			percentPowerUsage = Math.round(powerLoadCache / powerGenCache * 100 * 10.0)/10.0;

			String s = StyleManager.DECIMAL1_PERC.format(percentPowerUsage) + OPEN_PARA 
					+ StyleManager.DECIMAL_KW.format(powerLoadCache) 
					+ SLASH + StyleManager.DECIMAL_KW.format(powerGenCache) + CLOSE_PARA;
			
			percentPowerUsageLabel.setText(s);		
		}
		
		// Update power storage capacity TF.
		double cap = powerGrid.getStoredEnergyCapacity();
		// Update power stored TF.
		double store = powerGrid.getStoredEnergy();
		
		if (energyCapacityCache != cap || energyStoredCache != store) {
			energyCapacityCache = cap;
			energyStoredCache = store;
			percentEnergyUsage = Math.round(energyStoredCache / energyCapacityCache * 100 * 10.0)/10.0;
					
			String s = StyleManager.DECIMAL1_PERC.format(percentEnergyUsage) + OPEN_PARA 
					+ StyleManager.DECIMAL_KWH.format(energyStoredCache) 
					+ SLASH + StyleManager.DECIMAL_KWH.format(energyCapacityCache) + CLOSE_PARA;
			
			percentEnergyUsageLabel.setText(s);
		}


		// Update solar cell efficiency TF
		double eff = getAverageEfficiency();
		if (solarCellEfficiencyCache != eff) {
			solarCellEfficiencyCache = eff;
			solarCellEfficiencyTF.setText(StyleManager.DECIMAL1_PERC.format(eff * 100D));
		}
		// Update power table.
		powerTableModel.update();
	}

	/**
	 * Internal class used as model for the power table.
	 */
	private class PowerTableModel extends AbstractTableModel
				implements EntityModel  {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Icon dotRed;
		private Icon dotYellow;
		private Icon dotGreen;

		private PowerTableModel(Settlement settlement) {

			dotRed = ImageLoader.getIconByName("dot/red"); 
			dotYellow = ImageLoader.getIconByName("dot/yellow"); 
			dotGreen = ImageLoader.getIconByName("dot/green"); 

		}

		public int getRowCount() {
			return buildings.size();
		}

		public int getColumnCount() {
			return 5;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0)
				dataType = Icon.class;
			else if (columnIndex == 1)
				dataType = Object.class;
			else if (columnIndex == 2)
				dataType = Double.class;
			else if (columnIndex == 3)
				dataType = Double.class;
			else if (columnIndex == 4)
				dataType = Double.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return Msg.getString("TabPanelPowerGrid.column.s"); //$NON-NLS-1$
			else if (columnIndex == 1)
				return Msg.getString("TabPanelPowerGrid.column.building"); //$NON-NLS-1$
			else if (columnIndex == 2)
				return Msg.getString("TabPanelPowerGrid.column.generated"); //$NON-NLS-1$
			else if (columnIndex == 3)
				return Msg.getString("TabPanelPowerGrid.column.used"); //$NON-NLS-1$
			else
				return Msg.getString("TabPanelPowerGrid.column.stored"); //$NON-NLS-1$
		}

		@Override
		public Object getValueAt(int row, int column) {

			Building building = buildings.get(row);
			PowerMode powerMode = building.getPowerMode();

			if (column == 0) {
				if (powerMode == PowerMode.FULL_POWER) {
					return dotGreen;
				} else if (powerMode == PowerMode.LOW_POWER) {
					return dotYellow;
				} else if (powerMode == PowerMode.NO_POWER) {
					return dotRed;
				} else
					return null;
			} 
			
			else if (column == 1) {
				return buildings.get(row) + " ";
			}
			
			else if (column == 2) {
				double generated = 0D;
				if (building.hasFunction(FunctionType.POWER_GENERATION)) {
					try {
						generated = building.getPowerGeneration().getGeneratedPower();
					} catch (Exception e) {
					}
				}
//				if (building.hasFunction(FunctionType.THERMAL_GENERATION)) {
//					try {
//						generated += building.getThermalGeneration().getGeneratedPower();
//					} catch (Exception e) {
//					}
//				}
				return Math.round(generated * 10.0) / 10.0;
			} 
			
			else if (column == 3) {
				double used = 0D;
				if (powerMode == PowerMode.FULL_POWER)
					used = building.getFullPowerRequired();
				else if (powerMode == PowerMode.LOW_POWER)
					used = building.getLowPowerRequired();
				return Math.round(used * 10.0) / 10.0;
			} 
			
			else {
				PowerStorage ps = building.getPowerStorage();
				double stored = 0;
				if (ps != null) {
					stored = ps.getkWattHourStored();
					return Math.round(stored * 10.0) / 10.0;
				}
			
				return 0;
			}
				
		}

		public void update() {
//			// Check if building list has changed.
//			List<Building> tempBuildings = getBuildings();
//			if (!tempBuildings.equals(buildings)) {
//				buildings = tempBuildings;
//			}

	    	int numRow = getRowCount();
	    	int numCol = getColumnCount();
	    	for (int i=0; i< numRow; i++) {	
	    		for (int j=0; j< numCol; j++) {	
		    		if (j != 1)
		    			fireTableCellUpdated(i, j);
	    		}
	    	}
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return buildings.get(row);
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		solarCellEfficiencyTF = null;

		r0 = null;
		r1 = null;
		r2 = null;
		r3 = null;
		
		powerTableModel = null;
		powerGrid = null;
		manager = null;
		buildings = null;
	}
}


