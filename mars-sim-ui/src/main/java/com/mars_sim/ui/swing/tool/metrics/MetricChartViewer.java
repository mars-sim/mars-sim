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
import java.util.HashMap;
import java.util.Map;

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
import com.mars_sim.core.metrics.MetricKey;
import com.mars_sim.core.metrics.MetricManager;
import com.mars_sim.core.metrics.MetricManagerListener;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.utils.NamedListCellRenderer;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * A Swing component that displays multiple JFreeChart line charts in a tabbed pane.
 * Users can add and remove chart tabs using dropdown controls for Entity, Category, and Measure.
 */
@SuppressWarnings("serial")
public class MetricChartViewer extends ContentPanel
        implements MetricManagerListener {
    
    // Constants
    public static final String NAME = "metricsviewer";
    public static final String TITLE = "Metric Chart Viewer";
    public static final String ICON = "metrics";

    private static final String SELECT_PROMPT = "-- Select --";
    private static final String ALL_MEASURES = "ALL";  // Token value for all measures
    
    private transient MetricManager metricManager;
    private JTabbedPane tabbedPane;
    private JComboBox<Entity> entityComboBox;
    private JComboBox<MetricCategory> categoryComboBox;
    private JComboBox<String> measureComboBox;
    private JButton showMetric;
    private JButton removeButton;
    private JButton addButton;
    private JCheckBox cummulative;
    private JCheckBox shapes;
    private JButton showCategory;
    private Map<MetricKey, MetricDataset> categoryToDataset = new HashMap<>();
        
    /**
     * Creates a new MetricChartViewer component with the specified MetricManager.
     * 
     * @param metricManager The MetricManager to use for populating dropdowns and retrieving metrics
     */
    public MetricChartViewer(MetricManager metricManager) {
        super(NAME, TITLE, Placement.CENTER);

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
        showMetric.setEnabled(false);
        showCategory.setEnabled(false);
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
        
        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));

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
         
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));

        showMetric = new JButton("Show Metric");
        showMetric.addActionListener(e -> showMetricNewChart());
        showCategory = new JButton("Show Category");
        showCategory.addActionListener(e -> showCategoryNewChart());
        addButton = new JButton("Add To Chart");
        addButton.addActionListener(e -> addMeasureToChart());
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
        controlPanel.add(showMetric);
        controlPanel.add(addButton);
        controlPanel.add(showCategory);
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

        boolean enableNewChartButtons = false;
        if (entityComboBox.getSelectedIndex() > 0 && categoryComboBox.getSelectedIndex() > 0) {
            var measures = metricManager.getMeasures(
                                (Entity) entityComboBox.getSelectedItem(),
                                (MetricCategory) categoryComboBox.getSelectedItem());
        
            for (var m : measures) {
                measureComboBox.addItem(m);
            }
            measureComboBox.setSelectedIndex(0);
            enableNewChartButtons = true;
        }

        showCategory.setEnabled(enableNewChartButtons);
        showMetric.setEnabled(enableNewChartButtons);
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
                        
            // Find selected Metrix
            return metricManager.getMetric(entityObj, category, measure);
        }
        return null;
    }

    /**
     * Adds the selected metric to the currently selected chart tab.
     */
    private void addMeasureToChart() {
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
     * Show a new chart for the selected Metric
     */
    private void showMetricNewChart() {
        var data = getSelectedMetric();
        if (data != null) {
            // Create the chart
            var dataset = new MetricDataset();
            dataset.setCumulative(cummulative.isSelected());
            dataset.addMetric(data);

            buildNewChart(dataset);
           
            // Reset selections to encourage creating different charts
            measureComboBox.setSelectedIndex(0); // Reset to "-- Select --"
        }
    }
        
    /**
     * Show a new chart for the selected Category
     */
    private void showCategoryNewChart() {
        var entityObj = (Entity)entityComboBox.getSelectedItem();
        var category = (MetricCategory) categoryComboBox.getSelectedItem();
        
        // Make the category is selected and there are measures
        if (entityObj == null || category == null || measureComboBox.getModel().getSize() == 0) {
            return;
        }

        // Create the dataset
        var dataset = new MetricDataset();
        dataset.setCumulative(cummulative.isSelected());

        // add all known measures
        for(var m : metricManager.getMeasures(entityObj, category)) {
            // Find selected Metrix
            var metric = metricManager.getMetric(entityObj, category, m);
            dataset.addMetric(metric);
        }

        buildNewChart(dataset);

        // Add to map so new Measure are auto added
        categoryToDataset.put(new MetricKey(entityObj, category, ALL_MEASURES), dataset);
    }
    
    private void buildNewChart(MetricDataset dataset) {
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
    }

    /**
     * Removes the currently selected chart tab.
     */
    private void removeSelectedChart() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex >= 0) {
            var selection = tabbedPane.getTabComponentAt(selectedIndex);
            tabbedPane.removeTabAt(selectedIndex);
            updateButtonState();

            // Remove from categoryToDataset map if it was a category chart
            ChartPanel chartPanel = (ChartPanel) selection;
            JFreeChart chart = chartPanel.getChart();
            XYPlot plot = chart.getXYPlot();
            var dataset = (MetricDataset) plot.getDataset();

            // Check if the dataset is in the map and remove it
            for(var entry : categoryToDataset.entrySet()) {
                if (entry.getValue() == dataset) {
                    categoryToDataset.remove(entry.getKey());
                    break;
                }
            }
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

    /**
     * New metric added so update the dropdowns to include it.
     * Also check if the new Metric belongs to an existing Chart
     */
    @Override
    public void newMetric(Metric m) {
        populateCategoryComboBox();
        populateEntityComboBox();

        // Check if the new metric belongs to an existing chart for the same category
        var pseudoKey = new MetricKey(m.getKey().asset(), m.getKey().category(), ALL_MEASURES);
        var dataset = categoryToDataset.get(pseudoKey);
        if (dataset != null) {
            dataset.addMetric(m);
        }
    }
}