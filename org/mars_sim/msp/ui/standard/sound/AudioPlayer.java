/**
 * Mars Simulation Project
 * AudioPlayer.java
 * @version 2.78 2005-09-05
 * @author Dima Stepanchuk
 */

package org.mars_sim.msp.ui.standard.sound;

import java.io.*;
import javax.sound.sampled.*;

/**
 * A class to play sound files.
 */
public class AudioPlayer {

	// Data members
	private Clip clip; // The sound clip.
	private boolean mute; // Is the audio player muted?
	private float volume; // The volume of the audio player (0.0 to 1.0)
	
	public AudioPlayer() {
		clip = null;
		mute = false;
		volume = .5F;
	}
	
	/**
	 * Starts playing a sound clip
	 * @param filepath the file path to the sound clip.
	 * @param loop Should the sound clip be looped?
	 */
	private void startPlay(String filepath, boolean loop) {
		
		if ((filepath != null) && !filepath.equals("")) {
			try {
				File soundFile = new File(filepath);
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
				
				// Note: This block is used to be compatible with Java 1.4
				// In Java 1.5+ use clip=AudioSystem.getClip();
				// TODO: Change as soon as project migrates to 1.5
				// ###############################################
				AudioFormat format = new AudioFormat(
						AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED,
						16, 2, 4, AudioSystem.NOT_SPECIFIED, true);
				DataLine.Info info = new DataLine.Info(Clip.class, format);
				clip = (Clip) AudioSystem.getLine(info);
				// clip = AudioSystem.getClip();
				// ###############################################
				
				clip.open(audioInputStream);
				
				setVolume(volume);
				setMute(mute);
				
				if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
				else clip.loop(0);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Play a clip once.
	 * @param filepath the filepath to the sound file.
	 */
	public void play(String filepath) {
		this.startPlay(filepath, false);
	}
	
	/**
	 * Play the clip in a loop.
	 * @param filepath the filepath to the sound file.
	 */
	public void loop(String filepath) {
		this.startPlay(filepath, true);
	}

	/**
	 * Stops the playing clip.
	 */
	public void stop() {
		if (clip != null) clip.stop();
	}
	
	/**
	 * Gets the volume of the audio player.
	 * @return volume (0.0 to 1.0)
	 */
	public float getVolume() {
		return volume;
	}
	
	/**
	 * Sets the volume for the audio player.
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setVolume(float volume) {
		if ((volume < 0F) && (volume > 1F)) 
			throw new IllegalArgumentException("Volume invalid: " + volume);
		
		this.volume = volume;
		
		// Set volume
		if (clip != null) {
			// Note: No linear volume control for the clip, so use gain control.
			// Linear volume = pow(10.0, gainDB/20.0) 
			float gain = (float) Math.log10(volume) * 20F;
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(gain);
		}
	}
	
	/**
	 * Checks if the audio player is muted.
	 * @return true if muted.
	 */
	public boolean isMute() {
		return mute;
	}
	
	/**
	 * Sets if the audio player is mute or not.
	 * @param mute is audio player mute?
	 */
	public void setMute(boolean mute) {
		// Set mute value.
		this.mute = mute;
		
		if (clip != null) {
			BooleanControl muteControl = 
				(BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
			muteControl.setValue(mute);
		}
	}
}