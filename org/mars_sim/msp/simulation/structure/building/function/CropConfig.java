/**
 * Mars Simulation Project
 * CropConfig.java
 * @version 2.81 2007-08-27
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;


/**
 * Provides configuration information about greenhouse crops.
 * Uses a DOM document to get the information. 
 */
public class CropConfig implements Serializable {

	// Element names
	private static final String CROP = "crop";
	private static final String NAME = "name";
	private static final String GROWING_TIME = "growing-time";
	
	private Document cropDoc;
	private List<CropType> cropList;

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
    @SuppressWarnings("unchecked")
	public List<CropType> getCropList() throws Exception {
		
		if (cropList == null) {
			cropList = new ArrayList<CropType>();
			
			Element root = cropDoc.getRootElement();
			List<Element> crops = root.getChildren(CROP);
			
			for (Element crop : crops) {
				String name = "";
				
				try {
					// Get name.
					name = crop.getAttributeValue(NAME);
				
					// Get growing time.
					String growingTimeStr = crop.getAttributeValue(GROWING_TIME);
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