/*
 * Mars Simulation Project
 * MapPanel.java
 * @date 2023-06-19
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.map;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Painter;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;

import com.mars_sim.core.map.IntegerMapData;
import com.mars_sim.core.map.MapData;
import com.mars_sim.core.map.MapDataFactory;
import com.mars_sim.core.map.MapDataUtil;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;
import com.mars_sim.ui.swing.tool.mission.NavpointPanel;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;

@SuppressWarnings("serial")
public class MapPanel extends JPanel implements MouseWheelListener {

	private static final Logger logger = Logger.getLogger(MapPanel.class.getName());
	
	private static final double HALF_PI = Math.PI / 2d;

	private static final int SCALE_CONVERSION = 3;
	public static final int MAP_BOX_HEIGHT = MapDisplay.MAP_BOX_HEIGHT;
	public static final int MAP_BOX_WIDTH = MapDisplay.MAP_BOX_WIDTH;
	private static int dragx, dragy;

	private transient ExecutorService executor;

	// Data members
	private boolean mouseDragging;
	private boolean mapError;
	private boolean wait;
	private boolean recreateMap = false;
	
	public static double RHO_DEFAULT = MAP_BOX_HEIGHT / Math.PI;
	public static double MAX_RHO;
	public static double MIN_RHO;
	
	private static final double ZOOM_STEP = 16;

	private double multiplier;

	private String mapErrorMessage;
	
	private Coordinates centerCoords;

	private Image mapImage;

	private MapDisplay marsMap;
	
	private MainDesktopPane desktop;

	private NavigatorWindow navwin;
	
	private NavpointPanel navPanel;

	private Image starfield;
	
	private JSlider zoomSlider;

	
	private List<MapLayer> mapLayers;

	private final MapDataFactory mapFactory = MapDataUtil.instance().getMapDataFactory();

	private MapData backgroundMapData;

	/**
	 * Constructor 1.
	 * 
	 * @param desktop
	 * @param navwin
	 */
	public MapPanel(MainDesktopPane desktop, NavigatorWindow navwin) {
		this(desktop);
		this.navwin = navwin;
	}
	
	/**
	 * Constructor 2.
	 * 
	 * @param desktop
	 * @param navPanel
	 */
	public MapPanel(MainDesktopPane desktop, NavpointPanel navPanel) {
		this(desktop);
		this.navPanel = navPanel;
	}
	
	/**
	 * Constructor 3.
	 * 
	 * @param desktop
	 * @param refreshRate
	 */
	public MapPanel(MainDesktopPane desktop, long refreshRate) {
		this(desktop);
		this.desktop = desktop;
	}
	
	/**
	 * Constructor 4.
	 * 
	 * @param desktop
	 */
	public MapPanel(MainDesktopPane desktop) {
		super();
		this.desktop = desktop;
		
		init();
	}
	
	public void init() {

		setPreferredSize(new Dimension(MAP_BOX_WIDTH, MAP_BOX_HEIGHT));
		setMaximumSize(getPreferredSize());
		setSize(getPreferredSize());
		
		starfield = ImageLoader.getImage("map/starfield");
		
		executor = Executors.newSingleThreadExecutor();
		
		// Initializes map to default map level 0
		loadMap(MapDataFactory.DEFAULT_MAP_TYPE, 0);
		
		mapError = false;
		wait = false;
		mapLayers = new CopyOnWriteArrayList<>();
		centerCoords = new Coordinates(HALF_PI, 0D);
	
		buildZoomSlider();

		addMouseWheelListener(this);
		
		setLayout(new BorderLayout(10, 20));
		
		JPanel zoomPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 40));
       	add(zoomPane, BorderLayout.EAST);
       	
	    zoomPane.setBackground(new Color(0, 0, 0, 128));
	    zoomPane.setOpaque(false);
	    zoomPane.setAlignmentX(RIGHT_ALIGNMENT);
	    zoomPane.setAlignmentY(CENTER_ALIGNMENT);
       	zoomPane.add(zoomSlider);
		
		// Note: rho = pixelHeight / Math.PI;
		MAX_RHO = IntegerMapData.maxRho;
		MIN_RHO = IntegerMapData.minRho;
		multiplier = RHO_DEFAULT / ZOOM_STEP;

		logger.info("RHO_DEFAULT: " + Math.round(RHO_DEFAULT * 10.0)/10.0 + ".  multiplier: " + Math.round(multiplier * 10.0)/10.0 + ".");
	}

	private void buildZoomSlider() {

		UIDefaults sliderDefaults = new UIDefaults();

        sliderDefaults.put("Slider.thumbWidth", 15);
        sliderDefaults.put("Slider.thumbHeight", 15);
        sliderDefaults.put("Slider:SliderThumb.backgroundPainter", new Painter<JComponent>() {
            public void paint(Graphics2D g, JComponent c, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2f));
                g.setColor(Color.BLACK);
                g.fillOval(1, 1, w-1, h-1);
                g.setColor(Color.WHITE);
                g.drawOval(1, 1, w-1, h-1);
            }
        });
        sliderDefaults.put("Slider:SliderTrack.backgroundPainter", new Painter<JComponent>() {
            public void paint(Graphics2D g, JComponent c, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2f));
                g.setColor(Color.BLACK);
                g.fillRoundRect(0, 6, w, 6, 6, 6);
                g.setColor(Color.WHITE);
                g.drawRoundRect(0, 6, w, 6, 6, 6);
            }
        });

        zoomSlider = new JSlider(SwingConstants.VERTICAL, 0, 
        		(int)computeSliderValue(IntegerMapData.MAX_RHO_MULTIPLER), 10);
        zoomSlider.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 100));
        zoomSlider.setPreferredSize(new Dimension(60, 400));
        zoomSlider.setSize(new Dimension(60, 400));
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setForeground(Color.ORANGE.darker().darker());
		zoomSlider.setBackground(new Color(0, 0, 0, 128));
		zoomSlider.setOpaque(false);
		
		zoomSlider.setVisible(true);
		
		zoomSlider.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$
		zoomSlider.addChangeListener(e -> {
				// Change scale of map based on slider position.
				int newSliderValue = zoomSlider.getValue();
				double oldScale = getScale();	
				
				double oldRho = getRho();
				
				double scale = computeScale(newSliderValue);
				
				double rho = MapPanel.RHO_DEFAULT * scale;
				
				if (rho > MapPanel.MAX_RHO) {
					rho = MapPanel.MAX_RHO;
					scale = rho / MapPanel.RHO_DEFAULT;
				}
				else if (rho < MapPanel.MIN_RHO) {
					rho = MapPanel.MIN_RHO;
					scale = rho / MapPanel.RHO_DEFAULT;
				}
	
				if (scale != oldScale) {				
					setScale(scale);
				}
				
				if (rho != oldRho) {	
					// Note: Call setRho() will redraw the map
					setRho(rho);
				}

				
//				logger.info("res: " + mapPanel.getMapResolution()
//						+ "  newSliderValue: " + Math.round(newSliderValue * 10.0)/10.0 
//						+ "  Scale: " + Math.round(oldScale* 100.0)/100.0
//						+ " -> " + Math.round(scale* 1000.0)/1000.0
//						+ "  RHO_DEFAULT: " +  Math.round(MapPanel.RHO_DEFAULT * 10.0)/10.0 
//						+ "  rho: " + Math.round(oldRho* 10.0)/10.0
//						+ " -> " + Math.round(rho* 10.0)/10.0);

		});
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();	
		for (int i = 1; i < IntegerMapData.MAX_RHO_MULTIPLER + 1; i++) {
			labelTable.put((int)computeSliderValue(i), new JLabel(i + ""));
		}
