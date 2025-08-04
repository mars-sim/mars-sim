/*
 * Mars Simulation Project
 * TimeWindow.java
 * @date 2025-07-13
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.time;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.environment.OrbitInfo;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.ClockUtils;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MarsTimeFormat;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.tool_window.ToolWindow;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The TimeWindow is a tool window that displays the current Martian date and time and simulation parameters.
 */
public class TimeWindow extends ToolWindow {

	// Milliseconds between updates to date fields
	private static final long DATE_UPDATE_PERIOD = 300L;

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "time";
	public static final String ICON = "time";
	public static final String TITLE = Msg.getString("TimeWindow.title"); //$NON-NLS-1$

	public static final int WIDTH = 300;
	public static final int HEIGHT = 650;
	
	// Label Strings
	public final String X_END = " x";
	public final String DESIRE_TR = "Desired TR";
	public final String AVERAGE_TPS = "Average TPS";

	public final String WIKI_URL = Msg.getString("TimeWindow.calendar.url"); //$NON-NLS-1$
	public final String WIKI_TEXT = Msg.getString("TimeWindow.calendar.title"); //$NON-NLS-1$	
	
//    private final DateTimeFormatter DATE_TIME_FORMATTER = DateCommand.DATE_TIME_FORMATTER;
	
	/** the execution time label string */
	private final String EXEC = "Execution";
	/** the sleep time label string */
	private final String SLEEP_TIME = "Sleep";
	/** the time pulse width label string */
	private final String NEXT_PULSE_TIME = "Next Pulse Width";
	/** the pulse deviation label string */
	private final String PULSE_DEVIATION = "Pulse Deviation";
	/** the optimal pulse label string */
	private final String OPTIMAL = "Optimal Pulse Width";
	/** the reference pulse label string */
	private final String TASK_PULSE_TIME = "Task Pulse Width";
	/** the time ratio string */
	private final String ACTUAL_TIME_RATIO = Msg.getString("TimeWindow.actualTRHeader"); //$NON-NLS-1$
	/** the execution time unit */
	private final String MS = " ms";
	/** the Universal Mean Time abbreviation */
	private final String UMT = " (UMT) ";


	// Data members
	/** The time in ms when last updated. */
	private long lastUpdateTime = 0;
	
	private int solCache;
	
	private String northernSeasonTip ="";
	private String northernSeasonCache = "";
	private String southernSeasonTip = "";
	private String southernSeasonCache = "";

	/** Martian calendar panel. */
	private MarsCalendarDisplay calendarDisplay;

	/** The tick spinner */
	private JSpinner cpuSpinner;
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
	/** label for optimal Pulse Width. */
	private JLabel optimalPulseLabel;
	/** label for execution time. */
	private JLabel execTimeLabel;
	/** label for sleep time. */
	private JLabel sleepTimeLabel;
	/** label for next pulse width. */
	private JLabel nextPulseLabel;
	/** label for ref pulse width. */
	private JLabel taskPulseLabel;
	/** label for time compression. */
	private JLabel realTimeClockLabel;

	private JLabel monthLabel;

	private JLabel weeksolLabel;
	
