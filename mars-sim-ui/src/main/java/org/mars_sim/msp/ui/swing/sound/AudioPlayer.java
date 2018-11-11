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

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.javafx.mainmenu.MainMenu;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * A class to dispatch playback of OGG files to OGGSoundClip.
 */
public class AudioPlayer implements ClockListener {

	private static Logger logger = Logger.getLogger(AudioPlayer.class.getName());

	private final static int LOUD_TRACKS = 6;
	private final static int REPEATING_TRACKS = 4; // track the last 4 tracks and avoid playing them repetitively.

	public final static float DEFAULT_VOL = .5f;

	private static int num_tracks;

	/** The volume of the audio player (0.0 to 1.0) */
	private static double currentMusicVol = DEFAULT_VOL;
	private static double currentSoundVol = DEFAULT_VOL;

	private double lastMusicVol = 0;
	private double lastSoundVol = 0;

	private int play_times = 0;

	private static boolean hasMasterGain = true;
	private static boolean isSoundDisabled = false;

	// private boolean lastMusicState = true;
	// private boolean lastSoundState = true;

	private MainDesktopPane desktop;
	private static MainScene mainScene;

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
		// logger.config("constructor is on " + Thread.currentThread().getName());
		this.desktop = desktop;
		mainScene = desktop.getMainScene();

		masterClock = Simulation.instance().getMasterClock();

