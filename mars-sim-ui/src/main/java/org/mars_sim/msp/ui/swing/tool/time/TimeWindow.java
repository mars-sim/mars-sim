/**
 * Mars Simulation Project
 * TimeWindow.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.time;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.environment.OrbitInfo;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.ClockUtils;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.UpTimer;
//import org.mars_sim.msp.ui.swing.JSliderMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import com.alee.extended.label.WebStyledLabel;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The TimeWindow is a tool window that displays the current Martian and Earth
 * time.
 */
public class TimeWindow extends ToolWindow implements ClockListener {

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

	/** Uptime Timer. */
	private UpTimer uptimer;
	/** Martian calendar panel. */
	private MarsCalendarDisplay calendarDisplay;

	/** label for Martian time. */
	private WebStyledLabel martianTimeLabel;
	/** label for Earth time. */
	private WebStyledLabel earthTimeLabel;
	/** label for Martian month. */
	private WebLabel martianMonthHeaderLabel;
	/** label for areocentric longitude. */
	private WebLabel longLabel;
	/** label for Northern hemisphere season. */
	private WebLabel northernSeasonLabel;
	/** label for Southern hemisphere season. */
	private WebLabel southernSeasonLabel;
	/** label for uptimer. */
	private WebLabel uptimeLabel;
	/** label for pulses per second label. */
	private WebLabel ticksPerSecLabel;
	/** label for actual time ratio. */
	private WebLabel actuallTRLabel;
	/** label for preferred time ratio. */
	private WebLabel preferredTRLabel;
	/** header label for execution time. */
	private WebLabel execTimeHeader;
	/** header label for ave TPS. */
	private WebLabel aveTPSHeader;
	/** header label for sleep time. */
	private WebLabel sleepTimeHeader;
	/** header label for mars simulation time. */
	private WebLabel marsPulseHeader;
	/** label for execution time. */
	private WebLabel execTimeLabel;
	/** label for ave TPS. */
	private WebLabel aveTPSLabel;
	/** label for sleep time. */
	private WebLabel sleepTimeLabel;
	/** label for mars simulation time. */
	private WebLabel marsPulseLabel;
	/** label for time compression. */
	private WebLabel timeCompressionLabel;

	private final DecimalFormat formatter2 = new DecimalFormat(Msg.getString("TimeWindow.decimalFormat2")); //$NON-NLS-1$
	private final DecimalFormat formatter3 = new DecimalFormat(Msg.getString("TimeWindow.decimalFormat3")); //$NON-NLS-1$

	/** Master Clock. */
	private MasterClock masterClock;
	/** Martian Clock. */
	private MarsClock marsTime;
	/** Earth Clock. */
	private EarthClock earthTime;
	/** OrbitInfo instance. */
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
		masterClock = sim.getMasterClock();
		marsTime = masterClock.getMarsClock();
		earthTime = masterClock.getEarthClock();
		orbitInfo = sim.getMars().getOrbitInfo();
		
		uptimer = masterClock.getUpTimer();

		// Add this class to the master clock's listener at 5 per second
		// BUT there are 2 updates at different speeds within the clockPulse method
		masterClock.addClockListener(this, 200L);

		// Get content pane
		WebPanel mainPane = new WebPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create Martian time panel
		WebPanel martianTimePane = new WebPanel(new BorderLayout());
		mainPane.add(martianTimePane, BorderLayout.NORTH);

		// Create Martian time header label
		WebLabel martianTimeHeaderLabel = new WebLabel(Msg.getString("TimeWindow.martianTime"), WebLabel.CENTER); //$NON-NLS-1$
		martianTimeHeaderLabel.setFont(dialogFont);

		martianTimeLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		martianTimeLabel.setHorizontalAlignment(JLabel.CENTER);
		martianTimeLabel.setVerticalAlignment(JLabel.CENTER);
		martianTimeLabel.setFont(arialFont);
		martianTimeLabel.setForeground(new Color(135, 100, 39));
		martianTimeLabel.setText(marsTime.getDisplayDateTimeStamp() + UMT);
		TooltipManager.setTooltip(martianTimeLabel, "Mars Timestamp in Universal Mean Time (UMT)", TooltipWay.down);
		martianTimePane.add(martianTimeLabel, BorderLayout.SOUTH);
		martianTimePane.setBorder(BorderFactory.createTitledBorder(
				new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()),
				martianTimeHeaderLabel.getText(),
				TitledBorder.LEFT,
                TitledBorder.TOP,
                dialogFont));

		// Create Martian calendar panel
		WebPanel martianCalendarPane = new WebPanel(new FlowLayout());
		mainPane.add(martianCalendarPane, BorderLayout.CENTER);

		// Create Martian calendar month panel
		WebPanel calendarMonthPane = new WebPanel(new BorderLayout());
		martianCalendarPane.add(calendarMonthPane);

		// Create martian month label
		martianMonthHeaderLabel = new WebLabel("Month of " + marsTime.getMonthName(), WebLabel.CENTER);
		martianMonthHeaderLabel.setFont(dialogFont);
		// Create Martian calendar display
		calendarDisplay = new MarsCalendarDisplay(marsTime, desktop);
		WebPanel innerCalendarPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
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
		
		WebPanel statPane = new WebPanel(new BorderLayout());//FlowLayout(FlowLayout.LEFT));
		mainPane.add(statPane, BorderLayout.SOUTH);

		// Create Martian hemisphere panel
		WebPanel hemiPane = new WebPanel(new SpringLayout());
		statPane.add(hemiPane, BorderLayout.NORTH);
		
		// Create Martian season label
		WebLabel marsSeasonHeaderLabel = new WebLabel(Msg.getString("TimeWindow.martianSeasons"), WebLabel.CENTER); //$NON-NLS-1$
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

		hemiPane.setToolTip(str);

//		Note :
//		&#8201; Thin tab space
//		&#8194; En tab space
//		&#8195; Em tab space
			
		// Create areocentric longitude header label
		WebLabel longHeader = new WebLabel("Areocentric Longitude :",
				WebLabel.RIGHT); //$NON-NLS-1$
		hemiPane.add(longHeader);

		// Create areocentric longitude label
		longLabel = new WebLabel(" ", 
				WebLabel.LEFT);
		hemiPane.add(longLabel);
		
		// Create Northern season header label
		WebLabel northernSeasonHeader = new WebLabel(Msg.getString("TimeWindow.northernHemisphere"),
				WebLabel.RIGHT); //$NON-NLS-1$
		hemiPane.add(northernSeasonHeader);

		// Create Northern season label
		northernSeasonLabel = new WebLabel(" ", 
				WebLabel.LEFT);
		hemiPane.add(northernSeasonLabel);

		// Create Southern season header label
		WebLabel southernSeasonHeader = new WebLabel(Msg.getString("TimeWindow.southernHemisphere"),
				WebLabel.RIGHT); //$NON-NLS-1$
		hemiPane.add(southernSeasonHeader);

		// Create Southern season label
		southernSeasonLabel = new WebLabel(" ", 
				WebLabel.LEFT);
		hemiPane.add(southernSeasonLabel);

		// Use spring panel layout.
		SpringUtilities.makeCompactGrid(hemiPane,
				3, 2, 		// rows, cols
				25, 5,	// initX, initY
				7, 3);		// xPad, yPad

		// Create Earth time panel
		WebPanel earthTimePane = new WebPanel(new BorderLayout());
		statPane.add(earthTimePane, BorderLayout.CENTER);

		// Create Earth time header label
		WebLabel earthTimeHeaderLabel = new WebLabel(Msg.getString("TimeWindow.earthTime"), WebLabel.CENTER); //$NON-NLS-1$
		earthTimeHeaderLabel.setFont(dialogFont);

		// Create Earth time label
		earthTimeLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		earthTimeLabel.setHorizontalAlignment(JLabel.CENTER);
		earthTimeLabel.setVerticalAlignment(JLabel.CENTER);
		earthTimeLabel.setFont(arialFont);
		earthTimeLabel.setForeground(new Color(0, 69, 165));
		earthTimeLabel.setText(earthTime.getTimeStampF4() + GMT);
		TooltipManager.setTooltip(earthTimeLabel, "Earth Timestamp in Greenwich Mean Time (GMT)", TooltipWay.down);
		earthTimePane.add(earthTimeLabel, BorderLayout.SOUTH);
		earthTimePane.setBorder(BorderFactory.createTitledBorder(
				new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()),
				earthTimeHeaderLabel.getText(),
				TitledBorder.LEFT,
                TitledBorder.TOP,
                dialogFont));

		WebPanel southPane = new WebPanel(new BorderLayout());
		statPane.add(southPane, BorderLayout.SOUTH);

		// Create param panel
		WebPanel paramPane = new WebPanel(new SpringLayout());
