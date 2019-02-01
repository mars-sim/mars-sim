package org.mars_sim.javafx.tools;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;

import org.codefx.libfx.control.webview.WebViewHyperlinkListener;
import org.codefx.libfx.control.webview.WebViews;
import org.codefx.libfx.listener.handle.ListenerHandle;

/**
 * Demonstrates how to use the {@link WebViewHyperlinkListener}.
 */
public class WebViewHyperlinkListenerDemo extends Application {

	static final String PAGE_URL = "https://github.com/mars-sim/mars-sim/issues";
	// "https://en.wikipedia.org/wiki/Main_Page"
	// "https://sourceforge.net/p/mars-sim/discussion/"
	
	// #begin INITIALIZATION

	/**
	 * Runs this demo.
	 *
	 * @param args
	 *            command line arguments (will not be used)
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// controls
		WebView webView = createWebView();
		Label urlLabel = createUrlLabel();
		CheckBox listenerAttachedBox = createListenerAttachedBox();
		CheckBox cancelEventBox = createCancelEventBox();

		// listener
		WebViewHyperlinkListener listener = event -> {
			showEventOnLabel(event, urlLabel);
			return cancelEventBox.isSelected();
		};
		manageListener(webView, listener, listenerAttachedBox.selectedProperty());

		// put together
		VBox box = new VBox(webView, listenerAttachedBox, cancelEventBox, urlLabel);
		java.net.CookieHandler.setDefault(null);
		Scene scene = new Scene(box);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * Creates the web view to which the listener will be attached.
	 *
	 * @return a {@link WebView}
	 */
	private static WebView createWebView() {
		WebView webView = new WebView();
		System.setProperty("jsse.enableSNIExtension", "false");
		webView.getEngine().getLoadWorker().stateProperty().addListener(
				(obs, o, n) -> System.out.println("WEB VIEW WORKER STATUS: " + n));
		webView.getEngine().load(PAGE_URL); 
		return webView;
	}

	/**
	 * Creates the Label which will display the URL.
	 *
	 * @return a {@link Label}
	 */
	private static Label createUrlLabel() {
		return new Label();
	}

	/**
	 * Creates the check box with which the listener can be attached and detached.
	 *
	 * @return a {@link CheckBox}
	 */
	private static CheckBox createListenerAttachedBox() {
		return new CheckBox("hyperlink listener attached");
	}

	/**
	 * Creates the check box with which the further processing of events can be cancelled.
	 *
	 * @return a {@link CheckBox}
	 */
	private static CheckBox createCancelEventBox() {
		return new CheckBox("cancel event processing");
	}

	// #end INITIALIZATION

	// #begin LISTENER

	/**
	 * Attaches/detaches the specified listener to/from the specified web view according to the specified property's
	 * value.
	 *
	 * @param webView
	 *            the {@link WebView} to which the listener will be added
	 * @param listener
	 *            the added listener
	 * @param attachedProperty
	 *            defines whether the listener is attached or not
	 */
	private static void manageListener(WebView webView, WebViewHyperlinkListener listener,
			BooleanProperty attachedProperty) {
		attachedProperty.set(true);
		ListenerHandle listenerHandle = WebViews.addHyperlinkListener(webView, listener);

		attachedProperty.addListener((obs, wasAttached, isAttached) -> {
			if (isAttached) {
				listenerHandle.attach();
				System.out.println("LISTENER: attached.");
			} else {
				listenerHandle.detach();
				System.out.println("LISTENER: detached.");
			}
		});
	}

	/**
	 * Visualizes the specified event's type and URL on the specified label.
	 *
	 * @param event
	 *            the {@link HyperlinkEvent} to visualize
	 * @param urlLabel
	 *            the {@link Label} which will visualize the event
	 */
	private static void showEventOnLabel(HyperlinkEvent event, Label urlLabel) {
		if (event.getEventType() == EventType.ENTERED) {
			urlLabel.setTextFill(Color.BLACK);
			urlLabel.setText("ENTERED: " + event.getURL().toExternalForm());
			System.out.println("EVENT: " + WebViews.hyperlinkEventToString(event));
		} else if (event.getEventType() == EventType.EXITED) {
			urlLabel.setTextFill(Color.BLACK);
			urlLabel.setText("EXITED: " + event.getURL().toExternalForm());
			System.out.println("EVENT: " + WebViews.hyperlinkEventToString(event));
		} else if (event.getEventType() == EventType.ACTIVATED) {
			urlLabel.setText("ACTIVATED: " + event.getURL().toExternalForm());
			urlLabel.setTextFill(Color.RED);
			System.out.println("EVENT: " + WebViews.hyperlinkEventToString(event));
		}
	}

	// #end LISTENER

}
