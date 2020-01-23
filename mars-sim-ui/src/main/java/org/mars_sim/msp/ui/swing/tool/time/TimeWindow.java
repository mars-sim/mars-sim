/**
 * Mars Simulation Project
 * TimeWindow.java
 * @version 3.1.0 2017-10-05
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.time;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockUtils;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.UpTimer;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.JSliderMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import com.alee.extended.label.WebStyledLabel;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
//import com.alee.managers.language.data.TooltipWay;

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
	public static final String RESIDUAL_TIME = "Residual Time : ";
	/** the execution time unit */
	public static final String US = " micro-secs";
	/** the execution time label string */
	public static final String BASE_TBU = "Base TBU : ";
	/** the execution time unit */
	public static final String MS = " ms";
	/** the real second label string */	
	public static final String ONE_REAL_SEC = "1 Real Sec = ";
	/** the upper limit of the slider bar. */
	public static final int MAX = 13;
	/** the lower limit of the slider bar. */
	public static final int MIN = 0;

	// Data members
	private int solElapsedCache = 0;

	private String northernSeasonTip ="";
	private String northernSeasonCache = "";
	private String southernSeasonTip = "";
	private String southernSeasonCache = "";

	private Simulation sim;
	/** Master Clock. */
	private MasterClock masterClock;
	/** Martian Clock. */
	private MarsClock marsTime;
	/** Earth Clock. */
	private EarthClock earthTime;
	/** Uptime Timer. */
	private UpTimer uptimer;
	/** Martian calendar panel. */
	private MarsCalendarDisplay calendarDisplay;
	/** label for Martian time. */
	private WebStyledLabel martianTimeLabel;
	/** label for Martian month. */
	private WebLabel martianMonthLabel;
	/** label for Northern hemisphere season. */
	private WebLabel northernSeasonLabel;
	/** label for Southern hemisphere season. */
	private WebLabel southernSeasonLabel;
	/** label for Earth time. */
	private WebLabel earthTimeLabel;
	/** label for uptimer. */
	private WebLabel uptimeLabel;
	/** label for pulses per second label. */
	private WebLabel pulsesPerSecLabel;
	/** label for time ratio. */
	private WebLabel timeRatioLabel;
	/** label for execution time. */
	private WebLabel execTimeLabel;
	/** label for base TBU. */
	private WebLabel baseTBULabel;
	/** label for sleep time. */
	private WebLabel sleepTimeLabel;
	/** label for residual time. */
	private WebLabel residualTimeLabel;
	/** label for time compression. */	
	private WebLabel timeCompressionLabel;
	/** slider for pulse. */
	private JSliderMW pulseSlider;
	/** button for pause. */
	private WebButton pauseButton;
	/** button for play. */
	private WebButton playButton;
	
	/** Icon for play. */
	private Icon playIcon; 
	/** Icon for pause. */
	private Icon pauseIcon;
	
	/** MainWindow instance . */
	private MainWindow mainWindow;

	private DecimalFormat formatter = new DecimalFormat(Msg.getString("TimeWindow.decimalFormat")); //$NON-NLS-1$

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
		masterClock.addClockListener(this);
		marsTime = masterClock.getMarsClock();
		earthTime = masterClock.getEarthClock();
		uptimer = masterClock.getUpTimer();

