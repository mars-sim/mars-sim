/*
 * Mars Simulation Project
 * TimeTool.java
 * @date 2025-08-05
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.time;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import com.formdev.flatlaf.FlatClientProperties;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.environment.OrbitInfo;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.ClockUtils;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MarsTimeFormat;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The TimeWindow is a tool window that displays the current Martian date and time and simulation parameters.
 */
public class TimeTool extends ContentPanel {

	// Milliseconds between updates to date fields
	private static final long DATE_UPDATE_PERIOD = 300L;

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "time";
	public static final String ICON = "time";
	public static final String TITLE = Msg.getString("TimeWindow.title"); //$NON-NLS-1$

	private static final int WIDTH = 320;
	private static final int HEIGHT = 650;
	
	// Label Strings
	private static final String X_END = " x";
	private static final String DESIRE_TR = "Desired TR";
	private static final String AVERAGE_TPS = "Average TPS";

	private static final String WIKI_URL = Msg.getString("TimeWindow.calendar.url"); //$NON-NLS-1$
		
	/** the execution time label string */
	private static final String EXEC = "Execution";
	/** the sleep time label string */
	private static final String SLEEP_TIME = "Sleep";
	/** the lead time pulse width label string */
	private static final String LEAD_PULSE_TIME = "Lead Pulse Width";
	/** the pulse deviation label string */
	private static final String PULSE_DEVIATION = "Pulse Deviation";
	/** the optimal pulse label string */
	private static final String OPTIMAL = "Optimal Pulse Width";
	/** the reference pulse label string */
	private static final String REFERENCE = "Ref Pulse Width";
	/** the reference pulse label string */
	private static final String TASK_PULSE_TIME = "Task Pulse Width";
	/** the time ratio string */
	private static final String ACTUAL_TIME_RATIO = Msg.getString("TimeWindow.actualTRHeader"); //$NON-NLS-1$
	/** the execution time unit */
	private static final String MS = " ms";
	/** the Universal Mean Time abbreviation */
	private static final String UMT = " (UMT) ";


	// Data members
	/** The time in ms when last updated. */
	private long lastUpdateTime = 0;
	
	private int solCache;
	
	private String northernSeasonCache = "";
	private String southernSeasonCache = "";

	/** Martian calendar panel. */
	private MarsCalendarDisplay calendarDisplay;

	/** The cpu util spinner */
	private JSpinner cpuSpinner;
	/** The task pulse damper spinner */
	private JSpinner taskPulseDamperSpinner;
	/** The task pulse ratio spinner */
	private JSpinner taskPulseRatioSpinner;
	/** The ref pulse ratio spinner */
	private JSpinner refPulseRatioSpinner;
	/** The ref pulse damper spinner */
	private JSpinner refPulseDamperSpinner;
	/** label for Martian time. */
	private JLabel martianTimeLabel;
	/** label for areocentric longitude. */
	private JLabel lonLabel;
	/** label for Northern hemisphere season. */
	private JLabel northernSeasonLabel;
	/** label for Southern hemisphere season. */
	private JLabel southernSeasonLabel;
	/** label for uptimer. */
	private JLabel uptimeLabel;
	/** label for pulses per second label. */
	private JLabel ticksPerSecLabel;
	/** label for pulses per second label. */
	private JLabel averageTPSLabel;
	/** label for actual time ratio. */
	private JLabel actualTRLabel;
	/** label for desire time ratio. */
	private JLabel desireTRLabel;
	/** label for pulse deviation percent. */
	private JLabel pulseDeviationLabel;
	/** label for optimal pulse width. */
	private JLabel optimalPulseLabel;
	/** label for rff pulse width. */
	private JLabel refPulseLabel;
	/** label for execution time. */
	private JLabel execTimeLabel;
	/** label for sleep time. */
	private JLabel sleepTimeLabel;
	/** label for lead pulse width. */
	private JLabel leadPulseLabel;
	/** label for ref pulse width. */
	private JLabel taskPulseLabel;
	/** label for time compression. */
	private JLabel realTimeClockLabel;

	private JLabel monthLabel;

