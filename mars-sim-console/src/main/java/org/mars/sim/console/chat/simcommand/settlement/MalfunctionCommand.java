/*
 * Mars Simulation Project
 * MalfunctionCommand.java
 * @date 2022-06-14
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.Collection;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to create a malfunction in a Settlement.
 */
public class MalfunctionCommand extends AbstractSettlementCommand {

	public static final MalfunctionCommand MALFUNCTION = new MalfunctionCommand();

	/**
	 * Create a command that is assigned to a command group
	 * @param group
	 */
	private MalfunctionCommand() {
		super("ml", "malfunction", "Display any malfunctions in the buildings");
	}
	
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		StructuredResponse response = new StructuredResponse();
		Collection<Malfunctionable> entities = MalfunctionFactory.getAssociatedMalfunctionables(settlement);


		for(Malfunctionable e : entities) {
			for(Malfunction m : e.getMalfunctionManager().getMalfunctions()) {
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
