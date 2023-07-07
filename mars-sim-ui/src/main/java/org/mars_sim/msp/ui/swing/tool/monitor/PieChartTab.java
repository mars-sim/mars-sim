/*
 * Mars Simulation Project
 * PieChartTab.java
 * @date 2021-11-14
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.util.Rotation;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.PieDataset;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * This class presents a Pie chart as a Monitor tab. The data for the Pie
 * chart is extracted from a TableModel. The data from a single column is
 * collated into a category based on the distinct values in the column.
 *
 * The column specified should ideally not return a Number value since the
 * algorithm works best on a distinct data set values.
 */
@SuppressWarnings("serial")
class PieChartTab extends MonitorTab {

    /**
     * Minimum time (milliseconds) between chart updates
     * based on table update events.
     */
    private static final long MIN_TIME_BETWEEN_UPDATES = 1000L;

    /** The category name for unknown. */
    private final static String NONECAT = "None";

	public static final String ICON = "pie";

    /**
     * Basic Pie Data set with a method to recalculate.
     */
    static class TablePieDataset
    extends AbstractDataset
    implements PieDataset<String>, TableModelListener {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

        private TableModel model;
        private int column;
		private Map<String, Integer> dataMap;
        private long lastUpdateTime;

        public TablePieDataset(TableModel model, int column) {
            this.column = column;
            dataMap = Collections.synchronizedMap(new LinkedHashMap<String, Integer>(model.getRowCount()));
            setModel(model);
        }

        /**
         * Examines the associated model and recreates the internal hashmap of
         * values according to the values in one column.
         */
        void calculate() {

        	long time = System.nanoTime() / 1000000L;
        	if ((time - lastUpdateTime) > MIN_TIME_BETWEEN_UPDATES) {
        		lastUpdateTime = time;

        		int rows = model.getRowCount();

        		Map<String, Integer> tempMap = Collections.synchronizedMap(new LinkedHashMap<String, Integer>(dataMap));

        		// Clear the temp map.
        		Iterator<String> iter = tempMap.keySet().iterator();
        		while (iter.hasNext()) tempMap.put(iter.next(), 0);


        		// Add category values and categories.
        		for(int i = 0; i < rows; i++) {

                    Object catValue = model.getValueAt(i, column);
        			String category = null;
        			if (catValue == null) {
                        category = NONECAT;
                    }
                    else if (catValue instanceof String s) {
                        category = s.trim();
                    }
        			else {
                        category = catValue.toString();
                    }
        			Integer value = tempMap.get(category);
        			int count = 1;
        			if (value != null) count = value + 1;

        			// Put updated value in data map.
        			tempMap.put(category, count);
        		}

        		if (!dataMap.equals(tempMap)) {
        			dataMap.clear();
        			dataMap = tempMap;
        			fireDatasetChanged();
        		}
        		else tempMap.clear();
        	}
        }


        /**
         * Set the column that is displayed in the Pie chart. It results in
         * the values being recalculated.
         *
         * @param column New column index in the table model.
         */
        public void setColumn(int column) {
            this.column = column;

            calculate();
        }

        /**
         * Specify the model to monitor. This class will attach itself as
         * a listener. If a model is already attached, this one will be
         * detached before this.
         * This action triggers the reloading of the categories.
         * @param newModel New table model to monitor.
         */
        public void setModel(TableModel newModel) {
            if (model != null) {
                model.removeTableModelListener(this);
            }

            model = newModel;
            if (model != null) {
                model.addTableModelListener(this);
                calculate();
            }
        }

        /**
         * The underlying model has changed.
         */
        public void tableChanged(TableModelEvent e) {
            calculate();
        }

        /**
         * Returns the index for a given key.
         * @see org.jfree.data.KeyedValues#getIndex(Comparable)
         *
         * @param key the key.
         * @return the index.
         */
        @Override
        public int getIndex(String key) {
            int result = -1;

            Set<String> keys = dataMap.keySet();
            if (keys.contains(key)) {
                int count = 0;
                Iterator<String> i = keys.iterator();
                while (i.hasNext()) {
                    if (key == i.next()) result = count;
                    else count++;
                }
            }

            return result;
        }

        /**
         * Returns the value (possibly null) for a given key.
         * If the key is not recognized, the method should return null.
         * @see org.jfree.data.KeyedValues#getValue(Comparable)
         *
         * @param key the key.
         * @return the value.
         */
        @Override
        public Number getValue(String key) {
            return dataMap.get(key);
        }

        /**
         * Returns the number of items (values) in the collection.
         * @see org.jfree.data.Values#getItemCount()
         *
         * @return the item count.
         */
        public int getItemCount() {
            return dataMap.size();
        }

        /**
         * Returns a value.
         * @see org.jfree.data.Values#getValue(int)
         *
         * @param item the item of interest (zero-based index).
         * @return the value.
         */
        public Number getValue(int item) {
            Number result = null;

            Object[] keys = dataMap.keySet().toArray();
            if (item < keys.length) result = dataMap.get(keys[item]);

            return result;
        }

        /**
         * Returns the key associated with an item (value).
         * @see org.jfree.data.KeyedValues#getKey(int)
         *
         * @param index the item index (zero-based).
         * @return the key.
         */
        @Override
        public String getKey(int index) {
            String result = null;

            Object[] keys = dataMap.keySet().toArray();
            if (index < keys.length) result = (String) keys[index];

            return result;
        }

        /**
         * Returns the keys.
         * @see org.jfree.data.KeyedValues#getKeys()
         *
         * @return the keys.
         */
        @Override
        public List<String> getKeys() {
            return new ArrayList<>(dataMap.keySet());
        }
    }

