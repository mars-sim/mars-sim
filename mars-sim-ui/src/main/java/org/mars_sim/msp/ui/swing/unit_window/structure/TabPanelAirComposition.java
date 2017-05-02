/**
 * Mars Simulation Project
 * TabPanelTabPanelAirComposition.java
 * @version 3.1.0 2017-03-08
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.ColumnResizer;
import org.mars_sim.msp.ui.swing.tool.MultisortTableHeaderCellRenderer;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * This is a tab panel for displaying the composition of air of each inhabitable building in a settlement.
 */
public class TabPanelAirComposition
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// default logger.
	private static Logger logger = Logger.getLogger(TabPanelAirComposition.class.getName());

	// Data cache
	private int numBuildingsCache;
	private double o2Cache, cO2Cache, n2Cache, h2OCache, othersCache, totalPressureCache;

	private List<Building> buildingsCache;

	private JLabel o2Label, cO2Label, n2Label, h2OLabel, othersLabel, totalPressureLabel;

	private JTable table ;

	private TableModel tableModel;

	private DecimalFormat fmt3 = new DecimalFormat(Msg.getString("decimalFormat3")); //$NON-NLS-1$
	private DecimalFormat fmt2 = new DecimalFormat(Msg.getString("decimalFormat2")); //$NON-NLS-1$
	private DecimalFormat fmt1 = new DecimalFormat(Msg.getString("decimalFormat1")); //$NON-NLS-1$

	private Settlement settlement;
	private BuildingConfig config;
	private BuildingManager manager;
	private CompositionOfAir air;


	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelAirComposition(Unit unit, MainDesktopPane desktop) {

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelAirComposition.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelAirComposition.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;
		manager = settlement.getBuildingManager();
		air = settlement.getCompositionOfAir();
		config = SimulationConfig.instance().getBuildingConfiguration();

		buildingsCache = manager.getBuildingsWithLifeSupport();
		numBuildingsCache = buildingsCache.size();

		// Prepare heating System label panel.
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(labelPanel);

		JLabel label = new JLabel(Msg.getString("TabPanelAirComposition.title"), JLabel.CENTER); //$NON-NLS-1$
		label.setFont(new Font("Serif", Font.BOLD, 16));
		labelPanel.add(label);

		// Prepare heat info panel.
		JPanel infoPanel = new JPanel(new GridLayout(9, 1, 0, 0));
		infoPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(infoPanel);

		totalPressureCache = settlement.getAirPressure()/1000D; // convert to kPascal by multiplying 1000
		totalPressureLabel = new JLabel(Msg.getString("TabPanelAirComposition.label.totalPressure", fmt2.format(totalPressureCache)), JLabel.CENTER); //$NON-NLS-1$
		infoPanel.add(totalPressureLabel);

		// add an empty label for separation
		infoPanel.add(new JLabel(""));

		// CO2, H2O, N2, O2, Others (Ar2, He, CH4...)

		infoPanel.add(new JLabel(Msg.getString("TabPanelAirComposition.label"), JLabel.CENTER)); //$NON-NLS-1$

		cO2Cache = getComposition(0);
		cO2Label = new JLabel(Msg.getString("TabPanelAirComposition.label.cO2", fmt3.format(cO2Cache)), JLabel.CENTER); //$NON-NLS-1$
		infoPanel.add(cO2Label);

		h2OCache = getComposition(1);
		h2OLabel = new JLabel(Msg.getString("TabPanelAirComposition.label.h2O", fmt2.format(h2OCache)), JLabel.CENTER); //$NON-NLS-1$
		infoPanel.add(h2OLabel);

		n2Cache = getComposition(2);
		n2Label = new JLabel(Msg.getString("TabPanelAirComposition.label.n2", fmt1.format(n2Cache)), JLabel.CENTER); //$NON-NLS-1$
		infoPanel.add(n2Label);

		o2Cache = getComposition(3);
		o2Label = new JLabel(Msg.getString("TabPanelAirComposition.label.o2", fmt2.format(o2Cache)), JLabel.CENTER); //$NON-NLS-1$
		infoPanel.add(o2Label);

		othersCache = getComposition(4);
		othersLabel = new JLabel(Msg.getString("TabPanelAirComposition.label.others", fmt2.format(othersCache)), JLabel.CENTER); //$NON-NLS-1$
		infoPanel.add(othersLabel);

		// add an empty label for separation
		infoPanel.add(new JLabel(""));

		// Create scroll panel for the outer table panel.
		JScrollPane scrollPane = new JScrollPane();
		// scrollPane.setPreferredSize(new Dimension(257, 230));
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		centerContentPanel.add(scrollPane,BorderLayout.CENTER);

		tableModel = new TableModel(settlement);
		table = new ZebraJTable(tableModel);
	    SwingUtilities.invokeLater(() -> ColumnResizer.adjustColumnPreferredWidths(table));

		table.setCellSelectionEnabled(false);
		table.setDefaultRenderer(Double.class, new NumberCellRenderer());
		table.getColumnModel().getColumn(0).setPreferredWidth(55);
		table.getColumnModel().getColumn(1).setPreferredWidth(35);
		table.getColumnModel().getColumn(2).setPreferredWidth(25);
		table.getColumnModel().getColumn(3).setPreferredWidth(20);
		table.getColumnModel().getColumn(4).setPreferredWidth(20);
		table.getColumnModel().getColumn(5).setPreferredWidth(20);
		table.getColumnModel().getColumn(6).setPreferredWidth(20);

		table.setPreferredScrollableViewportSize(new Dimension(225, -1));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		table.setAutoCreateRowSorter(true);
		//if (!MainScene.OS.equals("linux")) {
		//	table.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		//}

		TableStyle.setTableStyle(table);

		scrollPane.setViewportView(table);

	}

	public double getComposition(int gas) {
		double result = 0;
		//List<Building> buildings = manager.getBuildingsWithLifeSupport();
		int size = buildingsCache.size();
		Iterator<Building> k = buildingsCache.iterator();
		while (k.hasNext()) {
			Building b = k.next();
			int id = b.getInhabitableID();
			double [][] vol = air.getPercentComposition();
			//System.out.println("vol.length : " + vol.length + "  vol[].length : " + vol[0].length);
			double percent = vol[gas][id];
			result += percent;
		}
		return result/size;
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {

		List<Building> buildings = manager.getBuildingsWithLifeSupport();//getBuildings(BuildingFunction.LIFE_SUPPORT);
		int numBuildings = buildings.size();

		if (numBuildings != numBuildingsCache) {
			numBuildingsCache = numBuildings;
			buildingsCache = buildings;
		}
		else {

			double o2 = getComposition(3);
			if (o2Cache != o2) {
				o2Cache = o2;
				o2Label.setText(
					Msg.getString("TabPanelAirComposition.label.o2", //$NON-NLS-1$
					fmt2.format(o2Cache)
					));
			}

			double cO2 = getComposition(0);
			if (cO2Cache != cO2) {
				cO2Cache = cO2;
				cO2Label.setText(
					Msg.getString("TabPanelAirComposition.label.cO2", //$NON-NLS-1$
					fmt3.format(cO2Cache)
					));
			}

			double h2O = getComposition(1);
			if (h2OCache != h2O) {
				h2OCache = h2O;
				h2OLabel.setText(
					Msg.getString("TabPanelAirComposition.label.h2O",  //$NON-NLS-1$
					fmt2.format(h2O)
					));
			}

			double n2 =  getComposition(2);
			if (n2Cache != n2) {
				n2Cache = n2;
				n2Label.setText(
					Msg.getString("TabPanelAirComposition.label.n2",  //$NON-NLS-1$
					fmt1.format(n2)
					));
			}

			double others = getComposition(4);
			if (othersCache != others) {
				othersCache = others;
				othersLabel.setText(
					Msg.getString("TabPanelAirComposition.label.others",  //$NON-NLS-1$
					fmt2.format(others)
					));
			}


			double totalPressure = settlement.getAirPressure()/1000D; // convert to kPascal by multiplying 1000
			if (totalPressureCache != totalPressure) {
				totalPressureCache = totalPressure;
				totalPressureLabel.setText(
					Msg.getString("TabPanelAirComposition.label.totalPressure",  //$NON-NLS-1$
					fmt2.format(totalPressureCache)//Math.round(totalPressureCache*10D)/10D
					));
			}

		}

		tableModel.update();
		TableStyle.setTableStyle(table);

	}

	/**
	 * Internal class used as model for the table.
	 */
	private static class TableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private int size;

		private Settlement settlement;
		private BuildingManager manager;

		private List<Building> buildingsWithLS = new ArrayList<>();;

		private CompositionOfAir air;

		private DecimalFormat fmt3 = new DecimalFormat(Msg.getString("decimalFormat3")); //$NON-NLS-1$
		private DecimalFormat fmt2 = new DecimalFormat(Msg.getString("decimalFormat2")); //$NON-NLS-1$
		private DecimalFormat fmt1 = new DecimalFormat(Msg.getString("decimalFormat1")); //$NON-NLS-1$

		private TableModel(Settlement settlement) {
			this.settlement = settlement;
			this.manager = settlement.getBuildingManager();
			this.air = settlement.getCompositionOfAir();
			this.buildingsWithLS = selectBuildingsWithLS();
			this.size = buildingsWithLS.size();

		}

		public List<Building> selectBuildingsWithLS() {
			return settlement.getBuildingManager().getBuildingsWithLifeSupport();
		}

		public int getRowCount() {
			return size;
		}

		public int getColumnCount() {
			return 7;

		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;//ImageIcon.class;
			else if (columnIndex == 1) dataType = Double.class;
			else if (columnIndex == 2) dataType = Double.class;
			else if (columnIndex == 3) dataType = Double.class;
			else if (columnIndex == 4) dataType = Double.class;
			else if (columnIndex == 5) dataType = Double.class;
			else if (columnIndex == 6) dataType = Double.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelAirComposition.column.buildingName"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelAirComposition.column.pressure"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelAirComposition.column.cO2"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelAirComposition.column.h2O"); //$NON-NLS-1$
			else if (columnIndex == 4) return Msg.getString("TabPanelAirComposition.column.n2"); //$NON-NLS-1$
			else if (columnIndex == 5) return Msg.getString("TabPanelAirComposition.column.o2"); //$NON-NLS-1$
			else if (columnIndex == 6) return Msg.getString("TabPanelAirComposition.column.others"); //$NON-NLS-1$

			else return null;
		}

		public Object getValueAt(int row, int column) {

			Building b = manager.getInhabitableBuilding(row);

			// CO2, H2O, N2, O2, Others (Ar2, He, CH4...)
			if (column == 0) {
				return b.getNickName();
			}
			else if (column == 1) {
				//return air.getTotalPressure()[row]* CompositionOfAir.kPASCAL_PER_ATM;
				return getTotalPressure(row);
			}
			else if (column > 1) {
				double amt = getComposition(column - 2);
				if (column == 2)
					return fmt3.format(amt);
				else if (column == 3 || column == 5 || column == 6)
					return fmt2.format(amt);
				else if (column == 4 )
					return fmt1.format(amt);
				else
					return null;
			}
			else  {
				return null;
			}

		}

		public double getComposition(int gas) {
			double result = 0;
			Iterator<Building> k = buildingsWithLS.iterator();
			while (k.hasNext()) {
				Building b = k.next();
				int id = b.getInhabitableID();
				double [][] vol = air.getPercentComposition();
				double percent = 0;
				if (id < vol[0].length)
					percent = vol[gas][id];
				else
					percent = 0;
				result += percent;
			}
			return result/size;
		}


		public double getTotalPressure(int row) {
			double [] tp = air.getTotalPressure();
			double p = 0;
			if (row < tp.length)
				p = tp[row];
			else
				p = 0;
			// convert from atm to kPascal
			return p * CompositionOfAir.kPASCAL_PER_ATM;
		}

		public void update() {
			//List<Building> newBuildings = selectBuildingsWithLS();
			//if (!buildingsWithLS.equals(newBuildings)) {
			//	Collections.sort(buildingsWithLS);
			//	buildingsWithLS = newBuildings;
			//}

			int newSize = buildingsWithLS.size();
			if (size != newSize) {
				size = newSize;
				buildingsWithLS = selectBuildingsWithLS();
				Collections.sort(buildingsWithLS);
			}
			fireTableDataChanged();
		}
	}
}