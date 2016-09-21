/**
 * Mars Simulation Project
 * HealthTabPanel.java
 * @version 3.07 2015-01-30
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.RadiationExposure;
import org.mars_sim.msp.core.person.medical.HealthProblem;
import org.mars_sim.msp.core.person.medical.Medication;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.BalloonToolTip;
import org.mars_sim.msp.ui.swing.tool.MultisortTableHeaderCellRenderer;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The HealthTabPanel is a tab panel for a person's health.
 */
public class TabPanelHealth
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data cache
	private double fatigueCache;
	private double hungerCache;
	private double energyCache;
	private double stressCache;
	private double performanceCache;

	private JLabel fatigueLabel;
	private JLabel hungerLabel;
	private JLabel energyLabel;
	private JLabel stressLabel;
	private JLabel performanceLabel;

	private MedicationTableModel medicationTableModel;
	private HealthProblemTableModel healthProblemTableModel;
	private RadiationTableModel radiationTableModel;
	private JTable radiationTable, medicationTable, healthProblemTable;
	private BalloonToolTip balloonToolTip = new BalloonToolTip();

	private DecimalFormat formatter = new DecimalFormat(Msg.getString("TabPanelHealth.decimalFormat")); //$NON-NLS-1$

	protected String[] radiationToolTips = {
		    "Exposure Interval", 
		    "[Max for BFO] 30-Day : 250; Annual : 500; Career : 1000",
		    "[Max for Eye] 30-Day : 1000; Annual : 2000; Career : 4000",
		    "[Max for Skin] 30-Day : 1500; Annual : 3000; Career : 6000"};
	
	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelHealth(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelHealth.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelHealth.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Person person = (Person) unit;
		PhysicalCondition condition = person.getPhysicalCondition();

		// Create health label panel.
		JPanel healthLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(healthLabelPanel);

		// Prepare health label
		JLabel healthLabel = new JLabel(Msg.getString("TabPanelHealth.label"), JLabel.CENTER); //$NON-NLS-1$
		healthLabel.setFont(new Font("Serif", Font.BOLD, 16));
		healthLabelPanel.add(healthLabel);

		// Prepare condition panel
		JPanel conditionPanel = new JPanel(new GridLayout(5, 2, 0, 0));
		conditionPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(conditionPanel, BorderLayout.NORTH);

		// Prepare fatigue name label
		JLabel fatigueNameLabel = new JLabel(Msg.getString("TabPanelHealth.fatigue"), JLabel.LEFT); //$NON-NLS-1$
		conditionPanel.add(fatigueNameLabel);

		// Prepare fatigue label
		fatigueCache = condition.getFatigue();
		fatigueLabel = new JLabel(Msg.getString("TabPanelHealth.millisols", //$NON-NLS-1$
		        formatter.format(fatigueCache)), JLabel.RIGHT);
		conditionPanel.add(fatigueLabel);

		// Prepare hunger name label
		JLabel hungerNameLabel = new JLabel(Msg.getString("TabPanelHealth.hunger"), JLabel.LEFT); //$NON-NLS-1$
		conditionPanel.add(hungerNameLabel);

		// Prepare hunger label
		hungerCache = condition.getHunger();
		hungerLabel = new JLabel(Msg.getString("TabPanelHealth.millisols", //$NON-NLS-1$
		        formatter.format(hungerCache)), JLabel.RIGHT);
		conditionPanel.add(hungerLabel);

		//
		// Prepare energy name label
		JLabel energyNameLabel = new JLabel(Msg.getString("TabPanelHealth.energy"), JLabel.LEFT); //$NON-NLS-1$
		conditionPanel.add(energyNameLabel);

		// Prepare energy label
		energyCache = condition.getEnergy();
		energyLabel = new JLabel(Msg.getString("TabPanelHealth.kJ", //$NON-NLS-1$
		        formatter.format(energyCache)), JLabel.RIGHT);
		conditionPanel.add(energyLabel);


		// Prepare stress name label
		JLabel stressNameLabel = new JLabel(Msg.getString("TabPanelHealth.stress"), JLabel.LEFT); //$NON-NLS-1$
		conditionPanel.add(stressNameLabel);

		// Prepare stress label
		stressCache = condition.getStress();
		stressLabel = new JLabel(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
		        formatter.format(stressCache)), JLabel.RIGHT);
		conditionPanel.add(stressLabel);

		// Prepare performance rating label
		JLabel performanceNameLabel = new JLabel(Msg.getString("TabPanelHealth.performance"), JLabel.LEFT); //$NON-NLS-1$
		conditionPanel.add(performanceNameLabel);

		// Performance rating label
		performanceCache = person.getPerformanceRating() * 100D;
		performanceLabel = new JLabel(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
		        formatter.format(performanceCache)), JLabel.RIGHT);
		conditionPanel.add(performanceLabel);


		// 2015-04-29 Added radiation dose info
		// Prepare radiation panel
		JPanel radiationPanel = new JPanel(new BorderLayout());//new GridLayout(2, 1, 0, 0));
		radiationPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(radiationPanel, BorderLayout.CENTER);

		// Prepare radiation label
		JLabel radiationLabel = new JLabel(Msg.getString("TabPanelRadiation.label"), JLabel.CENTER); //$NON-NLS-1$
		radiationPanel.add(radiationLabel, BorderLayout.NORTH);
		radiationLabel.setToolTipText(Msg.getString("TabPanelRadiation.tooltip")); //$NON-NLS-1$
		

		// Prepare radiation scroll panel
		JScrollPane radiationScrollPanel = new JScrollPane();
		radiationPanel.add(radiationScrollPanel, BorderLayout.CENTER);

		// Prepare radiation table model
		radiationTableModel = new RadiationTableModel(person);

		// Create radiation table
		radiationTable = new ZebraJTable(radiationTableModel) {	
		    //2016-04-15 Implemented radiation table header tool tips
		    protected JTableHeader createDefaultTableHeader() {
		        return new JTableHeader(columnModel) {
		            public String getToolTipText(MouseEvent e) {		            	
		                String tip = null;
		                java.awt.Point p = e.getPoint();
		                int index = columnModel.getColumnIndexAtX(p.x);
		                if (index > -1) {
			                int realIndex = columnModel.getColumn(index).getModelIndex();
			                return radiationToolTips[realIndex];
		            	}
		                else {
		                	return Msg.getString("TabPanelRadiation.tooltip");
		                }
		            }
		        };
		    }
		};
		//balloonToolTip.createBalloonTip(radiationTable, Msg.getString("TabPanelRadiation.tooltip")); //$NON-NLS-1$
		radiationTable.setPreferredScrollableViewportSize(new Dimension(225, 50));
		radiationTable.setCellSelectionEnabled(false);
		radiationScrollPanel.setViewportView(radiationTable);
		//radiationTable.setToolTipText(Msg.getString("TabPanelRadiation.tooltip")); //$NON-NLS-1$
		// 2015-06-08 Added sorting
		radiationTable.setAutoCreateRowSorter(true);
        if (!MainScene.OS.equals("linux")) {
        	radiationTable.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		}
		// 2015-06-08 Added setTableStyle()
		//TableStyle.setTableStyle(radiationTable);

		// Prepare table panel.
		JPanel tablePanel = new JPanel(new GridLayout(2, 1));
		centerContentPanel.add(tablePanel, BorderLayout.SOUTH);

		// Prepare medication panel.
		JPanel medicationPanel = new JPanel(new BorderLayout());
		medicationPanel.setBorder(new MarsPanelBorder());
		tablePanel.add(medicationPanel);

		// Prepare medication label.
		JLabel medicationLabel = new JLabel(Msg.getString("TabPanelHealth.medication"), JLabel.CENTER); //$NON-NLS-1$
		medicationPanel.add(medicationLabel, BorderLayout.NORTH);

		// Prepare medication scroll panel
		JScrollPane medicationScrollPanel = new JScrollPane();
		medicationPanel.add(medicationScrollPanel, BorderLayout.CENTER);

		// Prepare medication table model.
		medicationTableModel = new MedicationTableModel(person);

		// Prepare medication table.
		medicationTable = new ZebraJTable(medicationTableModel);
		medicationTable.setPreferredScrollableViewportSize(new Dimension(225, 50));
		medicationTable.setCellSelectionEnabled(false);
		medicationScrollPanel.setViewportView(medicationTable);

		// 2015-06-08 Added sorting
		medicationTable.setAutoCreateRowSorter(true);
       if (!MainScene.OS.equals("linux")) {
        	medicationTable.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		}
		// 2015-06-08 Added setTableStyle()
		//TableStyle.setTableStyle(medicationTable);

		// Prepare health problem panel
		JPanel healthProblemPanel = new JPanel(new BorderLayout());
		healthProblemPanel.setBorder(new MarsPanelBorder());
		tablePanel.add(healthProblemPanel);

		// Prepare health problem label
		JLabel healthProblemLabel = new JLabel(Msg.getString("TabPanelHealth.healthProblems"), JLabel.CENTER); //$NON-NLS-1$
		healthProblemPanel.add(healthProblemLabel, BorderLayout.NORTH);

		// Prepare health problem scroll panel
		JScrollPane healthProblemScrollPanel = new JScrollPane();
		healthProblemPanel.add(healthProblemScrollPanel, BorderLayout.CENTER);

		// Prepare health problem table model
		healthProblemTableModel = new HealthProblemTableModel(person);

		// Create health problem table
		healthProblemTable = new ZebraJTable(healthProblemTableModel);
		healthProblemTable.setPreferredScrollableViewportSize(new Dimension(225, 50));
		healthProblemTable.setCellSelectionEnabled(false);
		healthProblemScrollPanel.setViewportView(healthProblemTable);

		// 2015-06-08 Added sorting
		healthProblemTable.setAutoCreateRowSorter(true);
       if (!MainScene.OS.equals("linux")) {
        	healthProblemTable.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		}
		// 2015-06-08 Added setTableStyle()
		TableStyle.setTableStyle(healthProblemTable);
	}


	/**
	 * Updates the info on this panel.
	 */
	public void update() {

		TableStyle.setTableStyle(radiationTable);
		TableStyle.setTableStyle(medicationTable);
		TableStyle.setTableStyle(healthProblemTable);

		Person person = (Person) unit;
		PhysicalCondition condition = person.getPhysicalCondition();

		// Update fatigue if necessary.
		double newF = condition.getFatigue();
		if (fatigueCache *.95 > newF || fatigueCache *1.05 < newF) {
			fatigueCache = newF;
			fatigueLabel.setText(Msg.getString("TabPanelHealth.millisols", //$NON-NLS-1$
			        formatter.format(fatigueCache)));
		}

		// Update hunger if necessary.
		double newH = condition.getHunger();
		if (hungerCache *.95 > newH || hungerCache *1.05 < newH) {
			hungerCache = newH;
			hungerLabel.setText(Msg.getString("TabPanelHealth.millisols", //$NON-NLS-1$
			        formatter.format(hungerCache)));
		}

		// Update energy if necessary.
		double newEnergy = condition.getEnergy();
		if (energyCache *.98 > newEnergy || energyCache *1.02 < newEnergy   ) {
			energyCache = newEnergy;
			energyLabel.setText(Msg.getString("TabPanelHealth.kJ", //$NON-NLS-1$
			        formatter.format(energyCache)));
		}

		// Update stress if necessary.
		double newS = condition.getStress();
		if (stressCache *.95 > newS || stressCache*1.05 < newS) {
			stressCache = newS;
			stressLabel.setText(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
			        formatter.format(stressCache)));
		}

		// Update performance cache if necessary.
		double newP = person.getPerformanceRating();
		if (performanceCache *95D > newP || performanceCache *105D < newP) {
			performanceCache = newP * 100D;
			performanceLabel.setText(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
			        formatter.format(performanceCache)));
		}

		// Update medication table model.
		medicationTableModel.update();

		// Update health problem table model.
		healthProblemTableModel.update();

		// Update radiation dose table model
		radiationTableModel.update();
	}

	public class IconTextCellRemderer extends DefaultTableCellRenderer {
	    public Component getTableCellRendererComponent(JTable table,
	                                  Object value,
	                                  boolean isSelected,
	                                  boolean hasFocus,
	                                  int row,
	                                  int column) {
	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        //setToolTipText(...);	        
	        //BalloonToolTip balloonToolTip = new BalloonToolTip();
	        //balloonToolTip.createBalloonTip(value, ""); //$NON-NLS-1$

	        return this;
	    }
	}
	
	/**
	 * Internal class used as model for the radiation dose table.
	 */
	private static class RadiationTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private RadiationExposure radiation;

		private double dose[][];

		//private BalloonToolTip balloonToolTip = new BalloonToolTip();
		
		private RadiationTableModel(Person person) {
			radiation = person.getPhysicalCondition().getRadiationExposure();
			dose = radiation.getDose();
			
			//balloonToolTip.createBalloonTip(radiationTable, Msg.getString("TabPanelRadiation.tooltip")); //$NON-NLS-1$

		}

		public int getRowCount() {
			return 3;
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) {
			    dataType = String.class;
			}
			if (columnIndex == 1) {
			    dataType = String.class;
			}
			if (columnIndex == 2) {
			    dataType = String.class;
			}
			if (columnIndex == 3) {
			    dataType = String.class;
			}
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
			    return Msg.getString("TabPanelRadiation.column.interval"); //$NON-NLS-1$
			}
			else if (columnIndex == 1) {
			    return Msg.getString("TabPanelRadiation.column.BFO"); //$NON-NLS-1$
			}
			else if (columnIndex == 2) {
			    return Msg.getString("TabPanelRadiation.column.ocular"); //$NON-NLS-1$
			}
			else if (columnIndex == 3) {
			    return Msg.getString("TabPanelRadiation.column.skin"); //$NON-NLS-1$
			}
			else {
			    return null;
			}
		}

		public Object getValueAt(int row, int column) {
			String str = null;
			if (column == 0) {
				if (row == 0)
					str = "30-Day";
				else if (row == 1)
					str = "Annual";
				else if (row == 2)
					str = "Career";
			}
			else
				str = Math.round(dose[row][column-1] * 100.0D)/100.0D + "";
			return str;
		}

		public void update() {
			dose = radiation.getDose();
			fireTableDataChanged();
		}
		
	}


	/**
	 * Internal class used as model for the health problem table.
	 */
	private static class HealthProblemTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private PhysicalCondition condition;
		private Collection<?> problemsCache;

		private HealthProblemTableModel(Person person) {
			condition = person.getPhysicalCondition();
			problemsCache = condition.getProblems();
		}

		public int getRowCount() {
			return problemsCache.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) {
			    dataType = String.class;
			}
			if (columnIndex == 1) {
			    dataType = String.class;
			}
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
			    return Msg.getString("TabPanelHealth.column.problem"); //$NON-NLS-1$
			}
			else if (columnIndex == 1) {
			    return Msg.getString("TabPanelHealth.column.condition"); //$NON-NLS-1$
			}
			else {
			    return null;
			}
		}

		public Object getValueAt(int row, int column) {
			HealthProblem problem = null;
			if (row < problemsCache.size()) {
				Iterator<?> i = problemsCache.iterator();
				int count = 0;
				while (i.hasNext()) {
					HealthProblem prob = (HealthProblem) i.next();
					if (count == row) {
					    problem = prob;
					}
					count++;
				}
			}

			if (problem != null) {
				if (column == 0) {
				    return problem.getIllness().getType().toString();
				}
				else if (column == 1) {
					String conditionStr = problem.getStateString();
					if (!condition.isDead()) {
					    conditionStr = Msg.getString("TabPanelHealth.healthRating", //$NON-NLS-1$
					            conditionStr, Integer.toString(problem.getHealthRating()));
					}
					return conditionStr;
				}
				else {
				    return null;
				}
			}
			else {
			    return null;
			}
		}

		public void update() {
			// Make sure problems cache is current.
			if (!problemsCache.equals(condition.getProblems())) {
				problemsCache = condition.getProblems();
			}

			fireTableDataChanged();
		}
	}

	/**
	 * Internal class used as model for the medication table.
	 */
	private static class MedicationTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private PhysicalCondition condition;
		private List<Medication> medicationCache;

		private MedicationTableModel(Person person) {
			condition = person.getPhysicalCondition();
			medicationCache = condition.getMedicationList();
		}

		public int getRowCount() {
			return medicationCache.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) {
			    dataType = String.class;
			}
			else if (columnIndex == 1) {
			    dataType = Double.class;
			}
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
			    return Msg.getString("TabPanelHealth.column.medication"); //$NON-NLS-1$
			}
			else if (columnIndex == 1) {
			    return Msg.getString("TabPanelHealth.column.duration"); //$NON-NLS-1$
			}
			else {
			    return null;
			}
		}

		public Object getValueAt(int row, int column) {
			Object result = null;
			if (row < getRowCount()) {
				if (column == 0) {
				    result = medicationCache.get(row).getName();
				}
				else if (column == 1) {
				    result = medicationCache.get(row).getDuration();
				}
			}
			return result;
		}

		public void update() {
			// Make sure medication cache is current.
			if (!medicationCache.equals(condition.getMedicationList())) {
				medicationCache = condition.getMedicationList();
			}

			fireTableDataChanged();
		}
	}
}