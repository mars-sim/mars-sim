/*
 * Mars Simulation Project
 * MapPanel.java
 * @date 2023-06-19
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import com.mars_sim.core.data.Range;
import com.mars_sim.core.map.MapData;
import com.mars_sim.core.map.MapData.MapState;
import com.mars_sim.core.map.MapDataFactory;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;

@SuppressWarnings("serial")
public class MapPanel extends JPanel implements MouseWheelListener {

	private static final Logger logger = Logger.getLogger(MapPanel.class.getName());
	
	private static final double HALF_PI = Math.PI / 2d;

	private static final int MAX_SLIDER = 100;	// Slider internal value max
	private static final int SLIDER_LABELS = 6; // Number of labels on slider

	private static final boolean SHOW_MAP_DETAILS = false; // Enable for map details in header

	/**
	 * Control for the MapLayer to handle the visibility of the layer and the layer itself.
	 */
	private static class LayerControl {
		MapLayer layer;
		boolean visible;
		
		public LayerControl(MapLayer layer) {
			this.layer = layer;
			this.visible = true;
		}
	}
	
	private transient ExecutorService executor;

	// Data members
	private boolean mapError;

	private String mapErrorMessage;
	
	private Coordinates centerCoords;

	private MapDisplay marsMap;
	private UIContext desktop;
	
	private JSlider zoomSlider;
	
	private List<LayerControl> mapLayers;

	private JLabel mapDetails;
	private JLabel statusLabel;

	private List<MapHotspot> hotspots = new ArrayList<>();

	// Local callbacks to mouse actions
	private Consumer<Coordinates> mouseMover;
	private Consumer<Coordinates> mouseClicker;

	/**
	 * Constructor.
	 * 
	 * @param desktop
	 */
	public MapPanel(UIContext desktop) {
		super();
		this.desktop = desktop;

		setMinimumSize(new Dimension(100, 100));
		executor = Executors.newSingleThreadExecutor();
		
		mapError = false;
		mapLayers = new CopyOnWriteArrayList<>();
		centerCoords = new Coordinates(HALF_PI, 0D);
	
		buildZoomSlider();

		addMouseWheelListener(this);
		
		setLayout(new BorderLayout(10, 20));

		zoomSlider.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 10));
		zoomSlider.setAlignmentX(RIGHT_ALIGNMENT);
		zoomSlider.setAlignmentY(CENTER_ALIGNMENT);
		add(zoomSlider, BorderLayout.EAST);

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

		var mouseListener = new MapMouseListener(this);
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);

		addMouseWheelListener(this);
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
		var color = Color.WHITE;

        zoomSlider = new JSlider(SwingConstants.VERTICAL, 0, MAX_SLIDER, 25);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setForeground(color);
		zoomSlider.setOpaque(false);
		
		zoomSlider.setVisible(true);
		
		zoomSlider.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //-NLS-1$
		zoomSlider.addChangeListener(e -> setRho(calculateRHO()));

		Dictionary<Integer, JLabel> labelTable = new Hashtable<>();	
		for (int i = 1; i <= SLIDER_LABELS; i++) {
			var tick = new JLabel(Integer.toString(i));
			tick.setForeground(color);
			labelTable.put(i * (MAX_SLIDER/SLIDER_LABELS), tick);
		}
		zoomSlider.setLabelTable(labelTable);
    }

	/**
	 * Converts the RHO. This means convert the slider value
	 * into a rho that is between the min & max of the MapDisplay.
	 * This will update the rho value and hence redraw map.
	 */
	private double calculateRHO() {

		// Change scale of map based on slider position.
		double sliderRatio = (double)zoomSlider.getValue()/MAX_SLIDER;
		Range rhoRange = marsMap.getRhoRange();
						
		// Rho is the slider ratio applied to the min & max
		return rhoRange.min() + ((rhoRange.max() - rhoRange.min()) * sliderRatio);
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

	/**
	 * Adds a new map layer.
	 * 
	 * @param newLayer the new map layer.
	 */
	public void addMapLayer(MapLayer newLayer) {
		if (newLayer == null) {
			throw new IllegalArgumentException("newLayer is null");
		}

		if (mapLayers.stream().filter(ml -> ml.layer.equals(newLayer)).findFirst().isEmpty()) {
			mapLayers.add(new LayerControl(newLayer));
		}
	}

	/**
	 * Change visibility of a map layer.
	 * 
	 * @param layer the map layer.
	 * @param visible  the visibility of the map layer.
	 */
	public void setVisibleMapLayer(MapLayer layer, boolean visible) {
		if (layer == null) {
			throw new IllegalArgumentException("layer is null");
		}
		var found = mapLayers.stream().filter(ml -> ml.layer.equals(layer)).findFirst().orElse(null);
		if (found != null) {
			found.visible = visible;
		}
		else {
			logger.warning("Can't find layer for visibility: " + layer.getClass().getSimpleName());
		}
	}

	/**
	 * Checks if map has a map layer.
	 * 
	 * @param layer the map layer.
	 * @return true if map has the map layer.
	 */
	public boolean isLayerVisible(MapLayer layer) {
		return mapLayers.stream().filter(ml -> ml.layer.equals(layer)).findFirst().map(ml -> ml.visible).orElse(false);
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
		var mapmeta = MapDataFactory.getMapMetaData(newMapString);
		if (mapmeta == null) {
			logger.severe("No map meta with id " + newMapString);
			return false;
		}

		var newMapData = mapmeta.getData(res, this::loadCompleted);
		if (newMapData.getStatus() == MapState.LOADED) {
			// It is ready
			createMapDisplay(newMapData);
			return true;
		}

		// Wait for this map to load
		setStatusLabel("Loading " + mapmeta.getDescription() + " level:" + res);
		return false;
	}

	/**
	 * Callback when an async map data load is completed.
	 * @param md MapData loaded
	 */
	private void loadCompleted(MapData md) {
		var state = md.getStatus();
		if (state == MapState.LOADED) {
			// Background map is done so display it
			createMapDisplay(md);
			setStatusLabel(null);
		}
		else if (state == MapState.FAILED) {
			logger.warning("Background loading failed");
			setStatusLabel("Failed to load map data for " + md.getMetaData().getDescription());
		}
	}

	private void createMapDisplay(MapData newMapData) {
		marsMap = new CannedMarsMap(this, newMapData);

		// Apply the current user's Zoom to the new map; this will redraw map if the Panel is sized
		if (getWidth() > 0) {
			setRho(calculateRHO());
		}
	}

	public Coordinates getCenterLocation() {
		return centerCoords;
	}
	
	/**
	 * Show the map centred at newCenter, regenerating if necessary.
	 * @param newCenter
	 */
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
			updateDisplay(rho);
		}
	}
	
	/**		
	 * Updates the map display.
	 * 
	 * @return Map displayed was changed
	 */
	public void updateDisplay() {
		updateDisplay(getRho());
	}

	/**
	 * Updates the display on a thread.
	 * 
	 * @param rho
	 */
	private void updateDisplay(double rho) {
		if ((!executor.isTerminated() || !executor.isShutdown()) && (marsMap != null)) {
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
					// It happens at the start of the sim.
					if (marsMap == null) {
						return;
	 				}
					rho = marsMap.getRhoDefault();
					logger.warning("RHO requested is zero, default to " + rho);
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

				var sz = getSize();
				if ((sz.getWidth() > 0) && (sz.getHeight() > 0)) {
					marsMap.drawMap(centerCoords, rho, sz);
				}
				
				repaint();
				
			} catch (Exception e) {
				mapError = true;
				mapErrorMessage = e.getMessage();
				logger.log(Level.WARNING, "Can't draw surface map: " + e);
			}
		}
	}
	
	@Override	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		if (marsMap == null) {
			// First paint with no user defined map
			loadMap(MapDataFactory.DEFAULT_MAP_TYPE, 0);
			logger.warning("MarsMap is null, loading requested.");
		}

		if (mapError) {
			// Draw error message
			if (mapErrorMessage == null)
				mapErrorMessage = "Null Map";
			drawCenteredMessage(mapErrorMessage, g2d);
		}
		else {
			// Paint black background
			g2d.setColor(Color.BLACK);

			var size = getSize();
			
			g2d.fillRect(0, 0, (int)size.getWidth(), (int)size.getHeight());
		
			if ((centerCoords != null) && marsMap != null) {
				double activeRho = calculateRHO();
				Image mapImage = marsMap.getMapImage(centerCoords, activeRho, size);
				if (mapImage != null) {
					g2d.drawImage(mapImage, 0, 0, this);  
					mapImage.flush();
				}
				else {
					logger.warning("MapImage is null");
				}

				// Reset the hotspots
				hotspots = new ArrayList<>();

				// Display the layers and record any hotspots
				for (var i : mapLayers) {
					if (i.visible) {
						hotspots.addAll(i.layer.displayLayer(centerCoords, marsMap, g2d, size));
					}
				}
			
				g2d.setBackground(Color.BLACK);
			}
			else {
				drawCenteredMessage("Loading Map Data", g2d);

				logger.warning("Paint skipped " + 
						((centerCoords == null) ? "centerCoords is null. " : "") +
						((marsMap == null) ? "marsMap is null. " : "")
						);
			}
		}
	
	    g2d.dispose();
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
		int x = ((getWidth() - msgWidth) / 2);
		int y = ((getHeight() + msgHeight) / 2);

		// Draw message
		g.drawString(message, x, y);
	}

	/**
	 * Gets the parent desktop.
	 */
	public UIContext getDesktop() {
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
	private void setRho(double rho) {
		if (marsMap != null) {
			marsMap.drawMap(centerCoords, rho, getSize());
			repaint();
		}
	}

	/**
	 * Sets the mouse move listener. This is used to track the mouse movement as it crosses hotspots on the map such as a
	 * @param mouseMoveListener The listener to notify.
	 */
	public void setMouseMoveListener(Consumer<Coordinates> mouseMoveListener) {
		mouseMover = mouseMoveListener;
	}

	/**
	 * Notify the map panel that the mouse has moved to a new position.
	 * This is used to track the mouse movement as it crosses hotspots on the map such as a
	 * @param mousePos Mouse position
	 */
	void notifyMouseMoved(IntPoint mousePos) {
		if (mouseMover != null) {
			mouseMover.accept(convertMouseToCoordinates(mousePos));
		}
    }

	/**
	 * Notify the map panel that the mouse has been clicked at a position in terms of the Coordinates.
	 * @param dx Change in X
	 * @param dy Change in Y
	 */
	void dragPosition(int dx, int dy) {
		// Update the centerCoords while dragging
		centerCoords = centerCoords.convertRectIntToSpherical(dx, dy, marsMap.getRho());
		// Do we really want to update the map while dragging ? 
		// Yes. It's needed to provide smooth viewing of the surface map
		marsMap.drawMap(centerCoords, getRho(), getSize());
	
		repaint();
	}

	/**
	 * Notify the map panel that the mouse has been clicked at a position in terms of the Coordinates.
	 * @param mouseClickListener Listener to notify.
	 */
	public void setMouseClickListener(Consumer<Coordinates> mouseClickListener) {
		this.mouseClicker = mouseClickListener;
	}

	/**
	 * Notify the map panel that the mouse has been clicked at a position in terms of the Coordinates.
	 * @param mousePos Screen position of the mouse click.
	 */
	void notifyMouseClicked(IntPoint mousePos) {
		if (mouseClicker != null) {
			mouseClicker.accept(convertMouseToCoordinates(mousePos));
		}
	}

	private Coordinates convertMouseToCoordinates(IntPoint mousePos) {
		double xx = mousePos.getX() - getWidth() / 2.0;
		double yy = mousePos.getY() - getHeight() / 2.0;
		// Based on the current centerCoords
		return centerCoords.convertRectToSpherical(xx, yy, marsMap.getRho());
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
	 * Gets the current hotspot in the map.
	 * 
	 * @return
	 */
    public List<MapHotspot> getHotspots() {
        return hotspots;
    }

	/**
	 * Prepares map panel for deletion.
	 */
	public void destroy() {
		mapLayers.forEach(ml -> ml.layer.release());
		mapLayers = null;
		
		if (executor != null) {
			// Stop anything running
			executor.shutdownNow();
		}
		executor = null;
		marsMap = null;
	}

}