	private JLabel weeksolLabel;
	
	private OrbitInfo orbitInfo;

	
	/**
	 * Constructs a TimeTool content panel
	 *
	 * @param sim the simulation
	 */
	public TimeTool(Simulation sim) {
		// Use TimeWindow constructor
		super(NAME, TITLE, Placement.RIGHT);
	

		// Initialize data members
		MasterClock masterClock = sim.getMasterClock();
		MarsTime marsTime = masterClock.getMarsTime();
		orbitInfo = sim.getOrbitInfo();
		
		// Set up scroll pane
		var scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(20);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		
		// Set up main pane
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setPreferredSize(new Dimension(WIDTH - 5, HEIGHT));
		mainPane.setBorder(new MarsPanelBorder());

		scrollPane.setViewportView(mainPane);
		scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		// Set up martian pane
		JPanel martianPane = new JPanel(new BorderLayout());
		mainPane.add(martianPane, BorderLayout.NORTH);
		
		// Create Martian time panel
		JPanel martianTimePane = new JPanel(new BorderLayout());
		martianPane.add(martianTimePane, BorderLayout.NORTH);
	
		// Create Martian time header label
		martianTimeLabel = new JLabel();
		martianTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		martianTimeLabel.setVerticalAlignment(SwingConstants.CENTER);
		martianTimeLabel.setText("");
		martianTimeLabel.setToolTipText("Mars Timestamp in Universal Mean Time (UMT)");
		martianTimePane.add(martianTimeLabel, BorderLayout.CENTER);
		martianTimePane.setBorder(StyleManager.createLabelBorder(Msg.getString("TimeWindow.martianTime"))); //$NON-NLS-1$

		JButton wikiButton = new JButton(GuideWindow.wikiIcon);
		wikiButton.setAlignmentX(.5f);
		wikiButton.setAlignmentY(.5f);
		wikiButton.setToolTipText("Open Timekeeping Wiki in GitHub");
		wikiButton.addActionListener(e -> SwingHelper.openBrowser(WIKI_URL));

		JPanel linkPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		linkPane.add(wikiButton, SwingConstants.CENTER);
		martianTimePane.add(linkPane, BorderLayout.EAST);
		
		// Create Martian month panel
		JPanel martianMonthPane = new JPanel(new BorderLayout());
		martianMonthPane.setPreferredSize(new Dimension(WIDTH, 160));
		martianMonthPane.setBorder(StyleManager.createLabelBorder(Msg.getString("TimeWindow.martianMonth"))); //$NON-NLS-1$
		martianPane.add(martianMonthPane, BorderLayout.CENTER);
		
		// Create Martian calendar label panel
		AttributePanel labelPane = new AttributePanel(1, 2);
		labelPane.setAlignmentX(SwingConstants.CENTER);
		labelPane.setAlignmentY(SwingConstants.CENTER);
		martianMonthPane.add(labelPane, BorderLayout.NORTH);
		
		String mn = marsTime.getMonthName();
		monthLabel = labelPane.addTextField("Month", mn, null);
		
		String wd = MarsTimeFormat.getSolOfWeekName(marsTime);
		weeksolLabel = labelPane.addTextField("Weeksol", wd, null);

		// Create Martian calendar month panel
		JPanel calendarPane = new JPanel();
		calendarPane.setLayout(new BoxLayout(calendarPane, BoxLayout.Y_AXIS));
		calendarPane.setAlignmentX(SwingConstants.CENTER);
		calendarPane.setAlignmentY(SwingConstants.CENTER);
		martianMonthPane.add(calendarPane, BorderLayout.CENTER);

		JPanel innerCalendarPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		innerCalendarPane.setAlignmentX(SwingConstants.CENTER);
		innerCalendarPane.setAlignmentY(SwingConstants.CENTER);
	
		// Create Martian calendar display
		calendarDisplay = new MarsCalendarDisplay(marsTime);
		innerCalendarPane.add(calendarDisplay);
		calendarPane.add(innerCalendarPane, BorderLayout.CENTER);


		JPanel seasonPane = new JPanel(new BorderLayout());
		martianPane.add(seasonPane, BorderLayout.SOUTH);

		// Create Martian hemisphere panel
		AttributePanel hemiPane = new AttributePanel(3);
		seasonPane.add(hemiPane, BorderLayout.NORTH);		
		hemiPane.setBorder(StyleManager.createLabelBorder(Msg.getString("TimeWindow.martianSeasons"))); //$NON-NLS-1$

		String str =
				"<html>&#8201;Earth (days) vs Mars (sols)" +
				"<br>&#8201;Spring : 93 days vs 199 sols" +
				"<br>&#8201;Summer : 94 days vs 184 sols" +
				"<br>&#8201;Fall : 89 days vs 146 sols" +
				"<br>&#8201;Winter : 89 days vs 158 sols</html>";

		hemiPane.setToolTipText(str);

//		Note :
//		&#8201; Thin tab space
//		&#8194; En tab space
//		&#8195; Em tab space
		
		northernSeasonLabel = hemiPane.addTextField(Msg.getString("TimeWindow.northernHemisphere"), //$NON-NLS-1$
													"", null);
		southernSeasonLabel = hemiPane.addTextField(Msg.getString("TimeWindow.southernHemisphere"), //$NON-NLS-1$
													"", null);
		// Create areocentric longitude header label
		lonLabel = hemiPane.addTextField(Msg.getString("TimeWindow.areocentricLon"), "", null);
		lonLabel.setToolTipText("The Areocentric Longitude (L_s) of Mars with respect to the Sun");
		
		JPanel paramPane = new JPanel(new BorderLayout());
		mainPane.add(paramPane, BorderLayout.CENTER);

		JPanel attributePane = new JPanel(new BorderLayout());
		paramPane.add(attributePane, BorderLayout.NORTH);
		
		// Create speed panel
		AttributePanel speedPane = new AttributePanel(8);
		speedPane.setBorder(StyleManager.createLabelBorder(Msg.getString("TimeWindow.simParam"))); //$NON-NLS-1$
		attributePane.add(speedPane, BorderLayout.NORTH);

		ticksPerSecLabel = speedPane.addTextField(Msg.getString("TimeWindow.ticksPerSecond"), "", //$NON-NLS-1$
				"The current ticks per sec");
		averageTPSLabel = speedPane.addTextField(AVERAGE_TPS, "", 
				"The average ticks per sec");
		execTimeLabel = speedPane.addTextField(EXEC, "", 
				"The last execution time of a tick");
		sleepTimeLabel = speedPane.addTextField(SLEEP_TIME, "", 
				"The sleep time [ms] of the last tick");
		
		
		actualTRLabel = speedPane.addTextField(ACTUAL_TIME_RATIO, "",
				"Master clock's actual time ratio");
		desireTRLabel = speedPane.addTextField(DESIRE_TR, "",
				"Master clock's desire time ratio");
		realTimeClockLabel = speedPane.addTextField(Msg.getString("TimeWindow.rtc"), "", 
				"The amount of simulation time at the passing of each second of the real time"); //$NON-NLS-1$
		uptimeLabel = speedPane.addTextField(Msg.getString("TimeWindow.simUptime"), "", 
				"The amount of real time the simulation has been running"); //$NON-NLS-1$
	
		// Create pulse panel
		AttributePanel pulsePane = new AttributePanel(5);
		pulsePane.setBorder(StyleManager.createLabelBorder(Msg.getString("TimeWindow.pulseParams"))); //$NON-NLS-1$
		attributePane.add(pulsePane, BorderLayout.CENTER);
		
		taskPulseLabel = pulsePane.addTextField(TASK_PULSE_TIME, "", 
				"How many millisol the task pulse width is");
		refPulseLabel = pulsePane.addTextField(REFERENCE, "", 
				"How many millisol the reference pulse width is");
		optimalPulseLabel = pulsePane.addTextField(OPTIMAL, "", 
				"How many millisol the optimal pulse width is");
		pulseDeviationLabel = pulsePane.addTextField(PULSE_DEVIATION, "", 
				"The percentage of deviation between the optimal pulse width and the next pulse width");
		leadPulseLabel = pulsePane.addTextField(LEAD_PULSE_TIME, "", 
				"How many millisol the leading pulse width will be");
	
		createAdvancePanel(masterClock, paramPane);
		
		setPreferredSize(new Dimension(WIDTH, HEIGHT));

		// Update the two time labels
		updateFastLabels(masterClock);
		updateDateLabels(masterClock);
		updateTimeLabels(masterClock);
		
		// Update season labels
		updateSeason();
	}
	
