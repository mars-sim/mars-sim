/*
 * Mars Simulation Project
 * GlobeDisplay.java
 * @date 2022-08-02
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
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JComponent;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.map.MapPanel;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The Globe Display class displays a graphical globe of Mars in the Navigator
 * tool.
 */
@SuppressWarnings("serial")
public class GlobeDisplay extends JComponent implements ClockListener {

	private static final int GLOBE_BOX_HEIGHT = MapPanel.MAP_BOX_HEIGHT;
	private static final int GLOBE_BOX_WIDTH = MapPanel.MAP_BOX_WIDTH;
	private static final int RATIO = GLOBE_BOX_HEIGHT / 300; 
	/** The max amount of pixels in each mouse drag that the globe will update itself. */
	private static final int LIMIT = 60 * RATIO; 

	private static final double HALF_PI = Math.PI / 2;
	private static final double TWO_PI = Math.PI * 2;
	private static final int lowEdge = 0;
	
	private static int halfMap;
	private static int dragx;
	private static int dragy;
	private static int dxCache = 0;
	private static int dyCache = 0;

	// Data members
	/** The Map type. 0 = surface, 1 = topo, 2 = geology. */
	private int mapType;
	/** <code>true</code> if globe needs to be regenerated */
	private boolean recreate;
	/** width of the globe display component. */
	private int globeBoxWidth;
	/** height of the globe display component. */
	private int globeBoxHeight;

	/** Real surface sphere object. */
	private MarsMap marsSphere;
	/** Topographical sphere object. */
	private MarsMap topoSphere;
	/** Geological sphere object. */
	private MarsMap geoSphere;
	private MarsMap regionSphere;
	private MarsMap vikingSphere;
	
	
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
	private Font positionFont = new Font("Helvetica", Font.PLAIN, (int)(16 * RATIO / 1.4));
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

	/**
	 * Constructor.
	 * 
	 * @param navwin the navigator window.
	 * @param globeBoxWidth  the width of the globe display
	 * @param globeBoxHeight the height of the globe display
	 */
	public GlobeDisplay(final NavigatorWindow navwin) {
		this.desktop = navwin.getDesktop();

		// Initialize data members
		globeBoxWidth = GLOBE_BOX_WIDTH;
		globeBoxHeight = GLOBE_BOX_HEIGHT;

		globeCircumference = globeBoxHeight * 2D;
		rho = globeCircumference / TWO_PI;
		halfMap = globeBoxWidth / 2;

		starfield = ImageLoader.getImage("map/starfield");
	
		// Set component size
		setPreferredSize(new Dimension(globeBoxWidth, globeBoxHeight));
		setMaximumSize(getPreferredSize());

		// Construct sphere objects for surface, geo and topographical modes
		marsSphere = new MarsMap(MarsMapType.SURFACE_MID, this);
		topoSphere = new MarsMap(MarsMapType.TOPO_MID, this);
		geoSphere = new MarsMap(MarsMapType.GEO_MID, this);
		regionSphere = new MarsMap(MarsMapType.REGION_MID, this);
		vikingSphere = new MarsMap(MarsMapType.VIKING_MID, this);
		
		// Initialize global variables
		centerCoords = new Coordinates(HALF_PI, 0D);
		mapType = 0;
		recreate = true;


		addMouseListener(new MouseAdapter() {
			// Note: must use MouseAdapter's mousePressed separately from Dragger
			// Use mousePressed in Dragger would result in jumpy dragging
            @Override
            public void mousePressed(MouseEvent e) {
    			dragx = e.getX();
    			dragy = e.getY();
                repaint();
            }
        });
		
		dragger = new Dragger(navwin);
		
		addMouseMotionListener(dragger);

		// Initially show real surface globe
		showSurf();
		
		// Add listener once fully constructed
		desktop.getSimulation().getMasterClock().addClockListener(this, 1000L);
	}
	
