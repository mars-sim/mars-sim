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

package eu.hansolo.enzo.radialmenu;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.layout.Region;
import javafx.util.Duration;


/**
 * Created with IntelliJ IDEA.
 * User: hansolo
 * Date: 28.09.13
 * Time: 02:54
 * To change this template use File | Settings | File Templates.
 */
public class RadialMenuButton extends Region {
    private static final double      PREFERRED_SIZE    = 45;
    private static final double      MINIMUM_SIZE      = 20;
    private static final double      MAXIMUM_SIZE      = 1024;
    private static final PseudoClass OPEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private static boolean           clickable         = true;
    private RadialMenu               radialMenu;
    private BooleanProperty          open;
    private Region                   symbol;
    private RotateTransition         symbolRotate;


    // ******************** Constructors **************************************
    public RadialMenuButton(final RadialMenu RADIAL_MENU) {
        radialMenu = RADIAL_MENU;
        getStyleClass().setAll("menu-button");

        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getWidth(), 0) <= 0 ||
            Double.compare(getHeight(), 0) <= 0 ||
            Double.compare(getPrefWidth(), 0) <= 0 ||
            Double.compare(getPrefHeight(), 0) <= 0) {
            setPrefSize(PREFERRED_SIZE, PREFERRED_SIZE);
        }
        if (Double.compare(getMinWidth(), 0) <= 0 ||
            Double.compare(getMinHeight(), 0) <= 0) {
            setMinSize(MINIMUM_SIZE, MINIMUM_SIZE);
        }
        if (Double.compare(getMaxWidth(), 0) <= 0 ||
            Double.compare(getMaxHeight(), 0) <= 0) {
            setMaxSize(MAXIMUM_SIZE, MAXIMUM_SIZE);
        }
    }

    private void initGraphics() {
        setPickOnBounds(false);

        symbol = new Region();
        symbol.getStyleClass().add("symbol");
        symbol.setMouseTransparent(true);
        symbolRotate = new RotateTransition(Duration.millis(200), symbol);
        symbolRotate.setInterpolator(Interpolator.EASE_BOTH);

        // Add all nodes
        getChildren().addAll(symbol);
    }

    private void registerListeners() {
        widthProperty().addListener(observable -> resize());
        heightProperty().addListener(observable -> resize());
        setOnMouseClicked(actionEvent -> setOpen(!isOpen()));
        symbolRotate.setOnFinished(actionEvent -> clickable = true);
    }


    // ******************** Methods *******************************************
    public final boolean isOpen() {
        return null == open ? false : open.get();
    }
    public final void setOpen(final boolean OPEN) {
        openProperty().set(OPEN);
    }
    public final BooleanProperty openProperty() {
        if (null == open) {
            open = new BooleanPropertyBase(false) {
                @Override protected void invalidated() { pseudoClassStateChanged(OPEN_PSEUDO_CLASS, get()); }
                @Override public void set(final boolean OPEN) {
                    if (clickable) {
                        super.set(OPEN);
                        clickable = false;
                        rotate();
                    }
                }
                @Override public Object getBean() { return RadialMenuButton.this; }
                @Override public String getName() { return "open"; }
            };
        }
        return open;
    }

    private void rotate() {
        if (isOpen()) {
            symbolRotate.setFromAngle(0);
            symbolRotate.setToAngle(radialMenu.getOptions().isSimpleMode() ? -45 : -135);
        } else {
            symbolRotate.setFromAngle(radialMenu.getOptions().isSimpleMode() ? -45 : -135);
            symbolRotate.setToAngle(0);
        }
        symbolRotate.play();
    }

    private void resize() {
        symbol.setPrefSize(0.44444 * getPrefWidth(), 0.44444 * getPrefHeight());
        symbol.relocate((getPrefWidth() - symbol.getPrefWidth()) * 0.5, (getPrefHeight() - symbol.getPrefHeight()) * 0.5);
    }
}
