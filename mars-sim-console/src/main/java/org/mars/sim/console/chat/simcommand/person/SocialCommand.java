/*
 * Mars Simulation Project
 * SocialCommand.java
 * @date 2022-06-11
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;

/** 
 * Social circle of the Person
 */
public class SocialCommand extends AbstractPersonCommand {
	public static final ChatCommand SOCIAL = new SocialCommand();
	
	private SocialCommand() {
		super("so", "social", "About my social circle");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {

		// My opinions of them
		Map<Person, Double> friends = RelationshipUtil.getMyOpinionsOfThem(person);
		if (friends.isEmpty()) {
			context.println("I don't have any friends yet.");
		}
		else {
			StructuredResponse response = new StructuredResponse();
			
			response.appendHeading("My Opinion of them");
			List<Person> list = new ArrayList<>(friends.keySet());
			
			response.appendTableHeading("Toward this Person", CommandHelper.PERSON_WIDTH, "Score", 6, "My Attitude");

			double sum = 0;
			for (Person friend : list) {
				double score = friends.get(friend);
				sum += score;
				String relation = RelationshipUtil.describeRelationship(score);

				score = Math.round(score * 10.0) / 10.0;

				response.appendTableRow(friend.getName(), score, relation);
			}
			response.appendLabeledString("Ave. option of them", "" + sum/list.size());
			response.appendBlankLine();

			response.appendHeading("Their Opinion of me");

			// Their opinions of me
			friends = RelationshipUtil.getTheirOpinionsOfMe(person);
			list = new ArrayList<>(friends.keySet());
			
			response.appendTableHeading("Person towards me", CommandHelper.PERSON_WIDTH, "Score", "Their Attitude");

			sum = 0;
			for (Person friend : list) {
				double score = friends.get(friend);
				sum += score;
				String relation = RelationshipUtil.describeRelationship(score);

				score = Math.round(score * 10.0) / 10.0;

				response.appendTableRow(friend.getName(), score, relation);
			}
			response.appendLabeledString("Ave. option of me", "" + sum/list.size());
			
			context.println(response.getOutput());
		}
		
		return true;
	}
}
