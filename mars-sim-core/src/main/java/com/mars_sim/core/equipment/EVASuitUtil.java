/*
 * Mars Simulation Project
 * EVASuitUtil.java
 * @date 2023-06-27
 * @author Manny Kung
 */
package com.mars_sim.core.equipment;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.unit.UnitHolder;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * A utility class for finding an EVA suit from an inventory
 */
public final class EVASuitUtil {

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(EVASuitUtil.class.getName());

	private static final String FROM = " from ";
	private static final String TO = " to ";
	
	private EVASuitUtil() {
		// Static helper class
	}

	/**
	 * Take off EVA suit, puts on the garment and get back the thermal bottle.
	 * 
	 * @param person
	 * @param entity
	 * @param inSettlement
	 * @param entity
	 */
	public static void checkIn(Person person, Object entity, boolean inSettlement, boolean disqualified) {
		EquipmentOwner housing = null;
		
		if (inSettlement)
			housing = ((Building)entity).getSettlement();
		else
			housing = (Vehicle)entity;
		
		EVASuit suit = person.getSuit();
		
		// Transfer the EVA suit from person to the new destination
		if (suit != null) {
			// Doff this suit.
			boolean success = suit.transfer((UnitHolder)housing);
			
			if (!success) {
				logger.warning(person, 4_000,
						"Could not transfer " + suit + FROM + person + TO + housing.getName() + ".");
			}
		}
		
		if (disqualified) {
			// Remove pressure suit and put on garment
			if (inSettlement) {
				if (person.unwearPressureSuit(housing)) {
					person.wearGarment(housing);
				}
			}
			// Note: vehicle may or may not have garment available
			else if (((Rover)housing).hasGarment() && person.unwearPressureSuit(housing)) {
				person.wearGarment(housing);
			}
	
			// Assign thermal bottle
			person.assignThermalBottle();
		}
	}
	
	/**
	 * Finds the instance of the registered EVA suit or a good suit.
	 * 
	 * @Note: this method does not transfer the suit
	 * 
	 * @param p 	the person who's looking for an EVA Suit
	 * @return EVA suit's instance
	 */
	public static EVASuit findRegisteredOrGoodEVASuit(Person p) {
		
		EVASuit suit = p.getSuit();
		
		if (suit != null)
			return suit;
		
		var cu = p.getContainerUnit();
		if (cu instanceof EquipmentOwner eo) {
			return findEVASuitWithResources(eo, p);
		}
		logger.warning(p, "Can't find any EVA Suit from " + cu.getName() + ".");
		return null;
	}

	/**
	 * Finds the instance of an available EVA suit (preferably the one registered by the person) 
	 * with or without resources from a given inventory. 
	 * 
	 * @Note: this method does not transfer the suit
	 * 
	 * @param owner 	the EquipmentOwner
	 * @param p 		the person who's looking for an EVA Suit
	 * @return EVA suit's instance
	 */
	public static EVASuit findEVASuitWithResources(EquipmentOwner owner, Person p) {
		EVASuit previousSuit = null;
		List<EVASuit> noResourceSuits = new ArrayList<>(0);
		List<EVASuit> goodSuits = new ArrayList<>(0);
		List<EVASuit> suits = new ArrayList<>();
		
		for (Equipment e : owner.getSuitSet()) {
			EVASuit suit = (EVASuit)e;
			boolean lastOwner = p.getIdentifier() == suit.getRegisteredOwnerID();
			boolean malfunction = suit.getMalfunctionManager().hasMalfunction();
			if (!malfunction) {
				suits.add(suit);
			}
			else {
				logger.log(p, Level.WARNING, 50_000,
					"Spotted malfunction with " + suit.getName() + " when being examined.");
				continue;
			}
			
			if (lastOwner) {
				// Pick this EVA suit since it has been used by the same person
				previousSuit = suit;
			}
			
			boolean hasEnoughResources = false;
			
			try {
				hasEnoughResources = hasEnoughResourcesForSuit(owner, suit);

			} catch (Exception ex) {
				logger.severe(p, 50_000,
						"Could not find enough resources for " + suit.getName() + ".", ex);
			}
			
			if (hasEnoughResources) {
				if (p != null && lastOwner) {
					// Prefers to pick the same suit that a person has been tagged in the past
					return suit;
				}
				else
					// tag it as good suit for possible use below
					goodSuits.add(suit);
			}
			else {
				// tag it as no resource suit for possible use below
				noResourceSuits.add(suit);
			}
		}

		// Doesn't have enough resource
		
		// Picks any one of the good suits
		if (previousSuit != null) {
			return previousSuit;
		}
		else if (!goodSuits.isEmpty()) {
			int size = goodSuits.size();
			return goodSuits.get(RandomUtil.getRandomInt(size - 1));
		}
		else if (!noResourceSuits.isEmpty()) {
			// Picks any one of the no-resource suits
			int size = noResourceSuits.size();
			return noResourceSuits.get(RandomUtil.getRandomInt(size - 1));
		}
		
		return null;
	}