//		paramPane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		southPane.add(paramPane, BorderLayout.NORTH);

		WebLabel TPSHeaderLabel = new WebLabel(Msg.getString("TimeWindow.ticksPerSecond"),
				WebLabel.RIGHT); //$NON-NLS-1$
		TPSHeaderLabel.setFont(sansSerifFont);
		TPSHeaderLabel.setForeground(Color.MAGENTA.darker());
		
		String TicksPerSec = formatter2.format(masterClock.getCurrentPulsesPerSecond());
		ticksPerSecLabel = new WebLabel(TicksPerSec, WebLabel.LEFT);
		ticksPerSecLabel.setFont(sansSerifFont);
		ticksPerSecLabel.setForeground(Color.red.darker());
		
		aveTPSHeader = new WebLabel(AVE_TPS, WebLabel.RIGHT);
		aveTPSLabel = new WebLabel(0 + "", WebLabel.LEFT);

		// Create execution time label
		execTimeHeader = new WebLabel(EXEC, WebLabel.RIGHT);
		long execTime = masterClock.getExecutionTime();
		execTimeLabel = new WebLabel(execTime + MS, WebLabel.LEFT);

		// Create sleep time label
		sleepTimeHeader = new WebLabel(SLEEP_TIME, WebLabel.RIGHT);
		long sleepTime = masterClock.getSleepTime();
		sleepTimeLabel = new WebLabel(sleepTime + MS, WebLabel.LEFT);

		// Create pulse time label
		marsPulseHeader = new WebLabel(MARS_PULSE_TIME, WebLabel.RIGHT);
		double pulseTime = masterClock.getMarsPulseTime();
		marsPulseLabel = new WebLabel(formatter3.format(Math.round(pulseTime * 1000.0)/1000.0) + MILLISOLS, WebLabel.LEFT);

		// Create the target time ratio label
		WebLabel prefTRHeader = new WebLabel(Msg.getString("TimeWindow.prefTRHeader"), WebLabel.RIGHT); //$NON-NLS-1$
		TooltipManager.setTooltip(prefTRHeader, "User-preferred target time ratio", TooltipWay.down);
		int prefTR = masterClock.getDesiredTR();
		preferredTRLabel = new WebLabel(prefTR + "", WebLabel.LEFT); //$NON-NLS-1$
		preferredTRLabel.setFont(monospacedFont);

		// Create the actual time ratio label
		WebLabel actualTRHeader = new WebLabel(Msg.getString("TimeWindow.actualTRHeader"), WebLabel.RIGHT); //$NON-NLS-1$
		TooltipManager.setTooltip(actualTRHeader, "Master clock's actual time ratio", TooltipWay.down);
		double actualTR = masterClock.getActualTR();
		actuallTRLabel = new WebLabel(Math.round(actualTR*10.0)/10.0 + "", WebLabel.LEFT); //$NON-NLS-1$
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
		WebPanel compressionPane = new WebPanel(new BorderLayout());
		southPane.add(compressionPane, BorderLayout.CENTER);

		// Create the time compression header label
		WebLabel compressionHeaderLabel = new WebLabel(Msg.getString("TimeWindow.timeCompression"), 
				WebLabel.CENTER); //$NON-NLS-1$
		compressionHeaderLabel.setFont(dialogFont);
		
		// Create the time compression label
		timeCompressionLabel = new WebLabel(WebLabel.CENTER);
		compressionPane.add(timeCompressionLabel);
		compressionPane.setBorder(BorderFactory.createTitledBorder(
				new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()),
				compressionHeaderLabel.getText(),
				TitledBorder.LEFT,
                TitledBorder.TOP,
                dialogFont));

		// Create the compressionPane pane
		WebPanel upTimePane = new WebPanel(new BorderLayout());
		southPane.add(upTimePane, BorderLayout.SOUTH);

		WebLabel uptimeHeaderLabel = new WebLabel(Msg.getString("TimeWindow.simUptime"), WebLabel.RIGHT); //$NON-NLS-1$
 		upTimePane.setBorder(BorderFactory.createTitledBorder(
				new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()),
				uptimeHeaderLabel.getText(),
				TitledBorder.LEFT,
                TitledBorder.TOP,
                dialogFont));

		// Create uptime label
		uptimeLabel = new WebLabel(uptimer.getUptime(), WebLabel.CENTER);
		upTimePane.add(uptimeLabel);
		
		addMouseListener(new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				
				// Update the two time labels
				updateRateLabels();
			}
		});

		// Pack window
		pack();

		// Add 10 pixels to packed window width
		Dimension windowSize = getSize();
		setSize(new Dimension((int) windowSize.getWidth() + 10, (int) windowSize.getHeight()));

		// Update the two time labels
		updateRateLabels();
		// Update season labels
		updateSeason();
	}

	/**
	 * Update various time labels
	 */
	private void updateRateLabels() {

		// Update average TPS label
		double ave = masterClock.getAveragePulsesPerSecond();
		aveTPSLabel.setText(formatter2.format(ave));

		// Update execution time label
		long execTime = masterClock.getExecutionTime();
		execTimeLabel.setText(execTime + MS);

		// Update sleep time label
		long sleepTime = masterClock.getSleepTime();
		sleepTimeLabel.setText(sleepTime + MS);

		// Update pulse width label
		double pulseTime = masterClock.getMarsPulseTime();
		marsPulseLabel.setText(formatter3.format(Math.round(pulseTime * 1000.0)/1000.0) + MILLISOLS);

		// Update Preferred TR label
		int prefTR = masterClock.getDesiredTR();
		preferredTRLabel.setText(prefTR + "x");

		// Update actual TR label
		double actualTR = masterClock.getActualTR();
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
			TooltipManager.setTooltip(northernSeasonLabel, northernSeasonTip, TooltipWay.down);
		}

		if (!southernSeasonCache.equals(southernSeason)) {
			southernSeasonCache = southernSeason;

			if (orbitInfo.getSeason(OrbitInfo.SOUTHERN_HEMISPHERE) != null) {
				southernSeasonLabel.setText(southernSeason);
			}

			southernSeasonTip = getSeasonTip(southernSeason);
			TooltipManager.setTooltip(southernSeasonLabel, southernSeasonTip, TooltipWay.down);
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
	 */
	private void updateDateLabels() {
		// Update the calender
		calendarDisplay.update();
		// Update areocentric longitude
		longLabel.setText(Math.round(orbitInfo.getL_s() * 1000.0)/1000.0 + "");	
		
		// Update season
		if (masterClock.getClockPulse().isNewSol()) {
			String mn = marsTime.getMonthName();
			if (mn != null)
				SwingUtilities.invokeLater(() -> martianMonthHeaderLabel.setText("Month of " + mn));
			updateSeason();
		}
	}

	/**
	 * Updates date and time in Time Tool via clock pulse
	 */
	private void updateFastLabels() {
		if (marsTime != null && martianTimeLabel != null) {
			String ts = marsTime.getDisplayDateTimeStamp() + UMT;
			SwingUtilities.invokeLater(() -> martianTimeLabel.setText(ts));
		}

		if (earthTime != null) {
			String ts = earthTime.getTimeStampF4() + GMT;
			SwingUtilities.invokeLater(() -> earthTimeLabel.setText(ts));
		}

		if (masterClock != null) {
			SwingUtilities.invokeLater(() -> ticksPerSecLabel.setText(formatter2.format(masterClock.getCurrentPulsesPerSecond())));
		}

		if (uptimer != null) {
			SwingUtilities.invokeLater(() -> uptimeLabel.setText(uptimer.getUptime()));
		}
	}

	/**
	 * Change the pause status. Called by Masterclock's firePauseChange() since
	 * TimeWindow is on clocklistener.
	 *
	 * @param isPaused true if set to pause
	 * @param showPane true if the pane will show up
	 */
	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		if (!isPaused && desktop.isToolWindowOpen(TimeWindow.NAME)) {
			// update the slow labels
			updateDateLabels();
		}
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
		if (desktop.isToolWindowOpen(TimeWindow.NAME)) {
			// update the fast labels
			updateFastLabels();

			long currentTime = System.currentTimeMillis();
			if ((currentTime - lastDateUpdate) > DATE_UPDATE_PERIOD) {
				// update the slow labels
				updateDateLabels();
				updateRateLabels();
				lastDateUpdate = currentTime;
			}
		}
	}

	/**
	 * Prepare tool window for deletion.
	 */
	@Override
	public void destroy() {
		if (masterClock != null) {
			masterClock.removeClockListener(this);
		}
		masterClock = null;
		marsTime = null;
		earthTime = null;
		uptimer = null;
	}
}
