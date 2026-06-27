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
import com.mars_sim.core.person.ai.mission.AbstractMission;
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
	private record MemberScore(Worker candidate, double leaderLike, double qualification) {
		public double score() {
			return (leaderLike + qualification) / 2D;
		}
	}

    private AbstractMission mission;
    private Worker startingMember;

    public MissionRecruitment(AbstractMission target, Worker startingMember) {
        this.mission = target;
        this.startingMember = startingMember;
    }

    /**
     * Attempts to recruit members for the mission from a collection of possible candidates.
     * @param minMembers Minimum number of members required for the mission.
     * @param possibles Possible candidates for recruitment.
     * @return Successful recruitment?
     */
    public boolean recruitMembers(int minMembers, Collection<? extends Worker> possibles) {

		List<MemberScore> qualifiedPeople = new ArrayList<>();
		for (var w : possibles) {
			if (isCapableOfMission(w)) {
				// Determine the person's mission qualification.
				double qualification = mission.getMissionQualification(w) * 100D;

				// Determine how much the recruiter likes the person.
				double likability = 50D;
				if (startingMember instanceof Person sm && w instanceof Person p) {
					likability = RelationshipUtil.getOpinionOfPerson(sm, p);
				}

				// Check if person is the best recruit.
				qualifiedPeople.add(new MemberScore(w, likability, qualification));
			}
		}

		int max = startingMember.getAssociatedSettlement().getChainOfCommand().getMaxMissionMembers();
		
		// Max can not bigger than mission capacity
		max = Math.min(max, mission.getMissionCapacity());

		// Recruit the most qualified and most liked people first.
		qualifiedPeople.sort(Comparator.comparing(MemberScore::score, Comparator.reverseOrder()));
		while (!qualifiedPeople.isEmpty() && (mission.getMembers().size() < max)) {
			// Try to recruit best person available to the mission.
			MemberScore next = qualifiedPeople.remove(0);
			recruitWorker(next);
		}
		
		return (mission.getMembers().size() >= minMembers);
    }

    
	/**
	 * Attempts to recruit a new person into the mission.
	 *
	 * @param selected the Worker being recruited.
	 */
	private void recruitWorker(MemberScore selected) {
		if (isCapableOfMission(selected.candidate)) {
            double groupLikability = 50D;

            // For a person check the relationships with the fellow members
            if (selected.candidate instanceof Person re) {
                // Get the recruitee's average opinion of all the current mission members.
                List<Person> people = mission.getMembers().stream()
                        .filter(Person.class::isInstance)
                        .map(Person.class::cast)
                        .toList();
                groupLikability = RelationshipUtil.getAverageOpinionOfPeople(re, people);
            }

			double recruitmentChance = (selected.qualification + selected.leaderLike + groupLikability) / 3D;
            recruitmentChance = Math.clamp(recruitmentChance, 0D, 100D);
			if (RandomUtil.lessThanRandPercent(recruitmentChance)) {
				selected.candidate.setMission(mission);
				// NOTE: do not set his shift to ON_CALL until after the mission plan has been approved
			}
		}
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