//		ratioatmid = masterClock.getTimeRatio();

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
		martianTimeHeaderLabel.setFont(new Font("Serif", Font.BOLD, 14));
		martianTimePane.add(martianTimeHeaderLabel, BorderLayout.NORTH);

		// Create Martian time label
		Font font = new Font("Arial", Font.BOLD, 12);
		
		martianTimeLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		martianTimeLabel.setHorizontalAlignment(JLabel.CENTER);
		martianTimeLabel.setVerticalAlignment(JLabel.CENTER);
		martianTimeLabel.setFont(font);
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
		martianMonthLabel.setFont(new Font("Serif", Font.BOLD, 14));
		calendarMonthPane.add(martianMonthLabel, BorderLayout.NORTH);

		// Create Martian calendar display
		calendarDisplay = new MarsCalendarDisplay(marsTime, desktop);
		WebPanel innerCalendarPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		innerCalendarPane.setPreferredSize(new Dimension(140, 100));
		innerCalendarPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
		innerCalendarPane.add(calendarDisplay);
		calendarMonthPane.add(innerCalendarPane, BorderLayout.CENTER);

		WebPanel emptyP = new WebPanel();
		WebLabel emptyL = new WebLabel(" ", WebLabel.CENTER);
		emptyP.add(emptyL);
		emptyL.setMinimumSize(new Dimension(140, 15));
		emptyP.setMinimumSize(new Dimension(140, 15));
		calendarMonthPane.add(emptyP, BorderLayout.SOUTH);

		WebPanel seasonPane = new WebPanel(new BorderLayout());
		mainPane.add(seasonPane, BorderLayout.SOUTH);

		WebPanel simulationPane = new WebPanel(new BorderLayout());
		seasonPane.add(simulationPane, BorderLayout.SOUTH);

		// Create Martian season panel
		WebPanel marsSeasonPane = new WebPanel(new BorderLayout());
		marsSeasonPane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		seasonPane.add(marsSeasonPane, BorderLayout.NORTH);

		// Create Martian season label
		WebLabel marsSeasonLabel = new WebLabel(Msg.getString("TimeWindow.martianSeasons"), WebLabel.CENTER); //$NON-NLS-1$
		marsSeasonLabel.setFont(new Font("Serif", Font.BOLD, 14));
		marsSeasonPane.add(marsSeasonLabel, BorderLayout.NORTH);

		// Create Northern season label
		northernSeasonLabel = new WebLabel(Msg.getString("TimeWindow.northernHemisphere", //$NON-NLS-1$
				marsTime.getSeason(MarsClock.NORTHERN_HEMISPHERE)), WebLabel.CENTER);
		marsSeasonPane.add(northernSeasonLabel, BorderLayout.CENTER);

//		String str = "<html>\t\tEarth vs Mars " +
//		"<br>\tSpring : 93 days vs 199 days" + "<br>\tSummer : 94 days vs 184 days" +
//		"<br>\tFall : 89 days vs 146 days" +
//		"<br>\tWinter : 89 days vs 158 days</html>";

		// Create Southern season label
		southernSeasonLabel = new WebLabel(Msg.getString("TimeWindow.southernHemisphere", //$NON-NLS-1$
				marsTime.getSeason(MarsClock.SOUTHERN_HEMISPHERE)), WebLabel.CENTER);
		marsSeasonPane.add(southernSeasonLabel, BorderLayout.SOUTH);

		// Create Earth time panel
		WebPanel earthTimePane = new WebPanel(new BorderLayout());
		earthTimePane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		seasonPane.add(earthTimePane, BorderLayout.CENTER);

		// Create Earth time header label
		WebLabel earthTimeHeaderLabel = new WebLabel(Msg.getString("TimeWindow.earthTime"), WebLabel.CENTER); //$NON-NLS-1$
		earthTimeHeaderLabel.setFont(new Font("Serif", Font.BOLD, 14));
		earthTimePane.add(earthTimeHeaderLabel, BorderLayout.NORTH);

		// Create Earth time label
		earthTimeLabel = new WebLabel(earthTime.getTimeStampF0(), WebLabel.CENTER);
		earthTimePane.add(earthTimeLabel, BorderLayout.SOUTH);

		// Create uptime panel
		WebPanel uptimePane = new WebPanel(new BorderLayout());
		uptimePane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		simulationPane.add(uptimePane, BorderLayout.NORTH);

		WebPanel TPSPane = new WebPanel(new GridLayout(6, 1));//new BorderLayout());
		TPSPane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		uptimePane.add(TPSPane, BorderLayout.SOUTH);

		// Create uptime header label
		WebLabel uptimeHeaderLabel = new WebLabel(Msg.getString("TimeWindow.simUptime"), WebLabel.CENTER); //$NON-NLS-1$
		uptimeHeaderLabel.setFont(new Font("Serif", Font.BOLD, 14));
		uptimePane.add(uptimeHeaderLabel, BorderLayout.NORTH);

		WebLabel TPSHeaderLabel = new WebLabel(Msg.getString("TimeWindow.ticksPerSecond"), WebLabel.CENTER); //$NON-NLS-1$
		TPSHeaderLabel.setFont(new Font("Serif", Font.BOLD, 14));
		TPSPane.add(TPSHeaderLabel);

		// Create uptime label
		uptimeLabel = new WebLabel(uptimer.getUptime(), WebLabel.CENTER);
		uptimePane.add(uptimeLabel, BorderLayout.CENTER);

		String pulsePerSecond = "";
		
		if (masterClock.isFXGL) {
			pulsePerSecond = formatter.format(masterClock.getFPS());
		}
		else {
			pulsePerSecond = formatter.format(masterClock.getPulsesPerSecond());
		}
		pulsesPerSecLabel = new WebLabel(pulsePerSecond, WebLabel.CENTER);
		TPSPane.add(pulsesPerSecLabel);

		// Create execution time label
		long execTime = masterClock.getExecutionTime();
		execTimeLabel = new WebLabel(EXEC + execTime + US, WebLabel.CENTER);
		
		// Create TBU label
		double baseTBU = Math.round(masterClock.getBaseTBU()*10.0)/10.0;
		baseTBULabel = new WebLabel(BASE_TBU + baseTBU + MS, WebLabel.CENTER);
		
		// Create sleep time label
		long sleepTime = masterClock.getSleepTime();
		sleepTimeLabel = new WebLabel(SLEEP_TIME + sleepTime + MS, WebLabel.CENTER);
		
		// Create residual time label
		long residualTime = masterClock.getResidualTime();
		residualTimeLabel = new WebLabel(RESIDUAL_TIME + residualTime + MS, WebLabel.CENTER);
				
		TPSPane.add(execTimeLabel);
		TPSPane.add(baseTBULabel);
		TPSPane.add(sleepTimeLabel);
		TPSPane.add(residualTimeLabel);
		
		// Create the pulse pane
		WebPanel pulsePane = new WebPanel(new BorderLayout());
