/**
 * Mars Simulation Project
 * Infirmary.java
 * @version 2.74 2002-04-21
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.structure;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.*;
import org.mars_sim.msp.simulation.person.medical.*;
import org.mars_sim.msp.simulation.structure.*;

/**
 * This class represents a Infirmary that is based in a Settlement. It uses a
 * Delegation pattern to provide the access to the SickBay object that maintains
 * dispenses the Treatment.
 */
public class Infirmary extends Facility
            implements Serializable, MedicalAid {
    /**
     * Name of Infirmary
     */
    public final static String NAME = "Infirmary";
    private final static int LEVEL = 5;

    private SickBay sickBay; // Sickbay of the infirmary

    /** Constructor for random creation.
     *  @param manager the settlement facility manager
     */
    Infirmary(FacilityManager manager) {

        // Use Facility's constructor.
        super(manager, NAME);

        // Add scope string to malfunction manager.
	malfunctionManager.addScopeString("Infirmary");

        // Initialize random size from 1 to 5.
        sickBay = new SickBay(NAME, 1 + RandomUtil.getRandomInt(4),
                              LEVEL, manager.getMars(), this);
    }

    /**
     * Get the SickBay of this infirmary.
     * @return Sickbay object.
     */
    public SickBay getSickBay() {
        return sickBay;
    }

    /**
     * A person requests to start the specified treatment using this aid. This
     * aid may elect to record that this treatment is being performed. If the
     * treatment can not be satisfied, then a false return value is provided.
     *
     * @param sufferer Person with problem.
     * @return Can the treatment be satified.
     */
    public boolean requestTreatment(HealthProblem problem) {
        return sickBay.requestTreatment(problem);
    }

    /**
     * Stop a previously started treatment.
     *
     * @param problem Person with problem.
     */
    public void stopTreatment(HealthProblem problem) {
        sickBay.stopTreatment(problem);

        // Must check if anyome else can join infirmary
    }

    /**
     * Time passing for infirmary.
     * @param time the amount of time passing (millisols)
     */
    public void timePassing(double time) {
	super.timePassing(time);
        if (sickBay.getTreatedPatientCount() > 0) 
	    malfunctionManager.activeTimePassing(time);
    }

    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public PersonCollection getAffectedPeople() {
        PersonCollection people = super.getAffectedPeople();

	// Check for people in sick bay.
        Iterator i = sickBay.getPatients().iterator();
        while (i.hasNext()) {
            Person person = ((HealthProblem) i.next()).getSufferer();
	    if (!people.contains(person)) people.add(person);
	}

	// Check for people treating medical problems.
	PersonIterator i2 = getFacilityManager().getSettlement().getInhabitants().iterator();
        while (i2.hasNext()) {
	    Person person = i2.next();
	    Task task = person.getMind().getTaskManager().getTask();
            if (task instanceof MedicalAssistance) {
	        if (!people.contains(person)) people.add(person);
	    }
	}
	
	return people;
    }
}
