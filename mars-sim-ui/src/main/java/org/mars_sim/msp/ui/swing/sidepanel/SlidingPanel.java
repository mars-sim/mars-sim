/**
 * Mars Simulation Project
 * SlidePanel.java
 * @version 3.1.0 2016-11-24
 * @author Manny Kung
 */

// Adapted from http://www.codeproject.com/Articles/565425/Sliding-Panel-in-Java
// Original author : Shubhashish_Mandal, 22 Mar 2013

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mars_sim.msp.ui.swing.sidepanel;

import java.awt.Image;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author root
 */
public class SlidingPanel extends JPanel implements StateListener {

    final int EXPAND = 1;
    final int COLLAPSE = 0;
    int state = COLLAPSE;
    JPanel slideContainer;
    SlideAnimator slider = null;
    TitlePanel titlePanel = null;

    public SlidingPanel(JComponent slideComponent, String title, Image imageIcon, boolean isExpand) {
        slideContainer = new SlideContainer();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(null);
        slider = new SlideAnimator(slideContainer, slideComponent);
        titlePanel = new TitlePanel(title, imageIcon, slideComponent.getPreferredSize().width + 8);
        add(titlePanel);
        add(slideContainer);
        
        if(!isExpand)
        	slideContainer.setVisible(false);
        else
            toggleState();
        
        //if(isExpand)
        //   toggleState();
    }

    public void reset() {
        titlePanel.toggleState(false);
        slideContainer.setVisible(false);
        validate();
    }

    public void toggleState() {
        if (state == COLLAPSE) {
            titlePanel.toggleState(true);
            slider.showAt();
            validate();
            state = EXPAND;
        } else {
            titlePanel.toggleState(false);
            slideContainer.setVisible(false);
            validate();
           state = COLLAPSE;
        }
    }
}
