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

import javax.swing.BoxLayout;
import javax.swing.JPanel;
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
import org.mars_sim.msp.ui.swing.ImageLoader;
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

	private static final String HEALTH_ICON = Msg.getString("icon.health"); //$NON-NLS-1$
	
	private static final String THIRTY_DAY = "30-Day";
	private static final String ANNUAL = "Annual";
	private static final String CAREER = "Career";
	private static final String S4 = "%4d";
	private static final String S6 = "%6d";

	private static int theme;
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
	
	private WebLabel thirstLabel;
	private WebLabel fatigueLabel;
	private WebLabel hungerLabel;
	private WebLabel energyLabel;
	private WebLabel stressLabel;
	private WebLabel performanceLabel;
	
	private WebLabel leptinLabel;
	private WebLabel ghrelinLabel;
	private WebLabel leptinTLabel;
	private WebLabel ghrelinTLabel;
	
	/** The sleep hour text field. */	
	private WebTextField sleepTF;
	
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
	
	protected String[] radiationToolTips = {
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
	public TabPanelHealth(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getNewIcon(HEALTH_ICON),
			Msg.getString("TabPanelHealth.title"), //$NON-NLS-1$
			unit, desktop
		);

		person = (Person) unit;
	}

	@Override
	protected void buildUI(JPanel content) {
		
		condition = person.getPhysicalCondition();
		
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

		// Prepare condition panel
		WebPanel conditionPanel = new WebPanel(new SpringLayout());
		northPanel.add(conditionPanel);

		// Prepare fatigue name label
		WebLabel fatigueNameLabel = new WebLabel(Msg.getString("TabPanelHealth.fatigue"), SwingConstants.RIGHT); //$NON-NLS-1$
		conditionPanel.add(fatigueNameLabel);
		
		// Prepare fatigue label
		fatigueCache = (int)condition.getFatigue();
		fatigueLabel = new WebLabel(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
				String.format(S4, fatigueCache)), SwingConstants.RIGHT);
		conditionPanel.add(fatigueLabel);

		// Prepare hunger name label
		WebLabel thirstNameLabel = new WebLabel(Msg.getString("TabPanelHealth.thirst"), SwingConstants.RIGHT); //$NON-NLS-1$
		conditionPanel.add(thirstNameLabel);

		// Prepare hunger label
		thirstCache = (int)condition.getThirst();
		thirstLabel = new WebLabel(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
				String.format(S4, thirstCache)), SwingConstants.RIGHT);
		conditionPanel.add(thirstLabel);
		
		// Prepare hunger name label
		WebLabel hungerNameLabel = new WebLabel(Msg.getString("TabPanelHealth.hunger"), SwingConstants.RIGHT); //$NON-NLS-1$
		conditionPanel.add(hungerNameLabel);

		// Prepare hunger label
		hungerCache = (int)condition.getHunger();
		hungerLabel = new WebLabel(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
				String.format(S4, hungerCache)), SwingConstants.RIGHT);
		conditionPanel.add(hungerLabel);

		//
		// Prepare energy name label
		WebLabel energyNameLabel = new WebLabel(Msg.getString("TabPanelHealth.energy"), SwingConstants.RIGHT); //$NON-NLS-1$
		conditionPanel.add(energyNameLabel);

		// Prepare energy label
		energyCache = (int)condition.getEnergy();
		energyLabel = new WebLabel(Msg.getString("TabPanelHealth.kJ", //$NON-NLS-1$
				String.format(S6, energyCache)), SwingConstants.RIGHT);
		conditionPanel.add(energyLabel);


		// Prepare stress name label
		WebLabel stressNameLabel = new WebLabel(Msg.getString("TabPanelHealth.stress"), SwingConstants.RIGHT); //$NON-NLS-1$
		conditionPanel.add(stressNameLabel);

		// Prepare stress label
		stressCache = (int)condition.getStress();
		stressLabel = new WebLabel(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
				String.format(S4, stressCache)), SwingConstants.RIGHT);
		conditionPanel.add(stressLabel);

		// Prepare performance rating label
		WebLabel performanceNameLabel = new WebLabel(Msg.getString("TabPanelHealth.performance"), SwingConstants.RIGHT); //$NON-NLS-1$
		conditionPanel.add(performanceNameLabel);

		// Performance rating label
		performanceCache = (int)(person.getPerformanceRating() * 100);
		performanceLabel = new WebLabel(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
				String.format(S4, performanceCache)), SwingConstants.RIGHT);
		conditionPanel.add(performanceLabel);

		// Prepare leptin label
		WebLabel leptinNameLabel = new WebLabel(Msg.getString("TabPanelHealth.leptin"), SwingConstants.RIGHT); //$NON-NLS-1$
		conditionPanel.add(leptinNameLabel);

		leptinCache = (int)(person.getCircadianClock().getLeptin());
		leptinLabel = new WebLabel(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
				String.format(S4, leptinCache)), SwingConstants.RIGHT);
		conditionPanel.add(leptinLabel);
		
		// Prepare ghrelin label
		WebLabel ghrelinNameLabel = new WebLabel(Msg.getString("TabPanelHealth.ghrelin"), SwingConstants.RIGHT); //$NON-NLS-1$
		conditionPanel.add(ghrelinNameLabel);

		ghrelinCache = (int)(person.getCircadianClock().getGhrelin());
		ghrelinLabel = new WebLabel(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
				String.format(S4, ghrelinCache)), SwingConstants.RIGHT);
		conditionPanel.add(ghrelinLabel);

		// Prepare leptin threshold label
		WebLabel leptinTNameLabel = new WebLabel(Msg.getString("TabPanelHealth.leptin.threshold"), SwingConstants.RIGHT); //$NON-NLS-1$
		conditionPanel.add(leptinTNameLabel);

		leptinTCache = (int)(person.getCircadianClock().getLeptinT());
		leptinTLabel = new WebLabel(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
				String.format(S4, leptinTCache)), SwingConstants.RIGHT);
		conditionPanel.add(leptinTLabel);
		
		// Prepare ghrelin threshold label
		WebLabel ghrelinTNameLabel = new WebLabel(Msg.getString("TabPanelHealth.ghrelin.threshold"), SwingConstants.RIGHT); //$NON-NLS-1$
		conditionPanel.add(ghrelinTNameLabel);

		ghrelinTCache = (int)(person.getCircadianClock().getGhrelinT());
		ghrelinTLabel = new WebLabel(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
				String.format(S4, ghrelinTCache)), SwingConstants.RIGHT);
		conditionPanel.add(ghrelinTLabel);

		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(conditionPanel,
		                                5, 4, //rows, cols
		                                5, 4,        //initX, initY
		                                15, 3);       //xPad, yPad
		

		// Prepare SpringLayout for info panel.
		WebPanel springPanel = new WebPanel(new SpringLayout());
		northPanel.add(springPanel);
		
		// Prepare sleep hour name label
		WebLabel sleepHrLabel = new WebLabel(Msg.getString("TabPanelFavorite.sleepHour"), SwingConstants.RIGHT); //$NON-NLS-1$
		springPanel.add(sleepHrLabel);

		// Checks the 3 best sleep time
    	int bestSleepTime[] = person.getPreferredSleepHours();		
		WebPanel wrapper5 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
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
		sleepTF = new WebTextField(text.toString());
		sleepTF.setEditable(false);
		sleepTF.setColumns(20);
		sleepTF.setCaretPosition(0);
		wrapper5.add(sleepTF);
		springPanel.add(wrapper5);

		TooltipManager.setTooltip (sleepTF, "3 best times to go to bed [msol (weight)]", TooltipWay.down); //$NON-NLS-1$
				
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
		WebPanel radiationPanel = new WebPanel(new BorderLayout(0, 0));
		tablesPanel.add(radiationPanel, BorderLayout.NORTH);

		// Prepare radiation label
		WebLabel radiationLabel = new WebLabel(Msg.getString("TabPanelHealth.rad"), SwingConstants.CENTER); //$NON-NLS-1$
		radiationLabel.setFont(SUBTITLE_FONT);
		radiationPanel.add(radiationLabel, BorderLayout.NORTH);
		TooltipManager.setTooltip (radiationLabel, Msg.getString("TabPanelHealth.radiation.tooltip"), TooltipWay.down); //$NON-NLS-1$
			 
		// Prepare radiation scroll panel
		WebScrollPane radiationScrollPanel = new WebScrollPane();
		radiationPanel.add(radiationScrollPanel, BorderLayout.CENTER);

		// Prepare radiation table model
		radiationTableModel = new RadiationTableModel(person);

		// Create radiation table
		radiationTable = new ZebraJTable(radiationTableModel) {

            //Implement table cell tool tips.           
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
//              int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                try {
                	if (colIndex == 0) {
                    	tip = radiationToolTips[0];
                    }
                	else if (colIndex == 1) {
                    	tip = radiationToolTips[1];
                    }
                    else if (colIndex == 2) {
                    	tip = radiationToolTips[2];
                    }
                    else if (colIndex == 3) {
                    	tip = radiationToolTips[3];
                    }
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }

                return tip;
            }
        };
	
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		radiationTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		radiationTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		radiationTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		radiationTable.getColumnModel().getColumn(3).setCellRenderer(renderer);

		radiationTable.setPreferredScrollableViewportSize(new Dimension(225, 75));
		radiationTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		radiationTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		radiationTable.getColumnModel().getColumn(2).setPreferredWidth(65);
		radiationTable.getColumnModel().getColumn(3).setPreferredWidth(35);
		radiationTable.setRowSelectionAllowed(true);
		radiationScrollPanel.setViewportView(radiationTable);
		
		// Added sorting
		radiationTable.setAutoCreateRowSorter(true);
		TableStyle.setTableStyle(radiationTable);

		// Prepare sleep time panel
		WebPanel sleepPanel = new WebPanel(new BorderLayout(0, 0));
		tablesPanel.add(sleepPanel, BorderLayout.CENTER);

		// Prepare sleep time label
		WebLabel sleepLabel = new WebLabel(Msg.getString("TabPanelHealth.sleepExercise"), SwingConstants.CENTER); //$NON-NLS-1$
		sleepLabel.setFont(SUBTITLE_FONT);
		sleepPanel.add(sleepLabel, BorderLayout.NORTH);

		// Prepare sleep time scroll panel
		WebScrollPane sleepScrollPanel = new WebScrollPane();
		sleepPanel.add(sleepScrollPanel, BorderLayout.CENTER);

		// Prepare sleep time table model
		sleepExerciseTableModel = new SleepExerciseTableModel(person);
		
		// Create sleep time table
		sleepExerciseTable = new ZebraJTable(sleepExerciseTableModel);
		sleepExerciseTable.setPreferredScrollableViewportSize(new Dimension(225, 90));
		sleepExerciseTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		sleepExerciseTable.getColumnModel().getColumn(1).setPreferredWidth(70);
		sleepExerciseTable.getColumnModel().getColumn(2).setPreferredWidth(70);
		
		sleepExerciseTable.setRowSelectionAllowed(true);
		sleepScrollPanel.setViewportView(sleepExerciseTable);

		DefaultTableCellRenderer sleepRenderer = new DefaultTableCellRenderer();
		sleepRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		sleepExerciseTable.getColumnModel().getColumn(0).setCellRenderer(sleepRenderer);
		sleepExerciseTable.getColumnModel().getColumn(1).setCellRenderer(sleepRenderer);
		sleepExerciseTable.getColumnModel().getColumn(2).setCellRenderer(sleepRenderer);
		
		// Add sorting
		sleepExerciseTable.setAutoCreateRowSorter(true);
		TableStyle.setTableStyle(sleepExerciseTable);
	
		/////////////////////////////////////////////////////////
		
		// Prepare exercise time panel
		WebPanel foodPanel = new WebPanel(new BorderLayout(0, 0));
		tablesPanel.add(foodPanel, BorderLayout.SOUTH);

		// Prepare exercise time label
		WebLabel foodLabel = new WebLabel(Msg.getString("TabPanelHealth.food"), SwingConstants.CENTER); //$NON-NLS-1$
		foodLabel.setFont(SUBTITLE_FONT);
		foodPanel.add(foodLabel, BorderLayout.NORTH);

		// Prepare exercise time scroll panel
		WebScrollPane foodScrollPanel = new WebScrollPane();
		foodPanel.add(foodScrollPanel, BorderLayout.CENTER);

		// Prepare exercise time table model
		foodTableModel = new FoodTableModel(person);
		
		// Create exercise time table
		foodTable = new ZebraJTable(foodTableModel);
		foodTable.setPreferredScrollableViewportSize(new Dimension(225, 90));
		foodTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		foodTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		foodTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		foodTable.getColumnModel().getColumn(3).setPreferredWidth(50);
		foodTable.getColumnModel().getColumn(4).setPreferredWidth(50);

		foodTable.setRowSelectionAllowed(true);
		foodScrollPanel.setViewportView(foodTable);

		DefaultTableCellRenderer foodRenderer = new DefaultTableCellRenderer();
		foodRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		foodTable.getColumnModel().getColumn(0).setCellRenderer(foodRenderer);
		foodTable.getColumnModel().getColumn(1).setCellRenderer(foodRenderer);
		foodTable.getColumnModel().getColumn(2).setCellRenderer(foodRenderer);
		foodTable.getColumnModel().getColumn(3).setCellRenderer(foodRenderer);	
		foodTable.getColumnModel().getColumn(4).setCellRenderer(foodRenderer);	
		
		// Add sorting
		foodTable.setAutoCreateRowSorter(true);
		TableStyle.setTableStyle(foodTable);
		
		/////////////////////////////////////////////////////////		
		
		
		// Prepare health problem panel
		WebPanel healthProblemPanel = new WebPanel(new BorderLayout(0, 0));
		tablesPanel.add(healthProblemPanel);

		// Prepare health problem label
		WebLabel healthProblemLabel = new WebLabel(Msg.getString("TabPanelHealth.healthProblems"), SwingConstants.CENTER); //$NON-NLS-1$
		healthProblemLabel.setPadding(7, 0, 0, 0);
		healthProblemLabel.setFont(SUBTITLE_FONT);
		healthProblemPanel.add(healthProblemLabel, BorderLayout.NORTH);

		// Prepare health problem scroll panel
		WebScrollPane healthProblemScrollPanel = new WebScrollPane();
		healthProblemPanel.add(healthProblemScrollPanel, BorderLayout.CENTER);

		// Prepare health problem table model
		healthProblemTableModel = new HealthProblemTableModel(person);

		// Create health problem table
		healthProblemTable = new ZebraJTable(healthProblemTableModel);
		healthProblemTable.setPreferredScrollableViewportSize(new Dimension(225, 90));
		healthProblemTable.setRowSelectionAllowed(true);
		healthProblemScrollPanel.setViewportView(healthProblemTable);

		// Add sorting
		healthProblemTable.setAutoCreateRowSorter(true);
		TableStyle.setTableStyle(healthProblemTable);
		
		
		// Prepare medication panel.
		WebPanel medicationPanel = new WebPanel(new BorderLayout());
		tablesPanel.add(medicationPanel);

		// Prepare medication label.
		WebLabel medicationLabel = new WebLabel(Msg.getString("TabPanelHealth.medication"), SwingConstants.CENTER); //$NON-NLS-1$
		medicationLabel.setPadding(7, 0, 0, 0);
		medicationLabel.setFont(SUBTITLE_FONT);
		medicationPanel.add(medicationLabel, BorderLayout.NORTH);

		// Prepare medication scroll panel
		WebScrollPane medicationScrollPanel = new WebScrollPane();
		medicationPanel.add(medicationScrollPanel, BorderLayout.CENTER);

		// Prepare medication table model.
		medicationTableModel = new MedicationTableModel(person);

		// Prepare medication table.
		medicationTable = new ZebraJTable(medicationTableModel);
		medicationTable.setPreferredScrollableViewportSize(new Dimension(225, 90));
		medicationTable.setRowSelectionAllowed(true);
		medicationScrollPanel.setViewportView(medicationTable);

		// Add sorting
		medicationTable.setAutoCreateRowSorter(true);

		TableStyle.setTableStyle(medicationTable);
		
		// Update at least one before displaying it
		update();
	}


	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		int t = 0;//MainScene.getTheme();		
		if (theme != t) {
			theme = t;
			TableStyle.setTableStyle(radiationTable);
			TableStyle.setTableStyle(medicationTable);
			TableStyle.setTableStyle(healthProblemTable);
			TableStyle.setTableStyle(sleepExerciseTable);
			TableStyle.setTableStyle(foodTable);
		}
		
		// Update fatigue if necessary.
		int newF = (int)condition.getFatigue();
		if (fatigueCache != newF) {
			fatigueCache = newF;
			fatigueLabel.setText(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
					String.format(S4, newF)));
		}

		// Update thirst if necessary.
		int newT = (int)condition.getThirst();
		if (thirstCache != newT) {
			thirstCache = newT;
			thirstLabel.setText(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
					String.format(S4, newT)));
		}
		
		// Update hunger if necessary.
		int newH = (int)condition.getHunger();
		if (hungerCache != newH) {
			hungerCache = newH;
			hungerLabel.setText(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
					String.format(S4, newH)));
		}

		// Update energy if necessary.
		int newEnergy = (int)condition.getEnergy();
		if (energyCache != newEnergy) {
			energyCache = newEnergy;
			energyLabel.setText(Msg.getString("TabPanelHealth.kJ", //$NON-NLS-1$
					String.format(S6, newEnergy)));
		}

		// Update stress if necessary.
		int newS = (int)condition.getStress();
		if (stressCache != newS) {
			stressCache = newS;
			stressLabel.setText(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
					String.format(S4, newS)));
		}

		// Update performance cache if necessary.
		int newP = (int)(condition.getPerformanceFactor() * 100);
		if (performanceCache != newP) {
			performanceCache = newP;
			performanceLabel.setText(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
					String.format(S4, newP)));
		}
		
		// Update leptin if necessary.
		int newL = (int)(person.getCircadianClock().getLeptin());
		if (leptinCache != newL) {
			leptinCache = newL;
			leptinLabel.setText(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
					String.format(S4, newL)));
		}		
		
		// Update ghrelin if necessary.
		int newG = (int)(person.getCircadianClock().getGhrelin());
		if (ghrelinCache != newG) {
			ghrelinCache = newG;
			ghrelinLabel.setText(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
					String.format(S4, newG)));
		}		
		
		// Update leptin threshold if necessary.
		int newLT = (int)(person.getCircadianClock().getLeptinT());
		if (leptinTCache != newLT) {
			leptinTCache = newLT;
			leptinTLabel.setText(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
					String.format(S4, newLT)));
		}		
		
		// Update ghrelin threshold if necessary.
		int newGT = (int)(person.getCircadianClock().getGhrelinT());
		if (ghrelinTCache != newGT) {
			ghrelinTCache = newGT;
			ghrelinTLabel.setText(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
					String.format(S4, newGT)));
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
		medicationTableModel.update();

		// Update health problem table model.
		healthProblemTableModel.update();

		// Update radiation dose table model
		radiationTableModel.update();
		
		// Update sleep time table model
		sleepExerciseTableModel.update();
		
		// Update food table model
		foodTableModel.update();
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
	private static class SleepExerciseTableModel
	extends AbstractTableModel {

		private static final String SLEEP_TIME = "Sleep";
		private static final String EXERCISE_TIME = "Exercise";
		
		private static final String MISSION_SOL = "Sol"; 
		
		private DecimalFormat fmt = new DecimalFormat("0.0");

		private CircadianClock circadian;
		private Map<Integer, Double> sleepTime;
		private Map<Integer, Double> exerciseTime;
		private int solOffset = 1;

		private SleepExerciseTableModel(Person person) {
			circadian = person.getCircadianClock();
			sleepTime = circadian.getSleepHistory();
			exerciseTime = circadian.getExerciseHistory();
		}

		public int getRowCount() {
			return sleepTime.size();
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
						result = fmt.format(sleepTime.get(rowSol));
					else
						result = fmt.format(0);
				}
				else if (column == 2) {
					if (exerciseTime.containsKey(rowSol))
						result = fmt.format(exerciseTime.get(rowSol));
					else
						result = fmt.format(0);
				}
			}
			return result;
		}

		public void update() {
			sleepTime = circadian.getSleepHistory();
			exerciseTime = circadian.getExerciseHistory();
			
			// Find the lowest sol day in the data
			solOffset = sleepTime.keySet().stream()
					.mapToInt(v -> v)               
	                .min()                          
	                .orElse(Integer.MAX_VALUE);
			
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
		
		private DecimalFormat fmt = new DecimalFormat("0.000");

		private PhysicalCondition pc;
		private Map<Integer, Map<Integer, Double>> map;

		private int solOffset = 1;

		private FoodTableModel(Person person) {
			pc = person.getPhysicalCondition();
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
						result = fmt.format(returnAmount(rowSol, 0));
					else
						result = fmt.format(0);
				}
				else if (column == 2) {
					if (map.containsKey(rowSol))
						result = fmt.format(returnAmount(rowSol, 1));
					else
						result = fmt.format(0);
				}
				else if (column == 3) {
					if (map.containsKey(rowSol))
						result = fmt.format(returnAmount(rowSol, 2));
					else
						result = fmt.format(0);
				}
				else if (column == 4) {
					if (map.containsKey(rowSol))
						result = fmt.format(returnAmount(rowSol, 3));
					else
						result = fmt.format(0);
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
		
		
		public void update() {
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
