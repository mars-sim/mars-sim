/*
 * Mars Simulation Project
 * EventViewer.java
 * @date 2026-03-30
 * @author GitHub Copilot
 */
package com.mars_sim.ui.swing.tool.eventviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;

import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.events.HistoricalEventListener;
import com.mars_sim.core.events.HistoricalEventManager;
import com.mars_sim.ui.swing.ConfigurableWindow;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.displayinfo.EntityDisplayInfoFactory;

/**
 * EventViewer displays historical events in a collapsible panel format.
 * Events are sorted by timestamp with the most recent shown at the top.
 */
@SuppressWarnings("serial")
public class EventViewer extends ContentPanel implements ConfigurableWindow, HistoricalEventListener {

    private static final String SELECTED_CATEGORIES = "selectedCategories";
    public static final String NAME = "eventviewer";
	public static final String ICON = "event";
	public static final String TITLE = "Event Viewer";
    
    private HistoricalEventManager eventManager;
    private JPanel eventListPanel;
    private UIContext uiContext;
    private JButton filterButton;
    private JPopupMenu filterMenu;
    private Set<HistoricalEventCategory> selectedCategories;
    
    /**
     * Constructor.
     * 
     * @param uiContext The UI context
     */
    public EventViewer(UIContext uiContext, Properties userSettings) {
        super(NAME, "Historical Events", Placement.CENTER);
        
        this.uiContext = uiContext;
        this.eventManager = uiContext.getSimulation().getEventManager();

        // Load initial filter before loading events
        loadInitialFilter(userSettings);

        initializeUI();
        loadEvents();
        
        // Register as listener for new events
        eventManager.addListener(this);
    }
    
    /**
     * Load initial filter settings from user properties.
     * @param userSettings Settings for the viewer.
     */
    private void loadInitialFilter(Properties userSettings) {
        String selectedCategoryNames = userSettings.getProperty(SELECTED_CATEGORIES);
        if (selectedCategoryNames != null && !selectedCategoryNames.isEmpty()) {
            Set<HistoricalEventCategory> loadedCategories = EnumSet.noneOf(HistoricalEventCategory.class);
            for (String name : selectedCategoryNames.split(",")) {
                try {
                    HistoricalEventCategory category = HistoricalEventCategory.valueOf(name.trim());
                    loadedCategories.add(category);
                } catch (IllegalArgumentException e) {
                    // Ignore invalid category names
                }
            }
            selectedCategories = loadedCategories;
        }
        else {
            // By default, show all categories
            selectedCategories = EnumSet.allOf(HistoricalEventCategory.class);
        }
    }

    /**
     * Initialize the user interface components.
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create toolbar
        var toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);
        
        // Create the main panel to hold all event panels
        eventListPanel = new JPanel();
        eventListPanel.setLayout(new BoxLayout(eventListPanel, BoxLayout.Y_AXIS));
        
        // Create scroll pane
        var scrollPane = new JScrollPane(eventListPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);

        var dim = new Dimension(300, 400);
        setPreferredSize(dim);
        setMinimumSize(dim);
    }
    
    private boolean isEventVisible(HistoricalEvent event) {
        return selectedCategories.contains(event.getCategory());
    }

    /**
     * Load and display all events from the event manager.
     */
    private void loadEvents() {
        eventListPanel.removeAll();
        
        List<HistoricalEvent> events = eventManager.getEvents();
        
        // Filter and sort events by timestamp, most recent first
        List<HistoricalEvent> sortedEvents = events.stream()
            .filter(this::isEventVisible)
            .sorted((e1, e2) -> {
                return e2.getTimestamp().compareTo(e1.getTimestamp());
            })
            .collect(Collectors.toList());
        
        // Create collapsible panel for each event
        for (HistoricalEvent event : sortedEvents) {
            CollapsibleEventPanel eventPanel = new CollapsibleEventPanel(event, uiContext);
            eventListPanel.add(eventPanel);
            eventListPanel.add(Box.createVerticalStrut(2)); // Small gap between panels
        }
        
        // Add glue to push all panels to the top
        eventListPanel.add(Box.createVerticalGlue());
        
        // Refresh the display
        revalidate();
        repaint();
    }
    
