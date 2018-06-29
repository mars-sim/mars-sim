/**
 * Mars Simulation Project
 * TimeWindow.java
 * @version 3.1.0 2017-10-05
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.time;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.UpTimer;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.JSliderMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
//import com.alee.managers.language.data.TooltipWay;


/**
 * The TimeWindow is a tool window that displays the current Martian and Earth
 * time.<br/>
 * The numbers below have been tweaked with some care. At 20, the realworld:sim
 * ratio is 1:1 above 20, the numbers start climbing logarithmically maxing out
 * at around 100K this is really fast Below 20, the simulation goes in slow
 * motion, 1:0.0004 is around the slowest. The increments may be so small at
 * this point that events can't progress at all. When run too quickly, lots of
 * accidents occur, and lots of settlers die.
 */
public class TimeWindow extends ToolWindow implements ClockListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TimeWindow.class.getName());

	/** Tool name. */
	public static final String NAME = Msg.getString("TimeWindow.title"); //$NON-NLS-1$

	public static final String ONE_REAL_SEC = "1 real sec equals ";

	/** the max ratio the sim can be set at. */
	public static final double maxratio = 10800d;

	/** the minimum ratio the sim can be set at. */
	private static final double minfracratio = 0.01d;// 0.001d;

	/** the largest fractional ratio the sim can be set at. */
	private static final double maxfracratio = 0.98d;

	// don't recommend changing these:
	private static final double minslider = 20d;
	private static final double midslider = (50d - minslider);
	private static final double maxslider = 100d - minslider;
	private static final double minfracpos = 1d;
	private static final double maxfracpos = minslider - 1d;

	/** the "default" ratio that will be set at 50, the middle of the scale. */
	public static double ratioatmid = 500D; // default value = 500D This avoids
											// maven test error

	// Data members
	private int solElapsedCache = 0;

	private String northernSeasonTip, northernSeasonCache = "";
	private String southernSeasonTip, southernSeasonCache = "";

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
	private JLabel martianTimeLabel;
	/** label for Martian month. */
	private JLabel martianMonthLabel;
	/** label for Northern hemisphere season. */
	private JLabel northernSeasonLabel;
	/** label for Southern hemisphere season. */
	private JLabel southernSeasonLabel;
	/** label for Earth time. */
	private JLabel earthTimeLabel;
	/** label for uptimer. */
	private JLabel uptimeLabel;
	/** label for pulses per second label. */
	private JLabel pulsespersecondLabel, pulsesLabel;

	private JLabel timeRatioLabel, timeCompressionLabel;

	/** slider for pulse. */
	private JSliderMW pulseSlider;
	private JButton pauseButton;
	//private BalloonToolTip balloonToolTip;
	private MainScene mainScene;

	/**
	 * Constructs a TimeWindow object
	 *
	 * @param desktop
	 *            the desktop pane
	 */
	public TimeWindow(final MainDesktopPane desktop) {
		// Use TimeWindow constructor
		super(NAME, desktop);
		mainScene = desktop.getMainScene();

		ratioatmid = Simulation.instance().getMasterClock().getTimeRatio();

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
		//balloonToolTip = new BalloonToolTip();

		// Get content pane
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create Martian time panel
		JPanel martianTimePane = new JPanel(new BorderLayout());
		martianTimePane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		mainPane.add(martianTimePane, BorderLayout.NORTH);

		// Create Martian time header label
		JLabel martianTimeHeaderLabel = new JLabel(Msg.getString("TimeWindow.martianTime"), JLabel.CENTER); //$NON-NLS-1$
		martianTimePane.add(martianTimeHeaderLabel, BorderLayout.NORTH);

		// Create Martian time label
		martianTimeLabel = new JLabel(marsTime.getDateTimeStamp(), JLabel.CENTER);
		martianTimePane.add(martianTimeLabel, BorderLayout.SOUTH);

		// Create Martian calendar panel
		JPanel martianCalendarPane = new JPanel(new FlowLayout());
		martianCalendarPane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		mainPane.add(martianCalendarPane, BorderLayout.CENTER);

		// Create Martian calendar month panel
		JPanel calendarMonthPane = new JPanel(new BorderLayout());
		martianCalendarPane.add(calendarMonthPane);

		// Create martian month label
		martianMonthLabel = new JLabel("Month of " + marsTime.getMonthName(), JLabel.CENTER);
		calendarMonthPane.add(martianMonthLabel, BorderLayout.NORTH);

		// Create Martian calendar display
		calendarDisplay = new MarsCalendarDisplay(marsTime, desktop);
		JPanel innerCalendarPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		innerCalendarPane.setPreferredSize(new Dimension(140, 100));
		innerCalendarPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
		innerCalendarPane.add(calendarDisplay);
		calendarMonthPane.add(innerCalendarPane, BorderLayout.CENTER);

		JPanel emptyP = new JPanel();
		JLabel emptyL = new JLabel(" ", JLabel.CENTER);
		emptyP.add(emptyL);
		emptyL.setMinimumSize(new Dimension(140, 15));
		emptyP.setMinimumSize(new Dimension(140, 15));
		calendarMonthPane.add(emptyP, BorderLayout.SOUTH);

		JPanel southPane = new JPanel(new BorderLayout());
		mainPane.add(southPane, BorderLayout.SOUTH);

		JPanel simulationPane = new JPanel(new BorderLayout());
		southPane.add(simulationPane, BorderLayout.SOUTH);

		// Create Martian season panel
		JPanel marsSeasonPane = new JPanel(new BorderLayout());
		marsSeasonPane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		southPane.add(marsSeasonPane, BorderLayout.NORTH);

		// Create Martian season label
		JLabel marsSeasonLabel = new JLabel(Msg.getString("TimeWindow.martianSeasons"), JLabel.CENTER); //$NON-NLS-1$
		marsSeasonPane.add(marsSeasonLabel, BorderLayout.NORTH);

		// Create Northern season label
		northernSeasonLabel = new JLabel(Msg.getString("TimeWindow.northernHemisphere", //$NON-NLS-1$
				marsTime.getSeason(MarsClock.NORTHERN_HEMISPHERE)), JLabel.CENTER);
		marsSeasonPane.add(northernSeasonLabel, BorderLayout.CENTER);

		/*
		 * String str = "<html>\t\tEarth vs Mars " +
		 * "<br>\tSpring : 93 days vs 199 days" +
		 * "<br>\tSummer : 94 days vs 184 days" +
		 * "<br>\tFall : 89 days vs 146 days" +
		 * "<br>\tWinter : 89 days vs 158 days</html>";
		 */
		// balloonToolTip.createBalloonTip(northernSeasonLabel,
		// northernSeasonTip);//Msg.getString("TimeWindow.season.toolTip"));
		// //$NON-NLS-1$

		// Create Southern season label
		southernSeasonLabel = new JLabel(Msg.getString("TimeWindow.southernHemisphere", //$NON-NLS-1$
				marsTime.getSeason(MarsClock.SOUTHERN_HEMISPHERE)), JLabel.CENTER);
		marsSeasonPane.add(southernSeasonLabel, BorderLayout.SOUTH);

		// balloonToolTip.createBalloonTip(southernSeasonLabel,
		// southernSeasonTip);//Msg.getString("TimeWindow.season.toolTip"),
		// BalloonToolTip.Orientation.RIGHT_ABOVE); //$NON-NLS-1$

		// Create Earth time panel
		JPanel earthTimePane = new JPanel(new BorderLayout());
		earthTimePane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		southPane.add(earthTimePane, BorderLayout.CENTER);

		// Create Earth time header label
		JLabel earthTimeHeaderLabel = new JLabel(Msg.getString("TimeWindow.earthTime"), JLabel.CENTER); //$NON-NLS-1$
		earthTimePane.add(earthTimeHeaderLabel, BorderLayout.NORTH);

		// Create Earth time label
		earthTimeLabel = new JLabel(earthTime.getTimeStampF0(), JLabel.CENTER);
		earthTimePane.add(earthTimeLabel, BorderLayout.SOUTH);

		// Create uptime panel
		JPanel uptimePane = new JPanel(new BorderLayout());
		uptimePane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		simulationPane.add(uptimePane, BorderLayout.NORTH);

		JPanel pulsespersecondPane = new JPanel(new BorderLayout());
		pulsespersecondPane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		uptimePane.add(pulsespersecondPane, BorderLayout.SOUTH);

		JPanel pausePane = new JPanel(new FlowLayout());
		pauseButton = new JButton("    " + Msg.getString("TimeWindow.button.pause") + "    "); //$NON-NLS-1$
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				masterClock.setPaused(!masterClock.isPaused(), false);
			}
		});
		pausePane.add(pauseButton);

		// Create uptime header label
		JLabel uptimeHeaderLabel = new JLabel(Msg.getString("TimeWindow.simUptime"), JLabel.CENTER); //$NON-NLS-1$
		uptimePane.add(uptimeHeaderLabel, BorderLayout.NORTH);

		JLabel pulsespersecondHeaderLabel = new JLabel(Msg.getString("TimeWindow.ticksPerSecond"), JLabel.CENTER); //$NON-NLS-1$
		pulsespersecondPane.add(pulsespersecondHeaderLabel, BorderLayout.NORTH);

		// Create uptime label
		uptimeLabel = new JLabel(uptimer.getUptime(), JLabel.CENTER);
		uptimePane.add(uptimeLabel, BorderLayout.CENTER);

		DecimalFormat formatter = new DecimalFormat(Msg.getString("TimeWindow.decimalFormat")); //$NON-NLS-1$
		String pulsePerSecond = formatter.format(masterClock.getPulsesPerSecond());
		pulsespersecondLabel = new JLabel(pulsePerSecond, JLabel.CENTER);
		pulsespersecondPane.add(pulsespersecondLabel, BorderLayout.CENTER);

		/*
		 * String pulses = formatter.format(masterClock.getPulses());
		 * pulsesLabel = new JLabel(pulses, JLabel.CENTER);
		 * pulsespersecondPane.add(pulsesLabel, BorderLayout.SOUTH);
		 */

		// Create uptime panel
		JPanel pulsePane = new JPanel(new BorderLayout());
		pulsePane.setBorder(new CompoundBorder(new EtchedBorder(), MainDesktopPane.newEmptyBorder()));
		simulationPane.add(pulsePane, BorderLayout.SOUTH);

		pulsespersecondPane.add(pausePane, BorderLayout.SOUTH);

		StringBuilder s0 = new StringBuilder();
		double ratio = masterClock.getTimeRatio();
		String factor = String.format(Msg.getString("TimeWindow.timeFormat"), ratio); //$NON-NLS-1$
		s0.append(ONE_REAL_SEC);
		s0.append(masterClock.getTimeString(ratio));

		timeCompressionLabel = new JLabel(s0.toString(), JLabel.CENTER);
		pulsePane.add(timeCompressionLabel, BorderLayout.CENTER);

		// Create pulse header label
		JPanel northPanel = new JPanel(new GridLayout(2, 1));
		JLabel speedLabel = new JLabel(Msg.getString("TimeWindow.simSpeed"), JLabel.CENTER); //$NON-NLS-1$

		timeRatioLabel = new JLabel(Msg.getString("TimeWindow.timeRatioHeader", factor), JLabel.CENTER); //$NON-NLS-1$
		pulsePane.add(northPanel, BorderLayout.NORTH);

		northPanel.add(speedLabel);
		northPanel.add(timeRatioLabel);

		if (mainScene == null) {

			timeCompressionLabel.addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);

					StringBuilder s0 = new StringBuilder();
					double ratio = masterClock.getTimeRatio();
					String factor = String.format(Msg.getString("TimeWindow.timeFormat"), ratio); //$NON-NLS-1$
					s0.append(ONE_REAL_SEC);
					s0.append(masterClock.getTimeString(ratio));
					timeCompressionLabel.setText(s0.toString());

					timeRatioLabel.setText(Msg.getString("TimeWindow.timeRatioHeader", factor)); //$NON-NLS-1$

				}
			});


			// Create pulse slider
			int sliderpos = calculateSliderValue(masterClock.getTimeRatio());
			pulseSlider = new JSliderMW(1, 100, sliderpos);
			//pulseSlider.setEnabled(false);
			pulseSlider.setMajorTickSpacing(20);
			pulseSlider.setMinorTickSpacing(5);
			pulseSlider.setPaintTicks(true);
			pulseSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					try {

						setTimeRatioFromSlider(pulseSlider.getValue()); // (int)(mainScene.getTimeRatio()/mainScene.getInitialTimeRatio()))

						StringBuilder s0 = new StringBuilder();
						double ratio = masterClock.getTimeRatio();
						String factor = String.format(Msg.getString("TimeWindow.timeFormat"), ratio); //$NON-NLS-1$
						s0.append(ONE_REAL_SEC);
						s0.append(masterClock.getTimeString(ratio));
						timeCompressionLabel.setText(s0.toString());
						timeRatioLabel.setText(Msg.getString("TimeWindow.timeRatioHeader", factor)); //$NON-NLS-1$

					} catch (Exception e2) {
						logger.log(Level.SEVERE, e2.getMessage());
					}
				}
			});

			pulsePane.add(pulseSlider, BorderLayout.SOUTH);
			setTimeRatioSlider(masterClock.getTimeRatio());

		}

		// Pack window
		pack();

		// Add 10 pixels to packed window width
		Dimension windowSize = getSize();
		setSize(new Dimension((int) windowSize.getWidth() + 40, (int) windowSize.getHeight()));
	}

	/**
	 * Sets the time ratio for the simulation based on the slider value.
	 *
	 * @param sliderValue
	 *            the slider value (1 to 100).
	 */
	private void setTimeRatioFromSlider(int sliderValue) {
		double timeRatio = calculateTimeRatioFromSlider(sliderValue);
		// System.out.println("timeRatio : " + timeRatio);
		masterClock.setTimeRatio(timeRatio);
	}

	/**
	 * Calculates a time ratio given a slider value.
	 * @param sliderValue
	 *            the slider value from 1 to 100.
	 * @return time ratio value (simulation time / real time).
	 */
	public static double calculateTimeRatioFromSlider(int sliderValue) {

		double slope;
		double offset;
		double timeRatio;

		// sliderValue should be in the range 1..100 inclusive, if not it
		// defaults to
		// 1:15 real:sim ratio
		if ((sliderValue > 0) && (sliderValue <= 100)) {
			if (sliderValue >= (midslider + minslider)) {

				// Creates exponential curve between ratioatmid and maxratio.
				double a = ratioatmid;
				double b = maxratio / ratioatmid;
				double T = maxslider - midslider;
				double expo = (sliderValue - minslider - midslider) / T;
				timeRatio = a * Math.pow(b, expo);
			} else if (sliderValue >= minslider) {

				// Creates exponential curve between 1 and ratioatmid.
				double a = 1D;
				double b = ratioatmid;
				double T = midslider;
				double expo = (sliderValue - minslider) / T;
				timeRatio = a * Math.pow(b, expo);
			} else {
				// generates ratios < 1
				offset = minfracratio;
				slope = (maxfracratio - minfracratio) / (maxfracpos - minfracpos);
				timeRatio = (sliderValue - minfracpos) * slope + offset;
			}
		} else {
			timeRatio = 15D;
			throw new IllegalArgumentException(Msg.getString("TimeWindow.log.ratioError")); //$NON-NLS-1$
		}

		return timeRatio;
	}

	/**
	 * Moves the slider bar appropriately given the time ratio.
	 *
	 * @param timeRatio
	 *            the time ratio (simulation time / real time).
	 */
	public void setTimeRatioSlider(double timeRatio) {
		int sliderValue = calculateSliderValue(timeRatio);
		pulseSlider.setValue(sliderValue);
	}

	/**
	 * Calculates a slider value based on a time ratio. Note: This method is the
	 * inverse of calculateTimeRatioFromSlider.
	 *
	 * @param timeRatio
	 *            time ratio (simulation time / real time).
	 * @return slider value (1 to 100).
	 */
	public static int calculateSliderValue(double timeRatio) {

		int sliderValue = 1;

		// Moves the slider bar appropriately given the time ratio.
		if (timeRatio < minfracratio) {
			sliderValue = 1;
		} else if (timeRatio > maxratio) {
			sliderValue = 100;
		} else if ((timeRatio >= ratioatmid) && (timeRatio <= maxratio)) {
			double a = ratioatmid;
			double b = maxratio / ratioatmid;
			double T = maxslider - midslider;
			double temp1 = timeRatio / a;
			double expo = Math.log(temp1) / Math.log(b);
			double temp2 = (expo * T) + minslider + midslider;
			sliderValue = (int) Math.round(temp2);
		} else if ((timeRatio >= 1D) && (timeRatio <= ratioatmid)) {
			double a = 1D;
			double b = ratioatmid;
			double T = midslider;
			double temp1 = timeRatio / a;
			double expo = Math.log(temp1) / Math.log(b);
			double temp2 = (expo * T) + minslider;
			sliderValue = (int) Math.round(temp2);
		} else {
			double offset = minfracratio;
			double slope = (maxfracratio - minfracratio) / (maxfracpos - minfracpos);
			double temp1 = ((timeRatio - offset) / slope) + minfracpos;
			sliderValue = (int) Math.round(temp1);
		}

		return sliderValue;
	}

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
			//balloonToolTip.createBalloonTip(northernSeasonLabel, northernSeasonTip);// Msg.getString("TimeWindow.season.toolTip"));
																					// //$NON-NLS-1$
	   		TooltipManager.setTooltip (northernSeasonLabel, northernSeasonTip, TooltipWay.down);
		}

		if (!southernSeasonCache.equals(southernSeason)) {
			southernSeasonCache = southernSeason;

			if (marsTime.getSeason(MarsClock.SOUTHERN_HEMISPHERE) != null) {
				southernSeasonLabel.setText(Msg.getString("TimeWindow.southernHemisphere", //$NON-NLS-1$
						southernSeason));
			}

			southernSeasonTip = getSeasonTip(southernSeason);
			//balloonToolTip.createBalloonTip(southernSeasonLabel, southernSeasonTip);// Msg.getString("TimeWindow.season.toolTip"));
																					// //$NON-NLS-1$
	   		TooltipManager.setTooltip (southernSeasonLabel, southernSeasonTip, TooltipWay.down);
		}

	}

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

	@Override
	public void clockPulse(double time) {
		if (mainScene != null) {
			if (!mainScene.isMinimized() && mainScene.isMainTabOpen() && desktop.isToolWindowOpen(TimeWindow.NAME)) {			
				updateFXLabel();
			}
		}
		else if (desktop.isToolWindowOpen(TimeWindow.NAME)) {
			updateSwingLabel(time);
		}

	}
	
	public void updateFXLabel() {
		StringBuilder s0 = new StringBuilder();
		double ratio = masterClock.getTimeRatio();
		String factor = String.format(Msg.getString("TimeWindow.timeFormat"), ratio); //$NON-NLS-1$
		s0.append(ONE_REAL_SEC);
		s0.append(masterClock.getTimeString(ratio));
		if (timeRatioLabel != null) timeRatioLabel.setText(Msg.getString("TimeWindow.timeRatioHeader", factor)); //$NON-NLS-1$
		if (timeCompressionLabel != null) timeCompressionLabel.setText(s0.toString());
	}
	
	/**
	 * Updates date and time in Time Tool
	 *
	 * @param time
	 */
	public void updateSwingLabel(double time) {
		if (marsTime != null) {
			//SwingUtilities.invokeLater(() -> {
			String ts = marsTime.getDateTimeStamp();
				if (!ts.equals("  ") && ts != null && !ts.equals(""))
					if (martianTimeLabel != null)
						martianTimeLabel.setText(ts);
			//});
			int solElapsed = marsTime.getMissionSol();
			// 2015-02-24 Added solElapsedCache
			if (solElapsed != solElapsedCache) {
				//SwingUtilities.invokeLater(() -> {
				String mn = marsTime.getMonthName();
					if (mn != null)
						if (martianMonthLabel != null)
							martianMonthLabel.setText(mn);
				//});
				setSeason();

				solElapsedCache = solElapsed;
			}
		}

		if (earthTime != null) {
			String ts = earthTime.getTimeStampF0();
			if (ts != null)
				//SwingUtilities.invokeLater(() -> {
					//if (earthTime != null)
						earthTimeLabel.setText(ts);
				//});
		}

		if (masterClock != null) {
			DecimalFormat formatter = new DecimalFormat(Msg.getString("TimeWindow.decimalFormat")); //$NON-NLS-1$
			String pulsePerSecond = formatter.format(masterClock.getPulsesPerSecond());

			// String pulses = formatter.format(masterClock.getPulses());
			// pulsesLabel.setText(pulses);

			pulsespersecondLabel.setText(pulsePerSecond);
		}

		if (uptimer != null) {
			uptimeLabel.setText(uptimer.getUptime());
		}

		calendarDisplay.update();
	}

	// Called by Masterclock's firePauseChange() since TimeWindow is on
	// clocklistener
	// 2015-12-16 Revised pauseChange() to add getAutosaveTimeline().pause() or
	// .play()
	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// logger.info("TimeWindow : calling pauseChange()");
		// Update pause/resume button text based on master clock pause state.
		if (isPaused) {
			//if (showPane && mainScene != null && !masterClock.isSavingSimulation())
			//	mainScene.startPausePopup();
			pauseButton.setText("  " + Msg.getString("TimeWindow.button.resume") + "  "); //$NON-NLS-1$
			//desktop.getMarqueeTicker().pauseMarqueeTimer(true);
		
		} 
		else {
			pauseButton.setText("    " + Msg.getString("TimeWindow.button.pause") + "    "); //$NON-NLS-1$
			//desktop.getMarqueeTicker().pauseMarqueeTimer(false);
			//if (showPane && mainScene != null)
			//	mainScene.stopPausePopup();
		}
	}

	/**
	 * Enables/disables the pause button
	 *
	 * @param value
	 *            true or false
	 */
	// 2015-12-07 Added enablePauseButton()
	public void enablePauseButton(boolean value) {
		pauseButton.setEnabled(value);
		// Note : when a wizard or a dialog box is opened/close,
		// need to call below to remove/add the ability to use ESC to
		// unpause/pause
		if (!MainScene.isFXGL) 
			mainScene.setEscapeEventHandler(value, mainScene.getStage());
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