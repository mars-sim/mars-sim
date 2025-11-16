/*
 * Mars Simulation Project
 * ContentWindow.java
 * @date 2025-11-09
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.desktop;

import javax.swing.WindowConstants;

import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.tool_window.ToolWindow;

/**
 * This class renders a ContentPanel into an InternalWindow that can be displayed in a DesktopPane.
 */
public class ContentWindow extends ToolWindow {

    private ContentPanel content;

    public ContentWindow(MainDesktopPane desktop, ContentPanel content) {
        super(content.getName(), content.getTitle(), desktop);
        this.content = content;

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
	public void update(ClockPulse pulse) {
        content.update(pulse);
    }

    /**
     * Destroy the assocaited content panel.
     */
    @Override
    public void destroy() {
        content.destroy();
    }
}
