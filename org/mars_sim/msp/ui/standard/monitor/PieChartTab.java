/**
 * Mars Simulation Project
 * PieChartView.java
 * @version 2.74 2002-01-21
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.ui.standard.ImageLoader;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import javax.swing.Icon;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import com.jrefinery.data.DefaultPieDataset;
import com.jrefinery.chart.JFreeChart;
import com.jrefinery.chart.JFreeChartPanel;
import com.jrefinery.chart.ChartFactory;
import com.jrefinery.chart.PiePlot;

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
     * The percentage below which segment goes to others
     */
    private final static int OTHERPERC = 2;

    /**
     * The category name for others
     */
    private final static String OTHERCAT = "Others less than " + OTHERPERC + "%";

    /**
     * The category name for unknwown
     */
    private final static String NONECAT = "None";

    /**
     *  Basic Pie Dataset with a method to recalculate.
     */
    class TablePieDataset extends DefaultPieDataset
            implements TableModelListener {

        private TableModel model;
        private int column;

        public TablePieDataset(TableModel model, int column) {
            this.column = column;
            setModel(model);
        }

        /**
         * Examines the assoicated modle and recreats the internal hashmap
         * of values according to the values in one column. Any values which
         * are less than OTHERPERC are combined into a single entry.
         *
         */
        void calculate() {
            int rows = model.getRowCount();

            // Must clear incase values have disappeared
            TreeMap newData = new TreeMap();
            for(int i = 0; i < rows; i++) {
                // The TreeMap uses a Comparable operations on the keys
                // so they must implement this interface and it can not contain
                // nulls. To bypass this, all categories are converted to
                // strings
                Object category = model.getValueAt(i, column);
                if (category == null) {
                    category = NONECAT;
                }
                else if (!(category instanceof String)) {
                    category = category.toString();
                }


                Integer value = (Integer)newData.get(category);
                int count = 1;
                if (value != null) {
                    count = value.intValue() + 1;
                }

                // Put updated value back
                newData.put(category, new Integer(count));
            }

            // Knock off the small percentages. Copy the keys to avoid
            // concurrent modification problems.
            int smallSize = (OTHERPERC * rows)/100;
            Iterator catagories = new HashSet(newData.keySet()).iterator();
            while(catagories.hasNext()) {
                Object key = catagories.next();
                Integer value = (Integer)newData.get(key);

                // If it is less than a percentage, then remove.
                if (!key.equals(OTHERCAT) && (value.intValue() < smallSize)) {
                    newData.remove(key);

                    // Add it to the Other catagory
                    int otherscount = value.intValue();
                    Integer othersValue = (Integer)newData.get(OTHERCAT);
                    if (othersValue != null) {
                        otherscount += othersValue.intValue();
                    }
                    newData.put(OTHERCAT, new Integer(otherscount));
                }
            }

            // Update if needed
            if (!newData.equals(data)) {
                data = newData;
                fireDatasetChanged();
            }
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
    }

    private TablePieDataset pieModel = null;
    private JFreeChart chart = null;
    private JFreeChartPanel chartpanel = null;

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
        chart = ChartFactory.createPieChart(null, pieModel, true);
        chart.setChartBackgroundPaint(getBackground());
        pieModel.calculate();

        // then customise it a little...
        PiePlot plot = (PiePlot)chart.getPlot();
        plot.setCircular(false);
        plot.setRadiusPercent(0.60);
        plot.setSectionLabelType(PiePlot.PERCENT_LABELS);

        chartpanel = new JFreeChartPanel(chart);
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
        return new ArrayList();
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
        super.removeTab();
    }
}
