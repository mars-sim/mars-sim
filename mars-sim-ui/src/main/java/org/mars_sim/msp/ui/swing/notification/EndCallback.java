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
package org.mars_sim.msp.ui.swing.notification;

import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.callback.TimelineCallback;

/**
 * Implements an end callback for Timeline.
 * 
 * @author Paulo Roberto Massa Cereda
 * @version 2.1
 * @since 2.0
 */
public class EndCallback implements TimelineCallback {

	/**
	 * The TelegraphWindow to use
	 */
	private final TelegraphWindow telegraph;

	/**
	 * Constructor.
	 * 
	 * @param telegraph
	 *            The telegraph window.
	 */
	public EndCallback(final TelegraphWindow telegraph) {
		this.telegraph = telegraph;
	}

	/**
	 * @see TimelineCallback#onTimelineStateChanged(org.pushingpixels.trident.Timeline.TimelineState,
	 *      org.pushingpixels.trident.Timeline.TimelineState, float, float)
	 */
	@Override
	public void onTimelineStateChanged(final Timeline.TimelineState ts,
			final Timeline.TimelineState ts1, final float f, final float f1) {
		// If the timeline's down, then we dispose the telegraph window
		if (ts1 == Timeline.TimelineState.DONE)
			telegraph.dispose();
	}

	/**
	 * @see TimelineCallback#onTimelinePulse(float, float)
	 */
	@Override
	public void onTimelinePulse(final float f, final float f1) {
	}
}