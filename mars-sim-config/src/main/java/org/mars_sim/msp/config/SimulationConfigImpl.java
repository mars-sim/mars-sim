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

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
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

import java.io.InputStreamReader;

/**
 * DOCME: documentation is missing
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:mail@landrus.de">Christian Domsch</a>
 *
 */
public class SimulationConfigImpl implements SimulationConfig {

	/* ---------------------------------------------------------------------- *
	 * Members
	 * ---------------------------------------------------------------------- */

	private BuildingList buildings;
	private Construction constructions;
	private CropList crops;
	private LandmarkList landmarks;
	private MalfunctionList malfunctions;
	private Medical medicals;
	private MineralConcentrations minerals;
	private PartPackageList partPackages;
	private PartList parts;
	private PeopleConfiguration people;
	private ResourceList resources;
	private ResupplyList resupplies;
	private SettlementConfiguration settlements;
	private SimulationConfiguration simulationConfiguration;
	private VehicleConfiguration vehicles;

	/* ---------------------------------------------------------------------- *
	 * Constructors
	 * ---------------------------------------------------------------------- */

	public SimulationConfigImpl() throws MarshalException, ValidationException {
		load();
	}

	/* ---------------------------------------------------------------------- *
	 * Implements SimulationConfig
	 * ---------------------------------------------------------------------- */

	/** {@inheritDoc} */
	@Override
	public BuildingList getBuildings() {
		return buildings;
	}

	/** {@inheritDoc} */
	@Override
	public Construction getConstructions() {
		return constructions;
	}

	/** {@inheritDoc} */
	@Override
	public CropList getCrops() {
		return crops;
	}

	/** {@inheritDoc} */
	@Override
	public LandmarkList getLandmarks() {
		return landmarks;
	}

	/** {@inheritDoc} */
	@Override
	public MalfunctionList getMalfunctions() {
		return malfunctions;
	}

	/** {@inheritDoc} */
	@Override
	public Medical getMedicals() {
		return medicals;
	}

	/** {@inheritDoc} */
	@Override
	public MineralConcentrations getMinerals() {
		return minerals;
	}

	/** {@inheritDoc} */
	@Override
	public PartPackageList getPartPackages() {
		return partPackages;
	}

	/** {@inheritDoc} */
	@Override
	public PartList getParts() {
		return parts;
	}

	/** {@inheritDoc} */
	@Override
	public PeopleConfiguration getPeople() {
		return people;
	}

	/** {@inheritDoc} */
	@Override
	public ResourceList getResources() {
		return resources;
	}

	/** {@inheritDoc} */
	@Override
	public ResupplyList getResupplies() {
		return resupplies;
	}

	/** {@inheritDoc} */
	@Override
	public SettlementConfiguration getSettlements() {
		return settlements;
	}

	/** {@inheritDoc} */
	@Override
	public SimulationConfiguration getSimulationConfiguration() {
		return simulationConfiguration;
	}

	/** {@inheritDoc} */
	@Override
	public VehicleConfiguration getVehicles() {
		return vehicles;
	}

	/* ---------------------------------------------------------------------- *
	 * Private Methods
	 * ---------------------------------------------------------------------- */

	private void load() throws MarshalException, ValidationException {
		buildings = BuildingList.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/building.xml")));
		constructions = Construction.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/construction.xml")));
		crops = CropList.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/crops.xml")));
		landmarks = LandmarkList.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/landmarks.xml")));
		malfunctions = MalfunctionList.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/malfunctions.xml")));
		medicals = Medical.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/medical.xml")));
		minerals = MineralConcentrations.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/minerals.xml")));
		partPackages = PartPackageList.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/part_packages.xml")));
		parts = PartList.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/parts.xml")));
		people = PeopleConfiguration.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/people.xml")));
		resources = ResourceList.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/resources.xml")));
		resupplies = ResupplyList.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/resupplies.xml")));
		settlements = SettlementConfiguration.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/settlements.xml")));
		simulationConfiguration = SimulationConfiguration.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/simulation.xml")));
		vehicles = VehicleConfiguration.unmarshal(new InputStreamReader(getClass().getResourceAsStream("/config/vehicles.xml")));
	}

}
