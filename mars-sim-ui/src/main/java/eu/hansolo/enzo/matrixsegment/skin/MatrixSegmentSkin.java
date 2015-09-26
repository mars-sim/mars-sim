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

package eu.hansolo.enzo.matrixsegment.skin;

import eu.hansolo.enzo.matrixsegment.MatrixSegment;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.hansolo.enzo.matrixsegment.MatrixSegment.Dot;


public class MatrixSegmentSkin extends SkinBase<MatrixSegment> implements Skin<MatrixSegment> {
    private static final double            PREFERRED_WIDTH  = 71;
    private static final double            PREFERRED_HEIGHT = 100;
    private static final double            MINIMUM_WIDTH    = 5;
    private static final double            MINIMUM_HEIGHT   = 5;
    private static final double            MAXIMUM_WIDTH    = 1024;
    private static final double            MAXIMUM_HEIGHT   = 1024;
    private static double                  aspectRatio;
    private Map<MatrixSegment.Dot, Region> dotMap;
    private List<Region>                   highlights;
    private double                         size;
    private double                         width;
    private double                         height;
    private Pane                           pane;
    private Region                         background;
    private InnerShadow                    backgroundInnerShadow;
    private InnerShadow                    backgroundInnerHighlight;
    private InnerShadow                    dotInnerShadow;
    private DropShadow                     glow;
    private Region        d57;
    private Region        d47;
    private Region        d37;
    private Region        d27;
    private Region        d17;
    private Region        d56;
    private Region        d46;
    private Region        d36;
    private Region        d26;
    private Region        d16;
    private Region        d55;
    private Region        d45;
    private Region        d35;
    private Region        d25;
    private Region        d15;
    private Region        d54;
    private Region        d44;
    private Region        d34;
    private Region        d24;
    private Region        d14;
    private Region        d53;
    private Region        d43;
    private Region        d33;
    private Region        d23;
    private Region        d13;
    private Region        d52;
    private Region        d42;
    private Region        d32;
    private Region        d22;
    private Region        d12;
    private Region        d51;
    private Region        d41;
    private Region        d31;
    private Region        d21;
    private Region        d11;
    private Region        d57h;
    private Region        d47h;
    private Region        d37h;
    private Region        d27h;
    private Region        d17h;
    private Region        d56h;
    private Region        d46h;
    private Region        d36h;
    private Region        d26h;
    private Region        d16h;
    private Region        d55h;
    private Region        d45h;
    private Region        d35h;
    private Region        d25h;
    private Region        d15h;
    private Region        d54h;
    private Region        d44h;
    private Region        d34h;
    private Region        d24h;
    private Region        d14h;
    private Region        d53h;
    private Region        d43h;
    private Region        d33h;
    private Region        d23h;
    private Region        d13h;
    private Region        d52h;
    private Region        d42h;
    private Region        d32h;
    private Region        d22h;
    private Region        d12h;
    private Region        d51h;
    private Region        d41h;
    private Region        d31h;
    private Region        d21h;
    private Region        d11h;


    // ******************** Constructors **************************************
    public MatrixSegmentSkin(final MatrixSegment CONTROL) {
        super(CONTROL);
        aspectRatio = PREFERRED_HEIGHT / PREFERRED_WIDTH;
        dotMap      = new HashMap<>(35);
        highlights  = new ArrayList<>(35);
        pane        = new Pane();
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

        if (getSkinnable().getPrefWidth() != PREFERRED_WIDTH || getSkinnable().getPrefHeight() != PREFERRED_HEIGHT) {
            aspectRatio = getSkinnable().getPrefHeight() / getSkinnable().getPrefWidth();
        }
    }

