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

import javafx.application.Platform;

/**
 * A class to dispatch playback of OGG files to OGGSoundClip.
 */
@SuppressWarnings("restriction")
public class AudioPlayer {

	private static Logger logger = Logger.getLogger(AudioPlayer.class.getName());

	private static int num_tracks;
	
	/** The current clip sound. */
	private OGGSoundClip currentOGGSoundClip;
	private OGGSoundClip currentBackgroundTrack;

	private Map<String, OGGSoundClip> allBackgroundSoundTracks;
	private Map<String, OGGSoundClip> allOGGSoundClips;

	private List<String> soundTracks;
	private MainDesktopPane desktop;

	/** The volume of the audio player (0.0 to 1.0) */
	private float volume = .8f;

	private int play_times = 0;
	
	private boolean hasMasterGain = true;
	
	private boolean lastTrackState;
	private boolean lastEffectState;

	public AudioPlayer(MainDesktopPane desktop) {
		//logger.info("constructor is on " + Thread.currentThread().getName());
		this.desktop = desktop;

		currentOGGSoundClip = null;
		currentBackgroundTrack = null;

		allBackgroundSoundTracks = new HashMap<>();
		allOGGSoundClips = new HashMap<>();
		
		soundTracks = new ArrayList<>();
		soundTracks.add(SoundConstants.ST_FANTASCAPE);
		soundTracks.add(SoundConstants.ST_CITY);
		soundTracks.add(SoundConstants.ST_MISTY);
		soundTracks.add(SoundConstants.ST_MOONLIGHT);
		soundTracks.add(SoundConstants.ST_PUZZLE);
		soundTracks.add(SoundConstants.ST_DREAMY);
		//soundTracks.add(SoundConstants.ST_STRANGE);
		soundTracks.add(SoundConstants.ST_AREOLOGIE);
		soundTracks.add(SoundConstants.ST_MENU);
		soundTracks.add(SoundConstants.ST_AREOLOGIE);
		
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
			setVolume(.8f);
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
		if (!isMute(true, false)) {
			if (desktop.getMainScene() != null) {
					Platform.runLater(() -> {
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
		if (!isMute(false, true)) {
			if (desktop.getMainScene() != null) {
					Platform.runLater(() -> {
						try {
							if (allBackgroundSoundTracks.containsKey(filepath) && allBackgroundSoundTracks.get(filepath) != null) {
								currentBackgroundTrack = allBackgroundSoundTracks.get(filepath);
								currentBackgroundTrack.loop();
								if (play_times < 2) logger.info("Playing the background track " + filepath);
							}
							else {
								currentBackgroundTrack = new OGGSoundClip(filepath);
								allOGGSoundClips.put(filepath, currentBackgroundTrack);
								currentBackgroundTrack.loop();
								if (play_times < 2) logger.info("Playing the background track " + filepath);
							}
						} catch (IOException e) {
							//e.printStackTrace();
							logger.log(Level.SEVERE, "IOException in AudioPlayer's playInBackground()", e.getMessage());
						}
					});
			}

			else {
				SwingUtilities.invokeLater(() -> {
					try {
						if (allBackgroundSoundTracks.containsKey(filepath) && allBackgroundSoundTracks.get(filepath) != null) {
							allBackgroundSoundTracks.get(filepath).loop();
							if (play_times < 2) logger.info("Playing the sound track " + filepath);
						}
						else {
							currentBackgroundTrack = new OGGSoundClip(filepath);
							allOGGSoundClips.put(filepath, currentBackgroundTrack);
							currentBackgroundTrack.loop();
							if (play_times < 2) logger.info("Playing the sound track " + filepath);
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
	 * Gets the volume of the audio player.
	 * @return volume (0.0 to 1.0)
	 */
	public float getVolume() {
		return volume;
	}

	// 2016-09-28 volumeUp()
	public void volumeUp() {
		Platform.runLater(() -> {
			volume = currentBackgroundTrack.getVolume() + .05f;
			if (volume > 1f)
				volume = 1f;
			setVolume();
		});
	}

	// 2016-09-28 volumeDown()
	public void volumeDown() {
		Platform.runLater(() -> {
			volume = currentBackgroundTrack.getVolume() - .05f;
			if (volume < -1f)
				volume = -1f;
			setVolume();
		});
	}

	@SuppressWarnings("restriction")
	public void setVolume() {
		Platform.runLater(() -> {
			if (hasMasterGain) {
				if(!isMute(false, true)) {
					//logger.info("!isMute(false) is " + !isMute(false));
					// 2016-09-28 Added backgroundSoundTrack
					if (currentBackgroundTrack != null)
						if (!currentBackgroundTrack.isMute())	{
							currentBackgroundTrack.setGain(volume);
							//System.out.println("backgroundSoundTrack is " + backgroundSoundTrack);
							//backgroundSoundTrack.resume();//.play();
						}
				}
				else if(!isMute(true, false)) {
					if (currentOGGSoundClip != null)
						if (!currentOGGSoundClip.isMute()) {
							currentOGGSoundClip.setGain(volume);
							//currentOGGSoundClip.resume();
						}

				}
			}
		});
	}

	/**
	 * Sets the volume of the audio player.
	 * @param volume (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setVolume(float volume) {
		//logger.info("setVolume() is on " + Thread.currentThread().getName());
		if (volume < 0F)
			volume = 0F;
		if (volume > 1F)
			volume = 1F;

		this.volume = volume;
		//System.out.println("volume " + volume);
		if (hasMasterGain) {
			if (!isMute(false, true)) {
				//logger.info("!isMute(false) is " + !isMute(false));
				// 2016-09-28 Added backgroundSoundTrack
				if (currentBackgroundTrack != null) {
					if (!currentBackgroundTrack.isMute())	{
						currentBackgroundTrack.setGain(volume);
						//System.out.println("backgroundSoundTrack is " + backgroundSoundTrack);
						//backgroundSoundTrack.resume();
						//backgroundSoundTrack.setMute(false);
					}
				}
			}
			else if (!isMute(true, false)) {
				if (currentOGGSoundClip != null)
					if (!currentOGGSoundClip.isMute())
						currentOGGSoundClip.setGain(volume);

			}
		}

	}


	/**
	 * Checks if the audio player is muted.
	 * @param isEffect is the sound effect mute ?
	 * @param isTrack is the background music mute ?
	 * @return true if mute.
	 */
	public boolean isMute(boolean isEffect, boolean isTrack) {
		boolean result = false;
		if (isEffect) {
			if (currentOGGSoundClip != null) {
				result = currentOGGSoundClip.isMute();
			}
		}
		else if (isTrack) {
			// 2016-09-28 Added backgroundSoundTrack
			if (currentBackgroundTrack != null) {
				result = result || currentBackgroundTrack.isMute();
			}
		}
		
		return result;
	}

	public void restoreSound(boolean isEffect, boolean isTrack) {
		if (isEffect) {
			if (currentOGGSoundClip != null) {
				currentOGGSoundClip.setMute(lastEffectState);
			}
		}
		
		if (isTrack) {
			if (currentBackgroundTrack != null) {
				currentBackgroundTrack.setMute(lastTrackState);
			}
		}
	}
		
	

	public void pauseSound(boolean isEffect, boolean isTrack) {
		if (currentBackgroundTrack != null)
			lastTrackState = currentBackgroundTrack.isMute();
		if (currentOGGSoundClip != null)
			lastEffectState = currentOGGSoundClip.isMute();
		mute(isEffect, isTrack);
	}
	
	/**
	 * Sets the state of the audio player to mute or unmute.
	 * @param mute true if it will be set to mute
	 */
	public void mute(boolean effectMute, boolean trackMute) {
		if (effectMute) {
			if (currentOGGSoundClip != null) {
				//lastEffectState = currentOGGSoundClip.isMute();
				currentOGGSoundClip.setMute(true);
			}
			//else
			//	lastEffectState = true;
		}
		
		if (trackMute) {
			if (currentBackgroundTrack != null) {
				//lastTrackState = currentBackgroundTrack.isMute();
				currentBackgroundTrack.setMute(true);
			}
			//else
			//	lastTrackState = true;
		}
	}

	public void unmute(boolean effectMute, boolean trackMute) {
		if (effectMute) {
			if (currentOGGSoundClip != null) {
				//lastEffectState = currentOGGSoundClip.isMute();
				currentOGGSoundClip.setMute(false);
			}
			//else
				//lastEffectState = true;
		}
		
		if (trackMute) {
			if (currentBackgroundTrack != null) {
				//lastTrackState = currentBackgroundTrack.isMute();
				currentBackgroundTrack.setMute(false);
			}
			//else
				//lastTrackState = true;
		}
	}
	
	//public void cleanAudioPlayer() {
	//	stop();
	//}

	public void enableMasterGain(boolean value) {
		hasMasterGain = value;
	}

	public boolean isBackgroundTrackStopped() {
		if (currentBackgroundTrack == null)
			return true;
		return currentBackgroundTrack.checkState();
	}
	
	public void playRandomBackgroundTrack() {
		if (isBackgroundTrackStopped()) {
			// Since Areologie.ogg and Fantascape.ogg are 4 mins long. Don't need to repeat
			if (currentBackgroundTrack != null
					&& !currentBackgroundTrack.toString().equals("Areologie.ogg") 
					&& !currentBackgroundTrack.toString().equals("Fantascape.ogg")	
					&& play_times < 3) {
				playBackground(currentBackgroundTrack.toString());
				play_times++;
			}
			else {		
				List<String> keys = new ArrayList<String>(soundTracks);
				int rand = 0;
				if (currentBackgroundTrack != null) {
					keys.remove(currentBackgroundTrack.toString());
					rand = RandomUtil.getRandomInt(num_tracks-2);
				} 
				else
					rand = RandomUtil.getRandomInt(num_tracks-1);
				playBackground(keys.get(rand));
				// Reset the play times to 1
				play_times = 1;
			}
		}
	}
	
	public void destroy() {
		allOGGSoundClips = null;
		allBackgroundSoundTracks = null;
		desktop = null;
	}
	
}