//		labelTable.put(0, new JLabel("1/4"));
		zoomSlider.setLabelTable(labelTable);
    }
	
	/**
	 * Explicitly changes the scale and sets the zoom slider value.
	 * 
	 * @param rho
	 */
	public void updateScaleZoomSlider(double rho) {
		
		double newScale = rho / MapPanel.RHO_DEFAULT;
		
		if (getScale() != newScale && newScale < (int)computeSliderValue(IntegerMapData.MAX_RHO_MULTIPLER)) {
			setScale(newScale);

			double newSliderValue = computeSliderValue(newScale);
			
			zoomSlider.setValue((int)(Math.round(newSliderValue * 10.0)/10.0));
		}
	}
	
	/**
	 * Computes the new slider value.
	 * 
	 * @param scale
	 * @return
	 */
	private double computeSliderValue(double scale) {
		return (scale * IntegerMapData.MIN_RHO_FRACTION - SCALE_CONVERSION) * IntegerMapData.MAX_RHO_MULTIPLER;
	}
	
	/**
	 * Computes the new scale.
	 * 
	 * @param sliderValue
	 * @return
	 */
	private double computeScale(double sliderValue) {
		return (1.0 * sliderValue / IntegerMapData.MAX_RHO_MULTIPLER + SCALE_CONVERSION) / IntegerMapData.MIN_RHO_FRACTION;
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double movement = e.getPreciseWheelRotation();
		// Note: limiting the mouse movement to incrementing or decrementing 1 only
		// to lower the need of having to render a new map excessively		
		if (movement > 0) {
			// Move mouse wheel rotated down, thus moving down zoom slider.
			zoomSlider.setValue(zoomSlider.getValue() - 1);
		}
		else if (movement < 0) {
			// Move mouse wheel rotated up, thus moving up zoom slider.
			zoomSlider.setValue(zoomSlider.getValue() + 1);
		}
	}

	/**
	 * Gets the map resolution.
	 * 
	 * @return
	 */
	public int getMapResolution() {
		return marsMap.getResolution();
	}
	
	/*
	 * Sets up the mouse dragging capability.
	 */
	public void setMouseDragger(boolean isNavigator) {

		// Detect the mouse scroll
		addMouseWheelListener(this);
		
		// Note: need navWin prior to calling addMouseMotionListener()
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				int dx = dragx - x;
				int dy = dragy - y;

				if ((dx != 0 || dy != 0) 
					 && x > 0 && x < MAP_BOX_WIDTH 
					 && y > 0 && y < MAP_BOX_HEIGHT) {
					
					mouseDragging = true;
					
					// Update the centerCoords while dragging
					centerCoords = centerCoords.convertRectIntToSpherical(dx, dy, marsMap.getRho());
					// Do we really want to update the map while dragging ? 
					// Yes. It's needed to provide smooth viewing of the surface map
					marsMap.drawMap(centerCoords, getRho());
				
					repaint();
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

				mouseDragging = true;
				
				setCursor(new Cursor(Cursor.MOVE_CURSOR));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				dragx = 0;
				dragy = 0;
				
				mouseDragging = false;

				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
	}

	public boolean isMouseDragging() {
		return mouseDragging;
	}

	public boolean isChanging() {
		return mouseDragging;
	}
	
	/**
	 * Adds a new map layer.
	 * 
	 * @param newLayer the new map layer.
	 * @param index    the index order of the map layer.
	 */
	public void addMapLayer(MapLayer newLayer, int index) {
		if (newLayer != null) {
			if (!mapLayers.contains(newLayer)) {
				if (index < mapLayers.size()) {
					mapLayers.add(index, newLayer);
				} else {
					mapLayers.add(newLayer);
				}
			}
		} else
			throw new IllegalArgumentException("newLayer is null");
	}

	/**
	 * Removes a map layer.
	 * 
	 * @param oldLayer the old map layer.
	 */
	public void removeMapLayer(MapLayer oldLayer) {
		if (oldLayer != null) {
			if (mapLayers.contains(oldLayer)) {
				mapLayers.remove(oldLayer);
			}
		} else
			throw new IllegalArgumentException("oldLayer is null");
	}

	/**
	 * Checks if map has a map layer.
	 * 
	 * @param layer the map layer.
	 * @return true if map has the map layer.
	 */
	public boolean hasMapLayer(MapLayer layer) {
		return mapLayers.contains(layer);
	}
	   
	/**
	 * Gets the current map type instance.
	 * 
	 * @return MapMetaData
	 */
	public MapMetaData getMapMetaData() {
		return marsMap.getMapMetaData();
	}

	/**
	 * Gets the new map type instance.
	 * 
 	 * @param newMapType
	 * @return MapMetaData
	 */
	public MapMetaData getNewMapMetaData(String newMapType) {
		return mapFactory.getMapMetaData(newMapType);
	}
	
	public MapDisplay getMap() {
		return marsMap;
	}

	/**
	 * Loads the new map type.
	 * 
	 * @param mapStringType
	 * @param res
	 * @return Display was updated immediately
	 */
	public boolean loadMap(String newMapString, int res) {
		
		var newMapData = mapFactory.loadMapData(newMapString, res, getRho());
		if (newMapData.isReady()) {
			// It is ready
			createMapDisplay(newMapData);
			return true;
		}

		// Wait for this map to load
		backgroundMapData = newMapData;
		return false;
	}

	private void createMapDisplay(MapData newMapData) {
		marsMap = new CannedMarsMap(this, newMapData);

		// Redefine map param
		RHO_DEFAULT = IntegerMapData.rhoDefault;
		MAX_RHO = IntegerMapData.maxRho;
		MIN_RHO = IntegerMapData.minRho;
		multiplier = RHO_DEFAULT / ZOOM_STEP;
				
		recreateMap = true;
		
		showMap(centerCoords, getRho());
	}

	public Coordinates getCenterLocation() {
		return centerCoords;
	}
	
	public void showMap(Coordinates newCenter) {
		showMap(newCenter, getRho());
	}
	
	/**
	 * Displays map at given center, regenerating if necessary.
	 *
	 * @param newCenter the center location for the globe
	 * @param rho
	 */
	public void showMap(Coordinates newCenter, double rho) {
		if (newCenter == null) 
			return;
		
		if (centerCoords == null
			|| rho != getRho()
			|| !centerCoords.equals(newCenter)
			) {
				recreateMap = true;
				centerCoords = newCenter;
		}
		else {
			recreateMap = false;
		}
			
		if (recreateMap) {
			wait = true;
			updateDisplay(rho);
			recreateMap = false;
		}
	}

	
	/**		
	 * Updates the map display.
	 * @return Map displayed was changed
	 */
	public boolean updateDisplay() {
		boolean changed = false;
		if ((backgroundMapData != null) && backgroundMapData.isReady()) {
			// Background map is done so display it
			createMapDisplay(backgroundMapData);
			backgroundMapData = null;
			changed = true;
		}
		updateDisplay(getRho());

		return changed;
	}

	/**
	 * Updates the display on a thread.
	 * 
	 * @param rho
	 */
	private void updateDisplay(double rho) {
		if ((desktop.isToolWindowOpen(NavigatorWindow.NAME) 
			|| desktop.isToolWindowOpen(MissionWindow.NAME))
			&& (!executor.isTerminated() || !executor.isShutdown())) {
				executor.execute(new MapTask(rho));
		}
	}

	/**
	 * The task class for drawing the surface map.
	 */
	class MapTask implements Runnable {

		private double rho;
		
		private MapTask(double rho) {
			this.rho = rho;
		}

		@Override
		public void run() {
			try {
				mapError = false;

				if (centerCoords == null) {
					logger.severe("centerCoords is null.");
					centerCoords = new Coordinates(HALF_PI, 0);
				}

				marsMap.drawMap(centerCoords, rho);
				
				// Update the zoom slider value
				updateScaleZoomSlider(rho);
//				if (navwin != null)
//					navwin.updateScaleZoomSlider(rho);
//				else
//					updateScale(rho);
				
				wait = false;
				repaint();
				
			} catch (Exception e) {
				mapError = true;
				mapErrorMessage = e.getMessage();
				logger.severe("Can't draw surface map: " + e);
			}
		}
	}
	
	/**
	 * Explicitly changes the scale.
	 * 
	 * @param rho
	 */
	public void updateScale(double rho) {
		
		double newScale = rho / MapPanel.RHO_DEFAULT;
		
		if (getScale() != newScale) {

			setScale(newScale);
		}
	}
	
	@Override	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (desktop != null && isShowing() && 
        		(desktop.isToolWindowOpen(NavigatorWindow.NAME)
        		|| desktop.isToolWindowOpen(MissionWindow.NAME))) {
	        
        	Graphics2D g2d = (Graphics2D) g.create();
	        
        	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			
	        if (wait) {
	        	String message = "Generating Map";
	        	drawCenteredMessage(message, g2d);
	        }
	        else {
	        	if (mapError) {
	            	logger.log(Level.SEVERE,"mapError: " + mapErrorMessage);
	                // Draw error message
	                if (mapErrorMessage == null) mapErrorMessage = "Null Map";
	                drawCenteredMessage(mapErrorMessage, g2d);
	            }
	        	else {
	        		
	        		// Clear the background with white
//	        		// Not working: g2d.clearRect(0, 0, Map.DISPLAY_WIDTH, Map.DISPLAY_HEIGHT);
	        		// Paint black background
	        		g2d.setColor(Color.BLACK);
	                
	        		g2d.fillRect(0, 0, MapDisplay.MAP_BOX_WIDTH, MapDisplay.MAP_BOX_HEIGHT);
//	        		Not working: g2d.drawImage(starfield, 0, 0, Color.black, this);
//	        		Not working: g2d.setComposite(AlphaComposite.SrcOver); 
//	        		Not working: g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f)); 
	        		// or 0.0f)); // draw transparent background
	        		// or 1.0f)); // turn on opacity
	        		
//	        		Not working: g2d.fillRect(0, 0, Map.MAP_BOX_WIDTH, Map.MAP_BOX_HEIGHT);
        		
	                if (centerCoords != null) {
	                	if (marsMap != null && marsMap.isImageDone()) {
	                		mapImage = marsMap.getMapImage();
	                		if (mapImage != null) {
	                			g2d.drawImage(mapImage, 0, 0, this);  
	                		}         		
	                	}
	                	else
	                		return;
	
	                	// Display map layers.
	                	
	                	// Put the map layers here.
	                	// Say if the location of a vehicle is updated
	                	// it doesn't have to redraw the marsMap.
	                	// It only have to redraw its map layer below
	                	Iterator<MapLayer> i = mapLayers.iterator();
	                	while (i.hasNext()) i.next().displayLayer(centerCoords, marsMap, g);
              		
		        		g2d.setBackground(Color.BLACK);
	                }
	        	}
	        }
	        
	        g2d.dispose();
        }
    }

	/**
	 * Draws a message string in the center of the map panel.
	 * 
	 * @param message the message string
	 * @param g       the graphics context
	 */
	private void drawCenteredMessage(String message, Graphics g) {

		// Set message color
		g.setColor(Color.green);

		// Set up font
		Font messageFont = new Font("SansSerif", Font.BOLD, 25);
		g.setFont(messageFont);
		FontMetrics messageMetrics = getFontMetrics(messageFont);

		// Determine message dimensions
		int msgHeight = messageMetrics.getHeight();
		int msgWidth = messageMetrics.stringWidth(message);

		// Determine message draw position
		int x = (MapDisplay.MAP_BOX_WIDTH - msgWidth) / 2;
		int y = (MapDisplay.MAP_BOX_HEIGHT + msgHeight) / 2;

		// Draw message
		g.drawString(message, x, y);
	}

	/**
	 * Gets the true surface lat and lon coordinates of the mouse pointer.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
    public Coordinates getMouseCoordinates(int x, int y) {
		double xx = x - MapDisplay.MAP_BOX_WIDTH / 2.0;
		double yy = y - MapDisplay.MAP_BOX_HEIGHT / 2.0;
		// Based on the current centerCoords
		return centerCoords.convertRectToSpherical(xx, yy, marsMap.getRho());
    }

    /**
     * Gets the rho of the Mars surface map.
     * 
     * @return
     */
    public double getRho() {
    	if (marsMap != null)
    		return marsMap.getRho();
    	
    	return RHO_DEFAULT;
    }

	/**
	 * Sets the map rho.
	 *
	 * @param rho
	 */
	public void setRho(double rho) {
		if (marsMap != null) {
			marsMap.drawMap(centerCoords, rho);
			repaint();
		}
	}
	
    
    /**
     * Gets the scale of the Mars surface map.
     * 
     * @return
     */
    public double getScale() {
    	return getRho() / RHO_DEFAULT;
    }

	/**
	 * Sets the map scale.
	 *
	 * @param scale
	 */
	public void setScale(double scale) {
		double newRho = scale * RHO_DEFAULT;
		setRho(newRho);
	}

	/**
	 * Prepares map panel for deletion.
	 */
	public void destroy() {
		// Remove clock listener.
		mapLayers = null;
		if (executor != null) {
			// Stop anything running
			executor.shutdownNow();
		}
		executor = null;
		marsMap = null;
		mapImage = null;
	}

}
