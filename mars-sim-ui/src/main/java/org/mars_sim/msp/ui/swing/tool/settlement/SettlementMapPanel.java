/**
 * Mars Simulation Project
 * SettlementMapPanel.java
 * @version 3.01 2011-06-12
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import org.apache.batik.gvt.GraphicsNode;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.construction.ConstructionEvent;
import org.mars_sim.msp.core.structure.construction.ConstructionListener;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.ui.swing.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A panel for displaying the settlement map.
 */
public class SettlementMapPanel extends JPanel implements UnitListener, ConstructionListener {

    // Static members.
    public static final double DEFAULT_SCALE = 5D;
    public static final double MAX_SCALE = 55D;
    public static final double MIN_SCALE = 5D / 11D;
    private static final Color BUILDING_COLOR = Color.GREEN;
    private static final Color CONSTRUCTION_SITE_COLOR = Color.BLACK;
    private static final Color LABEL_COLOR = Color.BLUE;
    private static final Color LABEL_OUTLINE_COLOR = new Color(255, 255, 255, 127);
    private static final Color MAP_BACKGROUND = new Color(181, 95, 0);
    private static final int MAX_BACKGROUND_IMAGE_NUM = 20;
    private static final int MAX_BACKGROUND_DIMENSION = 1600;
    
    // Data members.
    private Settlement settlement;
    private double xPos;
    private double yPos;
    private double rotation;
    private double scale;
    private boolean showLabels;
    private Map<Settlement, String> settlementBackgroundMap;
    private Map<Double, Map<GraphicsNode, BufferedImage>> svgImageCache;
    private Image backgroundTileImage;
    
    /**
     * A panel for displaying a settlement map.
     */
    public SettlementMapPanel() {
        // Use JPanel constructor.
        super();
        
        // Initialize data members.
        xPos = 0D;
        yPos = 0D;
        rotation = 0D;
        scale = DEFAULT_SCALE;
        settlement = null;
        showLabels = false;
        settlementBackgroundMap = new HashMap<Settlement, String>(20);
        svgImageCache = new HashMap<Double, Map<GraphicsNode, BufferedImage>>(21);
        
        // Set preferred size.
        setPreferredSize(new Dimension(400, 400));
        
        // Set foreground and background colors.
        setOpaque(true);
        setBackground(MAP_BACKGROUND);
        setForeground(Color.WHITE);
        
        // Set Apache Batik library system property so that it doesn't output: 
        // "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" in system err.
        System.setProperty("org.apache.batik.warn_destination", "false");
    }
    
    /**
     * Gets the settlement currently displayed.
     * @return settlement or null if none.
     */
    public Settlement getSettlement() {
        return settlement;
    }
    
    /**
     * Sets the settlement to display.
     * @param settlement the settlement.
     */
    public void setSettlement(Settlement settlement) {
        // Remove as unit and construction listener for old settlement.
        if (this.settlement != null) {
            this.settlement.removeUnitListener(this);
            Iterator<ConstructionSite> i = this.settlement.getConstructionManager()
                    .getConstructionSites().iterator();
            while (i.hasNext()) {
                i.next().removeConstructionListener(this);
            }
        }
        
        this.settlement = settlement;
        
        // Add as unit and construction listener for new settlement.
        if (settlement != null) {
            settlement.addUnitListener(this);
            Iterator<ConstructionSite> i = settlement.getConstructionManager()
                    .getConstructionSites().iterator();
            while (i.hasNext()) {
                i.next().addConstructionListener(this);
            }
        }
        
        backgroundTileImage = null;
        
        repaint();
    }
    
    /**
     * Gets the map scale.
     * @return scale (pixels per meter).
     */
    public double getScale() {
        return scale;
    }
    
    /**
     * Sets the map scale.
     * @param scale (pixels per meter).
     */
    public void setScale(double scale) {
        this.scale = scale;
        backgroundTileImage = null;
        repaint();
    }
    
