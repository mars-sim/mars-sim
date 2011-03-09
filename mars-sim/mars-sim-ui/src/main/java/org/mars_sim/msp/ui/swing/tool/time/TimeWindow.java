/**
 * Mars Simulation Project
 * TimeWindow.java
 * @version 2.85 2008-07-19
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
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * The TimeWindow is a tool window that displays the current 
 * Martian and Earth time 
 */
public class TimeWindow extends ToolWindow implements ClockListener {
    
    private static String CLASS_NAME = "org.mars_sim.msp.ui.standard.tool.time.TimeWindow";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);

	// Tool name
	public static final String NAME = "Time Tool";		
	
    //private final static int RATIO_SCALE = 10;

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
        
        final JButton pauseButton = new JButton("Pause");
        
        pauseButton.addMouseListener(new MouseInputListener() {
			@Override
			public void mouseMoved(MouseEvent e) {			}
			@Override
			public void mouseDragged(MouseEvent e) {		}
			@Override
			public void mouseReleased(MouseEvent e) { 
				if (e.getButton() == MouseEvent.BUTTON1) {
					master.setPaused(!master.isPaused());
					
					if (master.isPaused()) {pauseButton.setText("Resume");}
					else {pauseButton.setText("Pause");}
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {		}
			@Override
			public void mouseExited(MouseEvent e) {	}
			@Override
			public void mouseEntered(MouseEvent e) {	}
			@Override
			public void mouseClicked(MouseEvent e) {	}
		});
        
        // Create uptime header label
        JLabel uptimeHeaderLabel = new JLabel("Simulation Uptime", JLabel.CENTER);
        uptimePane.add(uptimeHeaderLabel, "North");

        JLabel pulsespersecondHeaderLabel = new JLabel("Ticks Per Second", JLabel.CENTER);
        pulsespersecondPane.add(pulsespersecondHeaderLabel, "North");

        // Create uptime label
        uptimeLabel = new JLabel(uptimer.getUptime(), JLabel.CENTER);
        uptimePane.add(uptimeLabel, "Center");

        pulsespersecondLabel = new JLabel(uptimer.getUptime(), JLabel.CENTER);
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
			master.setTimeRatio(sliderpos);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        //String s = String.format("1 : %5.3f : %5.3f", master.getTimeRatio(),
    	//		MarsClock.convertSecondsToMillisols(master.getTimeRatio()) ).toString() ;
        String s =master.getTimeString(master.getTimeRatio());
        final JLabel pulseCurRatioLabel = new JLabel(s, JLabel.CENTER);
        pulsePane.add(pulseCurRatioLabel, "Center");

        pulseCurRatioLabel.addMouseListener(new MouseInputAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		// TODO Auto-generated method stub
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
                    	master.setTimeRatio(pulseSlider.getValue());
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
    
    public void setTimeRatioSlider(int r)
    {
    	//moves the slider bar appropriately given the ratio 
    	if (r>=pulseSlider.getMinimum() && r <= pulseSlider.getMaximum()) 
    		{pulseSlider.setValue(r);}
    }
	/**
	 * Change in time.
	 * param time the amount of time changed. (millisols)
	 */
	public void clockPulse(double time) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    if (master != null) {
			        martianTimeLabel.setText(marsTime.getTimeStamp());
			        martianMonthLabel.setText(marsTime.getMonthName());
			        earthTimeLabel.setText(earthTime.getTimeStamp());
	                uptimeLabel.setText(uptimer.getUptime());
	                
	                northernSeasonLabel.setText("Northern Hemisphere: " + 
	                        marsTime.getSeason(MarsClock.NORTHERN_HEMISPHERE));
	                southernSeasonLabel.setText("Southern Hemisphere: " + 
	                        marsTime.getSeason(MarsClock.SOUTHERN_HEMISPHERE));
	                
	                calendarDisplay.update();
	                String s = String.format("%8.4f",master.getPulsesPerSecond());
	                pulsespersecondLabel.setText(s);
			    }
			}
		});
	}
    
    /**
     * Prepare tool window for deletion.
     */
    public void destroy() {
    	master.removeClockListener(this);
    	master = null;
    	marsTime = null;
    	earthTime = null;
    	uptimer = null;
    }
}