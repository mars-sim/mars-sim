/**
 * Mars Simulation Project
 * TimeWindow.java
 * @version 3.03 2012-09-30
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.time;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.*;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * The TimeWindow is a tool window that displays the current 
 * Martian and Earth time 
 */
public class TimeWindow extends ToolWindow implements ClockListener {
	
    private static Logger logger = Logger.getLogger(TimeWindow.class.getName());

	// Tool name
	public static final String NAME = "Time Tool";		
	
    /*
     * the numbers below have been tweaked with some care. At 20, the realworld:sim ratio is 1:1
     * above 20, the numbers start climbing logarithmically maxing out at around 100K this is really fast
     * Below 20, the simulation goes in slow motion, 1:0.0004 is around the slowest. The increments may be
     * so small at this point that events can't progress at all. When run too quickly, lots of accidents occur,
     * and lots of settlers die.
     */
	// the "default" ratio that will be set at 50, the middle of the scale
	private static final double ratioatmid = 1000.0D;
	
	// the max ratio the sim can be set at
    private static final double maxratio = 10800.0D;
    
    // the minimum ratio the sim can be set at
    private static final double minfracratio = 0.001D;
    
    // the largest fractional ratio the sim can be set at
    private static final double maxfracratio = 0.98D;

    // don't recommend changing these:
    private static final double minslider = 20.0D;
    private static final double midslider = (50.0D - minslider);
    private static final double maxslider = 100D - minslider;
    private static final double minfracpos = 1D;
    private static final double maxfracpos = minslider - 1D;

    // Data members
    private MasterClock master;      // Master Clock
    private MarsClock marsTime;      // Martian Clock
    private EarthClock earthTime;    // Earth Clock
    private UpTimer uptimer;         // Uptime Timer
    private MarsCalendarDisplay calendarDisplay;  // Martian calendar panel
    private JLabel martianTimeLabel; // JLabel for Martian time
    private JLabel martianMonthLabel; // JLabel for Martian month
    private JLabel northernSeasonLabel; // JLabel for Northern hemisphere season
    private JLabel southernSeasonLabel; // JLabel for Southern hemisphere season
    private JLabel earthTimeLabel;   // JLabel for Earth time
    private JLabel uptimeLabel;      // JLabel for uptimer
    private JLabel pulsespersecondLabel;      // JLabel for pulses per second label 
    private JSlider pulseSlider;     // JSlider for pulse
    private int sliderpos = 50;
    private JButton pauseButton;
    
    /** Constructs a TimeWindow object 
     *  @param desktop the desktop pane
     */
    public TimeWindow(MainDesktopPane desktop) {

        // Use TimeWindow constructor
        super(NAME, desktop);

        // Set window resizable to false.
        setResizable(false);
        
        // Initialize data members
        master = Simulation.instance().getMasterClock();
        master.addClockListener(this);
        marsTime = master.getMarsClock();
        earthTime = master.getEarthClock();
        uptimer = master.getUpTimer(); 

        // Get content pane
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new MarsPanelBorder());
        setContentPane(mainPane);

        // Create Martian time panel
        JPanel martianTimePane = new JPanel(new BorderLayout());
        martianTimePane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        mainPane.add(martianTimePane, "North");

        // Create Martian time header label
        JLabel martianTimeHeaderLabel = new JLabel("Martian Time", JLabel.CENTER);
        martianTimePane.add(martianTimeHeaderLabel, "North");

        // Create Martian time label
        martianTimeLabel = new JLabel(marsTime.getTimeStamp(), JLabel.CENTER);
        martianTimePane.add(martianTimeLabel, "South");

        // Create Martian calendar panel
        JPanel martianCalendarPane = new JPanel(new FlowLayout());
        martianCalendarPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        mainPane.add(martianCalendarPane, "Center");

        // Create Martian calendar month panel
        JPanel calendarMonthPane = new JPanel(new BorderLayout());
        martianCalendarPane.add(calendarMonthPane);

        // Create martian month label
        martianMonthLabel = new JLabel(marsTime.getMonthName(), JLabel.CENTER);
        calendarMonthPane.add(martianMonthLabel, "North");

        // Create Martian calendar display
        calendarDisplay = new MarsCalendarDisplay(marsTime);
        JPanel innerCalendarPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        innerCalendarPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
        innerCalendarPane.add(calendarDisplay);
        calendarMonthPane.add(innerCalendarPane, "Center");        

