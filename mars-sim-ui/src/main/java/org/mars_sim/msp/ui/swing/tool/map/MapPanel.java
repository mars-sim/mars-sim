/**
 * Mars Simulation Project
 * MapPanel.java
 * @version 3.1.0 2017-10-05
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

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.mission.NavpointPanel;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;

import com.alee.laf.panel.WebPanel;

public class MapPanel extends WebPanel implements ClockListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(MapPanel.class.getName());
	private static final double HALF_PI = Math.PI / 2d;

	public final static int MAP_BOX_HEIGHT = NavigatorWindow.HORIZONTAL_SURFACE_MAP;
	public final static int MAP_BOX_WIDTH = NavigatorWindow.HORIZONTAL_SURFACE_MAP;
	private static int dragx, dragy;

//	private static final double PERIOD_IN_MILLISOLS = 10D * 500D / MarsClock.SECONDS_PER_MILLISOL; //3;

	// Data members
//	private double timeCache = 0;
	private boolean mapError;
	private boolean wait;
	private boolean update;

	private String mapErrorMessage;
	private String mapType;
	private String oldMapType;

	private List<MapLayer> mapLayers;
	private Map map;

	// private Thread displayThread;
	// private Thread createMapThread;

	private static MasterClock masterClock = Simulation.instance().getMasterClock();

	private Coordinates centerCoords;

	private Image mapImage;
	private SurfMarsMap surfMap;
	private TopoMarsMap topoMap;
	private GeologyMarsMap geoMap;

	private MainDesktopPane desktop;

//	private MainScene mainScene;

	private Graphics dbg;
	private Image dbImage = null;
	// private long refreshRate;
	private double rho = CannedMarsMap.PIXEL_RHO;

	// private ThreadPoolExecutor executor;
	private transient ExecutorService executor;

	public MapPanel(MainDesktopPane desktop, long refreshRate) {
		super();
		this.desktop = desktop;
//		this.mainScene = desktop.getMainScene();

		// executor = ? (ThreadPoolExecutor) Executors.newCachedThreadPool(); //
		// newFixedThreadPool(1); //
		executor = Executors.newSingleThreadExecutor();

		masterClock.addClockListener(this);

		// this.refreshRate = refreshRate;

		mapType = SurfMarsMap.TYPE;
		oldMapType = mapType;

		topoMap = new TopoMarsMap(this);
		surfMap = new SurfMarsMap(this);
		geoMap = new GeologyMarsMap(this);
		
		map = surfMap;
		mapError = false;
		wait = false;
		mapLayers = new CopyOnWriteArrayList<MapLayer>();
		update = true;
		centerCoords = new Coordinates(HALF_PI, 0D);

		setPreferredSize(new Dimension(MAP_BOX_WIDTH, MAP_BOX_HEIGHT));
		setBackground(Color.BLACK);
		setOpaque(true);
	}

	/*
	 * Sets up the mouse dragging capability
	 */
	public void setNavWin(NavigatorWindow navwin) {
		// showMap(centerCoords);
		setMapType(getMapType());
		map.drawMap(centerCoords);

		// Note: need navWin prior to calling addMouseMotionListener()
		addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				// setCursor(new Cursor(Cursor.MOVE_CURSOR));
				int dx, dy, x = e.getX(), y = e.getY();

				dx = dragx - x;
				dy = dragy - y;

				if (dx != 0 || dy != 0) {
					if (x > 0 && x < MAP_BOX_HEIGHT && y > 0 && y < MAP_BOX_HEIGHT) {
						// double rho = CannedMarsMap.PIXEL_RHO;
						centerCoords = centerCoords.convertRectToSpherical((double) dx, (double) dy, rho);

						// if (!executor.isTerminated() || !executor.isShutdown() )
						// executor.execute(new MapTask());

						map.drawMap(centerCoords);

//						paintDoubleBuffer();
						repaint();
					}
				}

				dragx = x;
				dragy = y;

				// setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// System.out.println("mousepressed X = " + e.getX());
				// System.out.println(" Y = " + e.getY());
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
		map.drawMap(centerCoords);

		// Note: need navWin prior to calling addMouseMotionListener()
		addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				// setCursor(new Cursor(Cursor.MOVE_CURSOR));
				int dx, dy, x = e.getX(), y = e.getY();

				dx = dragx - x;
				dy = dragy - y;

				if (dx != 0 || dy != 0) {
					if (x > 0 && x < MAP_BOX_HEIGHT && y > 0 && y < MAP_BOX_HEIGHT) {
						// double rho = CannedMarsMap.PIXEL_RHO;
						centerCoords = centerCoords.convertRectToSpherical((double) dx, (double) dy, rho);

						// if (!executor.isTerminated() || !executor.isShutdown() )
						// executor.execute(new MapTask());

						map.drawMap(centerCoords);

//						paintDoubleBuffer();
						repaint();
					}
				}

				dragx = x;
				dragy = y;

				// setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// System.out.println("mousepressed X = " + e.getX());
				// System.out.println(" Y = " + e.getY());
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
		return mapType;
	}

	/**
	 * Sets the map type.
	 */
	public void setMapType(String mapType) {
		this.mapType = mapType;
		if (SurfMarsMap.TYPE.equals(mapType))
			map = surfMap;
		else if (TopoMarsMap.TYPE.equals(mapType))
			map = topoMap;
		else if (GeologyMarsMap.TYPE.equals(mapType))
			map = geoMap;
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
				centerCoords.setCoords(newCenter);
			} else
				centerCoords = null;
		}

		if (!mapType.equals(oldMapType)) {
			recreateMap = true;
			oldMapType = mapType;
		}

		if (recreateMap) {
			wait = true;
			if (!executor.isTerminated() || !executor.isShutdown())
				executor.execute(new MapTask());

//			//			if ((createMapThread != null) && (createMapThread.isAlive()))
////				createMapThread.interrupt();
////				createMapThread = new Thread(new Runnable() {
//				public void run() {
//	    			try {
//	    				mapError = false;
//	    				map.drawMap(centerCoords);
//	    			}
//	    			catch (Exception e) {
//	    				e.printStackTrace(System.err);
//	    				mapError = true;
//	    				mapErrorMessage = e.getMessage();
//	    			}
//	    			wait = false;
//
//	    			paintDoubleBuffer();
//	    			repaint();
//	    		}
////			});
////			createMapThread.start();
//
		}

		updateDisplay();
	}

	class MapTask implements Runnable {

		private MapTask() {
		}

		@Override
		public void run() {
			try {
				mapError = false;
				map.drawMap(centerCoords);
			} catch (Exception e) {
				e.printStackTrace(System.err);
				mapError = true;
				mapErrorMessage = e.getMessage();
			}
			wait = false;

//			paintDoubleBuffer();
			repaint();
		}
	}

