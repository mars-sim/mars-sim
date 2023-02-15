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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.air.AirComposition;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * This is a tab panel for displaying the composition of air of each inhabitable building in a settlement.
 */
@SuppressWarnings("serial")
public class TabPanelAirComposition extends TabPanel {

	private static final String AIR_ICON = "air";
	
	private static final String LABEL_PERCENT = "TabPanelAirComposition.label.percent";
	
	private int numBuildingsCache;
	
	private double o2Cache;
	private double cO2Cache;
	private double n2Cache;
	private double h2OCache;
	private double arCache;
	private double averageTemperatureCache;

	private String indoorPressureCache;
	
	private List<Building> buildingsCache;

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

		buildingsCache = manager.getBuildingsWithLifeSupport();
		numBuildingsCache = buildingsCache.size();

		JPanel topContentPanel = new JPanel();
		topContentPanel.setLayout(new BoxLayout(topContentPanel, BoxLayout.Y_AXIS));
		content.add(topContentPanel, BorderLayout.NORTH);
		
		// Prepare the top panel using spring layout.
		JPanel topPanel = new JPanel(new SpringLayout());
		topContentPanel.add(topPanel);

		JLabel t_label = new JLabel(Msg.getString("TabPanelAirComposition.label.averageTemperature.title"), SwingConstants.RIGHT);
		topPanel.add(t_label);
		averageTemperatureCache = settlement.getTemperature();
		averageTemperatureLabel = new JLabel(Msg.getString("TabPanelAirComposition.label.averageTemperature", StyleManager.DECIMAL_PLACES2.format(averageTemperatureCache)), SwingConstants.LEFT); //$NON-NLS-1$
		topPanel.add(averageTemperatureLabel);
		
		JLabel p_label = new JLabel(Msg.getString("TabPanelAirComposition.label.indoorPressure.title"), SwingConstants.RIGHT);
		topPanel.add(p_label);
		indoorPressureCache = Math.round(settlement.getAirPressure()*100.0)/100.0 + "";
		indoorPressureLabel = new JLabel(Msg.getString("TabPanelAirComposition.label.totalPressure.kPa", indoorPressureCache), SwingConstants.LEFT); //$NON-NLS-1$
		topPanel.add(indoorPressureLabel);
		
		// Lay out the spring panel.
		SpringUtilities.makeCompactGrid(topPanel,
		                                2, 2, //rows, cols
		                                5, 5,        //initX, initY
		                                10, 1);       //xPad, yPad
		
		JPanel gasesPanel = new JPanel(new GridLayout(2,1));
		topContentPanel.add(gasesPanel); 
		
		// CO2, H2O, N2, O2, Others (Ar2, He, CH4...)
		JPanel gasTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JLabel gasLabel = new JLabel(Msg.getString("TabPanelAirComposition.label"), SwingConstants.CENTER);
		gasTitle.add(gasLabel);
		gasesPanel.add(gasTitle);

		JPanel gasPanel = new JPanel(new SpringLayout());
		gasesPanel.add(gasPanel);

		JLabel co2 = new JLabel(Msg.getString("TabPanelAirComposition.cO2.title"), SwingConstants.RIGHT);
		gasPanel.add(co2);
		cO2Cache = -1;
		cO2Label = new JLabel(Msg.getString(LABEL_PERCENT, StyleManager.DECIMAL_PLACES3.format(cO2Cache))+"   ", SwingConstants.LEFT); //$NON-NLS-1$
		gasPanel.add(cO2Label);

		JLabel ar = new JLabel(Msg.getString("TabPanelAirComposition.ar.title"), SwingConstants.RIGHT);
		gasPanel.add(ar);
		arCache = 0;
		arLabel = new JLabel(Msg.getString(LABEL_PERCENT, StyleManager.DECIMAL_PLACES2.format(arCache))+"   ", SwingConstants.LEFT); //$NON-NLS-1$
		gasPanel.add(arLabel);
		
		JLabel n2 = new JLabel(Msg.getString("TabPanelAirComposition.n2.title"), SwingConstants.RIGHT);
		gasPanel.add(n2);
		n2Cache = 0;
		n2Label = new JLabel(Msg.getString(LABEL_PERCENT, StyleManager.DECIMAL_PLACES1.format(n2Cache))+"   ", SwingConstants.LEFT); //$NON-NLS-1$
		gasPanel.add(n2Label);

		JLabel o2 = new JLabel(Msg.getString("TabPanelAirComposition.o2.title"), SwingConstants.RIGHT);
		gasPanel.add(o2);
		o2Cache = 0;
		o2Label = new JLabel(Msg.getString(LABEL_PERCENT, StyleManager.DECIMAL_PLACES2.format(o2Cache))+"   ", SwingConstants.LEFT); //$NON-NLS-1$
		gasPanel.add(o2Label);

