/**
 * Mars Simulation Project
 * MapDisplay.java
 * @version 2.71 2000-10-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import org.mars_sim.msp.simulation.*; 
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

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
    private UIProxyManager proxyManager; // Unit UI proxy manager
    private NavigatorWindow navWindow; // Navigator Tool Window
    private Map surfMap; // Surface image object
    private Map topoMap; // Topographical image object
    private boolean wait; // True if map is in pause mode
    private Coordinates centerCoords; // Spherical coordinates for center point of map
    private Thread showThread; // Refresh thread
    private boolean topo; // True if in topographical mode, false if in real surface mode
    private boolean recreate; // True if surface needs to be regenerated
    private boolean labels; // True if units should display labels
    private Image mapImage; // Main image

    private int width;
    private int height;
    
    // Constant data members
    private static final double HALF_PI = (Math.PI / 2D);
    private static final int HALF_MAP = 150;
    private static final double HALF_MAP_ANGLE = .48587D;

    /** Constructs a MapDisplay object 
     *  @param navWindow the navigator window pane
     *  @param proxyManager the UI proxy manager
     *  @param width the width of the map shown
     *  @param height the height of the map shown
     */
    public MapDisplay(NavigatorWindow navWindow, UIProxyManager proxyManager, 
            int width, int height) {

        // Initialize data members
        this.navWindow = navWindow;
        this.proxyManager = proxyManager;
        this.width = width;
        this.height = height;
      
        wait = false;
        recreate = true;
        topo = false;
        labels = true;
        centerCoords = new Coordinates(HALF_PI, 0D);

        // Set component size
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());

        // Set mouse listener
        addMouseListener(this);

        // Create surface objects for both real and topographical modes
        surfMap = new SurfMarsMap(this);
        //surfMap = new USGSMarsMap(this);
        topoMap = new TopoMarsMap(this);

        // initially show real surface map (versus topo map)
        showSurf();
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
            /*
             try {
             wait();
             } catch (InterruptedException e) {}
             */
           
            if (recreate) {
                // Regenerate surface if recreate is true, then display
                if (topo) {
                    topoMap.drawMap(centerCoords);
                } else {
                    surfMap.drawMap(centerCoords);
                }
                recreate = false;
                repaint();
            } else {
                // Pause for 2000 milliseconds between display refreshs
                try {
                    showThread.sleep(2000);
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
            // If in waiting mode, display "Preparing Map..."
            if (mapImage != null)
                g.drawImage(mapImage, 0, 0, this);
            g.setColor(Color.green);
            String message = "Preparing Map...";
            Font alertFont = new Font("TimesRoman", Font.BOLD, 30);
            FontMetrics alertMetrics = getFontMetrics(alertFont);
            int msgHeight = alertMetrics.getHeight();
            int msgWidth = alertMetrics.stringWidth(message);
            int x = (width - msgWidth) / 2;
            int y = (height + msgHeight) / 2;
            g.setFont(alertFont);
            g.drawString(message, x, y);
            wait = false;
        } else {
            // Paint black background
            g.setColor(Color.black);
            g.fillRect(0, 0, width, height);

            // Paint topo or real surface image
            Map map = topo ? topoMap : surfMap;

            if (map.isImageDone()) {
                mapImage = map.getMapImage();
                g.drawImage(mapImage, 0, 0, this);
            }

            drawUnits(g);
        }
    }

    /** Draws units on map 
     *  @param g graphics context
     */
    private void drawUnits(Graphics g) {
        UnitUIProxy[] proxies = proxyManager.getUIProxies();
        for (int x = 0; x < proxies.length; x++) {
            if (proxies[x].isMapDisplayed()) {
                Coordinates unitCoords = proxies[x].getUnit().getCoordinates();
                if (centerCoords.getAngle(unitCoords) < HALF_MAP_ANGLE) {
                    IntPoint rectLocation = getUnitRectPosition(unitCoords);
                    Image positionImage = proxies[x].getSurfMapIcon().getImage();
                    IntPoint imageLocation =
                            getUnitDrawLocation(rectLocation, positionImage);
                            
                    if (topo) {
                        g.drawImage(proxies[x].getTopoMapIcon().getImage(), 
                                imageLocation.getiX(), imageLocation.getiY(), this);
                    } else {
                        g.drawImage(proxies[x].getSurfMapIcon().getImage(), 
                                imageLocation.getiX(), imageLocation.getiY(), this);
                    }
                    
                    if (labels) {
                        if (topo) g.setColor(proxies[x].getTopoMapLabelColor());
                        else g.setColor(proxies[x].getSurfMapLabelColor());
                        g.setFont(proxies[x].getMapLabelFont());
                        IntPoint labelLocation = getLabelLocation(rectLocation, positionImage);
                        g.drawString(proxies[x].getUnit().getName(), labelLocation.getiX() + 
                                labelHorizOffset, labelLocation.getiY());
                    }
                }
            }
        }
    }

    /** MouseListener methods overridden. Perform appropriate action
      *  on mouse release. */
    public void mouseReleased(MouseEvent event) {

        Coordinates clickedPosition =
                centerCoords.convertRectToSpherical((double) (event.getX() -
                HALF_MAP - 1), (double) (event.getY() - HALF_MAP - 1));
        boolean unitsClicked = false;

        UnitUIProxy[] proxies = proxyManager.getUIProxies();
        
        // Open window if unit is clicked on the map
        for (int x=0; x < proxies.length; x++) {
            if (proxies[x].isMapDisplayed()) {
                Coordinates unitCoords = proxies[x].getUnit().getCoordinates();
                double clickRange = unitCoords.getDistance(clickedPosition);
                if (clickRange < proxies[x].getMapClickRange()) {
                    navWindow.openUnitWindow(proxies[x]);
                    unitsClicked = true;
                }
            }
        }

        if (!unitsClicked) navWindow.updateCoords(clickedPosition);
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseClicked(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}

    /** Returns unit x, y position on map panel 
     *  @param unitCoords location of unit
     *  @return display point on map
     */
    private IntPoint getUnitRectPosition(Coordinates unitCoords) {

        double rho = 1440D / Math.PI;
        int half_map = 720;
        int low_edge = half_map - 150;

        return Coordinates.findRectPosition(unitCoords, centerCoords, rho,
                half_map, low_edge);
    }

    /** Returns unit image draw position on map panel 
     *  @param unitPosition absolute unit position
     *  @param unitImage unit's map image
     *  @return draw position for unit image
     */
    private IntPoint getUnitDrawLocation(IntPoint unitPosition, Image unitImage) {
        return new IntPoint(unitPosition.getiX() - (unitImage.getWidth(this) / 2),
                unitPosition.getiY() - (unitImage.getHeight(this) / 2));
    }

    private static final int labelHorizOffset = 10;

    /** Returns label draw postion on map panel 
     *  @param unitPosition absolute unit position
     *  @param unitImage unit's map image
     *  @return draw position for unit label
     */
    private IntPoint getLabelLocation(IntPoint unitPosition, Image unitImage) {
        // this differs from getUnitDrawLocation by adding 10 to the horizontal position
        return new IntPoint(unitPosition.getiX() + (unitImage.getWidth(this) / 2) + labelHorizOffset,
                unitPosition.getiY() + (unitImage.getHeight(this) / 2));
    }
}
