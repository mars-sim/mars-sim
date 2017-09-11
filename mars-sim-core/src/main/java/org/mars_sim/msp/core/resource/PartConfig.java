/**
 * Mars Simulation Project
 * PartConfig.java
 * @version 3.1.0 2017-09-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.core.SimulationConfig;

/**
 * Provides configuration information about parts. Uses a DOM document to get the information.
 */
public final class PartConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Element names
	public static final String PART = "part";
	public static final String DESCRIPTION = "description";
	public static final String NAME = "name";
	public static final String MASS = "mass";
	public static final String MAINTENANCE_ENTITY_LIST = "maintenance-entity-list";
	public static final String ENTITY = "entity";
	public static final String PROBABILITY = "probability";
	public static final String MAX_NUMBER = "max-number";

	private static int new_id;
	
	// Data members.
	//private Set<ItemResource> itemResources = new HashSet<ItemResource>();
	private Set<Part> partSet = new TreeSet<Part>();

	private Map<String, Part> namePartMap;

	private Map<Integer, Double> MTBF_map;

	private Map<Integer, Double> usage_map;

	private Map<Integer, Double> reliability_map;
	
	private Map<Integer, Double> failure_map;
	
	
    /**
     * Constructor
     * @param itemResourceDoc the item resource XML document.
     * @throws Exception if error reading XML document
     */
    public PartConfig(Document itemResourceDoc) {
		new_id = 0;

        loadItemResources(itemResourceDoc);
	
		namePartMap = new HashMap<String, Part>();	

		for (Part p : partSet) {
			namePartMap.put(p.getName(), p);
		}
		
		createMTBFs();

    }

    public Map<Integer, Double> createMTBFs() {
    	if (MTBF_map == null) {
    		MTBF_map = new HashMap<Integer, Double>();
    		for (Part p : partSet) {
    			int id = p.getID();
    			MTBF_map.put(id, 0.0);
    		}
    	}	
    	return MTBF_map;
    }
    
    public Map<Integer, Double> getMTBFs() {
    	return MTBF_map;
    }
    
    public void computeMTBF(int sol) {
    	
		for (Part p : partSet) {
			int id = p.getID();
			double old_mtbf = MTBF_map.get(id);
			double new_mtbf = old_mtbf;
			
			
			
			MTBF_map.put(id, new_mtbf);
		}

    }
    
    /**
     * Loads item resources from the parts.xml config document.
     * @param itemResourceDoc the configuration XML document.
     * @throws Exception if error loading item resources.
     */
    @SuppressWarnings("unchecked")
    private void loadItemResources(Document itemResourceDoc) {
        Element root = itemResourceDoc.getRootElement();
        List<Element> partNodes = root.getChildren(PART);
        for (Element partElement : partNodes) {
			new_id++;
            String name = "";
            String description = "no description available.";

            // Get name.
            name = partElement.getAttributeValue(NAME).toLowerCase();

            // get description
            Element descriptElem = partElement.getChild(DESCRIPTION);
            if (descriptElem != null) {
            	description = descriptElem.getText();
            }
     		
            // Get mass.
            double mass = Double.parseDouble(partElement
                    .getAttributeValue(MASS));

            // Add part to item resources.
            Part p = new Part(name, new_id, description, mass, 1);
            partSet.add(p);
            //ItemResource r = new ItemResource(name, description, mass);
            //itemResources.add(r);

            // Add maintenance entities for part.
            Element entityListElement = partElement
                    .getChild(MAINTENANCE_ENTITY_LIST);
            if (entityListElement != null) {
                List<Element> entityNodes = entityListElement
                        .getChildren(ENTITY);
                for (Element entityElement : entityNodes) {
                    String entityName = entityElement.getAttributeValue(NAME);
                    int probability = Integer.parseInt(entityElement
                            .getAttributeValue(PROBABILITY));
                    int maxNumber = Integer.parseInt(entityElement
                            .getAttributeValue(MAX_NUMBER));
                    p.addMaintenanceEntity(entityName, probability,
                            maxNumber);
                }
            }
        }
    }

	/**
	 * Gets a set of all parts.
	 * @return a set of parts.
	 */
	public Set<Part> getPartSet() {
		return partSet;
	}
	/**
	 * gives back an alphabetically ordered map of all
	 * item resources.
	 * @return {@link TreeMap}<{@link String},{@link ItemResource}>
	 */
	//public Map<String, ItemResource> getItemResourcesMap() {
	//	return itemResourceMap;
	//}
	
	/**
	 * Gets a map of part names and corresponding objects
	 * @return a map of part names and objects
	 */
	public Map<String, Part> getNamePartMap() {
		return namePartMap;
/*
		TreeMap<String,Part> map = new TreeMap<String,Part>();
		for (Part resource : itemResources) {
			map.put(resource.getName(),resource);
		}
		return map;
*/		
	}
}