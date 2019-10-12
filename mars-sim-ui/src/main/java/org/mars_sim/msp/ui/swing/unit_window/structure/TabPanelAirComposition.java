/**
 * Mars Simulation Project
 * TabPanelAirComposition.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.radiobutton.WebRadioButton;
import com.alee.laf.scroll.WebScrollPane;

/**
 * This is a tab panel for displaying the composition of air of each inhabitable building in a settlement.
 */
@SuppressWarnings("serial")
public class TabPanelAirComposition
extends TabPanel {

	// default logger.
	//private static Logger logger = Logger.getLogger(TabPanelAirComposition.class.getName());

	// Data cache
	/** Is UI constructed. */
	private boolean uiDone = false;

	private int numBuildingsCache;
	
	private double o2Cache;
	private double cO2Cache;
	private double n2Cache;
	private double h2OCache;
	private double arCache;
	private double averageTemperatureCache;

	private String indoorPressureCache;
	
	private List<Building> buildingsCache;

	private WebLabel o2Label;
	private WebLabel cO2Label;
	private WebLabel n2Label;
	private WebLabel h2OLabel;
	private WebLabel arLabel;
	private WebLabel indoorPressureLabel;
	private WebLabel averageTemperatureLabel;

	private JTable table ;

	private WebRadioButton percent_btn;
	private WebRadioButton mass_btn;//, moles_btn, temperature_btn;
	private WebRadioButton kPa_btn;
	private WebRadioButton atm_btn;
	private WebRadioButton psi_btn;
	private WebRadioButton mb_btn;
	
	private WebScrollPane scrollPane;
	
	private ButtonGroup bG;
	
	private TableModel tableModel;

	private Settlement settlement;
	private BuildingManager manager;
	private CompositionOfAir air;

	private static DecimalFormat fmt3 = new DecimalFormat("0.000");//Msg.getString("decimalFormat3")); //$NON-NLS-1$
	private static DecimalFormat fmt2 = new DecimalFormat("0.00");//Msg.getString("decimalFormat2")); //$NON-NLS-1$
	private static DecimalFormat fmt1 = new DecimalFormat("0.0");//Msg.getString("decimalFormat1")); //$NON-NLS-1$


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

	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		manager = settlement.getBuildingManager();
		air = settlement.getCompositionOfAir();

		buildingsCache = manager.getBuildingsWithLifeSupport();
		numBuildingsCache = buildingsCache.size();

		// Prepare label panel.
		WebPanel titlePanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePanel);

		WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelAirComposition.title"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		titlePanel.add(titleLabel);

		// Prepare the top panel using spring layout.
		WebPanel topPanel = new WebPanel(new SpringLayout());
		//topPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(topPanel);

		WebLabel t_label = new WebLabel(Msg.getString("TabPanelAirComposition.label.averageTemperature.title"), WebLabel.RIGHT);
		topPanel.add(t_label);
		averageTemperatureCache = settlement.getTemperature();
		averageTemperatureLabel = new WebLabel(Msg.getString("TabPanelAirComposition.label.averageTemperature", fmt2.format(averageTemperatureCache)), WebLabel.LEFT); //$NON-NLS-1$
		topPanel.add(averageTemperatureLabel);
		
		WebLabel p_label = new WebLabel(Msg.getString("TabPanelAirComposition.label.indoorPressure.title"), WebLabel.RIGHT);
		topPanel.add(p_label);
		indoorPressureCache = Math.round(settlement.getAirPressure()*100.0)/100.0 + "";
		indoorPressureLabel = new WebLabel(Msg.getString("TabPanelAirComposition.label.totalPressure.kPa", indoorPressureCache), WebLabel.LEFT); //$NON-NLS-1$
		topPanel.add(indoorPressureLabel);
		
		// Lay out the spring panel.
		SpringUtilities.makeCompactGrid(topPanel,
		                                2, 2, //rows, cols
		                                5, 5,        //initX, initY
		                                10, 1);       //xPad, yPad
		
		WebPanel gasesPanel = new WebPanel(new GridLayout(2,1));
