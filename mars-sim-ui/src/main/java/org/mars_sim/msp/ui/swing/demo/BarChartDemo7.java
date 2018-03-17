package org.mars_sim.msp.ui.swing.demo;

/* ===========================================================
* JFreeChart : a free chart library for the Java(tm) platform
* ===========================================================
*
* (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
*
* Project Info:  http://www.jfree.org/jfreechart/index.html
*
* This library is free software; you can redistribute it and/or modify it under the terms
* of the GNU Lesser General Public License as published by the Free Software Foundation;
* either version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
* Boston, MA 02111-1307, USA.
*
* [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
* in the United States and other countries.]
*
* ------------------
* BarChartDemo7.java
* ------------------
* (C) Copyright 2004, by Object Refinery Limited and Contributors.
*
* Original Author:  David Gilbert (for Object Refinery Limited);
* Contributor(s):   -;
*
* $Id: BarChartDemo7.java,v 1.13 2004/05/19 09:57:51 mungady Exp $
*
* Changes
* -------
* 27-Jan-2004 : Version 1, based on BarChartDemo.java (DG);
*
*/

// enable maven jfreechart artifact

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;


/**
* A simple demonstration application showing how to create a bar chart with a custom item
* label generator.
*/
public class BarChartDemo7 extends ApplicationFrame {

   /**
    * A custom label generator.
    */
//   static class LabelGenerator extends StandardCategoryLabelGenerator {
       /**
        * Generates an item label.
        * 
        * @param dataset  the dataset.
        * @param series  th						//System.out.println("doubleLong is "+ doubleLong);
						//System.out.println("doubleLongNext is "+ doubleLongNext);
e series index.
        * @param category  the category index.
        * 
        * @return the label.
        */
 //      public String generateItemLabel(final CategoryDataset dataset, 
//                                       final int series, 
 //                                      final int category) {
   //        return dataset.getRowKey(series).toString();
     //  }
//   }
   
   /**
    * Creates a new demo instance.
    *
    * @param title  the frame title.
    */
   public BarChartDemo7(final String title) {
       super(title);
       final CategoryDataset dataset = createDataset();
       final JFreeChart chart = createChart(dataset);
       final ChartPanel chartPanel = new ChartPanel(chart);
       chartPanel.setPreferredSize(new Dimension(500, 270));
       setContentPane(chartPanel);
   }


   /**
    * Returns a sample dataset.
    * 
    * @return The dataset.
    */
   private CategoryDataset createDataset() {
       
       // row keys...
       final String series1 = "First";
       final String series2 = "Second";
       final String series3 = "Third";

       // column keys...
       final String category1 = "Category 1";
       final String category2 = "Category 2";
       final String category3 = "Category 3";
       final String category4 = "Category 4";
       final String category5 = "Category 5";

       // create the dataset...
       final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

       dataset.addValue(1.0, series1, category1);
       dataset.addValue(4.0, series1, category2);
       dataset.addValue(3.0, series1, category3);
       dataset.addValue(5.0, series1, category4);
       dataset.addValue(5.0, series1, category5);

       dataset.addValue(5.0, series2, category1);
       dataset.addValue(7.0, series2, category2);
       dataset.addValue(6.0, series2, category3);
       dataset.addValue(8.0, series2, category4);
       dataset.addValue(4.0, series2, category5);

       dataset.addValue(4.0, series3, category1);
       dataset.addValue(3.0, series3, category2);
       dataset.addValue(2.0, series3, category3);
       dataset.addValue(3.0, series3, category4);
       dataset.addValue(6.0, series3, category5);
       
       return dataset;
       
   }
   

   /**
    * Creates a sample chart.
    * 
    * @param dataset  the dataset.
    * 
    * @return The chart.
 */
   private JFreeChart createChart(final CategoryDataset dataset) {
       
       // create the chart...
       final JFreeChart chart = ChartFactory.createBarChart(
           "Bar Chart Demo 7",       // chart title
           "Category",               // domain axis label
           "Value",                  // range axis label
           dataset,                  // data
           PlotOrientation.VERTICAL, // orientation
           false,                    // include legend
           true,                     // tooltips?
           false                     // URLs?
       );

       // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

       // set the background color for the chart...
       chart.setBackgroundPaint(Color.white);

       // get a reference to the plot for further customisation...
       final CategoryPlot plot = chart.getCategoryPlot();
       plot.setBackgroundPaint(Color.lightGray);
       plot.setDomainGridlinePaint(Color.white);
       plot.setRangeGridlinePaint(Color.white);
       
       final IntervalMarker target = new IntervalMarker(4.5, 7.5);
       target.setLabel("Target Range");
       target.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
       target.setLabelAnchor(RectangleAnchor.LEFT);
       target.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
       target.setPaint(new Color(222, 222, 255, 128));
       plot.addRangeMarker(target, Layer.BACKGROUND);
       
       // set the range axis to display integers only...
       final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
       rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

       // disable bar outlines...
       final BarRenderer renderer = (BarRenderer) plot.getRenderer();
       renderer.setDrawBarOutline(false);
       //renderer.setItemMargin(0.10);
       renderer.setMaximumBarWidth(.5); // set maximum width to 35% of chart

       
       // set up gradient paints for series...
       final GradientPaint gp0 = new GradientPaint(
           0.0f, 0.0f, Color.blue, 
           0.0f, 0.0f, Color.lightGray
       );
       final GradientPaint gp1 = new GradientPaint(
           0.0f, 0.0f, Color.green, 
           0.0f, 0.0f, Color.lightGray
       );
       final GradientPaint gp2 = new GradientPaint(
           0.0f, 0.0f, Color.red, 
           0.0f, 0.0f, Color.lightGray
       );
       renderer.setSeriesPaint(0, gp0);
       renderer.setSeriesPaint(1, gp1);
       renderer.setSeriesPaint(2, gp2);
       
 //      renderer.setLabelGenerator(new BarChartDemo7.LabelGenerator());
       renderer.setDefaultItemLabelsVisible(true);
       final ItemLabelPosition p = new ItemLabelPosition(
           ItemLabelAnchor.INSIDE12, TextAnchor.CENTER_RIGHT, 
           TextAnchor.CENTER_RIGHT, -Math.PI / 2.0
       );
       renderer.setDefaultPositiveItemLabelPosition(p);

       final ItemLabelPosition p2 = new ItemLabelPosition(
           ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER_LEFT, 
           TextAnchor.CENTER_LEFT, -Math.PI / 2.0
       );
       renderer.setPositiveItemLabelPositionFallback(p2);
       final CategoryAxis domainAxis = plot.getDomainAxis();
       domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
       // OPTIONAL CUSTOMISATION COMPLETED.
       
       return chart;
       
   }
 

   // ****************************************************************************
   // * JFREECHART DEVELOPER GUIDE                                               *
   // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
   // * to purchase from Object Refinery Limited:                                *
   // *                                                                          *
   // * http://www.object-refinery.com/jfreechart/guide.html                     *
   // *                                                                          *
   // * Sales are used to provide funding for the JFreeChart project - please    * 
   // * support us so that we can continue developing free software.             *
   // ****************************************************************************
   
   /**
    * Starting point for the demonstration application.
    *
    * @param args  ignored.
    */
   public static void main(final String[] args) {
/*
       //Log.getInstance().addTarget(new PrintStreamLogTarget());
       TextUtilities.setUseFontMetricsGetStringBounds(false);
       final BarChartDemo7 demo = new BarChartDemo7("Bar Chart Demo 7");
       demo.pack();
       RefineryUtilities.centerFrameOnScreen(demo);
       demo.setVisible(true);
*/
   }

}

