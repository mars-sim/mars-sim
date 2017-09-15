/**
 * Mars Simulation Project
 * PartConfig.java
 * @version 3.1.0 2017-09-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Provides configuration information about parts. Uses a DOM document to get the information.
 */
public final class PartConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static Logger logger = Logger.getLogger(PartConfig.class.getName());
	
    private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1, logger.getName().length());

	// Element names
	public static final String PART = "part";
	public static final String DESCRIPTION = "description";
	public static final String NAME = "name";
	public static final String MASS = "mass";
	public static final String MAINTENANCE_ENTITY_LIST = "maintenance-entity-list";
	public static final String ENTITY = "entity";
	public static final String PROBABILITY = "probability";
	public static final String MAX_NUMBER = "max-number";

	public static final int NUM_YEARS = 3;
	
	public static final double MAX_MTBF = 669*NUM_YEARS;
	
	private static int new_id;
	
	private static MarsClock marsClock;
	
	private static UnitManager unitManager;
	
	// Data members.
	//private Set<ItemResource> itemResources = new HashSet<ItemResource>();
	private static Set<Part> partSet = new TreeSet<Part>();

	private static Map<String, Part> namePartMap;

	private static Map<Integer, Double> MTBF_map;

	//private Map<Integer, Double> usage_map;

	private static Map<Integer, Double> reliability_map;
	
	private static Map<Integer, Integer> failure_map;
	
	
    /**
     * Constructor
     * @param itemResourceDoc the item resource XML document.
     * @throws Exception if error reading XML document
     */
    public PartConfig(Document itemResourceDoc) {
		new_id = 0;
		
		unitManager = Simulation.instance().getUnitManager();
		
        loadItemResources(itemResourceDoc);
	
		namePartMap = new HashMap<String, Part>();	

		for (Part p : partSet) {
			namePartMap.put(p.getName(), p);
		}
		
		setupReliability();

    }

    public void setupReliability() {
    		MTBF_map = new HashMap<Integer, Double>();
    		reliability_map = new HashMap<Integer, Double>();
    		failure_map = new HashMap<Integer, Integer>();
    		
    		for (Part p : partSet) {
    			int id = p.getID();
    			MTBF_map.put(id, MAX_MTBF);
    			failure_map.put(id, 0);
    			reliability_map.put(id, 100.0);
    		}
    }
    
    public Map<Integer, Double> getMTBFs() {
    	return MTBF_map;
    }
    
    public void computeReliability(Part p) {
    	
		int id = p.getID();
		//double old_mtbf = MTBF_map.get(id);
		double new_mtbf = 0;
		
		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();

		int sol = marsClock.getMissionSol();
		int numSols = sol - p.getStartSol();
		int numFailures = failure_map.get(id);
		
		if (numFailures == 0)
			new_mtbf = MAX_MTBF;
		else {
			if (numSols == 0) {
				numSols = 1;
				
				new_mtbf = computeMTBF(numSols, numFailures, p);
			}
			else
				new_mtbf = computeMTBF(numSols, numFailures, p);
		}	
		
		
		MTBF_map.put(id, new_mtbf);
		
		double percent_reliability = Math.exp(-numSols/new_mtbf)*100;
		
		//LogConsolidated.log(logger, Level.INFO, 0, sourceName, 
		//		"The 3-year reliability rating of " + p.getName() + " is now " 
		//		+ Math.round(percent_reliability*100.0)/100.0 + " %", null);
		
		if (percent_reliability >= 100)
			percent_reliability = 99.999;
		
		reliability_map.put(id, percent_reliability);

     }
    
    public double computeMTBF(double numSols, int numFailures, Part p) {
		int numItem = 0;
		// obtain the total # of this part in used from all settlements
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
		Collection<Settlement> ss = unitManager.getSettlements();
		for (Settlement s : ss) {
			Inventory inv = s.getInventory();
			int num = inv.getItemResourceNum(p);
			numItem += num;
		}
		
		// Take the average between the factory mtbf and the field measured mtbf
		return (numItem *  numSols  / numFailures + MAX_MTBF) / 2D;
    }
    
    public void computeReliability() {
		for (Part p : partSet) {
			computeReliability(p);
		}
    }
    
    public int getFailure(int id) {
    	return failure_map.get(id);
    }
    
    public double getReliability(int id) {
    	return reliability_map.get(id);
    }
    
    
    
    public void setFailure(Part p, int num) {
    	int old_failures = failure_map.get(p.getID());
    	failure_map.put(p.getID(), old_failures + num); 	
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