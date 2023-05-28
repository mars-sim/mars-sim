/*
 * Mars Simulation Project
 * GlobeDisplay.java
 * @date 2023-05-02
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

	private final int STANDARD_MAP_HEIGHT = 1440;
	private final int STANDARD_MAP_WIDTH = 2880;
	private final int STANDARD_GLOBE_BOX_HEIGHT = 300;
	private final int MAP_HEIGHT = MapPanel.MAP_BOX_HEIGHT;
	
	private final int GLOBE_BOX_HEIGHT = MapPanel.MAP_BOX_HEIGHT;
	private final int GLOBE_BOX_WIDTH = MapPanel.MAP_BOX_WIDTH;
	private final int RATIO = GLOBE_BOX_HEIGHT / STANDARD_GLOBE_BOX_HEIGHT; 
	
	private final int X_PADDING_ZOOM_BOX = 116 * RATIO;
	private final int Y_PADDING_ZOOM_BOX = 116 * RATIO;
	private final int WIDTH_ZOOM_BOX = 70 * RATIO;
	private final int HEIGHT_ZOOM_BOX = 70 * RATIO;
	
	/** The max amount of pixels in each mouse drag that the globe will update itself. */
	private final int LIMIT = 60 * RATIO; 
	// Half of the map pixel height / 2 
	private final int halfMap = MAP_HEIGHT / 2;
	// lower edge = pixel height / 2 - map box height / 2
	private final int lowEdge = halfMap - MAP_HEIGHT / 2;	

	private final double HALF_PI = Math.PI / 2;

	private int dragx;
	private int dragy;
	private int dxCache = 0;
	private int dyCache = 0;
	private int xPaddingZoomBox = X_PADDING_ZOOM_BOX;
	private int yPaddingZoomBox = Y_PADDING_ZOOM_BOX;	
	private int widthZoomBox = WIDTH_ZOOM_BOX;
	private int heightZoomBox = HEIGHT_ZOOM_BOX;
	
	
	// Data members
	/** <code>true</code> if globe needs to be regenerated */
	private boolean recreate;
	/** The Map type. 0: surface, 1: topo, 2: geology, 3: regional, 4: viking */
	private MapMetaData mapType;

	// height pixels divided by pi
	private double rho = GLOBE_BOX_HEIGHT / Math.PI;
	
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
	private Font positionFont = new Font("Helvetica", Font.PLAIN, (int)(16 * RATIO / 1.4));
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
		setPreferredSize(new Dimension(GLOBE_BOX_WIDTH, GLOBE_BOX_HEIGHT));
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
			int width = navwin.getMapPanel().getMap().getPixelWidth();
			int height = navwin.getMapPanel().getMap().getPixelHeight();
			
			widthZoomBox = WIDTH_ZOOM_BOX * STANDARD_MAP_WIDTH / width;
			heightZoomBox = HEIGHT_ZOOM_BOX * STANDARD_MAP_HEIGHT / height;
			xPaddingZoomBox = X_PADDING_ZOOM_BOX + (WIDTH_ZOOM_BOX - widthZoomBox) / 2; 
			yPaddingZoomBox = Y_PADDING_ZOOM_BOX + (HEIGHT_ZOOM_BOX - heightZoomBox) / 2; 
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
			dbImage = createImage(GLOBE_BOX_WIDTH, GLOBE_BOX_HEIGHT);
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
		g2d.fillRect(0, 0, GLOBE_BOX_WIDTH, GLOBE_BOX_HEIGHT);
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

//		// Draw left horizontal line
//		g.drawLine(15, 			150 * RATIO, 117 * RATIO, 150 * RATIO);
//		drawDashedLine(g, 15, 			150 * RATIO, 117 * RATIO, 150 * RATIO);
//		// Draw right horizontal line
//		g.drawLine(184 * RATIO, 150 * RATIO, 285 * RATIO, 150 * RATIO);
//		drawDashedLine(g, 184 * RATIO, 150 * RATIO, 285 * RATIO, 150 * RATIO);
//		// Draw top vertical line
//		g.drawLine(150 * RATIO,  15 * RATIO, 150 * RATIO, 117 * RATIO);
//		drawDashedLine(g, 150 * RATIO,  15 * RATIO, 150 * RATIO, 117 * RATIO);		
//		// Draw bottom vertical line
//		g.drawLine(150 * RATIO, 185 * RATIO, 150 * RATIO, 285 * RATIO);	
//		drawDashedLine(g, 150 * RATIO, 185 * RATIO, 150 * RATIO, 285 * RATIO);		

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
		int longPosition = (GLOBE_BOX_HEIGHT - 25 * RATIO) * RATIO - rightWidth + ((rightWidth - longWidth) / 2);

		g.drawString(latString, latPosition, 50 * RATIO);
		g.drawString(longString, longPosition, 50 * RATIO);
		
//		g.setColor(Color.black);
		
		g.drawRect(xPaddingZoomBox, yPaddingZoomBox, widthZoomBox,  heightZoomBox);
		
		// Draw diagonal line to top right of the left panel	
		drawDashedLine(g, xPaddingZoomBox, yPaddingZoomBox, GLOBE_BOX_HEIGHT, 0);
		// Draw diagonal line to top right of the left panel	
		drawDashedLine(g, xPaddingZoomBox + widthZoomBox, yPaddingZoomBox, GLOBE_BOX_HEIGHT * RATIO, 0);
		// Draw diagonal line to bottom right of the left panel	
		drawDashedLine(g, xPaddingZoomBox, yPaddingZoomBox + heightZoomBox, GLOBE_BOX_HEIGHT, GLOBE_BOX_HEIGHT);
		// Draw diagonal line to bottom right of the left panel	
		drawDashedLine(g, xPaddingZoomBox + widthZoomBox, yPaddingZoomBox + heightZoomBox, GLOBE_BOX_HEIGHT * RATIO, GLOBE_BOX_HEIGHT);

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
