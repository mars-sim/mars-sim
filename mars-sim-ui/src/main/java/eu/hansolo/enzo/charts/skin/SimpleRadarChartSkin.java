/*
 * Copyright (c) 2015 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.enzo.charts.skin;

import eu.hansolo.enzo.charts.SimpleRadarChart;
import eu.hansolo.enzo.fonts.Fonts;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * User: hansolo
 * Date: 03.03.14
 * Time: 17:32
 */
public class SimpleRadarChartSkin extends SkinBase<SimpleRadarChart> implements Skin<SimpleRadarChart> {
    private static final double PREFERRED_WIDTH  = 250;
    private static final double PREFERRED_HEIGHT = 250;
    private static final double MINIMUM_WIDTH    = 100;
    private static final double MINIMUM_HEIGHT   = 100;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private double          size;
    private double          angleStep;
    private Pane            pane;
    private Canvas          canvas;
    private Text            title;
    private Text            unit;
    private Label           minValue;
    private Label           maxValue;    
    private Label           legend1;
    private Label           legend2;
    private Label           legend3;
    private Label           legend4;
    private double          legendStep;
    private List<Stop>      gradientStops;
    private GraphicsContext ctx;


    // ******************** Constructors **************************************
    public SimpleRadarChartSkin(SimpleRadarChart chart) {
        super(chart);
        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getMinHeight(), 0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getMaxHeight(), 0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        gradientStops = new ArrayList<>(16);
        for (Stop stop : getSkinnable().getGradientStops()) {
            if (Double.compare(stop.getOffset(), 0.0) == 0) gradientStops.add(new Stop(0, stop.getColor()));            
            gradientStops.add(new Stop(stop.getOffset() * 0.69924 + 0.30076, stop.getColor()));            
        }        
        
        canvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctx = canvas.getGraphicsContext2D();

        title = new Text(getSkinnable().getTitle());
        title.setTextAlignment(TextAlignment.CENTER);
        title.getStyleClass().add("title");
        title.setTextOrigin(VPos.CENTER);
        
        unit = new Text(getSkinnable().getUnit());
        unit.setTextAlignment(TextAlignment.CENTER);
        unit.getStyleClass().add("unit");
        unit.setTextOrigin(VPos.CENTER);
        
        minValue = new Label(String.format(Locale.US, "%.0f", getSkinnable().getMinValue()));
        minValue.setTextAlignment(TextAlignment.CENTER);
        minValue.getStyleClass().add("range");
        minValue.setAlignment(Pos.CENTER);
        minValue.setVisible(getSkinnable().isScaleVisible());

        legendStep = (getSkinnable().getMaxValue() - getSkinnable().getMinValue()) / 5d;

        legend1 = new Label(String.format(Locale.US, "%.0f", getSkinnable().getMinValue() + legendStep));
        legend1.setTextAlignment(TextAlignment.CENTER);
        legend1.getStyleClass().add("range");
        legend1.setAlignment(Pos.CENTER);
        legend1.setVisible(getSkinnable().isScaleVisible());

        legend2 = new Label(String.format(Locale.US, "%.0f", getSkinnable().getMinValue() + legendStep * 2));
        legend2.setTextAlignment(TextAlignment.CENTER);
        legend2.getStyleClass().add("range");
        legend2.setAlignment(Pos.CENTER);
        legend2.setVisible(getSkinnable().isScaleVisible());

        legend3 = new Label(String.format(Locale.US, "%.0f", getSkinnable().getMinValue() + legendStep * 3));
        legend3.setTextAlignment(TextAlignment.CENTER);
        legend3.getStyleClass().add("range");
        legend3.setAlignment(Pos.CENTER);
        legend3.setVisible(getSkinnable().isScaleVisible());

        legend4 = new Label(String.format(Locale.US, "%.0f", getSkinnable().getMinValue() + legendStep * 3));
        legend4.setTextAlignment(TextAlignment.CENTER);
        legend4.getStyleClass().add("range");
        legend4.setAlignment(Pos.CENTER);
        legend4.setVisible(getSkinnable().isScaleVisible());
        
        maxValue = new Label(String.format(Locale.US, "%.0f", getSkinnable().getMaxValue()));
        maxValue.setTextAlignment(TextAlignment.CENTER);
        maxValue.getStyleClass().add("range");
        maxValue.setAlignment(Pos.CENTER);
        maxValue.setVisible(getSkinnable().isScaleVisible());
        
        // Add all nodes
        pane = new Pane();
        pane.getStyleClass().setAll("simple-radar-chart");
        pane.getChildren().setAll(canvas, title, unit, minValue, legend4, legend3, legend2, legend1, maxValue);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().heightProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));                                        
        getSkinnable().titleProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().unitProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().scaleVisibleProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().minValueProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().maxValueProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().polygonModeProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().noOfSectorsProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));        
        getSkinnable().getData().addListener((MapChangeListener<Integer, XYChart.Data<String, Double>>) change -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().chartBackgroundProperty().addListener(observable -> handleControlPropertyChanged("REDRAW"));
        getSkinnable().chartForegroundProperty().addListener(observable -> handleControlPropertyChanged("REDRAW"));        
        getSkinnable().chartFillProperty().addListener(observable -> handleControlPropertyChanged("REDRAW"));
        getSkinnable().chartTextProperty().addListener(observable -> handleControlPropertyChanged("REDRAW"));
        getSkinnable().getGradientStops().addListener((ListChangeListener<Stop>) change -> handleControlPropertyChanged("REDRAW"));
        getSkinnable().filledProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
    }


    // ******************** Methods *******************************************        
    protected void handleControlPropertyChanged(final String PROPERTY) {
        if ("RESIZE".equals(PROPERTY)) {
            resize();
        } else if ("REDRAW".equals(PROPERTY)) {            
            gradientStops.clear();
            for (Stop stop : getSkinnable().getGradientStops()) {
                if (Double.compare(stop.getOffset(), 0.0) == 0) gradientStops.add(new Stop(0, stop.getColor()));                
                gradientStops.add(new Stop(stop.getOffset() * 0.69924 + 0.30076, stop.getColor()));                
            }                                                
            draw();
        }
    }
        

    // ******************** Private Methods ***********************************            
    private final void draw() {
        ctx.clearRect(0, 0, size, size);
        final double CENTER_X    = 0.5 * size;
        final double CENTER_Y    = 0.5 * size;        
        final double CIRCLE_SIZE = 0.95 * size;        
        final double DATA_RANGE  = (getSkinnable().getMaxValue() - getSkinnable().getMinValue());
        final double RANGE       = 0.35714 * CIRCLE_SIZE;
        final double OFFSET      = 0.14286 * CIRCLE_SIZE;
                
        // draw the chart background
        ctx.setFill(getSkinnable().getChartBackground());
        ctx.fillOval(0.5 * (size - CIRCLE_SIZE), 0.5 * (size - CIRCLE_SIZE), CIRCLE_SIZE, CIRCLE_SIZE);
                        
        // draw the chart data
        ctx.save();
        if (getSkinnable().getGradientStops().isEmpty()) {
            ctx.setFill(getSkinnable().getChartFill());
        } else {            
            ctx.setFill(new RadialGradient(0, 0,
                                           CENTER_X, CENTER_Y,
                                           0.5 * CIRCLE_SIZE,
                                           false, CycleMethod.NO_CYCLE,
                                           gradientStops));
        }
        ctx.setStroke(getSkinnable().getChartStroke());
                             
        double radius;        
        if (getSkinnable().isPolygonMode()) {
            // polygon mode
            ctx.beginPath();
            ctx.moveTo(CENTER_X, 0.36239 * size);            
            for (int i = 0 ; i < getSkinnable().getNoOfSectors() ; i++) {                
                radius = ((getSkinnable().getData().get(i).getYValue() - getSkinnable().getMinValue()) / DATA_RANGE);
                ctx.lineTo(CENTER_X, CENTER_Y - OFFSET - radius * RANGE);
                
                ctx.translate(CENTER_X, CENTER_Y);
                ctx.rotate(angleStep);
                ctx.translate(-CENTER_X, -CENTER_Y);                
            }            
            radius = ((getSkinnable().getData().get(getSkinnable().getNoOfSectors()).getYValue() - getSkinnable().getMinValue()) / DATA_RANGE);
            ctx.lineTo(CENTER_X, CENTER_Y - OFFSET - radius * RANGE);           
            ctx.closePath();            
        } else {
            // prerotate ctx to have 0 at 12 o'clock
            ctx.translate(CENTER_X, CENTER_Y);
            ctx.rotate(-90);
            ctx.translate(-CENTER_X, -CENTER_Y);
            // sector mode
            for (int i = 0 ; i < getSkinnable().getNoOfSectors() ; i++) {                
                radius = ((getSkinnable().getData().get(i).getYValue() - getSkinnable().getMinValue()) / DATA_RANGE);
                ctx.beginPath();
                ctx.moveTo(CENTER_X, CENTER_Y);
                ctx.arc(CENTER_X, CENTER_Y, radius * RANGE + OFFSET, radius * RANGE + OFFSET, 0, -angleStep);
                ctx.closePath();         

                ctx.translate(CENTER_X, CENTER_Y);
                ctx.rotate(angleStep);
                ctx.translate(-CENTER_X, -CENTER_Y);
            }    
        }
        if (getSkinnable().isFilled()) {
            ctx.fill();
        } else {
            ctx.stroke();
        }
                
        ctx.restore();
        
        // draw center point
        ctx.save();
        ctx.setFill(getSkinnable().getChartBackground());        
        ctx.translate(CENTER_X - OFFSET, CENTER_Y - OFFSET);
        ctx.fillOval(0, 0, 2 * OFFSET, 2 * OFFSET);
        ctx.restore();
        
        // draw concentric rings
        ctx.setStroke(getSkinnable().getChartForeground());
        double ringStepSize = (CIRCLE_SIZE - CIRCLE_SIZE * 0.28571) / 20.0;
        double pos = 0.5 * (size - CIRCLE_SIZE);
        double ringSize = CIRCLE_SIZE;
        for (int i = 0 ; i < 11 ; i++) {
            ctx.strokeOval(pos, pos, ringSize, ringSize);
            pos      += ringStepSize;
            ringSize -= 2 * ringStepSize;
        }
        // draw star lines
        ctx.save();
        for (int i = 0 ; i < getSkinnable().getNoOfSectors() ; i++) {
            ctx.strokeLine(CENTER_X, 0.36239 * size, CENTER_X, 0.5 * (size - CIRCLE_SIZE));
            ctx.translate(CENTER_X, CENTER_Y);
            ctx.rotate(angleStep);
            ctx.translate(-CENTER_X, -CENTER_Y);
        }
        ctx.restore();

        // draw zero line
        if (getSkinnable().isZeroLineVisible()) {
            ctx.setStroke(getSkinnable().getZeroLineColor());
            radius = ((-getSkinnable().getMinValue()) / DATA_RANGE);
            ctx.strokeOval(0.5 * size - OFFSET - radius * RANGE, 0.5 * size - OFFSET - radius * RANGE,
                           2 * (radius * RANGE + OFFSET), 2 * (radius * RANGE + OFFSET));
        }

        // prerotate if sectormode
        ctx.save();
        if (!getSkinnable().isPolygonMode()) {
            ctx.translate(CENTER_X, CENTER_Y);
            ctx.rotate(angleStep * 0.5);
            ctx.translate(-CENTER_X, -CENTER_Y);
        }
        
        // draw text        
        ctx.setFont(Fonts.robotoLight(0.025 * size));
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.setTextBaseline(VPos.CENTER);
        ctx.setFill(getSkinnable().getChartText());
        for (int i = 0 ; i < getSkinnable().getNoOfSectors() ; i++) {
            ctx.fillText(getSkinnable().getData().get(i).getXValue(), CENTER_X, 0.01 * size);
            ctx.translate(CENTER_X, CENTER_Y);
            ctx.rotate(angleStep);
            ctx.translate(-CENTER_X, -CENTER_Y);
        }
        ctx.restore();
    }
       
    private void resizeText() {
        title.setText(getSkinnable().getTitle());
        title.setFont(Fonts.robotoLight(0.045 * size));        
        if (title.getLayoutBounds().getWidth() > 0.25 * size) {
            double decrement = 0d;
            while (title.getLayoutBounds().getWidth() > 0.25 * size && title.getFont().getSize() > 0) {
                title.setFont(Fonts.robotoLight(size * (0.05 - decrement)));
                decrement += 0.01;
            }
        }
        title.setTranslateX((size - title.getLayoutBounds().getWidth()) * 0.5);        
        title.setTranslateY(size * 0.5);        
        
        unit.setText(getSkinnable().getUnit());
        unit.setFont(Fonts.robotoLight(0.038 * size));
        if (unit.getLayoutBounds().getWidth() > 0.25 * size) {
            double decrement = 0d;
            while (unit.getLayoutBounds().getWidth() > 0.25 * size && unit.getFont().getSize() > 0) {
                unit.setFont(Fonts.robotoLight(size * (0.05 - decrement)));
                decrement += 0.01;
            }
        }
        unit.setTranslateX((size - unit.getLayoutBounds().getWidth()) * 0.5);
        if (getSkinnable().getTitle().isEmpty()) {
            unit.setTranslateY(size * 0.5);
        } else {
            unit.setTranslateY(size * 0.59);
        }

        minValue.setVisible(getSkinnable().isScaleVisible());
        legend1.setVisible(getSkinnable().isScaleVisible());
        legend2.setVisible(getSkinnable().isScaleVisible());
        legend3.setVisible(getSkinnable().isScaleVisible());
        legend4.setVisible(getSkinnable().isScaleVisible());
        maxValue.setVisible(getSkinnable().isScaleVisible());
        
        if (getSkinnable().isScaleVisible()) {                                    
            minValue.setText(String.format(Locale.US, "%.0f", getSkinnable().getMinValue()));
            minValue.setFont(Fonts.robotoRegular(0.02 * size));            
            if (Double.compare(minValue.getLayoutBounds().getWidth(), 0) == 0) {
                minValue.relocate((size - 15) * 0.5, 0.3335 * size);
            } else {
                minValue.relocate((size - minValue.getLayoutBounds().getWidth()) * 0.5, 0.3335 * size);
            }

            legendStep = (getSkinnable().getMaxValue() - getSkinnable().getMinValue()) / 5d;

            legend1.setText(String.format(Locale.US, "%.0f", getSkinnable().getMinValue() + legendStep));
            legend1.setFont(Fonts.robotoRegular(0.02 * size));
            if (Double.compare(legend1.getLayoutBounds().getWidth(), 0) == 0) {
                legend1.relocate((size - 15) * 0.5, 0.2723 * size);
            } else {
                legend1.relocate((size - legend1.getLayoutBounds().getWidth()) * 0.5, 0.2723 * size);
            }

            legend2.setText(String.format(Locale.US, "%.0f", getSkinnable().getMinValue() + legendStep * 2));
            legend2.setFont(Fonts.robotoRegular(0.02 * size));
            if (Double.compare(legend2.getLayoutBounds().getWidth(), 0) == 0) {
                legend2.relocate((size - 15) * 0.5, 0.2111 * size);
            } else {
                legend2.relocate((size - legend2.getLayoutBounds().getWidth()) * 0.5, 0.2111 * size);
            }

            legend3.setText(String.format(Locale.US, "%.0f", getSkinnable().getMinValue() + legendStep * 3));
            legend3.setFont(Fonts.robotoRegular(0.02 * size));
            if (Double.compare(legend3.getLayoutBounds().getWidth(), 0) == 0) {
                legend3.relocate((size - 15) * 0.5, 0.1499 * size);
            } else {
                legend3.relocate((size - legend3.getLayoutBounds().getWidth()) * 0.5, 0.1499 * size);
            }

            legend4.setText(String.format(Locale.US, "%.0f", getSkinnable().getMinValue() + legendStep * 4));
            legend4.setFont(Fonts.robotoRegular(0.02 * size));
            if (Double.compare(legend4.getLayoutBounds().getWidth(), 0) == 0) {
                legend4.relocate((size - 15) * 0.5, 0.0887 * size);
            } else {
                legend4.relocate((size - legend4.getLayoutBounds().getWidth()) * 0.5, 0.0887 * size);
            }
            
            maxValue.setText(String.format(Locale.US, "%.0f", getSkinnable().getMaxValue()));
            maxValue.setFont(Fonts.robotoRegular(0.02 * size));
            if (Double.compare(minValue.getLayoutBounds().getWidth(), 0) == 0) {
                maxValue.relocate((size - 15) * 0.5, 0.0275 * size);
            } else {
                maxValue.relocate((size - maxValue.getLayoutBounds().getWidth()) * 0.5, 0.0275 * size);
            }            
        }        
    }

    private void resize() {
        size = getSkinnable().getWidth() < getSkinnable().getHeight() ? getSkinnable().getWidth() : getSkinnable().getHeight();

        if (size > 0) {            
            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);
            
            canvas.setWidth(size);
            canvas.setHeight(size);
            
            angleStep = 360d / getSkinnable().getNoOfSectors();
            
            draw();
            canvas.setCache(true);
            canvas.setCacheHint(CacheHint.QUALITY);                       

            resizeText();
        }
    }
}
