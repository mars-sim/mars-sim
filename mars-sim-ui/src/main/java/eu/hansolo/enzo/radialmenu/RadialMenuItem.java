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

import com.sun.javafx.css.converters.ColorConverter;
import eu.hansolo.enzo.common.SymbolType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: hansolo
 * Date: 21.09.12
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */
public class RadialMenuItem extends Region {
    private static final double PREFERRED_SIZE = 35;
    private static final double MINIMUM_SIZE   = 15;
    private static final double MAXIMUM_SIZE   = 1024;

    public static final Color DEFAULT_BACKGROUND_COLOR          = Color.rgb(41, 41, 40);
    public static final Color DEFAULT_BORDER_COLOR              = Color.rgb(24, 24, 24);
    public static final Color DEFAULT_FOREGROUND_COLOR          = Color.WHITE;
    public static final Color DEFAULT_SELECTED_BACKGROUND_COLOR = Color.rgb(253, 153, 52);
    public static final Color DEFAULT_SELECTED_FOREGROUND_COLOR = Color.WHITE;

    private static final PseudoClass SELECT_PSEUDO_CLASS = PseudoClass.getPseudoClass("select");
    private BooleanProperty            selected;
    private boolean                    _selectable;
    private BooleanProperty            selectable;
    private String                     _tooltip;
    private StringProperty             tooltip;
    private double                     _size;
    private DoubleProperty             size;
    private Color                      _backgroundColor;
    private ObjectProperty<Color>      backgroundColor;
    private Color                      _borderColor;
    private ObjectProperty<Color>      borderColor;
    private Color                      _foregroundColor;
    private ObjectProperty<Color>      foregroundColor;
    private Color                      _selectedBackgroundColor;
    private ObjectProperty<Color>      selectedBackgroundColor;
    private Color                      _selectedForegroundColor;
    private ObjectProperty<Color>      selectedForegroundColor;
    private SymbolType                 _symbolType;
    private ObjectProperty<SymbolType> symbolType;
    private StringProperty             thumbnailImageName;
    private StringProperty             text;


    // ******************** Constructors **************************************
    public RadialMenuItem() {
        this("", 35, DEFAULT_BACKGROUND_COLOR, DEFAULT_BORDER_COLOR, DEFAULT_FOREGROUND_COLOR, DEFAULT_SELECTED_BACKGROUND_COLOR, DEFAULT_SELECTED_FOREGROUND_COLOR, SymbolType.NONE, "");
    }
    public RadialMenuItem(final SymbolType SYMBOL_TYPE) {
        this("", 35, DEFAULT_BACKGROUND_COLOR, DEFAULT_BORDER_COLOR, DEFAULT_FOREGROUND_COLOR, DEFAULT_SELECTED_BACKGROUND_COLOR, DEFAULT_FOREGROUND_COLOR, SYMBOL_TYPE, "");
    }
    public RadialMenuItem(final String THUMBNAIL_IMAGE_NAME) {
        this("", 35, DEFAULT_BACKGROUND_COLOR, DEFAULT_BORDER_COLOR, DEFAULT_FOREGROUND_COLOR, DEFAULT_SELECTED_BACKGROUND_COLOR, DEFAULT_FOREGROUND_COLOR, SymbolType.NONE, THUMBNAIL_IMAGE_NAME);
    }
    public RadialMenuItem(final SymbolType SYMBOL_TYPE, final String TOOLTIP) {
        this(TOOLTIP, 35, DEFAULT_BACKGROUND_COLOR, DEFAULT_BORDER_COLOR, DEFAULT_FOREGROUND_COLOR, DEFAULT_SELECTED_BACKGROUND_COLOR, DEFAULT_FOREGROUND_COLOR, SYMBOL_TYPE, "");
    }
    public RadialMenuItem(final SymbolType SYMBOL_TYPE, final String TOOLTIP, final Color INNER_COLOR, final Color SELECTED_COLOR) {
        this(TOOLTIP, 35, DEFAULT_BACKGROUND_COLOR, DEFAULT_BORDER_COLOR, DEFAULT_FOREGROUND_COLOR, DEFAULT_SELECTED_BACKGROUND_COLOR, DEFAULT_FOREGROUND_COLOR, SYMBOL_TYPE, "");
    }
    public RadialMenuItem(final String TOOLTIP, final double SIZE, final Color BACKGROUND_COLOR, final Color BORDER_COLOR, final Color FOREGROUND_COLOR, final Color SELECTED_BACKGROUND_COLOR, final Color SELECTED_FOREGROUND_COLOR, final SymbolType SYMBOL_TYPE, final String THUMBNAIL_IMAGE_NAME) {
        getStyleClass().setAll("menu-item");

        _tooltip                 = TOOLTIP;
        _size                    = SIZE;
        _backgroundColor         = BACKGROUND_COLOR;
        _borderColor             = BORDER_COLOR;
        _foregroundColor         = FOREGROUND_COLOR;
        _selectedBackgroundColor = SELECTED_BACKGROUND_COLOR;
        _selectedForegroundColor = SELECTED_FOREGROUND_COLOR;
        _symbolType              = SYMBOL_TYPE;
        _selectable              = false;
        thumbnailImageName       = new SimpleStringProperty(this, "thumbnailImageName", THUMBNAIL_IMAGE_NAME);

        init();
        initGraphics();
        registerListeners();
        setPrefSize(getSize(), getSize());
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
    }

