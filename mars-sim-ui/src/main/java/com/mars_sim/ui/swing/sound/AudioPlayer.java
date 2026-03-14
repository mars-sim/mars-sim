/*
 * Mars Simulation Project
 * AudioPlayer.java
 * @date 2025-10-18
 * @author Lars Naesbye Christensen (complete rewrite for OGG)
 */

package com.mars_sim.ui.swing.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.ui.swing.UIConfig;

/**
 * A class to dispatch playback of OGG files to OGGSoundClip.
 */
public class AudioPlayer {
	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(AudioPlayer.class.getName());

	/** music files directory. */
	private static final String MUSIC_DIR = SimulationRuntime.getMusicDir();
	private static final String DEFAULT_MUSIC_DIR = "/music";
			
	private static final double DEFAULT_VOL = 0.5;
	private static final double STEP = 0.05;

	private static final String MUSIC_VOLUME = "music volume";
	private static final String SOUND_VOLUME = "sound volume";
	private static final String OGG = "ogg";
	private static final String FULL_PATH = DEFAULT_MUSIC_DIR + "/*." + OGG;

	private static final int PLAYLIST_SIZE = 3;


	/** The volume of the audio player (0.0 to 1.0) */
	private double currentMusicVol = DEFAULT_VOL;
	private double currentSoundVol = DEFAULT_VOL;

	private boolean hasMasterGain = true;
	private boolean isVolumeDisabled;

	/** The current clip sound. */
	private OGGSoundClip currentSoundClip;
	private OGGSoundClip currentMusic;

	private Map<String, OGGSoundClip> allSoundClips;
	private Map<String, String> musicTracks;
	
	private List<String> playedTracks = new ArrayList<>();

	private int playTimes = 0;


	/**
	 * The class for managing the audio.
	 * 
	 * @param props the properties to initialize the audio player.
	 */
	public AudioPlayer(Properties props) {

		
		if (!isVolumeDisabled) {
			loadMusicTracks();
		}

		double musicVol = UIConfig.extractDouble(props, MUSIC_VOLUME, DEFAULT_VOL);
		currentMusicVol = musicVol;
		
		double soundVol = UIConfig.extractDouble(props, SOUND_VOLUME, DEFAULT_VOL);
		currentSoundVol = soundVol;
	}
		
	/**
	 * Creates an OGGSoundClip instance for a music/sound file.
	 * 
	 * @param parent
	 * @param filename
	 * @return
	 */
	private OGGSoundClip obtainOGGMusicTrack(String parent, String filename) {
		try {
			File f = new File(parent, filename);
			if (f.exists() && f.canRead()) {
				InputStream targetStream = new FileInputStream(f);
				return new OGGSoundClip(filename , targetStream);
			}
			else {
				logger.severe( "Can't read the ogg music file '" + (parent + filename) + "'.");
			}
		} catch (IOException e) {
			logger.severe( "Can't obtain the ogg music file '" + (parent + filename) + "': ", e);
		}
		return null;
	}
	
	/**
	 * Loads the music tracks.
	 */
	private void loadMusicTracks() {
		allSoundClips = new HashMap<>();
		musicTracks = new HashMap<>();

	
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
		// if it exists as a file, log a warning and skip loading music tracks from this folder
		if (userFolder.isFile()) {
			logger.log(Level.CONFIG, "'" + userFolder +  "'" 
					+ " is not supposed to exist as a file.");
		}
		else {
			if (!userFolder.isDirectory()) {
				// Create this directory
				userFolder.mkdirs();
				logger.log(Level.CONFIG, "'" + userFolder +  "'" 
						+ " folder is created for storing sound tracks.");
			}
		
			addMusicTracks(userFolder);
		}
	}
	
