/*
 * Mars Simulation Project
 * IllnessCommand.java
 * @date 2023-11-02
 * @author Manny Kung
 */

package com.mars_sim.console.chat.simcommand.person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.hazard.HazardEvent;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.health.BodyRegionType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.Radiation;
import com.mars_sim.core.person.health.RadiationExposure;
import com.mars_sim.core.person.health.RadiationType;
import com.mars_sim.tools.util.RandomUtil;

/** 
 * 
 */
public class IllnessCommand extends AbstractPersonCommand {
	public static final ChatCommand ILLNESS = new IllnessCommand();
	
	private IllnessCommand() {
		super("il", "illness", "Get ill");
		
		setInteractive(true);
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		
		List<ComplaintType> complaints = Arrays.asList(ComplaintType.values());

		List<String> complaintNames = new ArrayList<>();
		for (ComplaintType c: complaints) {
			complaintNames.add(c.getName());
		}
		Collections.sort(complaintNames);
	
		context.println("");
		
		// Choose one
		int choice = CommandHelper.getOptionInput(context, complaintNames, 
				"Pick an illness from above by entering a number");
		
		if (choice < 0) {
			context.println("No illness was chosen. Try again.");
			return false;
		}

		ComplaintType complaintType = complaints.get(choice);
		
		HealthProblem problem = new HealthProblem(complaintType, person);
		person.getPhysicalCondition().addMedicalComplaint(problem.getComplaint());
	
		if (complaintNames.get(choice).equalsIgnoreCase(ComplaintType.RADIATION_SICKNESS.getName())) {			
			
			RadiationExposure exposure = person.getPhysicalCondition().getRadiationExposure();
			Radiation rad = null;
					
			int region = RandomUtil.getRandomInt(2);
			
			double buffer = exposure.getBufferDose(region);
			
			BodyRegionType regionType = null;
			
			if (region == 0) {
				regionType = BodyRegionType.BFO;
			}
			else if (region == 1) {
				regionType = BodyRegionType.OCULAR;
			}
			else if (region == 2) {
				regionType = BodyRegionType.SKIN;
			}
			
			rad = exposure.addDose(RadiationType.SEP, regionType, buffer * 1.2);
			
			HistoricalEvent hEvent = new HazardEvent(EventType.HAZARD_RADIATION_EXPOSURE,
					rad,
					rad.toString(),
					person.getTaskDescription(),
					person.getName(), 
					person
					);
			Simulation.instance().getEventManager().registerNewEvent(hEvent);

			person.fireUnitUpdate(UnitEventType.RADIATION_EVENT);
		}
	
		context.println("");
		context.println("You picked the illness '" + complaintType + "'.");
		
		GenderType type = person.getGender();
		String pronoun = "him";
		if (type == GenderType.FEMALE)
			pronoun = "her";
		
		context.println("");
		String toSave = context.getInput("Do you want " 
				+ pronoun
				+ " to be dead (Y/N)?");
		
        if ("Y".equalsIgnoreCase(toSave)) {
            context.println(person + " is now dead.");
            person.getPhysicalCondition().recordDead(problem, true, 
    				"I got inflicted with " + complaintType + " (not by my own choice).");
        }
        else {
        	context.println(person + " suffered from'" + complaintType + "'.");
        }
		
		context.println("");
		
		return true;
	}
}
