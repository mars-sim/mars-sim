package org.mars_sim.msp.ui.standard.tool.map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.IntPoint;

public class EllipseLayer implements MapLayer {

	private Color drawColor;
	private boolean displayEllipse;
	private IntPoint foci1;
	private IntPoint foci2;
	private int range;
	
	public EllipseLayer(Color drawColor) {
		this.drawColor = drawColor;
	}
	
	public void setDisplayEllipse(boolean displayEllipse) {
		this.displayEllipse = displayEllipse;
	}
	
	public void setEllipseDetails(IntPoint foci1, IntPoint foci2, int range) {
		this.foci1 = foci1;
		this.foci2 = foci2;
		this.range = range;
	}
	
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {
		
		Graphics2D g2d = (Graphics2D) g;
		
		if (displayEllipse) {
			g2d.setColor(drawColor);
			IntPoint center = getCenterPoint();
			int width = getWidth();
			int height = getHeight();
			int startX = center.getiX() - (width / 2);
			int startY = center.getiY() - (height / 2);
			
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			AffineTransform rat = new AffineTransform();
		    rat.setToRotation(getFociAngle(), center.getX(), center.getY());
		    g2d.transform(rat);
			
			Ellipse2D ellipse = new Ellipse2D.Double(startX, startY, width, height);
			g2d.draw(ellipse);
			
			rat.setToRotation((-1D * getFociAngle()), center.getX(), center.getY());
			g2d.transform(rat);
		}
	}
	
	private int getWidth() {
		return getFociDistance() + range;
	}
	
	private int getHeight() {
		double a = getFociDistance() / 2D;
		double h = (getFociDistance() + range) / 2D;
		return (int) Math.round(Math.sqrt(Math.abs(Math.pow(h, 2D) - Math.pow(a, 2D)))) * 2;
	}
	
	private int getFociDistance() {
		int xDiff = foci2.getiX() - foci1.getiX();
		int yDiff = foci2.getiY() - foci1.getiY();
		return (int) Math.round(Math.sqrt(Math.pow(xDiff, 2D) + Math.pow(yDiff, 2D)));
	}
	
	private IntPoint getCenterPoint() {
		int xCenter = (int) Math.round((foci1.getiX() + foci2.getiX()) / 2D);
		int yCenter = (int) Math.round((foci1.getiY() + foci2.getiY()) / 2D);
		return new IntPoint(xCenter, yCenter);
	}
	
	private double getFociAngle() {
		double result = 0D;
		
		int xDiff = foci2.getiX() - foci1.getiX();
		int yDiff = foci2.getiY() - foci1.getiY();
		/*
		if ((xDiff == 0) && (yDiff == 0)) result = 0D;
		else if (xDiff == 0) result = Math.PI;
		else if (yDiff == 0) result = 0D;
		else {
			result = Math.atan(yDiff / xDiff);
		}
		*/
		result = Math.atan2(yDiff, xDiff);
		return result;
	}
}