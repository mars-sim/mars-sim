package org.mars_sim.msp.ui.swing.utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import org.apache.batik.transcoder.TranscoderException;
import org.mars_sim.msp.ui.swing.tool.svg.SVGIcon;


public class IconManager {
    
    private Map<String,Icon> icons;

    public IconManager() {
        icons = new HashMap<>();
    }

    public void addSVGIcon(String id, String resourceName, int width, int height) {
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