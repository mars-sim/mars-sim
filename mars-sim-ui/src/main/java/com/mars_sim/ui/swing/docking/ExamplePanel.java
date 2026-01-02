package com.mars_sim.ui.swing.docking;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.app.Docking;

class ExamplePanel extends JPanel
                    implements Dockable {
    private final String tabText;
    private final String title;

    public ExamplePanel(String tabText, String title) {
        super(new BorderLayout());

        this.tabText = tabText;
        this.title = title;

        JPanel panel = new JPanel();
        panel.add(new JLabel(tabText + " - " + title));

        add(panel, BorderLayout.CENTER);

        // the single call to register any docking panel extending this abstract class
        Docking.registerDockable(this);
    }

    @Override
    public String getPersistentID() {
        return title;
    }

    @Override
    public String getTabText() {
        return tabText;
    }

    @Override
    public String getTitleText() {
        return title;
    }
}