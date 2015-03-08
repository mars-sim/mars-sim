/**
 * Mars Simulation Project
 * SettlementWindow.java
 * @version 3.07 2015-02-04
 * @author Lars Naesbye Christensen
 */
package org.mars_sim.msp.ui.swing.tool.settlement;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.sound.AngledLinesWindowsCornerIcon;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

/**
 * The SettlementWindow is a tool window that displays the Settlement Map Tool.
 */
public class SettlementWindow 
extends ToolWindow {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	//private static Logger logger = Logger.getLogger(SettlementWindow.class.getName());
	
	/** Tool name. */
	public static final String NAME = Msg.getString("SettlementWindow.title"); //$NON-NLS-1$

	/** The main desktop. */
	private MainDesktopPane desktop;
	/** Map panel. */
	private SettlementMapPanel mapPanel;

	private MarsClock marsClock;
	private javax.swing.Timer marsTimer = null;
	
    private JStatusBar statusBar;
    private JLabel leftLabel;
    //private JLabel maxMemLabel;
    //private JLabel memUsedLabel;
    //private JLabel dateLabel;
    private JLabel timeLabel;
    //private int maxMem;
    //private int memAV;
    //private int memUsed;
    private String statusText;
    private String populationText;
	private String marsTimeString;
	
    
	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 */
	public SettlementWindow(MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, desktop);
		this.desktop = desktop;	
		
		//SwingUtilities.invokeLater(new Runnable() {
        //    @Override
        //    public void run() {
        		init();
        //    }
        //});

		showMarsTime();
	}

	// 2015-02-04 Added init()
	public void init() {

		setMaximizable(true);
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		// 2014-12-27 Added preferred size and initial location
		setPreferredSize(new Dimension(800, 600));
		setLocation(600,600);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);
		
		mapPanel = new SettlementMapPanel(desktop, this); 
		mainPanel.add(mapPanel, BorderLayout.CENTER);

		// 2015-01-07 Added statusBar
        statusBar = new JStatusBar();
        leftLabel = new JLabel();  //statusText + populationText;
		statusBar.setLeftComponent(leftLabel);

        timeLabel = new JLabel();
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        statusBar.addRightComponent(timeLabel, false);

        statusBar.addRightComponent(new JLabel(new AngledLinesWindowsCornerIcon()), true);
   
        mainPanel.add(statusBar, BorderLayout.SOUTH);	   

		pack();
		setVisible(true);
		
	}
	
	// 2015-02-05 Added showMarsTime()
	public void showMarsTime() {
		// 2015-01-07 Added Martian Time on status bar 
		int timeDelay = 900;
		ActionListener timeListener = null;
		if (timeListener == null) {
			timeListener = new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent evt) {
			    	marsClock = Simulation.instance().getMasterClock().getMarsClock();
			    	marsTimeString = marsClock.getTimeStamp();
					timeLabel.setText("Martian Time : " + marsTimeString);
				    statusText = "" + MarsClock.getTotalSol(marsClock);
				    populationText = mapPanel.getSettlement().getAllAssociatedPeople().size() + "  of  " + mapPanel.getSettlement().getPopulationCapacity();
				    // 2015-02-09 Added leftLabel
				    leftLabel.setText("Sol :  " + statusText + "      Population :  " + populationText);
			    }
			};
		}
    	if (marsTimer == null) {
    		marsTimer = new javax.swing.Timer(timeDelay, timeListener);
    		marsTimer.start();
    	}
	}
	
	/**
	 * Gets the settlement map panel.
	 * @return the settlement map panel.
	 */
	public SettlementMapPanel getMapPanel() {
		return mapPanel;
	}

	/**
	 * Gets the main desktop panel for this tool.
	 * @return main desktop panel.
	 */
	public MainDesktopPane getDesktop() {
		return desktop;
	}

	@Override
	public void destroy() {
		mapPanel.destroy();
		mapPanel = null;
		marsTimer.stop();
		marsTimer = null;
		desktop = null;

	}

}