    /**
     * Gets the map rotation.
     * @return rotation (radians).
     */
    public double getRotation() {
        return rotation;
    }
    
    /**
     * Sets the map rotation.
     * @param rotation (radians).
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
        repaint();
    }
    /**
     * Resets the position, scale and rotation of the map.
     * Separate function that only uses one repaint.
     */
    public void reCenter() {        
        xPos = 0D;
        yPos = 0D;
        setRotation(0D);
        scale = DEFAULT_SCALE;
        backgroundTileImage = null;
        repaint();
    }
    
    /**
     * Moves the center of the map by a given number of pixels.
     * @param xDiff the X axis pixels.
     * @param yDiff the Y axis pixels.
     */
    public void moveCenter(double xDiff, double yDiff) {
        xDiff /= scale;
        yDiff /= scale;
        
        // Correct due to rotation of map.
        double realXDiff = (Math.cos(rotation) * xDiff) + (Math.sin(rotation) * yDiff);
        double realYDiff = (Math.cos(rotation) * yDiff) - (Math.sin(rotation) * xDiff);
        
        xPos += realXDiff;
        yPos += realYDiff;
        repaint();
    }
    
    /**
     * Checks if labels should be displayed.
     * @return true if labels should be displayed.
     */
    public boolean isShowLabels() {
        return showLabels;
    }
    
    /**
     * Sets if labels should be displayed.
     * @param showLabels true if labels should be displayed.
     */
    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        //long startTime = System.nanoTime();
        
        Graphics2D g2d = (Graphics2D) g;
        
        // Set graphics rendering hints.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Draw background image tiles.
        drawBackgroundImageTiles(g2d);
        
        //long bgTime = System.nanoTime();
        //double bgTimeDiff = (bgTime - startTime) / 1000000D;
        
        double mapCenterX = getWidth() / 2D;
        double mapCenterY = getHeight() / 2D;
        
        // Translate map from settlement center point.
        g2d.translate(xPos * scale, yPos * scale);
        
        // Rotate map from North.
        g2d.rotate(rotation, mapCenterX - (xPos * scale), mapCenterY - (yPos * scale));
        
        // Draw each building.
        drawBuildings(g2d);
        
        // Draw each construction site.
        drawConstructionSites(g2d);
        