    private void initGraphics() {

        background = new Region();
        background.getStyleClass().setAll("background");

        backgroundInnerShadow = new InnerShadow();
        backgroundInnerShadow.setOffsetY(-1.0);
        backgroundInnerShadow.setRadius(1.0 / 310.0 * PREFERRED_WIDTH);
        backgroundInnerShadow.setColor(Color.rgb(0, 0, 0, 0.65));
        backgroundInnerShadow.setBlurType(BlurType.TWO_PASS_BOX);

        backgroundInnerHighlight = new InnerShadow();
        backgroundInnerHighlight.setOffsetY(1.0);
        backgroundInnerHighlight.setRadius(1.0 / 310.0 * PREFERRED_WIDTH);
        backgroundInnerHighlight.setColor(Color.rgb(200, 200, 200, 0.65));
        backgroundInnerHighlight.setBlurType(BlurType.TWO_PASS_BOX);
        backgroundInnerHighlight.setInput(backgroundInnerShadow);

        background.setEffect(backgroundInnerHighlight);

        dotInnerShadow = new InnerShadow();
        dotInnerShadow.setRadius(0.025 * PREFERRED_WIDTH);
        dotInnerShadow.setColor(Color.rgb(0, 0, 0, 0.65));
        dotInnerShadow.setBlurType(BlurType.TWO_PASS_BOX);

        glow = new DropShadow();
        glow.setInput(dotInnerShadow);
        glow.setRadius(0.032 * PREFERRED_WIDTH);
        glow.setColor(getSkinnable().getColor());
        glow.setBlurType(BlurType.TWO_PASS_BOX);

        // dot definitions
        d57 = new Region();
        d57.getStyleClass().add("dot-off");
        d57.setEffect(dotInnerShadow);
        dotMap.put(Dot.D57, d57);

        d47 = new Region();
        d47.getStyleClass().add("dot-off");
        d47.setEffect(dotInnerShadow);
        dotMap.put(Dot.D47, d47);

        d37 = new Region();
        d37.getStyleClass().add("dot-off");
        d37.setEffect(dotInnerShadow);
        dotMap.put(Dot.D37, d37);

        d27 = new Region();
        d27.getStyleClass().add("dot-off");
        d27.setEffect(dotInnerShadow);
        dotMap.put(Dot.D27, d27);

        d17 = new Region();
        d17.getStyleClass().add("dot-off");
        d17.setEffect(dotInnerShadow);
        dotMap.put(Dot.D17, d17);

        d56 = new Region();
        d56.getStyleClass().add("dot-off");
        d56.setEffect(dotInnerShadow);
        dotMap.put(Dot.D56, d56);

        d46 = new Region();
        d46.getStyleClass().add("dot-off");
        d46.setEffect(dotInnerShadow);
        dotMap.put(Dot.D46, d46);

        d36 = new Region();
        d36.getStyleClass().add("dot-off");
        d36.setEffect(dotInnerShadow);
        dotMap.put(Dot.D36, d36);

        d26 = new Region();
        d26.getStyleClass().add("dot-off");
        d26.setEffect(dotInnerShadow);
        dotMap.put(Dot.D26, d26);

        d16 = new Region();
        d16.getStyleClass().add("dot-off");
        d16.setEffect(dotInnerShadow);
        dotMap.put(Dot.D16, d16);

        d55 = new Region();
        d55.getStyleClass().add("dot-off");
        d55.setEffect(dotInnerShadow);
        dotMap.put(Dot.D55, d55);

        d45 = new Region();
        d45.getStyleClass().add("dot-off");
        d45.setEffect(dotInnerShadow);
        dotMap.put(Dot.D45, d45);

        d35 = new Region();
        d35.getStyleClass().add("dot-off");
        d35.setEffect(dotInnerShadow);
        dotMap.put(Dot.D35, d35);

        d25 = new Region();
        d25.getStyleClass().add("dot-off");
        d25.setEffect(dotInnerShadow);
        dotMap.put(Dot.D25, d25);

        d15 = new Region();
        d15.getStyleClass().add("dot-off");
        d15.setEffect(dotInnerShadow);
        dotMap.put(Dot.D15, d15);

        d54 = new Region();
        d54.getStyleClass().add("dot-off");
        d54.setEffect(dotInnerShadow);
        dotMap.put(Dot.D54, d54);

        d44 = new Region();
        d44.getStyleClass().add("dot-off");
        d44.setEffect(dotInnerShadow);
        dotMap.put(Dot.D44, d44);

        d34 = new Region();
        d34.getStyleClass().add("dot-off");
        d34.setEffect(dotInnerShadow);
        dotMap.put(Dot.D34, d34);

        d24 = new Region();
        d24.getStyleClass().add("dot-off");
        d24.setEffect(dotInnerShadow);
        dotMap.put(Dot.D24, d24);

        d14 = new Region();
        d14.getStyleClass().add("dot-off");
        d14.setEffect(dotInnerShadow);
        dotMap.put(Dot.D14, d14);

        d53 = new Region();
        d53.getStyleClass().add("dot-off");
        d53.setEffect(dotInnerShadow);
        dotMap.put(Dot.D53, d53);

        d43 = new Region();
        d43.getStyleClass().add("dot-off");
        d43.setEffect(dotInnerShadow);
        dotMap.put(Dot.D43, d43);

        d33 = new Region();
        d33.getStyleClass().add("dot-off");
        d33.setEffect(dotInnerShadow);
        dotMap.put(Dot.D33, d33);

        d23 = new Region();
        d23.getStyleClass().add("dot-off");
        d23.setEffect(dotInnerShadow);
        dotMap.put(Dot.D23, d23);

        d13 = new Region();
        d13.getStyleClass().add("dot-off");
        d13.setEffect(dotInnerShadow);
        dotMap.put(Dot.D13, d13);

        d52 = new Region();
        d52.getStyleClass().add("dot-off");
        d52.setEffect(dotInnerShadow);
        dotMap.put(Dot.D52, d52);

        d42 = new Region();
        d42.getStyleClass().add("dot-off");
        d42.setEffect(dotInnerShadow);
        dotMap.put(Dot.D42, d42);

        d32 = new Region();
        d32.getStyleClass().add("dot-off");
        d32.setEffect(dotInnerShadow);
        dotMap.put(Dot.D32, d32);

        d22 = new Region();
        d22.getStyleClass().add("dot-off");
        d22.setEffect(dotInnerShadow);
        dotMap.put(Dot.D22, d22);

        d12 = new Region();
        d12.getStyleClass().add("dot-off");
        d12.setEffect(dotInnerShadow);
        dotMap.put(Dot.D12, d12);

        d51 = new Region();
        d51.getStyleClass().add("dot-off");
        d51.setEffect(dotInnerShadow);
        dotMap.put(Dot.D51, d51);

        d41 = new Region();
        d41.getStyleClass().add("dot-off");
        d41.setEffect(dotInnerShadow);
        dotMap.put(Dot.D41, d41);

        d31 = new Region();
        d31.getStyleClass().add("dot-off");
        d31.setEffect(dotInnerShadow);
        dotMap.put(Dot.D31, d31);

        d21 = new Region();
        d21.getStyleClass().add("dot-off");
        d21.setEffect(dotInnerShadow);
        dotMap.put(Dot.D21, d21);

        d11 = new Region();
        d11.getStyleClass().add("dot-off");
        d11.setEffect(dotInnerShadow);
        dotMap.put(Dot.D11, d11);

        // highlight definitions
        d57h = new Region();
        d57h.getStyleClass().add("dot-highlight");
        highlights.add(d57h);

        d47h = new Region();
        d47h.getStyleClass().add("dot-highlight");
        highlights.add(d47h);

        d37h = new Region();
        d37h.getStyleClass().add("dot-highlight");
        highlights.add(d37h);

        d27h = new Region();
        d27h.getStyleClass().add("dot-highlight");
        highlights.add(d27h);

        d17h = new Region();
        d17h.getStyleClass().add("dot-highlight");
        highlights.add(d17h);

        d56h = new Region();
        d56h.getStyleClass().add("dot-highlight");
        highlights.add(d56h);

        d46h = new Region();
        d46h.getStyleClass().add("dot-highlight");
        highlights.add(d46h);

        d36h = new Region();
        d36h.getStyleClass().add("dot-highlight");
        highlights.add(d36h);

        d26h = new Region();
        d26h.getStyleClass().add("dot-highlight");
        highlights.add(d26h);

        d16h = new Region();
        d16h.getStyleClass().add("dot-highlight");
        highlights.add(d16h);

        d55h = new Region();
        d55h.getStyleClass().add("dot-highlight");
        highlights.add(d55h);

        d45h = new Region();
        d45h.getStyleClass().add("dot-highlight");
        highlights.add(d45h);

        d35h = new Region();
        d35h.getStyleClass().add("dot-highlight");
        highlights.add(d35h);

        d25h = new Region();
        d25h.getStyleClass().add("dot-highlight");
        highlights.add(d25h);

        d15h = new Region();
        d15h.getStyleClass().add("dot-highlight");
        highlights.add(d15h);

        d54h = new Region();
        d54h.getStyleClass().add("dot-highlight");
        highlights.add(d54h);

        d44h = new Region();
        d44h.getStyleClass().add("dot-highlight");
        highlights.add(d44h);

        d34h = new Region();
        d34h.getStyleClass().add("dot-highlight");
        highlights.add(d34h);

        d24h = new Region();
        d24h.getStyleClass().add("dot-highlight");
        highlights.add(d24h);

        d14h = new Region();
        d14h.getStyleClass().add("dot-highlight");
        highlights.add(d14h);

        d53h = new Region();
        d53h.getStyleClass().add("dot-highlight");
        highlights.add(d53h);

        d43h = new Region();
        d43h.getStyleClass().add("dot-highlight");
        highlights.add(d43h);

        d33h = new Region();
        d33h.getStyleClass().add("dot-highlight");
        highlights.add(d33h);

        d23h = new Region();
        d23h.getStyleClass().add("dot-highlight");
        highlights.add(d23h);

        d13h = new Region();
        d13h.getStyleClass().add("dot-highlight");
        highlights.add(d13h);

        d52h = new Region();
        d52h.getStyleClass().add("dot-highlight");
        highlights.add(d52h);

        d42h = new Region();
        d42h.getStyleClass().add("dot-highlight");
        highlights.add(d42h);

        d32h = new Region();
        d32h.getStyleClass().add("dot-highlight");
        highlights.add(d32h);

        d22h = new Region();
        d22h.getStyleClass().add("dot-highlight");
        highlights.add(d22h);

        d12h = new Region();
        d12h.getStyleClass().add("dot-highlight");
        highlights.add(d12h);

        d51h = new Region();
        d51h.getStyleClass().add("dot-highlight");
        highlights.add(d51h);

        d41h = new Region();
        d41h.getStyleClass().add("dot-highlight");
        highlights.add(d41h);

        d31h = new Region();
        d31h.getStyleClass().add("dot-highlight");
        highlights.add(d31h);

        d21h = new Region();
        d21h.getStyleClass().add("dot-highlight");
        highlights.add(d21h);

        d11h = new Region();
        d11h.getStyleClass().add("dot-highlight");
        highlights.add(d11h);

        pane.getChildren().setAll(background,
                                  d57, d47, d37, d27, d17,
                                  d56, d46, d36, d26, d16,
                                  d55, d45, d35, d25, d15,
                                  d54, d44, d34, d24, d14,
                                  d53, d43, d33, d23, d13,
                                  d52, d42, d32, d22, d12,
                                  d51, d41, d31, d21, d11,
                                  d57h, d47h, d37h, d27h, d17h,
                                  d56h, d46h, d36h, d26h, d16h,
                                  d55h, d45h, d35h, d25h, d15h,
                                  d54h, d44h, d34h, d24h, d14h,
                                  d53h, d43h, d33h, d23h, d13h,
                                  d52h, d42h, d32h, d22h, d12h,
                                  d51h, d41h, d31h, d21h, d11h);

        getChildren().setAll(pane);

        resize();
        updateMatrix();
        updateMatrixColor();
        for (Region highlight : highlights) {
            highlight.setOpacity(getSkinnable().isHighlightsVisible() ? 1 : 0);
        }
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(observable -> handleControlPropertyChanged("RESIZE") );
        getSkinnable().heightProperty().addListener(observable -> handleControlPropertyChanged("RESIZE") );
        getSkinnable().prefWidthProperty().addListener(observable -> handleControlPropertyChanged("PREF_SIZE") );
        getSkinnable().prefHeightProperty().addListener(observable -> handleControlPropertyChanged("PREF_SIZE") );
        getSkinnable().colorProperty().addListener(observable -> handleControlPropertyChanged("COLOR") );
        getSkinnable().backgroundVisibleProperty().addListener(observable -> handleControlPropertyChanged("BACKGROUND") );
        getSkinnable().highlightsVisibleProperty().addListener(observable -> handleControlPropertyChanged("HIGHLIGHTS") );
        getSkinnable().characterProperty().addListener(observable -> handleControlPropertyChanged("CHARACTER") );
        getSkinnable().glowEnabledProperty().addListener(observable -> handleControlPropertyChanged("GLOW") );

        getSkinnable().getStyleClass().addListener((ListChangeListener<String>) change -> resize());
    }


