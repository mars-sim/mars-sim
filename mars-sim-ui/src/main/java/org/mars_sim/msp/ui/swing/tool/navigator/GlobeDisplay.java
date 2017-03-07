/**
 * Mars Simulation Project
 * GlobeDisplay.java
 * @version 3.1.0 2017-02-03
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.MemoryImageSource;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The Globe Display class displays a graphical globe of Mars in the Navigator
 * tool.
 */
public class GlobeDisplay extends JComponent implements ClockListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(GlobeDisplay.class.getName());
	private static double PERIOD_IN_MILLISOLS = 10D * 500D / MarsClock.SECONDS_IN_MILLISOL;

	public final static int GLOBE_BOX_HEIGHT = 300;
	public final static int GLOBE_BOX_WIDTH = 300;


	private static final double HALF_PI = Math.PI / 2d;
	private static int dragx, dragy;

	// Data members
	private double timeCache = 0;
	/** Real surface sphere object. */
	private MarsGlobe marsSphere;
	/** Topographical sphere object. */
	private MarsGlobe topoSphere;
	/** Spherical coordinates for globe center. */
	private Coordinates centerCoords;
	/** Refresh thread. */
	//private Thread showThread;
	/** <code>true</code> if in topographical mode, false if in real surface mode. */
	private boolean topo;
	/** <code>true</code> if globe needs to be regenerated */
	private boolean recreate;
	/** width of the globe display component. */
	private int width;
	/** height of the globe display component. */
	private int height;
	/** <code>true</code> if USGS surface map is to be used. */
	private boolean useUSGSMap;
	/** Array used to generate day/night shading image. */
	private int[] shadingArray;
	/** <code>true</code> if day/night shading is to be used. */
	private boolean showDayNightShading;
	/** <code>true</code> if globe should be updated. */
	private boolean update;
	/** <code>true</code> if refresh thread should continue. */
	private boolean keepRunning;

	/** stores the internationalized string for reuse in {@link #drawCrossHair(Graphics)}. */
	private String longitude = Msg.getString("direction.longitude"); //$NON-NLS-1$
	/** stores the internationalized string for reuse in {@link #drawCrossHair(Graphics)}. */
	private String latitude = Msg.getString("direction.latitude"); //$NON-NLS-1$
	/** stores the font for drawing lon/lat strings in {@link #drawCrossHair(Graphics)}. */
	private Font positionFont = new Font("Helvetica", Font.PLAIN, 10);
	/** measures the pixels needed to display text. */
	private FontMetrics positionMetrics = getFontMetrics(positionFont);
	/** stores the position for drawing lon/lat strings in {@link #drawCrossHair(Graphics)}. */
	int leftWidth = positionMetrics.stringWidth(latitude);
	/** stores the position for drawing lon/lat strings in {@link #drawCrossHair(Graphics)}. */
	int rightWidth = positionMetrics.stringWidth(longitude);

	//private Mars mars;
	private MainDesktopPane desktop;
	private NavigatorWindow navwin;
	private SurfaceFeatures surfaceFeatures;

	private Graphics dbg;
	private Image dbImage = null;
	private Image starfield;

	private boolean isOpenCache = false; //justLoaded = true,
	//private int difxCache, difyCache;
	private double globeCircumference;
    private double rho;

	/**
	 * Constructor.
	 * @param navwin the navigator window.
	 * @param width the width of the globe display
	 * @param height the height of the globe display
	 */
	public GlobeDisplay(final NavigatorWindow navwin) {//, int width, int height) {

		this.navwin = navwin;
		this.desktop = navwin.getDesktop();

		// Initialize data members
		this.width = GLOBE_BOX_WIDTH;
		this.height = GLOBE_BOX_HEIGHT;

		globeCircumference = height *2;
	    rho = globeCircumference / (2D * Math.PI);

		//starfield = ImageLoader.getImage("starfield.gif"); //TODO: localize
		starfield = ImageLoader.getImage(Msg.getString("img.mars.starfield300")); //$NON-NLS-1$

		Simulation.instance().getMasterClock().addClockListener(this);

		if (surfaceFeatures == null)
			surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();

		// Set component size
		setPreferredSize(new Dimension(width, height));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());

		// Construct sphere objects for both real and topographical modes
		marsSphere = new MarsGlobe(MarsGlobeType.SURFACE_MID, this);
		topoSphere = new MarsGlobe(MarsGlobeType.TOPO_MID, this);

		// Initialize global variables
		centerCoords = new Coordinates(HALF_PI, 0D);
		update = true;
		topo = false;
		recreate = true;
		keepRunning = true;
		useUSGSMap = false;
		shadingArray = new int[width * height *2 *2];
		showDayNightShading = true;

		addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				int difx, dify, x = e.getX(), y = e.getY();

				difx = dragx - x;
				dify = dragy - y;
				dragx = x;
				dragy = y;
				//System.out.println("x is " + x + "   y is " + y);
				//System.out.println("difx is " + difx + "   dify is " + dify);
				if ((difx != 0) || (dify != 0)
						&& x < 250 && y < 250) {

				    // Globe circumference in pixels.
				    //double globeCircumference = height *2;
				    //double rho = globeCircumference / (2D * Math.PI);
                    centerCoords = centerCoords.convertRectToSpherical(
                            (double) difx, (double) dify, rho);

					recreate = false;

					// Regenerate globe if recreate is true, then display
					drawSphere();


				}

				//e.consume();
				super.mouseDragged(e);
			}
		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				//System.out.println("mousepressed X = " + e.getX());
				//System.out.println("             Y = " + e.getY());
				dragx = e.getX();
				dragy = e.getY();
				navwin.setCursor(new Cursor(Cursor.MOVE_CURSOR));

				e.consume();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				dragx = 0;
				dragy = 0;
				navwin.updateCoords(centerCoords);
				navwin.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

				e.consume();
			}
		});

		// Initially show real surface globe
		showSurf();

		//drawSphere();