//	/**
//	 * Updates the current display
//
//    private void updateDisplay() {
//        if ((displayThread == null) || (!displayThread.isAlive())) {
//        	displayThread = new Thread(this, "Navpoint Map");
//        	displayThread.start();
//        } else {
//        	displayThread.interrupt();
//        }
//    }
//	 

	public void updateDisplay() {
		if (update) {
			if (!executor.isTerminated() || !executor.isShutdown())
				executor.execute(new MapTask());
		}
	}

//	public void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		if (dbImage != null) {
//			g.drawImage(dbImage, 0, 0, null);
//		}
//	}

//	/*
//	 * Uses double buffering to draws into its own graphics object dbg before
//	 * calling paintComponent()
//	 */
//	public void paintDoubleBuffer() {
//		if (dbImage == null) {
//			dbImage = createImage(MAP_BOX_WIDTH, MAP_BOX_HEIGHT);
//			if (dbImage == null) {
//				// System.out.println("dbImage is null");
//				return;
//			} else
//				dbg = dbImage.getGraphics();
//		}
//
////        Graphics2D g2d = (Graphics2D) dbg;
////        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//		if (wait) {
//			if (mapImage != null) {
//				dbg.drawImage(mapImage, 0, 0, this);
//			}
//			String message = "Generating Map";
//			drawCenteredMessage(message, dbg);
//		} else {
//			if (mapError) {
//				logger.log(Level.SEVERE, "mapError: " + mapErrorMessage);
//				// Display previous map image
//				if (mapImage != null) {
//					dbg.drawImage(mapImage, 0, 0, this);
//				}
//
//				// Draw error message
//				if (mapErrorMessage == null) {
//					mapErrorMessage = "Null Map";
//				}
//				drawCenteredMessage(mapErrorMessage, dbg);
//			} else {
//				// Paint black background
//				dbg.setColor(Color.black);
//				dbg.fillRect(0, 0, Map.DISPLAY_WIDTH, Map.DISPLAY_HEIGHT);
//
//				if (centerCoords != null) {
//					if (map != null) {
//						if (map.isImageDone()) {
//							mapImage = map.getMapImage();
//							dbg.drawImage(mapImage, 0, 0, this);
//						}
//					}
//
//					// Display map layers.
//					// List<MapLayer> tempMapLayers = new ArrayList<MapLayer>(mapLayers);
//					// Iterator<MapLayer> i = tempMapLayers.iterator();
//					// while (i.hasNext()) {
//					// i.next().displayLayer(centerCoords, mapType, dbg);
//					// }
//
//					for (MapLayer l : mapLayers) {
//						if (dbg != null)
//							l.displayLayer(centerCoords, mapType, dbg);
//					}
//				}
//			}
//		}
//	}

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
                	if (map.isImageDone()) {
                		mapImage = map.getMapImage();
                		g.drawImage(mapImage, 0, 0, this);
                	}

                	// Display map layers.
                	Iterator<MapLayer> i = mapLayers.iterator();
                	while (i.hasNext()) i.next().displayLayer(centerCoords, mapType, g);
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

	@Override
	public void clockPulse(double time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void uiPulse(double time) {
//		if (mainScene != null) {
//			if (!mainScene.isMinimized() && mainScene.isMainTabOpen()
//					&& (desktop.isToolWindowOpen(NavigatorWindow.NAME) || (desktop.isToolWindowOpen(MissionWindow.NAME)
//							&& ((MissionWindow) desktop.getToolWindow(MissionWindow.NAME)).isNavPointsMapTabOpen()))) {
//				// TODO: should also check if navpoints tab is open or not
////				timeCache += time;
////				if (timeCache > PERIOD_IN_MILLISOLS * time) {
//				// Repaint map panel
//				updateDisplay();
////					timeCache = 0;
////				}	
//			}
//		} else 
			if (desktop.isToolWindowOpen(NavigatorWindow.NAME) || desktop.isToolWindowOpen(MissionWindow.NAME)
		// ||desktop.isToolWindowOpen(ResupplyWindow.NAME)
		) {
//			timeCache += time;
//			if (timeCache > PERIOD_IN_MILLISOLS * time) {
			updateDisplay();
//				timeCache = 0;
//			}
		}
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// TODO Auto-generated method stub
	}

	/**
	 * Prepares map panel for deletion.
	 */
	public void destroy() {
		// Remove clock listener.
		masterClock.removeClockListener(this);
		mapLayers = null;
		centerCoords = null;
		executor = null;
		map = null;
		surfMap = null;
		topoMap = null;
		geoMap = null;
		update = false;
		dbg = null;
		dbImage = null;
		mapImage = null;
	}
}