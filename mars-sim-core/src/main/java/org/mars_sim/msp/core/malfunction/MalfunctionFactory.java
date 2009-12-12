/**
 * Mars Simulation Project
 * MalfunctionFactory.java
 * @version 2.84 2008-05-12
 * @author Scott Davis 
 */

package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This class is a factory for Malfunction objects.
 */
public final class MalfunctionFactory implements Serializable {

    // Data members
    private Collection<Malfunction> malfunctions;  // The possible malfunctions in the simulation.

    /**
     * Constructs a MalfunctionFactory object.
     * @param config malfunction configuration DOM document.
     * @throws Exception when malfunction list could not be found.
     */
    public MalfunctionFactory(MalfunctionConfig config) throws Exception {
		malfunctions = config.getMalfunctionList(); 
    }

    /**
     * Gets a randomly-picked malfunction for a given unit scope.
     * @param scope a collection of scope strings defining the unit.
     * @return a randomly-picked malfunction or null if there are none available.
     */
    public Malfunction getMalfunction(Collection<String> scope) {

        Malfunction result = null;

        double totalProbability = 0D;
        if (malfunctions.size() > 0) {
            Iterator<Malfunction> i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction temp = i.next();
    	        if (temp.unitScopeMatch(scope)) totalProbability += temp.getProbability();
            }
        }

        double r = RandomUtil.getRandomDouble(totalProbability);
	
        Iterator<Malfunction> i = malfunctions.iterator();
        while (i.hasNext()) {
            Malfunction temp = i.next();
            double probability = temp.getProbability();
            if (temp.unitScopeMatch(scope) && (result == null)) {
                if (r < probability) {
                	try {
                		result = temp.getClone();
                		result.determineRepairParts();
                	}
                	catch (Exception e) {
                		e.printStackTrace(System.err);
                	}
                }
                else r -= probability;
            }
        }

        return result;
    }
    
    /**
     * Gets a collection of malfunctionable entities local to the given person.
     * @return collection iterator
     */
    public static Collection<Malfunctionable> getMalfunctionables(Person person) {

        Collection<Malfunctionable> entities = new ArrayList<Malfunctionable>();
        String location = person.getLocationSituation();
	
        if (location.equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            entities.add(settlement);

            Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
            while (i.hasNext()) entities.add(i.next());
        }

        if (location.equals(Person.INVEHICLE)) entities.add(person.getVehicle());

        if (!location.equals(Person.OUTSIDE)) {
            Iterator<Unit> i = person.getContainerUnit().getInventory().getContainedUnits().iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unit instanceof Malfunctionable) entities.add((Malfunctionable) unit);
            }
        }

        Collection<Unit> inventoryUnits = person.getInventory().getContainedUnits();
        if (inventoryUnits.size() > 0) {
            Iterator<Unit> i = inventoryUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unit instanceof Malfunctionable) entities.add((Malfunctionable)unit);
            }
        }

        return entities;
    }

    /**
     * Gets a collection of malfunctionable entities
     * local to the given malfunctionable entity.
     * @return collection iterator
     */
    public static Collection<Malfunctionable> getMalfunctionables(Malfunctionable entity) {

        Collection<Malfunctionable> entities = new ArrayList<Malfunctionable>();

        entities.add(entity);

        if (entity instanceof Settlement) {
            Iterator<Building> i = ((Settlement) entity).getBuildingManager().getBuildings().iterator();
            while (i.hasNext()) entities.add(i.next());
        }

        Collection<Unit> inventoryUnits = entity.getInventory().getContainedUnits();
        if (inventoryUnits.size() > 0) {
            Iterator<Unit> i = inventoryUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unit instanceof Malfunctionable) entities.add((Malfunctionable)unit);
            }
        }

        return entities;
    }
    
    /**
     * Gets all malfunctionables associated with a settlement.
     * @param settlement the settlement.
     * @return collection of malfunctionables.
     */
    public static Collection<Malfunctionable> getAssociatedMalfunctionables(Settlement settlement) {
    	
    	// Add settlement, buildings and all other malfunctionables in settlement inventory.
    	Collection<Malfunctionable> entities = getMalfunctionables(settlement);
    	
    	// Add all associated rovers out on missions and their inventories.
    	Iterator i = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement())) 
					entities.addAll(getMalfunctionables(vehicle));
			}
		}
		
		// Get entities carried by people on EVA.
		Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.getLocationSituation().equals(Person.OUTSIDE)) 
				entities.addAll(getMalfunctionables(person));
		}
    	
    	return entities;
    }
    
    /**
     * Gets the repair part probabilities per malfunction for a set of entity scope strings.
     * @param scope a collection of entity scope strings.
     * @return map of repair parts and probable number of parts needed per malfunction.
     * @throws Exception if error finding repair part probabilities.
     */
    Map<Part, Double> getRepairPartProbabilities(Collection<String> scope) throws Exception {
    	Map<Part, Double> result = new HashMap<Part, Double>();
    	
    	Iterator<Malfunction> i = malfunctions.iterator();
    	while (i.hasNext()) {
    		Malfunction malfunction = i.next();
    		if (malfunction.unitScopeMatch(scope)) {
    			double malfunctionProbability = malfunction.getProbability() / 100D;
    			MalfunctionConfig config = SimulationConfig.instance().getMalfunctionConfiguration();
    			String[] partNames = config.getRepairPartNamesForMalfunction(malfunction.getName());
    			for (int x = 0; x < partNames.length; x++) {
    				double partProbability = config.getRepairPartProbability(malfunction.getName(), partNames[x]) / 100D;
    				int partNumber = config.getRepairPartNumber(malfunction.getName(), partNames[x]);
    				double averageNumber = RandomUtil.getRandomRegressionIntegerAverageValue(partNumber);
    				double totalNumber = averageNumber * partProbability * malfunctionProbability;
    				Part part = (Part) ItemResource.findItemResource(partNames[x]);
    				if (result.containsKey(part)) totalNumber += result.get(part);
    				result.put(part, totalNumber);
    			}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Gets the probabilities of parts per maintenance for a set of entity scope strings.
     * @param scope a collection of entity scope strings.
     * @return map of maintenance parts and probable number of parts needed per maintenance.
     * @throws Exception if error finding maintenance part probabilities.
     */
    Map<Part, Double> getMaintenancePartProbabilities(Collection<String> scope) throws Exception {
    	Map<Part, Double> result = new HashMap<Part, Double>();
    	
    	Iterator<String> i = scope.iterator();
    	while (i.hasNext()) {
    		String entity = i.next();
    		Iterator<Part> j = Part.getParts().iterator();
    		while (j.hasNext()) {
    			Part part = j.next();
    			if (part.hasMaintenanceEntity(entity)) {
    				double prob = part.getMaintenanceProbability(entity) / 100D;
    				int partNumber = part.getMaintenanceMaximumNumber(entity);
    				double averageNumber = RandomUtil.getRandomRegressionIntegerAverageValue(partNumber);
    				double totalNumber = averageNumber * prob;
    				if (result.containsKey(part)) totalNumber += result.get(part);
    				result.put(part, totalNumber);
    			}
    		}
    	}
    	
    	return result;
    }
}