/**
 * Mars Simulation Project
 * AudioPlayer.java
 * @version 3.08 2016-03-31
 * @author Lars Naesbye Christensen (complete rewrite for OGG)
 */

package org.mars_sim.msp.ui.swing.sound;

import java.io.IOException;

import org.mars_sim.msp.ui.swing.UIConfig;

/**
 * A class to dispatch playback of OGG files to OGGSoundClip.
 */
public class AudioPlayer {

	/** The current clip sound. */
	private OGGSoundClip currentOGGSoundClip;

	/** The volume of the audio player (0.0 to 1.0) */
	private float volume = .5F;

	public AudioPlayer() {
		currentOGGSoundClip = null;

		if (UIConfig.INSTANCE.useUIDefault()) {
			setMute(false);
			setVolume(.5F);
		} else {
			setMute(UIConfig.INSTANCE.isMute());
			setVolume(UIConfig.INSTANCE.getVolume());
		}
	}

	/**
	 * Play a clip once.
	 * 
	 * @param filepath
	 *            the file path to the sound file.
	 */
	public void play(String filepath) {
		try {
			currentOGGSoundClip = new OGGSoundClip(filepath);
			currentOGGSoundClip.play();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Play the clip in a loop.
	 * 
	 * @param filepath
	 *            the filepath to the sound file.
	 */
	public void loop(String filepath) {
		try {
			currentOGGSoundClip = new OGGSoundClip(filepath);
			currentOGGSoundClip.loop();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Stops the playing clip.
	 */
	public void stop() {
		if (currentOGGSoundClip != null) {
			currentOGGSoundClip.stop();
			currentOGGSoundClip = null;
		}

	}

	/**
	 * Gets the volume of the audio player.
	 * 
	 * @return volume (0.0 to 1.0)
	 */
	public float getVolume() {
		return volume;
	}

	/**
	 * Sets the volume of the audio player.
	 * 
	 * @param volume
	 *            (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setVolume(float volume) {
		if (volume < 0F)
			volume = 0F;
		if (volume > 1F)
			volume = 1F;

		this.volume = volume;
		if (currentOGGSoundClip != null) {
			currentOGGSoundClip.setGain(volume);
		}
	}

	/**
	 * Checks if the audio player is muted.
	 * 
	 * @return true if muted.
	 */
	public boolean isMute() {
		return currentOGGSoundClip.isMute();
	}

	/**
	 * Sets if the audio player is mute or not.
	 * 
	 * @param mute
	 *            is audio player mute?
	 */
	public void setMute(boolean mute) {
		if (currentOGGSoundClip != null) {
			currentOGGSoundClip.setMute(mute);
		}

	}

	public void cleanAudioPlayer() {
		stop();
	}

}