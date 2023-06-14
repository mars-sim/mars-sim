/*
 * Mars Simulation Project
 * ManufactureConfig.java
 * @date 2023-06-12
 * @author Scott Davis
 */

package org.mars_sim.msp.core.manufacture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.configuration.ConfigHelper;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;

public class ManufactureConfig {

	private static final Logger logger = Logger.getLogger(ManufactureConfig.class.getName());

	// Element names
	private static final String PROCESS = "process";
	private static final String NAME = "name";	
	private static final String TECH = "tech";
	private static final String SKILL = "skill";
	private static final String WORK_TIME = "work-time";
	private static final String PROCESS_TIME = "process-time";
	private static final String POWER_REQUIRED = "power-required";
	private static final String DESCRIPTION = "description";
	private static final String INPUTS = "inputs";
	private static final String OUTPUTS = "outputs";
	private static final String RESOURCE = "resource";
	private static final String AMOUNT = "amount";
	private static final String PART = "part";
	private static final String NUMBER = "number";
	private static final String EQUIPMENT = "equipment";
	private static final String VEHICLE = "vehicle";
	private static final String SALVAGE = "salvage";
	private static final String ITEM_NAME = "item-name";
	private static final String TYPE = "type";
	private static final String PART_SALVAGE = "part-salvage";

	private static List<ManufactureProcessInfo> manufactureProcessList;
	private static List<SalvageProcessInfo> salvageList;

	/**
	 * Constructor
	 * 
	 * @param manufactureDoc DOM document containing manufacture process
	 *                       configuration.
	 */
	public ManufactureConfig(Document manufactureDoc) {
		loadManufactureProcessList(manufactureDoc);
		loadSalvageList(manufactureDoc);
	}

	/**
	 * Gets a list of manufacturing process information.
	 * 
	 * @return list of manufacturing process information.
	 * @throws Exception if error getting info.
	 */
	public List<ManufactureProcessInfo> getManufactureProcessList() {
		return manufactureProcessList;
	}
	
	/**
	 * Gets a list of manufacturing process information.
	 * 
	 * @return list of manufacturing process information.
	 * @throws Exception if error getting info.
	 */
	private synchronized void loadManufactureProcessList(Document manufactureDoc) {
		if (manufactureProcessList != null) {
			// just in case if another thread is being created
			return;
		}
		
		// Build the global list in a temp to avoid access before it is built
		List<ManufactureProcessInfo> newList = new ArrayList<>();
		
		Element root = manufactureDoc.getRootElement();
		List<Element> processNodes = root.getChildren(PROCESS);

		for (Element processElement : processNodes) {

			// Create a map that stores the resource to be swapped out with an alternate resource
			Map<String, String> alternateResourceMap = new HashMap<>();
			
			ManufactureProcessInfo process = new ManufactureProcessInfo();

			String name = "";
			String description = "";

			name = processElement.getAttributeValue(NAME);
			
			process.setName(name);
			process.setTechLevelRequired(Integer.parseInt(processElement.getAttributeValue(TECH)));
			process.setSkillLevelRequired(Integer.parseInt(processElement.getAttributeValue(SKILL)));
			process.setWorkTimeRequired(Double.parseDouble(processElement.getAttributeValue(WORK_TIME)));
			process.setProcessTimeRequired(Double.parseDouble(processElement.getAttributeValue(PROCESS_TIME)));
			process.setPowerRequired(Double.parseDouble(processElement.getAttributeValue(POWER_REQUIRED)));

			Element descriptElem = processElement.getChild(DESCRIPTION);
			if (descriptElem != null) {
				description = descriptElem.getText();
			}
			process.setDescription(description);

			Element inputs = processElement.getChild(INPUTS);
			
			List<ManufactureProcessItem> inputList = new ArrayList<>();
			
			process.setInputList(inputList);

			parseInputResources(inputList, inputs.getChildren(RESOURCE), alternateResourceMap);
			parseParts(inputList, inputs.getChildren(PART));
			parseEquipment(inputList, inputs.getChildren(EQUIPMENT));
			parseVehicles(inputList, inputs.getChildren(VEHICLE));

			Element outputs = processElement.getChild(OUTPUTS);
			
			List<ManufactureProcessItem> outputList = new ArrayList<>();
			
			process.setOutputList(outputList);

			parseOutputResources(outputList, outputs.getChildren(RESOURCE));
			parseParts(outputList, outputs.getChildren(PART));
			parseEquipment(outputList, outputs.getChildren(EQUIPMENT));
			parseVehicles(outputList, outputs.getChildren(VEHICLE));

			// Add process to newList.
			newList.add(process);
		
			if (!alternateResourceMap.isEmpty()) {
				// Create a list for the original resources from alternateResourceMap
				List<String> originalResourceList = new ArrayList<>(alternateResourceMap.values());
				// Create a list for the alternate resources from alternateResourceMap
				List<String> altResourceList = new ArrayList<>(alternateResourceMap.keySet());
				
				String processName = process.getName();
		
//				System.out.println("processName: " + processName);
				
//				System.out.println("alternateResourceMap: " + alternateResourceMap);
				
				int size = altResourceList.size();
				
				for (int i = 0; i < size; i++) {
					
					// Use copy constructor to create a new instance
					ManufactureProcessInfo altProcess = new ManufactureProcessInfo(process);

					String altProcessName = processName + " " + (i + 1);
							
					String originalResource = originalResourceList.get(i);
					String altResource = altResourceList.get(i);

					// Rename the original process name by appending it with a numeral
					altProcess.setName(altProcessName);

					// Create a brand new list
					List<ManufactureProcessItem> newInputItems = new ArrayList<>();
					
					for (ManufactureProcessItem item: inputList) {
						
						String resName = item.getName();								
						double amount = item.getAmount();				
						ItemType type = item.getType();
					
						ManufactureProcessItem newItem = new ManufactureProcessItem();
						
						if (resName.equalsIgnoreCase(originalResource)) {
							AmountResource resource = ResourceUtil.findAmountResource(resName);
							if (resource != null) {
								// Replace with the alternate resource name
								newItem.setName(altResource);
							}
							else {
//								System.out.println("resName: " + resName + "  originalResource: " + originalResource + "  inputList: " + inputList);
								newItem.setName(resName);
							}
						}
						else {
							newItem.setName(resName);
						}
						
						newItem.setAmount(amount);						
						newItem.setType(type);
		
						newInputItems.add(newItem);					
					}
					
					// Write the modified input resource list onto the new list
					altProcess.setInputList(newInputItems);
					
					// Add process to newList.
					newList.add(altProcess);
				}
			}
		}
		
		// Assign the newList now built
		manufactureProcessList = Collections.unmodifiableList(newList);
	}

