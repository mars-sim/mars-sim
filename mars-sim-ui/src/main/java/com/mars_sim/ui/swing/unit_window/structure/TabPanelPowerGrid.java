/*
 * Mars Simulation Project
 * TabPanelPowerGrid.java
 * @date 2025-07-15
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.utility.power.PowerGeneration;
import com.mars_sim.core.building.utility.power.PowerGrid;
import com.mars_sim.core.building.utility.power.PowerMode;
import com.mars_sim.core.building.utility.power.PowerStorage;
import com.mars_sim.core.building.utility.power.SolarPowerSource;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.model.BaseBuildingModel;


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
	
	private double percentPowerUsage;

	private double percentEnergyUsage;

	private JDoubleLabel solarCellEfficiencyTF;
	private JLabel percentPowerUsageLabel;
	private JLabel percentEnergyUsageLabel;
	
	/** Table model for power info. */
	private PowerTableModel powerTableModel;
	/** The settlement's power grid. */
	private PowerGrid powerGrid;

	private BuildingManager manager;

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

		JPanel topContentPanel = new JPanel(new BorderLayout());

		// Prepare spring layout power info panel.
		AttributePanel powerInfoPanel = new AttributePanel(4);
		topContentPanel.add(powerInfoPanel);

		// Prepare power generated tf.
		powerGenCache = powerGrid.getGeneratedPower();
		// Prepare power used tf.
		powerLoadCache = powerGrid.getPowerLoad();
		// Prepare the power usage percent
		percentPowerUsage = Math.clamp((powerLoadCache / powerGenCache) * 100D, 0, 100);
		
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
		percentEnergyUsage = (energyStoredCache/energyCapacityCache) * 100D;

		percentEnergyUsageLabel = powerInfoPanel.addTextField(Msg.getString("TabPanelPowerGrid.energyUsage"),
				StyleManager.DECIMAL1_PERC.format(percentEnergyUsage) + OPEN_PARA 
				+ StyleManager.DECIMAL_KWH.format(energyStoredCache) 
				+ SLASH + StyleManager.DECIMAL_KWH.format(energyCapacityCache) + CLOSE_PARA,
				Msg.getString("TabPanelPowerGrid.energyUsage.tooltip"));
		
		// Create solar cell eff tf
		solarCellEfficiencyTF = new JDoubleLabel(StyleManager.DECIMAL1_PERC, getAverageEfficiency() * 100D);
		powerInfoPanel.addLabelledItem(Msg.getString("TabPanelPowerGrid.solarPowerEfficiency"), solarCellEfficiencyTF,
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
		r0.addActionListener(e -> setSelection(POWER));
		var r1 = new JRadioButton("Bldgs w/ Gen");
		r1.addActionListener(e -> setSelection(BUILDINGS_WITH_GENERATION));
		var r2 = new JRadioButton("Bldgs w/o Gen");
		r2.addActionListener(e -> setSelection(BUILDINGS_NO_GENERATION));
		var r3 = new JRadioButton("All");
		r3.addActionListener(e -> setSelection(ALL_BUILDINGS));

		group0.add(r0);
		group0.add(r1);
		group0.add(r2);
		group0.add(r3);
		
		buttonPanel.add(r0);
		buttonPanel.add(r1);
		buttonPanel.add(r2);
		buttonPanel.add(r3);
		
		return topContentPanel;
	}

	@Override
	protected TableModel createModel() {
		// Prepare power table model.
		powerTableModel = new PowerTableModel();
		setSelection(POWER);
		return powerTableModel;
	}

	/**
	 * Customises the power table's column details such as width and cell renderers.
	 */
	@Override
	protected void setColumnDetails(TableColumnModel powerColumns) {
		powerColumns.getColumn(0).setPreferredWidth(20);
		powerColumns.getColumn(0).setCellRenderer(new PowerModeCellRenderer());
	}

	private void setSelection(String selected) {
		List<Building> buildings = switch(selected) {
			case POWER -> manager.getBuildingsF1NoF2F3(FunctionType.POWER_GENERATION, 
					FunctionType.LIFE_SUPPORT, FunctionType.RESOURCE_PROCESSING);
			case BUILDINGS_WITH_GENERATION -> getBuildingsWithPowerGeneration();
			case BUILDINGS_NO_GENERATION -> manager.getBuildingsNoF1F2(FunctionType.POWER_GENERATION, 
					FunctionType.THERMAL_GENERATION);
			case ALL_BUILDINGS -> manager.getSortedBuildings();
			default -> Collections.emptyList();
		};
		powerTableModel.setEntities(buildings);
	}

	/**
	 * Gets a list of settlement's buildings with power generation function.
	 *
	 * @return list of buildings
	 */
	private List<Building> getBuildingsWithPowerGeneration() {
		return manager.getBuildings(FunctionType.POWER_GENERATION);
	}

	private double getAverageEfficiency() {
		return getBuildingsWithPowerGeneration().stream()
				.flatMap(b -> b.getPowerGeneration().getPowerSources().stream())
				.filter(SolarPowerSource.class::isInstance)
				.mapToDouble(s -> ((SolarPowerSource)s).getElectricEfficiency())
				.average().orElse(0);
	}

	/**
	 * Values are always updated on the Clock pulse as there are so many small attributes.
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {

		// Update power generated TF
		double gen = powerGrid.getGeneratedPower();
		// Update power used TF.
		double req = powerGrid.getPowerLoad();

		if (Math.abs(powerGenCache - gen) > .4 || Math.abs(powerLoadCache - req) > .4) {
			powerGenCache = gen;
			powerLoadCache = req;
			
			percentPowerUsage = Math.clamp(powerLoadCache / powerGenCache * 100, 0, 100);

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
			percentEnergyUsage = (energyStoredCache / energyCapacityCache) * 100D;
					
			String s = StyleManager.DECIMAL1_PERC.format(percentEnergyUsage) + OPEN_PARA 
					+ StyleManager.DECIMAL_KWH.format(energyStoredCache) 
					+ SLASH + StyleManager.DECIMAL_KWH.format(energyCapacityCache) + CLOSE_PARA;
			
			percentEnergyUsageLabel.setText(s);
		}


		// Update solar cell efficiency TF
		solarCellEfficiencyTF.setValue(getAverageEfficiency() * 100D);
		// Update power table.
		powerTableModel.update();
	}

	/**
	 * Custom cell renderer for displaying power mode icons in the power table.
	 */
	private static class PowerModeCellRenderer extends DefaultTableCellRenderer {
		private static final Icon DOT_RED = ImageLoader.getIconByName("dot/red"); 
		private static final Icon DOT_YELLOW = ImageLoader.getIconByName("dot/yellow"); 
		private static final Icon DOT_GREEN = ImageLoader.getIconByName("dot/green"); 

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			// Use null as value of JLabel is blank
			JLabel cell = (JLabel)super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row,
					column);
			
			PowerMode powerMode = (PowerMode)value;
			if (powerMode == null) {
				cell.setIcon(null);
				return cell;
			}
			cell.setIcon(switch (powerMode) {
				case PowerMode.FULL_POWER -> DOT_GREEN;
				case PowerMode.LOW_POWER -> DOT_YELLOW;
				case PowerMode.NO_POWER -> DOT_RED;
				default -> null;
			});

			return cell;
		}
	}

	/**
	 * Internal class used as model for the power table.
	 */
	private class PowerTableModel extends BaseBuildingModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private static final int STATUS_VAL = 101;
		private static final int PRIORITY_VAL = 102;
		private static final int GENERATED_VAL = 103;
		private static final int USED_VAL = 104;
		private static final int STORED_VAL = 105;
		private static final int CAP_VAL = 106;
		
		private static final EntityColumnSpec STATUS = new EntityColumnSpec(
						new ColumnSpec(STATUS_VAL, Msg.getString("TabPanelPowerGrid.column.s"), PowerMode.class), null);
		private static final EntityColumnSpec PRIORITY = new EntityColumnSpec(
						new ColumnSpec(PRIORITY_VAL, Msg.getString("TabPanelPowerGrid.column.priority"), Integer.class), null);
		private static final EntityColumnSpec GENERATED = new EntityColumnSpec(
						new ColumnSpec(GENERATED_VAL, Msg.getString("TabPanelPowerGrid.column.generated"), Double.class), null);
		private static final EntityColumnSpec USED = new EntityColumnSpec(
						new ColumnSpec(USED_VAL, Msg.getString("TabPanelPowerGrid.column.used"), Double.class), null);
		private static final EntityColumnSpec STORED = new EntityColumnSpec(
						new ColumnSpec(STORED_VAL, Msg.getString("TabPanelPowerGrid.column.stored"), Double.class), null);
		private static final EntityColumnSpec CAP = new EntityColumnSpec(
						new ColumnSpec(CAP_VAL, Msg.getString("TabPanelPowerGrid.column.cap"), Double.class), null);
		
		private PowerTableModel() {
			super(STATUS, PRIORITY, NAME, GENERATED, USED, STORED, CAP);
		}

		@Override
		protected Object getEntityValue(Building building, int valueIndex) {
			PowerMode powerMode = building.getPowerMonitor().getPowerMode();

			return switch (valueIndex) {
				case STATUS_VAL -> powerMode;
				case PRIORITY_VAL -> building.getPowerPriority();
				case GENERATED_VAL -> {
						double generated = 0D;
						PowerGeneration pg = building.getFunction(FunctionType.POWER_GENERATION);
						if (pg != null) {
							try {
								generated = pg.getGeneratedPower();
							} catch (Exception e) {
								// Should not happen
							}
						}
						yield generated;
				}
				case USED_VAL -> {
						double used = building.getPowerMonitor().getPowerLoad();
						yield used;
				}
				case STORED_VAL -> {
						double stored = 0D;
						PowerStorage ps = building.getPowerStorage();
						if (ps != null) {
							stored = ps.getBattery().getStoredEnergy();
						}
					
						yield stored;
				}
				case CAP_VAL -> {
						double cap = 0D;
						PowerStorage ps = building.getPowerStorage();
						if (ps != null) {
							cap = ps.getBattery().getEnergyStorageCapacity();
						}
					
						yield cap;
				}
				default -> super.getEntityValue(building, valueIndex);
			};
		}

		/**
		 * Gets tooltip for status column to show the power mode name.
		 */
		@Override
		protected String getEntityDescription(Building building, int valueIndex) {
			if (valueIndex == STATUS_VAL) {
				return building.getPowerMonitor().getPowerMode().getName();
			}
			
			return super.getEntityDescription(building, valueIndex);
		}

		public void update() {
			if (getRowCount() == 0)
				return;
			fireTableRowsUpdated(0, getRowCount() - 1);
		}
	}
}