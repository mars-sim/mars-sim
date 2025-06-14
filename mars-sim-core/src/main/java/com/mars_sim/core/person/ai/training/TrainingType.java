/*
 * Mars Simulation Project
 * TrainingType.java
 * @date 2023-07-21
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.training;

import com.mars_sim.core.tool.Msg;

public enum TrainingType {

	BIOETHICAL,EXTREME_ENV_OPS,NASA_DESERT_RATS, SURVIVAL_TRAINING,
	SCUBA_DIVING_MASTER,FLIGHT_SAFETY,SEARCH_AND_RESCUE,MOUNTAINEERING_MASTER,
	AIRBORNE_AND_RANGER_SCHOOL,HAUGHTON_MARS_GEOLOGICAL,HALO_JUMPMASTER,
	MISHAP_INVESTIGATION,MARS_500_C,MARS_ANALOG_ENVIRONMENT,UNDERSEA_MISSION,
	MILITARY_DEPLOYMENT,AVIATION_CERTIFICATION,ANTARCTICA_EDEN_ISS,MARS_TWO_FINAL_100,
	UNDERGROUND_CAVES_EXPLORATION,NASA_CHAPEA,MARS_V_GOBI;
	
	// References : 
	// 1. ESA Cooperative Adventure for Valuing and Exercising human behaviour and performance Skills (CAVES) training. 
	//    A three-week course prepares astronauts to work safely and effectively in multicultural teams in an environment 
	//    where safety is critical – in caves. 
	//    http://www.esa.int/Our_Activities/Human_and_Robotic_Exploration/Caves/A_new_journey_into_Earth_for_space_exploration
	// 2. NASA Desert Research and Technology Studies (Desert RATS) 
	//    https://www.nasa.gov/hrp/research/analogs/drats  
	//    https://www.youtube.com/watch?v=2Q13dyRi5ok 
	// 3. EDEN ISS is a 4-year project under the European Union‘s Research and Innovation Action program Horizon 2020
	//    Ground Demonstration of Plant Cultivation Technologies for Safe Food Production in Space
	//    https://eden-iss.net/
	//    https://www.youtube.com/watch?v=KOeSlwwuvWQ
	// 4. MARS-500 was intended to study the psychological, physiological, and technological challenges inherent to long-duration 
	//    space flight. Among other hurdles to overcome, the experiment examined the physiological effects of long-term weightlessness,
	//    the effectiveness of resource management, and the effects of isolation in a hermetically sealed environment. 
	//    MARS-500's communication systems were designed with an average delay of 13 min, to simulate the actual transmission time 
	//    to and from a Mars-bound spacecraft.
	//    https://en.wikipedia.org/wiki/MARS-500
	// 5. NASA CHAPEA allow researchers to collect cognitive and physical performance data to give us more insight into the 
	//    potential impacts of long-duration missions to Mars on crew health and performance.
	//    https://www.space.com/nasa-year-long-mars-simulation-chapea
	//    https://www.wearethemighty.com/military-news/chapea-nasa-first-mars-simulation-crew-includes-a-navy-microbiologist/

	private String name;

	/** hidden constructor. */
	private TrainingType() {
        this.name = Msg.getStringOptional("TrainingType", name());
	}

	public final String getName() {
		return this.name;
	}
}
