/**
 * Mars Simulation Project
 * AudioPlayer.java
 * @version 
 * @author Dima Stepanchuk
 */

package org.mars_sim.msp.ui.standard.sound;

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
		 

		try
        {
            soundFile = new File(path);
            audioInputStream = AudioSystem.getAudioInputStream(soundFile);
          // This block is used to be compatible with Java 1.4
          // In Java 1.5+ use clip=AudioSystem.getClip();
          //TODO: Change as soon as project migrates to 1.5
            AudioFormat format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED,
                    16, 2, 4, AudioSystem.NOT_SPECIFIED, true);
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip= (Clip) AudioSystem.getLine(info);
            // end of block
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

