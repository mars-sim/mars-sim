/**
 * Mars Simulation Project
 * CropConfig.java
 * @version 2.75 2004-03-18
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.util.*;

import org.w3c.dom.*;

/**
 * Provides configuration information about greenhouse crops.
 * Uses a DOM document to get the information. 
 */
public class CropConfig {

	// Element names
	private static final String CROP = "crop";
	private static final String NAME = "name";
	private static final String GROWING_TIME = "growing-time";
	
	private Document cropDoc;
	private List cropList;

	/**
	 * Constructor
	 * @param cropDoc the crop DOM document.
	 */
	public CropConfig(Document cropDoc) {
		this.cropDoc = cropDoc;
	}
	
	/**
	 * Gets a list of crop types.
	 * @return list of crop types
	 * @throws Exception when crops could not be parsed.
	 */
	public List getCropList() throws Exception {
		
		if (cropList == null) {
			cropList = new ArrayList();
			
			Element root = cropDoc.getDocumentElement();
			NodeList crops = root.getElementsByTagName(CROP);
			for (int x=0; x < crops.getLength(); x++) {
				String name = "";
				
				try {
					Element crop = (Element) crops.item(x);
				
					// Get name.
					name = crop.getAttribute(NAME);
				
					// Get growing time.
					String growingTimeStr = crop.getAttribute(GROWING_TIME);
					double growingTime = Double.parseDouble(growingTimeStr);
				
					// Create crop type.
					CropType cropType = new CropType(name, growingTime * 1000D);
				
					cropList.add(cropType);
				}
				catch (Exception e) {
					throw new Exception("Problems reading crop " + name + ": " + e.getMessage());
				}
			}
		}
		
		return cropList;
	}
}