	/**
	 * Creates the advanced panel for adjusting pulse params.
	 * 
	 * @param masterClock
	 * @param pane
	 */
	private void createAdvancePanel(MasterClock masterClock, JPanel pane) {
		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
		taskPaneContainer.setPreferredSize(new Dimension(WIDTH - 10, 180));
		JXTaskPane actionPane = new JXTaskPane();
		actionPane.setPreferredSize(new Dimension(WIDTH - 10, 180));
		actionPane.setBackground(new Color(0, 0, 0, 128));
		actionPane.setBackground(getBackground());
		actionPane.setOpaque(false);
		actionPane.setTitle("Advanced Setting");
		actionPane.setSpecial(true); // This can be used to highlight a primary task pane
		taskPaneContainer.add(actionPane); 	
		pane.add(taskPaneContainer, BorderLayout.CENTER);
		
		
		// Create the cpu spinner
		float value = Math.round(masterClock.getCPUUtil() * 100.0)/100.0f;
		float min = Math.round(value / 5 * 100.0)/100.0f;
		float max = Math.round(5 * value * 100.0)/100.0f;
		float step = Math.round(value / 20 * 100.0)/100.0f;
		cpuSpinner = createSpinner(value, min, max, step);
		cpuSpinner.addChangeListener(e -> {
			float cpu = ((SpinnerNumberModel)(cpuSpinner.getModel())).getNumber().floatValue();
			// Change the pulse load
			masterClock.setCPUUtil(cpu);
		});
		
		JPanel cpuPane = createPane("cpuUtil");
		cpuPane.add(cpuSpinner);
		
		JButton cpuButton = createResetButton();
		cpuButton.addActionListener(e ->
             masterClock.computeOriginalCPULoad()
        );
		cpuPane.add(cpuButton);
		
		actionPane.add(cpuPane);
		
		
		// Create the ref pulse ratio spinner
		value = Math.round(masterClock.getRefPulseRatio() * 100.0)/100.0f;
		min = .05f;
		max = 1; 
		step = .05f; 
		refPulseRatioSpinner = createSpinner(value, min, max, step);
		refPulseRatioSpinner.addChangeListener(e -> {
			float rpr = ((SpinnerNumberModel)(refPulseRatioSpinner.getModel())).getNumber().floatValue();
			// Change the ref pulse ratio
			masterClock.setRefPulseRatio(rpr);
		});
		
		JPanel rpRatioPane = createPane("refPulseRatio");
		rpRatioPane.add(refPulseRatioSpinner);
		
		JButton rprButton = createResetButton();
		rprButton.addActionListener(e -> 
             masterClock.resetRefPulseRatio()
         );
		rpRatioPane.add(rprButton);
		
		actionPane.add(rpRatioPane);
		
		// Create the ref pulse damper spinner
		value = masterClock.getRefPulseDamper();
		min = 5;
		max = 1000;
		step = 5;
		refPulseDamperSpinner = createSpinner(value, min, max, step);
		refPulseDamperSpinner.addChangeListener(e -> {
			int rpd = ((SpinnerNumberModel)(refPulseDamperSpinner.getModel())).getNumber().intValue();
			// Change the ref pulse damper
			masterClock.setRefPulseDamper(rpd);
		});
		
		JPanel rpDamperPane = createPane("refPulseDamper"); 
		rpDamperPane.add(refPulseDamperSpinner);
		
		JButton rpdButton = createResetButton();
		rpdButton.addActionListener(e -> 
             masterClock.resetRefPulseDamper()
        );
		rpDamperPane.add(rpdButton);
		
		actionPane.add(rpDamperPane);
		

		// Create the task pulse ratio spinner
		value = Math.round(masterClock.getTaskPulseRatio() * 100.0)/100.0f;
		min = .05f;
		max = 1;
		step = .05f;
		taskPulseRatioSpinner = createSpinner(value, min, max, step);
		taskPulseRatioSpinner.addChangeListener(e -> {
			float tpr = ((SpinnerNumberModel)(taskPulseRatioSpinner.getModel())).getNumber().floatValue();
			// Change the task pulse ratio
			masterClock.setTaskPulseRatio(tpr);
		});
		
		JPanel tpRatioPane = createPane("taskPulseRatio");
		tpRatioPane.add(taskPulseRatioSpinner);
		
		JButton tprButton = createResetButton();
		tprButton.addActionListener(e ->
             masterClock.resetTaskPulseRatio()
        );
		tpRatioPane.add(tprButton);
	
		actionPane.add(tpRatioPane);
		
		
		// Create the task pulse damper spinner
		value = masterClock.getTaskPulseDamper();
		min = 5;
		max = 1000;
		step = 5;
		taskPulseDamperSpinner = createSpinner(value, min, max, step);
		taskPulseDamperSpinner.addChangeListener(e -> {
			int tpd = ((SpinnerNumberModel)(taskPulseDamperSpinner.getModel())).getNumber().intValue();
			// Change the task pulse damper
			masterClock.setTaskPulseDamper(tpd);
		});
		
		JPanel tpDamperPane = createPane("taskPulseDamper");
		tpDamperPane.add(taskPulseDamperSpinner);
		
		JButton tpdButton = createResetButton();
		tpdButton.addActionListener(e ->
             masterClock.resetTaskPulseDamper()
        );
		tpDamperPane.add(tpdButton);
	
		actionPane.add(tpDamperPane);
	}

