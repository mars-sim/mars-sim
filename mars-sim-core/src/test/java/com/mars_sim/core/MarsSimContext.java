package com.mars_sim.core;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.unit.UnitHolder;

/**
 * Context that allows creating simulation entities for unit tests
 */
public interface MarsSimContext {

    Person buildPerson(String name, Settlement s);

    Building buildResearch(BuildingManager buildingManager, LocalPosition position, double facing, int i);

    Building buildEVA(BuildingManager buildingManager, LocalPosition position, double facing, int id);

    SimulationConfig getConfig();
    
    Simulation getSim();

    ClockPulse createPulse(MarsTime marsTime, boolean newSol, boolean newHalfSol);

    Building buildFunction(BuildingManager buildingManager, String string, BuildingCategory medical,
            FunctionType medicalCare, LocalPosition defaultPosition, double d, boolean b);

    UnitHolder getSurface();

}
