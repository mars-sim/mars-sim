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

package org.mars_sim_msp.config;

import junit.framework.Assert;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Test;
import org.mars_sim.msp.config.SimulationConfigImpl;
import org.mars_sim.msp.config.model.building.Building;

/**
 * DOCME: documentation is missing
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:mail@landrus.de">Christian Domsch</a>
 *
 */
public class Load {

	@Test
	public void load() {
		try {
			SimulationConfigImpl config = new SimulationConfigImpl();

			for (Building building : config.getBuildings().getBuilding()) {
				System.out.println(building.getName());
			}
		} catch (MarshalException e) {
			e.printStackTrace();
			Assert.fail("Marshalling error");
		} catch (ValidationException e) {
			e.printStackTrace();
			Assert.fail("Validation error");
		}
	}

}
