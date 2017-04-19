package org.mars_sim.msp.ui.javafx.demo.spinnerValueFactory;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.List;

public class Spinner<T> extends Control {

    // default style class, puts arrows on right, stacked vertically
    private static final String DEFAULT_STYLE_CLASS = "spinner";

    /** The arrows are placed on the right of the Spinner, pointing horizontally (i.e. left and right). */
    public static final String STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL = "arrows-on-right-horizontal";

    /** The arrows are placed on the left of the Spinner, pointing vertically (i.e. up and down). */
    public static final String STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL = "arrows-on-left-vertical";

    /** The arrows are placed on the left of the Spinner, pointing horizontally (i.e. left and right). */
    public static final String STYLE_CLASS_ARROWS_ON_LEFT_HORIZONTAL = "arrows-on-left-horizontal";

    /** The arrows are placed above and beneath the spinner, stretching to take the entire width. */
    public static final String STYLE_CLASS_SPLIT_ARROWS_VERTICAL = "split-arrows-vertical";

    /** The decrement arrow is placed on the left of the Spinner, and the increment on the right. */
    public static final String STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL = "split-arrows-horizontal";



    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public Spinner() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        getEditor().setOnAction(action -> {
            String text = getEditor().getText();
            SpinnerValueFactory<T> valueFactory = getValueFactory();
            if (valueFactory != null) {
                T value = valueFactory.fromString(text);
                valueFactory.setValue(value);
            }
        });

        getEditor().editableProperty().bind(editableProperty());

        value.addListener((o, oldValue, newValue) -> setText(newValue));

