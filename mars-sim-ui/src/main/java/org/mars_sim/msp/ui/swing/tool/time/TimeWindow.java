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
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.ClockUtils;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.UpTimer;
//import org.mars_sim.msp.ui.swing.JSliderMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
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

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TimeWindow.class.getName());

	/** Tool name. */
	public static final String NAME = Msg.getString("TimeWindow.title"); //$NON-NLS-1$
	/** the execution time label string */
	public static final String EXEC = "Execution : ";
	/** the sleep time label string */
	public static final String SLEEP_TIME = "Sleep : ";
	/** the residual time label string */
	public static final String MARS_PULSE_TIME = "Simulated Pulse : ";
	/** the execution time unit */
	public static final String MSOL = " millisol";
	/** the ave TPS label string */
	public static final String AVE_TPS = "Average TPS : ";
	/** the execution time unit */
	public static final String MS = " ms";
	/** the real second label string */	
	public static final String ONE_REAL_SEC = "1 Real Sec = ";
	/** the upper limit of the slider bar. */
	public static final int MAX = MasterClock.MAX_SPEED;
	/** the lower limit of the slider bar. */
	public static final int MIN = 0;

	// Data members
	private int solElapsedCache = 0;

	private String northernSeasonTip ="";
	private String northernSeasonCache = "";
	private String southernSeasonTip = "";
	private String southernSeasonCache = "";

	// A list of recent TPS for computing average value of TPS
	private List<Double> aveTPSList = new ArrayList<>();

	/** Uptime Timer. */
	private UpTimer uptimer;
	/** Martian calendar panel. */
	private MarsCalendarDisplay calendarDisplay;

	/** label for Martian time. */
	private WebStyledLabel martianTimeLabel;
	/** label for Earth time. */
	private WebStyledLabel earthTimeLabel;
	/** label for Martian month. */
	private WebLabel martianMonthLabel;
	/** label for Northern hemisphere season. */
	private WebLabel northernSeasonLabel;
	/** label for Southern hemisphere season. */
	private WebLabel southernSeasonLabel;
	/** label for uptimer. */
	private WebLabel uptimeLabel;
	/** label for pulses per second label. */
	private WebLabel ticksPerSecLabel;
	/** label for time ratio. */
	private WebLabel timeRatioLabel;
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
	/** slider for pulse. */
//	private JSliderMW pulseSlider;
//	/** button for pause. */
//	private WebButton pauseButton;
//	/** button for play. */
//	private WebButton playButton;
	
	/** Icon for play. */
	private Icon playIcon; 
	/** Icon for pause. */
	private Icon pauseIcon;
	
	/** MainWindow instance . */
	private MainWindow mainWindow;

	private final DecimalFormat formatter2 = new DecimalFormat(Msg.getString("TimeWindow.decimalFormat2")); //$NON-NLS-1$
	private final DecimalFormat formatter3 = new DecimalFormat(Msg.getString("TimeWindow.decimalFormat3")); //$NON-NLS-1$

	/** Simulation instance */	
	private Simulation sim;
	/** Master Clock. */
	private MasterClock masterClock;
	/** Martian Clock. */
	private MarsClock marsTime;
	/** Earth Clock. */
	private EarthClock earthTime;

	/** Arial font. */ 
	private final Font ARIAL_FONT = new Font("Arial", Font.PLAIN, 14);
	/** Sans serif font. */ 
	private final Font SANS_SERIF_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
	
	/**
	 * Constructs a TimeWindow object
	 *
	 * @param desktop the desktop pane
	 */
	public TimeWindow(final MainDesktopPane desktop) {
		// Use TimeWindow constructor
		super(NAME, desktop);
		mainWindow = desktop.getMainWindow();

		// new ClockTool();

		// Set window resizable to false.
		setResizable(false);

		// Initialize data members
		sim = Simulation.instance();
		masterClock = sim.getMasterClock();
		// Add this class to the master clock's listener
		masterClock.addClockListener(this);
		marsTime = masterClock.getMarsClock();
		earthTime = masterClock.getEarthClock();
		uptimer = masterClock.getUpTimer();

		// Get content pane
		WebPanel mainPane = new WebPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create Martian time panel
		WebPanel martianTimePane = new WebPanel(new BorderLayout());
		martianTimePane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		mainPane.add(martianTimePane, BorderLayout.NORTH);

		// Create Martian time header label
		WebLabel martianTimeHeaderLabel = new WebLabel(Msg.getString("TimeWindow.martianTime"), WebLabel.CENTER); //$NON-NLS-1$
		martianTimeHeaderLabel.setFont(SANS_SERIF_FONT);
		martianTimePane.add(martianTimeHeaderLabel, BorderLayout.NORTH);

		martianTimeLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		martianTimeLabel.setHorizontalAlignment(JLabel.CENTER);
		martianTimeLabel.setVerticalAlignment(JLabel.CENTER);
		martianTimeLabel.setFont(ARIAL_FONT);
		martianTimeLabel.setForeground(new Color(135,100,39));
		martianTimeLabel.setText(marsTime.getDateTimeStamp());
		martianTimePane.add(martianTimeLabel, BorderLayout.SOUTH);

		// Create Martian calendar panel
		WebPanel martianCalendarPane = new WebPanel(new FlowLayout());
		martianCalendarPane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		mainPane.add(martianCalendarPane, BorderLayout.CENTER);

		// Create Martian calendar month panel
		WebPanel calendarMonthPane = new WebPanel(new BorderLayout());
		martianCalendarPane.add(calendarMonthPane);

		// Create martian month label
		martianMonthLabel = new WebLabel("Month of " + marsTime.getMonthName(), WebLabel.CENTER);
		martianMonthLabel.setFont(SANS_SERIF_FONT);
		calendarMonthPane.add(martianMonthLabel, BorderLayout.NORTH);

		// Create Martian calendar display
		calendarDisplay = new MarsCalendarDisplay(marsTime, desktop);
		WebPanel innerCalendarPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		innerCalendarPane.setPreferredSize(new Dimension(140, 100));
		innerCalendarPane.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.ORANGE, Color.ORANGE));//new Color(210,105,30)));
		innerCalendarPane.add(calendarDisplay);
		calendarMonthPane.add(innerCalendarPane, BorderLayout.CENTER);

