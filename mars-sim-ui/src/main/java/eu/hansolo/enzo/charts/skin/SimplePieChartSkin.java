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

import eu.hansolo.enzo.charts.SimplePieChart;
import eu.hansolo.enzo.common.Util;
import eu.hansolo.enzo.fonts.Fonts;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Locale;


/**
 * User: hansolo
 * Date: 17.12.13
 * Time: 07:42
 */
public class SimplePieChartSkin extends SkinBase<SimplePieChart> implements Skin<SimplePieChart> {
    private static final double PREFERRED_WIDTH  = 200;
    private static final double PREFERRED_HEIGHT = 200;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private double                   size;
    private Pane                     pane;
    private Canvas                   dataCanvas;
    private GraphicsContext          dataCtx;
    private Region                   info;
    private Text                     sum;
    private Text                     title;
    private double                   angleStep;    
    private double                   interactiveAngle;


    // ******************** Constructors **************************************
    public SimplePieChartSkin(SimplePieChart chart) {
        super(chart);        
        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(),
                                                                                      0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(),
                                                                                  0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMinHeight(),
                                                                                     0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMaxHeight(),
                                                                                     0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {        
        Fonts.robotoLight(0.06 * PREFERRED_HEIGHT);
        
        dataCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        dataCtx = dataCanvas.getGraphicsContext2D();
                                
        info = new Region();                
        info.getStyleClass().setAll("info");

        sum = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getSum()));
        sum.setMouseTransparent(true);
        sum.setTextOrigin(VPos.CENTER);
        sum.getStyleClass().setAll("info-text");

        title = new Text(getSkinnable().getTitle());
        title.setTextOrigin(VPos.CENTER);
        title.getStyleClass().setAll("title");

