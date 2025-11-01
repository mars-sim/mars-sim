/*
 * Mars Simulation Project
 * OrbitViewer.java
 * @date 2022-07-10
 * @author Manny Kung
 * Original work by Osamu Ajiki and Ron Baalke (NASA/JPL)
 * http://www.astroarts.com/products/orbitviewer/
 * https://github.com/TheOrbitals/OrbitViewerApplet
 * http://neo.jpl.nasa.gov/
 */

// Based on OrbitViewer v1.3 Copyright(C) 1996-2001 by O.Ajiki/R.Baalke

package com.mars_sim.ui.swing.astroarts;

import java.awt.Adjustable;
import java.awt.BorderLayout;

/**
 * Orbit Projector
 *
 * Example (Comet)
 *
 *   <APPLET CODE="OrbitViewer" WIDTH=510 HEIGHT=400>
 *   <PARAM NAME="Name"  VALUE="1P/Halley">
 *   <PARAM NAME="T"     VALUE="19860209.7695">
 *   <PARAM NAME="e"     VALUE="0.967267">
 *   <PARAM NAME="q"     VALUE="0.587096">
 *   <PARAM NAME="Peri"  VALUE="111.8466">
 *   <PARAM NAME="Node"  VALUE=" 58.1440">
 *   <PARAM NAME="Incl"  VALUE="162.2393">
 *   <PARAM NAME="Eqnx"  VALUE="1950.0">
 *   </APPLET>
 *
 * Example (Minor Planet)
 *
 *   <APPLET CODE="OrbitViewer" WIDTH=510 HEIGHT=400>
 *   <PARAM NAME="Name"  VALUE="Ceres(1)">
 *   <PARAM NAME="Epoch" VALUE="19991118.5">
 *   <PARAM NAME="M"     VALUE="356.648434">
 *   <PARAM NAME="e"     VALUE="0.07831587">
 *   <PARAM NAME="a"     VALUE="2.76631592">
 *   <PARAM NAME="Peri"  VALUE=" 73.917708">
 *   <PARAM NAME="Node"  VALUE=" 80.495123">
 *   <PARAM NAME="Incl"  VALUE=" 10.583393">
 *   <PARAM NAME="Eqnx"  VALUE="2000.0">
 *   </APPLET>
 *
 * Optional paramter "Date" specifies initial date to display.
 * If "Date" parameter omitted, it start with current date.
 *
 *   <PARAM NAME="Date"  VALUE="19860209.7695">
 *
 */

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;

import com.mars_sim.core.astroarts.ATime;
import com.mars_sim.core.astroarts.Astro;
import com.mars_sim.core.astroarts.Comet;
import com.mars_sim.core.astroarts.TimeSpan;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;


/**
 * This Class creates a pictorial representation of a solar system showing the orbits of all planets plus a satellite of interest
 */