	public class Dragger extends MouseAdapter {
		NavigatorWindow navwin;
		
		public Dragger (NavigatorWindow navwin) {
			this.navwin = navwin;
	    }
	    
		@Override
		public void mouseDragged(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();

			int dx = dragx - x;
			int dy = dragy - y;

			if ((dx != 0 || dy != 0)// (dx < -2 || dx > 2) || (dy < -2 || dy > 2)) {
				&& dx > -LIMIT && dx < LIMIT && dy > -LIMIT && dy < LIMIT
				&& ((dxCache - dx) > -LIMIT) && ((dxCache - dx) < LIMIT) 
				&& ((dyCache - dy) > -LIMIT) && ((dyCache - dy) < LIMIT)
				&& x > 50 * RATIO && x < 245 * RATIO && y > 50 * RATIO && y < 245 * RATIO) {
					setCursor(new Cursor(Cursor.HAND_CURSOR));

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
	 * Draws the sphere.
	 */
	public void updateDisplay() {
		if (recreate)
			recreate = false;
		
		// Regenerate globe
		drawSphere();
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
			dbImage = createImage(globeBoxWidth, globeBoxHeight);
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
		g2d.fillRect(0, 0, globeBoxWidth, globeBoxHeight);
		// Image starfield = ImageLoader.getImage("starfield.gif");
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
		
		if (globe == null)
			return;
		
		Image image = globe.getGlobeImage();
		if (image != null) {
			if (globe.isImageDone()) {
				g2d.drawImage(image, 0, 0, this);
			} else {
				return;
			}
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
			g2d.drawImage(dbImage, 0, 0, this);
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
	 * Draw green rectangles and lines (cross-hair type thingy), and write the
	 * latitude and longitude of the center point of the current globe view.
	 * 
	 * @param g graphics context
	 */
	protected void drawCrossHair(Graphics2D g) {
		g.setColor(Color.orange);

		g.drawRect(118 * RATIO, 118 * RATIO,  66 * RATIO,  66 * RATIO);
		// Draw left horizontal line
		g.drawLine(15, 			150 * RATIO, 117 * RATIO, 150 * RATIO);
		// Draw right horizontal line
		g.drawLine(184 * RATIO, 150 * RATIO, 285 * RATIO, 150 * RATIO);
		// Draw top vertical line
		g.drawLine(150 * RATIO,  15 * RATIO, 150 * RATIO, 117 * RATIO);
		// Draw bottom vertical line
		g.drawLine(150 * RATIO, 185 * RATIO, 150 * RATIO, 285 * RATIO);	

		// use prepared font
		g.setFont(positionFont);

		// Draw longitude and latitude strings using prepared measurements
		g.drawString(latitude, 25 * RATIO, 30 * RATIO);
		g.drawString(longitude, 275 * RATIO - rightWidth, 30 * RATIO);

		String latString = centerCoords.getFormattedLatitudeString();
		String longString = centerCoords.getFormattedLongitudeString();

		int latWidth = positionMetrics.stringWidth(latString);
		int longWidth = positionMetrics.stringWidth(longString);

		int latPosition = ((leftWidth - latWidth) / 2) + 25 * RATIO;
		int longPosition = 275 * RATIO - rightWidth + ((rightWidth - longWidth) / 2);

		g.drawString(latString, latPosition, 50 * RATIO);
		g.drawString(longString, longPosition, 50 * RATIO);

	}

	/**
	 * Returns unit x, y position on globe panel
	 * 
	 * @param unitCoords the unit's location
	 * @return x, y position on globe panel
	 */
	private IntPoint getUnitDrawLocation(Coordinates unitCoords) {
		return Coordinates.findRectPosition(unitCoords, centerCoords, rho, halfMap, lowEdge);
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
		desktop  = null;

		marsSphere = null;
		topoSphere = null;
		centerCoords = null;

		dbg = null;
		dbImage = null;
		starfield = null;
	}
}