/*
		MouseEvent me = new MouseEvent(this, 0, 0, 0, 150, 150, 1, false);
		for (MouseListener ml: this.getMouseListeners())
		    ml.mousePressed(me);
		for (MouseMotionListener l: this.getMouseMotionListeners())
		    l.mouseDragged(me);
*/
	}

	/**
	 * Displays real surface globe, regenerating if necessary
	 */
	public void showSurf() {
		if (topo) {
			recreate = true;
		}
		topo = false;
		showGlobe(centerCoords);
	}

	public boolean isTopo() {
		return topo;
	}

	/**
	 * Displays topographical globe, regenerating if necessary
	 */
	public void showTopo() {
		if (!topo) {
			recreate = true;
		}
		topo = true;
		showGlobe(centerCoords);
	}

	/**
	 * Displays globe at given center regardless of mode, regenerating if
	 * necessary
	 *
	 * @param newCenter
	 *            the center location for the globe
	 */
	public void showGlobe(Coordinates newCenter) {
		if (!centerCoords.equals(newCenter)) {
			recreate = true;
			centerCoords.setCoords(newCenter);
		}
		updateDisplay();
	}

	/**
	 * Starts display update thread (or creates a new one if necessary)

	private void updateDisplay() {
		if ((showThread == null) || (!showThread.isAlive())) {
			showThread = new Thread(this, Msg.getString("GlobeDisplay.thread.globe")); //$NON-NLS-1$
			showThread.start();
		} else {
			showThread.interrupt();
		}
	}
*/

	/**
	 * the run method for the runnable interface
	 */
	public void updateDisplay() {
//		while (update) {
//			refreshLoop();
//		}
//	}
	/**
	 * loop, refreshing the globe display when necessary
	 */
//	public void refreshLoop() {
		if (keepRunning) {
			if (recreate) {
				//System.out.println("recreate is true");
				recreate = false;
				// Regenerate globe if recreate is true, then display
				drawSphere();

			} else {
				//System.out.println("recreate is false");
				//try {
					//boolean open = desktop.isToolWindowOpen(NavigatorWindow.NAME);
					//if (isOpenCache != open) {
					//	isOpenCache = open;
					//	if (open) {
							drawSphere();
							//paintDoubleBuffer();
							//repaint();
					//	}
					//}
				//	Thread.sleep(5000l);
				//} catch (InterruptedException e) {
					//e.printStackTrace(); // if enable, will print sleep interrupted
				//}
			}
		}
	}

