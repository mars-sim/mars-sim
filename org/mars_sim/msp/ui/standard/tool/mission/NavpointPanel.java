/**
 * Mars Simulation Project
 * NavpointPanel.java
 * @version 2.80 2006-10-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionEvent;
import org.mars_sim.msp.simulation.person.ai.mission.MissionListener;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.tool.navigator.Map;
import org.mars_sim.msp.ui.standard.tool.navigator.SurfMarsMap;

public class NavpointPanel extends JPanel implements ListSelectionListener,
		MissionListener {

	private Mission currentMission;
	
	NavpointPanel() {
		
		setLayout(new BorderLayout());
		
		Box mainPane = Box.createVerticalBox();
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);
		
		JPanel mapDisplayPane = new JPanel(new BorderLayout(0, 0));
		mainPane.add(mapDisplayPane);
		
		MapPanel mapPane = new MapPanel();
		mapDisplayPane.add(mapPane, BorderLayout.CENTER);
		
		JButton northButton = new JButton("^");
		mapDisplayPane.add(northButton, BorderLayout.NORTH);
		
		JButton westButton = new JButton("<");
		mapDisplayPane.add(westButton, BorderLayout.WEST);
		
		JButton eastButton = new JButton(">");
		mapDisplayPane.add(eastButton, BorderLayout.EAST);
		
		JButton southButton = new JButton("v");
		mapDisplayPane.add(southButton, BorderLayout.SOUTH);
	}
	
	public void valueChanged(ListSelectionEvent e) {
		if (currentMission != null) currentMission.removeListener(this);
		Mission mission = (Mission) ((JList) e.getSource()).getSelectedValue();
		if (mission != null) {
			mission.addListener(this);
			currentMission = mission;
		}
		else currentMission = null;
	}

	public void missionUpdate(MissionEvent event) {
	}
	
	private class MapPanel extends JPanel implements Runnable {
		
		private Map map;
		private Thread displayThread;
		private Coordinates centerCoords;
		private boolean recreateMap;
		private boolean mapError;
		private String mapErrorMessage;
		private Image mapImage;
		private boolean wait;
		private int width = 300;
		private int height = 300;
		
		MapPanel() {
			super();
			
			map = new SurfMarsMap(this);
			centerCoords = new Coordinates(0D, 0D);
			recreateMap = true;
			mapError = false;
			wait = true;
			
			setPreferredSize(new Dimension(300, 300));
			
			showMap(centerCoords);
		}
		
		public void showMap(Coordinates newCenter) {
			if ((centerCoords == null) && (newCenter != null)) {
				wait = true;
				recreateMap = true;
				centerCoords = newCenter;
			}
			else if (!centerCoords.equals(newCenter)) {
	            wait = true;
	            recreateMap = true;
	            centerCoords.setCoords(newCenter);
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
	    
	    /** 
	     * The run method for the runnable interface 
	     */
	    public void run() {
	        while (true) {
	        	// Display map
	        	// System.out.println("Display map");

	        	if (recreateMap) {
	        		mapError = false;
	        		try {
	        			map.drawMap(centerCoords);
	        		}
	        		catch (Exception e) {
	        			mapError = true;
	        			mapErrorMessage = e.getMessage();
	        		}
	        		recreateMap = false;
	        		repaint();
	        	}
	        	else {
	        		try {
	                    Thread.sleep(1000);
	                } 
		        	catch (InterruptedException e) {}
		        	repaint();
	        	}
	        }
	    }
	    
	    public void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        
	        if (wait) {
	        	if (mapImage != null) g.drawImage(mapImage, 0, 0, this);
	        	String message = "Generating Map";
	        	drawCenteredMessage(message, g);
	        	if (map.isImageDone() || mapError) wait = false;
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
	                g.fillRect(0, 0, width, height);
	                
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
	        int x = (width - msgWidth) / 2;
	        int y = (height + msgHeight) / 2;
	    
	        // Draw message
	        g.drawString(message, x, y);
	    }
	}
}