/*
 * Copyright (c) 2008-2009, Matthias Mann
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

import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.textarea.SimpleTextAreaModel;

/**
 *
 * @author Matthias Mann
 */
public class TextAreaDemoDialog2 extends FadeFrame {

    public TextAreaDemoDialog2()  {
        SimpleTextAreaModel tam = new SimpleTextAreaModel();
        tam.setText("This is a small test message. It's not too long.\n" +
                "\tThis is a small test message. It's not too long.\n" +
                "This\tis a small test message. It's not too long.\n" +
                "This is\ta small test message. It's not too long.\n" +
                "This is a\tsmall test message. It's not too long.\n" +
                "This is a small\ttest message. It's not too long.\n" +
                "This is a small test\tmessage. It's not too long.\n" +
                "This is a small test message.\tIt's not too long.\n" +
                "This is a small test message. It's\tnot too long.\n" +
                "This is a small test message. It's not\ttoo long.\n" +
                "This is a small test message. It's not too\tlong.");

        TextArea scrolledWidget2 = new TextArea(tam);
        scrolledWidget2.setTheme("textarea");

        ScrollPane scrollPane2 = new ScrollPane(scrolledWidget2);
        scrollPane2.setTheme("scrollpane");
        scrollPane2.setFixed(ScrollPane.Fixed.HORIZONTAL);

        setTheme("textAreaTestFrame");
        setTitle("TextArea tab test");
        add(scrollPane2);
    }
    
}
