/**
 * Mars Simulation Project
 * BuildingConstructionMissionMeta.java
 * @version 3.07 2014-10-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;

/**
 * A meta mission for the BuildingConstructionMission mission.
 */
public class BuildingConstructionMissionMeta implements MetaMission {

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.buildingConstructionMission"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(MiningMeta.class.getName());
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new BuildingConstructionMission(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;
        
        // Check if person is in a settlement.
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Settlement settlement = person.getSettlement();
        
            // Check if available light utility vehicles.
            boolean reservableLUV = BuildingConstructionMission.isLUVAvailable(settlement);
            
            // Check if enough available people at settlement for mission.
            int availablePeopleNum = 0;
            Iterator<Person> i = settlement.getInhabitants().iterator();
            while (i.hasNext()) {
                Person member = i.next();
                boolean noMission = !member.getMind().hasActiveMission();
                boolean isFit = !member.getPhysicalCondition().hasSeriousMedicalProblems();
                if (noMission && isFit) availablePeopleNum++;
            }
            boolean enoughPeople = (availablePeopleNum >= BuildingConstructionMission.MIN_PEOPLE);
            
            // Check if settlement has construction override flag set.
            boolean constructionOverride = settlement.getConstructionOverride();
            
            if (reservableLUV && enoughPeople && !constructionOverride) {
                
                try {
                    int constructionSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
                    ConstructionValues values =  settlement.getConstructionManager().getConstructionValues();
                    
                    // Check if enough construction materials are available for new construction site.
                    if (BuildingConstructionMission.hasAnyNewSiteConstructionMaterials(constructionSkill, settlement)) {
                        double constructionProfit = values.getSettlementConstructionProfit(constructionSkill);
                        if (constructionProfit > 0D) {
                            System.out.println(person.getName() + " construction profit: " + constructionProfit);
                        }
                        result += constructionProfit;
                        if (result > 10D) {
                            result = 10D;
                        }
                    }
                    
                    // Check if enough construction materials are available for an existing construction site.
                    ConstructionManager manager = settlement.getConstructionManager();
                    Iterator<ConstructionSite> j = manager.getConstructionSitesNeedingConstructionMission().iterator();
                    while (j.hasNext()) {
                        ConstructionSite site = j.next();
                        if (BuildingConstructionMission.hasExistingSiteConstructionMaterials(site, constructionSkill, 
                                settlement)) {
                            double constructionProfit = values.getConstructionSiteProfit(site, constructionSkill);
                            result += constructionProfit;
                            if (result > 10D) {
                                result = 10D;
                            }
                        }
                    }
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE, "Error getting construction site.", e);
                }
            }       
            
            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(person.getSettlement()) < 
                    BuildingConstructionMission.MIN_PEOPLE) {
                result = 0D;
            }
            
            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartMissionProbabilityModifier(BuildingConstructionMission.class);
            }
        }
        
        return result;
    }
}