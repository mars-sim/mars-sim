/*
 * Mars Simulation Project
 * TabPanelAirComposition.java
 * @date 2024-07-17
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.air.AirComposition;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityLauncher;
import com.mars_sim.ui.swing.utils.EntityModel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This is a tab panel for displaying the composition of air of each inhabitable building in a settlement.
 */
@SuppressWarnings("serial")
class TabPanelAirComposition extends EntityTabPanel<Settlement> implements TemporalComponent {

	private static final String BLDG = Msg.getString("Building.singular"); //$NON-NLS-1$
	private static final String TOTAL = Msg.getString("TabPanelAirComposition.column.total"); //$NON-NLS-1$
	private static final String O2 = Msg.getString("TabPanelAirComposition.o2"); //$NON-NLS-1$
	private static final String H2O = Msg.getString("TabPanelAirComposition.h2o"); //$NON-NLS-1$
	private static final String N2 = Msg.getString("TabPanelAirComposition.n2"); //$NON-NLS-1$
	private static final String CO2 = Msg.getString("TabPanelAirComposition.co2"); //$NON-NLS-1$
	private static final String AR = Msg.getString("TabPanelAirComposition.ar"); //$NON-NLS-1$
	
	private static final String AIR_ICON = "air";
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
	
	private AirTableModel airTableModel;

	private BuildingManager manager;

