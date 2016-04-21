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
 
	private boolean isLocalHtml = true;

    private final JFXPanel jfxPanel = new JFXPanel();
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel lblStatus = new JLabel();
    private final JButton btnGo = new JButton("Go");
    private final JTextField txtURL = new JTextField();
    private final JProgressBar progressBar = new JProgressBar();

    private WebEngine engine;

    private GuideWindow guideWindow;
    
    public BrowserJFX(GuideWindow gw) {
    	this.guideWindow = gw;
    }

    public JPanel init() {
        initJFX();
        
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadRemoteURL(txtURL.getText());
            }
        };
        
        btnGo.addActionListener(al);
        txtURL.addActionListener(al);			

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);

        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        topBar.add(txtURL, BorderLayout.CENTER);
        topBar.add(btnGo, BorderLayout.EAST);

        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(jfxPanel, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);

        return panel;
    }

    @SuppressWarnings("restriction")
	private void initJFX() {

        Platform.runLater(new Runnable() {
            @SuppressWarnings("unchecked")
			@Override
            public void run() {

			    //GuideWindow gw = guideWindow;

                WebView view = new WebView();
                view.setStyle("-fx-background-color: #656565;"
                		+ " -fx-font-color: white;"
                		+ " -fx-border-color: #00a7c8");
                engine = view.getEngine();
                
                Worker worker = engine.getLoadWorker();
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
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                lblStatus.setText(event.getData());
                                //System.out.println("BrowserJFX : event.getData() is " + event.getData());
                            }
                        });
                    }
                });

                engine.locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                            	if (isLocalHtml) {
                            		txtURL.setText("");
                            		//System.out.println("BrowserJFX : isLocalHtml is true. setText() to null");	
                            	}
                            	else {
                            		txtURL.setText(newValue);
                            		//System.out.println("BrowserJFX : isLocalHtml is false. setText(newValue). newValue is " + newValue);
                            	}
                            }
                        });
                    }
                });

                worker.workDoneProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setValue(newValue.intValue());
                            }
                        });
                    }
                });

                worker.exceptionProperty().addListener(new ChangeListener<Throwable>() {

                public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                	if (worker.getState() == FAILED) {
                		SwingUtilities.invokeLater(new Runnable() {
                			@Override 
                			public void run() {
                				JOptionPane.showMessageDialog(
                                            panel,
                                            (value != null) ?
                                            engine.getLocation() + "\n" + value.getMessage() :
                                            engine.getLocation() + "\nUnexpected error.",
                                            "Loading error...",
                                            JOptionPane.ERROR_MESSAGE);
                			}
                		});
                	}
             	
                }});

                worker.stateProperty().addListener(new ChangeListener<javafx.concurrent.Worker.State>() {
                    public void changed(ObservableValue ov, State oldState, State newState) {
                        if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                                // note next classes are from org.w3c.dom domain
                        	EventListener listener = new EventListener() {
								@Override
								public void handleEvent(org.w3c.dom.events.Event ev) {
																	    
									String href = ((Element)ev.getTarget()).getAttribute("href");
                                	
									if (href != null) {
										//System.out.println("BrowserJFX : href is " + href);
	                                	updateHistory(href);
	                                }
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
            }
        });
    }

	// 2016-04-18 Added updateHistory()
    public void updateHistory(String href) {
    	//System.out.println("BrowserJFX : href is " + href);
    	//System.out.println("guideWindow is " + guideWindow);
    	URL url = getClass().getResource(Msg.getString("doc.help") + href);
    	//System.out.println("BrowserJFX : url is " + url);
		guideWindow.updateHistory(url);
    	guideWindow.updateButtons();
    	
    	loadLocalURL(url.toExternalForm());
    }
    
    @SuppressWarnings("restriction")
	public void loadRemoteURL(final String url) {
    	isLocalHtml = false;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String tmp = toURL(url);
                //System.out.println("before, tmp is "+ tmp);
                if (tmp == null) {
                    tmp = toURL("http://" + url);
                }
                //System.out.println("before, tmp is "+ tmp);
                engine.load(tmp);
            }
        });
    }

    @SuppressWarnings("restriction")
	public void loadLocalURL(String path) {
       	isLocalHtml = true;
       	  	
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //System.out.println("before, path is "+ path);
                String p = path.replace("file://", "file:///").replace("file:/", "file:///");
                //System.out.println("after, path is "+ p);
                engine.load(p);
            }
        });
    }

    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
                return null;
        }
    }
    
}