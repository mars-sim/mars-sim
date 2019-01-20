///**
// * Mars Simulation Project
// * OrbitViewerMain.java
// * @version 3.1.0 2017-02-02
// * @author Manny Kung
// * Original work by Osamu Ajiki and Ron Baalke (NASA/JPL)
// * http://www.astroarts.com/products/orbitviewer/
// * http://neo.jpl.nasa.gov/
// */
//
//package org.mars_sim.msp.ui.astroarts;
//
///**
// * Orbit Projector
// *
// * Example (Comet)
// *
// *   <APPLET CODE="OrbitViewer" WIDTH=510 HEIGHT=400>
// *   <PARAM NAME="Name"  VALUE="1P/Halley">
// *   <PARAM NAME="T"     VALUE="19860209.7695">
// *   <PARAM NAME="e"     VALUE="0.967267">
// *   <PARAM NAME="q"     VALUE="0.587096">
// *   <PARAM NAME="Peri"  VALUE="111.8466">
// *   <PARAM NAME="Node"  VALUE=" 58.1440">
// *   <PARAM NAME="Incl"  VALUE="162.2393">
// *   <PARAM NAME="Eqnx"  VALUE="1950.0">
// *   </APPLET>
// *
// * Example (Minor Planet)
// *
// *   <APPLET CODE="OrbitViewer" WIDTH=510 HEIGHT=400>
// *   <PARAM NAME="Name"  VALUE="Ceres(1)">
// *   <PARAM NAME="Epoch" VALUE="19991118.5">
// *   <PARAM NAME="M"     VALUE="356.648434">
// *   <PARAM NAME="e"     VALUE="0.07831587">
// *   <PARAM NAME="a"     VALUE="2.76631592">
// *   <PARAM NAME="Peri"  VALUE=" 73.917708">
// *   <PARAM NAME="Node"  VALUE=" 80.495123">
// *   <PARAM NAME="Incl"  VALUE=" 10.583393">
// *   <PARAM NAME="Eqnx"  VALUE="2000.0">
// *   </APPLET>
// *
// * Optional paramter "Date" specifies initial date to display.
// * If "Date" parameter omitted, it start with current date.
// *
// *   <PARAM NAME="Date"  VALUE="19860209.7695">
// *
// */
//
//import java.awt.Cursor;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.Font;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseMotionAdapter;
//import java.awt.event.MouseWheelEvent;
//import java.awt.event.WindowEvent;
//import java.awt.event.WindowListener;
//import java.util.Date;
//
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
//import javax.swing.JComponent;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JScrollBar;
//import javax.swing.WindowConstants;
//import javax.swing.event.InternalFrameEvent;
//import javax.swing.event.InternalFrameListener;
//
//import org.mars_sim.msp.ui.swing.MainDesktopPane;
//import org.mars_sim.msp.ui.swing.MarsPanelBorder;
//import org.mars_sim.msp.ui.swing.ModalInternalFrame;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelAstronomicalObservation;
//
//
///**
// * This Class creates a pictorial representation of a solar system showing the orbits of all planets plus a satellite of interest
// */
//public class OrbitViewerMain
//extends JFrame
////extends JInternalFrame
////extends ModalInternalFrame
////implements InternalFrameListener, ActionListener, WindowListener 
//{
//
//	private static final int FRAME_WIDTH = 600;
//	private static final int FRAME_HEIGHT = 600;
//
//	/**
//	 * Components
//	 */
//	private JScrollBar		scrollZoom;
//
//	private int xvalue = 255;
//	private int yvalue = 130;
//	private int xCache;
//	private int yCache;
//
//	private OrbitCanvas		orbitCanvas;
//
//	private JButton			buttonDate;
//	private JButton			buttonRevPlay;
//	private JButton			buttonRevStep;
//	private JButton			buttonStop;
//	private JButton			buttonForStep;
//	private JButton			buttonForPlay;
//
//	//private Choice			choiceTimeStep;
//	//private Choice			choiceCenterObject;
//	//private Choice			choiceOrbitObject;
//
//	private JComboBox<String>			choiceTimeStep;
//	private JComboBox<String>			choiceCenterObject;
//	private JComboBox<String>			choiceOrbitObject;
//
//	private JCheckBox		checkPlanetName;
//	private JCheckBox		checkObjectName;
//	private JCheckBox		checkDistanceLabel;
//	private JCheckBox		checkDateLabel;
//
//	private DateDialog		dateDialog = null;
//
//	/**
//	 * Player thread
//	 */
//	private OrbitPlayerMain		orbitPlayer;
//	Thread					playerThread = null;
//
//	/**
//	 * Current Time Setting
//	 */
//	private ATime atime;
//
//	/**
//	 * Time step
//	 */
//	static final int timeStepCount = 8;
//	static final String timeStepLabel[] = {
//                "1 Hour",
//		"1 Day",   "3 Days",   "10 Days",
//		"1 Month", "3 Months", "6 Months",
//		"1 Year"
//	};
//
//	static final TimeSpan timeStepSpan[] = {
//                new TimeSpan(0, 0,  0, 1, 0, 0.0),
//		new TimeSpan(0, 0,  1, 0, 0, 0.0),
//		new TimeSpan(0, 0,  3, 0, 0, 0.0),
//		new TimeSpan(0, 0, 10, 0, 0, 0.0),
//		new TimeSpan(0, 1,  0, 0, 0, 0.0),
//		new TimeSpan(0, 3,  0, 0, 0, 0.0),
//		new TimeSpan(0, 6,  0, 0, 0, 0.0),
//		new TimeSpan(1, 0,  0, 0, 0, 0.0),
//	};
//
//	public TimeSpan timeStep = timeStepSpan[1];
//	public int      playDirection = ATime.F_INCTIME;
//
//    /**
//     * Centered Object
//     */
//    static final int CenterObjectCount = 11;
//    static final String CenterObjectLabel[] = {
//            "Sun",   "Asteroid/Comet", "Mercury", "Venus", "Earth",
//            "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"
//    };
//
//    public int CenterObjectSelected = 0;
//
//    /**
//     * Orbits Displayed
//     */
//    static final int OrbitDisplayCount = 14;
//    static final String OrbitDisplayLabel[] = {
//            "Default Orbits", "All Orbits", "No Orbits", "------",
//            "Asteroid/Comet", "Mercury", "Venus", "Earth",
//            "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"
//    };
//
//    public int OrbitCount = 11;
//    public boolean OrbitDisplay[] = {false, true, true, true, true, true, true,
//                                     false, false, false, false };
//    public boolean OrbitDisplayDefault[] = {false, true, true, true, true, true, true,
//                                     false, false, false, false };
//
//	/**
//	 * Limit of ATime
//	 */
////	private ATime minATime = new ATime(-30000,1,1,0,0,0.0,0.0);
//	private ATime minATime = new ATime( 1600,1,1,0,0,0.0,0.0);
//	private ATime maxATime = new ATime( 2200,1,1,0,0,0.0,0.0);
//
//	/**
//	 * Initial Settings
//	 */
//	//static final int initialScrollVert = 90+40;
//	//static final int initialScrollHorz = 255;
//	//static final int initialScrollVert = 120;
//	//static final int initialScrollHorz = 180;
//
//
//	static final int initialScrollZoom = 67;
//	static final int fontSize = 14;
//
//	/**
//	 * Applet information
//	 */
//	//public String getAppletInfo() {
//	//	return "OrbitViewer v1.3 Copyright(C) 1996-2001 by O.Ajiki/R.Baalke";
//	//}
//
//	private double xPos;
//	private double yPos;
//	//private double rotation;
//	private double scale;
//	/** Last X mouse drag position. */
//	//private int xLast;
//	/** Last Y mouse drag position. */
//	//private int yLast;
//
//	/**
//	 * Parameter Information
//	 */
//	public String[][] getParameterInfo() {
//		String info[][] = {
//			{ "Name",
//			  "String", "Name of the object",			"1P/Halley"     },
//			{ "T",
//			  "double", "Time of perihelion passage",	"19860209.7695" },
//			{ "e",
//			  "double", "Eccentricity", 				"0.967267"      },
//			{ "q",
//			  "double", "Perihelion distance AU", 		"0.587096"      },
//			{ "Peri",
//			  "double", "Argument of perihelion deg.",  "111.8466"      },
//			{ "Node",
//			  "double", "Ascending node deg.", 			"58.1440"      },
//			{ "Incl",
//			  "double", "Inclination deg.", 			"162.2393"      },
//			{ "Eqnx",
//			  "double", "Year of equinox", 				"1950.0"        },
//			{ "Epoch",
//			  "double", "Year/Month/Day of epoch", 		"19991118.5"    },
//			{ "M",
//			  "double", "Mean anomaly deg.", 			"356.648434"    },
//			{ "a",
//			  "double", "Semimajor axis AU", 			"2.76631592"    },
//			{ "Date",
//			  "double", "Initial date", 				"20280817.0000" },
//		}; // "19860209.7695"
//		return info;
//	}
//
//	public int rowOfMatrix;
//
////	public void windowClosing(WindowEvent e) {
////		    //dispose();
////		    System.exit(0);
////	}
////
////	public void windowOpened(WindowEvent e){ }
////	public void windowIconified(WindowEvent e){ }
////	public void windowClosed(WindowEvent e){
////		desktop.setOrbitViewer(null);
////	}
////	public void windowDeiconified(WindowEvent e){ }
////	public void windowActivated(WindowEvent e){ }
////	public void windowDeactivated(WindowEvent e){ }
//
//	public OrbitViewerMain() {
//		// Call ModalInternalFrame constructor
////        super("Orbit Viewer");
//
//		String array[][] = getParameterInfo();
//		rowOfMatrix = array.length;
//
//		createGUI();
//
//		// Player Thread
//		orbitPlayer = new OrbitPlayerMain(this);
//		playerThread = null;
//	}
//
//	public static void main (String[] args) {
//	 	OrbitViewerMain orbitViewer = new OrbitViewerMain();
//	 	orbitViewer.setSize(FRAME_WIDTH, FRAME_HEIGHT);
//	 	orbitViewer.setVisible(true);
//	 }
//
//
//	/*
//	 *
//	 */
//	public void createGUI() {
//
//	 	setLayout(new FlowLayout());
//
//		JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5,5));
//		GridBagLayout gblMainPanel = new GridBagLayout();
//		GridBagConstraints gbcMainPanel = new GridBagConstraints();
//		gbcMainPanel.fill = GridBagConstraints.BOTH; //HORIZONTAL;//
//		mainPanel.setLayout(gblMainPanel);
//
//		// Orbit Canvas
//		Comet object = getObject();
//		String strParam;
//		if ((strParam = getParameter("Date")) != null) {
//			this.atime = ymdStringToAtime(strParam);
//		} else {
//			Date date = new Date();
//			// TODO: get date from MarsClock
//			this.atime = new ATime(date.getYear() + 1900, date.getMonth() + 1,
//					   (double)date.getDate(), 0.0);
//		}
//		orbitCanvas = new OrbitCanvas(object, this.atime);
//		gbcMainPanel.weightx = 1.0;
//		gbcMainPanel.weighty = 1.0;
//		gbcMainPanel.gridwidth = GridBagConstraints.RELATIVE;
//		gblMainPanel.setConstraints(orbitCanvas, gbcMainPanel);
//		mainPanel.add(orbitCanvas);
//
//		//scrollPane = new JScrollPane();
//		//scrollPane.setBorder(new MarsPanelBorder());
//	    //scrollPane.setViewportView(orbitCanvas);
//		//mainPanel.add(scrollPane);
//
//		orbitCanvas.addMouseWheelListener(new MouseAdapter() {
//			public void mouseWheelMoved(MouseWheelEvent e) {
//				int value = 0;
//
//		       //String message = null;
//		       //String newline = "\n";
//
//		       int notches = e.getWheelRotation();
//		       
////		       if (notches < 0) {
////		           message = "Mouse wheel moved UP "
////		                        + -notches + " notch(es)" + newline;
////		       } else {
////		           message = "Mouse wheel moved DOWN "
////		                        + notches + " notch(es)" + newline;
////		       }
////
////		       if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
////		           message += "    Scroll type: WHEEL_UNIT_SCROLL" + newline;
////		           message += "    Scroll amount: " + e.getScrollAmount()
////		                   + " unit increments per notch" + newline;
////		           message += "    Units to scroll: " + e.getUnitsToScroll()
////		                   + " unit increments" + newline;
////		           message += "    Vertical unit increment: "
////		               //+ scrollPane.getVerticalScrollBar().getUnitIncrement(1)
////		               + " pixels" + newline;
////		           //value = e.getScrollAmount();
////		       } else { //scroll type == MouseWheelEvent.WHEEL_BLOCK_SCROLL
////		           message += "    Scroll type: WHEEL_BLOCK_SCROLL" + newline;
////		           message += "    Vertical block increment: "
////		               //+ scrollPane.getVerticalScrollBar().getBlockIncrement(1)
////		               + " pixels" + newline;
////		           //value = e.getScrollAmount();
////		       }
//
//		       value = scrollZoom.getValue() + notches * 2;
//		       if (value < 0)
//		    	   value = 0;
//		       scrollZoom.setValue(value);
//		       //System.out.println(message);
//		       //System.out.println("value is "+ value);
//		       orbitCanvas.setZoom(scrollZoom.getValue());
//		       orbitCanvas.repaint();
//			}
//		});
//
//
//
//		orbitCanvas.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mousePressed(MouseEvent evt){
//				//yLast = evt.getY();
//
////				setCursor(new Cursor(Cursor.MOVE_CURSOR));
////				double xDiff = (evt.getX() - xLast)/3D;
////				double yDiff = (evt.getY() - yLast)/3D;
////				System.out.println("xDiff is "+ xDiff);
////				orbitCanvas.setRotateHorz(270 - (int)xDiff);
//
//		    }
//			@Override
//		    public void mouseReleased(MouseEvent evt){
//
//				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//
//				if (270 - xvalue > 360) {
//					int x = (270-xvalue)%360;
//					xvalue = 270 - x;
//				}
//				else if (270 - xvalue < -360) {
//					int x  = -(270-xvalue)%360;
//					xvalue = x + 270;
//				}
//				if (180 - yvalue > 360) {
//					int y = (180-yvalue)%360;
//					yvalue = 180 - y;
//				}
//				else if (180 - yvalue < -360) {
//					int y = -(180-yvalue)%360;
//					yvalue = y + 180;
//				}
//
//				xCache = 0;
//				yCache = 0;
//		    }
//		});
//
//		orbitCanvas.addMouseMotionListener(new MouseMotionAdapter() {
//			@Override
//			public void mouseDragged(MouseEvent evt) {
//				setCursor(new Cursor(Cursor.MOVE_CURSOR));
//
//				int x = evt.getX();
//				int xDiff = 0;
//				// initialize xCache
//				if (xCache == 0 && x != 0)
//					xCache = x;
//
//				//xCache = x + 1;
//
//				if (x - xCache > 0) {
//					//System.out.print(">0 ");
//					xDiff = x - xCache;
//					xvalue = xvalue + xDiff;
//				}
//				else if (x - xCache == 0) {
//					//System.out.print("=0 ");
//				}
//				else if (x - xCache < 0) {
//					//System.out.print("<0 ");
//					xDiff = xCache - x;
//					xvalue = xvalue - xDiff;
//				}
//
//				orbitCanvas.setRotateHorz(270 - xvalue);
//
//
//				int y = evt.getY();
//				int yDiff = 0;
//				// initialize yCache
//				if (yCache == 0 && y != 0)
//					yCache = y;
//
//				if (y - yCache > 0) {
//					//System.out.print(">0 ");
//					yDiff = y - yCache;
//					yvalue = yvalue + yDiff;
//				}
//				else if (y - yCache == 0) {
//					//System.out.print("=0 ");
//				}
//				else if (y - yCache < 0) {
//					//System.out.print("<0 ");
//					yDiff = yCache - y;
//					yvalue = yvalue - yDiff;
//				}
//
//				orbitCanvas.setRotateVert(180 - yvalue);
//
//				orbitCanvas.repaint();
//
//				xCache = x;
//				yCache = y;
//			}
//		});
//
//		orbitCanvas.setRotateVert(180 - yvalue);
//		orbitCanvas.setRotateHorz(270 - xvalue);
//
////		// Vertical Scrollbar
////		scrollVert = new JScrollBar(JScrollBar.VERTICAL,
////								   initialScrollVert, 12, 0, 180+12);
////		//Scrollbar s = new Scrollbar(JScrollBar.VERTICAL,
////		//		   initialScrollVert, 12, 0, 180+12);
////		gbcMainPanel.weightx = 0.0;
////		gbcMainPanel.weighty = 0.0;
////		gbcMainPanel.gridwidth = GridBagConstraints.REMAINDER;
////		gblMainPanel.setConstraints(scrollVert, gbcMainPanel);
//////		mainPanel.add(scrollVert);
////		orbitCanvas.setRotateVert(180 - scrollVert.getValue());
////
////		// Horizontal Scrollbar
////		scrollHorz = new JScrollBar(JScrollBar.HORIZONTAL,
////								   initialScrollHorz, 15, 0, 360+15);
////		gbcMainPanel.weightx = 1.0;
////		gbcMainPanel.weighty = 0.0;
////		gbcMainPanel.gridwidth = 1;
////		gblMainPanel.setConstraints(scrollHorz, gbcMainPanel);
//////		mainPanel.add(scrollHorz);
////		orbitCanvas.setRotateHorz(270 - scrollHorz.getValue());
//
//		// Right-Bottom Corner Rectangle
//		JPanel cornerPanel = new JPanel();
//		gbcMainPanel.weightx = 0.0;
//		gbcMainPanel.weighty = 0.0;
//		gbcMainPanel.gridwidth = GridBagConstraints.REMAINDER;
//		gblMainPanel.setConstraints(cornerPanel, gbcMainPanel);
//		mainPanel.add(cornerPanel);
//
//		// Control Panel
//		JPanel ctrlPanel = new JPanel();
//		GridBagLayout gblCtrlPanel = new GridBagLayout();
//		GridBagConstraints gbcCtrlPanel = new GridBagConstraints();
//		gbcCtrlPanel.fill = GridBagConstraints.BOTH;
//		ctrlPanel.setLayout(gblCtrlPanel);
//		//ctrlPanel.setBackground(Color.white);
//		ctrlPanel.setBorder(new MarsPanelBorder());
//
//		// Set Date Button
//		buttonDate = new JButton(" Select Date ");
//		buttonDate.setFont(new Font("Dialog", Font.PLAIN, fontSize-2));
//		buttonDate.addActionListener(this);
//		gbcCtrlPanel.gridx = 0;
//		gbcCtrlPanel.gridy = 0;
//		gbcCtrlPanel.weightx = 0.0;
//		gbcCtrlPanel.weighty = 1.0;
//		gbcCtrlPanel.gridwidth = 1;
//		gbcCtrlPanel.gridheight = 1;
//		//gbcCtrlPanel.insets = new Insets(1, 1, 1, 1);
//		gblCtrlPanel.setConstraints(buttonDate, gbcCtrlPanel);
//		ctrlPanel.add(buttonDate);
//
//		// Reverse-Play Button
//		buttonRevPlay = new JButton("<<");
//		buttonRevPlay.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
//		buttonRevPlay.addActionListener(this);
//		gbcCtrlPanel.gridx = 1;
//		gbcCtrlPanel.gridy = 0;
//		gbcCtrlPanel.weightx = 0.0;
//		gbcCtrlPanel.weighty = 0.0;
//		gbcCtrlPanel.gridwidth = 1;
//		gbcCtrlPanel.gridheight = 1;
//		gbcCtrlPanel.insets = new Insets(0, 0, 3, 0);
//		gblCtrlPanel.setConstraints(buttonRevPlay, gbcCtrlPanel);
//		ctrlPanel.add(buttonRevPlay);
//
//		// Reverse-Step Button
//		buttonRevStep = new JButton("|<");
//		buttonRevStep.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
//		buttonRevStep.addActionListener(this);
//		gbcCtrlPanel.gridx = 2;
//		gbcCtrlPanel.gridy = 0;
//		gbcCtrlPanel.weightx = 0.0;
//		gbcCtrlPanel.weighty = 0.0;
//		gbcCtrlPanel.gridwidth = 1;
//		gbcCtrlPanel.gridheight = 1;
//		gbcCtrlPanel.insets = new Insets(0, 0, 3, 0);
//		gblCtrlPanel.setConstraints(buttonRevStep, gbcCtrlPanel);
//		ctrlPanel.add(buttonRevStep);
//
//		// Stop Button
//		buttonStop = new JButton("||");
//		buttonStop.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
//		buttonStop.addActionListener(this);
//		gbcCtrlPanel.gridx = 3;
//		gbcCtrlPanel.gridy = 0;
//		gbcCtrlPanel.weightx = 0.0;
//		gbcCtrlPanel.weighty = 0.0;
//		gbcCtrlPanel.gridwidth = 1;
//		gbcCtrlPanel.gridheight = 1;
//		gbcCtrlPanel.insets = new Insets(0, 0, 3, 0);
//		gblCtrlPanel.setConstraints(buttonStop, gbcCtrlPanel);
//		ctrlPanel.add(buttonStop);
//
//		// Step Button
//		buttonForStep = new JButton(">|");
//		buttonForStep.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
//		buttonForStep.addActionListener(this);
//		gbcCtrlPanel.gridx = 4;
//		gbcCtrlPanel.gridy = 0;
//		gbcCtrlPanel.weightx = 0.0;
//		gbcCtrlPanel.weighty = 0.0;
//		gbcCtrlPanel.gridwidth = 1;
//		gbcCtrlPanel.gridheight = 1;
//		gbcCtrlPanel.insets = new Insets(0, 0, 3, 0);
//		gblCtrlPanel.setConstraints(buttonForStep, gbcCtrlPanel);
//		ctrlPanel.add(buttonForStep);
//
//		// Play Button
//		buttonForPlay = new JButton(">>");
//		buttonForPlay.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
//		buttonForPlay.addActionListener(this);
//		gbcCtrlPanel.gridx = 5;
//		gbcCtrlPanel.gridy = 0;
//		gbcCtrlPanel.weightx = 0.0;
//		gbcCtrlPanel.weighty = 0.0;
//		gbcCtrlPanel.gridwidth = 1;
//		gbcCtrlPanel.gridheight = 1;
//		gbcCtrlPanel.insets = new Insets(0, 0, 3, 0);
//		gblCtrlPanel.setConstraints(buttonForPlay, gbcCtrlPanel);
//		ctrlPanel.add(buttonForPlay);
//
//        // Step Label
//        JLabel stepLabel = new JLabel("Select Step : ");
//        stepLabel.setHorizontalAlignment(JLabel.RIGHT);
//        stepLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
//        gbcCtrlPanel.gridx = 0;
//        gbcCtrlPanel.gridy = 1;
//        gbcCtrlPanel.weightx = 0.0;
//        gbcCtrlPanel.weighty = 1.0;
//        gbcCtrlPanel.gridwidth = 1;
//        gbcCtrlPanel.gridheight = 1;
//        gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
//        gblCtrlPanel.setConstraints(stepLabel, gbcCtrlPanel);
//        ctrlPanel.add(stepLabel);
//
//		// Step choice box
//		choiceTimeStep = new JComboBox();
//		choiceTimeStep.setFont(new Font("Dialog", Font.PLAIN, fontSize));
//		choiceTimeStep.addActionListener(this);
//		gbcCtrlPanel.gridx = 1;
//		gbcCtrlPanel.gridy = 1;
//		gbcCtrlPanel.weightx = 0.0;
//		gbcCtrlPanel.weighty = 0.0;
//		gbcCtrlPanel.gridwidth = 5;
//		gbcCtrlPanel.gridheight = 1;
//		gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
//		gblCtrlPanel.setConstraints(choiceTimeStep, gbcCtrlPanel);
//		ctrlPanel.add(choiceTimeStep);
//		for (int i = 0; i < timeStepCount; i++) {
//			choiceTimeStep.addItem(timeStepLabel[i]);
//                //choiceTimeStep.setSelectedIndex(1);//.select(timeStepLabel[1]);
//		}
//
//       // Center Object JLabel
//        JLabel centerLabel = new JLabel("Select Center : ");
//        centerLabel.setHorizontalAlignment(JLabel.RIGHT);
//        centerLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
//        gbcCtrlPanel.gridx = 0;
//        gbcCtrlPanel.gridy = 2;
//        gbcCtrlPanel.weightx = 0.0;
//        gbcCtrlPanel.weighty = 1.0;
//        gbcCtrlPanel.gridwidth = 1;
//        gbcCtrlPanel.gridheight = 1;
//        gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
//        gblCtrlPanel.setConstraints(centerLabel, gbcCtrlPanel);
//        ctrlPanel.add(centerLabel);
//
//       // Center Object choice box
//        choiceCenterObject = new JComboBox();
//        choiceCenterObject.setFont(new Font("Dialog", Font.PLAIN, fontSize));
//        choiceCenterObject.addActionListener(this);
//        gbcCtrlPanel.gridx = 1;
//        gbcCtrlPanel.gridy = 2;
//        gbcCtrlPanel.weightx = 0.0;
//        gbcCtrlPanel.weighty = 0.0;
//        gbcCtrlPanel.gridwidth = 5;
//        gbcCtrlPanel.gridheight = 1;
//        gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
//        gblCtrlPanel.setConstraints(choiceCenterObject, gbcCtrlPanel);
//        ctrlPanel.add(choiceCenterObject);
//        for (int i = 0; i < CenterObjectCount; i++) {
//                choiceCenterObject.addItem(CenterObjectLabel[i]);
//        }
//        orbitCanvas.SelectCenterObject(0);
//
//       // Display Orbits JLabel
//        JLabel orbitLabel = new JLabel("Select Orbits : ");
//        orbitLabel.setHorizontalAlignment(JLabel.RIGHT);
//        orbitLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
//        gbcCtrlPanel.gridx = 0;
//        gbcCtrlPanel.gridy = 3;
//        gbcCtrlPanel.weightx = 0.0;
//        gbcCtrlPanel.weighty = 1.0;
//        gbcCtrlPanel.gridwidth = 1;
//        gbcCtrlPanel.gridheight = 1;
//        gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
//        gblCtrlPanel.setConstraints(orbitLabel, gbcCtrlPanel);
//        ctrlPanel.add(orbitLabel);
//
//      // Display Orbit choice box
//        choiceOrbitObject = new JComboBox<String>();
//        choiceOrbitObject.setFont(new Font("Dialog", Font.PLAIN, fontSize));
//        choiceOrbitObject.addActionListener(this);
//        gbcCtrlPanel.gridx = 1;
//        gbcCtrlPanel.gridy = 3;
//        gbcCtrlPanel.weightx = 0.0;
//        gbcCtrlPanel.weighty = 0.0;
//        gbcCtrlPanel.gridwidth = 5;
//        gbcCtrlPanel.gridheight = 1;
//        gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
//        gblCtrlPanel.setConstraints(choiceOrbitObject, gbcCtrlPanel);
//        ctrlPanel.add(choiceOrbitObject);
//        for (int i = 0; i < OrbitDisplayCount; i++) {
//                choiceOrbitObject.addItem(OrbitDisplayLabel[i]);
//        }
//        for (int i = 0; i < OrbitCount; i++) {
//                OrbitDisplay[i] = OrbitDisplayDefault[i];
//        }
//        orbitCanvas.SelectOrbits(OrbitDisplay, OrbitCount);
//
//		// Date Label Checkbox
//		checkDateLabel = new JCheckBox("Date Label");
//		checkDateLabel.setSelected(true);
//		checkDateLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
//		checkDateLabel.addActionListener(this);
//		gbcCtrlPanel.gridx = 6;
//		gbcCtrlPanel.gridy = 0;
//		gbcCtrlPanel.weightx = 0.0;
//		gbcCtrlPanel.weighty = 0.0;
//		gbcCtrlPanel.gridwidth = 1;
//		gbcCtrlPanel.gridheight = 1;
//		gbcCtrlPanel.insets = new Insets(0, 12, 0, 0);
//		gblCtrlPanel.setConstraints(checkDateLabel, gbcCtrlPanel);
//		ctrlPanel.add(checkDateLabel);
//		orbitCanvas.switchPlanetName(checkDateLabel.isSelected());
//
//		// Planet Name Checkbox
//		checkPlanetName = new JCheckBox("Planet Labels");
//		checkPlanetName.setSelected(true);
//		checkPlanetName.setFont(new Font("Dialog", Font.PLAIN, fontSize));
//		checkPlanetName.addActionListener(this);
//		gbcCtrlPanel.gridx = 7;
//		gbcCtrlPanel.gridy = 0;
//		gbcCtrlPanel.weightx = 0.0;
//		gbcCtrlPanel.weighty = 0.0;
//		gbcCtrlPanel.gridwidth = 1;
//		gbcCtrlPanel.gridheight = 1;
//		gbcCtrlPanel.insets = new Insets(0, 12, 0, 0);
//		gblCtrlPanel.setConstraints(checkPlanetName, gbcCtrlPanel);
//		ctrlPanel.add(checkPlanetName);
//		orbitCanvas.switchPlanetName(checkPlanetName.isSelected());
//
//		// Distance JLabel Checkbox
//		checkDistanceLabel = new JCheckBox("Distance");
//		checkDistanceLabel.setSelected(true);
//		checkDistanceLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
//		checkDistanceLabel.addActionListener(this);
//		gbcCtrlPanel.gridx = 6;
//		gbcCtrlPanel.gridy = 1;
//		gbcCtrlPanel.weightx = 0.0;
//		gbcCtrlPanel.weighty = 0.0;
//		gbcCtrlPanel.gridwidth = 1;
//		gbcCtrlPanel.gridheight = 1;
//		gbcCtrlPanel.insets = new Insets(0, 12, 0, 0);
//		gblCtrlPanel.setConstraints(checkDistanceLabel, gbcCtrlPanel);
//		ctrlPanel.add(checkDistanceLabel);
//		orbitCanvas.switchPlanetName(checkDistanceLabel.isSelected());
//
//		// Object Name Checkbox
//		checkObjectName = new JCheckBox("Object Label");
//		checkObjectName.setSelected(true);
//		checkObjectName.setFont(new Font("Dialog", Font.PLAIN, fontSize));
//		checkObjectName.addActionListener(this);
//		gbcCtrlPanel.gridx = 7;
//		gbcCtrlPanel.gridy = 1;
//		gbcCtrlPanel.weightx = 0.0;
//		gbcCtrlPanel.weighty = 0.0;
//		gbcCtrlPanel.gridwidth = 1;
//		gbcCtrlPanel.gridheight = 1;
//		gbcCtrlPanel.insets = new Insets(0, 12, 0, 0);
//		gblCtrlPanel.setConstraints(checkObjectName, gbcCtrlPanel);
//		ctrlPanel.add(checkObjectName);
//		orbitCanvas.switchObjectName(checkObjectName.isSelected());
//
////		// Zoom JLabel
////		JLabel zoomLabel = new JLabel("Zoom:");
////		zoomLabel.setHorizontalAlignment(JLabel.LEFT);
////		zoomLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
////		gbcCtrlPanel.gridx = 6;
////		gbcCtrlPanel.gridy = 2;
////		gbcCtrlPanel.weightx = 0.0;
////		gbcCtrlPanel.weighty = 1.0;
////		gbcCtrlPanel.gridwidth = 2;
////		gbcCtrlPanel.gridheight = 1;
////		gbcCtrlPanel.insets = new Insets(10, 12, 0, 0);
////		gblCtrlPanel.setConstraints(zoomLabel, gbcCtrlPanel);
////		//ctrlPanel.add(zoomLabel);
//
//		// Zoom Scrollbar
//		scrollZoom = new JScrollBar(JScrollBar.HORIZONTAL,
//								   initialScrollZoom, 15, 5, 450);
//		//scrollZoom.addActionListener(this);
//		gbcCtrlPanel.gridx = 6;
//		gbcCtrlPanel.gridy = 3;
//		gbcCtrlPanel.weightx = 1.0;
//		gbcCtrlPanel.weighty = 1.0;
//		gbcCtrlPanel.gridwidth  = 2;
//		gbcCtrlPanel.gridheight = 1;
//		gbcCtrlPanel.insets = new Insets(0, 12, 6, 2);
//		gblCtrlPanel.setConstraints(scrollZoom, gbcCtrlPanel);
//		//ctrlPanel.add(scrollZoom);
//		orbitCanvas.setZoom(scrollZoom.getValue());
//
//		//
//		// Applet Layout
//		//
//		GridBagLayout gbl = new GridBagLayout();
//		GridBagConstraints gbc = new GridBagConstraints();
//		setLayout(gbl);
//		gbc.fill = GridBagConstraints.BOTH;
//
//		// Main Panel
//		gbc.weightx = 1.0;
//		gbc.weighty = 1.0;
//		gbc.gridwidth = GridBagConstraints.REMAINDER;
//		gbl.setConstraints(mainPanel, gbc);
//		add(mainPanel);
//
//		// Control Panel
//		gbc.weightx = 1.0;
//		gbc.weighty = 0.0;
//		gbc.gridwidth  = GridBagConstraints.REMAINDER;
//		gbc.gridheight = GridBagConstraints.REMAINDER;
//		gbc.insets = new Insets(6, 0, 10, 0);
//		gbl.setConstraints(ctrlPanel, gbc);
//		add(ctrlPanel);
//
//		setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
//		setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
//		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//
//		Dimension desktopSize = getParent().getSize();
//	    Dimension jInternalFrameSize = this.getSize();
//	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
//	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
//	    setLocation(width, height);
//
//	    setVisible(true);
//	}
//
//
//	private String getParameter(String value) {
////		int col = -1;
//		String result = null;
//		for (int i = 0; i < rowOfMatrix; i++) {
//			if (getParameterInfo()[i][0].equals(value)) {
//				result = getParameterInfo()[i][3];
//				//System.out.println(" col is " + i + "  value is " + result);
//				break;
//			}
//		}
//		return result;
//	}
//	
//	/**
//	 * Convert time in format "YYYYMMDD.H" to ATime
//	 */
//	private ATime ymdStringToAtime(String strYmd) {
//		double fYmd = Double.valueOf(strYmd).doubleValue();
//		int nYear = (int)Math.floor(fYmd / 10000.0);
//		fYmd -= (double)nYear * 10000.0;
//		int nMonth = (int)Math.floor(fYmd / 100.0);
//		double fDay = fYmd - (double)nMonth * 100.0;
//		// ignore H (hours)
//		return new ATime(nYear, nMonth, fDay, 0.0);
//	}
//
//	/**
//	 * Get required double parameter
//	 */
//	private double getRequiredParameter(String strName) {
//		String strValue = getParameter(strName);
//		if (strValue == null) {
//			throw new Error("Required parameter '"
//							   + strName + "' not found.");
//		}
//		return Double.valueOf(strValue).doubleValue();
//	}
//
//	/**
//	 * Get orbital elements of the object from applet parameter
//	 */
//	private Comet getObject() {
//		String strName = getParameter("Name");
//		if (strName == null) {
//			strName = "Object";
//		}
//		double e, q;
//		ATime T;
//		String strParam;
//		if ((strParam = getParameter("e")) == null) {
//			throw new Error("required parameter 'e' not found.");
//		}
//		e = Double.valueOf(strParam).doubleValue();
//		if ((strParam = getParameter("T")) != null) {
//			T = ymdStringToAtime(strParam);
//			if ((strParam = getParameter("q")) != null) {
//				q = Double.valueOf(strParam).doubleValue();
//			} else if ((strParam = getParameter("a")) != null) {
//				double a = Double.valueOf(strParam).doubleValue();
//				if (Math.abs(e - 1.0) < 1.0e-15) {
//					throw new Error("Orbit is parabolic, but 'q' not found.");
//				}
//				q = a * (1.0 - e);
//			} else {
//				throw new Error("Required parameter 'q' or 'a' not found.");
//			}
//		} else if ((strParam = getParameter("Epoch")) != null) {
//			ATime Epoch = ymdStringToAtime(strParam);
//			if (e > 0.95) {
//				throw new
//					Error("Orbit is nearly parabolic, but 'T' not found.");
//			}
//			double a;
//			if ((strParam = getParameter("a")) != null) {
//				a = Double.valueOf(strParam).doubleValue();
//				q = a * (1.0 - e);
//			} else if ((strParam = getParameter("q")) != null) {
//				q = Double.valueOf(strParam).doubleValue();
//				a = q / (1.0 - e);
//			} else {
//				throw new Error("Required parameter 'q' or 'a' not found.");
//			}
//			if (q < 1.0e-15) {
//				throw new Error("Too small perihelion distance.");
//			}
//			double n = Astro.GAUSS / (a * Math.sqrt(a));
//			if ((strParam = getParameter("M")) == null) {
//				throw new Error("Required parameter 'M' not found.");
//			}
//			double M = Double.valueOf(strParam).doubleValue()
//				* Math.PI / 180.0;
//			if (M < Math.PI) {
//				T = new ATime(Epoch.getJd() - M / n, 0.0);
//			} else {
//				T = new ATime(Epoch.getJd() + (Math.PI*2.0 - M) / n, 0.0);
//			}
//		} else {
//			throw new Error("Required parameter 'T' or 'Epoch' not found.");
//		}
//		return new Comet(strName, T.getJd(), e, q,
//						 getRequiredParameter("Peri")*Math.PI/180.0,
//						 getRequiredParameter("Node")*Math.PI/180.0,
//						 getRequiredParameter("Incl")*Math.PI/180.0,
//						 getRequiredParameter("Eqnx"));
//	}
//
//	/**
//	 * Limit ATime between minATime and maxATime
//	 */
//	private ATime limitATime(ATime atime) {
//		if (atime.getJd() <= minATime.getJd()) {
//			return new ATime(minATime);
//		} else if (maxATime.getJd() <= atime.getJd()) {
//			return new ATime(maxATime);
//		}
//		return atime;
//	}
//
//	/**
//	 * Set date and redraw canvas
//	 */
//	private void setNewDate() {
//		this.atime = limitATime(this.atime);
//		orbitCanvas.setDate(this.atime);
//		orbitCanvas.repaint();
//	}
//
//	/**
//	 * OrbitPlayer interface
//	 */
//	public ATime getAtime() {
//		return atime;
//	}
//	public void setNewDate(ATime atime) {
//		this.atime = limitATime(atime);
//		orbitCanvas.setDate(this.atime);
//		orbitCanvas.repaint();
//	}
//
//	/**
//	 * Override Function start()
//	 */
//	public void start() {
//		// if you want, you can initialize date here
//	}
//
//	/**
//	 * Override Function stop()
//	 */
//	public void stop() {
//		if (dateDialog != null) {
//			dateDialog.dispose();
//			endDateDialog(null);
//		}
//		if (playerThread != null) {
//			playerThread.stop();
//			playerThread = null;
//			buttonDate.setEnabled(true);//.enable();
//		}
//	}
//
//	/**
//	 * Destroy the applet
//	 */
//	public void destroy() {
//		removeAll();
//	}
//
//	 /**
//     * Action event occurs.
//     *
//     * @param event the action event
//     */
//    public void actionPerformed(ActionEvent evt) {
//        JComponent source = (JComponent) evt.getSource();
//
//        //if (source == buttonDate)
//        //    desktop.centerMapGlobe(unit.getCoordinates());
//
//    	//switch (evt.getID()) {
//		/*
//		case Event.SCROLL_ABSOLUTE:
//		case Event.SCROLL_LINE_DOWN:
//		case Event.SCROLL_LINE_UP:
//		case Event.SCROLL_PAGE_UP:
//		case Event.SCROLL_PAGE_DOWN:
//			if (evt.target == scrollHorz) {
//				orbitCanvas.setRotateHorz(270 - scrollHorz.getValue());
//			} else if (evt.target == scrollVert) {
//				orbitCanvas.setRotateVert(180 - scrollVert.getValue());
//			} else if (evt.target == scrollZoom) {
//				orbitCanvas.setZoom(scrollZoom.getValue());
//			} else {
//				return false;
//			}
//			orbitCanvas.repaint();
//			return true;
//		*/
//		//case :
//			if (source == buttonDate) {					// Set Date
//				dateDialog = new DateDialog(this, atime);
//				buttonDate.setEnabled(false);//.disable();
//				//return true;
//			} else if (source == buttonForPlay) {		// ForPlay
//				if (playerThread != null
//					&&  playDirection != ATime.F_INCTIME) {
//					playerThread.stop();
//					playerThread = null;
//				}
//				if (playerThread == null) {
//					buttonDate.setEnabled(false);//disable();
//					playDirection = ATime.F_INCTIME;
//					playerThread = new Thread(orbitPlayer);
//					playerThread.setPriority(Thread.MIN_PRIORITY);
//					playerThread.start();
//				}
//			} else if (source == buttonRevPlay) {		// RevPlay
//				if (playerThread != null
//					&&  playDirection != ATime.F_DECTIME) {
//					playerThread.stop();
//					playerThread = null;
//				}
//				if (playerThread == null) {
//					buttonDate.setEnabled(false);//disable();
//					playDirection = ATime.F_DECTIME;
//					playerThread = new Thread(orbitPlayer);
//					playerThread.setPriority(Thread.MIN_PRIORITY);
//					playerThread.start();
//				}
//			} else if (source == buttonStop) {			// Stop
//				if (playerThread != null) {
//					playerThread.stop();
//					playerThread = null;
//					buttonDate.setEnabled(true);//enable();
//				}
//			} else if (source == buttonForStep) {		// +1 Step
//				atime.changeDate(timeStep, ATime.F_INCTIME);
//				setNewDate();
//				//return true;
//			} else if (source == buttonRevStep) {		// -1 Step
//				atime.changeDate(timeStep, ATime.F_DECTIME);
//				setNewDate();
//				//return true;
//			} else if (source == checkPlanetName) {		// Planet Name
//				orbitCanvas.switchPlanetName(checkPlanetName.isSelected());
//				orbitCanvas.repaint();
//				//return true;
//			} else if (source == checkObjectName) {		// Object Name
//				orbitCanvas.switchObjectName(checkObjectName.isSelected());
//				orbitCanvas.repaint();
//				//return true;
//			} else if (source == checkDistanceLabel) {	// Distance
//				orbitCanvas.switchDistanceLabel(checkDistanceLabel.isSelected());
//				orbitCanvas.repaint();
//				//return true;
//			} else if (source == checkDateLabel) {		// Date
//				orbitCanvas.switchDateLabel(checkDateLabel.isSelected());
//				orbitCanvas.repaint();
//				//return true;
//			} else if (source == choiceTimeStep) {		// Time Step
//				for (int i = 0; i < timeStepCount; i++) {
//					if ((String)(source.getName()) == timeStepLabel[i]) {
//						timeStep = timeStepSpan[i];
//						break;
//					}
//				}
//			} else if (source == choiceCenterObject) {    // Center Object
//				for (int i = 0; i < CenterObjectCount; i++) {
//					if ((String)(source.getName()) == CenterObjectLabel[i]) {
//						CenterObjectSelected = i;
//						orbitCanvas.SelectCenterObject(i);
//						orbitCanvas.repaint();
//						break;
//					}
//				}
//			} else if (source == choiceOrbitObject) {    // Orbit Display
//				for (int i = 0; i < OrbitDisplayCount; i++) {
//					if ((String)(source.getName()) == OrbitDisplayLabel[i]) {
//						if (i == 1) {
//							for (int j = 0; j < OrbitCount; j++) {
//								OrbitDisplay[j] = true;
//							}
//						}
//						else if (i == 2) {
//							for (int j = 0; j < OrbitCount; j++) {
//								OrbitDisplay[j] = false;
//							}
//						}
//						else if (i == 0) {
//							for (int j = 0; j < OrbitCount; j++) {
//								OrbitDisplay[j] = OrbitDisplayDefault[j];
//							}
//						}
//						else if (i > 3) {
//							if (OrbitDisplay[i-3]) {
//								OrbitDisplay[i-3] = false;
//							}
//							else {
//								OrbitDisplay[i-3] = true;
//							}
//						}
//						//evt.getSource() = OrbitDisplayLabel[0];
//						//choiceOrbitObject.setSelectedIndex(0);
//						orbitCanvas.SelectOrbits(OrbitDisplay, OrbitCount);
//						orbitCanvas.repaint();
//						break;
//					}
//				}
//			}
//			//return false;
//		//default:
//		//	return false;
//		//}
//    }
//
////	/**
////	 * Event Handler
////
////    public boolean handleEvent(Event evt) {
////		switch (evt.id) {
////
////		case Event.SCROLL_ABSOLUTE:
////		case Event.SCROLL_LINE_DOWN:
////		case Event.SCROLL_LINE_UP:
////		case Event.SCROLL_PAGE_UP:
////		case Event.SCROLL_PAGE_DOWN:
////			if (evt.target == scrollHorz) {
////				orbitCanvas.setRotateHorz(270 - scrollHorz.getValue());
////			} else if (evt.target == scrollVert) {
////				orbitCanvas.setRotateVert(180 - scrollVert.getValue());
////			} else if (evt.target == scrollZoom) {
////				orbitCanvas.setZoom(scrollZoom.getValue());
////			} else {
////				return false;
////			}
////			orbitCanvas.repaint();
////			return true;
////
////		case Event.ACTION_EVENT:
////			if (evt.target == buttonDate) {					// Set Date
////				dateDialog = new DateDialog(this, atime);
////				buttonDate.disable();
////				return true;
////			} else if (evt.target == buttonForPlay) {		// ForPlay
////				if (playerThread != null
////					&&  playDirection != ATime.F_INCTIME) {
////					playerThread.stop();
////					playerThread = null;
////				}
////				if (playerThread == null) {
////					buttonDate.disable();
////					playDirection = ATime.F_INCTIME;
////					playerThread = new Thread(orbitPlayer);
////					playerThread.setPriority(Thread.MIN_PRIORITY);
////					playerThread.start();
////				}
////			} else if (evt.target == buttonRevPlay) {		// RevPlay
////				if (playerThread != null
////					&&  playDirection != ATime.F_DECTIME) {
////					playerThread.stop();
////					playerThread = null;
////				}
////				if (playerThread == null) {
////					buttonDate.disable();
////					playDirection = ATime.F_DECTIME;
////					playerThread = new Thread(orbitPlayer);
////					playerThread.setPriority(Thread.MIN_PRIORITY);
////					playerThread.start();
////				}
////			} else if (evt.target == buttonStop) {			// Stop
////				if (playerThread != null) {
////					playerThread.stop();
////					playerThread = null;
////					buttonDate.enable();
////				}
////			} else if (evt.target == buttonForStep) {		// +1 Step
////				atime.changeDate(timeStep, ATime.F_INCTIME);
////				setNewDate();
////				return true;
////			} else if (evt.target == buttonRevStep) {		// -1 Step
////				atime.changeDate(timeStep, ATime.F_DECTIME);
////				setNewDate();
////				return true;
////			} else if (evt.target == checkPlanetName) {		// Planet Name
////				orbitCanvas.switchPlanetName(checkPlanetName.getState());
////				orbitCanvas.repaint();
////				return true;
////			} else if (evt.target == checkObjectName) {		// Object Name
////				orbitCanvas.switchObjectName(checkObjectName.getState());
////				orbitCanvas.repaint();
////				return true;
////			} else if (evt.target == checkDistanceLabel) {	// Distance
////				orbitCanvas.switchDistanceLabel(checkDistanceLabel.getState());
////				orbitCanvas.repaint();
////				return true;
////			} else if (evt.target == checkDateLabel) {		// Date
////				orbitCanvas.switchDateLabel(checkDateLabel.getState());
////				orbitCanvas.repaint();
////				return true;
////			} else if (evt.target == choiceTimeStep) {		// Time Step
////				for (int i = 0; i < timeStepCount; i++) {
////					if ((String)evt.arg == timeStepLabel[i]) {
////						timeStep = timeStepSpan[i];
////						break;
////					}
////				}
////			} else if (evt.target == choiceCenterObject) {    // Center Object
////				for (int i = 0; i < CenterObjectCount; i++) {
////					if ((String)evt.arg == CenterObjectLabel[i]) {
////						CenterObjectSelected = i;
////						orbitCanvas.SelectCenterObject(i);
////						orbitCanvas.repaint();
////						break;
////					}
////				}
////			} else if (evt.target == choiceOrbitObject) {    // Orbit Display
////				for (int i = 0; i < OrbitDisplayCount; i++) {
////					if ((String)evt.arg == OrbitDisplayLabel[i]) {
////						if (i == 1) {
////							for (int j = 0; j < OrbitCount; j++) {
////								OrbitDisplay[j] = true;
////							}
////						}
////						else if (i == 2) {
////							for (int j = 0; j < OrbitCount; j++) {
////								OrbitDisplay[j] = false;
////							}
////						}
////						else if (i == 0) {
////							for (int j = 0; j < OrbitCount; j++) {
////								OrbitDisplay[j] = OrbitDisplayDefault[j];
////							}
////						}
////						else if (i > 3) {
////							if (OrbitDisplay[i-3]) {
////								OrbitDisplay[i-3] = false;
////							}
////							else {
////								OrbitDisplay[i-3] = true;
////							}
////						}
////						evt.arg = OrbitDisplayLabel[0];
////						orbitCanvas.SelectOrbits(OrbitDisplay, OrbitCount);
////						orbitCanvas.repaint();
////						break;
////					}
////				}
////			}
////			return false;
////		default:
////			return false;
////		}
////    }
//
//	/**
//	 * message sent by DateDialog (when disposed)
//	 */
//	public void endDateDialog(ATime atime) {
//		dateDialog = null;
//		buttonDate.setEnabled(true);//.enable();
//		if (atime != null) {
//			this.atime = limitATime(atime);
//			orbitCanvas.setDate(atime);
//			orbitCanvas.repaint();
//		}
//	}
//
////	@Override
////	public void windowOpened(WindowEvent e) {
////		// TODO Auto-generated method stub
////
////	}
////
////	@Override
////	public void windowClosing(WindowEvent e) {
////		// TODO Auto-generated method stub
////		System.exit(0);
////
////	}
////
////	@Override
////	public void windowClosed(WindowEvent e) {
////		// TODO Auto-generated method stub
////
////	}
////
////	@Override
////	public void windowIconified(WindowEvent e) {
////		// TODO Auto-generated method stub
////
////	}
////
////	@Override
////	public void windowDeiconified(WindowEvent e) {
////		// TODO Auto-generated method stub
////
////	}
////
////	@Override
////	public void windowActivated(WindowEvent e) {
////		// TODO Auto-generated method stub
////
////	}
////
////	@Override
////	public void windowDeactivated(WindowEvent e) {
////		// TODO Auto-generated method stub
////
////	}
//
//}
//