//		pulsePane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		simulationPane.add(pulsePane, BorderLayout.CENTER);

		// Create the time ratio label
		timeRatioLabel = new WebLabel(WebLabel.CENTER); //$NON-NLS-1$
		
		// Update the two time labels
		updateTimeLabels();
				
		// Create the simulation speed header label
		WebLabel speedLabel = new WebLabel(Msg.getString("TimeWindow.simSpeed"), WebLabel.CENTER); //$NON-NLS-1$
		speedLabel.setFont(new Font("Serif", Font.BOLD, 14));
		
		// Create the speed panel 
		WebPanel speedPanel = new WebPanel(new GridLayout(4, 1));
		pulsePane.add(speedPanel, BorderLayout.NORTH);
		
		// Create the simulation speed header label
		WebLabel TRHeader = new WebLabel(Msg.getString("TimeWindow.timeRatioHeader"), WebLabel.CENTER); //$NON-NLS-1$
		TRHeader.setFont(new Font("Serif", Font.BOLD, 14));
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
		
		// Create pulse slider
		int sliderpos = calculateSliderValue(masterClock.getTimeRatio());
		pulseSlider = new JSliderMW(MIN, MAX, sliderpos);
		// pulseSlider.setEnabled(false);
		pulseSlider.setMajorTickSpacing(5);
		pulseSlider.setMinorTickSpacing(1);
		// activated for custom tick space
		pulseSlider.setSnapToTicks(true); 
		pulseSlider.setPaintTicks(true);
		pulseSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				try {
					setTimeRatioFromSlider(pulseSlider.getValue()); 
					// Update the two time labels
					updateTimeLabels();

				} catch (Exception e2) {
					logger.log(Level.SEVERE, e2.getMessage());
				}
			}
		});

		pulsePane.add(pulseSlider, BorderLayout.SOUTH);
		setTimeRatioSlider(masterClock.getTimeRatio());

		WebPanel pausePane = new WebPanel(new FlowLayout());
		playButton = new WebButton();
		playButton.setSize(40, 25);
		playIcon = ImageLoader.getIcon(Msg.getString("img.speed.play")); 
		playButton.setIcon(playIcon);
		TooltipManager.setTooltip(playButton, "Play/Resume the simulation", TooltipWay.up);
		playButton.setEnabled(false);
		
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
					masterClock.setPaused(!masterClock.isPaused(), false);
