/*
 * Mars Simulation Project
 * TabPanelPowerGrid.java
 * @date 2023-05-23
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
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

import com.mars_sim.core.Unit;
import com.mars_sim.core.structure.PowerGrid;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.PowerMode;
import com.mars_sim.core.structure.building.function.PowerSource;
import com.mars_sim.core.structure.building.function.PowerStorage;
import com.mars_sim.core.structure.building.function.SolarPowerSource;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_window.TabPanelTable;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.UnitModel;


/**
 * This is a tab panel for a settlement's power grid information.
 */
@SuppressWarnings("serial")
public class TabPanelPowerGrid extends TabPanelTable {

	private static final String POWER_ICON = "power";
	
	private static final String PERCENT_PER_SOL = " % per sol";
	private static final String PERCENT = " %";
	private static final String[] TOOLTIPS = {"Power Status", "Building Name",
			"kW Power Generated","kWh Energy Stored in Battery"};
	
	// Data Members
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	// Data cache
	/** The total power generated cache. */
	private double powerGeneratedCache;
	/** The total power used cache. */
	private double powerUsedCache;
	/** The total power storage capacity cache. */
	private double energyStorageCapacityCache;
	/** The total power stored cache. */
	private double energyStoredCache;
	/** The total solar cell efficiency cache. */
	private double solarCellEfficiencyCache;
	
	private double percentPower;

	private double percentEnergy;

	
	/** The Settlement instance. */
	private Settlement settlement;

	private JLabel solarCellEfficiencyTF;
	private JLabel percentPowerLabel;
	private JLabel percentEnergyLabel;

	private JRadioButton r0;
	private JRadioButton r1;
	private JRadioButton r2;
	private JRadioButton r3;
	
	/** Table model for power info. */
	private PowerTableModel powerTableModel;
	/** The settlement's power grid. */
	private PowerGrid powerGrid;

	private BuildingManager manager;

	private List<PowerSource> powerSources;

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
		powerGeneratedCache = powerGrid.getGeneratedPower();
		// Prepare power used tf.
		powerUsedCache = powerGrid.getRequiredPower();
		// Prepare the power usage percent
		percentPower = Math.round(powerGeneratedCache/powerUsedCache * 1000.0)/10.0;
		
		percentPowerLabel = powerInfoPanel.addTextField(Msg.getString("TabPanelPowerGrid.powerUsage"),
				percentPower + PERCENT + " (" + StyleManager.DECIMAL_KW.format(powerUsedCache) 
				+ " / " + StyleManager.DECIMAL_KW.format(powerGeneratedCache) + ")",
				Msg.getString("TabPanelPowerGrid.powerUsage.tooltip"));
		
		// Prepare power storage capacity tf.
		energyStorageCapacityCache = powerGrid.getStoredEnergyCapacity();
		// Prepare power stored tf.
		energyStoredCache = powerGrid.getStoredEnergy();
		// Prepare the energy usage percent
		percentEnergy = Math.round(energyStoredCache/energyStorageCapacityCache * 1000.0)/10.0;

		percentEnergyLabel = powerInfoPanel.addTextField(Msg.getString("TabPanelPowerGrid.energyUsage"),
				percentEnergy + PERCENT + " (" + StyleManager.DECIMAL_KWH.format(energyStoredCache) 
				+ " / " + StyleManager.DECIMAL_KWH.format(energyStorageCapacityCache) + ")",
				Msg.getString("TabPanelPowerGrid.energyUsage.tooltip"));
		
		// Create solar cell eff tf
		solarCellEfficiencyCache = getAverageEfficiency();
		solarCellEfficiencyTF = powerInfoPanel.addTextField(Msg.getString("TabPanelPowerGrid.solarPowerEfficiency"),
											 StyleManager.DECIMAL_PLACES2.format(solarCellEfficiencyCache * 100D) + PERCENT,
											 Msg.getString("TabPanelPowerGrid.solarPowerEfficiency.tooltip"));

		// Create degradation rate tf.
		double solarPowerDegradRate = SolarPowerSource.DEGRADATION_RATE_PER_SOL;
		powerInfoPanel.addTextField(Msg.getString("TabPanelPowerGrid.solarPowerDegradRate"),
									StyleManager.DECIMAL_PLACES2.format(solarPowerDegradRate * 100D) + PERCENT_PER_SOL,
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
				buildings = manager.getBuildingsF1NoF2F3(
						FunctionType.POWER_GENERATION, FunctionType.LIFE_SUPPORT, FunctionType.RESOURCE_PROCESSING);
			}
			else if (button == r1) {
				buildings = manager.getBuildingsWithPowerGeneration();
			}
			else if (button == r2) {
				buildings = manager.getBuildingsNoF1F2(FunctionType.POWER_GENERATION, FunctionType.THERMAL_GENERATION);
			}
			else if (button == r3) {
				buildings = manager.getSortedBuildings();
			}

