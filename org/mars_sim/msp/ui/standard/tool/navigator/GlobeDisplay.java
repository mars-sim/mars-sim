/**
 * Mars Simulation Project
 * GlobeDisplay.java
 * @version 2.76 2004-06-02
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.navigator;
 
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.MemoryImageSource;

import javax.swing.JComponent;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.IntPoint;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.UnitIterator;
import org.mars_sim.msp.simulation.mars.Mars;
import org.mars_sim.msp.ui.standard.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.standard.unit_display_info.UnitDisplayInfoFactory;

/** 
 * The Globe Display class displays a graphical globe of Mars in the
 * Navigator tool.
 */
class GlobeDisplay extends JComponent implements Runnable {

    // Data members
    private MarsGlobe marsSphere; // Real surface sphere object
    private MarsGlobe topoSphere; // Topographical sphere object
    private Coordinates centerCoords; // Spherical coordinates for globe center
    private Thread showThread; // Refresh thread
    private boolean topo; // True if in topographical mode, false if in real surface mode
    private boolean recreate; // True if globe needs to be regenerated
    private int width; // width of the globe display component
    private int height; // height of the globe display component
    private boolean useUSGSMap;  // True if USGS surface map is to be used
    private int[] shadingArray; // Array used to generate day/night shading image
    private boolean showDayNightShading; // True if day/night shading is to be used

    private static final double HALF_PI = (Math.PI / 2);

    /** 
     * Constructor 
     *
     * @param width the width of the globe display
     * @param height the height of the globe display
     */
    public GlobeDisplay(int width, int height) {

        this.width = width;
        this.height = height;

        // Set component size
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());

        // Construct sphere objects for both real and topographical modes
        marsSphere = new MarsGlobe("surface", this);
        topoSphere = new MarsGlobe("topo", this);

        // Initialize global variables
        centerCoords = new Coordinates(HALF_PI, 0D);
        topo = false;
        recreate = true;
        useUSGSMap = false;
        shadingArray = new int[width * height];
        showDayNightShading = false;

