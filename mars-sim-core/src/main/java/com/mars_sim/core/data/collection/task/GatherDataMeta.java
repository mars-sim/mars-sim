/*
 * Mars Simulation Project
 * GatherDataMeta.java
 * @date 2026-08-27
 * @author Manny Kung
 */
package com.mars_sim.core.data.collection.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.shift.ShiftManager;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.SettlementParameters;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Meta task for the GatherDataMeta task.
 */
public abstract class GatherDataMeta extends MetaTask
    implements SettlementMetaTask
 {
    /**
     * This is a Settlement task to perform a digging job.
     */
    private static class GatherDataTaskJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;
		
        public GatherDataTaskJob(GatherDataMeta ownerTask, Settlement owner, RatingScore score, int total) {
            super(ownerTask, owner, ownerTask.getName().replaceFirst("ing", ""), null, score);
            setDemand(total);
            setEVA(true); // Enable the EVA based assessments
        }

        @Override
        public Task createTask(Person person) {
            return  ((GatherDataMeta)getMeta()).createTask(person);
        }

        @Override
        public Task createTask(Robot robot) {
            throw new UnsupportedOperationException("Robots cannot gather data for now");
        }
    }

    private static final int BASE = 100;
	private static final int MAX_BASE = 2_000;
	private static final int DEFAULT_EVA_NUM = 5;
	
    /* The maximum shift fraction completed for a person to start this task.
    If above this value, the person will not consider picking this task. */
    private static final double MAX_SHIFT_FRACTION = 0.66D;
    	
	public static Set<Integer> sensorSuite = Set.of(
			ItemResourceUtil.SEISMOELECTRIC_SENSOR_ID, 
			ItemResourceUtil.HEAT_PROBE_ID);

	public static Set<Integer> rockCompositionTool = Set.of(
			ItemResourceUtil.GAS_CHROMATOGRAPH_ID, 
			ItemResourceUtil.IR_SPECTROMETER_ID,
			ItemResourceUtil.MASS_SPECTROMETER_ID, 
			ItemResourceUtil.XRAY_SPECTROMETER_ID);
	
	public static Set<Integer> waterDetectionTool = Set.of(
			ItemResourceUtil.SNMRS_ID,
			ItemResourceUtil.TDEM_SOUNDER_ID, 
			ItemResourceUtil.DAN_ID,
			ItemResourceUtil.GPR_ID, 
			ItemResourceUtil.GRAVITY_GRADIOMETER_ID);
	
	private EquipmentType containerType;

    protected GatherDataMeta(String name, EquipmentType containerType) {
		super(name, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setPreferredJob(JobType.SCIENTISTS);
		setPreferredJob(JobType.ARCHITECT, JobType.ENGINEER, JobType.TECHNICIAN, JobType.AREOLOGIST, JobType.TECHNICIAN);
		setTrait(TaskTrait.AGILITY);

		this.containerType = containerType;
	}

    /**
     * Computes the probability of doing this task for a Settlement.
     * 
     * @param settlement
     * @param collectionProbability
     * @return
     */
    protected List<SettlementTask> getSettlementTaskJobs(Settlement settlement,
                            double collectionProbability) {
    	
        var rh = settlement.getEquipmentInventory();
        double popfactor = settlement.getPopulationFactor0();
        
        Map<Integer, Integer> instrumentAvailability = new HashMap<>();
        
        double instrumentAverageScore = 0;
        
        for (int id: waterDetectionTool) {
        	int num = rh.getItemResourceStored(id);
        	instrumentAvailability.put(id, num);
        }
        
        int size = waterDetectionTool.size();
        
        int availableSize = instrumentAvailability.size();
        for (int id: instrumentAvailability.keySet()) {
        	instrumentAverageScore += instrumentAvailability.get(id) / popfactor * BASE;
        }
        
        // If one of the instrument is not available, the score would be lower.
        instrumentAverageScore = instrumentAverageScore * availableSize / size;
        
        // Check preconditions
        // - an airlock is available for egress
        // - at least one EVA suit at settlement.
        // - at least one empty bag at settlement.
    	if ((collectionProbability == 0.0)
            || (rh.findNumDataRecorder() == 0)) {                
    		return Collections.emptyList();
        }

        double base = RandomUtil.getRandomDouble(collectionProbability / 3, collectionProbability);
        if (base <= 0) {
            return Collections.emptyList();
        }
        else if (base > MAX_BASE) {
        	base = MAX_BASE;
        }
 
        // Determine the base score
        RatingScore score = new RatingScore(base);

        // Note: Will work on monitoringLevel based on what the settlement needs later.
        int monitoringLevel = 10;
        		
        // Calculate the capacity for more EVAs
        int maxEVA = (int)Math.sqrt(1.0 + monitoringLevel) 
        		+ settlement.getPreferences().getIntValue(SettlementParameters.MAX_EVA,
                                                    DEFAULT_EVA_NUM);
        
        maxEVA -= getActiveEVAPersons(settlement);
        if (maxEVA <= 0) {
        	return Collections.emptyList();
        }
  
        // Should use the demand & resources stored to influence the score. 50% capacity is
        // the unmodified baseline
//        result.addModifier("capacity", 1 + (capacity - MIN_CAPACITY));

        List<SettlementTask> resultList = new ArrayList<>();
        resultList.add(new GatherDataTaskJob(this, settlement, score, maxEVA));
        return resultList;
    }

    
	/**
	 * Gets the number of Persons doing EVAOperations in a Settlement.
	 * 
	 * @param settlement
	 * @return
	 */
    private static int getActiveEVAPersons(Settlement settlement) {
		return settlement.getAllAssociatedPeople().stream()
							.filter(p -> p.getTaskManager().getTask() instanceof EVAOperation)
							.collect(Collectors.counting()).intValue();
	}

    /**
     * Assesses a person for a specific SettlementTask of this type.
     * 
     * @param t The Settlement task being evaluated
     * @param p Person in question
     * @return A new rating score applying the Person's modifiers
     */
    @Override
    public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
        // Check preconditions :
        // - an airlock is available for egress
    	// - Not signing up for a mission
        // - Qualified for digging local
        // - Physically fit for heavy EVA tasks
    	if ((p.isInSettlement() && !Walk.anyAirlocksForIngressEgress(p, false))
//    			|| p.getMission() != null
    			|| !GatherData.canGatherData(p)
    			|| !EVAOperation.isEVAFit(p)) {
            return RatingScore.ZERO_RATING;
        }

        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = p.getPhysicalCondition();

        double stress = condition.getStress();
        double fatigue = condition.getFatigue();
        double hunger = condition.getHunger();
        double thirst = condition.getThirst();
        double exerciseMillisols = p.getCircadianClock().getTodayExerciseTime();
        
        var result = new RatingScore(t.getScore());
    
        // Add a negative base to model Person fitness
        result.addBase("fitness", -(stress * 2 + fatigue + hunger + thirst + exerciseMillisols));

        result = assessPersonSuitability(result, p);

        // Encourage to get this task done early in a work shift
        result.addModifier("shift", ShiftManager.getShiftModifier(p, 
        		MAX_SHIFT_FRACTION, getMarsTime().getMillisolInt()));

        return result;
    }

    /**
     * Creates a specific Task of the appropriate activity.
     * 
     * @param person
     * @return
     */
    protected abstract Task createTask(Person person);


}
