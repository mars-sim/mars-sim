/*
 * Mars Simulation Project
 * DockingAdapter.java
 * @date 2025-12-30
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.docking;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.mars_sim.ui.swing.ContentPanel;

import io.github.andrewauclair.moderndocking.Dockable;

/**
 * Adapter class to wrap a ContentPanel as a Dockable for use in the docking framework.
 */
class DockingAdapter extends JPanel implements Dockable {

    private ContentPanel content;

    public DockingAdapter(ContentPanel contentPanel) {
        this.content = contentPanel;

        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    public String getPersistentID() {
        return content.getName();
    }

    @Override
    public String getTabText() {
        return content.getTitle();
    }

    /**
     * There is no listener to trap when a Dockable is closed so this method is used to 
     * notify the DockingWindow that the content panel is being closed so it can be deregistered and destroyed.
     */
    @Override
    public boolean requestClose() {

        // Line up the close in the future
        DockingWindow window = (DockingWindow) getTopLevelAncestor();
        SwingUtilities.invokeLater(() ->
            window.closeContentPanel(this)
        );
        // Return true to allow the close to proceed.
        return true;
    }

    /**
     * ContentPanels are designed to be have internal scrolling support.
     * @return false
     */
    @Override
    public boolean isWrappableInScrollpane() {
        return false;
    }

    /**
     * Get the content panel inside this docking adapter
     * @return the content panel
     */
    ContentPanel getContent() {
        return content;
    }
}
