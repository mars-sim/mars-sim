/*
 * Mars Simulation Project
 * MapPanel.java
 * @date 2023-05-02
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.mission.NavpointPanel;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;

public class MapPanel extends JPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(MapPanel.class.getName());
	
	private static final double HALF_PI = Math.PI / 2d;

	public static final int MAP_BOX_HEIGHT = 300;
	public static final int MAP_BOX_WIDTH = 300;
	private static int dragx, dragy;

	private transient ExecutorService executor;

	// Data members
	private boolean mapError;
	private boolean wait;
	private boolean update;

	/** The Map type. 0: surface, 1: topo, 2: geology, 3: regional, 4: viking */
	private int mapType = -1;
	
	private String mapErrorMessage;
	private String oldMapStringType;

	private List<MapLayer> mapLayers;

	private Coordinates centerCoords;

	private Image mapImage;

	private Map marsMap;
	
	private MainDesktopPane desktop;

	public MapPanel(MainDesktopPane desktop, long refreshRate) {
		super();
		this.desktop = desktop;

		executor = Executors.newSingleThreadExecutor();
		
		// Initializes map instance as surf map
		setMapType(SurfMarsMap.TYPE);
		oldMapStringType = marsMap.getType();
		
		mapError = false;
		wait = false;
		mapLayers = new CopyOnWriteArrayList<>();
		update = true;
		centerCoords = new Coordinates(HALF_PI, 0D);

		setPreferredSize(new Dimension(MAP_BOX_WIDTH, MAP_BOX_HEIGHT));
		setMaximumSize(getPreferredSize());
		setBackground(Color.BLACK);
		setOpaque(true);
	}

	/*
	 * Sets up the mouse dragging capability
	 */
	public void setNavWin(NavigatorWindow navwin) {
		// showMap(centerCoords);
		setMapType(getMapType());
		
//		if (map != null) map.drawMap(centerCoords);

		// Note: need navWin prior to calling addMouseMotionListener()
		addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				int dx, dy, x = e.getX(), y = e.getY();

				dx = dragx - x;
				dy = dragy - y;

				if ((dx != 0 || dy != 0) 
					 && x > 0 && x < MAP_BOX_WIDTH && y > 0 && y < MAP_BOX_HEIGHT) {
					centerCoords = centerCoords.convertRectToSpherical(dx, dy, marsMap.getScale());
					marsMap.drawMap(centerCoords);
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
				setCursor(new Cursor(Cursor.MOVE_CURSOR));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				dragx = 0;
				dragy = 0;
				navwin.updateCoords(centerCoords);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
	}

	/*
	 * Sets up the mouse dragging capability
	 */
	public void setNavpointPanel(NavpointPanel panel) {
		// showMap(centerCoords);
		setMapType(getMapType());
		
//		if (map != null) map.drawMap(centerCoords);

		// Note: need navWin prior to calling addMouseMotionListener()
		addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				// setCursor(new Cursor(Cursor.MOVE_CURSOR));
				int dx, dy, x = e.getX(), y = e.getY();

				dx = dragx - x;
				dy = dragy - y;

				if ((dx != 0 || dy != 0)
					 && x > 0 && x < MAP_BOX_WIDTH && y > 0 && y < MAP_BOX_HEIGHT) {

					centerCoords = centerCoords.convertRectToSpherical(dx, dy, marsMap.getScale());
					marsMap.drawMap(centerCoords);
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
				setCursor(new Cursor(Cursor.MOVE_CURSOR));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				dragx = 0;
				dragy = 0;
				panel.updateCoords(centerCoords);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});

	}
	
	/**
	 * Adds a new map layer
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
	 * Gets the map type.
	 * 
	 * @return map type.
	 */
	public String getMapType() {
		return marsMap.getType();
	}

	public Map getMap() {
		return marsMap;
	}

	/**
	 * Sets the map type.
	 */
	public void setMapType(String mapStringType) {
		
		if (SurfMarsMap.TYPE.equals(mapStringType)) {
			if (mapType != 0) {
				marsMap = new SurfMarsMap(this);
				mapType = 0;
			}
		}
		else if (TopoMarsMap.TYPE.equals(mapStringType)) {
			if (mapType != 1) {
				marsMap = new TopoMarsMap(this);
				mapType = 1;
			}
		}
		else if (GeologyMarsMap.TYPE.equals(mapStringType)) {
			if (mapType != 2) {
				marsMap = new GeologyMarsMap(this);
				mapType = 2;
			}
		}
		else if (RegionMarsMap.TYPE.equals(mapStringType)) {
			if (mapType != 3) {
				marsMap = new RegionMarsMap(this);
				mapType = 3;
			}
		}
		else if (VikingMarsMap.TYPE.equals(mapStringType)) {
			if (mapType != 4) {
				marsMap = new VikingMarsMap(this);
				mapType = 4;
			}
		}
			
		showMap(centerCoords);
	}

	public Coordinates getCenterLocation() {
		return centerCoords;
	}

	public void showMap(Coordinates newCenter) {
		boolean recreateMap = false;
		if (centerCoords == null) {
			if (newCenter != null) {
				recreateMap = true;
				centerCoords = new Coordinates(newCenter);
			}
		} else if (!centerCoords.equals(newCenter)) {
			if (newCenter != null) {
				recreateMap = true;
				centerCoords = newCenter;
			} 
		}

		String mapStringType = marsMap.getType();
		if (!mapStringType.equals(oldMapStringType)) {
			recreateMap = true;
			oldMapStringType = mapStringType;
		}

		if (recreateMap) {
			wait = true;
			updateDisplay();
		}
	}

	class MapTask implements Runnable {

		private MapTask() {
		}

		@Override
		public void run() {
			try {
				mapError = false;

				if (centerCoords == null) {
					logger.severe("centerCoords is null.");
					centerCoords = new Coordinates("0.0", "0.0");
				}
				
				marsMap.drawMap(centerCoords);
				wait = false;
				repaint();
				
			} catch (Exception e) {
				mapError = true;
				mapErrorMessage = e.getMessage();
				logger.severe("Can't draw surface map: " + e);
			}
		}
	}

	public void updateDisplay() {
		if ((desktop.isToolWindowOpen(NavigatorWindow.NAME) 
			|| desktop.isToolWindowOpen(MissionWindow.NAME))
			&& update 
			&& (!executor.isTerminated() || !executor.isShutdown())) {
				executor.execute(new MapTask());
		}
	}

	public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (wait) {
        	if (mapImage != null) g.drawImage(mapImage, 0, 0, this);
        	String message = "Generating Map";
        	drawCenteredMessage(message, g);
        }
        else {
        	if (mapError) {
            	logger.log(Level.SEVERE,"mapError: " + mapErrorMessage);
                // Display previous map image
                if (mapImage != null) g.drawImage(mapImage, 0, 0, this);

                // Draw error message
                if (mapErrorMessage == null) mapErrorMessage = "Null Map";
                drawCenteredMessage(mapErrorMessage, g);
            }
        	else {
        		// Paint black background
                g.setColor(Color.black);
                g.fillRect(0, 0, Map.DISPLAY_WIDTH, Map.DISPLAY_HEIGHT);

                if (centerCoords != null) {
                	if (marsMap != null && marsMap.isImageDone()) {
                		mapImage = marsMap.getMapImage();
                		g.drawImage(mapImage, 0, 0, this);
                	}

                	// Display map layers.
                	Iterator<MapLayer> i = mapLayers.iterator();
                	while (i.hasNext()) i.next().displayLayer(centerCoords, marsMap, g);
                }
        	}
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
		int x = (Map.DISPLAY_WIDTH - msgWidth) / 2;
		int y = (Map.DISPLAY_HEIGHT + msgHeight) / 2;

		// Draw message
		g.drawString(message, x, y);
	}

	public void update(ClockPulse pulse) {
		updateDisplay();
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
		update = false;
		mapImage = null;
	}

    public Coordinates getMouseCoordinates(int x, int y) {
		double xMap = x - (Map.DISPLAY_WIDTH / 2D) - 1;
		double yMap = y - (Map.DISPLAY_HEIGHT / 2D) - 1;
		
		return centerCoords.convertRectToSpherical(xMap, yMap, marsMap.getScale());
    }
}
