/**
 * Mars Simulation Project
 * MineralMap.java
 * @version 3.1.0 2017-10-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

public class MineralMapConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Element names
	private static final String MINERAL = "mineral";
	private static final String NAME = "name";
	private static final String FREQUENCY = "frequency";
	private static final String LOCALE_LIST = "locale-list";
	private static final String LOCALE = "locale";

	private Document mineralDoc;
	private List<MineralType> mineralTypes;

	/**
	 * Constructor
	 * 
	 * @param mineralDoc the XML document.
	 */
	public MineralMapConfig(Document mineralDoc) {
		this.mineralDoc = mineralDoc;
	}

	@SuppressWarnings("unchecked")
	List<MineralType> getMineralTypes() {
		if (mineralTypes != null)
			return mineralTypes;
		else {
			mineralTypes = new ArrayList<MineralType>();

			Element root = mineralDoc.getRootElement();
			List<Element> minerals = root.getChildren(MINERAL);

			for (Element mineral : minerals) {
				String name = "";

				// Get mineral name.
				name = mineral.getAttributeValue(NAME).toLowerCase().trim();

				// Get frequency.
				String frequency = mineral.getAttributeValue(FREQUENCY).toLowerCase().trim();

				// Create mineralType.
				MineralType mineralType = new MineralType(name, frequency);

				// Get locales.
				Element localeList = mineral.getChild(LOCALE_LIST);
				List<Element> locales = localeList.getChildren(LOCALE);

				for (Element locale : locales) {
					String localeName = locale.getAttributeValue(NAME).toLowerCase().trim();
					mineralType.addLocale(localeName);
				}

				// Add mineral type to list.
				mineralTypes.add(mineralType);
			}

			return mineralTypes;
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		mineralDoc = null;
		if (mineralTypes != null) {

			mineralTypes.clear();
			mineralTypes = null;
		}
	}

	static class MineralType implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		String name;
		String frequency;
		List<String> locales;

		private MineralType(String name, String frequency) {
			this.name = name;
			this.frequency = frequency;
			locales = new ArrayList<String>(3);
		}

		private void addLocale(String localeName) {
			locales.add(localeName);
		}
	}
}