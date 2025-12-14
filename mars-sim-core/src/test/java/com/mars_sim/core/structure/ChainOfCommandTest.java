/*
 * Mars Simulation Project
 * ChainOfCommandTest.java
 * @date 2025-09-08
 * @author Manny Kung
 */
package com.mars_sim.core.structure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.test.MarsSimUnitTest;

/**
 * Test the internals of the ChainOfCommand class
 */
class ChainOfCommandTest extends MarsSimUnitTest {

    @Test
    void testEstablishTopLeadership() {
        int numPeople = 30;
        Settlement settlement = buildSettlement("TestSettlement", numPeople);
        buildAccommodation(settlement.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);

        // Build people but 2 have higher leadership
        var dl = buildPerson("Deputy", settlement);
        dl.getNaturalAttributeManager().setAttribute(NaturalAttributeType.LEADERSHIP, 80);
        var l = buildPerson("Leader", settlement);
        l.getNaturalAttributeManager().setAttribute(NaturalAttributeType.LEADERSHIP, 100);

        for(int i = 0; i < numPeople - 2; i++) {
            var p = buildPerson("p" + i, settlement);
            p.getNaturalAttributeManager().setAttribute(NaturalAttributeType.LEADERSHIP, 1);
        }

        ChainOfCommand coc = settlement.getChainOfCommand();
        coc.establishTopLeadership();

        var council = coc.getGovernance().getCouncilRoles();
        assertEquals(council.get(0), l.getRole().getType(), "Leader role type");
        assertEquals(council.get(1), dl.getRole().getType(), "Deputy Leader role type");
    }

    /**
     * Test that the commander can vacate his role.
     */
    @Test
    void testCommander() {
        Settlement settlement = buildSettlement("TestSettlement", 5);
  
        ChainOfCommand coc = settlement.getChainOfCommand();	
  
        buildPerson("p0", settlement, RoleType.LOGISTIC_SPECIALIST, JobType.CHEF);
        buildPerson("p1", settlement, RoleType.ENGINEERING_SPECIALIST, JobType.ENGINEER);
        buildPerson("p2", settlement, RoleType.AGRICULTURE_SPECIALIST, JobType.BOTANIST);
        buildPerson("p3", settlement, RoleType.COMPUTING_SPECIALIST, JobType.COMPUTER_SCIENTIST);
        buildPerson("p4", settlement, RoleType.RESOURCE_SPECIALIST, JobType.CHEMIST);
        
        List<Person> personList = new ArrayList<>(settlement.getCitizens());
        
        coc.establishTopLeadership();
                
        Person commander = personList.stream()
        		.filter(p -> p.getRole().getType() == RoleType.COMMANDER)
        		.findFirst().orElse(null);
        
        assertNotNull(commander, "First commander elected");
        assertEquals(5, personList.size(), "# of people in this settlement");
                           
        assertFalse(coc.isRoleAvailable(RoleType.COMMANDER), "Is Commander role is available ?");
        
        commander.setDeclaredDead(true);
      
        Person dead = personList.stream()
        		.filter(Person::isDeclaredDead)
        		.findFirst().orElse(null);
        
        assertNotNull(dead, "Someone died");
      
        personList = new ArrayList<>(settlement.getCitizens());
        
        assertEquals(4, personList.size(), "Roles available");
                   
		coc.reelectLeadership(RoleType.COMMANDER);

        Person commander1 = personList.stream()
        		.filter(p -> p.getRole().getType() == RoleType.COMMANDER)
        		.findFirst().orElse(null);
        

        assertNotNull(commander1, "New commander elected");
    }
}
