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

import de.matthiasmann.twl.model.AutoCompletionResult;
import de.matthiasmann.twl.model.IntegerModel;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ProgressBar;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.model.AutoCompletionDataSource;
import de.matthiasmann.twl.model.OptionBooleanModel;
import de.matthiasmann.twl.model.SimpleAutoCompletionResult;
import java.util.ArrayList;
import java.util.Random;
import test.SimpleTest.StyleItem;

/**
 *
 * @author Matthias Mann
 */
public class WidgetsDemoDialog1 extends FadeFrame {

    private IntegerModel mSpeed;
    private ProgressBar progressBar;

    private int progress;
    private int timeout = 100;
    private boolean onoff = true;
    private Random r = new Random();
    
    public WidgetsDemoDialog1() {
        Label l1 = new Label("new Entry");
        final EditField e1 = new EditField();
        e1.setText("edit me");
        e1.setMaxTextLength(40);
        l1.setLabelFor(e1);

        Label l2 = new Label("Me");
        EditField e2 = new EditField();
        e2.setText("too!");
        e2.setMaxTextLength(40);
        e2.setPasswordMasking(true);
        l2.setLabelFor(e2);

        final SimpleChangableListModel<String> lm = new SimpleChangableListModel<String>(
                "Entry 1", "Entry 2", "Entry 3", "Another one", "ok, one more");

        final Button addBtn = new Button("Add to list");
        addBtn.addCallback(new Runnable() {
            public void run() {
                lm.addElement(e1.getText());
            }
        });
        addBtn.setTooltipContent("Adds the text from the edit field to the list box");

        e1.addCallback(new EditField.Callback() {
            public void callback(int key) {
                addBtn.setEnabled(e1.getTextLength() > 0);
            }
        });

        e1.setAutoCompletion(new AutoCompletionDataSource() {
            public AutoCompletionResult collectSuggestions(String text, int cursorPos, AutoCompletionResult prev) {
                text = text.substring(0, cursorPos);
                ArrayList<String> result = new ArrayList<String>();
                for(int i=0 ; i<lm.getNumEntries() ; i++) {
                    if(lm.matchPrefix(i, text)) {
                        result.add(lm.getEntry(i));
                    }
                }
                if(result.isEmpty()) {
                    return null;
                }
                return new SimpleAutoCompletionResult(text, 0, result);
            }
        });

        final EditField e3 = new EditField();
        e3.setText("This is a multi line Editfield\nTry it :)");
        e3.setMultiLine(true);

        ScrollPane sp = new ScrollPane(e3);
        sp.setFixed(ScrollPane.Fixed.HORIZONTAL);
        sp.setExpandContentSize(true);

        final SimpleChangableListModel<StyleItem> lmStyle = new SimpleChangableListModel<StyleItem>(
                new StyleItem("progressbar", "Simple"),
                new StyleItem("progressbar-glow", "Glow"),
                new StyleItem("progressbar-glow-anim", "Animated"));

        progressBar = new ProgressBar();

        ListBox<String> lb = new ListBox<String>(lm);

        final ToggleButton tb = new ToggleButton("");
        tb.setTheme("checkbox");
        tb.setActive(true);
        tb.setTooltipContent("Toggles the Frame title on/off");
        tb.addCallback(new Runnable() {
            public void run() {
                if(tb.isActive()) {
                    setTheme(SimpleTest.WITH_TITLE);
                } else {
                    setTheme(SimpleTest.WITHOUT_TITLE);
                }
                reapplyTheme();
            }
        });

        Label tbLabel = new Label("show title");
        tbLabel.setLabelFor(tb);

        final ComboBox<StyleItem> cb = new ComboBox<StyleItem>(lmStyle);
        cb.addCallback(new Runnable() {
            public void run() {
                int idx = cb.getSelected();
                progressBar.setTheme(lmStyle.getEntry(idx).theme);
                progressBar.reapplyTheme();
            }
        });
        cb.setSelected(2);
        cb.setComputeWidthFromModel(true);

        mSpeed = new SimpleIntegerModel(0, 100, 10);
        ValueAdjusterInt vai = new ValueAdjusterInt(mSpeed);
        Label l4 = new Label("Progressbar speed");
        l4.setLabelFor(vai);

        ToggleButton[] optionBtns = new ToggleButton[4];
        SimpleIntegerModel optionModel = new SimpleIntegerModel(1, optionBtns.length, 1);
        for(int i=0 ; i<optionBtns.length ; i++) {
            optionBtns[i] = new ToggleButton(new OptionBooleanModel(optionModel, i+1));
            optionBtns[i].setText(Integer.toString(i+1));
            optionBtns[i].setTheme("radiobutton");
        }

        DialogLayout box = new DialogLayout();
        box.setTheme("/optionsdialog"); // the '/' causes this theme to start at the root again
        box.setHorizontalGroup(box.createParallelGroup().addGroup(
                box.createSequentialGroup(
                    box.createParallelGroup(l1, l2, l4),
                    box.createParallelGroup().addGroup(box.createSequentialGroup(e1, addBtn)).addWidgets(e2, vai))).
                addWidget(progressBar).addWidget(lb).
                addWidget(sp).
                addGroup(box.createSequentialGroup(cb).addGap()).
                addGroup(box.createSequentialGroup(optionBtns).addGap()).
                addGroup(box.createSequentialGroup().addGap().addWidgets(tbLabel, tb)));
        box.setVerticalGroup(box.createSequentialGroup().
                addGroup(box.createParallelGroup(l1, e1, addBtn)).
                addGroup(box.createParallelGroup(l2, e2)).
                addGroup(box.createParallelGroup(l4, vai)).
                addWidgets(progressBar, lb, sp, cb).
                addGroup(box.createParallelGroup(optionBtns)).
                addGroup(box.createParallelGroup(tbLabel, tb)));

        setTheme(SimpleTest.WITH_TITLE);
        add(box);
        setTitle("TWL Example");
    }

    @Override
    protected void paint(GUI gui) {
        super.paint(gui);

        if(onoff) {
            progressBar.setValue(progress / 5000f);
            progress = (progress+mSpeed.getValue()) % 5000;
        }
        if(--timeout == 0) {
            onoff ^= true;
            timeout = 100 + r.nextInt(200);
        }
    }

}
