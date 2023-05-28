/*
 * Mars Simulation Project
 * TimeWindow.java
 * @date 2023-04-01
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.time;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.environment.OrbitInfo;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.ClockUtils;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * The TimeWindow is a tool window that displays the current Martian and Earth
 * time.
 */
public class TimeWindow extends ToolWindow {

	// Milliseconds between updates to date fields
	private static final long DATE_UPDATE_PERIOD = 500L;

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Tool name. */
	public static final String NAME = Msg.getString("TimeWindow.title"); //$NON-NLS-1$
	public static final String ICON = "time";
	
	    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
                                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

	/** the execution time label string */
	private static final String EXEC = "Execution";
	/** the sleep time label string */
	private static final String SLEEP_TIME = "Sleep";
	/** the residual time label string */
	private static final String MARS_PULSE_TIME = "Pulse Width";
	/** the execution time unit */
	private static final String MILLISOLS = " millisols";
	/** the execution time unit */
	private static final String MS = " ms";
	/** the Universal Mean Time abbreviation */
	private static final String UMT = " (UMT) ";

	// Data members
	private String northernSeasonTip ="";
	private String northernSeasonCache = "";
	private String southernSeasonTip = "";
	private String southernSeasonCache = "";

	/** Martian calendar panel. */
	private MarsCalendarDisplay calendarDisplay;

	/** label for Martian time. */
	private JLabel martianTimeLabel;
	/** label for Earth time. */
	private JLabel earthTimeLabel;
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
	/** label for actual time ratio. */
	private JLabel actuallTRLabel;
	/** label for execution time. */
	private JLabel execTimeLabel;
	/** label for sleep time. */
	private JLabel sleepTimeLabel;
	/** label for mars simulation time. */
	private JLabel marsPulseLabel;
	/** label for time compression. */
	private JLabel timeCompressionLabel;

	private OrbitInfo orbitInfo;
	
	/** Arial font. */
	private final Font arialFont = new Font("Arial", Font.PLAIN, 14);


	private long lastDateUpdate = 0;
	
	/**
	 * Constructs a TimeWindow object
	 *
	 * @param desktop the desktop pane
	 */
	public TimeWindow(final MainDesktopPane desktop) {
		// Use TimeWindow constructor
		super(NAME, desktop);
	
		// Set window resizable to false.
		setResizable(false);
		
		// Initialize data members
		Simulation sim = desktop.getSimulation();
		MasterClock masterClock = sim.getMasterClock();
		MarsClock marsTime = masterClock.getMarsClock();
		orbitInfo = sim.getOrbitInfo();
		
		// Get content pane
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create Martian time panel
		JPanel martianTimePane = new JPanel(new BorderLayout());
		mainPane.add(martianTimePane, BorderLayout.NORTH);

		// Create Martian time header label
		martianTimeLabel = new JLabel();
		martianTimeLabel.setHorizontalAlignment(JLabel.CENTER);
		martianTimeLabel.setVerticalAlignment(JLabel.CENTER);
		martianTimeLabel.setFont(arialFont);
		martianTimeLabel.setForeground(new Color(135, 100, 39));
		martianTimeLabel.setText(marsTime.getDisplayDateTimeStamp() + UMT);
		martianTimeLabel.setToolTipText("Mars Timestamp in Universal Mean Time (UMT)");
		martianTimePane.add(martianTimeLabel, BorderLayout.SOUTH);
		martianTimePane.setBorder(StyleManager.createLabelBorder(Msg.getString("TimeWindow.martianTime")));

		// Create Martian calendar panel
		JPanel martianCalendarPane = new JPanel(new FlowLayout());
		mainPane.add(martianCalendarPane, BorderLayout.CENTER);

		// Create Martian calendar month panel
		JPanel calendarMonthPane = new JPanel(new BorderLayout());
		martianCalendarPane.add(calendarMonthPane);

		// Create Martian calendar display
		calendarDisplay = new MarsCalendarDisplay(marsTime, desktop);
		JPanel innerCalendarPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		calendarMonthPane.setPreferredSize(new Dimension(140, 125));
		calendarMonthPane.setMaximumSize(new Dimension(140, 125));
		calendarMonthPane.setMinimumSize(new Dimension(140, 125));
		innerCalendarPane.add(calendarDisplay);
		calendarMonthPane.add(innerCalendarPane, BorderLayout.NORTH);
		martianCalendarPane.setBorder(StyleManager.createLabelBorder("Current Month"));
		
		JPanel statPane = new JPanel(new BorderLayout());//FlowLayout(FlowLayout.LEFT));
		mainPane.add(statPane, BorderLayout.SOUTH);

		// Create Martian hemisphere panel
		AttributePanel hemiPane = new AttributePanel(3);
		statPane.add(hemiPane, BorderLayout.NORTH);		
		hemiPane.setBorder(StyleManager.createLabelBorder(Msg.getString("TimeWindow.martianSeasons")));

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
			
		// Create areocentric longitude header label
		lonLabel = hemiPane.addTextField("Areocentric Longitude (Ls) :", "", null);
		northernSeasonLabel = hemiPane.addTextField(Msg.getString("TimeWindow.northernHemisphere"),
													"", null);
		southernSeasonLabel = hemiPane.addTextField(Msg.getString("TimeWindow.southernHemisphere"),
													"", null);

		// Create Earth time panel
		JPanel earthTimePane = new JPanel(new BorderLayout());
		statPane.add(earthTimePane, BorderLayout.CENTER);

		// Create Earth time label
		earthTimeLabel = new JLabel();
		earthTimeLabel.setHorizontalAlignment(JLabel.CENTER);
		earthTimeLabel.setVerticalAlignment(JLabel.CENTER);
		earthTimeLabel.setFont(arialFont);
		earthTimeLabel.setForeground(new Color(0, 69, 165));
		earthTimeLabel.setText("");
		earthTimeLabel.setToolTipText("Earth Timestamp in Greenwich Mean Time (GMT)");
		earthTimePane.add(earthTimeLabel, BorderLayout.SOUTH);
		earthTimePane.setBorder(StyleManager.createLabelBorder(Msg.getString("TimeWindow.earthTime")));

		JPanel southPane = new JPanel(new BorderLayout());
		statPane.add(southPane, BorderLayout.SOUTH);

		// Create param panel
		AttributePanel paramPane = new AttributePanel(8);
		southPane.add(paramPane, BorderLayout.NORTH);

		ticksPerSecLabel = paramPane.addTextField(Msg.getString("TimeWindow.ticksPerSecond"), "", null);
		execTimeLabel = paramPane.addTextField(EXEC, "", null);
		sleepTimeLabel = paramPane.addTextField(SLEEP_TIME, "", null);
		marsPulseLabel = paramPane.addTextField(MARS_PULSE_TIME, "", null);
		actuallTRLabel = paramPane.addTextField(Msg.getString("TimeWindow.actualTRHeader"), "",
									"Master clock's actual time ratio");
		timeCompressionLabel = paramPane.addTextField("01s [real-time]", "", null);
		uptimeLabel = paramPane.addTextField(Msg.getString("TimeWindow.simUptime"), "", null);
	
		// Pack window
		pack();

		// Add 10 pixels to packed window width
		Dimension windowSize = getSize();
		setSize(new Dimension((int) windowSize.getWidth() + 10, (int) windowSize.getHeight()));

		// Update the two time labels
		updateRateLabels(masterClock);
		// Update season labels
		updateSeason();
	}

