/*
 * Mars Simulation Project
 * ClasspathEntityResolver.java
 * @date 2024-08-10
 * @author Manny Kung
 */
package com.mars_sim.core;

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
