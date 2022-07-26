/*
 * Mars Simulation Project
 * GlobeDisplay.java
 * @date 2021-12-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.MemoryImageSource;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The Globe Display class displays a graphical globe of Mars in the Navigator
 * tool.
 */
@SuppressWarnings("serial")
public class GlobeDisplay extends JComponent implements ClockListener {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(GlobeDisplay.class.getName());

	public final static int GLOBE_BOX_HEIGHT = NavigatorWindow.HORIZONTAL_SURFACE_MAP;
	public final static int GLOBE_BOX_WIDTH = GLOBE_BOX_HEIGHT;
	/** The max amount of pixels in each mouse drag that the globe will update itself. */
	public final static int LIMIT = 60; 

	private static final double HALF_PI = Math.PI / 2d;
	private static int dragx, dragy, dxCache = 0, dyCache = 0;

	// Data members
	/** The Map type. 0 = surface, 1 = topo, 2 = geology. */
	private int mapType;
	/** <code>true</code> if globe needs to be regenerated */
	private boolean recreate;
	/** width of the globe display component. */
	private int globalWidth;
	/** height of the globe display component. */
	private int globalHeight;
	/** <code>true</code> if USGS surface map is to be used. */
	private boolean useUSGSMap;
	/** Array used to generate day/night shading image. */
	private int[] shadingArray;
	/** <code>true</code> if day/night shading is to be used. */
	private boolean showDayNightShading;
	/** <code>true</code> if globe should be updated. */
	private boolean update;
	/** <code>true</code> if refresh thread should continue. */
//	private boolean keepRunning;
	
	/** Real surface sphere object. */
	private MarsMap marsSphere;
	/** Topographical sphere object. */
	private MarsMap topoSphere;
	/** Geological sphere object. */
	private MarsMap geoSphere;
	/** Spherical coordinates for globe center. */
	private Coordinates centerCoords;
	/** A mouse adapter class. */
	private Dragger dragger;
	
	private Graphics dbg;
	private Image dbImage = null;
	private Image starfield;
	
	/**
	 * Stores the font for drawing lon/lat strings in
	 * {@link #drawCrossHair(Graphics)}.
	 */
	private Font positionFont = new Font("Helvetica", Font.PLAIN, 10);
	/** measures the pixels needed to display text. */
	private FontMetrics positionMetrics = getFontMetrics(positionFont);

	private double globeCircumference;
	private double rho;
	
	/**
	 * stores the internationalized string for reuse in
	 * {@link #drawCrossHair(Graphics)}.
	 */
	private String longitude = Msg.getString("direction.longitude"); //$NON-NLS-1$
	/**
	 * stores the internationalized string for reuse in
	 * {@link #drawCrossHair(Graphics)}.
	 */
	private String latitude = Msg.getString("direction.latitude"); //$NON-NLS-1$

	/**
	 * stores the position for drawing lon/lat strings in
	 * {@link #drawCrossHair(Graphics)}.
	 */
	private int leftWidth = positionMetrics.stringWidth(latitude);
	/**
	 * stores the position for drawing lon/lat strings in
	 * {@link #drawCrossHair(Graphics)}.
	 */
	private int rightWidth = positionMetrics.stringWidth(longitude);
	
	private MainDesktopPane desktop;
	private SurfaceFeatures surfaceFeatures;

	/**
	 * Constructor.
	 * 
	 * @param navwin the navigator window.
	 * @param globalWidth  the width of the globe display
	 * @param globalHeight the height of the globe display
	 */
	public GlobeDisplay(final NavigatorWindow navwin) {
		this.desktop = navwin.getDesktop();

		// Initialize data members
		this.globalWidth = GLOBE_BOX_WIDTH;
		this.globalHeight = GLOBE_BOX_HEIGHT;

		globeCircumference = globalHeight * 2;
		rho = globeCircumference / (2D * Math.PI);

		this.starfield = ImageLoader.getImage(Msg.getString("img.mars.starfield300")); //$NON-NLS-1$

		this.surfaceFeatures = desktop.getSimulation().getSurfaceFeatures();


		// Set component size
		setPreferredSize(new Dimension(globalWidth, globalHeight));
		setMaximumSize(getPreferredSize());

		// Construct sphere objects for both real surface and topographical modes
		marsSphere = new MarsMap(MarsMapType.SURFACE_MID, this);
		topoSphere = new MarsMap(MarsMapType.TOPO_MID, this);
		geoSphere = new MarsMap(MarsMapType.GEO_MID, this);

		// Initialize global variables
		centerCoords = new Coordinates(HALF_PI, 0D);
		update = true;
		mapType = 0;
		recreate = true;
		useUSGSMap = false;
		shadingArray = new int[globalWidth * globalHeight * 2 * 2];
		showDayNightShading = true;

		dragger = new Dragger(navwin);
		addMouseMotionListener(dragger);

		// Initially show real surface globe
		showSurf();
		
		// Add listener once fully constructed
		MasterClock masterClock = desktop.getSimulation().getMasterClock();
		masterClock.addClockListener(this, 1000L);
	}
	
