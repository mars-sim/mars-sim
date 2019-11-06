package org.mars_sim.msp.ui.swing.tool;

import java.awt.AlphaComposite;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * See https://stackoverflow.com/questions/12139001/add-a-transparent-gradient-to-a-jpanel
 */


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Administrator
 */
public class BarDrawer extends TexturedPanel {

    private int colorIndex;
    private Color[] colors = {
		Color.decode("#DE683A"),
		Color.decode("#DEB53A"),
		Color.decode("#B2DE3A"),
		Color.decode("#3ADEAD"),
		Color.decode("#3A65DE"),
		Color.decode("#973ADE"),
		Color.decode("#DE3A3A")
    };
    
    public LinkedHashMap<FileCollectionPanel, Double> collectionMap;

    public BarDrawer(LinkedHashMap<FileCollectionPanel, Double> collectionMap) throws IOException {
		this.collectionMap = collectionMap;
	
		this.collectionMap = new LinkedHashMap<FileCollectionPanel, Double>();
	//	collectionMap.put("Pictures", 13d);
	//	collectionMap.put("Music", 12d);
	//	collectionMap.put("Videos", 13d);
	//	collectionMap.put("TXT Files", 12d);
	//	collectionMap.put("EXE Files", 16d);
	//	collectionMap.put("Compressed Files", 10d);
	//	collectionMap.put("Downloads Folder", 1d);
	//	collectionMap.put("Another collection", 22d);
	
	
	//	collectionMap.put("Pictures", 66d);
	//	collectionMap.put("Music", 1d);
	//	collectionMap.put("Videos", 33d);
	
	
		FileCollectionPanel fcp1 = new FileCollectionPanel();
		fcp1.setTitle("Pictures");
		FileCollectionPanel fcp2 = new FileCollectionPanel();
		fcp2.setTitle("Music");
		FileCollectionPanel fcp3 = new FileCollectionPanel();
		fcp3.setTitle("Videos");
	//
		this.collectionMap.put(fcp1, 66d);
		this.collectionMap.put(fcp2, 11d);
		this.collectionMap.put(fcp3, 23d);
    }

    @Override
    protected void paintComponent(Graphics gr) {
		super.paintComponent(gr);
		Graphics2D g = (Graphics2D) gr;
		setPreferredSize(new Dimension(getWidth(), 150));
	
		Map desktopHints = (Map) (Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"));
		if (desktopHints != null) {
		    g.addRenderingHints(desktopHints);
		}
	
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
	
		colorIndex = 0;
		int realWidth = getWidth() - 1;
		int heightOfBar = (int) (getHeight() - 50);
	
	//	g.setColor(Color.decode("#cccccc"));
		double radius = getHeight() / 3;
		RoundRectangle2D r = new RoundRectangle2D.Double(0, 0, realWidth, heightOfBar, radius, radius * 2);
	
		g.setClip(getBounds());
		g.setColor(Color.black);
		double barSegment = realWidth / collectionMap.size();
		if (barSegment > 120) {
		    barSegment = 120;
		}
		int collectionNameX = 0;
	
		int x = 0;
		for (FileCollectionPanel fcp : collectionMap.keySet()) {
	
		    g.setColor(getNextColor());
	
		    double percentage = collectionMap.get(fcp) + adjustmentNeeded(collectionMap, fcp);
		    double width = (((double) percentage / 100) * realWidth);
	
		    g.setClip(r);
		    g.fillRect(x, 0, (int) width, heightOfBar);
	
		    g.setFont(g.getFont().deriveFont(14f));
		    String titleShortened = fcp.getTitle();
		    int lettersToRemove = 1;
		    while (g.getFontMetrics().stringWidth(titleShortened) > barSegment - 15) {
			titleShortened = shortenString(fcp.getTitle(), lettersToRemove);
			lettersToRemove++;
			titleShortened += "...";
		    }
	
		    g.setClip(getBounds());
		    g.setClip(0, 0, getWidth(), getHeight());
		    g.fillRect(collectionNameX + 15, heightOfBar + 10, 10, 10);
		    g.setColor(Color.black);
		    g.drawString(titleShortened, collectionNameX + 30, heightOfBar + 20);
		    System.out.println("heightOfBar = " + heightOfBar);
		    System.out.println("collectionNameX = " + collectionNameX);
		    g.setFont(g.getFont().deriveFont(11f));
		    g.drawString(Double.toString(collectionMap.get(fcp)) + "%", collectionNameX + 30, heightOfBar + 20 + g.getFontMetrics().getHeight());
	
		    collectionNameX += barSegment;
		    x = x + (int) width;
		}
	
		// draw lines
		g.setClip(r);
		for (int lineX = 15; lineX < realWidth - 1; lineX += 25) {
		    g.setColor(new Color(255, 255, 255, 75));
		    g.drawLine(lineX, 0, lineX - 15, heightOfBar - 1);
	
		    int gray = 128;
		    Color grayTransparent = new Color(gray, gray, gray, 75);
		    g.setColor(grayTransparent);
		    g.drawLine(lineX - 1, 0, lineX - 16, heightOfBar - 1);
		}
	
	
		// adding gradient
		Paint p = new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, 255), 0.0f, getHeight(), 
				new Color(255, 255, 255, 0));

//		Paint p2 = new LinearGradientPaint(
//				0.0f, 0.0f, getWidth(), getHeight(), new float[]{0.5f, 1.0f}, 
//			    new Color[] {
//			        new Color(255, 255, 255, 0),
//			        new Color(255, 255, 255, 128),
//			        new Color(255, 255, 255, 0),
//			    });
		
//		Paint p2 = new LinearGradientPaint(0.0f, 0.0f, getWidth(), getHeight()/8, 
//			new float[]{0.5f, .75f}, 
//			new Color[]{
//					new Color(255, 255, 255, 128), 
//					new Color(255, 255, 255, 0)
//			});
				
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
		
