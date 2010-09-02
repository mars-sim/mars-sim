/*
 * $Id$
 *
 * Copyright 2010 Home Entertainment Systems.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mars_sim.msp.config;

import org.mars_sim.msp.config.model.building.BuildingList;
import org.mars_sim.msp.config.model.construction.Construction;
import org.mars_sim.msp.config.model.crop.CropList;
import org.mars_sim.msp.config.model.landmark.LandmarkList;
import org.mars_sim.msp.config.model.malfunction.MalfunctionList;
import org.mars_sim.msp.config.model.medical.Medical;
import org.mars_sim.msp.config.model.mineral.MineralConcentrations;
import org.mars_sim.msp.config.model.part.PartList;
import org.mars_sim.msp.config.model.partpackage.PartPackageList;
import org.mars_sim.msp.config.model.people.PeopleConfiguration;
import org.mars_sim.msp.config.model.resource.ResourceList;
import org.mars_sim.msp.config.model.resupply.ResupplyList;
import org.mars_sim.msp.config.model.settlement.SettlementConfiguration;
import org.mars_sim.msp.config.model.simulation.SimulationConfiguration;
import org.mars_sim.msp.config.model.vehicle.VehicleConfiguration;

/**
 * DOCME: documentation is missing
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:mail@landrus.de">Christian Domsch</a>
 *
 */
public interface SimulationConfig {

	VehicleConfiguration getVehicles();
	SimulationConfiguration getSimulationConfiguration();
	SettlementConfiguration getSettlements();
	ResupplyList getResupplies();
	ResourceList getResources();
	PeopleConfiguration getPeople();
	PartList getParts();
	PartPackageList getPartPackages();
	MineralConcentrations getMinerals();
	Medical getMedicals();
	MalfunctionList getMalfunctions();
	LandmarkList getLandmarks();
	CropList getCrops();
	Construction getConstructions();
	BuildingList getBuildings();

}