//		WebPanel emptyP = new WebPanel();
//		WebLabel emptyL = new WebLabel(" ", WebLabel.CENTER);
//		emptyP.add(emptyL);
//		emptyL.setMinimumSize(new Dimension(140, 5));
//		emptyP.setMinimumSize(new Dimension(140, 5));
//		calendarMonthPane.add(emptyP, BorderLayout.SOUTH);

		WebPanel seasonPane = new WebPanel(new BorderLayout());
		mainPane.add(seasonPane, BorderLayout.SOUTH);

		WebPanel southPane = new WebPanel(new BorderLayout());
		seasonPane.add(southPane, BorderLayout.SOUTH);

		WebPanel marsSeasonPane = new WebPanel(new BorderLayout());
//		marsSeasonPane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		seasonPane.add(marsSeasonPane, BorderLayout.NORTH);

		// Create Martian season label
		WebLabel marsSeasonLabel = new WebLabel(Msg.getString("TimeWindow.martianSeasons"), WebLabel.CENTER); //$NON-NLS-1$
		marsSeasonLabel.setFont(SANS_SERIF_FONT);
		marsSeasonPane.add(marsSeasonLabel, BorderLayout.NORTH);

		// Create Martian season panel
		WebPanel hemiPane = new WebPanel(new SpringLayout());//BorderLayout());
		marsSeasonPane.add(hemiPane, BorderLayout.CENTER);

		String str =
				"<html>&#8201;Earth (days) vs Mars (sols)" +
				"<br>&#8201;Spring : 93 days vs 199 sols" +
				"<br>&#8201;Summer : 94 days vs 184 sols" +
				"<br>&#8201;Fall : 89 days vs 146 sols" +
				"<br>&#8201;Winter : 89 days vs 158 sols</html>";

