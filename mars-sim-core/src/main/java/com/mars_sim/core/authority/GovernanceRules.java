/*
 * Mars Simulation Project
 * GovernanceRules.java
 * @date 2025-11-04
 * @author Barry Evans
 */
package com.mars_sim.core.authority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.role.RoleUtil;

/**
 * Governance rules to control the behaviour of a Settlement council.
 * It defines the RoleTypes that can be assigned, who the leader is, and whether job assignments
 * need approval.
 */
public class GovernanceRules implements Serializable {
	private static final int MAX_POP_CREW = 4; 
	private static final int MIN_POP_CHIEFS = 24;

    private static final int MAX_POP_COMMANDER = 4;
	private static final int MAX_POP_SUB_COMMANDER = 12;
	private static final int POPULATION_WITH_CHIEFS = 24;
	private static final int MAX_POP_ADMINISTRATOR = 96;
	private static final int MAX_POP_DEPUTY_ADMINISTRATOR = 136;
	private static final int MAX_POP_MAYOR = 480;

    private List<RoleType> assignableRoles;
    private RoleType leader;
    private RoleType deputyLeader;

    private List<RoleType> allRoles;
    private boolean jobApproval;

    public GovernanceRules(int pop) {
        		
        this.jobApproval = (pop > MAX_POP_COMMANDER);
		this.allRoles = new ArrayList<>();

		// Phase 1 select workers roles
		if (pop <= MAX_POP_CREW) {
			assignableRoles = RoleUtil.getCrewRoles();
		}
		else {
			assignableRoles = RoleUtil.getSpecialists();
		}
		allRoles.addAll(assignableRoles);
		if (pop >= MIN_POP_CHIEFS) {
			allRoles.addAll(RoleUtil.getChiefs());
		}

		//Phase 2 select command roles
		List<RoleType> commandRoles = new ArrayList<>();
		if (pop <= MAX_POP_COMMANDER) {
			commandRoles.add(RoleType.COMMANDER);
		}

		else if (pop <= MAX_POP_SUB_COMMANDER) {
			commandRoles.add(RoleType.COMMANDER);
			commandRoles.add(RoleType.SUB_COMMANDER);
		}

		else if (pop <= MAX_POP_ADMINISTRATOR) {
			commandRoles.add(RoleType.ADMINISTRATOR);
			commandRoles.add(RoleType.COMMANDER);
		}
		
		else if (pop <= MAX_POP_DEPUTY_ADMINISTRATOR) {
			commandRoles.add(RoleType.ADMINISTRATOR);
			commandRoles.add(RoleType.DEPUTY_ADMINISTRATOR);
			commandRoles.add(RoleType.COMMANDER);
		}
		
		else if (pop <= MAX_POP_MAYOR) {
			commandRoles.add(RoleType.MAYOR);
			commandRoles.add(RoleType.ADMINISTRATOR);
			commandRoles.add(RoleType.DEPUTY_ADMINISTRATOR);
			commandRoles.add(RoleType.COMMANDER);
		}

		else {
			commandRoles.add(RoleType.PRESIDENT);
			commandRoles.add(RoleType.MAYOR);
			commandRoles.add(RoleType.ADMINISTRATOR);
			commandRoles.add(RoleType.DEPUTY_ADMINISTRATOR);
			commandRoles.add(RoleType.COMMANDER);
		}
		
		// Leader & deputy selection
		allRoles.addAll(commandRoles);
		leader = commandRoles.get(0);
		if (commandRoles.size() > 1) {
			deputyLeader = commandRoles.get(1);
		}
	}

    public List<RoleType> getAllRoles() {
        return allRoles;
    }

    /**
     * get the roles that can be assigned by the Governance Council
     * @return
     */
    public List<RoleType> getAssignableRoles() {
        return assignableRoles;
    }

    public RoleType getLeader() {
        return leader;
    }

    public RoleType getDeputyLeader() {
        return deputyLeader;
    }

    /**
     * Do any new Job assignments need approval?
     * @return
     */
    public boolean needJobApproval() {
        return jobApproval;
    }

    /**
     * What is the maximum number of chiefs for a given population size?
     * @param popSize
     * @return
     */
    public int getMaxChiefs(int popSize) {
		 return Math.max(0,popSize - POPULATION_WITH_CHIEFS + 1);
    }
}