//		gasesPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(gasesPanel); 
		
		// CO2, H2O, N2, O2, Others (Ar2, He, CH4...)
		WebPanel gasTitle = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		WebLabel gasLabel = new WebLabel(Msg.getString("TabPanelAirComposition.label"), WebLabel.CENTER);
		gasLabel.setFont(new Font("Serif", Font.BOLD, 16));
		gasTitle.add(gasLabel);
		gasesPanel.add(gasTitle);

		WebPanel gasPanel = new WebPanel(new SpringLayout());
		//gasPanel.setBorder(new MarsPanelBorder());
		gasesPanel.add(gasPanel);

		WebLabel co2 = new WebLabel(Msg.getString("TabPanelAirComposition.cO2.title"), WebLabel.RIGHT);
		gasPanel.add(co2);
		cO2Cache = getOverallComposition(0);
		cO2Label = new WebLabel(Msg.getString("TabPanelAirComposition.label.percent", fmt3.format(cO2Cache))+"   ", WebLabel.LEFT); //$NON-NLS-1$
		gasPanel.add(cO2Label);

		WebLabel ar = new WebLabel(Msg.getString("TabPanelAirComposition.ar.title"), WebLabel.RIGHT);
		gasPanel.add(ar);
		arCache = getOverallComposition(1);
		arLabel = new WebLabel(Msg.getString("TabPanelAirComposition.label.percent", fmt2.format(arCache))+"   ", WebLabel.LEFT); //$NON-NLS-1$
		gasPanel.add(arLabel);
		
		WebLabel n2 = new WebLabel(Msg.getString("TabPanelAirComposition.n2.title"), WebLabel.RIGHT);
		gasPanel.add(n2);
		n2Cache = getOverallComposition(2);
		n2Label = new WebLabel(Msg.getString("TabPanelAirComposition.label.percent", fmt1.format(n2Cache))+"   ", WebLabel.LEFT); //$NON-NLS-1$
		gasPanel.add(n2Label);

		WebLabel o2 = new WebLabel(Msg.getString("TabPanelAirComposition.o2.title"), WebLabel.RIGHT);
		gasPanel.add(o2);
		o2Cache = getOverallComposition(3);
		o2Label = new WebLabel(Msg.getString("TabPanelAirComposition.label.percent", fmt2.format(o2Cache))+"   ", WebLabel.LEFT); //$NON-NLS-1$
		gasPanel.add(o2Label);

		WebLabel h2O = new WebLabel(Msg.getString("TabPanelAirComposition.h2O.title"), WebLabel.RIGHT);
		gasPanel.add(h2O);
		h2OCache = getOverallComposition(4);
		h2OLabel = new WebLabel(Msg.getString("TabPanelAirComposition.label.percent", fmt2.format(h2OCache))+"   ", WebLabel.LEFT); //$NON-NLS-1$
		gasPanel.add(h2OLabel);
		gasPanel.add(new WebLabel(""));
		gasPanel.add(new WebLabel(""));
		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(gasPanel,
		                                2, 6, //rows, cols
		                                70, 1,        //initX, initY
		                                10, 1);       //xPad, yPad
		
		// Create override check box panel.
		WebPanel radioPane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(radioPane, BorderLayout.SOUTH);
		
	    percent_btn = new WebRadioButton(Msg.getString("TabPanelAirComposition.checkbox.percent")); //$NON-NLS-1$
	    percent_btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox.percent.tooltip")); //$NON-NLS-1$
	    mass_btn = new WebRadioButton(Msg.getString("TabPanelAirComposition.checkbox.mass")); //$NON-NLS-1$
	    mass_btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox.mass.tooltip")); //$NON-NLS-1$
   
	    kPa_btn = new WebRadioButton(Msg.getString("TabPanelAirComposition.checkbox.kPa")); //$NON-NLS-1$
	    kPa_btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox.kPa.tooltip")); //$NON-NLS-1$
	    atm_btn = new WebRadioButton(Msg.getString("TabPanelAirComposition.checkbox.atm")); //$NON-NLS-1$
	    atm_btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox.atm.tooltip")); //$NON-NLS-1$
	    psi_btn = new WebRadioButton(Msg.getString("TabPanelAirComposition.checkbox.psi")); //$NON-NLS-1$
	    psi_btn.setToolTipText(Msg.getString("TabPanelAirComposition.checkbox.psi.tooltip")); //$NON-NLS-1$
	    mb_btn = new WebRadioButton(Msg.getString("TabPanelAirComposition.checkbox.mb")); //$NON-NLS-1$
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

		WebPanel pressure_p = new WebPanel(new FlowLayout());
		pressure_p.setBorder(BorderFactory.createTitledBorder("Pressure"));
		pressure_p.add(kPa_btn);
		pressure_p.add(atm_btn);
		pressure_p.add(mb_btn);
		pressure_p.add(psi_btn);
		radioPane.add(pressure_p);
		
		WebPanel mass_p = new WebPanel(new FlowLayout());
		mass_p.setBorder(BorderFactory.createTitledBorder("Mass"));
		mass_p.add(mass_btn);
		radioPane.add(mass_p);
    
		WebPanel vol_p = new WebPanel(new FlowLayout());
		vol_p.setSize(60, 40);
		vol_p.setBorder(BorderFactory.createTitledBorder("Content"));
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
		scrollPane = new WebScrollPane();
		// scrollPane.setPreferredSize(new Dimension(257, 230));
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(WebScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		centerContentPanel.add(scrollPane,BorderLayout.CENTER);

		tableModel = new TableModel(settlement);
		table = new ZebraJTable(tableModel);
	    //SwingUtilities.invokeLater(() -> ColumnResizer.adjustColumnPreferredWidths(table));

		table.setRowSelectionAllowed(true);
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(20);
		table.getColumnModel().getColumn(2).setPreferredWidth(20);
		table.getColumnModel().getColumn(3).setPreferredWidth(20);
		table.getColumnModel().getColumn(4).setPreferredWidth(20);
		table.getColumnModel().getColumn(5).setPreferredWidth(20);
		table.getColumnModel().getColumn(6).setPreferredWidth(20);

		// Override default cell renderer for formatting double values.
		table.setDefaultRenderer(Double.class, new NumberCellRenderer(2, true));
        
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer);
//		table.getColumnModel().getColumn(1).setCellRenderer(renderer);
//		table.getColumnModel().getColumn(2).setCellRenderer(renderer);
//		table.getColumnModel().getColumn(3).setCellRenderer(renderer);
//		table.getColumnModel().getColumn(4).setCellRenderer(renderer);
//		table.getColumnModel().getColumn(5).setCellRenderer(renderer);
//		table.getColumnModel().getColumn(6).setCellRenderer(renderer);
		
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		table.setAutoCreateRowSorter(true);

		TableStyle.setTableStyle(table);

		scrollPane.setViewportView(table);

	}

