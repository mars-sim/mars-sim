/**
 * Copyright (c) 2011-2013 Bernhard Pauler, Tim Molderez.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 3-Clause BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/BSD-3-Clause
 */

package net.java.balloontip.styles;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * A balloon tip with rounded corners and an image as background
 * @author Tim Molderez
 */
public class TexturedBalloonStyle extends BalloonTipStyle {

	private final int arcWidth;
	private final int arcHeight;

	private final BufferedImage bg;
	private final Rectangle bgBounds;
	private final Color borderColor;

	/**
	 * Constructor
	 * @param arcWidth		width of the rounded corner
	 * @param arcHeight		height of the rounded corner
	 * @param background	the background image that's used (it's used as a pattern, so the image will repeat...)
	 * @param borderColor	line color
	 * @throws IOException	in case something went wrong when opening the background image
	 */
	public TexturedBalloonStyle(int arcWidth, int arcHeight, URL background, Color borderColor) throws IOException {
		super();
		this.arcWidth = arcWidth;
		this.arcHeight = arcHeight;
		this.bg = javax.imageio.ImageIO.read(background);
		bgBounds = new Rectangle(0,0, bg.getWidth(), bg.getHeight());
		this.borderColor = borderColor;
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
		width-=1;
		height-=1;

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

		outline.quadTo(x, yTop, x, yTop + arcHeight);
		outline.lineTo(x, yBottom - arcHeight);
		outline.quadTo(x, yBottom, x + arcWidth, yBottom);


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
		outline.quadTo(x + width, yBottom, x + width, yBottom - arcHeight);
		outline.lineTo(x + width, yTop + arcHeight);
		outline.quadTo(x + width, yTop, x + width - arcWidth, yTop);

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

		g2d.setPaint(new TexturePaint(bg, bgBounds));
		g2d.fill(outline);
		g2d.setPaint(borderColor);
		g2d.draw(outline);
	}

	public int getMinimalHorizontalOffset() {
		return arcWidth + verticalOffset;
	}
}
