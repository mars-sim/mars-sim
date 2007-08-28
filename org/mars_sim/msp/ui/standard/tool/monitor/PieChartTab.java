/**
 * Mars Simulation Project 
 * PieChartView.java
 * @version 2.81 2007-08-27
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.tool.monitor;

import java.util.*;

import javax.swing.Icon;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.mars_sim.msp.ui.standard.ImageLoader;
import org.mars_sim.msp.ui.standard.MainDesktopPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.AbstractDataset;
import org.jfree.data.PieDataset;

/**
 * This class presents a Pie chart as a Monitor tab. The data for the Pie
 * chart is extracted from a TableModel. The data from a single column is
 * collated into a category based on the distinct values in the column.
 *
 * The column specified should idealy not return a Number value since the
 * alogrithm works best on a distinct data set values.
 */
class PieChartTab extends MonitorTab {

    public final static Icon PIEICON = ImageLoader.getIcon("PieChart");

    /**
     * The category name for unknwown
     */
    private final static String NONECAT = "None";

    /**
     *  Basic Pie Dataset with a method to recalculate.
     */
    class TablePieDataset extends AbstractDataset
            implements PieDataset, TableModelListener {

        private TableModel model;
        private int column;
        private Map<Comparable, Integer> dataMap;

        public TablePieDataset(TableModel model, int column) {
            this.column = column;
            dataMap = Collections.synchronizedMap(new LinkedHashMap<Comparable, Integer>(model.getRowCount()));
            setModel(model);
        }

        /**
         * Examines the associated model and recreates the internal hashmap of
         * values according to the values in one column.
         */
        void calculate() {
            
            int rows = model.getRowCount();

            Map<Comparable, Integer> tempMap = Collections.synchronizedMap(new LinkedHashMap<Comparable, Integer>(dataMap));
            
            // Clear the temp map.
            Iterator<Comparable> iter = tempMap.keySet().iterator();
            while (iter.hasNext()) tempMap.put(iter.next(), 0);
                

            // Add category values and categories.
            for(int i = 0; i < rows; i++) {
                
                Comparable category = (Comparable) model.getValueAt(i, column);
                if (category == null) category = NONECAT;
                else if (!(category instanceof String)) category = category.toString();
                if (((String) category).trim().equals("")) category = "None";

                Integer value = tempMap.get(category);
                int count = 1;
                if (value != null) count = value.intValue() + 1;

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
        

        /**
         * Set the column that is displayed in the Pie chart. It results in
         * this values being recalculated.
         *
         * @param column New column index in the table model.
         */
        public void setColumn(int column) {
            this.column = column;

            calculate();
        }

        /**
         * Specify the model to monitor. This class will attached itself as
         * a listener. If a model is already attached, this one will be
         * deattached before this.
         * This action triggers the reloading of the catagories.
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
         * The underlying model has changed
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
        public int getIndex(Comparable key) {
            int result = -1;
            
            Set<Comparable> keys = dataMap.keySet();
            if (keys.contains(key)) {
                int count = 0;
                Iterator<Comparable> i = keys.iterator();
                while (i.hasNext()) {
                    if (key == i.next()) result = count;
                    else count++;
                }
            }
            
            return result;
        }
        
        /**
         * Returns the value (possibly null) for a given key.
         * If the key is not recognised, the method should return null. 
         * @see org.jfree.data.KeyedValues#getValue(Comparable)
         * 
         * @param key the key.
         * @return the value.
         */
        public Number getValue(Comparable key) {
            return (Number) dataMap.get(key);
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
        public Comparable getKey(int index) {
            Comparable result = null;
            
            Object[] keys = dataMap.keySet().toArray();
            if (index < keys.length) result = (Comparable) keys[index];
            
            return result;
        }
        
        /**
         * Returns the keys.
         * @see org.jfree.data.KeyedValues#getKeys()
         * 
         * @return the keys.
         */
        public List<Comparable> getKeys() {
            List<Comparable> result = new ArrayList<Comparable>(dataMap.size());
            Iterator<Comparable> i = dataMap.keySet().iterator();
            while (i.hasNext()) result.add(i.next());
            
            return result;
        }
    }

    private TablePieDataset pieModel = null;
    private JFreeChart chart = null;
    private ChartPanel chartpanel = null;

    /**
     * Create a PieChart view that display the data in a particular column.
     *
     * @param model Data source.
     * @param column Index of the column to collate.
     */
    public PieChartTab(MonitorModel model, int column) {
        super(model, false, PIEICON);

        String title = model.getName() + " - " + model.getColumnName(column);
        setName(title);

        pieModel = new TablePieDataset(model, column);
        chart = ChartFactory.createPieChart(null, pieModel, true, true, false);
        chart.setBackgroundPaint(getBackground());
        pieModel.calculate();

        // then customise it a little...
        PiePlot plot = (PiePlot)chart.getPlot();
        plot.setCircular(false);
        plot.setRadius(0.60);
        plot.setSectionLabelType(PiePlot.PERCENT_LABELS);
        pieModel.addChangeListener(plot);

        chartpanel = new ChartPanel(chart, true);
        add(chartpanel, "Center");
    }

    /**
     * Display the properties dialog that allows the data displayed to be
     * configured.
     *
     * @param desktop main window of simulation.
     */
    public void displayProps(MainDesktopPane desktop) {

        // Show modal column selector
        int column = ColumnSelector.createPieSelector(desktop.getMainWindow(), getModel());
        if (column >= 0) {
            setColumn(column);
        }

    }

    protected List getSelection() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Define which column is used to generate the Pie dataset.
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
     * The tab has been remove
     */
    public void removeTab() {
        chart = null;
        pieModel.setModel(null);
        pieModel = null;
    }
}