        // Fix for RT-29885
        getProperties().addListener((MapChangeListener<Object, Object>) change -> {
            if (change.wasAdded()) {
                if (change.getKey() == "FOCUSED") {
                    setFocused((Boolean)change.getValueAdded());
                    getProperties().remove("FOCUSED");
                }
            }
        });
        // End of fix for RT-29885
    }

    /**
     * Creates a Spinner instance with the
     * {@link #valueFactoryProperty() value factory} set to be an instance
     * of {@link spinner.SpinnerValueFactory.IntSpinnerValueFactory}.
     *
     * @param min
     * @param max
     * @param initialValue
     */
    public Spinner(int min, int max, int initialValue) {
        // This only works if the Spinner is of type Integer
        this((SpinnerValueFactory<T>)new SpinnerValueFactory.IntSpinnerValueFactory(min, max, initialValue));
    }

    public Spinner(List<T> items) {
        this(new SpinnerValueFactory.ListSpinnerValueFactory<T>(items));
    }

    public Spinner(SpinnerValueFactory<T> valueFactory) {
        this();

        setValueFactory(valueFactory);
    }

    @Override protected Skin<?> createDefaultSkin() {
        return new SpinnerSkin<T>(this);
    }

    @Override
	public String getUserAgentStylesheet() {
        return this.getClass().getResource("/fxui/css/spinner/spinner.css").toExternalForm();
    }


    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public void increment() {
        increment(1);
    }

    public void increment(int steps) {
        SpinnerValueFactory<T> valueFactory = getValueFactory();
        if (valueFactory == null) {
            throw new IllegalStateException("Cann't increment Spinner with a null SpinnerValueFactory");
        }
        valueFactory.increment(steps);
    }

    public void decrement() {
        decrement(1);
    }

    public void decrement(int steps) {
        SpinnerValueFactory<T> valueFactory = getValueFactory();
        if (valueFactory == null) {
            throw new IllegalStateException("Can't decrement Spinner with a null SpinnerValueFactory");
        }
        valueFactory.decrement(steps);
    }



    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // --- value (a read only, bound property to the value factory value property)
    private ReadOnlyObjectWrapper<T> value = new ReadOnlyObjectWrapper<T>(this, "value");
    public final T getValue() {
        return value.get();
    }
    public final ReadOnlyObjectProperty<T> valueProperty() {
        return value;
    }


    // --- valueFactory
    private ObjectProperty<SpinnerValueFactory<T>> valueFactory =
            new SimpleObjectProperty<SpinnerValueFactory<T>>(this, "valueFactory") {
                WeakReference<SpinnerValueFactory<T>> valueFactoryRef;

                @Override protected void invalidated() {
                    if (valueFactoryRef != null) {
                        SpinnerValueFactory<T> oldValueFactory = valueFactoryRef.get();
                        oldValueFactory.setSpinner(null);
                    }

                    value.unbind();

                    SpinnerValueFactory<T> newFactory = get();
                    if (newFactory != null) {
                        newFactory.setSpinner(Spinner.this);

                        // this binding is what ensures the Spinner.valueProperty()
                        // properly represents the value in the value factory
                        value.bind(newFactory.valueProperty());
                    }

                    valueFactoryRef = new WeakReference<SpinnerValueFactory<T>>(newFactory);
                }
            };
    public final void setValueFactory(SpinnerValueFactory<T> value) {
        valueFactory.setValue(value);
    }
    public final SpinnerValueFactory<T> getValueFactory() {
        return valueFactory.get();
    }
    public final ObjectProperty<SpinnerValueFactory<T>> valueFactoryProperty() {
        return valueFactory;
    }


    // --- wrapAround
    private BooleanProperty wrapAround;
    public final void setWrapAround(boolean value) {
        wrapAroundProperty().set(value);
    }
    public final boolean isWrapAround() {
        return wrapAround == null ? false : wrapAround.get();
    }
    public final BooleanProperty wrapAroundProperty() {
        if (wrapAround == null) {
            wrapAround = new SimpleBooleanProperty(this, "wrapAround", false);
        }
        return wrapAround;
    }


    // --- editable
    private BooleanProperty editable;
    public final void setEditable(boolean value) {
        editableProperty().set(value);
    }
    public final boolean isEditable() {
        return editable == null ? true : editable.get();
    }
    public final BooleanProperty editableProperty() {
        if (editable == null) {
            editable = new SimpleBooleanProperty(this, "editable", false);
        }
        return editable;
    }


    // --- editor
    private TextField textField;
    private ReadOnlyObjectWrapper<TextField> editor;
    public final TextField getEditor() {
        return editorProperty().get();
    }
    public final ReadOnlyObjectProperty<TextField> editorProperty() {
        if (editor == null) {
            editor = new ReadOnlyObjectWrapper<TextField>(this, "editor");
            textField = new ComboBoxListViewSkin.FakeFocusTextField();
            editor.set(textField);
        }
        return editor.getReadOnlyProperty();
    }



    /***************************************************************************
     *                                                                         *
     * Implementation                                                          *
     *                                                                         *
     **************************************************************************/

    /*
     * Update the TextField based on the current value
     */
    private void setText(T value) {
        SpinnerValueFactory<T> valueFactory = getValueFactory();
        if (valueFactory != null) {
            String text = valueFactory.toString(value);
            getEditor().setText(text);
        } else {
            if (value == null) {
                getEditor().clear();
            } else {
                getEditor().setText(value.toString());
            }
        }
    }

    static int wrapValue(int value, int min, int max) {
        if (max == 0) {
            throw new RuntimeException();
        }

        int r = value % max;
        if (r > min && max < min) {
            r = r + max - min;
        } else if (r < min && max > min) {
            r = r + max - min;
        }
        return r;
    }

    static BigDecimal wrapValue(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (max.doubleValue() == 0) {
            throw new RuntimeException();
        }

        // note that this wrap method differs from the others where we take the
        // difference - in this approach we wrap to the min or max - it feels better
        // to go from 1 to 0, rather than 1 to 0.05 (where max is 1 and step is 0.05).
        if (value.compareTo(min) < 0) {
            return max;
        } else if (value.compareTo(max) > 0) {
            return min;
        }
        return value;
    }
}