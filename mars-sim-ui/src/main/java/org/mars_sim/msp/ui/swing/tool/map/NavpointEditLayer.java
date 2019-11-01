/**
 * Mars Simulation Project
 * NavpointEditLayer.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.ui.swing.ImageLoader;

/**
 * A map layer to allow the editing of navpoints.
 */
public class NavpointEditLayer implements MapLayer {

	// Static members
	private static final String BLUE_ICON_NAME = "FlagBlue";
	private static final String GREEN_ICON_NAME = "FlagGreen";
	private static final Font FONT = new Font("SansSerif", Font.PLAIN, 9);
	
	// Domain members.
	private List<IntPoint> navpointPositions;
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
	public NavpointEditLayer(Component displayComponent, boolean drawNavNumbers) {
		this.displayComponent = displayComponent;
		navpointPositions = new ArrayList<IntPoint>();
		navpointIconColor = ImageLoader.getIcon(BLUE_ICON_NAME);
		navpointIconSelected = ImageLoader.getIcon(GREEN_ICON_NAME);
		selectedNavpoint = -1;
		this.drawNavNumbers = drawNavNumbers;
	}

	/**
	 * Add a new navpoint position.
	 * 
	 * @param newNavpointPosition the navpoint position.
	 */
	public void addNavpointPosition(IntPoint newNavpointPosition) {
		navpointPositions.add(newNavpointPosition);
	}

	/**
	 * Clear all navpoint positions.
	 */
	public void clearNavpointPositions() {
		navpointPositions.clear();
	}

	/**
	 * Gets a navpoint position at an index.
	 * 
	 * @param index the navpoint position index.
	 * @return null if index isn't valid.
	 */
	public IntPoint getNavpointPosition(int index) {
		if ((index > -1) && (index < navpointPositions.size()))
			return navpointPositions.get(index);
		else
			return null;
	}

	/**
	 * Sets a navpoint position at a given index.
	 * 
	 * @param index               the index to set the position.
	 * @param newNavpointPosition the position to set at the index.
	 */
	public void setNavpointPosition(int index, IntPoint newNavpointPosition) {
		if ((index > -1) && (index < navpointPositions.size()))
			navpointPositions.set(index, newNavpointPosition);
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

		for (int z = 0; z < navpointPositions.size(); z++) {
			if (result < 0) {
				IntPoint navpointPosition = navpointPositions.get(z);
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
	 * @param mapType   the type of map.
	 * @param g         graphics context of the map display.
	 */
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		// Draw the path lines.
		for (int x = 0; x < navpointPositions.size(); x++) {
			g2d.setColor(Color.GREEN);
			IntPoint currentPosition = navpointPositions.get(x);
			if ((x == 0) || (x == (navpointPositions.size() - 1)))
				g2d.drawLine(currentPosition.getiX(), currentPosition.getiY(), 150, 150);

			if (x != 0) {
				IntPoint prevPosition = navpointPositions.get(x - 1);
				g2d.drawLine(currentPosition.getiX(), currentPosition.getiY(), prevPosition.getiX(),
						prevPosition.getiY());
			}
		}

		// Draw navpoint icons and numbers.
		for (int x = 0; x < navpointPositions.size(); x++) {
			IntPoint currentPosition = navpointPositions.get(x);

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
	}
}