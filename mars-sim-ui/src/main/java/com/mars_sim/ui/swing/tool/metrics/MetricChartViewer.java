package com.mars_sim.ui.swing.tool.metrics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import com.mars_sim.core.Entity;
import com.mars_sim.core.metrics.Metric;
import com.mars_sim.core.metrics.MetricManager;
import com.mars_sim.ui.swing.components.NamedListCellRenderer;

/**
 * A Swing component that displays multiple JFreeChart line charts in a tabbed pane.
 * Users can add and remove chart tabs using dropdown controls for Entity, Category, and Measure.
 */
public class MetricChartViewer extends JPanel {
    
    // Constants
    private static final String SOL_LABEL = "Sol (Martian Day)";
    private static final String SELECT_PROMPT = "-- Select --";
    
    private transient MetricManager metricManager;
    private JTabbedPane tabbedPane;
    private JComboBox<Object> entityComboBox;
    private JComboBox<String> categoryComboBox;
    private JComboBox<String> measureComboBox;
    private JButton newButton;
    private JButton removeButton;
    private JButton addButton;
        
    /**
     * Creates a new MetricChartViewer component with the specified MetricManager.
     * 
     * @param metricManager The MetricManager to use for populating dropdowns and retrieving metrics
     */
    public MetricChartViewer(MetricManager metricManager) {
        this.metricManager = metricManager;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        populateInitialData();
    }
    
    /**
     * Initializes all the UI components.
     */
    private void initializeComponents() {
        tabbedPane = new JTabbedPane();
        
        // Initialize combo boxes - will be populated later
        entityComboBox = new JComboBox<>();
        entityComboBox.setRenderer(new NamedListCellRenderer());
        categoryComboBox = new JComboBox<>();
        measureComboBox = new JComboBox<>();

        newButton = new JButton("New Chart");
        addButton = new JButton("Add To Chart");
        removeButton = new JButton("Remove Chart");
        
        // Initially disable buttons
        newButton.setEnabled(false);
        removeButton.setEnabled(false);
    }
    
    /**
     * Sets up the layout of the component.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create the control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Add the tabbed pane
        add(tabbedPane, BorderLayout.CENTER);
        
        // Set preferred size
        setPreferredSize(new Dimension(800, 600));
    }
    
    /**
     * Creates the control panel with dropdowns and buttons.
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Chart Controls"));
        
        // Add labels and dropdowns
        panel.add(new JLabel("Entity:"));
        panel.add(entityComboBox);
        
        panel.add(new JLabel("Category:"));
        panel.add(categoryComboBox);
        
        panel.add(new JLabel("Measure:"));
        panel.add(measureComboBox);
        
        // Add some spacing
        panel.add(Box.createHorizontalStrut(20));
        
        // Add buttons
        panel.add(newButton);
        panel.add(addButton);
        panel.add(removeButton);
        
        return panel;
    }
    
    /**
     * Sets up event handlers for the UI components.
     */
    private void setupEventHandlers() {
        // Update add button state when selections change
        entityComboBox.addActionListener(e -> entitySelected());
        categoryComboBox.addActionListener(c -> categorySelected());
        
        newButton.addActionListener(e -> addNewChart());
        addButton.addActionListener(e -> addToChart());
        removeButton.addActionListener(e -> removeSelectedChart());
        
        // Update remove button state when tab selection changes
        tabbedPane.addChangeListener(e -> updateRemoveButtonState());
    }

    private void populateMeasureComboBox() {
        measureComboBox.removeAllItems();

        if (entityComboBox.getSelectedIndex() > 0 && categoryComboBox.getSelectedIndex() > 0) {
            var measures = metricManager.getMeasures(
                                (Entity) entityComboBox.getSelectedItem(),
                                (String) categoryComboBox.getSelectedItem());
        
            for (var m : measures) {
                measureComboBox.addItem(m);
            }
            measureComboBox.setSelectedIndex(0);
            newButton.setEnabled(true);
        }
        else {
            newButton.setEnabled(false);
        }
    }
    
    private void categorySelected() {
        populateEntityComboBox();
        populateMeasureComboBox();
    }

    private void entitySelected() {
        populateCategoryComboBox();
        populateMeasureComboBox();
    }

    
    /**
     * Populates the dropdown components with data from the MetricManager.
     */
    private void populateInitialData() {
        populateEntityComboBox();
        populateCategoryComboBox();
                
        // Set initial selection to make the dropdowns more obvious
        entityComboBox.setSelectedIndex(0);
        categoryComboBox.setSelectedIndex(0);
    }
    
