package org.mars_sim.msp.ui.swing.tool;

/*
 *  TexturedPanel.java
 *  2006-11-02
 */

//cb.aloe.decor;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * A JPanel with a textured background.
 * 
 * @author Christopher Bach
 */
public class TexturedPanel extends JPanel {

  private TexturePaint ourPainter = null;

  private Color ourDefaultForeground = Color.white;

  private Color ourDefaultBackground = Color.gray;

  /**
   * Creates a new TexturedPanel with a simple striped pattern.
   */
  public TexturedPanel() {
    super();
    ourDefaultForeground = Color.white;
    ourDefaultBackground = getBackground();
    setupDefaultPainter(ourDefaultForeground, ourDefaultBackground);
  }

  /**
   * Creates a new TexturedPanel with a simple striped pattern consisting of the
   * given foreground and background colors.
   */
  public TexturedPanel(Color foreground, Color background) {
    super();
    ourDefaultForeground = (foreground != null ? foreground : Color.white);
    ourDefaultBackground = (background != null ? background : getBackground());

    setupDefaultPainter(ourDefaultForeground, ourDefaultBackground);
  }

  /**
   * Creates a new TexturedPanel with a simple pattern based on the provided
   * texture map and consisting of the given foreground and background colors.
   */
  public TexturedPanel(Color foreground, Color background, boolean[][] texture) {
    super();
    ourDefaultForeground = (foreground != null ? foreground : Color.white);
    ourDefaultBackground = (background != null ? background : getBackground());

    setupTexturePainter(ourDefaultForeground, ourDefaultBackground, texture, 1);
  }

  /**
   * Creates a new TexturedPanel with a simple pattern based on the provided
   * texture map and consisting of the given foreground and background colors.
   */
  public TexturedPanel(Color foreground, Color background, boolean[][] texture, int scale) {
    super();
    ourDefaultForeground = (foreground != null ? foreground : Color.white);
    ourDefaultBackground = (background != null ? background : getBackground());

    setupTexturePainter(ourDefaultForeground, ourDefaultBackground, texture, scale);
  }

  /**
   * Creates a new TexturedPanel that tiles the provided image.
   */
  public TexturedPanel(Image texture) {
    super();
    ourDefaultForeground = Color.white;
    ourDefaultBackground = getBackground();

    if (texture != null)
      setupImagePainter(texture);
    else
      setupDefaultPainter(ourDefaultForeground, ourDefaultBackground);
  }

  /**
   * Creates a new TexturedPanel that tiles the provided icon.
   */
  public TexturedPanel(Icon textureIcon) {
    super();
    ourDefaultForeground = Color.white;
    ourDefaultBackground = getBackground();

    if (textureIcon != null) {
      setupIconPainter(textureIcon);
    }

    else
      setupDefaultPainter(ourDefaultForeground, ourDefaultBackground);
  }

  /**
   * Sets up this TexturedPanel to paint a simple background based on the
   * provided texture and consisting of the provided colors.
   */
  public void setTexture(Color foreground, Color background, boolean[][] texture) {
    if (foreground != null && background != null && texture != null && texture.length > 0
        && texture[0].length > 0) {
      setupTexturePainter(foreground, background, texture, 1);
    }
  }

  /**
   * 
   */
  public void setTexture(Color foreground, Color background, boolean[][] texture, int scale) {
    setupTexturePainter(foreground, background, texture, scale);
  }

  /**
   * Sets up this TexturedPanel to paint a striped background consisting of the
   * provided colors.
   */
  public void setTextureColors(Color foreground, Color background) {
    if (foreground != null && background != null) {
      ourDefaultForeground = foreground;
      ourDefaultBackground = background;
      setupDefaultPainter(foreground, background);
    }
  }

  /**
   * Sets up this TexturedPanel to paint a tiled background consisting of the
   * provided image. If the image is null, the background will revert to a
   * striped texture consisting of the last known colors.
   */
  public void setTextureImage(Image texture) {
    if (texture != null)
      setupImagePainter(texture);
    else
      setupDefaultPainter(ourDefaultForeground, ourDefaultBackground);
  }