	/**
	 * Parses the input amount resource elements in a node list.
	 * 
	 * @param list          the list to store the resources in.
	 * @param resourceNodes the node list.
	 * @param alternateResourceMap the map that stores the resource to be swapped out with an alternate resource
	 * @throws Exception if error parsing resources.
	 */
	private static void parseInputResources(List<ManufactureProcessItem> list, List<Element> resourceNodes, 
			Map<String, String> alternateResourceMap) {
		
		for (Element resourceElement : resourceNodes) {
			
			ManufactureProcessItem resourceItem = new ManufactureProcessItem();
			resourceItem.setType(ItemType.AMOUNT_RESOURCE);
			String originalResourceName = "";
			
			for (int i = 0; i < 4; i++) {
				String num = "";
				if (i == 0)
					num = "";
				else 
					num = i + "";
				String resourceXMLName = resourceElement.getAttributeValue(NAME + num);
				
				if (resourceXMLName != null) {
				
					if (i == 0) {
						originalResourceName = resourceXMLName;
					}
				
					// Checks if resourceName exists at all
					AmountResource resource = ResourceUtil.findAmountResource(resourceXMLName);
					if (resource == null)
						logger.severe(resourceXMLName + " shows up in manufacturing.xml but doesn't exist in resources.xml.");
					else {
						if (i == 0) {
							resourceItem.setName(resourceXMLName);						
							resourceItem.setAmount(Double.parseDouble(resourceElement.getAttributeValue(AMOUNT)));
							list.add(resourceItem);
						}
						else {
							alternateResourceMap.put(resourceXMLName, originalResourceName);
//							System.out.println(alternateResourceMap.size() + "  originalResourceName: " + originalResourceName
//									+ "  ->  resourceXMLName: " + resourceXMLName);
						}
					}
				}
			}
		}
	}

	/**
	 * Parses the output amount resource elements in a node list.
	 * 
	 * @param list          the list to store the resources in.
	 * @param resourceNodes the node list.
	 * @throws Exception if error parsing resources.
	 */
	private static void parseOutputResources(List<ManufactureProcessItem> list, List<Element> resourceNodes) {

		for (Element resourceElement : resourceNodes) {
			
			ManufactureProcessItem resourceItem = new ManufactureProcessItem();
			resourceItem.setType(ItemType.AMOUNT_RESOURCE);
			
			String resourceName = resourceElement.getAttributeValue(NAME);
			
			if (resourceName != null) {
				AmountResource resource = ResourceUtil.findAmountResource(resourceName);
				if (resource == null)
					logger.severe(resourceName + " shows up in manufacturing.xml but doesn't exist in resources.xml.");
				else {		
					resourceItem.setName(resourceName);
					resourceItem.setAmount(Double.parseDouble(resourceElement.getAttributeValue(AMOUNT)));
					list.add(resourceItem);
				}
			}
		}
	}
	
