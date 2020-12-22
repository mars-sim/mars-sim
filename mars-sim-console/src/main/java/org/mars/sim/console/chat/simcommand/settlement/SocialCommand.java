package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.structure.Settlement;

public class SocialCommand extends AbstractSettlementCommand {
	public static final ChatCommand SOCIAL = new SocialCommand();

	private SocialCommand() {
		super("so", "social", "Settlement social status");
	}

	@Override
	protected void execute(Conversation context, String input, Settlement settlement) {

		RelationshipManager relationshipManager = context.getSim().getRelationshipManager();
		double score = relationshipManager.getRelationshipScore(settlement);

		context.println(String.format("%s's social score : %3f%n", settlement.getName(), score));
	}

}
