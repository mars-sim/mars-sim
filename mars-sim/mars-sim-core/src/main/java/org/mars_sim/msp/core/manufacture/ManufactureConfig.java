/**
 * Mars Simulation Project
 * ManufactureConfig.java
 * @version 2.85 2008-11-28
 * @author Scott Davis
 */

package org.mars_sim.msp.core.manufacture;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;


public class ManufactureConfig implements Serializable {
	
	// Element names
	private static final String PROCESS = "process";
	private static final String NAME = "name";
	private static final String TECH = "tech";
	private static final String SKILL = "skill";
	private static final String WORK_TIME = "work-time";
	private static final String PROCESS_TIME = "process-time";
    private static final String POWER_REQUIRED = "power-required";
	private static final String INPUTS = "inputs";
	private static final String OUTPUTS = "outputs";
	private static final String RESOURCE = "resource";
	private static final String AMOUNT = "amount";
	private static final String PART = "part";
	private static final String NUMBER = "number";
	private static final String EQUIPMENT = "equipment";
	private static final String VEHICLE = "vehicle";
	
	private Document manufactureDoc;
	private List<ManufactureProcessInfo> manufactureProcessList;
	
	/**
	 * Constructor
	 * @param manufactureDoc DOM document containing manufacture process 
	 * configuration.
	 */
	public ManufactureConfig(Document manufactureDoc) {
		this.manufactureDoc = manufactureDoc;
	}
	
	/**
	 * Gets a list of manufacturing process information.
	 * @return list of manufacturing process information.
	 * @throws Exception if error getting info.
	 */
    @SuppressWarnings("unchecked")
	List<ManufactureProcessInfo> getManufactureProcessList() 
			throws Exception {
		
		if (manufactureProcessList == null) {
			
			Element root = manufactureDoc.getRootElement();
			List<Element> processNodes = root.getChildren(PROCESS);
			manufactureProcessList = new ArrayList<ManufactureProcessInfo>(
					processNodes.size());
			
			for (Element processElement : processNodes) {
				
				ManufactureProcessInfo process = new ManufactureProcessInfo();
				manufactureProcessList.add(process);
				String name = "";
				
				try {
					
					name = processElement.getAttributeValue(NAME);
					process.setName(name);
					
					process.setTechLevelRequired(Integer.parseInt(
							processElement.getAttributeValue(TECH)));
					
					process.setSkillLevelRequired(Integer.parseInt(
							processElement.getAttributeValue(SKILL)));
					
					process.setWorkTimeRequired(Double.parseDouble(
							processElement.getAttributeValue(WORK_TIME)));
					
					process.setProcessTimeRequired(Double.parseDouble(
							processElement.getAttributeValue(PROCESS_TIME)));
                    
                    process.setPowerRequired(Double.parseDouble(
                            processElement.getAttributeValue(POWER_REQUIRED)));
					
					Element inputs = processElement.getChild(INPUTS);
					List<ManufactureProcessItem> inputList = 
							new ArrayList<ManufactureProcessItem>();
					process.setInputList(inputList);
					
					parseResources(inputList, 
							inputs.getChildren(RESOURCE));
					
					parseParts(inputList, 
							inputs.getChildren(PART));
					
					parseEquipment(inputList, 
							inputs.getChildren(EQUIPMENT));
					
					parseVehicles(inputList, 
							inputs.getChildren(VEHICLE));
					
					Element outputs = processElement.getChild(OUTPUTS);
					List<ManufactureProcessItem> outputList = 
							new ArrayList<ManufactureProcessItem>();
					process.setOutputList(outputList);
			
					parseResources(outputList, 
							outputs.getChildren(RESOURCE));
			
					parseParts(outputList, 
							outputs.getChildren(PART));
			
					parseEquipment(outputList, 
							outputs.getChildren(EQUIPMENT));
			
					parseVehicles(outputList, 
							outputs.getChildren(VEHICLE));
				}
				catch (Exception e) {
					throw new Exception("Error reading manufacturing process "
							+ name + ": " + e.getMessage());
				}
			}
		}
		
		return manufactureProcessList;
	}
	
	/**
	 * Parses the amount resource elements in a node list.
	 * @param list the list to store the resources in.
	 * @param resourceNodes the node list.
	 * @throws Exception if error parsing resources.
	 */
	private void parseResources(List<ManufactureProcessItem> list, 
			List<Element> resourceNodes) throws Exception {
		for (Element resourceElement : resourceNodes) {
			ManufactureProcessItem resourceItem = new ManufactureProcessItem();
			resourceItem.setType(ManufactureProcessItem.AMOUNT_RESOURCE);
			resourceItem.setName(resourceElement.getAttributeValue(NAME));
			resourceItem.setAmount(Double.parseDouble(
					resourceElement.getAttributeValue(AMOUNT)));
			list.add(resourceItem);
		}
	}
	
	/**
	 * Parses the part elements in a node list.
	 * @param list the list to store the parts in.
	 * @param partNodes the node list.
	 * @throws Exception if error parsing parts.
	 */
	private void parseParts(List<ManufactureProcessItem> list, 
			List<Element> partNodes) throws Exception {
		for (Element partElement : partNodes) {
			ManufactureProcessItem partItem = new ManufactureProcessItem();
			partItem.setType(ManufactureProcessItem.PART);
			partItem.setName(partElement.getAttributeValue(NAME));
			partItem.setAmount(Integer.parseInt(
					partElement.getAttributeValue(NUMBER)));
			list.add(partItem);
		}
	}
	
	/**
	 * Parses the equipment elements in a node list.
	 * @param list the list to store the equipment in.
	 * @param equipmentNodes the node list.
	 * @throws Exception if error parsing equipment.
	 */
	private void parseEquipment(List<ManufactureProcessItem> list, 
			List<Element> equipmentNodes) throws Exception {
		for (Element equipmentElement : equipmentNodes) {
			ManufactureProcessItem equipmentItem = 
				new ManufactureProcessItem();
			equipmentItem.setType(ManufactureProcessItem.EQUIPMENT);
			equipmentItem.setName(equipmentElement.getAttributeValue(NAME));
			equipmentItem.setAmount(Integer.parseInt(
					equipmentElement.getAttributeValue(NUMBER)));
			list.add(equipmentItem);
		}
	}
	
	/**
	 * Parses the vehicle elements in a node list.
	 * @param list the list to store the vehicles in.
	 * @param vehicleNodes the node list.
	 * @throws Exception if error parsing vehicles.
	 */
	private void parseVehicles(List<ManufactureProcessItem> list, 
			List<Element> vehicleNodes) throws Exception {
		for (Element vehicleElement : vehicleNodes) {
			ManufactureProcessItem vehicleItem = new ManufactureProcessItem();
			vehicleItem.setType(ManufactureProcessItem.VEHICLE);
			vehicleItem.setName(vehicleElement.getAttributeValue(NAME));
			vehicleItem.setAmount(Integer.parseInt(vehicleElement.getAttributeValue(NUMBER)));
			list.add(vehicleItem);
		}
	}
}