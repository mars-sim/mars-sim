/**
 * Copyright (c) 2011-2013 Bernhard Pauler, Tim Molderez.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 3-Clause BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/BSD-3-Clause
 */

package net.java.balloontip.positioners;

import java.awt.Rectangle;

/**
 * This class positions a balloon tip below the component it's attached to, with the tip on the left
 * @author Tim Molderez
 */
public class LeftBelowPositioner extends BasicBalloonTipPositioner {
	public LeftBelowPositioner(int hO, int vO) {
		super(hO, vO);
	}

	protected void determineLocation(Rectangle attached) {
		// First calculate the location, without applying any correction tricks
		int balloonWidth = balloonTip.getPreferredSize().width;
		int balloonHeight = balloonTip.getPreferredSize().height;
		flipX = false;
		flipY = true;
		
		hOffset = preferredHorizontalOffset;
		if (fixedAttachLocation) {
			x = new Float(attached.x + attached.width * attachLocationX).intValue() - hOffset;
			y = new Float(attached.y + attached.height * attachLocationY).intValue();
		} else {
			x = attached.x;
			y = attached.y + attached.height;
		}
		
		// Apply orientation correction
		if (orientationCorrection) {
			// Check collision with the bottom of the window
			if (y + balloonHeight > balloonTip.getTopLevelContainer().getHeight()) {
				flipY = false;
				if (fixedAttachLocation) {
					y -= balloonHeight;
				} else {
					y = attached.y - balloonHeight;
				} 
			}
			
			// Check collision with the left side of the window
			if (x < 0) {
				flipX = true;
				if (fixedAttachLocation) {
					x -= balloonWidth - 2*hOffset;
				} else {
					x = attached.x + attached.width - balloonWidth;
				}
				hOffset = balloonWidth - hOffset;
			}
		}
		
		// Apply offset correction
		if (offsetCorrection) {
			applyOffsetCorrection();
		}
	}
}
