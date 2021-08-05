/**
 * Mars Simulation Project
 * AudioPlayer.java
 * @version 3.2.0 2021-06-20
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
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.UIConfig;

/**
 * A class to dispatch playback of OGG files to OGGSoundClip.
 */
public class AudioPlayer implements ClockListener {
	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(AudioPlayer.class.getName());

	/** music files directory. */
	public static final String MUSIC_DIR = System.getProperty("user.home") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.homeFolder") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.musicFolder"); //$NON-NLS-1$
	
	public static final double DEFAULT_VOL = .5;

	private static int numTracks;

	/** The volume of the audio player (0.0 to 1.0) */
	public static double currentMusicVol = DEFAULT_VOL;
	public static double currentSoundVol = DEFAULT_VOL;

	private static boolean hasMasterGain = true;
	private static boolean isVolumeDisabled;

	/** The current clip sound. */
	private static OGGSoundClip currentSoundClip;
	private static OGGSoundClip currentMusic;

	private static Map<String, OGGSoundClip> allSoundClips;

	private static List<String> soundEffects;
	private static List<String> musicTracks;
	private static List<Integer> played_tracks = new ArrayList<>();

	private static MasterClock masterClock;

	private int play_times = 0;

	public AudioPlayer(MainDesktopPane desktop) {

		masterClock = Simulation.instance().getMasterClock();

		// Add AudioPlayer to MasterClock's clock listener
		masterClock.addClockListener(this);
	
		if (!isVolumeDisabled) {
			loadMusicTracks();
			loadSoundEffects();
		}
		
		if (UIConfig.INSTANCE.useUIDefault()) {
			currentMusicVol = DEFAULT_VOL;
			currentSoundVol = DEFAULT_VOL;
		} 
		
		else {
			if (UIConfig.INSTANCE.isMute()) {
				muteSoundEffect();
				muteMusic();
				currentMusicVol = 0;
				currentSoundVol = 0;
			}

			double v = UIConfig.INSTANCE.getVolume();
			currentMusicVol = v;
			currentSoundVol = v;
		}
	}
		
	public OGGSoundClip obtainOGGMusicTrack(String name) {
		try {
			return new OGGSoundClip(name, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void loadMusicTracks() {
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
				e.printStackTrace();
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
				allSoundClips.put(s, new OGGSoundClip(s, false));
			} catch (IOException e) {
				e.printStackTrace();
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
		if (allSoundClips != null && allSoundClips.containsKey(filepath) && allSoundClips.get(filepath) != null) {
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
				logger.log(Level.SEVERE, "IOException in AudioPlayer's playSound()", e);
			}
		}
	}

	/**
	 * Plays a music track.
	 * 
	 * @param filepath the file path to the music track.
	 */
	public void playMusic(String filepath) {
		if (!isMusicMute()) {
			loadMusic(filepath);
		}
	}

	/**
	 * Loads up the music track.
	 * 
	 * @param filepath the file path to the music track.
	 */
	public void loadMusic(String filepath) {
		if (musicTracks.contains(filepath) && filepath != null) {
			currentMusic = obtainOGGMusicTrack(filepath);
			currentMusic.determineGain(currentMusicVol);
			currentMusic.loop();
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
		if (!isVolumeDisabled || !isMusicMute())
			pickANewTrack();
		if (!isVolumeDisabled && hasMasterGain && currentMusic != null
				&& currentMusic.getVol() < 1
				&& currentMusic != null) {

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
		if (!isVolumeDisabled || !isMusicMute())
			pickANewTrack();
		if (!isVolumeDisabled && hasMasterGain && currentMusic != null
				&& currentMusic.getVol() > 0
				&& currentMusic != null) {

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
				&& currentSoundClip.getVol() < 1
				&& currentSoundClip != null) {
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
				&& currentSoundClip.getVol() > 0
				&& currentSoundClip != null) {
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
	public boolean isMusicMute() {
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
	public boolean isEffectMute() {
		if (currentSoundClip == null && currentSoundVol <= 0) {
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
	public void pickANewTrack() {
		int rand = 0;
		// At the start of the sim, refrain from playing the last few tracks due to
		// their sudden loudness
		if (numTracks == 0)
			return;
		if (played_tracks.isEmpty())
			// Do not repeat the last 4 music tracks just played
			rand = RandomUtil.getRandomInt(numTracks - 1);
		
		boolean isNewTrack = false;
		// Do not repeat the last 4 music tracks just played
		while (!isNewTrack) {

			if (!played_tracks.contains(rand)) {
				isNewTrack = true;

				String name = musicTracks.get(rand);
				// Play this music
				playMusic(name);
				// Print its name
				logger.config("Playing background music track #" + (rand + 1) + " '" + name + "'.");
				// Add the new track
				played_tracks.add((rand));
				// Remove the earliest track
//				if (played_tracks.size() > REPEATING_TRACKS)
					played_tracks.remove(0);
				// Reset the play times to 1 for this new track
				play_times = 1;
				// break;
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
					&& play_times < 2) {
				pickANewTrack();
			} else if (currentMusic != null && !currentMusic.isMute() && currentMusic.getVol() != 0
					&& play_times < 4) {
				playMusic(currentMusic.toString());
				play_times++;
			} else {
				pickANewTrack();
			}
		}
	}

	/**
	 * Is the volume of the audio player disable ?
	 * @return
	 */
	public boolean isVolumeDisabled() {
		return isVolumeDisabled;
	}

	public static void disableVolume() {
		isVolumeDisabled = true;
		currentMusicVol = 0;
		currentSoundVol = 0;
		hasMasterGain = false;

		allSoundClips = null;
		currentSoundClip = null;
		currentMusic = null;
		musicTracks = null;
		played_tracks = null;
	}

	public int getNumTracks() {
		return numTracks;
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
		// TODO Auto-generated method stub

	}

	@Override
	public void uiPulse(double time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
//		if (isPaused) {
//			if (!isMusicMute())
//				muteMusic();
//			if (!isSoundEffectMute())
//				muteSoundEffect();
//		} else {
//			unmuteMusic();
//			unmuteSoundEffect();
//		}
	}

	
	public void destroy() {
		allSoundClips = null;
		currentSoundClip = null;
		currentMusic = null;
		musicTracks = null;
		played_tracks = null;
	}
}
