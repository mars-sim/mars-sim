/**
 * Mars Simulation Project
 * AudioPlayer.java
 * @version 3.1.0 2017-01-24
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
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.UIConfig;

//import javafx.application.Platform;

/**
 * A class to dispatch playback of OGG files to OGGSoundClip.
 */
@SuppressWarnings("restriction")
public class AudioPlayer {

	private static Logger logger = Logger.getLogger(AudioPlayer.class.getName());

	private static int num_tracks;
	
	/** The volume of the audio player (0.0 to 1.0) */
	private float musicVolume = .8f;
	private float effectVolume = .8f;

	private int play_times = 0;

	private boolean hasMasterGain = true;
	
	private boolean lastMusicState;
	private boolean lastEffectState;

	private MainDesktopPane desktop;
	
	/** The current clip sound. */
	private static OGGSoundClip currentOGGSoundClip;
	private static OGGSoundClip currentBackgroundTrack;

	private static Map<String, OGGSoundClip> allBackgroundSoundTracks;
	private static Map<String, OGGSoundClip> allOGGSoundClips;

	private static List<String> soundTracks;
	private static List<Integer> previous_tracks = new ArrayList<>();
	

	public AudioPlayer(MainDesktopPane desktop) {
		//logger.info("constructor is on " + Thread.currentThread().getName());
		this.desktop = desktop;

		currentOGGSoundClip = null;
		currentBackgroundTrack = null;

		allBackgroundSoundTracks = new HashMap<>();
		allOGGSoundClips = new HashMap<>();
		
		soundTracks = new ArrayList<>();
		soundTracks.add(SoundConstants.ST_AREOLOGIE);
		soundTracks.add(SoundConstants.ST_FANTASCAPE);
		soundTracks.add(SoundConstants.ST_PUZZLE);
		soundTracks.add(SoundConstants.ST_CITY);
		soundTracks.add(SoundConstants.ST_MISTY);
		soundTracks.add(SoundConstants.ST_MOONLIGHT);
		soundTracks.add(SoundConstants.ST_MENU);
			
		soundTracks.add(SoundConstants.ST_ONE_WORLD);
		soundTracks.add(SoundConstants.ST_BEDTIME);
		soundTracks.add(SoundConstants.ST_BOG_CREATURES);
		soundTracks.add(SoundConstants.ST_LOST_JUNGLE);
		soundTracks.add(SoundConstants.ST_CLIPPITY);
		soundTracks.add(SoundConstants.ST_MONKEY);
		soundTracks.add(SoundConstants.ST_SURREAL);

		num_tracks = soundTracks.size();
		
		for (String p : soundTracks) {
			try {
				allBackgroundSoundTracks.put(p, new OGGSoundClip(p));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (UIConfig.INSTANCE.useUIDefault()) {
			//setMute(false, false);
			setMusicVolume(.8f);
			setEffectVolume(.8f);
		} else {
			//setMute(UIConfig.INSTANCE.isMute(), UIConfig.INSTANCE.isMute());
		}
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
								//if (play_times < 2) logger.info("Playing the background track " + filepath);
							}
							else {
								currentBackgroundTrack = new OGGSoundClip(filepath);
								allBackgroundSoundTracks.put(filepath, currentBackgroundTrack);
								currentBackgroundTrack.loop();
								//if (play_times < 2) logger.info("Playing the background track " + filepath);
							}
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
							allBackgroundSoundTracks.get(filepath).loop();
							if (play_times == 1) logger.info("Playing the sound track " + filepath);
						}
						else {
							currentBackgroundTrack = new OGGSoundClip(filepath);
							allBackgroundSoundTracks.put(filepath, currentBackgroundTrack);
							currentBackgroundTrack.loop();
							if (play_times == 1) logger.info("Playing the sound track " + filepath);
						}
					} catch (IOException e) {
						//e.printStackTrace();
						logger.log(Level.SEVERE, "IOException in AudioPlayer's playInBackground()", e.getMessage());
					}
				});
			}
		}
	}

	/**
	 * Play the clip in a loop.
	 *
	 * @param filepath
	 *            the filepath to the sound file.
	 
	public void loop(String filepath) {
		try {
			// 2016-09-28 Replaced currentOGGSoundClip with backgroundSoundTrack for looping
			backgroundSoundTrack = new OGGSoundClip(filepath);
			backgroundSoundTrack.loop();

		} catch (IOException e) {
			//e.printStackTrace();
			logger.log(Level.SEVERE, "IOException in AudioPlayer's loop()", e.getMessage());
		}

	}
*/
	
	/**
	 * Stops the playing clip.
	 
	public void stop() {
		if (currentOGGSoundClip != null) {
			currentOGGSoundClip.stop();
			currentOGGSoundClip = null;
		}
		// 2016-09-28 Added backgroundSoundTrack
		if (backgroundSoundTrack != null) {
			backgroundSoundTrack.stop();
			backgroundSoundTrack = null;
		}
	}
*/
	
	/**
	 * Gets the volume of the background music.
	 * @return volume (0.0 to 1.0)
	 */
	public float getMusicVolume() {
		return musicVolume;
	}

	/**
	 * Gets the volume of the sound effect.
	 * @return volume (0.0 to 1.0)
	 */
	public float getEffectVolume() {
		return effectVolume;
	}
	
	public void musicVolumeUp() {
		//Platform.runLater(() -> {
			musicVolume = currentBackgroundTrack.getVolume() + .05f;
			if (musicVolume > 1f)
				musicVolume = 1f;
			setMusicVolume();
		//});
	}

	public void musicVolumeDown() {
		//Platform.runLater(() -> {
			musicVolume = currentBackgroundTrack.getVolume() - .05f;
			if (musicVolume < -1f)
				musicVolume = -1f;
			setMusicVolume();
		//});
	}

	public void effectVolumeUp() {
		//Platform.runLater(() -> {
			effectVolume = currentOGGSoundClip.getVolume() + .05f;
			if (effectVolume > 1f)
				effectVolume = 1f;
			setEffectVolume();
		//});
	}

	public void effectVolumeDown() {
		//Platform.runLater(() -> {
			effectVolume = currentOGGSoundClip.getVolume() - .05f;
			if (effectVolume < -1f)
				effectVolume = -1f;
			setEffectVolume();
		//});
	}
	
	@SuppressWarnings("restriction")
	public void setMusicVolume() {
		//Platform.runLater(() -> {
			if (hasMasterGain && !isMusicMute() && currentBackgroundTrack != null && !currentBackgroundTrack.isMute()){
				currentBackgroundTrack.setGain(musicVolume);
			}
		//});
	}

	@SuppressWarnings("restriction")
	public void setEffectVolume() {
		//Platform.runLater(() -> {
			if (hasMasterGain && !isEffectMute()&& currentOGGSoundClip != null && !currentOGGSoundClip.isMute()){
				currentOGGSoundClip.setGain(effectVolume);
			}
		//});
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
		this.musicVolume = volume;
		
		if (hasMasterGain && !isMusicMute() && currentBackgroundTrack != null && !currentBackgroundTrack.isMute()){
			currentBackgroundTrack.setGain(volume);
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
		this.effectVolume = volume;

		if (hasMasterGain && !isEffectMute() && currentOGGSoundClip != null && !currentOGGSoundClip.isMute()) {
			currentOGGSoundClip.setGain(volume);
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
			result = currentBackgroundTrack.isMute() || currentBackgroundTrack.getGain() == 0;
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
			result = currentOGGSoundClip.isMute() || currentOGGSoundClip.getGain() == 0 ;
		}

		return result;
	}

	public void restoreSound(boolean isEffect, boolean isTrack) {
		if (isEffect && currentOGGSoundClip != null) {
			currentOGGSoundClip.setMute(lastEffectState);
			setEffectVolume();
		}
		
		if (isTrack && currentBackgroundTrack != null) {
			currentBackgroundTrack.setMute(lastMusicState);
			setMusicVolume();
		}
	}
		
	

	public void pauseSound(boolean isEffect, boolean isTrack) {
		if (currentBackgroundTrack != null)
			lastMusicState = currentBackgroundTrack.isMute();
		if (currentOGGSoundClip != null)
			lastEffectState = currentOGGSoundClip.isMute();
		
		mute(isEffect, isTrack);
	}
	
	/**
	 * Sets the state of the audio player to mute or unmute.
	 * @param mute true if it will be set to mute
	 */
	public void mute(boolean effectMute, boolean trackMute) {
		if (effectMute && currentOGGSoundClip != null) {
			currentOGGSoundClip.setMute(true);
			currentOGGSoundClip.setGain(0);
		}
		
		if (trackMute && currentBackgroundTrack != null) {
			currentBackgroundTrack.setMute(true);
			currentBackgroundTrack.setGain(0);
		}
	}

	public void unmute(boolean effectMute, boolean trackMute) {
		if (effectMute && currentOGGSoundClip != null) {
			currentOGGSoundClip.setMute(false);
			setMusicVolume();
		}
		
		if (trackMute && currentBackgroundTrack != null) {
			currentBackgroundTrack.setMute(false);
			setEffectVolume();
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
	 * Play a randomly selected music track
	 */
	public void playRandomBackgroundTrack() {
		if (isBackgroundTrackStopped()) {
			// Since Areologie.ogg and Fantascape.ogg are 4 mins long. Don't need to repeat
			if (currentBackgroundTrack != null
				&& !currentBackgroundTrack.isMute() && currentBackgroundTrack.getGain() != 0
				&& play_times < 3) {
				playBackground(currentBackgroundTrack.toString());
				play_times++;
			}
			else if (currentBackgroundTrack != null
				&& currentBackgroundTrack.toString().equals(SoundConstants.ST_AREOLOGIE) 
				&& play_times < 1) {
				; // empty
			}
			else if (currentBackgroundTrack != null
				&& currentBackgroundTrack.toString().equals(SoundConstants.ST_FANTASCAPE) 
				&& play_times < 2)	{
				playBackground(currentBackgroundTrack.toString());
				play_times++;
			}
			else {		
				int rand = 0;
				// At the start of the sim, refrain from playing the last 3 tracks due to their sudden loudness
				if (previous_tracks.isEmpty()) {
						rand = RandomUtil.getRandomInt(num_tracks-4);
				}
				else
					RandomUtil.getRandomInt(num_tracks-1);
				boolean not_repeated = false;
				// Do not repeat the last 3 music tracks just played
				while (!not_repeated) {
					if (previous_tracks.isEmpty())
						not_repeated = true;
					else {
						for (int t : previous_tracks) {
							if (rand != t) {
								not_repeated = true;
							}
						}
					}
					if (not_repeated) {
						String name = soundTracks.get(rand);
						playBackground(soundTracks.get(rand));
						logger.info("Playing the background music track #" + (rand+1) + " '" + name + "'");
						// Remove the earliest track 
						if (!previous_tracks.isEmpty())
							previous_tracks.remove(0);
						// Add the new track
						previous_tracks.add((rand+1));
						// Reset the play times to 1
						play_times = 1;
						break;
					}
					else
						// need to pick another rand
						rand = RandomUtil.getRandomInt(num_tracks-1);
				}			
/*				
				if (currentBackgroundTrack != null) {	
					keys.remove(currentBackgroundTrack.toString());
					rand = RandomUtil.getRandomInt(num_tracks-2);
				} 
				else
					rand = RandomUtil.getRandomInt(num_tracks-1);
*/				
			}
		}
	}
	
	public void destroy() {
		allOGGSoundClips = null;
		allBackgroundSoundTracks = null;
		desktop = null;
	}
	
}