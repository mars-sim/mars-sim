/* Mars Simulation Project
 * BrowserJFX.java
 * @version 3.1.0 2016-10-07
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebHistory;
import javafx.scene.Cursor;
//import javafx.scene.text.Text;
import javafx.scene.Node;
import netscape.javascript.JSObject;
//import javax.swing.*;
//import javax.swing.event.HyperlinkEvent;

import org.codefx.libfx.control.webview.WebViewHyperlinkListener;
import org.codefx.libfx.control.webview.WebViews;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
//import java.util.EventListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent.EventType;

import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State;

public class BrowserJFX {

    /** initialized logger for this class. */
    private static Logger logger = Logger.getLogger(BrowserJFX.class.getName());

    public static final String MAP_FILE = "map";
    public static final String GLOBE_FILE = "globe";

    public static final String EVENT_TYPE_CLICK = "click";
    public static final String EVENT_TYPE_MOUSEOVER = "mouseover";
    public static final String EVENT_TYPE_MOUSEOUT = "mouseclick";

    public static final String MSP_HEADER = "msp://";
    public static final String HTTP_HEADER = "http://";
    public static final String HTTPS_HEADER = "https://";
    public static final String DOCS_HELP_DIR = "/docs/help/";

    public static final int INTERNAL_COMMAND = 0;
    public static final int LOCAL_HTML = 1;
    public static final int REMOTE_HTML = 2;
    public static final int UNKNOWN = 3;

    public static final int WIDTH = 25;

    public static final String DEFAULT_JQUERY_MIN_VERSION = "1.7.2";
    public static final String JQUERY_LOCATION = "http://code.jquery.com/jquery-1.7.2.min.js";



    private static final String CSS =
    		"a, a:link, a:visited, a:hover{color:rgb(184, 134, 11); text-decoration:none;}"
          + "body {"
          + "    background-color: rgb(50, 50, 50); "
          + "    font-family: Arial, Helvetica, san-serif;"
          + "}"
          + "body, h3{font-size:14pt;line-height:1.1em; color:white;}"
          + "h2{font-size:14pt; font-weight:700; line-height:1.2em; text-align:center; color:white;}"
          + "h3{font-weight:700; color:white;}"
          + "h4{font-weight:500; color:white; line-height:0.8em;}"
          + "p{margin-left:13pt; color:white;}"
          + "hr{width:90%;}";


	private boolean isLocalHtml = true;
	private Point2D pLimit;
	private double width, height;
    public volatile String textInputCache, addressURLText, statusBarURLText, inputCache;

    private JFXPanel jfxPanel = new JFXPanel();
    private JPanel panel = new JPanel(new BorderLayout());

    private JLabel statusBarLbl = new JLabel();

    //private JButton btnGo = new JButton("Go");
    //private JButton btnForward = new JButton(">");
    //private JButton btnBack = new JButton("<");
    //private JTextField urlTF = new JTextField();

    private JProgressBar progressBar = new JProgressBar();

    private Button reloadButton = new Button('\u27F3' + "");
    private Button backButton = new Button('\u25c0' + "");//\u21e6' + "");
    private Button forwardButton = new Button('\u25b6' + "");//u21e8' + "");
    private TextField tf = new TextField();
    private ComboBox<String> comboBox = new ComboBox<String>();
    private HBox bar = new HBox();
    private VBox vbox = new VBox();
    private HBox topButtonBar = new HBox();

    private MainScene mainScene;
    private MainDesktopPane desktop;
    private WebView view = new WebView();
    private WebEngine engine = view.getEngine();
    private WebHistory history = engine.getHistory();
    private GuideWindow ourGuide;

	private ObservableList<WebHistory.Entry> entryList = history.getEntries();
	private SingleSelectionModel<String> ssm = comboBox.getSelectionModel();


	// see http://www.java2s.com/Tutorials/Java/JavaFX/1500__JavaFX_WebEngine.htm

    public BrowserJFX(MainDesktopPane desktop) {
    	this.desktop = desktop;
    	mainScene = desktop.getMainScene();
		if (ourGuide == null)
			ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);

        Platform.runLater(() -> {

            //view = new WebView();
            //engine = view.getEngine();
            //history = engine.getHistory();
            //entryList = history.getEntries();
            //ssm = comboBox.getSelectionModel();
        	logger.info("Web Engine supported : " + engine.getUserAgent());
        	// For JDK 131, it prints the following :
        	// Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/602.1 (KHTML, like Gecko) JavaFX/8.0 Safari/602.1

        	highlight();
            // 2017-04-27 Disable context menu (copy option)
            view.setContextMenuEnabled(false);

/*
            // 2017-04-27 Add the use of WebEventDispatcher
            WebEventDispatcher webEventDispatcher = new WebEventDispatcher(view.getEventDispatcher());

			// features NOT used for now
            engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

                @Override
                public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
                    if(newValue.equals(State.SUCCEEDED)){
                        // dispatch all events
                        view.setEventDispatcher(webEventDispatcher);
                    }
                }

            });
*/
/*			// features NOT used for now
            engine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {

                @Override
                public WebEngine call(PopupFeatures p) {
                    Stage stage = new Stage(StageStyle.UTILITY);
                    WebView wv2 = new WebView();
                    stage.setScene(new Scene(wv2));
                    stage.show();
                    return wv2.getEngine();
                }
            });

			// features NOT used for now
            // 2017-04-27 Add ListChangeListener to disable mouse scroll
            view.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {

            	@Override
            	public void onChanged(ListChangeListener.Change<? extends Node> c) {
                    pLimit = view.localToScene(view.getWidth(), view.getHeight());
                    view.lookupAll(".scroll-bar")
                    		.stream()
                            .map(s -> (ScrollBar)s)
                            .forEach(s -> {
                                if(s.getOrientation().equals(Orientation.VERTICAL)){
                                    width = s.getBoundsInLocal().getWidth();
                                }
                                if(s.getOrientation().equals(Orientation.HORIZONTAL)){
                                    height = s.getBoundsInLocal().getHeight();
                                }
                            });
                    // dispatch all events
                    webEventDispatcher.setLimit(pLimit.subtract(width, height));
                }
            });
*/


            comboBox.setPromptText("History");
            //comboBox.setPadding(new Insets(3, 3, 3, 3));
            comboBox.setMaxHeight(WIDTH);
            comboBox.setMinHeight(WIDTH);
            comboBox.setPrefHeight(WIDTH);
            comboBox.setMaxWidth(WIDTH);
            comboBox.setMinWidth(WIDTH);
            comboBox.setPrefWidth(WIDTH);
            comboBox.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent ev) {
                	//System.out.println("reload");
                	//System.out.println("i : " + history.getCurrentIndex());
                    //int offset = ssm.getSelectedIndex() - history.getCurrentIndex();
                    history.go(ssm.getSelectedIndex());
                    readURLCombo();
                	//System.out.println("i : " + history.getCurrentIndex());
    		    	//System.out.println("textInputCache : " + textInputCache);
                	//System.out.println();
                }
            });

        	//reloadButton.setPadding(new Insets(0, 3, 0, 3));
            reloadButton.setMinWidth(WIDTH+5);
            reloadButton.setTooltip(new Tooltip("Reload this page"));
            reloadButton.setOnAction(e -> {
        		goLoad(tf.getText().trim());
		    	//System.out.println("textInputCache : " + textInputCache);
            	//System.out.println();
            });

            //backButton.setPadding(new Insets(0, 3, 0, 3));
            backButton.setMinWidth(WIDTH+5);
            backButton.setTooltip(new Tooltip("Go back"));
            backButton.setOnAction(e -> {
            	//System.out.println("backward");
            	//System.out.println("i : " + history.getCurrentIndex());
            	engine.executeScript("history.back()");
		    	int i = history.getCurrentIndex();
		    	//ssm.select(i); // question : will it load the url the 2nd time ?
            	//System.out.println("i : " + history.getCurrentIndex());
		    	if (i > 0)
		    		textInputCache = entryList.get(i-1).getUrl();
		    	//System.out.println("textInputCache : " + textInputCache);
            	showFormattedURL();
            	//System.out.println("i : " + history.getCurrentIndex());
            	//System.out.println();
            });

            //forwardButton.setPadding(new Insets(0, 3, 0, 3));
            forwardButton.setMinWidth(WIDTH+5);
            forwardButton.setTooltip(new Tooltip("Go forward"));
            forwardButton.setOnAction(e -> {
            	//System.out.println("forward");
            	//System.out.println("i : " + history.getCurrentIndex());
                engine.executeScript("history.forward()");
                int i = history.getCurrentIndex();
            	int size = entryList.size();
                //ssm.select(i);
            	//System.out.println("i : " + history.getCurrentIndex());
		    	if (i + 1 < size && size > 1)
		    		textInputCache = entryList.get(i+1).getUrl();
		    	//System.out.println("textInputCache : " + textInputCache);
                showFormattedURL();
            	//System.out.println("i : " + history.getCurrentIndex());
            	//System.out.println();
	        });


            //Google.setOnAction(e -> webEngine.load("http://www.google.com"));
            //Yahoo.setOnAction(e -> webEngine.load("http://www.yahoo.com"));
            //Bing.setOnAction(e -> webEngine.load("http://www.bing.com"));
            //Facebook.setOnAction(e -> webEngine.load("http://www.facebook.com"));
            //Twitter.setOnAction(e -> webEngine.load("http://www.twitter.com"));
            //YouTube.setOnAction(e -> webEngine.load("http://www.youtube.com"));

            entryList.addListener((Change<? extends Entry> c) -> {
                c.next();
                for (Entry e : c.getRemoved()) {
                    comboBox.getItems().remove(e.getUrl());
                    showFormattedURL();
                }
                for (Entry e : c.getAddedSubList()) {
                	String fullURL = e.getUrl();
                	String updateURL = fullURL;
                	if (fullURL.contains(DOCS_HELP_DIR)) {
                		isLocalHtml = true;
                		int i = fullURL.indexOf("docs")-1;
                		updateURL = fullURL.substring(i, fullURL.length());
                	}
                	else {
                	}

                    comboBox.getItems().add(updateURL);

                    tf.setText(updateURL);
            		statusBarURLText = updateURL;
            		statusBarLbl.setText(updateURL);

                }
            });

            //tf.setPadding(new Insets(0, 3, 0, 3));
            tf.setPromptText("URL Address");
            tf.setMaxHeight(WIDTH);
            tf.setMinHeight(WIDTH);
            tf.setPrefHeight(WIDTH);
            //tf.setMinWidth(1024);
            //tf.setPrefWidth(1024);
            tf.prefWidthProperty().bind(mainScene.getScene().widthProperty()
            		.subtract(comboBox.widthProperty())
            		.subtract(reloadButton.widthProperty())
            		.subtract(backButton.widthProperty())
            		.subtract(forwardButton.widthProperty())
            		);


            tf.setOnKeyPressed((KeyEvent ke) -> {
                KeyCode key = ke.getCode();
                if(key == KeyCode.ENTER){
            		goLoad(tf.getText().trim());
                    //engine.load("http://" + tf.getText());
                }
            });

