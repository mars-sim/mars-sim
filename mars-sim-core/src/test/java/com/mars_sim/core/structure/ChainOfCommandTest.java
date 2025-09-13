/*
 * Mars Simulation Project
 * ChainOfCommandTest.java
 * @date 2025-09-08
 * @author Manny Kung
 */
package com.mars_sim.core.structure;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;

/**
 * Test the internals of the ChainOfCommand class
 */
public class ChainOfCommandTest extends AbstractMarsSimUnitTest {

    /**
     * Test that the commander can vacate his role.
     */
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
        
        assertNotNull("First commander elected", commander);
        assertEquals("# of people in this settlement", 5, personList.size());
       
        assertGreaterThan("Roles available", 0, coc.getRoleAvailability().size());
        
        assertTrue("Does the roleRegistery have the commander role ?", coc.getRoleRegistry().containsKey(RoleType.COMMANDER));
            
        assertFalse("Is Commander role is available ?", coc.isRoleAvailable(RoleType.COMMANDER));
        
        commander.setDeclaredDead(true);
      
        Person dead = personList.stream()
        		.filter(p -> p.isDeclaredDead())
        		.findFirst().orElse(null);
        
        assertTrue("Someone died", dead != null);
      
        personList = new ArrayList<>(settlement.getCitizens());
        
        assertEquals("Roles available", 4, personList.size());
       
//        for (Person p: personList) System.out.println(p.getName() + " : " + p.getRole().getType().getName() + ", " + p.getMind().getJobType().getName());
            
		coc.reelectLeadership(RoleType.COMMANDER);

        Person commander1 = personList.stream()
        		.filter(p -> p.getRole().getType() == RoleType.COMMANDER)
        		.findFirst().orElse(null);
        

        assertNotNull("New commander elected", commander1);
             
    }

}