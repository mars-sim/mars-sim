/*
 * Mars Simulation Project
 * ConstructionUtil.java
 * @date 2021-12-15
 * @author Scott Davis
 */
package com.mars_sim.core.building.construction;

import java.util.Iterator;
import java.util.List;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.construction.ConstructionStageInfo.Stage;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.structure.Settlement;

/**
 * Utility class for construction.
 */
public class ConstructionUtil {

	private static ConstructionConfig config = SimulationConfig.instance().getConstructionConfiguration();
	
	/**
	 * Private constructor.
	 */
	private ConstructionUtil() {
	}

	/**
	 * Gets a construction stage info matching a given name.
	 * 
	 * @param stageName the stage info name.
	 * @return construction stage info or null if none found.
	 * @throws Exception if error finding construction stage info.
	 */
	public static ConstructionStageInfo getConstructionStageInfo(String stageName) {
		return config.getConstructionStageInfoByName(stageName);
	}

	/**
	 * Gets a list of all construction stage info of a given type.
	 * 
	 * @param stageType the type of stage.
	 * @param constructionSkill the architect's construction skill.
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public static List<ConstructionStageInfo> getConstructionStageInfoList(Stage stageType,
			int constructionSkill) {
		
		return config.getConstructionStageInfoList(stageType).stream()
						.filter(s -> s.getArchitectConstructionSkill() <= constructionSkill)
						.toList();
	}

	/**
	 * Gets the highest construction skill of all people associated with a settlement.
	 * 
	 * @param settlement the settlement.
	 * @return highest effective construction skill.
	 */
	public static int getBestConstructionSkillAtSettlement(Settlement settlement) {
	    int result = 0;
	    
	    Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
	    while (i.hasNext()) {
	        Person person = i.next();
	        if (!person.getPhysicalCondition().isDead()) {
	            int constructionSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
	            if (constructionSkill > result) {
	                result = constructionSkill;
	            }
	        }
	    }
	    
	    return result;
	}
}
