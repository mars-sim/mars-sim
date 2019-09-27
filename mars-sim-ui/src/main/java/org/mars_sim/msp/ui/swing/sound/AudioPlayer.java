/**
 * Mars Simulation Project
 * AudioPlayer.java
 * @version 3.1.0 2017-10-28
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
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * A class to dispatch playback of OGG files to OGGSoundClip.
 */
public class AudioPlayer implements ClockListener {

	private static Logger logger = Logger.getLogger(AudioPlayer.class.getName());
	private final String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());
	
//	private final static int LOUD_TRACKS = 6;
//	private final static int REPEATING_TRACKS = 4; // track the last 4 tracks and avoid playing them repetitively.

	public final static float DEFAULT_VOL = .5f;

	private static int numTracks;

	/** The volume of the audio player (0.0 to 1.0) */
	public static double currentMusicVol = DEFAULT_VOL;
	public static double currentSoundVol = DEFAULT_VOL;

//	private double lastMusicVol = 0;
//	private double lastSoundVol = 0;

	private int play_times = 0;

	private static boolean hasMasterGain = true;
	private static boolean isSoundDisabled;

	// private boolean lastMusicState = true;
	// private boolean lastSoundState = true;

//	private MainDesktopPane desktop;
//	private static MainScene mainScene;

	/** The current clip sound. */
	private static OGGSoundClip currentSoundClip;
	private static OGGSoundClip currentMusic;

