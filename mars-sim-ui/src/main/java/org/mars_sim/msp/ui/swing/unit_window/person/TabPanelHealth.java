/**
 * Mars Simulation Project
 * TabPanelHealth.java
 * @version 3.1.0 2017-10-18
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.Medication;
import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;
//import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The HealthTabPanel is a tab panel for a person's health.
 */
@SuppressWarnings("serial")
public class TabPanelHealth
extends TabPanel {

	private static final String THIRTY_DAY = "30-Day";
	private static final String ANNUAL = "Annual";
	private static final String CAREER = "Career";

	// Data cache
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	private static int theme;
	private double fatigueCache;
	private double thirstCache;
	private double hungerCache;
	private double energyCache;
	private double stressCache;
	private double performanceCache;

	private WebLabel thirstLabel;
	private WebLabel fatigueLabel;
	private WebLabel hungerLabel;
	private WebLabel energyLabel;
	private WebLabel stressLabel;
	private WebLabel performanceLabel;
	
	/** The sleep hour text field. */	
	private WebTextField sleepTF;
	
	private MedicationTableModel medicationTableModel;
	private HealthProblemTableModel healthProblemTableModel;
	private RadiationTableModel radiationTableModel;
	private SleepTableModel sleepTableModel;
	
	private JTable radiationTable;
	private JTable medicationTable;
	private JTable healthProblemTable;
	private JTable sleepTable;

	private DecimalFormat formatter = new DecimalFormat(Msg.getString("TabPanelHealth.decimalFormat")); //$NON-NLS-1$

	/** The Person instance. */
	private Person person = null;
	
	/** The PhysicalCondition instance. */
	private PhysicalCondition condition;

	private Font font = new Font("SansSerif", Font.ITALIC, 12);
	
	protected String[] radiationToolTips = {
		    "Exposure Interval",
		    "[Max for BFO]  30-Day :  250; Annual :  500; Career : 1000",
		    "[Max for Eye]  30-Day : 1000; Annual : 2000; Career : 4000",
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

		person = (Person) unit;
	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		condition = person.getPhysicalCondition();
		//PhysicalCondition condition = ((Person) unit).getPhysicalCondition();
		
		// Create health label panel.
		WebPanel healthLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(healthLabelPanel, BorderLayout.NORTH);

		// Prepare health label
		WebLabel healthLabel = new WebLabel(Msg.getString("TabPanelHealth.label"), WebLabel.CENTER); //$NON-NLS-1$
		healthLabel.setFont(new Font("Serif", Font.BOLD, 16));
		healthLabelPanel.add(healthLabel);

		// Prepare condition panel
		WebPanel conditionPanel = new WebPanel(new SpringLayout());//GridLayout(5, 2, 0, 0));
//		conditionPanel.setBorder(new MarsPanelBorder());
		//conditionPanel.setSize(180, 60);
		topContentPanel.add(conditionPanel, BorderLayout.CENTER);

		// Prepare fatigue name label
		WebLabel fatigueNameLabel = new WebLabel(Msg.getString("TabPanelHealth.fatigue"), WebLabel.RIGHT); //$NON-NLS-1$
		conditionPanel.add(fatigueNameLabel);

		// Prepare fatigue label
		fatigueCache = condition.getFatigue();
		fatigueLabel = new WebLabel(Msg.getString("TabPanelHealth.millisols", //$NON-NLS-1$
		        formatter.format(fatigueCache)), WebLabel.LEFT);
		conditionPanel.add(fatigueLabel);

		// Prepare hunger name label
		WebLabel thirstNameLabel = new WebLabel(Msg.getString("TabPanelHealth.thirst"), WebLabel.RIGHT); //$NON-NLS-1$
		conditionPanel.add(thirstNameLabel);

		// Prepare hunger label
		thirstCache = condition.getThirst();
		thirstLabel = new WebLabel(Msg.getString("TabPanelHealth.millisols", //$NON-NLS-1$
		        formatter.format(thirstCache)), WebLabel.LEFT);
		conditionPanel.add(thirstLabel);
		
		// Prepare hunger name label
		WebLabel hungerNameLabel = new WebLabel(Msg.getString("TabPanelHealth.hunger"), WebLabel.RIGHT); //$NON-NLS-1$
		conditionPanel.add(hungerNameLabel);

		// Prepare hunger label
		hungerCache = condition.getHunger();
		hungerLabel = new WebLabel(Msg.getString("TabPanelHealth.millisols", //$NON-NLS-1$
		        formatter.format(hungerCache)), WebLabel.LEFT);
		conditionPanel.add(hungerLabel);

		//
		// Prepare energy name label
		WebLabel energyNameLabel = new WebLabel(Msg.getString("TabPanelHealth.energy"), WebLabel.RIGHT); //$NON-NLS-1$
		conditionPanel.add(energyNameLabel);

		// Prepare energy label
		energyCache = condition.getEnergy();
		energyLabel = new WebLabel(Msg.getString("TabPanelHealth.kJ", //$NON-NLS-1$
		        formatter.format(energyCache)), WebLabel.LEFT);
		conditionPanel.add(energyLabel);


		// Prepare stress name label
		WebLabel stressNameLabel = new WebLabel(Msg.getString("TabPanelHealth.stress"), WebLabel.RIGHT); //$NON-NLS-1$
		conditionPanel.add(stressNameLabel);

		// Prepare stress label
		stressCache = condition.getStress();
		stressLabel = new WebLabel(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
		        formatter.format(stressCache)), WebLabel.LEFT);
		conditionPanel.add(stressLabel);

		// Prepare performance rating label
		WebLabel performanceNameLabel = new WebLabel(Msg.getString("TabPanelHealth.performance"), WebLabel.RIGHT); //$NON-NLS-1$
		conditionPanel.add(performanceNameLabel);

		// Performance rating label
		performanceCache = person.getPerformanceRating() * 100D;
		performanceLabel = new WebLabel(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
		        formatter.format(performanceCache)), WebLabel.LEFT);
		conditionPanel.add(performanceLabel);

		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(conditionPanel,
		                                3, 4, //rows, cols
		                                10, 4,        //initX, initY
		                                15, 3);       //xPad, yPad
		

		// Prepare SpringLayout for info panel.
		WebPanel springPanel = new WebPanel(new SpringLayout());//GridLayout(4, 2, 0, 0));
