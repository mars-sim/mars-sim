/**
 * Mars Simulation Project
 * MapDisplay.java
 * @version 2.76 2004-05-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.IntPoint;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.UnitIterator;
import org.mars_sim.msp.ui.standard.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.standard.unit_display_info.UnitDisplayInfoFactory;

/** The MapDisplay class is the visual component for the surface map
 *  of Mars in the UI. It can show either the surface or topographical
 *  maps at a given point. It maintains two Map objects; one for the
 *  topographical map image, and one for the surface map image.
 *
 *  It will recenter the map on the location of a mouse click, or open
 *  a vehicle/settlement window if one of their icons is clicked.
 */
public class MapDisplay extends JComponent implements MouseListener, Runnable {

    // Data members
    private Mars mars; // Virtual Mars object
    private NavigatorWindow navWindow; // Navigator Tool Window
    private Map surfMap; // Surface image object
    private Map usgsMap; // USGS surface image object
    private Map topoMap; // Topographical image object
    private boolean wait; // True if map is in pause mode
    private Coordinates centerCoords; // Spherical coordinates for center point of map
    private Thread showThread; // Refresh thread
    private boolean topo; // True if in topographical mode, false if in real surface mode
    private boolean recreate; // True if surface needs to be regenerated
    private boolean labels; // True if units should display labels
    private Image mapImage; // Main image
    private boolean useUSGSMap;  // True if USGS surface map is to be used
    private boolean showDayNightShading; // True if day/night shading is to be used
    private boolean showVehicleTrails; // True if vehicle trails are to be displayed.
    private boolean showLandmarks; // True if landmarks are to be displayed.
    private MapLayer unitLayer;  // Display layer for showing units.
    private MapLayer vehicleTrailLayer;  // Display layer for showing vehicle trails.
    private MapLayer shadingLayer; // Display layer for showing day/night shading.
    private MapLayer landmarkLayer; // Display layer for showing landmarks.
    private boolean mapError; // True if there is an error in rendering the map.
    private String mapErrorMessage; // The map error message.

    private int width;
    private int height;

    // Constant data members
    private static final double HALF_PI = (Math.PI / 2D);
    private static final int HALF_MAP = 150;
    static final double HALF_MAP_ANGLE_STANDARD = .48587D;
    static final double HALF_MAP_ANGLE_USGS = .06106D;
    static final double NORMAL_PIXEL_RHO = 1440D / Math.PI;
    static final double USGS_PIXEL_RHO = 11458D / Math.PI;
    private static final int NORMAL_HALF_MAP = 1440 / 2;
    private static final int USGS_HALF_MAP = 11458 / 2;
    private static final int LABEL_HORIZONTAL_OFFSET = 2;

    /** 
     * Constructor
     *
     * @param navWindow the navigator window pane
     * @param width the width of the map shown
     * @param height the height of the map shown
     * @param mars the Mars instance.
     */
    public MapDisplay(NavigatorWindow navWindow, int width, int height, Mars mars) {

        // Initialize data members
        this.navWindow = navWindow;
        this.width = width;
        this.height = height;
        this.mars = mars;

        wait = false;
        recreate = true;
        topo = false;
        labels = true;
        centerCoords = new Coordinates(HALF_PI, 0D);
        showDayNightShading = false;
        showVehicleTrails = true;
        showLandmarks = true;
        mapError = false;
        mapErrorMessage = null;

        // Set component size
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());

        // Set background color to black
        setBackground(Color.black);

        // Set mouse listener
        addMouseListener(this);

        // Create surface objects for both real, USGS and topographical modes
        topoMap = new TopoMarsMap(this, mars);
        surfMap = new SurfMarsMap(this);
        usgsMap = new USGSMarsMap(this);
        useUSGSMap = false;
        
        // Create map display layers.
        unitLayer = new UnitMapLayer(mars, this);
        vehicleTrailLayer = new VehicleTrailMapLayer(mars, this);
        shadingLayer = new ShadingMapLayer(mars, this);
        landmarkLayer = new LandmarkMapLayer(mars, this);

