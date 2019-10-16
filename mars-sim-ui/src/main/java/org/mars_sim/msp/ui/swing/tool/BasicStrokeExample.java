package org.mars_sim.msp.ui.swing.tool;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

public class BasicStrokeExample {
  public static void main(String[] args) {
    Frame frame = new Frame("BasicStrokeExample ");
    frame.setSize(300, 200);
    frame.add(new CanvasToDisplay());
    frame.setVisible(true);
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
  }
}

class CanvasToDisplay extends Component {
  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;
    BasicStroke bs = new BasicStroke(10);
    Rectangle2D r2d = new Rectangle2D.Double(10, 50, 280, 90);
    g2D.setStroke(bs);
    g2D.draw(r2d);
//    bs = new BasicStroke(2, BasicStroke.JOIN_MITER, BasicStroke.JOIN_BEVEL);
    bs = new BasicStroke(3f,
  	      BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, new float[] {1.0f}, 0.0f);
    r2d = new Rectangle2D.Double(30, 75, 60, 60);
    g2D.setStroke(bs);
    g2D.draw(r2d);
    bs = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    r2d = new Rectangle2D.Double(120, 75, 60, 40);
    g2D.setStroke(bs);
    g2D.draw(r2d);
    bs = new BasicStroke(2, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND);
    r2d = new Rectangle2D.Double(210, 75, 60, 40);
    g2D.setStroke(bs);
   
    float[] dashArray = bs.getDashArray();
    float dashPhase = bs.getDashPhase();
    int endCap = bs.getEndCap();
    int lineJoin = bs.getLineJoin();
    float lineWidth = bs.getLineWidth();
    float miterLimit = bs.getMiterLimit();
    Shape createStrokedShape = bs.createStrokedShape(r2d);
   
    g2D.draw(r2d);
  }
}