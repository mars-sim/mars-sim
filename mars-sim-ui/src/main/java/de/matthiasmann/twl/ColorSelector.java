/*
 * Copyright (c) 2008-2011, Matthias Mann
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

import de.matthiasmann.twl.model.AbstractFloatModel;
import de.matthiasmann.twl.model.AbstractIntegerModel;
import de.matthiasmann.twl.model.ColorModel;
import de.matthiasmann.twl.model.ColorSpace;
import de.matthiasmann.twl.renderer.DynamicImage;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.utils.TintAnimator;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * A color selector widget
 *
 * @author Matthias Mann
 */
public class ColorSelector extends DialogLayout {

    private static final String[] RGBA_NAMES = {"Red", "Green", "Blue", "Alpha"};
    private static final String[] RGBA_PREFIX = {"R: ", "G: ", "B: ", "A: "};

    final ByteBuffer imgData;
    final IntBuffer imgDataInt;

    ColorSpace colorSpace;
    float[] colorValues;
    ColorValueModel[] colorValueModels;
    private boolean useColorArea2D = true;
    private boolean showPreview = false;
    private boolean useLabels = true;
    private boolean showHexEditField = false;
    private boolean showNativeAdjuster = true;
    private boolean showRGBAdjuster = true;
    private boolean showAlphaAdjuster = true;
    private Runnable[] callbacks;
    private ColorModel model;
    private Runnable modelCallback;
    private boolean inModelSetValue;
    int currentColor;
    private ARGBModel[] argbModels;
    EditField hexColorEditField;
    private TintAnimator previewTintAnimator;
    private boolean recreateLayout;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ColorSelector(ColorSpace colorSpace) {
        // allocate enough space for 2D color areas
        imgData = ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * 4);
        imgData.order(ByteOrder.BIG_ENDIAN);
        imgDataInt = imgData.asIntBuffer();

        currentColor = Color.WHITE.toARGB();

