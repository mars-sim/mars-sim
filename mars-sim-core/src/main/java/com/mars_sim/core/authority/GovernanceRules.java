/*
 * Mars Simulation Project
 * GovernanceRules.java
 * @date 2025-11-04
 * @author Barry Evans
 */
package com.mars_sim.core.authority;

import java.io.Serializable;
import java.util.List;

import com.mars_sim.core.person.ai.role.RoleType;

/**
 * Governance rules to control the behaviour of a Settlement council.
 * It defines the RoleTypes that can be assigned, who the leader is, and whether job assignments
 * need approval.
 */
public class GovernanceRules implements Serializable {


    private List<RoleType> assignableRoles;
    private List<RoleType> councilRoles;

    private List<RoleType> allRoles;
    private boolean jobApproval;
    private String name;

    GovernanceRules(String name, List<RoleType> allRoles, List<RoleType> assignableRoles, List<RoleType> councilRoles,
			boolean jobApproval) {
		this.assignableRoles = assignableRoles;
		this.councilRoles = councilRoles;
		this.allRoles = allRoles;
		this.jobApproval = jobApproval;
		this.name = name;
	}
	
	/**
	 * get the name of these Governance Rules
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * get all roles defined by the Governance Council
	 * @return
	 */
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

    /**
     * Roles that govern the council and Settlement. Order in terms of importance
     * @return
     */
    public List<RoleType> getCouncilRoles() {
        return councilRoles;
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
    public static int getMaxChiefs(int popSize) {
		 return Math.max(0,popSize - GovernanceFactory.POPULATION_WITH_CHIEFS + 1);
    }
}
