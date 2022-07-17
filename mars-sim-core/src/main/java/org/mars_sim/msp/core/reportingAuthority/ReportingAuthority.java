/*
 * Mars Simulation Project
 * ReportingAuthority.java
 * @date 2022-07-15
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.configuration.UserConfigurable;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Represents a Reporting Authority that "owns" units.
 */
public class ReportingAuthority
implements UserConfigurable, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(ReportingAuthority.class.getName());

	// Maximum points for a top priority subaganda
	private static final double MAX_RATIO_PER_SUBAGENDA = 0.5D;
	// Maximum priority value
	private static final int MAX_PRIORITY_PER_SUBAGENDA = 10;

	private MissionAgenda missionAgenda;

	private String description;

	private String name;

	private List<String> countries;

	private List<String> settlementNames;

	private List<String> vehicleNames;

	private boolean predefined;
 
	public ReportingAuthority(String name, String description, boolean predefined,
							  MissionAgenda agenda, List<String> countries,
							  List<String> names, List<String> vehicleNames) {
		this.description  = description;
		this.name = name;
		this.predefined = predefined;
		this.missionAgenda = agenda;
		this.countries = countries;
		this.settlementNames = names;
		this.vehicleNames = vehicleNames;
	}

	/**
	 * Works on the mission objectives conducted.
	 * 
	 * @param unit
	 */
	public void conductMissionObjective(Worker unit) {
		missionAgenda.reportFindings(unit);
		missionAgenda.gatherSamples(unit);
	}

	/**
	 * Gets the Mission Agenda for this authority.
	 * 
	 * @return
	 */
	public MissionAgenda getMissionAgenda() {
		return missionAgenda;
	}

	/**
	 * Gets the unique name of this authority which is the short code.
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Gets the full name of the authority.
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isBundled() {
		return predefined;
	}
	
	/**
	 * Gets the countries associated to this Authority.
	 * 
	 * @return
	 */
	public List<String> getCountries() {
		return countries;
	}

	/**
	 * Gets the default Country for Authority. If there are multiples
	 * then one is chosen.
	 * 
	 * @return
	 */
	public String getDefaultCountry() {
		if (countries.size() == 1) {
			return countries.get(0);
		}
		else {
			return countries.get(RandomUtil.getRandomInt(0, countries.size() - 1));	
		}
	}
	
	/**
	 * Gets the name of Settlement for this Authority.
	 * 
	 * @return
	 */
	public List<String> getSettlementNames() {
		return settlementNames;
	}

	/**
	 * Gets the potential names of Vehicles.
	 * 
	 * @return
	 */
	public List<String> getVehicleNames() {
		return vehicleNames;
	}
	
	@Override
	public String toString() {
		return "ReportingAuthority [code=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportingAuthority other = (ReportingAuthority) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/**
	 * Gets a favourite ratio for a particular mission. Value of 1.0 is neutral.
	 * 
	 * @param type
	 * @return Ratio to apply to the mission score
	 */
    public double getMissionRatio(MissionType type) {
        double result = 1D;
		for (MissionSubAgenda subAgenda : missionAgenda.getAgendas()) {
			int modifier = subAgenda.getMissionModifiers().getOrDefault(type, 0);

			// The modifier is a value of 0..10 that represents a priority.
			// For each priority point add a value to the ratio. 
			modifier = Math.min(modifier, MAX_PRIORITY_PER_SUBAGENDA);
			result += (modifier * MAX_RATIO_PER_SUBAGENDA)/MAX_PRIORITY_PER_SUBAGENDA;
		}

		if (result != 1D) {
			logger.info(name + " has returned a boost " + result + " for Mission " + type);
		}
		return result;
    }

	/**
	 * Gets a favourite ratio for a particular science. Value of 1.0 is neutral.
	 * 
	 * @param science
	 * @return Ratio to apply to the Study score
	 */
    public double getStudyRatio(ScienceType science) {
        double result = 1D;
		for (MissionSubAgenda subAgenda : missionAgenda.getAgendas()) {
			int modifier = subAgenda.getScienceModifiers().getOrDefault(science, 0);

			// The modifier is a value of 0..10 that represents a priority.
			// For each priority point add a value to the ratio. 
			modifier = Math.min(modifier, MAX_PRIORITY_PER_SUBAGENDA);
			result += (modifier * MAX_RATIO_PER_SUBAGENDA)/MAX_PRIORITY_PER_SUBAGENDA;
		}
 		return result;
    }

}