		JLabel h2O = new JLabel(Msg.getString("TabPanelAirComposition.h2O.title"), SwingConstants.RIGHT);
		gasPanel.add(h2O);
		h2OCache = 0;
		h2OLabel = new JLabel(Msg.getString(LABEL_PERCENT, StyleManager.DECIMAL_PLACES2.format(h2OCache))+"   ", SwingConstants.LEFT); //$NON-NLS-1$
		gasPanel.add(h2OLabel);
		gasPanel.add(new JLabel(""));
		gasPanel.add(new JLabel(""));
		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(gasPanel,
		                                2, 6, //rows, cols
		                                70, 1,        //initX, initY
		                                10, 1);       //xPad, yPad
		
		// Create override check box panel.
		JPanel radioPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(radioPane, BorderLayout.SOUTH);
		
	    percent_btn = new JRadioButton(Msg.getString("TabPanelAirComposition.checkbox.percent")); //$NON-NLS-1$
	    percent_btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox.percent.tooltip")); //$NON-NLS-1$
	    mass_btn = new JRadioButton(Msg.getString("TabPanelAirComposition.checkbox.mass")); //$NON-NLS-1$
	    mass_btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox.mass.tooltip")); //$NON-NLS-1$
   
	    kPa_btn = new JRadioButton(Msg.getString("TabPanelAirComposition.checkbox.kPa")); //$NON-NLS-1$
	    kPa_btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox.kPa.tooltip")); //$NON-NLS-1$
	    atm_btn = new JRadioButton(Msg.getString("TabPanelAirComposition.checkbox.atm")); //$NON-NLS-1$
	    atm_btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox.atm.tooltip")); //$NON-NLS-1$
	    psi_btn = new JRadioButton(Msg.getString("TabPanelAirComposition.checkbox.psi")); //$NON-NLS-1$
	    psi_btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox.psi.tooltip")); //$NON-NLS-1$
	    mb_btn = new JRadioButton(Msg.getString("TabPanelAirComposition.checkbox.mb")); //$NON-NLS-1$
	    mb_btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox.mb.tooltip")); //$NON-NLS-1$

