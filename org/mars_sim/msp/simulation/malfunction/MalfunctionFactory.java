/**
 * Mars Simulation Project
 * MalfunctionFactory.java
 * @version 2.74 2002-04-21
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation.malfunction;

import org.mars_sim.msp.simulation.*;
import java.util.*;

/**
 * This class is a factory for Malfunction objects.
 */
public class MalfunctionFactory {

    // Data members
    private Collection malfunctions; // The possible malfunctions in the simulation.

    /**
     * Constructs a MalfunctionFactory object.
     */
    public MalfunctionFactory() {
        malfunctions = new ArrayList();

	MalfunctionXmlReader malfunctionReader = new MalfunctionXmlReader(this);
	malfunctionReader.parse();
    }

    /**
     * Adds a malfunction to the factory.
     * @param malfunction the new malfunction to add
     */
    public void addMalfunction(Malfunction malfunction) {
        malfunctions.add(malfunction);
    }

    /**
     * Gets a randomly-picked malfunction for a given unit scope.
     * @param scope a collection of scope strings defining the unit.
     * @return a randomly-picked malfunction or null if there are none available.
     */
    public Malfunction getMalfunction(Collection scope) {

        Malfunction result = null;

	double totalProbability = 0D;
	Iterator i = malfunctions.iterator();
	while (i.hasNext()) {
	    Malfunction temp = (Malfunction) i.next();
	    if (temp.unitScopeMatch(scope)) 
	        totalProbability = temp.getProbability();
	}

        double r = RandomUtil.getRandomDouble(totalProbability);
	
        i = malfunctions.iterator();
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
}     
