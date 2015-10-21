/**
 * Copyright (c) 2011-2013 Bernhard Pauler, Tim Molderez.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 3-Clause BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/BSD-3-Clause
 */

package net.java.balloontip.styles;

import java.awt.Component;
import java.awt.Insets;

import javax.swing.border.Border;

/**
 * A balloon tip style defines what a balloon tip should look like
 * @author Tim Molderez
 */
public abstract class BalloonTipStyle implements Border {
	protected int horizontalOffset = 0;
	protected int verticalOffset = 0;
	protected boolean flipX = false;
	protected boolean flipY = false;
	
	/**
	 * Sets a new value for the horizontal offset.
	 * @param px	horizontal offset (in pixels)
	 */
	public void setHorizontalOffset(int px) {
		horizontalOffset = px;
	}
	
	/**
	 * Sets a new value for the vertical offset.
	 * @param px	horizontal offset (in pixels)
	 */
	public void setVerticalOffset(int px) {
		verticalOffset = px;
	}
	
	/**
	 * Get the minimum value of the horizontal offset
	 * (Also useful as a maximum; maximum horizontaloffset = balloon tip width - minimum horizontal offset)
	 * @return		minimul horizontal offset
	 */
	public int getMinimalHorizontalOffset() {
		return verticalOffset;
	}
	
	/**
	 * Flip around the vertical axis
	 * @param flipX		if true, the balloon tip is flipped horizontally
	 */
	public void flipX(boolean flipX) {
		this.flipX = flipX;
	}
	
	/**
	 * Flip around the horizontal axis
	 * @param flipY		if true, the balloon tip is flipped vertically
	 */
	public void flipY(boolean flipY) {
		this.flipY = flipY;
	}
	
	/**
	 * Which mirror effect should be applied to the balloon tip
	 * @param flipX		if true, the balloon tip is flipped horizontally
	 * @param flipY		if true, the balloon tip is flipped vertically
	 */
	public void flip(boolean flipX, boolean flipY) {
		this.flipX = flipX;
		this.flipY = flipY;
	}
	
	/**
	 * Is this balloon tip opaque?
	 * @return		true if opaque, false if the border uses transparency
	 */
	public boolean isBorderOpaque() {
		return true;
	}
	
	/**
	 * Retrieve the balloon tip's border insets
	 * @return		the balloon tip's border insets
	 */
	public abstract Insets getBorderInsets(Component c);
}
