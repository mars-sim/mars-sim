/**
 * Mars Simulation Project
 * AudioPlayer.java
 * @version 2.81 2007-08-19
 * @author Dima Stepanchuk
 * @author Sebastien Venot
 */

package org.mars_sim.msp.ui.standard.sound;



import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;

import org.mars_sim.msp.ui.standard.UIConfig;

/**
 * A class to play sound files.
 */
public class AudioPlayer implements LineListener {

	// Data members
	private Line currentLine; // The current compressed sound.
	private Clip currentClip; // The current sound clip.
	private boolean mute; // Is the audio player muted?
	private float volume; // The volume of the audio player (0.0 to 1.0)
	private ConcurrentHashMap < String, Clip > audioCache  
	= new ConcurrentHashMap<String, Clip>();
	private boolean looping = false;
	
	public AudioPlayer() {
	       currentClip = null;
	       currentLine = null;
	       
		if (UIConfig.INSTANCE.useUIDefault()) {
			setMute(false);
			setVolume(.5F);
		}
		else {
			setMute(UIConfig.INSTANCE.isMute());
			setVolume(UIConfig.INSTANCE.getVolume());
		}
	}
	
	/**
	 * Starts playing a sound (either compressed or not)
	 * @param filepath the file path to the sound .
	 * @param loop Should the sound clip be looped?
	 */
	private void startPlay(final String filepath, final boolean loop) {
	    
	    //if the sound is long(the whole UI get stuck, so we play
	    //the sound within his own thread
	    Thread sound_player = new Thread() {
	        public void run() {
	    	if ((filepath != null) && !filepath.equals("")) {
			   if (filepath.endsWith(SoundConstants.SND_FORMAT_WAV)) {
			       startPlayWavSound(filepath,loop);
			   } else if (filepath.endsWith(SoundConstants.SND_FORMAT_MP3)){
			       startPlayCompressedSound(filepath, loop);
			   } else if(filepath.endsWith(SoundConstants.SND_FORMAT_OGG)) {
			       startPlayCompressedSound(filepath, loop);    
			   }
			}
	        }
	    
	    };
	    sound_player.start();
		
	}
	
	public void startPlayWavSound(String filepath, boolean loop) {
	    try {
	        Clip clip = null;
	    	if (!audioCache.containsKey(filepath)) {
	    	    System.out.println(filepath);
	    	    File soundFile = new File(filepath);
	    	    AudioInputStream audioInputStream = 
	    		AudioSystem.getAudioInputStream(soundFile);
	    	    clip = AudioSystem.getClip();
	    	    clip.open(audioInputStream);
	    	    audioCache.put(filepath, clip);
	    	} else {
	    	    clip = audioCache.get(filepath);
	    	    clip.setFramePosition(0);  
	    	}
		
	    	currentClip = clip;
	    	currentClip.addLineListener(this);
		setVolume(volume);
		setMute(mute);
		
		if (loop){
		    clip.loop(Clip.LOOP_CONTINUOUSLY); 
		} else { 		   
		    clip.start();
		}
	} 
	catch (Exception e) {
		e.printStackTrace();
	}
	    
	}
	
	public void startPlayCompressedSound(String filepath, boolean loop) {
	 
		AudioInputStream din = null;
		looping = loop;
		
		do {
		    
		try {
			File file = new File(filepath);
			AudioInputStream in = AudioSystem.getAudioInputStream(file);
			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = 
			new AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED,
			baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
			baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
			false);
			
			din = AudioSystem.getAudioInputStream(decodedFormat, in);
			
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, 
							       decodedFormat);
			
			SourceDataLine line = 
			(SourceDataLine) AudioSystem.getLine(info);
		
			if(line != null) {
			    	currentLine = line;
				currentLine.addLineListener(this);
				line.open(decodedFormat);
				
				byte[] data = new byte[128];
				// Start
				line.start();
				int nBytesRead = 0;

            		       while ((nBytesRead = din.read(data, 0, data.length)) != -1) {
					line.write(data, 0, nBytesRead);
				}
				
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(din != null) {
				try { 
				    din.close(); 
				 } 
				catch(IOException e) { }
			}
		}
		} while (looping);
	    
	    
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
	    
	    	looping = false;
	    	
		if (currentClip != null) {
		    currentClip.stop();
		    currentClip = null;
		}
		
		if (currentLine != null) {
		    currentLine.close();
		    currentLine = null;
		}
		
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
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) 
	 * (0.0 to 1.0 valid range)
	 */
	public void setVolume(float volume) {
		if ((volume < 0F) && (volume > 1F)) 
			throw new IllegalArgumentException("Volume invalid: " 
							    + volume);
		
		this.volume = volume;
		
		// Set volume
		if (currentClip != null) {
			// Note: No linear volume control for the clip, 
		        //so use gain control.
			// Linear volume = pow(10.0, gainDB/20.0) 
			// Note Math.log10 is Java 1.5 or better.
			// float gainLog10 = (float) Math.log10(volume);
			float gainLog10 = (float) (Math.log(volume) 
						   / Math.log(10F));
			float gain = gainLog10 * 20F;
			FloatControl gainControl = 
			(FloatControl) currentClip.getControl(
					FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(gain);
		}
		
		if (currentLine != null) {
			// Note: No linear volume control for the clip, 
		        //so use gain control.
			// Linear volume = pow(10.0, gainDB/20.0) 
			// Note Math.log10 is Java 1.5 or better.
			// float gainLog10 = (float) Math.log10(volume);
			float gainLog10 = (float) (Math.log(volume) 
						   / Math.log(10F));
			float gain = gainLog10 * 20F;
			FloatControl gainControl = 
			(FloatControl) currentLine.getControl(
					FloatControl.Type.MASTER_GAIN);
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
		
		if (currentClip != null) {
			BooleanControl muteControl = 
				(BooleanControl) currentClip.getControl(
						 BooleanControl.Type.MUTE);
			muteControl.setValue(mute);
		}
		
		if (currentLine != null) {
			BooleanControl muteControl = 
				(BooleanControl) currentLine.getControl(
						 BooleanControl.Type.MUTE);
			muteControl.setValue(mute);
		}
	}

	/* 
	 * 
	 */
	public void update(LineEvent event) {
	   if (event.getType() == LineEvent.Type.STOP){
	       
	       if (currentClip != null) {
		    currentClip.stop();
		    currentClip = null;
		}
		
		if (currentLine != null) {
		    currentLine.close();
		    currentLine = null;
		}
		
	   }    
	}
}