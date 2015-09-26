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

package eu.hansolo.enzo.common;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;


/**
 * Created by
 * User: hansolo
 * Date: 11.09.13
 * Time: 22:15
 */
public class Symbol extends Region {
    public static final  boolean       RESIZEABLE       = true;
    public static final  boolean       NOT_RESIZEABLE   = false;
    public static final boolean        SELECTABLE       = true;
    public static final boolean        NOT_SELECTABLE   = false;
    private static final double        PREFERRED_WIDTH  = 28;
    private static final double        PREFERRED_HEIGHT = 28;
    private static final double        MINIMUM_WIDTH    = 5;
    private static final double        MINIMUM_HEIGHT   = 5;
    private static final double        MAXIMUM_WIDTH    = 1024;
    private static final double        MAXIMUM_HEIGHT   = 1024;
    private final double               DEFAULT_SIZE;    
    private static final PseudoClass   NOT_AVAILABLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("not-available");
    private static final PseudoClass   SELECTED_PSEUDO_CLASS      = PseudoClass.getPseudoClass("selected");
    private BooleanProperty            notAvailable;
    private BooleanProperty            selected;
    private boolean                    isSelectable;
    private ObjectProperty<SymbolType> symbolType;
    private ObjectProperty<Color>      color;
    private double                     size;
    private boolean                    resizeable;
    private Region                     symbolRegion;
    private Pane                       pane;
    private Tooltip                    tooltip;
    private String                     tooltipText;