	/**
	 * Constructor.
		* @param unit the unit to display.
		* @param desktop the main desktop.
	 */
	public TabPanelAirComposition(Settlement unit, UIContext context) {

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelAirComposition.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(AIR_ICON), null,
			context, unit
		);
	}

	@Override
	protected void buildUI(JPanel content) {
		
		var settlement = getEntity();
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

		indoorPressureCache = StyleManager.DECIMAL_KPA.format(settlement.getAirPressure());
		indoorPressureLabel = topPanel.addTextField(Msg.getString("TabPanelAirComposition.label.indoorPressure.title"),
							indoorPressureCache, null); //$NON-NLS-1$
		
		// CO2, H2O, N2, O2, Others (Ar2, He, CH4...)
		AttributePanel gasPanel = new AttributePanel(2, 3);
		gasPanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("TabPanelAirComposition.label")));
		topContentPanel.add(gasPanel); 
		cO2Label = gasPanel.addTextField(CO2, StyleManager.DECIMAL2_PERC.format(cO2Cache), null);
		arLabel = gasPanel.addTextField(AR, StyleManager.DECIMAL2_PERC.format(arCache), null);
		n2Label = gasPanel.addTextField(N2, StyleManager.DECIMAL2_PERC.format(n2Cache), null);
		o2Label = gasPanel.addTextField(O2, StyleManager.DECIMAL2_PERC.format(o2Cache), null);
		h2OLabel = gasPanel.addTextField(H2O, StyleManager.DECIMAL2_PERC.format(h2OCache), null);
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
		pressure_p.setBorder(SwingHelper.createLabelBorder("Pressure"));
		pressure_p.add(kPa_btn);
		pressure_p.add(atm_btn);
		pressure_p.add(mb_btn);
		pressure_p.add(psi_btn);
		radioPane.add(pressure_p);
		
		JPanel mass_p = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		mass_p.setBorder(SwingHelper.createLabelBorder("Mass "));
		mass_p.add(mass_btn);
		radioPane.add(mass_p);
    
		JPanel vol_p = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		vol_p.setBorder(SwingHelper.createLabelBorder("Vol "));
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
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		content.add(scrollPane,BorderLayout.CENTER);

		airTableModel = new AirTableModel(settlement);
		table = new JTable(airTableModel);
		EntityLauncher.attach(table, getDesktop());

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
		table.setDefaultRenderer(Double.class, new NumberCellRenderer(2));
        
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		tableColumnModel.getColumn(0).setCellRenderer(renderer);
		
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		table.setAutoCreateRowSorter(true);

		scrollPane.setViewportView(table);

		//Force an update to load
		update();

	}

	private JRadioButton createSelectorButton(String selector) {
	    JRadioButton btn = new JRadioButton(Msg.getString("TabPanelAirComposition.checkbox." + selector)); //$NON-NLS-1$
	    btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox." + selector + ".tooltip")); //$NON-NLS-1$
	    btn.addActionListener(e -> airTableModel.update());
		return btn;
	}

	private double getOverallComposition(int gasId) {
		double result = 0;
		int size = buildingsCache.size();
		for (Building b : buildingsCache) {
			AirComposition.GasDetails gas = b.getLifeSupport().getAir().getGas(gasId);
			double percent = gas.getPercent();
			result += percent;
		}
		return (result/size);
	}

	private double getSubtotal(Building b) {
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

	@Override
	public void clockUpdate(ClockPulse pulse) {
		Set<Building> buildings = manager.getBuildingSet(FunctionType.LIFE_SUPPORT);
		int numBuildings = buildings.size();

		if (numBuildings != numBuildingsCache) {
			numBuildingsCache = numBuildings;
			buildingsCache = buildings;
		}
		else {
			var settlement = getEntity();

			double cO2 = getOverallComposition(ResourceUtil.CO2_ID);
			if (cO2Cache != cO2) {
				cO2Cache = cO2;
				cO2Label.setText(StyleManager.DECIMAL2_PERC.format(cO2Cache));
			}

			double ar = getOverallComposition(ResourceUtil.ARGON_ID);
			if (arCache != ar) {
				arCache = ar;
				arLabel.setText(StyleManager.DECIMAL2_PERC.format(ar));
			}

			double n2 =  getOverallComposition(ResourceUtil.NITROGEN_ID);
			if (n2Cache != n2) {
				n2Cache = n2;
				n2Label.setText(StyleManager.DECIMAL2_PERC.format(n2));
			}

			double o2 = getOverallComposition(ResourceUtil.OXYGEN_ID);
			if (o2Cache != o2) {
				o2Cache = o2;
				o2Label.setText(StyleManager.DECIMAL2_PERC.format(o2Cache));
			}

			double h2O = getOverallComposition(ResourceUtil.WATER_ID);
			if (h2OCache != h2O) {
				h2OCache = h2O;
				h2OLabel.setText(StyleManager.DECIMAL2_PERC.format(h2O));
			}
			
			double averageTemperature = settlement.getTemperature();
			if (averageTemperatureCache != averageTemperature) {
				averageTemperatureCache = averageTemperature;
				averageTemperatureLabel.setText(StyleManager.DECIMAL_CELCIUS.format(averageTemperatureCache));
			}
		
			String indoorPressure = StyleManager.DECIMAL_KPA.format(settlement.getAirPressure());
			
			if (kPa_btn.isSelected()) {
				// convert from atm to kPascal
				indoorPressure = StyleManager.DECIMAL_KPA.format(settlement.getAirPressure());
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

		airTableModel.update();
	}

	/**
	 * Internal class used as model for the table.
	 */
	private class AirTableModel extends AbstractTableModel 
				implements EntityModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private int size;

		private BuildingManager manager;

		private List<Building> buildings = new ArrayList<>();

		private AirTableModel(Settlement settlement) {
			this.manager = settlement.getBuildingManager();
			this.buildings = selectBuildingsWithLS();
			this.size = buildings.size();
		}

		private List<Building> selectBuildingsWithLS() {
			return manager.getBuildingsWithLifeSupport();
		}

		@Override
		public int getRowCount() {
			return size;
		}

		@Override
		public int getColumnCount() {
			return 7;

		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			else return Double.class;

		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> BLDG;
				case 1 -> TOTAL;
				case 2 -> O2;
				case 3 -> H2O;
				case 4 -> N2;
				case 5 -> CO2;
				case 6 -> AR;
				default -> null;
			};
		}

		private int getGasId(int columnIndex) {
		 	return switch (columnIndex) {
				case 2 -> ResourceUtil.OXYGEN_ID;
				case 3 -> ResourceUtil.WATER_ID;
				case 4 -> ResourceUtil.NITROGEN_ID;
				case 5 -> ResourceUtil.CO2_ID;
				case 6 -> ResourceUtil.ARGON_ID;
				default -> -1;
				};
		}

		@Override
		public Object getValueAt(int row, int column) {

			Building b = buildings.get(row);

			// CO2, H2O, N2, O2, Others (Ar2, He, CH4...)
			if (column == 0) {
				return b.getName();
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
		public Entity getAssociatedEntity(int row) {
			return buildings.get(row);
		}
	}
}