        setColorSpace(colorSpace);
    }

    public ColorSpace getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(ColorSpace colorModel) {
        if(colorModel == null) {
            throw new NullPointerException("colorModel");
        }
        if(this.colorSpace != colorModel) {
            boolean hasColor = this.colorSpace != null;

            this.colorSpace = colorModel;
            this.colorValues = new float[colorModel.getNumComponents()];

            if(hasColor) {
                setColor(currentColor);
            } else {
                setDefaultColor();
            }

            recreateLayout = true;
            invalidateLayout();
        }
    }

    public ColorModel getModel() {
        return model;
    }

    public void setModel(ColorModel model) {
        if(this.model != model) {
            removeModelCallback();
            this.model = model;
            if(model != null) {
                addModelCallback();
                modelValueChanged();
            }
        }
    }

    public Color getColor() {
        return new Color(currentColor);
    }

    public void setColor(Color color) {
        setColor(color.toARGB());
        updateModel();
    }

    public void setDefaultColor() {
        currentColor = Color.WHITE.toARGB();
        for(int i=0 ; i<colorSpace.getNumComponents() ; i++) {
            colorValues[i] = colorSpace.getDefaultValue(i);
        }
        updateAllColorAreas();
        colorChanged();
    }

    public boolean isUseColorArea2D() {
        return useColorArea2D;
    }

    /**
     * Use 2D color areas.
     *
     * Color component 0 is the X axis and component 1 the Y axis of
     * the first 2D area, etc. If the number of color components is
     * odd then the last component is displayed with a 1D.
     *
     * If disabled all components are displayed using 1D color areas.
     *
     * @param useColorArea2D true if 2D areas should be used
     */
    public void setUseColorArea2D(boolean useColorArea2D) {
        if(this.useColorArea2D != useColorArea2D) {
            this.useColorArea2D = useColorArea2D;
            recreateLayout = true;
            invalidateLayout();
        }
    }

    public boolean isShowPreview() {
        return showPreview;
    }

    /**
     * Show the currently selected color in a preview widget.
     * Default is false.
     *
     * @param showPreview true if the preview widget should be displayed
     */
    public void setShowPreview(boolean showPreview) {
        if(this.showPreview != showPreview) {
            this.showPreview = showPreview;
            recreateLayout = true;
            invalidateLayout();
        }
    }

    public boolean isShowHexEditField() {
        return showHexEditField;
    }

    /**
     * Includes an edit field which allows to edit the color hex values in ARGB.
     * Default is false.
     *
     * @param showHexEditField true if the edit field should be shown
     */
    public void setShowHexEditField(boolean showHexEditField) {
        if(this.showHexEditField != showHexEditField) {
            this.showHexEditField = showHexEditField;
            recreateLayout = true;
            invalidateLayout();
        }
    }

    public boolean isShowAlphaAdjuster() {
        return showAlphaAdjuster;
    }

    public void setShowAlphaAdjuster(boolean showAlphaAdjuster) {
        if(this.showAlphaAdjuster != showAlphaAdjuster) {
            this.showAlphaAdjuster = showAlphaAdjuster;
            recreateLayout = true;
            invalidateLayout();
        }
    }

    public boolean isShowNativeAdjuster() {
        return showNativeAdjuster;
    }

    /**
     * Includes adjuster for each clor component of the specified color space.
     * Default is true.
     *
     * @param showNativeAdjuster true if the native adjuster should be displayed
     */
    public void setShowNativeAdjuster(boolean showNativeAdjuster) {
        if(this.showNativeAdjuster != showNativeAdjuster) {
            this.showNativeAdjuster = showNativeAdjuster;
            recreateLayout = true;
            invalidateLayout();
        }
    }

    public boolean isShowRGBAdjuster() {
        return showRGBAdjuster;
    }

    public void setShowRGBAdjuster(boolean showRGBAdjuster) {
        if(this.showRGBAdjuster != showRGBAdjuster) {
            this.showRGBAdjuster = showRGBAdjuster;
            recreateLayout = true;
            invalidateLayout();
        }
    }

    public boolean isUseLabels() {
        return useLabels;
    }

    /**
     * Show labels infront of the value adjusters for the color components.
     * Default is true.
     *
     * @param useLabels true if labels should be displayed
     */
    public void setUseLabels(boolean useLabels) {
        if(this.useLabels != useLabels) {
            this.useLabels = useLabels;
            recreateLayout = true;
            invalidateLayout();
        }
    }

    public void addCallback(Runnable cb) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, cb, Runnable.class);
    }

    public void removeCallback(Runnable cb) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, cb);
    }
    
    protected void updateModel() {
        if(model != null) {
            inModelSetValue = true;
            try {
                model.setValue(getColor());
            } finally {
                inModelSetValue = false;
            }
        }
    }

    protected void colorChanged() {
        currentColor = (currentColor & (0xFF << 24)) | colorSpace.toRGB(colorValues);
        CallbackSupport.fireCallbacks(callbacks);
        updateModel();
        if(argbModels != null) {
            for(ARGBModel m : argbModels) {
                m.fireCallback();
            }
        }
        if(previewTintAnimator != null) {
            previewTintAnimator.setColor(getColor());
        }
        updateHexEditField();
    }

    protected void setColor(int argb) {
        currentColor = argb;
        colorValues = colorSpace.fromRGB(argb & 0xFFFFFF);
        updateAllColorAreas();
    }
    
    protected int getNumComponents() {
        return colorSpace.getNumComponents();
    }

    @Override
    public void layout() {
        if(recreateLayout) {
            createColorAreas();
        }
        super.layout();
    }

    @Override
    public int getMinWidth() {
        if(recreateLayout) {
            createColorAreas();
        }
        return super.getMinWidth();
    }

    @Override
    public int getMinHeight() {
        if(recreateLayout) {
            createColorAreas();
        }
        return super.getMinHeight();
    }

    @Override
    public int getPreferredInnerWidth() {
        if(recreateLayout) {
            createColorAreas();
        }
        return super.getPreferredInnerWidth();
    }

    @Override
    public int getPreferredInnerHeight() {
        if(recreateLayout) {
            createColorAreas();
        }
        return super.getPreferredInnerHeight();
    }

    protected void createColorAreas() {
        recreateLayout = false;
        setVerticalGroup(null); // stop layout engine while we create new rules
        removeAllChildren();

        // recreate models to make sure that no callback is left over
        argbModels = new ARGBModel[4];
        argbModels[0] = new ARGBModel(16);
        argbModels[1] = new ARGBModel(8);
        argbModels[2] = new ARGBModel(0);
        argbModels[3] = new ARGBModel(24);

        int numComponents = getNumComponents();

        Group horzAreas = createSequentialGroup().addGap();
        Group vertAreas = createParallelGroup();

        Group horzLabels = null;
        Group horzAdjuster = createParallelGroup();
        Group horzControlls = createSequentialGroup();
        
        if(useLabels) {
            horzLabels = createParallelGroup();
            horzControlls.addGroup(horzLabels);
        }
        horzControlls.addGroup(horzAdjuster);

        Group[] vertAdjuster = new Group[4 + numComponents];
        int numAdjuters = 0;
        
        for(int i=0 ; i<vertAdjuster.length ; i++) {
            vertAdjuster[i] = createParallelGroup();
        }

        colorValueModels = new ColorValueModel[numComponents];
        for(int component=0 ; component<numComponents ; component++) {
            colorValueModels[component] = new ColorValueModel(component);

            if(showNativeAdjuster) {
                ValueAdjusterFloat vaf = new ValueAdjusterFloat(colorValueModels[component]);

                if(useLabels) {
                    Label label = new Label(colorSpace.getComponentName(component));
                    label.setLabelFor(vaf);
                    horzLabels.addWidget(label);
                    vertAdjuster[numAdjuters].addWidget(label);
                } else {
                    vaf.setDisplayPrefix(colorSpace.getComponentShortName(component).concat(": "));
                    vaf.setTooltipContent(colorSpace.getComponentName(component));
                }

                horzAdjuster.addWidget(vaf);
                vertAdjuster[numAdjuters].addWidget(vaf);
                numAdjuters++;
            }
        }

        for(int i=0 ; i<argbModels.length ; i++) {
            if((i == 3 && showAlphaAdjuster) || (i < 3 && showRGBAdjuster)) {
                ValueAdjusterInt vai = new ValueAdjusterInt(argbModels[i]);

                if(useLabels) {
                    Label label = new Label(RGBA_NAMES[i]);
                    label.setLabelFor(vai);
                    horzLabels.addWidget(label);
                    vertAdjuster[numAdjuters].addWidget(label);
                } else {
                    vai.setDisplayPrefix(RGBA_PREFIX[i]);
                    vai.setTooltipContent(RGBA_NAMES[i]);
                }

                horzAdjuster.addWidget(vai);
                vertAdjuster[numAdjuters].addWidget(vai);
                numAdjuters++;
            }
        }

        int component = 0;

        if(useColorArea2D) {
            for(; component+1 < numComponents ; component+=2) {
                ColorArea2D area = new ColorArea2D(component, component+1);
                area.setTooltipContent(colorSpace.getComponentName(component) +
                        " / " + colorSpace.getComponentName(component+1));

                horzAreas.addWidget(area);
                vertAreas.addWidget(area);
            }
        }

        for( ; component<numComponents ; component++) {
            ColorArea1D area = new ColorArea1D(component);
            area.setTooltipContent(colorSpace.getComponentName(component));

            horzAreas.addWidget(area);
            vertAreas.addWidget(area);
        }

        if(showHexEditField && hexColorEditField == null) {
            createHexColorEditField();
        }

        if(showPreview) {
            if(previewTintAnimator == null) {
                previewTintAnimator = new TintAnimator(this, getColor());
            }

            Widget previewArea = new Widget();
            previewArea.setTheme("colorarea");
            previewArea.setTintAnimator(previewTintAnimator);

            Widget preview = new Container();
            preview.setTheme("preview");
            preview.add(previewArea);
            
            Label label = new Label();
            label.setTheme("previewLabel");
            label.setLabelFor(preview);

            Group horz = createParallelGroup();
            Group vert = createSequentialGroup();

            horzAreas.addGroup(horz.addWidget(label).addWidget(preview));
            vertAreas.addGroup(vert.addGap().addWidget(label).addWidget(preview));

            if(showHexEditField) {
                horz.addWidget(hexColorEditField);
                vert.addGap().addWidget(hexColorEditField);
            }
        }

        Group horzMainGroup = createParallelGroup()
                .addGroup(horzAreas.addGap())
                .addGroup(horzControlls);
        Group vertMainGroup = createSequentialGroup()
                .addGroup(vertAreas);

        for(int i=0 ; i<numAdjuters ; i++) {
            vertMainGroup.addGroup(vertAdjuster[i]);
        }

        if(showHexEditField) {
            if(hexColorEditField == null) {
                createHexColorEditField();
            }
            
            if(!showPreview) {
                horzMainGroup.addWidget(hexColorEditField);
                vertMainGroup.addWidget(hexColorEditField);
            }

            updateHexEditField();
        }
        setHorizontalGroup(horzMainGroup);
        setVerticalGroup(vertMainGroup.addGap());
    }

    protected void updateAllColorAreas() {
        if(colorValueModels != null) {
            for(ColorValueModel cvm : colorValueModels) {
                cvm.fireCallback();
            }
            colorChanged();
        }
    }

    @Override
    protected void afterAddToGUI(GUI gui) {
        super.afterAddToGUI(gui);
        addModelCallback();
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {
        removeModelCallback();
        super.beforeRemoveFromGUI(gui);
    }
    
    private void removeModelCallback() {
        if(model != null) {
            model.removeCallback(modelCallback);
        }
    }
    
    private void addModelCallback() {
        if(model != null && getGUI() != null) {
            if(modelCallback == null) {
                modelCallback = new Runnable() {
                    public void run() {
                        modelValueChanged();
                    }
                };
            }
            model.addCallback(modelCallback);
        }
    }

    private void createHexColorEditField() {
        hexColorEditField = new EditField() {
            @Override
            protected void insertChar(char ch) {
                if(isValid(ch)) {
                    super.insertChar(ch);
                }
            }

            @Override
            public void insertText(String str) {
                for(int i=0,n=str.length() ; i<n ; i++) {
                    if(!isValid(str.charAt(i))) {
                        StringBuilder sb = new StringBuilder(str);
                        for(int j=n ; j-- >= i ;) {
                            if(!isValid(sb.charAt(j))) {
                                sb.deleteCharAt(j);
                            }
                        }
                        str = sb.toString();
                        break;
                    }
                }
                super.insertText(str);
            }

            private boolean isValid(char ch) {
                int digit = Character.digit(ch, 16);
                return digit >= 0 && digit < 16;
            }
        };
        hexColorEditField.setTheme("hexColorEditField");
        hexColorEditField.setColumns(8);
        hexColorEditField.addCallback(new EditField.Callback() {
            public void callback(int key) {
                if(key == Event.KEY_ESCAPE) {
                    updateHexEditField();
                    return;
                }
                Color color = null;
                try {
                    color = Color.parserColor("#".concat(hexColorEditField.getText()));
                    hexColorEditField.setErrorMessage(null);
                } catch(Exception ex) {
                    hexColorEditField.setErrorMessage("Invalid color format");
                }
                if(key == Event.KEY_RETURN && color != null) {
                    setColor(color);
                }
            }
        });
    }

    void updateHexEditField() {
        if(hexColorEditField != null) {
            hexColorEditField.setText(String.format("%08X", currentColor));
        }
    }
    
    void modelValueChanged() {
        if(!inModelSetValue && model != null) {
            // don't call updateModel here
            setColor(model.getValue().toARGB());
        }
    }
    
    private static final int IMAGE_SIZE = 64;

    class ColorValueModel extends AbstractFloatModel {
        private final int component;

        ColorValueModel(int component) {
            this.component = component;
        }

        public float getMaxValue() {
            return colorSpace.getMaxValue(component);
        }

        public float getMinValue() {
            return colorSpace.getMinValue(component);
        }

        public float getValue() {
            return colorValues[component];
        }

        public void setValue(float value) {
            colorValues[component] = value;
            doCallback();
            colorChanged();
        }

        void fireCallback() {
            doCallback();
        }
    }

    class ARGBModel extends AbstractIntegerModel {
        private final int startBit;

        ARGBModel(int startBit) {
            this.startBit = startBit;
        }

        public int getMaxValue() {
            return 255;
        }

        public int getMinValue() {
            return 0;
        }

        public int getValue() {
            return (currentColor >> startBit) & 255;
        }

        public void setValue(int value) {
            setColor((currentColor & ~(255 << startBit)) | (value << startBit));
        }

        void fireCallback() {
            doCallback();
        }
    }

    abstract class ColorArea extends Widget implements Runnable {
        DynamicImage img;
        Image cursorImage;
        boolean needsUpdate;

        @Override
        protected void applyTheme(ThemeInfo themeInfo) {
            super.applyTheme(themeInfo);
            cursorImage = themeInfo.getImage("cursor");
        }

        abstract void createImage(GUI gui);
        abstract void updateImage();
        abstract void handleMouse(int x, int y);

        @Override
        protected void paintWidget(GUI gui) {
            if(img == null) {
                createImage(gui);
                needsUpdate = true;
            }
            if(img != null) {
                if(needsUpdate) {
                    updateImage();
                }
                img.draw(getAnimationState(), getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight());
            }
        }

        @Override
        public void destroy() {
            super.destroy();
            if(img != null) {
                img.destroy();
                img = null;
            }
        }

        @Override
        protected boolean handleEvent(Event evt) {
            switch (evt.getType()) {
                case MOUSE_BTNDOWN:
                case MOUSE_DRAGGED:
                    handleMouse(evt.getMouseX() - getInnerX(), evt.getMouseY() - getInnerY());
                    return true;
                case MOUSE_WHEEL:
                    return false;
                default:
                    if(evt.isMouseEvent()) {
                        return true;
                    }
                    break;
            }
            return super.handleEvent(evt);
        }

        public void run() {
            needsUpdate = true;
        }
    }
    
    class ColorArea1D extends ColorArea {
        final int component;

        @SuppressWarnings("LeakingThisInConstructor")
        ColorArea1D(int component) {
            this.component = component;

            for(int i=0,n=getNumComponents() ; i<n ; i++) {
                if(i != component) {
                    colorValueModels[i].addCallback(this);
                }
            }
        }

        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if(cursorImage != null) {
                float minValue = colorSpace.getMinValue(component);
                float maxValue = colorSpace.getMaxValue(component);
                int pos = (int)((colorValues[component] - maxValue) * (getInnerHeight()-1) / (minValue - maxValue) + 0.5f);
                cursorImage.draw(getAnimationState(), getInnerX(), getInnerY() + pos, getInnerWidth(), 1);
            }
        }

        protected void createImage(GUI gui) {
            img = gui.getRenderer().createDynamicImage(1, IMAGE_SIZE);
        }

        protected void updateImage() {
            final float[] temp = ColorSelector.this.colorValues.clone();
            final IntBuffer buf = imgDataInt;
            final ColorSpace cs = colorSpace;

            float x = cs.getMaxValue(component);
            float dx = (cs.getMinValue(component) - x) / (IMAGE_SIZE - 1);

            for(int i=0 ; i<IMAGE_SIZE ; i++) {
                temp[component] = x;
                buf.put(i, (cs.toRGB(temp) << 8) | 0xFF);
                x += dx;
            }

            img.update(imgData, DynamicImage.Format.RGBA);
            needsUpdate = false;
        }

        @Override
        void handleMouse(int x, int y) {
            float minValue = colorSpace.getMinValue(component);
            float maxValue = colorSpace.getMaxValue(component);
            int innerHeight = getInnerHeight();
            int pos = Math.max(0, Math.min(innerHeight, y));
            float value = maxValue + (minValue - maxValue) * pos / innerHeight;
            colorValueModels[component].setValue(value);
        }
    }

    class ColorArea2D extends ColorArea {
        private final int componentX;
        private final int componentY;

        @SuppressWarnings("LeakingThisInConstructor")
        ColorArea2D(int componentX, int componentY) {
            this.componentX = componentX;
            this.componentY = componentY;

            for(int i=0,n=getNumComponents() ; i<n ; i++) {
                if(i != componentX && i != componentY) {
                    colorValueModels[i].addCallback(this);
                }
            }
        }
        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if(cursorImage != null) {
                float minValueX = colorSpace.getMinValue(componentX);
                float maxValueX = colorSpace.getMaxValue(componentX);
                float minValueY = colorSpace.getMinValue(componentY);
                float maxValueY = colorSpace.getMaxValue(componentY);
                int posX = (int)((colorValues[componentX] - maxValueX) * (getInnerWidth()-1) / (minValueX - maxValueX) + 0.5f);
                int posY = (int)((colorValues[componentY] - maxValueY) * (getInnerHeight()-1) / (minValueY - maxValueY) + 0.5f);
                cursorImage.draw(getAnimationState(), getInnerX() + posX, getInnerY() + posY, 1, 1);
            }
        }

        protected void createImage(GUI gui) {
            img = gui.getRenderer().createDynamicImage(IMAGE_SIZE, IMAGE_SIZE);
        }

        protected void updateImage() {
            final float[] temp = ColorSelector.this.colorValues.clone();
            final IntBuffer buf = imgDataInt;
            final ColorSpace cs = colorSpace;

            float x0 = cs.getMaxValue(componentX);
            float dx = (cs.getMinValue(componentX) - x0) / (IMAGE_SIZE - 1);

            float y = cs.getMaxValue(componentY);
            float dy = (cs.getMinValue(componentY) - y) / (IMAGE_SIZE - 1);

            for(int i=0,idx=0 ; i<IMAGE_SIZE ; i++) {
                temp[componentY] = y;
                float x = x0;
                for(int j=0 ; j<IMAGE_SIZE ; j++) {
                    temp[componentX] = x;
                    buf.put(idx++, (cs.toRGB(temp) << 8) | 0xFF);
                    x += dx;
                }
                y += dy;
            }

            img.update(imgData, DynamicImage.Format.RGBA);
            needsUpdate = false;
        }

        @Override
        void handleMouse(int x, int y) {
            float minValueX = colorSpace.getMinValue(componentX);
            float maxValueX = colorSpace.getMaxValue(componentX);
            float minValueY = colorSpace.getMinValue(componentY);
            float maxValueY = colorSpace.getMaxValue(componentY);
            int innerWidtht = getInnerWidth();
            int innerHeight = getInnerHeight();
            int posX = Math.max(0, Math.min(innerWidtht, x));
            int posY = Math.max(0, Math.min(innerHeight, y));
            float valueX = maxValueX + (minValueX - maxValueX) * posX / innerWidtht;
            float valueY = maxValueY + (minValueY - maxValueY) * posY / innerHeight;
            colorValueModels[componentX].setValue(valueX);
            colorValueModels[componentY].setValue(valueY);
        }
    }
}
