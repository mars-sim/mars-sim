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

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

import org.mars_sim.msp.ui.swing.notification.Telegraph;
import org.mars_sim.msp.ui.swing.notification.TelegraphConfig;

/**
 * A simple Thread which allows to play a notification sound. It's called from
 * the {@link Telegraph#show()} object if the audio is enabled. Audio can be
 * enabled from the {@link TelegraphConfig}.
 * 
 * You can reuse this class easily for playing wav files. Just provide an
 * {@link AudioInputStream} for the object's construction, then call the
 * {@link #start()} method. It'll launch a new thread to play the sound.
 * 
 * @author Antoine Neveux
 * @version 2.1
 * @since 2.1
 * 
 */
public class TelegraphSound extends Thread {
	/**
	 * The {@link AudioInputStream} linked to the sound to play
	 */
	private final AudioInputStream audioInputStream;

	/**
	 * A custom listener in order to be notified at the end of the audio file
	 * playing
	 */
	private final AudioListener listener = new AudioListener();

	/**
	 * Constructor. You can create an {@link AudioInputStream} with:
	 * 
	 * {@link AudioSystem#getAudioInputStream(java.io.File)} ,
	 * {@link AudioSystem#getAudioInputStream(java.io.InputStream)},
	 * {@link AudioSystem#getAudioInputStream(java.net.URL)}
	 * 
	 * @param audioInputStream
	 *            The {@link AudioInputStream} linked to the sound to play. You
	 *            can create an {@link AudioInputStream} using the
	 *            {@link AudioSystem} object.
	 * 
	 */
	public TelegraphSound(final AudioInputStream audioInputStream) {
		super();
		this.audioInputStream = audioInputStream;
	}

	/**
	 * This method allows to actually play the sound provided from the
	 * {@link #audioInputStream}
	 * 
	 * @throws LineUnavailableException
	 *             if the {@link Clip} object can't be created
	 * @throws IOException
	 *             if the audio file can't be find
	 */
	protected void play() throws LineUnavailableException, IOException {
		final Clip clip = AudioSystem.getClip();
		clip.addLineListener(listener);
		clip.open(audioInputStream);
		try {
			clip.start();
			listener.waitUntilDone();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} finally {
			clip.close();
		}
		audioInputStream.close();
	}

	/**
	 * This method allows to play the sound while running the Thread
	 */
	@Override
	public void run() {
		try {
			play();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This Listener allows to notify when the play of the sound is ended
	 * 
	 * @author Antoine Neveux
	 * @version 2.1
	 * @since 2.1
	 * 
	 */
	class AudioListener implements LineListener {
		private boolean done = false;

		/**
		 * This method allows to be notified for each event while playing a
		 * sound
		 */
		@Override
		public synchronized void update(final LineEvent event) {
			final Type eventType = event.getType();
			if (eventType == Type.STOP || eventType == Type.CLOSE) {
				done = true;
				notifyAll();
			}
		}

		/**
		 * This method allows to wait until a sound is completly played
		 * 
		 * @throws InterruptedException
		 *             as we work with thread, this exception can occur
		 */
		public synchronized void waitUntilDone() throws InterruptedException {
			while (!done)
				wait();
		}
	}
}
