/* Mars Simulation Project
 * BrowserJFX.java
 * @version 3.1.0 2017-09-24
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.swing.BorderFactory;

import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent.EventType;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebHistory;
import javafx.scene.Cursor;

import netscape.javascript.JSObject;

import org.codefx.libfx.control.webview.WebViewHyperlinkListener;
import org.codefx.libfx.control.webview.WebViews;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.progressbar.WebProgressBar;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXScrollPane;

//import com.sun.javafx.webkit.WebConsoleListener;

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

    //public static final String DEFAULT_JQUERY_MIN_VERSION = "1.7.2";
    //public static final String JQUERY_LOCATION = "http://code.jquery.com/jquery-1.7.2.min.js";

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

    public volatile String textInputCache;
    public volatile String addressURLText;
    public volatile String statusBarURLText;
    public volatile String inputCache;

    private JFXPanel jfxPanel = new JFXPanel();
    private WebPanel panel = new WebPanel(new BorderLayout());
    private WebLabel statusBarLbl = new WebLabel();
    private WebProgressBar progressBar = new WebProgressBar();

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
    private WebView view;
    private WebEngine engine;
    private WebHistory history;
    private GuideWindow ourGuide;

	private ObservableList<WebHistory.Entry> entryList;// = history.getEntries();
	private SingleSelectionModel<String> ssm;// = comboBox.getSelectionModel();
	private BorderPane borderPane;

	// see http://www.java2s.com/Tutorials/Java/JavaFX/1500__JavaFX_WebEngine.htm
	public BrowserJFX(MainDesktopPane desktop) {
    	this.desktop = desktop;
    	mainScene = desktop.getMainScene();
    	
    	if (mainScene == null && ourGuide == null) {
    		ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
	        Platform.runLater(() -> {
//	    		createTopButtonBar();
	        	createGUI();
	            initJFX();
	        });
    	}
    	else {
    		createTopButtonBar();
    		createGUI();
            initJFX();
    	}
          
        if (mainScene == null)
        	panel = initWebPanel();

    }

    public void createTopButtonBar() {
    	
    	String shortcutsURL = getClass().getResource(Msg.getString("doc.shortcuts")).toExternalForm(); //$NON-NLS-1$
    	String guideURL = getClass().getResource(Msg.getString("doc.guide")).toExternalForm(); //$NON-NLS-1$
    	String aboutURL = getClass().getResource(Msg.getString("doc.about")).toExternalForm(); //$NON-NLS-1$
    	String tutorialURL = getClass().getResource(Msg.getString("doc.tutorial")).toExternalForm(); //$NON-NLS-1$
    	String projectsiteURL = Msg.getString("url.projectSite"); //$NON-NLS-1$
    	String wikiURL = Msg.getString("url.wiki"); //$NON-NLS-1$
    	String marspediaURL = Msg.getString("url.marspedia"); //$NON-NLS-1$
		
    	JFXButton b0 = new JFXButton(Msg.getString("GuideWindow.button.about")); //$NON-NLS-1$
    	b0.setPadding(new Insets(5,25,5,25));
    	b0.setMinWidth(WIDTH+5);
    	b0.setTooltip(new Tooltip("About mars-sim"));
    	b0.setOnAction(e -> {
    		fireURL(aboutURL);
        });

    	JFXButton b1 = new JFXButton(Msg.getString("GuideWindow.button.tutorial")); //$NON-NLS-1$
    	b1.setPadding(new Insets(5,25,5,25));
    	b1.setMinWidth(WIDTH+5);
    	b1.setTooltip(new Tooltip("Tutorial"));
    	b1.setOnAction(e -> {
    		fireURL(tutorialURL);
        });
    	
    	JFXButton b2 = new JFXButton(Msg.getString("GuideWindow.button.userguide")); //$NON-NLS-1$
    	b2.setPadding(new Insets(5,25,5,25));
    	b2.setMinWidth(WIDTH+5);
    	b2.setTooltip(new Tooltip("User Guide"));
    	b2.setOnAction(e -> {
    		fireURL(guideURL);
        });
    	
    	JFXButton b3 = new JFXButton(Msg.getString("GuideWindow.button.shortcuts")); //$NON-NLS-1$
    	b3.setPadding(new Insets(5,25,5,25));
    	b3.setMinWidth(WIDTH+5);
    	b3.setTooltip(new Tooltip("Shortcut Map"));
    	b3.setOnAction(e -> {
    		fireURL(shortcutsURL);
        });
    	
    	JFXButton b4 = new JFXButton(Msg.getString("GuideWindow.button.projectsite")); //$NON-NLS-1$
    	b4.setPadding(new Insets(5,25,5,25));
    	b4.setMinWidth(WIDTH+5);
    	b4.setTooltip(new Tooltip("Project Site in GitHub"));
    	b4.setOnAction(e -> {
    		fireURL(projectsiteURL);
        });
    	
    	JFXButton b5 = new JFXButton(Msg.getString("GuideWindow.button.wiki")); //$NON-NLS-1$
    	b5.setPadding(new Insets(5,25,5,25));
    	b5.setMinWidth(WIDTH+5);
    	b5.setTooltip(new Tooltip("mars-sim Wiki"));
    	b5.setOnAction(e -> {
			fireURL(wikiURL);
        });
 
    	JFXButton b6 = new JFXButton(Msg.getString("GuideWindow.button.marspedia")); //$NON-NLS-1$
    	b6.setPadding(new Insets(5,25,5,25));
    	b6.setMinWidth(WIDTH+5);
    	b6.setTooltip(new Tooltip("A Random Page from Marspedia"));
    	b6.setOnAction(e -> {
			fireURL(marspediaURL);
        });
    	    	
    	topButtonBar.setPadding(new Insets(5,5,5,5));
    	topButtonBar.getChildren().addAll(b0, b1, b2, b3, b4, b5, b6);
    }
    
    
    public void fireURL(String input) {
		setTextInputCache(input);
		checkInputURLType(input);
		showFormattedURL();
		fireButtonGo(input);
    }
    
    public void createGUI() {

        view = new WebView();
        engine = view.getEngine();
        history = engine.getHistory();
        entryList = history.getEntries();
        ssm = comboBox.getSelectionModel();
    	logger.info("Web Engine supported : " + engine.getUserAgent());
    	// For JDK 8u131, it prints the following :
    	// Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/602.1 (KHTML, like Gecko) JavaFX/8.0 Safari/602.1
    	// For JDK 10.0.2
    	//Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/605.1 (KHTML, like Gecko) JavaFX/10 Safari/605.1
    	highlight();
        // Disable context menu (copy option)
        view.setContextMenuEnabled(false);

        // Add the use of WebEventDispatcher
//        WebEventDispatcher webEventDispatcher = new WebEventDispatcher(view.getEventDispatcher());
//
//		// features NOT used for now
//        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
//
//            @Override
//            public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
//                if(newValue.equals(State.SUCCEEDED)){
//                    // dispatch all events
//                    view.setEventDispatcher(webEventDispatcher);
//                }
//            }
//
//        });

			// features NOT used for now
//        engine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
//
//            @Override
//            public WebEngine call(PopupFeatures p) {
//                Stage stage = new Stage(StageStyle.UTILITY);
//                WebView wv2 = new WebView();
//                stage.setScene(new Scene(wv2));
//                stage.show();
//                return wv2.getEngine();
//            }
//        });

		// features NOT used for now
        // Add ListChangeListener to disable mouse scroll
//        view.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
//
//        	@Override
//        	public void onChanged(ListChangeListener.Change<? extends Node> c) {
//                pLimit = view.localToScene(view.getWidth(), view.getHeight());
//                view.lookupAll(".scroll-bar")
//                		.stream()
//                        .map(s -> (ScrollBar)s)
//                        .forEach(s -> {
//                            if(s.getOrientation().equals(Orientation.VERTICAL)){
//                                width = s.getBoundsInLocal().getWidth();
//                            }
//                            if(s.getOrientation().equals(Orientation.HORIZONTAL)){
//                                height = s.getBoundsInLocal().getHeight();
//                            }
//                        });
//                // dispatch all events
//                webEventDispatcher.setLimit(pLimit.subtract(width, height));
//            }
//        });

        comboBox.setPromptText("History");
        comboBox.setMaxHeight(WIDTH);
        comboBox.setMinHeight(WIDTH);
        comboBox.setPrefHeight(WIDTH);
        comboBox.setMaxWidth(WIDTH);
        comboBox.setMinWidth(WIDTH);
        comboBox.setPrefWidth(WIDTH);
        comboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ev) {
                int offset = ssm.getSelectedIndex() - history.getCurrentIndex();
                history.go(offset);//ssm.getSelectedIndex());
                readURLCombo();
            }
        });

    	//reloadButton.setPadding(new Insets(0, 3, 0, 3));
        reloadButton.setPrefHeight(WIDTH);
        reloadButton.setMinWidth(WIDTH+5);
        reloadButton.getStyleClass().add("menu-button");
        //FontAwesomeIconView refresh = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        //reloadButton.setGraphic(refresh);
        reloadButton.setTooltip(new Tooltip("Reload this page"));
        reloadButton.setOnAction(e -> {
    		goLoad(tf.getText().trim());
        });

        //backButton.setPadding(new Insets(0, 3, 0, 3));
        backButton.setPrefHeight(WIDTH);
        backButton.setMinWidth(WIDTH+5);
        backButton.getStyleClass().add("menu-button");
        //AwesomeDude.setIcon(backButton, AwesomeIcon.BACKWARD, "12.0");
        //FontAwesomeIconView backward = new FontAwesomeIconView(FontAwesomeIcon.BACKWARD);
       // backButton.setGraphic(backward);
        backButton.setTooltip(new Tooltip("Go back"));
        backButton.setOnAction(e -> {
        	engine.executeScript("history.back()");
	    	int i = history.getCurrentIndex();
	    	if (i > 0)
	    		textInputCache = entryList.get(i-1).getUrl();
        	showFormattedURL();
        });

        //forwardButton.setPadding(new Insets(0, 3, 0, 3));
        forwardButton.setPrefHeight(WIDTH);
        forwardButton.setMinWidth(WIDTH+5);
        forwardButton.getStyleClass().add("menu-button");
        //AwesomeDude.setIcon(forwardButton, AwesomeIcon.FORWARD, "12.0");
        //FontAwesomeIconView forward = new FontAwesomeIconView(FontAwesomeIcon.FORWARD);
        //forwardButton.setGraphic(forward);
        forwardButton.setTooltip(new Tooltip("Go forward"));
        forwardButton.setOnAction(e -> {
        	//System.out.println("i : " + history.getCurrentIndex());
            engine.executeScript("history.forward()");
            int i = history.getCurrentIndex();
        	int size = entryList.size();
            //ssm.select(i);
	    	if (i + 1 < size && size > 1)
	    		textInputCache = entryList.get(i+1).getUrl();
            showFormattedURL();
        });

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
        
        if (mainScene != null) {
        	tf.prefWidthProperty().bind(mainScene.getStage().widthProperty()//getScene().widthProperty()
        		.subtract(comboBox.widthProperty())
        		.subtract(reloadButton.widthProperty())
        		.subtract(backButton.widthProperty())
        		.subtract(forwardButton.widthProperty())
        		);
        }
        else
            tf.setPrefWidth(900);
        	
        tf.setOnKeyPressed((KeyEvent ke) -> {
            KeyCode key = ke.getCode();
            if(key == KeyCode.ENTER){
        		goLoad(tf.getText().trim());
                //engine.load("http://" + tf.getText());
            }
        });

        bar.getChildren().addAll(comboBox, tf, backButton, reloadButton, forwardButton);
        vbox.getChildren().addAll(topButtonBar, bar);

    	//history.go(0);
    	updateButtons();
    	
    }
    
    public void readURLCombo() {
    	String content = (String) ssm.getSelectedItem();
    	if (content.contains(DOCS_HELP_DIR) && content.contains(".html")) {
        	if (mainScene == null && ourGuide == null) {
				ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
				content = ourGuide.getFullURL((String) ssm.getSelectedItem());
        	}
        	else {
				content = getFullURL((String) ssm.getSelectedItem());
        	}

    	}
        textInputCache = content;
        showFormattedURL();
    }

    public void goLoad(String input) {

		if (input.contains(DOCS_HELP_DIR) && input.contains(".html")) {
			
	    	if (mainScene == null) {    		
	    		if (ourGuide == null)
	    			ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
	    		// Call setURL to parse the input URL
				ourGuide.setURL(input);
	    	}
	    	else {
	    		setURL(input);
	    	}
	    	
		}
		else {
        	fireButtonGo(input);
		}
    }
    
	/**
	 * Gets the full URL string for internal html files.
	 */
	public String getFullURL(String fileloc) {
		return getClass().getResource(fileloc).toExternalForm();
	}
	
	/**
	 * Set a display URL
	 */
	public void setURL(String fileloc) {
		//goToURL(getClass().getResource(fileloc));
		//browser.getStatusBarLabel().setText(fileloc);
		String fullLink = getClass().getResource(fileloc).toExternalForm();
		//Platform.runLater(()-> {
			setTextInputCache(fullLink);
			checkInputURLType(fullLink);//, BrowserJFX.REMOTE_HTML);
			showFormattedURL();
			fireButtonGo(fullLink);
		//});
	}    


