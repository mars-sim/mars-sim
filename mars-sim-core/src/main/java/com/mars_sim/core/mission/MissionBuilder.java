/*
 * Mars Simulation Project
 * MissionBuilder.java
 * @date 2026-06-27
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * This class is responsible for building a mission.
 * There is a step to select a suitable vehicle.
 * It provides a step for recruiting members for a mission.
 * It evaluates potential candidates based on their:
 * - qualifications
 * - likability
 * - health status
 * The candidates are added to the mission.
 * 
 * 
 */
public class MissionBuilder {

	private static final long MIN_REMAINING_MEMBERS = 2;

	// Plain POJO to help score potential mission members
	private record MemberScore(Worker candidate, double score) {}

    private MetaMission mission;
    private Person startingMember;
	private int maxMembers;
	private int maxPeople;

    public MissionBuilder(MetaMission target, Person startingMember) {
        this.mission = target;
        this.startingMember = startingMember;
		
		var home = startingMember.getAssociatedSettlement();
		int settlementMax = home.getChainOfCommand().getMaxMissionMembers();
		
		// Max can not bigger than mission capacity
		this.maxMembers = Math.min(target.getDefaultCapacity(), settlementMax) - 1; // Subtract 1 for the starting member

		// Count number not on a mission
		var freePeople = home.getCitizens().stream().filter(p -> p.getMind().getMission() == null).count();
		this.maxPeople = (int) Math.min(maxMembers, freePeople - MIN_REMAINING_MEMBERS);
    }

	/**
	 * Select the best vehicle for the mission.
	 * @return Null if no suitable vehicle is available.
	 */
	public Vehicle selectBestVehicle() {
		var home = startingMember.getAssociatedSettlement();
		return mission.selectVehicle(home);
	}

    /**
     * Attempts to recruit members for a potential mission from a collection of possible candidates.
     * @param possibles Possible candidates for recruitment.
     * @return Selected for recruitment members.
     */
    public List<Worker> recruitMembers(Collection<? extends Worker> possibles) {

		List<MemberScore> qualified = ratePossibles(possibles);

		// Recruit the most qualified and most liked people first.
		List<Worker> recruitedMembers = new ArrayList<>();
		qualified.sort(Comparator.comparing(MemberScore::score, Comparator.reverseOrder()));
		while (!qualified.isEmpty() && (recruitedMembers.size() < maxMembers)) {
			// Try to recruit best worker available to the mission.
			MemberScore next = qualified.remove(0);
			if (RandomUtil.lessThanRandPercent(next.score)) {
				// Choose to join but must be a Robot or there must be room for more people in the mission.
				if (next.candidate instanceof Robot || maxPeople-- > 0) {
					recruitedMembers.add(next.candidate);
				} 
			}
		}
		
		return recruitedMembers;
    }

	/**
	 * Rate all the possibles and removes any not suitable. The remaining are ordered according to decreasing suitability.
	 * @param possibles Potential candidates for recruitment.
	 * @return Ordered list of suitable candidates with their scores.
	 */
	private List<MemberScore> ratePossibles(Collection<? extends Worker> possibles) {
		var qualified = new ArrayList<MemberScore>();
		for (var w : possibles) {
			if (!w.equals(startingMember) && isCapableOfMission(w)) {
				// Determine the person's mission qualification.
				double qualification = mission.getWorkerSuitability(w) * 100D;

				double likability = 50D;
				if (w instanceof Person p) {
					// Determine how much the recruiter likes the person.
					likability = RelationshipUtil.getOpinionOfPerson(startingMember, p);
            	}

				double recruitmentChance = (qualification + likability) / 2D;
				recruitmentChance = Math.max(0D, Math.min(100D, recruitmentChance));
				if (recruitmentChance > 0D) {
					// Check if person is the best recruit.
					qualified.add(new MemberScore(w, recruitmentChance));
				}
			}
		}
		return qualified;
	}

	/**
	 * Checks to see if a member is capable of joining a mission. This does not score the suitability.
	 *
	 * @param member the member to check.
	 * @return true if member could join mission.
	 */
	private boolean isCapableOfMission(Worker member) {
		if (member.getMission() == null) {
			if (member instanceof Person person) {

				// Make sure person doesn't have any serious health problems.
				return !person.getPhysicalCondition().hasSeriousMedicalProblems();
			}
			else if (member instanceof Robot robot) {
				return !robot.getMalfunctionManager().hasMalfunction();
			}
		}

		return false;
	}
}