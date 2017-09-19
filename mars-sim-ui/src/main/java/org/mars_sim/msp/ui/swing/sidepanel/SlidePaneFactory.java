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

import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.mars_sim.msp.ui.javafx.MainScene;

/**
 *
 * @author root
 */
public class SlidePaneFactory extends Box {
	
	private Color color = new Color(214,217,223);
	
	private StateListener listener;
    
	private List<StateListener> listeList = new ArrayList<StateListener>();

	private int themeCache;

    private SlidePaneFactory(final boolean isGroup, int theme) {
        super(BoxLayout.Y_AXIS);

    	if (themeCache != theme) {
        	themeCache = theme;
        	// pale blue : Color(198, 217, 217)) = new Color(0xC6D9D9)
        	// pale grey : Color(214,217,223) = D6D9DF
        	// pale mud : (193, 191, 157) = C1BF9D
			if (theme == 7)
				color = new Color(0xC1BF9D);
	    	else
	    		color = new Color(0xD6D9DF);
    	}	
    	
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
    
public static SlidePaneFactory getInstance(boolean isGroup, int theme) {
        return new SlidePaneFactory(isGroup, theme);
    }

    public static SlidePaneFactory getInstance(int theme) {
        return getInstance(false, theme) ;
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
        listener = new SlidingPanel(slideComponent, title, null, isExpand, color);
        super.add((JPanel) listener);
        super.add(Box.createVerticalGlue());
        listeList.add(listener);
    }
    public void add(JComponent slideComponent, String title, Image imageIcon, boolean isExpand) {
        listener = null;
        listener = new SlidingPanel(slideComponent, title, imageIcon, isExpand, color);
        super.add((JPanel) listener);
        super.add(Box.createVerticalGlue());
        listeList.add(listener);
    }
    
    public void update(Color color) {
    	
    	for (StateListener s : listeList) {
    		s.update(color);
    	}
    }
}
