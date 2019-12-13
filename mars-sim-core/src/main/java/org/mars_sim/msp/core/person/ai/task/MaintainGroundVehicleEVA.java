/**
 * Mars Simulation Project
 * MaintainGroundVehicleEVA.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The MaintainGroundVehicleGarage class is a task for performing
 * preventive maintenance on ground vehicles outside a settlement.
 */
public class MaintainGroundVehicleEVA
extends EVAOperation
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(MaintainGroundVehicleEVA.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintainGroundVehicleEVA"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase MAINTAIN_VEHICLE = new TaskPhase(Msg.getString(
            "Task.phase.maintainVehicle")); //$NON-NLS-1$

    // Data members.
    /** Vehicle to be maintained. */
    private GroundVehicle vehicle;
    private Settlement settlement;

    /**
     * Constructor.
     * @param person the person to perform the task
     */
    public MaintainGroundVehicleEVA(Person person) {
        super(NAME, person, true, 25);

     	settlement = CollectionUtils.findSettlement(person.getCoordinates());
     	if (settlement == null) {
        	return;
     	}
     	
        // Choose an available needy ground vehicle.
        vehicle = getNeedyGroundVehicle(person);
        if (vehicle != null) {
            vehicle.setReservedForMaintenance(true);
            vehicle.addStatus(StatusType.MAINTENANCE);
            // Determine location for maintenance.
            Point2D maintenanceLoc = determineMaintenanceLocation();
            setOutsideSiteLocation(maintenanceLoc.getX(), maintenanceLoc.getY());
            
            // Initialize phase.
            addPhase(MAINTAIN_VEHICLE);

            logger.finest(person.getName() + " starting MaintainGroundVehicleEVA task.");
        }
        else {
            endTask();
        }
    }

    /**
     * Determine location to perform vehicle maintenance.
     * @return location.
     */
    private Point2D determineMaintenanceLocation() {

        Point2D.Double newLocation = null;
        boolean goodLocation = false;
        for (int x = 0; (x < 50) && !goodLocation; x++) {
            Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(vehicle, 1D);
            newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(),
                    boundedLocalPoint.getY(), vehicle);
            goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(),
                    person.getCoordinates());
        }

        return newLocation;
    }

    @Override
    protected TaskPhase getOutsideSitePhase() {
        return MAINTAIN_VEHICLE;
    }

    @Override
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);

        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (MAINTAIN_VEHICLE.equals(getPhase())) {
            return maintainVehiclePhase(time);
        }
        else {
            return time;
        }
    }

    @Override
    protected void addExperience(double time) {

        // Add experience to "EVA Operations" skill.
        // (1 base experience point per 100 millisols of time spent)
        double evaExperience = time / 100D;

        // Experience points adjusted by person's "Experience Aptitude" attribute.
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
        evaExperience += evaExperience * experienceAptitudeModifier;
        evaExperience *= getTeachingExperienceModifier();
        person.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);

        // If phase is maintain vehicle, add experience to mechanics skill.
        if (MAINTAIN_VEHICLE.equals(getPhase())) {
            // 1 base experience point per 100 millisols of collection time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double mechanicsExperience = time / 100D;
            mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;
            person.getSkillManager().addExperience(SkillType.MECHANICS, mechanicsExperience, time);
        }
    }

    /**
     * Perform the maintain vehicle phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     */
    private double maintainVehiclePhase(double time) {

        MalfunctionManager manager = vehicle.getMalfunctionManager();
        boolean malfunction = manager.hasMalfunction();
        boolean finishedMaintenance = (manager.getEffectiveTimeSinceLastMaintenance() == 0D);
        if (finishedMaintenance) {
        	vehicle.setReservedForMaintenance(false);
            vehicle.removeStatus(StatusType.MAINTENANCE);
        }
        
        if (person.isOutside() && (finishedMaintenance || malfunction || shouldEndEVAOperation() ||
                addTimeOnSite(time))) {
            setPhase(WALK_BACK_INSIDE);
            return 0;
        }

        // Determine effective work time based on "Mechanic" and "EVA Operations" skills.
        double workTime = time;
        int skill = getEffectiveSkillLevel();
        if (skill == 0) workTime /= 2;
        if (skill > 1) workTime += workTime * (.2D * skill);

        // Add repair parts if necessary.
        Inventory inv = settlement.getInventory();
        if (Maintenance.hasMaintenanceParts(inv, vehicle)) {
            Map<Integer, Integer> parts = new HashMap<>(manager.getMaintenanceParts());
            Iterator<Integer> j = parts.keySet().iterator();
            while (j.hasNext()) {
            	Integer part = j.next();
                int number = parts.get(part);
                inv.retrieveItemResources(part, number);
                manager.maintainWithParts(part, number);
            }
        }
        else {
            setPhase(WALK_BACK_INSIDE);
            return 0;
        }

        // Add work to the maintenance
        manager.addMaintenanceWorkTime(workTime);

        // Add experience points
        addExperience(time);

        // Check if an accident happens during maintenance.
        checkForAccident(time);

        return 0D;
    }

    @Override
    protected void checkForAccident(double time) {

        // Use EVAOperation checkForAccident() method.
        super.checkForAccident(time);

        double chance = .001D;

        // Mechanic skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
        if (skill <= 3) {
            chance *= (4 - skill);
        }
        else {
            chance /= (skill - 2);
        }

        // Modify based on the vehicle's wear condition.
        chance *= vehicle.getMalfunctionManager().getWearConditionAccidentModifier();

        if (RandomUtil.lessThanRandPercent(chance * time)) {

			if (person != null) {
	            logger.info(person.getName() + " has an accident while performing maintenance on " 
			+ vehicle.getName() + ".");
	            vehicle.getMalfunctionManager().createASeriesOfMalfunctions(vehicle.getName(), person);
			}
			else if (robot != null) {
				logger.info(robot.getName() + " has an accident while performing maintenance on " 
			+ vehicle.getName() + ".");
	            vehicle.getMalfunctionManager().createASeriesOfMalfunctions(vehicle.getName(), robot);
			}


        }
    }

    /**
     * Gets the vehicle  the person is maintaining.
     * Returns null if none.
     * @return entity
     */
    public Malfunctionable getVehicle() {
        return vehicle;
    }

    /**
     * Gets all ground vehicles requiring maintenance that are parked outside the settlement.
     *
     * @param person person checking.
     * @return collection of ground vehicles available for maintenance.
     */
    public static Collection<Vehicle> getAllVehicleCandidates(Person person) {
        Collection<Vehicle> result = new ConcurrentLinkedQueue<Vehicle>();

        Settlement settlement = person.getSettlement();
        if (settlement != null) {
            Iterator<Vehicle> vI = settlement.getParkedVehicles().iterator();
            while (vI.hasNext()) {
                Vehicle vehicle = vI.next();
                if ((vehicle instanceof GroundVehicle) && !vehicle.isReservedForMission()) {
                    result.add(vehicle);
                }
            }
        }

        return result;
    }

    /**
     * Gets a ground vehicle that requires maintenance in a local garage.
     * Returns null if none available.
     * @param person person checking.
     * @return ground vehicle
     * @throws Exception if error finding needy vehicle.
     */
    private GroundVehicle getNeedyGroundVehicle(Person person) {

        GroundVehicle result = null;

        // Find all vehicles that can be maintained.
        Collection<Vehicle> availableVehicles = getAllVehicleCandidates(person);

        // Populate vehicles and probabilities.
        Map<Vehicle, Double> vehicleProb = new HashMap<Vehicle, Double>(availableVehicles.size());
        Iterator<Vehicle> i = availableVehicles.iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            double prob = getProbabilityWeight(vehicle);
            if (prob > 0D) {
                vehicleProb.put(vehicle, prob);
            }
        }

        // Randomly determine needy vehicle.
        if (!vehicleProb.isEmpty()) {
            result = (GroundVehicle) RandomUtil.getWeightedRandomObject(vehicleProb);
        }

        if (result != null) {
            setDescription(Msg.getString("Task.description.maintainGroundVehicleEVA.detail",
                    result.getName())); //$NON-NLS-1$
        }

        return result;
    }

    /**
     * Gets the probability weight for a vehicle.
     * @param vehicle the vehicle.
     * @return the probability weight.
     * @throws Exception if error determining probability weight.
     */
    private double getProbabilityWeight(Vehicle vehicle) {
        double result = 0D;
        MalfunctionManager manager = vehicle.getMalfunctionManager();
        boolean hasMalfunction = manager.hasMalfunction();
        double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
        boolean minTime = (effectiveTime >= 1000D);
        boolean enoughParts = Maintenance.hasMaintenanceParts(person, vehicle);
        if (!hasMalfunction && minTime && enoughParts) result = effectiveTime;
        return result;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        int mechanicsSkill = manager.getEffectiveSkillLevel(SkillType.MECHANICS);
        return (int) Math.round((double)(EVAOperationsSkill + mechanicsSkill) / 2D);
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(2);
        results.add(SkillType.EVA_OPERATIONS);
        results.add(SkillType.MECHANICS);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        vehicle = null;
    }
}