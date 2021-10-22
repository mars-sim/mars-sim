/*
 * Mars Simulation Project
 * InventoryUtil.java
 * @date 2021-10-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * A utility class for finding resources in an inventory
 */
public class InventoryUtil {

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(InventoryUtil.class.getName());
	
	/**
	 * Gets a good EVA suit
	 * 
	 * @param housing The unit housing the airlock to be used.
	 * @param p
	 * @return
	 */
	public static EVASuit getGoodEVASuit(Unit housing, Person p) {
		Collection<Equipment> candidates = ((EquipmentOwner) housing).getEquipmentList();
 		
 		// Find suit without malfunction
 		// TODO favorite a previous one
		for(Equipment e : candidates) {
			if ((e.getEquipmentType() == EquipmentType.EVA_SUIT)
					&& !((EVASuit)e).getMalfunctionManager().hasMalfunction()) {
				return (EVASuit)e;
			}
		}

		logger.warning(p, "Can not find an EVA suit in " + housing.getName());
		return null;
	}
	
	/**
	 * Gets a good working EVA suit from an inventory.
	 *
	 * @param inv the inventory to check.
	 * @return EVA suit or null if none available.
	 */
	public static EVASuit getGoodEVASuitNResource(EquipmentOwner owner, Person p) {
//		List<EVASuit> malSuits = new ArrayList<>(0);
		List<EVASuit> noResourceSuits = new ArrayList<>(0);
		List<EVASuit> goodSuits = new ArrayList<>(0);
		List<EVASuit> suits = new ArrayList<>();
		for (Equipment e : owner.getEquipmentList()) {
			if (e.getEquipmentType() == EquipmentType.EVA_SUIT) {
				EVASuit suit = (EVASuit)e;
				boolean malfunction = suit.getMalfunctionManager().hasMalfunction();
				if (!malfunction) {
					suits.add(suit);
				}
				else 
					logger.log(p, Level.WARNING, 50_000, 
						"Spotted the malfunction with " + suit.getName() + " when being examined.");
				
				try {
					boolean hasEnoughResources = hasEnoughResourcesForSuit(owner, suit);
					if (!malfunction && hasEnoughResources) {			
						if (p != null && suit.getLastOwner() == p) {
							// Prefers to pick the same suit that a person has been tagged in the past
							return suit;
						}
						else
							// tag it as good suit for possible use below
							goodSuits.add(suit);
					}
					else if (!malfunction && !hasEnoughResources) {
						// tag it as no resource suit for possible use below
						noResourceSuits.add(suit);					
					}
					
				} catch (Exception ex) {
					logger.log(p, Level.SEVERE, 50_000,
							"Could not find enough resources for " + suit.getName() + ".", ex);
				}
			}
		}

		// Picks any one of the good suits
		int size = goodSuits.size();
		if (size == 1)
			return goodSuits.get(0);
		else if (size > 1)
			return goodSuits.get(RandomUtil.getRandomInt(size - 1));
		
		// Picks any one of the good suits
		size = noResourceSuits.size();
		if (size == 1)
			return noResourceSuits.get(0);
		else if (size > 1)
			return noResourceSuits.get(RandomUtil.getRandomInt(size - 1));
		
		return null;
	}
	
	/**
	 * Checks if entity unit has enough resource supplies to fill the EVA suit.
	 * 
	 * @param entityInv the entity unit.
	 * @param suit      the EVA suit.
	 * @return true if enough supplies.
	 * @throws Exception if error checking suit resources.
	 */
	private static boolean hasEnoughResourcesForSuit(EquipmentOwner owner, EVASuit suit) {
		int otherPeopleNum = 0; 
		if (owner instanceof Settlement)
			otherPeopleNum = ((Settlement) owner).getIndoorPeopleCount() - 1;

		// Check if enough oxygen.
		double neededOxygen = suit.getAmountResourceRemainingCapacity(ResourceUtil.oxygenID);
		double availableOxygen = owner.getAmountResourceStored(ResourceUtil.oxygenID);
		// Make sure there is enough extra oxygen for everyone else.
		availableOxygen -= (neededOxygen * otherPeopleNum);
		boolean hasEnoughOxygen = (availableOxygen >= neededOxygen);

		// Check if enough water.
//		double neededWater = getAmountResourceRemainingCapacity(waterID);
//		double availableWater = entityInv.getAmountResourceStored(waterID, false);
//		// Make sure there is enough extra water for everyone else.
//		availableWater -= (neededWater * otherPeopleNum);
//		boolean hasEnoughWater = (availableWater >= neededWater);

		// it's okay even if there's not enough water
//		if (!hasEnoughWater)
//			LogConsolidated.log(Level.WARNING, 20_000, sourceName,
//					"[" + suit.getContainerUnit() + "] won't have enough water to feed " + suit.getNickName() + " but can still use it.", null);

		return hasEnoughOxygen;// && hasEnoughWater;
	}
	
}