//		Note :
//		&#8201; Thin tab space
//		&#8194; En tab space
//		&#8195; Em tab space

		marsSeasonPane.setToolTip(str);

		OrbitInfo orbitInfo = sim.getMars().getOrbitInfo();

		// Create Northern season header label
		WebLabel northernSeasonHeader = new WebLabel(Msg.getString("TimeWindow.northernHemisphere"),
				WebLabel.RIGHT); //$NON-NLS-1$
		hemiPane.add(northernSeasonHeader);

		// Create Northern season label
		northernSeasonLabel = new WebLabel(orbitInfo.getSeason(OrbitInfo.NORTHERN_HEMISPHERE), WebLabel.LEFT);
		hemiPane.add(northernSeasonLabel);

		// Create Southern season header label
		WebLabel southernSeasonHeader = new WebLabel(Msg.getString("TimeWindow.southernHemisphere"),
				WebLabel.RIGHT); //$NON-NLS-1$
		hemiPane.add(southernSeasonHeader);

		// Create Southern season label
		southernSeasonLabel = new WebLabel(orbitInfo.getSeason(OrbitInfo.SOUTHERN_HEMISPHERE), WebLabel.LEFT);
		hemiPane.add(southernSeasonLabel);

		// Use spring panel layout.
		SpringUtilities.makeCompactGrid(hemiPane,
				2, 2, 		// rows, cols
				40, 5,	// initX, initY
				7, 3);		// xPad, yPad

		// Create Earth time panel
		WebPanel earthTimePane = new WebPanel(new BorderLayout());
		earthTimePane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		seasonPane.add(earthTimePane, BorderLayout.CENTER);

		// Create Earth time header label
		WebLabel earthTimeHeaderLabel = new WebLabel(Msg.getString("TimeWindow.earthTime"), WebLabel.CENTER); //$NON-NLS-1$
		earthTimeHeaderLabel.setFont(SANS_SERIF_FONT);
		earthTimePane.add(earthTimeHeaderLabel, BorderLayout.NORTH);

		// Create Earth time label
		earthTimeLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		earthTimeLabel.setHorizontalAlignment(JLabel.CENTER);
		earthTimeLabel.setVerticalAlignment(JLabel.CENTER);
		earthTimeLabel.setFont(ARIAL_FONT);
		earthTimeLabel.setForeground(new Color(0, 69, 165));
		earthTimeLabel.setText(earthTime.getTimeStampF0());
		earthTimePane.add(earthTimeLabel, BorderLayout.SOUTH);

		// Create time panel
		WebPanel timePane = new WebPanel(new SpringLayout());
//		timePane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		southPane.add(timePane, BorderLayout.NORTH);

		// Create uptime header label
		WebLabel uptimeHeaderLabel = new WebLabel(Msg.getString("TimeWindow.simUptime"), WebLabel.RIGHT); //$NON-NLS-1$
		uptimeHeaderLabel.setFont(SANS_SERIF_FONT);
		timePane.add(uptimeHeaderLabel);

		// Create uptime label
		uptimeLabel = new WebLabel(uptimer.getUptime(), WebLabel.LEFT);
		timePane.add(uptimeLabel);

		WebLabel TPSHeaderLabel = new WebLabel(Msg.getString("TimeWindow.ticksPerSecond"), WebLabel.RIGHT); //$NON-NLS-1$
		TPSHeaderLabel.setFont(SANS_SERIF_FONT);
		timePane.add(TPSHeaderLabel);

		String TicksPerSec = "";
		
		if (masterClock.isFXGL) {
			TicksPerSec = formatter2.format(masterClock.getFPS());
		}
		else {
			TicksPerSec = formatter2.format(masterClock.getPulsesPerSecond());
		}

		ticksPerSecLabel = new WebLabel(TicksPerSec, WebLabel.LEFT);
		timePane.add(ticksPerSecLabel);

		// Use spring panel layout.
		SpringUtilities.makeCompactGrid(timePane,
				2, 2, 		// rows, cols
				23, 1,	// initX, initY
				7, 3);		// xPad, yPad

		// Create param panel
		WebPanel paramPane = new WebPanel(new SpringLayout());
		paramPane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		southPane.add(paramPane, BorderLayout.CENTER);

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
		marsPulseLabel = new WebLabel(formatter3.format(Math.round(pulseTime * 1000.0)/1000.0) + MSOL, WebLabel.LEFT);

		paramPane.add(aveTPSHeader);
		paramPane.add(aveTPSLabel);
		paramPane.add(execTimeHeader);
		paramPane.add(execTimeLabel);
		paramPane.add(sleepTimeHeader);
		paramPane.add(sleepTimeLabel);
		paramPane.add(marsPulseHeader);
		paramPane.add(marsPulseLabel);

		// Use spring panel layout.
		SpringUtilities.makeCompactGrid(paramPane,
				4, 2, //rows, cols
				35, 2,        //initX, initY
				7, 3);       //xPad, yPad

		// Create the pulse pane
		WebPanel pulsePane = new WebPanel(new BorderLayout());
