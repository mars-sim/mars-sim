package org.mars_sim.msp.ui.swing.utils;

import java.awt.Dimension;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
/*
 * Mars Simulation Project
 * MSPIconManager.java
 * @date 2023-01-05
 * @author Barry EVans
 */
import javax.swing.Icon;

import org.apache.batik.transcoder.TranscoderException;
import org.mars_sim.msp.ui.swing.tool.svg.SVGIcon;

import com.alee.api.resource.ClassResource;
import com.alee.extended.svg.SvgIconSource;
import com.alee.managers.icon.IconManager;
import com.alee.managers.icon.set.RuntimeIconSet;

/**
 * This maintians a cache of icons based on SVG images. 
 * It currently also supprots ALEE IconManager
 */
public class MSPIconManager {
    
    private Map<String,Icon> icons;
    private RuntimeIconSet iconSet;

    public MSPIconManager() {
        icons = new HashMap<>();

        iconSet = new RuntimeIconSet("mars-sim-set");
		
		// Add the icon set to the icon manager
		IconManager.addIconSet(iconSet);
    }

    public void addSVGIcon(String id, String resourceName, int width, int height) {
        // Add to the weblaf icon manager as a background activity
		iconSet.addIcon(new SvgIconSource (
            id,
            new ClassResource(getClass(), resourceName),
            new Dimension(width, height)));

        SVGIcon newIcon = null;
        try {
            if (resourceName.startsWith("/")) {
                resourceName = resourceName.substring(1);
            }
            URL resource = getClass().getClassLoader().getResource(resourceName);

            newIcon = new SVGIcon(resource.toString(), width, height);
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        icons.put(id, newIcon);
    }

    public Icon getIcon(String id) {
        return icons.get(id);
    }
}