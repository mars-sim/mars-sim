package org.mars_sim.msp.ui.swing.tool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.configeditor.CrewEditor;

public class Test {

    public static void main(String[] args) {
        new Test();
    }

    public Test() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                JButton manage = new JButton("Manage");
                JButton add = new JButton("Add");
                JButton search = new JButton("Search");
                JButton exit = new JButton("Exit");
                CustomToolBar tb = new CustomToolBar();
                tb.add(manage);
                tb.add(add);
                tb.add(search);
                tb.add(exit);

                JFrame frame = new JFrame("Testing");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(new TestPane());
                frame.add(tb, BorderLayout.NORTH);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class TestPane extends JPanel {

        private BufferedImage bgImg;

        public TestPane() {
            setLayout(new BorderLayout());
               
            try {
                ImageIcon yourImage = new ImageIcon(Test.class.getResource(MainWindow.ICON_IMAGE));
                Image image = yourImage.getImage();
                bgImg = (BufferedImage) image;
//                bgImg = ImageIO.read(new File("...")); 
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return bgImg == null ? new Dimension(200, 200) : new Dimension(bgImg.getWidth(), bgImg.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImg != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                int x = (getWidth() - bgImg.getWidth()) / 2;
                int y = (getHeight() - bgImg.getHeight()) / 2;
                g2d.drawImage(bgImg, x, y, this);
                g2d.dispose();
            }
        }

    }

    public class CustomToolBar extends JToolBar {

        public CustomToolBar() {
            setBorder(new LineBorder(Color.BLACK, 2));
            setOpaque(false);
        }

        @Override
        protected void addImpl(Component comp, Object constraints, int index) {
            super.addImpl(comp, constraints, index);
            if (comp instanceof JButton) {
                ((JButton) comp).setContentAreaFilled(false);
            }
        }

    }
}