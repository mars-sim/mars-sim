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
import java.awt.geom.GeneralPath;


/**
 * A simple rectangular balloon tip style with a one pixel border
 * @author Bernhard Pauler
 * @author Tim Molderez
 */
public class EdgedBalloonStyle extends BalloonTipStyle {
	private final Color borderColor;
	private final Color fillColor;

	/**
	 * Constructor
	 * @param borderColor	border line color
	 * @param fillColor		fill color
	 */
	public EdgedBalloonStyle(Color fillColor, Color borderColor) {
		super();
		this.borderColor = borderColor;
		this.fillColor = fillColor;
	}
	
	public Insets getBorderInsets(Component c) {
		if (flipY) {
			return new Insets(verticalOffset + 1, 1, 1, 1);
		}
		return new Insets(1, 1, verticalOffset + 1 , 1);
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
		outline.moveTo(x, yTop);
		outline.lineTo(x, yBottom);

		if (!flipX && !flipY) {
			outline.lineTo(x + horizontalOffset, yBottom);
			outline.lineTo(x + horizontalOffset, yBottom + verticalOffset);
			outline.lineTo(x + horizontalOffset + verticalOffset, yBottom);
		} else if (flipX && !flipY) {
			outline.lineTo(x + width - horizontalOffset - verticalOffset, yBottom);
			outline.lineTo(x + width - horizontalOffset, yBottom + verticalOffset);
			outline.lineTo(x + width - horizontalOffset, yBottom);
		}

		outline.lineTo(x + width, yBottom);
		outline.lineTo(x + width, yTop);

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

		g2d.setPaint(fillColor);
		g2d.fill(outline);
		g2d.setPaint(borderColor);
		g2d.draw(outline);
	}
}
