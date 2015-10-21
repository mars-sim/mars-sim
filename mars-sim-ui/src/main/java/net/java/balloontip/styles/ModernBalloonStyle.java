/**
 * Copyright (c) 2011-2013 Bernhard Pauler, Tim Molderez.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 3-Clause BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/BSD-3-Clause
 */

package net.java.balloontip.styles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;

/**
 * A balloon tip with a vertical linear gradient background
 * The border's thickness can be adjusted and anti-aliased.
 * You can also choose which corners should be rounded corners or just plain corners.
 * @author Tim Molderez
 */
public class ModernBalloonStyle extends BalloonTipStyle {

	private final int arcWidth;
	private final int arcHeight;

	private boolean topLeft = true;
	private boolean topRight = false;
	private boolean bottomLeft = false;
	private boolean bottomRight = true;

	private int borderThickness = 1;
	private boolean AAenabled = false;

	private final Color topFillColor;
	private final Color bottomFillColor;
	private final Color borderColor;

	/**
	 * Constructor
	 * @param arcWidth			width of the rounded corner
	 * @param arcHeight			height of the rounded color
	 * @param borderColor		line color
	 * @param topFillColor		top color of the lineair gradient fill color
	 * @param bottomFillColor	bottom color of the lineair gradient fill color
	 */
	public ModernBalloonStyle(int arcWidth, int arcHeight, Color topFillColor, Color bottomFillColor, Color borderColor) {
		super();
		this.arcWidth = arcWidth;
		this.arcHeight = arcHeight;
		this.topFillColor = topFillColor;
		this.bottomFillColor = bottomFillColor;
		this.borderColor = borderColor;
	}

	/**
	 * Sets the style for each corner.
	 * If true, this corner will be rounded; if false, it's just a regular corner
	 * @param topLeft		if true, the top-left corner is rounded
	 * @param topRight		if true, the top-right corner is rounded
	 * @param bottomLeft	if true, the bottom-left corner is rounded
	 * @param bottomRight	if true, the bottom-right corner is rounded
	 */
	public void setCornerStyles(boolean topLeft, boolean topRight, boolean bottomLeft, boolean bottomRight) {
		this.topLeft = topLeft;
		this.topRight = topRight;
		this.bottomLeft = bottomLeft;
		this.bottomRight = bottomRight;
	}

	/**
	 * Set the thickness of the balloon tip's border
	 * @param thickness		border thickness in pixels
	 */
	public void setBorderThickness(int thickness) {
		borderThickness = thickness;
	}

	/**
	 * Enable/disable anti-aliasing for this balloon tip
	 * @param enable	if true, AA is enabled; if false, the settings remain untouched
	 */
	public void enableAntiAliasing(boolean enable) {
		AAenabled = enable;
	}

	public Insets getBorderInsets(Component c) {
		if (flipY) {
			return new Insets(verticalOffset+arcHeight, arcWidth, arcHeight, arcWidth);
		}
		return new Insets(arcHeight, arcWidth, arcHeight+verticalOffset, arcWidth);
	}

	public boolean isBorderOpaque() {
		return true;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Graphics2D g2d = (Graphics2D) g;
		if (AAenabled) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}

		// Make room for the border line
		x+=borderThickness - 1;
		y+=borderThickness - 1;
		width-=borderThickness*2;
		height-=borderThickness*2;

		int yTop;		// Y-coordinate of the top side of the balloon
		int yBottom;	// Y-coordinate of the bottom side of the balloon
		if (flipY) {
			yTop = y + verticalOffset;
			yBottom = y + height;
		} else {
			yTop = y;
			yBottom = y + height - verticalOffset;
		}

		// Draw the outline of the balloon
		GeneralPath outline = new GeneralPath();
		outline.moveTo(x + arcWidth, yTop);

		if (topLeft) {
			outline.quadTo(x, yTop, x, yTop + arcHeight);
		} else {
			outline.lineTo(x, yTop);
			outline.lineTo(x, yTop + arcHeight);
		}

		outline.lineTo(x, yBottom - arcHeight);

		if (bottomLeft) {
			outline.quadTo(x, yBottom, x + arcWidth, yBottom);
		} else {
			outline.lineTo(x, yBottom);
			outline.lineTo(x + arcWidth, yBottom);
		}

		if (!flipX && !flipY) {
			outline.lineTo(x + horizontalOffset, yBottom);
			outline.lineTo(x + horizontalOffset, yBottom + verticalOffset);
			outline.lineTo(x + horizontalOffset + verticalOffset, yBottom);
		} else if (flipX && !flipY) {
			outline.lineTo(x + width - horizontalOffset - verticalOffset, yBottom);
			outline.lineTo(x + width - horizontalOffset, yBottom + verticalOffset);
			outline.lineTo(x + width - horizontalOffset, yBottom);
		}

		outline.lineTo(x + width - arcWidth, yBottom);

		if (bottomRight) {
			outline.quadTo(x + width, yBottom, x + width, yBottom - arcHeight);
		} else {
			outline.lineTo(x + width, yBottom);
			outline.lineTo(x + width, yBottom - arcHeight);
		}

		outline.lineTo(x + width, yTop + arcHeight);

		if (topRight) {
			outline.quadTo(x + width, yTop, x + width - arcWidth, yTop);
		} else {
			outline.lineTo(x + width, yTop);
			outline.lineTo(x + width - arcWidth, yTop);
		}

		if (!flipX && flipY) {
			outline.lineTo(x + horizontalOffset + verticalOffset, yTop);
			outline.lineTo(x + horizontalOffset, yTop - verticalOffset);
			outline.lineTo(x + horizontalOffset, yTop);	
		} else if (flipX && flipY) {
			outline.lineTo(x + width - horizontalOffset, yTop);
			outline.lineTo(x + width - horizontalOffset, yTop - verticalOffset);
			outline.lineTo(x + width - horizontalOffset - verticalOffset, yTop);
		}

		outline.closePath();

		// Now paint the sucker :)
		g2d.setPaint(new GradientPaint(0, yTop,topFillColor, 0, yBottom, bottomFillColor));
		g2d.fill(outline);
		g2d.setPaint(borderColor);
		Stroke backup = g2d.getStroke();
		g2d.setStroke(new BasicStroke(borderThickness));
		g2d.draw(outline);
		g2d.setStroke(backup);
	}

	public int getMinimalHorizontalOffset() {
		return arcWidth + verticalOffset + borderThickness;
	}
}
