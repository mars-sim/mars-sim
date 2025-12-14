/*
 * Mars Simulation Project
 * GovernanceFactory.java
 * @date 2025-11-04
 * @author Barry Evans
 */
package com.mars_sim.core.authority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.data.Range;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.role.RoleUtil;

/**
 * Factory class to create GovernanceRules based on population size.
 * These are cached for reuse and readonly
 */
public class GovernanceFactory {
	private static final int MAX_POP_CREW = 4; 
	private static final int MIN_POP_CHIEFS = 24;

    private static final int MAX_POP_COMMANDER = 4;
	private static final int MAX_POP_SUB_COMMANDER = 12;
	static final int POPULATION_WITH_CHIEFS = 24;
	private static final int MAX_POP_ADMINISTRATOR = 96;
	private static final int MAX_POP_DEPUTY_ADMINISTRATOR = 136;
	private static final int MAX_POP_MAYOR = 480;

    private static final Map<Range, GovernanceRules> RULE_RANGES = 
            Map.of(new Range(1, MAX_POP_CREW), create("Crew", MAX_POP_CREW),
                   new Range(MAX_POP_CREW + 1D, MAX_POP_SUB_COMMANDER), create("Committee", MAX_POP_SUB_COMMANDER),
                   new Range(MAX_POP_SUB_COMMANDER + 1D, MAX_POP_ADMINISTRATOR), create("Council", MAX_POP_ADMINISTRATOR),
                   new Range(MAX_POP_ADMINISTRATOR + 1D, MAX_POP_DEPUTY_ADMINISTRATOR), create("Council Chamber", MAX_POP_DEPUTY_ADMINISTRATOR),
                   new Range(MAX_POP_DEPUTY_ADMINISTRATOR + 1D, MAX_POP_MAYOR), create("Cabinet", MAX_POP_MAYOR),
                   new Range(MAX_POP_MAYOR + 1D, Integer.MAX_VALUE), create("Parliament", Integer.MAX_VALUE)
                   );
    
    private GovernanceFactory() {
        // Prevent instantiation
    }

    /**
     * Get the most approprate GovernanceRules for the given population size
     * @param population
     * @return
     */
    public static GovernanceRules getByPopulation(int population) {
        for (var entry : RULE_RANGES.entrySet()) {
            if (entry.getKey().isBetween(population)) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("No GovernanceRules found for population: " + population);
    }

    /**
     * Factory method that creates GovernanceRules based on max population size
     * @param pop
     * @return
     */
    private static GovernanceRules create(String name, int pop) {
     		
		List<RoleType> allRoles = new ArrayList<>();

		// Phase 1 select workers roles
        var assignableRoles = (pop <= MAX_POP_CREW) ? RoleUtil.getCrewRoles() : RoleUtil.getSpecialists();
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
        return new GovernanceRules(name, allRoles, assignableRoles, commandRoles, (pop > MAX_POP_COMMANDER));
    }
}
