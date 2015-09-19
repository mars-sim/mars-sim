/**
 * Mars Simulation Project
 * InventoryDemo.java
 * @version 3.08 2015-09-19
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.ResizableFrame;


public class InventoryDemo extends DesktopArea {

    final FPSCounter fpsCounter;
    final ResizableFrame frame;
    final InventoryPanel inventoryPanel;

    boolean quit;

    public InventoryDemo() {
        fpsCounter = new FPSCounter();

        inventoryPanel = new InventoryPanel(5, 5);

        frame = new ResizableFrame();
        frame.setTitle("Growing Areas");
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        frame.add(inventoryPanel);

        add(fpsCounter);
        add(frame);
    }

    void positionFrame() {
        frame.adjustSize();
        frame.setPosition(
                getInnerX() + (getInnerWidth() - frame.getWidth())/2,
                getInnerY() + (getInnerHeight() - frame.getHeight())/2);
    }

    @Override
    protected void layout() {
        super.layout();

        // fpsCounter is bottom right
        fpsCounter.adjustSize();
        fpsCounter.setPosition(
                getInnerRight() - fpsCounter.getWidth(),
                getInnerBottom() - fpsCounter.getHeight());
    }

}
