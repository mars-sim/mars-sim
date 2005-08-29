/**
 * Mars Simulation Project
 * AudioPlayer.java
 * @version 2.78 2005-08-28
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
}