	public class Dragger extends MouseAdapter {
		NavigatorWindow navwin;
		
		public Dragger (NavigatorWindow navwin) {
			this.navwin = navwin;
	    }

		@Override
		public void mousePressed(MouseEvent e) {
			navwin.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			dragx = e.getX();
			dragy = e.getY();

			e.consume();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			navwin.setCursor(new Cursor(Cursor.HAND_CURSOR));
			dragx = 0;
			dragy = 0;
			navwin.updateCoords(centerCoords);
			e.consume();
		}

	    @Override
	    public void mouseExited(MouseEvent e){
	    	navwin.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    }
	    
		@Override
		public void mouseDragged(MouseEvent e) {
			int dx, dy, x = e.getX(), y = e.getY();

			dx = dragx - x;
			dy = dragy - y;

			if ((dx != 0 || dy != 0)// (dx < -2 || dx > 2) || (dy < -2 || dy > 2)) {
				&& dx > -LIMIT && dx < LIMIT && dy > -LIMIT && dy < LIMIT
				&& ((dxCache - dx) > -LIMIT) && ((dxCache - dx) < LIMIT) 
				&& ((dyCache - dy) > -LIMIT) && ((dyCache - dy) < LIMIT)
				&& x > 50 && x < 245 && y > 50 && y < 245) {
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
					// Globe circumference in pixels.
					// double globeCircumference = height *2;
					// double rho = globeCircumference / (2D * Math.PI);
					centerCoords = centerCoords.convertRectToSpherical((double) dx, (double) dy, rho);
					navwin.updateCoords(centerCoords);				
					recreate = false;
					// Regenerate globe if recreate is true, then display
					drawSphere();
			}

			dxCache = dx;
			dyCache = dy;

			dragx = x;
			dragy = y;

			e.consume();
		}
	}
	/**
	 * Displays real surface globe, regenerating if necessary
	 */
	public void showSurf() {
		if (mapType != 0) {
			recreate = true;
		}
		mapType = 0;
		showGlobe(centerCoords);
	}

	public int getMapType() {
		return mapType;
	}

	/**
	 * Displays topographical globe, regenerating if necessary
	 */
	public void showTopo() {
		if (mapType != 1) {
			recreate = true;
		}
		mapType = 1;
		showGlobe(centerCoords);
	}


	/**
	 * Displays geological globe, regenerating if necessary
	 */
	public void showGeo() {
		if (mapType != 2) {
			recreate = true;
		}
		mapType = 2;
		showGlobe(centerCoords);
	}
	
	
	/**
	 * Displays globe at given center regardless of mode, regenerating if necessary
	 *
	 * @param newCenter the center location for the globe
	 */
	public void showGlobe(Coordinates newCenter) {
		if (!centerCoords.equals(newCenter)) {
			recreate = true;
			centerCoords = newCenter;
		}
		updateDisplay();
	}

	/**
	 * the run method for the runnable interface
	 */
	public void updateDisplay() {
		if (recreate) {
			// System.out.println("recreate is true");
			recreate = false;
			// Regenerate globe if recreate is true, then display
			drawSphere();

		} else {
			drawSphere();
		}
	}

	public void drawSphere() {

		if (mapType == 0) {
			marsSphere.drawSphere(centerCoords);
		} else if (mapType == 1) {
			topoSphere.drawSphere(centerCoords);
		} else if (mapType == 2) {
			geoSphere.drawSphere(centerCoords);
		}
		
		paintDoubleBuffer();
		repaint();

	}

