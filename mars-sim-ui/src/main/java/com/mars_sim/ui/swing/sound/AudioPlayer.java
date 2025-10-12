/*
 * Mars Simulation Project
 * AudioPlayer.java
 * @date 2025-09-18
 * @author Lars Naesbye Christensen (complete rewrite for OGG)
 */

package com.mars_sim.ui.swing.sound;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.UIConfig;

/**
 * A class to dispatch playback of OGG files to OGGSoundClip.
 */
public class AudioPlayer {
	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(AudioPlayer.class.getName());

	/** music files directory. */
	public static final String MUSIC_DIR = SimulationRuntime.getMusicDir();
	private static final String DEFAULT_MUSIC_DIR = "/music";
			
	public static final double DEFAULT_VOL = .95;
	public static final double STEP = 0.05;

	public static final String PROPS_NAME = "audio";
	private static final String VOLUME = "volume";
	private static final String MUTE = "mute";
	private static final String OGG = "ogg";
	private static final String FULL_PATH = DEFAULT_MUSIC_DIR + "/*." + OGG;


	/** The volume of the audio player (0.0 to 1.0) */
	public static double currentMusicVol = DEFAULT_VOL;
	public static double currentSoundVol = DEFAULT_VOL;

	private static boolean hasMasterGain = true;
	private static boolean isVolumeDisabled;

	private boolean userMuteMusic = false;
	private boolean userMuteSoundEffect = false;
	
	/** The current clip sound. */
	private static OGGSoundClip currentSoundClip;
	private static OGGSoundClip currentMusic;

	private static Map<String, OGGSoundClip> allSoundClips;

	private static List<String> musicTracks;
	private static List<Integer> playedTracks = new ArrayList<>();

	private static int playTimes = 0;
	private static int numTracks;
	
	private MasterClock masterClock;


	public AudioPlayer(MainDesktopPane desktop) {

		masterClock = desktop.getSimulation().getMasterClock();
		
		if (!isVolumeDisabled) {
			loadMusicTracks();
			loadSoundEffects();
		}
		
		UIConfig config = desktop.getMainWindow().getConfig();
		Properties props = config.getPropSet(PROPS_NAME);
		boolean mute = UIConfig.extractBoolean(props, MUTE, true);
		if (mute) {
			userMuteMusic = false;
			userMuteSoundEffect = false;
			currentMusicVol = 0;
			currentSoundVol = 0;
		}
		else {
			double v = UIConfig.extractDouble(props, VOLUME, DEFAULT_VOL);
			currentMusicVol = v;
			currentSoundVol = v;
		}
	}
		
	public static OGGSoundClip obtainOGGMusicTrack(String name) {
		try {
			return new OGGSoundClip(name, true);
		} catch (IOException e) {
			logger.severe( "Can't obtain the ogg music file '" + name + "': ", e);
		}
		return null;
	}
	
	/**
	 * Loads the music tracks.
	 */
	public void loadMusicTracks() {
		allSoundClips = new HashMap<>();
		musicTracks = new ArrayList<>();

		// Path in jarfile : 
		// jar:file:/Users/spacebear/git/mars-sim/mars-sim-dist/target/mars-sim-pre-3.10.0/lib/mars-sim-swing.jar!/music/
		
		// 1. Load from the music directory within the target folder or within jarfile
		ClassLoader cl = getClass().getClassLoader(); 
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
		try {
			Resource[] resources = resolver.getResources(FULL_PATH);
			logger.log(Level.CONFIG, "Loading from the target folder or within jarfile at " + FULL_PATH + ".");
			List<String> files = new ArrayList<>();
			for (Resource r: resources){
			    files.add(r.getFilename());
			}
			addMusicTracks(files);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to load from the target folder or within jarfile: " + e);
		}

		// 2. Load from the music directory in user home folder
		File userFolder = new File(MUSIC_DIR);
		logger.log(Level.CONFIG, "Loading from user home music folder at " + MUSIC_DIR + ".");
		// e.g. User Home music folder at /Users/spacebear/.mars-sim/music
		
		boolean dirExist = userFolder.isDirectory();
		boolean fileExist = userFolder.isFile();
		
		// if it exits as a file, delete it
		if (fileExist) {
			logger.log(Level.CONFIG, "'" + userFolder +  "'" 
					+ " is not supposed to exist as a file. Deleting it.");
			try {
				FileUtils.forceDelete(userFolder);
			} catch (IOException e) {
				logger.severe( "Can't load music files: ", e);
			}
		}
		
		if (!dirExist) {
			// Create this directory
			userFolder.mkdirs();
			logger.log(Level.CONFIG, "'" + userFolder +  "'" 
					+ " folder is created for storing sound tracks.");
		}
		
		addMusicTracks(userFolder);
		
		numTracks = musicTracks.size();
			
		if (numTracks > 0) {
			currentMusic = obtainOGGMusicTrack(musicTracks.get(numTracks -1));
		}
	}
	