        JPanel southPane = new JPanel(new BorderLayout());
        mainPane.add(southPane, "South");

        JPanel simulationPane = new JPanel(new BorderLayout());
        southPane.add(simulationPane, "South");

        // Create Martian season panel
        JPanel marsSeasonPane = new JPanel(new BorderLayout());
        marsSeasonPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        southPane.add(marsSeasonPane, "North");

        // Create Martian season label
        JLabel marsSeasonLabel = new JLabel("Martian Seasons", JLabel.CENTER);
        marsSeasonPane.add(marsSeasonLabel, "North");

        // Create Northern season label
        northernSeasonLabel = new JLabel("Northern Hemisphere: " + marsTime.getSeason(MarsClock.NORTHERN_HEMISPHERE), JLabel.CENTER);
        marsSeasonPane.add(northernSeasonLabel, "Center");
 
        // Create Southern season label
        southernSeasonLabel = new JLabel("Southern Hemisphere: " + marsTime.getSeason(MarsClock.SOUTHERN_HEMISPHERE), JLabel.CENTER);
        marsSeasonPane.add(southernSeasonLabel, "South");

        // Create Earth time panel
        JPanel earthTimePane = new JPanel(new BorderLayout());
        earthTimePane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        southPane.add(earthTimePane, "Center");

        // Create Earth time header label
        JLabel earthTimeHeaderLabel = new JLabel("Earth Time", JLabel.CENTER);
        earthTimePane.add(earthTimeHeaderLabel, "North");

        // Create Earth time label
        earthTimeLabel = new JLabel(earthTime.getTimeStamp(), JLabel.CENTER);
        earthTimePane.add(earthTimeLabel, "South");

        // Create uptime panel
        JPanel uptimePane = new JPanel(new BorderLayout());
        uptimePane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        simulationPane.add(uptimePane, "North");

        JPanel pulsespersecondPane = new JPanel(new BorderLayout());
        pulsespersecondPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        uptimePane.add(pulsespersecondPane, "South");