    // ******************** Constructor ***************************************
    public Symbol(final SymbolType SYMBOL_TYPE, final double SIZE, final Color COLOR, final boolean RESIZEABLE) {
        this(SYMBOL_TYPE, SIZE, COLOR, RESIZEABLE, false);
    }
    public Symbol(final SymbolType SYMBOL_TYPE, final double SIZE, final Color COLOR, final boolean RESIZEABLE, final String TOOLTIP_TEXT) {
        this(SYMBOL_TYPE, SIZE, COLOR, RESIZEABLE, false, TOOLTIP_TEXT);
    }
    public Symbol(final SymbolType SYMBOL_TYPE, final double SIZE, final Color COLOR, final boolean RESIZEABLE, final boolean SELECTABLE) {
        this(SYMBOL_TYPE, SIZE, COLOR, RESIZEABLE, SELECTABLE, "");
    }
    public Symbol(final SymbolType SYMBOL_TYPE, final double SIZE, final Color COLOR, final boolean RESIZEABLE, final boolean SELECTABLE, final String TOOLTIP_TEXT) {
        symbolType   = new SimpleObjectProperty<>(this, "symbolType", (null == SYMBOL_TYPE) ? SymbolType.NONE : SYMBOL_TYPE);
        color        = new SimpleObjectProperty<>(this, "color", (null == COLOR) ? Color.BLACK : COLOR);
        size         = SIZE;
        DEFAULT_SIZE = SIZE;
        resizeable   = RESIZEABLE;
        isSelectable = SELECTABLE;
        tooltipText  = TOOLTIP_TEXT;
        getStylesheets().add(Symbol.class.getResource("symbols.css").toExternalForm());
        getStyleClass().setAll("symbol");
        init();
        initGraphics();
        registerListeners();                
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getMinWidth(), 0.0) <= 0 || Double.compare(getMinHeight(), 0.0) <= 0) {
            setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getMaxWidth(), 0.0) <= 0 || Double.compare(getMaxHeight(), 0.0) <= 0) {
            setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {        
        symbolRegion = new Region();
        symbolRegion.setId(symbolType.get().STYLE_CLASS);
        //symbolRegion.setStyle("-symbol-color: " + getColor().toString().replace("0x", "#") + ";");

        pane = new Pane();
        pane.getStyleClass().add("symbol");
        pane.setStyle("-symbol-color: " + getColor().toString().replace("0x", "#") + ";");
        pane.getChildren().addAll(symbolRegion);

        getChildren().add(pane);
    }

    private void registerListeners() {        
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        symbolType.addListener(o -> Platform.runLater(() -> { symbolRegion.setId(getSymbolType().STYLE_CLASS); resize(); }));
        color.addListener(o -> symbolRegion.setStyle("-symbol-color: " + getColor().toString().replace("0x", "#") + ";"));
        sceneProperty().addListener(o -> {
            if (null != getScene()) {
                tooltip = new Tooltip(tooltipText);
                Tooltip.install(this, tooltip);
            }
        });
    }


    // ******************** Methods *******************************************    
    public final SymbolType getSymbolType() { return symbolType.get(); }
    public final void setSymbolType(final SymbolType SYMBOL_TYPE) { symbolType.set(SYMBOL_TYPE); }
    public final ObjectProperty<SymbolType> symbolTypeProperty() { return symbolType; }

    public final Color getColor() { return color.get(); }
    public final void setColor(final Color COLOR) { color.set(COLOR); }
    public final ObjectProperty<Color> colorProperty() { return color; }

    public final boolean isNotAvailable() { return null == notAvailable ? false : notAvailable.get(); }
    public final void setNotAvailable(final boolean SELECTED) { notAvailableProperty().set(SELECTED); }
    public final BooleanProperty notAvailableProperty() {
        if (null == notAvailable) {
            notAvailable = new BooleanPropertyBase(false) {
                @Override protected void invalidated() { symbolRegion.pseudoClassStateChanged(NOT_AVAILABLE_PSEUDO_CLASS, get()); }
                @Override public void set(final boolean NOT_AVAILABLE) { super.set(NOT_AVAILABLE); }
                @Override public Object getBean() { return Symbol.this; }
                @Override public String getName() { return "notAvailable"; }
            };
    }
        return notAvailable;
    }

    public final boolean isSelected() { return null == selected ? false : selected.get(); }
    public final void setSelected(final boolean SELECTED) { selectedProperty().set(SELECTED); }
    public final BooleanProperty selectedProperty() {
        if (null == selected) {
            selected = new BooleanPropertyBase(false) {
                @Override protected void invalidated() {
                    if (isSelectable) { symbolRegion.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get()); }
                }
                @Override public void set(final boolean SELECTED) {
                    if (isSelectable) { super.set(SELECTED); }
                }
                @Override public Object getBean() { return Symbol.this; }
                @Override public String getName() { return "selected"; }
            };
    }
        return selected;
    }

    public final Tooltip getTooltip() { return tooltip; }
    public final void setTooltip(final Tooltip TOOLTIP) {
        if (Platform.isFxApplicationThread()) {
            tooltip = TOOLTIP;
            Tooltip.install(this, tooltip);
    }
    }

    public final void setTooltipText(final String TOOLTIP_TEXT) { tooltip.setText(TOOLTIP_TEXT); }

    public void resize() {
        double width  = getWidth();
        double height = getHeight();
        size = width < height ? width : height;
        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            if (resizeable) {
                pane.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);
                symbolRegion.setPrefSize(size * getSymbolType().WIDTH_FACTOR, size * getSymbolType().HEIGHT_FACTOR);
                symbolRegion.relocate((width - symbolRegion.getPrefWidth()) * 0.5, (height - symbolRegion.getPrefHeight()) * 0.5);
            } else {
                pane.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);
                symbolRegion.setMinSize(DEFAULT_SIZE * getSymbolType().WIDTH_FACTOR, DEFAULT_SIZE * getSymbolType().HEIGHT_FACTOR);
                symbolRegion.setPrefSize(DEFAULT_SIZE * getSymbolType().WIDTH_FACTOR, DEFAULT_SIZE * getSymbolType().HEIGHT_FACTOR);
                symbolRegion.setMaxSize(DEFAULT_SIZE * getSymbolType().WIDTH_FACTOR, DEFAULT_SIZE * getSymbolType().HEIGHT_FACTOR);
                symbolRegion.relocate((width - symbolRegion.getPrefWidth()) * 0.5, (height - symbolRegion.getPrefHeight()) * 0.5);
            }
        }
    }
}