        // Add all nodes
        pane = new Pane();
        pane.getStyleClass().setAll("simple-pie-chart");
        pane.getChildren().setAll(dataCanvas,
                                  info,
                                  sum,
                                  title);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().heightProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().sumProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));        
        getSkinnable().titleProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().infoColorProperty().addListener(observable -> handleControlPropertyChanged("INFO_COLOR"));                
        getSkinnable().sectionTextVisibleProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));        
        getSkinnable().infoTextColorProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().titleTextColorProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().sectionTextColorProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));        
        getSkinnable().getData().addListener((ListChangeListener<PieChart.Data>) change -> handleControlPropertyChanged("RESIZE"));        
        dataCanvas.setOnMouseDragged(mouseEvent -> touchRotate(mouseEvent.getSceneX() - getSkinnable().getLayoutX(), mouseEvent.getSceneY() - getSkinnable().getLayoutY()));
    }


    // ******************** Methods *******************************************
    protected void handleControlPropertyChanged(final String PROPERTY) {
        if ("RESIZE".equals(PROPERTY)) {
            resize();
        } else if ("INFO_COLOR".equals(PROPERTY)) {
            info.setStyle("-info-color: " + Util.colorToCss((Color) getSkinnable().getInfoColor()));
        }
    }
        

    // ******************** Private Methods ***********************************        
    private void touchRotate(final double X, final double Y) {
        double theta     = getTheta(X, Y);
        interactiveAngle = (theta + 90) % 360;        
        dataCanvas.setRotate(interactiveAngle);        
    }

    private double getTheta(double x, double y) {
        double deltaX = x - size * 0.5;
        double deltaY = y - size * 0.5;
        double radius = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        double nx     = deltaX / radius;
        double ny     = deltaY / radius;
        double theta  = Math.atan2(ny, nx);
        return Double.compare(theta, 0.0) >= 0 ? Math.toDegrees(theta) : Math.toDegrees((theta)) + 360.0;
    }
    
    private final void drawData() {
        dataCtx.clearRect(0, 0, size, size);        
        final int NO_OF_DATA_POINTS    = getSkinnable().getData().size();
        final double DATA_POINT_OFFSET = size * 0.015;
        final double DATA_POINT_SIZE   = size - (size * 0.03);        
        double startAngle              = -getSkinnable().getStartAngle();        
        
        angleStep = 360d / getSkinnable().getSum();
        
        dataCtx.setFont(Font.font("Open Sans", FontWeight.NORMAL, 0.06 * size));
        dataCtx.setTextAlign(TextAlignment.CENTER);
        dataCtx.setTextBaseline(VPos.CENTER);
        
        for (int i = 0 ; i < NO_OF_DATA_POINTS ; i++) {
            final PieChart.Data DATA_POINT   = getSkinnable().getData().get(i);                        
            final double        ANGLE_EXTEND = -DATA_POINT.getPieValue() * angleStep;            
            
            dataCtx.save();
            switch(i) {
                case 0 : dataCtx.setFill(getSkinnable().getSectionFill0()); break;
                case 1 : dataCtx.setFill(getSkinnable().getSectionFill1()); break;
                case 2 : dataCtx.setFill(getSkinnable().getSectionFill2()); break;
                case 3 : dataCtx.setFill(getSkinnable().getSectionFill3()); break;
                case 4 : dataCtx.setFill(getSkinnable().getSectionFill4()); break;
                case 5 : dataCtx.setFill(getSkinnable().getSectionFill5()); break;
                case 6 : dataCtx.setFill(getSkinnable().getSectionFill6()); break;
                case 7 : dataCtx.setFill(getSkinnable().getSectionFill7()); break;
                case 8 : dataCtx.setFill(getSkinnable().getSectionFill8()); break;
                case 9 : dataCtx.setFill(getSkinnable().getSectionFill9()); break;
                case 10: dataCtx.setFill(getSkinnable().getSectionFill10()); break;
                case 11: dataCtx.setFill(getSkinnable().getSectionFill11()); break;
                case 12: dataCtx.setFill(getSkinnable().getSectionFill12()); break;
                case 13: dataCtx.setFill(getSkinnable().getSectionFill13()); break;
                case 14: dataCtx.setFill(getSkinnable().getSectionFill14()); break;
            }
            dataCtx.fillArc(DATA_POINT_OFFSET, DATA_POINT_OFFSET, DATA_POINT_SIZE, DATA_POINT_SIZE, startAngle, ANGLE_EXTEND, ArcType.ROUND);

            // Draw Section Text
            if (getSkinnable().isSectionTextVisible()) {                                                                                                
                double percentage = DATA_POINT.getPieValue() / getSkinnable().getSum() * 100;                                            
                dataCtx.setFill(getSkinnable().getSectionTextColor());                                                                                
                dataCtx.save();
                                                
                dataCtx.translate(size * 0.5, size * 0.5);                                          
                dataCtx.rotate(-startAngle - (ANGLE_EXTEND * 0.5));                
                Point2D textPoint = new Point2D(size * 0.35, 0);
                switch (getSkinnable().getDataFormat()) {
                    case TEXT:
                        dataCtx.fillText(DATA_POINT.getName(), textPoint.getX(), textPoint.getY());
                        break;
                    case VALUE:
                        dataCtx.fillText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", DATA_POINT.getPieValue()), textPoint.getX(), textPoint.getY());
                        break;
                    case PERCENTAGE:
                        dataCtx.fillText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", percentage) + "%", textPoint.getX(), textPoint.getY());
                        break;
                    case TEXT_AND_PERCENTAGE:                    
                        dataCtx.fillText(DATA_POINT.getName() + "\n" + String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", percentage) + "%", textPoint.getX(), textPoint.getY());
                        break;
                    case TEXT_AND_VALUE:
                    default:
                        dataCtx.fillText(DATA_POINT.getName() + "\n" + String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", DATA_POINT.getPieValue()), textPoint.getX(), textPoint.getY());
                        break;
                }   
                dataCtx.restore();
            }            
            dataCtx.restore();
                        
            startAngle += ANGLE_EXTEND;
        }
        
        // Draw white border around area                        
        dataCtx.setStroke(Color.WHITE);
        dataCtx.setLineWidth(5);
        dataCtx.strokeArc(DATA_POINT_OFFSET, DATA_POINT_OFFSET, DATA_POINT_SIZE, DATA_POINT_SIZE, 0d, 360d, ArcType.OPEN);
    }
        
    private void resizeText() {        
        sum.setFont(Fonts.robotoBold(0.145 * size));
        if (sum.getLayoutBounds().getWidth() > 0.38 * size) {
            double decrement = 0d;
            while (sum.getLayoutBounds().getWidth() > 0.38 * size && sum.getFont().getSize() > 0) {                
                sum.setFont(Fonts.robotoBold(size * (0.15 - decrement)));
                decrement += 0.01;
            }
        }
        sum.setTranslateX((size - sum.getLayoutBounds().getWidth()) * 0.5);
        sum.setTranslateY(size * (title.getText().isEmpty() ? 0.5 : 0.48));
        
        title.setFont(Fonts.robotoBold(0.045 * size));
        if (title.getLayoutBounds().getWidth() > 0.38 * size) {
            double decrement = 0d;
            while (title.getLayoutBounds().getWidth() > 0.38 * size && title.getFont().getSize() > 0) {                
                title.setFont(Fonts.robotoBold(size * (0.05 - decrement)));
                decrement += 0.01;
            }
        }
        title.setTranslateX((size - title.getLayoutBounds().getWidth()) * 0.5);
        title.setTranslateY(size * 0.5 + sum.getFont().getSize() * 0.7);
    }

    private void resize() {
        size = getSkinnable().getWidth() < getSkinnable().getHeight() ? getSkinnable().getWidth() : getSkinnable().getHeight();

        if (size > 0) {
            pane.setMaxSize(size, size);
            
            dataCanvas.setWidth(size);
            dataCanvas.setHeight(size);
            drawData();
            dataCanvas.setCache(true);
            dataCanvas.setCacheHint(CacheHint.QUALITY);
    
            info.setPrefSize(size * 0.45, size * 0.45);
            info.relocate((size - info.getPrefWidth()) * 0.5, (size - info.getPrefHeight()) * 0.5);                        
            
            sum.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getSum()));
            
            title.setText(getSkinnable().getTitle());
                    
            resizeText();
        }
    }
}
