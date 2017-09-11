/**
 * Mars Simulation Project
 * ItemResourceUtil.java
 * @version 3.1.0 2017-09-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.SimulationConfig;

public class ItemResourceUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Light utility vehicle attachment parts for mining.
	public static final String PNEUMATIC_DRILL = "pneumatic drill";
	public static final String BACKHOE = "backhoe";
	public static final String SMALL_HAMMER = "small hammer";
	public static final String SOCKET_WRENCH = "socket wrench";
	public static final String PIPE_WRENCH = "pipe wrench";

	public static Part pneumaticDrill, backhoe, smallHammer, socketWrench, pipeWrench;

	private static Set<Part> partSet;
	
	private static PartConfig partConfig;
	
	public ItemResourceUtil() {

		partConfig = SimulationConfig.instance().getPartConfiguration();

		pneumaticDrill = (Part) findItemResource(PNEUMATIC_DRILL);
		backhoe = (Part) findItemResource(BACKHOE);
		smallHammer = (Part) findItemResource(SMALL_HAMMER);
		socketWrench = (Part) findItemResource(SOCKET_WRENCH);
		pipeWrench = (Part) findItemResource(PIPE_WRENCH);
		
	}

	/**
	 * Finds an item resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static ItemResource findItemResource(String name) {
		// 2016-12-08 Using Java 8 stream
		return getItemResources()
				.stream()
				.filter(item -> item.getName().equals(name.toLowerCase()))
				.findFirst().orElse(null);//.get();

		//return getItemResourcesMap().get(name.toLowerCase());
	}
	
	/**
	 * Gets a ummutable collection of all the item resources.
	 * @return collection of item resources.
	 */
	//public static Set<ItemResource> getItemResources() {
	//	return Collections.unmodifiableSet(partConfig.getItemResources());
	//}

	public static Set<Part> getItemResources() {
		if (partSet == null)
			partSet = Collections.unmodifiableSet(partConfig.getPartSet());
		return partSet;
	}

	public static Map<String, Part> getItemResourcesMap() {
		//if (partConfig == null) System.err.println("partConfig == null");
		return partConfig.getNamePartMap();
	}
}


