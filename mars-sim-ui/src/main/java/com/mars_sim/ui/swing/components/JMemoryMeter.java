/*
 * Mars Simulation Project
 * JMemoryMeter.java
 * @date 2023-05-14
 * @author Barry EVans
 */
package com.mars_sim.ui.swing.components;

import java.awt.event.MouseEvent;

import javax.swing.JProgressBar;
import javax.swing.event.MouseInputAdapter;

/**
 * Creates a progress bar that displays Java memory details.
 */
@SuppressWarnings("serial")
public class JMemoryMeter extends JProgressBar {

    private static final long MEGA = (1024L*1024L);
    private static final String MB = " MB";
    private static final String OF = " of ";
    
    private static final String TOOLTIPFORMAT = 
    		"<html> Used: %,d MB <br/> Allocated: %,d MB <br/> Maximum: %,d MB <br/> Available cores: %,d";
    		
    private int totalMemoryMB;

    public JMemoryMeter() {
        setStringPainted(true);

        // Current values
        refresh();

        addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                doGC();                
            }
        }); 
    }
    
    private void doGC() {
        setString("Doing GC");
        System.gc();
        refresh();
    }

    /**
     * Refreshes the memory statistics.
     */
    public void refresh() {
        Runtime rt = Runtime.getRuntime();

        int newTotal = (int)(rt.totalMemory() / MEGA);
        if (newTotal != totalMemoryMB) {
            totalMemoryMB = newTotal;
            setMaximum(totalMemoryMB);
        }

        int freeMB = (int)(rt.freeMemory() / MEGA);
        int consumedMB = newTotal - freeMB;
        int maxMB = (int)(rt.maxMemory() / MEGA);
        
        
        setValue(consumedMB);
        setString(consumedMB + MB + OF + newTotal + MB + " [Max: " + maxMB + MB + "]");
        setToolTipText(String.format(TOOLTIPFORMAT, consumedMB, newTotal, 
        		(int)(rt.maxMemory()/MEGA), (int)(rt.availableProcessors())));
    }
}
