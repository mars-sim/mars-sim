/**
 * Mars Simulation Project
 * CropConfig.java
 * @version 3.08 2015-04-08
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
//2014-10-14 mkung: added new attribute: edibleBiomass, inedibleBiomass, edibleWaterContent.
// commented out ppf and photoperiod
public class CropConfig
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Element names
	// 2015-12-04 Added a number of elements below
	private static final String OXYGEN_CONSUMPTION_RATE = "oxygen-consumption-rate";
	private static final String WATER_CONSUMPTION_RATE = "water-consumption-rate";
	private static final String CARBON_DIOXIDE_CONSUMPTION_RATE = "carbon-dioxide-consumption-rate";
	private static final String VALUE= "value";
	
	private static final String CROP_LIST = "crop-list";
	private static final String CROP = "crop";
	private static final String NAME = "name";
	private static final String GROWING_TIME = "growing-time";
	private static final String CROP_CATEGORY = "crop-category";
	//private static final String PPF = "ppf";
	//private static final String PHOTOPERIOD = "photoperiod";
	private static final String EDIBLE_BIOMASS = "edible-biomass";
	private static final String EDIBLE_WATER_CONTENT = "edible-water-content";
	private static final String INEDIBLE_BIOMASS = "inedible-biomass";
	private static final String DAILY_PAR = "daily-PAR";
	//private static final String HARVEST_INDEX = "harvest-index";

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
			Element cropElement = root.getChild(CROP_LIST);
			List<Element> crops = cropElement.getChildren(CROP);

			for (Element crop : crops) {
				String name = "";

				// Get name.
				name = crop.getAttributeValue(NAME).toLowerCase();

				// Get growing time.
				String growingTimeStr = crop.getAttributeValue(GROWING_TIME);
				double growingTime = Double.parseDouble(growingTimeStr);

				// Get crop category
				String cropCategory ="";
				cropCategory = crop.getAttributeValue(CROP_CATEGORY);

				// Get ppf
				//String ppfStr = crop.getAttributeValue(PPF);
				//double ppf = Double.parseDouble(ppfStr);

				// Get photoperiod
				//String photoperiodStr = crop.getAttributeValue(PHOTOPERIOD);
				//double photoperiod = Double.parseDouble(photoperiodStr);

				// Get edibleBiomass
				String edibleBiomassStr = crop.getAttributeValue(EDIBLE_BIOMASS);
				double edibleBiomass = Double.parseDouble(edibleBiomassStr);

				// Get edible biomass water content [ from 0 to 1 ]
				String edibleWaterContentStr = crop.getAttributeValue(EDIBLE_WATER_CONTENT);
				double edibleWaterContent = Double.parseDouble(edibleWaterContentStr);

				// Get inedibleBiomass
				String inedibleBiomassStr = crop.getAttributeValue(INEDIBLE_BIOMASS);
				double inedibleBiomass = Double.parseDouble(inedibleBiomassStr);

				// 2015-04-08 Added daily PAR
				String dailyPARStr = crop.getAttributeValue(DAILY_PAR);
				double dailyPAR = Double.parseDouble(dailyPARStr);

				// Get harvestIndex
				//String harvestIndexStr = crop.getAttributeValue(HARVEST_INDEX);
				//double harvestIndex = Double.parseDouble(harvestIndexStr);

				// Create crop type.
				//CropType cropType = new CropType(name, growingTime * 1000D, cropCategory, ppf * 1D, photoperiod * 1D, harvestIndex);

				CropType cropType = new CropType(name, growingTime * 1000D, cropCategory,
						edibleBiomass , edibleWaterContent, inedibleBiomass, dailyPAR);

				cropList.add(cropType);
			}
		}

		return cropList;
	}

	/**
	 * Gets the carbon doxide consumption rate.
	 * @return carbon doxide rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	// 2015-12-04 Added getCarbonDioxideConsumptionRate()
	public double getCarbonDioxideConsumptionRate() {
		return getValueAsDouble(CARBON_DIOXIDE_CONSUMPTION_RATE);
	}

	/**
	 * Gets the oxygen consumption rate.
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	// 2015-12-04 Added getOxygenConsumptionRate()
	public double getOxygenConsumptionRate() {
		return getValueAsDouble(OXYGEN_CONSUMPTION_RATE);
	}

	/**
	 * Gets the water consumption rate.
	 * @return water rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	// 2015-12-04 Added getWaterConsumptionRate()
	public double getWaterConsumptionRate() {
		return getValueAsDouble(WATER_CONSUMPTION_RATE);
	}
	
	/*
	 * Gets the value of an element as a double
	 * @param an element
	 * @return a double 
	 */
	// 2015-12-04 Added getValueAsDouble()
	private double getValueAsDouble(String child) {
		Element root = cropDoc.getRootElement();
		Element element = root.getChild(child);
		String str = element.getAttributeValue(VALUE);
		return Double.parseDouble(str);
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
