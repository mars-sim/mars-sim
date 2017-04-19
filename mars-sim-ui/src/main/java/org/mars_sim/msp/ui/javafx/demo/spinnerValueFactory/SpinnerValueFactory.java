package org.mars_sim.msp.ui.javafx.demo.spinnerValueFactory;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;

public abstract class SpinnerValueFactory<T> extends StringConverter<T> {

    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/



    /***************************************************************************
     *                                                                         *
     * Abstract methods                                                        *
     *                                                                         *
     **************************************************************************/

    public abstract void decrement(int steps);
    public abstract void increment(int steps);



    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // --- value
    private ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");
    public final T getValue() {
        return value.get();
    }
    public final void setValue(T newValue) {
        value.set(newValue);
    }
    public final ObjectProperty<T> valueProperty() {
        return value;
    }


    // --- spinner
    /**
     * Sets the {@link Spinner} that this value factory is installed within.
     */
    private ReadOnlyObjectWrapper<Spinner<T>> spinner = new ReadOnlyObjectWrapper<>(this, "spinner");
    public final Spinner<T> getSpinner() {
        return spinner.get();
    }
    final void setSpinner(Spinner<T> value) {
        spinner.set(value);
    }
    public final ReadOnlyObjectProperty<Spinner<T>> spinnerProperty() {
        return spinner.getReadOnlyProperty();
    }



    /***************************************************************************
     *                                                                         *
     * API                                                                     *
     *                                                                         *
     **************************************************************************/

    /**
     * This method is called by the Spinner implementation when the spinner
     * increment / decrement buttons have been pressed and held down. It
     * allows for SpinnerValueFactory implementations to customize the
     * amount of steps that should occur every time the Spinner increments
     * or decrements (whilst the button is held down - once it is released
     * it returns to the hard-coded value of one).
     *
     * <p>This method can be paired with {@link #calculateStepDuration(int, double)}
     * to allow for spinner value factories to 'speed up' their spinning as
     * the press duration and step count increase.</p>
     *
     * @param stepCount The number of times the spinner has iterated in the
     *                  current interaction (i.e. since the mouse was pressed).
     * @param pressDuration The duration since the mouse was first pressed.
     * @return An int stating what argument should be passed to the
     *      {@link Spinner#increment(int)} or {@link Spinner#decrement(int)}
     *      methods, to indicate how many steps should be performed.
     */
    public int calculateStepAmount(int stepCount, double pressDuration) {
        return 1;
    }

    /**
     * This method is called by the Spinner implementation when the spinner
     * increment / decrement buttons have been pressed and held down. It
     * allows for SpinnerValueFactory implementations to customize the
     * time between steps (whilst the button is held down - once it is released
     * it returns to the hard-coded default value of 750ms).
     *
     * <p>This method can be paired with {@link #calculateStepAmount(int, double)}
     * to allow for spinner value factories to 'speed up' their spinning as
     * the press duration and step count increase.</p>
     *
     * @param stepCount The number of times the spinner has iterated in the
     *                  current interaction (i.e. since the mouse was pressed).
     * @param pressDuration The duration since the mouse was first pressed.
     * @return An long stating how long the Spinner should wait until it
     *      next calls the increment / decrement methods on the Spinner.
     */
    public long calculateStepDuration(int stepCount, double pressDuration) {
        return 750;
    }



    public static class ListSpinnerValueFactory<T> extends SpinnerValueFactory<T> {

        private final List<T> items;
        private int currentIndex = 0;

        public ListSpinnerValueFactory(List<T> items) {
            this.items = items;

            valueProperty().addListener((o, oldValue, newValue) -> {
                // when the value is set, we need to react to ensure it is a
                // valid value (and if not, blow up appropriately)
                int newIndex = -1;
                if (items.contains(newValue)) {
                    newIndex = items.indexOf(newValue);
                } else {
                    // add newValue to list
                    items.add(newValue);
                    newIndex = items.indexOf(newValue);
                }
                currentIndex = newIndex;
            });
            setValue(_getValue(currentIndex));
        }

        @Override public void decrement(int steps) {
            final int max = items.size() - 1;
            int newIndex = currentIndex - steps;
            currentIndex = newIndex >= 0 ? newIndex : (getSpinner().isWrapAround() ? Spinner.wrapValue(newIndex, 0, max + 1) : 0);
            setValue(_getValue(currentIndex));
        }

        @Override public void increment(int steps) {
            final int max = items.size() - 1;
            int newIndex = currentIndex + steps;
            currentIndex = newIndex <= max ? newIndex : (getSpinner().isWrapAround() ? Spinner.wrapValue(newIndex, 0, max + 1) : max);
            setValue(_getValue(currentIndex));
        }

        @Override public String toString(T object) {
            return object == null ? "" : object.toString();
        }

        @Override public T fromString(String string) {
            return (T) string;
        }

        private T _getValue(int index) {
            return (index >= 0 && index < items.size()) ? items.get(index) : null;
        }
    }

    public static class IntSpinnerValueFactory extends SpinnerValueFactory<Integer> {

        private final int min;
        private final int max;
        private final int amountToStepBy;

        public IntSpinnerValueFactory(int min, int max) {
            this(min, max, min);
        }