    private TablePieDataset pieModel = null;
    private JFreeChart chart = null;
    private ChartPanel chartpanel = null;

    /**
     * Create a PieChart view that displays the data in a particular column.
     *
     * @param model Data source.
     * @param column Index of the column to collate.
     */
    public PieChartTab(MonitorModel model, int column) {
        super(model, false, false, ImageLoader.getIconByName(ICON));

        String title = model.getName() + " - " + model.getColumnName(column);
        setName(title);

        pieModel = new TablePieDataset(model, column);

        chart = ChartFactory.createPieChart(null, pieModel, true, true, false);
        chart.setBackgroundPaint(getBackground());

        pieModel.calculate();

        // Use pie
        Plot rawPlot = chart.getPlot();
        if (rawPlot instanceof PiePlot plot) {
            plot.setStartAngle(270);
            plot.setDirection(Rotation.ANTICLOCKWISE);
            plot.setForegroundAlpha(0.6f);

        }
        pieModel.addChangeListener(rawPlot);


        chartpanel = new ChartPanel(chart, true);

        // Add setting below to keep the aspect ratio of 8:5
        // see http://www.jfree.org/forum/viewtopic.php?f=3&t=115763
        // Chart will always be drawn to an off-screen buffer that is the same size as the ChartPanel, so no scaling will happen when the offscreen image is copied to the panel.
        chartpanel.setPreferredSize(new Dimension (800, 300));
        chartpanel.setMinimumDrawWidth(0);
        chartpanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartpanel.setMinimumDrawHeight(0);
        chartpanel.setMaximumDrawHeight(Integer.MAX_VALUE);

		JPanel fixedSizePane = new JPanel(new FlowLayout());
		fixedSizePane.add(chartpanel);
		fixedSizePane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
            	int w = fixedSizePane.getWidth();
                int h = fixedSizePane.getHeight();
                int size =  Math.min(w, h);
                int newWidth = (int) (size *8D/3D);
                chartpanel.setPreferredSize(new Dimension(newWidth, size));
                fixedSizePane.revalidate();
            }
        });

        add(fixedSizePane, BorderLayout.CENTER);
    }

    /**
     * Display the properties dialog that allows the data displayed to be
     * configured.
     *
     * @param desktop main window of simulation.
     */
    public void displayProps(MainDesktopPane desktop) {
        // Show modal column selector
        int column = ColumnSelector.createPieSelector(desktop, getModel());
        if (column >= 0) {
            setColumn(column);
        }

    }

    /**
     * Define which column is used to generate the Pie data set.
     *
     * @param column New column index in table model.
     */
    private void setColumn(int column) {
        pieModel.setColumn(column);

        // Update the titles, JFreeChart supports multiple titles.
        String title = getModel().getName() + " - " +
                       getModel().getColumnName(column);
        setName(title);
    }

    /**
     * The tab has been removed.
     */
    @Override
    public void removeTab() {
        chart = null;
        pieModel.setModel(null);
        pieModel = null;
        super.removeTab();
    }
}