        // Initially show real surface globe
        showSurf();
    }

    /** Displays real surface globe, regenerating if necessary */
    public void showSurf() {
        if (topo) recreate = true; 
        topo = false;
        showGlobe(centerCoords);
    }

    /** Displays topographical globe, regenerating if necessary */
    public void showTopo() {
        if (!topo) recreate = true;
        topo = true;
        showGlobe(centerCoords);
    }

    /** Displays globe at given center regardless of mode, 
     *  regenerating if necessary 
     *  @param newCenter the center location for the globe
     */
    public void showGlobe(Coordinates newCenter) {
        if (!centerCoords.equals(newCenter)) {
            recreate = true;
            centerCoords.setCoords(newCenter);
        }
        updateDisplay();
    }

    /** Starts display update thread (or creates a new one if necessary) */
    private void updateDisplay() {
        if ((showThread == null) || (!showThread.isAlive())) {
            showThread = new Thread(this, "Globe");
            showThread.start();
        } else {
            showThread.interrupt();
        }
    }

    /** the run method for the runnable interface */
    public void run() { refreshLoop(); }

    /** loop, refreshing the globe display when necessary */
    public void refreshLoop() {
        while (true) { // Endless refresh loop
            if (recreate) {
                // Regenerate globe if recreate is true, then display
                if (topo) {
                    topoSphere.drawSphere(centerCoords);
                } else {
                    marsSphere.drawSphere(centerCoords);
                }
                recreate = false;
                repaint();
            } else {
                // Pause for 2 seconds between display refreshs
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {}
                repaint();
            }
        }
    }

    /** Overrides paintComponent method.  Displays globe, green lines,
     *  longitude and latitude. 
     *  @param g graphics context
     */
    public void paintComponent(Graphics g) {

        // Paint black background
        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);

        // Draw real or topo globe
        MarsGlobe globe = topo ? topoSphere : marsSphere;

        if (globe.isImageDone()) {
            g.drawImage(globe.getGlobeImage(), 0, 0, this);
        }

        if (!topo && showDayNightShading) drawShading(g);

        drawUnits(g);
        drawCrossHair(g);
    }

    /** Draws the day/night shading on the globe.
     * @param g graphics context
     */
    protected void drawShading(Graphics g) {
        int centerX = width / 2;
        int centerY = height / 2;

		Mars mars = Simulation.instance().getMars();
        Coordinates sunDirection = mars.getOrbitInfo().getSunDirection();

        Coordinates location = new Coordinates(0D, 0D);
        for (int x = 0; x < 150; x++) {
            for (int y = 0; y < 150; y++) {
                int xDiff = x - centerX;
                int yDiff = y - centerY;
                if (Math.sqrt((xDiff * xDiff) + (yDiff * yDiff)) <= 47.74648293D) {
                    centerCoords.convertRectToSpherical(xDiff, yDiff, 47.74648293D, location);
                    double sunlight = mars.getSurfaceFeatures().getSurfaceSunlight(location);
                    int sunlightInt = (int) (127 * sunlight);
                    shadingArray[x + (y * width)] = ((127 - sunlightInt) << 24) & 0xFF000000;
                }
                else shadingArray[x + (y * 150)] = 0xFF000000;
            }
        }

        // Create shading image for map
        Image shadingMap = this.createImage(new MemoryImageSource(width, height, shadingArray, 0, width));

        MediaTracker mt = new MediaTracker(this);
        mt.addImage(shadingMap, 0);
        try {
            mt.waitForID(0);
        }
        catch (InterruptedException e) {
            System.out.println("GlobeDisplay - ShadingMap interrupted: " + e);
        }

        // Draw the shading image
        g.drawImage(shadingMap, 0, 0, this);
    }

    /** draw the dots on the globe that identify units 
     *  @param g graphics context
     */
    protected void drawUnits(Graphics g) {
        UnitIterator i = Simulation.instance().getUnitManager().getUnits().iterator();
        while (i.hasNext()) {
            Unit unit = i.next();
            UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
            if (displayInfo.isGlobeDisplayed(unit)) {
                Coordinates unitCoords = unit.getCoordinates();
                if (centerCoords.getAngle(unitCoords) < HALF_PI) {
                    if (topo) g.setColor(displayInfo.getTopoGlobeColor());
                    else g.setColor(displayInfo.getSurfGlobeColor());
                    IntPoint tempLocation = getUnitDrawLocation(unitCoords);
                    g.fillRect(tempLocation.getiX(), tempLocation.getiY(), 1, 1);
                }
            }
        }
    }

    /** Draw green rectanges and lines (cross-hair type thingy), and
      *  write the latitude and logitude of the centerpoint of the
      *  current globe view. 
      *  @param g graphics context
      */
    protected void drawCrossHair(Graphics g) {
        g.setColor(Color.green);

        // If USGS map is used, use small crosshairs.
        if (useUSGSMap & !topo) {
            g.drawRect(72, 72, 6, 6);
            g.drawLine(0, 75, 71, 75);
            g.drawLine(79, 75, 149, 75);
            g.drawLine(75, 0, 75, 71);
            g.drawLine(75, 79, 75, 149);
        }
        // If not USGS map, use large crosshairs.
        else {
            g.drawRect(57, 57, 33, 33);
            g.drawLine(0, 74, 56, 74);
            g.drawLine(90, 74, 149, 74);
            g.drawLine(74, 0, 74, 57);
            g.drawLine(74, 90, 74, 149);
        }

        // Prepare font
        Font positionFont = new Font("Helvetica", Font.PLAIN, 10);
        FontMetrics positionMetrics = getFontMetrics(positionFont);
        g.setFont(positionFont);

        // Draw longitude and latitude strings
        int leftWidth = positionMetrics.stringWidth("Latitude:");
        int rightWidth = positionMetrics.stringWidth("Longitude:");

        g.drawString("Latitude:", 5, 130);
        g.drawString("Longitude:", 145 - rightWidth, 130);

        String latString = centerCoords.getFormattedLatitudeString();
        String longString = centerCoords.getFormattedLongitudeString();

        int latWidth = positionMetrics.stringWidth(latString);
        int longWidth = positionMetrics.stringWidth(longString);

        int latPosition = ((leftWidth - latWidth) / 2) + 5;
        int longPosition = 145 - rightWidth + ((rightWidth - longWidth) / 2);

        g.drawString(latString, latPosition, 142);
        g.drawString(longString, longPosition, 142);
    }

    /** Returns unit x, y position on globe panel 
     *  @param unitCoords the unit's location
     *  @return x, y position on globe panel
     */
    private IntPoint getUnitDrawLocation(Coordinates unitCoords) {
        double rho = width / Math.PI;
        int half_map = (int)(width / 2);
        int low_edge = 0;
        return Coordinates.findRectPosition(unitCoords, centerCoords, rho,
                half_map, low_edge);
    }
    
    /** Set USGS as surface map
     *  @param useUSGSMap true if using USGS map.
     */
    public void setUSGSMap(boolean useUSGSMap) {
    	this.useUSGSMap = useUSGSMap;
    }
    
    /** Sets day/night tracking to on or off.
     *  @param showDayNightShading true if globe is to use day/night tracking.
     */
    public void setDayNightTracking(boolean showDayNightShading) {
        this.showDayNightShading = showDayNightShading;
    }
}
