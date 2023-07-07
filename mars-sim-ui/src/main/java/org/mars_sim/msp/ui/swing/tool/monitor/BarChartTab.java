/*
 * Mars Simulation Project
 * BarChartTab.java
 * @date 2021-09-20
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractSeriesDataset;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

@SuppressWarnings("serial")
class BarChartTab
extends MonitorTab {

	/** Maximum label length. */
	private final static int MAXLABEL = 20; //12;
	private final static int COLUMNWIDTH = 25;//4;
	private final static int LABELWIDTH = 15;//8;
	/** Large non-scroll chart. */
	private final static int SCROLLTHRESHOLD = 800; // 400

	public static final String ICON = "bar";

	/**
	 * Minimum time (milliseconds) between chart updates
	 * based on table update events.
	 */
	private static final long MIN_TIME_BETWEEN_UPDATES = 1000L;

	/**
	 *  Basic Bar Data set to map a table model onto a Category Data set for
	 *  display on the Bar chart. The Categories are mapped onto the individual
	 *  rows of the Table Model. The Series are the columns specified to be
	 *  displayed.
	 */
	@SuppressWarnings("rawtypes")
	static class TableBarDataset
	extends AbstractSeriesDataset
	implements CategoryDataset, TableModelListener {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

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
			return model.getColumnName(columns[series]);// + "(" + getRowCount() + ")" ; ?
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
			categories = new ArrayList<>(model.getRowCount());

			// Iterate the rows and add the value from the first cell.
			for (int i = 0; i < model.getRowCount(); i++) {
				String value = (String) model.getValueAt(i, 0);
//				int num = model.getColumnCount();
				if ((value != null) && (value.length() > MAXLABEL)) {
					// "(" + num + ") " +
					value = value.substring(0, MAXLABEL-2) + ".."; //$NON-NLS-1$
				}
				categories.add(value);
			}
		}

		/**
		 * Get an individual value of a series and category. The Series is
		 * mapped onto a column, the category is mapped onto a row.
		 * @param category Category value.
		 * @param series Series index.
		 * @return Numeric value of the model cell.
		 */
		public Object getValue(int series, String category) {
			int rowId = categories.indexOf(category);
			return model.getValueAt(rowId, columns[series]);
		}

		/**
		 * Redefine the columns mapped into this dataset. Each column maps
		 * onto a different Series in the model.
		 * @param newcolumns Indexes in the source model.
		 */
		public void setColumns(int newcolumns[]) {
			columns = new int[newcolumns.length];
			System.arraycopy(newcolumns, 0, columns, 0, newcolumns.length);
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

		public Comparable<?> getRowKey(int index) {
			return model.getColumnName(columns[index]);
		}

		public List<String> getRowKeys() {
			List<String> result = new ArrayList<String>();
			for (int column : columns) {
				result.add(model.getColumnName(column));
			}
			return result;
		}

		public Comparable getColumnKey(int index) {
			return categories.get(index);
		}

		public List getColumnKeys() {
			return categories;
		}

		public int getColumnIndex(Comparable key) {
			int result = -1;

			for (int x=0; x < categories.size(); x++) {
				if (key.equals(categories.get(x))) result = x;
			}
			return result;
		}

		public int getColumnCount() {
			return getCategoryCount();
		}

		public Number getValue(int row, int column) {
			Object obj = model.getValueAt(column, columns[row]);
			if (obj instanceof Number) {
				return (Number)obj;
			}
			return (Number) Double.valueOf((String)obj);
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
				 for (int column : columns) {
					 if (column == e.getColumn()) dataChanged = true;
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

		@Override
		public Comparable getSeriesKey(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private TableBarDataset barModel = null;
	private JFreeChart chart = null;

	//private JComponent chartpanel;

	/**
	 * Create a PieChart view that displays the data in a particular column.
	 * @param model Data source.
	 * @param columns Indexes of columns to display.
	 */
	public BarChartTab(MonitorModel model, int []columns) {
		super(model, false, false, ImageLoader.getIconByName(ICON));

		String title = model.getName();
		setName(title);

		barModel = new TableBarDataset(model, columns);
		chart = ChartFactory.createBarChart(null, null, null, barModel, PlotOrientation.VERTICAL, true, true, false);

		// Limits the size of the bar to 35% if there are only very few category
		//BarRenderer3D renderer = (BarRenderer3D) chart.getCategoryPlot().getRenderer();
		BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
		renderer.setMaximumBarWidth(.1); // set maximum width to 10% of chart
		//renderer.setItemMargin(-1);

		Plot plot = chart.getPlot();

		// Adds set the range axis
		final ValueAxis rangeAxis = ((CategoryPlot) plot).getRangeAxis();//.getRangeAxis();
		rangeAxis.setAutoTickUnitSelection(true);//setStandardTickUnits//(CategoryAxis.DEFAULT_CATEGORY_MARGIN);//createIntegerTickUnits());
		rangeAxis.setTickLabelFont(new Font("Arial",Font.BOLD, 12));
		rangeAxis.setUpperMargin(0.1); // in percentage
		rangeAxis.setLowerMargin(0.05); // in percentage

		CategoryAxis domainAxis = ((CategoryPlot) plot).getDomainAxis();

		// Set the label position to go sideway at 45 deg downward
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45); // DOWN_90);
		domainAxis.setTickLabelFont(new Font("Calibri", Font.BOLD, 12));
		domainAxis.setMaximumCategoryLabelWidthRatio(1);
		//domainAxis.setMaximumCategoryLabelLines(2);
		domainAxis.setLowerMargin(0.01);
	    domainAxis.setUpperMargin(0.01);
	    domainAxis.setCategoryMargin(.5);
	    //domainAxis.setItemMargin(0.2);

		// Adds label on each bar
		//renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator()); // only work for 2D bar chart
		//renderer.setBaseItemLabelsVisible(true); // only work for 2D bar chart
		CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator();
		renderer.setSeriesItemLabelGenerator(0, generator);
		renderer.setSeriesItemLabelsVisible(0, true);
		renderer.setSeriesPositiveItemLabelPosition(0,
				new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER,
						TextAnchor.BASELINE_CENTER, 0.0));
		renderer.setItemLabelInsets(new RectangleInsets(10, 10, 10, 10));

		renderer.setDefaultToolTipGenerator(new MyToolTipGenerator());

		// Create a panel for chart
		ChartPanel chartpanel = new ChartPanel(chart);
		chart.setBackgroundPaint(getBackground());

		// Adds zooming
		chartpanel.setFillZoomRectangle(true);
		chartpanel.setMouseWheelEnabled(true);

		// Prevents label scaling
		chartpanel.setMaximumDrawHeight(10000);
		chartpanel.setMaximumDrawWidth(10000);
		chartpanel.setMinimumDrawWidth(20);
		chartpanel.setMinimumDrawHeight(20);

		// Estimate the width of the chart by multiplying the categories by the
		// number of series. First calculate the column width as this is
		// dependent upon the categories, it can not be smaller than the
		// label width.
		int columnWidth = barModel.getSeriesCount() * COLUMNWIDTH;

		if (columnWidth < LABELWIDTH) {
			columnWidth = LABELWIDTH;
		}
		// Check the width for possible scrolling
		int chartwidth = columnWidth * barModel.getCategoryCount();

		// Decide what to add
		JComponent comp = chartpanel;
		if (chartwidth > SCROLLTHRESHOLD) {
			// Scrolling will kick in, then fix the height so that it
			// automatically adjusts to Scroll Viewport height; the width
			// fix so that label are not too compressed.
			Dimension preferredSize = new Dimension(chartwidth, 0);
			chartpanel.setPreferredSize(preferredSize);
			comp = new JScrollPane(chartpanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}


		comp.setPreferredSize(new Dimension(800, 0));
		add(comp, BorderLayout.CENTER);
	}


    // Adds custom tooltip generator
    private class MyToolTipGenerator extends StandardCategoryToolTipGenerator {
        @Override
        public String generateToolTip(CategoryDataset dataset, int row, int column) {
            return 	"(1) Left click and drag a range to magnify "
            		+ "(2) Rotate mouse wheel to zoom in/out "
            		+ "(3) Right click to fully customize chart"
                + super.generateToolTip(dataset,row,column);
        }
    }


	/**
	 * Display the properties dialog that allows the data displayed to be
	 * configured.
	 *
	 * @param desktop main window of simulation.
	 */
	public void displayProps(MainDesktopPane desktop) {
        //System.out.println("BarChartTab.java : start calling displayProp()");
		// Show modal column selector
		int columns[] = ColumnSelector.createBarSelector(desktop, getModel());
		if (columns.length > 0) {
			barModel.setColumns(columns);
		}
	}

	/**
	 * The tab has been removed.
	 */
	@Override
	public void removeTab() {
		chart = null;
		barModel.setModel(null);
		barModel = null;
		super.removeTab();
	}
}