	/*
	 * Uses double buffering to draws into its own graphics object dbg before
	 * calling paintComponent()
	 */
	public void paintDoubleBuffer() {
		if (dbImage == null) {
			dbImage = createImage(globalWidth, globalHeight);
			if (dbImage == null) {
				return;
			} else
				dbg = dbImage.getGraphics();
		}

		Graphics2D g2d = (Graphics2D) dbg;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		g2d.setColor(Color.black);
		// dbg.fillRect(0, 0, 150, 150);
		g2d.fillRect(0, 0, globalWidth, globalHeight);
		// Image starfield = ImageLoader.getImage("starfield.gif"); //TODO: localize
		g2d.drawImage(starfield, 0, 0, Color.black, null);

		// Draw real, topo or geo globe
		MarsMap globe = null;
		
		if (mapType == 0) {
			globe = marsSphere;
		} else if (mapType == 1) {
			globe = topoSphere;
		} else if (mapType == 2) {
			globe = geoSphere;
		}
		
		Image image = globe.getGlobeImage();
		if (image != null) {
			if (globe.isImageDone()) {
				g2d.drawImage(image, 0, 0, this);
			} else {
				return;
			}
		}

		if (showDayNightShading) {
			drawShading(g2d);
		}

		drawUnits(g2d);
		drawCrossHair(g2d);

	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		if (dbImage != null)
			g2d.drawImage(dbImage, 0, 0, null);
	}

	/**
	 * Draws the day/night shading on the globe.
	 * 
	 * @param g graphics context
	 */
	protected void drawShading(Graphics2D g) {
		int centerX = globalWidth / 2;
		int centerY = globalHeight / 2;

		for (int x = 0; x < globalWidth * 2; x++) {
			for (int y = 0; y < globalHeight * 2; y++) {
				int xDiff = x - centerX;
				int yDiff = y - centerY;
				if (Math.sqrt((xDiff * xDiff) + (yDiff * yDiff)) <= 47.74648293D) {
					Coordinates location = centerCoords.convertRectToSpherical(xDiff, yDiff, 47.74648293D);

					double sunlight = 1D;
					try {
						sunlight = surfaceFeatures.getSurfaceSunlightRatio(location);
					} catch (NullPointerException e) {
						// Do nothing.
						// This may be caused if simulation hasn't been fully initialized yet.
					}

					if (sunlight > 1D) {
						sunlight = 1D;
					}
					int sunlightInt = (int) (127 * sunlight);
					shadingArray[x + (y * globalWidth)] = ((127 - sunlightInt) << 24) & 0xFF000000;
				} else if (Math.sqrt((xDiff * xDiff) + (yDiff * yDiff)) <= 49D) {
					// Draw black opaque pixel at boundary of Mars.
					shadingArray[x + (y * globalHeight)] = 0xFF000000;
				} else {
					// Draw transparent pixel so background stars will show through.
					shadingArray[x + (y * globalHeight)] = 0x00000000;
				}
			}
		}

		// Create shading image for map
		Image shadingMap = this.createImage(new MemoryImageSource(globalWidth, globalHeight, shadingArray, 0, globalWidth));
		// NOTE: Replace MediaTracker with faster method
		// Use BufferedImage image = ImageIO.read() ? 
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(shadingMap, 0);
		try {
			mt.waitForID(0);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, Msg.getString("GlobeDisplay.log.shadingInterrupted", e.toString()) //$NON-NLS-1$
			);
		}

