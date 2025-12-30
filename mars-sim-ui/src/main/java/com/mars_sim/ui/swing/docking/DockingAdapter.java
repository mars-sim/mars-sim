/*
 * Mars Simulation Project
 * DockingAdapter.java
 * @date 2025-12-30
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.docking;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mars_sim.ui.swing.ContentPanel;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.app.Docking;

/**
 * Adapter class to wrap a ContentPanel as a Dockable for use in the docking framework.
 */
class DockingAdapter extends JPanel implements Dockable {

    private ContentPanel content;

    public DockingAdapter(ContentPanel contentPanel) {
        this.content = contentPanel;
        Docking.registerDockable(this);

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
     * Get the content panel inside this docking adapter
     * @return
     */
    ContentPanel getContent() {
        return content;
    }
}
