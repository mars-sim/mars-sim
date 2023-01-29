/*
 * Mars Simulation Project
 * TimeWindow.java
 * @date 2022-08-02
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.time;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.environment.OrbitInfo;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.ClockUtils;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.UpTimer;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;

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
	/** the execution time label string */
	public static final String EXEC = "Execution : ";
	/** the sleep time label string */
	public static final String SLEEP_TIME = "Sleep : ";
	/** the residual time label string */
	public static final String MARS_PULSE_TIME = "Pulse Width : ";
	/** the execution time unit */
	public static final String MILLISOLS = " millisols";
	/** the ave TPS label string */
	public static final String AVE_TPS = "Average TPS : ";
	/** the execution time unit */
	public static final String MS = " ms";
	/** the Universal Mean Time abbreviation */
	private static final String UMT = " (UMT) ";
	/** the Greenwich Mean Time abbreviation */
	private static final String GMT = " (GMT) ";

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
	/** label for Martian month. */
	private JLabel martianMonthHeaderLabel;
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
	/** label for preferred time ratio. */
	private JLabel preferredTRLabel;
	/** header label for execution time. */
	private JLabel execTimeHeader;
	/** header label for ave TPS. */
	private JLabel aveTPSHeader;
	/** header label for sleep time. */
	private JLabel sleepTimeHeader;
	/** header label for mars simulation time. */
	private JLabel marsPulseHeader;
	/** label for execution time. */
	private JLabel execTimeLabel;
	/** label for ave TPS. */
	private JLabel aveTPSLabel;
	/** label for sleep time. */
	private JLabel sleepTimeLabel;
	/** label for mars simulation time. */
	private JLabel marsPulseLabel;
	/** label for time compression. */
	private JLabel timeCompressionLabel;

	private final DecimalFormat formatter2 = new DecimalFormat(Msg.getString("TimeWindow.decimalFormat2")); //$NON-NLS-1$
	private final DecimalFormat formatter4 = new DecimalFormat(Msg.getString("TimeWindow.decimalFormat3")); //$NON-NLS-1$

	private OrbitInfo orbitInfo;
	
	/** Arial font. */
	private final Font arialFont = new Font("Arial", Font.PLAIN, 14);
	/** Sans serif font. */
	private static final Font sansSerifFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
	private static final Font monospacedFont = new Font(Font.MONOSPACED, Font.ITALIC, 12);
	private static final Font dialogFont = new Font(Font.DIALOG, Font.BOLD, 14);
	private static final Font serifFont = new Font(Font.SERIF, Font.ITALIC, 12);

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
		EarthClock earthTime = masterClock.getEarthClock();
		orbitInfo = sim.getOrbitInfo();
		
		UpTimer uptimer = masterClock.getUpTimer();

		// Get content pane
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create Martian time panel
		JPanel martianTimePane = new JPanel(new BorderLayout());
		mainPane.add(martianTimePane, BorderLayout.NORTH);

		// Create Martian time header label
		JLabel martianTimeHeaderLabel = new JLabel(Msg.getString("TimeWindow.martianTime"), JLabel.CENTER); //$NON-NLS-1$
		martianTimeHeaderLabel.setFont(dialogFont);

		martianTimeLabel = new JLabel();
		martianTimeLabel.setHorizontalAlignment(JLabel.CENTER);
		martianTimeLabel.setVerticalAlignment(JLabel.CENTER);
		martianTimeLabel.setFont(arialFont);
		martianTimeLabel.setForeground(new Color(135, 100, 39));
		martianTimeLabel.setText(marsTime.getDisplayDateTimeStamp() + UMT);
		martianTimeLabel.setToolTipText("Mars Timestamp in Universal Mean Time (UMT)");
		martianTimePane.add(martianTimeLabel, BorderLayout.SOUTH);
		martianTimePane.setBorder(BorderFactory.createTitledBorder(
				new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()),
				martianTimeHeaderLabel.getText(),
				TitledBorder.LEFT,
                TitledBorder.TOP,
                dialogFont));

		// Create Martian calendar panel
		JPanel martianCalendarPane = new JPanel(new FlowLayout());
		mainPane.add(martianCalendarPane, BorderLayout.CENTER);

		// Create Martian calendar month panel
		JPanel calendarMonthPane = new JPanel(new BorderLayout());
		martianCalendarPane.add(calendarMonthPane);

		// Create martian month label
		martianMonthHeaderLabel = new JLabel("Month of " + marsTime.getMonthName(), JLabel.CENTER);
		martianMonthHeaderLabel.setFont(dialogFont);
		// Create Martian calendar display
		calendarDisplay = new MarsCalendarDisplay(marsTime, desktop);
		JPanel innerCalendarPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		calendarMonthPane.setPreferredSize(new Dimension(140, 125));
		calendarMonthPane.setMaximumSize(new Dimension(140, 125));
		calendarMonthPane.setMinimumSize(new Dimension(140, 125));
		innerCalendarPane.add(calendarDisplay);
