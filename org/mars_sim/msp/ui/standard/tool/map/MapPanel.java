/**
 * Mars Simulation Project
 * MapPanel.java
 * @version 2.81 2007-08-27
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.mars_sim.msp.simulation.Coordinates;

public class MapPanel extends JPanel implements Runnable {
    
	private static String CLASS_NAME = 
	    "org.mars_sim.msp.ui.standard.tool.map.MapPanel";
	
    	private static Logger logger = Logger.getLogger(CLASS_NAME);

	// Data members.
	private Map map;
	private Thread displayThread;
	private Thread createMapThread;
	private Coordinates centerCoords;
	private boolean mapError;
	private String mapErrorMessage;
	private Image mapImage;
	private boolean wait;
	private String mapType;
	private List<MapLayer> mapLayers;
	private String oldMapType;
	private SurfMarsMap surfMap;
	private TopoMarsMap topoMap;
	private USGSMarsMap usgsMap;
	private boolean update;
	
	public MapPanel() {
		super();
		
		mapType = SurfMarsMap.TYPE;
		oldMapType = mapType;
		topoMap = new TopoMarsMap(this);
		usgsMap = new USGSMarsMap(this);
		surfMap = new SurfMarsMap(this);
		map = surfMap;
		mapError = false;
		wait = false;
		mapLayers = new ArrayList<MapLayer>();
		update = true;
		
		setPreferredSize(new Dimension(300, 300));
		setBackground(Color.BLACK);
	}
	
	/**
	 * Adds a new map layer
	 * @param newLayer the new map layer.
	 */
	public void addMapLayer(MapLayer newLayer) {
		if (newLayer != null) {
			if (!mapLayers.contains(newLayer)) mapLayers.add(newLayer);
		}
		else throw new IllegalArgumentException("newLayer is null");
	}
	
	/**
	 * Removes a map layer.
	 * @param oldLayer the old map layer.
	 */
	public void removeMapLayer(MapLayer oldLayer) {
		if (oldLayer != null) {
			if (mapLayers.contains(oldLayer)) mapLayers.remove(oldLayer);
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
		else if (USGSMarsMap.TYPE.equals(mapType)) map = usgsMap;
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
	        repaint();
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
    	usgsMap = null;
    	update = false;
    }
}