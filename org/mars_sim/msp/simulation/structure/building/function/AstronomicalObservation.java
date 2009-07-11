/**
 * Mars Simulation Project
 * AstronomicalObservation.java
 * @version 2.87 2009-07-04
 * @author Sebastien Venot
 */

package org.mars_sim.msp.simulation.structure.building.function;


import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Lab;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingConfig;
import org.mars_sim.msp.simulation.structure.building.BuildingException;

public class AstronomicalObservation extends Function  implements Lab {
	private static final long serialVersionUID = 1L;
	
	
    private static String CLASS_NAME = 
    "org.mars_sim.msp.simulation.structure.building.function.AstronomicalObservation";

    private static Logger s_log = Logger.getLogger(CLASS_NAME);
    public  static String NAME = "Astronomical Observations";
    private double powerRequired;
    private int techLevel;
	private int labCapacity;
	private int observatoryCapacity;
	private List<String> researchSpecialities;
	private int researchersInLab;
	private int researchersInObservatory;
	
    
	public AstronomicalObservation(Building building) throws BuildingException {
		super(NAME, building);
		
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		
		try {
			powerRequired = config.getBasePowerRequirement(building.getName());
			techLevel = config.getResearchTechLevel(building.getName());
			labCapacity = config.getResearchCapacity(building.getName());
			observatoryCapacity = config.getAstronomicalObservationCapacity(building.getName());
			researchSpecialities = config.getResearchSpecialities(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("AstronomicalObservation.constructor: " 
					                     + e.getMessage());
		}
		
	}




	public double getFullPowerRequired() {
		return powerRequired;
	}

	public double getPowerDownPowerRequired() {
		return 0;
	}

	public void timePassing(double time) throws BuildingException {
		
	}

	@Override
	public void addResearcher() throws Exception {
		researchersInLab ++;
		if (researchersInLab > labCapacity) {
			researchersInLab = labCapacity;
			throw new Exception("Observatory lab already full of researchers.");
		}
	}
	

	public void addResearcherToObservatory() throws Exception {
		researchersInObservatory ++;
		if (researchersInLab > observatoryCapacity) {
			researchersInLab= observatoryCapacity;
			throw new Exception("Observatory already full of researchers.");
		}
	}


	public int getLaboratorySize() {
		return labCapacity;
	}

	@Override
	public int getResearcherNum() {
		return researchersInLab;
	}


	public String[] getTechSpecialities() {
		String[] result = new String[researchSpecialities.size()];
		for (int x=0; x < researchSpecialities.size(); x++)
			result[x] = (String) researchSpecialities.get(x);
		return result;
	}


	public int getTechnologyLevel() {
		return techLevel;
	}

	public boolean hasSpeciality(String speciality) {
		boolean result = false;
		Iterator<String> i = researchSpecialities.iterator();
		while (i.hasNext()) {
			if (i.next().equalsIgnoreCase(speciality)) result = true;
		}
		return result;
	}

	@Override
	public void removeResearcher() throws Exception {
		researchersInLab --;
		if (getResearcherNum() < 0) {
			researchersInLab = 0; 
			throw new Exception("Lab is already empty of researchers.");
		}
				
	}
	
	
	public void removeResearcherFromObservatory() throws Exception {
		researchersInObservatory --;
		if (researchersInObservatory < 0) {
			researchersInObservatory = 0; 
			throw new Exception("Lab is already empty of researchers.");
		}
		
	}
	
	 public static final double getFunctionValue(String buildingName, boolean newBuilding, 
	            Settlement settlement) throws Exception {
		 
		    //TODO: to improve...
		    BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
	        List<String> specialities = config.getResearchSpecialities(buildingName);
	        
	        double researchDemand = 0D;
	        Iterator<String> i = specialities.iterator();
	        while (i.hasNext()) {
	            String speciality = i.next();
	            Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
	            while (j.hasNext()) 
	                researchDemand += j.next().getMind().getSkillManager().getSkillLevel(speciality);
	        }
	        
	        double researchSupply = 0D;
	        boolean removedBuilding = false;
	        Iterator<Building> k = settlement.getBuildingManager().getBuildings(NAME).iterator();
	        while (k.hasNext()) {
	            Building building = k.next();
	            if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
	                removedBuilding = true;
	            }
	            else {
	            	AstronomicalObservation astroFunction = (AstronomicalObservation) building.getFunction(NAME);
	                int techLevel = astroFunction.getTechnologyLevel();
	                int labSize = astroFunction.getLaboratorySize();
	                for (int x = 0; x < astroFunction.getTechSpecialities().length; x++) {
	                    String speciality = astroFunction.getTechSpecialities()[x];
	                    if (specialities.contains(speciality)) researchSupply += techLevel * labSize;
	                }
	            }
	        }
	        
	        double existingResearchValue = researchDemand / (researchSupply + 1D);
	        
	        int techLevel = config.getResearchTechLevel(buildingName);
	        int labSize = config.getResearchCapacity(buildingName);
	        double buildingResearchSupply = specialities.size() * techLevel * labSize;
	        
	        return buildingResearchSupply * existingResearchValue;
	    }
	 
	 public int getCurrentObserversNumber(){
		 return researchersInObservatory;
	 }
	 
	 public int getObservatoryCapacity(){
		 return observatoryCapacity;
	 }

}