//	/**
//	 * Sets .
//	 * @param value true or false.
//
//	private void setMetric(boolean value) {
//		if (value)
//			buildings = manager.getSortedBuildings();
//		else
//			buildings = manager.getBuildingsWithThermal();
//		tableModel.update();
//	}
	
	public double getOverallComposition(int gas) {
		double result = 0;
		int size = buildingsCache.size();
		for (Building b : buildingsCache) {
			int id = b.getInhabitableID();
			double [][] vol = air.getPercentComposition();
			//System.out.println("vol.length : " + vol.length + "  vol[].length : " + vol[0].length);
			double percent = vol[gas][id];
			result += percent;
		}
		return Math.round(result/size*100.0)/100.0;
	}

	public String getSubtotal(int row) {
		double v = 0;
		
		if (percent_btn.isSelected()) {
			for (int gas = 0; gas < CompositionOfAir.numGases; gas++) {
				v += air.getPartialPressure()[gas][row];
			}			
//			return String.format("%1.1f", v/air.getTotalPressure()[row] *100D);
			return v/air.getTotalPressure()[row] *100D + "";
		}
		else if (kPa_btn.isSelected()) {
			v = air.getTotalPressure()[row];
			// convert from atm to kPascal
//			return String.format("%1.2f", v * CompositionOfAir.KPA_PER_ATM);
			return v * CompositionOfAir.KPA_PER_ATM + "";
		}
		else if (atm_btn.isSelected()) {
			v = air.getTotalPressure()[row];
			// convert from atm to kPascal
//			return String.format("%1.4f", v);
			return v + "";
		}
		else if (mb_btn.isSelected()) {
			v = air.getTotalPressure()[row];
			// convert from atm to kPascal
//			return String.format("%1.2f", v * CompositionOfAir.MB_PER_ATM);
			return v * CompositionOfAir.MB_PER_ATM + "";
		}
		else if (psi_btn.isSelected()) {
			v = air.getTotalPressure()[row];
			// convert from atm to kPascal
//			return String.format("%1.3f", v * CompositionOfAir.PSI_PER_ATM);
			return v * CompositionOfAir.PSI_PER_ATM + "";
		}
		//else if (moles_btn.isSelected()) {
		//	v = air.getTotalMoles()[row];
		//	return (String.format("%1.1e", v)).replaceAll("e+", "e"); 
		//}
		else if (mass_btn.isSelected()) {
			v = air.getTotalMass()[row];
//			return String.format("%1.3f", v); 
			return v + "";
		}
		//else if (temperature_btn.isSelected()) {
			/*
			for (int gas = 0; gas < CompositionOfAir.numGases; gas++) {
				v += air.getTemperature()[gas][row];
			}			
			return String.format("%2.1f", v/CompositionOfAir.numGases - CompositionOfAir.C_TO_K);
			*/
		//	return String.format("%2.1f", manager.getBuilding(row).getCurrentTemperature());
		//}
		else
			return null;

	}

	
	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		if (!uiDone)
			this.initializeUI();
		
		List<Building> buildings = manager.getBuildingsWithLifeSupport();//getBuildings(BuildingFunction.LIFE_SUPPORT);
		int numBuildings = buildings.size();

		if (numBuildings != numBuildingsCache) {
			numBuildingsCache = numBuildings;
			buildingsCache = buildings;
		}
		else {

			double cO2 = getOverallComposition(0);
			if (cO2Cache != cO2) {
				cO2Cache = cO2;
				cO2Label.setText(
					Msg.getString("TabPanelAirComposition.label.percent", //$NON-NLS-1$
					fmt3.format(cO2Cache))+"   "
					);
			}

			double ar = getOverallComposition(1);
			if (arCache != ar) {
				arCache = ar;
				arLabel.setText(
					Msg.getString("TabPanelAirComposition.label.percent",  //$NON-NLS-1$
					fmt2.format(ar))+"   "
					);
			}
			

			double n2 =  getOverallComposition(2);
			if (n2Cache != n2) {
				n2Cache = n2;
				n2Label.setText(
					Msg.getString("TabPanelAirComposition.label.percent",  //$NON-NLS-1$
					fmt1.format(n2))+"   "
					);
			}



			double o2 = getOverallComposition(3);
			if (o2Cache != o2) {
				o2Cache = o2;
				o2Label.setText(
					Msg.getString("TabPanelAirComposition.label.percent", //$NON-NLS-1$
					fmt2.format(o2Cache))+"   "
					);
			}

			double h2O = getOverallComposition(4);
			if (h2OCache != h2O) {
				h2OCache = h2O;
				h2OLabel.setText(
					Msg.getString("TabPanelAirComposition.label.percent",  //$NON-NLS-1$
					fmt2.format(h2O))+"   "
					);
			}
			
			double averageTemperature = Math.round(settlement.getTemperature()*1000.0)/1000.0; // convert to kPascal by multiplying 1000
			if (averageTemperatureCache != averageTemperature) {
				averageTemperatureCache = averageTemperature;
				averageTemperatureLabel.setText(
					Msg.getString("TabPanelAirComposition.label.averageTemperature",  //$NON-NLS-1$
					fmt2.format(averageTemperatureCache)
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
						Math.round(settlement.getAirPressure()/CompositionOfAir.KPA_PER_ATM*10000.0)/10000.0);
			}
			else if (mb_btn.isSelected()) {
				// convert from atm to mb
				indoorPressure = Msg.getString("TabPanelAirComposition.label.totalPressure.mb",  //$NON-NLS-1$
						Math.round(settlement.getAirPressure()/CompositionOfAir.KPA_PER_ATM * CompositionOfAir.MB_PER_ATM*100.0)/100.0);
			}
			else if (psi_btn.isSelected()) {
				// convert from atm to kPascal
				indoorPressure =  Msg.getString("TabPanelAirComposition.label.totalPressure.psi",  //$NON-NLS-1$
						Math.round(settlement.getAirPressure()/CompositionOfAir.KPA_PER_ATM * CompositionOfAir.PSI_PER_ATM*1000.0)/1000.0);
			}
			
			if (!indoorPressureCache.equals(indoorPressure)) {
				indoorPressureCache = indoorPressure;
				indoorPressureLabel.setText(indoorPressureCache);
			}

		}

		tableModel.update();
		TableStyle.setTableStyle(table);

	}

	/**
	 * Internal class used as model for the table.
	 */
	private class TableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private int size;