/*
	// active rendering the buffer image to the screen
	public void paintScreen() {
		Graphics g;
		try {
			g = this.getGraphics();
			if ((g != null) && (dbImage != null))
				g.drawImage(dbImage,  0, 0,  null);

			Toolkit.getDefaultToolkit().sync();
			g.dispose();

		} catch (Exception e){
			//System.out.println("Graphics context error: " + e);
		}
	}

*/
	public void drawSphere() {

		if (topo) {
			topoSphere.drawSphere(centerCoords);
		} else {
			marsSphere.drawSphere(centerCoords);
		}

		paintDoubleBuffer();
		repaint();

	}

	/*
	 * Uses double buffering to draws into its own graphics object dbg before calling paintComponent()
	 */
	public void paintDoubleBuffer() {
		if (dbImage == null) {
			//dbImage = createImage(150,150);
			dbImage = createImage(height, height);
			if (dbImage == null) {
				//System.out.println("dbImage is null");
				return;
			}
			else
				dbg = dbImage.getGraphics();
		}

		dbg.setColor(Color.black);
		//dbg.fillRect(0, 0, 150, 150);
		dbg.fillRect(0, 0, height, height);
		//Image starfield = ImageLoader.getImage("starfield.gif"); //TODO: localize
		dbg.drawImage(starfield, 0, 0, Color.black, null);

		// Draw real or topo globe
		MarsGlobe globe = topo ? topoSphere : marsSphere;

		if(globe.isImageDone()) {
			dbg.drawImage(globe.getGlobeImage(), 0, 0, this);
		}
		else {
			return;
		}


		if (showDayNightShading) {
			drawShading(dbg);
		}

		drawUnits(dbg);
		drawCrossHair(dbg);

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(dbImage != null)
			g.drawImage(dbImage,  0, 0, null);
	}

