/*
 * Mars Simulation Project
 * ElevationCommand.java
 * @date 2022-07-15
 * @author Manny Kung
 */

package com.mars_sim.console.chat.simcommand;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.map.location.Coordinates;

/**
 * Get the elevation of a specific location. 
 * Unit.
 */
public class ElevationCommand extends CoordinatesCommand {

	public static final ChatCommand ELEVATION = new ElevationCommand();

	private ElevationCommand() {
		super("el", "elevation",
					"Elevation of a Location", "Location");
	}

	@Override
	protected boolean execute(Conversation context, Coordinates location) {
		boolean result = false;
		
		context.println("Start location: " + location); 

		double elevationMEGDR = TerrainElevation.getMEGDRElevation(location);
		context.println("MEGDR Elevation: " + String.format(CommandHelper.KM_FORMAT, elevationMEGDR));
		
		return result;
	}
}
