/**
 * Mars Simulation Project
 * SettlementWindow.java
 * @version 3.1.0 2017-11-04
 * @author Lars Naesbye Christensen
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLayer;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import org.controlsfx.control.StatusBar;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import org.mars_sim.msp.ui.swing.tool.SpotlightLayerUI;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import com.alee.laf.desktoppane.WebDesktopPane;
import com.alee.laf.panel.WebPanel;

import javafx.scene.control.Label;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;


/**
 * The SettlementWindow is a tool window that displays the Settlement Map Tool.
 */
public class SettlementWindow
extends ToolWindow {

	// default logger.
	//private static Logger logger = Logger.getLogger(SettlementWindow.class.getName());

	/** Tool name. */
	public static final String NAME = Msg.getString("SettlementWindow.title"); //$NON-NLS-1$

	public static String css_file = MainDesktopPane.BLUE_CSS;

	public static final String SOL  = " Sol : ";
	public static final String POPULATION  = "  Population : ";
	public static final String CAP  = "  Capacity : ";
	public static final String POINTER = "\t\t\tPointer at (";
	public static final String COMMA = ", ";
	public static final String CLOSE_PARENT = ")";

	//public static final String MILLISOLS_UMST = " millisols (UMST) ";

	public static final int TIME_DELAY = 330;
	public static final int HORIZONTAL = 800;//630;
	public static final int VERTICAL = 600;//590;

	private int sol;
	private int cap;
	private int pop;
	private int themeCache = -1;

	private double widthCache;
	private double heightCache;
	private double xCoor, yCoor;

	private DoubleProperty width = new SimpleDoubleProperty(HORIZONTAL);
	private DoubleProperty height = new SimpleDoubleProperty(VERTICAL);

    private String marsDateString;
    private String marsTimeString;

    private boolean isBound;

    //private JStatusBar statusBar;
    private Label solLabel, popLabel, capLabel, xyLabel, timeLabel, dateLabel;
    private WebPanel subPanel;

	/** The main desktop. */
	private MainDesktopPane desktop;
	private MainScene mainScene;
	/** Map panel. */
	private SettlementMapPanel mapPanel;

	private static MarsClock marsClock;
	//private javax.swing.Timer marsTimer = null;

//	private MarqueeTicker marqueeTicker;

	private JFXPanel jfxPanel;
	private Scene scene;
	private StackPane stack;
	private StatusBar statusBar;
	private Timeline timeline;

	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 */
	public SettlementWindow(MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, desktop);
		this.desktop = desktop;
		mainScene = desktop.getMainScene();

    	if (marsClock == null)
    		marsClock = Simulation.instance().getMasterClock().getMarsClock();

        init();

		//showMarsTime();
	}

	public void init() {

		if (mainScene != null) {
			//setTitleName(null);
			// Remove title bar
		    putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
		    getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		    BasicInternalFrameUI bi = (BasicInternalFrameUI)super.getUI();
		    bi.setNorthPane(null);
		    setBorder(null);
		}

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

	    //getRootPane().setOpaque(false);
	    //getRootPane().setBackground(new Color(0,0,0,128));

		setBackground(java.awt.Color.BLACK);
	    //setOpaque(false);
	    //setBackground(new Color(0,0,0,128));

		WebPanel mainPanel = new WebPanel(new BorderLayout());
	    //mainPanel.setOpaque(false);
	    //mainPanel.setBackground(new Color(0,0,0,128));
		setContentPane(mainPanel);

		subPanel = new WebPanel(new BorderLayout());
	    mainPanel.add(subPanel, BorderLayout.CENTER);
	    //subPanel.setOpaque(false);
	    //subPanel.setBackground(new Color(0,0,0,128));
	    subPanel.setBackground(java.awt.Color.BLACK);

		mapPanel = new SettlementMapPanel(desktop, this);

		// Added SpotlightLayerUI
		LayerUI<WebPanel> layerUI = new SpotlightLayerUI(mapPanel);
		JLayer<WebPanel> jlayer = new JLayer<WebPanel>(mapPanel, layerUI);
    	subPanel.add(jlayer, BorderLayout.CENTER);
    	//subPanel.add(mapPanel, BorderLayout.CENTER);

 /*
		// 2015-01-07 Added statusBar
        statusBar = new JStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        solLabel = new JLabel();
        popLabel = new JLabel();  //statusText + populationText;
	    //xLabel = new JLabel();//.setText("x : " + xCoor);
	    //yLabel = new JLabel();//.setText("y : " + yCoor);

        statusBar.setLeftComponent(solLabel, true);
        statusBar.setLeftComponent(popLabel, true);
        //statusBar.setLeftComponent(xLabel, false);
        //statusBar.setLeftComponent(yLabel, true);

        dateLabel = new JLabel();
        timeLabel = new JLabel();
        balloonToolTip.createBalloonTip(timeLabel, Msg.getString("SettlementWindow.timeLabel.tooltip")); //$NON-NLS-1$
        balloonToolTip.createBalloonTip(dateLabel, Msg.getString("SettlementWindow.dateLabel.tooltip")); //$NON-NLS-1$
        //timeLabel.setHorizontalAlignment(JLabel.CENTER);
        statusBar.addRightComponent(dateLabel, false);
        statusBar.addRightComponent(timeLabel, false);
        //statusBar.addRightComponent(new JLabel(new AngledLinesWindowsCornerIcon()), true);
*/
    	// TODO: use RichTextFX for javaFX mode
    	// https://github.com/TomasMikula/RichTextFX/wiki/RichTextFX-CSS-Reference-Guide

    	// 2015-10-24 Create MarqueeTicker
//		marqueeTicker = new MarqueeTicker(this);
//		//marqueeTicker.setBackground(Color.BLACK);
//    	subPanel.add(marqueeTicker, BorderLayout.SOUTH);

		jfxPanel = new JFXPanel();

        Platform.runLater(new Runnable(){
            @Override
            public void run() {

                stack = new StackPane();
                stack.setStyle(
             		   "-fx-border-style: 2px; "
             		   //"-fx-background-color: #231d12; "
                			+ "-fx-background-color: transparent; "
                			+ "-fx-background-radius: 2px;"
             		   );

                solLabel = new Label();
                popLabel = new Label();
                capLabel = new Label();
        	    xyLabel = new Label();
        		timeLabel = new Label();

        		// Create ControlFX's StatusBar
        		statusBar = createStatusBar();
            	startMarsTimer();

                stack.getChildren().add(statusBar);

                scene = new Scene(stack, HORIZONTAL, 30);//mainPanel.getWidth(), mainPanel.getHeight());

                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);//.BLACK);
                jfxPanel.setScene(scene);

            }
        });

        mainPanel.add(jfxPanel, BorderLayout.SOUTH);

		if (mainScene != null) {
			//setSize(new Dimension((int)width.get(), (int)height.get()));
			setPreferredSize(new Dimension(mainScene.getWidth(), mainScene.getHeight() - MainScene.TITLE_BAR_HEIGHT));
			//setMinimumSize(new Dimension(mainScene.getHORIZONTAL/2, VERTICAL/2));
			setClosable(false);
			setResizable(false);
			setMaximizable(true);
		}
		else {
			setSize(new Dimension(HORIZONTAL, VERTICAL));
			setPreferredSize(new Dimension(HORIZONTAL, VERTICAL));
			setMinimumSize(new Dimension(HORIZONTAL/2, VERTICAL/2));
			setClosable(true);
			setResizable(false);
			setMaximizable(true);
		}

		setVisible(true);

		pack();

	}


	/*
	 * Creates the status bar for MainScene
	 */
	@SuppressWarnings("restriction")
	public StatusBar createStatusBar() {
		if (statusBar == null) {
			statusBar = new StatusBar();
			statusBar.setId("status-bar");
			statusBar.setText("");
			setTheme(null);
			//setStatusBarTheme(cssFile);
			
		}

    	if (marsClock == null)
    		marsClock = Simulation.instance().getMasterClock().getMarsClock();

    	sol = marsClock.getMissionSol();
    	pop = mapPanel.getSettlement().getAllAssociatedPeople().size();
    	cap = mapPanel.getSettlement().getPopulationCapacity();

		String statusText = "" + sol;
		String populationText = "" + pop;
		String capText = "" + cap;

	    // 2015-02-09 Added leftLabel
	    solLabel.setText(SOL+ statusText);
	    popLabel.setText(POPULATION + populationText);
	    capLabel.setText(CAP + capText);
	    xyLabel.setText(POINTER + xCoor + COMMA + yCoor + CLOSE_PARENT);
	    //yLabel.setText(", " + yCoor + ")" + " ");

	    if (mainScene != null) {
			//solLabel.setTooltip(new Tooltip ("Mission Day"));
			mainScene.setQuickToolTip(solLabel, "# of days since the start of mission");
			//popLabel.setTooltip(new Tooltip ("Population of this Settlement"));
			mainScene.setQuickToolTip(popLabel, "the current population of this settlement");
			//capLabel.setTooltip(new Tooltip ("Max Number of Beds/Quarters in this Settlement"));
			mainScene.setQuickToolTip(capLabel, "the max # of beds/quarters for this Settlement");
			//xyLabel.setTooltip(new Tooltip ("x and y meters from center of a Building (Updated when Right-Click inside)"));
			mainScene.setQuickToolTip(xyLabel, "x and y meters from the center of a building (Note: right-click inside to update)");
	    }
	    
		//statusBar.getLeftItems().add(new Separator(javafx.geometry.Orientation.VERTICAL));
		statusBar.getRightItems().add(solLabel);
		statusBar.getRightItems().add(new Separator(javafx.geometry.Orientation.VERTICAL));

		statusBar.getRightItems().add(popLabel);
		statusBar.getRightItems().add(new Separator(javafx.geometry.Orientation.VERTICAL));

		statusBar.getRightItems().add(capLabel);
		statusBar.getRightItems().add(new Separator(javafx.geometry.Orientation.VERTICAL));

		statusBar.getRightItems().add(xyLabel);
		//statusBar.getLeftItems().add(yLabel);
		//statusBar.getLeftItems().add(new Separator(javafx.geometry.Orientation.VERTICAL));

/*
    	marsDateString = marsClock.getDateString();
    	marsTimeString = marsClock.getTrucatedTimeString();
    	// For now, we denoted Martian Time in UMST as in Mars Climate Database Time. It's given as Local True Solar Time at longitude 0, LTST0
    	// see http://www-mars.lmd.jussieu.fr/mars/time/solar_longitude.html
		//dateLabel.setText("Martian Date : " + marsDateString + " ");
		//timeLabel.setText("Time : " + marsTimeString + " millisols (UMST)");

    	timeLabel.setText("  " + marsDateString + "  " + marsTimeString + MILLISOLS_UMST);
		//timeText.setStyle("-fx-text-inner-color: orange;");
		timeLabel.setTooltip(new Tooltip ("Martian Date/Time"));

		statusBar.getRightItems().add(new Separator(javafx.geometry.Orientation.VERTICAL));
		statusBar.getRightItems().add(timeLabel);
		statusBar.getRightItems().add(new Separator(javafx.geometry.Orientation.VERTICAL));
*/
/*
		Color c = Color.rgb(156,77,0);
		
		//String c = orange.toString().replace("0x", "");
		//System.out.println("c is " + c.toString().replace("0x", "")); // 9c4d00ff

		solLabel.setTextFill(c);
		popLabel.setTextFill(c);
		capLabel.setTextFill(c);
		xyLabel.setTextFill(c);
		timeLabel.setTextFill(c);
*/

/*
 * 		using setStyle("-fx-text-fill: orange;") will not allow
		solLabel.setStyle("-fx-text-fill: orange;");
		popLabel.setStyle("-fx-text-fill: orange;");
		capLabel.setStyle("-fx-text-fill: orange;");
		xyLabel.setStyle("-fx-text-fill: orange;");
		timeLabel.setStyle("-fx-text-fill: orange;");


		solLabel.setStyle("-fx-text-inner-color: orange;");
		popLabel.setStyle("-fx-text-inner-color: orange;");
		capLabel.setStyle("-fx-text-inner-color: orange;");
		xyLabel.setStyle("-fx-text-inner-color: orange;");
		timeLabel.setStyle("-fx-text-inner-color: orange;");
*/

		return statusBar;
	}

	/*
	 * Updates the cpu loads, memory usage and time text in the status bar
	 */
	public void updateStatusBarText() {
			
		setTheme(null);
		
		if (mainScene != null) {
			if (mainScene.isMainSceneDone() && !isBound) {
				isBound = true;
				height.bind(mainScene.getAnchorPane().heightProperty());
				width.bind(mainScene.getAnchorPane().widthProperty());
			}
		}

		if (widthCache != width.get() || heightCache != height.get()) {
			widthCache = width.get();
			heightCache = height.get();
    		SwingUtilities.invokeLater(()-> setSize(new Dimension((int)widthCache, (int)heightCache)));
		}
	/*
		String d = marsClock.getDateString();
		String t = marsClock.getTrucatedTimeString();

		if (!marsTimeString.equals(t)) {
			timeLabel.setText("  " + d + "  " + t + MILLISOLS_UMST);
			marsTimeString = t;
			marsDateString = d;
		}
		else if (marsDateString.equals(d)) {
			timeLabel.setText("  " + d + "  " + t + MILLISOLS_UMST);
			marsDateString = d;
			marsTimeString = t;
		}
	*/
    	int s = marsClock.getMissionSol();
    	int p = mapPanel.getSettlement().getAllAssociatedPeople().size();
    	int c = mapPanel.getSettlement().getPopulationCapacity();


	    if (sol != s) {
//		    SwingUtilities.invokeLater(()-> 
		    solLabel.setText(SOL + s);
//		    );
		    sol = s;
	    }


	    if (pop != p) {
//		    SwingUtilities.invokeLater(()-> 
		    popLabel.setText(POPULATION + p);
//		    );
		    pop = p;
	    }

	    if (cap != c) {
//		    SwingUtilities.invokeLater(()-> 
		    capLabel.setText(CAP + c);
//		    );
		    cap = c;
	    }

	    if (xCoor != 0 && yCoor != 0)
//	    	SwingUtilities.invokeLater(()-> 
	    	xyLabel.setText(POINTER + xCoor + COMMA + yCoor + CLOSE_PARENT);
//	    	);
	    else
//	    	SwingUtilities.invokeLater(()-> 
	    	xyLabel.setText("");
//	    	);
	}

	/**
	 * Creates and starts the Mars timer
	 */
	public void startMarsTimer() {
		timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), ae -> updateStatusBarText()));
		// Note: Infinite Timeline might result in a memory leak if not stopped properly.
		// All the objects with animated properties would not be garbage collected.
		timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
		timeline.play();

	}

