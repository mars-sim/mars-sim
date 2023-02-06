/*
 * Mars Simulation Project
 * AudioPlayer.java
 * @date 2021-08-21
 * @author Lars Naesbye Christensen (complete rewrite for OGG)
 */

package org.mars_sim.msp.ui.swing.sound;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.UIConfig;

/**
 * A class to dispatch playback of OGG files to OGGSoundClip.
 */
public class AudioPlayer {
	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(AudioPlayer.class.getName());

	/** music files directory. */
	public static final String MUSIC_DIR = SimulationFiles.getDataDir() +
			File.separator + Msg.getString("Simulation.musicFolder"); //$NON-NLS-1$
	
	public static final double DEFAULT_VOL = .5;

	/** The volume of the audio player (0.0 to 1.0) */
	public static double currentMusicVol = DEFAULT_VOL;
	public static double currentSoundVol = DEFAULT_VOL;

	private static boolean hasMasterGain = true;
	private static boolean isVolumeDisabled;

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
		if (!config.useUIDefault()) {
			
			if (config.isMute()) {
				muteSoundEffect();
				muteMusic();
				currentMusicVol = 0;
				currentSoundVol = 0;
			}
			else {
				double v = config.getVolume();
				currentMusicVol = v;
				currentSoundVol = v;
			}
		}
	}
		
	public static OGGSoundClip obtainOGGMusicTrack(String name) {
		try {
			return new OGGSoundClip(name, true);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Can't obtain the ogg music file '" + name + "': ", e);
		}
		return null;
	}
	
	public static void loadMusicTracks() {
		allSoundClips = new HashMap<>();
		musicTracks = new ArrayList<>();

		File folder = new File(MUSIC_DIR);

		boolean dirExist = folder.isDirectory();
		boolean fileExist = folder.isFile();
		
		// if it exits as a file, delete it
		if (fileExist) {
			logger.log(Level.CONFIG, "'" + folder +  "'" 
					+ " is not supposed to exist as a file. Deleting it.");
			try {
				FileUtils.forceDelete(folder);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Can't load music files: ", e);
			}
		}
		
		if (!dirExist) {
			// Create this directory
			folder.mkdirs();
			logger.log(Level.CONFIG, "'" + folder +  "'" 
					+ " folder is created for storing sound tracks");
		}
		else {
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				File f = listOfFiles[i];
				String filename = f.getName();
				String ext = filename.substring(filename.indexOf('.') + 1, filename.length());
				
				if (f.isFile() && ext.equalsIgnoreCase("ogg")) {
					musicTracks.add(f.getName());
				}
			}
			
			numTracks = musicTracks.size();
			
			if (numTracks > 0) {
				currentMusic = obtainOGGMusicTrack(musicTracks.get(numTracks -1));
			}	
		}
	}
	
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
				logger.log(Level.SEVERE, "Can't load the sound effect files: ", e);
			}
		}

		currentSoundClip = allSoundClips.get(SoundConstants.SND_PERSON_FEMALE1);
	}

	/**
	 * Play a sound clip.
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
			logger.log(Level.SEVERE, "allSoundClips is null.");
			return;
		}
		
		if (allSoundClips.containsKey(filepath) 
				&& allSoundClips.get(filepath) != null) {
			currentSoundClip = allSoundClips.get(filepath);
			currentSoundClip.determineGain(currentSoundVol);
			currentSoundClip.play();
		} else {
			try {
				currentSoundClip = new OGGSoundClip(filepath, false);
				allSoundClips.put(filepath, currentSoundClip);
				currentSoundClip.determineGain(currentSoundVol);
				currentSoundClip.play();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Can't load sound effect: ", e);
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
				currentMusic.determineGain(currentMusicVol);
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
	 * Increase the music volume
	 */
	public void musicVolumeUp() {
		if (!isVolumeDisabled || !isMusicMute()) {
			pickANewTrack();
		}
		
		if (!isVolumeDisabled && hasMasterGain
				&& currentMusic != null
				&& currentMusic.getVol() < 1) {

			double v = currentMusic.getVol() + .05;
			if (v > 1)
				v = 1;

			currentMusicVol = v;
			currentMusic.determineGain(v);
		}
	}

	/**
	 * Decrease the music volume
	 */
	public void musicVolumeDown() {
		if (!isVolumeDisabled || !isMusicMute()) {
			pickANewTrack();
		}
		
		if (!isVolumeDisabled && hasMasterGain
				&& currentMusic != null
				&& currentMusic.getVol() > 0) {

			double v = currentMusic.getVol() - .05;
			if (v < 0)
				v = 0;

			currentMusicVol = v;
			currentMusic.determineGain(v);
		}
	}

	/**
	 * Increase the sound effect volume
	 */
	public void soundVolumeUp() {
		if (!isVolumeDisabled && hasMasterGain 
				&& currentSoundClip != null
				&& currentSoundClip.getVol() < 1) {
			double v = currentSoundClip.getVol() + .05;
			if (v > 1)
				v = 1;

			currentSoundVol = v;
			currentSoundClip.determineGain(v);
		}
	}

	/**
	 * Decrease the sound effect volume
	 */
	public void soundVolumeDown() {
		if (!isVolumeDisabled && hasMasterGain 
				&& currentSoundClip != null
				&& currentSoundClip.getVol() > 0) {
			double v = currentSoundClip.getVol() - .05;
			if (v < 0)
				v = 0;

			currentSoundVol = v;
			currentSoundClip.determineGain(v);
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
			currentMusic.determineGain(volume);
		}
	}

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

		if (volume < 0F)
			volume = 0;
		if (volume > 1F)
			volume = 1F;

		currentSoundVol = volume;

		if (!isVolumeDisabled && hasMasterGain && currentSoundClip != null) {
			currentSoundClip.determineGain(volume);
		}
	}


	/**
	 * Restore the last sound effect gain
	 */
	public void restoreLastSoundEffectGain() {
		if (!isVolumeDisabled && hasMasterGain && currentSoundClip != null) {
//			if (lastSoundVol == 0)
				currentSoundClip.determineGain(currentSoundVol);
//			else
//				currentSoundClip.determineGain(lastSoundVol);
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
	 * Unmute the sound effect
	 */
	public void unmuteSoundEffect() {
		if (currentSoundClip != null && currentSoundClip.isMute()) {
			currentSoundClip.setMute(false);
			restoreLastSoundEffectGain();
		}
	}

	/**
	 * Unmute the music
	 */
	public void unmuteMusic() {
		if (currentMusic != null) {
			currentMusic.setMute(false);
			restoreLastMusicGain();
			resumeMusic();
		}
	}
	
	/**
	 * Mute the sound Effect
	 */
	public void muteSoundEffect() {
		if (currentSoundClip != null) {
			currentSoundClip.setMute(true);
			currentSoundClip.stop();
		}
	}
	
	/**
	 * Mute the music
	 */
	public void muteMusic() {
		if (currentMusic != null && !currentMusic.isMute()) {
			// Note: should check if it is already mute since 
			// user may pause and unpause consecutively too fast 
			currentMusic.setMute(true);
		}
	}

	/**
	 * Checks if the music track ever started or has stopped
	 * 
	 * @return true if no music track is playing
	 */
	public boolean isMusicTrackStopped() {
		if (currentMusic == null)
			return true;
		return currentMusic.stopped();
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
	 * Resume playing the music
	 */
	public void resumeMusic() {
		if (isMusicMute())
			return;
		if (currentMusic != null && !isVolumeDisabled) {
			currentMusic.resume();
		}
		else {
			playRandomMusicTrack();
		}
	}
	
	/**
	 * Play a randomly selected music track
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
	
	public void destroy() {
		allSoundClips = null;
		currentSoundClip = null;
		currentMusic = null;
		musicTracks = null;
	}
}