//		innerCalendarPane.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.ORANGE, Color.ORANGE));//new Color(210,105,30)));
		calendarMonthPane.add(innerCalendarPane, BorderLayout.NORTH);

		martianCalendarPane.setBorder(BorderFactory.createTitledBorder(
				MainDesktopPane.newEmptyBorder(),
//				new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()),
				martianMonthHeaderLabel.getText(),
				TitledBorder.LEFT,
                TitledBorder.TOP,
                dialogFont));
		
		JPanel statPane = new JPanel(new BorderLayout());//FlowLayout(FlowLayout.LEFT));
		mainPane.add(statPane, BorderLayout.SOUTH);

		// Create Martian hemisphere panel
		JPanel hemiPane = new JPanel(new SpringLayout());
		statPane.add(hemiPane, BorderLayout.NORTH);
		
		// Create Martian season label
		JLabel marsSeasonHeaderLabel = new JLabel(Msg.getString("TimeWindow.martianSeasons"), JLabel.CENTER); //$NON-NLS-1$
		marsSeasonHeaderLabel.setFont(dialogFont);
		hemiPane.setBorder(BorderFactory.createTitledBorder(
				new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()),
				marsSeasonHeaderLabel.getText(),
				TitledBorder.LEFT,
                TitledBorder.TOP,
                dialogFont));


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
		JLabel longHeader = new JLabel("Areocentric Longitude (Ls):",
				JLabel.RIGHT); //$NON-NLS-1$
		hemiPane.add(longHeader);

		// Create areocentric longitude label
		lonLabel = new JLabel(" ", 
				JLabel.LEFT);
		hemiPane.add(lonLabel);
		
		// Create Northern season header label
		JLabel northernSeasonHeader = new JLabel(Msg.getString("TimeWindow.northernHemisphere"),
				JLabel.RIGHT); //$NON-NLS-1$
		hemiPane.add(northernSeasonHeader);

		// Create Northern season label
		northernSeasonLabel = new JLabel(" ", 
				JLabel.LEFT);
		hemiPane.add(northernSeasonLabel);

		// Create Southern season header label
		JLabel southernSeasonHeader = new JLabel(Msg.getString("TimeWindow.southernHemisphere"),
				JLabel.RIGHT); //$NON-NLS-1$
		hemiPane.add(southernSeasonHeader);

		// Create Southern season label
		southernSeasonLabel = new JLabel(" ", 
				JLabel.LEFT);
		hemiPane.add(southernSeasonLabel);

		// Use spring panel layout.
		SpringUtilities.makeCompactGrid(hemiPane,
				3, 2, 		// rows, cols
				5, 5,	// initX, initY
				7, 3);		// xPad, yPad

		// Create Earth time panel
		JPanel earthTimePane = new JPanel(new BorderLayout());
		statPane.add(earthTimePane, BorderLayout.CENTER);

		// Create Earth time header label
		JLabel earthTimeHeaderLabel = new JLabel(Msg.getString("TimeWindow.earthTime"), JLabel.CENTER); //$NON-NLS-1$
		earthTimeHeaderLabel.setFont(dialogFont);

		// Create Earth time label
		earthTimeLabel = new JLabel();
		earthTimeLabel.setHorizontalAlignment(JLabel.CENTER);
		earthTimeLabel.setVerticalAlignment(JLabel.CENTER);
		earthTimeLabel.setFont(arialFont);
		earthTimeLabel.setForeground(new Color(0, 69, 165));
		earthTimeLabel.setText(earthTime.getTimeStampF4() + GMT);
		earthTimeLabel.setToolTipText("Earth Timestamp in Greenwich Mean Time (GMT)");
		earthTimePane.add(earthTimeLabel, BorderLayout.SOUTH);
		earthTimePane.setBorder(BorderFactory.createTitledBorder(
				new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()),
				earthTimeHeaderLabel.getText(),
				TitledBorder.LEFT,
                TitledBorder.TOP,
                dialogFont));

		JPanel southPane = new JPanel(new BorderLayout());
		statPane.add(southPane, BorderLayout.SOUTH);

		// Create param panel
		JPanel paramPane = new JPanel(new SpringLayout());
