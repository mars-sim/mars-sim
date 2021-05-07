package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;

/**
 * Get the distance to a specific location. The starting point is the location of the calling
 * Unit.
 */
public class DistanceCommand extends ChatCommand {

	public static final ChatCommand DISTANCE = new DistanceCommand();

	private DistanceCommand() {
		super(TopLevel.SIMULATION_GROUP, "di", "distance", "Distance to a destination");
		setInteractive(true);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		Coordinates start = null;
		
		// If a Unit then that is the start location
		ChatCommand parent = context.getCurrentCommand();
		if (parent instanceof ConnectedUnitCommand) {
			Unit source = ((ConnectedUnitCommand)context.getCurrentCommand()).getUnit();
			start = source.getCoordinates();
			context.println("Start location is " + start);
		}
		else {
			// Ask user
			start = getCoordinates("Start", context);			
		}
		
		boolean result = false;
		// If a start then continue
		if (start != null) {
			Coordinates end = getCoordinates("Destination", context);
			if (end != null) {
				double distance = start.getDistance(end);
				result  = true;
				
				context.println("The distance between (" + start.getCoordinateString() + ") and ("
						+ end.getCoordinateString() + ") is " + Math.round(distance *1_000.0)/1_000.0 + " km");
			}
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