    /**
     * Populates the entity combo box with available entities.
     */
    private void populateEntityComboBox() {
        var original = entityComboBox.getSelectedItem();
        entityComboBox.removeAllItems();
        entityComboBox.addItem(SELECT_PROMPT);
        
        String category = null;
        if (categoryComboBox.getSelectedIndex() > 0) {
            category = (String) categoryComboBox.getSelectedItem();
        }

        // Get all unique entities from all metrics
        List<Entity> entities = metricManager.getEntities(category);
        
        for (Entity entity : entities) {
            entityComboBox.addItem(entity);
        }
        entityComboBox.setSelectedItem(original);
    }
    
    /**
     * Populates the category combo box with available categories.
     */
    private void populateCategoryComboBox() {
        var original = categoryComboBox.getSelectedItem();
        categoryComboBox.removeAllItems();
        categoryComboBox.addItem(SELECT_PROMPT);
        
        // Get all unique categories from all metrics
        Entity entity = null;
        if (entityComboBox.getSelectedIndex() > 0) {
            entity = (Entity) entityComboBox.getSelectedItem();
        }
        List<String> categories = metricManager.getCategories(entity);
        
        for (String category : categories) {
            categoryComboBox.addItem(category);
        }
        categoryComboBox.setSelectedItem(original);
    }
    
    /**
     * Updates the enabled state of the remove button based on available tabs.
     */
    private void updateRemoveButtonState() {
        removeButton.setEnabled(tabbedPane.getTabCount() > 0);
    }

    private Metric getSelectedMetric() {
        Object entityObj = entityComboBox.getSelectedItem();
        String category = (String) categoryComboBox.getSelectedItem();
        String measure = (String) measureComboBox.getSelectedItem();
        
        if (entityObj != null && category != null && measure != null &&
            !SELECT_PROMPT.equals(entityObj) && !SELECT_PROMPT.equals(category) && !SELECT_PROMPT.equals(measure)) {
            
            Entity entity = (Entity) entityObj;
            
            // Find selected Metrix
            return metricManager.getMetric(entity, category, measure);
        }
        return null;
    }
        
    private void addToChart() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        var data = getSelectedMetric();
        if (data != null && (selectedIndex >= 0)) {
            ChartPanel chartPanel = (ChartPanel) tabbedPane.getSelectedComponent();

            JFreeChart chart = chartPanel.getChart();
            XYPlot plot = chart.getXYPlot();
            var dataset = (MetricDataset) plot.getDataset();
            dataset.addMetric(data);

            tabbedPane.setTitleAt(selectedIndex, dataset.getTitle());
        }
    }

    /**
     * Adds a new chart tab based on the current dropdown selections.
     */
    private void addNewChart() {

        var data = getSelectedMetric();
        if (data != null) {
            // Create the chart
            var dataset = new MetricDataset();
            dataset.addMetric(data);

            JFreeChart chart = createMetricChart(dataset);
            
            // Create chart panel
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(600, 400));
            chartPanel.setMouseWheelEnabled(true);
            
            // Add to tabbed pane
            tabbedPane.addTab(dataset.getTitle(), chartPanel);
            
            // Select the new tab
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            
            // Update button states
            updateRemoveButtonState();
            
            // Reset selections to encourage creating different charts
            entityComboBox.setSelectedIndex(0); // Reset to "-- Select --"
            categoryComboBox.setSelectedIndex(0);
        }
    }
    
    /**
     * Removes the currently selected chart tab.
     */
    private void removeSelectedChart() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex >= 0) {
            tabbedPane.removeTabAt(selectedIndex);
            updateRemoveButtonState();
        }
    }
    
    private JFreeChart createMetricChart(MetricDataset dataset) {

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true,
                true);
        renderer.setDefaultToolTipGenerator(new MetricToolTipGenerator());

        var timeAxis = new NumberAxis("MSol");
        timeAxis.setAutoRangeIncludesZero(false);
        var valueAxis = new NumberAxis("Values");
        valueAxis.setAutoRangeIncludesZero(false);  // override default

        XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, renderer);

        return new JFreeChart("Title", JFreeChart.DEFAULT_TITLE_FONT,
                plot, true);
    }

}