//		paramPane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		southPane.add(paramPane, BorderLayout.NORTH);

		JLabel TPSHeaderLabel = new JLabel(Msg.getString("TimeWindow.ticksPerSecond"),
				JLabel.RIGHT); //$NON-NLS-1$
		TPSHeaderLabel.setFont(sansSerifFont);
		TPSHeaderLabel.setForeground(Color.MAGENTA.darker());
		
		String TicksPerSec = formatter2.format(masterClock.getCurrentPulsesPerSecond());
		ticksPerSecLabel = new JLabel(TicksPerSec, JLabel.LEFT);
		ticksPerSecLabel.setFont(sansSerifFont);
		ticksPerSecLabel.setForeground(Color.red.darker());
		
		aveTPSHeader = new JLabel(AVE_TPS, JLabel.RIGHT);
		aveTPSLabel = new JLabel(0 + "", JLabel.LEFT);

		// Create execution time label
		execTimeHeader = new JLabel(EXEC, JLabel.RIGHT);
		long execTime = masterClock.getExecutionTime();
		execTimeLabel = new JLabel(execTime + MS, JLabel.LEFT);

		// Create sleep time label
		sleepTimeHeader = new JLabel(SLEEP_TIME, JLabel.RIGHT);
		long sleepTime = masterClock.getSleepTime();
		sleepTimeLabel = new JLabel(sleepTime + MS, JLabel.LEFT);

		// Create pulse time label
		marsPulseHeader = new JLabel(MARS_PULSE_TIME, JLabel.RIGHT);
		double pulseTime = masterClock.getMarsPulseTime();
		marsPulseLabel = new JLabel(formatter4.format(pulseTime) + MILLISOLS, JLabel.LEFT);

		// Create the target time ratio label
		JLabel prefTRHeader = new JLabel(Msg.getString("TimeWindow.prefTRHeader"), JLabel.RIGHT); //$NON-NLS-1$
		prefTRHeader.setToolTipText("User-preferred target time ratio");
		int prefTR = masterClock.getDesiredTR();
		preferredTRLabel = new JLabel(prefTR + "", JLabel.LEFT); //$NON-NLS-1$
		preferredTRLabel.setFont(monospacedFont);

		// Create the actual time ratio label
		JLabel actualTRHeader = new JLabel(Msg.getString("TimeWindow.actualTRHeader"), JLabel.RIGHT); //$NON-NLS-1$
		actualTRHeader.setToolTipText("Master clock's actual time ratio");
		double actualTR = masterClock.getActualTR();
		actuallTRLabel = new JLabel(Math.round(actualTR*10.0)/10.0 + "", JLabel.LEFT); //$NON-NLS-1$
		actuallTRLabel.setFont(serifFont);

		paramPane.add(TPSHeaderLabel);
		paramPane.add(ticksPerSecLabel);
		paramPane.add(aveTPSHeader);
		paramPane.add(aveTPSLabel);
		paramPane.add(execTimeHeader);
		paramPane.add(execTimeLabel);

		paramPane.add(sleepTimeHeader);
		paramPane.add(sleepTimeLabel);
		paramPane.add(marsPulseHeader);
		paramPane.add(marsPulseLabel);

		paramPane.add(prefTRHeader);
		paramPane.add(preferredTRLabel);
		paramPane.add(actualTRHeader);
		paramPane.add(actuallTRLabel);

		// Use spring panel layout.
		SpringUtilities.makeCompactGrid(paramPane,
				7, 2, //rows, cols
				43, 2,        //initX, initY
				7, 3);       //xPad, yPad

		// Create the compressionPane pane
		JPanel compressionPane = new JPanel(new BorderLayout());
		southPane.add(compressionPane, BorderLayout.CENTER);

		// Create the time compression header label
		JLabel compressionHeaderLabel = new JLabel(Msg.getString("TimeWindow.timeCompression"), 
				JLabel.CENTER); //$NON-NLS-1$
		compressionHeaderLabel.setFont(dialogFont);
		
		// Create the time compression label
		timeCompressionLabel = new JLabel();
		compressionPane.add(timeCompressionLabel);
		compressionPane.setBorder(BorderFactory.createTitledBorder(
				new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()),
				compressionHeaderLabel.getText(),
				TitledBorder.LEFT,
                TitledBorder.TOP,
                dialogFont));

		// Create the compressionPane pane
		JPanel upTimePane = new JPanel(new BorderLayout());
		southPane.add(upTimePane, BorderLayout.SOUTH);

		JLabel uptimeHeaderLabel = new JLabel(Msg.getString("TimeWindow.simUptime"), JLabel.RIGHT); //$NON-NLS-1$
 		upTimePane.setBorder(BorderFactory.createTitledBorder(
				new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()),
				uptimeHeaderLabel.getText(),
				TitledBorder.LEFT,
                TitledBorder.TOP,
                dialogFont));

		// Create uptime label
		uptimeLabel = new JLabel(uptimer.getUptime(), JLabel.CENTER);
		upTimePane.add(uptimeLabel);
		
		// addMouseListener(new MouseInputAdapter() {
		// 	@Override
		// 	public void mouseClicked(MouseEvent e) {
		// 		super.mouseClicked(e);
				
		// 		// Update the two time labels
		// 		updateRateLabels();
		// 	}
		// });

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

		// Update average TPS label
		double ave = mc.getAveragePulsesPerSecond();
		aveTPSLabel.setText(formatter2.format(ave));

		// Update execution time label
		long execTime = mc.getExecutionTime();
		execTimeLabel.setText(execTime + MS);

		// Update sleep time label
		long sleepTime = mc.getSleepTime();
		sleepTimeLabel.setText(sleepTime + MS);

		// Update pulse width label
		double pulseTime = mc.getMarsPulseTime();
		marsPulseLabel.setText(formatter4.format(pulseTime) + MILLISOLS);

		// Update Preferred TR label
		int prefTR = mc.getDesiredTR();
		preferredTRLabel.setText(prefTR + "x");

		// Update actual TR label
		double actualTR = mc.getActualTR();
		actuallTRLabel.setText((int)actualTR + "x");

		// Update time compression label
		timeCompressionLabel.setText(ClockUtils.getTimeString((int)actualTR));
	}

	/**
	 * Set and update the season labels
	 */
	private void updateSeason() {
		String northernSeason = orbitInfo.getSeason(OrbitInfo.NORTHERN_HEMISPHERE);
		String southernSeason = orbitInfo.getSeason(OrbitInfo.SOUTHERN_HEMISPHERE);
	
		if (!northernSeasonCache.equals(northernSeason)) {
			northernSeasonCache = northernSeason;

			if (orbitInfo.getSeason(OrbitInfo.NORTHERN_HEMISPHERE) != null && northernSeasonLabel != null) {
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
		lonLabel.setText(Math.round(orbitInfo.getL_s() * 1000.0)/1000.0 + "");	
		
		// Update season
		if (mc.getClockPulse().isNewSol()) {
			String mn = mc.getMarsClock().getMonthName();
			martianMonthHeaderLabel.setText("Month of " + mn);
			updateSeason();
		}
	}

	/**
	 * Updates date and time in Time Tool via clock pulse
	 */
	private void updateFastLabels(MasterClock mc) {
		String ts = mc.getMarsClock().getDisplayDateTimeStamp() + UMT;
		martianTimeLabel.setText(ts);

		ts = mc.getEarthClock().getTimeStampF4() + GMT;
		earthTimeLabel.setText(ts);
		ticksPerSecLabel.setText(formatter2.format(mc.getCurrentPulsesPerSecond()));

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