	private OrbitInfo orbitInfo;

	
	/**
	 * Constructs a TimeWindow object.
	 *
	 * @param desktop the desktop pane
	 */
	public TimeWindow(final MainDesktopPane desktop) {
		// Use TimeWindow constructor
		super(NAME, TITLE, desktop);
	
		// Set window resizable to false.
		setResizable(true);
		
		// Initialize data members
		Simulation sim = desktop.getSimulation();
		MasterClock masterClock = sim.getMasterClock();
		MarsTime marsTime = masterClock.getMarsTime();
		orbitInfo = sim.getOrbitInfo();
		
		// Get content pane
		JPanel mainPane = new JPanel();//new BoxLayout(mainPane, BoxLayout.Y_AXIS));//new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create Martian time panel
		JPanel martianTimePane = new JPanel(new BorderLayout());
		mainPane.add(martianTimePane, BorderLayout.NORTH);
	
		// Create Martian time header label
		martianTimeLabel = new JLabel();
		martianTimeLabel.setHorizontalAlignment(JLabel.CENTER);
		martianTimeLabel.setVerticalAlignment(JLabel.CENTER);
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
		martianMonthPane.setBorder(StyleManager.createLabelBorder(Msg.getString("TimeWindow.martianMonth"))); //$NON-NLS-1$
		mainPane.add(martianMonthPane, BorderLayout.CENTER);
		
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
		JPanel calendarPane = new JPanel();//new BorderLayout());
		calendarPane.setLayout(new BoxLayout(calendarPane, BoxLayout.Y_AXIS));
//		calendarPane.setPreferredSize(new Dimension(-1, -1));
		calendarPane.setAlignmentX(SwingConstants.CENTER);
		calendarPane.setAlignmentY(SwingConstants.CENTER);
		martianMonthPane.add(calendarPane, BorderLayout.CENTER);

		JPanel innerCalendarPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		innerCalendarPane.setAlignmentX(SwingConstants.CENTER);
		innerCalendarPane.setAlignmentY(SwingConstants.CENTER);
	
		// Create Martian calendar display
		calendarDisplay = new MarsCalendarDisplay(marsTime, desktop);
//		calendarDisplay.setPreferredSize(new Dimension(-1, -1));
		innerCalendarPane.add(calendarDisplay);
		calendarPane.add(innerCalendarPane, BorderLayout.CENTER);


		JPanel seasonPane = new JPanel(new BorderLayout());
		mainPane.add(seasonPane, BorderLayout.SOUTH);

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
		
		
		JPanel southPane = new JPanel(new BorderLayout());
		seasonPane.add(southPane, BorderLayout.SOUTH);

		// Create the tick spinner
		createCPUSpinner(masterClock);
		JPanel tickPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		tickPane.add(new JLabel("Pulse Load :"));
		tickPane.setToolTipText("The smaller the load, the more refined each simulation step");
		tickPane.add(cpuSpinner);
		southPane.add(tickPane, BorderLayout.NORTH);
		
		// Create param panel
		AttributePanel paramPane = new AttributePanel(13);
		paramPane.setBorder(StyleManager.createLabelBorder(Msg.getString("TimeWindow.simParam"))); //$NON-NLS-1$

		southPane.add(paramPane, BorderLayout.CENTER);

		ticksPerSecLabel = paramPane.addTextField(Msg.getString("TimeWindow.ticksPerSecond"), "", //$NON-NLS-1$
				"The current ticks per sec");
		averageTPSLabel = paramPane.addTextField(AVERAGE_TPS, "", 
				"The average ticks per sec");
		execTimeLabel = paramPane.addTextField(EXEC, "", 
				"The last execution time of a tick");
		sleepTimeLabel = paramPane.addTextField(SLEEP_TIME, "", 
				"The sleep time [ms] of the last tick");
		taskPulseLabel = paramPane.addTextField(TASK_PULSE_TIME, "", 
				"How many millisol the task pulse width is");
		optimalPulseLabel = paramPane.addTextField(OPTIMAL, "", 
				"How many millisol the optimal pulse width is");
		pulseDeviationLabel = paramPane.addTextField(PULSE_DEVIATION, "", 
				"The percentage of deviation between the optimal pulse width and the next pulse width");
		nextPulseLabel = paramPane.addTextField(NEXT_PULSE_TIME, "", 
				"How many millisol the next pulse width will be");
		actualTRLabel = paramPane.addTextField(ACTUAL_TIME_RATIO, "",
				"Master clock's actual time ratio");
		desireTRLabel = paramPane.addTextField(DESIRE_TR, "",
				"Master clock's desire time ratio");
		realTimeClockLabel = paramPane.addTextField(Msg.getString("TimeWindow.rtc"), "", 
				"The amount of simulation time at the passing of each second of the real time"); //$NON-NLS-1$
		uptimeLabel = paramPane.addTextField(Msg.getString("TimeWindow.simUptime"), "", 
				"The amount of real time the simulation has been running"); //$NON-NLS-1$
	

		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		// Pack window
		pack();

		// Update the two time labels
		updateFastLabels(masterClock);
		updateDateLabels(masterClock);
		updateTimeLabels(masterClock);
		
		// Update season labels
		updateSeason();
	}

