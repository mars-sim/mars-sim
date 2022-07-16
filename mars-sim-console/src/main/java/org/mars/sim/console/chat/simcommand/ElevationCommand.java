/*
 * Mars Simulation Project
 * ElevationCommand.java
 * @date 2022-07-15
 * @author Manny Kung
 */

package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.environment.TerrainElevation;

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
		double elevation = 0;
		boolean result = false;
		
		// If a Unit then that is the start location
		ChatCommand parent = context.getCurrentCommand();
		if (parent instanceof ConnectedUnitCommand) {
			Unit source = ((ConnectedUnitCommand)context.getCurrentCommand()).getUnit();
			location = source.getCoordinates();
			result = true;
			elevation = source.getGroundElevation();
			context.println("Start location is " + location 
					+ " at " + Math.round(elevation * 1000.0)/1000.0 + " km.");
		}
		else {
			// Ask user
			location = getCoordinates("Start", context);	
			result = true;
			elevation = TerrainElevation.getMOLAElevation(location);
			context.println("The elevation of this location is " 
					+ Math.round(elevation * 1000.0)/1000.0 + " km.");
		}
		
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
