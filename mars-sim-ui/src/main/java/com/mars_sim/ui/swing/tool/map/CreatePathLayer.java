/*
 * Mars Simulation Project
 * CreatePathLayer.java
 * @date 2026-02-02
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.ui.swing.ImageLoader;

/**
 * A map layer to allow the editing of navpoints.
 */
public class CreatePathLayer implements MapLayer {

	// Static members
	private static final String BLUE_ICON_NAME = "map/flag_blue";
	private static final String GREEN_ICON_NAME = "map/flag_green";
	private static final Font FONT = new Font("SansSerif", Font.PLAIN, 9);
	
	// Domain members.
	private Coordinates startPoint;
	private List<Coordinates> navPositions;
	private List<IntPoint> mapPositions;
	private int selectedNavpoint;
	private Component displayComponent;
	private Icon navpointIconColor;
	private Icon navpointIconSelected;
	private boolean drawNavNumbers;

	/**
	 * Constructor
	 * 
	 * @param displayComponent the component displaying the map.
	 * @param drawNavNumbers   display navpoint flag numbers?
	 */
	public CreatePathLayer(Component displayComponent, boolean drawNavNumbers, Coordinates startPoint) {
		this.displayComponent = displayComponent;
		this.startPoint = startPoint;
		navPositions = new ArrayList<>();
		mapPositions = new ArrayList<>();
		navpointIconColor = ImageLoader.getIconByName(BLUE_ICON_NAME);
		navpointIconSelected = ImageLoader.getIconByName(GREEN_ICON_NAME);
		selectedNavpoint = -1;
		this.drawNavNumbers = drawNavNumbers;
	}

	/**
	 * Add a new navpoint position.
	 * 
	 * @param firstPoint the navpoint position.
	 */
	public void addNavpointPosition(Coordinates firstPoint) {
		navPositions.add(firstPoint);
	}

	/**
	 * Gets a navpoint position at an index.
	 * 
	 * @param index the navpoint position index.
	 * @return null if index isn't valid.
	 */
	public IntPoint getNavpointPosition(int index) {
		return mapPositions.get(index);
	}

	/**
	 * Sets a navpoint position at a given index.
	 * 
	 * @param index               the index to set the position.
	 * @param newNavpointPosition the position to set at the index.
	 */
	public void setNavpointPosition(int index, IntPoint newNavpointPosition) {
		// if ((index > -1) && (index < navpointPositions.size()))
		// 	navpointPositions.set(index, newNavpointPosition);
	}

	/**
	 * Checks if navpoint position is within the display boundries.
	 * 
	 * @param newNavpointPosition the navpoint position to check.
	 * @return true if within display boundries.
	 */
	public boolean withinDisplayEdges(IntPoint newNavpointPosition) {
		boolean withinXBounds = ((newNavpointPosition.getiX() > -1)
				&& (newNavpointPosition.getiX() < (300 - navpointIconColor.getIconWidth())));
		boolean withinYBounds = ((newNavpointPosition.getiY() >= navpointIconColor.getIconHeight())
				&& (newNavpointPosition.getiY() < 300));
		return withinXBounds && withinYBounds;
	}

	/**
	 * Sets a navpoint at an index as selected.
	 * 
	 * @param index the index to select.
	 */
	public void selectNavpoint(int index) {
		selectedNavpoint = index;
	}

	/**
	 * Clears the selected navpoint if any.
	 */
	public void clearSelectedNavpoint() {
		selectedNavpoint = -1;
	}

	/**
	 * Checks if an x,y position is over a navpoint flag.
	 * 
	 * @param x the x position
	 * @param y the y position
	 * @return navpoint index the mouse is over or -1 if none.
	 */
	public int overNavIcon(int x, int y) {
		int result = -1;

		for (int z = 0; z < mapPositions.size(); z++) {
			if (result < 0) {
				IntPoint navpointPosition = mapPositions.get(z);
				int leftBound = navpointPosition.getiX();
				int rightBound = navpointPosition.getiX() + navpointIconColor.getIconWidth();
				int topBound = navpointPosition.getiY() - navpointIconColor.getIconHeight();
				int bottomBound = navpointPosition.getiY();

				if ((x >= leftBound) && (x <= rightBound) && (y >= topBound) && (y <= bottomBound))
					result = z;
			}
		}

		return result;
	}

	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g2d         graphics context of the map display.
	 */
	@Override
	public List<MapHotspot> displayLayer(Coordinates mapCenter, MapDisplay baseMap, Graphics2D g2d, Dimension d) {

		IntPoint prevPosition = MapUtils.getRectPosition(startPoint, mapCenter, baseMap, d);
		
		// Map points redone for new map center/scale.
		mapPositions = new ArrayList<>();

		// Draw the path lines.
		for (int x = 0; x < navPositions.size(); x++) {
			g2d.setColor(Color.GREEN);
			IntPoint currentPosition = MapUtils.getRectPosition(navPositions.get(x), mapCenter, baseMap, d);
			mapPositions.add(currentPosition);
			g2d.drawLine(currentPosition.getiX(), currentPosition.getiY(), prevPosition.getiX(),
						prevPosition.getiY());
		}

		// Draw navpoint icons and numbers after the lines so they overlap.
		for (int x = 0; x < mapPositions.size(); x++) {
			IntPoint currentPosition = 	mapPositions.get(x);

			// Determine navpoint icon to use.
			Icon navIcon = null;
			if (selectedNavpoint == x)
				navIcon = navpointIconSelected;
			else
				navIcon = navpointIconColor;

			// Determine navpoint icon draw position.
			IntPoint drawLocation = new IntPoint(currentPosition.getiX(),
					(currentPosition.getiY() - navIcon.getIconHeight()));

			// Draw the navpoint icon.
			navIcon.paintIcon(displayComponent, g2d, drawLocation.getiX(), drawLocation.getiY());

			if (drawNavNumbers) {
				g2d.setColor(Color.WHITE);
				g2d.setFont(FONT);
				g2d.drawString("" + (x + 1), (currentPosition.getiX() + 5), (currentPosition.getiY() + 5));
			}
		}

		return Collections.emptyList();
	}
}