	/**
	 * Adds music tracks from a folder.
	 * 
	 * @param folder
	 */
	private static void addMusicTracks(File folder) {
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			File f = listOfFiles[i];
			String filename = f.getName();
			String ext = filename.substring(filename.indexOf('.') + 1, filename.length());
			
			if (f.isFile() && ext.equalsIgnoreCase(OGG)) {
				logger.info(filename);
				musicTracks.add(f.getName());
			}
		}
	}
	
	/**
	 * Adds music tracks from a list of files.
	 * 
	 * @param folder
	 */
	private static void addMusicTracks(List<String> files) {
	
		for (int i = 0; i < files.size(); i++) {
			String filename = files.get(i);
			String ext = filename.substring(filename.indexOf('.') + 1, filename.length());
			
			if (ext.equalsIgnoreCase(OGG)) {
				logger.info(filename);
				musicTracks.add(filename);
			}
		}
	}
	

	/**
	 * Loads all the sound effect clip names into a map.
	 * 
	 */
	public void loadSoundEffects() {
		List<String> soundEffects = new ArrayList<>();
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
				allSoundClips.put(s, new OGGSoundClip(s, false));
			} catch (IOException e) {
				logger.severe( "Can't load the sound effect files: ", e);
			}
		}

		currentSoundClip = allSoundClips.get(SoundConstants.SND_PERSON_FEMALE1);
	}

	/**
	 * Plays a sound clip.
	 * 
	 * @param filepath the file path to the sound file.
	 */
	public void playSound(String filepath) {
		if (!isVolumeDisabled && !isEffectMute() && filepath != null && !filepath.equals("")) {
			loadSound(filepath);
		}
	}

	/**
	 * Loads up a sound clip.
	 * 
	 * @param filepath the file path to the music track.
	 */
	private void loadSound(String filepath) {
		if (allSoundClips == null) {
			logger.severe( "allSoundClips is null.");
			return;
		}
		
		if (allSoundClips.containsKey(filepath) 
				&& allSoundClips.get(filepath) != null) {
			currentSoundClip = allSoundClips.get(filepath);
			currentSoundClip.determineVolume(currentSoundVol);
			currentSoundClip.play();
		} else {
			try {
				currentSoundClip = new OGGSoundClip(filepath, false);
				allSoundClips.put(filepath, currentSoundClip);
				currentSoundClip.determineVolume(currentSoundVol);
				currentSoundClip.play();
			} catch (IOException e) {
				logger.severe( "Can't load sound effect: ", e);
			}
		}
	}

	/**
	 * Plays a music track.
	 * 
	 * @param filepath the file path to the music track.
	 */
	public static void playMusic(String filepath) {
		if (!isMusicMute()) {
			loadMusic(filepath);
		}
	}

	/**
	 * Loads up the music track.
	 * 
	 * @param filepath the file path to the music track.
	 */
	public static void loadMusic(String filepath) {
		if (musicTracks.contains(filepath) && filepath != null) {
			currentMusic = obtainOGGMusicTrack(filepath);
			if (currentMusic != null) {
				currentMusic.determineVolume(currentMusicVol);
				currentMusic.loop();
			}
		}
	}

	/**
	 * Gets the volume of the background music.
	 * 
	 * @return volume (0.0 to 1.0)
	 */
	public double getMusicVolume() {
		return currentMusicVol;
	}

	/**
	 * Gets the volume of the sound effect.
	 * 
	 * @return volume (0.0 to 1.0)
	 */
	public double getEffectVolume() {
		return currentSoundVol;
	}

	/**
	 * Increases the music volume.
	 */
	public void musicVolumeUp() {
		if (!isVolumeDisabled && hasMasterGain
				&& currentMusic != null
				&& currentMusic.getVol() < 1) {

			double v = currentMusic.getVol() + STEP;
			if (v > 1)
				v = 1;

			currentMusicVol = v;
			currentMusic.determineVolume(v);
		}
	}

	/**
	 * Decreases the music volume.
	 */
	public void musicVolumeDown() {	
		if (!isVolumeDisabled && hasMasterGain
				&& currentMusic != null
				&& currentMusic.getVol() > 0) {

			double v = currentMusic.getVol() - STEP;
			if (v < 0)
				v = 0;

			currentMusicVol = v;
			currentMusic.determineVolume(v);
		}
	}

	/**
	 * Increases the sound effect volume.
	 */
	public void soundVolumeUp() {
		if (!isVolumeDisabled && hasMasterGain 
				&& currentSoundClip != null
				&& currentSoundClip.getVol() < 1) {
			double v = currentSoundClip.getVol() + STEP;
			if (v > 1)
				v = 1;

			currentSoundVol = v;
			currentSoundClip.determineVolume(v);
		}
	}

	/**
	 * Decreases the sound effect volume.
	 */
	public void soundVolumeDown() {
		if (!isVolumeDisabled && hasMasterGain 
				&& currentSoundClip != null
				&& currentSoundClip.getVol() > 0) {
			double v = currentSoundClip.getVol() - STEP;
			if (v < 0)
				v = 0;

			currentSoundVol = v;
			currentSoundClip.determineVolume(v);
		}
	}

	/**
	 * Sets the volume of the audio player.
	 * 
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setMusicVolume(double volume) {

		if (volume < 0F)
			volume = 0;
		if (volume > 1F)
			volume = 1F;

		currentMusicVol = volume;

		if (!isVolumeDisabled && hasMasterGain && currentMusic != null) {
			currentMusic.determineVolume(volume);
		}
	}

	/**
	 * Restores previous music gain.
	 */
	public void restoreLastMusicGain() {
		if (!isVolumeDisabled && hasMasterGain && currentMusic != null) {
			currentMusic.determineVolume(currentMusicVol);
		}
	}
	
	/**
	 * Sets the volume of the audio player.
	 * 
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setSoundVolume(double volume) {

		if (volume < 0F)
			volume = 0;
		if (volume > 1F)
			volume = 1F;

		currentSoundVol = volume;

		if (!isVolumeDisabled && hasMasterGain && currentSoundClip != null) {
			currentSoundClip.determineVolume(volume);
		}
	}


	/**
	 * Restores the last sound effect gain.
	 */
	public void restoreLastSoundEffectGain() {
		if (!isVolumeDisabled && hasMasterGain && currentSoundClip != null) {
			currentSoundClip.determineVolume(currentSoundVol);
		}
	}

	/**
	 * Checks if the audio player's music is muted.
	 * 
	 * @return true if mute.
	 */
	public static boolean isMusicMute() {
		if (currentMusic == null || currentMusicVol <= 0)  {
			return true;
		}
		else {
			return currentMusic.isMute() || currentMusic.isPaused();
		}
	}

	/**
	 * Checks if the audio player's sound effect is muted.
	 * 
	 * @return true if mute.
	 */
	public static boolean isEffectMute() {
		if (currentSoundClip == null || currentSoundVol <= 0) {
			return true;
		}
		else {
			return currentSoundClip.isMute() || currentSoundClip.isPaused();
		}
	}

	/**
	 * Unmutes the sound effect.
	 */
	public void unmuteSoundEffect() {
		if (!userMuteSoundEffect && currentSoundClip != null && currentSoundClip.isMute()) {
			userMuteSoundEffect = false;
			currentSoundClip.setMute(false);
			restoreLastSoundEffectGain();
		}
	}

	/**
	 * Mutes the sound Effect.
	 */
	public void muteSoundEffect() {
		userMuteSoundEffect = true;
		if (currentSoundClip != null) {
			currentSoundClip.setMute(true);
			currentSoundClip.stop();
		}
	}

	/**
	 * Unmutes the music.
	 */
	public void unmuteMusic() {
		if (!userMuteMusic && currentMusic != null && currentMusic.isMute()) {
			userMuteMusic = false;
			currentMusic.setMute(false);
			restoreLastMusicGain();
		}
	}
	
	/**
	 * Mutes the music.
	 */
	public void muteMusic() {
		userMuteMusic = true;
		if (currentMusic != null) {
			currentMusic.setMute(true);
			currentMusic.stop();
		}
	}
	
	/**
	 * Sets the user mute music.
	 * 
	 * @param value
	 */
	public void setUserMuteMusic(boolean value) {
		userMuteMusic = value;
		if (currentMusic != null && !currentMusic.isMute()) {
			// Note: should check if it is already mute since 
			// user may pause and unpause consecutively too fast 
			currentMusic.setMute(value);
			restoreLastMusicGain();
		}
	}

	/**
	 * Gets the user mute music.
	 * 
	 * @return
	 */
	public boolean userMuteMusic() {
		return userMuteMusic;
	}
	
	/**
	 * Sets the user mute sound effect.
	 * 
	 * @param value
	 */
	public void setUserMuteSoundEffect(boolean value) {
		userMuteSoundEffect = value;
	}
	
	/**
	 * Gets the user mute sound effect.
	 * 
	 * @return
	 */
	public boolean userMuteSoundEffect() {
		return userMuteSoundEffect;
	}

	/**
	 * Checks if the music track ever started or has stopped
	 * 
	 * @return true if no music track is playing
	 */
	public boolean isMusicTrackStopped() {
		if (currentMusic == null)
			return true;
		return currentMusic.checkState(); 
	}


	/**
	 * Resumes playing the music.
	 */
	public void resumeMusic() {
		if (currentMusic != null && !isVolumeDisabled) {
			currentMusic.setMute(false);
			restoreLastMusicGain();
			currentMusic.resume();
		}
		else {
			loopThruBackgroundMusic();
		}
	}
	
	/**
	 * Pauses the music.
	 */
	public void pauseMusic() {
		if (isMusicMute())
			return;
		if (currentMusic != null && !isVolumeDisabled) {
			currentMusic.setPause(true);
		}
	}
	
	/**
	 * Loops through the background tracks.
	 */
	public void loopThruBackgroundMusic() {
		if (isMusicTrackStopped()) {
			playRandomMusicTrack();
		}		
	}
	
	/**
	 * Picks a new music track to play
	 */
	public static void pickANewTrack() {
		int rand = 0;
		// At the start of the sim, refrain from playing the last few tracks due to
		// their sudden loudness
		if (numTracks == 0)
			return;
		if (playedTracks.isEmpty())
			// Do not repeat the last 4 music tracks just played
			rand = RandomUtil.getRandomInt(numTracks - 1);
		
		boolean isNewTrack = false;
		// Do not repeat the last 4 music tracks just played
		while (!isNewTrack) {

			if (!playedTracks.contains(rand)) {
				isNewTrack = true;

				String name = musicTracks.get(rand);
				// Play this music
				playMusic(name);
				// Print its name
				logger.config("Playing background music track #" + (rand + 1) + " '" + name + "'.");
				// Add the new track
				playedTracks.add((rand));
				// Remove the earliest track
				playedTracks.remove(0);
				// Reset the play times to 1 for this new track
				playTimes = 1;
			} else
				rand = RandomUtil.getRandomInt(numTracks - 1);
		}

	}
	
	/**
	 * Plays a randomly selected music track.
	 */
	public void playRandomMusicTrack() {
		if (numTracks == 0)
			return;
		else if (isMusicMute())
			return;
		else if (masterClock.isPaused())
			return;
		else if (isVolumeDisabled)
			return;
		else if (!isMusicTrackStopped())
			return;
		else {
			// Since Areologie.ogg and Fantascape.ogg are 4 mins long, don't need to replay
			// them
			if (currentMusic != null
					&& playTimes < 2) {
				pickANewTrack();
			} else if (currentMusic != null && !currentMusic.isMute() && currentMusic.getVol() != 0
					&& playTimes < 4) {
				playMusic(currentMusic.toString());
				playTimes++;
			} else {
				pickANewTrack();
			}
		}
	}

	/**
	 * Is the volume of the audio player disable ?
	 * 
	 * @return
	 */
	public static boolean isAudioDisabled() {
		return isVolumeDisabled;
	}

	public static void setZeroVolume() {
		currentMusicVol = 0;
		currentSoundVol = 0;
	}
	
	public static void disableAudio() {
		isVolumeDisabled = true;
		hasMasterGain = false;
		
		setZeroVolume();
		
		allSoundClips = null;
		currentSoundClip = null;
		currentMusic = null;
		musicTracks = null;
		playedTracks = null;
	}

	public int getNumTracks() {
		return numTracks;
	}
	
	/**
	 * Gets the UI properties of the audio player to be stored for later use.
	 */
	public Properties getUIProps() {
        Properties result = new Properties();
		result.setProperty(VOLUME, Double.toString(currentSoundVol));
		result.setProperty(MUTE, Boolean.toString(AudioPlayer.isEffectMute()));

		return result;
    }

	public void destroy() {
		allSoundClips = null;
		currentSoundClip = null;
		currentMusic = null;
		musicTracks = null;
	}
}
