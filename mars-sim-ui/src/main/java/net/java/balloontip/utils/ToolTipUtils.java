/**
 * Copyright (c) 2011-2013 Bernhard Pauler, Tim Molderez.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 3-Clause BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/BSD-3-Clause
 */

package net.java.balloontip.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Timer;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.CustomBalloonTip;

/**
 * This class allows you to use a balloon tip as a tooltip
 * That is, the balloon tip will only show up for a certain amount of time while you hover over the attached component.
 * @author Tim Molderez
 */
public final class ToolTipUtils {

	/*
	 * Disallow instantiating this class
	 */
	private ToolTipUtils() {}

	/*
	 * This class monitors when the balloon tooltip should be shown
	 */
	private static class ToolTipController extends MouseAdapter implements MouseMotionListener {
		private final BalloonTip balloonTip;
		private final Timer initialTimer;
		private final Timer showTimer;

		/**
		 * Constructor
		 * @param balloonTip	the balloon tip to turn into a tooltip
		 * @param initialDelay	in milliseconds, how long should you hover over the attached component before showing the tooltip
		 * @param showDelay		in milliseconds, how long should the tooltip stay visible
		 */
		public ToolTipController(final BalloonTip balloonTip, int initialDelay, int showDelay) {
			super();
			this.balloonTip = balloonTip;
			initialTimer = new Timer(initialDelay, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					balloonTip.setVisible(true);
					showTimer.start();
				}
			});
			initialTimer.setRepeats(false);

			showTimer = new Timer(showDelay, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					balloonTip.setVisible(false);
				}
			});
			showTimer.setRepeats(false);
		}
		
		public void mouseEntered(MouseEvent e) {
			initialTimer.start();
		}
		
		public void mouseMoved(MouseEvent e) {
			if(balloonTip instanceof CustomBalloonTip) {
				// If the mouse is within the balloon tip's attached rectangle
				if (((CustomBalloonTip)balloonTip).getOffset().contains(e.getPoint())) {
					if (!balloonTip.isVisible() && !initialTimer.isRunning()) {
						initialTimer.start();
					}
				} else {
					stopTimers();
					balloonTip.setVisible(false);
				}
			}
		}

		public void mouseExited(MouseEvent e) {
			stopTimers();
			balloonTip.setVisible(false);
		}
		
		public void mousePressed(MouseEvent e) {
			stopTimers();
			balloonTip.setVisible(false);
		}
		
		/*
		 * Stops all timers related to this tool tip
		 */
		private void stopTimers() {
			initialTimer.stop();
			showTimer.stop();
		}
		
	}

	/**
	 * Turns a balloon tip into a tooltip
	 * This is done by adding a mouse listener to the attached component.
	 * (Call toolTipToBalloon() if you wish to remove this listener.)
	 * @param bT			the balloon tip
	 * @param initialDelay	in milliseconds, how long should you hover over the attached component before showing the tooltip
	 * @param showDelay		in milliseconds, how long should the tooltip stay visible
	 */
	public static void balloonToToolTip(final BalloonTip bT, int initialDelay, int showDelay) {
		bT.setVisible(false);
		// Add tooltip behaviour
		ToolTipController tTC = new ToolTipController(bT, initialDelay, showDelay);
		bT.getAttachedComponent().addMouseListener(tTC);
		bT.getAttachedComponent().addMouseMotionListener(tTC);
	}
	
	/**
	 * Turns a balloon tooltip back into a regular balloon tip
	 * @param bT			the balloon tip
	 */
	public static void toolTipToBalloon(final BalloonTip bT) {
		// Remove tooltip behaviour
		for (MouseListener m: bT.getAttachedComponent().getMouseListeners()) {
			if (m instanceof ToolTipController) {
				bT.getAttachedComponent().removeMouseListener(m);
				((ToolTipController) m).stopTimers();
				break;
			}
		}
		for (MouseMotionListener m: bT.getAttachedComponent().getMouseMotionListeners()) {
			if (m instanceof ToolTipController) {
				bT.getAttachedComponent().removeMouseMotionListener(m);
				break;
			}
		}
		
		bT.setVisible(true);
	}
}
