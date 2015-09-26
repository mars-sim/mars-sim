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

import eu.hansolo.enzo.charts.SimpleLineChart;
import eu.hansolo.enzo.common.Section;
import eu.hansolo.enzo.fonts.Fonts;
import javafx.collections.ListChangeListener;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.TextAlignment;

import java.util.stream.IntStream;


/**
 * Created by
 * User: hansolo
 * Date: 19.08.13
 * Time: 15:44
 */
public class SimpleLineChartSkin extends SkinBase<SimpleLineChart> implements Skin<SimpleLineChart> {
    private static final double      PREFERRED_WIDTH  = 200;
    private static final double      PREFERRED_HEIGHT = 100;
    private static final double      MINIMUM_WIDTH    = 50;
    private static final double      MINIMUM_HEIGHT   = 50;
    private static final double      MAXIMUM_WIDTH    = 1024;
    private static final double      MAXIMUM_HEIGHT   = 1024;
    private boolean                  keepAspect;
    private double                   aspectRatio;
    private double                   size;
    private double                   width;
    private double                   height;
    private double                   widthFactor;
    private double                   heightFactor;
    private double                   sectionMinimum;
    private double                   sectionMaximum;
    private Pane                     pane;
    private Canvas                   canvasBkg;
    private GraphicsContext          ctxBkg;
    private Canvas                   canvasFg;
    private GraphicsContext          ctxFg;