	/**
	 * Finds a EVA suit in a particular vehicle. Select one with the most resources already
	 * loaded.
	 * 
	 * @Note: this method does not transfer the suit
	 * 
	 * @param person Person needing the suit
	 * @param vehicle
	 * @return instance of EVASuit or null if none
	 */
	public static EVASuit findEVASuitFromVehicle(Person p,  Vehicle v) {
		return findEVASuitWithResources(v, p);
	}
	
	
	/**
	 * Checks if entity unit has enough resource supplies to fill the EVA suit.
	 *
	 * @param owner 	the EquipmentOwner
	 * @param suit      the EVA suit
	 * @return true if enough supplies
	 */
	private static boolean hasEnoughResourcesForSuit(EquipmentOwner owner, EVASuit suit) {
		int otherPeopleNum = 0;
		if (owner instanceof Settlement s)
			otherPeopleNum = s.getIndoorPeopleCount() - 1;
		else if (owner instanceof Crewable c)
			otherPeopleNum = c.getCrewNum();
		
		// Check if enough oxygen.
		double neededOxygen = suit.getRemainingCombinedCapacity(ResourceUtil.OXYGEN_ID);
		double availableOxygen = owner.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
		// Make sure there is enough extra oxygen for everyone else.
		availableOxygen -= (neededOxygen * otherPeopleNum);
		boolean hasEnoughOxygen = (availableOxygen >= neededOxygen);

		// Check if enough water.
		double neededWater = suit.getRemainingCombinedCapacity(ResourceUtil.WATER_ID);
		double availableWater = owner.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
		// Make sure there is enough extra water for everyone else.
		availableWater -= (neededWater * otherPeopleNum);
		boolean hasEnoughWater = (availableWater >= neededWater);

		// it's okay even if there's not enough water
		if (!hasEnoughWater) {
			if (owner instanceof Settlement settlement)
				logger.warning(settlement, 20_000, 
					"No enough water to feed " + suit.getName() 
					+ " but can still use the EVA Suit.");
			else if (owner instanceof Rover rover)
				logger.warning(rover, 20_000, 
					"No enough water to feed " + suit.getName() 
					+ " but can still use the EVA Suit.");
		}
			
		return hasEnoughOxygen;
	}

	/**
	 * Does the vehicle have at least the baseline numbers of EVA suits ?
	 *
	 * @param vehicle
	 * @return
	 */
	public static boolean hasBaselineNumEVASuit(Vehicle vehicle, Mission mission) {
		boolean result = false;

		int numV = vehicle.findNumEVASuits();

		int baseline = (int)(mission.getMembers().size() * 1.25);

		int numP = 0;

		for (Person p: ((Crewable)vehicle).getCrew()) {
			if (p.getSuit() != null)
				numP++;
		}

		if (numV + numP > baseline)
			return true;

		return result;
	}
	
	/**
	 * Fetches an EVA suit from settlement.
	 * 
	 * @param person
	 * @param vehicle
	 * @param settlement
	 * @return
	 */
	public static boolean fetchEVASuitFromSettlement(Person person, Vehicle vehicle, Settlement settlement) {
		EVASuit suit1 = EVASuitUtil.findEVASuitWithResources(settlement, person);
		// Note: In future, will need to handle this officially by coming up 
		// with a list of parts that are missing and have a person carries them to the vehicle
		// instead of cheating this way
		if (suit1 != null) {
			boolean success = suit1.transfer(vehicle);
			if (!success) {
				logger.warning(person, "Unable to fetch " + suit1 + FROM 
						+ settlement + TO + vehicle + ".");
				return false;
			}
			else {
//				// Register this suit to a person
				suit1.setRegisteredOwner(person);
				
				logger.info(person, "Just fetched " + suit1 + FROM
						+ settlement + TO + vehicle + ".");
				return true;
			}
		} else {
			logger.warning(person, "No EVA suit available from " + settlement + ".");
			return false;
		}
	}
	
	/**
	 * Fetches an EVA suit from vehicle or settlement.
	 * 
	 * @param person
	 * @param vehicle
	 * @param settlement
	 * @return
	 */
	public static boolean fetchEVASuitFromAny(Person person, Vehicle vehicle, Settlement settlement) {
		EVASuit suit0 = findEVASuitFromVehicle(person, vehicle);
		if (suit0 == null && settlement.getNumEVASuit() > 0) {
			return fetchEVASuitFromSettlement(person, vehicle, settlement);
		}
		return false;
	}

	/**
	 * Checks if each person possesses an EVA suit and fetch them from settlement to vehicle.
	 *
	 * @param p
	 * @param disembarkSettlement
	 */
	public static void checkTransferSuitsToVehicle(Person p, Settlement disembarkSettlement, Mission mission) {
		if (p.getSuit() == null && p.isInVehicle()) {

			Vehicle v = p.getVehicle();

			// If the person does not have an EVA suit
			int availableSuitNum = disembarkSettlement.getNumEVASuit();

			if (availableSuitNum > 1 && !hasBaselineNumEVASuit(v, mission)) {
		
				// Note: Deliver an EVA suit from the settlement to the rover by cheating it
				// Should generate a task for a person to hand deliver an extra suit

				fetchEVASuitFromAny(p, v, disembarkSettlement);
			}
		}
	}
}
