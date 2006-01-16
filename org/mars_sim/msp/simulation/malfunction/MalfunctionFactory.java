/**
 * Mars Simulation Project
 * MalfunctionFactory.java
 * @version 2.76 2004-06-01
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation.malfunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.UnitCollection;
import org.mars_sim.msp.simulation.UnitIterator;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;

/**
 * This class is a factory for Malfunction objects.
 */
public class MalfunctionFactory {

    // Data members
    private Collection malfunctions;  // The possible malfunctions in the simulation.

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
    public Malfunction getMalfunction(Collection scope) {

        Malfunction result = null;

        double totalProbability = 0D;
        if (malfunctions.size() > 0) {
            Iterator i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction temp = (Malfunction) i.next();
    	        if (temp.unitScopeMatch(scope)) 
                    totalProbability += temp.getProbability();
            }
        }

        double r = RandomUtil.getRandomDouble(totalProbability);
	
        Iterator i = malfunctions.iterator();
        while (i.hasNext()) {
            Malfunction temp = (Malfunction) i.next();
            double probability = temp.getProbability();
            if (temp.unitScopeMatch(scope) && (result == null)) {
                if (r < probability) result = temp.getClone();
                else r -= probability;
            }
        }

        return result;
    }
    
    /**
     * Gets a collection of malfunctionable entities local to the given person.
     * @return collection iterator
     */
    public static Collection getMalfunctionables(Person person) {

        Collection entities = new ArrayList();
        String location = person.getLocationSituation();
	
        if (location.equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            entities.add(settlement);

            Iterator i = settlement.getBuildingManager().getBuildings().iterator();
            while (i.hasNext()) entities.add(i.next());
        }

        if (location.equals(Person.INVEHICLE)) entities.add(person.getVehicle());

        if (!location.equals(Person.OUTSIDE)) {
            UnitIterator i = person.getContainerUnit().getInventory().getContainedUnits().iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unit instanceof Malfunctionable) entities.add(unit);
            }
        }

        UnitCollection inventoryUnits = person.getInventory().getContainedUnits();
        if (inventoryUnits.size() > 0) {
            UnitIterator i = inventoryUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unit instanceof Malfunctionable) entities.add(unit);
            }
        }

        return entities;
    }

    /**
     * Gets a collection of malfunctionable entities
     * local to the given malfunctionable entity.
     * @return collection iterator
     */
    public static Collection getMalfunctionables(Malfunctionable entity) {

        Collection entities = new ArrayList();

        entities.add(entity);

        if (entity instanceof Settlement) {
            Iterator i = ((Settlement) entity).getBuildingManager().getBuildings().iterator();
            while (i.hasNext()) entities.add(i.next());
        }

        UnitCollection inventoryUnits = entity.getInventory().getContainedUnits();
        if (inventoryUnits.size() > 0) {
            UnitIterator i = inventoryUnits.iterator();
            while (i.hasNext()) {
                Unit unit = i.next();
                if (unit instanceof Malfunctionable) entities.add(unit);
            }
        }

        return entities;
    }
}