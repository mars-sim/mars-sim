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
package sourceviewer;

import de.matthiasmann.twl.model.ObservableCharSequence;
import de.matthiasmann.twl.model.ObservableCharSequence.Callback;
import de.matthiasmann.twl.model.StringAttributes;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;

/**
 *
 * @author Matthias Mann
 */
public class StringSyntaxHighlighter {
    
    public static final StateKey STATE_COMMENT     = StateKey.get("comment");
    public static final StateKey STATE_COMMENT_TAG = StateKey.get("commentTag");
    public static final StateKey STATE_KEYWORD     = StateKey.get("keyword");
    public static final StateKey STATE_STRING      = StateKey.get("string");
    private final Callback callback;
    
    private final ObservableCharSequence sequence;
    private final StringAttributes attributes;

    public StringSyntaxHighlighter(ObservableCharSequence sequence, StringAttributes attributes) {
        this.sequence = sequence;
        this.attributes = attributes;
        this.callback = new ObservableCharSequence.Callback() {
            public void charactersChanged(int start, int oldCount, int newCount) {
                doHighlight();
            }
        };
        
        doHighlight();
    }
    
    public void registerCallback() {
        sequence.addCallback(callback);
    }
    
    public void unregisterCallback() {
        sequence.removeCallback(callback);
    }
    
    final void doHighlight() {
        attributes.clearAnimationStates();
        
        JavaScanner js = new JavaScanner(sequence);
        int start = js.getCurrentPosition();
        JavaScanner.Kind kind;
        while((kind=js.scan()) != JavaScanner.Kind.EOF) {
            int pos = js.getCurrentPosition();
            switch(kind) {
                case COMMENT:
                    attributes.setAnimationState(STATE_COMMENT, start, pos, true);
                    break;
                case COMMENT_TAG:
                    attributes.setAnimationState(STATE_COMMENT, start, pos, true);
                    attributes.setAnimationState(STATE_COMMENT_TAG, start, pos, true);
                    break;
                case KEYWORD:
                    attributes.setAnimationState(STATE_KEYWORD, start, pos, true);
                    break;
                case STRING:
                    attributes.setAnimationState(STATE_STRING, start, pos, true);
                    break;
            }
            start = pos;
        }
    }
}
