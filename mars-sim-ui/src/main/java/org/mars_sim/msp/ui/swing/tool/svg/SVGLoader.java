/**
 * Mars Simulation Project
 * SVGLoader.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.svg;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory; // for batik-transcoder1.8 and 1.9
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
//import org.apache.batik.dom.svg.SAXSVGDocumentFactory; // for batik-transcoder 1.7
//import org.apache.batik.dom.util.SAXDocumentFactory; // up to 1.6
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;



/**
 * This is a static utility class that acts as a helper to load SVG images for use in the UI.
 */
public class SVGLoader {

	// Static members
	private static Map<String, GraphicsNode> svgCache;
	public final static String SVG_DIR = "/svg/";

	/**
	 * Private constructor for utility class.
	 */
	private SVGLoader() {}

	/**
	 * Load the SVG image with the specified name. This operation may either
	 * create a new graphics node of returned a previously created one.
	 * @param name Name of the SVG file to load.
	 * @return GraphicsNode containing SVG image or null if none found.
	 */
	public static GraphicsNode getSVGImage(String prefix, String name) {
		if (svgCache == null) 
			svgCache = new HashMap<String, GraphicsNode>();

		GraphicsNode found = svgCache.get(name);
		
		if (found == null) {
			
			String subPath = prefix.replace(".", "/");
			String fileName = SVG_DIR + subPath + "/" + name;
			URL resource = SVGLoader.class.getResource(fileName);
			
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
				System.err.println("getSVGImage error: " + fileName);
				e.printStackTrace(System.err);
			}
		}

		return found;
	}
}
