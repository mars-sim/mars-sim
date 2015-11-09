/**
 * Mars Simulation Project
 * SettlementWindow.java
 * @version 3.07 2015-02-04
 * @author Lars Naesbye Christensen
 */
package org.mars_sim.msp.ui.swing.tool.settlement;


import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.AngledLinesWindowsCornerIcon;
import org.mars_sim.msp.ui.swing.tool.BalloonToolTip;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.tool.MarqueeTicker;
import org.mars_sim.msp.ui.swing.tool.MarqueeWindow;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

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

	public static final int TIME_DELAY = 1000;

	public static final int HORIZONTAL = 800;
	public static final int VERTICAL = 600;

    private String statusText;
    private String populationText;
	private String marsDateString, marsTimeString;

    private JStatusBar statusBar;
    private JLabel solLabel, popLabel;
    private JLabel timeLabel, dateLabel;
    private JPanel subPanel;

	/** The main desktop. */
	private MainDesktopPane desktop;
	/** Map panel. */
	private SettlementMapPanel mapPanel;

	private MarsClock marsClock;
	private javax.swing.Timer marsTimer = null;

	private BalloonToolTip balloonToolTip;

	private MarqueeTicker marqueeTicker;

	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 */
	public SettlementWindow(MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, desktop);
		this.desktop = desktop;

        balloonToolTip = new BalloonToolTip();

        init();

		showMarsTime();
	}

	// 2015-02-04 Added init()
	public void init() {

		//setMaximizable(true); // not compatible with day night map layer
		setResizable(true);

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		// 2014-12-27 Added preferred size and initial location
		setPreferredSize(new Dimension(HORIZONTAL, VERTICAL));
		setMaximizable(true);
//		setMaximumSize(new Dimension(HORIZONTAL, VERTICAL));
		setLocation(600,600);

	    //getRootPane().setOpaque(false);
	    //getRootPane().setBackground(new Color(0,0,0,128));

		setBackground(Color.BLACK);
	    //setOpaque(false);
	    //setBackground(new Color(0,0,0,128));

		JPanel mainPanel = new JPanel(new BorderLayout());
	    //mainPanel.setOpaque(false);
	    //mainPanel.setBackground(new Color(0,0,0,128));

		setContentPane(mainPanel);

		subPanel = new JPanel(new BorderLayout());
	    //subPanel.setOpaque(false);
	    //subPanel.setBackground(new Color(0,0,0,128));
	    subPanel.setBackground(Color.BLACK);
	    mainPanel.add(subPanel, BorderLayout.CENTER);

		mapPanel = new SettlementMapPanel(desktop, this);
		//mainPanel.add(mapPanel, BorderLayout.CENTER);
		subPanel.add(mapPanel, BorderLayout.CENTER);

		// 2015-10-24 Create MarqueeTicker
		marqueeTicker = new MarqueeTicker(this);
		marqueeTicker.setBackground(Color.BLACK);
		subPanel.add(marqueeTicker, BorderLayout.SOUTH);

		// 2015-01-07 Added statusBar
        statusBar = new JStatusBar();
        solLabel = new JLabel();
        popLabel = new JLabel();  //statusText + populationText;

        statusBar.setLeftComponent(solLabel, true);
        statusBar.setLeftComponent(popLabel, false);

        dateLabel = new JLabel();
        timeLabel = new JLabel();
        balloonToolTip.createBalloonTip(timeLabel, Msg.getString("SettlementWindow.timeLabel.tooltip")); //$NON-NLS-1$
        balloonToolTip.createBalloonTip(dateLabel, Msg.getString("SettlementWindow.dateLabel.tooltip")); //$NON-NLS-1$
        //timeLabel.setHorizontalAlignment(JLabel.CENTER);

        statusBar.addRightComponent(dateLabel, false);
        statusBar.addRightComponent(timeLabel, false);

        //statusBar.addRightComponent(new JLabel(new AngledLinesWindowsCornerIcon()), true);

        //subPanel
        mainPanel.add(statusBar, BorderLayout.SOUTH);

		pack();
		setVisible(true);

	}

	// 2015-02-05 Added showMarsTime()
	public void showMarsTime() {
		// 2015-01-07 Added Martian Time on status bar
		ActionListener timeListener = null;
		if (timeListener == null) {
			timeListener = new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent evt) {
			    	marsClock = Simulation.instance().getMasterClock().getMarsClock();
			    	marsDateString = marsClock.getDateString();
			    	marsTimeString = marsClock.getTimeString(); //getMillisolString(marsClock);
			    	// For now, we denoted Martian Time in UMST as in Mars Climate Database Time. It's given as Local True Solar Time at longitude 0, LTST0
			    	// see http://www-mars.lmd.jussieu.fr/mars/time/solar_longitude.html
					dateLabel.setText("Martian Date : " + marsDateString + " ");
					timeLabel.setText("Time : " + marsTimeString + " millisols (UMST)");
					statusText = "" + MarsClock.getTotalSol(marsClock);
				    populationText = mapPanel.getSettlement().getAllAssociatedPeople().size() + " of " + mapPanel.getSettlement().getPopulationCapacity();
				    // 2015-02-09 Added leftLabel
				    solLabel.setText("Sol : " + statusText);
				    popLabel.setText("Population : " + populationText);
			    }
			};
		}
    	if (marsTimer == null) {
    		marsTimer = new javax.swing.Timer(TIME_DELAY, timeListener);
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

	public MarqueeTicker getMarqueeTicker() {
		return marqueeTicker;
	}

/*
	public void paintComponent(Graphics g){
	    super.paintComponent(g);
	    g.setColor(Color.BLACK);
	    g.fillRect(subPanel.getX(), subPanel.getY(), subPanel.getWidth(), subPanel.getHeight());
	    subPanel.draw(g);
	}
*/

	@Override
	public void destroy() {
		mapPanel.destroy();
		mapPanel = null;
		marsTimer.stop();
		marsTimer = null;
		desktop = null;

	}

}