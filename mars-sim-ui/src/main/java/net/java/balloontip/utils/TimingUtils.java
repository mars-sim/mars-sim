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

import javax.swing.Timer;

import net.java.balloontip.BalloonTip;

/**
 * This class provides balloon tips that can time out
 * @author Tim
 */
public final class TimingUtils {

	/*
	 * Disallow instantiating this class
	 */
	private TimingUtils() {}

	/**
	 * Displays a balloon tip for a certain time, then close it.
	 * (Note that you cannot reuse this balloon tip after it has been closed.)
	 * @param balloon			the BalloonTip
	 * @param time				show the balloon for this amount of milliseconds
	 */
	public static void showTimedBalloon(final BalloonTip balloon, int time) {
		showTimedBalloon(balloon, time,new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				balloon.closeBalloon();
			}
		});
	}

	/**
	 * Displays a balloon tip for a certain time
	 * @param balloon			the BalloonTip
	 * @param time				show the balloon for this amount of milliseconds
	 * @param onTimeout			this action is taken when time runs out
	 */
	public static void showTimedBalloon(final BalloonTip balloon, int time, ActionListener onTimeout) {
		balloon.setVisible(true);
		Timer timer = new Timer(time, onTimeout);
		timer.setRepeats(false);
		timer.start();
	}
}
