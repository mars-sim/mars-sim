package org.mars_sim.msp.ui.javafx.demo.spinner;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Defines the Spinner Demo application for JavaFX.
 * @author Christoph Nahr
 * @version 1.0.1
 */
public class SpinnerDemo extends Application {
    /**
     * Starts the {@link SpinnerDemo} application.
     * @param primaryStage the primary {@link Stage} for the application
     */
    @Override
    public void start(Stage primaryStage) {
        int row = -1;

        final Label intRange = new Label("Integer from -100 to +100");
        GridPane.setHalignment(intRange, HPos.CENTER);
        GridPane.setRowIndex(intRange, ++row);
        GridPane.setColumnIndex(intRange, 0);
        GridPane.setColumnSpan(intRange, 3);

        final Label intBasic = new Label("Basic Converter");
        GridPane.setRowIndex(intBasic, ++row);
        GridPane.setColumnIndex(intBasic, 0);

        final Spinner<Integer> intBasicSpinner = new Spinner<>(-100, 100, 0, 10);
        initSpinner(intBasicSpinner);
        GridPane.setRowIndex(intBasicSpinner, row);
        GridPane.setColumnIndex(intBasicSpinner, 1);

        final Label intBasicOutput = new Label();
        initOutput(intBasicOutput);
        GridPane.setRowIndex(intBasicOutput, row);
        GridPane.setColumnIndex(intBasicOutput, 2);

        final Label intCustom = new Label("Custom Converter");
        GridPane.setRowIndex(intCustom, ++row);
        GridPane.setColumnIndex(intCustom, 0);

        final Spinner<Integer> intCustomSpinner = new Spinner<>(-100, 100, 0, 10);
        initSpinner(intCustomSpinner);
        IntegerStringConverter.createFor(intCustomSpinner);
        GridPane.setRowIndex(intCustomSpinner, row);
        GridPane.setColumnIndex(intCustomSpinner, 1);

        final Label intCustomOutput = new Label();
        initOutput(intCustomOutput);
        GridPane.setRowIndex(intCustomOutput, row);
        GridPane.setColumnIndex(intCustomOutput, 2);

        final Separator intSeparator = new Separator(Orientation.HORIZONTAL);
        GridPane.setRowIndex(intSeparator, ++row);
        GridPane.setColumnIndex(intSeparator, 0);
        GridPane.setColumnSpan(intSeparator, 3);

        final Label dblRange = new Label("Double from -100 to +100");
        GridPane.setHalignment(dblRange, HPos.CENTER);
        GridPane.setRowIndex(dblRange, ++row);
        GridPane.setColumnIndex(dblRange, 0);
        GridPane.setColumnSpan(dblRange, 3);

        final Label dblBasic = new Label("Basic Converter");
        GridPane.setRowIndex(dblBasic, ++row);
        GridPane.setColumnIndex(dblBasic, 0);

        final Spinner<Double> dblBasicSpinner = new Spinner<>(-100.0, 100.0, 0.0, 10.0);
        initSpinner(dblBasicSpinner);
        GridPane.setRowIndex(dblBasicSpinner, row);
        GridPane.setColumnIndex(dblBasicSpinner, 1);

        final Label dblBasicOutput = new Label();
        initOutput(dblBasicOutput);
        GridPane.setRowIndex(dblBasicOutput, row);
        GridPane.setColumnIndex(dblBasicOutput, 2);

        final Label dblCustom = new Label("Custom Converter");
        GridPane.setRowIndex(dblCustom, ++row);
        GridPane.setColumnIndex(dblCustom, 0);

        final Spinner<Double> dblCustomSpinner = new Spinner<>(-100.0, 100.0, 0.0, 10.0);
        initSpinner(dblCustomSpinner);
        DoubleStringConverter.createFor(dblCustomSpinner);
        GridPane.setRowIndex(dblCustomSpinner, row);
        GridPane.setColumnIndex(dblCustomSpinner, 1);

        final Label dblCustomOutput = new Label();
        initOutput(dblCustomOutput);
        GridPane.setRowIndex(dblCustomOutput, row);
        GridPane.setColumnIndex(dblCustomOutput, 2);

        final Separator dblSeparator = new Separator(Orientation.HORIZONTAL);
        GridPane.setRowIndex(dblSeparator, ++row);
        GridPane.setColumnIndex(dblSeparator, 0);
        GridPane.setColumnSpan(dblSeparator, 3);

        final Label exception = new Label();
        exception.setMinHeight(36); // reserve space for two lines
        exception.setTextFill(Color.RED);
        exception.setWrapText(true);
        GridPane.setRowIndex(exception, ++row);
        GridPane.setColumnIndex(exception, 0);
        GridPane.setColumnSpan(exception, 3);

        final GridPane root = new GridPane();
        root.setHgap(8);
        root.setVgap(8);
        root.setPadding(new Insets(8));
        root.getChildren().addAll(intRange,
                intBasic, intBasicSpinner, intBasicOutput,
                intCustom, intCustomSpinner, intCustomOutput,
                intSeparator, dblRange,
                dblBasic, dblBasicSpinner, dblBasicOutput,
                dblCustom, dblCustomSpinner, dblCustomOutput,
                dblSeparator, exception);

        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            exception.setText(e.toString());
            e.printStackTrace();
        });
        intBasicSpinner.getValueFactory().valueProperty().addListener((ov, oldValue, newValue) -> {
            exception.setText("");
            intBasicOutput.setText(Integer.toString(newValue));
        });
        intCustomSpinner.getValueFactory().valueProperty().addListener((ov, oldValue, newValue) -> {
            exception.setText("");
            intCustomOutput.setText(Integer.toString(newValue));
        });
        dblBasicSpinner.getValueFactory().valueProperty().addListener((ov, oldValue, newValue) -> {
            exception.setText("");
            dblBasicOutput.setText(Double.toString(newValue));
        });
        dblCustomSpinner.getValueFactory().valueProperty().addListener((ov, oldValue, newValue) -> {
            exception.setText("");
            dblCustomOutput.setText(Double.toString(newValue));
        });

        primaryStage.setTitle("Spinner Demo");
        primaryStage.setScene(new Scene(root));
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    /**
     * Launches the {@link SpinnerDemo} application.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private static void initSpinner(Spinner<?> spinner) {
        spinner.getEditor().setAlignment(Pos.CENTER_RIGHT);
        spinner.setEditable(true);
        spinner.setPrefWidth(80);
    }

    private static void initOutput(Label output) {
        output.setAlignment(Pos.CENTER);
        output.setPrefWidth(80);
    }
}
