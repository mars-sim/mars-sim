/**
 * Mars Simulation Project
 * GlobeDisplay.java
 * @version 2.70 2000-09-02
 * @author Scott Davis
 */

import java.awt.*;
import java.util.*;
import javax.swing.*;

/** The Globe Display class displays a graphical globe of Mars in the
 *  "Mars Navigator" tool.
 */
class GlobeDisplay extends JComponent implements Runnable {

    private NavigatorWindow navWindow;      // Navigator Tool Window
    private MarsGlobe marsSphere;           // Real surface sphere object
    private MarsGlobe topoSphere;           // Topographical sphere object
    private Coordinates centerCoords;       // Spherical coordinates for globe center
    private Thread showThread;              // Refresh thread
    private boolean topo;                   // True if in topographical mode, false if in real surface mode
    private boolean recreate;               // True if globe needs to be regenerated
    private int width;                      // width of the globe display component
    private int height;                     // height of the globe display component
    
    private static final double halfPI = (Math.PI / 2);

    public GlobeDisplay(NavigatorWindow navWindow, int width, int height) {

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
	centerCoords = new Coordinates(halfPI, 0D);
	topo = false;
	recreate = true;
	this.navWindow = navWindow;
		
	// Initially show real surface globe
	showReal();
    }

    /** Displays real surface globe, regenerating if necessary */
    public void showReal() {
	if (topo) { recreate = true; }
	topo = false;
	showGlobe(centerCoords);
    }
	
    /** Displays topographical globe, regenerating if necessary */
    public void showTopo() {
	if (!topo) { recreate = true; }
	topo = true;
	showGlobe(centerCoords);
    }

    /** Displays globe at given center regardless of mode, regenerating if necessary */
    public void showGlobe(Coordinates newCenter) {
	if (!centerCoords.equals(newCenter)) {
	    recreate = true;
	    centerCoords.setCoords(newCenter);
	}
	updateDisplay();
    }

    /** Starts display update thread (or creates a new one if necessary) */
    public void updateDisplay() {
	if ((showThread == null) || (!showThread.isAlive())) {
	    showThread = new Thread(this, "Globe");
	    showThread.start();
	} else {
	    showThread.interrupt();
	}
    }

    /** the run method for the runnable interface */
    public void run() {
	refreshLoop();
    }

    /** loop, refreshing the globe display when necessary */
    public void refreshLoop() {
	while(true) {  // Endless refresh loop
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
		try { showThread.sleep(2000); }
		catch (InterruptedException e) {}
		repaint();
	    }
	}
    }

    /** Overrides paintComponent method.  Displays globe, green lines,
     *  longitude and latitude. */
    public void paintComponent(Graphics g) {

	// Paint black background
	g.setColor(Color.black);
	g.fillRect(0, 0, width, height);

	// Draw real or topo globe
	MarsGlobe globe = topo ? topoSphere : marsSphere;
			
	if (globe.isImageDone()) {
	    g.drawImage(globe.getGlobeImage(), 0, 0, this);
	}

	drawVehicles(g);
	drawSettlements(g);
	drawCrossHair(g);
    }

    /** draw the dots on the globe that identify moving vehicles */
    protected void drawVehicles(Graphics g) {
	// topo=black, surf=white
	g.setColor(topo ? Color.black : Color.white);
		
	UnitInfo[] vehicleInfo = navWindow.getMovingVehicleInfo();
	for (int x=0; x < vehicleInfo.length; x++) {
	    if (centerCoords.getAngle(vehicleInfo[x].getCoords()) < halfPI) {
		IntPoint tempLocation = getUnitDrawLocation(vehicleInfo[x].getCoords());
		g.fillRect(tempLocation.getiX(), tempLocation.getiY(), 1, 1);
	    }
	}
    }

    /** draw the dots on the globe that identify settlements */    
    protected void drawSettlements(Graphics g) {
	// topo=black, surf=green
	g.setColor(topo ? Color.black : Color.green);
		
	UnitInfo[] settlementInfo = navWindow.getSettlementInfo();
	for (int x=0; x < settlementInfo.length; x++) {
	    if (centerCoords.getAngle(settlementInfo[x].getCoords()) < halfPI) {
		IntPoint tempLocation = getUnitDrawLocation(settlementInfo[x].getCoords());
		g.fillRect(tempLocation.getiX(), tempLocation.getiY(), 1, 1);
	    }
	}
    }

    /** Draw green rectanges and lines (cross-hair type thingy), and
     *  write the latitude and logitude of the centerpoint of the
     *  current glove view. */
    protected void drawCrossHair(Graphics g) {
	g.setColor(Color.green);
		
	g.drawRect(57, 57, 31, 31);
	g.drawLine(0, 73, 57, 73);
	g.drawLine(90, 73, 149, 73);
	g.drawLine(73, 0, 73, 57);
	g.drawLine(73, 90, 73, 149);

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
	
    /** Returns unit x, y position on globe panel */
    private IntPoint getUnitDrawLocation(Coordinates unitCoords) {
	double rho = width / Math.PI;
	int half_map = (int)(width / 2);
	int low_edge = 0;
	return Coordinates.findRectPosition(unitCoords, centerCoords, rho, half_map, low_edge);
    }
}
