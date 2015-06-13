package org.mars_sim.msp.ui.jme3;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class JmeWindow implements EventSubscriber<JmeRepositionEvent> {

	private JFrame frame;
	private Canvas canvas;
	private Pane jmePane;
	private Stage stage;
	private Scene scene;

	public JmeWindow(Canvas canvas) {

		this.canvas = canvas;
	}

/*
		EventBus.subscribeStrongly(JmeRepositionEvent.class, this);

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					initialize();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
*/

	private void initialize() {

		JPanel panel = new JPanel(new BorderLayout(0, 0));
		panel.add(canvas);
		frame = new JFrame();
		frame.setSize(new Dimension(480, 480));
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setAlwaysOnTop(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(false);
		frame.setUndecorated(true);
		frame.setVisible(true);

	}

	@Override
	public void onEvent(JmeRepositionEvent event) {
		frame.setLocation(event.x, event.y);
		frame.setSize(event.w, event.h);
	}


	public void createJMEWindow(Stage stage) {
		this.scene = stage.getScene();

		jmePane = new Pane();
		ChangeListener<Object> repoListener = new RepositionListener();

		stage.xProperty().addListener(repoListener);
		stage.yProperty().addListener(repoListener);
		stage.getScene().xProperty().addListener(repoListener);
		stage.getScene().yProperty().addListener(repoListener);
		jmePane.widthProperty().addListener(repoListener);
		jmePane.heightProperty().addListener(repoListener);

	}

	private class RepositionListener implements ChangeListener<Object> {

		@Override
		public void changed(ObservableValue<? extends Object> arg0, Object arg1, Object arg2) {
			int x = (int) (stage.getX() + jmePane.getLocalToSceneTransform().getTx()); //+ scene.getX()
			int y = (int) (stage.getY() + jmePane.getLocalToSceneTransform().getTy()); // scene.getY()
			int w = (int) jmePane.getWidth();
			int h = (int) jmePane.getHeight();
			EventBus.publish(new JmeRepositionEvent(x, y, w, h));
		}
	}

}