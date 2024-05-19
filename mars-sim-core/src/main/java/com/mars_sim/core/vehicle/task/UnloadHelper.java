/*
 * Mars Simulation Project
 * UnloadHelper.java
 * @date 2024-05-18
 * @author Barry Evans
 */
package com.mars_sim.core.vehicle.task;

import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.CollectMinedMinerals;
import com.mars_sim.core.person.ai.task.CollectResources;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Towing;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Helper class to handle unloading various activities of unloading a vehicle
 */
public final class UnloadHelper {
    private UnloadHelper() {
        // Stop instantiation
    }

    /**
     * Unload an dead bodies from the Crewable vehicle
     * @param crewable
     * @param dest
     */
    static void unloadDeceased(Crewable crewable, Settlement dest) {
    	var deadBodies = crewable.getCrew().stream()
    							.filter(Person::isDeclaredDead)
    							.toList();
    	for (Person p : deadBodies) {
    		if (p.transfer(dest)) {
    			BuildingManager.addToMedicalBuilding(p, dest);			
    			p.setAssociatedSettlement(dest.getIdentifier());
    			UnloadVehicleEVA.logger.info(p, "dead body from " + crewable.getName());
    		}
    		else {
    			UnloadVehicleEVA.logger.warning(p, "failed to retrieve the dead body from " + crewable.getName());
    		}
    	}
    }

    /**
     * Release any towed vehcile back to the settlement
     * @param towingVehicle Vehcile towing something
     * @param dest Place to transfer to
     */
    static void releaseTowedVehicle(Towing towingVehicle, Settlement dest) {
    	Vehicle towedVehicle = towingVehicle.getTowedVehicle();
    	if (towedVehicle != null) {
    		towingVehicle.setTowedVehicle(null);
    		towedVehicle.setTowingVehicle(null);
    		if (!dest.containsParkedVehicle(towedVehicle)) {
    			dest.addParkedVehicle(towedVehicle);
    			towedVehicle.findNewParkingLoc();					
    		}
    	}
    }

    /**
     * Unload any itemas from a Vehicle to a Settlement
     * @param source Vehicle being unloaded
     * @param dest Destination for any Items
     * @param amountUnloading Maximum amount to unloaded
     * @return Amount not used
     */
    private static double unloadItems(Vehicle source, Settlement dest, double amountUnloading) {
    	for(int id : source.getItemResourceIDs()) {
    		Part part = ItemResourceUtil.findItemResource(id);
    		double mass = part.getMassPerItem();
    		int num = source.getItemResourceStored(id);
    		if ((num * mass) > amountUnloading) {
    			num = (int) Math.round(amountUnloading / mass);
    			if (num == 0) {
    				num = 1;
    			}
    		}
    		source.retrieveItemResource(id, num);
    		dest.storeItemResource(id, num);
    		amountUnloading -= (num * mass);
    
    		if (amountUnloading <= 0) {
    			return 0D;
    		}
    	}
    	return amountUnloading;
    }

    /**
     * Unload any resources from a Vehicle to a Settlement
     * @param source Vehicle being unloaded
     * @param dest Destination for any Resources
     * @param amountUnloading Maximum amount to unloaded
     * @return Amount not used
     */
    private static double unloadResources(Vehicle source, Settlement dest, double amountUnloading) {
    	for(int id : source.getAmountResourceIDs()) {
    		double amount = source.getAmountResourceStored(id);
    		if (amount > amountUnloading) {
    			amount = amountUnloading;
    		}
    		double capacity = source.getAmountResourceRemainingCapacity(id);
    		if (capacity < amount) {
    			amount = capacity;
    		}
    		
    		// Transfer the amount resource from vehicle to settlement
    		source.retrieveAmountResource(id, amount);
    		dest.storeAmountResource(id, amount);
    		
    		// Resources count towards the output ??
    		if (!UnloadHelper.EXCLUDE_OUTPUTS.contains(id)) {
    			double laborTime = ((id == ResourceUtil.iceID) || (id == ResourceUtil.regolithID)
    								? CollectResources.LABOR_TIME : CollectMinedMinerals.LABOR_TIME);
    			dest.addOutput(id, amount, laborTime);
    		}
    
    		amountUnloading -= amount;
    		if (amountUnloading <= 0) {
    			return 0;
    		}
    	}
    	return amountUnloading;
    }

    /**
     * Unload any EVASuit from a Vehicle to a Settlement
     * @param source Vehicle being unloaded
     * @param dest Destination for any Items
     * @param amountUnloading Maximum amount to unloaded
     * @param minSuits Minimum suits that must be retained
     * @return Amount not used
     */
    static double unloadEVASuits(Vehicle source, Settlement dest, double amountUnloading, int minSuits) {
    	// FInd the suits that can be removed
    	Set<EVASuit> surplus = new HashSet<>();
    	for(var suit : source.getSuitSet()) {
    		if (minSuits > 0) {
    			// Skip this suit
    			minSuits--;
    		}
    		else {
    			surplus.add((EVASuit) suit);
    		}
    	}
    
    	// Unload the surplus
    	for(var extra : surplus) {
    		// Unload inventories of equipment (if possible)
    		unloadResourcesHolder(extra, dest);
    		extra.transfer(dest);		
    		amountUnloading -= extra.getMass();
    	}
    
    	return amountUnloading;
    }

    /**
	 * Unload the inventory from a piece of equipment.
	 *
	 * @param equipment the equipment.
	 */
	private static void unloadResourcesHolder(ResourceHolder rh, Settlement settlement) {

		// Note: only unloading amount resources at the moment.
        for(int resource : rh.getAmountResourceIDs()) {
            double amount = rh.getAmountResourceStored(resource);
            double capacity = settlement.getAmountResourceRemainingCapacity(resource);
            if (amount > capacity) {
                amount = capacity;
            }
            rh.retrieveAmountResource(resource, amount);
            settlement.storeAmountResource(resource, amount);
        }
	}

    /**
     * Unload inventory from a Vehicle to a Settlement
     * @param source Vehicle being unloaded
     * @param dest Destination for any Resources
     * @param amountUnloading Maximum amount to unloaded
     * @return Amount not used
     */
    public static double unloadInventory(Vehicle vehicle, Settlement settlement, double amountUnloading) {
		amountUnloading = unloadItems(vehicle, settlement, amountUnloading);
		if (amountUnloading > 0) {
			amountUnloading = unloadResources(vehicle, settlement, amountUnloading);
        }

        return amountUnloading;
    }

    static final Set<Integer> EXCLUDE_OUTPUTS = Set.of(ResourceUtil.waterID,
    ResourceUtil.methanolID,
    ResourceUtil.foodID,
    ResourceUtil.oxygenID);

}
