/**
 * Mars Simulation Project
 * SoundConstants.java
 * @version 3.06 2014-01-29
 * @author Dima Stepanchuk
 */
package org.mars_sim.msp.ui.swing.sound;

/**
 * File names to sounds used in the user interface.
 */
public interface SoundConstants {

	/** The root path for sounds. */
	public final static String SOUNDS_ROOT_PATH = "audio/";

	// Unit window sounds for rovers.
	public final static String SND_ROVER_MOVING = "rover_moving.wav";
	public final static String SND_ROVER_MALFUNCTION = "rover_malfunction.wav";
	public final static String SND_ROVER_MAINTENANCE = "rover_maintenance.wav";
	public final static String SND_ROVER_PARKED = "";

	/** Unit window sound for settlements. */
	public final static String SND_SETTLEMENT = "settlement.wav";

	/** Sound for splash. */
	public final static String SND_SPLASH = "splash.wav";

	// Unit window sounds for people.
	public final static String SND_PERSON_FEMALE1 = "female_person1.wav";
	public final static String SND_PERSON_FEMALE2 = "female_person2.wav";
	public final static String SND_PERSON_MALE1 = "male_person1.wav";
	public final static String SND_PERSON_MALE2 = "male_person2.wav";
	public final static String SND_PERSON_DEAD = "person_dead.wav";

	/** Unit window sounds for equipment. */
	public final static String SND_EQUIPMENT = "equipment.wav";

	// Supported sound formats
	public final static String SND_FORMAT_WAV  = ".wav";
	public final static String SND_FORMAT_MP3  = ".mp3";
	public final static String SND_FORMAT_OGG  = ".ogg";
	public final static String SND_FORMAT_MID =  ".mid";
	public final static String SND_FORMAT_MIDI = ".midi";

	/** maximum amount of clips in cache. */
	public final static int MAX_CACHE_SIZE = 5;
}