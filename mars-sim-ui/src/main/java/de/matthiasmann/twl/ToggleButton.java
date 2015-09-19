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

import de.matthiasmann.twl.model.BooleanModel;
import de.matthiasmann.twl.model.EnumModel;
import de.matthiasmann.twl.model.IntegerModel;
import de.matthiasmann.twl.model.OptionBooleanModel;
import de.matthiasmann.twl.model.OptionEnumModel;
import de.matthiasmann.twl.model.ToggleButtonModel;

/**
 * A toggle button.
 * 
 * <p>This class extends Button with the ablity to bind the selected state
 * to a {@link BooleanModel}.</p>
 * 
 * <p>The prefert way to use this class is by using the callback on the
 * {@code BooleanModel}.</p>
 * 
 * <p>To implement radio buttons one of the option models can be used:<ul>
 * <li>{@link OptionBooleanModel} which is backed by an {@link IntegerModel}</li>
 * <li>{@link OptionEnumModel} which is backed by an {@link EnumModel}</li>
 * </ul></p>
 *
 * @author Matthias Mann
 */
public class ToggleButton extends Button {

    public ToggleButton() {
        super(new ToggleButtonModel());
    }

    public ToggleButton(BooleanModel model) {
        super(new ToggleButtonModel(model));
    }
    
    public ToggleButton(String text) {
        this();
        setText(text);
    }

    public void setModel(BooleanModel model) {
        ((ToggleButtonModel)getModel()).setModel(model);
    }
    
    public boolean isActive() {
        return getModel().isSelected();
    }
    
    public void setActive(boolean  active) {
        getModel().setSelected(active);
    }
}