//		pulsePane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		southPane.add(pulsePane, BorderLayout.SOUTH);

		// Create the time ratio label
		timeRatioLabel = new WebLabel(WebLabel.CENTER); //$NON-NLS-1$
		
		// Update the two time labels
		updateTimeLabels();
				
		// Create the simulation speed header label
		WebLabel speedLabel = new WebLabel(Msg.getString("TimeWindow.simSpeed"), WebLabel.CENTER); //$NON-NLS-1$
		speedLabel.setFont(SANS_SERIF_FONT);
		
		// Create the speed panel 
		WebPanel speedPanel = new WebPanel(new GridLayout(4, 1));
		pulsePane.add(speedPanel, BorderLayout.NORTH);
		
		// Create the simulation speed header label
		WebLabel TRHeader = new WebLabel(Msg.getString("TimeWindow.timeRatioHeader"), WebLabel.CENTER); //$NON-NLS-1$
		TRHeader.setFont(SANS_SERIF_FONT);
		speedPanel.add(TRHeader);
		speedPanel.add(timeRatioLabel);
		speedPanel.add(speedLabel);
		
		// Create the time compression label
		timeCompressionLabel = new WebLabel(WebLabel.CENTER);
		timeCompressionLabel.addMouseListener(new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				// Update the two time labels
				updateTimeLabels();
			}
		});
		speedPanel.add(timeCompressionLabel);
		
//		// Create pulse slider
//		int sliderpos = calculateSliderValue(masterClock.getTimeRatio());
//		pulseSlider = new JSliderMW(MIN, MAX, sliderpos);
//		// pulseSlider.setEnabled(false);
//		pulseSlider.setMajorTickSpacing(4);
//		pulseSlider.setMinorTickSpacing(1);
//		// activated for custom tick space
//		pulseSlider.setSnapToTicks(true); 
//		pulseSlider.setPaintTicks(true);
//		pulseSlider.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				try {
//					JSliderMW sliderSource = (JSliderMW) e.getSource();
//					if (!sliderSource.getValueIsAdjusting()) {
//						setTimeRatioFromSlider(pulseSlider.getValue()); 
//						
//						// Update the two time labels
//						updateTimeLabels();
//					}
//
//				} catch (Exception e2) {
//					logger.log(Level.SEVERE, e2.getMessage());
//				}
//			}
//		});
//
//		pulsePane.add(pulseSlider, BorderLayout.SOUTH);
//		setTimeRatioSlider(masterClock.getTimeRatio());

		// Pack window
		pack();

		// Add 10 pixels to packed window width
		Dimension windowSize = getSize();
		setSize(new Dimension((int) windowSize.getWidth() + 40, (int) windowSize.getHeight()));
	}

	public void updateTimeLabels() {
		if (marsTime != null) {
//			String ts = marsTime.getDateTimeStamp();
//			if (!ts.equals(":") && ts != null && !ts.equals("") && martianTimeLabel != null)
//				SwingUtilities.invokeLater(() -> martianTimeLabel.setText(ts));			
			
			int solElapsed = marsTime.getMissionSol();
			if (solElapsedCache != solElapsed) {
				solElapsedCache = solElapsed;
				String mn = marsTime.getMonthName();
				if (mn != null)// && martianMonthLabel != null)
					SwingUtilities.invokeLater(() -> martianMonthLabel.setText("Month of " + mn));
				setSeason();
			}
		}
		
		
		StringBuilder s0 = new StringBuilder();
		int ratio = (int)masterClock.getTimeRatio();
		s0.append(ONE_REAL_SEC);
		s0.append(ClockUtils.getTimeString(ratio));

		if (timeRatioLabel != null)
			SwingUtilities.invokeLater(() -> timeRatioLabel.setText(ratio + "x")); //$NON-NLS-1$
		if (timeCompressionLabel != null)
			SwingUtilities.invokeLater(() -> timeCompressionLabel.setText(s0.toString()));
		
		// Create execution time label
		long execTime = masterClock.getExecutionTime();
		if (execTimeLabel != null) execTimeLabel.setText(execTime + MS);

//		// Compute the average value of TPS
//		double tps = masterClock.getPulsesPerSecond();
//		aveTPSList.add(tps);
//		if (aveTPSList.size() > 20)
//			aveTPSList.remove(0);
//
//		DoubleSummaryStatistics stats = aveTPSList.stream().collect(Collectors.summarizingDouble(Double::doubleValue));
//		double ave = stats.getAverage();
//		if (ave < .01) {
//			aveTPSList.clear();
//			ave = tps;
//		}
		
		double ave = masterClock.updateAverageTPS();

		aveTPSLabel.setText(formatter2.format(ave));

		// Create sleep time label
		long sleepTime = masterClock.getSleepTime();
		if (sleepTimeLabel != null) sleepTimeLabel.setText(sleepTime + MS);
		
		// Create mars pulse label
		double pulseTime = masterClock.getMarsPulseTime();
		if (marsPulseLabel != null) marsPulseLabel.setText(Math.round(pulseTime *1000.0)/1000.0 + MSOL);
	}
	