/*
	// 2015-02-05 Added showMarsTime()
	public void showMarsTime() {
		// 2015-01-07 Added Martian Time on status bar
		ActionListener timeListener = null;
		if (timeListener == null) {
			timeListener = new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent evt) {
			    	marsDateString = marsClock.getDateString();
			    	marsTimeString = marsClock.getTrucatedTimeString();
			    	// For now, we denoted Martian Time in UMST as in Mars Climate Database Time. It's given as Local True Solar Time at longitude 0, LTST0
			    	// see http://www-mars.lmd.jussieu.fr/mars/time/solar_longitude.html
					dateLabel.setText("Martian Date : " + marsDateString + " ");
					timeLabel.setText("Time : " + marsTimeString + " millisols (UMST)");
					statusText = "" + marsClock.getSolElapsedFromStart();
				    populationText = mapPanel.getSettlement().getAllAssociatedPeople().size() + "   Cap : " + mapPanel.getSettlement().getPopulationCapacity();
				    // 2015-02-09 Added leftLabel
				    solLabel.setText("Sol : " + statusText);
				    popLabel.setText("Population : " + populationText);
				    //xLabel.setText("x : " + xCoor);
				    //yLabel.setText("y : " + yCoor);
			    }
			};
		}
    	if (marsTimer == null) {
    		marsTimer = new javax.swing.Timer(TIME_DELAY, timeListener);
    		marsTimer.start();
    	}
	}
*/

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
	public WebDesktopPane getDesktop() {
		return desktop;
	}

