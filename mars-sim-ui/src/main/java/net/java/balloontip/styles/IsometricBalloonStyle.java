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
 * A balloon tip style with a depth effect
 * @author Bernhard Pauler
 * @author Tim Molderez
 */
public class IsometricBalloonStyle extends BalloonTipStyle {
	private final Color sideColor;
	private final Color frontColor;
	private int depth;

	/**
	 * Constructor
	 * @param frontColor		front face color
	 * @param sideColor			side face color
	 * @param depth				depth of the balloon tip (in px)
	 */
	public IsometricBalloonStyle(Color frontColor, Color sideColor, int depth) {
		super();
		this.sideColor = sideColor;
		this.frontColor = frontColor;
		this.depth = depth;
	}
	
	public Insets getBorderInsets(Component c) {
		if (flipY) {
			return new Insets(verticalOffset+depth+1, 1, 1, depth+1);
		}
		return new Insets(depth+1, 1, verticalOffset+1, depth+1);
	}

	public boolean isBorderOpaque() {
		return true;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Graphics2D g2d = (Graphics2D) g;
		width-=(depth+1);
		height-=1;

		int yTop;		// Y-coordinate of the top side of the balloon
		int yBottom;	// Y-coordinate of the bottom side of the balloon
		if (flipY) {
			yTop = y + verticalOffset + depth;
			yBottom = y + height;
		} else {
			yTop = y + depth;
			yBottom = y + height - verticalOffset;
		}
		
		// Draw the top of the balloon
		GeneralPath top = new GeneralPath();
		top.moveTo(x, yTop);
		top.lineTo(width, yTop);
		top.lineTo(width+depth, yTop-depth);
		top.lineTo(x+depth, yTop-depth);
		top.closePath();
		g2d.setPaint(sideColor);
		g2d.fill(top);
		
		// Draw the side of the balloon
		GeneralPath side = new GeneralPath();
		side.moveTo(width, yTop);
		side.lineTo(width+depth, yTop-depth);
		side.lineTo(width+depth, yBottom-depth);
		side.lineTo(width, yBottom);
		side.closePath();
		g2d.setPaint(sideColor.darker());
		g2d.fill(side);
		
		// Draw the tip's side
		if (flipX && !flipY) {
			GeneralPath tipSide = new GeneralPath();
			tipSide.moveTo(x+width-horizontalOffset, yBottom);
			tipSide.lineTo(x+width-horizontalOffset+depth, yBottom);
			tipSide.lineTo(x+width-horizontalOffset+depth, yBottom+verticalOffset-depth);
			tipSide.lineTo(x+width-horizontalOffset, yBottom+verticalOffset);
			tipSide.closePath();
			g2d.setPaint(sideColor.darker());
			g2d.fill(tipSide);
		} else if (!flipX && flipY) {
			GeneralPath tipSide = new GeneralPath();
			tipSide.moveTo(x+horizontalOffset, yTop-verticalOffset);
			tipSide.lineTo(x+horizontalOffset+depth, yTop-verticalOffset-depth);
			tipSide.lineTo(x+horizontalOffset+verticalOffset+depth, yTop-depth);
			tipSide.lineTo(x+horizontalOffset+verticalOffset, yTop);
			tipSide.closePath();
			g2d.setPaint(sideColor.darker());
			g2d.fill(tipSide);
		} else if (flipX && flipY) {
			GeneralPath tipSide = new GeneralPath();
			tipSide.moveTo(x+width-horizontalOffset, yTop);
			tipSide.lineTo(x+width-horizontalOffset+depth, yTop-depth);
			tipSide.lineTo(x+width-horizontalOffset+depth, yTop-depth-verticalOffset);
			tipSide.lineTo(x+width-horizontalOffset, yTop-verticalOffset);
			tipSide.closePath();
			g2d.setPaint(sideColor.darker());
			g2d.fill(tipSide);
		}

		// Draw the front of the balloon
		GeneralPath front = new GeneralPath();
		front.moveTo(x, yTop);
		front.lineTo(x, yBottom);

		if (!flipX && !flipY) {
			front.lineTo(x + horizontalOffset, yBottom);
			front.lineTo(x + horizontalOffset, yBottom + verticalOffset);
			front.lineTo(x + horizontalOffset + verticalOffset, yBottom);
		} else if (flipX && !flipY) {
			front.lineTo(x + width - horizontalOffset - verticalOffset, yBottom);
			front.lineTo(x + width - horizontalOffset, yBottom + verticalOffset);
			front.lineTo(x + width - horizontalOffset, yBottom);
		}

		front.lineTo(x + width, yBottom);
		front.lineTo(x + width, yTop);

		if (!flipX && flipY) {
			front.lineTo(x + horizontalOffset + verticalOffset, yTop);
			front.lineTo(x + horizontalOffset, yTop - verticalOffset);
			front.lineTo(x + horizontalOffset, yTop);	
		} else if (flipX && flipY) {
			front.lineTo(x + width - horizontalOffset, yTop);
			front.lineTo(x + width - horizontalOffset, yTop - verticalOffset);
			front.lineTo(x + width - horizontalOffset - verticalOffset, yTop);
		}

		front.closePath();

		g2d.setPaint(frontColor);
		g2d.fill(front);
	}
}
