/*
 * Mars Simulation Project
 * MetricChartViewer.java
 * @date 2025-10-25
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.metrics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import com.mars_sim.core.Entity;
import com.mars_sim.core.metrics.Metric;
import com.mars_sim.core.metrics.MetricCategory;
import com.mars_sim.core.metrics.MetricManager;
import com.mars_sim.core.metrics.MetricManagerListener;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.components.NamedListCellRenderer;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * A Swing component that displays multiple JFreeChart line charts in a tabbed pane.
 * Users can add and remove chart tabs using dropdown controls for Entity, Category, and Measure.
 */
public class MetricChartViewer extends ContentPanel
        implements MetricManagerListener {
    
    // Constants
    public static final String NAME = "metricsviewer";
    public static final String ICON = "metrics";

    private static final String SELECT_PROMPT = "-- Select --";
    
    private transient MetricManager metricManager;
    private JTabbedPane tabbedPane;
    private JComboBox<Entity> entityComboBox;
    private JComboBox<MetricCategory> categoryComboBox;
    private JComboBox<String> measureComboBox;
    private JButton newButton;
    private JButton removeButton;
    private JButton addButton;
    private JCheckBox cummulative;
    private JCheckBox shapes;
        
    /**
     * Creates a new MetricChartViewer component with the specified MetricManager.
     * 
     * @param metricManager The MetricManager to use for populating dropdowns and retrieving metrics
     */
    public MetricChartViewer(MetricManager metricManager) {
        super(NAME, "Metric Chart Viewer", Placement.CENTER);

        this.metricManager = metricManager;

        initializeComponents();
        setupEventHandlers();
        populateInitialData();

        metricManager.addListener(this);
    }
    
    /**
     * Initializes all the UI components.
     */
    private void initializeComponents() {

        setLayout(new BorderLayout());
        
        // Create the control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // Add the tabbed pane
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
                        
        // Initially disable buttons
        newButton.setEnabled(false);
        updateButtonState();

        // Set preferred size
        setPreferredSize(new Dimension(800, 600));
    }
    
    /**
     * Creates the control panel with dropdowns and buttons.
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(SwingHelper.createLabelBorder("Chart Controls"));
        
        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Add labels and dropdowns
        keyPanel.add(new JLabel("Entity:"));
        entityComboBox = new JComboBox<>();
        entityComboBox.setRenderer(new NamedListCellRenderer(SELECT_PROMPT));
        keyPanel.add(entityComboBox);
        
        keyPanel.add(new JLabel("Category:"));
        categoryComboBox = new JComboBox<>();
        categoryComboBox.setRenderer(new NamedListCellRenderer(SELECT_PROMPT));
        keyPanel.add(categoryComboBox);
        
        keyPanel.add(new JLabel("Measure:"));
        measureComboBox = new JComboBox<>();
        keyPanel.add(measureComboBox);
        panel.add(keyPanel);
         
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        newButton = new JButton("New Chart");
        newButton.addActionListener(e -> addNewChart());
        addButton = new JButton("Add To Chart");
        addButton.addActionListener(e -> addToChart());
        removeButton = new JButton("Remove Chart");
        removeButton.addActionListener(e -> removeSelectedChart());

        cummulative = new JCheckBox("Cumulative Totals");
        cummulative.addActionListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex >= 0) {
                ChartPanel chartPanel = (ChartPanel) tabbedPane.getComponentAt(selectedIndex);
                JFreeChart chart = chartPanel.getChart();
                XYPlot plot = chart.getXYPlot();
                var dataset = (MetricDataset) plot.getDataset();
                dataset.setCumulative(cummulative.isSelected());
            }
        });

        shapes = new JCheckBox("Show Points");
        shapes.setSelected(true);
        shapes.addActionListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex >= 0) {
                ChartPanel chartPanel = (ChartPanel) tabbedPane.getComponentAt(selectedIndex);
                JFreeChart chart = chartPanel.getChart();
                XYPlot plot = chart.getXYPlot();
                ((XYLineAndShapeRenderer) plot.getRenderer()).setDefaultShapesVisible(shapes.isSelected());
            }
        });

        // Add buttons
        controlPanel.add(newButton);
        controlPanel.add(addButton);
        controlPanel.add(removeButton);
        controlPanel.add(cummulative);
        controlPanel.add(shapes);

        panel.add(controlPanel);
        
        return panel;
    }
    
    /**
     * Sets up event handlers for the UI components.
     */
    private void setupEventHandlers() {
        // Update add button state when selections change
        entityComboBox.addActionListener(e -> entitySelected());
        categoryComboBox.addActionListener(c -> categorySelected());
        
        // Update remove button state when tab selection changes
        tabbedPane.addChangeListener(e -> updateButtonState());
    }

    private void populateMeasureComboBox() {
        measureComboBox.removeAllItems();

        if (entityComboBox.getSelectedIndex() > 0 && categoryComboBox.getSelectedIndex() > 0) {
            var measures = metricManager.getMeasures(
                                (Entity) entityComboBox.getSelectedItem(),
                                (MetricCategory) categoryComboBox.getSelectedItem());
        
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
        entityComboBox.addItem(null);
        
        MetricCategory category = null;
        if (categoryComboBox.getSelectedIndex() > 0) {
            category = (MetricCategory) categoryComboBox.getSelectedItem();
        }

        // Get all unique entities from all metrics
        var entities = metricManager.getEntities(category);
        
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
        categoryComboBox.addItem(null);
        
        // Get all unique categories from all metrics
        Entity entity = null;
        if (entityComboBox.getSelectedIndex() > 0) {
            entity = (Entity) entityComboBox.getSelectedItem();
        }
        
        var categories = metricManager.getCategories(entity);
        for (var category : categories) {
            categoryComboBox.addItem(category);
        }
        categoryComboBox.setSelectedItem(original);
    }
    
    /**
     * Updates the enabled state of the control buttons based on selected tab.
     * And refresh selection
     */
    private void updateButtonState() {
        int selected = tabbedPane.getSelectedIndex();
        if (selected >= 0) {
            refreshChartTab(tabbedPane.getSelectedComponent());
        }

        boolean isTabSelected = selected >= 0;
        removeButton.setEnabled(isTabSelected);
        addButton.setEnabled(isTabSelected);
    }

    /**
     * Gets the currently selected Metric based on dropdown selections.
     * @return
     */
    private Metric getSelectedMetric() {
        var entityObj = (Entity)entityComboBox.getSelectedItem();
        var category = (MetricCategory) categoryComboBox.getSelectedItem();
        var measure = (String) measureComboBox.getSelectedItem();
        
        if (entityObj != null && category != null && !SELECT_PROMPT.equals(measure)) {
            
            Entity entity = (Entity) entityObj;
            
            // Find selected Metrix
            return metricManager.getMetric(entity, category, measure);
        }
        return null;
    }
        
    /**
     * Adds the selected metric to the currently selected chart tab.
     */
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
            chart.setTitle(dataset.getTitle());
            measureComboBox.setSelectedIndex(0);
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
            dataset.setCumulative(cummulative.isSelected());
            if (!dataset.addMetric(data)) {
                // Metric already present, do not add duplicate chart
                return;
            }

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
            updateButtonState();
            
            // Reset selections to encourage creating different charts
            measureComboBox.setSelectedIndex(0); // Reset to "-- Select --"
        }
    }
    
    /**
     * Removes the currently selected chart tab.
     */
    private void removeSelectedChart() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex >= 0) {
            tabbedPane.removeTabAt(selectedIndex);
            updateButtonState();
        }
    }
    
    private JFreeChart createMetricChart(MetricDataset dataset) {

        var timeAxis = new NumberAxis("MSol");
        timeAxis.setNumberFormatOverride(new MarsTimeFormatter());
        timeAxis.setAutoRangeIncludesZero(false);

        var valueAxis = new NumberAxis("Values");
        valueAxis.setAutoRangeIncludesZero(false);  // override default

        var renderer = new XYLineAndShapeRenderer(true, shapes.isSelected());
        renderer.setDefaultToolTipGenerator(new MetricToolTipGenerator());
        XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, renderer);

        return new JFreeChart(dataset.getTitle(), JFreeChart.DEFAULT_TITLE_FONT,
                plot, true);
    }

    /**
     * Remove the listener on Metric manager.
     */
    @Override
    public void destroy() {
        super.destroy();
        metricManager.removeListener(this);
    }
    	
	/**
	 * Updates metric views by refreshing the visible chart
	 * 
	 * @param pulse Clock step advancement
	 */
    @Override
	public void clockUpdate(ClockPulse pulse) {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex >= 0) {
            refreshChartTab(tabbedPane.getSelectedComponent());
        }
    }

    /**
     * Trigger a refresh of the given chart tab panel
     * @param tabPanel
     */
    private void refreshChartTab(Component tabPanel) {
        JFreeChart chart = ((ChartPanel) tabPanel).getChart();
        XYPlot plot = chart.getXYPlot();
        var dataset = (MetricDataset) plot.getDataset();
        dataset.refresh();
	}

    @Override
    public void newMetric(Metric m) {
        populateCategoryComboBox();
        populateEntityComboBox();
    }
}