    /**
     * New event has been created. Add it to the top of the list if it matches the current filter.
     * @param event The new event added
     */
    @Override
    public void eventAdded(HistoricalEvent event) {
        if (isEventVisible(event)) {
            // Add new event at the top of the list
            CollapsibleEventPanel eventPanel = new CollapsibleEventPanel(event, uiContext);
            eventListPanel.add(eventPanel, 0);
            eventListPanel.add(Box.createVerticalStrut(2), 1); // Small gap after the new panel
            
            // Refresh the display
            revalidate();
            repaint();

            // New events play a sound to alert the user
            var entity = event.getEntity();
            if (entity != null) {
                var displayInfo = EntityDisplayInfoFactory.getDisplayInfo(entity);
                if (displayInfo != null) {
                    uiContext.playSound(displayInfo.getSound(entity));
                }
            }
        }
    }

    /**
     * Create the toolbar with filter controls.
     */
    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        // Add filter label
        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(StyleManager.getLabelFont());
        toolbar.add(filterLabel);
        
        // Create filter button
        filterButton = new JButton();
        filterButton.setToolTipText("Click to filter events by category");
        filterButton.addActionListener(e -> showFilterMenu());
        toolbar.add(filterButton);
        updateFilterButton();  // Aligned with any preloaded filter settings
        
        // Create filter popup menu
        createFilterMenu();

        return toolbar;
    }
    
    /**
     * Create the filter popup menu with category checkboxes.
     */
    private void createFilterMenu() {
        filterMenu = new JPopupMenu("Category Filter");
        
        // Add "All" option
        JMenuItem allItem = new JMenuItem("All");
        allItem.addActionListener(e -> {
            selectedCategories.clear();
            selectedCategories.addAll(EnumSet.allOf(HistoricalEventCategory.class));
            updateFilterButton();
            loadEvents();
            filterMenu.setVisible(false);
        });
        filterMenu.add(allItem);
        
        filterMenu.addSeparator();
        
        // Add checkbox for each category
        for (HistoricalEventCategory category : HistoricalEventCategory.values()) {
            JCheckBoxMenuItem categoryItem = new JCheckBoxMenuItem(category.getName(), true);
            categoryItem.addActionListener(e -> {
                if (categoryItem.isSelected()) {
                    selectedCategories.add(category);
                } else {
                    selectedCategories.remove(category);
                }
                updateFilterButton();
                loadEvents();
            });
            filterMenu.add(categoryItem);
        }
    }
    
    /**
     * Show the filter menu below the filter button.
     */
    private void showFilterMenu() {
        // Update checkbox states based on current selection
        for (int i = 2; i < filterMenu.getComponentCount(); i++) { // Skip All and separator
            if (filterMenu.getComponent(i) instanceof JCheckBoxMenuItem item) {
                HistoricalEventCategory category = HistoricalEventCategory.values()[i - 2];
                item.setSelected(selectedCategories.contains(category));
            }
        }
        
        filterMenu.show(filterButton, 0, filterButton.getHeight());
    }
    
    /**
     * Update the filter button text based on selected categories.
     */
    private void updateFilterButton() {
        int selectedCount = selectedCategories.size();
        int totalCount = HistoricalEventCategory.values().length;
        
        if (selectedCount == totalCount) {
            filterButton.setText("All Categories");
        } else if (selectedCount == 1) {
            HistoricalEventCategory category = selectedCategories.iterator().next();
            filterButton.setText(category.getName() + " ");
        } else {
            filterButton.setText(selectedCount + " Categories ");
        }
    }
    
    /**
     * Remove listener on event manager.
     */
    @Override
    public void destroy() {
        // Remove listener when panel is destroyed
        if (eventManager != null) {
            eventManager.removeListener(this);
        }
        super.destroy();
    }

    @Override
    public Properties getUIProps() {
        var props = new Properties();

        var selectedCategoryNames = selectedCategories.stream()
            .map(HistoricalEventCategory::name)
            .collect(Collectors.joining(","));
        props.setProperty(SELECTED_CATEGORIES, selectedCategoryNames);

        return props;
    }
}