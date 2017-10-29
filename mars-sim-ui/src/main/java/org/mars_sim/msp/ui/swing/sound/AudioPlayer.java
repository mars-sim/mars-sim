/**
 * Mars Simulation Project
 * AudioPlayer.java
 * @version 3.1.0 2017-10-28
 * @author Lars Naesbye Christensen (complete rewrite for OGG)
 */

package org.mars_sim.msp.ui.swing.sound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

//import javafx.application.Platform;

/**
 * A class to dispatch playback of OGG files to OGGSoundClip.
 */
@SuppressWarnings("restriction")
public class AudioPlayer {

	private static Logger logger = Logger.getLogger(AudioPlayer.class.getName());

	private final static int LOUD_TRACKS = 6;
	private final static int REPEATING_TRACKS = 4; // track the last 4 tracks and avoid playing them repetitively.
	
	private static int num_tracks;
	
	/** The volume of the audio player (0.0 to 1.0) */
	private float musicVol = .8f;
	private float effectVol = .8f;

	private int play_times = 0;

	private boolean hasMasterGain = true;
	
	private boolean lastMusicState;
	private boolean lastEffectState;

	private MainDesktopPane desktop;
	private MainScene mainScene;
	
	/** The current clip sound. */
	private static OGGSoundClip currentOGGSoundClip;
	private static OGGSoundClip currentBackgroundTrack;

	private static Map<String, OGGSoundClip> allBackgroundSoundTracks;
	private static Map<String, OGGSoundClip> allOGGSoundClips;

	private static List<String> soundEffects;
	private static List<String> soundTracks;
	private static List<Integer> previous_tracks = new ArrayList<>();
	

