/**
 * Mars Simulation Project
 * ClasspathEntityResolver.java
 * @version 3.1.0 2017-11-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public class ClasspathEntityResolver implements EntityResolver {

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (systemId.contains("/conf/")) {
			String dtd = systemId.substring(systemId.indexOf("/conf"));
			
			return new InputSource(getClass().getResourceAsStream(dtd));
		}
		
		return null;
	}

}
