/**
 * Mars Simulation Project
 * MapDisplay.java
 * @version 2.75 2003-08-03
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.ui.standard.unit_display_info.*;

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
    private int[] shadingArray;  // Array used to generate day/night shading image
    private boolean showDayNightShading; // True if day/night shading is to be used
    private boolean showVehicleTrails; // True if vehicle trails are to be displayed.

    private int width;
    private int height;

    // Constant data members
    private static final double HALF_PI = (Math.PI / 2D);
    private static final int HALF_MAP = 150;
    private static final double HALF_MAP_ANGLE_STANDARD = .48587D;
    private static final double HALF_MAP_ANGLE_USGS = .06106D;
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
        shadingArray = new int[width * height];
        showDayNightShading = false;
        showVehicleTrails = true;

        // Set component size
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());

        // Set background color to black
        setBackground(Color.black);

        // Set mouse listener
        addMouseListener(this);

        // Create surface objects for both real, USGS and topographical modes
        topoMap = new TopoMarsMap(this);
        surfMap = new SurfMarsMap(this);
        usgsMap = new USGSMarsMap(this);
        useUSGSMap = false;

        // initially show real surface map (versus topo map)
        showSurf();
    }

	/** Set USGS as surface map
     *  @param useUSGSMap true if using USGS map.
     */
    public void setUSGSMap(boolean useUSGSMap) {
    	if (!topo && (this.useUSGSMap != useUSGSMap)) recreate = true;
    	this.useUSGSMap = useUSGSMap;
    }

    /** Change label display flag
     *  @param labels true if labels are to be displayed
     */
    public void setLabels(boolean labels) {
        this.labels = labels;
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

    /** Display topographical map */
    public void showTopo() {
        if (!topo) {
            wait = true;
            recreate = true;
        }
        topo = true;
        showMap(centerCoords);
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
                // Regenerate surface if recreate is true, then display
                if (topo) topoMap.drawMap(centerCoords);
                else {
                	if (useUSGSMap) usgsMap.drawMap(centerCoords);
                	else surfMap.drawMap(centerCoords);
                }
                recreate = false;
                repaint();
            } else {
                // Pause for 2000 milliseconds between display refreshs
                try {
                    Thread.sleep(2000);
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

        if (wait) {
            // If in waiting mode, display wait string
            if (mapImage != null) g.drawImage(mapImage, 0, 0, this);

            if (topo) g.setColor(Color.black);
            else g.setColor(Color.green);

            String message = "Generating Map";
            if (useUSGSMap) message = "Downloading Map";
            Font messageFont = new Font("SansSerif", Font.BOLD, 25);
            FontMetrics messageMetrics = getFontMetrics(messageFont);
            int msgHeight = messageMetrics.getHeight();
            int msgWidth = messageMetrics.stringWidth(message);
            int x = (width - msgWidth) / 2;
            int y = (height + msgHeight) / 2;
            g.setFont(messageFont);
            g.drawString(message, x, y);
            wait = false;
        } 
        else {
            // Paint black background
            g.setColor(Color.black);
            g.fillRect(0, 0, width, height);

            // Paint topo, real or USGS surface image
            Map map = null;
            if (topo) map = topoMap;
            else {
            	if (useUSGSMap) map = usgsMap;
            	else map = surfMap;
            }

            if (map.isImageDone()) {
                mapImage = map.getMapImage();
                g.drawImage(mapImage, 0, 0, this);
            }

            if (!topo && showDayNightShading) drawShading(g);
            if (showVehicleTrails) drawVehicleTrails(g);
            drawUnits(g);
        }
    }

    /** Draws the day/night shading on the map.
     *  @param g graphics context
     */
    protected void drawShading(Graphics g) {
        int centerX = width / 2;
        int centerY = width / 2;

        Coordinates sunDirection = mars.getOrbitInfo().getSunDirection();

        double rho = 1440D / Math.PI;
        if (useUSGSMap) rho = 11458D / Math.PI;

        boolean nightTime = true;
        boolean dayTime = true;
        Coordinates location = new Coordinates(0D, 0D);
        for (int x = 0; x < width; x+=2) {
            for (int y = 0; y < height; y+=2) {
                centerCoords.convertRectToSpherical(x - centerX, y - centerY, rho, location);
                int sunlight = mars.getSurfaceFeatures().getSurfaceSunlight(location);
                int shadeColor = ((127 - sunlight) << 24) & 0xFF000000;
                shadingArray[x + (y * width)] = shadeColor;
                shadingArray[x + 1 + (y * width)] = shadeColor;
                if (y < height -1) {
                    shadingArray[x + ((y + 1) * width)] = shadeColor;
                    shadingArray[x + 1 + ((y + 1) * width)] = shadeColor;
                }
                // shadingArray[x + (y * width)] = ((127 - sunlight) << 24) & 0xFF000000;
                if (sunlight > 0) nightTime = false;
                if (sunlight < 127) dayTime = false;
            }
        }
        if (nightTime) {
            g.setColor(new Color(0, 0, 0, 128));
            g.fillRect(0, 0, width, height);
        }
        else if (!dayTime) {
            // Create shading image for map
            Image shadingMap = this.createImage(new MemoryImageSource(width, height, shadingArray, 0, width));

            MediaTracker mt = new MediaTracker(this);
            mt.addImage(shadingMap, 0);
            try {
                mt.waitForID(0);
            }
            catch (InterruptedException e) {
                System.out.println("MapDisplay - ShadingMap interrupted: " + e);
            }

            // Draw the shading image
            g.drawImage(shadingMap, 0, 0, this);
        }
    }

    /** Draws units on map
     *  @param g graphics context
     */
    private void drawUnits(Graphics g) {
        UnitIterator i = mars.getUnitManager().getUnits().iterator();
        while (i.hasNext()) {
            Unit unit = i.next();
            UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
            if (displayInfo.isMapDisplayed(unit)) {
                Coordinates unitCoords = unit.getCoordinates();
                double angle = 0D;
                if (useUSGSMap && !topo) angle = HALF_MAP_ANGLE_USGS;
                else angle = HALF_MAP_ANGLE_STANDARD;

                if (centerCoords.getAngle(unitCoords) < angle) {
                    IntPoint rectLocation = getUnitRectPosition(unitCoords);
                    IntPoint imageLocation =
                            getUnitDrawLocation(rectLocation, displayInfo.getSurfMapIcon());

                    if (topo) displayInfo.getTopoMapIcon().paintIcon(this, g, 
                        imageLocation.getiX(), imageLocation.getiY());
                    else displayInfo.getSurfMapIcon().paintIcon(this, g, 
                        imageLocation.getiX(), imageLocation.getiY());

                    if (labels) {
                        if (topo) g.setColor(displayInfo.getTopoMapLabelColor());
                        else g.setColor(displayInfo.getSurfMapLabelColor());
                        g.setFont(displayInfo.getMapLabelFont());
                        IntPoint labelLocation = getLabelLocation(rectLocation, displayInfo.getSurfMapIcon());
                        g.drawString(unit.getName(), labelLocation.getiX(), labelLocation.getiY());
                    }
                }
            }
        }
    }

    /**
     * Draws vehicle trails.
     * @param g graphics context
     */
    private void drawVehicleTrails(Graphics g) {
        
        // Set trail color
        if (topo) g.setColor(Color.black);
        else g.setColor(new Color(0, 96, 0));
        
        // Get map angle
        double angle = 0D;
        if (useUSGSMap && !topo) angle = HALF_MAP_ANGLE_USGS;
        else angle = HALF_MAP_ANGLE_STANDARD;
        
        // Draw trail
        VehicleIterator i = mars.getUnitManager().getVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            IntPoint oldSpot = null;
            Iterator j = (new ArrayList(vehicle.getTrail())).iterator();
            while (j.hasNext()) {
                Coordinates trailSpot = (Coordinates) j.next();
                if (centerCoords.getAngle(trailSpot) < angle) {
                    IntPoint spotLocation = getUnitRectPosition(trailSpot);
                    if ((oldSpot == null))                            
                        g.drawRect(spotLocation.getiX(), spotLocation.getiY(), 1, 1);
                    else if (!spotLocation.equals(oldSpot))
                        g.drawLine(oldSpot.getiX(), oldSpot.getiY(), spotLocation.getiX(), spotLocation.getiY());
                    oldSpot = spotLocation;
                }
            }
        }
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

    /** Returns unit x, y position on map panel
     *  @param unitCoords location of unit
     *  @return display point on map
     */
    private IntPoint getUnitRectPosition(Coordinates unitCoords) {

        double rho;
        int half_map;

        if (useUSGSMap && !topo) {
            rho = 11458D / Math.PI;
            half_map = 11458 / 2;
        }
        else {
            rho = 1440D / Math.PI;
            half_map = 1440 / 2;
       	}

        int low_edge = half_map - 150;

        return Coordinates.findRectPosition(unitCoords, centerCoords, rho, half_map, low_edge);
    }

    /** 
     * Gets the unit image draw position on map panel.
     *
     * @param unitPosition absolute unit position
     * @param unitIcon unit's map image icon
     * @return draw position for unit image
     */
    private IntPoint getUnitDrawLocation(IntPoint unitPosition, Icon unitIcon) {
        return new IntPoint(unitPosition.getiX() - (unitIcon.getIconWidth() / 2),
                unitPosition.getiY() - (unitIcon.getIconHeight() / 2));
    }

    /** 
     * Gets the label draw postion on map panel.
     *
     * @param unitPosition absolute unit position
     * @param unitIcon unit's map icon
     * @return draw position for unit label
     */
    private IntPoint getLabelLocation(IntPoint unitPosition, Icon unitIcon) {
        // this differs from getUnitDrawLocation by adding 10 to the horizontal position
        return new IntPoint(unitPosition.getiX() + (unitIcon.getIconWidth() / 2) + 
            LABEL_HORIZONTAL_OFFSET, unitPosition.getiY() + (unitIcon.getIconHeight() / 2));
    }

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
}