/*
            VBox statusBar = new VBox();
            progressBar.setPreferredSize(new Dimension(150, 18));
            progressBar.setStringPainted(true);
            statusBar.getChildren().add(progressBar);
            //sp.getChildren().addAll(comboBox, Google, Yahoo, Bing, Facebook, Twitter, YouTube);
            //ap.setTop(sp);
*/
            bar.getChildren().addAll(comboBox, tf, backButton, reloadButton, forwardButton);
            vbox.getChildren().addAll(topButtonBar, bar);

        	//history.go(0);
        	updateButtons();
        });

        initJFX();
        panel = initJPanel();

        //SwingUtilities.invokeLater(()-> {
        //    btnGo.doClick(); // not useful
        //});
    }

    public void readURLCombo() {
    	String content = (String) ssm.getSelectedItem();
    	if (content.contains(DOCS_HELP_DIR) && content.contains(".html")) {
			if (ourGuide == null)
				ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
			content = ourGuide.getFullURL((String) ssm.getSelectedItem());
    	}
        textInputCache = content;
        showFormattedURL();
    }

    public void goLoad(String input) {

		if (input.contains(DOCS_HELP_DIR) && input.contains(".html")) {
			if (ourGuide == null)
				ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
			ourGuide.setURL(input); //$NON-NLS-1$
		}
		else {
        	fireButtonGo(input);
		}
    }

