/**
 * Mars Simulation Project
 * SupplyTableModel.java
 * @version 3.02 2012-05-06
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;

public class SupplyTableModel extends AbstractTableModel {

    // Supply categories.
    private final static String BUILDING = "Building";
    private final static String VEHICLE = "Vehicle";
    private final static String EQUIPMENT = "Equipment";
    private final static String RESOURCE = "Resource";
    private final static String PART = "Part";
    
    // Data members
    private List<SupplyItem> supplyList;
    
    /**
     * Constructor
     * @param resupply the resupply mission or null if none.
     */
    public SupplyTableModel(Resupply resupply) {
        
        // Initialize data members.
        supplyList = new ArrayList<SupplyItem>();
        
        if (resupply != null) {
            // Populate supply list from resupply mission.
            populateSupplies(resupply);
        }
    }
    
    /**
     * Populate supply list from resupply mission.
     * @param resupply the resupply mission.
     */
    private void populateSupplies(Resupply resupply) {
        
        // Populate buildings.
        populateSupplyTypeList(BUILDING, resupply.getNewBuildings());
        
        // Populate vehicles.
        populateSupplyTypeList(VEHICLE, resupply.getNewVehicles());
        
        // Populate equipment.
        List<String> sortEquipment = 
            new ArrayList<String>(resupply.getNewEquipment().keySet());
        Collections.sort(sortEquipment);
        Iterator<String> i = sortEquipment.iterator();
        while (i.hasNext()) {
            String equipmentType = i.next();
            int num = resupply.getNewEquipment().get(equipmentType);
            SupplyItem supplyItem = new SupplyItem(EQUIPMENT, equipmentType, num);
            supplyList.add(supplyItem);
        }
        
        // Populate resources.
        List<AmountResource> sortResources = 
            new ArrayList<AmountResource>(resupply.getNewResources().keySet());
        Collections.sort(sortResources);
        Iterator<AmountResource> j = sortResources.iterator();
        while (j.hasNext()) {
            AmountResource resource = j.next();
            double amount = resupply.getNewResources().get(resource);
            SupplyItem supplyItem = new SupplyItem(RESOURCE, resource.getName(), amount);
            supplyList.add(supplyItem);
        }
        
        // Populate parts.
        List<Part> sortParts = 
            new ArrayList<Part>(resupply.getNewParts().keySet());
        Collections.sort(sortParts);
        Iterator<Part> k = sortParts.iterator();
        while (k.hasNext()) {
            Part part = k.next();
            int num = resupply.getNewParts().get(part);
            SupplyItem supplyItem = new SupplyItem(PART, part.getName(), num);
            supplyList.add(supplyItem);
        }
    }
    
    /**
     * Populate supplies that are in a list of strings.
     * @param category the supply category.
     * @param supplies the list of supplies as strings.
     */
    private void populateSupplyTypeList(String category, List<String> supplies) {
        
        // Create map of supplies and their numbers.
        Map<String, Integer> supplyMap = 
            new HashMap<String, Integer>(supplies.size());
        Iterator<String> i = supplies.iterator();
        while (i.hasNext()) {
            String supplyType = i.next();
            if (supplyMap.keySet().contains(supplyType)) {
                int num = supplyMap.get(supplyType) + 1;
                supplyMap.put(supplyType, num);
            }
            else {
                supplyMap.put(supplyType, 1);
            }
        }
        
        // Create and add supply item for each supply.
        List<String> sortKeys = new ArrayList<String>(supplyMap.keySet());
        Collections.sort(sortKeys);
        Iterator<String> j = sortKeys.iterator();
        while (j.hasNext()) {
            String supplyType = j.next();
            int num = supplyMap.get(supplyType);
            SupplyItem supplyItem = new SupplyItem(category, supplyType, num);
            supplyList.add(supplyItem);
        }
    }
    
    @Override
    public int getColumnCount() {
        return 3;
    }
    
    @Override
    public String getColumnName(int column) {
        String result = null;
        if (column == 0) result = "Category";
        else if (column == 1) result = "Supply Type";
        else if (column == 2) result = "Number/Amount (kg)";
        return result;
    }

    @Override
    public int getRowCount() {
        return supplyList.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int colIndex) {
        Object result = null;
        if (rowIndex < supplyList.size()) {
            SupplyItem item = supplyList.get(rowIndex);
            if (colIndex == 0) result = item.category;
            else if (colIndex == 1) result = item.type;
            else if (colIndex == 2) result = item.number;
        }
        return result;
    }
    
    /**
     * Inner class to represent a supply table item.
     */
    public class SupplyItem {
        
        // Data members
        public String category;
        public String type;
        public Number number;
        
        /**
         * Constructor
         * @param category the supply category.
         * @param type the supply type.
         * @param number the supply number.
         */
        public SupplyItem(String category, String type, Number number) {
            this.category = category;
            this.type = type;
            this.number = number;
        }
    }
}