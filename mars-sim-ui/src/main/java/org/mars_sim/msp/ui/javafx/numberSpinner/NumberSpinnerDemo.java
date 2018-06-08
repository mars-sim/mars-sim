package org.mars_sim.msp.ui.javafx.numberSpinner;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 *
 * @author Thomas Bolz
 */
public class NumberSpinnerDemo extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX Spinner Demo");
        GridPane root = new GridPane();
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(10, 10, 10, 10));
        final NumberSpinner defaultSpinner = new NumberSpinner();
        final NumberSpinner decimalFormat = new NumberSpinner(BigDecimal.ZERO, new BigDecimal("0.05"), new DecimalFormat("#,##0.00"));
        final NumberSpinner percent = new NumberSpinner(BigDecimal.ZERO, new BigDecimal("0.01"), NumberFormat.getPercentInstance());
        final NumberSpinner localizedCurrency = new NumberSpinner(BigDecimal.ZERO, new BigDecimal("0.01"), NumberFormat.getCurrencyInstance(Locale.UK));
        root.addRow(1, new Label("default"), defaultSpinner);
        root.addRow(2, new Label("custom decimal format"), decimalFormat);
        root.addRow(3, new Label("percent"), percent);
        root.addRow(4, new Label("localized currency"), localizedCurrency);
        Button button = new Button("Dump layout bounds");
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                defaultSpinner.dumpSizes();
            }
        });
        root.addRow(5, new Label(), button);

        Scene scene = new Scene(root);
        //String path = NumberSpinnerDemo.class.getResource("/fxui/css/spinner/number_spinner.css").toExternalForm();
        String path = "/fxui/css/spinner/number_spinner.css";
        System.out.println("path = " + path);
        //scene.getStylesheets().add(path);
        scene.getStylesheets().add(getClass().getResource(path).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
