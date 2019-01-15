/**
 * Mars Simulation Project
 * EllipseLayer.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;

/**
 * A map layer for displaying ellipses.
 */
public class EllipseLayer implements MapLayer {

	// Data members.
	private Color drawColor;
	private boolean displayEllipse;
	private IntPoint foci1;
	private IntPoint foci2;
	private int range;

	/**
	 * Constructor.
	 * 
	 * @param drawColor the color to draw the ellipse.
	 */
	public EllipseLayer(Color drawColor) {
		this.drawColor = drawColor;
	}

	/**
	 * Sets the flag for displaying the ellipse.
	 * 
	 * @param displayEllipse true if ellipse is to be displayed.
	 */
	public void setDisplayEllipse(boolean displayEllipse) {
		this.displayEllipse = displayEllipse;
	}

	/**
	 * Sets the ellipse details.
	 * 
	 * @param foci1 the position of the first focus point.
	 * @param foci2 the position of the second focus point.
	 * @param range the ellipse range.
	 */
	public void setEllipseDetails(IntPoint foci1, IntPoint foci2, int range) {
		this.foci1 = foci1;
		this.foci2 = foci2;
		this.range = range;
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
		// Display ellipse if flag is true.
		if (displayEllipse) {
			g2d.setColor(drawColor);
			IntPoint center = getCenterPoint();
			int width = getWidth();
			int height = getHeight();
			int startX = center.getiX() - (width / 2);
			int startY = center.getiY() - (height / 2);

			// Rotate graphics so that ellipse is drawn at angle between its foci.
			AffineTransform rat = new AffineTransform();
			rat.setToRotation(getFociAngle(), center.getX(), center.getY());
			g2d.transform(rat);

			// Draw the ellipse.
			Ellipse2D ellipse = new Ellipse2D.Double(startX, startY, width, height);
			g2d.draw(ellipse);

			// Rotate graphics back to where it was originally.
			rat.setToRotation((-1D * getFociAngle()), center.getX(), center.getY());
			g2d.transform(rat);
		}
	}

	/**
	 * Gets the width of the ellipse.
	 * 
	 * @return width in pixels.
	 */
	private int getWidth() {
		return getFociDistance() + range;
	}

	/**
	 * Gets the height of the ellipse.
	 * 
	 * @return height in pixels.
	 */
	private int getHeight() {
		double a = getFociDistance() / 2D;
		double h = (getFociDistance() + range) / 2D;
		return (int) Math.round(Math.sqrt(Math.abs(Math.pow(h, 2D) - Math.pow(a, 2D)))) * 2;
	}

	/**
	 * Gets the distance between the focus points.
	 * 
	 * @return distance in pixels.
	 */
	private int getFociDistance() {
		int xDiff = foci2.getiX() - foci1.getiX();
		int yDiff = foci2.getiY() - foci1.getiY();
		return (int) Math.round(Math.sqrt(Math.pow(xDiff, 2D) + Math.pow(yDiff, 2D)));
	}

	/**
	 * Gets the center point of the ellipse.
	 * 
	 * @return center point.
	 */
	private IntPoint getCenterPoint() {
		int xCenter = (int) Math.round((foci1.getiX() + foci2.getiX()) / 2D);
		int yCenter = (int) Math.round((foci1.getiY() + foci2.getiY()) / 2D);
		return new IntPoint(xCenter, yCenter);
	}

	/**
	 * Gets the angle between the focus points.
	 * 
	 * @return angle (radians)
	 */
	private double getFociAngle() {
		int xDiff = foci2.getiX() - foci1.getiX();
		int yDiff = foci2.getiY() - foci1.getiY();
		return Math.atan2(yDiff, xDiff);
	}
}