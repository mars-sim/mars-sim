/*
 * Copyright (c) 2008-2010, Matthias Mann
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
package test;

import de.matthiasmann.twl.ColorSelector;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.model.ColorSpaceHSL;
import de.matthiasmann.twl.utils.TintAnimator;

/**
 *
 * @author Matthias Mann
 */
public class ColorSelectorDemoDialog1 extends FadeFrame {

    public ColorSelectorDemoDialog1()  {
        final ColorSelector cs = new ColorSelector(new ColorSpaceHSL());

        final ToggleButton btnUse2D = new ToggleButton();
        btnUse2D.setActive(cs.isUseColorArea2D());
        btnUse2D.addCallback(new Runnable() {
            public void run() {
                cs.setUseColorArea2D(btnUse2D.isActive());
            }
        });
        Label labelUse2D = new Label("Use 2D color area");
        labelUse2D.setLabelFor(btnUse2D);

        final ToggleButton btnUseLabels = new ToggleButton();
        btnUseLabels.setActive(cs.isUseColorArea2D());
        btnUseLabels.addCallback(new Runnable() {
            public void run() {
                cs.setUseLabels(btnUseLabels.isActive());
            }
        });
        Label labelUseLabels = new Label("show labels for adjusters");
        labelUseLabels.setLabelFor(btnUseLabels);

        final ToggleButton btnShowPreview = new ToggleButton();
        btnShowPreview.setActive(cs.isShowPreview());
        btnShowPreview.addCallback(new Runnable() {
            public void run() {
                cs.setShowPreview(btnShowPreview.isActive());
            }
        });
        Label labelShowPreview = new Label("show color preview");
        labelShowPreview.setLabelFor(btnShowPreview);

        final ToggleButton btnShowHexEditField = new ToggleButton();
        btnShowHexEditField.setActive(cs.isShowHexEditField());
        btnShowHexEditField.addCallback(new Runnable() {
            public void run() {
                cs.setShowHexEditField(btnShowHexEditField.isActive());
            }
        });
        Label labelShowHexEditField = new Label("show hex edit field");
        labelShowHexEditField.setLabelFor(btnShowHexEditField);

        final ToggleButton btnShowNativeAdjuster = new ToggleButton();
        btnShowNativeAdjuster.setActive(cs.isShowNativeAdjuster());
        btnShowNativeAdjuster.addCallback(new Runnable() {
            public void run() {
                cs.setShowNativeAdjuster(btnShowNativeAdjuster.isActive());
            }
        });
        Label labelShowNativeAdjuster = new Label("show native (HSL) adjuster");
        labelShowNativeAdjuster.setLabelFor(btnShowNativeAdjuster);

        final ToggleButton btnShowRGBAdjuster = new ToggleButton();
        btnShowRGBAdjuster.setActive(cs.isShowRGBAdjuster());
        btnShowRGBAdjuster.addCallback(new Runnable() {
            public void run() {
                cs.setShowRGBAdjuster(btnShowRGBAdjuster.isActive());
            }
        });
        Label labelShowRGBAdjuster = new Label("show RGB adjuster");
        labelShowRGBAdjuster.setLabelFor(btnShowRGBAdjuster);

        final ToggleButton btnShowAlphaAdjuster = new ToggleButton();
        btnShowAlphaAdjuster.setActive(cs.isShowAlphaAdjuster());
        btnShowAlphaAdjuster.addCallback(new Runnable() {
            public void run() {
                cs.setShowAlphaAdjuster(btnShowAlphaAdjuster.isActive());
            }
        });
        Label labelShowAlphaAdjuster = new Label("show alpha adjuster");
        labelShowAlphaAdjuster.setLabelFor(btnShowAlphaAdjuster);

        final TintAnimator tintAnimator = new TintAnimator(new TintAnimator.GUITimeSource(this));

        Label testDisplay = new Label("This is a test display");
        testDisplay.setTheme("testDisplay");
        testDisplay.setTintAnimator(tintAnimator);

        Label testDisplay2 = new Label("This is a test display");
        testDisplay2.setTheme("testDisplay2");
        testDisplay2.setTintAnimator(tintAnimator);

        Runnable colorChangedCB = new Runnable() {
            public void run() {
                tintAnimator.setColor(cs.getColor());
            }
        };

        cs.addCallback(colorChangedCB);
        colorChangedCB.run();   // make sure the preview has the right color
        
        DialogLayout dl = new DialogLayout();
        dl.setHorizontalGroup(dl.createParallelGroup()
                .addWidgets(cs, testDisplay, testDisplay2)
                .addGroup(dl.createSequentialGroup().addGap().addWidgets(labelUse2D, btnUse2D))
                .addGroup(dl.createSequentialGroup().addGap().addWidgets(labelUseLabels, btnUseLabels))
                .addGroup(dl.createSequentialGroup().addGap().addWidgets(labelShowPreview, btnShowPreview))
                .addGroup(dl.createSequentialGroup().addGap().addWidgets(labelShowHexEditField, btnShowHexEditField))
                .addGroup(dl.createSequentialGroup().addGap().addWidgets(labelShowNativeAdjuster, btnShowNativeAdjuster))
                .addGroup(dl.createSequentialGroup().addGap().addWidgets(labelShowRGBAdjuster, btnShowRGBAdjuster))
                .addGroup(dl.createSequentialGroup().addGap().addWidgets(labelShowAlphaAdjuster, btnShowAlphaAdjuster)));
        dl.setVerticalGroup(dl.createSequentialGroup()
                .addWidget(cs)
                .addGap(DialogLayout.MEDIUM_GAP)
                .addWidget(testDisplay).addGap(0).addWidget(testDisplay2)
                .addGap(DialogLayout.MEDIUM_GAP)
                .addGroup(dl.createParallelGroup(labelUse2D, btnUse2D))
                .addGroup(dl.createParallelGroup(labelUseLabels, btnUseLabels))
                .addGroup(dl.createParallelGroup(labelShowPreview, btnShowPreview))
                .addGroup(dl.createParallelGroup(labelShowHexEditField, btnShowHexEditField))
                .addGroup(dl.createParallelGroup(labelShowNativeAdjuster, btnShowNativeAdjuster))
                .addGroup(dl.createParallelGroup(labelShowRGBAdjuster, btnShowRGBAdjuster))
                .addGroup(dl.createParallelGroup(labelShowAlphaAdjuster, btnShowAlphaAdjuster)));

        setTheme("colorSelectorDemoFrame");
        setTitle("Color Selector Demo");
        add(dl);
    }
    
}
