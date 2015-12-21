/**
 * Mars Simulation Project
 * LocationTabPanel.java
 * @version 3.07 2015-03-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.mars.TerrainElevation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;

import eu.hansolo.steelseries.gauges.DisplaySingle;
import eu.hansolo.steelseries.gauges.DigitialRadial;
import eu.hansolo.steelseries.gauges.DisplayCircular;
import eu.hansolo.steelseries.tools.BackgroundColor;
import eu.hansolo.steelseries.tools.FrameDesign;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.Orientation;
import eu.hansolo.steelseries.tools.PointerType;

/**
 * The LocationTabPanel is a tab panel for location information.
 */
public class LocationTabPanel
extends TabPanel
implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	 /** default logger.   */
	//private static Logger logger = Logger.getLogger(LocationTabPanel.class.getName());

	private int themeCache;

	private double elevationCache;

	//private String locationText = "Mars";

	// 2014-11-11 Added new panels and labels
	private JPanel tpPanel =  new JPanel();
	private JPanel outsideReadingPanel = new JPanel();
	private JPanel containerPanel = new JPanel();
	//private JLabel temperatureLabel;
	//private JLabel airPressureLabel;
	private JLabel locationLabel;
	private JLabel locLabel;
	//private Color THEME_COLOR = Color.ORANGE;
	//private double airPressureCache;
	//private int temperatureCache;
	private Unit containerCache, topContainerCache;

	private JPanel coordsPanel;
	private JLabel latitudeLabel;
	private JLabel longitudeLabel;
	private JPanel centerPanel;
	//private JButton locationButton;

	private TerrainElevation terrainElevation;
	private Coordinates locationCache;
	private MainScene mainScene;

	private JButton locatorButton;

	private DisplaySingle lcdLong, lcdLat, lcdText; // lcdElev,
	private DisplayCircular gauge;//RadialQuarterN gauge;

	DecimalFormat fmt = new DecimalFormat("##0");
	DecimalFormat fmt2 = new DecimalFormat("#0.00");
    /**
     * Constructor.
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public LocationTabPanel(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super(Msg.getString("LocationTabPanel.title"),
        		null,
        		Msg.getString("LocationTabPanel.tooltip"), unit, desktop);

    	if (terrainElevation == null)
			terrainElevation = Simulation.instance().getMars().getSurfaceFeatures().getSurfaceTerrain();

    	mainScene = desktop.getMainScene();

        // Initialize location header.
		JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePane);

		JLabel titleLabel = new JLabel(Msg.getString("LocationTabPanel.title"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		titlePane.add(titleLabel);

        // Create location panel
        JPanel locationPanel = new JPanel(new BorderLayout( 5, 5));//new GridLayout(2,1,0,0));//new FlowLayout(FlowLayout.CENTER));// new BorderLayout(0,0));
        locationPanel.setBorder(new MarsPanelBorder());
        locationPanel.setBorder(new EmptyBorder(1, 1, 1, 1) );
        topContentPanel.add(locationPanel);


        // Initialize location cache
        locationCache = new Coordinates(unit.getCoordinates());
        themeCache = mainScene.getTheme();

        String dir_N_S = null;
        String dir_E_W = null;
        if (locationCache.getLatitudeDouble() >= 0)
        	dir_N_S = Msg.getString("direction.degreeSign")+"N";
        else
        	dir_N_S = Msg.getString("direction.degreeSign")+"S";

        if (locationCache.getLongitudeDouble() >= 0)
        	dir_E_W = Msg.getString("direction.degreeSign")+"E";
        else
        	dir_E_W = Msg.getString("direction.degreeSign")+"W";


        JPanel northPanel = new JPanel(new FlowLayout());
        locationPanel.add(northPanel, BorderLayout.NORTH);

        lcdLat = new DisplaySingle();
        lcdLat.setLcdUnitString(dir_N_S);
        lcdLat.setLcdValueAnimated(Math.abs(locationCache.getLatitudeDouble()));
        lcdLat.setLcdInfoString("Latitude");
        //lcd1.setLcdColor(LcdColor.BLUELIGHTBLUE_LCD);
        lcdLat.setLcdColor(LcdColor.BEIGE_LCD);
        //lcdLat.setBackground(BackgroundColor.NOISY_PLASTIC);
        lcdLat.setGlowColor(Color.orange);

        //lcd1.setBorder(new EmptyBorder(5, 5, 5, 5));
        lcdLat.setDigitalFont(true);
        lcdLat.setLcdDecimals(2);
        lcdLat.setSize(new Dimension(150, 45));
        lcdLat.setMaximumSize(new Dimension(150, 45));
        lcdLat.setPreferredSize(new Dimension(150, 45));
        lcdLat.setVisible(true);
        //locationPanel.add(lcdLat, BorderLayout.WEST);
        northPanel.add(lcdLat);


        elevationCache = terrainElevation.getElevation(unit.getCoordinates());
 /*
        //System.out.println("elevation is "+ elevation);
        lcdElev = new DisplaySingle();
        lcdElev.setLcdValueFont(new Font("Serif", Font.ITALIC, 12));
        lcdElev.setLcdUnitString("km");
        lcdElev.setLcdValueAnimated(elevationCache);
        lcdElev.setLcdDecimals(3);
        lcdElev.setLcdInfoString("Elevation");
        //lcd0.setLcdColor(LcdColor.DARKBLUE_LCD);
        lcdElev.setLcdColor(LcdColor.YELLOW_LCD);//REDDARKRED_LCD);
        lcdElev.setDigitalFont(true);
        //lcd0.setBorder(new EmptyBorder(5, 5, 5, 5));
        lcdElev.setSize(new Dimension(150, 60));
        lcdElev.setMaximumSize(new Dimension(150, 60));
        lcdElev.setPreferredSize(new Dimension(150, 60));
        lcdElev.setVisible(true);
        locationPanel.add(lcdElev, BorderLayout.NORTH);
 */

        // Create center map button
        locatorButton = new JButton(ImageLoader.getIcon("locator48_orange"));
        //centerMapButton = new JButton(ImageLoader.getIcon("locator_blue"));
        locatorButton.setBorder(new EmptyBorder(1, 1, 1, 1) );
        locatorButton.addActionListener(this);
        locatorButton.setOpaque(false);
        locatorButton.setToolTipText("Locate the unit on Mars Navigator");
        locatorButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));//new Cursor(Cursor.HAND_CURSOR));

		JPanel locatorPane = new JPanel(new FlowLayout());
		locatorPane.add(locatorButton);
		//locationPanel.add(locatorPane, BorderLayout.NORTH);
	    northPanel.add(locatorPane);

        lcdLong = new DisplaySingle();
        //lcdLong.setCustomLcdForeground(getForeground());
        lcdLong.setLcdUnitString(dir_E_W);
        lcdLong.setLcdValueAnimated(Math.abs(locationCache.getLongitudeDouble()));
        lcdLong.setLcdInfoString("Longitude");
        //lcd2.setLcdColor(LcdColor.BLUELIGHTBLUE_LCD);
        lcdLong.setLcdColor(LcdColor.BEIGE_LCD);
        //setBackgroundColor(BackgroundColor.LINEN);
        lcdLong.setGlowColor(Color.yellow);
        lcdLong.setDigitalFont(true);
        lcdLong.setLcdDecimals(2);
        lcdLong.setSize(new Dimension(150, 45));
        lcdLong.setMaximumSize(new Dimension(150, 45));
        lcdLong.setPreferredSize(new Dimension(150,45));
        lcdLong.setVisible(true);
       //locationPanel.add(lcdLong, BorderLayout.EAST);
        northPanel.add(lcdLong);


        int max = -1;
        int min = 2;
        // Note: The peak of Olympus Mons is 21,229 meters (69,649 feet) above the Mars areoid (a reference datum similar to Earth's sea level). The lowest point is within the Hellas Impact Crater (marked by a flag with the letter "L").
        // The lowest point in the Hellas Impact Crater is 8,200 meters (26,902 feet) below the Mars areoid.
        if (elevationCache < -8) {
        	max = -8;
        	min = -9;
        }
        else if (elevationCache < -5) {
        	max = -5;
        	min = -9;
        }
        else if (elevationCache < -3) {
        	max = -3;
        	min = -5;
        }
        else if (elevationCache < 0) {
        	max = 1;
        	min = -1;
        }
        else if (elevationCache < 1) {
        	max = 2;
        	min = 0;
        }
        else if (elevationCache < 3) {
        	max = 5;
        	min = 0;
        }
        else if (elevationCache < 10){
        	max = 10;
        	min = 5;
        }
        else if (elevationCache < 20){
        	max = 20;
        	min = 10;
        }
        else if (elevationCache < 30){
        	max = 30;
        	min = 20;
        }

        gauge = new DisplayCircular();
        setGauge(gauge, min, max);
        locationPanel.add(gauge, BorderLayout.CENTER);



		//centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));//GridLayout(2,1,0,0)); // new BorderLayout())
