/*
 * Mars Simulation Project
 * CollapsibleEventPanel.java
 * @date 2026-03-30
 * @author GitHub Copilot
 */
package com.mars_sim.ui.swing.tool.eventviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;

/**
 * A collapsible panel that displays a HistoricalEvent.
 * Shows basic information when collapsed and detailed information when expanded.
 */
@SuppressWarnings("serial")
class CollapsibleEventPanel extends JPanel {
    
    private static final String EXPAND_SYMBOL = "▶";
    private static final String COLLAPSE_SYMBOL = "▼";

    private JPanel detailPanel;
    private JLabel expandCollapseLabel;
    private boolean expanded = false;
    private HistoricalEvent event;
    private UIContext uiContext;
    private EventViewer viewer;
    
    /**
     * Constructor.
     * 
     * @param event The historical event to display
     * @param eventViewer 
     * @param uiContext The UI context for creating EntityLabels
     */
    public CollapsibleEventPanel(HistoricalEvent event, EventViewer eventViewer, UIContext uiContext) {

        this.viewer = eventViewer;
        initializeUI(event, uiContext);
    }
    
    /**
     * Initialize the user interface components.
     */
    private void initializeUI(HistoricalEvent event, UIContext uiContext) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEtchedBorder());

        this.event = event;
        this.uiContext = uiContext;
        
        var headerPanel = createHeaderPanel(event);
        
        add(headerPanel, BorderLayout.NORTH);

        // Any width but must have a max height to prevent excessively large panels
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
    }
    
    /**
     * Create the header panel that shows basic event information.
     * @param event The historical event to display
     * @return The header panel component
     */
    private JPanel createHeaderPanel(HistoricalEvent event) {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Add mouse listener to toggle expansion
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleExpansion();
            }
        });
        
        // Expand/collapse indicator
        expandCollapseLabel = new JLabel(EXPAND_SYMBOL);
        expandCollapseLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        headerPanel.add(expandCollapseLabel, BorderLayout.WEST);
        
        // Main header content
        JPanel topDetailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        topDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Event type
        JLabel typeLabel = new JLabel(event.getType().getName());
        topDetailsPanel.add(typeLabel);
        
        // Timestamp
        if (event.getTimestamp() != null) {
            JLabel timeLabel = new JLabel("@ " + event.getTimestamp().getTruncatedDateTimeStamp());
            topDetailsPanel.add(timeLabel);
        }
        
        var detailsPanel = Box.createVerticalBox();
        detailsPanel.add(topDetailsPanel);

        // Source
        JLabel sourceLabel = new JLabel("(" + event.getSource().getName() + ")");
        sourceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sourceLabel.setBackground(Color.RED);
        detailsPanel.add(sourceLabel);
        
        headerPanel.add(detailsPanel, BorderLayout.CENTER);
        return headerPanel;
    }
    
    /**
     * Create the detail panel that shows expanded event information.
     */
    private void createDetailPanel() {
        detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createEmptyBorder(0, 3, 0, 3),
                            BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 10);
        
        int row = 0;
        
        // Acknowledged checkbox
        var acknowledgedCheckBox = new JCheckBox();
        acknowledgedCheckBox.setSelected(event.isAcknowledged());
        acknowledgedCheckBox.addActionListener(e -> {
            event.setAcknowledged(acknowledgedCheckBox.isSelected());

            // If acknowledged check it is still visible.
            if (event.isAcknowledged()) {
                viewer.recheckEvent(event, this);
            }   
        });
        addDetailRow("Source:", new EntityLabel(event.getSource(), uiContext), gbc, row++);
        addDetailRow("Acknowledged:", acknowledgedCheckBox, gbc, row++);
        
        // Category
        addDetailRow("Category:", new JLabel(event.getCategory().getName()), gbc, row++);
        
        // What Cause
        if (event.getWhatCause() != null && !event.getWhatCause().isEmpty()) {
            addDetailRow("What Cause:", new JLabel(event.getWhatCause()), gbc, row++);
        }
        
        // While Doing
        if (event.getWhileDoing() != null && !event.getWhileDoing().isEmpty()) {
            addDetailRow("While Doing:", new JLabel(event.getWhileDoing()), gbc, row++);
        }
        
        // Who
        if (event.getAffected() != null) {
            addDetailRow("Affected:", new EntityLabel(event.getAffected(), uiContext), gbc, row++);
        }
        
        // Coordinates but not both
        if (event.getCoordinates() != null) {
            addDetailRow("Location:", new JLabel(event.getCoordinates().getFormattedString()), gbc, row++);
        }
    }
    
    /**
     * Add a detail row to the detail panel.
     * 
     * @param label The label text
     * @param value The value component
     * @param gbc The GridBagConstraints
     * @param row The row number
     */
    private void addDetailRow(String label, JComponent valueComp, GridBagConstraints gbc, int row) {
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(StyleManager.getLabelFont());
        detailPanel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        detailPanel.add(valueComp, gbc);
    }
    
    /**
     * Toggle the expansion state of the panel.
     */
    private void toggleExpansion() {
        setExpanded(!expanded);
    }
    
    /**
     * Set the expansion state of the panel.
     * 
     * @param expanded True to expand, false to collapse
     */
    private void setExpanded(boolean expanded) {
        this.expanded = expanded;
        
        if (expanded) {
            expandCollapseLabel.setText(COLLAPSE_SYMBOL);

            // Create detail panel on demand
            if (detailPanel == null) {
                createDetailPanel();
            }
            add(detailPanel, BorderLayout.CENTER);
        } else {
            expandCollapseLabel.setText(EXPAND_SYMBOL);
            remove(detailPanel);
        }
        
        revalidate();
        repaint();
    }
}