@SuppressWarnings("serial")
public class OrbitViewer extends JPanel
implements ActionListener {

	public static final String NAME = "astro";
	public static final String ICON = "astro";
	
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 600;
	private static final int INITIAL_ZOOM_LEVEL = 175;
	private static final int ONE_STEP = 5;

	private int xvalue = 255;
	private int yvalue = 130;
	
	private int xCache;
	private int yCache;	
	
	private int selectedDate;
	
	/**
	 * Components
	 */
	private JScrollBar 		scrollHor;
	private JScrollBar 		scrollVert;
	private OrbitCanvas		orbitCanvas;
	private JButton			buttonDate;
	private JButton			buttonForStep;
	private JButton			buttonStop;
	private JButton			buttonRevStep;

	private MasterClock	    masterClock = null;
	/**
	 * Player thread
	 */
	private OrbitPlayer		orbitPlayer;

	/**
	 * Current Time Setting
	 */
	private ATime atime;

	/**
	 * Time step
	 */
	private static final TimeSpan[] timeStepSpan = {
		new TimeSpan("1 Hour", 0, 0,  0, 1, 0, 0.0),
		new TimeSpan("1 Day", 0, 0,  1, 0, 0, 0.0),
		new TimeSpan("3 Days", 0, 0,  3, 0, 0, 0.0),
		new TimeSpan("10 Days", 0, 0, 10, 0, 0, 0.0),
		new TimeSpan("1 Month", 0, 1,  0, 0, 0, 0.0),
		new TimeSpan("3 Months", 0, 3,  0, 0, 0, 0.0),
		new TimeSpan("6 Months", 0, 6,  0, 0, 0, 0.0),
		new TimeSpan("1 Year", 1, 0,  0, 0, 0, 0.0),
	};

	private TimeSpan timeStep = timeStepSpan[0];
	private int playDirection = ATime.F_INCTIME;

    /**
     * Centered Object
     */
    private static final String[] centerObjectLabel = {
            "Sun", "Halley", "Mercury", "Venus", "Earth",
            "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"
    };

    

    /**
     * Orbits Displayed
     */
    private static final String[] orbitDisplayLabel = {
            "Default Orbits", "All Orbits", "No Orbits", "------",
            "Halley", "Mercury", "Venus", "Earth",
            "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"
    };

    private boolean[] orbitDisplay = {false, true, true, true, true, true, true,
                                     false, false, false, false };
    private static final boolean[] orbitDisplayDefault = {false, true, true, true, true, true, true,
                                     false, false, false, false };

	/**
	 * Limit of ATime
	 */
	private ATime minATime = new ATime( 1600,1,1,0,0,0.0,0.0);
	private ATime maxATime = new ATime( 2200,1,1,0,0,0.0,0.0);

	private static final String SELECT_DATE = "SelectDate";
	private static final String PLAY = "Play";
	private static final String REV_PLAY = "RevPay";
	private static final String STOP = "Stop";
	private static final String STEP = "Step";
	private static final String REV_STEP = "RevStep";
	private static final String PLANET_LABEL = "PlanetLabel";
	private static final String OBJECT_LABEL = "ObjectLabel";
	private static final String DISTANCE_LABEL = "DistLabel";
	private static final String DATE_LABEL = "DateLabel";
	private static final String STEP_CHOICE = "StepChoice";
	private static final String CENTER_CHOICE = "CenterChoice";
	private static final String ORBIT_CHOICE = "OrbitChoise";


	/**
	 * Parameter Information
	 */
	public String[][] getParameterInfo() {
		return new String[][]{
			{ "Name",
			  "String", "Name of the object",			"1P/Halley"     },
			{ "T",
			  "double", "Time of perihelion passage",	"19860209.7695" },
			{ "e",
			  "double", "Eccentricity", 				"0.967267"      },
			{ "q",
			  "double", "Perihelion distance AU", 		"0.587096"      },
			{ "Peri",
			  "double", "Argument of perihelion deg.",  "111.8466"      },
			{ "Node",
			  "double", "Ascending node deg.", 			"58.1440"      },
			{ "Incl",
			  "double", "Inclination deg.", 			"162.2393"      },
			{ "Eqnx",
			  "double", "Year of equinox", 				"1950.0"        },
			{ "Epoch",
			  "double", "Year/Month/Day of epoch", 		"19991118.5"    },
			{ "M",
			  "double", "Mean anomaly deg.", 			"356.648434"    },
			{ "a",
			  "double", "Semimajor axis AU", 			"2.76631592"    },
			{ "Date",
			  "double", "Initial date", 				"19860209.7695" },
		};
	}

	private int rowOfMatrix;

	/**
	 * Initialization.
	 */
	public OrbitViewer(MasterClock masterClock) {

		this.masterClock = masterClock;
		String[][] array = getParameterInfo();
		rowOfMatrix = array.length;

		createGUI();

		// Player Thread	
		orbitPlayer = new OrbitPlayer(this);
	}

	private void createGUI() {

		setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel(new BorderLayout());

		GridBagLayout gblMainPanel = new GridBagLayout();
		mainPanel.setLayout(gblMainPanel);
		
		GridBagConstraints constraints = new GridBagConstraints();

		scrollHor = new JScrollBar(Adjustable.HORIZONTAL, INITIAL_ZOOM_LEVEL, ONE_STEP, 0, 300);
		scrollVert = new JScrollBar(Adjustable.VERTICAL, INITIAL_ZOOM_LEVEL, ONE_STEP, 0, 300);
		
		// Create a comet object
		Comet object = getObject();
		String strParam;
		if ((strParam = getParameter("Date")) != null) {
			this.atime = ymdStringToAtime(strParam);
		} else {
			this.atime = new ATime(1900, 1, 1, 0.0);
		}
		
		constraints.fill = GridBagConstraints.BOTH;

		// Orbit Canvas
		orbitCanvas = new OrbitCanvas(object, this.atime);
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		gblMainPanel.setConstraints(orbitCanvas, constraints);
		mainPanel.add(orbitCanvas);

		orbitCanvas.setZoom(scrollHor.getValue());
		orbitCanvas.setZoom(scrollVert.getValue());
		
		orbitCanvas.addMouseWheelListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int value = 0;

				int notches = e.getWheelRotation();

				value = scrollHor.getValue() + notches;
				if (value < 0)
					value = 0;
				scrollHor.setValue(value);
				orbitCanvas.setZoom(scrollHor.getValue());
				
				value = scrollVert.getValue() + notches;
				if (value < 0)
					value = 0;
				scrollVert.setValue(value);
				orbitCanvas.setZoom(scrollVert.getValue());
				
				orbitCanvas.repaint();
			}
		});

		orbitCanvas.addMouseListener(new MouseAdapter() {
			@Override
		    public void mouseReleased(MouseEvent evt){

				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

				if (270 - xvalue > 360) {
					int x = (270-xvalue)%360;
					xvalue = 270 - x;
				}
				else if (270 - xvalue < -360) {
					int x  = -(270-xvalue)%360;
					xvalue = x + 270;
				}
				if (180 - yvalue > 360) {
					int y = (180-yvalue)%360;
					yvalue = 180 - y;
				}
				else if (180 - yvalue < -360) {
					int y = -(180-yvalue)%360;
					yvalue = y + 180;
				}

				xCache = 0;
				yCache = 0;
		    }
		});

		orbitCanvas.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent evt) {
				setCursor(new Cursor(Cursor.MOVE_CURSOR));

				int x = evt.getX();
				int xDiff = 0;
				// initialize xCache
				if (xCache == 0 && x != 0)
					xCache = x;


				if (x - xCache > 0) {
					xDiff = x - xCache;
					xvalue = xvalue + xDiff;
				}
				else if (x - xCache == 0) {
					// Do nothing
				}
				else if (x - xCache < 0) {
					xDiff = xCache - x;
					xvalue = xvalue - xDiff;
				}

				orbitCanvas.setRotateHorz(270 - xvalue);

				int y = evt.getY();
				int yDiff = 0;
				// initialize yCache
				if (yCache == 0 && y != 0)
					yCache = y;

				if (y - yCache > 0) {
					yDiff = y - yCache;
					yvalue = yvalue + yDiff;
				}
				else if (y - yCache == 0) {
					// do nothing
				}
				else if (y - yCache < 0) {
					yDiff = yCache - y;
					yvalue = yvalue - yDiff;
				}

				orbitCanvas.setRotateVert(180 - yvalue);

				orbitCanvas.repaint();

				xCache = x;
				yCache = y;
			}
		});

		orbitCanvas.setRotateVert(180 - yvalue);
		orbitCanvas.setRotateHorz(270 - xvalue);
	
		constraints.fill = GridBagConstraints.VERTICAL;
		
		// Note: may add scrollHor and scrollVert to make the zoom bar visible
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		gblMainPanel.setConstraints(scrollHor, constraints);
		
		Font labelFont = StyleManager.getLabelFont();
		
		//
		// Control Panel
		//
		JPanel ctrlPanel = new JPanel();
		GridBagLayout gblCtrlPanel = new GridBagLayout();
		GridBagConstraints gbcCtrlPanel = new GridBagConstraints();
		
		gbcCtrlPanel.fill = GridBagConstraints.BOTH;
		
		ctrlPanel.setLayout(gblCtrlPanel);
		ctrlPanel.setBorder(new MarsPanelBorder());
		
		// Set Control Label
		JLabel controlLabel = new JLabel("Controls", SwingConstants.CENTER);
		controlLabel.setAlignmentX(CENTER_ALIGNMENT);
		gbcCtrlPanel.gridx = 0;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 5;
		gbcCtrlPanel.gridheight = 1;
		gblCtrlPanel.setConstraints(controlLabel, gbcCtrlPanel);
		ctrlPanel.add(controlLabel);

		gbcCtrlPanel.fill = GridBagConstraints.HORIZONTAL;
		
		// Reverse-Play Button
		JButton buttonRevPlay = new JButton("<");
		buttonRevPlay.setToolTipText("Reverse Play Continuously");
		buttonRevPlay.setActionCommand(REV_PLAY);
		buttonRevPlay.addActionListener(this);
		gbcCtrlPanel.gridx = 0;
		gbcCtrlPanel.gridy = 1;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 0, 5, 0);
		gblCtrlPanel.setConstraints(buttonRevPlay, gbcCtrlPanel);
		ctrlPanel.add(buttonRevPlay);

		// Reverse-Step Button
		buttonRevStep = new JButton("|<");
		buttonRevStep.setToolTipText("Reverse One Step");
		buttonRevStep.setActionCommand(REV_STEP);
		buttonRevStep.addActionListener(this);
		gbcCtrlPanel.gridx = 1;
		gbcCtrlPanel.gridy = 1;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 0, 5, 0);
		gblCtrlPanel.setConstraints(buttonRevStep, gbcCtrlPanel);
		ctrlPanel.add(buttonRevStep);

		// Stop Button
		buttonStop = new JButton("||");
		buttonStop.setToolTipText("Stop");
		buttonStop.setActionCommand(STOP);
		buttonStop.addActionListener(this);
		gbcCtrlPanel.gridx = 2;
		gbcCtrlPanel.gridy = 1;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 0, 5, 0);
		gblCtrlPanel.setConstraints(buttonStop, gbcCtrlPanel);
		ctrlPanel.add(buttonStop);

		// Step Button
		buttonForStep = new JButton(">|");
		buttonForStep.setToolTipText("One Step Forward");
		buttonForStep.setActionCommand(STEP);
		buttonForStep.addActionListener(this);
		gbcCtrlPanel.gridx = 3;
		gbcCtrlPanel.gridy = 1;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 0, 5, 0);
		gblCtrlPanel.setConstraints(buttonForStep, gbcCtrlPanel);
		ctrlPanel.add(buttonForStep);

		// Play Button
		JButton buttonForPlay = new JButton(">");
		buttonForPlay.setToolTipText("Play Forward Continuously");
		buttonForPlay.setActionCommand(PLAY);
		buttonForPlay.addActionListener(this);
		gbcCtrlPanel.gridx = 4;
		gbcCtrlPanel.gridy = 1;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 0, 5, 0);
		gblCtrlPanel.setConstraints(buttonForPlay, gbcCtrlPanel);
		ctrlPanel.add(buttonForPlay);
		
		gbcCtrlPanel.fill = GridBagConstraints.HORIZONTAL;
		
		// Set Date Button
		buttonDate = new JButton("Select A Date");
		buttonDate.setActionCommand(SELECT_DATE);
		buttonDate.addActionListener(this);
		gbcCtrlPanel.gridx = 0;
		gbcCtrlPanel.gridy = 2;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 2;
		gblCtrlPanel.setConstraints(buttonDate, gbcCtrlPanel);
		ctrlPanel.add(buttonDate);
		
		
        // Step Label
        JLabel stepLabel = new JLabel("Step : ");
		stepLabel.setFont(labelFont);
        stepLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbcCtrlPanel.gridx = 1;
        gbcCtrlPanel.gridy = 2;
        gbcCtrlPanel.weightx = 0.0;
        gbcCtrlPanel.weighty = 0.0;
        gbcCtrlPanel.gridwidth = 1;
        gbcCtrlPanel.gridheight = 1;
        gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
        gblCtrlPanel.setConstraints(stepLabel, gbcCtrlPanel);
        ctrlPanel.add(stepLabel);

		// Step choice box
		JComboBox<TimeSpan> choiceTimeStep = new JComboBox<>(timeStepSpan);
		gbcCtrlPanel.gridx = 2;
		gbcCtrlPanel.gridy = 2;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
		gblCtrlPanel.setConstraints(choiceTimeStep, gbcCtrlPanel);
		ctrlPanel.add(choiceTimeStep);
		choiceTimeStep.setSelectedIndex(1);
		choiceTimeStep.setActionCommand(STEP_CHOICE);
		choiceTimeStep.addActionListener(this);
		
       // Center Object JLabel
        JLabel centerLabel = new JLabel("Center : ");
		centerLabel.setFont(labelFont);
        centerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbcCtrlPanel.gridx = 3;
        gbcCtrlPanel.gridy = 2;
        gbcCtrlPanel.weightx = 0.0;
        gbcCtrlPanel.weighty = 0.0;
        gbcCtrlPanel.gridwidth = 1;
        gbcCtrlPanel.gridheight = 1;
        gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
        gblCtrlPanel.setConstraints(centerLabel, gbcCtrlPanel);
        ctrlPanel.add(centerLabel);

       // Center Object choice box
        JComboBox<String> choiceCenterObject = new JComboBox<>(centerObjectLabel);
        gbcCtrlPanel.gridx = 4;
        gbcCtrlPanel.gridy = 2;
        gbcCtrlPanel.weightx = 0.0;
        gbcCtrlPanel.weighty = 0.0;
        gbcCtrlPanel.gridwidth = 1;
        gbcCtrlPanel.gridheight = 1;
        gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
        gblCtrlPanel.setConstraints(choiceCenterObject, gbcCtrlPanel);
        ctrlPanel.add(choiceCenterObject);
        choiceCenterObject.setSelectedIndex(0);
        orbitCanvas.selectCenterObject(0);
		choiceCenterObject.setActionCommand(CENTER_CHOICE);
        choiceCenterObject.addActionListener(this);
        
       // Display Orbits JLabel
        JLabel orbitLabel = new JLabel("Orbit : ");
		orbitLabel.setFont(labelFont);
        orbitLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbcCtrlPanel.gridx = 3;
        gbcCtrlPanel.gridy = 3;
        gbcCtrlPanel.weightx = 0.0;
        gbcCtrlPanel.weighty = 0.0;
        gbcCtrlPanel.gridwidth = 1;
        gbcCtrlPanel.gridheight = 1;
        gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
        gblCtrlPanel.setConstraints(orbitLabel, gbcCtrlPanel);
        ctrlPanel.add(orbitLabel);

      // Display Orbit choice box
        JComboBox<String> choiceOrbitObject = new JComboBox<>(orbitDisplayLabel);
		choiceOrbitObject.setActionCommand(ORBIT_CHOICE);
        choiceOrbitObject.addActionListener(this);
        gbcCtrlPanel.gridx = 4;
        gbcCtrlPanel.gridy = 3;
        gbcCtrlPanel.weightx = 0.0;
        gbcCtrlPanel.weighty = 0.0;
        gbcCtrlPanel.gridwidth = 1;
        gbcCtrlPanel.gridheight = 1;
        gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
        gblCtrlPanel.setConstraints(choiceOrbitObject, gbcCtrlPanel);
        ctrlPanel.add(choiceOrbitObject);
        for (int i = 0; i < orbitDisplayDefault.length; i++) {
                orbitDisplay[i] = orbitDisplayDefault[i];
        }
        choiceOrbitObject.setSelectedIndex(1);
        orbitCanvas.selectOrbits(orbitDisplay);

		// Create Date Label Check Box
		JCheckBox checkDateLabel = new JCheckBox("Date Label");
		checkDateLabel.setSelected(true);
		checkDateLabel.setActionCommand(DATE_LABEL);
		checkDateLabel.addActionListener(this);
		gbcCtrlPanel.gridx = 6;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(5, 12, 0, 0);
		gblCtrlPanel.setConstraints(checkDateLabel, gbcCtrlPanel);
		ctrlPanel.add(checkDateLabel);
		orbitCanvas.switchPlanetName(checkDateLabel.isSelected());

		// Planet Name Checkbox
		JCheckBox checkPlanetName = new JCheckBox("Planet Labels");
		checkPlanetName.setActionCommand(PLANET_LABEL);
		checkPlanetName.setSelected(true);
		checkPlanetName.addActionListener(this);
		gbcCtrlPanel.gridx = 6;
		gbcCtrlPanel.gridy = 1;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 12, 0, 0);
		gblCtrlPanel.setConstraints(checkPlanetName, gbcCtrlPanel);
		ctrlPanel.add(checkPlanetName);
		orbitCanvas.switchPlanetName(checkPlanetName.isSelected());

		// Distance JLabel Checkbox
		JCheckBox checkDistanceLabel = new JCheckBox("Distance");
		checkDistanceLabel.setActionCommand(DISTANCE_LABEL);
		checkDistanceLabel.setSelected(true);
		checkDistanceLabel.addActionListener(this);
		gbcCtrlPanel.gridx = 6;
		gbcCtrlPanel.gridy = 2;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 12, 0, 0);
		gblCtrlPanel.setConstraints(checkDistanceLabel, gbcCtrlPanel);
		ctrlPanel.add(checkDistanceLabel);
		orbitCanvas.switchPlanetName(checkDistanceLabel.isSelected());

		// Object Name Checkbox
		JCheckBox checkObjectName = new JCheckBox("Object Label");
		checkObjectName.setActionCommand(OBJECT_LABEL);
		checkObjectName.setSelected(true);
		checkObjectName.addActionListener(this);
		gbcCtrlPanel.gridx = 6;
		gbcCtrlPanel.gridy = 3;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 12, 0, 0);
		gblCtrlPanel.setConstraints(checkObjectName, gbcCtrlPanel);
		ctrlPanel.add(checkObjectName);
		orbitCanvas.switchObjectName(checkObjectName.isSelected());

		//
		// Layout
		//
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(gbl);
		gbc.fill = GridBagConstraints.BOTH;

		// Main Panel
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(mainPanel, gbc);
		add(mainPanel);

		// Control Panel
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridwidth  = GridBagConstraints.REMAINDER;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(6, 0, 10, 0);
		gbl.setConstraints(ctrlPanel, gbc);
		add(ctrlPanel);

		// Set buttons as stopped player state
		setButtonState(false);

		setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
	}


	private String getParameter(String value) {
		String result = null;
		for (int i = 0; i < rowOfMatrix; i++) {
			if (getParameterInfo()[i][0].equals(value)) {
				result = getParameterInfo()[i][3];
				break;
			}
		}
		return result;
	}
	
	/**
	 * Convert time in format "YYYYMMDD.H" to ATime
	 */
	private ATime ymdStringToAtime(String strYmd) {
		double fYmd = Double.parseDouble(strYmd);
		int nYear = (int)Math.floor(fYmd / 10000.0);
		fYmd -= nYear * 10000.0;
		int nMonth = (int)Math.floor(fYmd / 100.0);
		double fDay = fYmd - nMonth * 100.0;
		// ignore H (hours)
		return new ATime(nYear, nMonth, fDay, 0.0);
	}

	/**
	 * Get required double parameter
	 */
	private double getRequiredParameter(String strName) {
		String strValue = getParameter(strName);
		if (strValue == null) {
			throw new IllegalArgumentException("Required parameter '"
							   + strName + "' not found.");
		}
		return Double.parseDouble(strValue);
	}

	/**
	 * Get orbital elements of the object from parameter
	 */
	private Comet getObject() {
		String strName = getParameter("Name");
		if (strName == null) {
			strName = "Object";
		}
		double e, q;
		ATime T;
		String strParam;
		if ((strParam = getParameter("e")) == null) {
			throw new IllegalArgumentException("required parameter 'e' not found.");
		}
		e = Double.parseDouble(strParam);
		if ((strParam = getParameter("T")) != null) {
			T = ymdStringToAtime(strParam);
			if ((strParam = getParameter("q")) != null) {
				q = Double.parseDouble(strParam);
			} else if ((strParam = getParameter("a")) != null) {
				double a = Double.parseDouble(strParam);
				if (Math.abs(e - 1.0) < 1.0e-15) {
					throw new IllegalArgumentException("Orbit is parabolic, but 'q' not found.");
				}
				q = a * (1.0 - e);
			} else {
				throw new IllegalArgumentException("Required parameter 'q' or 'a' not found.");
			}
		} else if ((strParam = getParameter("Epoch")) != null) {
			ATime epoch = ymdStringToAtime(strParam);
			if (e > 0.95) {
				throw new
					IllegalArgumentException("Orbit is nearly parabolic, but 'T' not found.");
			}
			double a;
			if ((strParam = getParameter("a")) != null) {
				a = Double.parseDouble(strParam);
				q = a * (1.0 - e);
			} else if ((strParam = getParameter("q")) != null) {
				q = Double.parseDouble(strParam);
				a = q / (1.0 - e);
			} else {
				throw new IllegalArgumentException("Required parameter 'q' or 'a' not found.");
			}
			if (q < 1.0e-15) {
				throw new IllegalArgumentException("Too small perihelion distance.");
			}
			double n = Astro.GAUSS / (a * Math.sqrt(a));
			if ((strParam = getParameter("M")) == null) {
				throw new IllegalArgumentException("Required parameter 'M' not found.");
			}
			double M = Double.parseDouble(strParam)
				* Math.PI / 180.0;
			if (M < Math.PI) {
				T = new ATime(epoch.getJd() - M / n, 0.0);
			} else {
				T = new ATime(epoch.getJd() + (Math.PI*2.0 - M) / n, 0.0);
			}
		} else {
			throw new IllegalArgumentException("Required parameter 'T' or 'Epoch' not found.");
		}
		return new Comet(strName, T.getJd(), e, q,
						 getRequiredParameter("Peri")*Math.PI/180.0,
						 getRequiredParameter("Node")*Math.PI/180.0,
						 getRequiredParameter("Incl")*Math.PI/180.0,
						 getRequiredParameter("Eqnx"));
	}

	/**
	 * Limit ATime between minATime and maxATime
	 */
	private ATime limitATime(ATime atime) {
		if (atime.getJd() <= minATime.getJd()) {
			return new ATime(minATime);
		} else if (maxATime.getJd() <= atime.getJd()) {
			return new ATime(maxATime);
		}
		return atime;
	}

	/**
	 * Set date and redraw canvas
	 */
	private void setNewDate() {
		this.atime = limitATime(this.atime);
		orbitCanvas.setDate(this.atime);
	}

	ATime advanceTime() {
		atime.changeDate(timeStep, playDirection);
		atime = limitATime(atime);

		orbitCanvas.setDate(atime);
		return atime;
	}

	/**
	 * Destroy.
	 */
	public void destroy() {
		orbitPlayer.stop();
	}

	private void setButtonState(boolean playing) {
		buttonDate.setEnabled(!playing);
		buttonForStep.setEnabled(!playing);
		buttonRevStep.setEnabled(!playing);
		buttonStop.setEnabled(playing);
	}

	private void startPlayer(int direction){
		playDirection = direction;
		setButtonState(true);
		orbitPlayer.start();
	}
	
	private void stopPlayer() {
		orbitPlayer.stop();
		setButtonState(false);
	}

	/**
     * Action event occurs.
     *
     * @param event the action event
     */
	@Override
    public void actionPerformed(ActionEvent evt) {
        int centerObjectSelected = 0;
        JComponent source = (JComponent) evt.getSource();
    	switch (evt.getActionCommand()) {
		
			case SELECT_DATE:
				// Set Date
				var dateDialog = new DateDialog(this, atime, masterClock.getEarthTime());
				dateDialog.setLocationRelativeTo(this);
				dateDialog.setVisible(true);
				break;
			case PLAY:
				startPlayer(ATime.F_INCTIME);
				break;
			case REV_PLAY:
				startPlayer(ATime.F_DECTIME);
				break;
			case STOP:
				stopPlayer();
				break;
			case STEP:
				atime.changeDate(timeStep, ATime.F_INCTIME);
				setNewDate();
				break;
			case REV_STEP:
				atime.changeDate(timeStep, ATime.F_DECTIME);
				setNewDate();
				break;
			case PLANET_LABEL:
				orbitCanvas.switchPlanetName(((JCheckBox)source).isSelected());
				orbitCanvas.repaint();
				break;
			case OBJECT_LABEL:
				orbitCanvas.switchObjectName(((JCheckBox)source).isSelected());
				orbitCanvas.repaint();
				break;
			case DISTANCE_LABEL:
				orbitCanvas.switchDistanceLabel(((JCheckBox)source).isSelected());
				orbitCanvas.repaint();
				break;
			case DATE_LABEL:
				orbitCanvas.switchDateLabel(((JCheckBox)source).isSelected());
				orbitCanvas.repaint();
				break;
			case STEP_CHOICE:
				if (source instanceof JComboBox jc) {
					timeStep = (TimeSpan)jc.getSelectedItem();
				}
				break;
			case CENTER_CHOICE:
				if (source instanceof JComboBox jc) {
					centerObjectSelected = jc.getSelectedIndex();
					orbitCanvas.selectCenterObject(centerObjectSelected);
					orbitCanvas.repaint();
				} break;
			case ORBIT_CHOICE:
				if (source instanceof JComboBox jc) {
					int i = jc.getSelectedIndex();
					switch(i) {
						case 1 : 
							for (int j = 0; j < orbitDisplay.length; j++) {
								orbitDisplay[j] = true;
							} break;
						case 2:
							for (int j = 0; j < orbitDisplay.length; j++) {
								orbitDisplay[j] = false;
							} break;
						case 0: 
							for (int j = 0; j < orbitDisplay.length; j++) {
								orbitDisplay[j] = orbitDisplayDefault[j];
							} break;
						case 3:
							break;
						default:
                            orbitDisplay[i-3] = !orbitDisplay[i - 3];
							break;
						}
						orbitCanvas.selectOrbits(orbitDisplay);
						orbitCanvas.repaint();
					}
				break;
			default:
				// Do nothing
		}
    }


	/**
	 * message sent by DateDialog (when disposed)
	 */
	void endDateDialog(ATime atime) {
		buttonDate.setEnabled(true);
		if (atime != null) {
			this.atime = limitATime(atime);
			orbitCanvas.setDate(atime);
			orbitCanvas.repaint();
		}
	}
	
	public int getSelectedDate() {
		return selectedDate;
	}
	
	public void setSelectedDate(int button) {
		selectedDate = button;
	}
}