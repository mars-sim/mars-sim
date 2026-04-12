/*
 * Mars Simulation Project
 * DockingAdapter.java
 * @date 2025-12-30
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.docking;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.SwingHelper;

import io.github.andrewauclair.moderndocking.Dockable;

/**
 * Adapter class to wrap a ContentPanel as a Dockable for use in the docking framework.
 */
class DockingAdapter extends JPanel implements Dockable {

    private ContentPanel content;
    private JLabel contentLabel;
    private JLabel contentSize;

    /**
     * Create a new DockingAdapter to wrap the given content panel.
     * @param contentPanel Panel holding the content.
     */
    public DockingAdapter(ContentPanel contentPanel) {
        this.content = contentPanel;

        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        // Add a diagnostic panel if debug mode
        if (StyleManager.isDebug()) {
            add(createDiagPanel(), BorderLayout.SOUTH);
        }
    }

    /**
     * Create a panel to display diagnostic information about the content panel. This is only shown in debug mode.
     * @return the diagnostic panel
     */
    private Component createDiagPanel() {
        var header = new JPanel(new FlowLayout());

        contentLabel = new JLabel();
        contentLabel.setFont(StyleManager.getSmallLabelFont());
        header.add(contentLabel);

        contentSize = new JLabel();
        contentSize.setFont(StyleManager.getSmallLabelFont());
        header.add(contentSize);

        updateDiagPanel();

        // Listen for size changes to update the diagnostic info        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                updateDiagPanel();
            }

            @Override
            public void componentShown(ComponentEvent evt) {
                updateDiagPanel();
            }
        });
        return header;
    }

    private void updateDiagPanel() {
        contentLabel.setText("Content Min: " + SwingHelper.toString(content.getMinimumSize()));
        contentSize.setText("Content Size: " + SwingHelper.toString(content.getSize()));
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

        // Line up the close in the future, cannot deregister until after the call stack unwinds
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
