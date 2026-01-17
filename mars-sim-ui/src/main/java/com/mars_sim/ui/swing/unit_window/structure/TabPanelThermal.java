/*
 * Mars Simulation Project
 * TabPanelThermal.java
 * @date 2025-07-15
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.utility.heating.ElectricHeatSource;
import com.mars_sim.core.building.utility.heating.HeatMode;
import com.mars_sim.core.building.utility.heating.HeatSource;
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
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityModel;

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

	private List<HeatSource> heatSources;
	
	private List<Building> buildings;
	
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
		buildings = manager.getBuildingsWithThermal();

		JPanel topContentPanel = new JPanel();
		topContentPanel.setLayout(new BoxLayout(topContentPanel, BoxLayout.Y_AXIS));
		
		// Prepare heat info panel.
		AttributePanel heatInfoPanel = new AttributePanel(10);
		topContentPanel.add(heatInfoPanel);

		// Prepare total heat load label.
		totHeatLoadLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, thermalSystem.getTotalHeatReq());
		heatInfoPanel.addLabelledItem(Msg.getString("TabPanelThermalSystem.totalHeatLoad"), 
				totHeatLoadLabel,
				Msg.getString("TabPanelThermalSystem.totalHeatLoad.tooltip")); //$NON-NLS-1$

		
		// Prepare total heat gen label.
		totHeatGenLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, thermalSystem.getTotalHeatGen());
		heatInfoPanel.addLabelledItem(Msg.getString("TabPanelThermalSystem.totalHeatGen"), 
					totHeatGenLabel,
					Msg.getString("TabPanelThermalSystem.totalHeatGen.tooltip")); //$NON-NLS-1$
		
		// Prepare solar heat gen label.
		heatGenSolarLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, thermalSystem.getHeatGenSolar());
		heatInfoPanel.addLabelledItem(Msg.getString("TabPanelThermalSystem.heatGenSolar"), 
					heatGenSolarLabel,
					Msg.getString("TabPanelThermalSystem.heatGenSolar.tooltip")); //$NON-NLS-1$

		// Prepare nuclear heat gen label.
		heatGenNuclearLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, thermalSystem.getHeatGenNuclear());
		heatInfoPanel.addLabelledItem(Msg.getString("TabPanelThermalSystem.heatGenNuclear"), 
					heatGenNuclearLabel,
					Msg.getString("TabPanelThermalSystem.heatGenNuclear.tooltip")); //$NON-NLS-1$

		// Prepare electric heat gen label.
		heatGenElectricLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, thermalSystem.getHeatGenElectric());
		heatInfoPanel.addLabelledItem(Msg.getString("TabPanelThermalSystem.heatGenElectric"), 
					heatGenElectricLabel,
					Msg.getString("TabPanelThermalSystem.heatGenElectric.tooltip")); //$NON-NLS-1$

		// Prepare fuel heat gen label.
		heatGenFuelLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, thermalSystem.getHeatGenFuel());
		heatInfoPanel.addLabelledItem(Msg.getString("TabPanelThermalSystem.heatGenFuel"), 
					heatGenFuelLabel,
					Msg.getString("TabPanelThermalSystem.heatGenFuel.tooltip")); //$NON-NLS-1$

		electricEffTF = new JDoubleLabel(StyleManager.DECIMAL_PERC, getAverageEfficiencyElectricHeat() * 100D);
		heatInfoPanel.addLabelledItem(Msg.getString("TabPanelThermalSystem.electricHeatingEfficiency"),
					electricEffTF,
					Msg.getString("TabPanelThermalSystem.electricHeatingEfficiency.tooltip")); //$NON-NLS-1$

		solarEffTF = new JDoubleLabel(StyleManager.DECIMAL_PERC, getAverageEfficiencySolarHeating() * 100D);
		heatInfoPanel.addLabelledItem(Msg.getString("TabPanelThermalSystem.solarHeatingEfficiency"),
					solarEffTF,	Msg.getString("TabPanelThermalSystem.solarHeatingEfficiency.tooltip")); //$NON-NLS-1$		

		// Prepare degradation rate label.
		double degradRate = SolarHeatingSource.DEGRADATION_RATE_PER_SOL;
		heatInfoPanel.addTextField(Msg.getString("TabPanelThermalSystem.degradRate"),
							StyleManager.DECIMAL_PERC.format(degradRate*100D),
							Msg.getString("TabPanelThermalSystem.degradRate.tooltip")); //$NON-NLS-1$	

		// Prepare power generated label.
		powerGenLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, thermalSystem.getTotalPowerGen());
		heatInfoPanel.addLabelledItem(Msg.getString("TabPanelThermalSystem.totalPowerGen"),
					powerGenLabel,
					Msg.getString("TabPanelThermalSystem.totalPowerGen.tooltip")); //$NON-NLS-1$

		// Create override check box panel.
		JPanel checkboxPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(checkboxPane, BorderLayout.SOUTH);
		
		// Create override check box.
		checkbox = new JCheckBox(Msg.getString("TabPanelThermalSystem.checkbox.value")); //$NON-NLS-1$
		checkbox.setToolTipText(Msg.getString("TabPanelThermalSystem.checkbox.tooltip")); //$NON-NLS-1$
		checkbox.addActionListener(e -> setNonGenerating(checkbox.isSelected()));
		checkbox.setSelected(false);
		checkboxPane.add(checkbox);
		
		return topContentPanel;
	}

	@Override
	protected TableModel createModel() {
		// Prepare thermal control table model.
		heatTableModel = new HeatTableModel();
		return heatTableModel;
	}

	@Override
	protected void setColumnDetails(TableColumnModel heatColumns) {

		heatColumns.getColumn(0).setPreferredWidth(10);
		heatColumns.getColumn(1).setPreferredWidth(130);
		heatColumns.getColumn(2).setPreferredWidth(30);
		heatColumns.getColumn(3).setPreferredWidth(55);
		heatColumns.getColumn(4).setPreferredWidth(55);
		heatColumns.getColumn(5).setPreferredWidth(40);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		heatColumns.getColumn(2).setCellRenderer(renderer);
		heatColumns.getColumn(3).setCellRenderer(renderer);
		heatColumns.getColumn(4).setCellRenderer(renderer);
		heatColumns.getColumn(5).setCellRenderer(renderer);
	}

	/**
	 * Sets if non-generating buildings should be shown.
	 * 
	 * @param value true or false.
	 */
	private void setNonGenerating(boolean value) {
		if (value)
			buildings = manager.getSortedBuildings();
		else
			buildings = manager.getBuildingsWithThermal();
		
		heatTableModel.fireTableDataChanged();
	}

	/**
	 * Gets average solar heating thermal efficiency.
	 * 
	 * @return
	 */
	public double getAverageEfficiencySolarHeating() {
		double effSolar = 0;
		int i = 0;
		Iterator<Building> iHeat = manager.getBuildingsWithThermal().iterator();
		while (iHeat.hasNext()) {
			Building building = iHeat.next();
			heatSources = building.getThermalGeneration().getHeatSources();
			Iterator<HeatSource> j = heatSources.iterator();
			while (j.hasNext()) {
				HeatSource heatSource = j.next();
				if (heatSource instanceof SolarHeatingSource source) {
					i++;
					effSolar += source.getThermalEfficiency();
				}
			}
		}
		// get the average eff
		if (i > 0) {
			effSolar = effSolar / i;
		}
		return effSolar;
	}

	/**
	 * Gets average electric heating thermal efficiency.
	 * 
	 * @return
	 */
	public double getAverageEfficiencyElectricHeat() {

		double effElectric = 0;
		int i = 0;
		Iterator<Building> iHeat = manager.getBuildingsWithThermal().iterator();
		while (iHeat.hasNext()) {
			Building building = iHeat.next();
			heatSources = building.getThermalGeneration().getHeatSources();
			Iterator<HeatSource> j = heatSources.iterator();
			while (j.hasNext()) {
				HeatSource heatSource = j.next();
				if (heatSource instanceof ElectricHeatSource source) {
					i++;
					effElectric += source.getThermalEfficiency();
				}
			}
		}
		// get the average eff
		if (i > 0) {
			effElectric = effElectric / i;
		}
		return effElectric;
		
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
		heatTableModel.update();
	}

	/**
	 * Internal class used as model for the thermal control table.
	 */
	private class HeatTableModel extends AbstractTableModel
		implements EntityModel {

		private static final Map<HeatMode, Icon> HEAT_ICONS = Map.of(
			HeatMode.HEAT_OFF, ImageLoader.getIconByName("dot/yellow"),
			HeatMode.ONE_EIGHTH_HEAT, ImageLoader.getIconByName("dot/green_quarter"),
			HeatMode.QUARTER_HEAT, ImageLoader.getIconByName("dot/green_quarter"),
			HeatMode.THREE_EIGHTH_HEAT, ImageLoader.getIconByName("dot/green_half"),
			HeatMode.HALF_HEAT, ImageLoader.getIconByName("dot/green_half"),
			HeatMode.FIVE_EIGHTH_HEAT, ImageLoader.getIconByName("dot/green_three_quarter"),
			HeatMode.THREE_QUARTER_HEAT, ImageLoader.getIconByName("dot/green_three_quarter"),
			HeatMode.SEVEN_EIGHTH_HEAT, ImageLoader.getIconByName("dot/green"),
			HeatMode.FULL_HEAT, ImageLoader.getIconByName("dot/green"),
			HeatMode.OFFLINE, ImageLoader.getIconByName("dot/red")
		);
		
		/** default serial id. */
		private static final long serialVersionUID = 1L;


		private HeatTableModel() {
		}

		@Override
		public int getRowCount() {
			return buildings.size();
		}

		@Override
		public int getColumnCount() {
			return 6;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> Icon.class;
				case 1 -> String.class;
				default -> Double.class;
			};
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> Msg.getString("TabPanelThermalSystem.column.s"); //$NON-NLS-1$
				case 1 -> Msg.getString("Building.singular"); //$NON-NLS-1$
				case 2 -> Msg.getString("TabPanelThermalSystem.column.temperature"); //$NON-NLS-1$
				case 3 -> Msg.getString("TabPanelThermalSystem.column.heat.gen"); //$NON-NLS-1$
				case 4 -> Msg.getString("TabPanelThermalSystem.column.heat.load"); //$NON-NLS-1$
				case 5 -> Msg.getString("TabPanelThermalSystem.column.heat.cap"); //$NON-NLS-1$
				default -> null;
			};
		}

		@Override
		public Object getValueAt(int row, int column) {

			Building building = buildings.get(row);
	
			ThermalGeneration heater = building.getThermalGeneration();

			// if the building has thermal control system, 
			// display the following columns: 
			
			if (column == 0) {
				
				double heatReq = building.getHeatRequired();
				double heatCap = 0.0;
				double percentReq = 0;
				
				if (heater == null) {
					return null;
				}
				else {
					heatCap = building.getThermalGeneration().getHeatGenerationCapacity();	
					percentReq = heatReq / heatCap * 100;
				}
				
				HeatMode heatMode = HeatMode.HEAT_OFF;  // Required could be negative
				for(HeatMode hm : HeatMode.values()) {
					double percentageHeat = hm.getPercentage();
					if (percentReq >= percentageHeat) {
						heatMode = hm;	
					}
				}

				return HEAT_ICONS.get(heatMode);
			}
			else if (column == 1)
				return buildings.get(row).getName();
			else if (column == 2)
				return  Math.round(building.getCurrentTemperature() * 100.0)/100.0;
			else if (column == 3) {
//				if (heatMode == HeatMode.HEAT_OFF
//					|| heatMode == HeatMode.OFFLINE) {
//					return 0.0;
//				}			

				if (heater != null) {
					return Math.round(heater.getGeneratedHeat() * 100.0)/100.0;
				}
				else
					return null;
			}
			else if (column == 4) {

				if (heater != null) {
					return Math.round(heater.getHeatRequired() * 100.0)/100.0;
				}
				else
					return 0;
			}
			else if (column == 5) {
				double generatedCapacity = 0;
				try {
					generatedCapacity = building.getThermalGeneration().getHeatGenerationCapacity();
				}
				catch (Exception e) {}
				return generatedCapacity;
			}
			return null;
		}

		public void update() {
			if (buildings.isEmpty()) {
				return;
			}

			fireTableRowsUpdated(0, buildings.size()-1);
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return buildings.get(row);
		}
	}
}
