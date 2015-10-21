/**
 * Copyright (c) 2011-2013 Bernhard Pauler, Tim Molderez.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 3-Clause BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/BSD-3-Clause
 */

package net.java.balloontip.positioners;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * A positioner that horizontally centers a balloon tip relative to its attached component
 * @author Tim Molderez
 */
public class CenteredPositioner extends BalloonTipPositioner {

	protected int x = 0;							// Current position
	protected int y = 0;
	
	protected boolean flipY = false;
	protected int preferredVerticalOffset;			// The preferred value of the vertical offset		

	protected boolean orientationCorrection = true;	// If true, a balloon tip should flip/mirror itself if otherwise it would become invisible 

	protected boolean fixedAttachLocation = false;	// If true, attachLocationX and attachLocationY should be used to determine the position of the tip
	protected float attachLocationY = 0.0f;			// A percentage that determines the Y-location of the tip on the attached object

	/**
	 * Constructor
	 * @param vO	preferred vertical offset
	 */
	public CenteredPositioner(int vO) {
		super();
		preferredVerticalOffset = vO;
	}

	/**
	 * Retrieve the preferred vertical offset
	 * @return		preferred vertical offset (in pixels)
	 */
	public int getPreferredVerticalOffset() {
		return preferredVerticalOffset;
	}

	/**
	 * Set the preferred horizontal offset
	 * @param preferredVerticalOffset	preferred vertical offset (in pixels)
	 */
	public void setPreferredVerticalOffset(int preferredVerticalOffset) {
		this.preferredVerticalOffset = preferredVerticalOffset;
		balloonTip.getStyle().setVerticalOffset(preferredVerticalOffset);
	}

	/**
	 * Is orientation correction enabled?
	 * @return		true if orientation correction is enabled
	 */
	public boolean isOrientationCorrected() {
		return orientationCorrection;
	}

	/**
	 * Set orientation correction
	 * @param orientationCorrection		enabled if true
	 */
	public void enableOrientationCorrection(boolean orientationCorrection) {
		this.orientationCorrection = orientationCorrection;
	}

	/**
	 * Does the tip have a fixed location?
	 * @return		true if the balloon has a fixed attaching location
	 */
	public boolean isFixedAttachLocation() {
		return fixedAttachLocation;
	}

	/**
	 * Set whether the tip should have a fixed location
	 * @param fixedAttachLocation	the tip has a fixed location if true
	 */
	public void enableFixedAttachLocation(boolean fixedAttachLocation) {
		this.fixedAttachLocation = fixedAttachLocation;
	}

	/**
	 * Returns the percentage that determines the Y-coordinate of the tip within the attached component
	 * (whereas 0.0 is the top and 1.0 is the bottom)
	 * @return		the percentage that determines the Y-coordinate of the attaching location
	 */
	public float getAttachLocationY() {
		return attachLocationY;
	}

	/**
	 * Set where the tip should be located, relative to the component the balloon is attached to.
	 * @param attachLocationY	a number from 0.0 to 1.0 (whereas 0 is the top; 1 is the bottom)
	 */
	public void setAttachLocation(float attachLocationX, float attachLocationY) {
		this.attachLocationY = attachLocationY;
	}

	public Point getTipLocation() {
		int tipX = x + balloonTip.getWidth() / 2;
		int tipY = y + balloonTip.getHeight();
		if (flipY) {
			tipY = y;
		}
		return new Point(tipX, tipY);
	}

	public void determineAndSetLocation(Rectangle attached) {
		determineLocation(attached);
		balloonTip.getStyle().flip(false, flipY);
		balloonTip.setBounds(x, y, balloonTip.getPreferredSize().width, balloonTip.getPreferredSize().height);
		balloonTip.revalidate(); // Revalidate is needed in case the balloon gets flipped; validate wouldn't do in that case.
	}
	
	/*
	 * Calculates the current position of the balloon tip, but does not apply it yet
	 * @param attached		the balloon tip is attached to this rectangle
	 */
	protected void determineLocation(Rectangle attached) {
		// First calculate the location, without applying any correction tricks
		int balloonWidth = balloonTip.getPreferredSize().width;
		int balloonHeight = balloonTip.getPreferredSize().height;
		
		flipY = false;
		
		int hOffset = balloonWidth / 2;
		float attachLocationX = 0.5f;
		
		x = new Float(attached.x + attached.width * attachLocationX).intValue() - hOffset;
		if (fixedAttachLocation) {
			y = new Float(attached.y + attached.height * attachLocationY).intValue() - balloonHeight;
		} else {
			y = attached.y - balloonHeight;
		}
		
		// Apply orientation correction
		if (orientationCorrection) {
			// Check collision with the top of the window
			if (y < 0) {
				flipY = true;
				if (fixedAttachLocation) {
					y += balloonHeight;
				} else {
					y = attached.y + attached.height;
				} 
			}
		}
		
		// Finally set the balloon tip's location
		balloonTip.getStyle().setHorizontalOffset(hOffset);
	}

	protected void onStyleChange() {
		balloonTip.getStyle().setHorizontalOffset(getBalloonTip().getWidth() / 2);
		balloonTip.getStyle().setVerticalOffset(preferredVerticalOffset);
	}
}
