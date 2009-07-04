package org.mars_sim.msp.simulation.structure.building.function;

import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Lab;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingConfig;
import org.mars_sim.msp.simulation.structure.building.BuildingException;

public class AstronomicalObservation extends Function  implements Lab {
	private static final long serialVersionUID = 1L;
	
    private static String CLASS_NAME = 
    "org.mars_sim.msp.simulation.structure.building.function.AstronomicalObservation";

    private static Logger s_log = Logger.getLogger(CLASS_NAME);
    private static String NAME = "Astronomical Observations";
    private double powerRequired;
    private int techLevel;
	private int researcherCapacity;
	private List<String> researchSpecialities;
	private int researcherNum;
    
	public AstronomicalObservation(Building building) throws BuildingException {
		super(NAME, building);
		
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		
		try {
			powerRequired = config.getBasePowerRequirement(building.getName());
			techLevel = config.getResearchTechLevel(building.getName());
			researcherCapacity = config.getResearchCapacity(building.getName());
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
		researcherNum ++;
		if (researcherNum > researcherCapacity) {
			researcherNum = researcherCapacity;
			throw new Exception("Observatory already full of researchers.");
		}
	}

	@Override
	public int getLaboratorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getResearcherNum() {
		// TODO Auto-generated method stub
		return researcherNum;
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
		return researchSpecialities.contains(speciality);
	}

	@Override
	public void removeResearcher() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
