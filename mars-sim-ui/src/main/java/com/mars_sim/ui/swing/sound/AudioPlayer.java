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
import java.util.function.Consumer;
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
	private static final String SOUNDS_ROOT_PATH = "audio/";
			
	private static final double DEFAULT_VOL = 0.5;

	private static final String MUSIC_VOLUME = "music volume";
	private static final String MUSIC_MUTE = "music mute";
	private static final String SOUND_VOLUME = "sound effect volume";
	private static final String SOUND_MUTE = "sound effect mute";
	private static final String OGG = "ogg";
	private static final String FULL_PATH = DEFAULT_MUSIC_DIR + "/*." + OGG;

	private static final int PLAYLIST_SIZE = 3;

	/**
	 * The control a stream of audio.
	 */
	private static class AudioFeed {
		private boolean isMuted;
		private double volume;
		private OGGSoundClip currentClip;
		private Consumer<OGGSoundClip> callback;

		/**
		 * Creates an audio feed.
		 * @param isMuted Initial muted setting.
		 * @param volume Initial volume.
		 * @param callback Callback when the any clip finishes playing
		 */
		public AudioFeed(boolean isMuted, double volume, Consumer<OGGSoundClip> callback) {
			this.isMuted = isMuted;
			this.volume = volume;
			this.callback = callback;
		}

		public double getVolume() {
			return volume;
		}

		public boolean isMuted() {
			return isMuted;
		}

		public void setMuted(boolean muted) {
			this.isMuted = muted;
			if (currentClip != null) {
				if (muted) {
					currentClip.stop();
					currentClip.setStopped(true);
				}
				else {
					currentClip.setStopped(false);
					currentClip.play(volume, callback);
				}
			}
		}

		public void setVolume(double newVolume) {
			volume = Math.clamp(newVolume, 0, 1);
			if (volume == 0) {
				setMuted(true);
			}
			else if (!isMuted && currentClip != null) {
				currentClip.determineGain(volume);
			}
		}

		public void play(OGGSoundClip newClip) {
			currentClip = newClip;
			if (!isMuted)
				currentClip.play(volume, callback);
		}
	}

	private Map<String, OGGSoundClip> allSoundClips;
	private Map<String, String> musicTracks;
	
	private List<String> playedTracks = new ArrayList<>();

	private AudioFeed musicFeed;
	private AudioFeed soundEffectFeed;

	/**
	 * The class for managing the audio.
	 * 
	 * @param props the properties to initialize the audio player.
	 */
	public AudioPlayer(Properties props) {
		
		loadMusicTracks();

		double musicVol = UIConfig.extractDouble(props, MUSIC_VOLUME, DEFAULT_VOL);
		boolean musicMute = UIConfig.extractBoolean(props, MUSIC_MUTE, false);
		musicFeed = new AudioFeed(musicMute, musicVol, e -> pickNextTrack(e));
		
		double soundVol = UIConfig.extractDouble(props, SOUND_VOLUME, DEFAULT_VOL);
		boolean soundMute = UIConfig.extractBoolean(props, SOUND_MUTE, false);
		soundEffectFeed = new AudioFeed(soundMute, soundVol, null);
	}
		
	/**
	 * Plays random music tracks on repeat.
	 */
	public void playRandomTracks() {
		pickNextTrack(null);
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
		OGGSoundClip currentSoundClip = null;
		if (allSoundClips.containsKey(filepath) 
				&& allSoundClips.get(filepath) != null) {
			currentSoundClip = allSoundClips.get(filepath);
		} else {
			try {
				InputStream soundStream = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(SOUNDS_ROOT_PATH + filepath);
				currentSoundClip = new OGGSoundClip(filepath, soundStream);
				allSoundClips.put(filepath, currentSoundClip);
			} catch (IOException e) {
				logger.severe( "Can't load sound effect: ", e);
			}
		}

		if (currentSoundClip != null) {
			soundEffectFeed.play(currentSoundClip);
		}
	}

	/**
	 * Plays a music track.
	 * 
	 * @param filename
	 */
	private void playMusic(String filename) {
		if (!isMusicMute()) {
			String parent = musicTracks.get(filename);
			if (parent != null) {
				var currentMusic = obtainOGGMusicTrack(parent, filename);		
				if (currentMusic != null) {
					musicFeed.play(currentMusic);
				}
			}
		}
		else {
			logger.config("Skip music " + filename);
		}
	}

	/**
	 * Gets the volume of the sound effect.
	 * 
	 * @return volume (0.0 to 1.0)
	 */
	public double getSoundEffectVolume() {
		return soundEffectFeed.getVolume();
	}
	
	/**
	 * Sets the volume of the audio player.
	 * 
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setSoundEffectVolume(double volume) {
		soundEffectFeed.setVolume(volume);
	}

	/**
	 * Checks if the audio player's sound effect is muted.
	 * 
	 * @return true if mute.
	 */
	public boolean isSoundEffectMute() {
		return soundEffectFeed.isMuted();
	}

	/**
	 * Sets whether sound effects are muted.
	 *
	 * @param isMuted true if sound effects should be muted.
	 */
	public void setSoundEffectMute(boolean isMuted) {
		soundEffectFeed.setMuted(isMuted);
	}

	/**
	 * Gets the volume of the background music.
	 * 
	 * @return volume (0.0 to 1.0)
	 */
	public double getMusicVolume() {
		return musicFeed.getVolume();
	}
	
	/**
	 * Sets the volume of the audio player.
	 * 
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setMusicVolume(double volume) {
		musicFeed.setVolume(volume);
	}

	/**
	 * Sets whether music is muted.
	 *
	 * @param isMute true if music should be muted.
	 */
	public void setMusicMute(boolean isMute) {
		musicFeed.setMuted(isMute);
		
		// Nothing running, so pick a track to play if unmuting
		if (!isMute && musicFeed.currentClip == null) {
			pickNextTrack(null);
		}
	}

	/**
	 * Checks if the audio player's music is muted.
	 * 
	 * @return true if mute.
	 */
	public boolean isMusicMute() {
		return musicFeed.isMuted();
	}
	
	/**
	 * Picks a new music track to play
	 * @param lastClip The last clip just finsihed 
	 */
	private void pickNextTrack(OGGSoundClip lastClip) {

		if (musicFeed.isMuted()) {
			return;
		}

		List<String> musicList = new ArrayList<>(musicTracks.keySet());
		musicList.removeAll(playedTracks);
		if (musicList.isEmpty()) {
			playedTracks.clear();
			musicList = new ArrayList<>(musicTracks.keySet());
		}

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
	}

	/**
	 * Gets the UI properties of the audio player to be stored for later use.
	 */
	public Properties getUIProps() {
        Properties result = new Properties();
		result.setProperty(MUSIC_VOLUME, Double.toString(musicFeed.getVolume()));
		result.setProperty(MUSIC_MUTE, Boolean.toString(musicFeed.isMuted()));
		result.setProperty(SOUND_VOLUME, Double.toString(soundEffectFeed.getVolume()));
		result.setProperty(SOUND_MUTE, Boolean.toString(soundEffectFeed.isMuted()));

		return result;
    }
}
