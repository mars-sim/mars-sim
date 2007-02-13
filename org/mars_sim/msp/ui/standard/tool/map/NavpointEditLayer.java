package org.mars_sim.msp.ui.standard.tool.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import org.mars_sim.msp.simulation.IntPoint;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.ui.standard.ImageLoader;

public class NavpointEditLayer implements MapLayer {

	// Static members
	private static final String BLUE_ICON_NAME = "FlagBlue";
	private static final String GREEN_ICON_NAME = "FlagGreen";
	
	private IntPoint displayPosition;
	private int radiusLimit;
	private Component displayComponent;
	private Icon navpointIconColor;
	private Icon navpointIconSelected;
	private boolean isSelected;
	
	public NavpointEditLayer(Component displayComponent) {
		this.displayComponent = displayComponent;
		displayPosition = new IntPoint(-1, -1);
		navpointIconColor = ImageLoader.getIcon(BLUE_ICON_NAME);
		navpointIconSelected = ImageLoader.getIcon(GREEN_ICON_NAME);
		selectIcon(false);
	}
	
	public IntPoint getDisplayPosition() {
		return displayPosition;
	}
	
	public void setDisplayPosition(IntPoint newDisplayPosition) {
		if ((newDisplayPosition.getiX() > -1) && (newDisplayPosition.getiX() < (300 - navpointIconColor.getIconWidth()))) {
			if ((newDisplayPosition.getiY() >= navpointIconColor.getIconHeight()) && (newDisplayPosition.getiY() < 300)) {
				if (withinRadiusLimit(newDisplayPosition)) displayPosition = newDisplayPosition;
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
	
	public void selectIcon(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	public boolean isOverNavIcon(int x, int y) {
		int leftBound = displayPosition.getiX();
		int rightBound = displayPosition.getiX() + navpointIconColor.getIconWidth();
		int topBound = displayPosition.getiY() - navpointIconColor.getIconHeight();
		int bottomBound = displayPosition.getiY();
		
		if ((x >= leftBound) && (x <= rightBound) && (y >= topBound) && (y <= bottomBound)) return true;
		else return false;
	}
	
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {

		// Draw the path line.
		g.setColor(Color.GREEN);
		g.drawLine(displayPosition.getiX(), displayPosition.getiY(), 150, 150);
		
		// Determine navpoint icon to use.
		Icon navIcon = null;
		if (isSelected) navIcon = navpointIconSelected;
		else navIcon = navpointIconColor;
		
		// Determine navpoint icon draw position.
		IntPoint drawLocation = new IntPoint(displayPosition.getiX(), (displayPosition.getiY() - navIcon.getIconHeight()));
		
		// Draw the navpoint icon.
        navIcon.paintIcon(displayComponent, g, drawLocation.getiX(), drawLocation.getiY());
	}
}