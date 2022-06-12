/*
 * Mars Simulation Project
 * SocialCommand.java
 * @date 2022-06-11
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.structure.Settlement;

public class SocialCommand extends AbstractSettlementCommand {
	public static final ChatCommand SOCIAL = new SocialCommand();

	private SocialCommand() {
		super("so", "social", "Settlement social status");
	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		double score = RelationshipUtil.getRelationshipScore(settlement);

		context.println(String.format("%s's social score : %3f%n", settlement.getName(), score));
		
		return true;
	}

}