	/**
	 * Creates the CPU spinner.
	 * 
	 * @param masterClock
	 */
	private void createCPUSpinner(MasterClock masterClock) {

//		masterClock.computeReferencePulse();
		
		double value = masterClock.getPulseLoad();
	
		double min = Math.round(value / 5 * 100.0)/100.0;
		double max = Math.round(5 * value * 100.0)/100.0;
		
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, Math.round(value/20 * 100.0)/100.0);

		spinnerModel.setValue(value);
		
		cpuSpinner = new JSpinner(spinnerModel);	
		// 1. Get the editor component of your spinner:
		Component spinnerEditor = cpuSpinner.getEditor();
		// 2. Get the text field of your spinner's editor:
		JFormattedTextField jftf = ((JSpinner.DefaultEditor) spinnerEditor).getTextField();
		// 3. Set a default size to the text field:
		jftf.setColumns(4);
	
		jftf.setHorizontalAlignment(JTextField.LEFT);
		
		cpuSpinner.addChangeListener(e -> {
			float pulseChoice = spinnerModel.getNumber().floatValue();
			// Change the pulse load
			masterClock.setPulseLoad(pulseChoice);
		});
	}
	
	
	/**
	 * Updates various time labels.
	 * 
	 * @param masterClock
	 */
	private void updateTimeLabels(MasterClock masterClock) {
		float value = masterClock.getPulseLoad();
		SpinnerNumberModel spinnerModel = (SpinnerNumberModel)(cpuSpinner.getModel());
		
		float cpuValue = spinnerModel.getNumber().floatValue();
		if (cpuValue != value) {
		
			double min = Math.round(value / 5 * 100.0)/100.0;
			double max = Math.round(5 * value * 100.0)/100.0;
			
			spinnerModel.setValue(value);
			spinnerModel.setMinimum(min);
			spinnerModel.setMaximum(max);
			spinnerModel.setStepSize(Math.round(value/20 * 100.0)/100.0);
		}
		
		// Update execution time label
		short execTime = masterClock.getExecutionTime();
		execTimeLabel.setText(execTime + MS);

		// Update sleep time label
		float sleepTime = masterClock.getSleepTime();
		sleepTimeLabel.setText(sleepTime + MS);

		// Update pulse width label
		float nextPulse = masterClock.getNextPulseTime();
		float optPulse = masterClock.getOptPulseTime();
		float taskPulse = masterClock.geTaskPulseWidth();
		
		StringBuilder taskPulseText = new StringBuilder();
		taskPulseText.append(StyleManager.DECIMAL_PLACES4.format(taskPulse));
		taskPulseLabel.setText(taskPulseText.toString());

		StringBuilder pulseText = new StringBuilder();
		pulseText.append(StyleManager.DECIMAL_PLACES4.format(nextPulse));
		nextPulseLabel.setText(pulseText.toString());
		
		// Update pulse deviation label
		double percent = masterClock.getNextPulseDeviation() * 100;
		StringBuilder pulseDevText = new StringBuilder();
		pulseDevText.append(StyleManager.DECIMAL1_PERC.format(percent));
		pulseDeviationLabel.setText(pulseDevText.toString());
		
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
		
		// Update the calender
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
	public void update(ClockPulse pulse) {
		if (desktop.isToolWindowOpen(TimeWindow.NAME)) {
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
	
	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {
		super.destroy();

		calendarDisplay = null;
		cpuSpinner = null;
		martianTimeLabel = null;
		lonLabel = null;
		northernSeasonLabel = null;
		southernSeasonLabel = null;
		uptimeLabel = null;
		ticksPerSecLabel = null;
		actualTRLabel = null;
		desireTRLabel = null;
		pulseDeviationLabel = null;
		execTimeLabel = null;
		sleepTimeLabel = null;
		nextPulseLabel = null;
		taskPulseLabel = null;
		realTimeClockLabel = null;
		monthLabel = null;
		weeksolLabel = null;		
		orbitInfo = null;
	}
}
