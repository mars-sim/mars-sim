/*
 * Mars Simulation Project
 * TabPanelHealth.java
 * @date 2022-07-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.Medication;
import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.core.person.health.RadiationExposure.DoseHistory;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * The HealthTabPanel is a tab panel for a person's health.
 */
@SuppressWarnings("serial")
public class TabPanelHealth
extends TabPanel {

	private static final String HEALTH_ICON = "health"; //$NON-NLS-1$
	
	private static final String THIRTY_DAY = "30-Day";
	private static final String ANNUAL = "Annual";
	private static final String CAREER = "Career";
	private static final DecimalFormat DECIMAL_MSOLS = new DecimalFormat("0 msols");


	private int fatigueCache;
	private int thirstCache;
	private int hungerCache;
	private int energyCache;
	private int stressCache;
	private int performanceCache;
	private int leptinCache;
	private int leptinTCache;
	private int ghrelinCache;
	private int ghrelinTCache;
	
	private JLabel thirstLabel;
	private JLabel fatigueLabel;
	private JLabel hungerLabel;
	private JLabel energyLabel;
	private JLabel stressLabel;
	private JLabel performanceLabel;
	
	private JLabel leptinLabel;
	private JLabel ghrelinLabel;
	private JLabel leptinTLabel;
	private JLabel ghrelinTLabel;
	
	/** The sleep hour text field. */	
	private JTextField sleepTF;
	
	private MedicationTableModel medicationTableModel;
	private HealthProblemTableModel healthProblemTableModel;
	private RadiationTableModel radiationTableModel;
	private SleepExerciseTableModel sleepExerciseTableModel;
	private FoodTableModel foodTableModel;
	
	private JTable radiationTable;
	private JTable medicationTable;
	private JTable healthProblemTable;
	private JTable sleepExerciseTable;
	private JTable foodTable;
	
	/** The Person instance. */
	private Person person = null;
	
	/** The PhysicalCondition instance. */
	private PhysicalCondition condition;
	private CircadianClock circadianClock;
	
	private static String[] RADIATION_TOOL_TIPS = {
		    "Exposure Interval",
		    "[Max for BFO]  30-Day :  250; Annual :  500; Career : 1000",
		    "[Max for Eye]  30-Day : 1000; Annual : 2000; Career : 4000",
		    "[Max for Skin] 30-Day : 1500; Annual : 3000; Career : 6000"};


	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelHealth(Person unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(HEALTH_ICON),
			Msg.getString("TabPanelHealth.title"), //$NON-NLS-1$
			desktop
		);

		person = unit;
		condition = person.getPhysicalCondition();
		circadianClock = person.getCircadianClock();
	}

	@Override
	protected void buildUI(JPanel content) {
				
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

		// Prepare condition panel
		AttributePanel conditionPanel = new AttributePanel(5, 2);
		northPanel.add(conditionPanel);
		
		fatigueCache = (int)condition.getFatigue();
		fatigueLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.fatigue"),
										DECIMAL_MSOLS.format(fatigueCache), null);
		thirstCache = (int)condition.getThirst();
		fatigueLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.thirst"),
										DECIMAL_MSOLS.format(thirstCache), null);
		hungerCache = (int)condition.getHunger();
		hungerLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.hunger"),
										DECIMAL_MSOLS.format(thirstCache), null);
		energyCache = (int)condition.getEnergy();
		energyLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.energy"),
										StyleManager.DECIMAL_PLACES0.format(energyCache) + " kj", null);
		stressCache = (int)condition.getStress();	
		stressLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.stress"),
										StyleManager.DECIMAL_PERC.format(stressCache), null);
		performanceCache = (int)(person.getPerformanceRating() * 100);
		performanceLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.performance"),
										StyleManager.DECIMAL_PERC.format(performanceCache), null);
		leptinCache = (int)(circadianClock.getLeptin());
		leptinLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.leptin"),
										DECIMAL_MSOLS.format(leptinCache), null);
		ghrelinCache = (int)(circadianClock.getGhrelin());
		ghrelinLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.ghrelin"),
										DECIMAL_MSOLS.format(ghrelinCache), null);		
		leptinTCache = (int)(circadianClock.getLeptinT());
		leptinTLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.leptin.threshold"),
										DECIMAL_MSOLS.format(leptinTCache), null);	
		ghrelinTCache = (int)(circadianClock.getGhrelinT());
		ghrelinTLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.ghrelin.threshold"),
										DECIMAL_MSOLS.format(ghrelinTCache), null);	
		

		// Prepare SpringLayout for info panel.
		JPanel springPanel = new JPanel(new SpringLayout());
		northPanel.add(springPanel);
		
		// Prepare sleep hour name label
		JLabel sleepHrLabel = new JLabel(Msg.getString("TabPanelFavorite.sleepHour"), SwingConstants.RIGHT); //$NON-NLS-1$
		sleepHrLabel.setFont(StyleManager.getLabelFont());
		springPanel.add(sleepHrLabel);

		// Checks the 3 best sleep time
    	int bestSleepTime[] = person.getPreferredSleepHours();		
		JPanel wrapper5 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		Arrays.sort(bestSleepTime);
		
		// Prepare sleep time TF
		StringBuilder text = new StringBuilder();
		int size = bestSleepTime.length;
		for (int i=0; i<size; i++) {
			text.append(bestSleepTime[i])
			.append(" (") 
			.append(person.getSleepWeight(bestSleepTime[i]))
			.append(")");
			if (i != size - 1)
				text.append(",  ");
		}
		sleepTF = new JTextField(text.toString());
		sleepTF.setEditable(false);
		sleepTF.setColumns(20);
		sleepTF.setCaretPosition(0);
		wrapper5.add(sleepTF);
		springPanel.add(wrapper5);

		sleepTF.setToolTipText("3 best times to go to bed [msol (weight)]"); //$NON-NLS-1$
				
		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(springPanel,
		                                1, 2, //rows, cols
		                                50, 10,        //initX, initY
		                                5, 3);       //xPad, yPad
	
		content.add(northPanel, BorderLayout.NORTH);
		
		// Panel of vertical tables
        JPanel tablesPanel = new JPanel(new BorderLayout());
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));
		content.add(tablesPanel, BorderLayout.CENTER);

		// Add radiation dose info
		// Prepare radiation panel
		JPanel radiationPanel = new JPanel(new BorderLayout(0, 0));
		tablesPanel.add(radiationPanel, BorderLayout.NORTH);

		// Prepare radiation label
		JLabel radiationLabel = new JLabel(Msg.getString("TabPanelHealth.rad"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(radiationLabel);
		radiationPanel.add(radiationLabel, BorderLayout.NORTH);
		radiationLabel.setToolTipText(Msg.getString("TabPanelHealth.radiation.tooltip")); //$NON-NLS-1$
			 
		// Prepare radiation scroll panel
		JScrollPane radiationScrollPanel = new JScrollPane();
		radiationPanel.add(radiationScrollPanel, BorderLayout.CENTER);

		// Prepare radiation table model
		radiationTableModel = new RadiationTableModel(condition);

		// Create radiation table
		radiationTable = new JTable(radiationTableModel) {

            //Implement table cell tool tips.           
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
//              int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
				if (colIndex < RADIATION_TOOL_TIPS.length) {
                    tip = RADIATION_TOOL_TIPS[colIndex];
				}
                return tip;
            }
        };
	
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		TableColumnModel rModel = radiationTable.getColumnModel();
		rModel.getColumn(0).setCellRenderer(renderer);
		rModel.getColumn(1).setCellRenderer(renderer);
		rModel.getColumn(2).setCellRenderer(renderer);
		rModel.getColumn(3).setCellRenderer(renderer);

		radiationTable.setPreferredScrollableViewportSize(new Dimension(225, 75));
		rModel.getColumn(0).setPreferredWidth(40);
		rModel.getColumn(1).setPreferredWidth(100);
		rModel.getColumn(2).setPreferredWidth(65);
		rModel.getColumn(3).setPreferredWidth(35);
		radiationTable.setRowSelectionAllowed(true);
		radiationScrollPanel.setViewportView(radiationTable);
		
		// Added sorting
		radiationTable.setAutoCreateRowSorter(true);

		// Prepare sleep time panel
		JPanel sleepPanel = new JPanel(new BorderLayout(0, 0));
		tablesPanel.add(sleepPanel, BorderLayout.CENTER);

		// Prepare sleep time label
		JLabel sleepLabel = new JLabel(Msg.getString("TabPanelHealth.sleepExercise"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(sleepLabel);
		sleepPanel.add(sleepLabel, BorderLayout.NORTH);

		// Prepare sleep time scroll panel
		JScrollPane sleepScrollPanel = new JScrollPane();
		sleepPanel.add(sleepScrollPanel, BorderLayout.CENTER);

		// Prepare sleep time table model
		sleepExerciseTableModel = new SleepExerciseTableModel(circadianClock);
		
		// Create sleep time table
		sleepExerciseTable = new JTable(sleepExerciseTableModel);
		TableColumnModel sModel = sleepExerciseTable.getColumnModel();
		sleepExerciseTable.setPreferredScrollableViewportSize(new Dimension(225, 90));
		sModel.getColumn(0).setPreferredWidth(10);
		sModel.getColumn(1).setPreferredWidth(70);
		sModel.getColumn(2).setPreferredWidth(70);
		
		sleepExerciseTable.setRowSelectionAllowed(true);
		sleepScrollPanel.setViewportView(sleepExerciseTable);

		DefaultTableCellRenderer sleepRenderer = new DefaultTableCellRenderer();
		sleepRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		sModel.getColumn(0).setCellRenderer(sleepRenderer);
		sModel.getColumn(1).setCellRenderer(sleepRenderer);
		sModel.getColumn(2).setCellRenderer(sleepRenderer);
		
		// Add sorting
		sleepExerciseTable.setAutoCreateRowSorter(true);
	
		/////////////////////////////////////////////////////////
		
		// Prepare exercise time panel
		JPanel foodPanel = new JPanel(new BorderLayout(0, 0));
		tablesPanel.add(foodPanel, BorderLayout.SOUTH);

		// Prepare exercise time label
		JLabel foodLabel = new JLabel(Msg.getString("TabPanelHealth.food"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(foodLabel);
		foodPanel.add(foodLabel, BorderLayout.NORTH);

		// Prepare exercise time scroll panel
		JScrollPane foodScrollPanel = new JScrollPane();
		foodPanel.add(foodScrollPanel, BorderLayout.CENTER);

		// Prepare exercise time table model
		foodTableModel = new FoodTableModel(condition);
		
		// Create exercise time table
		foodTable = new JTable(foodTableModel);
		foodTable.setPreferredScrollableViewportSize(new Dimension(225, 90));
		TableColumnModel fModel = foodTable.getColumnModel();
		fModel.getColumn(0).setPreferredWidth(10);
		fModel.getColumn(1).setPreferredWidth(50);
		fModel.getColumn(2).setPreferredWidth(50);
		fModel.getColumn(3).setPreferredWidth(50);
		fModel.getColumn(4).setPreferredWidth(50);

		foodTable.setRowSelectionAllowed(true);
		foodScrollPanel.setViewportView(foodTable);

		DefaultTableCellRenderer foodRenderer = new DefaultTableCellRenderer();
		foodRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		fModel.getColumn(0).setCellRenderer(foodRenderer);
		fModel.getColumn(1).setCellRenderer(foodRenderer);
		fModel.getColumn(2).setCellRenderer(foodRenderer);
		fModel.getColumn(3).setCellRenderer(foodRenderer);	
		fModel.getColumn(4).setCellRenderer(foodRenderer);	
		
		// Add sorting
		foodTable.setAutoCreateRowSorter(true);
		
		/////////////////////////////////////////////////////////		
		
		
		// Prepare health problem panel
		JPanel healthProblemPanel = new JPanel(new BorderLayout(0, 0));
		tablesPanel.add(healthProblemPanel);

		// Prepare health problem label
		JLabel healthProblemLabel = new JLabel(Msg.getString("TabPanelHealth.healthProblems"), SwingConstants.CENTER); //$NON-NLS-1$
		//healthProblemLabel.setPadding(7, 0, 0, 0);
		StyleManager.applySubHeading(healthProblemLabel);
		healthProblemPanel.add(healthProblemLabel, BorderLayout.NORTH);

		// Prepare health problem scroll panel
		JScrollPane healthProblemScrollPanel = new JScrollPane();
		healthProblemPanel.add(healthProblemScrollPanel, BorderLayout.CENTER);

		// Prepare health problem table model
		healthProblemTableModel = new HealthProblemTableModel(condition);

		// Create health problem table
		healthProblemTable = new JTable(healthProblemTableModel);
		healthProblemTable.setPreferredScrollableViewportSize(new Dimension(225, 90));
		healthProblemTable.setRowSelectionAllowed(true);
		healthProblemScrollPanel.setViewportView(healthProblemTable);

		// Add sorting
		healthProblemTable.setAutoCreateRowSorter(true);
		
		
		// Prepare medication panel.
		JPanel medicationPanel = new JPanel(new BorderLayout());
		tablesPanel.add(medicationPanel);

		// Prepare medication label.
		JLabel medicationLabel = new JLabel(Msg.getString("TabPanelHealth.medication"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(medicationLabel);
		medicationPanel.add(medicationLabel, BorderLayout.NORTH);

		// Prepare medication scroll panel
		JScrollPane medicationScrollPanel = new JScrollPane();
		medicationPanel.add(medicationScrollPanel, BorderLayout.CENTER);

		// Prepare medication table model.
		medicationTableModel = new MedicationTableModel(condition);

		// Prepare medication table.
		medicationTable = new JTable(medicationTableModel);
		medicationTable.setPreferredScrollableViewportSize(new Dimension(225, 90));
		medicationTable.setRowSelectionAllowed(true);
		medicationScrollPanel.setViewportView(medicationTable);

		// Add sorting
		medicationTable.setAutoCreateRowSorter(true);
		
		// Update at least one before displaying it
		update();
	}


	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		// Update fatigue if necessary.
		int newF = (int)condition.getFatigue();
		if (fatigueCache != newF) {
			fatigueCache = newF;
			fatigueLabel.setText(DECIMAL_MSOLS.format(newF));
		}

		// Update thirst if necessary.
		int newT = (int)condition.getThirst();
		if (thirstCache != newT) {
			thirstCache = newT;
			thirstLabel.setText(DECIMAL_MSOLS.format(newT));
		}
		
		// Update hunger if necessary.
		int newH = (int)condition.getHunger();
		if (hungerCache != newH) {
			hungerCache = newH;
			hungerLabel.setText(DECIMAL_MSOLS.format(newH));
		}

		// Update energy if necessary.
		int newEnergy = (int)condition.getEnergy();
		if (energyCache != newEnergy) {
			energyCache = newEnergy;
			energyLabel.setText(StyleManager.DECIMAL_PLACES0.format(energyCache) + " kj");
		}

		// Update stress if necessary.
		int newS = (int)condition.getStress();
		if (stressCache != newS) {
			stressCache = newS;
			stressLabel.setText(StyleManager.DECIMAL_PERC.format(newS));
		}

		// Update performance cache if necessary.
		int newP = (int)(condition.getPerformanceFactor() * 100);
		if (performanceCache != newP) {
			performanceCache = newP;
			performanceLabel.setText(StyleManager.DECIMAL_PERC.format(newP));
		}
		
		// Update leptin if necessary.
		int newL = (int)(circadianClock.getLeptin());
		if (leptinCache != newL) {
			leptinCache = newL;
			leptinLabel.setText(DECIMAL_MSOLS.format(newL));
		}		
		
		// Update ghrelin if necessary.
		int newG = (int)(circadianClock.getGhrelin());
		if (ghrelinCache != newG) {
			ghrelinCache = newG;
			ghrelinLabel.setText(DECIMAL_MSOLS.format(newG));
		}		
		
		// Update leptin threshold if necessary.
		int newLT = (int)(circadianClock.getLeptinT());
		if (leptinTCache != newLT) {
			leptinTCache = newLT;
			leptinTLabel.setText(DECIMAL_MSOLS.format(newLT));
		}		
		
		// Update ghrelin threshold if necessary.
		int newGT = (int)(circadianClock.getGhrelinT());
		if (ghrelinTCache != newGT) {
			ghrelinTCache = newGT;
			ghrelinTLabel.setText(DECIMAL_MSOLS.format(newGT));
		}
		
		// Checks the 3 best sleep times
    	int bestSleepTime[] = person.getPreferredSleepHours();		
		Arrays.sort(bestSleepTime);
		
		// Prepare sleep time TF
		StringBuilder text = new StringBuilder();
		int size = bestSleepTime.length;
		for (int i=0; i<size; i++) {
			text.append(bestSleepTime[i])
			.append(" (") 
			.append(person.getSleepWeight(bestSleepTime[i]))
			.append(")");
			if (i != size - 1)
				text.append(",  ");
		}
	
		if (!sleepTF.getText().equalsIgnoreCase(text.toString()))
			sleepTF.setText(text.toString());
		
		// Update medication table model.
		medicationTableModel.update(condition);

		// Update health problem table model.
		healthProblemTableModel.update(condition);

		// Update radiation dose table model
		radiationTableModel.update();
		
		// Update sleep time table model
		sleepExerciseTableModel.update(circadianClock);
		
		// Update food table model
		foodTableModel.update(condition);
	}

	/**
	 * Internal class used as model for the radiation dose table.
	 */
	private static class RadiationTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private RadiationExposure radiation;

		private DoseHistory[] dose;

		private RadiationTableModel(PhysicalCondition condition) {
			radiation = condition.getRadiationExposure();
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
			else {
				DoseHistory active = dose[column-1];
				double value = switch(row) {
					case 0 -> active.getThirtyDay();
					case 1 -> active.getAnnual();
					case 2 -> active.getCareer();
					default -> 0D;
				};
			
				str = Math.round(value * 100.0D)/100.0D + "";
			}
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

		private Collection<?> problemsCache;
		private boolean isDead;

		private HealthProblemTableModel(PhysicalCondition condition) {
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
					if (!isDead) {
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

		public void update(PhysicalCondition condition) {
			// Make sure problems cache is current.
			if (!problemsCache.equals(condition.getProblems())) {
				problemsCache = condition.getProblems();
			}
			isDead = condition.isDead();

			fireTableDataChanged();
		}
	}

	
	/**
	 * Internal class used as model for the sleep time table.
	 */
	private static class SleepExerciseTableModel
	extends AbstractTableModel {

		private static final String SLEEP_TIME = "Sleep";
		private static final String EXERCISE_TIME = "Exercise";
		
		private static final String MISSION_SOL = "Sol";

		private Map<Integer, Double> sleepTime;
		private Map<Integer, Double> exerciseTime;
		private int solOffset = 1;
		private int rowCount = 0;

		private SleepExerciseTableModel(CircadianClock circadian) {
			update(circadian);
		}

		public int getRowCount() {
			return rowCount;
		}

		public int getColumnCount() {
			return 3;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) {
			    dataType = Integer.class;
			}
			else {
			    dataType = String.class;
			}
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
			    return MISSION_SOL; 
			}
			else if (columnIndex == 1) {
			    return SLEEP_TIME;
			}
			else if (columnIndex == 2) {
			    return EXERCISE_TIME;
			}
			return null;
		}

		public Object getValueAt(int row, int column) {
			Object result = null;
			if (row < getRowCount()) {
				int rowSol = row + solOffset;
				if (column == 0) {
				    result = rowSol;
				}
				else if (column == 1) {
					if (sleepTime.containsKey(rowSol))
						result = StyleManager.DECIMAL_PLACES1.format(sleepTime.get(rowSol));
					else
						result = StyleManager.DECIMAL_PLACES1.format(0);
				}
				else if (column == 2) {
					if (exerciseTime.containsKey(rowSol))
						result = StyleManager.DECIMAL_PLACES1.format(exerciseTime.get(rowSol));
					else
						result = StyleManager.DECIMAL_PLACES1.format(0);
				}
			}
			return result;
		}

		public void update(CircadianClock circadian) {
			sleepTime = circadian.getSleepHistory();
			exerciseTime = circadian.getExerciseHistory();
			
			Set<Integer> largestSet;
			if (sleepTime.size() > exerciseTime.size()) {
				largestSet = sleepTime.keySet();
			}
			else {
				largestSet = exerciseTime.keySet();
			}

			// Find the lowest sol day in the data
			solOffset = largestSet.stream()
					.mapToInt(v -> v)               
	                .min()                          
	                .orElse(Integer.MAX_VALUE);
			rowCount = largestSet.size();

			fireTableDataChanged();
		}
	}
	
	/**
	 * Internal class used as model for the food time table.
	 */
	private static class FoodTableModel
	extends AbstractTableModel {

		private static final String FOOD_AMOUNT = "Food";
		private static final String MEAL_AMOUNT = "Meal";
		private static final String DESSERT_AMOUNT = "Dessert";
		private static final String WATER_AMOUNT = "Water";
		
		private static final String MISSION_SOL = "Sol"; 

		private Map<Integer, Map<Integer, Double>> map;

		private int solOffset = 1;

		private FoodTableModel(PhysicalCondition pc) {
			map = pc.getConsumptionHistory();
		}

		public int getRowCount() {
			return map.size();
		}

		public int getColumnCount() {
			return 5;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) {
			    dataType = Integer.class;
			}
			else {
			    dataType = String.class;
			}
			
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
			    return MISSION_SOL; 
			}
			else if (columnIndex == 1) {
			    return FOOD_AMOUNT;
			}
			else if (columnIndex == 2) {
			    return MEAL_AMOUNT;
			}
			else if (columnIndex == 3) {
			    return DESSERT_AMOUNT;
			}
			else if (columnIndex == 4) {
			    return WATER_AMOUNT;
			}
			return null;
		}

		public Object getValueAt(int row, int column) {
			Object result = null;
			if (row < getRowCount()) {
				int rowSol = row + solOffset;
				if (column == 0) {
					return rowSol;
				}
				else if (column == 1) {
					if (map.containsKey(rowSol))
						result = StyleManager.DECIMAL_PLACES3.format(returnAmount(rowSol, 0));
					else
						result = StyleManager.DECIMAL_PLACES3.format(0);
				}
				else if (column == 2) {
					if (map.containsKey(rowSol))
						result = StyleManager.DECIMAL_PLACES3.format(returnAmount(rowSol, 1));
					else
						result = StyleManager.DECIMAL_PLACES3.format(0);
				}
				else if (column == 3) {
					if (map.containsKey(rowSol))
						result = StyleManager.DECIMAL_PLACES3.format(returnAmount(rowSol, 2));
					else
						result = StyleManager.DECIMAL_PLACES3.format(0);
				}
				else if (column == 4) {
					if (map.containsKey(rowSol))
						result = StyleManager.DECIMAL_PLACES3.format(returnAmount(rowSol, 3));
					else
						result = StyleManager.DECIMAL_PLACES3.format(0);
				}
			}
			return result;
		}

		private double returnAmount(int rowSol, int type) {
			if (map.containsKey(rowSol)) {
				Map<Integer, Double> map1 = map.get(rowSol);
				Double amount = map1.get(type);
				if (amount != null)
					return amount.doubleValue();
			}
			return 0;
		}
		
		
		public void update(PhysicalCondition pc) {
			// The size of map needs to be updated
			map = pc.getConsumptionHistory();
			
			// Find the lowest sol day in the data
			solOffset = map.keySet().stream()
					.mapToInt(v -> v)               
	                .min()                          
	                .orElse(Integer.MAX_VALUE);
			
			fireTableDataChanged();
		}
	}
	
	/**
	 * Internal class used as model for the medication table.
	 */
	private static class MedicationTableModel
	extends AbstractTableModel {

		private List<Medication> medicationCache;

		private MedicationTableModel(PhysicalCondition condition) {
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

		public void update(PhysicalCondition condition) {
			// Make sure medication cache is current.
			if (!medicationCache.equals(condition.getMedicationList())) {
				medicationCache = condition.getMedicationList();
			}

			fireTableDataChanged();
		}
	}
}
