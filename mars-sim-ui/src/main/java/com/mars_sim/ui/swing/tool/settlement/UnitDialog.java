/*
 * Mars Simulation Project
 * UnitDialog.java
 * @date 2025-08-28
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.formdev.flatlaf.FlatLaf;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.task.util.Worker;


/**
 * This is a dialog for an unit
 */
public class UnitDialog {

	private WorkerListener listener;
	private JDialog dialog;
	private Worker worker;
	private SettlementMapPanel settlementMapPanel;
	
	UnitDialog(Worker worker) {
		this.worker = worker;
		addListener(worker);
	}
	
    /**
     * Creates a popup window that displays a content panel.
     * It is shown below the current mouse position
     * but can be offset in the X & Y directions.
     * 
     * Size will default to the preferred size of the content unless overridden.
     * @param unit 
     * @param content Content to display
     * @param width Fixed width; can be -1
     * @param height Fixed height; can be -1
     * @param xOffset Offset of popup point in X
     * @param yOffset  Offset of popup point in X
     * @return
     */
    public JDialog createPopupWindow(JPanel content, int width, int height, int xOffset, int yOffset) {
    	settlementMapPanel = ((UnitInfoPanel)content).getDesktop().getSettlementMapPanel();
		dialog = new JDialog();
		dialog.setUndecorated(true);
                
		if (width <= 0 || height <= 0) {
			Dimension dims = content.getPreferredSize();
			width = (int) dims.getWidth();
			height = (int) dims.getHeight();
		}
		dialog.setSize(width, height);
		dialog.setResizable(false);
		dialog.add(content);

		// Make it to appear at the mouse cursor
		Point location = MouseInfo.getPointerInfo().getLocation();
		location.translate(xOffset, yOffset);
		dialog.setLocation(location);

		LocalPosition lp = worker.getPosition();
		dialog.setLocation(new Point((int)lp.getX(), (int)lp.getY()));
		
		dialog.addWindowFocusListener(new WindowFocusListener() {
			public void windowLostFocus(WindowEvent e) {
				removeListener(worker);
				dialog.dispose();
			}
			public void windowGainedFocus(WindowEvent e) {
				// no change
			}
		});

		// Call to update the all components if a new theme is chosen
		FlatLaf.updateUI();
		
		return dialog;
	}

	/**
	 * Removes the listener for a worker.
	 */
	public void removeListener(Worker w) {
		w.removeEntityListener(listener);
	}

	/**
	 * Removes the listener for a worker.
	 */
	public void addListener(Worker w) {
		listener = new WorkerListener();
		w.addEntityListener(listener);
	}
	
	/**
	 * PersonListener class listens to the change of each settler in a settlement.
	 */
	private class WorkerListener implements EntityListener {

		/**
		 * Catch unit update event.
		 *
		 * @param event the unit event.
		 */
		@Override
		public void entityUpdate(EntityEvent event) {
			if (event.getType().equals(EntityEventType.LOCAL_POSITION_EVENT) && event.getSource() instanceof Worker w) {
				LocalPosition lp = w.getPosition();
				
				// Transform the worker's position to pixel on screen				
				Point p = settlementMapPanel.convertToPixelPos(lp);
				dialog.setLocation((int)p.getX(), (int)p.getY());
			}	
		}
	}
	
	public void destroy() {
		removeListener(worker);
		listener = null;
		dialog = null;
		worker = null;
	}
}
