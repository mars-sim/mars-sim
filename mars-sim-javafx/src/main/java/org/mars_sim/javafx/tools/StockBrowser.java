package org.mars_sim.javafx.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;

public class StockBrowser extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        
        String usersHomeDir = System.getProperty("user.home");
        String canonDataFile = usersHomeDir + "/" + "StockBrowser.data";
        String[] stockSymbols = readFileToStringArray(canonDataFile);
        
        Group root = new Group();
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double initialWidth = primaryScreenBounds.getWidth()*2/3;
        double initialHeight = primaryScreenBounds.getHeight()*2/3;
        Scene scene = new Scene(root, initialWidth, initialHeight);
        
        // LIST VIEW (URLS)
        ListView<String> listView = new ListView<String>();
        ObservableList<String> data = FXCollections.observableArrayList (stockSymbols);
        listView.setItems(data);
        listView.setPrefWidth(150);
        
        // BROWSER
        final WebView webView = new WebView();
        final WebEngine webEngine = webView.getEngine();

        listView.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<String>() {
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    String url = getUrlFromStockSymbol(newValue);
                    webEngine.load(url);
            }
        });
        
        // THESE METHODS DO GET CALLED (THIS RESIZE APPROACH WORKS)
        // https://blog.idrsolutions.com/2012/11/adding-a-window-resize-listener-to...
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                webView.setPrefWidth(newSceneWidth.doubleValue()*0.9);
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                webView.setPrefHeight(newSceneHeight.doubleValue()*0.98);
            }
        });
        
        // BORDER PANE
        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(listView);
        borderPane.setCenter(webView);
        root.getChildren().add(borderPane);
        
        primaryStage.setTitle("Stocks Browser");
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    private static String getUrlFromStockSymbol(String symbol) {
        // google finance (their urls depend on nasdaq, etc.)
        //return "https://www.google.com/finance?q=NASDAQ%3AAAPL";
//        String url1 = "http://finance.yahoo.com/echarts?s=";
//        String url2 = "+Interactive#symbol=yhoo;range=1y;compare=;indicator=volume;charttype=area;crosshair=on;ohlcvalues=0;logscale=off;source=undefined;";
//        return url1 + symbol.toUpperCase() + url2;
        return "http://finance.yahoo.com/q?s=" + symbol.trim().toUpperCase();
    }

    private String[] readFileToStringArray(String canonFilename) throws IOException {
        FileReader fileReader = new FileReader(canonFilename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();
        return lines.toArray(new String[lines.size()]);
    }
    
}