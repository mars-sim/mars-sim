/**
 * Mars Simulation Project
 * MapPanel.java
 * @version 3.07 2014-12-06

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;

public class MapPanel
extends JPanel
implements Runnable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(MapPanel.class.getName());
	private static final double HALF_PI = Math.PI / 2d;
	private static int dragx, dragy;

	// Data members.
	private boolean recreate;
	private boolean mapError;
	private boolean wait;
	private boolean update;
	private boolean topo;

	private String mapErrorMessage;
	private String mapType;
	private String oldMapType;

	private List<MapLayer> mapLayers;
	private Map map;

	private Thread displayThread;
	private Thread createMapThread;
	private Coordinates centerCoords;

	private Image mapImage;
	private SurfMarsMap surfMap;
	private TopoMarsMap topoMap;

	private NavigatorWindow navwin;
	private Graphics dbg;
	private Image dbImage = null;

	public MapPanel() {
		super();

		mapType = SurfMarsMap.TYPE;
		oldMapType = mapType;
		topoMap = new TopoMarsMap(this);
		surfMap = new SurfMarsMap(this);
		map = surfMap;
		mapError = false;
		wait = false;
		mapLayers = new ArrayList<MapLayer>();
		update = true;
		centerCoords = new Coordinates(HALF_PI, 0D);
		recreate = true;

		setPreferredSize(new Dimension(300, 300));
		setBackground(Color.BLACK);
	}

	/*
	 * Sets up the mouse dragging capability
	 */
	// 2015-06-26 Added setNavWin()
	public void setNavWin(final NavigatorWindow navwin) {
		this.navwin = navwin;

		// 2015-06-26 Note: need navWin prior to calling addMouseMotionListener()
		addMouseMotionListener(new MouseAdapter() {
			int lastx, lasty;

			@Override
			public void mouseDragged(MouseEvent e) {
				navwin.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				int difx, dify, x = e.getX(), y = e.getY();

				if (y > dragy) {
					if (y < lasty) {
						dragy = y;
					}
				} else {
					if (y > lasty) {
						dragy = y;
					}
				}

				if (x > dragx) {
					if (x < lastx) {
						dragx = x;
					}
				} else {
					if (x > lastx) {
						dragx = x;
					}
				}
				dify = dragy - y;
				difx = dragx - x;
				lastx = x;
				lasty = y;
				/*
				 * System.out.println("GlobeDisplay mouseDragged difx = "+difx);
				 * System.out.println("GlobeDisplay mouseDragged dify = "+dify);
				 * System.out.println("GlobeDisplay mouseDragged dragx = "+dragx);
				 * System.out.println("GlobeDisplay mouseDragged dragy = "+dragy);
				 * System.out.println("GlobeDisplay mouseDragged X = "+e.getX());
				 * System.out.println("GlobeDisplay mouseDragged Y = "+e.getY());
				 */
				if (dragx != 0 && dragy != 0) {
					double newPhi = centerCoords.getPhi() + ((double) dify * .0025D % (Math.PI));
					if (newPhi > Math.PI) {
						newPhi = Math.PI;
					}
					if (newPhi < 0D) {
						newPhi = 0D;
					}

					double newTheta = centerCoords.getTheta() + ((double) difx * .0025D % (Math.PI / 2D));
					while (newTheta > (Math.PI * 2D)) {
						newTheta -= (Math.PI * 2D);
					}
					while (newTheta < 0D) {
						newTheta += (Math.PI * 2D);
					}

					centerCoords = new Coordinates(newPhi, newTheta);
/*
					topo = navwin.getMarsGlobe().isTopo();

					if (topo) {
						topoMap.drawSphere(centerCoords);
					} else {
						surfMap.drawSphere(centerCoords);
					}
*/
					setMapType(getMapType());
					map.drawMap(centerCoords);

					recreate = false;

					paintDoubleBuffer();
					repaint();

				}

				super.mouseDragged(e);
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				//System.out.println("mousepressed X = " + e.getX());
				//System.out.println("             Y = " + e.getY());
				dragx = e.getX();
				dragy = e.getY();
				navwin.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				dragx = 0;
				dragy = 0;
				navwin.updateCoords(centerCoords);
				super.mouseReleased(e);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				//navwin.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				super.mouseReleased(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				navwin.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				super.mouseReleased(e);
			}
		});

		//showMap(centerCoords);
		setMapType(getMapType());
		map.drawMap(centerCoords);
	}

	/**
	 * Adds a new map layer
	 * @param newLayer the new map layer.
	 * @param index the index order of the map layer.
	 */
	public void addMapLayer(MapLayer newLayer, int index) {
		if (newLayer != null) {
			if (!mapLayers.contains(newLayer)) {
			    if (index < mapLayers.size()) {
			        mapLayers.add(index, newLayer);
			    }
			    else {
			        mapLayers.add(newLayer);
			    }
			}
		}
		else throw new IllegalArgumentException("newLayer is null");
	}

	/**
	 * Removes a map layer.
	 * @param oldLayer the old map layer.
	 */
	public void removeMapLayer(MapLayer oldLayer) {
		if (oldLayer != null) {
			if (mapLayers.contains(oldLayer)) {
			    mapLayers.remove(oldLayer);
			}
		}
		else throw new IllegalArgumentException("oldLayer is null");
	}

	/**
	 * Checks if map has a map layer.
	 * @param layer the map layer.
	 * @return true if map has the map layer.
	 */
	public boolean hasMapLayer(MapLayer layer) {
		return mapLayers.contains(layer);
	}

	/**
	 * Gets the map type.
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
		if (SurfMarsMap.TYPE.equals(mapType)) map = surfMap;
		else if (TopoMarsMap.TYPE.equals(mapType)) map = topoMap;
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
		}
		else if (!centerCoords.equals(newCenter)) {
            if (newCenter != null) {
            	recreateMap = true;
            	centerCoords.setCoords(newCenter);
            }
            else centerCoords = null;
        }

		if (!mapType.equals(oldMapType)) {
			recreateMap = true;
			oldMapType = mapType;
		}

		if (recreateMap) {
			wait = true;
			if ((createMapThread != null) && (createMapThread.isAlive()))
				createMapThread.interrupt();
			createMapThread = new Thread(new Runnable() {
				public void run() {
	    			try {
	    				mapError = false;
	    				map.drawMap(centerCoords);
	    			}
	    			catch (Exception e) {
	    				e.printStackTrace(System.err);
	    				mapError = true;
	    				mapErrorMessage = e.getMessage();
	    			}
	    			wait = false;

	    			paintDoubleBuffer();
	    			repaint();
	    		}
			});
			createMapThread.start();
		}

        updateDisplay();
    }

	/**
	 * Updates the current display
	 */
    private void updateDisplay() {
        if ((displayThread == null) || (!displayThread.isAlive())) {
        	displayThread = new Thread(this, "Navpoint Map");
        	displayThread.start();
        } else {
        	displayThread.interrupt();
        }
    }

	public void run() {
		while (update) {
        	try {
                Thread.sleep(1000);
            }
	        catch (InterruptedException e) {}

			paintDoubleBuffer();
	        repaint();
        }
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(dbImage != null)
			g.drawImage(dbImage,  0, 0, null);
	}

	/*
	 * Uses double buffering to draws into its own graphics object dbg before calling paintComponent()
	 */
	public void paintDoubleBuffer() {
		if (dbImage == null) {
			dbImage = createImage(300,300);
			if (dbImage == null) {
				//System.out.println("dbImage is null");
				return;
			}
			else
				dbg = dbImage.getGraphics();
		}

        Graphics2D g2d = (Graphics2D) dbg;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (wait) {
        	if (mapImage != null) dbg.drawImage(mapImage, 0, 0, this);
        	String message = "Generating Map";
        	drawCenteredMessage(message, dbg);
        }
        else {
        	if (mapError) {
            	logger.log(Level.SEVERE,"mapError: " + mapErrorMessage);
                // Display previous map image
                if (mapImage != null) dbg.drawImage(mapImage, 0, 0, this);

                // Draw error message
                if (mapErrorMessage == null) mapErrorMessage = "Null Map";
                drawCenteredMessage(mapErrorMessage, dbg);
            }
        	else {
        		// Paint black background
                dbg.setColor(Color.black);
                dbg.fillRect(0, 0, Map.DISPLAY_WIDTH, Map.DISPLAY_HEIGHT);

                if (centerCoords != null) {
                	if (map.isImageDone()) {
                		mapImage = map.getMapImage();
                		dbg.drawImage(mapImage, 0, 0, this);
                	}

                	// Display map layers.
                	Iterator<MapLayer> i = mapLayers.iterator();
                	while (i.hasNext()) i.next().displayLayer(centerCoords, mapType, dbg);
                }
        	}
        }
    }

/*
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
*/

    /**
     * Draws a message string in the center of the map panel.
     * @param message the message string
     * @param g the graphics context
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

    /**
     * Prepares map panel for deletion.
     */
    public void destroy() {
    	map = null;
    	surfMap = null;
    	topoMap = null;
    	update = false;
		dbg = null;
		dbImage = null;
    }
}