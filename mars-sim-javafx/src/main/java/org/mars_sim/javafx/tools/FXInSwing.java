package org.mars_sim.javafx.tools;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class FXInSwing extends JFrame{

    JFXPanel panel;
    Scene scene;
    StackPane stack;
    Text hello;

    boolean wait = true;

    public FXInSwing(){
        panel = new JFXPanel();
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                stack = new StackPane();
                scene = new Scene(stack,300,300);
                hello = new Text("Hello");

                scene.setFill(Color.BLACK);
                hello.setFill(Color.WHEAT);
                hello.setEffect(new Reflection());

                panel.setScene(scene);
                stack.getChildren().add(hello);

                wait = false;
            }
        });
        this.getContentPane().add(panel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 300);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                new FXInSwing();
            }
        });
    }
}