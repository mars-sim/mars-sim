package org.mars_sim.msp.ui.jme3;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;

import com.jme3.system.AppSettings;
import com.jme3.system.awt.AwtPanel;
import com.jme3.system.awt.AwtPanelsContext;
import com.jme3.system.awt.PaintMode;

import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class AwtCanvasWrapper extends Application {
    private static final int W = 400;
    private static final int H = 400;

    private static TestAwtPanels2 app;
    private static AwtPanel awtPanel;

    @Override public void start(final Stage stage) throws Exception {
        final AwtInitializerTask awtInitializerTask = new AwtInitializerTask(() -> {
            JPanel panel = new JPanel();

            panel.add(new CustomAwtCanvas(W, H));

            return panel;
        });

        SwingUtilities.invokeLater(awtInitializerTask);

        SwingNode swingNode = new SwingNode();
        swingNode.setContent(awtInitializerTask.get());

        stage.setScene(new Scene(new Group(swingNode), W, H));
        stage.setResizable(false);
        stage.show();
    }

    private class AwtInitializerTask extends FutureTask<JPanel> {
        public AwtInitializerTask(Callable<JPanel> callable) {
            super(callable);
        }
    }

    private class CustomAwtCanvas extends Canvas {

        public CustomAwtCanvas(int width, int height) {
            setSize(width, height);

            //app = new TestAwtPanels2();
            //app.start();
        }

        public void paint(Graphics g) {
            Graphics2D g2;
            g2 = (Graphics2D) g;
            g2.setColor(Color.GRAY);
            g2.fillRect(
                0, 0,
                (int) getSize().getWidth(), (int) getSize().getHeight()
            );
            g2.setColor(Color.BLACK);
            g2.drawString("It is a custom canvas area", 25, 50);
        }

    }


    public static void main(String[] args) {
        Application.launch(args);
    }

}