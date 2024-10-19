/*
 * Mars Simulation Project
 * MineralMapConfig.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package com.mars_sim.core.mineral;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.goods.GoodType;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * Holds the details of the supported mineral types.
 */
public class MineralMapConfig {

	// Element names
	private static final String MINERAL = "mineral";
	private static final String COLOR = "color";
	private static final String NAME = "name";
	private static final String FREQUENCY = "frequency";
	private static final String LOCALE_LIST = "locale-list";
	private static final String LOCALE = "locale";

	// Frequency Strings
	private static final String COMMON_TAG = "common";
	private static final String UNCOMMON_TAG = "uncommon";
	private static final String RARE_TAG = "rare";
	private static final String VERY_RARE_TAG = "very rare";
	static final int COMMON_FREQUENCY = 10;
	static final int UNCOMMON_FREQUENCY = 30;
	static final int RARE_FREQUENCY = 60;
	static final int VERY_RARE_FREQUENCY = 90;

	private List<MineralType> mineralTypes;

	/**
	 * Constructor.
	 * 
	 * @param mineralDoc DOM document containing mineral configuration.
	 */
	public MineralMapConfig(Document mineralDoc) {
		buildMineralList(mineralDoc);
	}

	/**
	 * Gets a list of mineralTypes.
	 * 
	 * @return list of mineralTypes
	 * @throws Exception when mineralTypes can not be resolved.
	 */
	public List<MineralType> getMineralTypes() {
		return mineralTypes;
	}
	
	/**
	 * Gets the dividend due to frequency of mineral type.
	 * 
	 * @param frequency the frequency ("common", "uncommon", "rare" or "very rare").
	 * @return frequency modifier.
	 */
	private static int getFrequencyModifier(String frequency) {
		return switch(frequency) {
			case COMMON_TAG -> COMMON_FREQUENCY;
			case UNCOMMON_TAG -> UNCOMMON_FREQUENCY;
			case RARE_TAG -> RARE_FREQUENCY;
			case VERY_RARE_TAG -> VERY_RARE_FREQUENCY;
			default -> 1;
		};
	}

	/**
	 * Builds the mineralTypes list.
	 * 
	 * @param configDoc
	 */
	private synchronized void buildMineralList(Document configDoc) {
		if (mineralTypes != null) {
			// just in case if another thread is being created
			return;
		}
			
		// Build the global list in a temp to avoid access before it is built
		List<MineralType> newList = new ArrayList<>();

		Element root = configDoc.getRootElement();
		List<Element> minerals = root.getChildren(MINERAL);
		for (Element mineral : minerals) {
			String name = mineral.getAttributeValue(NAME).trim();
			var resource = ResourceUtil.findAmountResource(name);
			if ((resource == null) || (resource.getGoodType() != GoodType.MINERAL)) {
				throw new IllegalArgumentException(name + " is not a known mineral resource");
			}
			String color = mineral.getAttributeValue(COLOR).trim();
			var frequency = getFrequencyModifier(mineral.getAttributeValue(FREQUENCY).trim());

			// Get locales.
			Element localeList = mineral.getChild(LOCALE_LIST);
			Set<String> locales = localeList.getChildren(LOCALE).stream()
							.map(m -> m.getAttributeValue(NAME).trim().toLowerCase())
							.collect(Collectors.toSet());

			// Create mineralType.
			MineralType mineralType = new MineralType(resource.getID(), name, color, frequency, locales);

			// Add mineral type to newList.
			newList.add(mineralType);
		}

		// Assign the newList now built
		mineralTypes = Collections.unmodifiableList(newList);
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		if (mineralTypes != null) {
			mineralTypes = null;
		}
	}
}
