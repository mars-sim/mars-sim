/**
 * Mars Simulation Project
 * ToolFrameListener.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.toolWindow;

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
	private static Logger logger = Logger.getLogger(ToolFrameListener.class.getName());

//	protected ToolWindow toolWindow;
//
//	public ToolFrameListener (ToolWindow toolWindow) {
//		this.toolWindow = toolWindow;
//	}

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

//	/** closes internal frame
//	 *
//	 *
//	// Note: NOT working. Reverted to using ToolWindow's update()
//	@Override
//	public void internalFrameClosed(InternalFrameEvent e) {
//		//System.out.println("running internalFrameClosed");
//		JInternalFrame frame = (JInternalFrame) e.getSource();
//		try {
//			frame.setClosed(true);
//			toolWindow.update();
//		}
//		catch (java.beans.PropertyVetoException v) {
//			System.err.println( "Closing exception!" );
//			logger.log(
//				Level.SEVERE,
//				Msg.getString(
//					"ToolFrameListener.log.veto", //$NON-NLS-1$
//					frame.getTitle()
//				)
//			);
//		}
//	}

}