    // ******************** Methods *******************************************
    protected void handleControlPropertyChanged(final String PROPERTY) {
        if ("RESIZE".equals(PROPERTY)) {
            resize();
        } else if ("PREF_SIZE".equals(PROPERTY)) {
            aspectRatio = getSkinnable().getPrefHeight() / getSkinnable().getPrefWidth();
        } else if ("COLOR".equals(PROPERTY)) {
            updateMatrixColor();
        } else if ("BACKGROUND".equals(PROPERTY)) {
            background.setOpacity(getSkinnable().isBackgroundVisible() ? 1 : 0);
        } else if ("HIGHLIGHTS".equals(PROPERTY)) {
            for (Region highlight : highlights) {
                highlight.setOpacity(getSkinnable().isHighlightsVisible() ? 1 : 0);
            }
        } else if ("CHARACTER".equals(PROPERTY)) {
            updateMatrix();
        } else if ("GLOW".equals(PROPERTY)) {
            updateMatrix();
        }
    }
   

    // ******************** Update ********************************************
    public void updateMatrixColor() {
        getSkinnable().setStyle("-dot-on-color: " + colorToCss(getSkinnable().getColor()) + ";");
        //getSkinnable().setBackground(new Background(new BackgroundFill(getSkinnable().getColor(), null, null)));
        glow.setColor(getSkinnable().getColor());
    }