//    public void goURL() {
//
//		if (input.contains(DOCS_HELP_DIR) && input.contains(".html")) {
//			if (ourGuide == null)
//				ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
//			ourGuide.setURL(input); //$NON-NLS-1$
//			setTextInputCache(fullLink);
//			inputURLType(fullLink);//, BrowserJFX.REMOTE_HTML);
//			showFormattedURL();
//		}
//    }


	public void fireButtonGo(String input) {
		if (input != null && !input.isEmpty()) {
			// if the address bar is not empty
			Platform.runLater(() -> {
            	//System.out.println("i : " + history.getCurrentIndex());
				checkInputURLType(input);
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

    /**
     * Sets up the Web Panel
     */
    public WebPanel initWebPanel() {

//        ActionListener al = new ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent e) {
//
//            	highlight();
//
//        		String input = urlTF.getText().trim();
//
//        		if (input.contains(DOCS_HELP_DIR) && input.contains(".html")) {
//        			if (ourGuide == null)
//        				ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
//        			ourGuide.setURL(input); //$NON-NLS-1$
//        		}
//        		else {
//                	fireButtonGo(input);
//        		}
//
//            }
//        };
//
//        ActionListener bl = new ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent e) {
//                Platform.runLater(() -> {
//                    goBack();
//                });
//            }
//        };
//
//        ActionListener fl = new ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent e) {
//                Platform.runLater(() -> {
//                    goForward();
//                });
//            }
//        };
//
//        btnBack.addActionListener(bl);
//        btnForward.addActionListener(fl);
//        btnGo.addActionListener(al);
//        urlTF.addActionListener(al);
//
//        urlTF.setEditable(true);
//        urlTF.requestFocusInWindow();

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);

//        WebPanel topBar = new WebPanel(new BorderLayout(5, 0));
//        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
//        topBar.add(urlTF, BorderLayout.CENTER);
//
//        WebPanel buttonPane = new WebPanel(new FlowLayout());
//        buttonPane.add(btnBack);
//        buttonPane.add(btnGo);
//        buttonPane.add(btnForward);
//        topBar.add(buttonPane, BorderLayout.EAST);

        WebPanel statusBar = new WebPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(statusBarLbl, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        //panel.add(topBar, BorderLayout.NORTH);
        panel.add(jfxPanel, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Check the input URL before parsing it
     * @param input
     */
    public void checkInputURLType(String input) {

    	if (input != null && !input.isEmpty()) {
    		textInputCache = input;

    		if (input.equals("ticket")
    				|| input.equals(MAP_FILE)
    				|| input.equals(GLOBE_FILE)) {
    			parseInput(input, INTERNAL_COMMAND);
    		}
    		
    		else if (input.contains(DOCS_HELP_DIR)) { //"file:/")) {
//    			System.out.println("checkInputURLType : " + input);
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
    
    /**
     * Parses the input URL 
     * @param input
     * @param URL_type the type of URL
     */
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


	private void initJFX() {

//		System.setProperty("jsse.enableSNIExtension", "false");
//		System.setProperty("-Djdk.tls.client.protocols", "TLSv1");
//		System.setProperty("javax.net.ssl.trustStore", "path to truststore");

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
		    public boolean verify(String hostname, SSLSession session) {
		        return true;
		    }
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid); 
		
//		engine.setUserAgent("AppleWebKit/537.44");
		
//		WebConsoleListener.setDefaultListener(new WebConsoleListener(){
//		    @Override
//		    public void messageAdded(WebView webView, String message, int lineNumber, String sourceId) {
//		        System.out.println("Console: [" + sourceId + ":" + lineNumber + "] " + message);
//		    }
//		});
		
    	//java.net.CookieHandler.setDefault(null);

        //Platform.runLater(() -> {

                WebViewHyperlinkListener eventPrintingListener = event -> {

                	if (event.getEventType() == EventType.ACTIVATED) {
                		//System.out.println("BrowserJFX : WebViewHyperlinkListener is activated");
                		String input = null;

	                	if (event.getURL() != null) {

		                    if (mainScene != null) mainScene.getRootStackPane().setCursor(Cursor.HAND);

		                	input = event.getURL().toString(); // can get NullPointerException on some links.

		                	if (input.toLowerCase().contains(HTTP_HEADER.toLowerCase())
		                			||input.toLowerCase().contains(HTTPS_HEADER.toLowerCase())) {

		    					isLocalHtml = false;
		        			}
		    				else {
		    					isLocalHtml = true;
		    				}

		                    updateButtons();
		                    // Fix the URL not being displayed correctly
		                    textInputCache = input;

		                    showFormattedURL();
		                    //System.out.println("just clicked at a link");
	                	}
                	}

                	else {
                		if (mainScene != null) mainScene.getRootStackPane().setCursor(Cursor.DEFAULT);
                	}

                    return false;
                };

                WebViews.addHyperlinkListener(view, eventPrintingListener);

                view.setStyle("-fx-background-color: #656565;"
                		+ " -fx-font-color: white;"
                		+ " -fx-border-color: #00a7c8");


//                Button reloadB = new Button("Refresh");
//                reloadB.setMaxWidth(110);
//
//                Button backB = new Button("Back");
//                backB.setMaxWidth(110);
//
//                Button forwardB = new Button("Forward");
//                forwardB.setMaxWidth(110);
//
//                reloadB.setOnAction(e -> engine.reload());
//
//                backB.setOnAction(e -> {
//
//                	engine.executeScript("history.back()");
//                	String input = urlTF.getText().trim();
//
//                	getURLType(input);
//                	System.out.println("calling history.back()");
//
//                });
//
//                forwardB.setOnAction(e -> {
//
//                	engine.executeScript("history.forward()");
//                	String input = urlTF.getText().trim();
//
//                	getURLType(input);
//                	System.out.println("calling history.forward()");
//                });

//                engine.titleProperty().addListener(new ChangeListener<String>() {
//                    @Override
//                    public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
//                        SwingUtilities.invokeLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                //SimpleSwingBrowser.this.setTitle(newValue);
//                            }
//                        });
//                    }
//                });



                // show the url address whenever a mouse hovers over a hyperlink
                engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                    @Override
                    public void handle(final WebEvent<String> event) {
                        SwingUtilities.invokeLater(()-> {
                        	//System.out.println("BrowserJFX : hovering over a hyperlink, calling setOnStatusChanged() to display its url on the status bar");
                        	// Note: it shows the content of the hyperlink (even before the user clicks on it.
                            String content = event.getData();
                            if (content != null && !content.isEmpty()) {
                            	if (mainScene != null) mainScene.getRootStackPane().setCursor(Cursor.HAND);
    		                    //System.out.println("now hovering over a link");
                            	// Truncate off the initial portion of the path to look more "user-friendly"/improve viewing comfort.
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
                            	if (mainScene != null) mainScene.getRootStackPane().setCursor(Cursor.DEFAULT);
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
                		
                		//System.out.println("Received exception: " + value.getMessage());
                		
//                		Throwable t = engine.getLoadWorker().getException();
//                		
//                		if (t != null && engine.getLoadWorker().getState() == State.FAILED) {
//                			System.out.println(", " + engine.getLoadWorker().getException().toString());
//                		}
                		
	                	if (engine.getLoadWorker().getState() == FAILED) {
	                		//SwingUtilities.invokeLater(()-> {
	                			if (engine.getLocation() != null)
	                				System.out.println("BrowserJFX : worker.getState() == FAILED in exceptionProperty() : "
	                						+ engine.getLocation());
	                			if (value != null)
	                				System.out.println(" : " + value.getMessage()
	                                        + ".  Loading error...");

		                	}
		                }
		            }
                );

//                engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
//                    if (newState == Worker.State.SUCCEEDED) {
//                    	System.out.println("calling stateProperty");
//                    	getURLType(getCurrentURL());
//                        JSObject jsobj = (JSObject) engine.executeScript("window");
//        				jsobj.setMember("JavaBridge", new TicketSubmission());
//                    }
//                });



                
                // process page loading
                engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                    @Override
                    public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                        //System.out.println(engine.getLoadWorker().exceptionProperty());
                        //System.out.println("webEngine result "+ newState.toString());
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
                });

                borderPane = new BorderPane();
                borderPane.setTop(vbox);
                borderPane.setCenter(view);
                
                JFXScrollPane scrollPane = new JFXScrollPane();
                scrollPane.getChildren().add(borderPane);
                //JFXScrollPane.smoothHScrolling(scrollPane);
                
                if (mainScene == null)
                	jfxPanel.setScene(new Scene(scrollPane));
        //});
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
    		SwingUtilities.invokeLater(()-> statusBarLbl.setText(shortened));
    	}
    	else {
    		// this is a remote link or internal link
            //urlTF.setText(content);
            tf.setText(content);
    		statusBarURLText = content;
    		SwingUtilities.invokeLater(()-> statusBarLbl.setText(content));
    	}

		//System.out.println("isLocalHtml : " + isLocalHtml + "   isInternal : " + isInternal);
// 		if (isLocalHtml) {
//     		if (isInternal){
//        		;//urlTF.setText(urlTF.getText());
//     		}
//    		else {
//    			// if it is a local html file, show blank on the address bar
//    			urlTF.setText("");
//    			//urlTF.setText(textInputCache);
//    		}
//     	}
//    	else {
//     		urlTF.setText(textInputCache);
//     	}

    }

    public void addCSS() {
    	//logger.info("BrowserJFX's addCSS() is on " + Thread.currentThread().getName() );
    	if (isLocalHtml) {// && go_flag && !isInternal) {
		   	//System.out.println("adding css");

            //engine.executeScript(CSS);

    		Document doc = engine.getDocument() ;
		    Element styleNode = engine.getDocument().createElement("style");
		    Text styleContent = engine.getDocument().createTextNode(CSS);
		    styleNode.appendChild(styleContent);
		    if (doc.getDocumentElement().getElementsByTagName("head").item(0) != null)
		    	doc.getDocumentElement().getElementsByTagName("head").item(0).appendChild(styleNode);
		    if (doc.getDocumentElement().getElementsByTagName("HEAD").item(0) != null)
		    	doc.getDocumentElement().getElementsByTagName("HEAD").item(0).appendChild(styleNode);

	   }
    }

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

	public void loadLocalURL(String content) {
       	isLocalHtml = true;
    	Platform.runLater(()-> {
            engine.load(content);
            updateButtons();
            textInputCache = content;
            if (content != null && !content.isEmpty()) {
            	// Truncated off the initial portion of the path to look more "user-friendly"/improve viewing comfort.
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

	public void highlight() {
        //System.out.println("highlight()");
        Platform.runLater(() -> {
                engine.setJavaScriptEnabled(true);
                //executejQuery(engine, " $(\"a\").css(\"color\", \"red\")");
        });
    }


//    private static Object executejQuery(final WebEngine engine, String minVersion, String script) {
//        return executejQuery(engine, DEFAULT_JQUERY_MIN_VERSION, script);//JQUERY_LOCATION, script);
//    }
//
//    private Object executejQuery(final WebEngine engine, String script) {
//        return executejQuery(engine, DEFAULT_JQUERY_MIN_VERSION, script);
//    }



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

    public WebPanel getPanel() {
    	return panel;
    }

    public WebLabel getStatusBarLabel() {
    	return statusBarLbl;
    }

    public void updateButtons() {
    	
    	//final WebHistory history = engine.getHistory();
    	//ObservableList<WebHistory.Entry> entryList = history.getEntries();
//    	int currentIndex = history.getCurrentIndex();
       	//System.out.println("updateButtons()'s currentIndex : " + currentIndex + "  size : " + entryList.size());

//    	if (entryList.size() > 1) {
//    		if (currentIndex > 0)
//    			btnBack.setEnabled(true);
//    		else
//    			btnBack.setEnabled(false);
//
//    		if  (currentIndex < entryList.size() - 1)
//    			btnForward.setEnabled(true);
//    		else
//    			btnForward.setEnabled(false);
//    	}
//    	else {
//    		btnBack.setEnabled(false);
//    		btnForward.setEnabled(false);
//    	}


//    	if (entryList.size() > 1
//				&& currentIndex > 0)
//    		btnBack.setEnabled(true);
//    	else
//    		btnBack.setEnabled(false);
//
//    	if (entryList.size() > 1
//				&& currentIndex < entryList.size() - 1)
//    		btnForward.setEnabled(true);
//    	else
//    		btnForward.setEnabled(false);

    }


//    @SuppressWarnings("restriction")
//    public void goBack() {
//    	Platform.runLater(() -> {
//        	int currentIndex = history.getCurrentIndex();
//        	//System.out.println("goBack()'s currentIndex : " + currentIndex + "  size : " + entryList.size());
//    		history.go(entryList.size() > 1
//    				&& currentIndex > 0
//    				? -1
//    				: 0);
//
//       		showURL();
//
//        	currentIndex = history.getCurrentIndex();
//        	//System.out.println("goBack()'s currentIndex : " + currentIndex + "  size : " + entryList.size());
//        	if (entryList.size() > 1) {
//        		if (currentIndex > 0)
//        			btnBack.setEnabled(true);
//        		else
//        			btnBack.setEnabled(false);
//
//        		if  (currentIndex < entryList.size() - 1)
//        			btnForward.setEnabled(true);
//        		else
//        			btnForward.setEnabled(false);
//        	}
//        	else {
//        		btnBack.setEnabled(false);
//        		btnForward.setEnabled(false);
//        	}
//       	});
//
//	}
//
//    @SuppressWarnings("restriction")
//    public void goForward() {
//    	Platform.runLater(() -> {
//        	int currentIndex = history.getCurrentIndex();
//        	//System.out.println("goBack()'s currentIndex : " + currentIndex + "  size : " + entryList.size());
//
//    		history.go(entryList.size() > 1
//    				&& currentIndex < entryList.size() - 1
//    				? 1
//    				: 0);
//
//        	currentIndex = history.getCurrentIndex();
//        	//System.out.println("goBack()'s currentIndex : " + currentIndex + "  size : " + entryList.size());
//        	if (entryList.size() > 1) {
//        		if (currentIndex > 0)
//        			btnBack.setEnabled(true);
//        		else
//        			btnBack.setEnabled(false);
//
//        		if  (currentIndex < entryList.size() - 1)
//        			btnForward.setEnabled(true);
//        		else
//        			btnForward.setEnabled(false);
//        	}
//        	else {
//        		btnBack.setEnabled(false);
//        		btnForward.setEnabled(false);
//        	}
//
//    	});
//	}


    public String getTextInputCache() {
    	return textInputCache;
    }

    public void setTextInputCache(String value) {
    	textInputCache = value;
    }

    public BorderPane getBorderPane() {
    	return borderPane;
    }
    
    
    public void destroy() {
        jfxPanel = null;
        panel = null;
        statusBarLbl = null;
        progressBar = null;
        mainScene = null;
        desktop = null;
        view = null;
        engine = null;
        history = null;
        entryList = null;
        reloadButton = null;
        backButton = null;
        forwardButton = null;
        tf = null;
        comboBox = null;
        bar = null;
        vbox = null;
        topButtonBar = null;
        ourGuide = null;
    	ssm = null;
    	borderPane = null;
    }

}

class TicketSubmission {
	public String submit(String name) {
		return "Hi," + name;
	}
}