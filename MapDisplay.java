/**
 * Mars Simulation Project
 * MapDisplay.java
 * @version 2.70 2000-09-09
 * @author Scott Davis
 */

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

    private VirtualMars mars;
    private NavigatorWindow navWindow;        // Navigator Tool Window
    private Map surfMap;                      // Surface image object
    private Map topoMap;                      // Topographical image object
    private boolean wait;                     // True if map is in pause mode
    private Coordinates centerCoords;         // Spherical coordinates for center point of map
    private Thread showThread;                // Refresh thread
    private boolean topo;                     // True if in topographical mode, false if in real surface mode
    private boolean recreate;                 // True if surface needs to be regenerated
    private boolean labels;                   // True if units should display labels
    private Image mapImage;	              // Main image

    private int width;
    private int height;

    public MapDisplay(NavigatorWindow navWindow, int width, int height, VirtualMars mars) {

	this.mars = mars;
	this.width = width;
	this.height = height;
	this.navWindow = navWindow;
	wait = false;
	recreate = true;
	topo = false;
	labels = true;
	centerCoords = new Coordinates(Math.PI / 2D, 0D);
		
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
	
    /** Change label display flag */
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

    /** Display surface with new coords, regenerating image if necessary */
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
	while(true) {
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
		try { showThread.sleep(2000); }
		catch (InterruptedException e) {}
		repaint();
	    }
	}
    }

    /** Overrides paintComponent method.  Displays map image or
     *  "Preparing Map..." message. */
    public void paintComponent(Graphics g) {
	super.paintComponent(g);

	if (wait) {
	    // If in waiting mode, display "Preparing Map..."
	    if (mapImage != null) g.drawImage(mapImage, 0, 0, this);
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

    private void drawUnits(Graphics g) {
	
	for (int x=0; x < mars.getAllUnits().getUnitCount(); x++) {
	    Unit u = mars.getAllUnits().getUnit(x);

	    if (u.isDrawn()) {
		UnitInfo info = u.getUnitInfo();

		// what's this .48587 magic number?
		if (centerCoords.getAngle(info.getCoords()) < .48587D) {
		    IntPoint rectLocation = getUnitRectPosition(info.getCoords());
		    IntPoint imageLocation = getUnitDrawLocation(rectLocation, u.getSurfIcon());
		    if (topo) {
			g.drawImage(u.getTopoIcon(), imageLocation.getiX(), imageLocation.getiY(), this);
		    } else {
			g.drawImage(u.getSurfIcon(), imageLocation.getiX(), imageLocation.getiY(), this);
		    }
		    if (labels) {
			g.setColor(u.getLabelColor(topo));
			g.setFont(u.getLabelFont());
			IntPoint labelLocation = getLabelLocation(imageLocation);
			g.drawString(info.getName(), labelLocation.getiX(), labelLocation.getiY());
		    }
		}
	    }
	}
    }

    /** MouseListener methods overridden. Perform appropriate action
     *  on mouse release. */
    public void mouseReleased(MouseEvent event) { 
	
	// what's this -149 magic number?
	Coordinates clickedPosition = centerCoords.convertRectToSpherical((double) event.getX() - 149D,
									  (double) event.getY() - 149D);
	boolean unitsClicked = false;
	
	UnitInfo[] movingVehicleInfo = mars.getMovingVehicleInfo();

	// check if user clicked on a vehicle
	// note: an event should really be generated by the vehicle itself
	for (int x=0; x < movingVehicleInfo.length; x++) {
	    if (movingVehicleInfo[x].getCoords().getDistance(clickedPosition) < 40D) {
		navWindow.openUnitWindow(movingVehicleInfo[x].getID());
		unitsClicked = true;
	    }
	}
		
	UnitInfo[] settlementInfo = mars.getSettlementInfo();
		
	// check if user clicked on a settlement
	// note: an event should really be generated by the settlement itself
	for (int x=0; x < settlementInfo.length; x++) {
	    if (settlementInfo[x].getCoords().getDistance(clickedPosition) < 90D) {
		navWindow.openUnitWindow(settlementInfo[x].getID());
		unitsClicked = true;
	    }
	}
		
	if (!unitsClicked) {
	    navWindow.updateCoords(clickedPosition);
	}
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseClicked(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
	
    /** Returns unit x, y position on map panel */
    private IntPoint getUnitRectPosition(Coordinates unitCoords) {
	
	double rho = 1440D / Math.PI;
	int half_map = 720;
	int low_edge = half_map - 150;
	
	return Coordinates.findRectPosition(unitCoords, centerCoords, rho, half_map, low_edge);
    }
	
    /** Returns unit image draw position on map panel */
    private IntPoint getUnitDrawLocation(IntPoint unitPosition, Image unitImage) {

	return new IntPoint(unitPosition.getiX() - Math.round(unitImage.getWidth(this) / 2),
			    unitPosition.getiY() - Math.round(unitImage.getHeight(this) / 2));
    }

    private static final int labelHorizOffset = 10;
    
    /** Returns label draw postion on map panel */
    private IntPoint getLabelLocation(IntPoint unitPosition, Image unitImage) {
	// this differs from getUnitDrawLocation by adding 10 to the horizontal position
	return new IntPoint(unitPosition.getiX() + Math.round(unitImage.getWidth(this) / 2) +
			    labelHorizOffset,
			    unitPosition.getiY() + Math.round(unitImage.getHeight(this) / 2));
    }

    private IntPoint getLabelLocation(IntPoint iconLocation) {
	return new IntPoint(iconLocation.getiX() + labelHorizOffset,
			    iconLocation.getiY());
    }
}