//	public MarqueeTicker getMarqueeTicker() {
//		return marqueeTicker;
//	}

/*
	public void paintComponent(Graphics g){
	    super.paintComponent(g);
	    g.setColor(Color.BLACK);
	    g.fillRect(subPanel.getX(), subPanel.getY(), subPanel.getWidth(), subPanel.getHeight());
	    subPanel.draw(g);
	}
*/

	public StatusBar getStatusBar() {
		return statusBar;
	}

/*	
	public void setStatusBarTheme(String cssFile) {
		if (statusBar != null) {
			statusBar.getStylesheets().clear();
			statusBar.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		}
	}
*/
	
	public void setXCoor(double x) {
		this.xCoor = x;
	}

	public void setYCoor(double y) {
		this.yCoor = y;
	}

	
	public void setTheme(Color c) {
		if (solLabel != null) {
			
			if (c == null) {
				int theme = MainScene.getTheme();
				if (themeCache != theme) {
					themeCache = theme;
					// orange theme : F4BA00
					// blue theme : 3291D2
					//String color = txtColor.replace("0x", "");
					if (theme == 0 || theme == 6) {
						css_file = MainDesktopPane.BLUE_CSS;
						c = Color.rgb(0,107,184);
					}
					else if (theme == 7) {
						css_file = MainDesktopPane.ORANGE_CSS;	
						c = Color.rgb(156,77,0);
					}
					
					
					if (statusBar != null) {
						statusBar.getStylesheets().clear();
						statusBar.getStylesheets().add(getClass().getResource(css_file).toExternalForm());
					}
					
					solLabel.setTextFill(c);
					popLabel.setTextFill(c);
					capLabel.setTextFill(c);
					xyLabel.setTextFill(c);
					timeLabel.setTextFill(c);
					
				}
			}
		}
	}

	////public void setDesktop(MainDesktopPane desktop) {
	//	this.desktop = desktop;
	//}

	@Override
	public void destroy() {
		//marsTimer.stop();
		//marsTimer = null;
		mapPanel.destroy();
		mapPanel = null;
		desktop = null;
		timeline = null;
//		marqueeTicker = null;
		jfxPanel = null;
		scene = null;
		stack = null;

	}

}