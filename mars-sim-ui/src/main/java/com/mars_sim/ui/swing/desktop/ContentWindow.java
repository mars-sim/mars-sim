/*
 * Mars Simulation Project
 * ContentWindow.java
 * @date 2025-11-09
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.desktop;

import javax.swing.WindowConstants;

import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.tool_window.ToolWindow;

/**
 * This class renders a ContentPanel into an InternalWindow that can be displayed in a DesktopPane.
 */
public class ContentWindow extends ToolWindow {

    private ContentPanel content;

    public ContentWindow(String name, MainDesktopPane desktop, ContentPanel content) {
        super(name, content.getTitle(), desktop);
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
}