	/**
	 * Creates a pane.
	 * 
	 * @param label
	 * @return
	 */
	private JPanel createPane(String label) {
		String fullStr = "TimeWindow." + label;
		fullStr = Msg.getString(fullStr); //$NON-NLS-1$
		JPanel tpDamperPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		JLabel label3 = new JLabel(fullStr);
		label3.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		fullStr = "TimeWindow." + label + ".tooltip";
		fullStr = Msg.getString(fullStr); //$NON-NLS-1$
		tpDamperPane.add(label3);
		tpDamperPane.setToolTipText(fullStr); 
		return tpDamperPane;
	}
	
	/**
	 * Creates a reset button.
	 * 
	 * @return
	 */
	private JButton createResetButton() {
		JButton tpdButton = new JButton("\u238c");
		tpdButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		tpdButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
		tpdButton.setToolTipText(Msg.getString("TimeWindow.reset.tooltip")); 
		return tpdButton;
	}
	
	/**
	 * Creates a spinner.
	 * 
	 * @param masterClock
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	private JSpinner createSpinner(double value, double min, double max, double step) {
		
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);

		spinnerModel.setValue(value);
		
		JSpinner spinner = new JSpinner(spinnerModel);	
		
		spinner.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		// 1. Get the editor component of your spinner:
		Component spinnerEditor = spinner.getEditor();
		// 2. Get the text field of your spinner's editor:
		JFormattedTextField jftf = ((JSpinner.DefaultEditor) spinnerEditor).getTextField();
		// 3. Set a default size to the text field:
		jftf.setColumns(3);
	
		jftf.setHorizontalAlignment(SwingConstants.RIGHT);
		
		return spinner;
	}
	
	
	/**
	 * Updates various time labels.
	 * 
	 * @param masterClock
	 */
	private void updateTimeLabels(MasterClock masterClock) {
		
		// Update the cpu util spinner
		float value0 = Math.round(masterClock.getCPUUtil() * 100.0)/100.0f;	
		SpinnerNumberModel spinnerModel0 = (SpinnerNumberModel)(cpuSpinner.getModel());
		float spinValue0 = spinnerModel0.getNumber().floatValue();
		if (spinValue0 != value0) {
			spinnerModel0.setValue(value0);
		}

		// Update the ref pulse damper spinner
		int value2 = masterClock.getRefPulseDamper();
		SpinnerNumberModel spinnerModel2 = (SpinnerNumberModel)(refPulseDamperSpinner.getModel());
		int spinValue2 = spinnerModel2.getNumber().intValue();
		if (spinValue2 != value2) {
			spinnerModel2.setValue(value2);
		}

		// Update the ref pulse ratio spinner
		float value3 = Math.round(masterClock.getRefPulseRatio() * 100.0)/100.0f;
		SpinnerNumberModel spinnerModel3 = (SpinnerNumberModel)(refPulseRatioSpinner.getModel());
		float spinValue3 = spinnerModel3.getNumber().floatValue();
		if (spinValue3 != value3) {
			spinnerModel3.setValue(value3);
		}
		
		// Update the task pulse damper spinner
		int value4 = masterClock.getTaskPulseDamper();
		SpinnerNumberModel spinnerModel4 = (SpinnerNumberModel)(taskPulseDamperSpinner.getModel());
		int spinValue4 = spinnerModel4.getNumber().intValue();
		if (spinValue4 != value4) {
			spinnerModel4.setValue(value4);
		}

		// Update the task pulse ratio spinner
		float value5 = Math.round(masterClock.getTaskPulseRatio() * 100.0)/100.0f;
		SpinnerNumberModel spinnerModel5 = (SpinnerNumberModel)(taskPulseRatioSpinner.getModel());
		float spinValue5 = spinnerModel5.getNumber().floatValue();
		if (spinValue5 != value5) {
			spinnerModel5.setValue(value5);
		}
		
		
		// Update execution time label
		short execTime = masterClock.getExecutionTime();
		execTimeLabel.setText(execTime + MS);

		// Update sleep time label
		float sleepTime = masterClock.getSleepTime();
		sleepTimeLabel.setText(sleepTime + MS);

		// Update pulse width label
		float leadPulse = masterClock.getLeadPulseTime();
		float refPulse = masterClock.getReferencePulse();
		float optPulse = masterClock.getOptPulseTime();
		float taskPulse = masterClock.geTaskPulseWidth();
		
		StringBuilder taskPulseText = new StringBuilder();
		taskPulseText.append(StyleManager.DECIMAL_PLACES4.format(taskPulse));
		taskPulseLabel.setText(taskPulseText.toString());

		StringBuilder leadPulseText = new StringBuilder();
		leadPulseText.append(StyleManager.DECIMAL_PLACES4.format(leadPulse));
		leadPulseLabel.setText(leadPulseText.toString());
		
		// Update pulse deviation label
		double percent = masterClock.getNextPulseDeviation() * 100;
		StringBuilder pulseDevText = new StringBuilder();
		pulseDevText.append(StyleManager.DECIMAL1_PERC.format(percent));
		pulseDeviationLabel.setText(pulseDevText.toString());
		
		StringBuilder refText = new StringBuilder();
		refText.append(StyleManager.DECIMAL_PLACES4.format(refPulse));
		refPulseLabel.setText(refText.toString());
		
		StringBuilder optimalText = new StringBuilder();
		optimalText.append(StyleManager.DECIMAL_PLACES4.format(optPulse));
		optimalPulseLabel.setText(optimalText.toString());
		
		// Update actual TR label
		StringBuilder atrText = new StringBuilder();
		atrText.append(StyleManager.DECIMAL_PLACES1.format(masterClock.getActualTR()))
			  .append(X_END);
		
		StringBuilder dtrText = new StringBuilder();
		dtrText.append(StyleManager.DECIMAL_PLACES1.format(masterClock.getDesiredTR()))
			  .append(X_END);
			  
		actualTRLabel.setText(atrText.toString());
		desireTRLabel.setText(dtrText.toString());
		
		// Update real time clock (RTC) or time compression label
		realTimeClockLabel.setText(ClockUtils.getRTCString(masterClock.getActualTR()));
	}

