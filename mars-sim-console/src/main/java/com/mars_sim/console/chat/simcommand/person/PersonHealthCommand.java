/*
 * Mars Simulation Project
 * PersonHealthCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.health.BodyRegionType;
import com.mars_sim.core.person.health.Complaint;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.RadiationExposure;
import com.mars_sim.core.person.health.RadiationType;
import com.mars_sim.core.tool.RandomUtil;

/** 
 * Reports on a Persons health.
 */
public class PersonHealthCommand extends AbstractPersonCommand {
	
	// Characteristics that can be set
	private static final String FATIGUE = "Fatigue";
	private static final String HUNGER = "Hunger";
	private static final String THIRST = "Thirst";
	private static final String STRESS = "Stress";
	private static final String PERFORMANCE = "Performance";
	private static final String ENERGY = "Energy";
	private static final String ILLNESS = "Illness";

	private static final List<String> CHARACTERISTICS = List.of(
													FATIGUE, 
													HUNGER,
													STRESS,
													THIRST,
													ILLNESS);

	public PersonHealthCommand() {
		super("h", "health", "About health");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		boolean isExpert = context.getRoles().contains(ConversationRole.EXPERT);
		boolean active = isExpert; // If expert then stay looping until told otherwise
				
		do {
			StructuredResponse responseText = new StructuredResponse();		
			responseText.appendHeading("Health Indicators");
			
			PhysicalCondition pc = person.getPhysicalCondition();
			
			double energy = Math.round(pc.getEnergy()*10.0)/10.0;
	        double stress = pc.getStress();
	        double perf = pc.getPerformanceFactor()*100D;
	                
			String h = !pc.isHungry() ? "(Not Hungry)" : "(Hungry)";
			String t = !pc.isThirsty() ? "(Not Thirsty)" : "(Thirsty)";
			
			responseText.appendLabeledString(THIRST, String.format(CommandHelper.MILLISOL_FORMAT, pc.getThirst()) + " " + t);		
			responseText.appendLabeledString(HUNGER, String.format(CommandHelper.MILLISOL_FORMAT, pc.getHunger()) + " " + h);
			responseText.appendLabeledString(ENERGY, energy + " kJ");
			responseText.appendLabeledString(FATIGUE, String.format(CommandHelper.MILLISOL_FORMAT, pc.getFatigue()));		
			responseText.appendLabeledString(PERFORMANCE, String.format(CommandHelper.PERC_FORMAT, perf));
			responseText.appendLabeledString(STRESS,  String.format(CommandHelper.PERC_FORMAT, stress));		
			responseText.appendLabeledString("Surplus Ghrelin", String.format(CommandHelper.MILLISOL_FORMAT,
											person.getCircadianClock().getSurplusGhrelin()));
			responseText.appendLabeledString("Surplus Leptin", String.format(CommandHelper.MILLISOL_FORMAT,
											 person.getCircadianClock().getSurplusLeptin()));
			
			List<String> probs = pc.getProblems().stream().map(hp -> hp.getComplaint().getType().getName())
												 .toList();
			responseText.appendNumberedList("Problems", probs);
			
			context.println(responseText.getOutput());
			
			// If expert then maybe change values
			if (isExpert) {		        
		        if (context.getBooleanInput("Change value")) {
		        	changeCondition(context, person, pc);
		        }
		        else {
		        	active = false;
		        }
			}
		}
		while (active);
		return true;
	}

	/**
	 * Changes certain characteristics of a condition.
	 * 
	 * @param context
	 * @param pc
	 */
	private void changeCondition(Conversation context,Person person, PhysicalCondition pc) {
		// Choose characteristic
		int choice = CommandHelper.getOptionInput(context, CHARACTERISTICS, "Which attribute to change?");
		if (choice < 0) {
			return;
		}

		String choosen = CHARACTERISTICS.get(choice);
		if (choosen.equals(ILLNESS)) {
			addHealthProblem(context,person, pc);
		}
		else {
			// Simple numeric value
 			int newValue = context.getIntInput("New value:");
			
			context.println("Setting characteristic " + CHARACTERISTICS.get(choice)
							+ " to the new value " + newValue);
			switch(CHARACTERISTICS.get(choice)) {
			case FATIGUE:
				pc.setFatigue(newValue);
				break;
				
			case STRESS:
				pc.setStress(newValue);
				break;
				
			case HUNGER:
				pc.setHunger(newValue);
				break;
				
			case THIRST:
				pc.setThirst(newValue);
				break;
	
			default:
				context.println("Invalid input !");
			}
		}
	}
	
	/**
	 * Adds a new medical problem, to a person.
	 * 
	 * @param context
	 * @param pc
	 */
	private void addHealthProblem(Conversation context, Person person, PhysicalCondition pc) {
	
		// Choose one
		List<Complaint> complaints = new ArrayList<>(SimulationConfig.instance().getMedicalConfiguration().getComplaintList());
		Collections.sort(complaints, Comparator.comparing(Complaint::getType));
		List<String> problems = complaints.stream().map(c -> c.getType().getName()).toList();
		int choice = CommandHelper.getOptionInput(context, problems, "Choose a new Complaint");
		if (choice <= 0) {
			return;
		}
	
		Complaint choosen = complaints.get(choice);
		context.println("Adding new Complaint " + choosen.getType().getName());
		var problem = pc.addMedicalComplaint(choosen);

		// Radition has extra values
		if (choosen.getType() == ComplaintType.RADIATION_SICKNESS) {			
			RadiationExposure exposure = person.getPhysicalCondition().getRadiationExposure();
					
			int region = RandomUtil.getRandomInt(2);
			double buffer = exposure.getBufferDose(region);
			BodyRegionType regionType = switch(region) {
				case 0 -> BodyRegionType.BFO;
				case 1 -> BodyRegionType.OCULAR;
				case 2 -> BodyRegionType.SKIN;
				default -> BodyRegionType.BFO;
			};
			
			var rad = exposure.addDose(RadiationType.SEP, regionType, buffer * 1.2);
			
			HistoricalEvent hEvent = new HistoricalEvent(HistoricalEventType.HAZARD_RADIATION_EXPOSURE,
														rad, rad.toString(), person.getTaskDescription(),
														person.getName(), person, person.getAssociatedSettlement());
			Simulation.instance().getEventManager().registerNewEvent(hEvent);

			person.fireUnitUpdate(UnitEventType.RADIATION_EVENT);
		}

		if (context.getBooleanInput("Will this cause death ?")) {
			context.println(person + " is now dead.");
			person.getPhysicalCondition().recordDead(problem, true, "Act of God");
		}
	}
}
