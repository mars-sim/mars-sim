/*
 * Mars Simulation Project
 * GlobeDisplay.java
 * @date 2023-06-03
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JComponent;

import org.mars.sim.mapdata.MapMetaData;
import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.location.IntPoint;
import org.mars.sim.tools.Msg;
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
	
//	private static final Logger logger = Logger.getLogger(GlobeDisplay.class.getName());
	
	// Data members
	/** <code>true</code> if globe needs to be regenerated */
	private boolean recreate;
	
	private int surfaceMapWidth = 1440;
	
	private final int STANDARD_GLOBE_BOX_HEIGHT = 300;
	
	private final int NEW_GLOBE_BOX_HEIGHT = MapPanel.MAP_BOX_HEIGHT; // 450

//	private final double BLACK_PADDING_RATIO = 0.6;
	
	private final double RATIO_EXPANSION = 1.0 * NEW_GLOBE_BOX_HEIGHT / STANDARD_GLOBE_BOX_HEIGHT; // 1.5
	// The radius of the standard old surface map
	private double RHO_SURFACE = surfaceMapWidth / Math.PI / 2; // 458.37
	// The standard rho ratio for the old surface map
//	private double RHO_STANDARD_RATIO;
//	// The scale of the old surface map
//	private double rhoSurfaceCache = RHO_SURFACE; // 458.37
//	// The width on the surface map outside of the zoom box
//	private double outsideZoomBoxWidth; 
//	// The measured black padding width of the empty space around the mars globe. Based on surfaceMapHeight of 1440.
//	private double blackPaddingWidth;
//	// The globe length ratio. Based on surfaceMapHeight of 1440.
//	private double globeFullLength;
//	// The globe height ratio. Based on surfaceMapHeight of 1440.
//	private double globeBoxRatio;
//	// The scale of the globe map. It's height pixels divided by pi
	private double rhoGlobe = NEW_GLOBE_BOX_HEIGHT / Math.PI;
//	// The ratio of the new surface map scale to the old. This number will change when zooming in and out of surface map
//	private double rhoMapRatio = rhoSurfaceCache / RHO_SURFACE;
//	
//	// The width of the zoom box over the globe
//	private int widthZoomBox;
//	// The padding space before the zoom box over the globe	
//	private int paddingZoomBox;
	// Half of the map pixel height / 2 
	private final int halfMap = NEW_GLOBE_BOX_HEIGHT / 2;
	// lower edge = pixel height / 2 - map box height / 2
	private final int lowEdge = halfMap - NEW_GLOBE_BOX_HEIGHT / 2;	

	private final double HALF_PI = Math.PI / 2;

	private int dragx;
	
	private int dragy;
	
	/** The Map type. 0: surface, 1: topo, 2: geology, 3: regional, 4: viking */
	private MapMetaData mapType;

	private GlobeMap globeMap;
	/** Spherical coordinates for globe center. */
	private Coordinates centerCoords;
	
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
	
		// Set component size
		setPreferredSize(new Dimension(NEW_GLOBE_BOX_HEIGHT, NEW_GLOBE_BOX_HEIGHT));
		setMaximumSize(getPreferredSize());
		
		// Initialize global variables
		centerCoords = new Coordinates(HALF_PI, 0D);
		recreate = true;
		
		// Set the mouse dragger
		setMouseDragger(navwin);

		// Add listener once fully constructed
		desktop.getSimulation().getMasterClock().addClockListener(this, 1000L);
	}
	
//	public void updateWidth(int width, double scale) {
//		surfaceMapWidth = width;
//		updateMapScale(scale);
//		computeZoomBox();
//	}
//	
//	/**
//	 * Gets the scale of the Mars surface map.
//	 * 
//	 * @param value
//	 */
//    public void setMapScale(double value) {
//    	rhoSurfaceCache = value;
//    	rhoMapRatio = rhoSurfaceCache / RHO_SURFACE / RHO_STANDARD_RATIO;
//    }
//    
//	/**
//	 * Gets the scale of the Mars surface map.
//	 * 
//	 * @param value
//	 */
//    public void updateMapScale(double value) {
//    	rhoSurfaceCache = value;
//    	RHO_STANDARD_RATIO = rhoSurfaceCache / RHO_SURFACE;
//       	rhoMapRatio = rhoSurfaceCache / RHO_SURFACE / RHO_STANDARD_RATIO;
//    }
    
