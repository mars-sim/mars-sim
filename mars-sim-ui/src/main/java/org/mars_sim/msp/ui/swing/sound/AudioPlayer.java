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
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.javafx.mainmenu.MainMenu;
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
	
	public final static float DEFAULT_VOL = .5f;
	
	private static int num_tracks;
	
	/** The volume of the audio player (0.0 to 1.0) */
	private float currentMusicVol = DEFAULT_VOL;
	private float currentSoundVol = DEFAULT_VOL;

	private float lastMusicVol = 0;
	private float lastSoundVol = 0;

	private int play_times = 0;

	private boolean hasMasterGain = true;
	private boolean isSoundDisabled = false;
	
	//private boolean lastMusicState = true;
	//private boolean lastSoundState = true;

	private MainDesktopPane desktop;
	//private MainScene mainScene;
	
	/** The current clip sound. */
	private static OGGSoundClip currentSoundClip;
	private static OGGSoundClip currentMusicTrack;

	private static Map<String, OGGSoundClip> allMusicTracks;
	private static Map<String, OGGSoundClip> allSoundClips;

	private static List<String> soundEffects;
	private static List<String> soundTracks;
	private static List<Integer> played_tracks = new ArrayList<>();
	
	private static MasterClock masterClock;

	public AudioPlayer(MainDesktopPane desktop) {
		//logger.info("constructor is on " + Thread.currentThread().getName());
		this.desktop = desktop;
		//mainScene = desktop.getMainScene();

		masterClock = Simulation.instance().getMasterClock();
		
		if (MainMenu.isSoundDisabled()) {
			isSoundDisabled = true;
			currentMusicVol = 0;
			currentSoundVol = 0;
		}
		
		else {
			
			allMusicTracks = new HashMap<>();
			allSoundClips = new HashMap<>();
			
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
					allMusicTracks.put(p, new OGGSoundClip(p));
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
					allSoundClips.put(s, new OGGSoundClip(s));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			currentSoundClip = allSoundClips.get(SoundConstants.SND_PERSON_FEMALE1);
			currentMusicTrack = null;

		}
		//if (UIConfig.INSTANCE.useUIDefault()) {
		//}
	}

	/**
	 * Play a sound clip.
	 * @param filepath the file path to the sound file.
	 */
	public void playSound(String filepath) {
		if (!isSoundMute()) {
			if (desktop.getMainScene() != null)
				//Platform.runLater(() -> {
					loadSound(filepath);
				//});
			else
				SwingUtilities.invokeLater(() -> loadSound(filepath));
		}
	}

	/**
	 * Loads up a sound clip.
	 * @param filepath the file path to the music track.
	 */
	public void loadSound(String filepath) {
		if (allSoundClips.containsKey(filepath) && allSoundClips.get(filepath) != null) {
			currentSoundClip = allSoundClips.get(filepath);
			currentSoundClip.determineGain(currentSoundVol);
			currentSoundClip.play();
		}
		else {
			try {
				currentSoundClip = new OGGSoundClip(filepath);
			} catch (IOException e) {
				//e.printStackTrace();
				logger.log(Level.SEVERE, "IOException in AudioPlayer's playSound()", e.getMessage());
			}
			allSoundClips.put(filepath, currentSoundClip);
			currentSoundClip.determineGain(currentSoundVol);
			currentSoundClip.play();
		}
	}
	
	/**
	 * Plays a music track.
	 * @param filepath the file path to the music track.
	 */
	public void playMusic(String filepath) {
		//logger.info("play() is on " + Thread.currentThread().getName());
		if (!isMusicMute()) {
			if (desktop.getMainScene() != null)
				//Platform.runLater(() -> { 
					loadMusic(filepath);
				//});
			else
				SwingUtilities.invokeLater(() -> loadMusic(filepath));
		}
	}

	/**
	 * Loads up the music track.
	 * @param filepath the file path to the music track.
	 */
	public void loadMusic(String filepath) {
		if (allMusicTracks.containsKey(filepath) && allMusicTracks.get(filepath) != null) {
			currentMusicTrack = allMusicTracks.get(filepath);
			currentMusicTrack.determineGain(currentMusicVol);
			currentMusicTrack.loop();
		}
		else {
			try {
				currentMusicTrack = new OGGSoundClip(filepath);
			} catch (IOException e) {
				//e.printStackTrace();
				logger.log(Level.SEVERE, "IOException in AudioPlayer's playInBackground()", e.getMessage());
			}
			allMusicTracks.put(filepath, currentMusicTrack);
			currentMusicTrack.determineGain(currentMusicVol);
			currentMusicTrack.loop();
		}
	}
	
	/**
	 * Gets the volume of the background music.
	 * @return volume (0.0 to 1.0)
	 */
	public float getMusicVolume() {
		return currentMusicVol;
	}

	/**
	 * Gets the volume of the sound effect.
	 * @return volume (0.0 to 1.0)
	 */
	public float getEffectVolume() {
		return currentSoundVol;
	}
	
	public void musicVolumeUp() {
		lastMusicVol = currentMusicVol;
		float v = currentMusicTrack.getVol() + .05f;
		if (v > 1f)
			v = 1f;
		if (!isSoundDisabled && hasMasterGain && currentMusicTrack != null)
			currentMusicTrack.determineGain(v);	
	}

	public void musicVolumeDown() {
		lastMusicVol = currentMusicVol;
		float v = currentMusicTrack.getVol() - .05f;
		if (v < 0f)
			v = 0;
		if (!isSoundDisabled && hasMasterGain && currentMusicTrack != null)
			currentMusicTrack.determineGain(v);

	}

	public void soundVolumeUp() {
		lastSoundVol = currentSoundVol;
		float v = currentSoundClip.getVol() + .05f;
		if (v > 1f)
			v = 1f;
		if (!isSoundDisabled && hasMasterGain && currentSoundClip != null)
			currentSoundClip.determineGain(v);

	}

	public void soundVolumeDown() {
		lastSoundVol = currentSoundVol;
		float v = currentSoundClip.getVol() - .05f;
		if (v < 0f)
			v = 0;
		if (!isSoundDisabled && hasMasterGain && currentSoundClip != null)
			currentSoundClip.determineGain(v);

	}
	

	public void restoreLastMusicVolume() {
		if (!isSoundDisabled && hasMasterGain && currentMusicTrack != null) {
			//currentMusicTrack.resume();
			currentMusicTrack.determineGain(lastMusicVol);
		}
	}


	public void restoreLastSoundVolume() {
		if (!isSoundDisabled && hasMasterGain && currentSoundClip != null)
			currentSoundClip.determineGain(lastSoundVol);
	}

	/**
	 * Sets the volume of the audio player.
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setMusicVolume(float volume) {
		//logger.info("setVolume() is on " + Thread.currentThread().getName());
		if (volume < 0F)
			volume = 0;
		if (volume > 1F)
			volume = 1F;
		lastMusicVol = currentMusicVol;
		currentMusicVol = volume;
		
		if (!isSoundDisabled && hasMasterGain && currentMusicTrack != null) {// && !currentMusicTrack.isMute()){ 
			currentMusicTrack.determineGain(volume);
		}
	}

	/**
	 * Sets the volume of the audio player.
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setSoundVolume(float volume) {
		//logger.info("setVolume() is on " + Thread.currentThread().getName());
		if (volume < 0F)
			volume = 0;
		if (volume > 1F)
			volume = 1F;
		lastSoundVol = currentSoundVol;
		currentSoundVol = volume;

		if (!isSoundDisabled && hasMasterGain && currentSoundClip != null) {// && !currentSoundClip.isMute()) {
			currentSoundClip.determineGain(volume);
		}
	}

	/**
	 * Checks if the audio player is muted.
	 * @param isEffect is the sound effect mute ?
	 * @param isTrack is the background music mute ?
	 * @return true if mute.
	 */
	public boolean isMusicMute() {
		//if (currentMusicTrack != null) {
		//	result = currentMusicTrack.isMute() || currentMusicTrack.getVol() == 0;
		//}	
		return currentMusicVol == 0;
	}
	
	
	/**
	 * Checks if the audio player is muted.
	 * @param isEffect is the sound effect mute ?
	 * @param isTrack is the background music mute ?
	 * @return true if mute.
	 */
	public boolean isSoundMute() {
		//if (currentSoundClip != null) {
		//	result = currentSoundClip.isMute() || currentSoundClip.getVol() == 0;
		//}
		return currentSoundVol == 0;
	}