		// Draw the shading image
		g.drawImage(shadingMap, 0, 0, this);
	}

	/**
	 * draw the dots on the globe that identify units
	 * 
	 * @param g graphics context
	 */
	protected void drawUnits(Graphics2D g) {
		UnitManager unitManager = desktop.getSimulation().getUnitManager();
		Iterator<Unit> i = unitManager.getDisplayUnits().iterator();
		while (i.hasNext()) {
			Unit unit = i.next();
			
			if (unit.getUnitType() == UnitType.VEHICLE) {
				if (((Vehicle)unit).isOutsideOnMarsMission()) {
					// Proceed to below to set cursor;
				}
				else 
					continue;
			}
			
			UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
			if (displayInfo != null && displayInfo.isGlobeDisplayed(unit)) {
				Coordinates unitCoords = unit.getCoordinates();
				if (centerCoords.getAngle(unitCoords) < HALF_PI) {
					if (mapType == 0) {
						g.setColor(displayInfo.getSurfGlobeColor());
					} else if (mapType == 1) {
						g.setColor(displayInfo.getTopoGlobeColor());
					} else if (mapType == 2) {
						g.setColor(displayInfo.getGeologyGlobeColor());
					}
					
					IntPoint tempLocation = getUnitDrawLocation(unitCoords);
					g.fillRect(tempLocation.getiX(), tempLocation.getiY(), 3, 3);
				}
			}
		}
	}

	/**
	 * Draw green rectanges and lines (cross-hair type thingy), and write the
	 * latitude and logitude of the center point of the current globe view.
	 * 
	 * @param g graphics context
	 */
	protected void drawCrossHair(Graphics2D g) {
		g.setColor(Color.orange);

		// If USGS map is used, use small crosshairs.
		if (useUSGSMap && mapType == 0) {
			g.drawRect(72, 72, 6, 6);
			g.drawLine(0, 75, 71, 75);
			g.drawLine(79, 75, 149, 75);
			g.drawLine(75, 20, 75, 71);
			g.drawLine(75, 79, 75, 149);
		}
		// If not USGS map, use large crosshairs.
		else {
			g.drawRect(118, 118, 66, 66);
			g.drawLine(0, 150, 117, 150);
			g.drawLine(184, 150, 299, 150);
			g.drawLine(150, 20, 150, 117);
			g.drawLine(150, 185, 150, 300);	
		}

		// use prepared font
		g.setFont(positionFont);

		// Draw longitude and latitude strings using prepared measurements
		g.drawString(latitude, 25, 30);
		g.drawString(longitude, 275 - rightWidth, 30);

		String latString = centerCoords.getFormattedLatitudeString();
		String longString = centerCoords.getFormattedLongitudeString();

		int latWidth = positionMetrics.stringWidth(latString);
		int longWidth = positionMetrics.stringWidth(longString);

		int latPosition = ((leftWidth - latWidth) / 2) + 25;
		int longPosition = 275 - rightWidth + ((rightWidth - longWidth) / 2);

		g.drawString(latString, latPosition, 50);
		g.drawString(longString, longPosition, 50);

	}

	/**
	 * Returns unit x, y position on globe panel
	 * 
	 * @param unitCoords the unit's location
	 * @return x, y position on globe panel
	 */
	private IntPoint getUnitDrawLocation(Coordinates unitCoords) {
		double rho = globalWidth / Math.PI;
		int half_map = globalWidth / 2;
		int low_edge = 0;
		return Coordinates.findRectPosition(unitCoords, centerCoords, rho, half_map, low_edge);
	}

	/**
	 * Set USGS as surface map
	 * 
	 * @param useUSGSMap true if using USGS map.
	 */
	public void setUSGSMap(boolean useUSGSMap) {
		this.useUSGSMap = useUSGSMap;
	}

	/**
	 * Sets day/night tracking to on or off.
	 * 
	 * @param showDayNightShading true if globe is to use day/night tracking.
	 */
	public void setDayNightTracking(boolean showDayNightShading) {
		this.showDayNightShading = showDayNightShading;
	}

	/**
	 * Gets the center coordinates of the globe.
	 * 
	 * @return coordinates.
	 */
	public Coordinates getCoordinates() {
		return centerCoords;
	}

	/**
	 * Sets the center coordinates of the globe.
	 * 
	 * @param c the center coordinates.
	 */
	public void setCoordinates(Coordinates c) {
		if (c != null) {
			centerCoords = c;
		}
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
		if (desktop.isToolWindowOpen(NavigatorWindow.NAME)) {
			updateDisplay();
		}
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// placeholder
	}

	/**
	 * Prepare globe for deletion.
	 */
	public void destroy() {
		MasterClock masterClock = desktop.getSimulation().getMasterClock();
		masterClock.removeClockListener(this);
		removeMouseListener(dragger);
		dragger = null;
		surfaceFeatures = null;
		desktop  = null;

		marsSphere = null;
		topoSphere = null;
		centerCoords = null;

		dbg = null;
		dbImage = null;
		starfield = null;
	}
}
