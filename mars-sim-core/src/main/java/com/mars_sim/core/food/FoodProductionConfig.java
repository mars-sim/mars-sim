/*
 * Mars Simulation Project
 * FoodProductionConfig.java
 * @date 2023-08-17
 * @author Manny Kung
 */

package com.mars_sim.core.food;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.ResourceUtil;

public class FoodProductionConfig {

	private static final Logger logger = Logger.getLogger(FoodProductionConfig.class.getName());
	
	// Element names
	private static final int MAX_NUM_INPUT_RESOURCES = 5;
	
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
	
	private List<FoodProductionProcessInfo> processList;

    /**
     * Constructor.
     * 
     * @param foodProductionDoc DOM document containing foodProduction process configuration.
     */
    public FoodProductionConfig(Document foodProductionDoc) {
    	buildFoodProductionProcessList(foodProductionDoc);
    }

    /**
     * Gets a list of manufacturing process information.
     * @return list of manufacturing process information.
     * @throws Exception if error getting info.
     */
    public List<FoodProductionProcessInfo> getFoodProductionProcessList() {
        return processList;
    }

    private synchronized void buildFoodProductionProcessList(Document foodProductionDoc) {
    	if (processList != null) {
    		// List has been built by a different thread !!!
    		return;
    	}
    	
		// Build the global list in a temp to avoid access before it is built
        List<FoodProductionProcessInfo> newList = new ArrayList<>();
	
        Element root = foodProductionDoc.getRootElement();
        List<Element> processNodes = root.getChildren(PROCESS);

        for (Element processElement : processNodes) {

			// Create a map that stores the resource to be swapped out with an alternate resource
			Map<String, String> alternateResourceMap = new HashMap<>();

            FoodProductionProcessInfo process = new FoodProductionProcessInfo();
         
            String name = "";
            String description = "";

            name = processElement.getAttributeValue(NAME);
            
            process.setName(name);
            process.setTechLevelRequired(Integer.parseInt(processElement
                    .getAttributeValue(TECH)));
            process.setSkillLevelRequired(Integer.parseInt(processElement
                    .getAttributeValue(SKILL)));
            process.setWorkTimeRequired(Double.parseDouble(processElement
                    .getAttributeValue(WORK_TIME)));
            process.setProcessTimeRequired(Double
                    .parseDouble(processElement
                            .getAttributeValue(PROCESS_TIME)));
            process.setPowerRequired(Double.parseDouble(processElement
                    .getAttributeValue(POWER_REQUIRED)));
            
            Element descriptElem = processElement.getChild(DESCRIPTION);
            if (descriptElem != null) {
            	description = descriptElem.getText();
            }
            process.setDescription(description);

            Element inputs = processElement.getChild(INPUTS);
            List<FoodProductionProcessItem> inputList = new ArrayList<>();
            process.setInputList(inputList);

    		parseInputResources(inputList, inputs.getChildren(RESOURCE), alternateResourceMap);	
            parseParts(inputList, inputs.getChildren(PART));
            parseEquipment(inputList, inputs.getChildren(EQUIPMENT));

            Element outputs = processElement.getChild(OUTPUTS);
    
            List<FoodProductionProcessItem> outputList = new ArrayList<>();
            
            process.setOutputList(outputList);

            parseOutputResources(outputList, outputs.getChildren(RESOURCE));
            parseParts(outputList, outputs.getChildren(PART));
            parseEquipment(outputList, outputs.getChildren(EQUIPMENT));               
       
            // Add process to newList.
 			newList.add(process);
 		
 			if (!alternateResourceMap.isEmpty()) {
 				// Create a list for the original resources from alternateResourceMap
 				List<String> originalResourceList = new ArrayList<>(alternateResourceMap.values());
 				// Create a list for the alternate resources from alternateResourceMap
 				List<String> altResourceList = new ArrayList<>(alternateResourceMap.keySet());
 				
 				String processName = process.getName();
 		
//         				System.out.println("processName: " + processName);
 				
//         				System.out.println("alternateResourceMap: " + alternateResourceMap);
 				
 				int size = altResourceList.size();
 				
 				for (int i = 0; i < size; i++) {
 					
 					// Use copy constructor to create a new instance
 					FoodProductionProcessInfo altProcess = new FoodProductionProcessInfo(process);

 					String altProcessName = processName + " " + (i + 1);
 							
 					String originalResource = originalResourceList.get(i);
 					String altResource = altResourceList.get(i);

 					// Rename the original process name by appending it with a numeral
 					altProcess.setName(altProcessName);

 					// Create a brand new list
 					List<FoodProductionProcessItem> newInputItems = new ArrayList<>();
 					
 					for (FoodProductionProcessItem item: inputList) {
 						
 						String resName = item.getName();								
 						double amount = item.getAmount();				
 						ItemType type = item.getType();
 					
 						FoodProductionProcessItem newItem = new FoodProductionProcessItem();
 						
 						if (resName.equalsIgnoreCase(originalResource)) {
 							AmountResource resource = ResourceUtil.findAmountResource(resName);
 							if (resource != null) {
 								// Replace with the alternate resource name
 								newItem.setName(altResource);
 							}
 							else {
//         						System.out.println("resName: " + resName + "  originalResource: " + originalResource + "  inputList: " + inputList);
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
		processList = Collections.unmodifiableList(newList);
    }
    
	/**
	 * Parses the input amount resource elements in a node list.
	 * 
	 * @param list          the list to store the resources in.
	 * @param resourceNodes the node list.
	 * @param alternateResourceMap the map that stores the resource to be swapped out with an alternate resource
	 * @throws Exception if error parsing resources.
	 */
	private static void parseInputResources(List<FoodProductionProcessItem> list, List<Element> resourceNodes, 
			Map<String, String> alternateResourceMap) {
		
		for (Element resourceElement : resourceNodes) {
			
			FoodProductionProcessItem resourceItem = new FoodProductionProcessItem();
			resourceItem.setType(ItemType.AMOUNT_RESOURCE);
			String originalResourceName = "";
			
			for (int i = 0; i < MAX_NUM_INPUT_RESOURCES; i++) {
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
						logger.severe(resourceXMLName + " shows up in foodProduction.xml but doesn't exist in resources.xml.");
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
	private static void parseOutputResources(List<FoodProductionProcessItem> list, List<Element> resourceNodes) {

		for (Element resourceElement : resourceNodes) {
			
			FoodProductionProcessItem resourceItem = new FoodProductionProcessItem();
			resourceItem.setType(ItemType.AMOUNT_RESOURCE);
			
			String resourceName = resourceElement.getAttributeValue(NAME);
			
			if (resourceName != null) {
				AmountResource resource = ResourceUtil.findAmountResource(resourceName);
				if (resource == null)
					logger.severe(resourceName + " shows up in foodProduction.xml but doesn't exist in resources.xml.");
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
     * @param list the list to store the parts in.
     * @param partNodes the node list.
     * @throws Exception if error parsing parts.
     */
    private void parseParts(List<FoodProductionProcessItem> list,
            List<Element> partNodes) {
        for (Element partElement : partNodes) {
            FoodProductionProcessItem partItem = new FoodProductionProcessItem();
            partItem.setType(ItemType.PART);
            partItem.setName(partElement.getAttributeValue(NAME));
            partItem.setAmount(Integer.parseInt(partElement
                    .getAttributeValue(NUMBER)));
            list.add(partItem);
        }
    }

    /**
     * Parses the equipment elements in a node list.
     * @param list the list to store the equipment in.
     * @param equipmentNodes the node list.
     * @throws Exception if error parsing equipment.
     */
    private void parseEquipment(List<FoodProductionProcessItem> list,
            List<Element> equipmentNodes) {
        for (Element equipmentElement : equipmentNodes) {
            FoodProductionProcessItem equipmentItem = new FoodProductionProcessItem();
            equipmentItem.setType(ItemType.EQUIPMENT);
            equipmentItem.setName(equipmentElement.getAttributeValue(NAME));
            equipmentItem.setAmount(Integer.parseInt(equipmentElement
                    .getAttributeValue(NUMBER)));
            list.add(equipmentItem);
        }
    }

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
//        foodProductionDoc = null;

        if(processList != null){

            Iterator<FoodProductionProcessInfo> i = processList.iterator();
            while (i.hasNext()) {
                i.next().destroy();
            }
            processList.clear();
            processList = null;
        }
    }
}
