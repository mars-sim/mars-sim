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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.person.CircadianClock;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.health.BodyRegionType;
import com.mars_sim.core.person.health.CuredProblem;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.HealthRiskType;
import com.mars_sim.core.person.health.Medication;
import com.mars_sim.core.person.health.RadiationExposure;
import com.mars_sim.core.person.health.RadiationExposure.DoseHistory;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.components.MarsTimeTableCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The HealthTabPanel is a tab panel for a person's health.
 */
@SuppressWarnings("serial")
class TabPanelHealth extends EntityTabPanel<Person>
		implements TemporalComponent {

	private static final String HEALTH_ICON = "health"; //-NLS-1$
	
	/** Max rows to render in the Health Risks list. */
	private static final int RISK_ROWS = 10;

	private static final String THIRTY_DAY = "30-Day";
	private static final String ANNUAL = "Annual";
	private static final String CAREER = "Career";
	
	private static final String WIKI_URL = Msg.getString("TabPanelHealth.radiation.url"); //-NLS-1$
	private static final String[] RADIATION_TOOL_TIPS = {
		    " Exposure Interval - 30-Day, Annual, or Career", 
		    " Standard Dose Limit [mSv] on BFO - 30-Day:  250;  Annual: 1000;  Career: 1500",
		    " Standard Dose Limit [mSv] on Eye - 30-Day:  500;  Annual: 2000;  Career: 3000",
		    " Standard Dose Limit [mSv] on Skin - 30-Day: 1000;  Annual: 4000;  Career: 6000"};

	private JDoubleLabel thirstLabel;
	private JDoubleLabel fatigueLabel;
	private JDoubleLabel hungerLabel;
	private JDoubleLabel energyLabel;
	private JDoubleLabel stressLabel;
	private JDoubleLabel performanceLabel;
	
	private JDoubleLabel leptinLabel;
	private JDoubleLabel ghrelinLabel;
	private JDoubleLabel leptinTLabel;
	private JDoubleLabel ghrelinTLabel;

	private JDoubleLabel muscleTorLabel;
	private JDoubleLabel muscleHealthLabel;
	private JDoubleLabel muscleSorenessLabel;
	
	private JDoubleLabel appetiteLabel;
	private JDoubleLabel maxDailyEnergyLabel;
	private JDoubleLabel bodyMassDevLabel;
	
	private JLabel bedLocationLabel;
	private DefaultListModel<String> sleepTimes;

	private RadiationTableModel radiationTableModel;
	private SleepExerciseTableModel sleepExerciseTableModel;
	private FoodTableModel foodTableModel;
	private HealthProblemTableModel healthProblemTableModel;
	private MedicationTableModel medicationTableModel;
	private HealthLogTableModel healthLogTableModel;
	
	/** The PhysicalCondition instance. */
	private PhysicalCondition condition;
	private CircadianClock circadianClock;


	/**
	 * Constructor.
	 * 
	 * @param person the person to display.
	 * @param context the UI context.
	 */
	public TabPanelHealth(Person person, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelHealth.title"),
			ImageLoader.getIconByName(HEALTH_ICON), null,
			context, person
		);

		condition = person.getPhysicalCondition();
		circadianClock = person.getCircadianClock();
	}
	
	@Override
	protected void buildUI(JPanel content) {
				
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		content.add(northPanel, BorderLayout.NORTH);	
		
		// Prepare condition panel
		AttributePanel conditionPanel = new AttributePanel(8, 2);
		northPanel.add(conditionPanel);
		
		fatigueLabel = new JDoubleLabel(StyleManager.DECIMAL_MSOL, condition.getFatigue());
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.fatigue"), fatigueLabel, null);
		
		energyLabel = new JDoubleLabel(StyleManager.DECIMAL_KJ, condition.getEnergy());
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.energy"), energyLabel, null);
						
		hungerLabel = new JDoubleLabel(StyleManager.DECIMAL_MSOL, condition.getHunger());
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.hunger"), hungerLabel, null);
		
		maxDailyEnergyLabel = new JDoubleLabel(StyleManager.DECIMAL_KJ, condition.getPersonalMaxEnergy());
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.maxDailyEnergy"), maxDailyEnergyLabel, null);
				
		thirstLabel = new JDoubleLabel(StyleManager.DECIMAL_MSOL, condition.getThirst());
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.thirst"), thirstLabel, null);
		
		appetiteLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, condition.getAppetite(), 0.1);
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.appetite"), appetiteLabel, null);
		
		var person = getEntity();
		performanceLabel = new JDoubleLabel(StyleManager.DECIMAL_PERC, person.getPerformanceRating() * 100);
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.performance"), performanceLabel, null);
		
		muscleHealthLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, condition.getMuscleHealth(), 0.1);
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.muscle.health"), muscleHealthLabel, null);

		stressLabel = new JDoubleLabel(StyleManager.DECIMAL_PERC, condition.getStress());
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.stress"), stressLabel, null);
		
		muscleTorLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, condition.getMusclePainTolerance(), 0.1);
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.muscle.tolerance"), muscleTorLabel, null);
		
		bodyMassDevLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, condition.getBodyMassDeviation(), 0.1);
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.bodyMassDev"), bodyMassDevLabel, null);
		
		muscleSorenessLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, condition.getMuscleSoreness(), 0.1);
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.muscle.soreness"), muscleSorenessLabel, null);
	
		leptinLabel = new JDoubleLabel(StyleManager.DECIMAL_MSOL, circadianClock.getLeptin());
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.leptin"), leptinLabel, null);
		
		ghrelinLabel = new JDoubleLabel(StyleManager.DECIMAL_MSOL, circadianClock.getGhrelin());
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.ghrelin"), ghrelinLabel, null);
		
		leptinTLabel = new JDoubleLabel(StyleManager.DECIMAL_MSOL, circadianClock.getLeptinT());
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.leptin.threshold"), leptinTLabel, null);
		
		ghrelinTLabel = new JDoubleLabel(StyleManager.DECIMAL_MSOL, circadianClock.getGhrelinT());
		conditionPanel.addLabelledItem(Msg.getString("TabPanelHealth.ghrelin.threshold"), ghrelinTLabel, null);
		
		northPanel.add(Box.createVerticalStrut(7));
		
		JPanel bedPanel = new JPanel(new BorderLayout(8, 1));
		northPanel.add(bedPanel);
	
		// Prepare bed time ta
		sleepTimes = new DefaultListModel<>();
		var sleeps = new JList<>(sleepTimes);
		sleeps.setToolTipText("The 3 best times to go to bed [msol (weight)]");

		// Wrap with a panel to get border correct
		var sleepsPanel = new JPanel(new BorderLayout());
		sleepsPanel.setBorder(SwingHelper.createLabelBorder("Sleep times"));
		sleepsPanel.add(sleeps, BorderLayout.CENTER);
		
		// Prepare panel for bed time ta .
		JPanel bedTimePanel = new JPanel(new BorderLayout(0, 0));
		bedTimePanel.add(sleepsPanel, BorderLayout.CENTER);
		bedPanel.add(bedTimePanel, BorderLayout.CENTER);

		// Prepare panel for bed location
		JPanel bedLocPanel = new JPanel(new BorderLayout(0, 0));
		bedLocationLabel = new JLabel("Assigned : ", SwingConstants.CENTER);
		bedLocPanel.add(bedLocationLabel);
		bedPanel.add(bedLocPanel, BorderLayout.NORTH);
			
		// Prepare middle panel
        JPanel midPanel = new JPanel(new BorderLayout());
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
		content.add(midPanel, BorderLayout.CENTER);

		// Prepare bottom panel
		JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
		content.add(bottomPanel, BorderLayout.SOUTH);
				
		// Prepare sleep time panel
		JPanel sleepPanel = new JPanel(new BorderLayout(0, 0));
		midPanel.add(sleepPanel, BorderLayout.NORTH);

		// Prepare sleep time label
		JLabel sleepLabel = new JLabel(Msg.getString("TabPanelHealth.sleepExercise"), //$NON-NLS-1$
				SwingConstants.CENTER); 
		StyleManager.applySubHeading(sleepLabel);
		sleepPanel.add(sleepLabel, BorderLayout.NORTH);

		// Prepare sleep time scroll panel
		JScrollPane sleepScrollPanel = new JScrollPane();
		sleepPanel.add(sleepScrollPanel, BorderLayout.CENTER);

		// Prepare sleep time table model
		sleepExerciseTableModel = new SleepExerciseTableModel(circadianClock);
		
		var tableSize = new Dimension(225, 70);

		// Create sleep time table
		var sleepExerciseTable = new JTable(sleepExerciseTableModel);
		TableColumnModel sModel = sleepExerciseTable.getColumnModel();
		sleepExerciseTable.setPreferredScrollableViewportSize(tableSize);
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
		var foodTable = new JTable(foodTableModel);
		foodTable.setPreferredScrollableViewportSize(tableSize);
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
        var doseLimits = condition.getRadiationExposure().getDoseLimits();
        
        String[] limits = new String[3];
        for(var r : BodyRegionType.values()) {
			int i = r.ordinal();
            limits[i] = "Dose Limit [mSv] on " + r.getName()
            	+ " - 30-Day: " + Math.round(doseLimits[i].getThirtyDay()*10.0)/10.0
            	+ ";  Annual: " + Math.round(doseLimits[i].getAnnual()*10.0)/10.0
            	+ ";  Career: " + Math.round(doseLimits[i].getCareer()*10.0)/10.0;
        }
 
		// Create radiation table
		var radiationTable = new JTable(radiationTableModel) {
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
			
		// Prepare attribute panel.
		AttributePanel attributePanel = new AttributePanel();
		
		JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		listPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		listPanel.add(attributePanel, BorderLayout.CENTER);
		bottomPanel.add(listPanel, BorderLayout.NORTH);
		
		listPanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("TabPanelHealth.healthRisks.title")));

		Map<HealthRiskType, Double> riskMap = condition.getHealthRisks();
		
		List<HealthRiskType> riskList = new ArrayList<>(riskMap.keySet());
		// Sort by descending risk value and cap the number of displayed rows.
		riskList.sort((a, b) -> Double.compare(
				riskMap.getOrDefault(b, 0D),
				riskMap.getOrDefault(a, 0D)));
		int rows = Math.min(RISK_ROWS, riskList.size());
		for (int i = 0; i < rows; i++) {
			HealthRiskType type = riskList.get(i);
			double pct = Math.round(riskMap.getOrDefault(type, 0D) * 100.0) / 100.0;
			attributePanel.addTextField(type.getName(), pct + " %", null);
		}

				
		JPanel southPanel = new JPanel(new BorderLayout());
		bottomPanel.add(southPanel, BorderLayout.CENTER);
				
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
		var healthProblemTable = new JTable(healthProblemTableModel);
		healthProblemTable.setPreferredScrollableViewportSize(new Dimension(225, 50));
		healthProblemTable.setRowSelectionAllowed(true);
		healthProblemScrollPanel.setViewportView(healthProblemTable);

		// Add sorting
		healthProblemTable.setAutoCreateRowSorter(true);

		
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
		var medicationTable = new JTable(medicationTableModel);
		medicationTable.setPreferredScrollableViewportSize(new Dimension(225, 50));
		medicationTable.setRowSelectionAllowed(true);
		medicationScrollPanel.setViewportView(medicationTable);
	
		// Add sorting
		medicationTable.setAutoCreateRowSorter(true);
						
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
		var healthLogTable = new JTable(healthLogTableModel);
		healthLogTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
		healthLogTable.setRowSelectionAllowed(true);
		healthLogTable.setDefaultRenderer(MarsTime.class, new MarsTimeTableCellRenderer());
		healthLogScrollPanel.setViewportView(healthLogTable);
		
		// Add sorting
		healthLogTable.setAutoCreateRowSorter(true);
				
		// Update at least one before displaying it
		clockUpdate(null);
		updateSleepTime(getEntity());
	}

	/**
	 * Updates the sleep time.
	 * @param  person Person being observed 
	 * 
	 * @return Text representation of sleep time
	 */
	private void updateSleepTime(Person person) {	
		// Checks the 3 best sleep time
		sleepTimes.clear();

    	int [] bestSleepTime = person.getPreferredSleepHours();
		int size = bestSleepTime.length;
		if (size == 0) {
			sleepTimes.addElement("Not Known yet");
		}
		Arrays.sort(bestSleepTime);
		
		// Prepare sleep time TF
		for (int i=0; i<size; i++) {
			int sleepTime = bestSleepTime[i];
			String text = sleepTime + " msol (w:" + person.getSleepWeight(sleepTime) + ")";
			sleepTimes.addElement(text);
		}
	}
	
	/**
	 * Update the sleep time on re-selection of the tab.
	 */
	@Override
	public void refreshUI() {
		updateSleepTime(getEntity());
	}

	/**
	 * Updates the info on this panel in the clock pulse as the values displayed are affected
	 * by many actions
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {

		var person = getEntity();

		// Update fatigue
		fatigueLabel.setValue(condition.getFatigue());
		thirstLabel.setValue(condition.getThirst());
		hungerLabel.setValue(condition.getHunger());
		energyLabel.setValue(condition.getEnergy());
		stressLabel.setValue(condition.getStress());
		performanceLabel.setValue(person.getPerformanceRating() * 100);
		leptinLabel.setValue(circadianClock.getLeptin());
		ghrelinLabel.setValue(circadianClock.getGhrelin());
		leptinTLabel.setValue(circadianClock.getLeptinT());
		ghrelinTLabel.setValue(circadianClock.getGhrelinT());
		muscleTorLabel.setValue(condition.getMusclePainTolerance());
		muscleHealthLabel.setValue(condition.getMuscleHealth());
		muscleSorenessLabel.setValue(condition.getMuscleSoreness());
		appetiteLabel.setValue(condition.getAppetite());
		maxDailyEnergyLabel.setValue(condition.getPersonalMaxEnergy());
		bodyMassDevLabel.setValue(condition.getBodyMassDeviation());

		String bedText = "";
		var allocatedBed = person.getBed();
		if (allocatedBed != null) {
			bedText = allocatedBed.getSpotDescription();
		}
 		bedLocationLabel.setText("Assigned : " + bedText);
		
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
	private static class RadiationTableModel extends AbstractTableModel {

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

			// Find the lowest sol day in the data, guard against empty sets
			if (largestSet.isEmpty()) {
				solOffset = 1;
				rowCount = 0;
			}
			else {
				solOffset = largestSet.stream()
						.mapToInt(v -> v)               
						.min()                          
						.orElse(1);
				rowCount = largestSet.size();
			}

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
			
			// Find the lowest sol day in the data, guard against empty map
			if (map.isEmpty()) {
				solOffset = 1;
			}
			else {
				solOffset = map.keySet().stream()
						.mapToInt(v -> v)               
						.min()                          
						.orElse(1);
			}
			
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
			return switch (columnIndex) {
				case 0 -> Msg.getString("TabPanelHealth.column.medication");
				case 1 -> Msg.getString("TabPanelHealth.column.duration");
				default -> null;
			};
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
