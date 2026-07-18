/*
 * Mars Simulation Project
 * TabPanelThermal.java
 * @date 2025-07-15
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.utility.heating.ElectricHeatSource;
import com.mars_sim.core.building.utility.heating.HeatMode;
import com.mars_sim.core.building.utility.heating.SolarHeatingSource;
import com.mars_sim.core.building.utility.heating.ThermalGeneration;
import com.mars_sim.core.building.utility.heating.ThermalSystem;
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
 * This is a tab panel for settlement's Thermal System .
 */
@SuppressWarnings("serial")
class TabPanelThermal extends EntityTableTabPanel<Settlement> 
								implements TemporalComponent {
	
	private static final String HEAT_ICON = "heat";

	private JCheckBox checkbox;
	
	private JDoubleLabel heatGenElectricLabel;
	private JDoubleLabel heatGenFuelLabel;
	private JDoubleLabel heatGenNuclearLabel;
	private JDoubleLabel heatGenSolarLabel;	
	private JDoubleLabel totHeatGenLabel;
	private JDoubleLabel totHeatLoadLabel;	
	private JDoubleLabel powerGenLabel;
	private JDoubleLabel electricEffTF;
	private JDoubleLabel solarEffTF;
	
	/** Table model for heat info. */
	private HeatTableModel heatTableModel;
	/** The settlement's Heating System */
	private ThermalSystem thermalSystem;
	
	private BuildingManager manager;

	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param context the main desktop.
	 */
	public TabPanelThermal(Settlement unit, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelThermalSystem.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(HEAT_ICON), null,
			unit, context
		);
	}

	
	@Override
	protected JPanel createInfoPanel() {
		var settlement = getEntity();
		manager = settlement.getBuildingManager();
		thermalSystem = settlement.getThermalSystem();

		JPanel topContentPanel = new JPanel();
		topContentPanel.setLayout(new BoxLayout(topContentPanel, BoxLayout.Y_AXIS));
		
		// Prepare heat info panel.
		AttributePanel heatInfoPanel = new AttributePanel(10);
		topContentPanel.add(heatInfoPanel);

		// Prepare total heat load label.
		totHeatLoadLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, thermalSystem.getTotalHeatReq());
		heatInfoPanel.addLabelledItem(
				Msg.getString("TabPanelThermalSystem.totalHeatLoad"), //$NON-NLS-1$
				totHeatLoadLabel,
				Msg.getString("TabPanelThermalSystem.totalHeatLoad.tooltip")); //$NON-NLS-1$

		// Prepare total heat gen label.
		totHeatGenLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, thermalSystem.getTotalHeatGen());
		heatInfoPanel.addLabelledItem(
				Msg.getString("TabPanelThermalSystem.totalHeatGen"), //$NON-NLS-1$
					totHeatGenLabel,
					Msg.getString("TabPanelThermalSystem.totalHeatGen.tooltip")); //$NON-NLS-1$

		electricEffTF = new JDoubleLabel(StyleManager.DECIMAL_PERC, getAverageEfficiencyElectricHeat() * 100D);
		heatInfoPanel.addLabelledItem(
				Msg.getString("TabPanelThermalSystem.electricHeatingEfficiency"),//$NON-NLS-1$
					electricEffTF,
					Msg.getString("TabPanelThermalSystem.electricHeatingEfficiency.tooltip")); //$NON-NLS-1$

		solarEffTF = new JDoubleLabel(StyleManager.DECIMAL_PERC, getAverageEfficiencySolarHeating() * 100D);
		heatInfoPanel.addLabelledItem(
				Msg.getString("TabPanelThermalSystem.solarHeatingEfficiency"), //$NON-NLS-1$
					solarEffTF,	Msg.getString("TabPanelThermalSystem.solarHeatingEfficiency.tooltip")); //$NON-NLS-1$		

		// Prepare degradation rate label.
		double degradRate = SolarHeatingSource.DEGRADATION_RATE_PER_SOL;
		heatInfoPanel.addTextField(
				Msg.getString("TabPanelThermalSystem.degradRate"), //$NON-NLS-1$
							StyleManager.DECIMAL_PERC.format(degradRate*100D),
							Msg.getString("TabPanelThermalSystem.degradRate.tooltip")); //$NON-NLS-1$	

		// Prepare power generated label.
		powerGenLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, thermalSystem.getTotalPowerGen());
		heatInfoPanel.addLabelledItem(
				Msg.getString("TabPanelThermalSystem.totalPowerGen"), //$NON-NLS-1$
					powerGenLabel,
					Msg.getString("TabPanelThermalSystem.totalPowerGen.tooltip")); //$NON-NLS-1$

		// Create total head panel
		JPanel totalHeatPanel = new JPanel(new GridLayout(2, 4));
		totalHeatPanel.setBorder(SwingHelper.createLabelBorder("Heat Generated (kW)"));
		topContentPanel.add(totalHeatPanel);

		totalHeatPanel.add(generateHeatLabel("heatGenSolar"));
		totalHeatPanel.add(generateHeatLabel("heatGenNuclear"));
		totalHeatPanel.add(generateHeatLabel("heatGenElectric"));
		totalHeatPanel.add(generateHeatLabel("heatGenFuel"));

		heatGenSolarLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, thermalSystem.getHeatGenSolar());
		totalHeatPanel.add(heatGenSolarLabel);
		
		heatGenNuclearLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, thermalSystem.getHeatGenNuclear());
		totalHeatPanel.add(heatGenNuclearLabel);

		heatGenElectricLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, thermalSystem.getHeatGenElectric());
		totalHeatPanel.add(heatGenElectricLabel);

		heatGenFuelLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, thermalSystem.getHeatGenFuel());
		totalHeatPanel.add(heatGenFuelLabel);

		// Create override check box panel.
		JPanel checkboxPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(checkboxPane);
		
		// Create override check box.
		checkbox = new JCheckBox(Msg.getString("TabPanelThermalSystem.checkbox.value")); //$NON-NLS-1$
		checkbox.setToolTipText(Msg.getString("TabPanelThermalSystem.checkbox.tooltip")); //$NON-NLS-1$
		checkbox.addActionListener(e -> setNonGenerating(checkbox.isSelected()));
		checkbox.setSelected(false);
		checkboxPane.add(checkbox);
		
		return topContentPanel;
	}

	private static final JLabel generateHeatLabel(String key) {
		JLabel label = new JLabel(
				Msg.getString("TabPanelThermalSystem." + key)); //$NON-NLS-1$
		label.setFont(StyleManager.getLabelFont());
		label.setToolTipText(
				Msg.getString("TabPanelThermalSystem." + key + ".tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
		return label;
	}

	@Override
	protected TableModel createModel() {
		// Prepare thermal control table model.
		heatTableModel = new HeatTableModel();
		setNonGenerating(checkbox.isSelected());
		return heatTableModel;
	}

	@Override
	protected void setColumnDetails(TableColumnModel heatColumns) {
		
		var renderer = new HeatModeCellRenderer();
		heatColumns.getColumn(0).setCellRenderer(renderer);
	}

	/**
	 * Sets if non-generating buildings should be shown.
	 * 
	 * @param value true or false.
	 */
	private void setNonGenerating(boolean value) {
		Collection<Building> buildings;
		if (value)
			buildings = manager.getBuildingSet();
		else
			buildings = getBuildingsWithThermal();
		
		heatTableModel.setEntities(buildings);
	}

	private Collection<Building> getBuildingsWithThermal() {
		return manager.getBuildings(FunctionType.THERMAL_GENERATION);
	}

	/**
	 * Gets average solar heating thermal efficiency.
	 * 
	 * @return
	 */
	private double getAverageEfficiencySolarHeating() {
		return getBuildingsWithThermal().stream()
				.flatMap(b -> b.getThermalGeneration().getHeatSources().stream())
				.filter(SolarHeatingSource.class::isInstance)
				.mapToDouble(s -> ((SolarHeatingSource)s).getThermalEfficiency())
				.average()
				.orElse(0D);
	}

	/**
	 * Gets average electric heating thermal efficiency.
	 * 
	 * @return
	 */
	private double getAverageEfficiencyElectricHeat() {
		return getBuildingsWithThermal().stream()
				.flatMap(b -> b.getThermalGeneration().getHeatSources().stream())
				.filter(ElectricHeatSource.class::isInstance)
				.mapToDouble(s -> ((ElectricHeatSource)s).getThermalEfficiency())
				.average()
				.orElse(0D);
	}

	/**
	 * Refresh the dynamic parts of the panel
	 * @param pulse
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		refreshDetails();
	}

	/**
	 * Updates the info on this panel.
	 */
	private void refreshDetails() {

		totHeatGenLabel.setValue(thermalSystem.getTotalHeatGen());
		totHeatLoadLabel.setValue(thermalSystem.getTotalHeatReq());
		heatGenElectricLabel.setValue(thermalSystem.getHeatGenElectric());
		heatGenFuelLabel.setValue(thermalSystem.getHeatGenFuel());
		heatGenNuclearLabel.setValue(thermalSystem.getHeatGenNuclear());
		heatGenSolarLabel.setValue(thermalSystem.getHeatGenSolar());
		powerGenLabel.setValue(thermalSystem.getTotalPowerGen());
		electricEffTF.setValue(getAverageEfficiencyElectricHeat() * 100D);
		solarEffTF.setValue(getAverageEfficiencySolarHeating() * 100D);

		// Update thermal control table.
		if (heatTableModel != null) {
			heatTableModel.update();
		}
	}

	/**
	 * Custom cell renderer for displaying power mode icons in the power table.
	 */
	private static class HeatModeCellRenderer extends DefaultTableCellRenderer {
		private static final Icon DOT_RED = ImageLoader.getIconByName("dot/red"); 
		private static final Icon DOT_YELLOW = ImageLoader.getIconByName("dot/yellow"); 
		private static final Icon DOT_GREEN = ImageLoader.getIconByName("dot/green"); 
		private static final Icon DOT_GREEN_QUARTER = ImageLoader.getIconByName("dot/green_quarter");
		private static final Icon DOT_GREEN_HALF = ImageLoader.getIconByName("dot/green_half");
		private static final Icon DOT_GREEN_THREE_QUARTER = ImageLoader.getIconByName("dot/green_three_quarter");

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			// Use null as value of JLabel is blank
			JLabel cell = (JLabel)super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row,
					column);
			
			HeatMode heatMode = (HeatMode)value;
			if (heatMode == null) {
				cell.setIcon(null);
				return cell;
			}
			var icon = switch(heatMode) {
				case HEAT_OFF -> DOT_YELLOW;
				case OFFLINE -> DOT_RED;
				case ONE_EIGHTH_HEAT, QUARTER_HEAT -> DOT_GREEN_QUARTER;
				case THREE_EIGHTH_HEAT, HALF_HEAT -> DOT_GREEN_HALF;
				case FIVE_EIGHTH_HEAT, THREE_QUARTER_HEAT -> DOT_GREEN_THREE_QUARTER;
				case SEVEN_EIGHTH_HEAT, FULL_HEAT -> DOT_GREEN;
				default -> null;
			};
			cell.setIcon(icon);

			return cell;
		}
	}

	/**
	 * Internal class used as model for the thermal control table.
	 */
	private class HeatTableModel extends BaseBuildingModel {

		private static final long serialVersionUID = 1L;
		private static final int STATUS_VAL = 101;
		private static final int TEMP_VAL = 102;
		private static final int GENERATED_VAL = 103;
		private static final int LOAD_VAL = 104;
		private static final int CAPACITY_VAL = 105;

		private static final EntityColumnSpec STATUS = new EntityColumnSpec(
						new ColumnSpec(STATUS_VAL, Msg.getString("TabPanelThermalSystem.column.s"), HeatMode.class), null);
		private static final EntityColumnSpec TEMP = new EntityColumnSpec(
						new ColumnSpec(TEMP_VAL, Msg.getString("TabPanelThermalSystem.column.temperature"), Double.class), null);
		private static final EntityColumnSpec GENERATED = new EntityColumnSpec(
						new ColumnSpec(GENERATED_VAL, Msg.getString("TabPanelThermalSystem.column.heat.gen"), Double.class), null);
		private static final EntityColumnSpec LOAD = new EntityColumnSpec(
						new ColumnSpec(LOAD_VAL, Msg.getString("TabPanelThermalSystem.column.heat.load"), Double.class), null);
		private static final EntityColumnSpec CAPACITY = new EntityColumnSpec(
						new ColumnSpec(CAPACITY_VAL, Msg.getString("TabPanelThermalSystem.column.heat.cap"), Double.class), null);


		private HeatTableModel() {
			super(STATUS, NAME, TEMP, GENERATED, LOAD, CAPACITY);
		}

		@Override
		protected Object getEntityValue(Building building, int valueIndex) {
			ThermalGeneration heater = building.getThermalGeneration();

			return switch (valueIndex) {
				case STATUS_VAL -> (heater == null) ? null : getHeatMode(building);
				case TEMP_VAL -> Math.round(building.getCurrentTemperature() * 100.0)/100.0;
				case GENERATED_VAL -> (heater != null ? Math.round(heater.getGeneratedHeat() * 100.0)/100.0 : null);
				case LOAD_VAL -> (heater != null ? Math.round(heater.getHeatRequired() * 100.0)/100.0 : 0D);
				case CAPACITY_VAL -> {
					double generatedCapacity = 0D;
					try {
						generatedCapacity = building.getThermalGeneration().getHeatGenerationCapacity();
					}
					catch (Exception e) {
						// In case building doesn't have thermal generation, return 0 capacity.
					}
					yield generatedCapacity;
				}
				default -> super.getEntityValue(building, valueIndex);
			};
		}
		
		private static HeatMode getHeatMode(Building building) {
			var heatReq = building.getHeatRequired();
			var heatCap = building.getThermalGeneration().getHeatGenerationCapacity();	
			var percentReq = heatReq / heatCap * 100;	

			HeatMode heatMode = HeatMode.HEAT_OFF;  // Required could be negative
			for(HeatMode hm : HeatMode.values()) {
				double percentageHeat = hm.getPercentage();
				if (percentReq >= percentageHeat) {
					heatMode = hm;	
				}
			}

			return heatMode;
		}

		/**
		 * Tooltip for status column to show the heat mode name.
		 */
		@Override
		protected String getEntityDescription(Building building, int valueIndex) {
			if (valueIndex == STATUS_VAL && building.getThermalGeneration() != null) {
				return getHeatMode(building).getName();
			}
			
			return super.getEntityDescription(building, valueIndex);
		}


		public void update() {
			if (getRowCount() == 0) {
				return;
			}

			fireTableRowsUpdated(0, getRowCount()-1);
		}
	}
}
