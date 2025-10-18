/*
 * Mars Simulation Project
 * SoundConstants.java
 * @date 2025-10-14
 * @author Lars Naesbye Christensen (complete rewrite for OGG)
 */

package com.mars_sim.ui.swing.sound;

/**
 * Constants for sounds used in the simulation.
 */
public interface SoundConstants {

	/** The path (relative to classpath) for sounds. */
	public final static String SOUNDS_ROOT_PATH = "audio/";

	/** Sound for splash screen in classic MSP */
	public final static String SND_SPLASH = "splash.ogg";

	/** Unit window sounds for equipment. */
	public final static String SND_EQUIPMENT = "equipment.ogg";

	/** Unit window sounds for people. */
	public final static String SND_PERSON_DEAD = "person_dead.ogg";
	public final static String SND_PERSON_FEMALE1 = "female_person1.ogg";
	public final static String SND_PERSON_FEMALE2 = "female_person2.ogg";
	public final static String SND_PERSON_MALE1 = "male_person1.ogg";
	public final static String SND_PERSON_MALE2 = "male_person2.ogg";

	/** Unit window sounds for rovers. */
	public final static String SND_ROVER_MAINTENANCE = "rover_maintenance.ogg";
	public final static String SND_ROVER_MALFUNCTION = "rover_malfunction.ogg";
	public final static String SND_ROVER_MOVING = "rover_moving.ogg";
	public final static String SND_ROVER_PARKED = "rover_moving.ogg";

	/** Unit window sounds for settlements. */
	public final static String SND_SETTLEMENT = "settlement.ogg";
}