	/**
	 * Parses the part elements in a node list.
	 * 
	 * @param list      the list to store the parts in.
	 * @param partNodes the node list.
	 * @throws Exception if error parsing parts.
	 */
	private static void parseParts(List<ManufactureProcessItem> list, List<Element> partNodes) {
		for (Element partElement : partNodes) {
			ManufactureProcessItem partItem = new ManufactureProcessItem();
			partItem.setType(ItemType.PART);

			String partName = partElement.getAttributeValue(NAME);
			Part part = (Part) ItemResourceUtil.findItemResource(partName);

			if (part == null)
				logger.severe(partName + " shows up in manufacturing.xml but doesn't exist in parts.xml.");
			else {
				partItem.setName(partName);
				partItem.setAmount(Integer.parseInt(partElement.getAttributeValue(NUMBER)));
				list.add(partItem);
			}
		}
	}

	/**
	 * Parses the equipment elements in a node list.
	 * 
	 * @param list           the list to store the equipment in.
	 * @param equipmentNodes the node list.
	 * @throws Exception if error parsing equipment.
	 */
	private static void parseEquipment(List<ManufactureProcessItem> list, List<Element> equipmentNodes) {
		for (Element equipmentElement : equipmentNodes) {
			ManufactureProcessItem equipmentItem = new ManufactureProcessItem();
			equipmentItem.setType(ItemType.EQUIPMENT);
			String equipmentName = equipmentElement.getAttributeValue(NAME);

			EquipmentType eType = EquipmentType.valueOf(ConfigHelper.convertToEnumName(equipmentName));
			if (eType != null) {
				equipmentItem.setName(equipmentName);
				equipmentItem.setAmount(Integer.parseInt(equipmentElement.getAttributeValue(NUMBER)));
				list.add(equipmentItem);
			}
		}
	}

	/**
	 * Parses the vehicle elements in a node list.
	 * 
	 * @param list         the list to store the vehicles in.
	 * @param vehicleNodes the node list.
	 * @throws Exception if error parsing vehicles.
	 */
	private static void parseVehicles(List<ManufactureProcessItem> list, List<Element> vehicleNodes) {
		for (Element vehicleElement : vehicleNodes) {
			ManufactureProcessItem vehicleItem = new ManufactureProcessItem();
			vehicleItem.setType(ItemType.VEHICLE);
			vehicleItem.setName(vehicleElement.getAttributeValue(NAME));
			vehicleItem.setAmount(Integer.parseInt(vehicleElement.getAttributeValue(NUMBER)));
			list.add(vehicleItem);
		}
	}

	/**
	 * Gets a list of salvage process information.
	 * 
	 * @return list of salvage process information.
	 * @throws Exception if error getting info.
	 */
	public List<SalvageProcessInfo> getSalvageList() {
		return salvageList;
	}
		
	/**
	 * Gets a list of salvage process information.
	 * 
	 * @return list of salvage process information.
	 * @throws Exception if error getting info.
	 */
	private synchronized void loadSalvageList(Document manufactureDoc) {
		if (salvageList != null) {
			// just in case if another thread is being created
			return;
		}
		
		Element root = manufactureDoc.getRootElement();
		List<Element> salvageNodes = root.getChildren(SALVAGE);
		List<SalvageProcessInfo> newList = new ArrayList<>();
		
		Iterator<Element> i = salvageNodes.iterator();
		while (i.hasNext()) {
			Element salvageElement = i.next();
			SalvageProcessInfo salvage = new SalvageProcessInfo();
			String itemName = "";
			itemName = salvageElement.getAttributeValue(ITEM_NAME);
			
			salvage.setItemName(itemName);
			salvage.setType(salvageElement.getAttributeValue(TYPE));
			salvage.setTechLevelRequired(Integer.parseInt(salvageElement.getAttributeValue(TECH)));
			salvage.setSkillLevelRequired(Integer.parseInt(salvageElement.getAttributeValue(SKILL)));
			salvage.setWorkTimeRequired(Double.parseDouble(salvageElement.getAttributeValue(WORK_TIME)));

			List<Element> partSalvageNodes = salvageElement.getChildren(PART_SALVAGE);
			List<PartSalvage> partSalvageList = new ArrayList<>();
			salvage.setPartSalvageList(partSalvageList);

			Iterator<Element> j = partSalvageNodes.iterator();
			while (j.hasNext()) {
				Element partSalvageElement = j.next();
				PartSalvage part = new PartSalvage();
				
				partSalvageList.add(part);

				part.setName(partSalvageElement.getAttributeValue(NAME));
				part.setNumber(Integer.parseInt(partSalvageElement.getAttributeValue(NUMBER)));
			}

			// Add salvage to newList.
			newList.add(salvage);
		}

		// Assign the newList now built
		salvageList = Collections.unmodifiableList(newList);
	}
}
