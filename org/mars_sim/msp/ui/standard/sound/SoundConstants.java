/**
 * Mars Simulation Project
 * SoundConstants.java
 * @version 2.78 2005-08-28
 * @author Dima Stepanchuk
 */
package org.mars_sim.msp.ui.standard.sound;

/**
 * File names to sounds used in the user interface.
 */
public interface SoundConstants {
	
	// The root path for sounds.
	public final static String SOUNDS_ROOT_PATH = "sounds/";
	
	// Unit window sounds for rovers.
	public final static String SND_ROVER_MOVING = "rover_moving.wav";
    public final static String SND_ROVER_MALFUNCTION = "rover_malfunction.wav";
    public final static String SND_ROVER_MAINTENANCE = "rover_maintenance.wav";
    public final static String SND_ROVER_PARKED = "";
    
    // Unit window sound for settlements.
    public final static String SND_SETTLEMENT = "";

    // Unit window sounds for people.
    // TODO: Add additional sounds for people based on activity.
    public final static String SND_PERSON = "";
    
    // Unit window sounds for equipment.
    public final static String SND_EQUIPMENT = "";
}