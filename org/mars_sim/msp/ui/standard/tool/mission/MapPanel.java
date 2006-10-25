/**
 * Mars Simulation Project
 * MapPanel.java
 * @version 2.80 2006-10-24
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.ui.standard.tool.navigator.Map;
import org.mars_sim.msp.ui.standard.tool.navigator.SurfMarsMap;

public class MapPanel extends JPanel implements Runnable {

	private final int WIDTH = 300;
	private final int HEIGHT = 300;
	
	private Map map;
	private Thread displayThread;
	private Thread createMapThread;
	private Coordinates centerCoords;
	private boolean mapError;
	private String mapErrorMessage;
	private Image mapImage;
	private boolean wait;
	
	MapPanel() {
		super();
		
		map = new SurfMarsMap(this);
		mapError = false;
		wait = false;
		
		setPreferredSize(new Dimension(300, 300));
		setBackground(Color.BLACK);
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
            recreateMap = true;
            centerCoords.setCoords(newCenter);
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
		while (true) {
        	try {
                Thread.sleep(1000);
            } 
	        catch (InterruptedException e) {}
	        repaint();
        }
	}
	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (wait) {
        	if (mapImage != null) g.drawImage(mapImage, 0, 0, this);
        	String message = "Generating Map";
        	drawCenteredMessage(message, g);
        	// if (map.isImageDone() || mapError) wait = false;
        }
        else {
        	if (mapError) {
            	System.err.println("mapError");
                // Display previous map image
                if (mapImage != null) g.drawImage(mapImage, 0, 0, this);
                
                // Draw error message
                drawCenteredMessage(mapErrorMessage, g);
            }
        	else {
        		// Paint black background
                g.setColor(Color.black);
                g.fillRect(0, 0, WIDTH, HEIGHT);
                
                if (map.isImageDone()) {
                    mapImage = map.getMapImage();
                    g.drawImage(mapImage, 0, 0, this);
                }    
        	}
        }
    }
	
    /**
     * Draws a message string in the center of the map display.
     *
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
        int x = (WIDTH - msgWidth) / 2;
        int y = (HEIGHT + msgHeight) / 2;
    
        // Draw message
        g.drawString(message, x, y);
    }
}