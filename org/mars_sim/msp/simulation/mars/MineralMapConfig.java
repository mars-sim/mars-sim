/**
 * Mars Simulation Project
 * MineralMap.java
 * @version 2.84 2008-04-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.mars;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MineralMapConfig implements Serializable {
	
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
	 * @param mineralDoc the XML document.
	 */
	public MineralMapConfig(Document mineralDoc) {
		this.mineralDoc = mineralDoc;
	}
	
	List<MineralType> getMineralTypes() throws Exception {
		if (mineralTypes != null) return mineralTypes;
		else {
			mineralTypes = new ArrayList<MineralType>();
		
			Element root = mineralDoc.getDocumentElement();
			NodeList minerals = root.getElementsByTagName(MINERAL);
		
			for (int x = 0; x < minerals.getLength(); x++) {
				String name = "";
			
				try {
					Element mineral = (Element) minerals.item(x);
				
					// Get mineral name.
					name = mineral.getAttribute(NAME).toLowerCase().trim();
				
					// Get frequency.
					String frequency = mineral.getAttribute(FREQUENCY).toLowerCase().trim();
				
					// Create mineralType.
					MineralType mineralType = new MineralType(name, frequency);
				
					// Get locales.
					Element localeList = (Element) mineral.getElementsByTagName(LOCALE_LIST).item(0);
					NodeList locales = localeList.getElementsByTagName(LOCALE);
					for (int y = 0; y < locales.getLength(); y++) {
						Element locale = (Element) locales.item(y);
						String localeName = locale.getAttribute(NAME).toLowerCase().trim();
						mineralType.addLocale(localeName);
					}
				
					// Add mineral type to list.
					mineralTypes.add(mineralType);
				}
				catch (Exception e) {
					throw new Exception("Error reading mineral type " + name + ": " + e.getMessage());
				}
			}
		
			return mineralTypes;
		}
	}
	
	class MineralType implements Serializable {
		
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