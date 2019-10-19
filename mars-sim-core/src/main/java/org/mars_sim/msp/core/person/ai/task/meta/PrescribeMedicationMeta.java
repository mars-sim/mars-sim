/**
 * Mars Simulation Project
 * PrescribeMedicationMeta.java
 * @version 3.1.0 2017-03-09
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
//import org.mars_sim.msp.core.location.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Doctor;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PrescribeMedication;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.health.AnxietyMedication;
import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.core.person.health.RadioProtectiveAgent;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Medicbot;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the PrescribeMedication task.
 */
public class PrescribeMedicationMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.prescribeMedication"); //$NON-NLS-1$

    private int numPatients;
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new PrescribeMedication(person);
    }

    public Task constructInstance(Robot robot) {
        return new PrescribeMedication(robot);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isOutside())
        	return 0;	
        
        Person patient = determinePatients(person);
        if (patient == null || numPatients == 0) {
        	return 0;
        }
        	
        Job job = person.getMind().getJob();
        
        if (job instanceof Doctor) {
            result = numPatients * 300D;
        }
        
        else {
        	boolean hasDoctor = hasADoctor(patient);
            if (hasDoctor) {
            	return 0;
            }
            else {
                result = numPatients * 150D;
            }
        }
            
        double pref = person.getPreference().getPreferenceScore(this);
        
        if (pref > 0)
        	result = result * 3D;
        
        if (result < 0) result = 0;
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        return result;
    }

    public boolean hasADoctor(Person patient) {
    	Collection<Person> list = null;
//        LocationSituation loc = patient.getLocationSituation();
        if (patient.isInSettlement()) {//LocationSituation.IN_SETTLEMENT == loc) {
            list = patient.getSettlement().getIndoorPeople();

        }
        else if (patient.isInVehicle()) {//LocationSituation.IN_VEHICLE == loc) {
        	Rover rover = (Rover)patient.getContainerUnit();
        	list = rover.getCrew();
        	
        }
        
        if (list != null) {
	        for (Person person : list) {
	        	Job job = person.getMind().getJob();
	        	if (job instanceof Doctor)
	        		return true;
	        }
        }
        return false;
    }
    
    
    public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.isOutside())
        	return 0;
        
        // Only medicbot or a doctor is allowed to perform this task.
        if (robot.getBotMind().getRobotJob() instanceof Medicbot) {
        	
            // Determine patient needing medication.
        	Person patient = determinePatients(robot);
            if (patient == null || numPatients == 0) {
            	return 0;
            }
	
            else {//if (patient != null) {
            	result = numPatients * 100D;             
            }
        }

        // Effort-driven task modifier.
        result *= robot.getPerformanceRating();

        return result;
    }


	public Person determinePatients(Unit doctor) {
		Person patient = null;
        Person p = null;
        Robot r = null;
        if (doctor instanceof Person)
        	p = (Person) doctor;
        else
        	r = (Robot) doctor;
        
        // Get possible patient list.
        // Note: Doctor can also prescribe medication for himself.
        Collection<Person> patientList = null;
        
        if (p != null) {
	        if (p.isInSettlement()) {//LocationSituation.IN_SETTLEMENT == p.getLocationSituation()) {
	            patientList = p.getSettlement().getIndoorPeople();
	        }
	        else if (p.isInVehicle()) {//LocationSituation.IN_VEHICLE == p.getLocationSituation()) {
	            Vehicle vehicle = p.getVehicle();
	            if (vehicle instanceof Crewable) {
	                Crewable crewVehicle = (Crewable) vehicle;
	                patientList = crewVehicle.getCrew();
	            }
	        }
        }
        
        else if (r != null) {
	        if (r.isInSettlement()) {//LocationSituation.IN_SETTLEMENT == r.getLocationSituation()) {
	            patientList = r.getSettlement().getIndoorPeople();
	        }
	        else if (r.isInVehicle()) {//LocationSituation.IN_VEHICLE == r.getLocationSituation()) {
	            Vehicle vehicle = r.getVehicle();
	            if (vehicle instanceof Crewable) {
	                Crewable crewVehicle = (Crewable) vehicle;
	                patientList = crewVehicle.getCrew();
	            }
	        }
        }

        // Determine patient.
        if (patientList != null) {
            Iterator<Person> i = patientList.iterator();
            while (i.hasNext()) {
                Person person = i.next();
                PhysicalCondition condition = person.getPhysicalCondition();
                RadiationExposure exposure = condition.getRadiationExposure();
                if (!condition.isDead()) {
                	if (condition.isStressedOut()) {
                        // Only prescribing anti-stress medication at the moment.
                        if (!condition.hasMedication(AnxietyMedication.NAME)) {
                        	patient = person;
                            numPatients++;
                        }
                	}
                	else if (exposure.isSick()) {
                        if (!condition.hasMedication(RadioProtectiveAgent.NAME)) {
                        	patient = person;
                        	numPatients++;
                        }
                	}
                }
            }
        }

        return patient;
	}

}