		// Add AudioPlayer to MasterClock's clock listener
		masterClock.addClockListener(this);

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
//			soundTracks.add(SoundConstants.ST_SURREAL);
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
			//currentMusicTrack = null;

		}
		// if (UIConfig.INSTANCE.useUIDefault()) {
		// }
	}

	/**
	 * Play a sound clip.
	 * 
	 * @param filepath the file path to the sound file.
	 */
	public void playSound(String filepath) {
		if (!isSoundMute() && filepath != null && !filepath.equals("")) {
			if (mainScene != null)
				// Platform.runLater(() -> {
				loadSound(filepath);
			// });
			else
				SwingUtilities.invokeLater(() -> loadSound(filepath));
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
				currentSoundClip = new OGGSoundClip(filepath);
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
			if (mainScene != null)
//			if (desktop.getMainScene() != null)
				// Platform.runLater(() -> {
				loadMusic(filepath);
			// });
			else
				SwingUtilities.invokeLater(() -> loadMusic(filepath));
		}
	}

	/**
	 * Loads up the music track.
	 * 
	 * @param filepath the file path to the music track.
	 */
	public void loadMusic(String filepath) {
		if (allMusicTracks.containsKey(filepath) && allMusicTracks.get(filepath) != null) {
			currentMusicTrack = allMusicTracks.get(filepath);
			currentMusicTrack.determineGain(currentMusicVol);
			currentMusicTrack.loop();
		} else {
			try {
				currentMusicTrack = new OGGSoundClip(filepath);
				allMusicTracks.put(filepath, currentMusicTrack);
				currentMusicTrack.determineGain(currentMusicVol);
				currentMusicTrack.loop();
			} catch (IOException e) {
				// e.printStackTrace();
				logger.log(Level.SEVERE, "IOException in AudioPlayer's playInBackground()", e.getMessage());
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

	public void musicVolumeUp() {
		lastMusicVol = currentMusicVol;
		double v = currentMusicTrack.getVol() + .05;
		if (v > 1)
			v = 1;
		if (!isSoundDisabled && hasMasterGain && currentMusicTrack != null)
			currentMusicTrack.determineGain(v);
	}

	public void musicVolumeDown() {
		lastMusicVol = currentMusicVol;
		double v = currentMusicTrack.getVol() - .05;
		if (v < 0)
			v = 0;
		if (!isSoundDisabled && hasMasterGain && currentMusicTrack != null)
			currentMusicTrack.determineGain(v);

	}

	public void soundVolumeUp() {
		lastSoundVol = currentSoundVol;
		double v = currentSoundClip.getVol() + .05;
		if (v > 1)
			v = 1;
		if (!isSoundDisabled && hasMasterGain && currentSoundClip != null)
			currentSoundClip.determineGain(v);

	}

	public void soundVolumeDown() {
		lastSoundVol = currentSoundVol;
		double v = currentSoundClip.getVol() - .05;
		if (v < 0)
			v = 0;
		if (!isSoundDisabled && hasMasterGain && currentSoundClip != null)
			currentSoundClip.determineGain(v);

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
		lastMusicVol = currentMusicVol;
		currentMusicVol = volume;

		if (!isSoundDisabled && hasMasterGain && currentMusicTrack != null) {
			currentMusicTrack.determineGain(volume);
			//currentMusicTrack.resume();
		}
	}

	public void restoreLastMusicVolume() {
		if (!isSoundDisabled && hasMasterGain && currentMusicTrack != null) {
			currentMusicTrack.determineGain(lastMusicVol);
			//currentMusicTrack.resume();
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
		lastSoundVol = currentSoundVol;
		currentSoundVol = volume;

		if (!isSoundDisabled && hasMasterGain && currentSoundClip != null) {
			currentSoundClip.determineGain(volume);
		}
	}


	public void restoreLastSoundVolume() {
		if (!isSoundDisabled && hasMasterGain && currentSoundClip != null) {
			currentSoundClip.determineGain(lastSoundVol);
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
		// if (currentMusicTrack != null) {
		// result = currentMusicTrack.isMute() || currentMusicTrack.getVol() == 0;
		// }
		return currentMusicVol == 0;
	}

	/**
	 * Checks if the audio player is muted.
	 * 
	 * @param isEffect is the sound effect mute ?
	 * @param isTrack  is the background music mute ?
	 * @return true if mute.
	 */
	public boolean isSoundMute() {
		// if (currentSoundClip != null) {
		// result = currentSoundClip.isMute() || currentSoundClip.getVol() == 0;
		// }
		return currentSoundVol == 0;
	}


	/**
	 * Unmute the sound clip and/or music clip
	 * 
	 * @param isSound
	 * @param isMusic
	 */
	public void unmutePlayer(boolean isSound, boolean isMusic) {
		if (isSound) {
			if (currentSoundClip != null && currentSoundClip.isMute()) {
				currentSoundClip.setMute(false);
				//currentSoundClip.resume();
			}
			restoreLastSoundVolume();
		}

		if (isMusic) {
			if (currentMusicTrack != null && currentMusicTrack.isMute()) {
				currentMusicTrack.setMute(false);
				resumeMusic();
			}
			restoreLastMusicVolume();
		}
	}

	/**
	 * Mute the sound clip and/or music clip
	 * 
	 * @param mute true if it will be set to mute
	 */
	public void mutePlayer(boolean isSound, boolean isMusic) {
		if (isSound) {
			if (currentSoundClip != null && !currentSoundClip.isMute()) {
				currentSoundClip.setMute(true);
				currentSoundClip.stop();
				currentSoundClip.determineGain(0);
			}
			lastSoundVol = currentSoundVol;
			currentSoundVol = 0;
		}

		if (isMusic) {
			if (currentMusicTrack != null && !currentMusicTrack.isMute()) {
				// Note: should check if it is already mute since 
				// user may pause and unpause consecutively too fast 
				currentMusicTrack.setMute(true);
				currentMusicTrack.determineGain(0);
			}
			lastMusicVol = currentMusicVol;
			currentMusicVol = 0;
		}
	}

	public static void enableMasterGain(boolean value) {
		hasMasterGain = value;
	}

	/**
	 * Checks if the music track ever started or has stopped
	 * 
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
		// At the start of the sim, refrain from playing the last few tracks due to
		// their sudden loudness
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
				logger.config("Playing background music track #" + (rand + 1) + " '" + name + "'");
				// Add the new track
				played_tracks.add((rand));
				// Remove the earliest track
				if (played_tracks.size() > REPEATING_TRACKS)
					played_tracks.remove(0);
				// Reset the play times to 1 for this new track
				play_times = 1;
				// break;
			} else
				rand = RandomUtil.getRandomInt(num_tracks - 1);
		}

	}

	public void resumeMusic() {
		if (currentMusicTrack != null && currentMusicTrack.isPaused()) {
			currentMusicTrack.resume();
		}
		else {
			playRandomMusicTrack();
		}
	}
	
	/**
	 * Play a randomly selected music track
	 */
	public void playRandomMusicTrack() {
		if (isMusicTrackStopped() && !masterClock.isPaused() && !isSoundDisabled()) {
			// Since Areologie.ogg and Fantascape.ogg are 4 mins long, don't need to replay
			// them
			if (currentMusicTrack != null && currentMusicTrack.toString().equals(SoundConstants.ST_AREOLOGIE)
					&& play_times < 2) {
				pickANewTrack();
			} else if (currentMusicTrack != null && currentMusicTrack.toString().equals(SoundConstants.ST_FANTASCAPE)
					&& play_times < 2) {
				pickANewTrack();
			} else if (currentMusicTrack != null && !currentMusicTrack.isMute() && currentMusicTrack.getVol() != 0
					&& play_times < 4) {
				playMusic(currentMusicTrack.toString());
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
		enableMasterGain(false);

		allSoundClips = null;
		allMusicTracks = null;
		currentSoundClip = null;
		currentMusicTrack = null;
		soundTracks = null;
		played_tracks = null;

		if (mainScene != null)
			MainScene.disableSound();

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