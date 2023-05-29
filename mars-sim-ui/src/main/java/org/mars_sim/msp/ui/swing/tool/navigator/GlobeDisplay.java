/*
 * Mars Simulation Project
 * GlobeDisplay.java
 * @date 2023-05-28
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
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

import org.mars_sim.mapdata.MapMetaData;
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

	private final int STANDARD_GLOBE_BOX_HEIGHT = 300;
	private final int NEW_GLOBE_BOX_HEIGHT = MapPanel.MAP_BOX_HEIGHT; // 450

	private final double RATIO_EXPANSION = 1.0 * NEW_GLOBE_BOX_HEIGHT / STANDARD_GLOBE_BOX_HEIGHT; // 1.5
	/** The max amount of pixels in each mouse drag that the globe will update itself. */
	private final double LIMIT = (NEW_GLOBE_BOX_HEIGHT / 4 - 20) * RATIO_EXPANSION;  // NEW_GLOBE_BOX_HEIGHT / 4 - 20
	// Half of the map pixel height / 2 
	private final int halfMap = NEW_GLOBE_BOX_HEIGHT / 2;
	// lower edge = pixel height / 2 - map box height / 2
	private final int lowEdge = halfMap - NEW_GLOBE_BOX_HEIGHT / 2;	

	private final double HALF_PI = Math.PI / 2;

	private int surfaceMapHeight = 1440;

	private double projectedGlobeRadius = surfaceMapHeight / Math.PI; // 458.37
	private double projectedGlobeBlackPadding = projectedGlobeRadius * 2 / 3.5 ; // 261.92
	private double projectedGlobe2DFullLength = 2 * (projectedGlobeRadius + projectedGlobeBlackPadding); // 1440.58
	private double projectedRatioMap2Box = projectedGlobe2DFullLength / NEW_GLOBE_BOX_HEIGHT; // 3.20

	private int widthZoomBox = (int) (NEW_GLOBE_BOX_HEIGHT / projectedRatioMap2Box); // 140.57
	private int paddingZoomBox = (int) (NEW_GLOBE_BOX_HEIGHT - widthZoomBox) / 2; // 154.72 
	
	private int dragx;
	private int dragy;
	private int dxCache = 0;
	private int dyCache = 0;

	
	// Data members
	/** <code>true</code> if globe needs to be regenerated */
	private boolean recreate;
	/** The Map type. 0: surface, 1: topo, 2: geology, 3: regional, 4: viking */
	private MapMetaData mapType;

	// height pixels divided by pi
	private double rho = NEW_GLOBE_BOX_HEIGHT / Math.PI;
	
	private MarsMap marsMap;
	
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
	private Font positionFont = new Font("Helvetica", Font.PLAIN, (int)(16 * RATIO_EXPANSION / 1.4));
	/** measures the pixels needed to display text. */
	private FontMetrics positionMetrics = getFontMetrics(positionFont);


	/**
	 * Stores the internationalized string for reuse in
	 * {@link #drawCrossHair(Graphics)}.
	 */
	private String longitude = Msg.getString("direction.longitude"); //$NON-NLS-1$
	/**
	 * Stores the internationalized string for reuse in
	 * {@link #drawCrossHair(Graphics)}.
	 */
	private String latitude = Msg.getString("direction.latitude"); //$NON-NLS-1$

	/**
	 * Stores the position for drawing lon/lat strings in
	 * {@link #drawCrossHair(Graphics)}.
	 */
	private int leftWidth = positionMetrics.stringWidth(latitude);
	/**
	 * Stores the position for drawing lon/lat strings in
	 * {@link #drawCrossHair(Graphics)}.
	 */
	private int rightWidth = positionMetrics.stringWidth(longitude);
	
	private NavigatorWindow navwin;
	
	private MainDesktopPane desktop;

	/**
	 * Constructor.
	 * 
	 * @param navwin the navigator window.
	 * @param globeBoxWidth  the width of the globe display
	 * @param globeBoxHeight the height of the globe display
	 */
	public GlobeDisplay(final NavigatorWindow navwin) {
		this.navwin = navwin;
		this.desktop = navwin.getDesktop();
		
		starfield = ImageLoader.getImage("map/starfield");
		//marsMap = new MarsMap(mapType, this);
		
		// Set component size
		setPreferredSize(new Dimension(NEW_GLOBE_BOX_HEIGHT, NEW_GLOBE_BOX_HEIGHT));
		setMaximumSize(getPreferredSize());
		
		// Initialize global variables
		centerCoords = new Coordinates(HALF_PI, 0D);
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
				&& x > 50 * RATIO_EXPANSION && x < 245 * RATIO_EXPANSION && y > 50 * RATIO_EXPANSION && y < 245 * RATIO_EXPANSION) {
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
	 * Displays topographical globe, regenerating if necessary.
	 */
	public void setMapType(MapMetaData newMapType) {
		if (!newMapType.equals(mapType)) {
			marsMap = new MarsMap(newMapType, this);
			recreate = true;
		}
		mapType = newMapType;
		showGlobe(centerCoords);
	}
	
	/**
	 * Displays globe at given center regardless of mode, regenerating if necessary.
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
		if (marsMap != null) {
			if (recreate) {
				recreate = false;
			}
		
			// Updates the position of the zoom box
			updateZoomBox();
			
			// Regenerate globe
			drawSphere();
		}
	}

	/**
	 * Updates the position of the zoom box.
	 */
	public void updateZoomBox() {
		if (navwin.getMapPanel() != null) {
			surfaceMapHeight = navwin.getMapPanel().getMap().getPixelWidth();	
			
			projectedGlobeRadius = surfaceMapHeight / Math.PI; // 458.37
			projectedGlobeBlackPadding = projectedGlobeRadius * 2 / 3.5; // 261.92
			projectedGlobe2DFullLength = 2 * (projectedGlobeRadius + projectedGlobeBlackPadding); // 1440.58
			projectedRatioMap2Box = projectedGlobe2DFullLength / NEW_GLOBE_BOX_HEIGHT; // 3.20

			widthZoomBox = (int) (NEW_GLOBE_BOX_HEIGHT / projectedRatioMap2Box); // 140.57
			paddingZoomBox = (int) (NEW_GLOBE_BOX_HEIGHT - widthZoomBox) / 2; // 154.72 
		}
	}
	
	public void drawSphere() {
		marsMap.drawSphere(centerCoords);
		paintDoubleBuffer();
		repaint();
	}

	/*
	 * Uses double buffering to draws into its own graphics object dbg before
	 * calling paintComponent().
	 */
	public void paintDoubleBuffer() {
		if (dbImage == null) {
			dbImage = createImage(NEW_GLOBE_BOX_HEIGHT, NEW_GLOBE_BOX_HEIGHT);
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
		g2d.fillRect(0, 0, NEW_GLOBE_BOX_HEIGHT, NEW_GLOBE_BOX_HEIGHT);
		// Image starfield = ImageLoader.getImage("starfield.gif");
		g2d.drawImage(starfield, 0, 0, Color.black, null);
		
		if (marsMap == null)
			return;
		
		Image image = marsMap.getGlobeImage();
		if (image != null) {
			if (marsMap.isImageDone()) {
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
		
		// Get rid of the copy
		g2d.dispose();
	}


	/**
	 * Draws the dots on the globe that identify units.
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
					g.setColor(displayInfo.getGlobeColor(mapType));
					
					IntPoint tempLocation = getUnitDrawLocation(unitCoords);
					g.fillRect(tempLocation.getiX(), tempLocation.getiY(), 3, 3);
				}
			}
		}
	}

	/**
	 * Draws green rectangles and lines (cross-hair type thingy), and write the
	 * latitude and longitude of the center point of the current globe view.
	 * 
	 * @param g graphics context
	 */
	protected void drawCrossHair(Graphics2D g) {
		g.setColor(Color.orange.brighter());

		// use prepared font
		g.setFont(positionFont);

		// Draw longitude and latitude strings using prepared measurements
		g.drawString(latitude, (int) (25 * RATIO_EXPANSION), (int) (30 * RATIO_EXPANSION));
		g.drawString(longitude, (int)((STANDARD_GLOBE_BOX_HEIGHT - 25) * RATIO_EXPANSION - rightWidth), (int)(30 * RATIO_EXPANSION));

		String latString = centerCoords.getFormattedLatitudeString();
		String longString = centerCoords.getFormattedLongitudeString();

		int latWidth = positionMetrics.stringWidth(latString);
		int longWidth = positionMetrics.stringWidth(longString);

		int latPosition = (int)(((leftWidth - latWidth) / 2) + 25 * RATIO_EXPANSION);
		int longPosition = (int)((NEW_GLOBE_BOX_HEIGHT - 25 * RATIO_EXPANSION) * RATIO_EXPANSION - rightWidth + ((rightWidth - longWidth) / 2));

		g.drawString(latString, latPosition, (int)(50 * RATIO_EXPANSION));
		g.drawString(longString, longPosition, (int)(50 * RATIO_EXPANSION));
		
//		g.setColor(Color.black);

		g.drawRect(paddingZoomBox, paddingZoomBox, widthZoomBox, widthZoomBox);

		// Draw a diagonal line from top left of the zoom box to top left of the right panel	
		drawDashedLine(g, paddingZoomBox, paddingZoomBox, NEW_GLOBE_BOX_HEIGHT, 0);
		// Draw a diagonal line from top right of the zoom box to top left of the right panel	
		drawDashedLine(g, paddingZoomBox + widthZoomBox, paddingZoomBox, NEW_GLOBE_BOX_HEIGHT, 0);
		// Draw a diagonal line from bottom left of the zoom box to bottom left of the right panel	
		drawDashedLine(g, paddingZoomBox, paddingZoomBox + widthZoomBox, NEW_GLOBE_BOX_HEIGHT, NEW_GLOBE_BOX_HEIGHT);
		// Draw a diagonal line from bottom right of the zoom box to bottom left of the right panel	
		drawDashedLine(g, paddingZoomBox + widthZoomBox, paddingZoomBox + widthZoomBox, NEW_GLOBE_BOX_HEIGHT, NEW_GLOBE_BOX_HEIGHT);
	}

	public void drawDashedLine(Graphics g, int x1, int y1, int x2, int y2){

		  // Create a copy of the Graphics instance
		  Graphics2D g2d = (Graphics2D) g.create();

		  // Set the stroke of the copy, not the original 
		  Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
		  g2d.setStroke(dashed);

		  // Draw to the copy
		  g2d.drawLine(x1, y1, x2, y2);

		  // Get rid of the copy
		  g2d.dispose();
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

		marsMap = null;
		
		centerCoords = null;

		dbg = null;
		dbImage = null;
		starfield = null;
	}
}
