/**
 * Mars Simulation Project
 * UnitInspector.java
 * @version 
 * @author Dima Stepanchuk
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.ui.standard.sound.AudioPlayer;
import org.mars_sim.msp.ui.standard.sound.SoundConstants;

public class UnitInspector implements Runnable {

	private AudioPlayer aPlayer;
	private static final UnitInspector globalInstance=new UnitInspector();
	private UnitInspector(){
		aPlayer=new AudioPlayer();
	}
	public static UnitInspector getGlobalInstance()
	{
		return globalInstance;
	}
	public void focused(final Unit unit)
	{
		
		resolveAndPlaySound(unit);
		
	}
	public void unFocused(final Unit unit)
	{
		//stop playing sound
		aPlayer.stop();
	}
	
	private void resolveAndPlaySound(final Unit unit)
	{
		String soundType = unit.getSound();

		// temp code
		if (soundType.equals(SoundConstants.SND_VEHICLE_MOVING)) {
			aPlayer.loop("sounds/VehicleMoving.wav");

		} else if (soundType.equals(SoundConstants.SND_VEHICLE_MALFUNCTION)) {
			aPlayer.loop("sounds/VehicleMalf.wav");

		} else if (soundType.equals(SoundConstants.SND_VEHICLE_MAINTENANCE)) {
			aPlayer.loop("sounds/VehicleMaint.wav");

		}
	}
	public void run() {
		// TODO Auto-generated method stub

	}

}
