/**
 * Mars Simulation Project
 * LegendDisplay.java
 * @version 2.87 2009-10-04
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.swing.tool.navigator;

import java.awt.*;
import javax.swing.*;

import org.mars_sim.msp.ui.swing.ImageLoader;

/** 
 * The LegendDisplay class is a UI class that represents a map legend
 * in the `Mars Navigator' tool. It can either show a distance
 * legend, or a color chart indicating elevation for the
 * topographical map.
 */
public class LegendDisplay extends JLabel {

    // Data members
    private ImageIcon legend; // Image icon
    private Image colorImg;
    private Image distanceImg;
    private Image usgsDistanceImg;
    private boolean useUSGSLegend;

    /** Constructs a LegendDisplay object */
    public LegendDisplay() {
        colorImg = ImageLoader.getImage("Color_Legend.jpg");
        distanceImg = ImageLoader.getImage("Map_Legend.jpg");
        usgsDistanceImg = ImageLoader.getImage("USGSMap_Legend.png");
        legend = new ImageIcon(distanceImg);
        setIcon(legend);
        useUSGSLegend = false;
    }

    /** Change to topographical mode */
    public void showColor() {
        legend.setImage(colorImg);
        repaint();
    }

    /** Change to distance mode and refresh canvas */
    public void showMap() {
    	if (useUSGSLegend) legend.setImage(usgsDistanceImg);
        else legend.setImage(distanceImg);
        repaint();
    }

    /** Set USGS map legend mode
     *  @param useUSGSLegend true if using USGS map legend
     */
    public void setUSGSMode(boolean useUSGSLegend) {
    	this.useUSGSLegend = useUSGSLegend;
   	}
}