/*
	public void restore(boolean isSound, boolean isMusic) {
		if (isSound)  {
			if (currentSoundClip != null) 
				currentSoundClip.setMute(lastSoundState);
			restoreLastSoundVolume();
		}
		
		if (isMusic) {
			if (currentMusicTrack != null) {
				currentMusicTrack.setMute(lastMusicState);
			}
			restoreLastMusicVolume();
		}
	}
*/	
	public void unmute(boolean isSound, boolean isMusic) {
		if (isSound) {
			if (currentSoundClip != null) 
				currentSoundClip.setMute(false);
			restoreLastSoundVolume();
		}
		
		if (isMusic) {
			if (currentMusicTrack != null)
				currentMusicTrack.setMute(false);
			restoreLastMusicVolume();
		}
	}
	
/*
	public void pause(boolean isSound, boolean isMusic) {
		if (currentMusicTrack != null)// && !currentMusicTrack.isMute())
			lastMusicState = currentMusicTrack.isMute();
		if (currentSoundClip != null)// && !currentSoundClip.isMute())
			lastSoundState = currentSoundClip.isMute();		
		mute(isSound, isMusic);
	}
*/
	/**
	 * Sets the state of the audio player to mute or unmute.
	 * @param mute true if it will be set to mute
	 */
	public void mute(boolean isSound, boolean isMusic) {
		if (isSound) {
			if (currentSoundClip != null) {
				currentSoundClip.setMute(true);
				currentSoundClip.determineGain(0);
			}
			lastSoundVol = currentSoundVol;
			currentSoundVol = 0;
		}
		
		if (isMusic) {
			if (currentMusicTrack != null) {
				currentMusicTrack.setMute(true);
				currentMusicTrack.determineGain(0);
			}
			lastMusicVol = currentMusicVol;
			currentMusicVol = 0;
		}
	}


	public void enableMasterGain(boolean value) {
		hasMasterGain = value;
	}

	/**
	 * Checks if the music track ever started or has stopped 
	 * @return true if no music track is playing
	 */
	public boolean isMusicTrackStopped() {
		if (currentMusicTrack == null)
			return true;
		return currentMusicTrack.checkState();
	}
	
	/**
	 * Picks a new music track to play
	 */
	public void pickANewTrack() {
		int rand = 0;
		// At the start of the sim, refrain from playing the last few tracks due to their sudden loudness
		if (played_tracks.isEmpty())
			// Do not repeat the last 4 music tracks just played
			rand = RandomUtil.getRandomInt(num_tracks - LOUD_TRACKS - 1);
		else
			rand = RandomUtil.getRandomInt(num_tracks - 1);
		boolean isNewTrack = false;
		// Do not repeat the last 4 music tracks just played
		while (!isNewTrack) {
			
			if (!played_tracks.contains(rand)) {
				isNewTrack = true;
		
				String name = soundTracks.get(rand);
				playMusic(name);
				logger.info("Playing background music track #" + (rand+1) + " '" + name + "'");
				// Add the new track
				played_tracks.add((rand));
				// Remove the earliest track 
				if (played_tracks.size() > REPEATING_TRACKS)
					played_tracks.remove(0);
				// Reset the play times to 1 for this new track
				play_times = 1;
				//break;
			}
			else
				rand = RandomUtil.getRandomInt(num_tracks - 1);
		}
		
	}
	
	/**
	 * Play a randomly selected music track
	 */
	public void playRandomMusicTrack() {
		if (isMusicTrackStopped() && !masterClock.isPaused()) {
			// Since Areologie.ogg and Fantascape.ogg are 4 mins long, don't need to replay them
			if (currentMusicTrack != null
					&& currentMusicTrack.toString().equals(SoundConstants.ST_AREOLOGIE) 
					&& play_times < 2) {
				pickANewTrack();
			}
			else if (currentMusicTrack != null
					&& currentMusicTrack.toString().equals(SoundConstants.ST_FANTASCAPE) 
					&& play_times < 2)	{
				pickANewTrack();
			}
			else if (currentMusicTrack != null
					&& !currentMusicTrack.isMute() && currentMusicTrack.getVol() != 0
					&& play_times < 4) {
				playMusic(currentMusicTrack.toString());
				play_times++;
			}
			else {
				pickANewTrack();
			}
		}
	}
	
	
	public boolean isSoundDisabled() {
		return isSoundDisabled;
	}
	
	public void setSoundDisabled(boolean mode) {
		isSoundDisabled = mode;
	}
	
	public void destroy() {
		allSoundClips = null;
		allMusicTracks = null;
		desktop = null;
		currentSoundClip = null;
		currentMusicTrack = null;
		soundTracks = null;
		played_tracks = null;
	}
	
}