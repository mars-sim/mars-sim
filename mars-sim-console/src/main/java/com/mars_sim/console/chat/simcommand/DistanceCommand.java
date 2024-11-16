/*
 * Mars Simulation Project
 * DistanceCommand.java
 * @date 2022-07-15
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.map.location.Coordinates;

/**
 * Get the distance to a specific location. The starting point is the location of the calling
 * Unit.
 */
public class DistanceCommand extends CoordinatesCommand {

	public static final ChatCommand DISTANCE = new DistanceCommand();

	private DistanceCommand() {
		super("di", "distance", "Distance to a destination", "Start");
	}

	@Override
	public boolean execute(Conversation context, Coordinates start) {
		
		boolean result = false;
		// If a start then continue
		if (start != null) {
			Coordinates end = CommandHelper.getCoordinates("Destination", context);
			if (end != null) {
				double distance = start.getDistance(end);
				result  = true;
				
				context.println("The distance between (" + start.getFormattedString() + ") and ("
						+ end.getFormattedString() + ") is " + Math.round(distance *1_000.0)/1_000.0 + " km");
			}
		}
		
		return result;
	}
}
