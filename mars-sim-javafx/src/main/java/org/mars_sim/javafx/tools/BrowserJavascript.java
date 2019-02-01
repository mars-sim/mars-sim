package org.mars_sim.javafx.tools;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class BrowserJavascript extends Application {

    @Override
    public void start(Stage primaryStage) {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                addFunctionHandlerToDocument(engine);
            }
        });

        // Just a demo: in real life can load external HTML resource:
        engine.loadContent(
                "<html><head><script>"
                + "var count = 0 ;"
                + "function someFunction(x) {"
                + "    count ++ ;"
                + "    document.getElementById(x).innerHTML = 'Count: '+count ;"
                + "}"
                + "</script></head>"
                + "<body>"
                + "    <input type=\"button\" value=\"Click Me\" onclick=\"someFunction('display');\"/>"
                + "    <div id='display'></div>"
                + "</body>"
                + "</html>"
        );

        Button registerButton = new Button("Register handler for 'someFunction'");
        registerButton.setOnAction(event -> {
            registerFunction("someFunction", engine);
            // registering the same function twice will break everything
            // so don't allow this to happen again:
            registerButton.setDisable(true);
        });

        HBox controls = new HBox(5, registerButton);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane(webView, null, null, controls, null);
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private void registerFunction(String functionName, WebEngine engine) {
        engine.executeScript(
            "var fun = " + functionName + " ;"
            + functionName + " = function() {"
            + "    app.functionCalled('" + functionName + "');"
            + "    fun.apply(this, arguments)"
            + "}"
        );
    }

    private void addFunctionHandlerToDocument(WebEngine engine) {
        JSObject window = (JSObject) engine.executeScript("window");
        window.setMember("app", this);
    }

    public void functionCalled(String name) {
        System.out.println(name + " was called");
    }

    public static void main(String[] args) {
        launch(args);
    }
}