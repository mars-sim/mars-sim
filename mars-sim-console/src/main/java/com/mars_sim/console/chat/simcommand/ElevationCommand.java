/*
 * Mars Simulation Project
 * ElevationCommand.java
 * @date 2022-07-15
 * @author Manny Kung
 */

package com.mars_sim.console.chat.simcommand;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.Unit;
import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.map.location.Coordinates;

/**
 * Get the elevation of a specific location. 
 * Unit.
 */
public class ElevationCommand extends ChatCommand {

	public static final ChatCommand ELEVATION = new ElevationCommand();

	private ElevationCommand() {
		super(TopLevel.SIMULATION_GROUP, "el", "elevation", "Elevation of a Location");
		setInteractive(true);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		Coordinates location = null;
		boolean result = false;
		
		// If a Unit then that is the start location
		ChatCommand parent = context.getCurrentCommand();
		if (parent instanceof ConnectedUnitCommand) {
			Unit source = ((ConnectedUnitCommand)context.getCurrentCommand()).getUnit();
			location = source.getCoordinates();
			result = true;
		}
		else {
			// Ask user
			location = getCoordinates("Start", context);	
			if (location == null)
				return false;
			result = true;
		}
		
		context.println("Start location: " + location); 

		double elevationMEGDR = TerrainElevation.getMEGDRElevation(location);
		context.println("MEGDR Elevation: " + String.format(CommandHelper.KM_FORMAT, elevationMEGDR));
		
		return result;
	}

	private Coordinates getCoordinates(String desc, Conversation context) {
		double lat1 = 0;
		double lon1 = 0;
		boolean good = false;
		
		//Get lat
		do {
			try {
				String latitudeStr1 = context.getInput("What is the latitude (e.g. 10.03 N, 5.01 S) of the " 
						+ desc.toLowerCase() + " coordinate ?");
				if (latitudeStr1.equalsIgnoreCase("quit") || latitudeStr1.equalsIgnoreCase("/q")
						|| latitudeStr1.isBlank())
					return null;
				else {
					lat1 = Coordinates.parseLatitude2Phi(latitudeStr1);
					good = true;
				}
			} catch(IllegalStateException e) {
				context.println("Not a valid format");
				good = false;
			}
		} while (!good);
		
		do {
			try {
				String longitudeStr = context.getInput("What is the longitude (e.g. 5.09 E, 18.04 W) of the "
						+ desc.toLowerCase() + " coordinate ?");
				if (longitudeStr.equalsIgnoreCase("quit") || longitudeStr.equalsIgnoreCase("/q")
						|| longitudeStr.isBlank())
					return null;
				else {
					lon1 = Coordinates.parseLongitude2Theta(longitudeStr);
					good = true;
				}
			} catch(IllegalStateException e) {
				context.println("Not a valid format");
				good = false;
			}
		} while (!good);
		
		return new Coordinates(lat1, lon1);
	}

}