  /**
   * Sets up this TexturedPanel to paint a tiled background consisting of the
   * provided icon. If the icon is null, the background will revert to a striped
   * texture consisting of the last known colors.
   */
  public void setTextureIcon(Icon textureIcon) {
    if (textureIcon != null) {
      setupIconPainter(textureIcon);
    }

    else
      setupDefaultPainter(ourDefaultForeground, ourDefaultBackground);
  }

  /**
   * Returns the image buffer used by this TexturedPanel's painter.
   */
  public Image getTexture() {
    if (ourPainter == null)
      return null;
    else
      return ourPainter.getImage();
  }

  /**
   * Creates a new TexturePaint using the provided colors.
   */
  private void setupDefaultPainter(Color foreground, Color background) {
    if (foreground == null || background == null) {
      ourPainter = null;
      return;
    }

    BufferedImage buff = new BufferedImage(6, 6, BufferedImage.TYPE_INT_ARGB_PRE);

    Graphics2D g2 = buff.createGraphics();

    g2.setColor(background);
    g2.fillRect(0, 0, 6, 6);

    g2.setColor(foreground);
    g2.drawLine(0, 2, 6, 2);
    g2.drawLine(0, 5, 6, 5);

    ourPainter = new TexturePaint(buff, new Rectangle(0, 0, 6, 6));

    g2.dispose();
  }

  /**
   * Creates a new TexturePaint using the provided colors and texture map.
   */
  private void setupTexturePainter(Color foreground, Color background, boolean[][] texture,
      int scale) {
    if (texture == null || texture.length < 1 || texture[0].length < 1) {
      setupDefaultPainter(foreground, background);
      return;
    }

    else if (foreground == null || background == null) {
      ourPainter = null;
      return;
    }

    scale = Math.max(1, scale);
    int w = texture[0].length;
    int h = texture.length;

    BufferedImage buff = new BufferedImage(w * scale, h * scale, BufferedImage.TYPE_INT_ARGB_PRE);

    Graphics2D g2 = buff.createGraphics();

    g2.setColor(background);
    g2.fillRect(0, 0, w * scale, h * scale);

    g2.setColor(foreground);
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        try {
          if (texture[i][j])
            g2.fillRect(j * scale, i * scale, scale, scale);
        }
        // g2.drawLine(j, i, j, i); }
        catch (ArrayIndexOutOfBoundsException aob) {
        }
      }
    }

    ourPainter = new TexturePaint(buff, new Rectangle(0, 0, w * scale, h * scale));

    g2.dispose();
  }

  /**
   * Creates a new TexturePaint using the provided image.
   */
  private void setupImagePainter(Image texture) {
    if (texture == null) {
      ourPainter = null;
      return;
    }

    int w = texture.getWidth(this);
    int h = texture.getHeight(this);

    if (w <= 0 || h <= 0) {
      ourPainter = null;
      return;
    }

    BufferedImage buff = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);

    Graphics2D g2 = buff.createGraphics();
    g2.drawImage(texture, 0, 0, this);
    ourPainter = new TexturePaint(buff, new Rectangle(0, 0, w, h));

    g2.dispose();
  }

  /**
   * Creates a new TexturePaint using the provided icon.
   */
  private void setupIconPainter(Icon texture) {
    if (texture == null) {
      ourPainter = null;
      return;
    }

    int w = texture.getIconWidth();
    int h = texture.getIconHeight();

    if (w <= 0 || h <= 0) {
      ourPainter = null;
      return;
    }

    BufferedImage buff = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);

    Graphics2D g2 = buff.createGraphics();
    texture.paintIcon(this, g2, 0, 0);
    ourPainter = new TexturePaint(buff, new Rectangle(0, 0, w, h));

    g2.dispose();
  }

  /**
   * Paints this component with its textured background.
   */
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (ourPainter != null) {
      int w = getWidth();
      int h = getHeight();
      Insets in = getInsets();

      int x = in.left;
      int y = in.top;
      w = w - in.left - in.right;
      h = h - in.top - in.bottom;

      if (w >= 0 && h >= 0) {
        Graphics2D g2 = (Graphics2D) g;
        Paint pt = g2.getPaint();
        g2.setPaint(ourPainter);
        g2.fillRect(x, y, w, h);
        g2.setPaint(pt);
      }
    }
  }

}