//		infoPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(springPanel, BorderLayout.SOUTH);
		
		// Prepare sleep hour name label
		WebLabel sleepHrLabel = new WebLabel(Msg.getString("TabPanelFavorite.sleepHour"), WebLabel.RIGHT); //$NON-NLS-1$
//		sleepLabel.setFont(font);
		springPanel.add(sleepHrLabel);

		// Checks the two best sleep hours
    	int bestSleepTime[] = person.getPreferredSleepHours();		
		WebPanel wrapper5 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		Arrays.sort(bestSleepTime);
		
		// Prepare sleep hour TF
		String text = "";
		int size = bestSleepTime.length;
		for (int i=0; i<size; i++) {
			text += bestSleepTime[i] + "";
			if (i != size - 1)
				text += " and ";
		}
		sleepTF = new WebTextField(text);
		sleepTF.setEditable(false);
		sleepTF.setColumns(8);
		//activityTF.requestFocus();
		sleepTF.setCaretPosition(0);
		wrapper5.add(sleepTF);
		springPanel.add(wrapper5);

		TooltipManager.setTooltip (sleepTF, "Time in millisols", TooltipWay.down); //$NON-NLS-1$
				
		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(springPanel,
		                                1, 2, //rows, cols
		                                120, 10,        //initX, initY
		                                7, 3);       //xPad, yPad
	
		
		// Add radiation dose info
		// Prepare radiation panel
		WebPanel radiationPanel = new WebPanel(new BorderLayout(0, 0));//new GridLayout(2, 1, 0, 0));
//		radiationPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(radiationPanel, BorderLayout.NORTH);

		// Prepare radiation label
		WebLabel radiationLabel = new WebLabel(Msg.getString("TabPanelHealth.rad"), WebLabel.CENTER); //$NON-NLS-1$
		radiationLabel.setFont(font);
		radiationPanel.add(radiationLabel, BorderLayout.NORTH);
		TooltipManager.setTooltip (radiationLabel, Msg.getString("TabPanelHealth.radiation.tooltip"), TooltipWay.down); //$NON-NLS-1$
			 
		// Prepare radiation scroll panel
		WebScrollPane radiationScrollPanel = new WebScrollPane();
		radiationPanel.add(radiationScrollPanel, BorderLayout.CENTER);

		// Prepare radiation table model
		radiationTableModel = new RadiationTableModel(person);

		// Create radiation table
		radiationTable = new ZebraJTable(radiationTableModel);
