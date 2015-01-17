/**
 * Mars Simulation Project
 * SettlementWindow.java
 * @version 3.07 2015-01-16
 * @author Lars Naesbye Christensen
 */
package org.mars_sim.msp.ui.swing.tool.settlement;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.sound.AngledLinesWindowsCornerIcon;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

/**
 * The SettlementWindow is a tool window that displays the Settlement Map Tool.
 */
public class SettlementWindow extends ToolWindow {

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
	private String marsTimeString;
	private javax.swing.Timer marsTimer = null;
    
	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 */
	public SettlementWindow(final MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, desktop);
		this.desktop = desktop;	
		//final SettlementWindow settlementWindow = this;
	 
		setMaximizable(true);

		// 2014-12-27 Added preferred size and initial location
		setPreferredSize(new Dimension(800, 600));
		setLocation(600,600);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);
		
		mapPanel = new SettlementMapPanel(desktop, this); 
		mainPanel.add(mapPanel, BorderLayout.CENTER);

		// 2015-01-07 Added statusBar
        statusBar = new JStatusBar();
        //statusText = "News Today on the Settlement";
        leftLabel = new JLabel(statusText);
		statusBar.setLeftComponent(leftLabel);

        timeLabel = new JLabel();
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        statusBar.addRightComponent(timeLabel, false);

        statusBar.addRightComponent(new JLabel(new AngledLinesWindowsCornerIcon()), true);
   
        mainPanel.add(statusBar, BorderLayout.SOUTH);	   
		
		// 2015-01-07 Added Martian Time on status bar 
		int timeDelay = 900;
		ActionListener timeListener = null;
		if (timeListener == null) {
			timeListener = new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent evt) {
			    	marsTimeString = Simulation.instance().getMasterClock().getMarsClock().getTimeStamp();
					timeLabel.setText("Martian Time: " + marsTimeString);
			    }
			};
		}
    	if (marsTimer == null) {
    		marsTimer = new javax.swing.Timer(timeDelay, timeListener);
    		marsTimer.start();
    	}
    	
		pack();
		setVisible(true);
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