/*
    public void goURL() {

		if (input.contains(DOCS_HELP_DIR) && input.contains(".html")) {
			if (ourGuide == null)
				ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
			ourGuide.setURL(input); //$NON-NLS-1$
			setTextInputCache(fullLink);
			inputURLType(fullLink);//, BrowserJFX.REMOTE_HTML);
			showFormattedURL();
		}
    }
*/

    public void fireButtonGo(String input) {
		if (input != null && !input.isEmpty()) {
			// if the address bar is not empty
			Platform.runLater(() -> {
            	//System.out.println("fireButtonGo");
            	//System.out.println("i : " + history.getCurrentIndex());
				inputURLType(input);
            	//System.out.println("i : " + history.getCurrentIndex());
		    	int i = history.getCurrentIndex();
		    	ssm.select(i); // question : will it load the url the 2nd time ?
            	//System.out.println("i : " + history.getCurrentIndex());
		    	if (entryList.size() != 0)
		    		textInputCache = entryList.get(i).getUrl();
            	//System.out.println("i : " + history.getCurrentIndex());
		    	//System.out.println("textInputCache : " + textInputCache);
            	//System.out.println();
			});

		}
    }

    // 2016-04-22 Added ability to interpret internal commands
    public JPanel initJPanel() {
/*
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {

            	highlight();

        		String input = urlTF.getText().trim();

        		if (input.contains(DOCS_HELP_DIR) && input.contains(".html")) {
        			if (ourGuide == null)
        				ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
        			ourGuide.setURL(input); //$NON-NLS-1$
        		}
        		else {
                	fireButtonGo(input);
        		}

            }
        };

        ActionListener bl = new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Platform.runLater(() -> {
                    goBack();
                });
            }
        };

        ActionListener fl = new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Platform.runLater(() -> {
                    goForward();
                });
            }
        };

        btnBack.addActionListener(bl);
        btnForward.addActionListener(fl);
        btnGo.addActionListener(al);
        urlTF.addActionListener(al);

        urlTF.setEditable(true);
        urlTF.requestFocusInWindow();
*/
        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);
