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

import java.io.InputStreamReader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.mars_sim.msp.config.model.building.BuildingList;
import org.mars_sim.msp.config.model.construction.Construction;
import org.mars_sim.msp.config.model.crop.CropList;
import org.mars_sim.msp.config.model.landmark.LandmarkList;
import org.mars_sim.msp.config.model.malfunction.MalfunctionList;
import org.mars_sim.msp.config.model.medical.Medical;

/**
 * DOCME: documentation is missing
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:mail@landrus.de">Christian Domsch</a>
 *
 */
public class SimulationConfig {

	/* ---------------------------------------------------------------------- *
	 * Members
	 * ---------------------------------------------------------------------- */

	private BuildingList buildings;
	private Construction constructions;
	private CropList crops;
	private LandmarkList landmarks;
	private MalfunctionList malfunctions;
	private Medical medicals;

	/* ---------------------------------------------------------------------- *
	 * Constructors
	 * ---------------------------------------------------------------------- */

	public SimulationConfig() throws MarshalException, ValidationException {
		load();
	}

	/* ---------------------------------------------------------------------- *
	 * Getter
	 * ---------------------------------------------------------------------- */

	public BuildingList getBuildings() {
		return buildings;
	}

	public Construction getConstructions() {
		return constructions;
	}

	public CropList getCrops() {
		return crops;
	}

	public LandmarkList getLandmarks() {
		return landmarks;
	}

	public MalfunctionList getMalfunctions() {
		return malfunctions;
	}

	public Medical getMedicals() {
		return medicals;
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
	}

}
