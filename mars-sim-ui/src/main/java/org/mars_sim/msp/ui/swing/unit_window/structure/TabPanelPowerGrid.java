/**
 * Mars Simulation Project
 * TabPanelPowerGrid.java
 * @version 3.1.0 2017-02-14
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.PowerGrid;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.PowerSource;
import org.mars_sim.msp.core.structure.building.function.SolarPowerSource;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;
import com.jidesoft.swing.SearchableUtils;
import com.jidesoft.swing.TableSearchable;

/**
 * This is a tab panel for a settlement's power grid information.
 */
@SuppressWarnings("serial")
public class TabPanelPowerGrid extends TabPanel {

	private static final String kW = " kW";
	private static final String kWh = " kWh";
	private static final String PERCENT_PER_SOL = " % per sol";
	private static final String PERCENT = " %";

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
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	private JTable powerTable;
	/** The total power generated label. */
	private WebLabel powerGeneratedLabel;
	/** The total power used label. */
	private WebLabel powerUsedLabel;
	/** The total power storage capacity label. */
	private WebLabel energyStorageCapacityLabel;
	/** The total power stored label. */
	private WebLabel energyStoredLabel;
	/** The power efficiency label. */
	private WebLabel electricEfficiencyLabel;

	private WebTextField powerGeneratedTF;
	private WebTextField powerUsedTF;
	private WebTextField energyStorageCapacityTF;
	private WebTextField energyStoredTF;
	private WebTextField solarCellEfficiencyTF;
	private WebTextField degradRateTF;

	private WebScrollPane powerScrollPane;

	private WebCheckBox checkbox;

	/** Table model for power info. */
	private PowerTableModel powerTableModel;
	/** The settlement's power grid. */
	private PowerGrid powerGrid;

	private BuildingConfig config;

	private BuildingManager manager;

	private List<PowerSource> powerSources;

	private List<Building> buildings;