	/**
	 * Sets and updates the season labels.
	 */
	private void updateSeason() {
  String southernSeasonTip = "";
  String northernSeasonTip ="";
		String northernSeason = orbitInfo.getSeason(OrbitInfo.NORTHERN_HEMISPHERE);
		String southernSeason = orbitInfo.getSeason(OrbitInfo.SOUTHERN_HEMISPHERE);
	
		if (!northernSeasonCache.equals(northernSeason)) {
			northernSeasonCache = northernSeason;

			if (orbitInfo.getSeason(OrbitInfo.NORTHERN_HEMISPHERE) != null) {
				northernSeasonLabel.setText(northernSeason);
			}

			northernSeasonTip = getSeasonTip(northernSeason);
			northernSeasonLabel.setToolTipText(northernSeasonTip);
		}

		if (!southernSeasonCache.equals(southernSeason)) {
			southernSeasonCache = southernSeason;

			if (orbitInfo.getSeason(OrbitInfo.SOUTHERN_HEMISPHERE) != null) {
				southernSeasonLabel.setText(southernSeason);
			}

			southernSeasonTip = getSeasonTip(southernSeason);
			southernSeasonLabel.setToolTipText(southernSeasonTip);
		}
	}

	/**
	 * Gets the text for the season label tooltip.
	 *
	 * @param hemi the northern or southern hemisphere
	 */
	private static String getSeasonTip(String hemi) {
		if (hemi.contains("Spring"))
			return Msg.getString("TimeWindow.season.spring");
		else if (hemi.contains("Summer"))
			return Msg.getString("TimeWindow.season.summer");
		else if (hemi.contains("Autumn"))
			return Msg.getString("TimeWindow.season.autumn");
		else if (hemi.contains("Winter"))
			return Msg.getString("TimeWindow.season.winter");
		else
			return null;
	}

