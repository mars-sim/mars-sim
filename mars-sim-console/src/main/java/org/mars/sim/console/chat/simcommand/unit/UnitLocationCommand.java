package org.mars.sim.console.chat.simcommand.unit;

import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.location.LocationTag;

/**
 * Command to stop speaking with an entity.
 * This is a singleton.
 */
public class UnitLocationCommand extends AbstractUnitCommand {


	public UnitLocationCommand(String groupName) {
		super(groupName, "l", "location", "What is the location.");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit source) {
		
		LocationTag target = source.getLocationTag();
		context.println(target.getExtendedLocations());
		return true;
	}

}