	private static DecimalFormat formatter = new DecimalFormat(Msg.getString("TabPanelPowerGrid.decimalFormat")); //$NON-NLS-1$
	private static DecimalFormat formatter2 = new DecimalFormat(Msg.getString("decimalFormat2")); //$NON-NLS-1$
//	private static DecimalFormat formatter3 = new DecimalFormat(Msg.getString("decimalFormat3")); //$NON-NLS-1$

	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelPowerGrid(Unit unit, MainDesktopPane desktop) {

		// Use the TabPanel constructor
		super(Msg.getString("TabPanelPowerGrid.title"), //$NON-NLS-1$
				null, Msg.getString("TabPanelPowerGrid.tooltip"), //$NON-NLS-1$
				unit, desktop);

		settlement = (Settlement) unit;

	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		powerGrid = settlement.getPowerGrid();
		manager = settlement.getBuildingManager();
		config = SimulationConfig.instance().getBuildingConfiguration();
		buildings = manager.getBuildingsWithPowerGeneration();

		// Prepare power grid label panel.
		WebPanel powerGridLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(powerGridLabelPanel);

		// Prepare power grid label.
		WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelPowerGrid.label"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		// titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		powerGridLabelPanel.add(titleLabel);

		// Prepare spring layout power info panel.
		WebPanel powerInfoPanel = new WebPanel(new SpringLayout());// GridLayout(6, 2, 0, 0));
//		powerInfoPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(powerInfoPanel);

		// Prepare power generated label.
		powerGeneratedCache = powerGrid.getGeneratedPower();
		powerGeneratedLabel = new WebLabel(Msg.getString("TabPanelPowerGrid.totalPowerGenerated"), WebLabel.RIGHT); //$NON-NLS-1$
		powerGeneratedLabel.setToolTipText(Msg.getString("TabPanelPowerGrid.totalPowerGenerated.tooltip")); //$NON-NLS-1$
		powerInfoPanel.add(powerGeneratedLabel);

		WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		powerGeneratedTF = new WebTextField(formatter.format(powerGeneratedCache) + kW);
		powerGeneratedTF.setEditable(false);
		powerGeneratedTF.setPreferredSize(new Dimension(120, 24));// setColumns(20);
		wrapper1.add(powerGeneratedTF);
		powerInfoPanel.add(wrapper1);

		// Prepare power used label.
		powerUsedCache = powerGrid.getRequiredPower();
		powerUsedLabel = new WebLabel(Msg.getString("TabPanelPowerGrid.totalPowerUsed"), WebLabel.RIGHT); //$NON-NLS-1$
		powerUsedLabel.setToolTipText(Msg.getString("TabPanelPowerGrid.totalPowerUsed.tooltip")); //$NON-NLS-1$
		powerInfoPanel.add(powerUsedLabel);

		WebPanel wrapper2 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		powerUsedTF = new WebTextField(formatter.format(powerUsedCache) + kW);
		powerUsedTF.setEditable(false);
		powerUsedTF.setPreferredSize(new Dimension(120, 24));// setColumns(20);
		wrapper2.add(powerUsedTF);
		powerInfoPanel.add(wrapper2);

		// Prepare power storage capacity label.
		energyStorageCapacityCache = powerGrid.getStoredEnergyCapacity();
		energyStorageCapacityLabel = new WebLabel(Msg.getString("TabPanelPowerGrid.energyStorageCapacity"), //$NON-NLS-1$
				WebLabel.RIGHT);
		energyStorageCapacityLabel.setToolTipText(Msg.getString("TabPanelPowerGrid.energyStorageCapacity.tooltip")); //$NON-NLS-1$
		powerInfoPanel.add(energyStorageCapacityLabel);

		WebPanel wrapper3 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		energyStorageCapacityTF = new WebTextField(formatter.format(energyStorageCapacityCache) + kWh);
		energyStorageCapacityTF.setEditable(false);
		energyStorageCapacityTF.setPreferredSize(new Dimension(120, 24));// setColumns(20);
		wrapper3.add(energyStorageCapacityTF);
		powerInfoPanel.add(wrapper3);

		// Prepare power stored label.
		energyStoredCache = powerGrid.getStoredEnergy();
		energyStoredLabel = new WebLabel(Msg.getString("TabPanelPowerGrid.totalEnergyStored"), WebLabel.RIGHT); //$NON-NLS-1$
		energyStoredLabel.setToolTipText(Msg.getString("TabPanelPowerGrid.totalEnergyStored.tooltip")); //$NON-NLS-1$
		powerInfoPanel.add(energyStoredLabel);

		WebPanel wrapper4 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		energyStoredTF = new WebTextField(formatter.format(energyStoredCache) + kWh);
		energyStoredTF.setEditable(false);
		energyStoredTF.setPreferredSize(new Dimension(120, 24));// setColumns(20);
		wrapper4.add(energyStoredTF);
		powerInfoPanel.add(wrapper4);

		// 2015-05-08 Added eff_electric_label
		solarCellEfficiencyCache = getAverageEfficiency();
		electricEfficiencyLabel = new WebLabel(Msg.getString("TabPanelPowerGrid.solarPowerEfficiency"), WebLabel.RIGHT); //$NON-NLS-1$
		electricEfficiencyLabel.setToolTipText(Msg.getString("TabPanelPowerGrid.solarPowerEfficiency.tooltip"));
		// ("<html><p width=\"300\">Note: the Shockley-Quiesser theoretical limit for a
		// single junction solar cell is only 33.7%. "
		// + "For a tandem structure or multi-junction p-n cells, the limit can be as
		// high as ~68% for unconcentrated sunlight.</p></html>");
		powerInfoPanel.add(electricEfficiencyLabel);

		WebPanel wrapper5 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		solarCellEfficiencyTF = new WebTextField(formatter2.format(solarCellEfficiencyCache * 100D) + PERCENT);
		solarCellEfficiencyTF.setEditable(false);
		solarCellEfficiencyTF.setPreferredSize(new Dimension(120, 24));// setColumns(20);
		wrapper5.add(solarCellEfficiencyTF);
		powerInfoPanel.add(wrapper5);

		// 2015-05-08 Added degradation rate label.
		double solarPowerDegradRate = SolarPowerSource.DEGRADATION_RATE_PER_SOL;
		WebLabel solarPowerDegradRateLabel = new WebLabel(Msg.getString("TabPanelPowerGrid.solarPowerDegradRate"), //$NON-NLS-1$
				WebLabel.RIGHT);
		solarPowerDegradRateLabel.setToolTipText(Msg.getString("TabPanelPowerGrid.solarPowerDegradRate.tooltip"));
		powerInfoPanel.add(solarPowerDegradRateLabel);

		WebPanel wrapper6 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		degradRateTF = new WebTextField(formatter2.format(solarPowerDegradRate * 100D) + PERCENT_PER_SOL);
		degradRateTF.setEditable(false);
		degradRateTF.setPreferredSize(new Dimension(120, 24));// setColumns(20);
		wrapper6.add(degradRateTF);
		powerInfoPanel.add(wrapper6);

		// Create override check box panel.
		WebPanel checkboxPane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(checkboxPane, BorderLayout.SOUTH);

		// Create override check box.
		checkbox = new WebCheckBox(Msg.getString("TabPanelPowerGrid.checkbox.value")); //$NON-NLS-1$
		checkbox.setToolTipText(Msg.getString("TabPanelPowerGrid.checkbox.tooltip")); //$NON-NLS-1$
		checkbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setNonGenerating(checkbox.isSelected());
			}
		});
		checkbox.setSelected(false);
		checkboxPane.add(checkbox);

		// Create scroll panel for the outer table panel.
		powerScrollPane = new WebScrollPane();
		// powerScrollPane.setPreferredSize(new Dimension(257, 230));
		// increase vertical mousewheel scrolling speed for this one
		powerScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		powerScrollPane.setHorizontalScrollBarPolicy(WebScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		centerContentPanel.add(powerScrollPane, BorderLayout.CENTER);

		// Prepare power table model.
		powerTableModel = new PowerTableModel(settlement);

		// Prepare power table.
		powerTable = new ZebraJTable(powerTableModel);
		// SwingUtilities.invokeLater(() ->
		// ColumnResizer.adjustColumnPreferredWidths(powerTable));

		powerTable.setRowSelectionAllowed(true);
		
		powerTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		powerTable.getColumnModel().getColumn(1).setPreferredWidth(130);
		powerTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		powerTable.getColumnModel().getColumn(3).setPreferredWidth(50);

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		// powerTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		powerTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		powerTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		powerTable.getColumnModel().getColumn(3).setCellRenderer(renderer);

		// Resizable automatically when its Panel resizes
		powerTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		// powerTable.setAutoResizeMode(WebTable.AUTO_RESIZE_ALL_COLUMNS);
		powerTable.setAutoCreateRowSorter(true);
		TableStyle.setTableStyle(powerTable);

		powerScrollPane.setViewportView(powerTable);

		// 2015-06-17 Added resourcesSearchable
		TableSearchable searchable = SearchableUtils.installSearchable(powerTable);
		searchable.setPopupTimeout(5000);
		searchable.setCaseSensitive(false);

		// Lay out the spring panel.
		SpringUtilities.makeCompactGrid(powerInfoPanel, 6, 2, // rows, cols
				20, 10, // initX, initY
				10, 1); // xPad, yPad

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
			buildings = manager.getBuildingsWithPowerGeneration();
		powerTableModel.update();
	}

	/**
	 * Gets a list of buildings should be shown.
	 * 
	 * @return a list of buildings
	 */
	private List<Building> getBuildings() {
		if (checkbox.isSelected())
			return manager.getSortedBuildings();
		else
			return manager.getBuildingsWithPowerGeneration();
	}

	public double getAverageEfficiency() {
		double eff = 0;
		int i = 0;
		Iterator<Building> iPower = manager.getBuildingsWithPowerGeneration().iterator();
		while (iPower.hasNext()) {
			Building building = iPower.next();
			powerSources = config.getPowerSources(building.getBuildingType());
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
		eff = eff / i;
		return eff;
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		if (!uiDone)
			initializeUI();
		
		TableStyle.setTableStyle(powerTable);

		// Update power generated TF
		double gen = powerGrid.getGeneratedPower();
		if (powerGeneratedCache != gen) {
			powerGeneratedCache = gen;
			powerGeneratedTF.setText(formatter.format(powerGeneratedCache) + kW);
		}

		// Update power used TF.
		double req = powerGrid.getRequiredPower();
		if (powerUsedCache != req) {
			double average = .5 * (powerUsedCache + req);
			powerUsedCache = req;
			powerUsedTF.setText(formatter.format(average) + kW);
		}

		// Update power storage capacity TF.
		double cap = powerGrid.getStoredEnergyCapacity();
		if (energyStorageCapacityCache != cap) {
			energyStorageCapacityCache = cap;
			energyStorageCapacityTF.setText(formatter.format(energyStorageCapacityCache) + kWh);
		}

		// Update power stored TF.
		double store = powerGrid.getStoredEnergy();
		if (energyStoredCache != store) {
			energyStoredCache = store;
			energyStoredTF.setText(formatter.format(energyStoredCache) + kWh);
		}

		// Update solar cell efficiency TF
		double eff = getAverageEfficiency();
		if (solarCellEfficiencyCache != eff) {
			solarCellEfficiencyCache = eff;
			solarCellEfficiencyTF.setText(formatter2.format(eff * 100D) + PERCENT);
		}
		// Update power table.
		powerTableModel.update();
	}

	/**
	 * Internal class used as model for the power table.
	 */
	private class PowerTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

//		private Settlement settlement;
		private ImageIcon dotRed;
		private ImageIcon dotYellow;
		private ImageIcon dotGreen;

//		private int size;

		private PowerTableModel(Settlement settlement) {
//			this.settlement = settlement;

			dotRed = ImageLoader.getIcon(Msg.getString("img.dotRed")); //$NON-NLS-1$
			dotYellow = ImageLoader.getIcon(Msg.getString("img.dotYellow")); //$NON-NLS-1$
			dotGreen = ImageLoader.getIcon(Msg.getString("img.dotGreen_full")); //$NON-NLS-1$

			// size = getBuildings().size();
		}

		public int getRowCount() {
			return buildings.size();
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0)
				dataType = ImageIcon.class;
			else if (columnIndex == 1)
				dataType = Object.class;
			else if (columnIndex == 2)
				dataType = Double.class;
			else if (columnIndex == 3)
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
				return null;
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
			} else if (column == 1)
				return buildings.get(row) + " ";
			else if (column == 2) {
				double generated = 0D;
				if (building.hasFunction(FunctionType.POWER_GENERATION)) {
					try {
						// PowerGeneration generator = (PowerGeneration)
						// building.getFunction(BuildingFunction.POWER_GENERATION);
						generated = building.getPowerGeneration().getGeneratedPower();
					} catch (Exception e) {
					}
				}
				if (building.hasFunction(FunctionType.THERMAL_GENERATION)) {
					try {
						// ThermalGeneration heater = (ThermalGeneration)
						// building.getFunction(BuildingFunction.THERMAL_GENERATION);
						generated += building.getThermalGeneration().getGeneratedPower();
					} catch (Exception e) {
					}
				}
				return Math.round(generated * 1000.0) / 1000.0;
			} else if (column == 3) {
				double used = 0D;
				if (powerMode == PowerMode.FULL_POWER)
					used = building.getFullPowerRequired();
				else if (powerMode == PowerMode.POWER_DOWN)
					used = building.getPoweredDownPowerRequired();
				return Math.round(used * 1000.0) / 1000.0;
			} else
				return null;
		}

		public void update() {
			// Check if building list has changed.
			List<Building> tempBuildings = getBuildings();
			if (!tempBuildings.equals(buildings)) {
				buildings = tempBuildings;
				powerScrollPane.validate();
			}
			/*
			 * int newSize = buildings.size(); if (size != newSize) { size = newSize;
			 * buildings =
			 * settlement.getBuildingManager().getBuildingsWithPowerGeneration();
			 * //Collections.sort(buildings); } else { List<Building> newBuildings =
			 * settlement.getBuildingManager().getACopyOfBuildings(); if
			 * (!buildings.equals(newBuildings)) { buildings = newBuildings;
			 * //Collections.sort(buildings); } }
			 */
			fireTableDataChanged();
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		powerTable = null;
		powerGeneratedLabel = null;
		powerUsedLabel = null;
		energyStorageCapacityLabel = null;
		energyStoredLabel = null;
		electricEfficiencyLabel = null;
		powerGeneratedTF = null;
		powerUsedTF = null;
		energyStorageCapacityTF = null;
		energyStoredTF = null;
		solarCellEfficiencyTF = null;
		degradRateTF = null;
		powerScrollPane = null;

		checkbox = null;
		formatter = null;
		formatter2 = null;
//		formatter3 = null;
		powerTableModel = null;
		powerGrid = null;
		config = null;
		manager = null;
		powerSources = null;
		buildings = null;
	}
}