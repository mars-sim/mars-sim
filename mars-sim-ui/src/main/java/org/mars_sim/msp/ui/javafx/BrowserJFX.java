/* Mars Simulation Project
 * BrowserJFX.java
 * @version 3.08 2015-12-31
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
//import java.util.EventListener;

import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State;

public class BrowserJFX {

    public static final String EVENT_TYPE_CLICK = "click";
    public static final String EVENT_TYPE_MOUSEOVER = "mouseover";
    public static final String EVENT_TYPE_MOUSEOUT = "mouseclick";
 
    public static final String MSP_HEADER = "msp://";
    public static final String HTTP_HEADER = "http://";
    public static final String HTTPS_HEADER = "https://";
    
    public String textInputCache;
    
	private boolean isLocalHtml = false, isInternal = false;

    private final JFXPanel jfxPanel = new JFXPanel();
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel statusBarLbl = new JLabel();
    private final JButton btnGo = new JButton("Go");
    private final JTextField urlTF = new JTextField();
    private final JProgressBar progressBar = new JProgressBar();

    private WebEngine engine;

    private GuideWindow guideWindow;
    
    public BrowserJFX(GuideWindow gw) {
    	this.guideWindow = gw;
    }

    // 2016-04-22 Added ability to interpret internal commands
    public JPanel init() {
        initJFX();
        
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	
        		String input = urlTF.getText().trim();          	
        		textInputCache = input; 

        		if (input.equals("ticket") 
        				|| input.equals("map1")
        				|| input.equals("globe1")) {
        			parseInput(input, 0);
        		}
        		else 
        			parseInput(input, 2); //or 3 
            }
        };
        
        btnGo.addActionListener(al);
        urlTF.addActionListener(al);			

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);

        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        topBar.add(urlTF, BorderLayout.CENTER);
        topBar.add(btnGo, BorderLayout.EAST);

        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(statusBarLbl, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(jfxPanel, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);

        return panel;
    }

    // 2016-04-22 Added parseInput()
    public void parseInput(String input, int URL_type) {
		
		//boolean status = input.toLowerCase().contains(MSP_HEADER.toLowerCase());          	
		//int pos = input.toLowerCase().indexOf(MSP_HEADER.toLowerCase());
		
		//if (status && pos == 0) {
			// remove the "msp://"
		//	input = input.replace(MSP_HEADER, "");         		
		//}
		
			
		if (URL_type == 0)  {
			isInternal = true;
			isLocalHtml = true;
			System.out.println("BrowserJFX : input is " + input);
			//URL url = getClass().getResource(Msg.getString("doc.help") + input + ".html");
			//loadLocalURL(url.toExternalForm());
			updateHistory(input + ".html", 0);
	    }
		else if (URL_type == 1) {
			isLocalHtml = true;			
			updateHistory(input, 1);
		}
		
		else {
			isInternal = false;
			
			boolean status = input.toLowerCase().contains(HTTPS_HEADER.toLowerCase());          	
			//int pos = input.toLowerCase().indexOf(HTTPS_HEADER.toLowerCase());
			
			if (status) {// && pos == 0) {
				//input = input.replace(HTTP_HEADER, "");  
				isLocalHtml = false;
				updateHistory(input, 2);
				//loadRemoteURL(input);
			}
			else {
				status = input.toLowerCase().contains(HTTP_HEADER.toLowerCase());          	
				//pos = input.toLowerCase().indexOf(HTTP_HEADER.toLowerCase());
				
				if (status) {// && pos == 0) {
					//input = input.replace(HTTP_HEADER, "");
					isLocalHtml = false;
					updateHistory(input, 2);
					//loadRemoteURL(input);
				}
				else {
					// e.g. type in google.com
					isLocalHtml = false;
					// will need to add http://
					updateHistory(input, 3);
									
				}				
			}
			
			
		}
    }
	
    @SuppressWarnings("restriction")
	private void initJFX() {

        Platform.runLater(() -> {       
                WebView view = new WebView();
                view.setStyle("-fx-background-color: #656565;"
                		+ " -fx-font-color: white;"
                		+ " -fx-border-color: #00a7c8");
                engine = view.getEngine();              
                Worker<?> worker = engine.getLoadWorker();
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
                engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                    @Override
                    public void handle(final WebEvent<String> event) {
                        SwingUtilities.invokeLater(()->{
                        	//System.out.println("setOnStatusChanged()");
                        	// Note: it shows the content of the hyperlink (even before the user clicks on it.
                            String content = event.getData();
                            if (content != null && !content.equals(null)) {
                            	statusBarLbl.setText(content);
                                // TODO: tweak how lblStatus displays the loading of a internal file having file:///... 
                                //System.out.println("BrowserJFX : event.getData() is [" + event.getData() + "]");
                            }
                        });
                    }
                });

                engine.locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(() ->{
                        	//System.out.println("locationProperty()");
                        	if (isLocalHtml) {
                            	// TODO: consider parsing what to display on the textfield AFTER the user clicks on it.
                        		if (isInternal)
                            		urlTF.setText(textInputCache);
                        		else
                        			urlTF.setText("");
                        		//System.out.println("BrowserJFX : isLocalHtml is true. setText() to null");	
                        	}
                        	else {
                            	// Note: it displays the content of a remote URL AFTER the user clicks on it.
                        		urlTF.setText(newValue);
                        		//System.out.println("BrowserJFX : isLocalHtml is false. setText(newValue). newValue is " + newValue);
                        	}
                        });
                    }
                });

                worker.workDoneProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                        SwingUtilities.invokeLater(()->{
                        	//System.out.println("workDoneProperty()");
                                progressBar.setValue(newValue.intValue());
                        });
                    }
                });

                worker.exceptionProperty().addListener(new ChangeListener<Throwable>() {

                public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                	if (worker.getState() == FAILED) {
                		SwingUtilities.invokeLater(()-> {
                				System.out.println("exceptionProperty()");
                				//System.out.println("BrowserJFX : worker.getState() == FAILED");
/*                				JOptionPane.showMessageDialog(
                                            panel,
                                            (value != null) ?
                                            engine.getLocation() + "\n" + value.getMessage() :
                                            engine.getLocation() + "\nUnexpected error.",
                                            "Loading error...",
                                            JOptionPane.ERROR_MESSAGE);
*/
                		});
                	}
             	
                }});

                // Listens for clicking on a hyperlink (internal link on a local html file or remote link on a local html file)
                worker.stateProperty().addListener(new ChangeListener<javafx.concurrent.Worker.State>() {
                    public void changed(ObservableValue ov, State oldState, State newState) {
                        if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                             // Note : classes are from org.w3c.dom domain
                        	EventListener listener = new EventListener() {
								@Override
								public void handleEvent(org.w3c.dom.events.Event ev) {	
									//System.out.println("stateProperty()");
									String href = ((Element)ev.getTarget()).getAttribute("href");                                	
									if (href != null && !href.equals(null)) {									
										System.out.println("BrowserJFX : before calling parseInput(). href is " + href);
										
										boolean status = href.toLowerCase().contains(HTTPS_HEADER.toLowerCase());       
										
										if (status) {
											parseInput(href, 2);
										}
										else {
											status = href.toLowerCase().contains(HTTP_HEADER.toLowerCase());       
											if (status) {
												parseInput(href, 2);
											}	
											else {
												parseInput(href, 1);
											}
										}
										
/*		                                if (isLocalHtml) {
											// Note: call up updateHistory to update index and buttons and load the local html file
		                                	updateHistory(href, false);
		                                }
		                                else {
		                                	updateHistory(href, true);
		                                }
*/	                                }
								}
                            };

                            Document doc = engine.getDocument();                   
                            NodeList nodeList = doc.getElementsByTagName("a");
                            for (int i = 0; i < nodeList.getLength(); i++) {
                                ((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_CLICK, listener, false);
                                //((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_MOUSEOVER, listener, false);
                                //((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_MOUSEOVER, listener, false);
                            }
                        }
                    }
                });
                
                jfxPanel.setScene(new Scene(view));
        });
    }

	// 2016-04-18 Added updateHistory()
    public void updateHistory(String href, int URL_type) {
    	if (URL_type == 0 || URL_type == 1) {
	    	URL url = getClass().getResource(Msg.getString("doc.help") + href);
	    	//System.out.println("BrowserJFX : updateHistory(). Type " + URL_type + " url is " + url);
			guideWindow.updateHistory(url);
	    	guideWindow.updateButtons(); 
	    	loadLocalURL(url.toExternalForm());
    	}
    	else if (URL_type == 2) {
    		try {
    			URL url = new URL(href);
    	    	//System.out.println("BrowserJFX : updateHistory(). Type 2. url is " + url);
				guideWindow.updateHistory(url);
		    	guideWindow.updateButtons();
		    	loadRemoteURL(url.toExternalForm());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else if (URL_type == 3) {
    		try {
    			URL url = new URL(HTTP_HEADER + href);
    	    	//System.out.println("BrowserJFX : updateHistory(). Type 2. url is " + url);
				guideWindow.updateHistory(url);
		    	guideWindow.updateButtons();
		    	loadRemoteURL(url.toExternalForm());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
   
    @SuppressWarnings("restriction")
	public void loadRemoteURL(final String input) {
    	isLocalHtml = false;
    	isInternal = false;
    	
        Platform.runLater(()-> {
/*            String url2 = toURL(url);
            //System.out.println("before, tmp is "+ tmp);
            if (url2 == null) {
            	if (isSecure)
            		url2 = toURL("https://" + url);
            	else
            		url2 = toURL("http://" + url);
            	
            }
            //System.out.println("before, tmp is "+ tmp);
*/            
        				
			boolean status = input.toLowerCase().contains(HTTPS_HEADER.toLowerCase());          	
			//int pos = input.toLowerCase().indexOf(HTTPS_HEADER.toLowerCase());
			
			if (status) {// && pos == 0) {
				//input = input.replace(HTTP_HEADER, "");  
				engine.load(input);
			}
			else {
				status = input.toLowerCase().contains(HTTP_HEADER.toLowerCase());          	
				//pos = input.toLowerCase().indexOf(HTTP_HEADER.toLowerCase());
				
				if (status) {// && pos == 0) {
					//input = input.replace(HTTP_HEADER, "");
					engine.load(input);
				}
				else {
					engine.load(HTTP_HEADER + input);
					
					// TODO: should it try https as well ?
					
					// TODO: how to handle the case when the remote url is bad ?
					// should delete this bad url and its history index, instead of saving it
					
				}				
			}
			
			
            // TODO: how to check if the url is valid or if it's loaded successfully?
        });
    }

    @SuppressWarnings("restriction")
	public void loadLocalURL(String input) {
       	isLocalHtml = true;
       	  	
        Platform.runLater(()-> {
            //System.out.println("before, path is "+ path);
            //String p = path.replace("file://", "file:///").replace("file:/", "file:///");
            //System.out.println("after, path is "+ p);
            engine.load(input);
        });
    }
/*
    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
                return null;
        }
    }
*/    
}