        // initially show real surface map (versus topo map)
        showSurf();
    }

    /**
     * Gets the map center coordinates.
     * @return map center
     */
    public Coordinates getMapCenter() {
        return centerCoords;
    }
    
	/** 
     * Set USGS as surface map
     * @param useUSGSMap true if using USGS map.
     */
    public void setUSGSMap(boolean useUSGSMap) {
    	if (!topo && (this.useUSGSMap != useUSGSMap)) recreate = true;
    	this.useUSGSMap = useUSGSMap;
    }
    
    /**
     * Checks if using a USGS map.
     * @return true if USGS map
     */
    public boolean isUsgs() {
        return useUSGSMap;
    }

    /** 
     * Change unit label display flag
     * @param labels true if labels are to be displayed
     */
    public void setUnitLabels(boolean labels) {
        this.labels = labels;
    }
    
    /**
     * Checks if unit labels are displayed.
     * @return true if labels are displayed
     */
    public boolean useUnitLabels() {
        return labels;
    }

    /** Display real surface image */
    public void showSurf() {
        if (topo) {
            wait = true;
            recreate = true;
        }
        topo = false;
        showMap(centerCoords);
    }
    
    /**
     * Checks if showing the surface map.
     * @return true if surface map.
     */
    public boolean isSurface() {
        return !topo;
    }

    /** Display topographical map */
    public void showTopo() {
        if (!topo) {
            wait = true;
            recreate = true;
        }
        topo = true;
        showMap(centerCoords);
    }
    
    /**
     * Checks if showing the topo map.
     * @return true if topo map.
     */
    public boolean isTopo() {
        return topo;
    }

    /** Display surface with new coords, regenerating image if necessary
     *  @param newCenter new center location for map
     */
    public void showMap(Coordinates newCenter) {
        if (!centerCoords.equals(newCenter)) {
            wait = true;
            recreate = true;
            centerCoords.setCoords(newCenter);
        }
        updateDisplay();
    }

    /** updates the current display */
    private void updateDisplay() {
        if ((showThread == null) || (!showThread.isAlive())) {
            // we need to create the display thread
            showThread = new Thread(this, "Map");
            showThread.start();
        } else {
            showThread.interrupt();
        }
    }

    /** the run method for the runnable interface */
    public void run() {
        refreshLoop();
    }

    /** loop, refreshing the display when necessary */
    private void refreshLoop() {
        while (true) {
            if (recreate) {
                // Regenerate surface if recreate is true, then display.
                mapError = false;
                try {
                    if (topo) topoMap.drawMap(centerCoords);
                    else {
                	    if (useUSGSMap) usgsMap.drawMap(centerCoords);
                	    else surfMap.drawMap(centerCoords);
                    }
                }
                catch (Exception e) {
                    mapError = true;
                    mapErrorMessage = e.getMessage();
                    wait = false;
                }
                
                recreate = false;
                repaint();
            } else {
                // Check if bad connection for USGS map.
                if (useUSGSMap && ((USGSMarsMap) usgsMap).isConnectionTimeout()) {
                    mapError = true;
                    mapErrorMessage = "Unable to Connect";
                    wait = false;
                }
                
                // Set thread sleep time.
				long sleepTime = 2000;
				if (wait) sleepTime = 100;
                
                // Pause for 2000 milliseconds between display refreshs
                try {
                    // Thread.sleep(2000);
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {}
                repaint();
            }
        }
    }

    /** Overrides paintComponent method.  Displays map image or
     *  "Preparing Map..." message.
     *  @param g graphics context
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // System.out.println("paintComponent()");
        // Determine map type.
        Map map = null;
        if (isTopo()) map = topoMap;
        else {
            if (isUsgs()) map = usgsMap;
            else map = surfMap;
        }
        
        if (wait) {
        	// System.out.println("wait");
            // display previous map image.
            if (mapImage != null) g.drawImage(mapImage, 0, 0, this);

            // Create the message string.
            String message = "Generating Map";
            if (isUsgs()) message = "Downloading Map";
            
            // Draw message
            drawCenteredMessage(message, g);
            
            if (map.isImageDone() || mapError) wait = false;
            // System.out.println("end wait");
        } 
        else {
        	// System.out.println("Go");
            if (mapError) {
            	System.out.println("mapError");
                // Display previous map image
                if (mapImage != null) g.drawImage(mapImage, 0, 0, this);
                
                // Draw error message
                drawCenteredMessage(mapErrorMessage, g);
            }
            else {
                // Paint black background
                g.setColor(Color.black);
                g.fillRect(0, 0, width, height);

                if (map.isImageDone()) {
                    mapImage = map.getMapImage();
                    g.drawImage(mapImage, 0, 0, this);
                }

                // Display day/night shading.
                if (isSurface() && showDayNightShading) shadingLayer.displayLayer(g);
            
				// Display landmarks.
				if (showLandmarks) landmarkLayer.displayLayer(g);
            
                // Display vehicle trails.
                if (showVehicleTrails) vehicleTrailLayer.displayLayer(g);
            
                // Display units.
                unitLayer.displayLayer(g);
            }
            // System.out.println("end go");
        }
    }
    
    /**
     * Draws a message string in the center of the map display.
     *
     * @param message the message string
     * @param g the graphics context
     */
    private void drawCenteredMessage(String message, Graphics g) {
        
        // Set message color
        if (isTopo()) g.setColor(Color.black);
        else g.setColor(Color.green);
        
        // Set up font
        Font messageFont = new Font("SansSerif", Font.BOLD, 25);
        g.setFont(messageFont);
        FontMetrics messageMetrics = getFontMetrics(messageFont);
        
        // Determine message dimensions
        int msgHeight = messageMetrics.getHeight();
        int msgWidth = messageMetrics.stringWidth(message);
        
        // Determine message draw position
        int x = (width - msgWidth) / 2;
        int y = (height + msgHeight) / 2;
    
        // Draw message
        g.drawString(message, x, y);
    }
    
    /** MouseListener methods overridden. Perform appropriate action
     *  on mouse click. */
    public void mouseClicked(MouseEvent event) {

        double rho;
        if (useUSGSMap && !topo) rho = 11458D / Math.PI;
        else rho = 1440D / Math.PI;

        Coordinates clickedPosition = centerCoords.convertRectToSpherical(
                (double)(event.getX() - HALF_MAP - 1),
                (double)(event.getY() - HALF_MAP - 1), rho);
        boolean unitsClicked = false;

        UnitIterator i = mars.getUnitManager().getUnits().iterator();

        // Open window if unit is clicked on the map
        while (i.hasNext()) {
            Unit unit = i.next();
            UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
            if (displayInfo.isMapDisplayed(unit)) {
                Coordinates unitCoords = unit.getCoordinates();
                double clickRange = unitCoords.getDistance(clickedPosition);
                double unitClickRange = displayInfo.getMapClickRange();
                if (useUSGSMap && !topo) unitClickRange *= .1257D;
                if (clickRange < unitClickRange) {
                    navWindow.openUnitWindow(unit);
                    unitsClicked = true;
                }
            }
        }

        if (!unitsClicked) navWindow.updateCoords(clickedPosition);
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}

    /** Sets day/night tracking to on or off.
     *  @param showDayNightShading true if map is to use day/night tracking.
     */
    public void setDayNightTracking(boolean showDayNightShading) {
        this.showDayNightShading = showDayNightShading;
    }

    /**
     * Sets the vehicle trails flag.
     * @param showVehicleTrails true if vehicle trails are to be displayed.
     */
    public void setVehicleTrails(boolean showVehicleTrails) {
        this.showVehicleTrails = showVehicleTrails;
    }
    
	/**
	 * Sets the landmarks flag.
	 * @param showLandmarks true if landmarks are to be displayed.
	 */
    public void setLandmarks(boolean showLandmarks) {
    	this.showLandmarks = showLandmarks;
    }
    
    /** 
     * Gets a coordinate x, y position on the map image.
     *
     * @param coords location of unit
     * @return display point on map
     */
    IntPoint getRectPosition(Coordinates coords) {

        double rho;
        int half_map;

        if (isUsgs() && isSurface()) {
            rho = USGS_PIXEL_RHO;
            half_map = USGS_HALF_MAP;
        }
        else {
            rho = NORMAL_PIXEL_RHO;
            half_map = NORMAL_HALF_MAP;
       	}

        int low_edge = half_map - 150;

        return Coordinates.findRectPosition(coords, getMapCenter(), 
            rho, half_map, low_edge);
    }
    
    /**
     * Gets the width of the map display.
     *
     * @return width as int.
     */
    public int getWidth() { 
        return width;
    }
    
    /**
     * Gets the height of the map display.
     *
     * @return height as int.
     */
    public int getHeight() {
        return height;
    }
}