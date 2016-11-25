/**
 * Mars Simulation Project
 * SlidePaneFactory.java
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author root
 */
public class SlidePaneFactory extends Box {

    StateListener listener;
    List<StateListener> listeList = new ArrayList<StateListener>();

    private SlidePaneFactory(final boolean isGroup) {
        super(BoxLayout.Y_AXIS);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getSource() instanceof SlidePaneFactory)
                    return;
                for (StateListener lister : listeList) {
                    if (((JPanel) e.getSource()).getParent() == lister) {
                        lister.toggleState();
                        continue;
                    }
                    if(isGroup)
                        lister.reset();
                }
            }
        });
    }
public static SlidePaneFactory getInstance(boolean isGroup) {
        return new SlidePaneFactory(isGroup);
    }
    public static SlidePaneFactory getInstance() {
        return getInstance(false) ;
    }

    public void add(JComponent slideComponent) {
        add(slideComponent, null, null, false);
    }

    public void add(JComponent slideComponent, String title) {
        add(slideComponent, title, null, false);
    }
    public void add(JComponent slideComponent, String title, Image imageIcon) {
        add(slideComponent, title, imageIcon, false);
    }
    public void add(JComponent slideComponent, String title, boolean isExpand) {
        listener = null;
        listener = new SlidingPanel(slideComponent, title, null, isExpand);
        super.add((JPanel) listener);
        super.add(Box.createVerticalGlue());
        listeList.add(listener);
    }
    public void add(JComponent slideComponent, String title, Image imageIcon, boolean isExpand) {
        listener = null;
        listener = new SlidingPanel(slideComponent, title, imageIcon, isExpand);
        super.add((JPanel) listener);
        super.add(Box.createVerticalGlue());
        listeList.add(listener);
    }
}