	    percent_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tableModel.update();
			}
		});
	    kPa_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tableModel.update();
			}
		});
	    atm_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tableModel.update();
			}
		});
	    mb_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tableModel.update();
			}
		});
	    psi_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tableModel.update();
			}
		});
	    mass_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tableModel.update();
			}
		});

		JPanel pressure_p = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		pressure_p.setBorder(BorderFactory.createTitledBorder("Pressure "));
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
	   // bG.add(moles_btn);
	    //bG.add(temperature_btn);
	    
		// Create scroll panel for the outer table panel.
		scrollPane = new JScrollPane();
		// scrollPane.setPreferredSize(new Dimension(257, 230));
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		content.add(scrollPane,BorderLayout.CENTER);

		tableModel = new TableModel(settlement);
		table = new JTable(tableModel);

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

	public String getSubtotal(Building b) {
		double v = 0;
		AirComposition air = b.getLifeSupport().getAir();

		if (percent_btn.isSelected()) {
			/** 
			 * Percentage is ALWAYS going to be 100%
			for (int gas = 0; gas < CompositionOfAir.numGases; gas++) {
				v += air.getPartialPressure()[gas][row];
			}			
			// convert to percent
			return String.format("%1.1f", v/air.getTotalPressure(b) *100D);
			*/
			return "100.0";
		}
		else if (kPa_btn.isSelected()) {
			v = air.getTotalPressure();
			// convert to kPascal
			return String.format("%1.2f", v * AirComposition.KPA_PER_ATM);
		}
		else if (atm_btn.isSelected()) {
			v = air.getTotalPressure();
			// convert to atm
			return String.format("%1.4f", v);
		}
		else if (mb_btn.isSelected()) {
			v = air.getTotalPressure();
			// convert to millibar
			return String.format("%1.1f", v * AirComposition.MB_PER_ATM);
		}
		else if (psi_btn.isSelected()) {
			v = air.getTotalPressure();
			// convert to psi
			return String.format("%1.2f", v * AirComposition.PSI_PER_ATM);
		}
		//else if (moles_btn.isSelected()) {
		//	v = air.getTotalMoles()[row];
		//	return (String.format("%1.1e", v)).replaceAll("e+", "e"); 
		//}
		else if (mass_btn.isSelected()) {
			v = air.getTotalMass();
			return String.format("%1.2f", v); 
		}
		else
			return null;

	}

	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		List<Building> buildings = manager.getBuildingsWithLifeSupport();
		int numBuildings = buildings.size();

		if (numBuildings != numBuildingsCache) {
			numBuildingsCache = numBuildings;
			buildingsCache = buildings;
		}
		else {

			double cO2 = getOverallComposition(ResourceUtil.co2ID);
			if (cO2Cache != cO2) {
				cO2Cache = cO2;
				cO2Label.setText(
					Msg.getString(LABEL_PERCENT, //$NON-NLS-1$
					StyleManager.DECIMAL_PLACES3.format(cO2Cache))+"   "
					);
			}

			double ar = getOverallComposition(ResourceUtil.argonID);
			if (arCache != ar) {
				arCache = ar;
				arLabel.setText(
					Msg.getString(LABEL_PERCENT,  //$NON-NLS-1$
					StyleManager.DECIMAL_PLACES2.format(ar))+"   "
					);
			}
			

			double n2 =  getOverallComposition(ResourceUtil.nitrogenID);
			if (n2Cache != n2) {
				n2Cache = n2;
				n2Label.setText(
					Msg.getString(LABEL_PERCENT,  //$NON-NLS-1$
					StyleManager.DECIMAL_PLACES1.format(n2))+"   "
					);
			}



			double o2 = getOverallComposition(ResourceUtil.oxygenID);
			if (o2Cache != o2) {
				o2Cache = o2;
				o2Label.setText(
					Msg.getString(LABEL_PERCENT, //$NON-NLS-1$
					StyleManager.DECIMAL_PLACES2.format(o2Cache))+"   "
					);
			}

			double h2O = getOverallComposition(ResourceUtil.waterID);
			if (h2OCache != h2O) {
				h2OCache = h2O;
				h2OLabel.setText(
					Msg.getString(LABEL_PERCENT,  //$NON-NLS-1$
					StyleManager.DECIMAL_PLACES2.format(h2O))+"   "
					);
			}
			
			double averageTemperature = Math.round(settlement.getTemperature()*1000.0)/1000.0; // convert to kPascal by multiplying 1000
			if (averageTemperatureCache != averageTemperature) {
				averageTemperatureCache = averageTemperature;
				averageTemperatureLabel.setText(
					Msg.getString("TabPanelAirComposition.label.averageTemperature",  //$NON-NLS-1$
					StyleManager.DECIMAL_PLACES2.format(averageTemperatureCache)
					));
			}
			
			String indoorPressure = Msg.getString("TabPanelAirComposition.label.totalPressure.kPa",  //$NON-NLS-1$
					Math.round(settlement.getAirPressure()*100.0)/100.0);
			
			if (kPa_btn.isSelected()) {
				// convert from atm to kPascal
				indoorPressure = Msg.getString("TabPanelAirComposition.label.totalPressure.kPa",  //$NON-NLS-1$
						Math.round(settlement.getAirPressure()*100.0)/100.0);
			}
			else if (atm_btn.isSelected()) {
				indoorPressure = Msg.getString("TabPanelAirComposition.label.totalPressure.atm",  //$NON-NLS-1$
						Math.round(settlement.getAirPressure()/AirComposition.KPA_PER_ATM*10000.0)/10000.0);
			}
			else if (mb_btn.isSelected()) {
				// convert from atm to mb
				indoorPressure = Msg.getString("TabPanelAirComposition.label.totalPressure.mb",  //$NON-NLS-1$
						Math.round(settlement.getAirPressure()/AirComposition.KPA_PER_ATM * AirComposition.MB_PER_ATM*100.0)/100.0);
			}
			else if (psi_btn.isSelected()) {
				// convert from atm to kPascal
				indoorPressure =  Msg.getString("TabPanelAirComposition.label.totalPressure.psi",  //$NON-NLS-1$
						Math.round(settlement.getAirPressure()/AirComposition.KPA_PER_ATM * AirComposition.PSI_PER_ATM*1000.0)/1000.0);
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
	private class TableModel extends AbstractTableModel {

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
			else if (columnIndex == 2) return Msg.getString("TabPanelAirComposition.column.cO2"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelAirComposition.column.ar"); //$NON-NLS-1$
			else if (columnIndex == 4) return Msg.getString("TabPanelAirComposition.column.n2"); //$NON-NLS-1$
			else if (columnIndex == 5) return Msg.getString("TabPanelAirComposition.column.o2"); //$NON-NLS-1$
			else if (columnIndex == 6) return Msg.getString("TabPanelAirComposition.column.h2o"); //$NON-NLS-1$

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
				double amt = getValue(getGasId(column), b);
				if (amt == 0)
					return "N/A";
				else
					return amt;
			}
			else  {
				return null;
			}
		}

		public double getValue(int gasId, Building b) {
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
