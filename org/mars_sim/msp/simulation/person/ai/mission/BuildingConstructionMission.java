package org.mars_sim.msp.simulation.person.ai.mission;

import java.io.Serializable;
import java.util.Map;

import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.resource.Resource;
import org.mars_sim.msp.simulation.structure.Settlement;

public class BuildingConstructionMission extends Mission implements Serializable {

    // Default description.
    public static final String DEFAULT_DESCRIPTION = "Construct Building";
    
    // Minimum number of members.
    private static final int MIN_PEOPLE = 1;
    
    public BuildingConstructionMission(Person startingPerson) throws MissionException {
        // Use Mission constructor.
        super(DEFAULT_DESCRIPTION, startingPerson, MIN_PEOPLE);
        
        // TODO Implement
    }
    
    @Override
    protected void determineNewPhase() throws MissionException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void performPhase(Person person) throws MissionException {
        // TODO Auto-generated method stub

    }

    @Override
    public Settlement getAssociatedSettlement() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Resource, Number> getResourcesNeededForRemainingMission(
            boolean useBuffer, boolean parts) throws MissionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Class, Integer> getEquipmentNeededForRemainingMission(
            boolean useBuffer) throws MissionException {
        // TODO Auto-generated method stub
        return null;
    }
}