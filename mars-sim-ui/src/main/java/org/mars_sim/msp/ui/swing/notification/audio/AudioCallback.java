/*
 *   JTelegraph -- a Java message notification library
 *   Copyright (c) 2012, Paulo Roberto Massa Cereda
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions
 *   are met:
 *
 *   1. Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *   3. Neither the name of the project's author nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *   COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *   BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 *   WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGE.
 */
package org.mars_sim.msp.ui.swing.notification.audio;

import org.mars_sim.msp.ui.swing.notification.Telegraph;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.callback.TimelineCallback;

/**
 * This simple callback allows to play a sound notification before calling
 * another timeline
 * 
 * @author Antoine Neveux
 * @version 2.1
 * @since 2.1
 * 
 */
public class AudioCallback implements TimelineCallback {
	/**
	 * The current {@link Telegraph} object
	 */
	private final Telegraph telegraph;
	/**
	 * The next {@link Timeline} to be called
	 */
	private final Timeline nextTimeline;

	/**
	 * Constructor.
	 * 
	 * @param telegraph
	 *            {@link #telegraph}
	 * @param nextTimeline
	 *            {@link #nextTimeline}
	 */
	public AudioCallback(final Telegraph telegraph, final Timeline nextTimeline) {
		this.telegraph = telegraph;
		this.nextTimeline = nextTimeline;
	}

	/**
	 * @see TimelineCallback#onTimelineStateChanged(TimelineState,
	 *      TimelineState, float, float)
	 */
	@Override
	public void onTimelineStateChanged(final TimelineState oldState,
			final TimelineState newState, final float durationFraction,
			final float timelinePosition) {
		if (newState == Timeline.TimelineState.DONE)
			nextTimeline.play();
	}

	/**
	 * @see TimelineCallback#onTimelinePulse(float, float)
	 */
	@Override
	public void onTimelinePulse(final float durationFraction,
			final float timelinePosition) {
		if (telegraph.getConfig().isAudioEnabled()) {
			final TelegraphSound sound = new TelegraphSound(telegraph
					.getConfig().getAudioInputStream());
			sound.start();
		}
	}
}