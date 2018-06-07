package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

@SuppressWarnings("serial")
public class MoveRedDot extends JPanel {   
   private static final int PREF_W = 500;
   private static final int PREF_H = PREF_W;
   private static final int ANIMATION_DELAY = 15;
   private static final int RECT_WIDTH = 15;
   private static final Color RECT_COLOR = Color.red;
   private EnumMap<KeyboardDirection, Boolean> enumMap = new EnumMap<>(KeyboardDirection.class);
   private Map<Integer, KeyboardDirection> keyboardMap = new HashMap<>();
   private Timer animationTimer;
   public int xLoc;
   public int yLoc;


   public MoveRedDot() {
      for (KeyboardDirection dir : KeyboardDirection.values()) {
         enumMap.put(dir, Boolean.FALSE);
      }
      keyboardMap.put(KeyEvent.VK_UP, KeyboardDirection.UP);
      keyboardMap.put(KeyEvent.VK_DOWN, KeyboardDirection.DOWN);
      keyboardMap.put(KeyEvent.VK_LEFT, KeyboardDirection.LEFT);
      keyboardMap.put(KeyEvent.VK_RIGHT, KeyboardDirection.RIGHT);
      setKeyBindings();

      animationTimer = new Timer(ANIMATION_DELAY, new AnimationListener());
      animationTimer.start();
   }

   @Override
   public Dimension getPreferredSize() {
      return new Dimension(PREF_W, PREF_H);
   }

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      g.setColor(RECT_COLOR);
      g.fillRect(xLoc, yLoc, RECT_WIDTH, RECT_WIDTH);
   }

   private void setKeyBindings() {
      int condition = WHEN_IN_FOCUSED_WINDOW;
      final InputMap inputMap = getInputMap(condition);
      final ActionMap actionMap = getActionMap();
      boolean[] keyPressed = { true, false };
      for (Integer keyCode : keyboardMap.keySet()) {
         KeyboardDirection dir = keyboardMap.get(keyCode);
         for (boolean onKeyPress : keyPressed) {
            boolean onKeyRelease = !onKeyPress;
            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, 0,
                  onKeyRelease);
            Object key = keyStroke.toString();
            inputMap.put(keyStroke, key);
            actionMap.put(key, new KeyBindingsAction(dir, onKeyPress));
         }
      }
   }

   private class KeyBindingsAction extends AbstractAction {
      private KeyboardDirection dir;
      boolean pressed;

      public KeyBindingsAction(KeyboardDirection dir, boolean pressed) {
         this.dir = dir;
         this.pressed = pressed;
      }

      @Override
      public void actionPerformed(ActionEvent evt) {
         enumMap.put(dir, pressed);
      }
   }

   private class AnimationListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent evt) {
         boolean repaint = false;
         for (KeyboardDirection dir : KeyboardDirection.values()) {
            if (enumMap.get(dir)) {
               xLoc += dir.getIncrX();
               yLoc += dir.getIncrY();
               repaint = true;
            }
         }
         if (repaint) {
            repaint();
         }
      }
   }

   private static void createAndShowGui() {
      MoveRedDot mainPanel = new MoveRedDot();

      JFrame frame = new JFrame("Class1B");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().add(mainPanel);
      frame.pack();
      frame.setLocationByPlatform(true);
      frame.setVisible(true);
   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            createAndShowGui();
         }
      });
   }
}

enum KeyboardDirection {
   UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
   private int incrX;
   private int incrY;

   private KeyboardDirection(int incrX, int incrY) {
      this.incrX = incrX;
      this.incrY = incrY;
   }

   public int getIncrX() {
      return incrX;
   }

   public int getIncrY() {
      return incrY;
   }
}