	/**
	 * Adds music tracks from a folder.
	 * 
	 * @param folder
	 */
	private void addMusicTracks(File folder) {
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
	 * Plays a sound clip.
	 * 
	 * @param filepath the file path to the sound file.
	 */
	public void playSound(String filepath) {
		logger.info("playSound: " + filepath);
		if (!isVolumeDisabled && !isEffectMute()) {
			if (allSoundClips.containsKey(filepath) 
					&& allSoundClips.get(filepath) != null) {
				currentSoundClip = allSoundClips.get(filepath);
			} else {
				try {
					InputStream soundStream = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(SoundConstants.SOUNDS_ROOT_PATH + filepath);
					currentSoundClip = new OGGSoundClip(filepath, soundStream);
					allSoundClips.put(filepath, currentSoundClip);
				} catch (IOException e) {
					logger.severe( "Can't load sound effect: ", e);
				}
			}

			if (currentSoundClip != null) {
				currentSoundClip.play(currentSoundVol);
			}
		}
	}


	/**
	 * Checks if it's playing.
	 */
	public boolean isPlaying() {
		return currentMusic != null && currentMusic.isPlaying();
	}
	

	/**
	 * Plays a music track.
	 * 
	 * @param filename
	 */
	public void playMusic(String filename) {
		if (!isPlaying() && !isMusicMute()) {
			String parent = musicTracks.get(filename);
			if (parent != null) {
				currentMusic = obtainOGGMusicTrack(parent, filename);		
				if (currentMusic != null) {
					currentMusic.loop(currentMusicVol);
				}
			}
		}
		else {
			logger.config("Skip music " + filename);
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
	public double getSoundEffectVolume() {
		return currentSoundVol;
	}

	/**
	 * Increases the music volume.
	 */
	public void musicVolumeUp() {
		if (!isVolumeDisabled && hasMasterGain) {

			double v = currentMusicVol + STEP;
			if (v > 1)
				v = 1.0;

			unmuteMusic();
			
			currentMusicVol = v;
			
			determineMusicGain(v);
			
			logger.info("New Music Volume: " + v);
		}
	}

	/**
	 * Decreases the music volume.
	 */
	public void musicVolumeDown() {	
		if (!isVolumeDisabled && hasMasterGain) {

			double v = currentMusicVol - STEP;
			if (v <= 0) {
				v = 0.0;
				muteMusic();	
			}

			currentMusicVol = v;

			determineMusicGain(v);
			
			logger.info("New Music Volume: " + v);
		}
	}

	/**
	 * Increases the sound effect volume.
	 */
	public void soundVolumeUp() {
		if (!isVolumeDisabled && hasMasterGain) {
			double v = currentSoundVol + STEP;
			if (v > 1)
				v = 1.0;

			unmuteSoundEffect();
			
			currentSoundVol = v;
		
			determineSoundGain(v);
			
			logger.info("New Sound Volume: " + v);
		}
	}

	/**
	 * Decreases the sound effect volume.
	 */
	public void soundVolumeDown() {
		if (!isVolumeDisabled && hasMasterGain) {
			double v = currentSoundVol - STEP;
			if (v <= 0) {
				v = 0.0;
				muteSoundEffect();
			}

			currentSoundVol = v;

			determineSoundGain(v);
			
			logger.info("New Sound Volume: " + v);
		}
	}

	/**
	 * Sets the volume of the audio player.
	 * 
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setMusicVolume(double volume) {

		if (volume <= 0) {
			volume = 0.0;
			muteMusic();
		}
		else if (volume > 1) {
			volume = 1.0;
		}
		else {
			unmuteMusic();
		}

		currentMusicVol = volume;
		
		determineMusicGain(volume);
	}

	/**
	 * Adjusts the music gain.
	 * 
	 * @param volume
	 */
	private void determineMusicGain(double volume) {
		if (currentMusic != null) {
			currentMusic.determineGain(volume);
		}
	}
	
	/**
	 * Restores previous music gain.
	 */
	private void restoreLastMusicGain() {
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

		if (volume <= 0) {
			volume = 0.0;
			muteSoundEffect();
		}
		else if (volume > 1)
			volume = 1.0;
		else
			unmuteSoundEffect();
		
		currentSoundVol = volume;

		determineSoundGain(volume);
	}
	
	/**
	 * Adjusts the sound effect gain.
	 * 
	 * @param volume
	 */
	private void determineSoundGain(double volume) {
		if (currentSoundClip != null) {
			currentSoundClip.determineGain(volume);
		}
	}
	
	/**
	 * Checks if the audio player's music is muted.
	 * 
	 * @return true if mute.
	 */
	private boolean isMusicMute() {
		if (currentMusicVol <= 0.0)  {
			return true;
		}
		else {
			return currentMusic != null && (currentMusic.isMute() || currentMusic.isPaused());
		}
	}

	/**
	 * Checks if the audio player's sound effect is muted.
	 * 
	 * @return true if mute.
	 */
	private boolean isEffectMute() {
		if (currentSoundVol <= 0.0) {
			return true;
		}
		else {
			return currentSoundClip != null && (currentSoundClip.isMute() || currentSoundClip.isPaused());
		}
	}

	/**
	 * Unmutes the sound effect.
	 */
	private void unmuteSoundEffect() {
		if (currentSoundClip != null && currentSoundClip.isMute()) {
			currentSoundClip.setMute(false);
		}
	}

	/**
	 * Mutes the sound Effect.
	 */
	private void muteSoundEffect() {
		if (currentSoundClip != null) {
			currentSoundClip.setMute(true);
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
		}
	}

	/**
	 * Checks if the music track ever started or has stopped
	 * 
	 * @return true if no music track is playing
	 */
	private boolean isMusicTrackStopped() {
		if (currentMusic == null)
			return true;
		return currentMusic.checkState(); 
	}
	
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
	private void pickANewTrack() {

		List<String> musicList = new ArrayList<>(musicTracks.keySet());
		musicList.removeAll(playedTracks);

		var choosen = RandomUtil.getRandomElement(musicList);

		// Play this music
		playMusic(choosen);
		// Print its name
		logger.config("Playing background music track " + choosen + "'.");
		// Add the new track
		playedTracks.addLast(choosen);
		if (playedTracks.size() > PLAYLIST_SIZE) {
			playedTracks.remove(0); // Remove oldest
		}

		// Reset the play times to 1 for this new track
		playTimes = 1;
	}
	
	/**
	 * Plays a randomly selected music track.
	 */
	private void playRandomMusicTrack() {
		if (isMusicMute()) {
			logger.config(5_000, "Music is muted.");
			return;
		}
		else if (isVolumeDisabled) {
			logger.config(5_000, "Volume is disable.");
			return;
		}
		else if (!isMusicTrackStopped()) {
			logger.config(5_000, "Music track not stopped.");
			return;
		}
		else {
//			logger.info("1. passed all checks.");
			// Since Areologie.ogg and Fantascape.ogg are 4 mins long, don't need to replay
			// them
			if (currentMusic != null
					&& playTimes < 2) {
				logger.config(5_000, "Case 1. pickANewTrack.");
				pickANewTrack();
			} else if (currentMusic != null && !currentMusic.isMute()
					&& playTimes < 4) {
				logger.config(5_000, "Case 2. playTimes < 4. playMusic.");
				playMusic(currentMusic.toString());
				logger.config("Playing background music " + " '" + currentMusic.toString() + "'.");
				playTimes++;
			} else {
				logger.config(5_000, "Case 3. pickANewTrack.");
				pickANewTrack();
			}
		}
	}

	/**
	 * Gets the UI properties of the audio player to be stored for later use.
	 */
	public Properties getUIProps() {
        Properties result = new Properties();
		result.setProperty(MUSIC_VOLUME, Double.toString(currentMusicVol));
		result.setProperty(SOUND_VOLUME, Double.toString(currentSoundVol));
		return result;
    }
}
