/*
 * Mars Simulation Project
 * TimeTool.java
 * @date 2025-08-05
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.time;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.environment.OrbitInfo;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MarsTimeFormat;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The TimeWindow is a tool window that displays the current Martian date and time.
 */
public class TimeTool extends ContentPanel {

	// Milliseconds between updates to date fields
	private static final long DATE_UPDATE_PERIOD = 300L;

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 320;
	private static final int HEIGHT = 360;
	
	
	public static final String NAME = "time";
	public static final String ICON = "time";
	public static final String TITLE = Msg.getString("TimeWindow.title"); //$NON-NLS-1$

	// Label Strings
	private static final String SPRING = "Spring";
	private static final String SPRING_STRING = Msg.getString("TimeWindow.season.spring");
	private static final String SUMMER = "Summer";
	private static final String SUMMER_STRING = Msg.getString("TimeWindow.season.summer");
	private static final String AUTUMN = "Autumn";
	private static final String AUTUMN_STRING = Msg.getString("TimeWindow.season.autumn");
	private static final String WINTER = "Winter";
	private static final String WINTER_STRING = Msg.getString("TimeWindow.season.winter");
	
	private static final String DEG = Msg.getString("direction.degreeSign");

	private static final String WIKI_URL = Msg.getString("TimeWindow.calendar.url"); //$NON-NLS-1$

	private static final DateTimeFormatter EARTH_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM,
															FormatStyle.SHORT);


	// Data members
	/** The time in ms when last updated. */
	private long lastUpdateTime = 0;
	
	private int solCache;
	
	private String northernSeasonCache = "";
	private String southernSeasonCache = "";

	/** Martian calendar panel. */
	private MarsCalendarDisplay calendarDisplay;

	private JLabel martianTimeLabel;
	private JLabel earthTimeLabel;
	private JLabel uptimeLabel;

	/** The label for areocentric longitude. */
	private JLabel lonLabel;
	/** The label for Northern hemisphere season. */
	private JLabel northernSeasonLabel;
	/** The label for Southern hemisphere season. */
	private JLabel southernSeasonLabel;
	/** The label for the month. */
	private JLabel monthLabel;
	/** The label for the week. */
	private JLabel weeksolLabel;
	/** The orbit info instance. */
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

		setLayout(new BorderLayout());
		
		// Set up main pane
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(SwingHelper.createEtchedBorder());

		var size = new Dimension(WIDTH, HEIGHT);
		mainPane.setPreferredSize(size);
		mainPane.setMinimumSize(size);

		add(mainPane, BorderLayout.CENTER);
		
		// Set up martian pane
		JPanel martianPane = new JPanel(new BorderLayout());
		mainPane.add(martianPane, BorderLayout.NORTH);
		
		// Create Martian time panel
		JPanel martianTimePane = new JPanel(new BorderLayout());
		martianPane.add(martianTimePane, BorderLayout.NORTH);
	
		// Create Martian time header label
		var timePanel = new AttributePanel();
		martianTimeLabel = timePanel.addTextField(Msg.getString("TimeWindow.martianTime"), null, "Mars Timestamp in Mars Central Time (MCT 0)");
		earthTimeLabel = timePanel.addTextField("Earth Time", null, "Time back on Earth");
		uptimeLabel = timePanel.addTextField(Msg.getString("TimeWindow.simUptime"), null, "Simulation Uptime");
		martianTimePane.add(timePanel, BorderLayout.CENTER);
		martianTimePane.setBorder(SwingHelper.createLabelBorder("Times")); //$NON-NLS-1$

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
		martianMonthPane.setBorder(SwingHelper.createLabelBorder(Msg.getString("TimeWindow.martianMonth"))); //$NON-NLS-1$
		martianPane.add(martianMonthPane, BorderLayout.CENTER);
		
		// Create Martian calendar label panel
		AttributePanel labelPane = new AttributePanel(2);
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
		AttributePanel hemiPane = new AttributePanel();
		seasonPane.add(hemiPane, BorderLayout.NORTH);		
		hemiPane.setBorder(SwingHelper.createLabelBorder(Msg.getString("TimeWindow.martianSeasons"))); //$NON-NLS-1$

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
		lonLabel = hemiPane.addTextField(Msg.getString("TimeWindow.areocentricLon"), "", 
				"The Areocentric Longitude [0 to 360 degrees] of Mars with respect to the Sun");

		
		// Update the two time labels
		updateTime(masterClock);
		updateDateLabels(masterClock);
		
		// Update season labels
		updateSeason();
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
		if (hemi.contains(SPRING))
			return SPRING_STRING;
		else if (hemi.contains(SUMMER))
			return SUMMER_STRING;
		else if (hemi.contains(AUTUMN))
			return AUTUMN_STRING;
		else if (hemi.contains(WINTER))
			return WINTER_STRING;
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
		lonLabel.setText(Math.round(orbitInfo.getSunAreoLongitude() * 10_000.0)/10_000.0 + DEG); //"\u00B0");	
		
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
	private void updateTime(MasterClock mc) {
		// Update mars time
		MarsTime mTime = mc.getMarsTime();
		martianTimeLabel.setText(MarsTimeFormat.getSolOfWeekString(mTime.getSolOfWeek())
						+ " " + mTime.getTruncatedDateTimeStamp());
		earthTimeLabel.setText(mc.getEarthTime().format(EARTH_FORMAT));

		uptimeLabel.setText(mc.getUpTimer().getUptime());
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		MasterClock masterClock = pulse.getMasterClock();

		// update the fast labels
		updateTime(masterClock);

		long currentTime = System.currentTimeMillis();
		if ((currentTime - lastUpdateTime) > DATE_UPDATE_PERIOD) {
			// update the slow labels
			updateDateLabels(masterClock);
			lastUpdateTime = currentTime;
		}
	}
}