//		{
		    // Implement radiation table header tool tips
//		    protected JTableHeader createDefaultTableHeader() {
//		        return new JTableHeader(columnModel) {
//		            public String getToolTipText(MouseEvent e) {
//		                //String tip = null;
//		                java.awt.Point p = e.getPoint();
//		                int index = columnModel.getColumnIndexAtX(p.x);
//		                if (index > -1) {
//			                int realIndex = columnModel.getColumn(index).getModelIndex();
//			                return radiationToolTips[realIndex];
//		            	}
//		                else {
//		                	return Msg.getString("TabPanelHealth.tooltip");
//		                }
//		            }
//		        };
//		    }		        
//		};
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		radiationTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		radiationTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		radiationTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		radiationTable.getColumnModel().getColumn(3).setCellRenderer(renderer);

		radiationTable.setPreferredScrollableViewportSize(new Dimension(225, 70));
		radiationTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		radiationTable.getColumnModel().getColumn(1).setPreferredWidth(80);
		radiationTable.getColumnModel().getColumn(2).setPreferredWidth(70);
		radiationTable.getColumnModel().getColumn(2).setPreferredWidth(30);
		radiationTable.setRowSelectionAllowed(true);
		radiationScrollPanel.setViewportView(radiationTable);

		// Added sorting
		radiationTable.setAutoCreateRowSorter(true);
        //if (!MainScene.OS.equals("linux")) {
        //	radiationTable.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		//}
		// Add setTableStyle()
		TableStyle.setTableStyle(radiationTable);


		// Prepare table panel.
		WebPanel tablePanel = new WebPanel(new GridLayout(3, 1));
		centerContentPanel.add(tablePanel, BorderLayout.SOUTH);

		// Prepare sleep time panel
		WebPanel sleepPanel = new WebPanel(new BorderLayout(0, 0));
//		sleepPanel.setBorder(new MarsPanelBorder());
		tablePanel.add(sleepPanel);

		// Prepare sleep time label
		WebLabel sleepLabel = new WebLabel(Msg.getString("TabPanelHealth.sleep"), WebLabel.CENTER); //$NON-NLS-1$
		sleepLabel.setFont(font);
		sleepPanel.add(sleepLabel, BorderLayout.NORTH);

		// Prepare sleep time scroll panel
		WebScrollPane sleepScrollPanel = new WebScrollPane();
		sleepPanel.add(sleepScrollPanel, BorderLayout.CENTER);

		// Prepare sleep time table model
		sleepTableModel = new SleepTableModel(person);
		
		// Create sleep time table
		sleepTable = new ZebraJTable(sleepTableModel);
		sleepTable.setPreferredScrollableViewportSize(new Dimension(225, 90));
		sleepTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		sleepTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		sleepTable.setRowSelectionAllowed(true);
		sleepScrollPanel.setViewportView(sleepTable);

		DefaultTableCellRenderer sleepRenderer = new DefaultTableCellRenderer();
		sleepRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		sleepTable.getColumnModel().getColumn(0).setCellRenderer(sleepRenderer);
		sleepTable.getColumnModel().getColumn(1).setCellRenderer(sleepRenderer);
		
		// Add sorting
		sleepTable.setAutoCreateRowSorter(true);
        //if (!MainScene.OS.equals("linux")) {
        // 	sleepTable.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		//}
		TableStyle.setTableStyle(sleepTable);
		

		// Prepare health problem panel
		WebPanel healthProblemPanel = new WebPanel(new BorderLayout(0, 0));