			powerTableModel.update();
	    }
	}
	

	/**
	 * Gets a list of buildings should be shown.
	 * 
	 * @return a list of buildings
	 */
	private List<Building> getBuildings() {
		return buildings;
	}

	public double getAverageEfficiency() {
		double eff = 0;
		int i = 0;
		Iterator<Building> iPower = manager.getBuildingsWithPowerGeneration().iterator();
		while (iPower.hasNext()) {
			Building building = iPower.next();
			powerSources = building.getPowerGeneration().getPowerSources();
			Iterator<PowerSource> j = powerSources.iterator();
			while (j.hasNext()) {
				PowerSource powerSource = j.next();
				if (powerSource instanceof SolarPowerSource) {
					i++;
					SolarPowerSource solarPowerSource = (SolarPowerSource) powerSource;
					eff += solarPowerSource.getEfficiency();
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

		if (powerGeneratedCache != gen || powerUsedCache != req) {
			powerGeneratedCache = gen;
			powerUsedCache = req;
//			double powerAverage = .5 * (gen + req);
			percentPower = Math.round(powerUsedCache / powerGeneratedCache * 1000.0)/10.0;

			String s = percentPower + " % (" + StyleManager.DECIMAL_KW.format(powerUsedCache) 
					+ " / " + StyleManager.DECIMAL_KW.format(powerGeneratedCache) + ")";
			
			percentPowerLabel.setText(s);		
		}
		
		// Update power storage capacity TF.
		double cap = powerGrid.getStoredEnergyCapacity();
		// Update power stored TF.
		double store = powerGrid.getStoredEnergy();
		
		if (energyStorageCapacityCache != cap || energyStoredCache != store) {
			energyStorageCapacityCache = cap;
			energyStoredCache = store;
			percentEnergy = Math.round(energyStoredCache / energyStorageCapacityCache * 1000.0)/10.0;
					
			String s = percentEnergy + " % (" + StyleManager.DECIMAL_KWH.format(energyStoredCache) 
			+ " / " + StyleManager.DECIMAL_KWH.format(energyStorageCapacityCache) + ")";
			
			percentEnergyLabel.setText(s);
		}


		// Update solar cell efficiency TF
		double eff = getAverageEfficiency();
		if (solarCellEfficiencyCache != eff) {
			solarCellEfficiencyCache = eff;
			solarCellEfficiencyTF.setText(StyleManager.DECIMAL_PLACES2.format(eff * 100D) + PERCENT);
		}
		// Update power table.
		powerTableModel.update();
	}

	/**
	 * Internal class used as model for the power table.
	 */
	private class PowerTableModel extends AbstractTableModel
				implements UnitModel  {

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

		public Object getValueAt(int row, int column) {

			Building building = buildings.get(row);
			PowerMode powerMode = building.getPowerMode();

			if (column == 0) {
				if (powerMode == PowerMode.FULL_POWER) {
					return dotGreen;
				} else if (powerMode == PowerMode.POWER_DOWN) {
					return dotYellow;
				} else if (powerMode == PowerMode.POWER_UP) {
					return dotGreen;
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
				if (building.hasFunction(FunctionType.THERMAL_GENERATION)) {
					try {
						generated += building.getThermalGeneration().getGeneratedPower();
					} catch (Exception e) {
					}
				}
				return Math.round(generated * 100.0) / 100.0;
			} 
			
			else if (column == 3) {
				double used = 0D;
				if (powerMode == PowerMode.FULL_POWER)
					used = building.getFullPowerRequired();
				else if (powerMode == PowerMode.POWER_DOWN)
					used = building.getPoweredDownPowerRequired();
				return Math.round(used * 100.0) / 100.0;
			} 
			
			else {
				PowerStorage ps = building.getPowerStorage();
				double stored = 0;
				if (ps != null) {
					stored = ps.getkWattHourStored();
					return Math.round(stored * 100.0) / 100.0;
				}
			
				return 0;
			}
				
		}

		public void update() {
			// Check if building list has changed.
			List<Building> tempBuildings = getBuildings();
			if (!tempBuildings.equals(buildings)) {
				buildings = tempBuildings;
			}

			fireTableDataChanged();
		}

		@Override
		public Unit getAssociatedUnit(int row) {
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
		powerSources = null;
		buildings = null;
	}
}


