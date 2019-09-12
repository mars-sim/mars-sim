/**
 * Mars Simulation Project
 * SoundConstants.java
 * @version 3.1.0 2017-09-18
 * @author Lars Naesbye Christensen (complete rewrite for OGG)
 */

package org.mars_sim.msp.ui.swing.sound;

/**
 * Constants for sounds used in the simulation.
 */
public interface SoundConstants {

	/** The path (relative to classpath) for sounds. */
	public final static String SOUNDS_ROOT_PATH = "audio/";

	/** Sound for splash screen in classic MSP */
//	public final static String SND_SPLASH = "splash.ogg"; // can perhaps be
															// deleted

	/** Music tracks */
//	public final static String ST_FANTASCAPE = "Fantascape.ogg";
//	public final static String ST_CITY = "City_Beneath_the_Waves.ogg";
//	public final static String ST_MISTY = "Misty_Bog.ogg";
//	public final static String ST_MOONLIGHT = "Moonlight_Flying.ogg";
//	public final static String ST_PUZZLE = "Puzzle_Game.ogg";
//	public final static String ST_AREOLOGIE = "Areologie.ogg";
//	public final static String ST_MENU = "Game_Menu.ogg";
//	
//	public final static String ST_BEDTIME = "Bedtime_Story.ogg";
//	public final static String ST_BOG_CREATURES = "Bog_Creatures_on_the_Move.ogg";
//	public final static String ST_CLIPPITY = "Clippity_Clop.ogg";
//	public final static String ST_LOST_JUNGLE = "Lost_Jungle.ogg";
//	public final static String ST_MONKEY = "Monkey_Drama.ogg";
//	public final static String ST_ONE_WORLD = "One_World.ogg";
//	public final static String ST_SURREAL = "Surreal_Chase.ogg";
	
	
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