/*
        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        topBar.add(urlTF, BorderLayout.CENTER);

        JPanel buttonPane = new JPanel(new FlowLayout());
        buttonPane.add(btnBack);
        buttonPane.add(btnGo);
        buttonPane.add(btnForward);
        topBar.add(buttonPane, BorderLayout.EAST);
*/
        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(statusBarLbl, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        //panel.add(topBar, BorderLayout.NORTH);
        panel.add(jfxPanel, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);

        return panel;
    }


    public void inputURLType(String input) {

    	if (input != null && !input.isEmpty()) {
    		textInputCache = input;

    		if (input.equals("ticket")
    				|| input.equals(MAP_FILE)
    				|| input.equals(GLOBE_FILE)) {
    			parseInput(input, INTERNAL_COMMAND);
    		}
    		else if (input.contains(DOCS_HELP_DIR)) { //"file:/")) {
    				parseInput(input, LOCAL_HTML);
    		}

    		else if (input.contains("https://")
    				|| input.contains("http://")
    				//|| input.contains("www")
    				//|| input.contains(".html")
    				//|| input.contains(".htm")
    				//|| input.contains(".asp")
    				//|| input.contains(".aspx")
    				) {
    			parseInput(input, REMOTE_HTML);
        	}
    		else
    			//if (input.contains("www")
    			//	|| input.contains("http")
    			//	|| input.contains(".html")
    			//	|| input.contains(".htm")
    			//	|| input.contains(".asp")
    			//	|| input.contains(".aspx")
    			//	)
    		{
    			parseInput(input, UNKNOWN);
        	}
		}
    	//else {
    	//	System.out.println("input is null");
		//	parseInput(input, LOCAL_HTML); //or UNKNOWN ?!?
    	//}

    }

    // 2016-04-22 Added parseInput()
    public void parseInput(String input, int URL_type) {

		// Type 0 is internal command
		if (URL_type == INTERNAL_COMMAND)  {
			isLocalHtml = true;
			//System.out.println("BrowserJFX : input is " + input);
			determineURL(input + ".html", INTERNAL_COMMAND);
			//addCSS();
	    }
		// Type 1 is local html file
		else if (URL_type == LOCAL_HTML) {
			isLocalHtml = true;
			determineURL(input, LOCAL_HTML);
			//addCSS();
			//btnGo.doClick();
		}

		else if (URL_type == REMOTE_HTML) {
			isLocalHtml = false;
			determineURL(input, REMOTE_HTML);
		}

		else {
			isLocalHtml = false;
			boolean https = input.toLowerCase().contains(HTTPS_HEADER);
			boolean http = input.toLowerCase().contains(HTTP_HEADER);

			// Type 2 is a remote url
			if (https || http) {

				determineURL(input, REMOTE_HTML);
			}
			else {
	    		//System.out.println("parseInput() : URL_type is " + URL_type);
	    		System.out.println("parseInput() : unknown url");
				// Type 3 could be a remote url that has no "http://" or an invalid input
				// e.g. type in google.com
				// will need to add http://
				determineURL(input, UNKNOWN);
			}


		}
    }


    @SuppressWarnings("restriction")
	private void initJFX() {

    	//java.net.CookieHandler.setDefault(null);

        Platform.runLater(() -> {

                WebViewHyperlinkListener eventPrintingListener = event -> {

                	if (event.getEventType() == EventType.ACTIVATED) {
                		//System.out.println("BrowserJFX : WebViewHyperlinkListener is activated");
                		String input = null;

	                	if (event.getURL() != null) {

		                    if (mainScene != null) mainScene.getScene().setCursor(Cursor.HAND);

		                	input = event.getURL().toString(); // can get NullPointerException on some links.

		                	if (input.toLowerCase().contains(HTTP_HEADER.toLowerCase())
		                			||input.toLowerCase().contains(HTTPS_HEADER.toLowerCase())) {

		    					isLocalHtml = false;
		        			}
		    				else {
		    					isLocalHtml = true;
		    				}

		                    updateButtons();
		                    // 2016-11-30 Fix the URL not being displayed correctly
		                    textInputCache = input;

		                    showFormattedURL();
		                    //System.out.println("just clicked at a link");
	                	}
                	}

                	else {
                		if (mainScene != null) mainScene.getScene().setCursor(Cursor.DEFAULT);
                	}

                    return false;
                };

                WebViews.addHyperlinkListener(view, eventPrintingListener);

                view.setStyle("-fx-background-color: #656565;"
                		+ " -fx-font-color: white;"
                		+ " -fx-border-color: #00a7c8");
                		//+ " -webkit-scrollbar: orange;");

/*
                Button reloadB = new Button("Refresh");
                reloadB.setMaxWidth(110);

                Button backB = new Button("Back");
                backB.setMaxWidth(110);

                Button forwardB = new Button("Forward");
                forwardB.setMaxWidth(110);

                reloadB.setOnAction(e -> engine.reload());

                backB.setOnAction(e -> {

                	engine.executeScript("history.back()");
                	String input = urlTF.getText().trim();

                	getURLType(input);
                	System.out.println("calling history.back()");

                });

                forwardB.setOnAction(e -> {

                	engine.executeScript("history.forward()");
                	String input = urlTF.getText().trim();

                	getURLType(input);
                	System.out.println("calling history.forward()");
                });
 */
/*
                engine.titleProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                //SimpleSwingBrowser.this.setTitle(newValue);
                            }
                        });
                    }
                });
*/


                // show the url address whenever a mouse hovers over a hyperlink
                engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                    @Override
                    public void handle(final WebEvent<String> event) {
                        SwingUtilities.invokeLater(()-> {
                        	//System.out.println("BrowserJFX : hovering over a hyperlink, calling setOnStatusChanged() to display its url on the status bar");
                        	// Note: it shows the content of the hyperlink (even before the user clicks on it.
                            String content = event.getData();
                            if (content != null && !content.isEmpty()) {
                            	if (mainScene != null) mainScene.getScene().setCursor(Cursor.HAND);
    		                    //System.out.println("now hovering over a link");
                            	// 2016-06-07 Truncated off the initial portion of the path to look more "user-friendly"/improve viewing comfort.
                            	if (content.contains(DOCS_HELP_DIR)) {
                            		int i = content.indexOf("docs")-1;
                            		//System.out.println("shortened content is " + content.substring(i, content.length()));
                            		statusBarLbl.setText(content.substring(i, content.length()));
                            	}
                            	else {
                            		//System.out.println("content is " + content);
                            		// this is a remote link or internal link
                            		statusBarLbl.setText(content);
                            	}
                            }

                            else {
                            	if (mainScene != null) mainScene.getScene().setCursor(Cursor.DEFAULT);
                            	// if the mouse pointer is not on any hyperlink
                           		//System.out.println("The null content is " + content);
                            	statusBarLbl.setText(content);
                            }
                        });
                    }
                });

                engine.locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {

                    	if (oldValue != newValue) {
                            if (newValue.contains(DOCS_HELP_DIR + "ticket.html")) {
                        		//System.out.println("BrowserJFX : locationProperty() change");
		                    	Platform.runLater(()-> {
			                        JSObject jsobj = (JSObject) engine.executeScript("window");
			        				jsobj.setMember("JavaBridge", new TicketSubmission());
		                    	});
                            }

	                    	//SwingUtilities.invokeLater(() ->{
                    		textInputCache = newValue;
                       		//showURL();
                    	//});
                    	}
                    }
                });

                engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                    	if (oldValue != newValue)
	                    	SwingUtilities.invokeLater(()->{
	                        	//System.out.println("BrowserJFX : workDoneProperty() change");
	                                progressBar.setValue(newValue.intValue());
	                        });
                    }
                });

                engine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
                	@Override
	                public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
	                	if (engine.getLoadWorker().getState() == FAILED) {
	                		//SwingUtilities.invokeLater(()-> {
	                			if (engine.getLocation() != null)
	                				System.out.println("BrowserJFX : worker.getState() == FAILED in exceptionProperty() : "
	                						+ engine.getLocation());
	                			if (value != null)
	                				System.out.println(" : " + value.getMessage()
	                                        + ".  Loading error...");

	/*                				JOptionPane.showMessageDialog(
	                                            panel,
	                                            (value != null) ?
	                                            engine.getLocation() + "\n" + value.getMessage() :
	                                            engine.getLocation() + "\nUnexpected error.",
	                                            "Loading error...",
	                                            JOptionPane.ERROR_MESSAGE);
	*/
		                		//});
		                	}
		                }
		            }
                );
 /*
                engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                    	System.out.println("calling stateProperty");
                    	getURLType(getCurrentURL());
                        JSObject jsobj = (JSObject) engine.executeScript("window");
        				jsobj.setMember("JavaBridge", new TicketSubmission());
                    }
                });
*/

                // process page loading
                engine.getLoadWorker().stateProperty().addListener(
                    new ChangeListener<State>() {
                        @Override
                        public void changed(ObservableValue<? extends State> ov,
                            State oldState, State newState) {
                                if (oldState != newState) {
                                	if (newState == State.SUCCEEDED) {
	                                	String input = getCurrentURL();
	                                	//System.out.println("BrowserJFX's stateProperty()");
	                            		if (input.contains(DOCS_HELP_DIR)) {
	                            			isLocalHtml = true;
	                                    	// Note: after hours of experiments, it's found that the only "safe" way
	                            			// (without causing NullPointerException) is to call addCSS()
	                            			// through stateProperty() here.
	                            			addCSS();
	                            		}
                                	}
                                }
                            }
                        }
                );

                BorderPane borderPane = new BorderPane();

                borderPane.setTop(vbox);
                borderPane.setCenter(view);
                jfxPanel.setScene(new Scene(borderPane));
        });
    }

    /*
     * Parses the url text and show it in both the status bar and the address textfield
     */
    public void showFormattedURL() {
    	//logger.info("BrowserJFX's showURL() is on " + Thread.currentThread().getName() );
    	String content = textInputCache;
        //System.out.println("urlTF is " + urlTF.getText());
        //System.out.println("textInputCache is " + textInputCache);

    	if (content.contains(DOCS_HELP_DIR)) {
    		isLocalHtml = true;
    		int i = content.indexOf("docs")-1;
            String shortened = content.substring(i, content.length());
            //System.out.println("shortened is " + shortened);
            //urlTF.setText(shortened);
            tf.setText(shortened);
    		statusBarURLText = shortened;
    		statusBarLbl.setText(shortened);
    	}
    	else {
    		// this is a remote link or internal link
            //urlTF.setText(content);
            tf.setText(content);
    		statusBarURLText = content;
    		statusBarLbl.setText(content);
    	}
/*
		//System.out.println("isLocalHtml : " + isLocalHtml + "   isInternal : " + isInternal);
 		if (isLocalHtml) {
     		if (isInternal){
        		;//urlTF.setText(urlTF.getText());
     		}
    		else {
    			// if it is a local html file, show blank on the address bar
    			urlTF.setText("");
    			//urlTF.setText(textInputCache);
    		}
     	}
    	else {
     		urlTF.setText(textInputCache);
     	}
*/
    }

    public void addCSS() {
    	//logger.info("BrowserJFX's addCSS() is on " + Thread.currentThread().getName() );
    	if (isLocalHtml) {// && go_flag && !isInternal) {
		   	//System.out.println("adding css");

            //engine.executeScript(CSS);

			Document doc = engine.getDocument() ;
			//SwingUtilities.invokeLater(() -> {
			    Element styleNode = doc.createElement("style");
			    Text styleContent = doc.createTextNode(CSS);
			    styleNode.appendChild(styleContent);
			    if (doc.getDocumentElement().getElementsByTagName("head").item(0) != null)
			    	doc.getDocumentElement().getElementsByTagName("head").item(0).appendChild(styleNode);
			    if (doc.getDocumentElement().getElementsByTagName("HEAD").item(0) != null)
			    	doc.getDocumentElement().getElementsByTagName("HEAD").item(0).appendChild(styleNode);
			//});

	   }
    }

	// 2016-04-18 Added updateURL()
    public void determineURL(String href, int URL_type) {
    	if (href != null && !href.isEmpty()) {
	    	if (URL_type == INTERNAL_COMMAND) {
		    	URL url = getClass().getResource(Msg.getString("doc.help") + href);
		    	addressURLText = url.toExternalForm();
		    	loadLocalURL(addressURLText);
	    	}
	    	else if (URL_type == LOCAL_HTML) {
		    	addressURLText = href;
		    	loadLocalURL(href);

	    	}
	    	else if (URL_type == REMOTE_HTML) {
       			addressURLText = href;
			    loadRemoteURL(href);
	    	}
	    	else if (URL_type == UNKNOWN) {
	    		try {
	    			if (!href.contains(DOCS_HELP_DIR)) {
		    			// assume the text in the address bar has no 'http://'
		    			URL url = new URL(HTTP_HEADER + href);
		    			addressURLText = url.toExternalForm();
				    	loadRemoteURL(addressURLText);
	    			}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
	    	}
    	}
    }

    @SuppressWarnings("restriction")
	public void loadRemoteURL(final String content) {
    	isLocalHtml = false;
    	//Platform.runLater(()-> {
			//try {
				engine.load(content);
				updateButtons();
				textInputCache = content;
				statusBarURLText = content;
			//} catch (StringIndexOutOfBoundsException e) {
			//	e.printStackTrace();
			//}
        //});

        SwingUtilities.invokeLater(()-> statusBarLbl.setText(content));
    }

    @SuppressWarnings("restriction")
	public void loadLocalURL(String content) {
       	isLocalHtml = true;
    	Platform.runLater(()-> {
            engine.load(content);
            updateButtons();
            textInputCache = content;
            if (content != null && !content.isEmpty()) {
            	// 2016-06-07 Truncated off the initial portion of the path to look more "user-friendly"/improve viewing comfort.
            	if (content.contains(DOCS_HELP_DIR)) {
            		int i = content.indexOf("docs")-1;
                    addressURLText = content;
            		statusBarURLText = content.substring(i, content.length());
            	}
            	else {
            		// this is a remote link or internal link, is this condition needed ?
            		statusBarURLText = content;
            	}
        		statusBarLbl.setText(statusBarURLText);
                //urlTF.setText(statusBarURLText);
                tf.setText(statusBarURLText);
            }
        });

    }

    @SuppressWarnings("restriction")
	public void highlight() {
        //System.out.println("highlight()");
        Platform.runLater(() -> {
                engine.setJavaScriptEnabled(true);
                //executejQuery(engine, " $(\"a\").css(\"color\", \"red\")");
        });
    }

/*
    private static Object executejQuery(final WebEngine engine, String minVersion, String script) {
        return executejQuery(engine, DEFAULT_JQUERY_MIN_VERSION, script);//JQUERY_LOCATION, script);
    }

    private Object executejQuery(final WebEngine engine, String script) {
        return executejQuery(engine, DEFAULT_JQUERY_MIN_VERSION, script);
    }
*/

    @SuppressWarnings("restriction")
	public String getCurrentURL() {
        //history = engine.getHistory();
        ObservableList<WebHistory.Entry> entryList = history.getEntries();
        int currentIndex = history.getCurrentIndex();
        String txt = null;
        if (currentIndex >=0 ) {
        	txt = entryList.get(currentIndex).getUrl();
        	//System.out.println("currentIndex is " + currentIndex + " url is " + txt);
        	//Platform.runLater(() -> { history.go(0);} );
        }
        return txt;
      }

    public JPanel getPanel() {
    	return panel;
    }

    public JLabel getStatusBarLabel() {
    	return statusBarLbl;
    }

    public void updateButtons() {
    	/*
    	//final WebHistory history = engine.getHistory();
    	//ObservableList<WebHistory.Entry> entryList = history.getEntries();
    	int currentIndex = history.getCurrentIndex();
       	//System.out.println("updateButtons()'s currentIndex : " + currentIndex + "  size : " + entryList.size());

    	if (entryList.size() > 1) {
    		if (currentIndex > 0)
    			btnBack.setEnabled(true);
    		else
    			btnBack.setEnabled(false);

    		if  (currentIndex < entryList.size() - 1)
    			btnForward.setEnabled(true);
    		else
    			btnForward.setEnabled(false);
    	}
    	else {
    		btnBack.setEnabled(false);
    		btnForward.setEnabled(false);
    	}

*/
/*
    	if (entryList.size() > 1
				&& currentIndex > 0)
    		btnBack.setEnabled(true);
    	else
    		btnBack.setEnabled(false);

    	if (entryList.size() > 1
				&& currentIndex < entryList.size() - 1)
    		btnForward.setEnabled(true);
    	else
    		btnForward.setEnabled(false);
*/

    }


/*
    @SuppressWarnings("restriction")
    public void goBack() {
    	Platform.runLater(() -> {
        	int currentIndex = history.getCurrentIndex();
        	//System.out.println("goBack()'s currentIndex : " + currentIndex + "  size : " + entryList.size());
    		history.go(entryList.size() > 1
    				&& currentIndex > 0
    				? -1
    				: 0);

       		showURL();

        	currentIndex = history.getCurrentIndex();
        	//System.out.println("goBack()'s currentIndex : " + currentIndex + "  size : " + entryList.size());
        	if (entryList.size() > 1) {
        		if (currentIndex > 0)
        			btnBack.setEnabled(true);
        		else
        			btnBack.setEnabled(false);

        		if  (currentIndex < entryList.size() - 1)
        			btnForward.setEnabled(true);
        		else
        			btnForward.setEnabled(false);
        	}
        	else {
        		btnBack.setEnabled(false);
        		btnForward.setEnabled(false);
        	}
       	});

	}

    @SuppressWarnings("restriction")
    public void goForward() {
    	Platform.runLater(() -> {
        	int currentIndex = history.getCurrentIndex();
        	//System.out.println("goBack()'s currentIndex : " + currentIndex + "  size : " + entryList.size());

    		history.go(entryList.size() > 1
    				&& currentIndex < entryList.size() - 1
    				? 1
    				: 0);

        	currentIndex = history.getCurrentIndex();
        	//System.out.println("goBack()'s currentIndex : " + currentIndex + "  size : " + entryList.size());
        	if (entryList.size() > 1) {
        		if (currentIndex > 0)
        			btnBack.setEnabled(true);
        		else
        			btnBack.setEnabled(false);

        		if  (currentIndex < entryList.size() - 1)
        			btnForward.setEnabled(true);
        		else
        			btnForward.setEnabled(false);
        	}
        	else {
        		btnBack.setEnabled(false);
        		btnForward.setEnabled(false);
        	}

    	});
	}
*/

    public String getTextInputCache() {
    	return textInputCache;
    }

    public void setTextInputCache(String value) {
    	textInputCache = value;
    }

    public void destroy() {
        jfxPanel = null;
        panel = null;
        statusBarLbl = null;
        //btnGo = null;
        //btnForward = null;
        //btnBack = null;
        //urlTF = null;
        progressBar = null;
        mainScene = null;
        desktop = null;
        view = null;
        engine = null;
        history = null;
        entryList = null;
    }

}

class TicketSubmission {
	public String submit(String name) {
		return "Hi," + name;
	}
}