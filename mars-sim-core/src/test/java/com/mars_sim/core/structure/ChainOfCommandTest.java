/*
 * Mars Simulation Project
 * ChainOfCommandTest.java
 * @date 2025-09-08
 * @author Manny Kung
 */
package com.mars_sim.core.structure;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;

/**
 * Test the internals of the ChainOfCommand class
 */
public class ChainOfCommandTest extends MarsSimUnitTest {

    /**
     * Test that the commander can vacate his role.
     */
    @Test
    public void testCommander() {
        Settlement settlement = buildSettlement(5);
  
        ChainOfCommand coc = settlement.getChainOfCommand();	
  
        Person p0 = buildPerson("p0", settlement, RoleType.LOGISTIC_SPECIALIST, JobType.CHEF);
       
        Person p1 = buildPerson("p1", settlement, RoleType.ENGINEERING_SPECIALIST, JobType.ENGINEER);
        
        Person p2 = buildPerson("p2", settlement, RoleType.AGRICULTURE_SPECIALIST, JobType.BOTANIST);
        
        Person p3 = buildPerson("p3", settlement, RoleType.COMPUTING_SPECIALIST, JobType.COMPUTER_SCIENTIST);
        
        Person p4 = buildPerson("p4", settlement, RoleType.RESOURCE_SPECIALIST, JobType.CHEMIST);
        
        List<Person> personList = new ArrayList<>(settlement.getCitizens());
        
        coc.establishTopLeadership();
        
        // for (Person p: personList) System.out.println(p.getName() + " : " + p.getRole().getType().getName() + ", " + p.getMind().getJobType().getName());
        
        Person commander = personList.stream()
        		.filter(p -> p.getRole().getType() == RoleType.COMMANDER)
        		.findFirst().orElse(null);
        
        assertNotNull(commander, "First commander elected");
        assertEquals(5, personList.size(), "# of people in this settlement");
       
        assertGreaterThan("Roles available", 0, coc.getRoleAvailability().size());
        
        assertTrue(coc.getRoleRegistry().containsKey(RoleType.COMMANDER), "Does the roleRegistery have the commander role ?");
            
        assertFalse(coc.isRoleAvailable(RoleType.COMMANDER), "Is Commander role is available ?");
        
        commander.setDeclaredDead(true);
      
        Person dead = personList.stream()
        		.filter(p -> p.isDeclaredDead())
        		.findFirst().orElse(null);
        
        assertTrue(dead != null, "Someone died");
      
        personList = new ArrayList<>(settlement.getCitizens());
        
        assertEquals(4, personList.size(), "Roles available");
       
//        for (Person p: personList) System.out.println(p.getName() + " : " + p.getRole().getType().getName() + ", " + p.getMind().getJobType().getName());
            
		coc.reelectLeadership(RoleType.COMMANDER);

        Person commander1 = personList.stream()
        		.filter(p -> p.getRole().getType() == RoleType.COMMANDER)
        		.findFirst().orElse(null);
        

        assertNotNull(commander1, "New commander elected");
             
    }

}
