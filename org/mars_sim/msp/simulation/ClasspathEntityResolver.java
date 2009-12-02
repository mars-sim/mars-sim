package org.mars_sim.msp.simulation;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ClasspathEntityResolver implements EntityResolver {

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (systemId.contains("/conf/")) {
			String dtd = systemId.substring(systemId.indexOf("/conf"));
			
			return new InputSource(getClass().getResourceAsStream(dtd));
		}
		
		return null;
	}

}
