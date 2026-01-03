/*
 * Mars Simulation Project
 * TabPanelPowerGrid.java
 * @date 2025-07-15
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.utility.power.PowerGeneration;
import com.mars_sim.core.building.utility.power.PowerGrid;
import com.mars_sim.core.building.utility.power.PowerMode;
import com.mars_sim.core.building.utility.power.PowerSource;
import com.mars_sim.core.building.utility.power.PowerStorage;
import com.mars_sim.core.building.utility.power.SolarPowerSource;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityModel;
import com.mars_sim.ui.swing.utils.SwingHelper;


/**
 * This is a tab panel for a settlement's power grid information.
 */
@SuppressWarnings("serial")
class TabPanelPowerGrid extends EntityTableTabPanel<Settlement> implements TemporalComponent {

	private static final String POWER_ICON = "power";

	private static final String POWER = "P";
	private static final String BUILDINGS_WITH_GENERATION = "BWG";
	private static final String BUILDINGS_NO_GENERATION = "BNG";
	private static final String ALL_BUILDINGS = "AB";
	
	private static final String PERCENT_PER_SOL = " % per sol";
	private static final String SLASH = " / ";
	private static final String OPEN_PARA = " (";
	private static final String CLOSE_PARA = ")";
	
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

	private JLabel solarCellEfficiencyTF;
	private JLabel percentPowerUsageLabel;
	private JLabel percentEnergyUsageLabel;
	
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
	 * @param context the UI context.
	 */
	public TabPanelPowerGrid(Settlement unit, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelPowerGrid.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(POWER_ICON), null,
			unit, context
		);
	}
	
	/**
	 * Show the main power details.
	 */
	@Override
	protected JPanel createInfoPanel() {
		var settlement = getEntity();

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
		percentPowerUsage = Math.round(powerGenCache/powerLoadCache * 100 * 10.0)/10.0 ;
		
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
		
		buttonPanel.setBorder(SwingHelper.createLabelBorder("Choose Buildings"));
		buttonPanel.setToolTipText("Select the type of buildings");

		ButtonGroup group0 = new ButtonGroup();

		var r0 = new JRadioButton("Power Bldgs", true);
		r0.setActionCommand(POWER);
		var r1 = new JRadioButton("Bldgs w/ Gen");
		r1.setActionCommand(BUILDINGS_WITH_GENERATION);
		var r2 = new JRadioButton("Bldgs w/o Gen");
		r2.setActionCommand(BUILDINGS_NO_GENERATION);
		var r3 = new JRadioButton("All");
		r3.setActionCommand(ALL_BUILDINGS);

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
		powerTableModel = new PowerTableModel(getEntity());
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

			switch(event.getActionCommand()) {
				case POWER -> buildings = manager.getBuildingsF1NoF2F3(FunctionType.POWER_GENERATION, 
						FunctionType.LIFE_SUPPORT, FunctionType.RESOURCE_PROCESSING);
				case BUILDINGS_WITH_GENERATION -> buildings = getBuildingsWithPowerGeneration();
				case BUILDINGS_NO_GENERATION -> buildings = manager.getBuildingsNoF1F2(FunctionType.POWER_GENERATION, 
						FunctionType.THERMAL_GENERATION);
				case ALL_BUILDINGS -> buildings = manager.getSortedBuildings();
				default -> {
					// Do nothing as not understood
				}
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


	@Override
	public void clockUpdate(ClockPulse pulse) {

		// Update power generated TF
		double gen = powerGrid.getGeneratedPower();
		// Update power used TF.
		double req = powerGrid.getRequiredPower();

		if (Math.abs(powerGenCache - gen) > .4 || Math.abs(powerLoadCache - req) > .4) {
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
		
		if (Math.abs(energyCapacityCache - cap) > .4 || Math.abs(energyStoredCache - store) > .4) {
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
		if (Math.abs(solarCellEfficiencyCache - eff) > .4) {
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
			return switch (columnIndex) {
				case 0 -> Icon.class;
				case 1 -> String.class;
				case 2,3,4 -> Double.class;
				default -> null;
			};
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> Msg.getString("TabPanelPowerGrid.column.s");
				case 1 -> Msg.getString("Building.singular");
				case 2 -> Msg.getString("TabPanelPowerGrid.column.generated");
				case 3 -> Msg.getString("TabPanelPowerGrid.column.used");
				case 4 -> Msg.getString("TabPanelPowerGrid.column.stored");
				default -> null;
			};
		}

		@Override
		public Object getValueAt(int row, int column) {

			Building building = buildings.get(row);
			PowerMode powerMode = building.getPowerMode();

			switch (column) {
				case 0 -> {
						return switch (powerMode) {
							case PowerMode.FULL_POWER -> dotGreen;
							case PowerMode.LOW_POWER -> dotYellow;
							case PowerMode.NO_POWER -> dotRed;
							default -> null;
						};
					}
				case 1 -> { return buildings.get(row).getName(); }
				case 2 -> {
						double generated = 0D;
						PowerGeneration pg = building.getFunction(FunctionType.POWER_GENERATION);
						if (pg != null) {
							try {
								generated = pg.getGeneratedPower();
							} catch (Exception e) {
							}
						}
						return Math.round(generated * 10.0) / 10.0;
					}
				case 3 -> {
						double used = 0D;
						if (powerMode == PowerMode.FULL_POWER)
							used = building.getFullPowerRequired();
						else if (powerMode == PowerMode.LOW_POWER)
							used = building.getLowPowerRequired();
						return Math.round(used * 10.0) / 10.0;
					}
				default -> {
						PowerStorage ps = building.getPowerStorage();
						double stored = 0D;
						if (ps != null) {
							stored = ps.getBattery().getCurrentStoredEnergy();
							return Math.round(stored * 10.0) / 10.0;
						}
					
						return 0D;
					}
			}	
		}

		public void update() {
			if (buildings.isEmpty())
				return;
			fireTableRowsUpdated(0, buildings.size() - 1);
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return buildings.get(row);
		}
	}
}