        JPanel pausePane = new JPanel( new BorderLayout());
        southPane.add(pausePane, "North");
        
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                master.setPaused(!master.isPaused());
            }
        });
        
        // Create uptime header label
        JLabel uptimeHeaderLabel = new JLabel("Simulation Uptime", JLabel.CENTER);
        uptimePane.add(uptimeHeaderLabel, "North");

        JLabel pulsespersecondHeaderLabel = new JLabel("Ticks Per Second", JLabel.CENTER);
        pulsespersecondPane.add(pulsespersecondHeaderLabel, "North");

        // Create uptime label
        uptimeLabel = new JLabel(uptimer.getUptime(), JLabel.CENTER);
        uptimePane.add(uptimeLabel, "Center");

        DecimalFormat formatter = new DecimalFormat("0.00");
        String pulsePerSecond = formatter.format(master.getPulsesPerSecond());
        pulsespersecondLabel = new JLabel(pulsePerSecond, JLabel.CENTER);
        pulsespersecondPane.add(pulsespersecondLabel, "Center");

        // Create uptime panel
        JPanel pulsePane = new JPanel(new BorderLayout());
        pulsePane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        simulationPane.add(pulsePane, "South");

        // Create pulse header label
        final JLabel pulseHeaderLabel = new JLabel("1 realsec : sim Time", JLabel.CENTER);
        pulsePane.add(pulseHeaderLabel, "North");
        
        pulsespersecondPane.add(pauseButton, "South");
        
        // create time ratio readout showing real / earth / mars time ratios currently set
        try {
            setTimeRatioFromSlider(sliderpos);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
        //String s = String.format("1 : %5.3f : %5.3f", master.getTimeRatio(),
    	//		MarsClock.convertSecondsToMillisols(master.getTimeRatio()) ).toString() ;
        String s = master.getTimeString(master.getTimeRatio());
        final JLabel pulseCurRatioLabel = new JLabel(s, JLabel.CENTER);
        pulsePane.add(pulseCurRatioLabel, "Center");

        pulseCurRatioLabel.addMouseListener(new MouseInputAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		super.mouseClicked(e);
        		if (pulseCurRatioLabel.getText().contains(":")) {
            		pulseCurRatioLabel.setText(String.format("%8.4f", master.getTimeRatio() ) );
        		} else {
            		pulseCurRatioLabel.setText(master.getTimeString(master.getTimeRatio()) );
        		}
        	}
		});
        
        // Create pulse slider
        pulseSlider = new JSlider(1, 100, sliderpos);
        pulseSlider.setMajorTickSpacing(10);
        pulseSlider.setMinorTickSpacing(2);
        pulseSlider.setPaintTicks(true);
        pulseSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    try {
                        setTimeRatioFromSlider(pulseSlider.getValue());
                    	if (pulseCurRatioLabel.getText().contains(":") ) 
                    	{pulseCurRatioLabel.setText(master.getTimeString(master.getTimeRatio()));}
                    	else 
                    	{pulseCurRatioLabel.setText(String.format("%8.4f", master.getTimeRatio() ) );}
                    }
                    catch (Exception e2) {
                    	logger.log(Level.SEVERE,e2.getMessage());
                    }
                    
                }
        });
        pulsePane.add(pulseSlider, "South");
        
        
        // Pack window
        pack();

        // Add 10 pixels to packed window width
        Dimension windowSize = getSize();
        setSize(new Dimension((int)windowSize.getWidth() + 10, (int) windowSize.getHeight()));
    }
    
    /**
     * Sets the time ratio for the simulation based on the slider value.
     */
    private void setTimeRatioFromSlider(int sliderValue) {
        double slope; 
        double offset;
        double timeRatio;
        
        // sliderValue should be in the range 1..100 inclusive, if not it defaults to
        // 1:15 real:sim ratio
        if ((sliderValue > 0) && (sliderValue <= 100)) {
            if (sliderValue >= (midslider + minslider)) {
                
                // Creates exponential curve between ratioatmid and maxratio.
                double a = ratioatmid;
                double b = maxratio / ratioatmid;
                double T = maxslider - midslider;
                double expo = (sliderValue - minslider - midslider) / T;
                timeRatio = a * Math.pow(b, expo);
            }
            else if (sliderValue >= minslider) {
                
                // Creates exponential curve between 1 and ratioatmid.
                double a = 1D;
                double b = ratioatmid;
                double T = midslider;
                double expo = (sliderValue - minslider) / T;
                timeRatio = a * Math.pow(b, expo);
            } 
            else {
                // generates ratios < 1
                offset = minfracratio;
                slope = (maxfracratio - minfracratio) / (maxfracpos - minfracpos);
                timeRatio = (sliderValue - minfracpos) * slope + offset;
            }
        }
        else {
            timeRatio = 15D;
            throw new IllegalArgumentException("Time ratio should be in 1..100");
        }
        
        master.setTimeRatio(timeRatio);
    }
    
    public void setTimeRatioSlider(int r) {
    	//moves the slider bar appropriately given the ratio 
    	if (r>=pulseSlider.getMinimum() && r <= pulseSlider.getMaximum()) 
    		{pulseSlider.setValue(r);}
    }

    @Override
	public void clockPulse(double time) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    
			    if (marsTime != null) {
			        martianTimeLabel.setText(marsTime.getTimeStamp());
                    martianMonthLabel.setText(marsTime.getMonthName());
                    northernSeasonLabel.setText("Northern Hemisphere: " + 
                            marsTime.getSeason(MarsClock.NORTHERN_HEMISPHERE));
                    southernSeasonLabel.setText("Southern Hemisphere: " + 
                            marsTime.getSeason(MarsClock.SOUTHERN_HEMISPHERE));
			    }
			    
			    if (earthTime != null) {
			        earthTimeLabel.setText(earthTime.getTimeStamp());
			    }
			    
			    if (master != null) {
			        DecimalFormat formatter = new DecimalFormat("0.00");
			        String pulsePerSecond = formatter.format(master.getPulsesPerSecond());
			        pulsespersecondLabel.setText(pulsePerSecond);
			    }
			    
			    if (uptimer != null) {
			        uptimeLabel.setText(uptimer.getUptime());
			    }
			    
			    calendarDisplay.update();
			}
		});
	}
	
    @Override
    public void pauseChange(boolean isPaused) {
        // Update pause/resume button text based on master clock pause state.
        if (isPaused) {
            pauseButton.setText("Resume");
        }
        else {
            pauseButton.setText("Pause");
        }
    }
    
    /**
     * Prepare tool window for deletion.
     */
    public void destroy() {
        if (master != null) {
            master.removeClockListener(this);
        }
    	master = null;
    	marsTime = null;
    	earthTime = null;
    	uptimer = null;
    }
}