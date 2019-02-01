package org.mars_sim.javafx.tools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

public class SwingDialog extends JFrame { //or JDialog
    public static void main(String[] args){
        final SwingDialog dialog = new SwingDialog();
        dialog.setVisible(true);
    }

    public SwingDialog(){
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(250, 250));
        final JComboBox<String> combo = new JComboBox<String>();
        for (int i = 0; i< 101; i++){
            combo.addItem("text" + i);
        }
        final JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0,0,0));
        panel.setLayout(new FlowLayout());
        panel.setPreferredSize(new Dimension(100, 300));
        panel.add(combo);
        panel.add(createJFXPanel());
        panel.add(createJFXPanel0());
        final JScrollPane scroll = new JScrollPane(panel);
        getContentPane().add(scroll);
    }

    private JFXPanel createJFXPanel(){
        final JFXPanelEx panel = new JFXPanelEx();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final VBox vbox = new VBox();
                final ComboBox<String> combo = new ComboBox<String>();
                for (int i = 0; i< 101; i++){
                    combo.getItems().add("text" + i);
                }
                vbox.getChildren().addAll(combo);
                final Scene scene = new Scene(vbox);
                panel.setScene(scene);
            };
        });
        return panel;
    }

    private JFXPanel createJFXPanel0(){
        final JFXPanel panel = new JFXPanel();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final VBox vbox = new VBox();
                final ComboBox<String> combo = new ComboBox<String>();
                for (int i = 0; i< 101; i++){
                    combo.getItems().add("text" + i);
                }
                vbox.getChildren().addAll(combo);
                final Scene scene = new Scene(vbox);
                panel.setScene(scene);
            };
        });
        return panel;
    }
    
    private class JFXPanelEx extends JFXPanel {
        public JFXPanelEx(){
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    hidePopUps();
                }
            });
        }

        private ComponentListener componentListener;

        @Override
        public void removeNotify() {
            super.removeNotify();
            final Component parent = SwingUtilities.getRoot(this);
            if (parent != null){
                parent.removeComponentListener(componentListener);
            }
        }

        @Override
        public void addNotify() {
            super.addNotify();
            final Component parent = SwingUtilities.getRoot(this);
            componentListener = new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent e) {
                    hidePopUps();
                    processComponentEvent(new ComponentEvent(JFXPanelEx.this, ComponentEvent.COMPONENT_MOVED));
                }

                @Override
                public void componentResized(ComponentEvent e) {
                    hidePopUps();
                    processComponentEvent(new ComponentEvent(JFXPanelEx.this, ComponentEvent.COMPONENT_RESIZED));
                    processComponentEvent(new ComponentEvent(JFXPanelEx.this, ComponentEvent.COMPONENT_MOVED)); //is important!!!
                }
            };
            parent.addComponentListener(componentListener);
        }

        private void hidePopUps(){
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    hidePopUpsImpl();
                }
            };
            if (Platform.isFxApplicationThread()){
                runnable.run();
            } else {
                Platform.runLater(runnable);
            }
        }

        private void hidePopUpsImpl(){
            if (getScene() != null && getScene().getRoot() != null){
                for (final Node node : getScene().getRoot().getChildrenUnmodifiable()){ //JavaFX need interfaces!!!
                    final ComboBox<?> combo = (ComboBox<?>)node;
                    if (combo.isShowing()){ //JavaFX need interfaces!!!
                        combo.hide();
                    }
                }
            }
        }
    }
}