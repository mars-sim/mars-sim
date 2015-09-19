/*
 * Copyright (c) 2008-2013, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.matthiasmann.twl;

import de.matthiasmann.twl.DraggableButton.DragListener;
import de.matthiasmann.twl.Event.Type;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.textarea.TextAreaModel;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.FontCache;
import de.matthiasmann.twl.renderer.FontMapper;
import de.matthiasmann.twl.renderer.FontParameter;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.MouseCursor;
import de.matthiasmann.twl.renderer.Renderer;
import de.matthiasmann.twl.textarea.OrderedListType;
import de.matthiasmann.twl.textarea.Style;
import de.matthiasmann.twl.textarea.StyleAttribute;
import de.matthiasmann.twl.textarea.StyleSheet;
import de.matthiasmann.twl.textarea.StyleSheetResolver;
import de.matthiasmann.twl.textarea.TextDecoration;
import de.matthiasmann.twl.textarea.Value;
import de.matthiasmann.twl.theme.StateSelectImage;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.utils.StateExpression;
import de.matthiasmann.twl.utils.StateSelect;
import de.matthiasmann.twl.utils.StringList;
import de.matthiasmann.twl.utils.TextUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Formatted text rendering (HTML/CSS subset). Supports embedded images, bullet point lists,
 * hyper links, multiple fonts, block text, embedded widgets and floating elements.
 *
 * It uses a simplified HTML/CSS model.
 * 
 * @author Matthias Mann
 */
public class TextArea extends Widget {

    public interface WidgetResolver {
        public Widget resolveWidget(String name, String param);
    }

    public interface ImageResolver {
        public Image resolveImage(String name);
    }

    public interface Callback {
        /**
         * Called when a link has been clicked
         * @param href the href of the link
         */
        public void handleLinkClicked(String href);
    }
    
    public interface Callback2 extends Callback {
        /**
         * Called for {@code MOUSE_BTNDOWN}, {@code MOUSE_BTNUP} and {@code MOUSE_CLICKED}
         * events performed over elements.
         * 
         * <p>This callback is fired before {@link Callback#handleLinkClicked(java.lang.String) }</p>
         * 
         * <p>This callback should not modify the model - if this feature is required use
         * {@link GUI#invokeLater(java.lang.Runnable) }.</p>
         * 
         * @param evt the mouse event
         * @param element the element under the mouse
         * @see Event.Type#MOUSE_BTNDOWN
         * @see Event.Type#MOUSE_BTNUP
         * @see Event.Type#MOUSE_CLICKED
         */
        public void handleMouseButton(Event evt, TextAreaModel.Element element);
    }

    public static final StateKey STATE_HOVER = StateKey.get("hover");
    
    static final char[] EMPTY_CHAR_ARRAY = new char[0];
    
    private final HashMap<String, Widget> widgets;
    private final HashMap<String, WidgetResolver> widgetResolvers;
    private final HashMap<String, Image> userImages;
    private final ArrayList<ImageResolver> imageResolvers;

    StyleSheetResolver styleClassResolver;
    private final Runnable modelCB;
    private TextAreaModel model;
    private ParameterMap fonts;
    private ParameterMap images;
    private Font defaultFont;
    private Callback[] callbacks;
    private MouseCursor mouseCursorNormal;
    private MouseCursor mouseCursorLink;
    private DraggableButton.DragListener dragListener;

    private final LClip layoutRoot;
    private final ArrayList<LImage> allBGImages;
    private final RenderInfo renderInfo;
    private boolean inLayoutCode;
    private boolean forceRelayout;
    private Dimension preferredInnerSize;
    private FontMapper fontMapper;
    private FontMapperCacheEntry[] fontMapperCache;

    private int lastMouseX;
    private int lastMouseY;
    private boolean lastMouseInside;
    private boolean dragging;
    private int dragStartX;
    private int dragStartY;
    private LElement curLElementUnderMouse;

