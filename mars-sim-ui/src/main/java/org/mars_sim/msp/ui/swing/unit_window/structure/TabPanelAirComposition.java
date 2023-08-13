/*
 * Mars Simulation Project
 * TabPanelAirComposition.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.air.AirComposition;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;
import org.mars_sim.msp.ui.swing.utils.UnitModel;
import org.mars_sim.msp.ui.swing.utils.UnitTableLauncher;

/**
 * This is a tab panel for displaying the composition of air of each inhabitable building in a settlement.
 */
@SuppressWarnings("serial")
public class TabPanelAirComposition extends TabPanel {

	private static final String AIR_ICON = "air";
	private static final DecimalFormat DECIMAL_KPA = new DecimalFormat("0.00 kPa");
	private static final DecimalFormat DECIMAL_ATM = new DecimalFormat("0.0 atm");
	private static final DecimalFormat DECIMAL_MB = new DecimalFormat("0.00 mb");
	private static final DecimalFormat DECIMAL_PSI = new DecimalFormat("0.00 psi");

	private int numBuildingsCache;
	
	private double o2Cache;
	private double cO2Cache;
	private double n2Cache;
	private double h2OCache;
	private double arCache;
	private double averageTemperatureCache;

	private String indoorPressureCache;
	
	private Set<Building> buildingsCache;

	private JLabel o2Label;
	private JLabel cO2Label;
	private JLabel n2Label;
	private JLabel h2OLabel;
	private JLabel arLabel;
	private JLabel indoorPressureLabel;
	private JLabel averageTemperatureLabel;

	private JTable table ;

	private JRadioButton percent_btn;
	private JRadioButton mass_btn;
	private JRadioButton kPa_btn;
	private JRadioButton atm_btn;
	private JRadioButton psi_btn;
	private JRadioButton mb_btn;
	
	private JScrollPane scrollPane;
	
	private ButtonGroup bG;
	
	private TableModel tableModel;