    private void registerListeners() {
        sizeProperty().addListener((ov, oldSize, newSize) -> setPrefSize(newSize.doubleValue(), newSize.doubleValue()));
    }


    // ******************** Methods *******************************************
    @Override protected double computePrefWidth(final double PREF_HEIGHT) {
        double prefHeight = PREFERRED_SIZE;
        if (PREF_HEIGHT != -1) {
            prefHeight = Math.max(0, PREF_HEIGHT - getInsets().getTop() - getInsets().getBottom());
        }
        return super.computePrefWidth(prefHeight);
    }
    @Override protected double computePrefHeight(final double PREF_WIDTH) {
        double prefWidth = PREFERRED_SIZE;
        if (PREF_WIDTH != -1) {
            prefWidth = Math.max(0, PREF_WIDTH - getInsets().getLeft() - getInsets().getRight());
        }
        return super.computePrefWidth(prefWidth);
    }

    @Override protected double computeMinWidth(final double MIN_HEIGHT) {
        return super.computeMinWidth(Math.max(MINIMUM_SIZE, MIN_HEIGHT - getInsets().getTop() - getInsets().getBottom()));
    }
    @Override protected double computeMinHeight(final double MIN_WIDTH) {
        return super.computeMinHeight(Math.max(MINIMUM_SIZE, MIN_WIDTH - getInsets().getLeft() - getInsets().getRight()));
    }

    @Override protected double computeMaxWidth(final double MAX_HEIGHT) {
        return super.computeMaxWidth(Math.min(MAXIMUM_SIZE, MAX_HEIGHT - getInsets().getTop() - getInsets().getBottom()));
    }
    @Override protected double computeMaxHeight(final double MAX_WIDTH) {
        return super.computeMaxHeight(Math.min(MAXIMUM_SIZE, MAX_WIDTH - getInsets().getLeft() - getInsets().getRight()));
    }

    public String getTooltip() {
        return null == tooltip ? _tooltip : tooltip.get();
    }
    public void setTooltip(final String TOOLTIP) {
        if (null == tooltip) {
            _tooltip = TOOLTIP;
        } else {
            tooltip.set(TOOLTIP);
        }
    }
    public StringProperty tooltipProperty() {
        if (null == tooltip) {
            tooltip = new SimpleStringProperty(this, "tooltip", _tooltip);
        }
        return tooltip;
    }

    public double getSize() {
        return null == size ? _size : size.get();
    }
    public void setSize(final double SIZE) {
        if (null == size) {
            _size = SIZE;
        } else {
            size.set(SIZE);
        }
    }
    public DoubleProperty sizeProperty() {
        if (null == size) {
            size = new SimpleDoubleProperty(this, "size", _size);
        }
        return size;
    }