	public AudioPlayer(MainDesktopPane desktop) {
		//logger.info("constructor is on " + Thread.currentThread().getName());
		this.desktop = desktop;
		mainScene = desktop.getMainScene();

		currentOGGSoundClip = null;
		currentBackgroundTrack = null;

		allBackgroundSoundTracks = new HashMap<>();
		allOGGSoundClips = new HashMap<>();
		
		soundTracks = new ArrayList<>();
		soundTracks.add(SoundConstants.ST_AREOLOGIE);
		soundTracks.add(SoundConstants.ST_PUZZLE);
		soundTracks.add(SoundConstants.ST_MISTY);
		soundTracks.add(SoundConstants.ST_MENU);
			
		soundTracks.add(SoundConstants.ST_ONE_WORLD);
		soundTracks.add(SoundConstants.ST_BEDTIME);
		soundTracks.add(SoundConstants.ST_BOG_CREATURES);
		soundTracks.add(SoundConstants.ST_LOST_JUNGLE);
		
		// not for playing at the start of the sim due to its loudness
		// Set LOUD_TRACKS to 5
		soundTracks.add(SoundConstants.ST_MOONLIGHT);
		soundTracks.add(SoundConstants.ST_CITY);
		soundTracks.add(SoundConstants.ST_CLIPPITY);
		soundTracks.add(SoundConstants.ST_MONKEY);
		soundTracks.add(SoundConstants.ST_SURREAL);
		soundTracks.add(SoundConstants.ST_FANTASCAPE);

		num_tracks = soundTracks.size();
		
		for (String p : soundTracks) {
			try {
				allBackgroundSoundTracks.put(p, new OGGSoundClip(p));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		soundEffects = new ArrayList<>();
		soundEffects.add(SoundConstants.SND_EQUIPMENT);
		soundEffects.add(SoundConstants.SND_PERSON_DEAD);
		soundEffects.add(SoundConstants.SND_PERSON_FEMALE1);
		soundEffects.add(SoundConstants.SND_PERSON_FEMALE2);
		soundEffects.add(SoundConstants.SND_PERSON_MALE1);
		soundEffects.add(SoundConstants.SND_PERSON_MALE2);

		soundEffects.add(SoundConstants.SND_ROVER_MAINTENANCE);
		soundEffects.add(SoundConstants.SND_ROVER_MALFUNCTION);
		soundEffects.add(SoundConstants.SND_ROVER_MOVING);
		soundEffects.add(SoundConstants.SND_ROVER_PARKED);
		soundEffects.add(SoundConstants.SND_SETTLEMENT);
		
		for (String s : soundEffects) {
			try {
				allOGGSoundClips.put(s, new OGGSoundClip(s));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//if (UIConfig.INSTANCE.useUIDefault()) {
			musicVol = mainScene.getMusic()/100f;
			effectVol = mainScene.getSoundEffect()/100f;
		//}
	}

	/**
	 * Play a clip once.
	 * @param filepath the file path to the sound file.
	 */
	@SuppressWarnings("restriction")
	public void playSound(String filepath) {
		//logger.info("play() is on " + Thread.currentThread().getName());
		if (!isEffectMute()) {
			if (desktop.getMainScene() != null) {
					//Platform.runLater(() -> {
						try {
							if (allOGGSoundClips.containsKey(filepath) && allOGGSoundClips.get(filepath) != null) {
								currentOGGSoundClip = allOGGSoundClips.get(filepath);
								currentOGGSoundClip.play();

							}
							else {
								currentOGGSoundClip = new OGGSoundClip(filepath);
								allOGGSoundClips.put(filepath, currentOGGSoundClip);
								currentOGGSoundClip.play();
							}
							
							currentOGGSoundClip.determineGain(effectVol);
							
						} catch (IOException e) {
							//e.printStackTrace();
							logger.log(Level.SEVERE, "IOException in AudioPlayer's play()", e.getMessage());
						}
					//});
			}

			else {
				SwingUtilities.invokeLater(() -> {
						try {
							if (allOGGSoundClips.containsKey(filepath) && allOGGSoundClips.get(filepath) != null) {
								currentOGGSoundClip = allOGGSoundClips.get(filepath);
								currentOGGSoundClip.play();
							}
							else {
								currentOGGSoundClip = new OGGSoundClip(filepath);
								allOGGSoundClips.put(filepath, currentOGGSoundClip);
								currentOGGSoundClip.play();
							}
							
							currentOGGSoundClip.determineGain(effectVol);
							
						} catch (IOException e) {
							//e.printStackTrace();
							logger.log(Level.SEVERE, "IOException in AudioPlayer's play()", e.getMessage());

						}
				});
			}
		}
	}

	/**
	 * Play a clip once.
	 * @param filepath  the file path to the sound file.
	 */
	@SuppressWarnings("restriction")
	public void playBackground(String filepath) {
		//logger.info("play() is on " + Thread.currentThread().getName());
		if (!isMusicMute()) {
			if (desktop.getMainScene() != null) {
					//Platform.runLater(() -> {
						try {
							if (allBackgroundSoundTracks.containsKey(filepath) && allBackgroundSoundTracks.get(filepath) != null) {
								currentBackgroundTrack = allBackgroundSoundTracks.get(filepath);
								currentBackgroundTrack.loop();
							}
							else {
								currentBackgroundTrack = new OGGSoundClip(filepath);
								allBackgroundSoundTracks.put(filepath, currentBackgroundTrack);
								currentBackgroundTrack.loop();
							}
							
							currentBackgroundTrack.determineGain(musicVol);
							
						} catch (IOException e) {
							//e.printStackTrace();
							logger.log(Level.SEVERE, "IOException in AudioPlayer's playInBackground()", e.getMessage());
						}
					//});
			}

			else {
				SwingUtilities.invokeLater(() -> {
					try {
						if (allBackgroundSoundTracks.containsKey(filepath) && allBackgroundSoundTracks.get(filepath) != null) {
							currentBackgroundTrack = allBackgroundSoundTracks.get(filepath);
							currentBackgroundTrack.loop();
						}
						else {
							currentBackgroundTrack = new OGGSoundClip(filepath);
							allBackgroundSoundTracks.put(filepath, currentBackgroundTrack);
							currentBackgroundTrack.loop();
						}
						
						currentBackgroundTrack.determineGain(musicVol);

						
					} catch (IOException e) {
						//e.printStackTrace();
						logger.log(Level.SEVERE, "IOException in AudioPlayer's playInBackground()", e.getMessage());
					}
				});
			}
		}
	}

	
	/**
	 * Gets the volume of the background music.
	 * @return volume (0.0 to 1.0)
	 */
	public float getMusicGain() {
		return musicVol;
	}

	/**
	 * Gets the volume of the sound effect.
	 * @return volume (0.0 to 1.0)
	 */
	public float getEffectGain() {
		return effectVol;
	}
	
	public void musicVolumeUp() {
		float v = currentBackgroundTrack.getVol() + .05f;
		if (v > 1f)
			v = 1f;
		if (hasMasterGain && currentBackgroundTrack != null)
			currentBackgroundTrack.determineGain(v);	
		setLastMusicVolume();
	}

	public void musicVolumeDown() {
		float v = currentBackgroundTrack.getVol() - .05f;
		if (v < -1f)
			v = -1f;
		if (hasMasterGain && currentBackgroundTrack != null)
			currentBackgroundTrack.determineGain(v);
		setLastMusicVolume();
	}

	public void effectVolumeUp() {
		float v = currentOGGSoundClip.getVol() + .05f;
		if (v > 1f)
			v = 1f;
		if (hasMasterGain && currentOGGSoundClip != null)
			currentOGGSoundClip.determineGain(v);
		setLastEffectVolume();
	}

	public void effectVolumeDown() {
		float volume = currentOGGSoundClip.getVol() - .05f;
		if (volume < -1f)
			volume = -1f;
		if (hasMasterGain && currentOGGSoundClip != null)
			currentOGGSoundClip.determineGain(volume);
		setLastEffectVolume();
	}
	

	public void setLastMusicVolume() {
		musicVol = currentBackgroundTrack.getVol();
	}


	public void setLastEffectVolume() {
		effectVol = currentOGGSoundClip.getVol();
	}

	/**
	 * Sets the volume of the audio player.
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setMusicVolume(float volume) {
		//logger.info("setVolume() is on " + Thread.currentThread().getName());
		if (volume < 0F)
			volume = 0F;
		if (volume > 1F)
			volume = 1F;
		this.musicVol = volume;
		
		if (hasMasterGain && currentBackgroundTrack != null && !currentBackgroundTrack.isMute()){ //&& !isMusicMute()
			currentBackgroundTrack.determineGain(volume);
		}
	}

	/**
	 * Sets the volume of the audio player.
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setEffectVolume(float volume) {
		//logger.info("setVolume() is on " + Thread.currentThread().getName());
		if (volume < 0F)
			volume = 0F;
		if (volume > 1F)
			volume = 1F;
		this.effectVol = volume;

		if (hasMasterGain && currentOGGSoundClip != null && !currentOGGSoundClip.isMute()) { // && !isEffectMute()
			currentOGGSoundClip.determineGain(volume);
		}
	}

	/**
	 * Checks if the audio player is muted.
	 * @param isEffect is the sound effect mute ?
	 * @param isTrack is the background music mute ?
	 * @return true if mute.
	 */
	public boolean isMusicMute() {
		boolean result = false;
		if (currentBackgroundTrack != null) {
			result = currentBackgroundTrack.isMute() || currentBackgroundTrack.getVol() == 0 || musicVol == 0;
		}
		
		return result;
	}
	
	
	/**
	 * Checks if the audio player is muted.
	 * @param isEffect is the sound effect mute ?
	 * @param isTrack is the background music mute ?
	 * @return true if mute.
	 */
	public boolean isEffectMute() {
		boolean result = false;
		if (currentOGGSoundClip != null) {
			result = currentOGGSoundClip.isMute() || currentOGGSoundClip.getVol() == 0 || effectVol == 0;
		}

		return result;
	}

	public void restoreSound(boolean isEffect, boolean isTrack) {
		if (isEffect && currentOGGSoundClip != null) {
			//logger.info("restoreSound() lastMusicState:"+ lastMusicState);
			currentOGGSoundClip.setMute(lastEffectState);
			setLastEffectVolume();
		}
		
		if (isTrack && currentBackgroundTrack != null) {
			//logger.info("restoreSound() lastEffectState:"+ lastEffectState);
			currentBackgroundTrack.setMute(lastMusicState);
			setLastMusicVolume();
		}
	}
		
	

	public void pauseSound(boolean isEffect, boolean isTrack) {
		if (currentBackgroundTrack != null && !currentBackgroundTrack.isMute())
			lastMusicState = currentBackgroundTrack.isMute();
		//logger.info("pauseSound() lastMusicState:"+ lastMusicState);
		if (currentOGGSoundClip != null && !currentOGGSoundClip.isMute())
			lastEffectState = currentOGGSoundClip.isMute();
		//logger.info("pauseSound() lastEffectState:"+ lastEffectState);		
		mute(isEffect, isTrack);
	}
	
	/**
	 * Sets the state of the audio player to mute or unmute.
	 * @param mute true if it will be set to mute
	 */
	public void mute(boolean effectMute, boolean trackMute) {
		if (effectMute && currentOGGSoundClip != null) {
			currentOGGSoundClip.setMute(true);
			currentOGGSoundClip.determineGain(0);
		}
		
		if (trackMute && currentBackgroundTrack != null) {
			currentBackgroundTrack.setMute(true);
			currentBackgroundTrack.determineGain(0);
		}
	}

	public void unmute(boolean effectMute, boolean trackMute) {
		if (effectMute && currentOGGSoundClip != null) {
			currentOGGSoundClip.setMute(false);
			setLastMusicVolume();
		}
		
		if (trackMute && currentBackgroundTrack != null) {
			currentBackgroundTrack.setMute(false);
			setLastEffectVolume();
		}
	}
	
	public void enableMasterGain(boolean value) {
		hasMasterGain = value;
	}

	/**
	 * Checks if the music track ever started or has stopped 
	 * @return true if no music track is playing
	 */
	public boolean isBackgroundTrackStopped() {
		if (currentBackgroundTrack == null)
			return true;
		return currentBackgroundTrack.checkState();
	}
	
	/**
	 * Picks a new music track to play
	 */
	public void pickANewTrack() {
		int rand = 0;
		// At the start of the sim, refrain from playing the last few tracks due to their sudden loudness
		if (previous_tracks.isEmpty()) {
			rand = RandomUtil.getRandomInt(num_tracks - LOUD_TRACKS - 1);
		}
		else
			rand = RandomUtil.getRandomInt(num_tracks - 1);
		boolean not_old = false;
		// Do not repeat the last 4 music tracks just played
		while (!not_old) {
			if (previous_tracks.isEmpty())
				not_old = true;
			else if (!previous_tracks.contains(rand))
				not_old = true;
		
			if (not_old) {
				String name = soundTracks.get(rand);
				playBackground(soundTracks.get(rand));
				logger.info("Playing background music track #" + (rand+1) + " '" + name + "'");
				// Add the new track
				if (!previous_tracks.contains(rand))
					previous_tracks.add((rand));
				// Remove the earliest track 
				if (previous_tracks.size() > REPEATING_TRACKS)
					previous_tracks.remove(0);
				// Reset the play times to 1 for this new track
				play_times = 1;
				break;
			}
			else
				// need to pick another track and run while loop again
				rand = RandomUtil.getRandomInt(num_tracks - LOUD_TRACKS - 1);
		}
		
	}
	
	/**
	 * Play a randomly selected music track
	 */
	public void playRandomBackgroundTrack() {
		if (isBackgroundTrackStopped()) {
			// Since Areologie.ogg and Fantascape.ogg are 4 mins long, don't need to replay them
			if (currentBackgroundTrack != null
					&& currentBackgroundTrack.toString().equals(SoundConstants.ST_AREOLOGIE) 
					&& play_times < 2) {
				pickANewTrack();
			}
			else if (currentBackgroundTrack != null
					&& currentBackgroundTrack.toString().equals(SoundConstants.ST_FANTASCAPE) 
					&& play_times < 2)	{
				pickANewTrack();
			}
			else if (currentBackgroundTrack != null
					&& !currentBackgroundTrack.isMute() && currentBackgroundTrack.getVol() != 0
					&& play_times < 4) {
				playBackground(currentBackgroundTrack.toString());
				play_times++;
			}
			else {
				pickANewTrack();
			}
		}
	}
	
	public void destroy() {
		allOGGSoundClips = null;
		allBackgroundSoundTracks = null;
		desktop = null;
		currentOGGSoundClip = null;
		currentBackgroundTrack = null;
		soundTracks = null;
		previous_tracks = null;
	}
	
}