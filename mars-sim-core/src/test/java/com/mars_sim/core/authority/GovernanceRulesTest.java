package com.mars_sim.core.authority;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.role.RoleUtil;

class GovernanceRulesTest {

    @ParameterizedTest
    @CsvSource({
        "1, COMMANDER, , false",
        "10, COMMANDER, SUB_COMMANDER, true",
        "20, ADMINISTRATOR, COMMANDER, true",
        "100, ADMINISTRATOR, DEPUTY_ADMINISTRATOR, true",
        "200, MAYOR, ADMINISTRATOR, true",
        "500, PRESIDENT, MAYOR, true",
        })
    void testGetLeader(int population, RoleType leader, RoleType deputyLeader, boolean jobApproval) {
        RoleUtil.initialize();
        GovernanceRules rules = GovernanceFactory.getByPopulation(population);

        var councilRoles = rules.getCouncilRoles();

        assertEquals(jobApproval, rules.needJobApproval(), "Job approval for " + population);
        assertEquals(leader, councilRoles.get(0), "Leader for " + population);
        
        assertEquals(deputyLeader, councilRoles.size() > 1 ? councilRoles.get(1) : null, "Deputy for " + population);
    }
}
