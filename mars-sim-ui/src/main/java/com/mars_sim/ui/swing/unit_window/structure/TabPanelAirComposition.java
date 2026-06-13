/*
 * Mars Simulation Project
 * TabPanelAirComposition.java
 * @date 2024-07-17
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

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
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.model.BaseBuildingModel;

/**
 * This is a tab panel for displaying the composition of air of each inhabitable building in a settlement.
 */
@SuppressWarnings("serial")
class TabPanelAirComposition extends EntityTabPanel<Settlement> implements TemporalComponent {

	private static final String O2_NAME = Msg.getString("TabPanelAirComposition.o2"); //$NON-NLS-1$
	private static final String H2O_NAME = Msg.getString("TabPanelAirComposition.h2o"); //$NON-NLS-1$
	private static final String N2_NAME = Msg.getString("TabPanelAirComposition.n2"); //$NON-NLS-1$
	private static final String CO2_NAME = Msg.getString("TabPanelAirComposition.co2"); //$NON-NLS-1$
	private static final String AR_NAME = Msg.getString("TabPanelAirComposition.ar"); //$NON-NLS-1$
	
	private static final String AIR_ICON = "air";
	private static final DecimalFormat DECIMAL_ATM = new DecimalFormat("0.0 atm");
	private static final DecimalFormat DECIMAL_MB = new DecimalFormat("0.00 mb");
	private static final DecimalFormat DECIMAL_PSI = new DecimalFormat("0.00 psi");

	private int numBuildingsCache;
	
	private String indoorPressureCache;
	
	private Set<Building> buildingsCache;

	private JDoubleLabel o2Label;
	private JDoubleLabel cO2Label;
	private JDoubleLabel n2Label;
	private JDoubleLabel h2OLabel;
	private JDoubleLabel arLabel;
	private JLabel indoorPressureLabel;
	private JDoubleLabel averageTemperatureLabel;

	private static final int SHOW_PERC = 1;
	private static final int SHOW_KPA = 2;
	private static final int SHOW_ATM = 3;
	private static final int SHOW_MB = 4;
	private static final int SHOW_PSI = 5;
	private static final int SHOW_MASS = 6;
	private int styleSelected = SHOW_PERC;

	private AirTableModel airTableModel;

	private BuildingManager manager;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param context the main desktop.
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
		AttributePanel topPanel = new AttributePanel();
		topContentPanel.add(topPanel);

		averageTemperatureLabel = new JDoubleLabel(StyleManager.DECIMAL_CELCIUS, settlement.getTemperature());
		topPanel.addLabelledItem(Msg.getString("TabPanelAirComposition.label.averageTemperature.title"), averageTemperatureLabel, null);
		indoorPressureCache = StyleManager.DECIMAL_KPA.format(settlement.getAirPressure());
		indoorPressureLabel = topPanel.addTextField(Msg.getString("TabPanelAirComposition.label.indoorPressure.title"),
							indoorPressureCache, null);
									
