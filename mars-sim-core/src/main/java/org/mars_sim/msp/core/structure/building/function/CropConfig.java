/**
 * Mars Simulation Project
 * CropConfig.java
 * @version 3.06 2014-10-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Provides configuration information about greenhouse crops. Uses a DOM document to get the information.
 */
//2014-10-05 mkung: added new attributes: harvestIndex, cropCategory, ppf and photoperiod
public class CropConfig
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Element names
	private static final String CROP = "crop";
	private static final String NAME = "name";
	private static final String GROWING_TIME = "growing-time";
	private static final String CROP_CATEGORY = "crop-category";
	private static final String PPF = "ppf";
	private static final String PHOTOPERIOD = "photoperiod";
	private static final String HARVEST_INDEX = "harvest-index";

	private Document cropDoc;
	private List<CropType> cropList;

	/**
	 * Constructor.
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
	public List<CropType> getCropList() {

		if (cropList == null) {
			cropList = new ArrayList<CropType>();

			Element root = cropDoc.getRootElement();
			List<Element> crops = root.getChildren(CROP);

			for (Element crop : crops) {
				String name = "";

				// Get name.
				name = crop.getAttributeValue(NAME);

				// Get growing time.
				String growingTimeStr = crop.getAttributeValue(GROWING_TIME);
				double growingTime = Double.parseDouble(growingTimeStr);

				// Get crop category
				String cropCategory ="";
				cropCategory = crop.getAttributeValue(CROP_CATEGORY);

				// Get ppf
				String ppfStr = crop.getAttributeValue(PPF);
				double ppf = Double.parseDouble(ppfStr);

				// Get photoperiod
				String photoperiodStr = crop.getAttributeValue(PHOTOPERIOD);
				double photoperiod = Double.parseDouble(photoperiodStr);

				// Get harvestIndex
				String harvestIndexStr = crop.getAttributeValue(HARVEST_INDEX);
				double harvestIndex = Double.parseDouble(harvestIndexStr);

				// Create crop type.
				CropType cropType = new CropType(name, growingTime * 1000D, cropCategory, ppf * 1D, photoperiod * 1D, harvestIndex);

				cropList.add(cropType);
			}
		}

		return cropList;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		cropDoc = null;
		if(cropList != null){
			cropList.clear();
			cropList = null;
		}
	}
}