    public void updateMatrix() {
        final int ASCII = getSkinnable().getCharacter().isEmpty() ? 20 : getSkinnable().getCharacter().toUpperCase().charAt(0);

        if (getSkinnable().getCustomDotMapping().isEmpty()) {
            for (Dot dot : dotMap.keySet()) {
                if (getSkinnable().getDotMapping().containsKey(ASCII)) {
                    if (getSkinnable().getDotMapping().get(ASCII).contains(dot)) {
                        dotMap.get(dot).getStyleClass().setAll("dot-on");
                        dotMap.get(dot).setEffect(getSkinnable().isGlowEnabled() ? glow : dotInnerShadow);
                    } else {
                        dotMap.get(dot).getStyleClass().setAll("dot-off");
                        dotMap.get(dot).setEffect(dotInnerShadow);
                    }
                } else {
                    dotMap.get(dot).getStyleClass().setAll("dot-off");
                    dotMap.get(dot).setEffect(dotInnerShadow);
                }
            }
        } else {
            for (Dot dot : dotMap.keySet()) {
                if (getSkinnable().getCustomDotMapping().containsKey(ASCII)) {
                    if (getSkinnable().getCustomDotMapping().get(ASCII).contains(dot)) {
                        dotMap.get(dot).getStyleClass().setAll("dot-on");
                        dotMap.get(dot).setEffect(getSkinnable().isGlowEnabled() ? glow : dotInnerShadow);
                    } else {
                        dotMap.get(dot).getStyleClass().setAll("dot-off");
                        dotMap.get(dot).setEffect(dotInnerShadow);
                    }
                } else {
                    dotMap.get(dot).getStyleClass().setAll("dot-off");
                    dotMap.get(dot).setEffect(dotInnerShadow);
                }
            }
        }
    }


