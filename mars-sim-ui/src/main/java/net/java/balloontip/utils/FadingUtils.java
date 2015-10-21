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
 * A utility class for adding simple linear fade-in/out effects to balloon tips
 * @author Tim Molderez
 */
public final class FadingUtils {

	/*
	 * Disallow instantiating this class
	 */
	private FadingUtils() {}

	/**
	 * Execute a fade-in effect on a balloon tip
	 * @param balloon		the balloon tip
	 * @param onStop		this action listener is triggered once the effect has stopped (may be null)
	 * @param time			the duration of the fade-in effect (in ms)
	 * @param refreshRate	at how many frames-per-second should the effect run
	 */
	public static void fadeInBalloon(final BalloonTip balloon, final ActionListener onStop, final int time, final int refreshRate) {
		balloon.setOpacity(0.0f);
		balloon.setVisible(true);

		final int timeDelta = 1000/refreshRate;
		// Trigger this timer at the desired refresh rate and stop it once full opacity is reached.
		final Timer timer = new Timer(timeDelta, new ActionListener () {
			int curTime=0;
			public void actionPerformed(ActionEvent e) {
				curTime += timeDelta;
				float newOpacity = ((float)curTime)/time; // f(time)=curTime/time
				if (newOpacity >= 0.9999999f || Float.isNaN(newOpacity)) {
					((Timer)e.getSource()).stop();
					/* Because of some weird bug, possibly in AlphaComposite, the balloon tip is shifted 1px when the opacity is 1.0f
					 * We'll just use something as close to 1 as a workaround, for now.. */
					balloon.setOpacity(0.9999999f);
					if (onStop != null) {
						onStop.actionPerformed(e);
					}
				} else {
					balloon.setOpacity(newOpacity);
				}
			}
		});
		timer.setRepeats(true);
		timer.start();
	}

	/**
	 * Execute a fade-in effect on a balloon tip
	 * @param balloon		the balloon tip
	 * @param onStop		this action listener is triggered once the effect has stopped (may be null)
	 * @param time			the duration of the fade-out effect (in ms)
	 * @param refreshRate	at how many frames-per-second should the effect run
	 */
	public static void fadeOutBalloon(final BalloonTip balloon, final ActionListener onStop, final int time, final int refreshRate) {
		balloon.setOpacity(0.9999999f);
		balloon.setVisible(true);

		final int timeDelta = 1000/refreshRate;
		final Timer timer = new Timer(timeDelta, new ActionListener () {
			int curTime=0;
			public void actionPerformed(ActionEvent e) {
				curTime += timeDelta;
				float newOpacity = (-1.0f/time)*curTime+1.0f; // f(time)=(-1/time)*curTime+1
				if (newOpacity <= 0.0f || Float.isNaN(newOpacity)) {
					((Timer)e.getSource()).stop();
					balloon.setOpacity(0.0f);
					if (onStop != null) {
						onStop.actionPerformed(e);
					}
				} else {
					balloon.setOpacity(newOpacity);
				}
			}
		});
		timer.setRepeats(true);
		timer.start();
	}
}
