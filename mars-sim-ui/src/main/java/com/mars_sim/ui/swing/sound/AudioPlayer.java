/*
 * Mars Simulation Project
 * AudioPlayer.java
 * @date 2025-10-18
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
			
	public static final double DEFAULT_VOL = 0.5;
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

	/** The current clip sound. */
	private static OGGSoundClip currentSoundClip;
	private static OGGSoundClip currentMusic;

	private static Map<String, OGGSoundClip> allSoundClips;
	private static Map<String, String> musicTracks;
	private static List<String> musicList;
	
	private static List<Integer> playedTracks = new ArrayList<>();

	private static int playTimes = 0;
	private static int numTracks;
	volatile static boolean playing;
	
	private MasterClock masterClock;

//	private OggPlayer oggPlayer = new OggPlayer();

	/**
	 * The class for managing the audio.
	 * 
	 * @param desktop
	 */
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
			currentMusicVol = 0;
			currentSoundVol = 0;
		}
		else {
			double v = UIConfig.extractDouble(props, VOLUME, DEFAULT_VOL);
			currentMusicVol = v;
			currentSoundVol = v;
		}
	}
		
	/**
	 * Creates an OGGSoundClip instance for a music/sound file.
	 * 
	 * @param parent
	 * @param filename
	 * @return
	 */
	public static OGGSoundClip obtainOGGMusicTrack(String parent, String filename) {
		try {
			return new OGGSoundClip(parent, filename , true);
		} catch (IOException e) {
			logger.severe( "Can't obtain the ogg music file '" + (parent + filename) + "': ", e);
		}
		return null;
	}
	
	/**
	 * Loads the music tracks.
	 */
	public void loadMusicTracks() {
		allSoundClips = new HashMap<>();
		musicTracks = new HashMap<>();

		// Path in jarfile : 
		// jar:file:/Users/spacebear/git/mars-sim/mars-sim-dist/target/mars-sim-pre-3.10.0/lib/mars-sim-swing.jar!/music/
		
		// 1. Load from the music directory within the target folder or within jarfile
		ClassLoader cl = getClass().getClassLoader(); 
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
		try {
			Resource[] resources = resolver.getResources(FULL_PATH);
			
			if (resources.length > 0) {
				logger.log(Level.CONFIG, "Loading music file(s) from the target folder or within jarfile at " + FULL_PATH);
	
				for (Resource r: resources) {
					String filename = r.getFilename();
				    String ext = filename.substring(filename.indexOf('.') + 1, filename.length());
					
					if (ext.equalsIgnoreCase(OGG)) {
						logger.info(filename);
						musicTracks.put(filename, r.getFile().getParent());
					}
				}
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to load from the mars-sim's target folder or within jarfile: " + e);
		}

		// 2. Load from the music directory in user home folder
		File userFolder = new File(MUSIC_DIR);
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
			
		musicList = new ArrayList<>(musicTracks.keySet());
		if (numTracks > 0) {
			String music = musicList.get(numTracks -1);
			currentMusic = obtainOGGMusicTrack(musicTracks.get(music), music);
		}
	}
	
	/**
	 * Adds music tracks from a folder.
	 * 
	 * @param folder
	 */
	private static void addMusicTracks(File folder) {
		File[] listOfFiles = folder.listFiles();
		
		int length = listOfFiles.length;
				
		if (length > 0) {
			logger.log(Level.CONFIG, "Loading music file(s) from user home music folder at " + MUSIC_DIR);

			for (int i = 0; i < length; i++) {
				File f = listOfFiles[i];
				String filename = f.getName();
				String ext = filename.substring(filename.indexOf('.') + 1, filename.length());
				
				if (f.isFile() && ext.equalsIgnoreCase(OGG)) {
					logger.info(filename);
					musicTracks.put(f.getName(), folder.getParent());
				}
			}
		}
	}

	/**
	 * Loads all the sound effect clip names into a map.
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
				allSoundClips.put(s, new OGGSoundClip(null, s, false));
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
		logger.info("playSound: " + filepath);
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
		logger.info("loadSound: " + filepath);
		if (allSoundClips.containsKey(filepath) 
				&& allSoundClips.get(filepath) != null) {
			currentSoundClip = allSoundClips.get(filepath);
			currentSoundClip.play(currentSoundVol);
		} else {
			try {
				currentSoundClip = new OGGSoundClip(null, filepath, false);
				allSoundClips.put(filepath, currentSoundClip);
				currentSoundClip.play(currentSoundVol);
			} catch (IOException e) {
				logger.severe( "Can't load sound effect: ", e);
			}
		}
	}

	/**
	 * Plays a music track.
	 * 
	 * @param filename
	 */
	public void playMusic(String filename) {
		if (!isPlaying()) {
//			oggPlayer.play(musicTracks.get(filename), filename);
			loadMusic(filename);
		}
	}

	/**
	 * Checks if it's playing.
	 */
	public boolean isPlaying() {
		return currentMusic != null && currentMusic.isPlaying();
	}
	
	/**
	 * Loads up the music track.
	 * 
	 * @param filepath the file path to the music track.
	 */
	public static void loadMusic(String filepath) {
		if (musicList.contains(filepath) && filepath != null) {
			String parent = musicTracks.get(filepath);
			currentMusic = obtainOGGMusicTrack(parent, filepath);		
			if (currentMusic != null) {
				// Do NOT call resume() or else ogg file won't play
//				currentMusic.resume();
				logger.info("Music Volume: " + currentMusicVol);
				currentMusicVol = DEFAULT_VOL;
				logger.info("Music Volume: " + currentMusicVol);
				currentMusic.loop(currentMusicVol);
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
				&& currentMusic != null) {

			double v = currentMusicVol + STEP;
			if (v > 1)
				v = 1.0;

			currentMusicVol = v;
			currentMusic.determineGain(v);
			logger.info("New Music Volume: " + v);
		}
	}

	/**
	 * Decreases the music volume.
	 */
	public void musicVolumeDown() {	
		if (!isVolumeDisabled && hasMasterGain
				&& currentMusic != null) {

			double v = currentMusicVol - STEP;
			if (v < 0)
				v = 0.0;

			currentMusicVol = v;
			currentMusic.determineGain(v);
			logger.info("New Music Volume: " + v);
		}
	}

	/**
	 * Increases the sound effect volume.
	 */
	public void soundVolumeUp() {
		if (!isVolumeDisabled && hasMasterGain 
				&& currentSoundClip != null) {
			double v = currentSoundVol + STEP;
			if (v > 1)
				v = 1.0;

			currentSoundVol = v;
			currentSoundClip.determineGain(v);
			logger.info("New Sound Volume: " + v);
		}
	}

	/**
	 * Decreases the sound effect volume.
	 */
	public void soundVolumeDown() {
		if (!isVolumeDisabled && hasMasterGain 
				&& currentSoundClip != null) {
			double v = currentSoundVol - STEP;
			if (v < 0)
				v = 0.0;

			currentSoundVol = v;
			currentSoundClip.determineGain(v);
			logger.info("New Sound Volume: " + v);
		}
	}

	/**
	 * Sets the volume of the audio player.
	 * 
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setMusicVolume(double volume) {

		if (volume < 0)
			volume = 0.0;
		if (volume > 1)
			volume = 1.0;

		currentMusicVol = volume;

		if (!isVolumeDisabled && hasMasterGain && currentMusic != null) {
			currentMusic.determineGain(volume);
		}
	}

	/**
	 * Restores previous music gain.
	 */
	public void restoreLastMusicGain() {
		if (!isVolumeDisabled && hasMasterGain && currentMusic != null) {
			currentMusic.determineGain(currentMusicVol);
		}
	}
	
	/**
	 * Sets the volume of the audio player.
	 * 
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setSoundVolume(double volume) {

		if (volume < 0)
			volume = 0.0;
		if (volume > 1)
			volume = 1.0;

		currentSoundVol = volume;

		if (!isVolumeDisabled && hasMasterGain && currentSoundClip != null) {
			currentSoundClip.determineGain(volume);
		}
	}

//	/**
//	 * Restores the last sound effect gain.
//	 */
//	public void restoreLastSoundEffectGain() {
//		if (!isVolumeDisabled && hasMasterGain && currentSoundClip != null) {
//			currentSoundClip.determineGain(currentSoundVol);
//		}
//	}

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
		if (currentSoundClip != null && currentSoundClip.isMute()) {
			currentSoundClip.setMute(false);
//			restoreLastSoundEffectGain();
		}
	}

	/**
	 * Mutes the sound Effect.
	 */
	public void muteSoundEffect() {
		if (currentSoundClip != null) {
			currentSoundClip.setMute(true);
//			currentSoundClip.stop();
		}
	}

	/**
	 * Unmutes the music.
	 */
	public void unmuteMusic() {
		if (currentMusic != null && currentMusic.isMute()) {
			currentMusic.setMute(false);
			restoreLastMusicGain();
		}
	}
	
	/**
	 * Mutes the music.
	 */
	public void muteMusic() {
		if (currentMusic != null) {
			currentMusic.setMute(true);
//			currentMusic.stop();
		}
	}
	
	/**
//	 * Sets the user mute music.
//	 * 
//	 * @param value
//	 */
//	public void setUserMuteMusic(boolean value) {
//		if (currentMusic != null && !currentMusic.isMute()) {
//			// Note: should check if it is already mute since 
//			// user may pause and unpause consecutively too fast 
//			currentMusic.setMute(value);
//			restoreLastMusicGain();
//		}
//	}

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


//	/**
//	 * Resumes playing the music.
//	 */
//	public void resumeMusic() {
//		if (currentMusic != null && !isVolumeDisabled) {
////			currentMusic.setMute(false);
////			restoreLastMusicGain();
//			currentMusic.resume(currentMusicVol);
//		}
////		else {
////			loopThruBackgroundMusic();
////		}
//	}
	
//	/**
//	 * Pauses the music.
//	 */
//	public void pauseMusic() {
//		if (isMusicMute())
//			return;
//		if (currentMusic != null && !isVolumeDisabled) {
//			currentMusic.setPause(true);
//		}
//	}
	
	/**
	 * Loops through the background tracks.
	 */
	public void loopThruBackgroundMusic() {
		if (isMusicTrackStopped()) {
			logger.info("Run playRandomMusicTrack");
			playRandomMusicTrack();
		}		
	}
	
	/**
	 * Picks a new music track to play
	 */
	public void pickANewTrack() {
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

				String name = musicList.get(rand);
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
		if (numTracks == 0) {
			return;
		}
//		else if (isMusicMute()) {
//			logger.info(5_000, "Music muted.");
//			return;
//		}
		else if (masterClock.isPaused()) {
			logger.info(5_000, "Master clock on pause.");
			return;
		}
		else if (isVolumeDisabled) {
			logger.info(5_000, "Volume is disable.");
			return;
		}
		else if (!isMusicTrackStopped()) {
			logger.info(5_000, "Music track not stopped.");
			return;
		}
		else {
//			logger.info("1. passed all checks.");
			// Since Areologie.ogg and Fantascape.ogg are 4 mins long, don't need to replay
			// them
			if (currentMusic != null
					&& playTimes < 2) {
				logger.info(5_000, "Case 1. pickANewTrack.");
				pickANewTrack();
			} else if (currentMusic != null && !currentMusic.isMute()
					&& playTimes < 4) {
				logger.info(5_000, "Case 2. playTimes < 4. playMusic.");
				playMusic(currentMusic.toString());
				logger.config("Playing background music " + " '" + currentMusic.toString() + "'.");
				playTimes++;
			} else {
				logger.info(5_000, "Case 3. pickANewTrack.");
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
		musicTracks.clear();
		musicTracks = null;
		musicList = null;
		playedTracks.clear();
		playedTracks = null;
	}


}
