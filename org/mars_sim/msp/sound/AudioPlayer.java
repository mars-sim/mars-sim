/**
 * Mars Simulation Project
 * AudioPlayer.java
 * @version 
 * @author Dima Stepanchuk
 */

package org.mars_sim.msp.sound;

import java.io.*;

import javax.sound.sampled.*;

public class AudioPlayer {
	private static final int EXTERNAL_BUFFER_SIZE = 128000;
	private AudioInputStream audioInputStream = null;
	private File soundFile;
	private Clip clip;

	public AudioPlayer()
	{			
	}
	private void startPlay(String path, boolean loop) {
		 

		try {
			soundFile= new File(path);
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			
		
			
			if (loop) {
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			} else {
				clip.loop(1);
			}
		} catch (Exception e) {
			e.printStackTrace();

		}

	}
	public void play(String path)
	{
		this.startPlay(path, false);
	}
	public void loop(String path)
	{
		this.startPlay(path, true);
	}

	public void stop() {
		if (clip != null) {
			clip.stop();
		}
	}
}