    public Color getBackgroundColor() {
        return null == backgroundColor ? _backgroundColor : backgroundColor.get();
    }
    public void setBackgroundColor(final Color BACKGROUND_COLOR) {
        if (null == backgroundColor) {
            _backgroundColor = BACKGROUND_COLOR;
        } else {
            backgroundColor.set(BACKGROUND_COLOR);
        }
    }
    public ObjectProperty<Color> backgroundColorProperty() {
        if (null == backgroundColor) {
            backgroundColor = new StyleableObjectProperty<Color>(DEFAULT_BACKGROUND_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.BACKGROUND_COLOR; }
                @Override public Object getBean() { return RadialMenuItem.this; }
                @Override public String getName() { return "backgroundColor"; }
            };
        }
        return backgroundColor;
    }

    public Color getBorderColor() {
        return null == borderColor ? _borderColor : borderColor.get();
    }
    public void setBorderColor(final Color BORDER_COLOR) {
        if (null == borderColor) {
            _borderColor = BORDER_COLOR;
        } else {
            borderColor.set(BORDER_COLOR);
        }
    }
    public ObjectProperty<Color> borderColorProperty() {
        if (null == borderColor) {
            borderColor = new StyleableObjectProperty<Color>(DEFAULT_BORDER_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.BORDER_COLOR; }
                @Override public Object getBean() { return RadialMenuItem.this; }
                @Override public String getName() { return "borderColor"; }
            };
        }
        return borderColor;
    }

    public Color getForegroundColor() {
        return null == foregroundColor ? _foregroundColor : foregroundColor.get();
    }
    public void setForegroundColor(final Color FOREGROUND_COLOR) {
        if (null == foregroundColor) {
            _foregroundColor = FOREGROUND_COLOR;
        } else {
            foregroundColor.set(FOREGROUND_COLOR);
        }
    }
    public ObjectProperty<Color> foregroundColorProperty() {
        if (null == foregroundColor) {
            foregroundColor = new StyleableObjectProperty<Color>(DEFAULT_FOREGROUND_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.FOREGROUND_COLOR; }
                @Override public Object getBean() { return RadialMenuItem.this; }
                @Override public String getName() { return "foregroundColor"; }
            };
        }
        return foregroundColor;
    }

    public Color getSelectedBackgroundColor() {
        return null == selectedBackgroundColor ? _selectedBackgroundColor : selectedBackgroundColor.get();
    }
    public void setSelectedBackgroundColor(final Color SELECTED_BACKGROUND_COLOR) {
        if (null == selectedBackgroundColor) {
            _selectedBackgroundColor = SELECTED_BACKGROUND_COLOR;
        } else {
            selectedBackgroundColor.set(SELECTED_BACKGROUND_COLOR);
        }
    }
    public ObjectProperty<Color> selectedBackgroundColorProperty() {
        if (null == selectedBackgroundColor) {
            selectedBackgroundColor = new StyleableObjectProperty<Color>(DEFAULT_SELECTED_BACKGROUND_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SELECTED_BACKGROUND_COLOR; }
                @Override public Object getBean() { return RadialMenuItem.this; }
                @Override public String getName() { return "selectedBackgroundColor"; }
            };
        }
        return selectedBackgroundColor;
    }

    public Color getSelectedForegroundColor() {
        return null == selectedForegroundColor ? _selectedForegroundColor : selectedForegroundColor.get();
    }
    public void setSelectedForegroundColor(final Color SELECTED_FOREGROUND_COLOR) {
        if (null == selectedForegroundColor) {
            _selectedForegroundColor = SELECTED_FOREGROUND_COLOR;
        } else {
            selectedForegroundColor.set(SELECTED_FOREGROUND_COLOR);
        }
    }
    public ObjectProperty<Color> selectedForegroundColorProperty() {
        if (null == selectedForegroundColor) {
            selectedForegroundColor = new StyleableObjectProperty<Color>(DEFAULT_SELECTED_FOREGROUND_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SELECTED_FOREGROUND_COLOR; }
                @Override public Object getBean() { return RadialMenuItem.this; }
                @Override public String getName() { return "selectedForegroundColor"; }
            };
        }
        return selectedForegroundColor;
    }

    public SymbolType getSymbolType() {
        return null == symbolType ? _symbolType : symbolType.get();
    }
    public void setSymbolType(final SymbolType SYMBOL_TYPE) {
        if (null == symbolType) {
            _symbolType = SYMBOL_TYPE;
        } else {
            symbolType.set(SYMBOL_TYPE);
        }
    }
    public ObjectProperty<SymbolType> symbolTypeProperty() {
        if (null == symbolType) {
            symbolType = new SimpleObjectProperty<>(this, "symbolType", _symbolType);
        }
        return symbolType;
   }

    public String getThumbnailImageName() {
        return thumbnailImageName.get();
    }
    public void setThumbnailImageName(final String THUMBNAIL_IMAGE_NAME) {
        thumbnailImageName.set(THUMBNAIL_IMAGE_NAME);
    }
    public StringProperty thumbnailImageNameProperty() {
        return thumbnailImageName;
    }

    public String getText() {
        return null == text ? "" : text.get();
    }
    public void setText(final String TEXT) {
        textProperty().set(TEXT);
    }
    public StringProperty textProperty() {
        if (null == text) {
            text = new SimpleStringProperty(this, "text", "");
        }
        return text;
    }

    public boolean isSelectable() {
        return null == selectable ? _selectable : selectable.get();
    }
    public void setSelectable(final boolean SELECTABLE) {
        if (null == selectable) {
            _selectable = SELECTABLE;
        } else {
            selectable.set(SELECTABLE);
        }
    }
    public BooleanProperty selectableProperty() {
        if (null == selectable) {
            selectable = new SimpleBooleanProperty(this, "selectable", _selectable);
        }
        return selectable;
    }

    public final boolean isSelected() {
        return null == selected ? false : selected.get();
    }
    public final void setSelected(final boolean SELECTED) {
        if (isSelectable()) {
            if (null == selected) initSelected();
            selected.set(SELECTED);
        }
    }
    public final ReadOnlyBooleanProperty selectedProperty() {
        if (null == selected) initSelected();
        return selected;
    }
    private void initSelected() {
        selected = new BooleanPropertyBase(false) {
            @Override protected void invalidated() { pseudoClassStateChanged(SELECT_PSEUDO_CLASS, get());}
            @Override public Object getBean() { return this; }
            @Override public String getName() { return "select"; }
        };
    }


    // ******************** CSS Meta Data *************************************
    private static class StyleableProperties {
        private static final CssMetaData<RadialMenuItem, Color> BACKGROUND_COLOR =
            new CssMetaData<RadialMenuItem, Color>("-item-background",
                                                   ColorConverter.getInstance(),
                                                   DEFAULT_BACKGROUND_COLOR) {

                @Override public boolean isSettable(RadialMenuItem node) {
                    return null == node.backgroundColor || !node.backgroundColor.isBound();
                }

                @Override public StyleableProperty<Color> getStyleableProperty(RadialMenuItem node) {
                    return (StyleableProperty) node.backgroundColorProperty();
                }

                @Override public Color getInitialValue(RadialMenuItem node) {
                    return node.getBackgroundColor();
                }
            };

        private static final CssMetaData<RadialMenuItem, Color> BORDER_COLOR =
            new CssMetaData<RadialMenuItem, Color>("-item-border", ColorConverter.getInstance(), DEFAULT_BORDER_COLOR) {

                @Override public boolean isSettable(RadialMenuItem node) {
                    return null == node.borderColor || !node.borderColor.isBound();
                }

                @Override public StyleableProperty<Color> getStyleableProperty(RadialMenuItem node) {
                    return (StyleableProperty) node.borderColorProperty();
                }

                @Override public Color getInitialValue(RadialMenuItem node) {
                    return node.getBorderColor();
                }
            };

        private static final CssMetaData<RadialMenuItem, Color> FOREGROUND_COLOR =
            new CssMetaData<RadialMenuItem, Color>("-item-foreground",
                                                   ColorConverter.getInstance(),
                                                   DEFAULT_FOREGROUND_COLOR) {

                @Override public boolean isSettable(RadialMenuItem node) {
                    return null == node.foregroundColor || !node.foregroundColor.isBound();
                }

                @Override public StyleableProperty<Color> getStyleableProperty(RadialMenuItem node) {
                    return (StyleableProperty) node.foregroundColorProperty();
                }

                @Override public Color getInitialValue(RadialMenuItem node) {
                    return node.getForegroundColor();
                }
            };

        private static final CssMetaData<RadialMenuItem, Color> SELECTED_BACKGROUND_COLOR =
            new CssMetaData<RadialMenuItem, Color>("-item-selected-background",
                                                   ColorConverter.getInstance(),
                                                   DEFAULT_SELECTED_BACKGROUND_COLOR) {

                @Override public boolean isSettable(RadialMenuItem node) {
                    return null == node.selectedBackgroundColor || !node.selectedBackgroundColor.isBound();
                }

                @Override public StyleableProperty<Color> getStyleableProperty(RadialMenuItem node) {
                    return (StyleableProperty) node.selectedBackgroundColorProperty();
                }

                @Override public Color getInitialValue(RadialMenuItem node) {
                    return node.getSelectedBackgroundColor();
                }
            };

        private static final CssMetaData<RadialMenuItem, Color> SELECTED_FOREGROUND_COLOR =
            new CssMetaData<RadialMenuItem, Color>("-item-selected-foreground",
                                                   ColorConverter.getInstance(),
                                                   DEFAULT_SELECTED_FOREGROUND_COLOR) {

                @Override public boolean isSettable(RadialMenuItem node) {
                    return null == node.selectedForegroundColor || !node.selectedForegroundColor.isBound();
                }

                @Override public StyleableProperty<Color> getStyleableProperty(RadialMenuItem node) {
                    return (StyleableProperty) node.selectedForegroundColorProperty();
                }

                @Override public Color getInitialValue(RadialMenuItem node) {
                    return node.getSelectedForegroundColor();
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                               BACKGROUND_COLOR,
                               BORDER_COLOR,
                               FOREGROUND_COLOR,
                               SELECTED_BACKGROUND_COLOR,
                               SELECTED_FOREGROUND_COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
}