	/**
	 * Update various time labels
	 */
	private void updateRateLabels(MasterClock mc) {

		// Update execution time label
		long execTime = mc.getExecutionTime();
		execTimeLabel.setText(execTime + MS);

		// Update sleep time label
		long sleepTime = mc.getSleepTime();
		sleepTimeLabel.setText(sleepTime + MS);

		// Update pulse width label
		double pulseTime = mc.getMarsPulseTime();
		marsPulseLabel.setText(StyleManager.DECIMAL_PLACES3.format(pulseTime) + MILLISOLS);

		// Update actual TR label
		StringBuilder trText = new StringBuilder();
		trText.append((int)mc.getActualTR())
			  .append("x (target ")
			  .append(mc.getDesiredTR())
			  .append("x)");
		actuallTRLabel.setText(trText.toString());

		// Update time compression label
		timeCompressionLabel.setText(ClockUtils.getTimeString((int)mc.getActualTR()));
	}

	/**
	 * Set and update the season labels
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
	 * Get the text for the season label tooltip
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
	 * Update the calendar, the areocentric longitude and the time labels via ui pulse
	 * @param mc
	 */
	private void updateDateLabels(MasterClock mc) {
		// Update the calender
		calendarDisplay.update(mc.getMarsClock());
		// Update areocentric longitude
		lonLabel.setText(Math.round(orbitInfo.getSunAreoLongitude() * 1_000.0)/1_000.0 + "");	
		
		// Update season
		if (mc.getClockPulse().isNewSol()) {
			updateSeason();
		}
	}

	/**
	 * Updates date and time in Time Tool via clock pulse
	 */
	private void updateFastLabels(MasterClock mc) {
		String ts = mc.getMarsClock().getDisplayDateTimeStamp() + UMT;
		martianTimeLabel.setText(ts);

		ts = mc.getEarthTime().format(DATE_TIME_FORMATTER);
		earthTimeLabel.setText(ts);

		// Update average TPS label
		double ave = mc.getAveragePulsesPerSecond();
		StringBuilder tpText = new StringBuilder();
		tpText.append(StyleManager.DECIMAL_PLACES2.format(mc.getCurrentPulsesPerSecond()))
			  .append(" (ave. ")
			  .append(StyleManager.DECIMAL_PLACES2.format(ave))
			  .append(")");
		
		ticksPerSecLabel.setText(tpText.toString());

		uptimeLabel.setText(mc.getUpTimer().getUptime());
	}

	@Override
	public void update(ClockPulse pulse) {
		if (desktop.isToolWindowOpen(TimeWindow.NAME)) {
			MasterClock masterClock = pulse.getMasterClock();

			// update the fast labels
			updateFastLabels(masterClock);

			long currentTime = System.currentTimeMillis();
			if ((currentTime - lastDateUpdate) > DATE_UPDATE_PERIOD) {
				// update the slow labels
				updateDateLabels(masterClock);
				updateRateLabels(masterClock);
				lastDateUpdate = currentTime;
			}
		}
	}

}