    // ******************** Resizing ******************************************
    private static String colorToCss(final Color COLOR) {
        return COLOR.toString().replace("0x", "#");
    }
    
    private void resize() {        
        width  = getSkinnable().getWidth();
        height = getSkinnable().getHeight();
        size   = width < height ? width : height;

        if (aspectRatio * width > height) {
            width  = 1 / (aspectRatio / height);
        } else if (1 / (aspectRatio / height) > width) {
            height = aspectRatio * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            // resize the background
            background.setPrefSize(width, height);
            backgroundInnerShadow.setRadius(0.0025 * size);
            backgroundInnerHighlight.setRadius(0.0025 * size);

            dotInnerShadow.setRadius(0.025 * width);
            glow.setRadius(0.032 * width);

            // resize the dots
            double dotWidth  = 0.13548387096774195 * width;
            double dotHeight = 0.0967741935483871 * height;

            for (Region dot : dotMap.values()) {
                dot.setPrefSize(dotWidth, dotHeight);
            }

            d57.setTranslateX(0.832258064516129 * width);
            d57.setTranslateY(0.880184331797235 * height);

            d47.setTranslateX(0.632258064516129 * width);
            d47.setTranslateY(0.880184331797235 * height);

            d37.setTranslateX(0.432258064516129 * width);
            d37.setTranslateY(0.880184331797235 * height);

            d27.setTranslateX(0.23225806451612904 * width);
            d27.setTranslateY(0.880184331797235 * height);

            d17.setTranslateX(0.03225806451612903 * width);
            d17.setTranslateY(0.880184331797235 * height);

            d56.setTranslateX(0.832258064516129 * width);
            d56.setTranslateY(0.7373271889400922 * height);

            d46.setTranslateX(0.632258064516129 * width);
            d46.setTranslateY(0.7373271889400922 * height);

            d36.setTranslateX(0.432258064516129 * width);
            d36.setTranslateY(0.7373271889400922 * height);

            d26.setTranslateX(0.23225806451612904 * width);
            d26.setTranslateY(0.7373271889400922 * height);

            d16.setTranslateX(0.03225806451612903 * width);
            d16.setTranslateY(0.7373271889400922 * height);

            d55.setTranslateX(0.832258064516129 * width);
            d55.setTranslateY(0.5944700460829493 * height);

            d45.setTranslateX(0.632258064516129 * width);
            d45.setTranslateY(0.5944700460829493 * height);

            d35.setTranslateX(0.432258064516129 * width);
            d35.setTranslateY(0.5944700460829493 * height);

            d25.setTranslateX(0.23225806451612904 * width);
            d25.setTranslateY(0.5944700460829493 * height);

            d15.setTranslateX(0.03225806451612903 * width);
            d15.setTranslateY(0.5944700460829493 * height);

            d54.setTranslateX(0.832258064516129 * width);
            d54.setTranslateY(0.45161290322580644 * height);

            d44.setTranslateX(0.632258064516129 * width);
            d44.setTranslateY(0.45161290322580644 * height);

            d34.setTranslateX(0.432258064516129 * width);
            d34.setTranslateY(0.45161290322580644 * height);

            d24.setTranslateX(0.23225806451612904 * width);
            d24.setTranslateY(0.45161290322580644 * height);

            d14.setTranslateX(0.03225806451612903 * width);
            d14.setTranslateY(0.45161290322580644 * height);

            d53.setTranslateX(0.832258064516129 * width);
            d53.setTranslateY(0.3087557603686636 * height);

            d43.setTranslateX(0.632258064516129 * width);
            d43.setTranslateY(0.3087557603686636 * height);

            d33.setTranslateX(0.432258064516129 * width);
            d33.setTranslateY(0.3087557603686636 * height);

            d23.setTranslateX(0.23225806451612904 * width);
            d23.setTranslateY(0.3087557603686636 * height);

            d13.setTranslateX(0.03225806451612903 * width);
            d13.setTranslateY(0.3087557603686636 * height);

            d52.setTranslateX(0.832258064516129 * width);
            d52.setTranslateY(0.16589861751152074 * height);

            d42.setTranslateX(0.632258064516129 * width);
            d42.setTranslateY(0.16589861751152074 * height);

            d32.setTranslateX(0.432258064516129 * width);
            d32.setTranslateY(0.16589861751152074 * height);

            d22.setTranslateX(0.23225806451612904 * width);
            d22.setTranslateY(0.16589861751152074 * height);

            d12.setTranslateX(0.03225806451612903 * width);
            d12.setTranslateY(0.16589861751152074 * height);

            d51.setTranslateX(0.832258064516129 * width);
            d51.setTranslateY(0.02304147465437788 * height);

            d41.setTranslateX(0.632258064516129 * width);
            d41.setTranslateY(0.02304147465437788 * height);

            d31.setTranslateX(0.432258064516129 * width);
            d31.setTranslateY(0.02304147465437788 * height);

            d21.setTranslateX(0.23225806451612904 * width);
            d21.setTranslateY(0.02304147465437788 * height);

            d11.setTranslateX(0.03225806451612903 * width);
            d11.setTranslateY(0.02304147465437788 * height);


            // resize the highlights
            double highlightWidth  = 0.07741935483870968 * width;
            double highlightHeight = 0.03456221198156682 * height;

            for (Region highlight : highlights) {
                highlight.setPrefSize(highlightWidth, highlightHeight);
            }

            d57h.setTranslateX(0.8612903225806452 * width);
            d57h.setTranslateY(0.8894009216589862 * height);

            d47h.setTranslateX(0.6612903225806451 * width);
            d47h.setTranslateY(0.8894009216589862 * height);

            d37h.setTranslateX(0.4612903225806452 * width);
            d37h.setTranslateY(0.8894009216589862 * height);

            d27h.setTranslateX(0.26129032258064516 * width);
            d27h.setTranslateY(0.8894009216589862 * height);

            d17h.setTranslateX(0.06129032258064516 * width);
            d17h.setTranslateY(0.8894009216589862 * height);

            d56h.setTranslateX(0.8612903225806452 * width);
            d56h.setTranslateY(0.7465437788018433 * height);

            d46h.setTranslateX(0.6612903225806451 * width);
            d46h.setTranslateY(0.7465437788018433 * height);

            d36h.setTranslateX(0.4612903225806452 * width);
            d36h.setTranslateY(0.7465437788018433 * height);

            d26h.setTranslateX(0.26129032258064516 * width);
            d26h.setTranslateY(0.7465437788018433 * height);

            d16h.setTranslateX(0.06129032258064516 * width);
            d16h.setTranslateY(0.7465437788018433 * height);

            d55h.setTranslateX(0.8612903225806452 * width);
            d55h.setTranslateY(0.6036866359447005 * height);

            d45h.setTranslateX(0.6612903225806451 * width);
            d45h.setTranslateY(0.6036866359447005 * height);

            d35h.setTranslateX(0.4612903225806452 * width);
            d35h.setTranslateY(0.6036866359447005 * height);

            d25h.setTranslateX(0.26129032258064516 * width);
            d25h.setTranslateY(0.6036866359447005 * height);

            d15h.setTranslateX(0.06129032258064516 * width);
            d15h.setTranslateY(0.6036866359447005 * height);

            d54h.setTranslateX(0.8612903225806452 * width);
            d54h.setTranslateY(0.4608294930875576 * height);

            d44h.setTranslateX(0.6612903225806451 * width);
            d44h.setTranslateY(0.4608294930875576 * height);

            d34h.setTranslateX(0.4612903225806452 * width);
            d34h.setTranslateY(0.4608294930875576 * height);

            d24h.setTranslateX(0.26129032258064516 * width);
            d24h.setTranslateY(0.4608294930875576 * height);

            d14h.setTranslateX(0.06129032258064516 * width);
            d14h.setTranslateY(0.4608294930875576 * height);

            d53h.setTranslateX(0.8612903225806452 * width);
            d53h.setTranslateY(0.31797235023041476 * height);

            d43h.setTranslateX(0.6612903225806451 * width);
            d43h.setTranslateY(0.31797235023041476 * height);

            d33h.setTranslateX(0.4612903225806452 * width);
            d33h.setTranslateY(0.31797235023041476 * height);

            d23h.setTranslateX(0.26129032258064516 * width);
            d23h.setTranslateY(0.31797235023041476 * height);

            d13h.setTranslateX(0.06129032258064516 * width);
            d13h.setTranslateY(0.31797235023041476 * height);

            d52h.setTranslateX(0.8612903225806452 * width);
            d52h.setTranslateY(0.17511520737327188 * height);

            d42h.setTranslateX(0.6612903225806451 * width);
            d42h.setTranslateY(0.17511520737327188 * height);

            d32h.setTranslateX(0.4612903225806452 * width);
            d32h.setTranslateY(0.17511520737327188 * height);

            d22h.setTranslateX(0.26129032258064516 * width);
            d22h.setTranslateY(0.17511520737327188 * height);

            d12h.setTranslateX(0.06129032258064516 * width);
            d12h.setTranslateY(0.17511520737327188 * height);

            d51h.setTranslateX(0.8612903225806452 * width);
            d51h.setTranslateY(0.03225806451612903 * height);

            d41h.setTranslateX(0.6612903225806451 * width);
            d41h.setTranslateY(0.03225806451612903 * height);

            d31h.setTranslateX(0.4612903225806452 * width);
            d31h.setTranslateY(0.03225806451612903 * height);

            d21h.setTranslateX(0.26129032258064516 * width);
            d21h.setTranslateY(0.03225806451612903 * height);

            d11h.setTranslateX(0.06129032258064516 * width);
            d11h.setTranslateY(0.03225806451612903 * height);
        }
    }
}
