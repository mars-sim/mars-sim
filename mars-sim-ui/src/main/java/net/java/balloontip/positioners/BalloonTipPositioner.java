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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import net.java.balloontip.BalloonTip;

/**
 * A BalloonTipPositioner is used to determine the position of a BalloonTip
 * Note: If you change a positioner's settings, the changes may not be visible until the balloon tip is redrawn.
 * @author Tim Molderez
 */
public abstract class BalloonTipPositioner {
	protected BalloonTip balloonTip = null;
	private PropertyChangeListener styleListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			onStyleChange();
		}
	};
	
	/**
	 * Default constructor
	 */
	public BalloonTipPositioner() {}
	
	/**
	 * Retrieve the balloon tip that uses this positioner
	 * @return The balloon tip that uses this positioner
	 */
	public final BalloonTip getBalloonTip() {
		return balloonTip;
	}
	
	/**
	 * This method is meant only to be used by BalloonTip!
	 * A BalloonTip must call this method at the end of its construction (or when it's swapping for a new BalloonTipPositioner).
	 * @param balloonTip	the balloon tip
	 */
	public final void setBalloonTip(final BalloonTip balloonTip) {
		this.balloonTip = balloonTip;
		this.balloonTip.addPropertyChangeListener("style", styleListener);
		onStyleChange();
	}
	
	/**
	 * Find the current location of the balloon's tip, relative to the top-level container
	 * @return				the location of the tip
	 */
	 public abstract Point getTipLocation();

	/**
	 * Determine and set the current location of the balloon tip
	 * @param attached		the rectangle to which the balloon tip attaches itself
	 */
	public abstract void determineAndSetLocation(Rectangle attached);
	
	/**
	 * This method is called whenever the balloon tip's style changes.
	 * The positioner will ensure the new style is set up properly.
	 */
	protected abstract void onStyleChange();
	
	protected void finalize() throws Throwable {
		if (balloonTip!=null) {
			balloonTip.removePropertyChangeListener("style", styleListener);
		}
		super.finalize();
	}
	
}
