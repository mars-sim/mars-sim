/*
 * Mars Simulation Project
 * ToolFrameListener.java
 * @date 2022-07-23
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.toolwindow;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.mars_sim.msp.core.Msg;

/**
 * ToolFrameListener manages internal frame behaviors for tool windows.
 */
public class ToolFrameListener
extends InternalFrameAdapter {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(ToolFrameListener.class.getName());

	/** opens internal frame (overridden) */
	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		JInternalFrame frame = (JInternalFrame) e.getSource();
		try { frame.setClosed(false); }
		catch (java.beans.PropertyVetoException v) {
			logger.log(
				Level.SEVERE,
				Msg.getString(
					"ToolFrameListener.log.veto", //$NON-NLS-1$
					frame.getTitle()
				)
			);
		}
	}
}