    public TextArea() {
        this.widgets = new HashMap<String, Widget>();
        this.widgetResolvers = new HashMap<String, WidgetResolver>();
        this.userImages = new HashMap<String, Image>();
        this.imageResolvers = new ArrayList<ImageResolver>();
        this.layoutRoot = new LClip(null);
        this.allBGImages = new ArrayList<LImage>();
        this.renderInfo = new RenderInfo(getAnimationState());
        
        this.modelCB = new Runnable() {
            public void run() {
                forceRelayout();
            }
        };
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public TextArea(TextAreaModel model) {
        this();
        setModel(model);
    }

    public TextAreaModel getModel() {
        return model;
    }

    public void setModel(TextAreaModel model) {
        if(this.model != null) {
            this.model.removeCallback(modelCB);
        }
        this.model = model;
        if(model != null) {
            model.addCallback(modelCB);
        }
        forceRelayout();
    }

    public void registerWidget(String name, Widget widget) {
        if(name == null) {
            throw new NullPointerException("name");
        }
        if(widget.getParent() != null) {
            throw new IllegalArgumentException("Widget must not have a parent");
        }
        if(widgets.containsKey(name) || widgetResolvers.containsKey(name)) {
            throw new IllegalArgumentException("widget name already in registered");
        }
        if(widgets.containsValue(widget)) {
            throw new IllegalArgumentException("widget already registered");
        }
        widgets.put(name, widget);
    }

    public void registerWidgetResolver(String name, WidgetResolver resolver) {
        if(name == null) {
            throw new NullPointerException("name");
        }
        if(resolver == null) {
            throw new NullPointerException("resolver");
        }
        if(widgets.containsKey(name) || widgetResolvers.containsKey(name)) {
            throw new IllegalArgumentException("widget name already in registered");
        }
        widgetResolvers.put(name, resolver);
    }

    public void unregisterWidgetResolver(String name) {
        if(name == null) {
            throw new NullPointerException("name");
        }
        widgetResolvers.remove(name);
    }

    public void unregisterWidget(String name) {
        if(name == null) {
            throw new NullPointerException("name");
        }
        Widget w = widgets.get(name);
        if(w != null) {
            int idx = getChildIndex(w);
            if(idx >= 0) {
                super.removeChild(idx);
                forceRelayout();
            }
        }
    }

    public void unregisterAllWidgets() {
        widgets.clear();
        super.removeAllChildren();
        forceRelayout();
    }

    public void registerImage(String name, Image image) {
        if(name == null) {
            throw new NullPointerException("name");
        }
        userImages.put(name, image);
    }

    public void registerImageResolver(ImageResolver resolver) {
        if(resolver == null) {
            throw new NullPointerException("resolver");
        }
        if(!imageResolvers.contains(resolver)) {
            imageResolvers.add(resolver);
        }
    }

    public void unregisterImage(String name) {
        userImages.remove(name);
    }

    public void unregisterImageResolver(ImageResolver imageResolver) {
        imageResolvers.remove(imageResolver);
    }

    public void addCallback(Callback cb) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, cb, Callback.class);
    }

    public void removeCallback(Callback cb) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, cb);
    }

    public DragListener getDragListener() {
        return dragListener;
    }

    public void setDragListener(DragListener dragListener) {
        this.dragListener = dragListener;
    }

    public StyleSheetResolver getStyleClassResolver() {
        return styleClassResolver;
    }

    public void setStyleClassResolver(StyleSheetResolver styleClassResolver) {
        this.styleClassResolver = styleClassResolver;
        forceRelayout();
    }

    /**
     * Sets a default style sheet with the following content:
     * <pre>p, ul {
     *    margin-bottom: 1em
     *}
     *pre {
     *    white-space: pre
     *}</pre>
     */
    public void setDefaultStyleSheet() {
        try {
            StyleSheet styleSheet = new StyleSheet();
            styleSheet.parse("p,ul{margin-bottom:1em}");
            setStyleClassResolver(styleSheet);
        } catch(IOException ex) {
            Logger.getLogger(TextArea.class.getName()).log(Level.SEVERE,
                    "Can't create default style sheet", ex);
        }
    }

    public Rect getElementRect(TextAreaModel.Element element) {
        int[] offset = new int[2];
        LElement le = layoutRoot.find(element, offset);
        if(le != null) {
            return new Rect(le.x + offset[0], le.y + offset[1], le.width, le.height);
        } else {
            return null;
        }
    }
    
    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeTextArea(themeInfo);
    }

    protected void applyThemeTextArea(ThemeInfo themeInfo) {
        fonts = themeInfo.getParameterMap("fonts");
        images = themeInfo.getParameterMap("images");
        defaultFont = themeInfo.getFont("font");
        mouseCursorNormal = themeInfo.getMouseCursor("mouseCursor");
        mouseCursorLink = themeInfo.getMouseCursor("mouseCursor.link");
        forceRelayout();
    }

    @Override
    protected void afterAddToGUI(GUI gui) {
        super.afterAddToGUI(gui);
        renderInfo.asNormal.setGUI(gui);
        renderInfo.asHover.setGUI(gui);
    }

    @Override
    public void insertChild(Widget child, int index) {
        throw new UnsupportedOperationException("use registerWidget");
    }

    @Override
    public void removeAllChildren() {
        throw new UnsupportedOperationException("use registerWidget");
    }

    @Override
    public Widget removeChild(int index) {
        throw new UnsupportedOperationException("use registerWidget");
    }

    private void computePreferredInnerSize() {
        int prefWidth = -1;
        int prefHeight = -1;
        
        if(model == null) {
            prefWidth = 0;
            prefHeight = 0;
            
        } else if(getMaxWidth() > 0) {
            final int borderHorizontal = getBorderHorizontal();
            final int maxWidth = Math.max(0, getMaxWidth() - borderHorizontal);
            final int minWidth = Math.max(0, getMinWidth() - borderHorizontal);
            
            if(minWidth < maxWidth) {
                //System.out.println("Doing preferred size computation");
                
                LClip tmpRoot = new LClip(null);
                startLayout();
                try {
                    tmpRoot.width = maxWidth;
                    Box box = new Box(tmpRoot, 0, 0, 0, false);
                    layoutElements(box, model);
                    box.finish();

                    prefWidth = Math.max(0, maxWidth - box.minRemainingWidth);
                    prefHeight = box.curY;
                } finally {
                    endLayout();
                }
            }
        }
        preferredInnerSize = new Dimension(prefWidth, prefHeight);
    }
    
    @Override
    public int getPreferredInnerWidth() {
        if(preferredInnerSize == null) {
            computePreferredInnerSize();
        }
        if(preferredInnerSize.getX() >= 0) {
            return preferredInnerSize.getX();
        }
        return getInnerWidth();
    }

    @Override
    public int getPreferredInnerHeight() {
        if(getInnerWidth() == 0) {
            if(preferredInnerSize == null) {
                computePreferredInnerSize();
            }
            if(preferredInnerSize.getY() >= 0) {
                return preferredInnerSize.getY();
            }
        }
        validateLayout();
        return layoutRoot.height;
    }
    
    @Override
    public int getPreferredWidth() {
        int maxWidth = getMaxWidth();
        return computeSize(getMinWidth(), super.getPreferredWidth(), maxWidth);
    }

    @Override
    public void setMaxSize(int width, int height) {
        if(width != getMaxWidth()) {
            preferredInnerSize = null;
            invalidateLayout();
        }
        super.setMaxSize(width, height);
    }

    @Override
    public void setMinSize(int width, int height) {
        if(width != getMinWidth()) {
            preferredInnerSize = null;
            invalidateLayout();
        }
        super.setMinSize(width, height);
    }
    
    @Override
    protected void layout() {
        int targetWidth = getInnerWidth();

        //System.out.println(this+" minWidth="+getMinWidth()+" width="+getWidth()+" maxWidth="+getMaxWidth()+" targetWidth="+targetWidth+" preferredInnerSize="+preferredInnerSize);
        
        // only recompute the layout when it has changed
        if(layoutRoot.width != targetWidth || forceRelayout) {
            layoutRoot.width = targetWidth;
            inLayoutCode = true;
            forceRelayout = false;
            int requiredHeight;
            
            startLayout();
            try {
                clearLayout();
                Box box = new Box(layoutRoot, 0, 0, 0, true);
                if(model != null) {
                    layoutElements(box, model);

                    box.finish();

                    // set position & size of all widget elements
                    layoutRoot.adjustWidget(getInnerX(), getInnerY());
                    layoutRoot.collectBGImages(0, 0, allBGImages);
                }
                updateMouseHover();
                requiredHeight = box.curY;
            } finally {
                inLayoutCode = false;
                endLayout();
            }

            //System.out.println("layoutRoot.height="+layoutRoot.height+"  box.curY="+box.curY+"  remaining="+box.minRemainingWidth);
            
            if(layoutRoot.height != requiredHeight) {
                layoutRoot.height = requiredHeight;
                if(getInnerHeight() != requiredHeight) {
                    // call outside of inLayoutCode range
                    invalidateLayout();
                }
            }
        }
    }

    @Override
    protected void paintWidget(GUI gui) {
        final ArrayList<LImage> bi = allBGImages;
        final RenderInfo ri = renderInfo;
        ri.offsetX = getInnerX();
        ri.offsetY = getInnerY();
        ri.renderer = gui.getRenderer();

        for(int i=0,n=bi.size() ; i<n ; i++) {
            bi.get(i).draw(ri);
        }

        layoutRoot.draw(ri);
    }

    @Override
    protected void sizeChanged() {
        if(!inLayoutCode) {
            invalidateLayout();
        }
    }

    @Override
    protected void childAdded(Widget child) {
        // always ignore
    }

    @Override
    protected void childRemoved(Widget exChild) {
        // always ignore
    }

    @Override
    protected void allChildrenRemoved() {
        // always ignore
    }

    @Override
    public void destroy() {
        super.destroy();
        clearLayout();
        forceRelayout();
    }

    @Override
    protected boolean handleEvent(Event evt) {
        if(super.handleEvent(evt)) {
            return true;
        }

        if(evt.isMouseEvent()) {
            final Type eventType = evt.getType();

            if(dragging) {
                if(eventType == Event.Type.MOUSE_DRAGGED) {
                    if(dragListener != null) {
                        dragListener.dragged(evt.getMouseX()-dragStartX, evt.getMouseY()-dragStartY);
                    }
                }
                if(evt.isMouseDragEnd()) {
                    if(dragListener != null) {
                        dragListener.dragStopped();
                    }
                    dragging = false;
                    updateMouseHover(evt);
                }
                return true;
            }

            updateMouseHover(evt);

            if(eventType == Event.Type.MOUSE_WHEEL) {
                return false;
            }

            if(eventType == Event.Type.MOUSE_BTNDOWN) {
                dragStartX = evt.getMouseX();
                dragStartY = evt.getMouseY();
            }

            if(eventType == Event.Type.MOUSE_DRAGGED) {
                assert !dragging;
                dragging = true;
                if(dragListener != null) {
                    dragListener.dragStarted();
                }
                return true;
            }
            
            if(curLElementUnderMouse != null && (
                    eventType == Event.Type.MOUSE_CLICKED ||
                    eventType == Event.Type.MOUSE_BTNDOWN ||
                    eventType == Event.Type.MOUSE_BTNUP)) {
                TextAreaModel.Element e = curLElementUnderMouse.element;
                if(callbacks != null) {
                    for(Callback l : callbacks) {
                        if(l instanceof Callback2) {
                            ((Callback2)l).handleMouseButton(evt, e);
                        }
                    }
                }
            }
            
            if(eventType == Event.Type.MOUSE_CLICKED) {
                if(curLElementUnderMouse != null && curLElementUnderMouse.href != null) {
                    String href = curLElementUnderMouse.href;
                    if(callbacks != null) {
                        for(Callback l : callbacks) {
                            l.handleLinkClicked(href);
                        }
                    }
                }
            }

            return true;
        }

        return false;
    }

    @Override
    protected Object getTooltipContentAt(int mouseX, int mouseY) {
        if(curLElementUnderMouse != null) {
            if(curLElementUnderMouse.element instanceof TextAreaModel.ImageElement) {
                return ((TextAreaModel.ImageElement)curLElementUnderMouse.element).getToolTip();
            }
        }
        return super.getTooltipContentAt(mouseX, mouseY);
    }

    private void updateMouseHover(Event evt) {
        lastMouseInside = isMouseInside(evt);
        lastMouseX = evt.getMouseX();
        lastMouseY = evt.getMouseY();
        updateMouseHover();
    }
    
    private void updateMouseHover() {
        LElement le = null;
        if(lastMouseInside) {
            le = layoutRoot.find(lastMouseX - getInnerX(), lastMouseY - getInnerY());
        }
        if(curLElementUnderMouse != le) {
            curLElementUnderMouse = le;
            layoutRoot.setHover(le);
            renderInfo.asNormal.resetAnimationTime(STATE_HOVER);
            renderInfo.asHover.resetAnimationTime(STATE_HOVER);
            updateTooltip();
        }
        
        if(le != null && le.href != null) {
            setMouseCursor(mouseCursorLink);
        } else {
            setMouseCursor(mouseCursorNormal);
        }
        
        getAnimationState().setAnimationState(STATE_HOVER, lastMouseInside);
    }
    
    void forceRelayout() {
        forceRelayout = true;
        preferredInnerSize = null;
        invalidateLayout();
    }
    
    private void clearLayout() {
        layoutRoot.destroy();
        allBGImages.clear();
        super.removeAllChildren();
    }
    
    private void startLayout() {
        if(styleClassResolver != null) {
            styleClassResolver.startLayout();
        }
        
        GUI gui = getGUI();
        fontMapper = (gui != null) ? gui.getRenderer().getFontMapper() : null;
        fontMapperCache = null;
    }
    
    private void endLayout() {
        if(styleClassResolver != null) {
            styleClassResolver.layoutFinished();
        }
        fontMapper = null;
        fontMapperCache = null;
    }

    private void layoutElements(Box box, Iterable<TextAreaModel.Element> elements) {
        for(TextAreaModel.Element e : elements) {
            layoutElement(box, e);
        }
    }

    private void layoutElement(Box box, TextAreaModel.Element e) {
        box.clearFloater(e.getStyle().get(StyleAttribute.CLEAR, styleClassResolver));

        if(e instanceof TextAreaModel.TextElement) {
            layoutTextElement(box, (TextAreaModel.TextElement)e);
        } else if(e instanceof TextAreaModel.LineBreakElement) {
            box.nextLine(true);
        } else {
            if(box.wasPreformatted) {
                box.nextLine(false);
                box.wasPreformatted = false;
            }
            if(e instanceof TextAreaModel.ParagraphElement) {
                layoutParagraphElement(box, (TextAreaModel.ParagraphElement)e);
            } else if(e instanceof TextAreaModel.ImageElement) {
                layoutImageElement(box, (TextAreaModel.ImageElement)e);
            } else if(e instanceof TextAreaModel.WidgetElement) {
                layoutWidgetElement(box, (TextAreaModel.WidgetElement)e);
            } else if(e instanceof TextAreaModel.ListElement) {
                layoutListElement(box, (TextAreaModel.ListElement)e);
            } else if(e instanceof TextAreaModel.OrderedListElement) {
                layoutOrderedListElement(box, (TextAreaModel.OrderedListElement)e);
            } else if(e instanceof TextAreaModel.BlockElement) {
                layoutBlockElement(box, (TextAreaModel.BlockElement)e);
            } else if(e instanceof TextAreaModel.TableElement) {
                layoutTableElement(box, (TextAreaModel.TableElement)e);
            } else if(e instanceof TextAreaModel.LinkElement) {
                layoutLinkElement(box, (TextAreaModel.LinkElement)e);
            } else if(e instanceof TextAreaModel.ContainerElement) {
                layoutContainerElement(box, (TextAreaModel.ContainerElement)e);
            } else {
                Logger.getLogger(TextArea.class.getName()).log(Level.SEVERE, "Unknown Element subclass: {0}", e.getClass());
            }
        }
    }
    
    private void layoutImageElement(Box box, TextAreaModel.ImageElement ie) {
        Image image = selectImage(ie.getImageName());
        if(image == null) {
            return;
        }

        LImage li = new LImage(ie, image);
        li.href = box.href;
        layout(box, ie, li);
    }

    private void layoutWidgetElement(Box box, TextAreaModel.WidgetElement we) {
        Widget widget = widgets.get(we.getWidgetName());
        if(widget == null) {
            WidgetResolver resolver = widgetResolvers.get(we.getWidgetName());
            if(resolver != null) {
                widget = resolver.resolveWidget(we.getWidgetName(), we.getWidgetParam());
            }
            if(widget == null) {
                return;
            }
        }

        if(widget.getParent() != null) {
            Logger.getLogger(TextArea.class.getName()).log(Level.SEVERE, "Widget already added: {0}", widget);
            return;
        }

        super.insertChild(widget, getNumChildren());
        widget.adjustSize();
        
        LWidget lw = new LWidget(we, widget);
        lw.width = widget.getWidth();
        lw.height = widget.getHeight();

        layout(box, we, lw);
    }

    private void layout(Box box, TextAreaModel.Element e, LElement le) {
        Style style = e.getStyle();

        TextAreaModel.FloatPosition floatPosition = style.get(StyleAttribute.FLOAT_POSITION, styleClassResolver);
        TextAreaModel.Display display = style.get(StyleAttribute.DISPLAY, styleClassResolver);

        le.marginTop = (short)convertToPX0(style, StyleAttribute.MARGIN_TOP, box.boxWidth);
        le.marginLeft = (short)convertToPX0(style, StyleAttribute.MARGIN_LEFT, box.boxWidth);
        le.marginRight = (short)convertToPX0(style, StyleAttribute.MARGIN_RIGHT, box.boxWidth);
        le.marginBottom = (short)convertToPX0(style, StyleAttribute.MARGIN_BOTTOM, box.boxWidth);

        int autoHeight = le.height;
        int width = convertToPX(style, StyleAttribute.WIDTH, box.boxWidth, le.width);
        if(width > 0) {
            if(le.width > 0) {
                autoHeight = width * le.height / le.width;
            }
            le.width = width;
        }
        
        int height = convertToPX(style, StyleAttribute.HEIGHT, le.height, autoHeight);
        if(height > 0) {
            le.height = height;
        }

        layout(box, e, le, floatPosition, display);
    }
    
    private void layout(Box box, TextAreaModel.Element e, LElement le, TextAreaModel.FloatPosition floatPos, TextAreaModel.Display display) {
        boolean leftRight = (floatPos != TextAreaModel.FloatPosition.NONE);

        if(leftRight || display != TextAreaModel.Display.INLINE) {
            box.nextLine(false);
            if(!leftRight) {
                box.curY = box.computeTopPadding(le.marginTop);
                box.checkFloaters();
            }
        }

        box.advancePastFloaters(le.width, le.marginLeft, le.marginRight);
        if(le.width > box.lineWidth) {
            le.width = box.lineWidth;
        }

        if(leftRight) {
            if(floatPos == TextAreaModel.FloatPosition.RIGHT) {
                le.x = box.computeRightPadding(le.marginRight) - le.width;
                box.objRight.add(le);
            } else {
                le.x = box.computeLeftPadding(le.marginLeft);
                box.objLeft.add(le);
            }
        } else if(display == TextAreaModel.Display.INLINE) {
            if(box.getRemaining() < le.width && !box.isAtStartOfLine()) {
                box.nextLine(false);
            }
            le.x = box.getXAndAdvance(le.width);
        } else {
            switch(e.getStyle().get(StyleAttribute.HORIZONTAL_ALIGNMENT, styleClassResolver)) {
            case CENTER:
            case JUSTIFY:
                le.x = box.lineStartX + (box.lineWidth - le.width) / 2;
                break;

            case RIGHT:
                le.x = box.computeRightPadding(le.marginRight) - le.width;
                break;

            default:
                le.x = box.computeLeftPadding(le.marginLeft);
            }
        }

        box.layout.add(le);

        if(leftRight) {
            assert box.lineStartIdx == box.layout.size() - 1;
            box.lineStartIdx++;
            le.y = box.computeTopPadding(le.marginTop);
            box.computePadding();
        } else if(display != TextAreaModel.Display.INLINE) {
            box.accountMinRemaining(Math.max(0, box.lineWidth - le.width));
            box.nextLine(false);
        }
    }
    
    private static final int DEFAULT_FONT_SIZE = 14;

    int convertToPX(Style style, StyleAttribute<Value> attribute, int full, int auto) {
        style = style.resolve(attribute, styleClassResolver);
        Value valueUnit = style.getNoResolve(attribute, styleClassResolver);
        
        Font font = null;
        if(valueUnit.unit.isFontBased()) {
            if(attribute == StyleAttribute.FONT_SIZE) {
                style = style.getParent();
                if(style == null) {
                    return DEFAULT_FONT_SIZE;
                }
            }
            font = selectFont(style);
            if(font == null) {
                return 0;
            }
        }
        
        float value = valueUnit.value;
        switch(valueUnit.unit) {
            case EM:
                value *= font.getEM();
                break;
            case EX:
                value *= font.getEX();
                break;
            case PERCENT:
                value *= full * 0.01f;
                break;
            case PT:
                value *= 1.33f; // 96 DPI
                break;
            case AUTO:
                return auto;
        }
        if(value >= Short.MAX_VALUE) {
            return Short.MAX_VALUE;
        }
        if(value <= Short.MIN_VALUE) {
            return Short.MIN_VALUE;
        }
        return Math.round(value);
    }

    int convertToPX0(Style style, StyleAttribute<Value> attribute, int full) {
        return Math.max(0, convertToPX(style, attribute, full, 0));
    }

    private Font selectFont(Style style) {
        StringList fontFamilies = style.get(StyleAttribute.FONT_FAMILIES, styleClassResolver);
        if(fontFamilies != null) {
            if(fontMapper != null) {
                Font font = selectFontMapper(style, fontMapper, fontFamilies);
                if(font != null) {
                    return font;
                }
            }

            if(fonts != null) {
                do {
                    Font font = fonts.getFont(fontFamilies.getValue());
                    if(font != null) {
                        return font;
                    }
                } while((fontFamilies=fontFamilies.getNext()) != null);
            }
        }
        return defaultFont;
    }
    
    private static final StateSelect HOVER_STATESELECT =
            new StateSelect(new StateExpression.Check(STATE_HOVER));
    
    private static final int FONT_MAPPER_CACHE_SIZE = 16;
    
    private static final class FontMapperCacheEntry {
        final int fontSize;
        final int fontStyle;
        final StringList fontFamilies;
        final TextDecoration tdNormal;
        final TextDecoration tdHover;
        final int hashCode;
        final Font font;
        FontMapperCacheEntry next;

        FontMapperCacheEntry(int fontSize, int fontStyle, StringList fontFamilies, TextDecoration tdNormal, TextDecoration tdHover, int hashCode, Font font) {
            this.fontSize = fontSize;
            this.fontStyle = fontStyle;
            this.fontFamilies = fontFamilies;
            this.tdNormal = tdNormal;
            this.tdHover = tdHover;
            this.hashCode = hashCode;
            this.font = font;
        }
    }
    
    private Font selectFontMapper(Style style, FontMapper fontMapper, StringList fontFamilies) {
        int fontSize = convertToPX(style, StyleAttribute.FONT_SIZE, DEFAULT_FONT_SIZE, DEFAULT_FONT_SIZE);
        int fontStyle = 0;
        if(style.get(StyleAttribute.FONT_WEIGHT, styleClassResolver) >= 550) {
            fontStyle |= FontMapper.STYLE_BOLD;
        }
        if(style.get(StyleAttribute.FONT_ITALIC, styleClassResolver)) {
            fontStyle |= FontMapper.STYLE_ITALIC;
        }
        
        TextDecoration textDecoration = style.get(StyleAttribute.TEXT_DECORATION, styleClassResolver);
        TextDecoration textDecorationHover = style.get(StyleAttribute.TEXT_DECORATION_HOVER, styleClassResolver);
        
        int hashCode = fontSize;
        hashCode = hashCode * 67 + fontStyle;
        hashCode = hashCode * 67 + fontFamilies.hashCode();
        hashCode = hashCode * 67 + textDecoration.hashCode();
        hashCode = hashCode * 67 + ((textDecorationHover != null) ? textDecorationHover.hashCode() : 0);
        
        int cacheIdx = hashCode & (FONT_MAPPER_CACHE_SIZE-1);
        
        if(fontMapperCache != null) {
            for(FontMapperCacheEntry ce=fontMapperCache[cacheIdx] ; ce!=null ; ce=ce.next) {
                if(ce.hashCode == hashCode &&
                        ce.fontSize == fontSize &&
                        ce.fontStyle == fontStyle &&
                        ce.tdNormal == textDecoration &&
                        ce.tdHover == textDecorationHover &&
                        ce.fontFamilies.equals(fontFamilies)) {
                    return ce.font;
                }
            }
        } else {
            fontMapperCache = new FontMapperCacheEntry[FONT_MAPPER_CACHE_SIZE];
        }
        
        FontParameter fpNormal = createFontParameter(textDecoration);
        
        StateSelect select;
        FontParameter[] params;
        
        if(textDecorationHover != null) {
            FontParameter fpHover = createFontParameter(textDecorationHover);

            select = HOVER_STATESELECT;
            params = new FontParameter[] { fpHover, fpNormal };
        } else {
            select = StateSelect.EMPTY;
            params = new FontParameter[] { fpNormal };
        }
        
        Font font = fontMapper.getFont(fontFamilies, fontSize, fontStyle, select, params);
        
        FontMapperCacheEntry ce = new FontMapperCacheEntry(fontSize, fontStyle,
                fontFamilies, textDecoration, textDecorationHover, hashCode, font);
        ce.next = fontMapperCache[cacheIdx];
        fontMapperCache[cacheIdx] = ce;
        
        return font;
    }
    
    private static FontParameter createFontParameter(TextDecoration deco) {
        FontParameter fp = new FontParameter();
        fp.put(FontParameter.UNDERLINE, deco == TextDecoration.UNDERLINE);
        fp.put(FontParameter.LINETHROUGH, deco == TextDecoration.LINE_THROUGH);
        return fp;
    }
    
    private FontData createFontData(Style style) {
        Font font = selectFont(style);
        if(font == null) {
            return null;
        }
        
        return new FontData(font,
                style.get(StyleAttribute.COLOR, styleClassResolver),
                style.get(StyleAttribute.COLOR_HOVER, styleClassResolver));
    }

    private Image selectImage(Style style, StyleAttribute<String> element) {
        String imageName = style.get(element, styleClassResolver);
        if(imageName != null) {
            return selectImage(imageName);
        } else {
            return null;
        }
    }

    private Image selectImage(String name) {
        Image image = userImages.get(name);
        if(image != null) {
            return image;
        }
        for(int i=0 ; i<imageResolvers.size() ; i++) {
            image = imageResolvers.get(i).resolveImage(name);
            if(image != null) {
                return image;
            }
        }
        if(images != null) {
            return images.getImage(name);
        }
        return null;
    }

    private void layoutParagraphElement(Box box, TextAreaModel.ParagraphElement pe) {
        final Style style = pe.getStyle();
        final Font font = selectFont(style);

        doMarginTop(box, style);
        LElement anchor = box.addAnchor(pe);
        box.setupTextParams(style, font, true);
        
        layoutElements(box, pe);

        if(box.textAlignment == TextAreaModel.HAlignment.JUSTIFY) {
            box.textAlignment = TextAreaModel.HAlignment.LEFT;
        }
        box.nextLine(false);
        box.inParagraph = false;

        anchor.height = box.curY - anchor.y;
        doMarginBottom(box, style);
    }
    
    private void layoutTextElement(Box box, TextAreaModel.TextElement te) {
        final String text = te.getText();
        final Style style = te.getStyle();
        final FontData fontData = createFontData(style);
        final boolean pre = style.get(StyleAttribute.PREFORMATTED, styleClassResolver);

        if(fontData == null) {
            return;
        }

        final boolean inheritHover;
        Boolean inheritHoverStyle = style.resolve(StyleAttribute.INHERIT_HOVER, styleClassResolver).getRaw(StyleAttribute.INHERIT_HOVER);
        if(inheritHoverStyle != null) {
            inheritHover = inheritHoverStyle.booleanValue();
        } else {
            inheritHover = (box.style != null) && (box.style == style.getParent());
        }
        
        box.setupTextParams(style, fontData.font, false);

        if(pre && !box.wasPreformatted) {
            box.nextLine(false);
        }
        
        if(pre) {
            int idx = 0;
            while(idx < text.length()) {
                int end = TextUtil.indexOf(text, '\n', idx);
                layoutTextPre(box, te, fontData, text, idx, end, inheritHover);
                if(end < text.length() && text.charAt(end) == '\n') {
                    end++;
                    box.nextLine(true);
                }
                idx = end;
            }
        } else {
            layoutText(box, te, fontData, text, 0, text.length(), inheritHover);
        }

        box.wasPreformatted = pre;
    }

    private void layoutText(Box box, TextAreaModel.TextElement te, FontData fontData,
            String text, int textStart, int textEnd, boolean inheritHover) {
        int idx = textStart;
        // trim start
        while(textStart < textEnd && isSkip(text.charAt(textStart))) {
            textStart++;
        }
        // trim end
        boolean endsWithSpace = false;
        while(textEnd > textStart && isSkip(text.charAt(textEnd-1))) {
            endsWithSpace = true;
            textEnd--;
        }

        Font font = fontData.font;
        
        // check if we skipped white spaces and the previous element in this
        // row was not a text cell
        if(textStart > idx && box.prevOnLineEndsNotWithSpace()) {
            box.curX += font.getSpaceWidth();
        }

        Boolean breakWord = null;    // lazy lookup

        idx = textStart;
        while(idx < textEnd) {
            assert !isSkip(text.charAt(idx));

            int end = idx;
            int visibleEnd = idx;
            if(box.textAlignment != TextAreaModel.HAlignment.JUSTIFY) {
                end = idx + font.computeVisibleGlpyhs(text, idx, textEnd, box.getRemaining());
                visibleEnd = end;

                if(end < textEnd) {
                    // if we are at a punctuation then walk backwards until we hit
                    // the word or a break. This ensures that the punctuation stays
                    // at the end of a word
                    while(end > idx && isPunctuation(text.charAt(end))) {
                        end--;
                    }

                    // if we are not at the end of this text element
                    // and the next character is not a space
                    if(!isBreak(text.charAt(end))) {
                        // then we walk backwards until we find spaces
                        // this prevents the line ending in the middle of a word
                        while(end > idx && !isBreak(text.charAt(end-1))) {
                            end--;
                        }
                    }
                }

                // now walks backwards until we hit the end of the previous word
                while(end > idx && isSkip(text.charAt(end-1))) {
                    end--;
                }
            }

            boolean advancePastFloaters = false;

            // if we found no word that fits
            if(end == idx) {
                // we may need a new line
                if(box.textAlignment != TextAreaModel.HAlignment.JUSTIFY && box.nextLine(false)) {
                    continue;
                }
                if(breakWord == null) {
                    breakWord = te.getStyle().get(StyleAttribute.BREAKWORD, styleClassResolver);
                }
                if(breakWord) {
                    if(visibleEnd == idx) {
                        end = idx + 1;  // ensure progress
                    } else {
                        end = visibleEnd;
                    }
                } else {
                    // or we already are at the start of a line
                    // just put the word there even if it doesn't fit
                    while(end < textEnd && !isBreak(text.charAt(end))) {
                        end++;
                    }
                    // some characters need to stay at the end of a word
                    while(end < textEnd && isPunctuation(text.charAt(end))) {
                        end++;
                    }
                }
                advancePastFloaters = true;
            }

            if(idx < end) {
                LText lt = new LText(te, fontData, text, idx, end, box.doCacheText);
                if(advancePastFloaters) {
                    box.advancePastFloaters(lt.width, box.marginLeft, box.marginRight);
                }
                if(box.textAlignment == TextAreaModel.HAlignment.JUSTIFY && box.getRemaining() < lt.width) {
                    box.nextLine(false);
                }

                int width = lt.width;
                if(end < textEnd && isSkip(text.charAt(end))) {
                    width += font.getSpaceWidth();
                }

                lt.x = box.getXAndAdvance(width);
                lt.marginTop = (short)box.marginTop;
                lt.href = box.href;
                lt.inheritHover = inheritHover;
                box.layout.add(lt);
            }

            // find the start of the next word
            idx = end;
            while(idx < textEnd && isSkip(text.charAt(idx))) {
                idx++;
            }
        }

        if(!box.isAtStartOfLine() && endsWithSpace) {
            box.curX += font.getSpaceWidth();
        }
    }

    private void layoutTextPre(Box box, TextAreaModel.TextElement te, FontData fontData,
            String text, int textStart, int textEnd, boolean inheritHover) {
        Font font = fontData.font;
        int idx = textStart;
        for(;;) {
            while(idx < textEnd) {
                if(text.charAt(idx) == '\t') {
                    idx++;
                    int tabX = box.computeNextTabStop(te.getStyle(), font);
                    if(tabX < box.lineWidth) {
                        box.curX = tabX;
                    } else if(!box.isAtStartOfLine()) {
                        break;
                    }
                }

                int tabIdx = text.indexOf('\t', idx);
                int end = textEnd;
                if(tabIdx >= 0 && tabIdx < textEnd) {
                    end = tabIdx;
                }

                if(end > idx) {
                    int count = font.computeVisibleGlpyhs(text, idx, end, box.getRemaining());
                    if(count == 0 && !box.isAtStartOfLine()) {
                        break;
                    }

                    end = idx + Math.max(1, count);

                    LText lt = new LText(te, fontData, text, idx, end, box.doCacheText);
                    lt.x = box.getXAndAdvance(lt.width);
                    lt.marginTop = (short)box.marginTop;
                    lt.inheritHover = inheritHover;
                    box.layout.add(lt);
                }

                idx = end;
            }

            if(idx >= textEnd) {
                break;
            }

            box.nextLine(false);
        }
    }

    private void doMarginTop(Box box, Style style) {
        int marginTop = convertToPX0(style, StyleAttribute.MARGIN_TOP, box.boxWidth);
        box.nextLine(false);    // need to complete line before computing targetY
        box.advanceToY(box.computeTopPadding(marginTop));
    }

    private void doMarginBottom(Box box, Style style) {
        int marginBottom = convertToPX0(style, StyleAttribute.MARGIN_BOTTOM, box.boxWidth);
        box.setMarginBottom(marginBottom);
    }

    private void layoutContainerElement(Box box, TextAreaModel.ContainerElement ce) {
        Style style = ce.getStyle();
        doMarginTop(box, style);
        box.addAnchor(ce);
        layoutElements(box, ce);
        doMarginBottom(box, style);
    }

    private void layoutLinkElement(Box box, TextAreaModel.LinkElement le) {
        String oldHref = box.href;
        box.href = le.getHREF();
        
        Style style = le.getStyle();
        TextAreaModel.Display display = style.get(StyleAttribute.DISPLAY, styleClassResolver);
        if(display == TextAreaModel.Display.BLOCK) {
            layoutBlockElement(box, le);
        } else {
            layoutContainerElement(box, le);
        }
        
        box.href = oldHref;
    }
    
    private void layoutListElement(Box box, TextAreaModel.ListElement le) {
        Style style = le.getStyle();

        doMarginTop(box, style);

        Image image = selectImage(style, StyleAttribute.LIST_STYLE_IMAGE);
        if(image != null) {
            LImage li = new LImage(le, image);
            li.marginRight = (short)convertToPX0(style, StyleAttribute.PADDING_LEFT, box.boxWidth);
            layout(box, le, li, TextAreaModel.FloatPosition.LEFT, TextAreaModel.Display.BLOCK);
            
            int imageHeight = li.height;
            li.height = Short.MAX_VALUE;

            layoutElements(box, le);

            li.height = imageHeight;

            box.objLeft.remove(li);
            box.advanceToY(li.bottom());
            box.computePadding();
        } else {
            layoutElements(box, le);
            box.nextLine(false);
        }

        doMarginBottom(box, style);
    }

    private void layoutOrderedListElement(Box box, TextAreaModel.OrderedListElement ole) {
        Style style = ole.getStyle();
        FontData fontData = createFontData(style);

        if(fontData == null) {
            return;
        }
        
        doMarginTop(box, style);
        LElement anchor = box.addAnchor(ole);

        int start = Math.max(1, ole.getStart());
        int count = ole.getNumElements();
        OrderedListType type = style.get(StyleAttribute.LIST_STYLE_TYPE, styleClassResolver);

        String[] labels = new String[count];
        int maxLabelWidth = convertToPX0(style, StyleAttribute.PADDING_LEFT, box.boxWidth);
        for(int i=0 ; i<count ; i++) {
            labels[i] = type.format(start + i).concat(". ");
            int width = fontData.font.computeTextWidth(labels[i]);
            maxLabelWidth = Math.max(maxLabelWidth, width);
        }

        for(int i=0 ; i<count ; i++) {
            String label = labels[i];
            TextAreaModel.Element li = ole.getElement(i);
            Style liStyle = li.getStyle();
            doMarginTop(box, liStyle);
            
            LText lt = new LText(ole, fontData, label, 0, label.length(), box.doCacheText);
            int labelWidth = lt.width;
            int labelHeight = lt.height;

            lt.width += convertToPX0(liStyle, StyleAttribute.PADDING_LEFT, box.boxWidth);
            layout(box, ole, lt, TextAreaModel.FloatPosition.LEFT, TextAreaModel.Display.BLOCK);
            lt.x += Math.max(0, maxLabelWidth - labelWidth);
            lt.height = Short.MAX_VALUE;

            layoutElement(box, li);

            lt.height = labelHeight;

            box.objLeft.remove(lt);
            box.advanceToY(lt.bottom());
            box.computePadding();

            doMarginBottom(box, liStyle);
        }

        anchor.height = box.curY - anchor.y;
        doMarginBottom(box, style);
    }

    private Box layoutBox(LClip clip, int continerWidth, int paddingLeft, int paddingRight, TextAreaModel.ContainerElement ce, String href, boolean doCacheText) {
        Style style = ce.getStyle();
        int paddingTop = convertToPX0(style, StyleAttribute.PADDING_TOP, continerWidth);
        int paddingBottom = convertToPX0(style, StyleAttribute.PADDING_BOTTOM, continerWidth);
        int marginBottom = convertToPX0(style, StyleAttribute.MARGIN_BOTTOM, continerWidth);

        Box box = new Box(clip, paddingLeft, paddingRight, paddingTop, doCacheText);
        box.href = href;
        box.style = style;
        layoutElements(box, ce);
        box.finish();
        
        int contentHeight = box.curY + paddingBottom;
        int boxHeight = Math.max(contentHeight, convertToPX(style, StyleAttribute.HEIGHT, contentHeight, contentHeight));
        if(boxHeight > contentHeight) {
            int amount = 0;
            switch(style.get(StyleAttribute.VERTICAL_ALIGNMENT, styleClassResolver)) {
                case BOTTOM:
                    amount = boxHeight - contentHeight;
                    break;
                    
                case FILL:
                case MIDDLE:
                    amount = (boxHeight - contentHeight)/2;
                    break;
            }
            if(amount > 0) {
                clip.moveContentY(amount);
            }
        }
            
        clip.height = boxHeight;
        clip.marginBottom = (short)Math.max(marginBottom, box.marginBottomAbs - box.curY);
        return box;
    }

    private void layoutBlockElement(Box box, TextAreaModel.ContainerElement be) {
        box.nextLine(false);

        final Style style = be.getStyle();
        final TextAreaModel.FloatPosition floatPosition =
                style.get(StyleAttribute.FLOAT_POSITION, styleClassResolver);

        LImage bgImage = createBGImage(box, be);

        int marginTop = convertToPX0(style, StyleAttribute.MARGIN_TOP, box.boxWidth);
        int marginLeft = convertToPX0(style, StyleAttribute.MARGIN_LEFT, box.boxWidth);
        int marginRight = convertToPX0(style, StyleAttribute.MARGIN_RIGHT, box.boxWidth);

        int bgX = box.computeLeftPadding(marginLeft);
        int bgY = box.computeTopPadding(marginTop);
        int bgWidth;

        int remaining = Math.max(0, box.computeRightPadding(marginRight) - bgX);
        int paddingLeft = convertToPX0(style, StyleAttribute.PADDING_LEFT, box.boxWidth);
        int paddingRight = convertToPX0(style, StyleAttribute.PADDING_RIGHT, box.boxWidth);

        if(floatPosition == TextAreaModel.FloatPosition.NONE) {
            bgWidth = convertToPX(style, StyleAttribute.WIDTH, remaining, remaining);
        } else {
            bgWidth = convertToPX(style, StyleAttribute.WIDTH, box.boxWidth, Integer.MIN_VALUE);
            if(bgWidth == Integer.MIN_VALUE) {
                LClip dummy = new LClip(null);
                dummy.width = Math.max(0, box.lineWidth - paddingLeft - paddingRight);

                Box dummyBox = layoutBox(dummy, box.boxWidth, paddingLeft, paddingRight, be, null, false);
                dummyBox.nextLine(false);
                
                bgWidth = Math.max(0, dummy.width - dummyBox.minRemainingWidth);
            }
        }
        
        bgWidth = Math.max(0, bgWidth) + paddingLeft + paddingRight;

        if(floatPosition != TextAreaModel.FloatPosition.NONE) {
            box.advancePastFloaters(bgWidth, marginLeft, marginRight);
            
            bgX = box.computeLeftPadding(marginLeft);
            bgY = Math.max(bgY, box.curY);
            remaining = Math.max(0, box.computeRightPadding(marginRight) - bgX);
        }

        bgWidth = Math.min(bgWidth, remaining);

        if(floatPosition == TextAreaModel.FloatPosition.RIGHT) {
            bgX = box.computeRightPadding(marginRight) - bgWidth;
        }

        LClip clip = new LClip(be);
        clip.x = bgX;
        clip.y = bgY;
        clip.width = bgWidth;
        clip.marginLeft = (short)marginLeft;
        clip.marginRight = (short)marginRight;
        clip.href = box.href;
        box.layout.add(clip);

        Box clipBox = layoutBox(clip, box.boxWidth, paddingLeft, paddingRight, be, box.href, box.doCacheText);

        // sync main box with layout
        box.lineStartIdx = box.layout.size();

        if(floatPosition == TextAreaModel.FloatPosition.NONE) {
            box.advanceToY(bgY + clip.height);
            box.setMarginBottom(clip.marginBottom);
            box.accountMinRemaining(clipBox.minRemainingWidth);
        } else {
            if(floatPosition == TextAreaModel.FloatPosition.RIGHT) {
                box.objRight.add(clip);
            } else {
                box.objLeft.add(clip);
            }
            box.computePadding();
        }
        
        if(bgImage != null) {
            bgImage.x = bgX;
            bgImage.y = bgY;
            bgImage.width = bgWidth;
            bgImage.height = clip.height;
            bgImage.hoverSrc = clip;
        }
    }

    private void computeTableWidth(TextAreaModel.TableElement te,
            int maxTableWidth, int columnWidth[], int columnSpacing[], boolean[] columnsWithFixedWidth) {
        final int numColumns = te.getNumColumns();
        final int numRows = te.getNumRows();
        final int cellSpacing = te.getCellSpacing();
        final int cellPadding = te.getCellPadding();
        
        HashMap<Integer, Integer> colspanWidths = null;
        
        for(int col=0 ; col<numColumns ; col++) {
            int width = 0;
            int marginLeft = 0;
            int marginRight = 0;
            boolean hasFixedWidth = false;
            
            for(int row=0 ; row<numRows ; row++) {
                TextAreaModel.TableCellElement cell = te.getCell(row, col);
                if(cell != null) {
                    Style cellStyle = cell.getStyle();
                    int colspan = cell.getColspan();
                    int cellWidth = convertToPX(cellStyle, StyleAttribute.WIDTH, maxTableWidth, Integer.MIN_VALUE);
                    if(cellWidth == Integer.MIN_VALUE && (colspan > 1 || !hasFixedWidth)) {
                        int paddingLeft = Math.max(cellPadding, convertToPX0(cellStyle, StyleAttribute.PADDING_LEFT, maxTableWidth));
                        int paddingRight = Math.max(cellPadding, convertToPX0(cellStyle, StyleAttribute.PADDING_RIGHT, maxTableWidth));

                        LClip dummy = new LClip(null);
                        dummy.width = maxTableWidth;
                        Box dummyBox = layoutBox(dummy, maxTableWidth, paddingLeft, paddingRight, cell, null, false);
                        dummyBox.finish();

                        cellWidth = maxTableWidth - dummyBox.minRemainingWidth;
                    } else if(colspan == 1 && cellWidth >= 0) {
                        hasFixedWidth = true;
                    }
                    
                    if(colspan > 1) {
                        if(colspanWidths == null) {
                            colspanWidths = new HashMap<Integer, Integer>();
                        }
                        Integer key = (col << 16) + colspan;
                        Integer value = colspanWidths.get(key);
                        if(value == null || cellWidth > value) {
                            colspanWidths.put(key, cellWidth);
                        }
                    } else {
                        width = Math.max(width, cellWidth);
                        marginLeft = Math.max(marginLeft, convertToPX(cellStyle, StyleAttribute.MARGIN_LEFT, maxTableWidth, 0));
                        marginRight = Math.max(marginRight, convertToPX(cellStyle, StyleAttribute.MARGIN_LEFT, maxTableWidth, 0));
                    }
                }
            }
            
            columnsWithFixedWidth[col] = hasFixedWidth;
            columnWidth[col] = width;
            columnSpacing[col  ] = Math.max(columnSpacing[col], marginLeft);
            columnSpacing[col+1] = Math.max(cellSpacing, marginRight);
        }
        
        if(colspanWidths != null) {
            for(Map.Entry<Integer, Integer> e : colspanWidths.entrySet()) {
                int key = e.getKey();
                int col = key >>> 16;
                int colspan = key & 0xFFFF;
                int width = e.getValue();
                int remainingCols = colspan;
                
                for(int i=0 ; i<colspan ; i++) {
                    if(columnsWithFixedWidth[col+i]) {
                        width -= columnWidth[col+i];
                        remainingCols--;
                    }
                }
                
                if(width > 0) {
                    for(int i=0 ; i<colspan && remainingCols > 0 ; i++) {
                        if(!columnsWithFixedWidth[col+i]) {
                            int colWidth = width / remainingCols;
                            columnWidth[col+i] = Math.max(columnWidth[col+i], colWidth);
                            width -= colWidth;
                            remainingCols--;
                        }
                    }
                }
            }
        }
    } 
    
    private void layoutTableElement(Box box, TextAreaModel.TableElement te) {
        final int numColumns = te.getNumColumns();
        final int numRows = te.getNumRows();
        final int cellSpacing = te.getCellSpacing();
        final int cellPadding = te.getCellPadding();
        final Style tableStyle = te.getStyle();

        if(numColumns == 0 || numRows == 0) {
            return;
        }

        doMarginTop(box, tableStyle);
        LElement anchor = box.addAnchor(te);
        
        int left = box.computeLeftPadding(convertToPX0(tableStyle, StyleAttribute.MARGIN_LEFT, box.boxWidth));
        int right = box.computeRightPadding(convertToPX0(tableStyle, StyleAttribute.MARGIN_RIGHT, box.boxWidth));
        int maxTableWidth = Math.max(0, right - left);
        int tableWidth = Math.min(maxTableWidth, convertToPX(tableStyle, StyleAttribute.WIDTH, box.boxWidth, Integer.MIN_VALUE));
        boolean autoTableWidth = tableWidth == Integer.MIN_VALUE;

        if(tableWidth <= 0) {
            tableWidth = maxTableWidth;
        }
        
        int columnWidth[] = new int[numColumns];
        int columnSpacing[] = new int[numColumns + 1];
        boolean[] columnsWithFixedWidth = new boolean[numColumns];
        
        columnSpacing[0] = Math.max(cellSpacing, convertToPX0(tableStyle, StyleAttribute.PADDING_LEFT, box.boxWidth));
        
        computeTableWidth(te, tableWidth, columnWidth, columnSpacing, columnsWithFixedWidth);
        
        columnSpacing[numColumns] = Math.max(columnSpacing[numColumns],
                convertToPX0(tableStyle, StyleAttribute.PADDING_RIGHT, box.boxWidth));

        int columnSpacingSum = 0;
        for(int spacing : columnSpacing) {
            columnSpacingSum += spacing;
        }

        int columnWidthSum = 0;
        for(int width : columnWidth) {
            columnWidthSum += width;
        }
        
        if(autoTableWidth) {
            tableWidth = Math.min(maxTableWidth, columnWidthSum + columnSpacingSum);
        }
        
        int availableColumnWidth = Math.max(0, tableWidth - columnSpacingSum);
        if(availableColumnWidth != columnWidthSum && columnWidthSum > 0) {
            int available = availableColumnWidth;
            int toDistribute = columnWidthSum;
            int remainingCols = numColumns;
            
            for(int col=0 ; col<numColumns ; col++) {
                if(columnsWithFixedWidth[col]) {
                    int width = columnWidth[col];
                    available -= width;
                    toDistribute -= width;
                    remainingCols--;
                }
            }
            
            boolean allColumns = false;
            if(availableColumnWidth < 0) {
                available = availableColumnWidth;
                toDistribute = columnWidthSum;
                remainingCols = numColumns;
                allColumns = true;
            }
            
            for(int col=0 ; col<numColumns && remainingCols > 0 ; col++) {
                if(allColumns || !columnsWithFixedWidth[col]) {
                    int width = columnWidth[col];
                    int newWidth = (toDistribute > 0) ? width * available / toDistribute : 0;
                    columnWidth[col] = newWidth;
                    available -= newWidth;
                    toDistribute -= width;
                }
            }
        }
        
        LImage tableBGImage = createBGImage(box, te);

        box.textAlignment = TextAreaModel.HAlignment.LEFT;
        box.curY += Math.max(cellSpacing, convertToPX0(tableStyle, StyleAttribute.PADDING_TOP, box.boxWidth));

        LImage bgImages[] = new LImage[numColumns];

        for(int row=0 ; row<numRows ; row++) {
            if(row > 0) {
                box.curY += cellSpacing;
            }
            
            LImage rowBGImage = null;
            Style rowStyle = te.getRowStyle(row);
            if(rowStyle != null) {
                int marginTop = convertToPX0(rowStyle, StyleAttribute.MARGIN_TOP, tableWidth);
                box.curY = box.computeTopPadding(marginTop);
                
                Image image = selectImage(rowStyle, StyleAttribute.BACKGROUND_IMAGE);
                if(image == null) {
                    image = createBackgroundColor(rowStyle);
                }
                if(image != null) {
                    rowBGImage = new LImage(te, image);
                    rowBGImage.y = box.curY;
                    rowBGImage.x = left;
                    rowBGImage.width = tableWidth;
                    box.clip.bgImages.add(rowBGImage);
                }

                box.curY += convertToPX0(rowStyle, StyleAttribute.PADDING_TOP, tableWidth);
                box.minLineHeight = convertToPX0(rowStyle, StyleAttribute.HEIGHT, tableWidth);
            }

            int x = left;
            for(int col=0 ; col<numColumns ; col++) {
                x += columnSpacing[col];
                TextAreaModel.TableCellElement cell = te.getCell(row, col);
                int width = columnWidth[col];
                if(cell != null) {
                    for(int c=1 ; c<cell.getColspan() ; c++) {
                        width += columnSpacing[col+c] + columnWidth[col+c];
                    }

                    Style cellStyle = cell.getStyle();

                    int paddingLeft = Math.max(cellPadding, convertToPX0(cellStyle, StyleAttribute.PADDING_LEFT, tableWidth));
                    int paddingRight = Math.max(cellPadding, convertToPX0(cellStyle, StyleAttribute.PADDING_RIGHT, tableWidth));

                    LClip clip = new LClip(cell);
                    LImage bgImage = createBGImage(box, cell);
                    if(bgImage != null) {
                        bgImage.x = x;
                        bgImage.width = width;
                        bgImage.hoverSrc = clip;
                        bgImages[col] = bgImage;
                    }
                    
                    clip.x = x;
                    clip.y = box.curY;
                    clip.width = width;
                    clip.marginTop = (short)convertToPX0(cellStyle, StyleAttribute.MARGIN_TOP, tableWidth);
                    box.layout.add(clip);

                    layoutBox(clip, tableWidth, paddingLeft, paddingRight, cell, null, box.doCacheText);

                    col += Math.max(0, cell.getColspan()-1);
                }
                x += width;
            }
            box.nextLine(false);

            for(int col=0 ; col<numColumns ; col++) {
                LImage bgImage = bgImages[col];
                if(bgImage != null) {
                    bgImage.height = box.curY - bgImage.y;
                    bgImages[col] = null;   // clear for next row
                }
            }

            if(rowStyle != null) {
                box.curY += convertToPX0(rowStyle, StyleAttribute.PADDING_BOTTOM, tableWidth);
                
                if(rowBGImage != null) {
                    rowBGImage.height = box.curY - rowBGImage.y;
                }

                doMarginBottom(box, rowStyle);
            }
        }

        box.curY += Math.max(cellSpacing, convertToPX0(tableStyle, StyleAttribute.PADDING_BOTTOM, box.boxWidth));
        box.checkFloaters();
        box.accountMinRemaining(Math.max(0, box.lineWidth - tableWidth));

        if(tableBGImage != null) {
            tableBGImage.height = box.curY - tableBGImage.y;
            tableBGImage.x = left;
            tableBGImage.width = tableWidth;
        }

        // anchor.y already set (by addAnchor)
        anchor.x = left;
        anchor.width = tableWidth;
        anchor.height = box.curY - anchor.y;

        doMarginBottom(box, tableStyle);
    }

    private LImage createBGImage(Box box, TextAreaModel.Element element) {
        Style style = element.getStyle();
        Image image = selectImage(style, StyleAttribute.BACKGROUND_IMAGE);
        if(image == null) {
            image = createBackgroundColor(style);
        }
        if(image != null) {
            LImage bgImage = new LImage(element, image);
            bgImage.y = box.curY;
            box.clip.bgImages.add(bgImage);
            return bgImage;
        }
        return null;
    }
    
    private Image createBackgroundColor(Style style) {
        Color color = style.get(StyleAttribute.BACKGROUND_COLOR, styleClassResolver);
        if(color.getAlpha() != 0) {
            Image white = selectImage("white");
            if(white != null) {
                Image image = white.createTintedVersion(color);
                Color colorHover = style.get(StyleAttribute.BACKGROUND_COLOR_HOVER, styleClassResolver);
                if(colorHover != null) {
                    return new StateSelectImage(HOVER_STATESELECT, null,
                            white.createTintedVersion(colorHover), image);
                }
                return image;
            }
        }
        return null;
    }

    static boolean isSkip(char ch) {
        return Character.isWhitespace(ch);
    }

    static boolean isPunctuation(char ch) {
        return ":;,.-!?".indexOf(ch) >= 0;
    }

    static boolean isBreak(char ch) {
        return Character.isWhitespace(ch) || isPunctuation(ch) ||
                (ch == 0x3001) || (ch == 0x3002);
    }

    class Box {
        final LClip clip;
        final ArrayList<LElement> layout;
        final ArrayList<LElement> objLeft = new ArrayList<LElement>();
        final ArrayList<LElement> objRight = new ArrayList<LElement>();
        final StringBuilder lineInfo = new StringBuilder();
        final int boxLeft;
        final int boxWidth;
        final int boxMarginOffsetLeft;
        final int boxMarginOffsetRight;
        final boolean doCacheText;
        int curY;
        int curX;
        int lineStartIdx;
        int lastProcessedAnchorIdx;
        int marginTop;
        int marginLeft;
        int marginRight;
        int marginBottomAbs;
        int marginBottomNext;
        int lineStartX;
        int lineWidth;
        int fontLineHeight;
        int minLineHeight;
        int lastLineEnd;
        int lastLineBottom;
        int minRemainingWidth;
        boolean inParagraph;
        boolean wasAutoBreak;
        boolean wasPreformatted;
        TextAreaModel.HAlignment textAlignment;
        String href;
        Style style;

        Box(LClip clip, int paddingLeft, int paddingRight, int paddingTop, boolean doCacheText) {
            this.clip = clip;
            this.layout = clip.layout;
            this.boxLeft = paddingLeft;
            this.boxWidth = Math.max(0, clip.width - paddingLeft - paddingRight);
            this.boxMarginOffsetLeft = paddingLeft;
            this.boxMarginOffsetRight = paddingRight;
            this.doCacheText = doCacheText;
            this.curX = paddingLeft;
            this.curY = paddingTop;
            this.lineStartX = paddingLeft;
            this.lineWidth = boxWidth;
            this.minRemainingWidth = boxWidth;
            this.textAlignment = TextAreaModel.HAlignment.LEFT;
            assert layout.isEmpty();
        }

        void computePadding() {
            int left = computeLeftPadding(marginLeft);
            int right = computeRightPadding(marginRight);

            lineStartX = left;
            lineWidth = Math.max(0, right - left);

            if(isAtStartOfLine()) {
                curX = lineStartX;
            }
            
            accountMinRemaining(getRemaining());
        }

        int computeLeftPadding(int marginLeft) {
            int left = boxLeft + Math.max(0, marginLeft - boxMarginOffsetLeft);

            for(int i=0,n=objLeft.size() ; i<n ; i++) {
                LElement e = objLeft.get(i);
                left = Math.max(left, e.x + e.width + Math.max(e.marginRight, marginLeft));
            }

            return left;
        }

        int computeRightPadding(int marginRight) {
            int right = boxLeft + boxWidth - Math.max(0, marginRight - boxMarginOffsetRight);

            for(int i=0,n=objRight.size() ; i<n ; i++) {
                LElement e = objRight.get(i);
                right = Math.min(right, e.x - Math.max(e.marginLeft, marginRight));
            }

            return right;
        }

        int computePaddingWidth(int marginLeft, int marginRight) {
            return Math.max(0, computeRightPadding(marginRight) - computeLeftPadding(marginLeft));
        }

        int computeTopPadding(int marginTop) {
            return Math.max(marginBottomAbs, curY + marginTop);
        }

        void setMarginBottom(int marginBottom) {
            if(isAtStartOfLine()) {
                marginBottomAbs = Math.max(marginBottomAbs, curY + marginBottom);
            } else {
                marginBottomNext = Math.max(marginBottomNext, marginBottom);
            }
        }
        
        int getRemaining() {
            return Math.max(0, lineWidth - curX + lineStartX);
        }
        
        void accountMinRemaining(int remaining) {
            minRemainingWidth = Math.min(minRemainingWidth, remaining);
        }

        int getXAndAdvance(int amount) {
            int x = curX;
            curX = x + amount;
            return x;
        }

        boolean isAtStartOfLine() {
            return lineStartIdx == layout.size();
        }

        boolean prevOnLineEndsNotWithSpace() {
            int layoutSize = layout.size();
            if(lineStartIdx < layoutSize) {
                LElement le = layout.get(layoutSize-1);
                if(le instanceof LText) {
                    LText lt = (LText)le;
                    return !isSkip(lt.text.charAt(lt.end-1));
                }
                return true;
            }
            return false;
        }

        void checkFloaters() {
            removeObjFromList(objLeft);
            removeObjFromList(objRight);
            computePadding();
            // curX is set by computePadding()
        }

        void clearFloater(TextAreaModel.Clear clear) {
            if(clear != TextAreaModel.Clear.NONE) {
                int targetY = -1;
                if(clear == TextAreaModel.Clear.LEFT || clear == TextAreaModel.Clear.BOTH) {
                    for(int i=0,n=objLeft.size() ; i<n ; ++i) {
                        LElement le = objLeft.get(i);
                        if(le.height != Short.MAX_VALUE) {  // special case for list elements
                            targetY = Math.max(targetY, le.y + le.height);
                        }
                    }
                }
                if(clear == TextAreaModel.Clear.RIGHT || clear == TextAreaModel.Clear.BOTH) {
                    for(int i=0,n=objRight.size() ; i<n ; ++i) {
                        LElement le = objRight.get(i);
                        targetY = Math.max(targetY, le.y + le.height);
                    }
                }
                if(targetY >= 0) {
                    advanceToY(targetY);
                }
            }
        }

        void advanceToY(int targetY) {
            nextLine(false);
            if(targetY > curY) {
                curY = targetY;
                checkFloaters();
            }
        }

        void advancePastFloaters(int requiredWidth, int marginLeft, int marginRight) {
            if(computePaddingWidth(marginLeft, marginRight) < requiredWidth) {
                nextLine(false);
                do {
                    int targetY = Integer.MAX_VALUE;
                    if(!objLeft.isEmpty()) {
                        LElement le = objLeft.get(objLeft.size()-1);
                        if(le.height != Short.MAX_VALUE) {  // special case for list elements
                            targetY = Math.min(targetY, le.bottom());
                        }
                    }
                    if(!objRight.isEmpty()) {
                        LElement le = objRight.get(objRight.size()-1);
                        targetY = Math.min(targetY, le.bottom());
                    }
                    if(targetY == Integer.MAX_VALUE || targetY < curY) {
                        return;
                    }
                    curY = targetY;
                    checkFloaters();
                } while(computePaddingWidth(marginLeft, marginRight) < requiredWidth);
            }
        }

        boolean nextLine(boolean force) {
            if(isAtStartOfLine() && (wasAutoBreak || !force)) {
                wasAutoBreak = !force;
                return false;
            }

            accountMinRemaining(getRemaining());
            
            int targetY = curY;
            int lineHeight = minLineHeight;

            if(isAtStartOfLine()) {
                lineHeight = Math.max(lineHeight, fontLineHeight);
            } else {
                for(int idx=lineStartIdx ; idx<layout.size() ; idx++) {
                    LElement le = layout.get(idx);
                    lineHeight = Math.max(lineHeight, le.height);
                }

                LElement lastElement = layout.get(layout.size() - 1);
                int remaining = (lineStartX + lineWidth) - (lastElement.x + lastElement.width);

                switch(textAlignment) {
                case RIGHT: {
                    for(int idx=lineStartIdx ; idx<layout.size() ; idx++) {
                        LElement le = layout.get(idx);
                        le.x += remaining;
                    }
                    break;
                }
                case CENTER: {
                    int offset = remaining / 2;
                    for(int idx=lineStartIdx ; idx<layout.size() ; idx++) {
                        LElement le = layout.get(idx);
                        le.x += offset;
                    }
                    break;
                }
                case JUSTIFY:
                    if(remaining < lineWidth / 4) {
                        int num = layout.size() - lineStartIdx;
                        for(int i=1 ; i<num ; i++) {
                            LElement le = layout.get(lineStartIdx + i);
                            int offset = remaining * i / (num-1);
                            le.x += offset;
                        }
                    }
                    break;
                }

                for(int idx=lineStartIdx ; idx<layout.size() ; idx++) {
                    LElement le = layout.get(idx);
                    switch(le.element.getStyle().get(StyleAttribute.VERTICAL_ALIGNMENT, styleClassResolver)) {
                    case BOTTOM:
                        le.y = lineHeight - le.height;
                        break;
                    case TOP:
                        le.y = 0;
                        break;
                    case MIDDLE:
                        le.y = (lineHeight - le.height)/2;
                        break;
                    case FILL:
                        le.y = 0;
                        le.height = lineHeight;
                        break;
                    }
                    targetY = Math.max(targetY, computeTopPadding(le.marginTop - le.y));
                    marginBottomNext = Math.max(marginBottomNext, le.bottom() - lineHeight);
                }
                
                for(int idx=lineStartIdx ; idx<layout.size() ; idx++) {
                    LElement le = layout.get(idx);
                    le.y += targetY;
                }
            }

            processAnchors(targetY, lineHeight);

            minLineHeight = 0;
            lineStartIdx = layout.size();
            wasAutoBreak = !force;
            curY = targetY + lineHeight;
            marginBottomAbs = Math.max(marginBottomAbs, curY + marginBottomNext);
            marginBottomNext = 0;
            marginTop = 0;
            checkFloaters();
            // curX is set by computePadding() inside checkFloaters()
            return true;
        }

        void finish() {
            nextLine(false);
            clearFloater(TextAreaModel.Clear.BOTH);
            processAnchors(curY, 0);
            int lineInfoLength = lineInfo.length();
            clip.lineInfo = new char[lineInfoLength];
            lineInfo.getChars(0, lineInfoLength, clip.lineInfo, 0);
        }

        int computeNextTabStop(Style style, Font font) {
            int em = font.getEM();
            int tabSize = style.get(StyleAttribute.TAB_SIZE, styleClassResolver);
            if(tabSize <= 0 || em <= 0) {
                // replace with single space when tabs are disabled
                return curX + font.getSpaceWidth();
            }
            int tabSizePX = Math.min(tabSize, Short.MAX_VALUE / em) * em;
            int x = curX - lineStartX + font.getSpaceWidth();
            return curX + tabSizePX - (x % tabSizePX);
        }

        private void removeObjFromList(ArrayList<LElement> list) {
            for(int i=list.size() ; i-->0 ;) {
                LElement e = list.get(i);
                if(e.bottom() <= curY) {
                    // can't update marginBottomAbs here - results in layout error for text
                    list.remove(i);
                }
            }
        }

        void setupTextParams(Style style, Font font, boolean isParagraphStart) {
            if(font != null) {
                fontLineHeight = font.getLineHeight();
            } else {
                fontLineHeight = 0;
            }

            if(isParagraphStart) {
                nextLine(false);
                inParagraph = true;
            }

            if(isParagraphStart || (!inParagraph && isAtStartOfLine())) {
                marginLeft = convertToPX0(style, StyleAttribute.MARGIN_LEFT, boxWidth);
                marginRight = convertToPX0(style, StyleAttribute.MARGIN_RIGHT, boxWidth);
                textAlignment = style.get(StyleAttribute.HORIZONTAL_ALIGNMENT, styleClassResolver);
                computePadding();
                curX = Math.max(0, lineStartX + convertToPX(style, StyleAttribute.TEXT_INDENT, boxWidth, 0));
            }
            
            marginTop = convertToPX0(style, StyleAttribute.MARGIN_TOP, boxWidth);
        }

        LElement addAnchor(TextAreaModel.Element e) {
            LElement le = new LElement(e);
            le.y = curY;
            le.x = boxLeft;
            le.width = boxWidth;
            clip.anchors.add(le);
            return le;
        }

        private void processAnchors(int y, int height) {
            while(lastProcessedAnchorIdx < clip.anchors.size()) {
                LElement le = clip.anchors.get(lastProcessedAnchorIdx++);
                if(le.height == 0) {
                    le.y = y;
                    le.height = height;
                }
            }
            if(lineStartIdx > lastLineEnd) {
                lineInfo.append((char)0).append((char)(lineStartIdx - lastLineEnd));
            }
            if(y > lastLineBottom) {
                lineInfo.append((char)y).append((char)0);
            }
            lastLineBottom = y + height;
            lineInfo.append((char)lastLineBottom).append((char)(layout.size() - lineStartIdx));
            lastLineEnd = layout.size();
        }
    }

    static class RenderInfo {
        int offsetX;
        int offsetY;
        Renderer renderer;
        final AnimationState asNormal;
        final AnimationState asHover;

        RenderInfo(AnimationState parent) {
            asNormal = new AnimationState(parent);
            asNormal.setAnimationState(STATE_HOVER, false);
            asHover = new AnimationState(parent);
            asHover.setAnimationState(STATE_HOVER, true);
        }
        
        AnimationState getAnimationState(boolean isHover) {
            return isHover ? asHover : asNormal;
        }
    }

    static class LElement {
        final TextAreaModel.Element element;
        int x;
        int y;
        int width;
        int height;
        short marginTop;
        short marginLeft;
        short marginRight;
        short marginBottom;
        String href;
        boolean isHover;
        boolean inheritHover;

        LElement(TextAreaModel.Element element) {
            this.element = element;
        }

        void adjustWidget(int offX, int offY) {}
        void collectBGImages(int offX, int offY, ArrayList<LImage> allBGImages) {}
        void draw(RenderInfo ri) {}
        void destroy() {}

        boolean isInside(int x, int y) {
            return (x >= this.x) && (x < this.x + this.width) &&
                    (y >= this.y) && (y < this.y + this.height);
        }
        LElement find(int x, int y) {
            return this;
        }
        LElement find(TextAreaModel.Element element, int[] offset) {
            if(this.element == element) {
                return this;
            }
            return null;
        }
        boolean setHover(LElement le) {
            isHover = (this == le) || (le != null && element == le.element);
            return isHover;
        }

        int bottom() {
            return y + height + marginBottom;
        }
    }
    
    static class FontData {
        final Font font;
        final Color color;
        final Color colorHover;

        FontData(Font font, Color color, Color colorHover) {
            if(colorHover == null) {
                colorHover = color;
            }
            this.font = font;
            this.color = maskWhite(color);
            this.colorHover = maskWhite(colorHover);
        }
        
        public Color getColor(boolean isHover) {
            return isHover ? colorHover : color;
        }
        
        private static Color maskWhite(Color c) {
            return Color.WHITE.equals(c) ? null : c;
        }
    }

    static class LText extends LElement {
        final FontData fontData;
        final String text;
        final int start;
        final int end;
        FontCache cache;

        LText(TextAreaModel.Element element, FontData fontData, String text, int start, int end, boolean doCache) {
            super(element);
            Font font = fontData.font;
            this.fontData = fontData;
            this.text = text;
            this.start = start;
            this.end = end;
            this.cache = doCache ? font.cacheText(null, text, start, end) : null;
            this.height = font.getLineHeight();

            if(cache != null) {
                this.width = cache.getWidth();
            } else {
                this.width = font.computeTextWidth(text, start, end);
            }
        }

        @Override
        void draw(RenderInfo ri) {
            Color c = fontData.getColor(isHover);
            if(c != null) {
                drawTextWithColor(ri, c);
            } else {
                drawText(ri);
            }
        }
        
        private void drawTextWithColor(RenderInfo ri, Color c) {
            ri.renderer.pushGlobalTintColor(c.getRedFloat(), c.getGreenFloat(), c.getBlueFloat(), c.getAlphaFloat());
            drawText(ri);
            ri.renderer.popGlobalTintColor();
        }
        
        private void drawText(RenderInfo ri) {
            AnimationState as = ri.getAnimationState(isHover);
            if(cache != null) {
                cache.draw(as, x+ri.offsetX, y+ri.offsetY);
            } else {
                fontData.font.drawText(as, x+ri.offsetX, y+ri.offsetY, text, start, end);
            }
        }

        @Override
        void destroy() {
            if(cache != null) {
                cache.destroy();
                cache = null;
            }
        }
    }

    static class LWidget extends LElement {
        final Widget widget;

        LWidget(TextAreaModel.Element element, Widget widget) {
            super(element);
            this.widget = widget;
        }

        @Override
        void adjustWidget(int offX, int offY) {
            widget.setPosition(x + offX, y + offY);
            widget.setSize(width, height);
        }
    }

    static class LImage extends LElement {
        final Image img;
        LElement hoverSrc;

        @SuppressWarnings("LeakingThisInConstructor")
        LImage(TextAreaModel.Element element, Image img) {
            super(element);
            this.img = img;
            this.width = img.getWidth();
            this.height = img.getHeight();
            this.hoverSrc = this;
        }
        
        @Override
        void draw(RenderInfo ri) {
            img.draw(ri.getAnimationState(hoverSrc.isHover),
                    x+ri.offsetX, y+ri.offsetY, width, height);
        }
    }

    static class LClip extends LElement {
        final ArrayList<LElement> layout;
        final ArrayList<LImage> bgImages;
        final ArrayList<LElement> anchors;
        char[] lineInfo;

        LClip(TextAreaModel.Element element) {
            super(element);
            this.layout = new ArrayList<LElement>();
            this.bgImages = new ArrayList<LImage>();
            this.anchors = new ArrayList<LElement>();
            this.lineInfo = EMPTY_CHAR_ARRAY;
        }
        
        @Override
        void draw(RenderInfo ri) {
            ri.offsetX += x;
            ri.offsetY += y;
            ri.renderer.clipEnter(ri.offsetX, ri.offsetY, width, height);
            try {
                if(!ri.renderer.clipIsEmpty()) {
                    final ArrayList<LElement> ll = layout;
                    for(int i=0,n=ll.size() ; i<n ; i++) {
                        ll.get(i).draw(ri);
                    }
                }
            } finally {
                ri.renderer.clipLeave();
                ri.offsetX -= x;
                ri.offsetY -= y;
            }
        }
        
        @Override
        void adjustWidget(int offX, int offY) {
            offX += x;
            offY += y;
            for(int i=0,n=layout.size() ; i<n ; i++) {
                layout.get(i).adjustWidget(offX, offY);
            }
        }
        
        @Override
        void collectBGImages(int offX, int offY, ArrayList<LImage> allBGImages) {
            offX += x;
            offY += y;
            for(int i=0,n=bgImages.size() ; i<n ; i++) {
                LImage img = bgImages.get(i);
                img.x += offX;
                img.y += offY;
                allBGImages.add(img);
            }
            for(int i=0,n=layout.size() ; i<n ; i++) {
                layout.get(i).collectBGImages(offX, offY, allBGImages);
            }
        }

        @Override
        void destroy() {
            for(int i=0,n=layout.size() ; i<n ; i++) {
                layout.get(i).destroy();
            }
            layout.clear();
            bgImages.clear();
            lineInfo = EMPTY_CHAR_ARRAY;
        }

        @Override
        LElement find(int x, int y) {
            x -= this.x;
            y -= this.y;
            int lineTop = 0;
            int layoutIdx = 0;
            for(int lineIdx=0 ; lineIdx<lineInfo.length && y >= lineTop ;) {
                int lineBottom = lineInfo[lineIdx++];
                int layoutCount = lineInfo[lineIdx++];
                if(layoutCount > 0) {
                    if(lineBottom == 0 || y < lineBottom) {
                        for(int i=0 ; i<layoutCount ; i++) {
                            LElement le = layout.get(layoutIdx + i);
                            if(le.isInside(x, y)) {
                                return le.find(x, y);
                            }
                        }
                        if(lineBottom > 0 && x >= layout.get(layoutIdx).x) {
                            LElement prev = null;
                            for(int i=0 ; i<layoutCount ; i++) {
                                LElement le = layout.get(layoutIdx + i);
                                if(le.x >= x && (prev == null || prev.element == le.element)) {
                                    return le;
                                }
                                prev = le;
                            }
                        }
                    }
                    layoutIdx += layoutCount;
                }
                if(lineBottom > 0) {
                    lineTop = lineBottom;
                }
            }
            return this;
        }

        @Override
        LElement find(TextAreaModel.Element element, int[] offset) {
            if(this.element == element) {
                return this;
            }
            LElement match = find(layout, element, offset);
            if(match == null) {
                match = find(anchors, element, offset);
            }
            return match;
        }
        
        private LElement find(ArrayList<LElement> l, TextAreaModel.Element e, int[] offset) {
            for(int i=0,n=l.size() ; i<n ; i++) {
                LElement match = l.get(i).find(e, offset);
                if(match != null) {
                    if(offset != null) {
                        offset[0] += this.x;
                        offset[1] += this.y;
                    }
                    return match;
                }
            }
            return null;
        }

        @Override
        boolean setHover(LElement le) {
            boolean childHover = false;
            for(int i=0,n=layout.size() ; i<n ; i++) {
                childHover |= layout.get(i).setHover(le);
            }
            if(childHover) {
                isHover = true;
            } else {
                super.setHover(le);
            }
            for(int i=0,n=layout.size() ; i<n ; i++) {
                LElement child = layout.get(i);
                if(child.inheritHover) {
                    child.isHover = isHover;
                }
            }
            return isHover;
        }
        
        void moveContentY(int amount) {
            for(int i=0,n=layout.size() ; i<n ; i++) {
                layout.get(i).y += amount;
            }
            if(lineInfo.length > 0) {
                if(lineInfo[1] == 0) {
                    lineInfo[0] += amount;
                } else {
                    int n = lineInfo.length;
                    char[] tmpLineInfo = new char[n+2];
                    tmpLineInfo[0] = (char)amount;
                    for(int i=0 ; i<n ; i+=2) {
                        int lineBottom = lineInfo[i];
                        if(lineBottom > 0) {
                            lineBottom += amount;
                        }
                        tmpLineInfo[i+2] = (char)lineBottom;
                        tmpLineInfo[i+3] = lineInfo[i+1];
                    }
                    lineInfo = tmpLineInfo;
                }
            }
        }
    }
}
