package org.mars_sim.msp.ui.swing;

import com.ui4j.api.browser.BrowserEngine;
import com.ui4j.api.browser.BrowserFactory;
import com.ui4j.api.browser.Page;
import static com.ui4j.api.browser.BrowserFactory.getWebKit;


public class BrowserUi4j {

	private static String site = "http://mars-sim.sourceforge.net";//"http://lunarone.sourceforge.net";//"http://mars-sim.sourceforge.net";

    public static void main(String[] args) {
        // get the instance of the webkit
        BrowserEngine browser = BrowserFactory.getWebKit();

        // navigate to blank page
        Page page = browser.navigate(site); // "about:blank"

        // show the browser page
        page.show();

        // append html header to the document body
        //page.getDocument().getBody().append("<h1>Hello, World!</h1>");

/*
        try (Page p = getWebKit().navigate("https://news.ycombinator.com")) {
            p
                .getDocument()
                .queryAll(".title a")
                .forEach(e -> {
                    System.out.println(e.getText().get());
                });
        }
*/
    }
}