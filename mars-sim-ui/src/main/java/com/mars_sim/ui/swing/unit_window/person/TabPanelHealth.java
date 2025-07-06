/*
 * Mars Simulation Project
 * TabPanelHealth.java
 * @date 2025-07-04
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.person.CircadianClock;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.health.CuredProblem;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.HealthRiskType;
import com.mars_sim.core.person.health.Medication;
import com.mars_sim.core.person.health.RadiationExposure;
import com.mars_sim.core.person.health.RadiationExposure.DoseHistory;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.MarsTimeTableCellRenderer;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

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

	private static final String WIKI_URL = Msg.getString("TabPanelHealth.radiation.url"); //$NON-NLS-1$
	private static final String[] RADIATION_TOOL_TIPS = {
		    " Exposure Interval - 30-Day, Annual, or Career", 
		    " Standard Dose Limit [mSv] on BFO - 30-Day:  250;  Annual: 1000;  Career: 1500",
		    " Standard Dose Limit [mSv] on Eye - 30-Day:  500;  Annual: 2000;  Career: 3000",
		    " Standard Dose Limit [mSv] on Skin - 30-Day: 1000;  Annual: 4000;  Career: 6000"};

	private static final String KJ = " kJ";

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
	private int maxDailyEnergy;
	
	private double muscleTor;
	private double muscleHealth;
	private double muscleSoreness;
	private double appetite;
	private double bodyMassDev;
	
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

	private JLabel muscleTorLabel;
	private JLabel muscleHealthLabel;
	private JLabel muscleSorenessLabel;
	
	private JLabel appetiteLabel;
	private JLabel maxDailyEnergyLabel;
	private JLabel bodyMassDevLabel;
	
	private JLabel bedLocationLabel;
	private JTextArea sleepTimeTA;

	private RadiationTableModel radiationTableModel;
	private SleepExerciseTableModel sleepExerciseTableModel;
	private FoodTableModel foodTableModel;
	private HealthProblemTableModel healthProblemTableModel;
	private MedicationTableModel medicationTableModel;
	private HealthLogTableModel healthLogTableModel;
	
	/** The Person instance. */
	private Person person = null;
	
	/** The PhysicalCondition instance. */
	private PhysicalCondition condition;
	private CircadianClock circadianClock;


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

	/*
	 * Creates a text area.
	 * 
	 * @param panel
	 * @return
	 */
	private JTextArea createTA(JPanel panel) {
		JTextArea ta = new JTextArea();
		ta.setEditable(false);
		ta.setColumns(35);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		panel.add(ta);
		return ta;
	}
	
	@Override
	protected void buildUI(JPanel content) {
        DoseHistory[] doseLimits;
        JTable radiationTable;
        JTable medicationTable;
        JTable healthProblemTable;
        JTable healthLogTable;
        JTable sleepExerciseTable;
        JTable foodTable;
				
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

		// Prepare condition panel
		AttributePanel conditionPanel = new AttributePanel(8, 2);
		northPanel.add(conditionPanel);
		
		fatigueCache = (int)condition.getFatigue();
		fatigueLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.fatigue"),
										DECIMAL_MSOLS.format(fatigueCache), null);
		
		maxDailyEnergy = (int)(condition.getPersonalMaxEnergy());
		maxDailyEnergyLabel = conditionPanel.addRow(Msg.getString("TabPanelHealth.maxDailyEnergy"),
										maxDailyEnergy + KJ);
						
		hungerCache = (int)condition.getHunger();
		hungerLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.hunger"),
										DECIMAL_MSOLS.format(thirstCache), null);
		
		energyCache = (int)condition.getEnergy();
		energyLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.energy"),
										StyleManager.DECIMAL_PLACES0.format(energyCache) + " kJ", null);
				
		thirstCache = (int)condition.getThirst();
		thirstLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.thirst"),
										DECIMAL_MSOLS.format(thirstCache), null);
		
		appetite = Math.round(condition.getAppetite()*10.0)/10.0;
		appetiteLabel = conditionPanel.addRow(Msg.getString("TabPanelHealth.appetite"),
				StyleManager.DECIMAL_PLACES1.format(appetite));
		
		performanceCache = (int)(person.getPerformanceRating() * 100);
		performanceLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.performance"),
					StyleManager.DECIMAL_PERC.format(performanceCache), null);
		
		muscleHealth = Math.round(condition.getMuscleHealth()*10.0)/10.0;
		muscleHealthLabel = conditionPanel.addRow(Msg.getString("TabPanelHealth.muscle.health"),
				StyleManager.DECIMAL_PLACES1.format(muscleHealth));	

		stressCache = (int)condition.getStress();	
		stressLabel = conditionPanel.addTextField(Msg.getString("TabPanelHealth.stress"),
				StyleManager.DECIMAL_PERC.format(stressCache), null);
		
		muscleTor = Math.round(condition.getMusclePainTolerance()*10.0)/10.0;
		muscleTorLabel = conditionPanel.addRow(Msg.getString("TabPanelHealth.muscle.tolerance"),
				StyleManager.DECIMAL_PLACES1.format(muscleTor));	
		
		bodyMassDev = Math.round(condition.getPersonalMaxEnergy()*10.0)/10.0;
		bodyMassDevLabel = conditionPanel.addRow(Msg.getString("TabPanelHealth.bodyMassDev"),
				StyleManager.DECIMAL_PLACES1.format(bodyMassDev));
		
		muscleSoreness = Math.round(condition.getMuscleSoreness()*10.0)/10.0;
		muscleSorenessLabel = conditionPanel.addRow(Msg.getString("TabPanelHealth.muscle.soreness"),
				StyleManager.DECIMAL_PLACES1.format(muscleSoreness));
	
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

		
		// Prepare panel for bed .
		JPanel bedPanel = new JPanel();
		bedPanel.setLayout(new BoxLayout(bedPanel, BoxLayout.X_AXIS));
        
		northPanel.add(bedPanel);
		
		JLabel bedLabel = new JLabel("Bed : ", SwingConstants.RIGHT); //$NON-NLS-1$
		bedLabel.setFont(StyleManager.getLabelFont());
		bedPanel.add(bedLabel);
		bedLocationLabel = new JLabel("");
		bedLocationLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		bedPanel.add(bedLocationLabel);
		
		// Prepare sleep time text area
		JPanel titledPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		northPanel.add(titledPanel);
		addBorder(titledPanel, Msg.getString("TabPanelHealth.sleepTime"));
		
		String sleepTime = updateSleepTime().toString();
	
		sleepTimeTA = createTA(titledPanel);
		sleepTimeTA.setToolTipText("The 3 best times to go to bed [msol (weight)]"); //$NON-NLS-1$
		sleepTimeTA.append(" " + sleepTime);
	
		content.add(northPanel, BorderLayout.NORTH);	
		
		//////////////////////////////
	
		// Prepare middle panel
        JPanel midPanel = new JPanel(new BorderLayout());
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
		content.add(midPanel, BorderLayout.CENTER);

		// Prepare bottom panel
		JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
		content.add(bottomPanel, BorderLayout.SOUTH);
			
		//////////////////////////////
	
		// Prepare sleep time panel
		JPanel sleepPanel = new JPanel(new BorderLayout(0, 0));
		midPanel.add(sleepPanel, BorderLayout.NORTH);

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
		
		// Prepare food panel
		JPanel foodPanel = new JPanel(new BorderLayout(0, 0));
		midPanel.add(foodPanel, BorderLayout.CENTER);

		// Prepare food label
		JLabel foodLabel = new JLabel(Msg.getString("TabPanelHealth.food"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(foodLabel);
		foodPanel.add(foodLabel, BorderLayout.NORTH);

		// Prepare food scroll panel
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

		// Add radiation dose info
		// Prepare radiation panel
		JPanel radiationPanel = new JPanel(new BorderLayout(0, 0));
		midPanel.add(radiationPanel, BorderLayout.SOUTH);

		JPanel radLabelPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		radiationPanel.add(radLabelPane, BorderLayout.NORTH);
		
		// Prepare radiation label
		JLabel radiationLabel = new JLabel(Msg.getString("TabPanelHealth.rad"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(radiationLabel);
		radLabelPane.add(radiationLabel);
		radiationLabel.setToolTipText(Msg.getString("TabPanelHealth.radiation.tooltip")); //$NON-NLS-1$
			 
		// Prepare radiation scroll panel
		JScrollPane radiationScrollPanel = new JScrollPane();
		radiationPanel.add(radiationScrollPanel, BorderLayout.CENTER);

		JPanel linkPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		radiationPanel.add(linkPane, BorderLayout.SOUTH);
		
		JButton wikiButton = new JButton(GuideWindow.wikiIcon);
		linkPane.add(wikiButton, SwingConstants.CENTER);
		wikiButton.setAlignmentX(.5f);
		wikiButton.setAlignmentY(.5f);
		wikiButton.setToolTipText("Open Radiation Wiki in GitHub");
		wikiButton.addActionListener(e -> SwingHelper.openBrowser(WIKI_URL));

		// Prepare radiation table model
		radiationTableModel = new RadiationTableModel(condition);

		// Gets the person's radiation dose limits
        doseLimits = condition.getRadiationExposure().getDoseLimits();
        
        String[] regions = {"BFO", "Ocular", "Skin"};
        String[] limits = new String[3];
        
        for (int i=0; i<3; i++) {
            limits[i] = "Dose Limit [mSv] on " + regions[i]
            	+ " - 30-Day: " + Math.round(doseLimits[i].getThirtyDay()*10.0)/10.0
            	+ ";  Annual: " + Math.round(doseLimits[i].getAnnual()*10.0)/10.0
            	+ ";  Career: " + Math.round(doseLimits[i].getCareer()*10.0)/10.0;
        }
 
		// Create radiation table
		radiationTable = new JTable(radiationTableModel) {
            // Implement table cell tool tips. 
			@Override          
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int colIndex = columnAtPoint(p);
				if (colIndex == 0) {
                    tip = RADIATION_TOOL_TIPS[colIndex];
				}
				else if (colIndex < 4) {			
                    tip = limits[colIndex-1];
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

		radiationTable.setPreferredScrollableViewportSize(new Dimension(225, 65));
		rModel.getColumn(0).setPreferredWidth(35);
		rModel.getColumn(1).setPreferredWidth(75);
		rModel.getColumn(2).setPreferredWidth(45);
		rModel.getColumn(3).setPreferredWidth(35);
		radiationTable.setRowSelectionAllowed(true);
		radiationScrollPanel.setViewportView(radiationTable);
		
		// Added sorting
		radiationTable.setAutoCreateRowSorter(true);
	
		/////////////////////////////////////////////////////////
		
		// Prepare attribute panel.
		AttributePanel attributePanel = new AttributePanel(10);
		
		JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		listPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		listPanel.add(attributePanel, BorderLayout.CENTER);
		bottomPanel.add(listPanel, BorderLayout.NORTH);
		
		addBorder(listPanel, Msg.getString("TabPanelHealth.healthRisks.title"));

		Map<HealthRiskType, Double> riskMap = condition.getHealthRisks();
		
		List<HealthRiskType> riskList = new ArrayList<>(riskMap.keySet());
		
		Collections.sort(riskList);
		
		for (int i = 0; i < 10; i++) {
			HealthRiskType type = riskList.get(i);
			attributePanel.addTextField(type.getName(), Math.round(riskMap.get(type) * 100.0)/100.0 + " %", null);
		}

		
		//////////////////////////////
		
		JPanel southPanel = new JPanel(new BorderLayout());
		bottomPanel.add(southPanel, BorderLayout.CENTER);
			
		//////////////////////////////
	
		// Prepare health problem panel
		JPanel healthProblemPanel = new JPanel(new BorderLayout(0, 0));
		southPanel.add(healthProblemPanel, BorderLayout.NORTH);
		
		// Prepare health problem label
		JLabel healthProblemLabel = new JLabel(Msg.getString("TabPanelHealth.healthProblems"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(healthProblemLabel);
		healthProblemPanel.add(healthProblemLabel, BorderLayout.NORTH);

		// Prepare health problem scroll panel
		JScrollPane healthProblemScrollPanel = new JScrollPane();
		healthProblemPanel.add(healthProblemScrollPanel, BorderLayout.CENTER);

		// Prepare health problem table model
		healthProblemTableModel = new HealthProblemTableModel(condition);

		// Create health problem table
		healthProblemTable = new JTable(healthProblemTableModel);
		healthProblemTable.setPreferredScrollableViewportSize(new Dimension(225, 50));
		healthProblemTable.setRowSelectionAllowed(true);
		healthProblemScrollPanel.setViewportView(healthProblemTable);

		// Add sorting
		healthProblemTable.setAutoCreateRowSorter(true);
	
		
		/////////////////////////////////////////////////////////	

		
		// Prepare medication panel.
		JPanel medicationPanel = new JPanel(new BorderLayout());
		southPanel.add(medicationPanel, BorderLayout.CENTER);
		
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
		medicationTable.setPreferredScrollableViewportSize(new Dimension(225, 50));
		medicationTable.setRowSelectionAllowed(true);
		medicationScrollPanel.setViewportView(medicationTable);
	
		// Add sorting
		medicationTable.setAutoCreateRowSorter(true);
		
		//////////////////////////////
				
		// Prepare health problem panel
		JPanel healthLogPanel = new JPanel(new BorderLayout(0, 0));
		southPanel.add(healthLogPanel, BorderLayout.SOUTH);
		
		// Prepare health problem label
		JLabel healthLogLabel = new JLabel(Msg.getString("TabPanelHealth.healthLog"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(healthLogLabel);
		healthLogPanel.add(healthLogLabel, BorderLayout.NORTH);
		
		// Prepare health problem scroll panel
		JScrollPane healthLogScrollPanel = new JScrollPane();
		healthLogPanel.add(healthLogScrollPanel, BorderLayout.CENTER);
		
		// Prepare health problem table model
		healthLogTableModel = new HealthLogTableModel(condition);
		
		// Create health problem table
		healthLogTable = new JTable(healthLogTableModel);
		healthLogTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
		healthLogTable.setRowSelectionAllowed(true);
		healthLogTable.setDefaultRenderer(MarsTime.class, new MarsTimeTableCellRenderer());
		healthLogScrollPanel.setViewportView(healthLogTable);
		
		// Add sorting
		healthLogTable.setAutoCreateRowSorter(true);
				
		// Update at least one before displaying it
		update();
	}

	/**
	 * Updates the sleep time.
	 * 
	 * @return
	 */
	private StringBuilder updateSleepTime() {	
		// Checks the 3 best sleep time
    	int [] bestSleepTime = person.getPreferredSleepHours();
		Arrays.sort(bestSleepTime);
		
		// Prepare sleep time TF
		StringBuilder text = new StringBuilder();
		int size = bestSleepTime.length;
		int lastSleepTime = -1;
		for (int i=0; i<size; i++) {
			int sleepTime = bestSleepTime[i];
			if (lastSleepTime != sleepTime) {
				if (i != 0) {
					text.append(",  ");
				}
				text.append(sleepTime)
				.append(" msol (w:")
				.append(person.getSleepWeight(sleepTime))
				.append(")");
				lastSleepTime = sleepTime;
			}
		}
		
		return text;
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
			energyLabel.setText(energyCache + KJ);
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

		double muscleTor0 = Math.round(condition.getMusclePainTolerance()*10.0)/10.0;
		if (muscleTor != muscleTor0) {
			muscleTor = muscleTor0;
			muscleTorLabel.setText(StyleManager.DECIMAL_PLACES1.format(muscleTor0));
		}
		
		double muscleHealth0 = Math.round(condition.getMuscleHealth()*10.0)/10.0;
		if (muscleHealth != muscleHealth0) {
			muscleHealth = muscleHealth0;
			muscleHealthLabel.setText(StyleManager.DECIMAL_PLACES1.format(muscleHealth0));
		}
		
		double muscleSoreness0 = Math.round(condition.getMuscleSoreness()*10.0)/10.0;
		if (muscleSoreness != muscleSoreness0) {
			muscleSoreness = muscleSoreness0;
			muscleSorenessLabel.setText(StyleManager.DECIMAL_PLACES1.format(muscleSoreness0));
		}

		double appetite0 = Math.round(condition.getAppetite()*10.0)/10.0;
		if (appetite != appetite0) {
			appetite = appetite0;
			appetiteLabel.setText(StyleManager.DECIMAL_PLACES1.format(appetite0));
		}
		
		int maxDailyEnergy0 = (int)(condition.getPersonalMaxEnergy());
		if (maxDailyEnergy != maxDailyEnergy0) {
			maxDailyEnergy = maxDailyEnergy0;
			maxDailyEnergyLabel.setText(maxDailyEnergy0 + KJ);
		}

		double bodyMassDev0 = Math.round(condition.getBodyMassDeviation()*10.0)/10.0;
		if (bodyMassDev != bodyMassDev0) {
			bodyMassDev = bodyMassDev0;
			bodyMassDevLabel.setText(StyleManager.DECIMAL_PLACES1.format(bodyMassDev0));
		}
		
		// Update sleep time TF
		StringBuilder text = updateSleepTime();

		if (!sleepTimeTA.getText().equalsIgnoreCase(text.toString()))
			sleepTimeTA.setText(" " + text.toString());
	
		String bedText = "";
		var allocatedBed = person.getBed();
		if (allocatedBed != null) {
			bedText = allocatedBed.getSpotDescription();
		}
 		bedLocationLabel.setText(bedText);
		
		// Update sleep time table model
		sleepExerciseTableModel.update(circadianClock);
		
		// Update food table model
		foodTableModel.update(condition);

		// Update radiation dose table model
		radiationTableModel.update();
		
		// Update medication table model.
		medicationTableModel.update(condition);

		// Update health problem table model.
		var problemsChanged = healthProblemTableModel.update(condition);
		
		// Update health log table model.
		if (problemsChanged) {
			healthLogTableModel.update();
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

		/**
		 * Always returns a String
		 * @param columnIndex Ignored
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> Msg.getString("TabPanelHealth.column.interval"); //$NON-NLS-1$
				case 1 -> Msg.getString("TabPanelHealth.column.BFO"); //$NON-NLS-1$
				case 2 -> Msg.getString("TabPanelHealth.column.ocular"); //$NON-NLS-1$
				case 3 ->  Msg.getString("TabPanelHealth.column.skin"); //$NON-NLS-1$
				default -> null;
			};
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

		private List<HealthProblem> problemsCache;
		private boolean isDead;

		private HealthProblemTableModel(PhysicalCondition condition) {
			problemsCache = new ArrayList<>(condition.getProblems());
		}

		public int getRowCount() {
			return problemsCache.size();
		}

		public int getColumnCount() {
			return 2;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return (columnIndex == 0 ? Msg.getString("TabPanelHealth.column.problem")
									: Msg.getString("TabPanelHealth.column.condition")); //$NON-NLS-1$
		}

		public Object getValueAt(int row, int column) {
			HealthProblem problem = problemsCache.get(row);

			if (column == 0) {
				return problem.getComplaint().getType().getName();
			}
			else if (column == 1) {
				String conditionStr = problem.getState().getName();
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

		public boolean update(PhysicalCondition condition) {
			var changed = false;

			// Make sure problems cache is current.
			if (problemsCache.size() != condition.getProblems().size()) {
				problemsCache = new ArrayList<>(condition.getProblems());
				changed = true;
			}
			isDead = condition.isDead();

			fireTableDataChanged();
			return changed;
		}
	}

	/**
	 * Internal class used as model for the health log table.
	 */
	private static class HealthLogTableModel extends AbstractTableModel {

	
		private List<CuredProblem> history;

		private HealthLogTableModel(PhysicalCondition condition) {
			history = condition.getHealthHistory();
		}

		@Override
		public int getRowCount() {
			return history.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return String.class;
			}
			return MarsTime.class;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> Msg.getString("TabPanelHealth.column.complaint");
				case 1 -> Msg.getString("TabPanelHealth.column.startedon");
				case 2 -> Msg.getString("TabPanelHealth.column.curedon");
				default -> null;
			};
		}

		public Object getValueAt(int row, int column) {
			var h = history.get(row);
			
			return switch(column) {
				case 0 -> h.complaint().getName();
				case 1 -> h.start();
				case 2 -> h.cured();
				default -> null;
			};
		}

		public void update() {			
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

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = null;
			if (columnIndex == 0) {
			    dataType = Integer.class;
			}
			else {
			    dataType = String.class;
			}
			return dataType;
		}

		@Override
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

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return (columnIndex == 0) ? Integer.class : String.class;
		}

		@Override
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
			if (row >= getRowCount()) {
				return null;
			}

			int rowSol = row + solOffset;
			if (column == 0) {
				return rowSol;
			}
			else if (!map.containsKey(rowSol)) {
				return "0.000";
			}
			return StyleManager.DECIMAL_PLACES3.format(returnAmount(rowSol, column-1));
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

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = null;
			if (columnIndex == 0) {
			    dataType = String.class;
			}
			else if (columnIndex == 1) {
			    dataType = Double.class;
			}
			return dataType;
		}

		@Override
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