//	/**
//	 * Sets the time ratio for the simulation based on the slider value.
//	 *
//	 * @param sliderValue the slider value (1 to 100).
//	 */
//	private void setTimeRatioFromSlider(int sliderValue) {
//		double timeRatio = calculateTimeRatioFromSlider(sliderValue);
//		masterClock.setTimeRatio((int)timeRatio);
//	}

	/**
	 * Calculates a time ratio given a slider value.
	 * 
	 * @param sliderValue the slider value from 1 to 100.
	 * @return time ratio value (simulation time / real time).
	 */
	public static double calculateTimeRatioFromSlider(int sliderValue) {
		return Math.pow(2, sliderValue);
	}

//	/**
//	 * Moves the slider bar appropriately given the time ratio.
//	 *
//	 * @param timeRatio the time ratio (simulation time / real time).
//	 */
//	public void setTimeRatioSlider(double timeRatio) {
//		int sliderValue = calculateSliderValue(timeRatio);
//		int currentSlider = pulseSlider.getValue();
//		if (sliderValue != currentSlider) {
//			// Prevent feedback when setting a new value without user
//			pulseSlider.setValueIsAdjusting(true);
//			pulseSlider.setValue(sliderValue);
//			pulseSlider.setValueIsAdjusting(false);
//		}
//	}

	/**
	 * Calculates a slider value based on a time ratio. Note: This method is the
	 * inverse of calculateTimeRatioFromSlider.
	 *
	 * @param timeRatio time ratio (simulation time / real time).
	 * @return slider value (MIN to MAX).
	 */
	public static int calculateSliderValue(double timeRatio) {
		int speed = 0;
    	int tr = (int) timeRatio;	
        int base = 2;

        while (tr != 1) {
            tr = tr/base;
            --speed;
        }
        
    	return -speed;
	}


	/**
	 * Set and update the season labels
	 */
	public void setSeason() {
		OrbitInfo orbitInfo = sim.getMars().getOrbitInfo();
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
	public String getSeasonTip(String hemi) {
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
	 * Update the calendar, the time ratio and time compression labels via ui pulse
	 */
	public void updateSlowLabels() {
		// Update the two time labels
		updateTimeLabels();
		// Update the calender
		calendarDisplay.update();
	}

	/**
	 * Updates date and time in Time Tool via clock pulse
	 */
	public void updateFastLabels() {
		if (marsTime != null && martianTimeLabel != null) {
			String ts = marsTime.getDateTimeStamp();
			if (ts != null && !ts.equals(":") && !ts.equals("") )
				SwingUtilities.invokeLater(() -> martianTimeLabel.setText(ts));			
			
//			int solElapsed = marsTime.getMissionSol();
//			if (solElapsedCache != solElapsed) {
//				solElapsedCache = solElapsed;
//				String mn = marsTime.getMonthName();
//				if (mn != null)// && martianMonthLabel != null)
//					SwingUtilities.invokeLater(() -> martianMonthLabel.setText("Month of " + mn));
//				setSeason();
//			}
		}

		if (earthTime != null) {
			String ts = earthTime.getTimeStampF0();
			if (ts != null)
				SwingUtilities.invokeLater(() -> earthTimeLabel.setText(ts));
		}

		if (masterClock != null) {
			if (masterClock.isFXGL) {
				SwingUtilities.invokeLater(() -> ticksPerSecLabel.setText(formatter2.format(masterClock.getFPS())));
			}
			else {
				SwingUtilities.invokeLater(() -> ticksPerSecLabel.setText(formatter2.format(masterClock.getPulsesPerSecond())));
			}
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
		// logger.info("TimeWindow : calling pauseChange()");
		if (!isPaused) {
			if (desktop.isToolWindowOpen(TimeWindow.NAME)) {
				// Update the slider based on the latest time ratio
//				setTimeRatioSlider(masterClock.getTimeRatio());
				// update the slow labels
				updateSlowLabels();
			}
		}
	}

	/**
	 * Enables/disables the pause button
	 *
	 * @param value true or false
	 */
	public void enablePauseButton(boolean value) {
		// Note : when a wizard or a dialog box is opened/close,
		// need to call below to remove/add the ability to use ESC to
		// unpause/pause
//		if (!MainScene.isFXGL)
//			mainScene.setEscapeEventHandler(value, mainScene.getStage());
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
		if (desktop.isToolWindowOpen(TimeWindow.NAME)) {
			// update the fast labels
			updateFastLabels();
		}
	}

	@Override
	public void uiPulse(double time) {
		if (desktop.isToolWindowOpen(TimeWindow.NAME)) {
//			// Update the slider based on the latest time ratio
//			setTimeRatioSlider(masterClock.getTimeRatio());
			// update the slow labels
			updateSlowLabels();
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
