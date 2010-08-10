/**
 * Mars Simulation Project
 * BarChartTab.java
 * @version 3.00 2010-08-10
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.jfree.data.AbstractSeriesDataset;
import org.jfree.data.CategoryDataset;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;

/**
 */
class BarChartTab extends MonitorTab {

    public final static Icon BARICON = ImageLoader.getIcon("BarChart");

    private final static int MAXLABEL = 12;         // Maximum label length
    private final static int COLUMNWIDTH = 4;
    private final static int LABELWIDTH = 8;
    private final static int SCROLLTHRESHOLD = 400; // Large non-scroll chart
    
    // Minimum time (milliseconds) between chart updates based on table
    // update events.
    private static final long MIN_TIME_BETWEEN_UPDATES = 1000L;

    /**
     *  Basic Bar Dataset to map a table model onto a Category Data set for
     *  display on the Bar chart. The Categories are mapped onto the individual
     *  rows of the Table Model. The Series are the columns specified to be
     *  displayed.
     */
    class TableBarDataset extends AbstractSeriesDataset
            implements CategoryDataset, TableModelListener {
        
        private TableModel model;
        private int[] columns;
        private List<String> categories;
        private long lastUpdateTime;

        public TableBarDataset(TableModel model, int columns[]) {
            setModel(model);
            setColumns(columns);
        }
     
        /**
          * Get the number of series displayed in chart. This is the number of
          * columns mapped in the table model.
          *
          * @return Number of series supported.
          */
        public int getSeriesCount() {
            return columns.length;
        }

        /**
         * The series names are mapped onto the column names.
         *
         * @param series Index of the series.
         * @return Name of the Series.
         */
        public String getSeriesName(int series) {
            return model.getColumnName(columns[series]);
        }

        /**
         * Return the list of displayed categories.
         *
         * @return List of String that represent Unit names.
         */
        public List<String> getCategories() {
            return categories;
        }

        /**
         * How many categories are displayed in this model. This is the number
         * of units in the model.
         *
         * @return Number of loaded categories.
         */
        public int getCategoryCount() {
            return categories.size();
        }

        /**
         * Load the categories that this model displays. These are the labels
         * of the rows in the model, i.e. the first column.
         */
        private void loadCategories() {
            categories = new ArrayList<String>(model.getRowCount());

            // Iterate the rows and add the value from the first cell.
            for(int i = 0; i < model.getRowCount(); i++) {
                String value = (String) model.getValueAt(i, 0);
                if ((value != null) && (value.length() > MAXLABEL)) {
                    value = value.substring(0, MAXLABEL-2) + "..";
                }
                categories.add(value);
            }
        }
        
        /**
         * Get an individual value of a series and category. The Series is
         * mapped onto a column, the category is mapped onto a row.
         *
         * @param category Category value.
         * @param series Series index.
         * @return Numeric value of the model cell.
         */
        public Number getValue(int series, String category) {
            int rowId = categories.indexOf(category);
            return (Number)model.getValueAt(rowId, columns[series]);
        }

        /**
         * Redefine the columns mapped into this dataset. Each column maps
         * onto a different Series in the model.
         *
         * @param newcolumns Indexes in the source model.
         */
        public void setColumns(int newcolumns[]) {
            columns = new int[newcolumns.length];
            for(int i = 0; i < newcolumns.length; i++) columns[i] = newcolumns[i];

            fireDatasetChanged();
        }
        
        public int getRowIndex(Comparable key) {
            int result = -1;
            
            if (key instanceof String) {
                String keyStr = (String) key;
                for (int x=0; x < columns.length; x++) {
                    if (model.getColumnName(columns[x]).equals(keyStr)) result = x;
                }
            }
            
            return result;    
        }
        
        public int getRowCount() {
            return columns.length;
        }
        
        public Comparable getRowKey(int index) {
            return model.getColumnName(columns[index]);
        }
        
        public List<String> getRowKeys() {
            List<String> result = new ArrayList<String>();
            for (int x=0; x < columns.length; x++) {
                result.add(model.getColumnName(columns[x]));
            }
            
            return result;
        }
        
        public Comparable getColumnKey(int index) {
            return (Comparable) getCategories().get(index);
        }
        
        public List getColumnKeys() {
            return getCategories();
        }
        
        public int getColumnIndex(Comparable key) {
            int result = -1;
            
            for (int x=0; x < getCategories().size(); x++) {
                if (key.equals(getCategories().get(x))) result = x;
            }
            
            return result;
        }
        
        public int getColumnCount() {
            return getCategoryCount();
        }
        
        public Number getValue(int row, int column) {
            return (Number) model.getValueAt(column, columns[row]);
        }
        
        public Number getValue(Comparable rowKey, Comparable columnKey) {
            int rowIndex = getRowIndex(rowKey);
            int columnIndex = getColumnIndex(columnKey);
            return getValue(rowIndex, columnIndex);
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
                loadCategories();
            }
        }

