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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Painter;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;

import com.mars_sim.core.data.Range;
import com.mars_sim.core.map.MapData;
import com.mars_sim.core.map.MapDataFactory;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.MapData.MapState;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;
import com.mars_sim.ui.swing.tool.mission.NavpointPanel;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;

@SuppressWarnings("serial")
public class MapPanel extends JPanel implements MouseWheelListener {

	private static final Logger logger = Logger.getLogger(MapPanel.class.getName());
	
	private static final double HALF_PI = Math.PI / 2d;

	public static final int MAP_BOX_HEIGHT = MapDisplay.MAP_BOX_HEIGHT;
	public static final int MAP_BOX_WIDTH = MapDisplay.MAP_BOX_WIDTH;

	private static final int MAX_SLIDER = 100;	// Slider internal value max
	private static final int SLIDER_LABELS = 6; // Number of labels on slider

	private int dragx;
	private int dragy;

	private transient ExecutorService executor;

	// Data members
	private boolean mouseDragging;
	private boolean mapError;
	private boolean wait;

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

	private MapData backgroundMapData;

	private static final boolean SHOW_MAP_DETAILS = true;
	private JLabel mapDetails;
	private JLabel statusLabel;

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
	private MapPanel(MainDesktopPane desktop) {
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
		
		mapError = false;
		wait = false;
		mapLayers = new CopyOnWriteArrayList<>();
		centerCoords = new Coordinates(HALF_PI, 0D);
	
		buildZoomSlider();

		// Initializes map to default map level 0
		loadMap(MapDataFactory.DEFAULT_MAP_TYPE, 0);

		addMouseWheelListener(this);
		
		setLayout(new BorderLayout(10, 20));
		
		JPanel zoomPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 40));
       	add(zoomPane, BorderLayout.EAST);
       	
	    zoomPane.setBackground(new Color(0, 0, 0, 128));
	    zoomPane.setOpaque(false);
	    zoomPane.setAlignmentX(RIGHT_ALIGNMENT);
	    zoomPane.setAlignmentY(CENTER_ALIGNMENT);
       	zoomPane.add(zoomSlider);

		// Build the status panel
		statusLabel = new JLabel("");
		statusLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		statusLabel.setOpaque(false);
		add(statusLabel, BorderLayout.SOUTH);

		// Build the map debug message
		if (SHOW_MAP_DETAILS) {
			mapDetails = new JLabel("");
			mapDetails.setOpaque(true);
			add(mapDetails, BorderLayout.NORTH);
		}
	}

	/**
	 * Update the status message. If the new message is null then the label is hidden
	 * @param msg New message
	 */
	private void setStatusLabel(String msg) {
		if (msg == null) {
			statusLabel.setText("");
			statusLabel.setOpaque(false);
		}
		else {
			statusLabel.setText(msg);
			statusLabel.setOpaque(true);
		}
	}

	private void buildZoomSlider() {

		UIDefaults sliderDefaults = new UIDefaults();

        sliderDefaults.put("Slider.thumbWidth", 15);
        sliderDefaults.put("Slider.thumbHeight", 15);
        sliderDefaults.put("Slider:SliderThumb.backgroundPainter", (Painter<JComponent>) (g, c, w, h) -> {
		    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g.setStroke(new BasicStroke(2f));
		    g.setColor(Color.BLACK);
		    g.fillOval(1, 1, w-1, h-1);
		    g.setColor(Color.WHITE);
		    g.drawOval(1, 1, w-1, h-1);
		});
        sliderDefaults.put("Slider:SliderTrack.backgroundPainter", (Painter<JComponent>) (g, c, w, h) -> {
		    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g.setStroke(new BasicStroke(2f));
		    g.setColor(Color.BLACK);
		    g.fillRoundRect(0, 6, w, 6, 6, 6);
		    g.setColor(Color.WHITE);
		    g.drawRoundRect(0, 6, w, 6, 6, 6);
		});

        zoomSlider = new JSlider(SwingConstants.VERTICAL, 0, MAX_SLIDER, 25);
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
		zoomSlider.addChangeListener(e -> applyZoomToMap());

		Dictionary<Integer, JLabel> labelTable = new Hashtable<>();	
		for (int i = 1; i <= SLIDER_LABELS; i++) {
			labelTable.put(i * (MAX_SLIDER/SLIDER_LABELS), new JLabel(Integer.toString(i)));
		}
		zoomSlider.setLabelTable(labelTable);
    }
		
	/**
	 * Applies the zoom slider to the current map. This means convert the slider value
	 * into a rho that is between the min & max of the MapDisplay.
	 * This will update the rho value and hence redraw map.
	 */
	private void applyZoomToMap() {

		// Change scale of map based on slider position.
		double sliderRatio = (double)zoomSlider.getValue()/MAX_SLIDER;
		Range rhoRange = marsMap.getRhoRange();
						
		// Rho is the slider ratio applied to the min & max
		double newRho = rhoRange.min() + ((rhoRange.max() - rhoRange.min()) * sliderRatio);

		// Note: Call setRho() will redraw the map
		setRho(newRho);
	}

	/**
	 * The mouse wheel has moved.
	 */	
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
		return MapDataFactory.getMapMetaData(newMapType);
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
		if (backgroundMapData != null) {
			logger.warning("Map already loading in the background");
			return false;
		}

		var mapmeta = MapDataFactory.getMapMetaData(newMapString);
		if (mapmeta == null) {
			logger.severe("No map meta with id " + newMapString);
			return false;
		}

		var newMapData = mapmeta.getData(res);
		if (newMapData.getStatus() == MapState.LOADED) {
			// It is ready
			createMapDisplay(newMapData);
			return true;
		}

		// Wait for this map to load
		backgroundMapData = newMapData;
		setStatusLabel("Loading " + mapmeta.getDescription() + " level:" + res);
		return false;
	}

	private void createMapDisplay(MapData newMapData) {
		marsMap = new CannedMarsMap(this, newMapData);

		// Apply the current user's Zoom to the new map; this will redraw
		applyZoomToMap();
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
	private void showMap(Coordinates newCenter, double rho) {
		if (newCenter == null) 
			return;
		
		boolean recreateMap = false;
		if (centerCoords == null
			|| rho != getRho()
			|| !centerCoords.equals(newCenter)
			) {
				recreateMap = true;
				centerCoords = newCenter;
		}
			
		if (recreateMap) {
			wait = true;
			updateDisplay(rho);
		}
	}
	
	/**		
	 * Updates the map display.
	 * @return Map displayed was changed
	 */
	public boolean updateDisplay() {
		boolean changed = false;
		if (backgroundMapData != null) {
			var state = backgroundMapData.getStatus();
			if (state == MapState.LOADED) {
				// Background map is done so display it
				createMapDisplay(backgroundMapData);
				backgroundMapData = null;
				changed = true;
				setStatusLabel(null);
			}
			else if (state == MapState.FAILED) {
				logger.warning("Background loading failed");
				backgroundMapData = null;
				setStatusLabel(null);
			}
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
				if (rho == 0D) {
					// Should never happen but it can
					rho = marsMap.getRhoDefault();
					logger.warning("RHO requested is zero");
				}
				
				// Add some debug
				if (mapDetails != null) {
					var range = marsMap.getRhoRange();
					String buf = "id: " + marsMap.getMapMetaData().getId() 
								+ "  res:" + marsMap.getResolution()
								+ "  range: " + StyleManager.DECIMAL_PLACES2.format(range.min())
								+ "->" + StyleManager.DECIMAL_PLACES2.format(range.max())
								+ "  rho: " + StyleManager.DECIMAL_PLACES2.format(rho);
					mapDetails.setText(buf);
				}

				marsMap.drawMap(centerCoords, rho);
				
				wait = false;
				repaint();
				
			} catch (Exception e) {
				mapError = true;
				mapErrorMessage = e.getMessage();
				logger.severe("Can't draw surface map: " + e);
			}
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
	        		// Not working: g2d.clearRect(0, 0, Map.DISPLAY_WIDTH, Map.DISPLAY_HEIGHT)
	        		// Paint black background
	        		g2d.setColor(Color.BLACK);
	                
	        		g2d.fillRect(0, 0, MapDisplay.MAP_BOX_WIDTH, MapDisplay.MAP_BOX_HEIGHT);
        		
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
	                	for( var i : mapLayers) {
	                		i.displayLayer(centerCoords, marsMap, g);
						}
              		
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
	 * Get teh parent desktop
	 */
	public MainDesktopPane getDesktop() {
		return desktop;
	}

    /**
     * Gets the rho of the Mars surface map.
     * 
     * @return
     */
    public double getRho() {
    	if (marsMap != null)
    		return marsMap.getRho();
    	
    	return 0;
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
    	return getRho() / marsMap.getRhoDefault();
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
