package org.mars_sim.msp.ui.javafx.demo.webfxml;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by rene on 31/05/16.
 */
public class LoadWebController implements Initializable {

    @FXML
    private WebView webView;


    public void initialize(URL location, ResourceBundle resources) {
        WebEngine engine = webView.getEngine();
        String url = getClass().getResource("/webfxml/web/loadweb.html").toExternalForm();
        engine.load(url);
    }
}