	/**
	 * Updates the calendar, the areocentric longitude and the time labels via ui pulse.
	 * 
	 * @param mc
	 */
	private void updateDateLabels(MasterClock mc) {
		
		String mn = mc.getMarsTime().getMonthName();
		monthLabel.setText(mn);
		
		String wd = MarsTimeFormat.getSolOfWeekName(mc.getMarsTime());
		weeksolLabel.setText(wd);
		
		// Update the calendar
		calendarDisplay.update(mc.getMarsTime());
		// Update areocentric longitude
		lonLabel.setText(Math.round(orbitInfo.getSunAreoLongitude() * 10_000.0)/10_000.0 + "");	
		
		int sol = mc.getMarsTime().getMissionSol();
		// Note: must use the local solCache for updating UI element
		if (solCache != sol) {
			solCache = sol;
			// Update season
			updateSeason();
		}
	}

	/**
	 * Updates date and time in Time Tool via clock pulse.
	 * 
	 * @param mc
	 */
	private void updateFastLabels(MasterClock mc) {
		MarsTime mTime = mc.getMarsTime();
		String ts = mTime.getDateTimeStamp() + " " + MarsTimeFormat.getSolOfWeekName(mTime) + UMT;
		martianTimeLabel.setText(ts);

		// Update average TPS label
		double atps = mc.getAveragePulsesPerSecond();
		StringBuilder atpsText = new StringBuilder();
		atpsText.append(StyleManager.DECIMAL_PLACES2.format(atps));
		averageTPSLabel.setText(atpsText.toString());

		double ctps = mc.getCurrentPulsesPerSecond();
		StringBuilder ctpsText = new StringBuilder();
		ctpsText.append(StyleManager.DECIMAL_PLACES2.format(ctps));
		ticksPerSecLabel.setText(ctpsText.toString());
		
		uptimeLabel.setText(mc.getUpTimer().getUptime());
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		MasterClock masterClock = pulse.getMasterClock();

		// update the fast labels
		updateFastLabels(masterClock);

		long currentTime = System.currentTimeMillis();
		if ((currentTime - lastUpdateTime) > DATE_UPDATE_PERIOD) {
			// update the slow labels
			updateDateLabels(masterClock);
			updateTimeLabels(masterClock);
			lastUpdateTime = currentTime;
		}
	}
}