    // ******************** Constructors **************************************
    public SimpleLineChartSkin(SimpleLineChart chart) {
        super(chart);
        keepAspect  = true;
        aspectRatio = PREFERRED_HEIGHT / PREFERRED_WIDTH;
        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMinHeight(), 0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMaxHeight(), 0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {                
        canvasBkg = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctxBkg    = canvasBkg.getGraphicsContext2D();

        canvasFg  = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctxFg     = canvasFg.getGraphicsContext2D();

        pane      = new Pane();
        pane.getChildren().setAll(canvasBkg, canvasFg);

        getChildren().setAll(pane);
        resize();
        drawBackground();
        drawForeground();
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().heightProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().sectionRangeVisibleProperty().addListener(observable -> handleControlPropertyChanged("REDRAW_BACKGROUND"));
        getSkinnable().unitProperty().addListener(observable -> handleControlPropertyChanged("REDRAW_BACKGROUND"));
        getSkinnable().getSections().addListener((ListChangeListener<Section>) change -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().getSeries().getData().addListener((ListChangeListener) change -> handleControlPropertyChanged("REDRAW_FOREGROUND"));
        getSkinnable().fromProperty().addListener(observable -> handleControlPropertyChanged("REDRAW_FOREGROUND"));
        getSkinnable().toProperty().addListener(observable -> handleControlPropertyChanged("REDRAW_FOREGROUND"));
        getSkinnable().titleVisibleProperty().addListener(observable -> handleControlPropertyChanged("REDRAW_FOREGROUND"));
    }


    // ******************** Methods *******************************************
    protected void handleControlPropertyChanged(final String PROPERTY) {
        if ("RESIZE".equals(PROPERTY)) {
            resize();
            drawBackground();
            drawForeground();
        } else if ("REDRAW".equals(PROPERTY)) {
            drawBackground();
            drawForeground();
        } else if ("REDRAW_FOREGROUND".equals(PROPERTY)) {
            drawForeground();
        } else if ("REDRAW_BACKGROUND".equals(PROPERTY)) {
            drawBackground();
        }
    }

  
    // ******************** Private Methods ***********************************
    private void drawForeground() {
        ctxFg.clearRect(0, 0, width, height);
        ctxFg.setStroke(getSkinnable().getSeriesStroke());
        ctxFg.setLineCap(StrokeLineCap.ROUND);
        ctxFg.setLineJoin(StrokeLineJoin.ROUND);
        ctxFg.setLineWidth(0.025 * height);

        ctxFg.save();
        ctxFg.translate(0, sectionMinimum * heightFactor);

        widthFactor = width / (getSkinnable().getSeries().getData().size());
        int noOfDataPoints = getSkinnable().getSeries().getData().size();
        if (noOfDataPoints > 2) {
            for (int i = 0 ; i < noOfDataPoints - 1 ; i++) {
                XYChart.Data p1 = (XYChart.Data) getSkinnable().getSeries().getData().get(i);
                XYChart.Data p2 = (XYChart.Data) getSkinnable().getSeries().getData().get(i + 1);
                ctxFg.strokeLine(widthFactor / 2 + i * widthFactor, height - (Double) p1.getYValue() * heightFactor, widthFactor / 2 + (i + 1) * widthFactor, height - (Double) p2.getYValue() * heightFactor);
                drawBullet(ctxFg, widthFactor / 2 + i * widthFactor, height - (Double) p1.getYValue() * heightFactor, getSkinnable().getBulletFill());
            }
            drawBullet(ctxFg, widthFactor / 2 + (noOfDataPoints - 1) * widthFactor, height - (Double) (getSkinnable().getSeries().getData().get(noOfDataPoints - 1)).getYValue() * heightFactor, getSkinnable().getBulletFill());
        }
        ctxFg.save();
        ctxFg.applyEffect(new DropShadow(0.025 * height, 0, 0.025 * height, Color.rgb(0, 0, 0, 0.65)));
        ctxFg.restore();

        // draw from and to text
        ctxFg.setFill(Color.WHITE);        
        ctxFg.setFont(Fonts.robotoLight(0.1 * height));
        ctxFg.setTextBaseline(VPos.BOTTOM);
        ctxFg.setTextAlign(TextAlignment.LEFT);
        ctxFg.fillText(getSkinnable().getFrom(), 2, height - 2);
        ctxFg.setTextAlign(TextAlignment.RIGHT);
        ctxFg.fillText(getSkinnable().getTo(), width - 2, height -2);

        // draw title text
        if (getSkinnable().isTitleVisible()) {
            ctxFg.setTextBaseline(VPos.TOP);
            ctxFg.setTextAlign(TextAlignment.CENTER);
            ctxFg.fillText(getSkinnable().getSeries().getName(), width * 0.5, 2);
        }

        ctxFg.restore();
    }

    private void drawBackground() {
        ctxBkg.clearRect(0, 0, width, height);
        sectionMinimum = Double.MAX_VALUE;
        sectionMaximum = -Double.MAX_VALUE;
        double lowestSection  = Double.MAX_VALUE;
        for (Section section : getSkinnable().getSections()) {
            sectionMinimum = Math.min(sectionMinimum, section.getStart());
            sectionMaximum = Math.max(sectionMaximum, section.getStop());
            lowestSection  = Math.min(lowestSection, Math.abs(section.getStop() - section.getStart()));
        }
        ctxBkg.setStroke(Color.BLACK);
        ctxBkg.strokeRect(0, 0, width, height);

        heightFactor = height / (sectionMaximum - sectionMinimum);

        ctxBkg.save();
        ctxBkg.translate(0, sectionMinimum * heightFactor);
        
        ctxBkg.setFont(Fonts.robotoRegular(lowestSection * 0.8 * heightFactor));
        IntStream.range(0, getSkinnable().getSections().size()).forEach(
            i -> {
                final Section SECTION = getSkinnable().getSections().get(i);
                ctxBkg.save();
                switch(i) {
                    case 0: ctxBkg.setFill(getSkinnable().getSectionFill0()); break;
                    case 1: ctxBkg.setFill(getSkinnable().getSectionFill1()); break;
                    case 2: ctxBkg.setFill(getSkinnable().getSectionFill2()); break;
                    case 3: ctxBkg.setFill(getSkinnable().getSectionFill3()); break;
                    case 4: ctxBkg.setFill(getSkinnable().getSectionFill4()); break;
                    case 5: ctxBkg.setFill(getSkinnable().getSectionFill5()); break;
                    case 6: ctxBkg.setFill(getSkinnable().getSectionFill6()); break;
                    case 7: ctxBkg.setFill(getSkinnable().getSectionFill7()); break;
                    case 8: ctxBkg.setFill(getSkinnable().getSectionFill8()); break;
                    case 9: ctxBkg.setFill(getSkinnable().getSectionFill9()); break;
                }
                ctxBkg.fillRect(0, height - SECTION.getStop() * heightFactor, width, Math.abs(SECTION.getStop() - SECTION.getStart()) * heightFactor);
                ctxBkg.restore();                
            }
        );        
        if (getSkinnable().isSectionRangeVisible()) {
            IntStream.range(0, getSkinnable().getSections().size()).forEach(
                i -> {
                    final Section SECTION = getSkinnable().getSections().get(i);
                    ctxBkg.setFill(getSkinnable().getSeriesStroke());
                    ctxBkg.setTextBaseline(VPos.CENTER);
                    ctxBkg.fillText(SECTION.getStop() + getSkinnable().getUnit(), 0.02 * height, height - SECTION.getStop() * heightFactor);    
                }
            );                        
        }
        ctxBkg.restore();
    }

    private void drawBullet(final GraphicsContext CTX, final double X, final double Y, final Paint COLOR) {
        double iconSize = 0.04 * size;
        CTX.save();
        CTX.setLineWidth(0.0125 * height);
        CTX.setStroke(getSkinnable().getSeriesStroke());
        CTX.setFill(COLOR);
        CTX.strokeOval(X - iconSize * 0.5, Y - iconSize * 0.5, iconSize, iconSize);
        CTX.fillOval(X - iconSize * 0.5, Y - iconSize * 0.5, iconSize, iconSize);
        CTX.restore();
    }

    private void resize() {
        size   = getSkinnable().getWidth() < getSkinnable().getHeight() ? getSkinnable().getWidth() : getSkinnable().getHeight();
        width  = getSkinnable().getWidth();
        height = getSkinnable().getHeight();

        if (keepAspect) {
            if (aspectRatio * width > height) {
                width = 1 / (aspectRatio / height);
            } else if (1 / (aspectRatio / height) > width) {
                height = aspectRatio * width;
            }
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            canvasBkg.setWidth(width);
            canvasBkg.setHeight(height);
            canvasFg.setWidth(width);
            canvasFg.setHeight(height);
            drawBackground();
            drawForeground();
        }
    }
}
