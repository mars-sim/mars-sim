/*
 * Mars Simulation Project
 * ContentWindow.java
 * @date 2025-11-09
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.desktop;

import java.util.Properties;

import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;

import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.ui.swing.ConfigurableWindow;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MainWindow;
import com.mars_sim.ui.swing.TemporalComponent;

/**
 * This class renders a ContentPanel into an InternalWindow that can be displayed in a DesktopPane.
 */
public class ContentWindow extends JInternalFrame
    implements ConfigurableWindow, TemporalComponent {

    private ContentPanel content;

    /**
     * Create an internal window to display the specified content panel inside a desktiop.
     * @param desktop
     * @param content
     */
    public ContentWindow(MainDesktopPane desktop, ContentPanel content) {

		// use JInternalFrame constructor
		super(content.getTitle(), true, // resizable
				true, // closable
				false, // maximizable
				false // iconifiable
		);

        this.content = content;

        setFrameIcon(MainWindow.getLanderIcon());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setContentPane(content);
		desktop.add(this);
			
		pack();
        setVisible(true);
    }

    /**
     * Get the content panel being displayed in this window
     * @return
     */
    public ContentPanel getContent() {
        return content;
    }

	/**
	 * Updates window. 
	 * Note: This is overridden by subclasses.
	 * 
	 * @param pulse Clock step advancement
	 */
    @Override
	public void clockUpdate(ClockPulse pulse) {
        content.clockUpdate(pulse);
    }

    /**
     * Destroy the assocaited content panel.
     */
    public void destroy() {
        content.destroy();
    }

    /**
     * Gets the UI properties for this window.
     */
    @Override
    public Properties getUIProps() {
        if (content instanceof ConfigurableWindow cw) {
            return cw.getUIProps();
        }
        return new Properties();
    }
}