		g.setPaint(p);
		
		g.fillRect(0, 0, getWidth(), getHeight());
    }

    public static String shortenString(String s, int lettersToRemove) {
	if (lettersToRemove > s.length()) {
	    return Character.toString(s.charAt(0));
	}
	char[] cArr = new char[s.length() - lettersToRemove];
	for (int i = 0; i < cArr.length; i++) {
	    cArr[i] = s.charAt(i);
	}

	return new String(cArr);
    }

    /**
     * Checks for two things. First, it checks that the entry being dealt with
     * is the last entry in a {@link LinkedHashMap LinkedHashMap}. Second, if it
     * is, then the total of the all the values is counted. If the total is less
     * than 100, then the result of this method will be adjustment needed to get
     * to 100. E.g. if the total of all the doubles in the map is 98, and the
     * entry passed is the last one, then this method will return 2. Else, it
     * will return 0.
     *
     * @param map
     * @param value
     * @return
     */
    public double adjustmentNeeded(LinkedHashMap<FileCollectionPanel, Double> map, FileCollectionPanel fcp) {
	double minVal = Double.MAX_VALUE;
	FileCollectionPanel smallestFcp = null;
	for (FileCollectionPanel currentCollection : map.keySet()) {
	    double val = map.get(currentCollection);
	    if (minVal > val) {
		minVal = val;
		smallestFcp = currentCollection;
	    }
	}

	double total = 0;
	for (Double val : map.values()) {
	    total += val;
	}

	if ((total < 100d) && (smallestFcp.equals(fcp))) {
	    return 100d - total;
	}

	return 0;
    }

    public Color getSelectedColor() {
	return colors[colorIndex];
    }

    public Color getNextColor() {
	if (colorIndex < 0 || colorIndex >= colors.length - 1) {
	    colorIndex = 0;
	} else {
	    colorIndex++;
	}

	return colors[colorIndex];
    }

    public static void main(String[] args) throws IOException {
	JFrame frame = new JFrame();

	frame.add(new BarDrawer(null));
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setSize(600, 150);
	frame.setVisible(true);
    }
}

class FileCollectionPanel extends JPanel {

    private String title;

    public FileCollectionPanel() {
    }

    /**
     * @return the title
     */
    public String getTitle() {
	return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
	this.title = title;
    }
}