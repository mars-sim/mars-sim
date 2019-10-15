/**
 * Mars Simulation Project
 * ManufactureConfig.java
 * @version 3.1.0 2017-09-11
 * @author Scott Davis
 */

package org.mars_sim.msp.core.manufacture;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;

public class ManufactureConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(ManufactureConfig.class.getName());

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

	private static Document manufactureDoc;
	private static List<ManufactureProcessInfo> manufactureProcessList;
	private static List<SalvageProcessInfo> salvageList;

	/**
	 * Constructor
	 * 
	 * @param manufactureDoc DOM document containing manufacture process
	 *                       configuration.
	 */
	public ManufactureConfig(Document manufactureDoc) {
		this.manufactureDoc = manufactureDoc;
	}

	/**
	 * Gets a list of manufacturing process information.
	 * 
	 * @return list of manufacturing process information.
	 * @throws Exception if error getting info.
	 */
	public static List<ManufactureProcessInfo> getManufactureProcessList() {

		if (manufactureProcessList == null) {

			Element root = manufactureDoc.getRootElement();
			List<Element> processNodes = root.getChildren(PROCESS);
			manufactureProcessList = new ArrayList<ManufactureProcessInfo>(processNodes.size());

			for (Element processElement : processNodes) {

				ManufactureProcessInfo process = new ManufactureProcessInfo();
				manufactureProcessList.add(process);
				String name = "";
				String description = "";

				name = processElement.getAttributeValue(NAME).toLowerCase();
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
				List<ManufactureProcessItem> inputList = new ArrayList<ManufactureProcessItem>();
				process.setInputList(inputList);

				parseResources(inputList, inputs.getChildren(RESOURCE));

				parseParts(inputList, inputs.getChildren(PART));

				parseEquipment(inputList, inputs.getChildren(EQUIPMENT));

				parseVehicles(inputList, inputs.getChildren(VEHICLE));

				Element outputs = processElement.getChild(OUTPUTS);
				List<ManufactureProcessItem> outputList = new ArrayList<ManufactureProcessItem>();
				process.setOutputList(outputList);

				parseResources(outputList, outputs.getChildren(RESOURCE));

				parseParts(outputList, outputs.getChildren(PART));

				parseEquipment(outputList, outputs.getChildren(EQUIPMENT));

				parseVehicles(outputList, outputs.getChildren(VEHICLE));
			}
		}

		return manufactureProcessList;
	}

	/**
	 * Parses the amount resource elements in a node list.
	 * 
	 * @param list          the list to store the resources in.
	 * @param resourceNodes the node list.
	 * @throws Exception if error parsing resources.
	 */
	private static void parseResources(List<ManufactureProcessItem> list, List<Element> resourceNodes) {
		for (Element resourceElement : resourceNodes) {
			ManufactureProcessItem resourceItem = new ManufactureProcessItem();
			resourceItem.setType(ItemType.AMOUNT_RESOURCE);
			String resourceName = resourceElement.getAttributeValue(NAME).toLowerCase();
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

			Set<String> names = EquipmentType.getNameSet();// EquipmentFactory.getEquipmentNames();
			boolean result = false;
			for (String s : names) {
				if (s.equalsIgnoreCase(equipmentName)) {
					result = true;
				}
			}

			if (result) {
				equipmentItem.setName(equipmentName);
				equipmentItem.setAmount(Integer.parseInt(equipmentElement.getAttributeValue(NUMBER)));
				list.add(equipmentItem);
			} else
				logger.severe("The equipment '" + equipmentName + "' shows up in manufacturing.xml but doesn't "
						+ "exist in EquipmentType.");
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
	List<SalvageProcessInfo> getSalvageList() {

		if (salvageList == null) {

			Element root = manufactureDoc.getRootElement();
			List<Element> salvageNodes = root.getChildren(SALVAGE);
			salvageList = new ArrayList<SalvageProcessInfo>(salvageNodes.size());
			Iterator<Element> i = salvageNodes.iterator();
			while (i.hasNext()) {
				Element salvageElement = i.next();
				SalvageProcessInfo salvage = new SalvageProcessInfo();
				salvageList.add(salvage);
				String itemName = "";

				itemName = salvageElement.getAttributeValue(ITEM_NAME);
				salvage.setItemName(itemName);

				salvage.setType(salvageElement.getAttributeValue(TYPE));

				salvage.setTechLevelRequired(Integer.parseInt(salvageElement.getAttributeValue(TECH)));

				salvage.setSkillLevelRequired(Integer.parseInt(salvageElement.getAttributeValue(SKILL)));

				salvage.setWorkTimeRequired(Double.parseDouble(salvageElement.getAttributeValue(WORK_TIME)));

				List<Element> partSalvageNodes = salvageElement.getChildren(PART_SALVAGE);
				List<PartSalvage> partSalvageList = new ArrayList<PartSalvage>(partSalvageNodes.size());
				salvage.setPartSalvageList(partSalvageList);

				Iterator<Element> j = partSalvageNodes.iterator();
				while (j.hasNext()) {
					Element partSalvageElement = j.next();
					PartSalvage part = new PartSalvage();
					partSalvageList.add(part);

					part.setName(partSalvageElement.getAttributeValue(NAME));

					part.setNumber(Integer.parseInt(partSalvageElement.getAttributeValue(NUMBER)));
				}
			}
		}

		return salvageList;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		manufactureDoc = null;

		if (manufactureProcessList != null) {

			Iterator<ManufactureProcessInfo> i = manufactureProcessList.iterator();
			while (i.hasNext()) {
				i.next().destroy();
			}
			manufactureProcessList.clear();
			manufactureProcessList = null;
		}

		if (salvageList != null) {

			Iterator<SalvageProcessInfo> j = salvageList.iterator();
			while (j.hasNext()) {
				j.next().destroy();
			}
			salvageList.clear();
			salvageList = null;
		}
	}
}