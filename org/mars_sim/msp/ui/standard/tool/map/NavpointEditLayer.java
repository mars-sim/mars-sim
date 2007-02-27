package org.mars_sim.msp.ui.standard.tool.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.mars_sim.msp.simulation.IntPoint;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.ui.standard.ImageLoader;

public class NavpointEditLayer implements MapLayer {

	// Static members
	private static final String BLUE_ICON_NAME = "FlagBlue";
	private static final String GREEN_ICON_NAME = "FlagGreen";
	
	private List navpointPositions;
	private int selectedNavpoint;
	private int radiusLimit;
	private Component displayComponent;
	private Icon navpointIconColor;
	private Icon navpointIconSelected;
	
	public NavpointEditLayer(Component displayComponent) {
		this.displayComponent = displayComponent;
		navpointPositions = new ArrayList();
		navpointIconColor = ImageLoader.getIcon(BLUE_ICON_NAME);
		navpointIconSelected = ImageLoader.getIcon(GREEN_ICON_NAME);
		selectedNavpoint = -1;
		radiusLimit = Integer.MAX_VALUE;
	}
	
	public void addNavpointPosition(IntPoint newNavpointPosition) {
		navpointPositions.add(newNavpointPosition);
	}
	
	public void clearNavpointPositions() {
		navpointPositions.clear();
	}
	
	public IntPoint getNavpointPosition(int index) {
		return (IntPoint) navpointPositions.get(index);
	}
	
	public void setNavpointPosition(int index, IntPoint newNavpointPosition) {
		if ((newNavpointPosition.getiX() > -1) && (newNavpointPosition.getiX() < (300 - navpointIconColor.getIconWidth()))) {
			if ((newNavpointPosition.getiY() >= navpointIconColor.getIconHeight()) && (newNavpointPosition.getiY() < 300)) {
				if (withinRadiusLimit(newNavpointPosition)) navpointPositions.set(index, newNavpointPosition);
			}
		}
	}
	
	private boolean withinRadiusLimit(IntPoint position) {
		int radius = (int) Math.round(Math.sqrt(Math.pow(150D - position.getX(), 2D) + Math.pow(150D - position.getY(), 2D)));
		return (radius <= radiusLimit);
	}
	
	public void setRadiusLimit(int radiusLimit) {
		this.radiusLimit = radiusLimit;
	}
	
	public void selectNavpoint(int index) {
		selectedNavpoint = index;
	}
	
	public void clearSelectedNavpoint() {
		selectedNavpoint = -1;
	}
	
	public int overNavIcon(int x, int y) {
		int result = -1;
		
		for (int z = 0; z < navpointPositions.size(); z++) {
			if (result < 0) {
				IntPoint navpointPosition = (IntPoint) navpointPositions.get(z);
				int leftBound = navpointPosition.getiX();
				int rightBound = navpointPosition.getiX() + navpointIconColor.getIconWidth();
				int topBound = navpointPosition.getiY() - navpointIconColor.getIconHeight();
				int bottomBound = navpointPosition.getiY();
		
				if ((x >= leftBound) && (x <= rightBound) && (y >= topBound) && (y <= bottomBound)) result = z;
			}
		}
		
		return result;
	}
	
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {
		
		// Draw the path lines.
		for (int x = 0; x < navpointPositions.size(); x++) {
			g.setColor(Color.GREEN);
			IntPoint currentPosition = (IntPoint) navpointPositions.get(x);
			if ((x == 0) || (x == (navpointPositions.size() - 1))) 
				g.drawLine(currentPosition.getiX(), currentPosition.getiY(), 150, 150);
			
			if (x != 0) {
				IntPoint prevPosition = (IntPoint) navpointPositions.get(x - 1);
				g.drawLine(currentPosition.getiX(), currentPosition.getiY(), prevPosition.getiX(), prevPosition.getiY());
			}
		}

		// Draw navpoint icons.
		for (int x = 0; x < navpointPositions.size(); x ++) {
			IntPoint currentPosition = (IntPoint) navpointPositions.get(x);
			
			// Determine navpoint icon to use.
			Icon navIcon = null;
			if (selectedNavpoint == x) navIcon = navpointIconSelected;
			else navIcon = navpointIconColor;
			
			// Determine navpoint icon draw position.
			IntPoint drawLocation = new IntPoint(currentPosition.getiX(), (currentPosition.getiY() - navIcon.getIconHeight()));
			
			// Draw the navpoint icon.
	        navIcon.paintIcon(displayComponent, g, drawLocation.getiX(), drawLocation.getiY());
		}
	}
}