package org.mars_sim.msp.ui.sodium.swidgets.app;


import org.mars_sim.msp.ui.sodium.swidgets.SButton;
import org.mars_sim.msp.ui.sodium.swidgets.SLabel;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nz.sodium.CellLoop;
import nz.sodium.Stream;
import nz.sodium.Transaction;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class SSpinnerFX extends Application {
	
   @Override
    public void start(Stage primaryStage) throws Exception {

        SwingNode node = new SwingNode();
		JPanel mainPane = new JPanel(new FlowLayout(20,20,20));
		mainPane.setSize(400, 160);
	
        Transaction.runVoid(() -> {
            CellLoop<Integer> value = new CellLoop<>();
            SLabel lblValue = new SLabel(
                         value.map(i -> Integer.toString(i)));
            lblValue.setMinimumSize(new Dimension(100, 30));
            SButton plus = new SButton("+");
            SButton minus = new SButton("-");
            mainPane.add(minus);
            mainPane.add(lblValue);
            mainPane.add(plus);
            Stream<Integer> sPlusDelta = plus.sClicked.map(u -> 1);
            Stream<Integer> sMinusDelta = minus.sClicked.map(u -> -1);
            Stream<Integer> sDelta = sPlusDelta.orElse(sMinusDelta);
            Stream<Integer> sUpdate = sDelta.snapshot(value,
                    (delta, value_) -> delta + value_
                ).filter(n -> n >= 0);
            value.loop(sUpdate.hold(0));
        });
        
		SwingUtilities.invokeLater(() -> node.setContent(mainPane));
		node.requestFocus();
        BorderPane pane = new BorderPane(node);
        
        Scene scene = new Scene(pane, 600, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
   }
	   
	   
    public static void main(String[] args) {
        launch(args);
    }
}

