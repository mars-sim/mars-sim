/*
 * Mars Simulation Project
 * JMemoryMeter.java
 * @date 2023-05-14
 * @author Barry EVans
 */
package org.mars_sim.msp.ui.swing.utils;

import java.awt.event.MouseEvent;

import javax.swing.JProgressBar;
import javax.swing.event.MouseInputAdapter;

/**
 * Creates a progress bar that displays Java memory details.
 */
@SuppressWarnings("serial")
public class JMemoryMeter extends JProgressBar {

    private final static long MEGA = (1024L*1024L);
    private final String TOOLTIPFORMAT = "Used: %,d MB, Allocated: %,d MB, Maximum: %,d MB";

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

        int freeMB = (int) (rt.freeMemory()/MEGA);
        int consumedMB = totalMemoryMB - freeMB;
        setValue(consumedMB);
        setString(consumedMB + "MB/" + totalMemoryMB + "MB");
        setToolTipText(String.format(TOOLTIPFORMAT,  consumedMB, totalMemoryMB, (int)(rt.maxMemory()/MEGA)));
    }
}
