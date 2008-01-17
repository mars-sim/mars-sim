/**
 * Mars Simulation Project
 * ManufactureConfig.java
 * @version 2.83 2008-01-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.manufacture;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ManufactureConfig implements Serializable {
	
	// Element names
	private static final String PROCESS = "process";
	private static final String NAME = "name";
	private static final String TECH = "tech";
	private static final String SKILL = "skill";
	private static final String WORK_TIME = "work-time";
	private static final String PROCESS_TIME = "process-time";
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
	
	List<ManufactureProcessInfo> getManufactureProcessList() 
			throws Exception {
		
		if (manufactureProcessList == null) {
			
			Element root = manufactureDoc.getDocumentElement();
			NodeList processNodes = root.getElementsByTagName(PROCESS);
			manufactureProcessList = new ArrayList<ManufactureProcessInfo>(
					processNodes.getLength());
			
			for (int x=0; x < processNodes.getLength(); x++) {
				
				ManufactureProcessInfo process = new ManufactureProcessInfo();
				String name = "";
				
				try {
					Element processElement = (Element) processNodes.item(x);
					
					name = processElement.getAttribute(NAME);
					process.setName(name);
					
					process.setTechLevelRequired(Integer.parseInt(
							processElement.getAttribute(TECH)));
					
					process.setSkillLevelRequired(Integer.parseInt(
							processElement.getAttribute(SKILL)));
					
					process.setWorkTimeRequired(Double.parseDouble(
							processElement.getAttribute(WORK_TIME)));
					
					process.setProcessTimeRequired(Double.parseDouble(
							processElement.getAttribute(PROCESS_TIME)));
					
					Element inputs = (Element) processElement.
							getElementsByTagName(INPUTS).item(0);
					List<ManufactureProcessItem> inputList = 
							new ArrayList<ManufactureProcessItem>();
					process.setInputList(inputList);
					
					parseResources(inputList, 
							inputs.getElementsByTagName(RESOURCE));
					
					parseParts(inputList, 
							inputs.getElementsByTagName(PART));
					
					parseEquipment(inputList, 
							inputs.getElementsByTagName(EQUIPMENT));
					
					parseVehicles(inputList, 
							inputs.getElementsByTagName(VEHICLE));
					
					Element outputs = (Element) processElement.
							getElementsByTagName(OUTPUTS).item(0);
					List<ManufactureProcessItem> outputList = 
							new ArrayList<ManufactureProcessItem>();
					process.setOutputList(outputList);
			
					parseResources(outputList, 
							outputs.getElementsByTagName(RESOURCE));
			
					parseParts(outputList, 
							outputs.getElementsByTagName(PART));
			
					parseEquipment(outputList, 
							outputs.getElementsByTagName(EQUIPMENT));
			
					parseVehicles(outputList, 
							outputs.getElementsByTagName(VEHICLE));
				}
				catch (Exception e) {
					throw new Exception("Error reading manufacturing process "
							+ name + ": " + e.getMessage());
				}
			}
		}
		
		return manufactureProcessList;
	}
	
	private void parseResources(List<ManufactureProcessItem> list, 
			NodeList resourceNodes) throws Exception {
		for (int y = 0; y < resourceNodes.getLength(); y++) {
			Element resourceElement = (Element) resourceNodes.item(y);
			ManufactureProcessItem resourceItem = new ManufactureProcessItem();
			resourceItem.setType(ManufactureProcessItem.AMOUNT_RESOURCE);
			resourceItem.setName(resourceElement.getAttribute(NAME));
			resourceItem.setAmount(Double.parseDouble(
					resourceElement.getAttribute(AMOUNT)));
			list.add(resourceItem);
		}
	}
	
	private void parseParts(List<ManufactureProcessItem> list, 
			NodeList partNodes) throws Exception {
		for (int y = 0; y < partNodes.getLength(); y++) {
			Element partElement = (Element) partNodes.item(y);
			ManufactureProcessItem partItem = new ManufactureProcessItem();
			partItem.setType(ManufactureProcessItem.PART);
			partItem.setName(partElement.getAttribute(NAME));
			partItem.setAmount(Integer.parseInt(
					partElement.getAttribute(NUMBER)));
			list.add(partItem);
		}
	}
	
	private void parseEquipment(List<ManufactureProcessItem> list, 
			NodeList equipmentNodes) throws Exception {
		for (int y = 0; y < equipmentNodes.getLength(); y++) {
			Element equipmentElement = (Element) equipmentNodes.item(y);
			ManufactureProcessItem equipmentItem = 
				new ManufactureProcessItem();
			equipmentItem.setType(ManufactureProcessItem.EQUIPMENT);
			equipmentItem.setName(equipmentElement.getAttribute(NAME));
			equipmentItem.setAmount(Integer.parseInt(
					equipmentElement.getAttribute(NUMBER)));
			list.add(equipmentItem);
		}
	}
	
	private void parseVehicles(List<ManufactureProcessItem> list, 
			NodeList vehicleNodes) throws Exception {
		for (int y = 0; y < vehicleNodes.getLength(); y++) {
			Element vehicleElement = (Element) vehicleNodes.item(y);
			ManufactureProcessItem vehicleItem = new ManufactureProcessItem();
			vehicleItem.setType(ManufactureProcessItem.VEHICLE);
			vehicleItem.setName(vehicleElement.getAttribute(NAME));
			vehicleItem.setAmount(Integer.parseInt(vehicleElement.getAttribute(NUMBER)));
			list.add(vehicleItem);
		}
	}
}