/**
 * Mars Simulation Project
 * LegendDisplay.java
 * @version 2.70 2000-09-04
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

    private ImageIcon legend;  // Image icon
    private Image colorImg;
    private Image distanceImg;
    
    public LegendDisplay() {
	colorImg = Toolkit.getDefaultToolkit().getImage("images/Color_Legend.jpg");
	distanceImg = Toolkit.getDefaultToolkit().getImage("images/Map_Legend.jpg");
	legend = new ImageIcon(distanceImg);
	setIcon(legend);
    }

    /** Change to topographical mode */
    public void showColor() { 
	legend.setImage(colorImg);
	repaint();
    }

    /** Change to distance mode and refresh canvas */
    public void showMap() {
	legend.setImage(distanceImg);
	repaint();
    }
}
