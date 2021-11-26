/**
 * Mars Simulation Project
 * FoodProductionConfig.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.foodProduction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.resource.ItemType;

public class FoodProductionConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

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
	
	private List<FoodProductionProcessInfo> foodproductionProcessList;

    /**
     * Constructor
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
        return foodproductionProcessList;
    }

    private synchronized void buildFoodProductionProcessList(Document foodProductionDoc) {
    	if (foodproductionProcessList != null) {
    		// List has been built by a different thread !!!
    		return;
    	}
    	
		// Build the global list in a temp to avoid access before it is built
        List<FoodProductionProcessInfo> newList = new ArrayList<>();
	
        Element root = foodProductionDoc.getRootElement();
        List<Element> processNodes = root.getChildren(PROCESS);

        for (Element processElement : processNodes) {

            FoodProductionProcessInfo process = new FoodProductionProcessInfo();
            newList.add(process);
            String name = "";
            String description = "";

            name = processElement.getAttributeValue(NAME).toLowerCase();
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

            parseResources(inputList, inputs.getChildren(RESOURCE));

            parseParts(inputList, inputs.getChildren(PART));

            parseEquipment(inputList, inputs.getChildren(EQUIPMENT));

            Element outputs = processElement.getChild(OUTPUTS);
            List<FoodProductionProcessItem> outputList = new ArrayList<>();
            process.setOutputList(outputList);

            parseResources(outputList, outputs.getChildren(RESOURCE));

            parseParts(outputList, outputs.getChildren(PART));

            parseEquipment(outputList, outputs.getChildren(EQUIPMENT));               
        }
        
		// Assign the newList now built
		foodproductionProcessList = Collections.unmodifiableList(newList);
    }
    
    /**
     * Parses the amount resource elements in a node list.
     * @param list the list to store the resources in.
     * @param resourceNodes the node list.
     * @throws Exception if error parsing resources.
     */
    private void parseResources(List<FoodProductionProcessItem> list,
            List<Element> resourceNodes) {
        for (Element resourceElement : resourceNodes) {
            FoodProductionProcessItem resourceItem = new FoodProductionProcessItem();
            resourceItem.setType(ItemType.AMOUNT_RESOURCE);
            resourceItem.setName(resourceElement.getAttributeValue(NAME).toLowerCase());
            resourceItem.setAmount(Double.parseDouble(resourceElement
                    .getAttributeValue(AMOUNT)));
            list.add(resourceItem);
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
            partItem.setName(partElement.getAttributeValue(NAME).toLowerCase());
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

        if(foodproductionProcessList != null){

            Iterator<FoodProductionProcessInfo> i = foodproductionProcessList.iterator();
            while (i.hasNext()) {
                i.next().destroy();
            }
            foodproductionProcessList.clear();
            foodproductionProcessList = null;
        }
    }
}