	private Settlement settlement;
	private BuildingManager manager;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelAirComposition(Unit unit, MainDesktopPane desktop) {

		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(AIR_ICON),
			Msg.getString("TabPanelAirComposition.title"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;

	}

	@Override
	protected void buildUI(JPanel content) {
		
		manager = settlement.getBuildingManager();

		buildingsCache = manager.getBuildingSet(FunctionType.LIFE_SUPPORT);
		numBuildingsCache = buildingsCache.size();

		JPanel topContentPanel = new JPanel();
		topContentPanel.setLayout(new BoxLayout(topContentPanel, BoxLayout.Y_AXIS));
		content.add(topContentPanel, BorderLayout.NORTH);
		
		// Prepare the top panel using spring layout.
		AttributePanel topPanel = new AttributePanel(2);
		topContentPanel.add(topPanel);

		averageTemperatureCache = settlement.getTemperature();
		averageTemperatureLabel = topPanel.addTextField(Msg.getString("TabPanelAirComposition.label.averageTemperature.title"),
							StyleManager.DECIMAL_CELCIUS.format(averageTemperatureCache), null); //$NON-NLS-1$

		indoorPressureCache = DECIMAL_KPA.format(settlement.getAirPressure());
		indoorPressureLabel = topPanel.addTextField(Msg.getString("TabPanelAirComposition.label.indoorPressure.title"),
							indoorPressureCache, null); //$NON-NLS-1$
		
		// CO2, H2O, N2, O2, Others (Ar2, He, CH4...)
		AttributePanel gasPanel = new AttributePanel(2, 3);
		gasPanel.setBorder(StyleManager.createLabelBorder(Msg.getString("TabPanelAirComposition.label")));
		topContentPanel.add(gasPanel); 
		cO2Label = gasPanel.addTextField(Msg.getString("TabPanelAirComposition.cO2"),
											StyleManager.DECIMAL_PERC2.format(cO2Cache), null);
		arLabel = gasPanel.addTextField(Msg.getString("TabPanelAirComposition.ar"),
											StyleManager.DECIMAL_PERC2.format(arCache), null);
		n2Label = gasPanel.addTextField(Msg.getString("TabPanelAirComposition.n2"),
											StyleManager.DECIMAL_PERC2.format(n2Cache), null);
		o2Label = gasPanel.addTextField(Msg.getString("TabPanelAirComposition.o2"),
											StyleManager.DECIMAL_PERC2.format(o2Cache), null);
		h2OLabel = gasPanel.addTextField(Msg.getString("TabPanelAirComposition.h2o"),
											StyleManager.DECIMAL_PERC2.format(h2OCache), null);
		gasPanel.addTextField(null, null, null); // Add a blank to balance it out

		// Create override check box panel.
		JPanel radioPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(radioPane, BorderLayout.SOUTH);
		
		percent_btn = createSelectorButton("percent");
		mass_btn = createSelectorButton("mass");
		kPa_btn = createSelectorButton("kPa");
		atm_btn = createSelectorButton("atm");
		psi_btn = createSelectorButton("psi");
		mb_btn = createSelectorButton("mb");

		JPanel pressure_p = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		pressure_p.setBorder(BorderFactory.createTitledBorder("Pressure"));
		pressure_p.add(kPa_btn);
		pressure_p.add(atm_btn);
		pressure_p.add(mb_btn);
		pressure_p.add(psi_btn);
		radioPane.add(pressure_p);
		
		JPanel mass_p = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		mass_p.setBorder(BorderFactory.createTitledBorder("Mass "));
		mass_p.add(mass_btn);
		radioPane.add(mass_p);
    
		JPanel vol_p = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		vol_p.setBorder(BorderFactory.createTitledBorder("Vol "));
		vol_p.add(percent_btn);
		radioPane.add(vol_p);

	    percent_btn.setSelected(true);
		
	    bG = new ButtonGroup();
	    bG.add(kPa_btn);
	    bG.add(atm_btn);
	    bG.add(mb_btn);
	    bG.add(psi_btn);
	    bG.add(mass_btn);
	    bG.add(percent_btn);

	    
		// Create scroll panel for the outer table panel.
		scrollPane = new JScrollPane();
		// scrollPane.setPreferredSize(new Dimension(257, 230));
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		content.add(scrollPane,BorderLayout.CENTER);

		tableModel = new TableModel(settlement);
		table = new JTable(tableModel);
		table.addMouseListener(new UnitTableLauncher(getDesktop()));

		table.setRowSelectionAllowed(true);
		TableColumnModel tableColumnModel = table.getColumnModel();
		tableColumnModel.getColumn(0).setPreferredWidth(100);
		tableColumnModel.getColumn(1).setPreferredWidth(20);
		tableColumnModel.getColumn(2).setPreferredWidth(20);
		tableColumnModel.getColumn(3).setPreferredWidth(20);
		tableColumnModel.getColumn(4).setPreferredWidth(20);
		tableColumnModel.getColumn(5).setPreferredWidth(20);
		tableColumnModel.getColumn(6).setPreferredWidth(20);

		// Override default cell renderer for formatting double values.
		table.setDefaultRenderer(Double.class, new NumberCellRenderer(2, true));
        
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		tableColumnModel.getColumn(0).setCellRenderer(renderer);
		
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		table.setAutoCreateRowSorter(true);

		scrollPane.setViewportView(table);

		//Foruce an update to load
		update();

	}

	private JRadioButton createSelectorButton(String selector) {
	    JRadioButton btn = new JRadioButton(Msg.getString("TabPanelAirComposition.checkbox." + selector)); //$NON-NLS-1$
	    btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox." + selector + ".tooltip")); //$NON-NLS-1$
	    btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tableModel.update();
			}
		});

		return btn;
	}

	public double getOverallComposition(int gasId) {
		double result = 0;
		int size = buildingsCache.size();
		for (Building b : buildingsCache) {
			AirComposition.GasDetails gas = b.getLifeSupport().getAir().getGas(gasId);
			double percent = gas.getPercent();
			result += percent;
		}
		return (result/size);
	}

	public double getSubtotal(Building b) {
		AirComposition air = b.getLifeSupport().getAir();
		double v = air.getTotalPressure();

		if (percent_btn.isSelected()) {
			return 100D;
		}
		else if (kPa_btn.isSelected()) {
			// convert to kPascal
			return v * AirComposition.KPA_PER_ATM;
		}
		else if (atm_btn.isSelected()) {
			// convert to atm
			return v;
		}
		else if (mb_btn.isSelected()) {
			// convert to millibar
			return v * AirComposition.MB_PER_ATM;
		}
		else if (psi_btn.isSelected()) {
			// convert to psi
			return  v * AirComposition.PSI_PER_ATM;
		}

		else if (mass_btn.isSelected()) {
			return air.getTotalMass();
		}
		else
			return 0D;

	}

	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		Set<Building> buildings = manager.getBuildingSet(FunctionType.LIFE_SUPPORT);
		int numBuildings = buildings.size();

		if (numBuildings != numBuildingsCache) {
			numBuildingsCache = numBuildings;
			buildingsCache = buildings;
		}
		else {

			double cO2 = getOverallComposition(ResourceUtil.co2ID);
			if (cO2Cache != cO2) {
				cO2Cache = cO2;
				cO2Label.setText(StyleManager.DECIMAL_PERC2.format(cO2Cache));
			}

			double ar = getOverallComposition(ResourceUtil.argonID);
			if (arCache != ar) {
				arCache = ar;
				arLabel.setText(StyleManager.DECIMAL_PERC2.format(ar));
			}
			

			double n2 =  getOverallComposition(ResourceUtil.nitrogenID);
			if (n2Cache != n2) {
				n2Cache = n2;
				n2Label.setText(StyleManager.DECIMAL_PERC2.format(n2));
			}



			double o2 = getOverallComposition(ResourceUtil.oxygenID);
			if (o2Cache != o2) {
				o2Cache = o2;
				o2Label.setText(StyleManager.DECIMAL_PERC2.format(o2Cache));
			}

			double h2O = getOverallComposition(ResourceUtil.waterID);
			if (h2OCache != h2O) {
				h2OCache = h2O;
				h2OLabel.setText(StyleManager.DECIMAL_PERC2.format(h2O));
			}
			
			double averageTemperature = Math.round(settlement.getTemperature()*1000.0)/1000.0; // convert to kPascal by multiplying 1000
			if (averageTemperatureCache != averageTemperature) {
				averageTemperatureCache = averageTemperature;
				averageTemperatureLabel.setText(StyleManager.DECIMAL_CELCIUS.format(averageTemperatureCache));
			}
			
			String indoorPressure = DECIMAL_KPA.format(settlement.getAirPressure());
			
			if (kPa_btn.isSelected()) {
				// convert from atm to kPascal
				indoorPressure = DECIMAL_KPA.format(settlement.getAirPressure());
			}
			else if (atm_btn.isSelected()) {
				indoorPressure = DECIMAL_ATM.format(settlement.getAirPressure()/AirComposition.KPA_PER_ATM);
			}
			else if (mb_btn.isSelected()) {
				// convert from atm to mb
				indoorPressure = DECIMAL_MB.format(settlement.getAirPressure()/AirComposition.KPA_PER_ATM * AirComposition.MB_PER_ATM);
			}
			else if (psi_btn.isSelected()) {
				// convert from atm to kPascal
				indoorPressure =  DECIMAL_PSI.format(settlement.getAirPressure()/AirComposition.KPA_PER_ATM * AirComposition.PSI_PER_ATM);
			}
			
			if (!indoorPressureCache.equals(indoorPressure)) {
				indoorPressureCache = indoorPressure;
				indoorPressureLabel.setText(indoorPressureCache);
			}

		}

		tableModel.update();

	}

	/**
	 * Internal class used as model for the table.
	 */
	private class TableModel extends AbstractTableModel 
				implements UnitModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private int size;

		private BuildingManager manager;

		private List<Building> buildings = new ArrayList<>();

		private TableModel(Settlement settlement) {
			this.manager = settlement.getBuildingManager();
			this.buildings = selectBuildingsWithLS();
			this.size = buildings.size();
		}

		private List<Building> selectBuildingsWithLS() {
			return manager.getBuildingsWithLifeSupport();
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
			else if (columnIndex == 1) return Msg.getString("TabPanelAirComposition.column.total"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelAirComposition.cO2"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelAirComposition.ar"); //$NON-NLS-1$
			else if (columnIndex == 4) return Msg.getString("TabPanelAirComposition.n2"); //$NON-NLS-1$
			else if (columnIndex == 5) return Msg.getString("TabPanelAirComposition.o2"); //$NON-NLS-1$
			else if (columnIndex == 6) return Msg.getString("TabPanelAirComposition.h2o"); //$NON-NLS-1$

			else return null;
		}

		private int getGasId(int columnIndex) {
		 	if (columnIndex == 2) return ResourceUtil.co2ID;
			else if (columnIndex == 3) return ResourceUtil.argonID;
			else if (columnIndex == 4) return ResourceUtil.nitrogenID;
			else if (columnIndex == 5) return ResourceUtil.oxygenID;
			else if (columnIndex == 6) return ResourceUtil.waterID;

			else return -1;
		}

		public Object getValueAt(int row, int column) {

			Building b = buildings.get(row);

			// CO2, H2O, N2, O2, Others (Ar2, He, CH4...)
			if (column == 0) {
				return b.getNickName() + " ";
			}
			else if (column == 1) {
				return getSubtotal(b);
			}
			else if (column > 1) {				
				double amt = getGasValue(getGasId(column), b);
				if (amt == 0)
					return null;
				else
					return amt;
			}
			else  {
				return null;
			}
		}

		private double getGasValue(int gasId, Building b) {
			AirComposition.GasDetails gas = b.getLifeSupport().getAir().getGas(gasId);
			if (percent_btn.isSelected())
				return gas.getPercent();
			else if (kPa_btn.isSelected())
				return gas.getPartialPressure() * AirComposition.KPA_PER_ATM;
			else if (atm_btn.isSelected())
				return gas.getPartialPressure();
			else if (mb_btn.isSelected())
				return gas.getPartialPressure() * AirComposition.MB_PER_ATM;
			else if (psi_btn.isSelected())
				return gas.getPartialPressure() * AirComposition.PSI_PER_ATM;
			//else if (temperature_btn.isSelected())
			//	return air.getTemperature()[gas][id] - CompositionOfAir.C_TO_K;
			//else if (moles_btn.isSelected())
			//	return air.getNumMoles()[gas][id];
			else if (mass_btn.isSelected())
				return gas.getMass();
			else
				return 0;
		}

		public void update() {
			List<Building> newBuildings = selectBuildingsWithLS();
			if (!buildings.equals(newBuildings)) {
				buildings = newBuildings;
				scrollPane.validate();
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
		
		buildingsCache = null;
		o2Label = null;
		cO2Label = null;
		n2Label = null;
		h2OLabel = null;
		arLabel = null;
		indoorPressureLabel = null;
		averageTemperatureLabel = null;
		table = null;
		percent_btn = null;
		mass_btn = null;	
		kPa_btn = null;
		atm_btn = null;
		psi_btn = null;
		mb_btn = null;		
		scrollPane = null;		
		bG = null;	
		tableModel = null;
		settlement = null;
		manager = null;
	}
}