//		private Settlement settlement;
		private BuildingManager manager;

		private List<Building> buildings = new ArrayList<>();;

		private CompositionOfAir air;

//		private DecimalFormat fmt3 = new DecimalFormat(Msg.getString("decimalFormat3")); //$NON-NLS-1$
//		private DecimalFormat fmt2 = new DecimalFormat(Msg.getString("decimalFormat2")); //$NON-NLS-1$
//		private DecimalFormat fmt1 = new DecimalFormat(Msg.getString("decimalFormat1")); //$NON-NLS-1$

		private TableModel(Settlement settlement) {
//			this.settlement = settlement;
			this.manager = settlement.getBuildingManager();
			this.air = settlement.getCompositionOfAir();
			this.buildings = selectBuildingsWithLS();
			this.size = buildings.size();

		}

		public List<Building> selectBuildingsWithLS() {
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

		public Object getValueAt(int row, int column) {

			//Building b = buildings.get(row);
			Building b = manager.getInhabitableBuilding(row);

			// CO2, H2O, N2, O2, Others (Ar2, He, CH4...)
			if (column == 0) {
				return b.getNickName();
			}
			else if (column == 1) {
				//return air.getTotalPressure()[row]* CompositionOfAir.kPASCAL_PER_ATM;
				return getSubtotal(row) + " ";//getTotalPressure(row);
			}
			else if (column > 1) {
				//double amt = air.getPercentComposition()[column - 2][b.getInhabitableID()];//getComposition(column - 2);
				
				double amt = getValue(column - 2, b.getInhabitableID());
				if (amt == 0)
					return "N/A";
				else
					return amt;
				
//				else if (percent_btn.isSelected())
//					return String.format("%1.3f", amt); 
//				else if (kPa_btn.isSelected())
//					return String.format("%1.2f", amt); 
//				else if (atm_btn.isSelected())
//					return String.format("%1.4f", amt); 
//				else if (mb_btn.isSelected())
//					return String.format("%1.2f", amt); 
//				else if (psi_btn.isSelected())
//					return String.format("%1.3f", amt); 
//				//else if (moles_btn.isSelected())
//				//	return String.format("%1.1e", amt).replaceAll("e+0", "e"); 
//				else if (mass_btn.isSelected())
//					return String.format("%1.3f", amt);//.replaceAll("e+0", "e"); 
//				//else if (temperature_btn.isSelected())
//				//	return String.format("%3.1f", amt); 
//				else if (column == 2 || column == 6)
//					return fmt3.format(amt);
//				else if (column == 3 || column == 4 || column == 5)
//					return fmt2.format(amt);
//				else
//					return null;
			}
			else  {
				return null;
			}
		}

		public double getValue(int gas, int id) {
			//double[][] value = new double[CompositionOfAir.numGases][size];
			if (percent_btn.isSelected())
				return air.getPercentComposition()[gas][id];
			else if (kPa_btn.isSelected())
				return air.getPartialPressure()[gas][id] * CompositionOfAir.KPA_PER_ATM;
			else if (atm_btn.isSelected())
				return air.getPartialPressure()[gas][id];
			else if (mb_btn.isSelected())
				return air.getPartialPressure()[gas][id] * CompositionOfAir.MB_PER_ATM;
			else if (psi_btn.isSelected())
				return air.getPartialPressure()[gas][id] * CompositionOfAir.PSI_PER_ATM;
			//else if (temperature_btn.isSelected())
			//	return air.getTemperature()[gas][id] - CompositionOfAir.C_TO_K;
			//else if (moles_btn.isSelected())
			//	return air.getNumMoles()[gas][id];
			else if (mass_btn.isSelected())
				return air.getMass()[gas][id];
			else
				return 0;
		}
			
//		public double getMole(int gas) {
//			double mole = 0;
//			Iterator<Building> k = buildings.iterator();
//			while (k.hasNext()) {
//				Building b = k.next();
//				int id = b.getInhabitableID();
//				double [][] numMoles = air.getNumMoles();
//
//				if (id < numMoles[0].length)
//					mole = numMoles[gas][id];
//				else
//					mole = 0;
//			}
//			return mole;
//		}	
//		
//		public double getMass(int gas) {
//			double kg = 0;
//			double moles = getMole(gas);
//			if (gas == 0)
//				kg = CompositionOfAir.CO2_MOLAR_MASS * moles;
//			else if (gas == 1)
//				kg = CompositionOfAir.ARGON_MOLAR_MASS * moles;
//			else if (gas == 2)
//				kg = CompositionOfAir.N2_MOLAR_MASS * moles;
//			else if (gas == 3)
//				kg = CompositionOfAir.O2_MOLAR_MASS * moles;
//			else if (gas == 4)
//				kg = 0;
//							
//			return kg;
//		}
	
//		public double getComposition(int gas) {
//			double percent = 0;
//			Iterator<Building> k = buildings.iterator();
//			while (k.hasNext()) {
//				Building b = k.next();
//				int id = b.getInhabitableID();
//				double [][] comp = air.getPercentComposition();
//
//				if (id < comp[0].length)
//					percent = comp[gas][id];
//				else
//					percent = 0;
//
//			}
//			return percent;
//		}

//		public double getTotalPressure(int row) {
//			double [] tp = air.getTotalPressure();
//			double p = 0;
//			if (row < tp.length)
//				p = tp[row];
//			else
//				p = 0;
//			// convert from atm to kPascal
//			return p * CompositionOfAir.kPASCAL_PER_ATM;
//		}

		public void update() {
			//int newSize = buildings.size();
			//if (size != newSize) {
			//	size = newSize;
			//	buildings = selectBuildingsWithLS();
				//Collections.sort(buildings);
			//}
			//else {
				List<Building> newBuildings = selectBuildingsWithLS();
				if (!buildings.equals(newBuildings)) {
					buildings = newBuildings;
					scrollPane.validate();
					//Collections.sort(buildings);
				}
			//}

			fireTableDataChanged();
		}
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
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
		air = null;
		fmt3 = null;
		fmt2 = null;
		fmt1 = null;

	}
}