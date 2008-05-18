/**
 * Mars Simulation Project
 * AmountResourceConfig.java
 * @version 2.84 2008-05-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.resource;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Provides configuration information about amount resources.
 * Uses a DOM document to get the information. 
 */
public class AmountResourceConfig implements Serializable {

	// Element names
	private static final String RESOURCE = "resource";
	private static final String NAME = "name";
	private static final String PHASE = "phase";
	private static final String LIFE_SUPPORT = "life-support";
	
	/**
	 * Constructor
	 * @param amountResourceDoc the amount resource XML document.
	 * @throws Exception if error reading XML document
	 */
	public AmountResourceConfig(Document amountResourceDoc) throws Exception {
		loadAmountResources(amountResourceDoc);
	}
	
	/**
	 * Loads amount resources from the resources.xml config document.
	 * @param amountResourceDoc the configuration XML document.
	 * @throws Exception if error loading amount resources.
	 */
	private void loadAmountResources(Document amountResourceDoc) throws Exception {
		Element root = amountResourceDoc.getDocumentElement();
		NodeList resourceNodes = root.getElementsByTagName(RESOURCE);
		for (int x=0; x < resourceNodes.getLength(); x++) {
			String name = "";
			
			try {
				Element resourceElement = (Element) resourceNodes.item(x);
				
				// Get name.
				name = resourceElement.getAttribute(NAME);
				
				// Get phase.
				String phaseString = resourceElement.getAttribute(PHASE);
				Phase phase = Phase.findPhase(phaseString);
				
				// Get life support
				Boolean lifeSupport = Boolean.parseBoolean(resourceElement.getAttribute(LIFE_SUPPORT));
				
				// Create new amount resource.
				new AmountResource(name, phase, lifeSupport);
			}
			catch (Exception e) {
				throw new Exception("Error reading resources " + name + ": " + e.getMessage());
			}
		}
	}
}