/**
 * Mars Simulation Project
 * MapDisplay.java
 * @version 2.70 2000-09-01
 * @author Scott Davis
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** The MapDisplay class is a display component for the surface map of
 *  Mars in the UI. It can show either the surface or topographical
 *  maps at a given point. It uses two SurfaceMap objects to display
 *  the maps.
 *
 *  It will recenter the map on the location of a mouse click, or will
 *  alternatively open a vehicle or settlement window if one of their
 *  icons is clicked.
 */
public class MapDisplay extends JComponent implements MouseListener, Runnable {

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
    private Image vehicleSymbol;              // Real vehicle symbol
    private Image topoVehicleSymbol;          // Topograhical vehicle symbol
    private Image settlementSymbol;           // Real settlement symbol
    private Image topoSettlementSymbol;       // Topographical settlement symbol

    private int width;
    private int height;

    public MapDisplay(NavigatorWindow navWindow, int width, int height) {

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
	//surfMap = new SurfMarsMap(this);
	surfMap = new USGSMarsMap(this);
	topoMap = new TopoMarsMap(this);

	// Load vehicle and settlement images
	vehicleSymbol = Vehicle.getSurfIcon();
	topoVehicleSymbol = Vehicle.getTopoIcon();
	settlementSymbol = Settlement.getSurfIcon();
	topoSettlementSymbol = Settlement.getTopoIcon();
	
	// initially show real surface map (versus topo map)
	showSurf();
    }
	
    /** Change label display flag */
    public void setLabels(boolean labels) {
	this.labels = labels;
    }

    /** Displays real surface map */
    public void showSurf() {
	if (topo) {
	    wait = true;
	    recreate = true;
	}
	topo = false;
	showMap(centerCoords);
    }

    /** Displays topographical map */
    public void showTopo() {
	if (!topo) {
	    wait = true;
	    recreate = true;
	}
	topo = true;
	showMap(centerCoords);
    }

    /** Displays surface with new coords, regenerating image if necessary */
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

	    drawVehicles(g);
	    drawSettlements(g);
	}
    }

    private void drawVehicles(Graphics g) {
	// topo=black, surf=white
	g.setColor(topo ? Color.black : Color.white);

	// Draw a vehicle symbol for each moving vehicle within the viewing map
	g.setFont(new Font("Helvetica", Font.PLAIN, 9));
	
	UnitInfo[] vehicleInfo = navWindow.getMovingVehicleInfo();
			
	for (int x=0; x < vehicleInfo.length; x++) {
	    // what's this .48587 magic number?
	    if (centerCoords.getAngle(vehicleInfo[x].getCoords()) < .48587D) {
		IntPoint rectLocation = getUnitRectPosition(vehicleInfo[x].getCoords());
		IntPoint imageLocation = getUnitDrawLocation(rectLocation, vehicleSymbol);
		if (topo) {
		    g.drawImage(topoVehicleSymbol, imageLocation.getiX(), imageLocation.getiY(), this);
		} else {
		    g.drawImage(vehicleSymbol, imageLocation.getiX(), imageLocation.getiY(), this);
		}
		    
		if (labels) {
		    IntPoint labelLocation = getLabelLocation(rectLocation, vehicleSymbol);
		    g.drawString(vehicleInfo[x].getName(), labelLocation.getiX(), labelLocation.getiY());	
		}
	    }
	}
    }
			
    private void drawSettlements(Graphics g) {
	// topo=black, surf=green
	g.setColor(topo ? Color.black : Color.green);

	// Draw a settlement symbol for each settlement within the viewing map
	g.setFont(new Font("Helvetica", Font.PLAIN, 12));

	UnitInfo[] settlementInfo = navWindow.getSettlementInfo();

	for (int x=0; x < settlementInfo.length; x++) {
	    // what's this .48587 magic number?
	    if (centerCoords.getAngle(settlementInfo[x].getCoords()) < .48587D) {
		IntPoint rectLocation = getUnitRectPosition(settlementInfo[x].getCoords());
		IntPoint imageLocation = getUnitDrawLocation(rectLocation, settlementSymbol);
		if (topo) {
		    g.drawImage(topoSettlementSymbol, imageLocation.getiX(), imageLocation.getiY(), this);
		} else {
		    g.drawImage(settlementSymbol, imageLocation.getiX(), imageLocation.getiY(), this);
		}
		if (labels) {
		    IntPoint labelLocation = getLabelLocation(rectLocation, settlementSymbol);
		    g.drawString(settlementInfo[x].getName(), labelLocation.getiX(), labelLocation.getiY());
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
	
	UnitInfo[] movingVehicleInfo = navWindow.getMovingVehicleInfo();

	// check if user clicked on a vehicle
	// note: an event should really be generated by the vehicle itself
	for (int x=0; x < movingVehicleInfo.length; x++) {
	    if (movingVehicleInfo[x].getCoords().getDistance(clickedPosition) < 40D) {
		navWindow.openUnitWindow(movingVehicleInfo[x].getID());
		unitsClicked = true;
	    }
	}
		
	UnitInfo[] settlementInfo = navWindow.getSettlementInfo();
		
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
    
    /** Returns label draw postion on map panel */
    private IntPoint getLabelLocation(IntPoint unitPosition, Image unitImage) {
		
	return new IntPoint(unitPosition.getiX() + Math.round(unitImage.getWidth(this) / 2) + 10,
			    unitPosition.getiY() + Math.round(unitImage.getHeight(this) / 2));
    }
}