/*
		// 2015-12-09 Prepare loc label
        locLabel = new JLabel();
        locLabel.setFont(font);
        //locLabel.setOpaque(false);
        locLabel.setFont(new Font("Serif", Font.PLAIN, 13));
        locLabel.setHorizontalAlignment(SwingConstants.CENTER);
 */

        String loc = "On Mars";
		lcdText = new DisplaySingle();
        lcdText.setLcdInfoString("Last Unknown Position");
        //lcdText.setLcdColor(LcdColor.REDDARKRED_LCD);
        lcdText.setGlowColor(Color.ORANGE);
        //lcdText.setBackground(Background.SATIN_GRAY);
        lcdText.setDigitalFont(true);
        lcdText.setSize(new Dimension(150, 30));
        lcdText.setMaximumSize(new Dimension(150, 30));
        lcdText.setPreferredSize(new Dimension(150,30));
        lcdText.setVisible(true);
        lcdText.setLcdNumericValues(false);
        lcdText.setLcdValueFont(new Font("Serif", Font.ITALIC, 8));
        //lcdText.setLcdText(locationText);
        lcdText.setLcdText(loc);
        lcdText.setLcdTextScrolling(true);
        //centerPanel.add(lcdText);
		//locationPanel.add(centerPanel, BorderLayout.SOUTH);
		locationPanel.add(lcdText, BorderLayout.SOUTH);

		checkTheme(true);
    }

    public void checkTheme(boolean firstRun) {
        if (mainScene != null) {
            int theme = mainScene.getTheme();

            if (themeCache != theme || firstRun) {
            	themeCache = theme;

	        	if (theme == 7) {
	                lcdText.setLcdColor(LcdColor.REDDARKRED_LCD);
	                gauge.setFrameDesign(FrameDesign.GOLD);
	                locatorButton.setIcon(ImageLoader.getIcon("locator48_orange"));
	        	}
	        	else if (theme == 6) {
	        		lcdText.setLcdColor(LcdColor.DARKBLUE_LCD);
	        		gauge.setFrameDesign(FrameDesign.STEEL);
	        		locatorButton.setIcon(ImageLoader.getIcon("locator48_blue"));
	        	}
            }
        }
    }

    public void setGauge(DisplayCircular gauge, int min, int max) {
        gauge.setDisplayMulti(false);
    	gauge.setDigitalFont(true);
        //gauge.setFrameDesign(FrameDesign.GOLD);
        //gauge.setOrientation(Orientation.EAST);//.NORTH);//.VERTICAL);
        //gauge.setPointerType(PointerType.TYPE5);
        //gauge.setTextureColor(Color.yellow);//, Texture_Color BRUSHED_METAL and PUNCHED_SHEET);
        gauge.setUnitString("km");
        gauge.setTitle("Elevation");
        //gauge.setMinValue(min);
        //gauge.setMaxValue(max);
        //gauge.setTicklabelsVisible(true);
        //gauge.setMaxNoOfMajorTicks(10);
        //gauge.setMaxNoOfMinorTicks(10);
        gauge.setBackgroundColor(BackgroundColor.NOISY_PLASTIC);//.BRUSHED_METAL);
        //alt.setGlowColor(Color.yellow);
        //gauge.setLcdColor(LcdColor.BEIGE_LCD);//.BLACK_LCD);
        //gauge.setLcdInfoString("Elevation");
        //gauge.setLcdUnitString("km");
        gauge.setLcdValueAnimated(elevationCache);
        gauge.setValueAnimated(elevationCache);
        //gauge.setValue(elevationCache);
        gauge.setLcdDecimals(3);

        //alt.setMajorTickmarkType(TICKMARK_TYPE);
        //gauge.setSize(new Dimension(250, 250));
        //gauge.setMaximumSize(new Dimension(250, 250));
        //gauge.setPreferredSize(new Dimension(250, 250));

        gauge.setSize(new Dimension(250, 250));
        gauge.setMaximumSize(new Dimension(250, 250));
        gauge.setPreferredSize(new Dimension(250, 250));

        gauge.setVisible(true);

    }

	private String getLatitudeString() {
		return locationCache.getFormattedLatitudeString();
	}

	private String getLongitudeString() {
		return locationCache.getFormattedLongitudeString();
	}


    /**
     * Action event occurs.
     *
     * @param event the action event
     */
    public void actionPerformed(ActionEvent event) {
        JComponent source = (JComponent) event.getSource();

        // If the center map button was pressed, update navigator tool.
        if (source == locatorButton) {
        	// 2015-12-19 Added codes to open the settlement map tool and center the map to
        	// show the exact/building location inside a settlement if possible
        	Person p = null;
        	Robot r = null;
        	Vehicle v = null;
        	if (unit instanceof Person) {
        		p = (Person) unit;
    		    SettlementMapPanel mapPanel = desktop.getSettlementWindow().getMapPanel();

        		if (p.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
        			desktop.openToolWindow(SettlementWindow.NAME);
        			//System.out.println("Just open Settlement Map Tool");
        			mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(p.getSettlement());

        			Building b = p.getBuildingLocation();
        			double xLoc = b.getXLocation();
        			double yLoc = b.getYLocation();
        			double scale = mapPanel.getScale();
        			mapPanel.reCenter();
        			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
        			mapPanel.setShowBuildingLabels(true);

        			mapPanel.selectPerson(p);
            	}
        		else if (p.getLocationSituation() == LocationSituation.IN_VEHICLE) {

        			Vehicle vv = p.getVehicle();
        			if (vv.getSettlement() == null) {
        				// out there on a mission
        				desktop.centerMapGlobe(p.getCoordinates());
        			}
        			else {
        				// still parked inside a garage or within the premise of a settlement
	        			desktop.openToolWindow(SettlementWindow.NAME);
	        			//System.out.println("Just open Settlement Map Tool");
	        			mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(p.getSettlement());

	        			double xLoc = vv.getXLocation();
	        			double yLoc = vv.getYLocation();
	        			double scale = mapPanel.getScale();
	        			mapPanel.reCenter();
	        			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
	        			mapPanel.setShowVehicleLabels(true);

	        			mapPanel.selectPerson(p);

	        		}
            	}
        		else if (p.getLocationSituation() == LocationSituation.OUTSIDE) {
        			Vehicle vv = p.getVehicle();

        			if (vv == null) {
        				// he's stepped outside the settlement temporally
               			desktop.openToolWindow(SettlementWindow.NAME);
            			//System.out.println("Just open Settlement Map Tool");
             			mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(p.getSettlement());

        				double xLoc = p.getXLocation();
            			double yLoc = p.getYLocation();
            			double scale = mapPanel.getScale();
            			mapPanel.reCenter();
            			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
            			mapPanel.setShowBuildingLabels(true);

            			mapPanel.selectPerson(p);
        			}
        			else
        				// he's stepped outside a vehicle
        				desktop.centerMapGlobe(p.getCoordinates());
        		}

        	} else if (unit instanceof Robot) {
        		r = (Robot) unit;
        		SettlementMapPanel mapPanel = desktop.getSettlementWindow().getMapPanel();

        		if (r.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
        			desktop.openToolWindow(SettlementWindow.NAME);
        			//System.out.println("Just open Settlement Map Tool");
        			mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(r.getSettlement());

        			Building b = r.getBuildingLocation();
        			double xLoc = b.getXLocation();
        			double yLoc = b.getYLocation();
        			double scale = mapPanel.getScale();
        			mapPanel.reCenter();
        			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
        			mapPanel.setShowBuildingLabels(true);

        			mapPanel.selectRobot(r);
            	}
        		else if (r.getLocationSituation() == LocationSituation.IN_VEHICLE) {

        			Vehicle vv = r.getVehicle();
        			if (vv.getSettlement() == null) {
        				// out there on a mission
        				desktop.centerMapGlobe(r.getCoordinates());
        			}
        			else {
        				// still parked inside a garage or within the premise of a settlement
	        			desktop.openToolWindow(SettlementWindow.NAME);
	        			//System.out.println("Just open Settlement Map Tool");
	        			mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(r.getSettlement());

	        			double xLoc = vv.getXLocation();
	        			double yLoc = vv.getYLocation();
	        			double scale = mapPanel.getScale();
	        			mapPanel.reCenter();
	        			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
	        			mapPanel.setShowVehicleLabels(true);

	        			mapPanel.selectRobot(r);

	        		}
            	}
        		else if (r.getLocationSituation() == LocationSituation.OUTSIDE) {
        			Vehicle vv = r.getVehicle();

        			if (vv == null) {
        				// he's stepped outside the settlement temporally
               			desktop.openToolWindow(SettlementWindow.NAME);
            			//System.out.println("Just open Settlement Map Tool");
             			mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(r.getSettlement());

        				double xLoc = r.getXLocation();
            			double yLoc = r.getYLocation();
            			double scale = mapPanel.getScale();
            			mapPanel.reCenter();
            			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
            			mapPanel.setShowBuildingLabels(true);

            			mapPanel.selectRobot(r);
        			}
        			else
        				// he's stepped outside a vehicle
        				desktop.centerMapGlobe(r.getCoordinates());
        		}

        	} else if (unit instanceof Vehicle) {
        		v = (Vehicle) unit;
          		if (v.getSettlement() != null) {
        			desktop.openToolWindow(SettlementWindow.NAME);
        			//System.out.println("Just open Settlement Map Tool");
        		    SettlementMapPanel mapPanel = desktop.getSettlementWindow().getMapPanel();
        			mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(v.getSettlement());

        			double xLoc = v.getXLocation();
        			double yLoc = v.getYLocation();
        			double scale = mapPanel.getScale();
        			mapPanel.reCenter();
        			mapPanel.moveCenter(xLoc*scale, yLoc*scale);

        			mapPanel.setShowVehicleLabels(true);

            	}
        		else
        			desktop.centerMapGlobe(unit.getCoordinates());
        	}
        }

        // If the location button was pressed, open the unit window.
        //if (source == locationButton)
        //    desktop.openUnitWindow(unit.getContainerUnit(), false);
    }

    /**
     * Updates the info on this panel.
     */
    // 2014-11-11 Overhauled update()
    public void update() {

        // If unit's location has changed, update location display.
    	// TODO: if a person goes outside the settlement for servicing an equipment
    	// does the coordinate (down to how many decimal) change?
    	Coordinates location = unit.getCoordinates();
        if (!locationCache.equals(location)) {
            locationCache.setCoords(location);

            String dir_N_S = null;
            String dir_E_W = null;

            if (locationCache.getLatitudeDouble() >= 0)
            	dir_N_S = Msg.getString("direction.degreeSign")+"N";
            else
            	dir_N_S = Msg.getString("direction.degreeSign")+"S";

            if (locationCache.getLongitudeDouble() >= 0)
            	dir_E_W = Msg.getString("direction.degreeSign")+"E";
            else
            	dir_E_W = Msg.getString("direction.degreeSign")+"W";

            lcdLat.setLcdValueAnimated(Math.abs(locationCache.getLatitudeDouble()));
            lcdLong.setLcdValueAnimated(Math.abs(locationCache.getLongitudeDouble()));

            lcdLat.setLcdValueAnimated(Math.abs(locationCache.getLatitudeDouble()));
            lcdLong.setLcdValueAnimated(Math.abs(locationCache.getLongitudeDouble()));

            double elevation = terrainElevation.getElevation(location);

            if (elevationCache != elevation) {
            	elevationCache = elevation;

                int max = 0;
                int min = 0;
                if (elevationCache < -8) {
                	max = -8;
                	min = -9;
                }
                else if (elevationCache < -5) {
                	max = -5;
                	min = -9;
                }
                else if (elevationCache < -3) {
                	max = -3;
                	min = -5;
                }
                else if (elevationCache < 0) {
                	max = 1;
                	min = -1;
                }
                else if (elevationCache < 1) {
                	max = 2;
                	min = 0;
                }
                else if (elevationCache < 3) {
                	max = 5;
                	min = 0;
                }
                else if (elevationCache < 10){
                	max = 10;
                	min = 5;
                }
                else if (elevationCache < 20){
                	max = 20;
                	min = 10;
                }
                else if (elevationCache < 30){
                	max = 30;
                	min = 20;
                }

                setGauge(gauge, min, max);

            }
        }

        // 2015-12-09 Prepare loc label
        // Update location button or location text label as necessary.
        Unit container = unit.getContainerUnit();
        if (containerCache != container) {
        	containerCache = container;
        	updateLocation();
        }

        Unit topContainer = unit.getTopContainerUnit();
        if (topContainerCache != topContainer) {
        	topContainerCache = topContainer;
        	updateLocation();
        }

        checkTheme(false);

    }

    /**
     * Tracks the location of a person/bot/vehicle/object
     */
    // 2015-12-09 Added updateLocation()
    public void updateLocation() {

    	String loc = null;

        // Case F or case G
        if (containerCache == null && topContainerCache == null) {
        	if (unit instanceof Person) {
        		Person p = (Person) unit;
        		if (p.getLocationSituation() == LocationSituation.OUTSIDE) {
        			//Vehicle v = p.getVehicle();
        			//Rover ro = (Rover) p.getVehicle();
        			//boolean crew = ro.isCrewmember(p);
        			//if (crew) {
        			if (p.getLocationState().getName().equals("Within a settlement's vicinity")) {
        				loc = " Within the vicinity of " + topContainerCache;
        			}

        			else if (p.getLocationState().getName().equals("Outside on the surface of Mars")) {
        				// case F
        				loc = " stepped outside a vehicle on a mission";
        			}

        		} else if (p.getLocationSituation() == LocationSituation.BURIED)
        			// case G
        			loc = " buried outside " + p.getBuriedSettlement().getName();

        	}
        	else if (unit instanceof Robot) {
        		Robot r = (Robot) unit;
        		if (r.getLocationSituation() == LocationSituation.OUTSIDE)
        			loc = " on a mission, stepped outside a vehicle at prescribed coordinates";
        		else if (r.getLocationSituation() == LocationSituation.BURIED)
        			loc = " decommmissed ";// + r.getBuriedSettlement().getName();
        	}
        }

        // case B
        ////else if (containerCache == null && topContainerCache == null) {
        //	loc = " near the premise of " + topContainerCache;
        //}

        // case D
        //else if (topContainerCache == null && containerCache != null) {
        //	loc = " inside a vehicle on a mission outside";
        //}

        // Case A, Case C, Case D, or Case E
        else if (topContainerCache != null && containerCache != null) {
        	if (unit instanceof Person) {
        		Person p = (Person) unit;
        		if (p.getLocationSituation() == LocationSituation.IN_SETTLEMENT)
        			// case A
        			loc = " at " + p.getBuildingLocation().getNickName() + " in " + topContainerCache;
        		else if (p.getLocationSituation() == LocationSituation.IN_VEHICLE) {
        			Vehicle vehicle = (Vehicle) unit.getContainerUnit();
        			if (vehicle.getSettlement() != null) {
        				Building building = BuildingManager.getBuilding(vehicle);

        				if (building == null) {
                			Settlement settlement = (Settlement) vehicle.getContainerUnit();
                			// case D
        					//loc = " in a vehicle parked within the premise of a settlement";
        					loc = " in " + containerCache + " parked within the premise of " + settlement;
        				}
        				else {
        					// case C
            				// vehicle.getSettlement() <==> getTopContainerUnit()
             				// e.g. " in LUV1 inside Garage 1 in Alpha Base;
            				// vehicle = containerCache
                   			loc = " in " + vehicle + " inside " + p.getBuildingLocation() + " in " + vehicle.getSettlement();
        				}

        			} else {
            			// case E
            			loc = " in " + containerCache + " gone outside on a mission ";
        			}
        		}
        	}
        	else if (unit instanceof Robot) {
        		Robot r = (Robot) unit;
        		if (r.getLocationSituation() == LocationSituation.IN_SETTLEMENT)
        			// case A
        			loc = " at " + r.getBuildingLocation().getNickName() + " in " + topContainerCache;
        		else if (r.getLocationSituation() == LocationSituation.IN_VEHICLE) {
         			if (r.getSettlement() != null)
        				// case C
               			loc = " in " + containerCache + " inside a garage";
        			else {
             			Vehicle vehicle = (Vehicle) unit.getContainerUnit();
            	     	// Note: a vehicle's container unit may be null if it's outside a settlement
            			Settlement settlement = (Settlement) vehicle.getContainerUnit();

        				if (settlement == null)
            				// case E
               				loc = " in " + containerCache + " on a mission outside";
        				else
        					// case D
        					//loc = " in a vehicle parked within the premise of a settlement";
        					loc = " in " + containerCache + " parked within the premise of " + settlement;
        			}
        		}
        	}
        }

        lcdText.setLcdText(loc);

    }
}
