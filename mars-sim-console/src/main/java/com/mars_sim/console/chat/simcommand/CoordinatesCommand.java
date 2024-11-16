/*
 * Mars Simulation Project
 * CoordinatesCommand.java
 * @date 2024-11-03
 * @author Barry Evans
 */
package com.mars_sim.console.chat.simcommand;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;

/**
 * This is a Chat command that executes on a Coordinate. The Coordinate can be inherited
 * from a connected entity if it is a SurfacePOI or user entered.
 */
public abstract class CoordinatesCommand extends ChatCommand {

    private static final String COORDINATES_GROUP = "Coordinates";
    private String locationName;
    
    protected CoordinatesCommand(String shortCommand, String longCommand,
                    String description, String location) {
        super(COORDINATES_GROUP, shortCommand, longCommand, description);
        
        setInteractive(true);
        this.locationName = location;
    }

    /**
     * Execute a command based on a user's input. This will identify a target
     * Coordinate to be executed
     */
    @Override
    public final boolean execute(Conversation context, String input) {
        Coordinates location = null;

		// If connected to a Unit that can be on the surface; use it's coordinates
		ChatCommand parent = context.getCurrentCommand();
		if ((parent instanceof ConnectedUnitCommand c) 
            && (c.getUnit() instanceof SurfacePOI s)) {
			location = s.getCoordinates();
		}
		else {
			// Ask user
			location =  CommandHelper.getCoordinates(locationName, context);	
			if (location == null)
				return false;
		}
		
        return execute(context, location);
    }

    /**
     * Execute thsi command against as specific Coordinate.
     * @param context Current context
     * @param location Location to apply the command to
     * @return
     */
    protected abstract boolean execute(Conversation context, Coordinates location);
}