        public IntSpinnerValueFactory(int min, int max, int initialValue) {
            this(min, max, initialValue, 1);
        }

        public IntSpinnerValueFactory(int min, int max, int initialValue, int amountToStepBy) {
            this.min = min;
            this.max = max;
            this.amountToStepBy = amountToStepBy;

            valueProperty().addListener((o, oldValue, newValue) -> {
                // when the value is set, we need to react to ensure it is a
                // valid value (and if not, blow up appropriately)
                if (newValue < min || newValue > max) {
                    setValue(oldValue);
                }
            });
            setValue(initialValue >= min && initialValue <= max ? initialValue : min);
        }

        @Override public void decrement(int steps) {
            int newIndex = getValue() - steps * amountToStepBy;
            setValue(newIndex >= min ? newIndex : (getSpinner().isWrapAround() ? Spinner.wrapValue(newIndex, min, max) + 1 : min));
        }

        @Override public void increment(int steps) {
            final int currentValue = getValue();
            final int newIndex = currentValue + steps * amountToStepBy;
            setValue(newIndex <= max ? newIndex : (getSpinner().isWrapAround() ? Spinner.wrapValue(newIndex, min, max) - 1 : max));
        }

        @Override public String toString(Integer integer) {
            return integer.toString();
        }

        @Override public Integer fromString(String string) {
            return Integer.valueOf(string);
        }
    }


    // internally we use BigDecimal for accuracy
    public static class DoubleSpinnerValueFactory extends SpinnerValueFactory<Double> {

        private final BigDecimal minBigDecimal;
        private final BigDecimal maxBigDecimal;
        private final BigDecimal amountToStepByBigDecimal;

        private final double min;
        private final double max;

        private final DecimalFormat df = new DecimalFormat("#.##");

        public DoubleSpinnerValueFactory(double min, double max) {
            this(min, max, min);
        }

        public DoubleSpinnerValueFactory(double min, double max, double initialValue) {
            this(min, max, initialValue, 1);
        }

        public DoubleSpinnerValueFactory(double min, double max, double initialValue, double amountToStepBy) {
            this.min = min;
            this.minBigDecimal = BigDecimal.valueOf(min);
            this.max = max;
            this.maxBigDecimal = BigDecimal.valueOf(max);
            this.amountToStepByBigDecimal = BigDecimal.valueOf(amountToStepBy);

            valueProperty().addListener((o, oldValue, newValue) -> {
                // when the value is set, we need to react to ensure it is a
                // valid value (and if not, blow up appropriately)
                if (newValue < min || newValue > max) {
                    setValue(oldValue);
                }
            });
            setValue(initialValue >= min && initialValue <= max ? initialValue : min);
        }

        @Override public void decrement(int steps) {
            BigDecimal currentValue = BigDecimal.valueOf(getValue());
            BigDecimal newValue = currentValue.subtract(amountToStepByBigDecimal.multiply(BigDecimal.valueOf(steps)));
            setValue(newValue.compareTo(minBigDecimal) >= 0 ? newValue.doubleValue() :
                    (getSpinner().isWrapAround() ? Spinner.wrapValue(newValue, minBigDecimal, maxBigDecimal).doubleValue() : min));
        }

        @Override public void increment(int steps) {
            BigDecimal currentValue = BigDecimal.valueOf(getValue());
            BigDecimal newValue = currentValue.add(amountToStepByBigDecimal.multiply(BigDecimal.valueOf(steps)));
            setValue(newValue.compareTo(maxBigDecimal) <= 0 ? newValue.doubleValue() :
                    (getSpinner().isWrapAround() ? Spinner.wrapValue(newValue, minBigDecimal, maxBigDecimal).doubleValue() : max));
        }

        @Override public String toString(Double value) {
            return df.format(value);
        }

        @Override public Double fromString(String string) {
            return Double.valueOf(string);
        }
    }


    public static class LocalDateSpinnerValueFactory extends SpinnerValueFactory<LocalDate> {

        private long amountToStepBy;
        private TemporalUnit temporalUnit;

        public LocalDateSpinnerValueFactory() {
            this(LocalDate.now());
        }

        public LocalDateSpinnerValueFactory(LocalDate initialValue) {
            this(initialValue, 1, ChronoUnit.DAYS);
        }

        public LocalDateSpinnerValueFactory(LocalDate initialValue, long amountToStepBy, TemporalUnit temporalUnit) {
            this.amountToStepBy = amountToStepBy;
            this.temporalUnit = temporalUnit;

            valueProperty().addListener((o, oldValue, newValue) -> {
                // when the value is set, we need to react to ensure it is a
                // valid value (and if not, blow up appropriately)

                // in the case of LocalDateSpinnerValueFactory, we have no
                // error conditions (yet), so this is a no-op
            });
            setValue(initialValue != null ? initialValue : LocalDate.now());
        }

        @Override public void decrement(int steps) {
            setValue(getValue().minus(amountToStepBy * steps, temporalUnit));
        }

        @Override public void increment(int steps) {
            setValue(getValue().plus(amountToStepBy * steps, temporalUnit));
        }

        @Override public String toString(LocalDate object) {
            return object.toString();
        }

        @Override public LocalDate fromString(String string) {
            return LocalDate.parse(string);
        }
    }
}