		// CO2, H2O, N2, O2, Others (Ar2, He, CH4...)
		AttributePanel gasPanel = new AttributePanel(2, 3);
		gasPanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("TabPanelAirComposition.label")));
		topContentPanel.add(gasPanel); 
		cO2Label = new JDoubleLabel(StyleManager.DECIMAL2_PERC, getOverallComposition(ResourceUtil.CO2_ID));
		gasPanel.addLabelledItem(CO2_NAME, cO2Label, null);
		arLabel = new JDoubleLabel(StyleManager.DECIMAL2_PERC, getOverallComposition(ResourceUtil.ARGON_ID));
		gasPanel.addLabelledItem(AR_NAME, arLabel, null);
		n2Label = new JDoubleLabel(StyleManager.DECIMAL2_PERC, getOverallComposition(ResourceUtil.NITROGEN_ID));
		gasPanel.addLabelledItem(N2_NAME, n2Label, null);
		o2Label = new JDoubleLabel(StyleManager.DECIMAL2_PERC, getOverallComposition(ResourceUtil.OXYGEN_ID));
		gasPanel.addLabelledItem(O2_NAME, o2Label, null);
		h2OLabel = new JDoubleLabel(StyleManager.DECIMAL2_PERC, getOverallComposition(ResourceUtil.WATER_ID));
		gasPanel.addLabelledItem(H2O_NAME, h2OLabel, null);
		gasPanel.addBlankField();

		// Create override check box panel.
		topContentPanel.add(createSelectionPanel(), BorderLayout.SOUTH);
 
		// Create scroll panel for the outer table panel.
		airTableModel = new AirTableModel(settlement);
		var table = SwingHelper.createScrolledTable(airTableModel, getContext(), null, null);
		content.add(table ,BorderLayout.CENTER);

		//Force an update to load
		clockUpdate(null);
	}

	private JPanel createSelectionPanel() {
		JPanel radioPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		var percentBtn = createSelectorButton("percent", SHOW_PERC);
		var massBtn = createSelectorButton("mass", SHOW_MASS);
		var kPaBtn = createSelectorButton("kPa", SHOW_KPA);
		var atmBtn = createSelectorButton("atm", SHOW_ATM);
		var psiBtn = createSelectorButton("psi", SHOW_PSI);
		var mbBtn = createSelectorButton("mb", SHOW_MB);

		JPanel pressurePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		pressurePanel.setBorder(SwingHelper.createLabelBorder("Pressure"));
		pressurePanel.add(kPaBtn);
		pressurePanel.add(atmBtn);
		pressurePanel.add(mbBtn);
		pressurePanel.add(psiBtn);
		radioPane.add(pressurePanel);
		
		JPanel massPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		massPanel.setBorder(SwingHelper.createLabelBorder("Mass "));
		massPanel.add(massBtn);
		radioPane.add(massPanel);
    
		JPanel volPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		volPanel.setBorder(SwingHelper.createLabelBorder("Vol "));
		volPanel.add(percentBtn);
		radioPane.add(volPanel);

	    percentBtn.setSelected(true);
		
	    var bG = new ButtonGroup();
	    bG.add(kPaBtn);
	    bG.add(atmBtn);
	    bG.add(mbBtn);
	    bG.add(psiBtn);
	    bG.add(massBtn);
	    bG.add(percentBtn);

		return radioPane;
	}

	private JRadioButton createSelectorButton(String selector, int value) {
	    JRadioButton btn = new JRadioButton(Msg.getString("TabPanelAirComposition.checkbox." + selector)); //$NON-NLS-1$
	    btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox." + selector + ".tooltip")); //$NON-NLS-1$
	    btn.addActionListener(e -> changeSelection(value));
		return btn;
	}

	private void changeSelection(int newSelection) {
		styleSelected = newSelection;
		if (airTableModel != null) {
			airTableModel.update();
		}
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

			// Update gas composition values
			cO2Label.setValue(getOverallComposition(ResourceUtil.CO2_ID));
			arLabel.setValue(getOverallComposition(ResourceUtil.ARGON_ID));
			n2Label.setValue(getOverallComposition(ResourceUtil.NITROGEN_ID));
			o2Label.setValue(getOverallComposition(ResourceUtil.OXYGEN_ID));
			h2OLabel.setValue(getOverallComposition(ResourceUtil.WATER_ID));
			
			// Update average temperature
			averageTemperatureLabel.setValue(settlement.getTemperature());
		
			// Update indoor pressure (complex formatting based on button selection)
			String indoorPressure = switch (styleSelected) {
				case SHOW_KPA -> StyleManager.DECIMAL_KPA.format(settlement.getAirPressure());
				case SHOW_ATM -> DECIMAL_ATM.format(settlement.getAirPressure()/AirComposition.KPA_PER_ATM);
				case SHOW_MB -> DECIMAL_MB.format(settlement.getAirPressure()/AirComposition.KPA_PER_ATM * AirComposition.MB_PER_ATM);
				case SHOW_PSI -> DECIMAL_PSI.format(settlement.getAirPressure()/AirComposition.KPA_PER_ATM * AirComposition.PSI_PER_ATM);
				default -> StyleManager.DECIMAL2_PERC.format(100D);
			};
			
			if (!indoorPressure.equals(indoorPressureCache)) {
				indoorPressureCache = indoorPressure;
				indoorPressureLabel.setText(indoorPressureCache);
			}

		}

		airTableModel.update();
	}

	/**
	 * Internal class used as model for the table.
	 */
	private class AirTableModel extends BaseBuildingModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private static final int TOTAL_VAL = 101;
		private static final int O2_VAL = 102;
		private static final int H2O_VAL = 103;
		private static final int N2_VAL = 104;
		private static final int CO2_VAL = 105;	
		private static final int AR_VAL = 106;

		private static final EntityColumnSpec TOTAL = new EntityColumnSpec(new ColumnSpec(TOTAL_VAL, Msg.getString("TabPanelAirComposition.column.total"), Double.class), null);
		private static final EntityColumnSpec O2 = new EntityColumnSpec(new ColumnSpec(O2_VAL, O2_NAME, Double.class), null);
		private static final EntityColumnSpec H2O = new EntityColumnSpec(new ColumnSpec(H2O_VAL, H2O_NAME, Double.class), null);
		private static final EntityColumnSpec N2 = new EntityColumnSpec(new ColumnSpec(N2_VAL, N2_NAME, Double.class), null);
		private static final EntityColumnSpec CO2 = new EntityColumnSpec(new ColumnSpec(CO2_VAL, CO2_NAME, Double.class), null);	
		private static final EntityColumnSpec AR = new EntityColumnSpec(new ColumnSpec(AR_VAL, AR_NAME, Double.class), null);	

		private BuildingManager manager;

		private AirTableModel(Settlement settlement) {
			super(NAME, TOTAL, O2, H2O, N2, CO2, AR);
			this.manager = settlement.getBuildingManager();
			setEntities(manager.getBuildingsWithLifeSupport());
		}

		@Override
		protected Object getEntityValue(Building building, int valueIndex) {
			return switch (valueIndex) {
				case TOTAL_VAL -> getSubtotal(building);
				case O2_VAL -> getGasValue(ResourceUtil.OXYGEN_ID, building);
				case H2O_VAL -> getGasValue(ResourceUtil.WATER_ID, building);
				case N2_VAL -> getGasValue(ResourceUtil.NITROGEN_ID, building);
				case CO2_VAL -> getGasValue(ResourceUtil.CO2_ID, building);
				case AR_VAL -> getGasValue(ResourceUtil.ARGON_ID, building);
				default -> super.getEntityValue(building, valueIndex);
			};
		}

		private double getSubtotal(Building b) {
			AirComposition air = b.getLifeSupport().getAir();
			double v = air.getTotalPressure();

			return switch (styleSelected) {
				case SHOW_PERC -> 100D;
				case SHOW_KPA -> v * AirComposition.KPA_PER_ATM;
				case SHOW_ATM -> v;
				case SHOW_MB -> v * AirComposition.MB_PER_ATM;	
				case SHOW_PSI -> v * AirComposition.PSI_PER_ATM;
				case SHOW_MASS -> air.getTotalMass();
				default -> 0D;
			};
		}

		private double getGasValue(int gasId, Building b) {
			AirComposition.GasDetails gas = b.getLifeSupport().getAir().getGas(gasId);
			return switch (styleSelected) {
				case SHOW_PERC -> gas.getPercent();
				case SHOW_KPA -> gas.getPartialPressure() * AirComposition.KPA_PER_ATM;
				case SHOW_ATM -> gas.getPartialPressure();
				case SHOW_MB -> gas.getPartialPressure() * AirComposition.MB_PER_ATM;
				case SHOW_PSI -> gas.getPartialPressure() * AirComposition.PSI_PER_ATM;
				case SHOW_MASS -> gas.getMass();
				default -> 0D;
			};
		}

		public void update() {
			fireTableRowsUpdated(0, getRowCount() - 1);
		}
	}
}