        /**
         * The underlying model has changed.
         */
        public void tableChanged(TableModelEvent e) {
            if ((e.getType() == TableModelEvent.INSERT) ||
                (e.getType() == TableModelEvent.DELETE)) {
                loadCategories();
                fireDatasetChanged();
            }
            else if (e.getColumn() == TableModelEvent.ALL_COLUMNS) fireDatasetChanged();
            else {
            	boolean dataChanged = false;
            	for (int x=0; x < columns.length; x++) {
            		if (columns[x] == e.getColumn()) dataChanged = true;
            	}
            	if (dataChanged) {
            		long time = System.nanoTime() / 1000000L;
                	if ((time - lastUpdateTime) > MIN_TIME_BETWEEN_UPDATES) {
                		lastUpdateTime = time;
                		fireDatasetChanged();
                	}
            	}
            }
        }
    }

    private TableBarDataset barModel = null;
    private JFreeChart chart = null;

    /**
     * Create a PieChart view that displays the data in a particular column.
     *
     * @param model Data source.
     * @param columns Indexes of columns to display.
     */
    public BarChartTab(MonitorModel model, int []columns) {
        super(model, false, BARICON);

        String title = model.getName();
        setName(title);

        barModel = new TableBarDataset(model, columns);
        chart = ChartFactory.createBarChart(null, null, null, barModel, PlotOrientation.VERTICAL, true, false, false);
        Plot plot = chart.getPlot();
        
        // Set bottom labels to vertical.
        CategoryAxis hAxis = ((CategoryPlot) plot).getDomainAxis();
        CategoryLabelPosition defaultPos = new CategoryLabelPosition();
        CategoryLabelPosition verticalPosition = new CategoryLabelPosition(defaultPos.getCategoryAnchor(), 
            defaultPos.getLabelAnchor(), defaultPos.getRotationAnchor(), (3D * Math.PI / 2D));
        hAxis.setBottomCategoryLabelPosition(verticalPosition);

        // Estimate the width of the chart by multipling the categories by the
        // number of series. First calculate the column width as this is
        // dependent upon the categories, it can not be smaller than the
        // label width.
        int columnWidth = barModel.getSeriesCount() * COLUMNWIDTH;
        if (columnWidth < LABELWIDTH) {
            columnWidth = LABELWIDTH;
        }

        // Create a panel for chart
        JComponent panel = new ChartPanel(chart);
        chart.setBackgroundPaint(getBackground());

        // Check the width for possible scrolling
        int chartwidth = columnWidth * barModel.getCategoryCount();

        if (chartwidth > SCROLLTHRESHOLD) {
            // Scrolling will kick in, then fix the hieght so that it
            // automatically adjusts to Scroll Viewport hieght; the width
            // fix so that label are not too compressed.
            Dimension preferredSize = new Dimension(chartwidth, 0);
            panel.setPreferredSize(preferredSize);
            panel = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }

        add(panel, "Center");
    }


    /**
     * Display the properties dialog that allows the data displayed to be
     * configured.
     *
     * @param desktop main window of simulation.
     */
    public void displayProps(MainDesktopPane desktop) {

        // Show modal column selector
        int columns[] = ColumnSelector.createBarSelector(desktop.getMainWindow(), getModel());
        if (columns.length > 0) {
            barModel.setColumns(columns);
        }
    }

    protected List getSelection() {
        return new ArrayList();
    }

    /**
     * The tab has been removed.
     */
    public void removeTab() {
        chart = null;
        barModel.setModel(null);
        barModel = null;
    }
}