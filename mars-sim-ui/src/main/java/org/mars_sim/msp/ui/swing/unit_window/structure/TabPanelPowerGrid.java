/**
 * Mars Simulation Project
 * TabPanelPowerGrid.java
 * @date 2021-12-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.PowerGrid;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.PowerSource;
import org.mars_sim.msp.core.structure.building.function.PowerStorage;
import org.mars_sim.msp.core.structure.building.function.SolarPowerSource;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * This is a tab panel for a settlement's power grid information.
 */
@SuppressWarnings("serial")
public class TabPanelPowerGrid extends TabPanel {

	private static final String kW = " kW";
	private static final String kWh = " kWh";
	private static final String PERCENT_PER_SOL = " % per sol";
	private static final String PERCENT = " %";
	private static final String[] toolTips = {"Power Status", "Building Name",
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
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	private JTable powerTable;

	private JTextField powerGeneratedTF;
	private JTextField powerUsedTF;
	private JTextField energyStorageCapacityTF;
	private JTextField energyStoredTF;
	private JTextField solarCellEfficiencyTF;

	private WebScrollPane powerScrollPane;

	private WebCheckBox checkbox;

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
	public TabPanelPowerGrid(Unit unit, MainDesktopPane desktop) {

		// Use the TabPanel constructor
		super(Msg.getString("TabPanelPowerGrid.title"), //$NON-NLS-1$
				Msg.getString("TabPanelPowerGrid.label"),
				null, Msg.getString("TabPanelPowerGrid.tooltip"), //$NON-NLS-1$
				unit, desktop);

		settlement = (Settlement) unit;

	}
	
	@Override
	protected void buildUI(JPanel content) {
		powerGrid = settlement.getPowerGrid();
		manager = settlement.getBuildingManager();
		buildings = manager.getBuildingsWithPowerGeneration();

		JPanel topContentPanel = new JPanel(new BorderLayout());
		content.add(topContentPanel, BorderLayout.NORTH);

		// Prepare spring layout power info panel.
		WebPanel powerInfoPanel = new WebPanel(new SpringLayout());
		topContentPanel.add(powerInfoPanel);

		// Prepare power generated tf.
		powerGeneratedCache = powerGrid.getGeneratedPower();
		powerGeneratedTF = addTextField(powerInfoPanel, Msg.getString("TabPanelPowerGrid.totalPowerGenerated"),
										DECIMAL_PLACES1.format(powerGeneratedCache) + kW,
										Msg.getString("TabPanelPowerGrid.totalPowerGenerated.tooltip"));

		// Prepare power used tf.
		powerUsedCache = powerGrid.getRequiredPower();
		powerUsedTF = addTextField(powerInfoPanel, Msg.getString("TabPanelPowerGrid.totalPowerUsed"),
								   DECIMAL_PLACES1.format(powerUsedCache) + kW,
								   Msg.getString("TabPanelPowerGrid.totalPowerUsed.tooltip"));

		// Prepare power storage capacity tf.
		energyStorageCapacityCache = powerGrid.getStoredEnergyCapacity();
		energyStorageCapacityTF = addTextField(powerInfoPanel, Msg.getString("TabPanelPowerGrid.energyStorageCapacity"),
											   DECIMAL_PLACES1.format(energyStorageCapacityCache) + kWh,
											   Msg.getString("TabPanelPowerGrid.energyStorageCapacity.tooltip"));

		// Prepare power stored tf.
		energyStoredCache = powerGrid.getStoredEnergy();
		energyStoredTF = addTextField(powerInfoPanel, Msg.getString("TabPanelPowerGrid.totalEnergyStored"),
									  DECIMAL_PLACES1.format(energyStoredCache) + kWh,
									  Msg.getString("TabPanelPowerGrid.totalEnergyStored.tooltip"));

		// Create solar cell eff tf
		solarCellEfficiencyCache = getAverageEfficiency();
		solarCellEfficiencyTF = addTextField(powerInfoPanel, Msg.getString("TabPanelPowerGrid.solarPowerEfficiency"),
											 DECIMAL_PLACES2.format(solarCellEfficiencyCache * 100D) + PERCENT,
											 Msg.getString("TabPanelPowerGrid.solarPowerEfficiency.tooltip"));


		// Create degradation rate tf.
		double solarPowerDegradRate = SolarPowerSource.DEGRADATION_RATE_PER_SOL;
		addTextField(powerInfoPanel, Msg.getString("TabPanelPowerGrid.solarPowerDegradRate"),
									DECIMAL_PLACES2.format(solarPowerDegradRate * 100D) + PERCENT_PER_SOL,
									Msg.getString("TabPanelPowerGrid.solarPowerDegradRate.tooltip"));

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
		content.add(powerScrollPane, BorderLayout.CENTER);

		// Prepare power table model.
		powerTableModel = new PowerTableModel(settlement);

		// Prepare power table.
		powerTable = new ZebraJTable(powerTableModel);
		// SwingUtilities.invokeLater(() ->
		// ColumnResizer.adjustColumnPreferredWidths(powerTable));

		powerTable.setRowSelectionAllowed(true);
		
		powerTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		powerTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		powerTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		powerTable.getColumnModel().getColumn(3).setPreferredWidth(50);
		powerTable.getColumnModel().getColumn(4).setPreferredWidth(50);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		// powerTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		powerTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		powerTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		powerTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
		powerTable.getColumnModel().getColumn(4).setCellRenderer(renderer);
		
		// Set up tooltips for the column headers
		ToolTipHeader tooltipHeader = new ToolTipHeader(powerTable.getColumnModel());
	    tooltipHeader.setToolTipStrings(toolTips);
	    powerTable.setTableHeader(tooltipHeader);
			
		// Resizable automatically when its Panel resizes
		powerTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		// powerTable.setAutoResizeMode(WebTable.AUTO_RESIZE_ALL_COLUMNS);
		powerTable.setAutoCreateRowSorter(true);
		TableStyle.setTableStyle(powerTable);

		powerScrollPane.setViewportView(powerTable);

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
		
		TableStyle.setTableStyle(powerTable);

		// Update power generated TF
		double gen = powerGrid.getGeneratedPower();
		if (powerGeneratedCache != gen) {
			powerGeneratedCache = gen;
			powerGeneratedTF.setText(DECIMAL_PLACES1.format(powerGeneratedCache) + kW);
		}

		// Update power used TF.
		double req = powerGrid.getRequiredPower();
		if (powerUsedCache != req) {
			double average = .5 * (powerUsedCache + req);
			powerUsedCache = req;
			powerUsedTF.setText(DECIMAL_PLACES1.format(average) + kW);
		}

		// Update power storage capacity TF.
		double cap = powerGrid.getStoredEnergyCapacity();
		if (energyStorageCapacityCache != cap) {
			energyStorageCapacityCache = cap;
			energyStorageCapacityTF.setText(DECIMAL_PLACES1.format(energyStorageCapacityCache) + kWh);
		}

		// Update power stored TF.
		double store = powerGrid.getStoredEnergy();
		if (energyStoredCache != store) {
			energyStoredCache = store;
			energyStoredTF.setText(DECIMAL_PLACES1.format(energyStoredCache) + kWh);
		}

		// Update solar cell efficiency TF
		double eff = getAverageEfficiency();
		if (solarCellEfficiencyCache != eff) {
			solarCellEfficiencyCache = eff;
			solarCellEfficiencyTF.setText(DECIMAL_PLACES2.format(eff * 100D) + PERCENT);
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
			return 5;
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
	@Override
	public void destroy() {
		super.destroy();
		
		powerTable = null;
		powerGeneratedTF = null;
		powerUsedTF = null;
		energyStorageCapacityTF = null;
		energyStoredTF = null;
		solarCellEfficiencyTF = null;
		powerScrollPane = null;

		checkbox = null;
		powerTableModel = null;
		powerGrid = null;
		manager = null;
		powerSources = null;
		buildings = null;
	}
	
	// implementation code to set a tooltip text to each column of JTableHeader
	class ToolTipHeader extends JTableHeader {
		String[] toolTips;
		
		public ToolTipHeader(TableColumnModel model) {
			super(model);
		}
		
		public String getToolTipText(MouseEvent e) {
			int col = columnAtPoint(e.getPoint());
			int modelCol = getTable().convertColumnIndexToModel(col);
			String retStr;
			try {
				retStr = toolTips[modelCol];
			} catch (NullPointerException ex) {
				retStr = "";
			} catch (ArrayIndexOutOfBoundsException ex) {
				retStr = "";
			}
			if (retStr.length() < 1) {
				retStr = super.getToolTipText(e);
			}
			return retStr;
		}
		
		public void setToolTipStrings(String[] toolTips) {
			this.toolTips = toolTips;
		}
	}
}


