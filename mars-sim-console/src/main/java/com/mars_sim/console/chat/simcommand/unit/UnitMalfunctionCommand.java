/*
 * Mars Simulation Project
 * UnitMalfunctionCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.unit;

import java.util.Collection;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.Unit;
import com.mars_sim.core.malfunction.Malfunction;
import com.mars_sim.core.malfunction.MalfunctionFactory;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;

/**
 * Command to create a malfunction in a Malfunctionable.
 */
public class UnitMalfunctionCommand extends AbstractUnitCommand {

	/**
	 * Creates a command that is assigned to a command group.
	 * 
	 * @param group
	 */
	public UnitMalfunctionCommand(String group) {
		super(group, "ml", "malfunction", "Display any malfunctions");
	}
	
	@Override
	protected boolean execute(Conversation context, String input, Unit source) {
		
		if (!(source instanceof Malfunctionable)) {
			context.println("Sorry ! Can't connect to a Malfunctionable Unit");
			return false;
		}

		StructuredResponse response = new StructuredResponse();
		Collection<Malfunctionable> entities = MalfunctionFactory.getMalfunctionables((Malfunctionable) source);

		for( Malfunctionable e : entities) {
			MalfunctionManager mgr = e.getMalfunctionManager();
			for (Malfunction m : mgr.getMalfunctions()) {
				if (!m.isFixed()) {
					CommandHelper.outputMalfunction(response, e, m);
					response.appendBlankLine();
				}
			}
		}
		context.println(response.getOutput());
		return true;
	}
}