        //long endTime = System.nanoTime();
        //double timeDiff = (endTime - startTime) / 1000000D;
        //System.out.println("SMT paint time: " + (int) timeDiff + " ms, bg time: " + (int) bgTimeDiff + " ms");
    }
    
    /**
     * Draws the background image tiles on the map.
     */
    private void drawBackgroundImageTiles(Graphics2D g2d) {
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
        double mapCenterX = getWidth() / 2D;
        double mapCenterY = getHeight() / 2D;
        
        g2d.rotate(rotation, mapCenterX, mapCenterY);
        
        double diagonal = Math.hypot(getWidth(), getHeight());
        
        if (backgroundTileImage == null) {
            ImageIcon backgroundTileIcon = getBackgroundImage(settlement);
            double imageScale = scale / DEFAULT_SCALE;
            int imageWidth = (int) (backgroundTileIcon.getIconWidth() * imageScale);
            int imageHeight = (int) (backgroundTileIcon.getIconHeight() * imageScale);
        
            backgroundTileImage = resizeImage(backgroundTileIcon.getImage(), 
                    backgroundTileIcon.getImageObserver(), imageWidth, imageHeight);
        }
        
        if (backgroundTileImage != null) {
            
            int offsetX = (int) (xPos * scale);
            int tileWidth = backgroundTileImage.getWidth(this);
            int bufferX = (int) diagonal - getWidth();
            int tileCenterOffsetX = ((getWidth() / 2) % tileWidth) - (int) (1.5F * tileWidth);
            
            // Calculate starting X position for drawing tile.
            int startX = tileCenterOffsetX;
            while ((startX + offsetX) > (0 - bufferX)) {
                startX -= tileWidth;
            }
            while ((startX + offsetX) < (0 - tileWidth - bufferX)) {
                startX += tileWidth;
            }
            
            // Calculate ending X position for drawing tile.
            int endX = getWidth();
            while ((endX + offsetX) < (getWidth() + bufferX)) {
                endX += tileWidth;
            }
            while ((endX + offsetX) > (getWidth() + tileWidth + bufferX)) {
                endX -= tileWidth;
            }
            
            for (int x = startX; x < endX; x+= tileWidth) {
                
                int offsetY = (int) (yPos * scale);
                int tileHeight = backgroundTileImage.getHeight(this);
                int bufferY = (int) diagonal - getHeight();
                int tileCenterOffsetY = ((getHeight() / 2) % tileHeight) - (int) (1.5F * tileHeight);
                
                // Calculate starting Y position for drawing tile.
                int startY = tileCenterOffsetY;
                while ((startY + offsetY) > (0 - bufferY)) {
                    startY -= tileHeight;
                }
                while ((startY + offsetY) < (0 - tileHeight - bufferY)) {
                    startY += tileHeight;
                }
                
                // Calculate ending Y position for drawing tile.
                int endY = getHeight();
                while ((endY + offsetY) < (getHeight() + bufferY)) {
                    endY += tileHeight;
                }
                while ((endY + offsetY) > (getHeight() + tileHeight + bufferY)) {
                    endY -= tileHeight;
                }
                
                for (int y = startY; y < endY; y+= tileHeight) {
                    // Draw tile image.
                    g2d.drawImage(backgroundTileImage, x + offsetX, y + offsetY, this);
                }
            }
        }
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Creates a resized instance of a background image.
     * @param image the original background image.
     * @param width the resized image width.
     * @param height the resized image height.
     * @return image with the new size.
     */
    private Image resizeImage(Image image, ImageObserver observer, int width, int height) {
        Image result = image;
        
        int w = image.getWidth(observer);
        int h = image.getHeight(observer);
        
        do {
            if (w > width) {
                w /= 2;
                if (w < width) w = width;
            }
            else if (w < width) {
                w = width;
            }
            
            if (h > height) {
                h /= 2;
                if (h < height) h = height;
            }
            else if (h < height) {
                h = height;
            }
            
            int bufferWidth = w;
            int bufferHeight = h;
            int xOffset = 0;
            int yOffset = 0;
            if ((w > MAX_BACKGROUND_DIMENSION) || (h > MAX_BACKGROUND_DIMENSION)) {
                float reductionW = (float) MAX_BACKGROUND_DIMENSION / (float) w;
                float reductionH = (float) MAX_BACKGROUND_DIMENSION / (float) h;
                float reduction = reductionW;
                if (reductionH < reductionW) {
                    reduction = reductionH;
                }
                
                bufferWidth = (int) (w * reduction);
                bufferHeight = (int) (h * reduction);
                
                xOffset = (w - bufferWidth) / -2;
                yOffset = (h - bufferHeight) / -2;
            }
            
            BufferedImage tmpImage = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) tmpImage.getGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setClip(0, 0, bufferWidth, bufferHeight);
            g2d.drawImage(result, xOffset, yOffset, w, h, null);
            g2d.dispose();
            
            result = tmpImage;
            
        } while ((w != width) || (h != height));
        
        return result;
    }
    
    /**
     * Gets the background tile image icon for a settlement.
     * @param settlement the settlement to display.
     * @return the background tile image icon or null if none found.
     */
    private ImageIcon getBackgroundImage(Settlement settlement) {
        ImageIcon result = null;
        
        if (settlementBackgroundMap.containsKey(settlement)) {
            String backgroundImageName = settlementBackgroundMap.get(settlement);
            result = ImageLoader.getIcon(backgroundImageName, "jpg");
        }
        else {
            int count = 1;
            Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
            while (i.hasNext()) {
                if (i.next().equals(settlement)) {
                    String backgroundImageName = "settlement_map_tile" + count;
                    settlementBackgroundMap.put(settlement, backgroundImageName);
                    result = ImageLoader.getIcon(backgroundImageName, "jpg");
                }
                count++;
                if (count > MAX_BACKGROUND_IMAGE_NUM) {
                    count = 1;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Draw all of the buildings in the settlement.
     * @param g2d the graphics context.
     */
    private void drawBuildings(Graphics2D g2d) {
        if (settlement != null) {
            Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
            while (i.hasNext()) drawBuilding(i.next(), g2d);
        }
    }
    
    /**
     * Draws a building on the map.
     * @param building the building.
     * @param g2d the graphics context.
     */
    private void drawBuilding(Building building, Graphics2D g2d) {
        
        // Use SVG image for building if available.
        GraphicsNode svg = SVGMapUtil.getBuildingSVG(building.getName().toLowerCase());
        if (svg != null) {
            drawSVGStructure(g2d, building.getXLocation(), building.getYLocation(), 
                    building.getWidth(), building.getLength(), building.getFacing(), svg);
        }
        else {
            // Otherwise draw colored rectangle for building.
            drawRectangleStructure(g2d, building.getXLocation(), building.getYLocation(), 
                    building.getWidth(), building.getLength(), building.getFacing(), 
                    BUILDING_COLOR);
        }
        
        // Draw building label if displaying labels.
        if (showLabels) {
            drawLabel(g2d, building.getName(), building.getXLocation(), building.getYLocation());
        }
    }
    
    /**
     * Draw all of the construction sites in the settlement.
     * @param g2d the graphics context.
     */
    private void drawConstructionSites(Graphics2D g2d) {
        if (settlement != null) {
            Iterator<ConstructionSite> i = settlement.getConstructionManager().
                    getConstructionSites().iterator();
            while (i.hasNext()) drawConstructionSite(i.next(), g2d);
        }
    }
    
    /**
     * Draws a construction site on the map.
     * @param site the construction site.
     * @param g2d the graphics context.
     */
    private void drawConstructionSite(ConstructionSite site, Graphics2D g2d) {
        
        // Use SVG image for construction site if available.
        String stageName = site.getCurrentConstructionStage().getInfo().getName().toLowerCase();
        GraphicsNode svg = SVGMapUtil.getConstructionSiteSVG(stageName);
        if (svg != null) {
            drawSVGStructure(g2d, site.getXLocation(), site.getYLocation(), 
                    site.getWidth(), site.getLength(), site.getFacing(), svg);
        }
        else {
            // Else draw colored rectangle for construction site.
            drawRectangleStructure(g2d, site.getXLocation(), site.getYLocation(), 
                    site.getWidth(), site.getLength(), site.getFacing(), 
                    CONSTRUCTION_SITE_COLOR);
        }
        
        // Draw construction site label if displaying labels.
        if (showLabels) {
            drawLabel(g2d, getConstructionLabel(site), site.getXLocation(), site.getYLocation());
        }
    }
    
    /**
     * Gets the label for a construction site.
     * @param site the construction site.
     * @return the construction label.
     */
    private String getConstructionLabel(ConstructionSite site) {
        String label = "";
        ConstructionStage stage = site.getCurrentConstructionStage();
        if (stage != null) {
            if (site.isUndergoingConstruction()) {
                label = "Constructing " + stage.getInfo().getName();
            }
            else if (site.isUndergoingSalvage()) {
                label = "Salvaging " + stage.getInfo().getName();
            }
            else if (site.hasUnfinishedStage()) {
                if (stage.isSalvaging()) {
                    label = "Salvaging " + stage.getInfo().getName() + " unfinished";
                }
                else {
                    label = "Constructing " + stage.getInfo().getName() + " unfinished";
                }
            }
            else {
                label = stage.getInfo().getName() + " completed";
            }
        }
        else {
            label = "No construction";
        }
        
        return label;
    }
    
    /**
     * Draws a structure as a SVG image on the map.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param facing the structure facing (degrees from North clockwise).
     * @param svg the SVG graphics node.
     */
    private void drawSVGStructure(Graphics2D g2d, double xLoc, double yLoc,
            double width, double length, double facing, GraphicsNode svg) {
        
        drawStructure(true, g2d, xLoc, yLoc, width, length, facing, svg, null);
    }
    
    /**
     * Draws a structure as a rectangle on the map.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param facing the structure facing (degrees from North clockwise).
     * @param color the color to draw the rectangle.
     */
    private void drawRectangleStructure(Graphics2D g2d, double xLoc, double yLoc, 
            double width, double length, double facing, Color color) {
        
        drawStructure(false, g2d, xLoc, yLoc, width, length, facing, null, color);
    }
    
    /**
     * Draws a structure on the map.
     * @param isSVG true if using a SVG image.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param facing the structure facing (degrees from North clockwise).
     * @param svg the SVG graphics node.
     * @param color the color to display the rectangle if no SVG image.
     */
    private void drawStructure(boolean isSVG, Graphics2D g2d, double xLoc, double yLoc,
            double width, double length, double facing, GraphicsNode svg, Color color) {
        
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
        // Determine bounds.
        Rectangle2D bounds = null;
        if (isSVG) bounds = svg.getBounds();
        else bounds = new Rectangle2D.Double(0, 0, width, length);
        
        // Determine transform information.
        double scalingWidth = width / bounds.getWidth() * scale;
        double scalingLength = length / bounds.getHeight() * scale;
        
        double boundsPosX = bounds.getX() * scalingWidth;
        double boundsPosY = bounds.getY() * scalingLength;
        double centerX = width * scale / 2D;
        double centerY = length * scale / 2D;
        double centerMapX = getWidth() / 2D;
        double centerMapY = getHeight() / 2D;
        double translationX = (-1D * xLoc * scale) - centerX - boundsPosX + centerMapX;
        double translationY = (-1D * yLoc * scale) - centerY - boundsPosY + centerMapY;
        double facingRadian = facing / 180D * Math.PI;
        
        // Apply graphic transforms for structure.
        AffineTransform newTransform = new AffineTransform();
        newTransform.translate(translationX, translationY);
        newTransform.rotate(facingRadian, centerX, centerY);
        
        if (isSVG) {
            // Draw SVG image.
            /*
            newTransform.scale(scalingWidth, scalingLength);
            svg.setTransform(newTransform);
            svg.paint(g2d);
            g2d.setTransform(saveTransform);
            */
            
            // Draw buffered image of structure.
            BufferedImage image = getBufferedImage(svg, width, length);
            if (image != null) {
                g2d.transform(newTransform);
                g2d.drawImage(image, 0, 0, this);
            }
        }
        else {
            // Draw filled rectangle.
            newTransform.scale(scalingWidth, scalingLength);
            g2d.transform(newTransform);
            g2d.setColor(color);
            g2d.fill(bounds);
        }
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Gets a buffered image for a given graphics node.
     * @param svg the graphics node.
     * @param width the structure width.
     * @param length the structure length.
     * @return buffered image.
     */
    private BufferedImage getBufferedImage(GraphicsNode svg, double width, double length) {
        
        // Get image cache for current scale or create it if it doesn't exist.
        Map<GraphicsNode, BufferedImage> imageCache = null;
        if (svgImageCache.containsKey(scale)) {
            imageCache = svgImageCache.get(scale);
        }
        else {
            imageCache = new HashMap<GraphicsNode, BufferedImage>(100);
            svgImageCache.put(scale, imageCache);
        }
        
        // Get image from image cache or create it if it doesn't exist.
        BufferedImage image = null;
        if (imageCache.containsKey(svg)) image = imageCache.get(svg);
        else {
            image = createBufferedImage(svg, (int) width, (int) length);
            imageCache.put(svg, image);
        }
        
        return image;
    }
    
    /**
     * Creates a buffered image from a SVG graphics node.
     * @param svg the SVG graphics node.
     * @param width the width of the produced image.
     * @param length the length of the produced image.
     * @return the created buffered image.
     */
    private BufferedImage createBufferedImage(GraphicsNode svg, int width, int length) {
        BufferedImage bufferedImage = new BufferedImage((int) (width * scale), (int) (length * scale), 
                BufferedImage.TYPE_INT_ARGB);
        
        // Determine bounds.
        Rectangle2D bounds = svg.getBounds();
        
        // Determine transform information.
        double scalingWidth = width / bounds.getWidth() * scale;
        double scalingLength = length / bounds.getHeight() * scale;
        
        // Draw the SVG image on the buffered image.
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        svg.setTransform(AffineTransform.getScaleInstance(scalingWidth, scalingLength));
        svg.paint(g2d);

        // Cleanup and return image
        g2d.dispose();
        
        return bufferedImage;
    }
    
    /**
     * Draws a building or construction site label.
     * @param g2d the graphics 2D context.
     * @param label the label string.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     */
    private void drawLabel(Graphics2D g2d, String label, double xLoc, double yLoc) {
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
        // Determine bounds.
        TextLayout textLayout = new TextLayout(label, g2d.getFont(), g2d.getFontRenderContext());
        Rectangle2D bounds = textLayout.getBounds();
        
        // Determine transform information.
        double boundsPosX = bounds.getX();
        double boundsPosY = bounds.getY();
        double centerX = bounds.getWidth() / 2D;
        double centerY = bounds.getHeight() / 2D;
        double centerMapX = getWidth() / 2D;
        double centerMapY = getHeight() / 2D;
        double translationX = (-1D * xLoc * scale) - centerX - boundsPosX + centerMapX;
        double translationY = (-1D * yLoc * scale) - centerY - boundsPosY + centerMapY;
        
        // Apply graphic transforms for label.
        AffineTransform newTransform = new AffineTransform();
        newTransform.translate(translationX, translationY);
        newTransform.rotate(rotation * -1D, centerX, centerY);
        Shape labelShape = textLayout.getOutline(newTransform);
        
        // Draw label outline.
        Stroke saveStroke = g2d.getStroke();
        g2d.setColor(LABEL_OUTLINE_COLOR);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(labelShape);
        g2d.setStroke(saveStroke);
        
        // Fill label
        g2d.setColor(LABEL_COLOR);
        g2d.fill(labelShape);
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }

    @Override
    public void constructionUpdate(ConstructionEvent event) {
        // Draw map.
        repaint();
    }

    @Override
    public void unitUpdate(UnitEvent event) {
        // Add as listener for new construction sites.
        if (ConstructionManager.START_CONSTRUCTION_SITE_EVENT.equals(event.getType())) {
            ConstructionSite site = (ConstructionSite) event.getTarget();
            if (site != null) {
                site.addConstructionListener(this);
            }
        }
        
        // Redraw map for construction or building events.
        if (ConstructionManager.START_CONSTRUCTION_SITE_EVENT.equals(event.getType()) ||
                ConstructionManager.FINISH_BUILDING_EVENT.equals(event.getType()) ||
                ConstructionManager.FINISH_SALVAGE_EVENT.equals(event.getType()) ||
                BuildingManager.ADD_BUILDING_EVENT.equals(event.getType()) ||
                BuildingManager.REMOVE_BUILDING_EVENT.equals(event.getType())) {
            repaint();
        }
    }
    
    /**
     * Cleans up the map panel for disposal.
     */
    public void destroy() {
        // Remove as unit or construction listener.
        if (this.settlement != null) {
            this.settlement.removeUnitListener(this);
            Iterator<ConstructionSite> i = this.settlement.getConstructionManager()
                    .getConstructionSites().iterator();
            while (i.hasNext()) {
                i.next().removeConstructionListener(this);
            }
        }
        
        // Clear all buffered image caches.
        Iterator<Map<GraphicsNode, BufferedImage>> i = svgImageCache.values().iterator();
        while (i.hasNext()) {
            i.next().clear();
        }
        svgImageCache.clear();
    }
}