//		healthProblemPanel.setBorder(new MarsPanelBorder());
		tablePanel.add(healthProblemPanel);

		// Prepare health problem label
		WebLabel healthProblemLabel = new WebLabel(Msg.getString("TabPanelHealth.healthProblems"), WebLabel.CENTER); //$NON-NLS-1$
		healthProblemLabel.setPadding(7, 0, 0, 0);
		healthProblemLabel.setFont(font);
		healthProblemPanel.add(healthProblemLabel, BorderLayout.NORTH);

		// Prepare health problem scroll panel
		WebScrollPane healthProblemScrollPanel = new WebScrollPane();
		healthProblemPanel.add(healthProblemScrollPanel, BorderLayout.CENTER);

		// Prepare health problem table model
		healthProblemTableModel = new HealthProblemTableModel(person);

		// Create health problem table
		healthProblemTable = new ZebraJTable(healthProblemTableModel);
		healthProblemTable.setPreferredScrollableViewportSize(new Dimension(225, 10));
		healthProblemTable.setRowSelectionAllowed(true);
		healthProblemScrollPanel.setViewportView(healthProblemTable);

		// Add sorting
		healthProblemTable.setAutoCreateRowSorter(true);
        //if (!MainScene.OS.equals("linux")) {
        // 	healthProblemTable.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		//}
		TableStyle.setTableStyle(healthProblemTable);
		
		
		// Prepare medication panel.
		WebPanel medicationPanel = new WebPanel(new BorderLayout());
//		medicationPanel.setBorder(new MarsPanelBorder());
		tablePanel.add(medicationPanel);

		// Prepare medication label.
		WebLabel medicationLabel = new WebLabel(Msg.getString("TabPanelHealth.medication"), WebLabel.CENTER); //$NON-NLS-1$
		medicationLabel.setPadding(7, 0, 0, 0);
		medicationLabel.setFont(font);
		medicationPanel.add(medicationLabel, BorderLayout.NORTH);

		// Prepare medication scroll panel
		WebScrollPane medicationScrollPanel = new WebScrollPane();
		medicationPanel.add(medicationScrollPanel, BorderLayout.CENTER);

		// Prepare medication table model.
		medicationTableModel = new MedicationTableModel(person);

		// Prepare medication table.
		medicationTable = new ZebraJTable(medicationTableModel);
		medicationTable.setPreferredScrollableViewportSize(new Dimension(225, 10));
		medicationTable.setRowSelectionAllowed(true);
		medicationScrollPanel.setViewportView(medicationTable);

		// Add sorting
		medicationTable.setAutoCreateRowSorter(true);
       //if (!MainScene.OS.equals("linux")) {
        //	medicationTable.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		//}
		TableStyle.setTableStyle(medicationTable);

	}


	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();

		int t = 0;//MainScene.getTheme();		
		if (theme != t) {
			theme = t;
			TableStyle.setTableStyle(radiationTable);
			TableStyle.setTableStyle(medicationTable);
			TableStyle.setTableStyle(healthProblemTable);
			TableStyle.setTableStyle(sleepTable);
		}
		
		// Update fatigue if necessary.
		double newF = Math.round(condition.getFatigue()* 1.0)/1.0;
		//if (fatigueCache *.95 > newF || fatigueCache *1.05 < newF) {
		if (fatigueCache != newF) {
			fatigueCache = newF;
			fatigueLabel.setText(Msg.getString("TabPanelHealth.millisols", //$NON-NLS-1$
			        formatter.format(fatigueCache)));
		}

		// Update thirst if necessary.
		double newT = Math.round(condition.getThirst()* 1.0)/1.0;
		//if (thirstCache *.95 > newT || thirstCache *1.05 < newT) {
		if (thirstCache != newT) {
			thirstCache = newT;
			thirstLabel.setText(Msg.getString("TabPanelHealth.millisols", //$NON-NLS-1$
			        formatter.format(thirstCache)));
		}
		
		// Update hunger if necessary.
		double newH = Math.round(condition.getHunger()* 1.0)/1.0;
		//if (hungerCache *.95 > newH || hungerCache *1.05 < newH) {
		if (hungerCache != newH) {
			hungerCache = newH;
			hungerLabel.setText(Msg.getString("TabPanelHealth.millisols", //$NON-NLS-1$
			        formatter.format(hungerCache)));
		}

		// Update energy if necessary.
		double newEnergy = Math.round(condition.getEnergy()* 1.0)/1.0;
		//if (energyCache *.98 > newEnergy || energyCache *1.02 < newEnergy   ) {
		if (energyCache != newEnergy) {
			energyCache = newEnergy;
			energyLabel.setText(Msg.getString("TabPanelHealth.kJ", //$NON-NLS-1$
			        formatter.format(energyCache)));
		}

		// Update stress if necessary.
		double newS = Math.round(condition.getStress()* 1.0)/1.0;
		//if (stressCache *.95 > newS || stressCache*1.05 < newS) {
		if (stressCache != newS) {
			stressCache = newS;
			stressLabel.setText(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
			        formatter.format(stressCache)));
		}

		// Update performance cache if necessary.
		double newP = Math.round(condition.getPerformanceFactor() * 100)/1.0;
		//if (performanceCache *95D > newP || performanceCache *105D < newP) {
		if (performanceCache != newP) {
			performanceCache = newP;
			performanceLabel.setText(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
			        formatter.format(performanceCache)));
		}
		
		// Checks the two best sleep hours
    	int bestSleepTime[] = person.getPreferredSleepHours();		
		Arrays.sort(bestSleepTime);
		
		// Prepare sleep hour TF
		String text = "";
		int size = bestSleepTime.length;
		for (int i=0; i<size; i++) {
			text += bestSleepTime[i] + "";
			if (i != size - 1)
				text += " and ";
		}
		sleepTF.setText(text);
		
		// Update medication table model.
		medicationTableModel.update();

		// Update health problem table model.
		healthProblemTableModel.update();

		// Update radiation dose table model
		radiationTableModel.update();
		
		// Update sleep time table model
		sleepTableModel.update();
    	
	}