/*
	public void paintComponent(Graphics g) {

		Image starfield = ImageLoader.getImage("starfield.gif"); //TODO: localize
		g.drawImage(starfield, 0, 0, Color.black, null);
		// Draw real or topo globe
		MarsGlobe globe = topo ? topoSphere : marsSphere;

		if (globe.isImageDone()) {
			g.drawImage(globe.getGlobeImage(), 0, 0, this);
		}

		if (showDayNightShading) {
			drawShading(g);
		}

		drawUnits(g);
		drawCrossHair(g);
	}
*/

	/**
	 * Draws the day/night shading on the globe.
	 * @param g graphics context
	 */
	protected void drawShading(Graphics g) {
		int centerX = width / 2;
		int centerY = height / 2;

		//if (mars == null)
		//	mars = Simulation.instance().getMars();

		//if (surfaceFeatures == null)
		//	surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();

		// Coordinates sunDirection = mars.getOrbitInfo().getSunDirection();

		Coordinates location = new Coordinates(0D, 0D);
		//for (int x = 0; x < 150; x++) {
			//for (int y = 0; y < 150; y++) {
		for (int x = 0; x < width *2; x++) {
			for (int y = 0; y < height *2; y++) {
				int xDiff = x - centerX;
				int yDiff = y - centerY;
				if (Math.sqrt((xDiff * xDiff) + (yDiff * yDiff)) <= 47.74648293D) {
					centerCoords.convertRectToSpherical(xDiff, yDiff,
							47.74648293D, location);

					double sunlight = 1D;
					try {
					    sunlight = surfaceFeatures.getSurfaceSunlight(location);
//					    sunlight =surfaceFeatures.getSolarIrradiance(location) / SurfaceFeatures.MEAN_SOLAR_IRRADIANCE;
					}
					catch (NullPointerException e) {
					    // Do nothing.
					    // This may be caused if simulation hasn't been fully initialized yet.
					}

					if (sunlight > 1D) {
					    sunlight = 1D;
					}
					int sunlightInt = (int) (127 * sunlight);
					shadingArray[x + (y * width )] = ((127 - sunlightInt) << 24) & 0xFF000000;
				}
				else if (Math.sqrt((xDiff * xDiff) + (yDiff * yDiff)) <= 49D) {
				    // Draw black opaque pixel at boundary of Mars.
				    //shadingArray[x + (y * 150)] = 0xFF000000;
				    shadingArray[x + (y * height )] = 0xFF000000;
				}
				else {
				    // Draw transparent pixel so background stars will show through.
					//shadingArray[x + (y * 150)] = 0x00000000;
					shadingArray[x + (y * height )] = 0x00000000;
				}
			}
		}

		// Create shading image for map
		Image shadingMap = this.createImage(new MemoryImageSource(width,
				height, shadingArray, 0, width));

		MediaTracker mt = new MediaTracker(this);
		mt.addImage(shadingMap, 0);
		try {
			mt.waitForID(0);
		} catch (InterruptedException e) {
			logger.log(
				Level.SEVERE,
				Msg.getString("GlobeDisplay.log.shadingInterrupted",e.toString()) //$NON-NLS-1$
			);
		}

		// Draw the shading image
		g.drawImage(shadingMap, 0, 0, this);
	}

	/**
	 * draw the dots on the globe that identify units
	 * @param g graphics context
	 */
	protected void drawUnits(Graphics g) {
		Iterator<Unit> i = Simulation.instance().getUnitManager().getUnits()
				.iterator();
		while (i.hasNext()) {
			Unit unit = i.next();
			UnitDisplayInfo displayInfo = UnitDisplayInfoFactory
					.getUnitDisplayInfo(unit);
			if (displayInfo.isGlobeDisplayed(unit)) {
				Coordinates unitCoords = unit.getCoordinates();
				if (centerCoords.getAngle(unitCoords) < HALF_PI) {
					if (topo) {
						g.setColor(displayInfo.getTopoGlobeColor());
					}
					else {
						g.setColor(displayInfo.getSurfGlobeColor());
					}

					IntPoint tempLocation = getUnitDrawLocation(unitCoords);
					g.fillRect(tempLocation.getiX(), tempLocation
							.getiY(), 1, 1);
				}
			}
		}
	}

	/**
	 * Draw green rectanges and lines (cross-hair type thingy), and write the
	 * latitude and logitude of the center point of the current globe view.
	 * @param g graphics context
	 */
	protected void drawCrossHair(Graphics g) {
		g.setColor(Color.orange);

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
			//g.drawRect(57, 57, 33, 33);
			//g.drawLine(0, 74, 56, 74);
			//g.drawLine(90, 74, 149, 74);
			//g.drawLine(74, 0, 74, 57);
			//g.drawLine(74, 90, 74, 149);

			g.drawRect(118, 118, 66, 66);
			g.drawLine(0, 150, 117, 150);
			g.drawLine(184, 150, 299, 150);
			g.drawLine(150, 0, 150, 117);
			g.drawLine(150, 185, 150, 300);

		}

		// use prepared font
		g.setFont(positionFont);

		// Draw longitude and latitude strings using prepared measurements
		//g.drawString(latitude, 5, 130);
		g.drawString(latitude, 5, 260);
		//g.drawString(longitude, 145 - rightWidth, 130);
		g.drawString(longitude, 290 - rightWidth, 260);

		String latString = centerCoords.getFormattedLatitudeString();
		String longString = centerCoords.getFormattedLongitudeString();

		int latWidth = positionMetrics.stringWidth(latString);
		int longWidth = positionMetrics.stringWidth(longString);

		int latPosition = ((leftWidth - latWidth) / 2) + 5;
		int longPosition = 290 - rightWidth + ((rightWidth - longWidth) / 2);

		//g.drawString(latString, latPosition, 142);
		//g.drawString(longString, longPosition, 142);
		g.drawString(latString, latPosition, 284);
		g.drawString(longString, longPosition, 284);

	}

	/**
	 * Returns unit x, y position on globe panel
	 * @param unitCoords the unit's location
	 * @return x, y position on globe panel
	 */
	private IntPoint getUnitDrawLocation(Coordinates unitCoords) {
		double rho = width / Math.PI;
		int half_map = width / 2;
		int low_edge = 0;
		return Coordinates.findRectPosition(unitCoords, centerCoords, rho,
				half_map, low_edge);
	}

	/**
	 * Set USGS as surface map
	 * @param useUSGSMap true if using USGS map.
	 */
	public void setUSGSMap(boolean useUSGSMap) {
		this.useUSGSMap = useUSGSMap;
	}

	/**
	 * Sets day/night tracking to on or off.
	 * @param showDayNightShading true if globe is to use day/night tracking.
	 */
	public void setDayNightTracking(boolean showDayNightShading) {
		this.showDayNightShading = showDayNightShading;
	}

	/**
	 * Gets the center coordinates of the globe.
	 * @return coordinates.
	 */
	public Coordinates getCoordinates() {
		return centerCoords;
	}

	/**
	 * Sets the center coordinates of the globe.
	 * @param c the center coordinates.
	 */
	public void setCoordinates(Coordinates c) {
		if (c != null) {
			centerCoords = c;
		}
	}

	//public void setJustLoaded(boolean value) {
	//	justLoaded = true;
	//}

	@Override
	public void clockPulse(double time) {
		timeCache = timeCache + time;
		if (timeCache > PERIOD_IN_MILLISOLS) {
			//System.out.println("calling GlobeDisplay's clockPulse()");
			updateDisplay();
			//justLoaded = false;
			timeCache = 0;
		}
	}

	@Override
	public void pauseChange(boolean isPaused) {
		// TODO Auto-generated method stub

	}

	/**
	 * Prepare globe for deletion.
	 */
	public void destroy() {
		//showThread = null;
		update = false;
		keepRunning = false;
		marsSphere = null;
		topoSphere = null;
		centerCoords = null;
		surfaceFeatures = null;
		dbg = null;
		dbImage = null;
		starfield = null;
	}
}