//	public void computeZoomBox() {
//		blackPaddingWidth =  BLACK_PADDING_RATIO * rhoGlobe ;/// BLACK_PADDING_RATIO; // 261.92
//		outsideZoomBoxWidth = blackPaddingWidth surfaceMapWidth / rhoMapRatio * 0.1;
//		globeFullLength = 2 * (surfaceMapWidth * rhoMapRatio + outsideZoomBoxWidth); // 1440.58
//		globeBoxRatio = globeFullLength / NEW_GLOBE_BOX_HEIGHT; // 3.20
//
//		widthZoomBox = (int) (2 * NEW_GLOBE_BOX_HEIGHT * RATIO_EXPANSION / globeBoxRatio); // 140.57
//		paddingZoomBox = (int) (NEW_GLOBE_BOX_HEIGHT - widthZoomBox) / 2; // 154.72
//		
//		logger.info("rRatio: " + Math.round(rhoMapRatio * 10.0)/10.0
////				+ "  BLACK_PADDING_RATIO: " + Math.round(BLACK_PADDING_RATIO * 10.0)/10.0
////				+ "  rhoSurfaceCache: " + Math.round(rhoSurfaceCache * 10.0)/10.0
//				+ "  B: " + Math.round(blackPaddingWidth  * 10.0)/10.0
//				+ "  O: " + Math.round(outsideZoomBoxWidth * 10.0)/10.0
//				+ "  globeFullLength: " + Math.round(globeFullLength * 10.0)/10.0
//				+ "  globeBoxRatio: " + Math.round(globeBoxRatio * 10.0)/10.0
//				+ "  widthZoomBox: " + Math.round(widthZoomBox * 10.0)/10.0
//				+ "  paddingZoomBox: " + Math.round(paddingZoomBox * 10.0)/10.0
//				);
//	}
//
//	/**
//	 * Updates the position of the zoom box.
//	 */
//	public void updateZoomBox() {
//		if (navwin.getMapPanel() != null) {
//			surfaceMapWidth = navwin.getMapPanel().getMap().getPixelWidth();	
//			computeZoomBox();
//		}
//	}
    
	/*
	 * Sets up the mouse dragging capability.
	 */
	public void setMouseDragger(NavigatorWindow navwin) {

		// Note: need navWin prior to calling addMouseMotionListener()
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int dx, dy, x = e.getX(), y = e.getY();

				dx = dragx - x;
				dy = dragy - y;

				if ((dx != 0 || dy != 0) 
					 && x > 0 && x < NEW_GLOBE_BOX_HEIGHT 
					 && y > 0 && y < NEW_GLOBE_BOX_HEIGHT) {
					
					centerCoords = centerCoords.convertRectToSpherical((double) dx, (double) dy, rhoGlobe);
					navwin.updateCoordsMaps(centerCoords);				
					recreate = false;
					// Regenerate globe if recreate is true, then display
					drawSphere();
				}

				dragx = x;
				dragy = y;
			}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				dragx = e.getX();
				dragy = e.getY();
				setCursor(new Cursor(Cursor.MOVE_CURSOR));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				dragx = 0;
				dragy = 0;
				navwin.updateCoordsMaps(centerCoords);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
	}
	
	/**
	 * Displays topographical globe, regenerating if necessary.
	 */
	public void setMapType(MapMetaData newMapType) {
		if (!newMapType.equals(mapType)) {
			globeMap = new GlobeMap(this, newMapType, this);
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
		if (globeMap != null) {
			if (recreate) {
				recreate = false;
			}
		
			// Updates the position of the zoom box
//			updateZoomBox();
			
			// Regenerate globe
			drawSphere();
		}
	}
	
	public void drawSphere() {
		globeMap.drawSphere(centerCoords);
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
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		g2d.setColor(Color.black);
		// dbg.fillRect(0, 0, 150, 150);
		g2d.fillRect(0, 0, NEW_GLOBE_BOX_HEIGHT, NEW_GLOBE_BOX_HEIGHT);
		// Image starfield = ImageLoader.getImage("starfield.gif");
		g2d.drawImage(starfield, 0, 0, Color.black, null);
		
		if (globeMap == null)
			return;
		
		Image image = globeMap.getGlobeImage();
		if (image != null) {
			if (globeMap.isImageDone()) {
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
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
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
		g.drawString(latitude, (int)(25 * RATIO_EXPANSION), (int) (30 * RATIO_EXPANSION));
		g.drawString(longitude, (int)((STANDARD_GLOBE_BOX_HEIGHT - 25) * RATIO_EXPANSION - rightWidth), (int)(30 * RATIO_EXPANSION));

		String latString = centerCoords.getFormattedLatitudeString();
		String longString = centerCoords.getFormattedLongitudeString();

		int latWidth = positionMetrics.stringWidth(latString);
		int longWidth = positionMetrics.stringWidth(longString);

		int latPosition = (int)(((leftWidth - latWidth) / 2) + 25 * RATIO_EXPANSION);
		int longPosition = (int)(NEW_GLOBE_BOX_HEIGHT - latPosition - longWidth - 2.5 * RATIO_EXPANSION);

		g.drawString(latString, latPosition, (int)(50 * RATIO_EXPANSION));
		g.drawString(longString, longPosition, (int)(50 * RATIO_EXPANSION));
		
//		g.setColor(Color.black);

//		g.drawRect(paddingZoomBox, paddingZoomBox, widthZoomBox, widthZoomBox);
//
//		// Draw a diagonal line from top left of the zoom box to top left of the right panel	
//		drawDashedLine(g, paddingZoomBox, paddingZoomBox, NEW_GLOBE_BOX_HEIGHT, 0);
//		// Draw a diagonal line from top right of the zoom box to top left of the right panel	
//		drawDashedLine(g, paddingZoomBox + widthZoomBox, paddingZoomBox, NEW_GLOBE_BOX_HEIGHT, 0);
//		// Draw a diagonal line from bottom left of the zoom box to bottom left of the right panel	
//		drawDashedLine(g, paddingZoomBox, paddingZoomBox + widthZoomBox, NEW_GLOBE_BOX_HEIGHT, NEW_GLOBE_BOX_HEIGHT);
//		// Draw a diagonal line from bottom right of the zoom box to bottom left of the right panel	
//		drawDashedLine(g, paddingZoomBox + widthZoomBox, paddingZoomBox + widthZoomBox, NEW_GLOBE_BOX_HEIGHT, NEW_GLOBE_BOX_HEIGHT);
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
		return Coordinates.findRectPosition(unitCoords, centerCoords, rhoGlobe, halfMap, lowEdge);
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

	public NavigatorWindow getNavigatorWindow() {
		return navwin;
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
		desktop  = null;
		globeMap = null;		
		centerCoords = null;
		dbg = null;
		dbImage = null;
		starfield = null;
	}
}
