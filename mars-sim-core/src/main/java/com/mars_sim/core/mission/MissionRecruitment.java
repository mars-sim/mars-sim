/*
 * Mars Simulation Project
 * MissionRecruitment.java
 * @date 2026-06-27
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class is responsible for recruiting members for a mission.
 * It evaluates potential candidates based on their:
 * - qualifications
 * - likability
 * - health status
 * The candidates are added to the mission.
 */
public class MissionRecruitment {

	// Plain POJO to help score potential mission members
	private record MemberScore(Worker candidate, double score) {}

    private Mission mission;
    private Person startingMember;
	private int maxMembers;

    public MissionRecruitment(Mission target, Person startingMember) {
        this.mission = target;
        this.startingMember = startingMember;
		this.maxMembers = target.getMissionCapacity();
    }

	/**
	 * Define the maximum members to recruit. By default it takes the fixed Maximum capactiy from the MetaMission.
	 * @param maxMembers New overide number fo rmaximum members.
	 */
	public void setMaxmMembers(int maxMembers) {
		this.maxMembers = maxMembers;
	}	

    /**
     * Attempts to recruit members for the mission from a collection of possible candidates.
     * @param minMembers Minimum number to recruit from these potentials.
     * @param possibles Possible candidates for recruitment.
     * @return Successful recruitment?
     */
    public boolean recruitMembers(int minMembers, Collection<? extends Worker> possibles) {

		List<MemberScore> qualifiedPeople = new ArrayList<>();
		for (var w : possibles) {
			if (!w.equals(startingMember) && isCapableOfMission(w)) {
				// Determine the person's mission qualification.
				double qualification = mission.getMissionQualification(w) * 100D;

				double likability = 50D;
				double groupLikability = 50D;
				if (w instanceof Person p) {
					// Determine how much the recruiter likes the person.
					likability = RelationshipUtil.getOpinionOfPerson(startingMember, p);

					// Get the recruitee's average opinion of all the current mission members.
					List<Person> people = mission.getMembers().stream()
							.filter(Person.class::isInstance)
							.map(Person.class::cast)
							.toList();
					groupLikability = RelationshipUtil.getAverageOpinionOfPeople(p, people);
            	}

				double recruitmentChance = (qualification + likability + groupLikability) / 3D;
				if (recruitmentChance > 0D) {
					// Check if person is the best recruit.
					qualifiedPeople.add(new MemberScore(w, recruitmentChance));
				}
			}
		}

		int actualMax = startingMember.getAssociatedSettlement().getChainOfCommand().getMaxMissionMembers();
		
		// Max can not bigger than mission capacity
		actualMax = Math.min(actualMax, maxMembers);

		// Recruit the most qualified and most liked people first.
		qualifiedPeople.sort(Comparator.comparing(MemberScore::score, Comparator.reverseOrder()));
		while (!qualifiedPeople.isEmpty() && (mission.getMembers().size() < actualMax)) {
			// Try to recruit best person available to the mission.
			MemberScore next = qualifiedPeople.remove(0);
			if (RandomUtil.lessThanRandPercent(next.score)) {
				next.candidate.setMission(mission);
			}
		}
		
		return (mission.getMembers().size() >= minMembers);
    }

	/**
	 * Checks to see if a member is capable of joining a mission.
	 *
	 * @param member the member to check.
	 * @return true if member could join mission.
	 */
	private boolean isCapableOfMission(Worker member) {
		boolean result = false;

		if (member == null) {
			throw new IllegalArgumentException("member is null");
		}

		if (member instanceof Person person) {

			// Make sure person isn't already on a mission.
			boolean onMission = (person.getMind().getMission() != null);

			// Make sure person doesn't have any serious health problems.
			boolean healthProblem = person.getPhysicalCondition().hasSeriousMedicalProblems();

			// Check if person is qualified to join the mission.
			boolean isQualified = (mission.getMissionQualification(person) > 0D);

			if (!onMission && !healthProblem && isQualified) {
				result = true;
			}
		}

		return result;
	}
}