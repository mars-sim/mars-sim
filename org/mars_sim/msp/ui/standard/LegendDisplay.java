/**
 * Mars Simulation Project
 * LegendDisplay.java
 * @version 2.71 2000-10-22
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard;

import java.awt.*;
import javax.swing.*;

/** The LegendDisplay class is a UI class that represents a map legend
 *  in the `Mars Navigator' tool. It can either show a distance
 *  legend, or a color chart indicating elevation for the
 *  topographical map.
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
        colorImg = Toolkit.getDefaultToolkit().getImage("images/Color_Legend.jpg");
        distanceImg = Toolkit.getDefaultToolkit().getImage("images/Map_Legend.jpg");
        usgsDistanceImg = Toolkit.getDefaultToolkit().getImage("images/USGSMap_Legend.gif");
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
     *  @param useUSGSMap true if using USGS map legend
     */
    public void setUSGSMode(boolean useUSGSLegend) {
    	this.useUSGSLegend = useUSGSLegend;
   	}
}