//	private static Map<String, OGGSoundClip> allMusicTracks;
	private static Map<String, OGGSoundClip> allSoundClips;

	private static List<String> soundEffects;
	private static List<String> musicTracks;
	private static List<Integer> played_tracks = new ArrayList<>();

	private static MasterClock masterClock;

	public AudioPlayer(MainDesktopPane desktop) {
		// logger.config("constructor is on " + Thread.currentThread().getName());
//		this.desktop = desktop;

		masterClock = Simulation.instance().getMasterClock();

		// Add AudioPlayer to MasterClock's clock listener
		masterClock.addClockListener(this);
	
		if (!isSoundDisabled) {
			loadMusicTracks();
			loadSoundEffects();
		}
		
//		if (UIConfig.INSTANCE.useUIDefault())
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
//		allMusicTracks = new HashMap<>();
		allSoundClips = new HashMap<>();

		musicTracks = new ArrayList<>();
//			musicTracks.add(SoundConstants.ST_AREOLOGIE);
//			musicTracks.add(SoundConstants.ST_PUZZLE);
//			musicTracks.add(SoundConstants.ST_MISTY);
//			musicTracks.add(SoundConstants.ST_MENU);

//			musicTracks.add(SoundConstants.ST_ONE_WORLD);
//			musicTracks.add(SoundConstants.ST_BEDTIME);
//			musicTracks.add(SoundConstants.ST_BOG_CREATURES);
//			musicTracks.add(SoundConstants.ST_LOST_JUNGLE);

		// not for playing at the start of the sim due to its loudness
		// Set LOUD_TRACKS to 5
//			musicTracks.add(SoundConstants.ST_MOONLIGHT);
//			musicTracks.add(SoundConstants.ST_CITY);
//			musicTracks.add(SoundConstants.ST_CLIPPITY);
//			musicTracks.add(SoundConstants.ST_MONKEY);
//			musicTracks.add(SoundConstants.ST_SURREAL);
//			musicTracks.add(SoundConstants.ST_FANTASCAPE);

		File folder = new File(Simulation.MUSIC_DIR);
//	        FileSystem fileSys = FileSystems.getDefault();

//			Path path = fileSys.getPath(folder.getPath());
		
		boolean dirExist = folder.isDirectory();
		boolean fileExist = folder.isFile();
		
		// if it exits as a file, delete it
		if (fileExist) {
			LogConsolidated.log(Level.CONFIG, 0, sourceName, "'" + folder +  "'" 
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
			LogConsolidated.log(Level.CONFIG, 0, sourceName, "'" + folder +  "'" 
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
		if (!isSoundDisabled && !isSoundMute() && filepath != null && !filepath.equals("")) {
//				SwingUtilities.invokeLater(() -> loadSound(filepath));
			loadSound(filepath);
		}
	}

	/**
	 * Loads up a sound clip.
	 * 
	 * @param filepath the file path to the music track.
	 */
	public void loadSound(String filepath) {
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
				// e.printStackTrace();
				logger.log(Level.SEVERE, "IOException in AudioPlayer's playSound()", e.getMessage());
			}
		}
	}

	/**
	 * Plays a music track.
	 * 
	 * @param filepath the file path to the music track.
	 */
	public void playMusic(String filepath) {
		// logger.config("play() is on " + Thread.currentThread().getName());
		if (!isMusicMute()) {
//			SwingUtilities.invokeLater(() -> loadMusic(filepath));
			loadMusic(filepath);
		}
	}

	/**
	 * Loads up the music track.
	 * 
	 * @param filepath the file path to the music track.
	 */
	public void loadMusic(String filepath) {
//		if (currentMusic != null) {
//			currentMusic.determineGain(currentMusicVol);
//			currentMusic.resume();
//		}
//		else 
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

	public void musicVolumeUp() {
		if (!isSoundDisabled && hasMasterGain 
				&& currentMusic.getVol() < 1
				&& currentMusic != null) {
//			System.out.print("musicVolumeUp");
			double v = currentMusic.getVol() + .05;
			if (v > 1)
				v = 1;
//			lastMusicVol = currentMusicVol;
			currentMusicVol = v;
			currentMusic.determineGain(v);
		}
	}

	public void musicVolumeDown() {
		if (!isSoundDisabled && hasMasterGain 
				&& currentMusic.getVol() > 0
				&& currentMusic != null) {
//			System.out.print("musicVolumeDown");
			double v = currentMusic.getVol() - .05;
			if (v < 0)
				v = 0;
//			lastMusicVol = currentMusicVol;
			currentMusicVol = v;
			currentMusic.determineGain(v);
		}
	}

	public void soundVolumeUp() {
		if (!isSoundDisabled && hasMasterGain 
				&& currentSoundClip.getVol() < 1
				&& currentSoundClip != null) {
			double v = currentSoundClip.getVol() + .05;
			if (v > 1)
				v = 1;
//			lastSoundVol = currentSoundVol;
			currentSoundVol = v;
			currentSoundClip.determineGain(v);
		}
	}

	public void soundVolumeDown() {
		if (!isSoundDisabled && hasMasterGain 
				&& currentSoundClip.getVol() > 0
				&& currentSoundClip != null) {
			double v = currentSoundClip.getVol() - .05;
			if (v < 0)
				v = 0;
//			lastSoundVol = currentSoundVol;
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
		// logger.config("setVolume() is on " + Thread.currentThread().getName());
		if (volume < 0F)
			volume = 0;
		if (volume > 1F)
			volume = 1F;
//		lastMusicVol = currentMusicVol;
		currentMusicVol = volume;

		if (!isSoundDisabled && hasMasterGain && currentMusic != null) {
			currentMusic.determineGain(volume);
		}
	}

	public void restoreLastMusicGain() {
		if (!isSoundDisabled && hasMasterGain && currentMusic != null) {
//			if (lastMusicVol == 0)
				currentMusic.determineGain(currentMusicVol);
//			else
//				currentMusic.determineGain(lastMusicVol);
		}
	}
	
	/**
	 * Sets the volume of the audio player.
	 * 
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setSoundVolume(double volume) {
		// logger.config("setVolume() is on " + Thread.currentThread().getName());
		if (volume < 0F)
			volume = 0;
		if (volume > 1F)
			volume = 1F;
//		lastSoundVol = currentSoundVol;
		currentSoundVol = volume;

		if (!isSoundDisabled && hasMasterGain && currentSoundClip != null) {
			currentSoundClip.determineGain(volume);
		}
	}


	public void restoreLastSoundEffectGain() {
		if (!isSoundDisabled && hasMasterGain && currentSoundClip != null) {
//			if (lastSoundVol == 0)
				currentSoundClip.determineGain(currentSoundVol);
//			else
//				currentSoundClip.determineGain(lastSoundVol);
		}
	}

	/**
	 * Checks if the audio player is muted.
	 * 
	 * @param isEffect is the sound effect mute ?
	 * @param isTrack  is the background music mute ?
	 * @return true if mute.
	 */
	public boolean isMusicMute() {
		if (currentMusic != null || currentMusicVol <= 0) {
			if (currentMusic.isMute() || currentMusic.isPaused())
				return true;
		}

		return false;
	}

	/**
	 * Checks if the audio player is muted.
	 * 
	 * @param isEffect is the sound effect mute ?
	 * @param isTrack  is the background music mute ?
	 * @return true if mute.
	 */
	public boolean isSoundMute() {
		if (currentSoundClip != null || currentSoundVol <= 0) {
			if (currentSoundClip.isMute() || currentSoundClip.isPaused())
				return true;
		}

		return false;
	}

	/**
	 * Unmute the sound effect
	 * 
	 */
	public void unmuteSoundEffect() {
		if (currentSoundClip != null && currentSoundClip.isMute()) {
			currentSoundClip.setMute(false);
//			currentSoundVol = lastSoundVol;
			restoreLastSoundEffectGain();
		}
	}

	/**
	 * Unmute the music
	 * 
	 */
	public void unmuteMusic() {
		if (currentMusic != null) {// && currentMusic.isMute()) {
			currentMusic.setMute(false);
//			currentMusicVol = lastMusicVol;
			restoreLastMusicGain();
			resumeMusic();
//			System.out.println("Music should be unmute now. currentMusicVol : " + currentMusicVol + "  lastMusicVol : " + lastMusicVol);
		}
	}
	
	/**
	 * Mute the sound Effect
	 * 
	 */
	public void muteSoundEffect() {
		if (currentSoundClip != null) {// && !currentSoundClip.isMute()) {
			currentSoundClip.setMute(true);
			currentSoundClip.stop();
//			currentSoundClip.determineGain(0);
//			lastSoundVol = currentSoundVol;
//			currentSoundVol = 0;
		}
	}
	
	/**
	 * Mute the music
	 * 
	 */
	public void muteMusic() {
		if (currentMusic != null && !currentMusic.isMute()) {
			// Note: should check if it is already mute since 
			// user may pause and unpause consecutively too fast 
			currentMusic.setMute(true);
//			currentMusic.determineGain(0);
//			lastMusicVol = currentMusicVol;
//			currentMusicVol = 0;
		}
	}

//	public static void enableMasterGain(boolean value) {
//		hasMasterGain = value;
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
//			rand = RandomUtil.getRandomInt(numTracks - LOUD_TRACKS - 1);
//		else
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
				logger.config("Playing background music track #" + (rand + 1) + " '" + name + "'");
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

	public void resumeMusic() {
		if (currentMusic != null && !isSoundDisabled) {
			currentMusic.resume();
//			System.out.println("Resuming music");
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
		else if (!isMusicTrackStopped())
			return;
		else if (masterClock.isPaused())
			return;
		else if (isSoundDisabled)
			return;
		else {
			// Since Areologie.ogg and Fantascape.ogg are 4 mins long, don't need to replay
			// them
			if (currentMusic != null //&& currentMusicTrack.toString().equals(SoundConstants.ST_AREOLOGIE)
					&& play_times < 2) {
				pickANewTrack();
//			} else if (currentMusic != null //&& currentMusicTrack.toString().equals(SoundConstants.ST_FANTASCAPE)
//					&& play_times < 2) {
//				pickANewTrack();
			} else if (currentMusic != null && !currentMusic.isMute() && currentMusic.getVol() != 0
					&& play_times < 4) {
				playMusic(currentMusic.toString());
				play_times++;
			} else {
				pickANewTrack();
			}
		}
	}

	public boolean isSoundDisabled() {
		return isSoundDisabled;
	}

	public static void disableSound() {
		isSoundDisabled = true;
		currentMusicVol = 0;
		currentSoundVol = 0;
		hasMasterGain = false;

		allSoundClips = null;
		currentSoundClip = null;
		currentMusic = null;
		musicTracks = null;
		played_tracks = null;

//		if (mainScene != null)
//			MainScene.disableSound();

	}

	public int getNumTracks() {
		return numTracks;
	}
	
	public void destroy() {
		allSoundClips = null;
//		desktop = null;
		currentSoundClip = null;
		currentMusic = null;
		musicTracks = null;
		played_tracks = null;
	}

	@Override
	public void clockPulse(double time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void uiPulse(double time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
//		if (isPaused) {
//			marqueeTicker.pauseMarqueeTimer(true);
//			if (!isMusicMute())
//				mutePlayer(false, true);
//			if (!isSoundMute())
//				mutePlayer(true, false);
//		} else {
//			marqueeTicker.pauseMarqueeTimer(false);	
//			unmutePlayer(true, true);
//		}
	}

}