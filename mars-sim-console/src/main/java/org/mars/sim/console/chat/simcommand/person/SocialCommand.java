package org.mars.sim.console.chat.simcommand.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;

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
		RelationshipManager relationshipManager = context.getSim().getRelationshipManager();

		// My opinions of them
		Map<Person, Double> friends = relationshipManager.getMyOpinionsOfThem(person);
		if (friends.isEmpty()) {
			context.println("I don't have any friends yet.");
		}
		else {
			StructuredResponse response = new StructuredResponse();
			
			response.appendHeading("My Opinion of them");
			List<Person> list = new ArrayList<>(friends.keySet());
			
			response.appendTableHeading("Toward this Person", PERSON_WIDTH, "Score", 6, "My Attitude");

			double sum = 0;
			for (Person friend : list) {
				double score = friends.get(friend);
				sum += score;
				String relation = RelationshipManager.describeRelationship(score);

				score = Math.round(score * 10.0) / 10.0;

				response.appendTableRow(friend.getName(), score, relation);
			}
			response.appendLabeledString("Ave. option of them", "" + sum/list.size());
			response.append(System.lineSeparator());

			response.appendHeading("Their Opinion of me");

			// Their opinions of me
			friends = relationshipManager.getTheirOpinionsOfMe(person);
			list = new ArrayList<>(friends.keySet());
			
			response.appendTableHeading("Person towards me", PERSON_WIDTH, "Score", "Their Attitude");

			sum = 0;
			for (Person friend : list) {
				double score = friends.get(friend);
				sum += score;
				String relation = RelationshipManager.describeRelationship(score);

				score = Math.round(score * 10.0) / 10.0;

				response.appendTableRow(friend.getName(), score, relation);
			}
			response.appendLabeledString("Ave. option of me", "" + sum/list.size());
			
			context.println(response.getOutput());
		}
		
		return true;
	}
}
