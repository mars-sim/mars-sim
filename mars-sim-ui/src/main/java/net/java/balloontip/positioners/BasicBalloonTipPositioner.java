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
 * Provides common functionality for the positioner classes
 * LeftAbovePositioner, LeftBelowPositioner, RightAbovePositioner and RightBelowPositioner
 * @author Tim Molderez
 */
public abstract class BasicBalloonTipPositioner extends BalloonTipPositioner {

	protected int x = 0;							// Current position
	protected int y = 0;
	protected int hOffset = 0;						// Current horizontal offset
	protected boolean flipX = false;				// Current orientation
	protected boolean flipY = false;

	protected int preferredHorizontalOffset;		// The preferred value of the horizontal offset
	protected int preferredVerticalOffset;			// The preferred value of the vertical offset
	protected int minimumHorizontalOffset;			// The horizontal offset may not become smaller than this value		

	protected boolean offsetCorrection = true;		// If true, a balloon tip should adjust its horizontal offset when the left/right side collides with the window's border
	protected boolean orientationCorrection = true;	// If true, a balloon tip should flip/mirror itself if otherwise it would become invisible 

	protected boolean fixedAttachLocation = false;	// If true, attachLocationX and attachLocationY should be used to determine the position of the tip

	protected float attachLocationX = 0.0f;			// A percentage that determines the X-location of the tip on the attached object
	protected float attachLocationY = 0.0f;			// A percentage that determines the Y-location of the tip on the attached object
													// For example, if attachLocationX and Y are both 0.5, then the tip is centered on the attached object
	
	/**
	 * Constructor
	 * @param hO	preferred horizontal offset (in pixels)
	 * @param vO	preferred vertical offset (in pixels)
	 */
	public BasicBalloonTipPositioner(int hO, int vO) {
		super();
		preferredHorizontalOffset = hO;
		preferredVerticalOffset = vO;
	}
	
	protected void onStyleChange() {
		balloonTip.getStyle().setHorizontalOffset(preferredHorizontalOffset);
		balloonTip.getStyle().setVerticalOffset(preferredVerticalOffset);
		minimumHorizontalOffset = balloonTip.getStyle().getMinimalHorizontalOffset();
	}

	/**
	 * Retrieve the preferred horizontal offset (in pixels)
	 * @return 		preferred horizontal offset (in pixels)
	 */
	public int getPreferredHorizontalOffset() {
		return preferredHorizontalOffset;
	}

	/**
	 * Set the preferred horizontal offset
	 * @param preferredHorizontalOffset		preferred horizontal offset (in pixels)
	 */
	public void setPreferredHorizontalOffset(int preferredHorizontalOffset) {
		this.preferredHorizontalOffset = preferredHorizontalOffset;
		balloonTip.getStyle().setHorizontalOffset(preferredHorizontalOffset);
		balloonTip.repaint();
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
	 * @param preferredVerticalOffset		preferred horizontal offset (in pixels)
	 */
	public void setPreferredVerticalOffset(int preferredVerticalOffset) {
		this.preferredVerticalOffset = preferredVerticalOffset;
		this.minimumHorizontalOffset = 2 * preferredVerticalOffset;
		balloonTip.getStyle().setVerticalOffset(preferredVerticalOffset);
	}

	/**
	 * Is offset correction enabled?
	 * @return		true if offset correction is enabled
	 */
	public boolean isOffsetCorrected() {
		return offsetCorrection;
	}

	/**
	 * Set offset correction
	 * @param offsetCorrection	enabled if true
	 */
	public void enableOffsetCorrection(boolean offsetCorrection) {
		this.offsetCorrection = offsetCorrection;
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
	 * Returns the percentage that determines the X-coordinate of the tip within the attached component
	 * (whereas 0.0 is the left side and 1.0 is the right side)
	 * @return		the percentage that determines the X-coordinate of the attaching location
	 */
	public float getAttachLocationX() {
		return attachLocationX;
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
	 * @param attachLocationX	a number from 0.0 to 1.0 (whereas 0 is the left side; 1 is the right side)
	 * @param attachLocationY	a number from 0.0 to 1.0 (whereas 0 is the top; 1 is the bottom)
	 */
	public void setAttachLocation(float attachLocationX, float attachLocationY) {
		this.attachLocationX = attachLocationX;
		this.attachLocationY = attachLocationY;
	}

	public Point getTipLocation() {
		int tipX = x + hOffset;
		int tipY = y + balloonTip.getHeight();

		if (flipX) {
			tipX = x + hOffset;
		}
		if (flipY) {
			tipY = y;
		}

		return new Point(tipX, tipY);
	}

	/*
	 * Applies offset correction to the current position of the balloon tip
	 */
	protected void applyOffsetCorrection() {
		// Check collision with the left side of the window
		int overflow = -x;
		int balloonWidth = balloonTip.getPreferredSize().width;

		if (overflow > 0) {
			x += overflow;
			hOffset -= overflow;
			// Take into account the minimum horizontal offset
			if (hOffset < minimumHorizontalOffset) {
				hOffset = minimumHorizontalOffset;
				if (flipX) {
					x += -overflow +  (balloonWidth - preferredHorizontalOffset) - minimumHorizontalOffset;
				}else {
					x += -overflow +  preferredHorizontalOffset - minimumHorizontalOffset;
				}
			}
		}

		// Check collision with the right side of the window
		overflow = (x+balloonWidth) - balloonTip.getTopLevelContainer().getWidth();
		if (overflow > 0) {
			x -= overflow;
			hOffset += overflow;

			// Take into account the minimum horizontal offset
			if (hOffset > balloonWidth - minimumHorizontalOffset) {
				hOffset = balloonWidth - minimumHorizontalOffset;
				if (flipX) {
					x += overflow + preferredHorizontalOffset + minimumHorizontalOffset;
				}else {
					x += overflow -  (balloonWidth - preferredHorizontalOffset) + minimumHorizontalOffset;
				}
			}
		}		
	}
	
	public void determineAndSetLocation(Rectangle attached) {
		determineLocation(attached);
		
		if (flipX) {
			balloonTip.getStyle().setHorizontalOffset(balloonTip.getPreferredSize().width - hOffset);
		} else {
			balloonTip.getStyle().setHorizontalOffset(hOffset);
		}
		
		balloonTip.getStyle().flip(flipX, flipY);
		balloonTip.setBounds(x, y, balloonTip.getPreferredSize().width, balloonTip.getPreferredSize().height);
		
		balloonTip.revalidate(); // Revalidate is needed in case the balloon gets flipped; validate wouldn't do in that case.
		if (hOffset != preferredHorizontalOffset) {
			balloonTip.repaint(); // In certain cases, when the horizontal offset changes, it doesn't get redrawn properly without a repaint...
		}
	}
	
	/*
	 * Calculates the current position of the balloon tip, but does not apply it yet
	 * @param attached		the balloon tip is attached to this rectangle
	 */
	protected abstract void determineLocation(Rectangle attached);
}
