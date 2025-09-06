/*
 * Mars Simulation Project
 * SVGLoader.java
 * @date 2025-08-25
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.svg;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory; 
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;


/**
 * This is a static utility class that acts as a helper to load SVG images for use in the UI.
 */
public class SVGLoader {

	// Static members
	private static final Logger logger = Logger.getLogger(SVGLoader.class.getName());
	 
	public static final String SVG_DIR = "/svg/";
	public static final String SLASH = "/";
	public static final String DOT = ".";
	
	private static Map<String, GraphicsNode> svgCache;
	
	/**
	 * Private constructor for utility class.
	 */
	private SVGLoader() {}

	/**
	 * Loads the SVG image with the specified name. This operation may either
	 * create a new graphics node of returned a previously created one.
	 * 
	 * @param name Name of the SVG file to load.
	 * @return GraphicsNode containing SVG image or null if none found.
	 */
	public static GraphicsNode getSVGImage(String prefix, String name) {
		if (svgCache == null) 
			svgCache = new HashMap<>();

		GraphicsNode found = svgCache.get(name);
		
		if (found == null) {
			
			String subPath = prefix.replace(DOT, SLASH);
			String fileName = SVG_DIR + subPath + SLASH + name;
			URL resource = SVGLoader.class.getResource(fileName);
			if (resource == null) {
				logger.severe("No image found called " + fileName);
				return null;
			}
			
			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			UserAgent userAgent = new UserAgentAdapter();
			DocumentLoader loader = new DocumentLoader(userAgent);
			BridgeContext bridgeContext = new BridgeContext(userAgent, loader);
			bridgeContext.setDynamic(true);
			
			try {
				found = new GVTBuilder().build(bridgeContext, f.createDocument(resource.toString()));
				svgCache.put(name, found);
			} catch (IOException e) {
				logger.severe("getSVGImage error. fileName: " + fileName);
			}
		}

		return found;
	}
}