//					if (playButton.isSelected())
//						masterClock.setPaused(false, false);
//					else
//						masterClock.setPaused(true, false);		
			}
		});
		
		pauseButton = new WebButton();
		pauseButton.setSize(40, 25);
		pauseIcon = ImageLoader.getIcon(Msg.getString("img.speed.pause"));
		pauseButton.setIcon(pauseIcon);
		TooltipManager.setTooltip(pauseButton, "Resume the simulation", TooltipWay.up);

		
//		if (masterClock.isPaused()) {
////			pauseButton.setText("  " + Msg.getString("TimeWindow.button.resume") + "  "); //$NON-NLS-1$	
//			pauseButton.setIcon(playIcon);
//			TooltipManager.setTooltip(pauseButton, "Resume the simulation", TooltipWay.up);			
//		}
//		else {
////			pauseButton.setText("    " + Msg.getString("TimeWindow.button.pause") + "    ");  //$NON-NLS-1$
//			pauseButton.setIcon(pauseIcon);
//			TooltipManager.setTooltip(pauseButton, "Pause the simulation", TooltipWay.up);
//		}
			
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
					masterClock.setPaused(!masterClock.isPaused(), false);
//					if (!pauseButton.isSelected())
//						masterClock.setPaused(false, false);
//					else
//						masterClock.setPaused(true, false);					
			}
		});
		
		pausePane.add(playButton);
		pausePane.add(pauseButton);
			
		simulationPane.add(pausePane, BorderLayout.SOUTH);
			
		// Pack window
		pack();

		// Add 10 pixels to packed window width
		Dimension windowSize = getSize();
		setSize(new Dimension((int) windowSize.getWidth() + 40, (int) windowSize.getHeight()));
	}

	public void updateTimeLabels() {
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
		if (execTimeLabel != null) execTimeLabel.setText(EXEC + execTime + US);		
		
		// Create TBU label
		double baseTBU = Math.round(masterClock.getBaseTBU()*10.0)/10.0;
		if (baseTBULabel != null) baseTBULabel.setText(BASE_TBU + baseTBU + MS);
		
		// Create sleep time label
		long sleepTime = masterClock.getSleepTime();
		if (sleepTimeLabel != null) sleepTimeLabel.setText(SLEEP_TIME + sleepTime + MS);
		
		// Create residual time label
		long residualTime = masterClock.getResidualTime();
		if (residualTimeLabel != null) residualTimeLabel.setText(RESIDUAL_TIME + residualTime + MS);
	}
	
	/**
	 * Sets the time ratio for the simulation based on the slider value.
	 *
	 * @param sliderValue the slider value (1 to 100).
	 */
	private void setTimeRatioFromSlider(int sliderValue) {
		double timeRatio = calculateTimeRatioFromSlider(sliderValue);
		masterClock.setTimeRatio((int)timeRatio);
	}

	/**
	 * Calculates a time ratio given a slider value.
	 * 
	 * @param sliderValue the slider value from 1 to 100.
	 * @return time ratio value (simulation time / real time).
	 */
	public static double calculateTimeRatioFromSlider(int sliderValue) {
		return Math.pow(2, sliderValue);
	}

	/**
	 * Moves the slider bar appropriately given the time ratio.
	 *
	 * @param timeRatio the time ratio (simulation time / real time).
	 */
	public void setTimeRatioSlider(double timeRatio) {
		int sliderValue = calculateSliderValue(timeRatio);
		pulseSlider.setValue(sliderValue);
	}

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

		String northernSeason = marsTime.getSeason(MarsClock.NORTHERN_HEMISPHERE);
		String southernSeason = marsTime.getSeason(MarsClock.SOUTHERN_HEMISPHERE);

		if (!northernSeasonCache.equals(northernSeason)) {
			northernSeasonCache = northernSeason;

			if (marsTime.getSeason(MarsClock.NORTHERN_HEMISPHERE) != null && northernSeasonLabel != null) {
				northernSeasonLabel.setText(Msg.getString("TimeWindow.northernHemisphere", //$NON-NLS-1$
						northernSeason));
			}

			northernSeasonTip = getSeasonTip(northernSeason);
			TooltipManager.setTooltip(northernSeasonLabel, northernSeasonTip, TooltipWay.down);
		}

		if (!southernSeasonCache.equals(southernSeason)) {
			southernSeasonCache = southernSeason;

			if (marsTime.getSeason(MarsClock.SOUTHERN_HEMISPHERE) != null) {
				southernSeasonLabel.setText(Msg.getString("TimeWindow.southernHemisphere", //$NON-NLS-1$
						southernSeason));
			}

			southernSeasonTip = getSeasonTip(southernSeason);
			TooltipManager.setTooltip(southernSeasonLabel, southernSeasonTip, TooltipWay.down);
		}

	}

	/**
	 * Get the text for the season label tooltip
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
		if (marsTime != null) {
			String ts = marsTime.getDateTimeStamp();
			if (!ts.equals(":") && ts != null && !ts.equals("") && martianTimeLabel != null)
				SwingUtilities.invokeLater(() -> martianTimeLabel.setText(ts));
			int solElapsed = marsTime.getMissionSol();

			if (solElapsed != solElapsedCache) {
				String mn = marsTime.getMonthName();
				if (mn != null)// && martianMonthLabel != null)
					SwingUtilities.invokeLater(() -> martianMonthLabel.setText("Month of " + mn));
				setSeason();
				solElapsedCache = solElapsed;
			}
		}

		if (earthTime != null) {
			String ts = earthTime.getTimeStampF0();
			if (ts != null)
				SwingUtilities.invokeLater(() -> earthTimeLabel.setText(ts));
		}

		if (masterClock != null) {
			if (masterClock.isFXGL) {
				SwingUtilities.invokeLater(() -> pulsesPerSecLabel.setText(formatter.format(masterClock.getFPS())));
			}
			else {
				SwingUtilities.invokeLater(() -> pulsesPerSecLabel.setText(formatter.format(masterClock.getPulsesPerSecond())));
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
		// Update pause/resume button text based on master clock pause state.
		if (isPaused) {
//			if (showPane && mainScene != null && !masterClock.isSavingSimulation())
//				mainScene.startPausePopup();
//			pauseButton.setIcon(playIcon);
//			pauseButton.setText("  " + Msg.getString("TimeWindow.button.resume") + "  "); //$NON-NLS-1$
			// desktop.getMarqueeTicker().pauseMarqueeTimer(true);
			pauseButton.setEnabled(false);
			playButton.setEnabled(true);
		} else {
//			pauseButton.setIcon(pauseIcon);
//			pauseButton.setText("    " + Msg.getString("TimeWindow.button.pause") + "    "); //$NON-NLS-1$
			// desktop.getMarqueeTicker().pauseMarqueeTimer(false);
//			if (showPane && mainScene != null)
//				mainScene.stopPausePopup();
			pauseButton.setEnabled(true);
			playButton.setEnabled(false);
		}
	}

	/**
	 * Enables/disables the pause button
	 *
	 * @param value true or false
	 */
	public void enablePauseButton(boolean value) {
		pauseButton.setEnabled(value);
		playButton.setEnabled(!value);

		// Note : when a wizard or a dialog box is opened/close,
		// need to call below to remove/add the ability to use ESC to
		// unpause/pause
//		if (!MainScene.isFXGL)
//			mainScene.setEscapeEventHandler(value, mainScene.getStage());
	}

	@Override
	public void clockPulse(double time) {
		if (desktop.isToolWindowOpen(TimeWindow.NAME)) {
			// update the fast labels
			updateFastLabels();
		}
	}

	@Override
	public void uiPulse(double time) {
		if (desktop.isToolWindowOpen(TimeWindow.NAME)) {
			// update the slow labels
			updateSlowLabels();
			// Update the slider based on the latest time ratio
			setTimeRatioSlider(masterClock.getTimeRatio());
			updateTimeLabels();
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