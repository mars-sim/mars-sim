/**
 * Mars Simulation Project
 * ItemResourceUtil.java
 * @version 3.1.0 2017-09-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	public static final String EXTINGUSHER = "fire extingusher";
	public static final String WORK_GLOVES = "work gloves";
	public static final String CONTAINMENT = "mushroom containment kit";
	
	private static PartConfig partConfig  = SimulationConfig.instance().getPartConfiguration();
	
	public static Part pneumaticDrillAR = (Part) findItemResource(PNEUMATIC_DRILL);
	public static Part backhoeAR = (Part) findItemResource(BACKHOE);
	public static Part smallHammerAR = (Part) findItemResource(SMALL_HAMMER);
	public static Part socketWrenchAR = (Part) findItemResource(SOCKET_WRENCH);
	public static Part pipeWrenchAR = (Part) findItemResource(PIPE_WRENCH);
	public static Part fireExtinguisherAR = (Part) findItemResource(EXTINGUSHER);
	public static Part workGlovesAR = (Part) findItemResource(WORK_GLOVES);
	public static Part mushroomBoxAR = (Part) findItemResource(CONTAINMENT);

	private static Set<Part> partSet;

	private static List<Part> sortedParts;

	public ItemResourceUtil() {
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

	
	public static List<Part> getSortedParts() {
		sortedParts = new ArrayList<>(partSet);
		Collections.sort(sortedParts);
		return sortedParts;
	}
	
	public static Map<String, Part> getItemResourcesMap() {
		//if (partConfig == null) System.err.println("partConfig == null");
		return partConfig.getNamePartMap();
	}
}