//	public class IconTextCellRenderer extends DefaultTableCellRenderer {
//	    public Component getTableCellRendererComponent(WebTable table,
//	                                  Object value,
//	                                  boolean isSelected,
//	                                  boolean hasFocus,
//	                                  int row,
//	                                  int column) {
//	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//	        return this;
//	    }
//	}

	/**
	 * Internal class used as model for the radiation dose table.
	 */
	private static class RadiationTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private RadiationExposure radiation;

		private double dose[][];

		private RadiationTableModel(Person person) {
			radiation = person.getPhysicalCondition().getRadiationExposure();
			dose = radiation.getDose();

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
			    return Msg.getString("TabPanelHealth.column.interval"); //$NON-NLS-1$
			}
			else if (columnIndex == 1) {
			    return Msg.getString("TabPanelHealth.column.BFO"); //$NON-NLS-1$
			}
			else if (columnIndex == 2) {
			    return Msg.getString("TabPanelHealth.column.ocular"); //$NON-NLS-1$
			}
			else if (columnIndex == 3) {
			    return Msg.getString("TabPanelHealth.column.skin"); //$NON-NLS-1$
			}
			else {
			    return null;
			}
		}

		public Object getValueAt(int row, int column) {
			String str = null;
			if (column == 0) {
				if (row == 0)
					str = THIRTY_DAY;
				else if (row == 1)
					str = ANNUAL;
				else if (row == 2)
					str = CAREER;
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
	 * Internal class used as model for the sleep time table.
	 */
	private static class SleepTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private DecimalFormat fmt = new DecimalFormat("0.0");

		private CircadianClock circadian;
		private Map<Integer, Double> sleepTime;

		private SleepTableModel(Person person) {
			circadian = person.getCircadianClock();
			sleepTime = circadian.getSleepTime();
		}

		public int getRowCount() {
			return sleepTime.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) {
			    dataType = Integer.class;
			}
			else if (columnIndex == 1) {
			    dataType = Double.class;
			}
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
			    return "Mission Sol"; 
			}
			else if (columnIndex == 1) {
			    return "Sleep Time [in millisols]";
			}
			else {
			    return null;
			}
		}

		public Object getValueAt(int row, int column) {
			Object result = null;
			if (row < getRowCount()) {
				if (column == 0) {
				    result = row + 1;
				}
				else if (column == 1) {
					if (sleepTime.containsKey(row + 1))
						result = fmt.format(sleepTime.get(row + 1));
					else
						result = fmt.format(0);
				}
			}
			return result;
		}

		public void update() {
			sleepTime = circadian.getSleepTime();
			
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