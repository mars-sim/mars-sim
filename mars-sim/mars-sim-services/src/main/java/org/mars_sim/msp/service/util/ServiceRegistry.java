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
package org.mars_sim.msp.service.util;

import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;

/**
 * DOCME: documentation is missing
 * 
 * @version $Revision: 14 $ $Date: 2007-03-29 19:20:40 +0000 (Do, 29 Mrz 2007) $
 * @author <a href="mailto:mail@landrus.de">Christian Domsch</a>
 */
public class ServiceRegistry {

	/* ---------------------------------------------------------------------- *
	 * Members
	 * ---------------------------------------------------------------------- */

	private static boolean initialized = false;
	private static Registry registry;

	/* ---------------------------------------------------------------------- *
	 * Constructors
	 * ---------------------------------------------------------------------- */

	private ServiceRegistry() {
	}

	/* ---------------------------------------------------------------------- *
	 * Interface
	 * ---------------------------------------------------------------------- */

	public static void construct() {
		if (!initialized) {
			registry = IOCUtilities.buildDefaultRegistry();
			initialized = true;
		}
	}

	public static <T> T getService(Class<T> serviceInterface) {
		construct();
		return registry.getService(serviceInterface);
	}

	public static <T> T getService(String serviceId, Class<T> serviceInterface) {
		construct();
		return registry.getService(serviceId, serviceInterface);
	}

	public static void shutdown() {
		if (initialized) {
			registry.shutdown();
		}
	}

}
