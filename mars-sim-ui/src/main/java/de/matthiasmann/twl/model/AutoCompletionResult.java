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
package de.matthiasmann.twl.model;

import de.matthiasmann.twl.EditField;

/**
 * An abstract container for auto completion results.
 *
 * @author Matthias Mann
 */
public abstract class AutoCompletionResult {

    /**
     * Return value for {@link #getCursorPosForResult} to use EditField's
     * default cursor position for {@link EditField#setText(java.lang.String)}
     */
    public static final int DEFAULT_CURSOR_POS = -1;

    protected final String text;
    protected final int prefixLength;

    public AutoCompletionResult(String text, int prefixLength) {
        this.text = text;
        this.prefixLength = prefixLength;
    }

    /**
     * The prefix length is the length of prefix which was used to collect the data.
     * The remaining part of the text is used for high lighting the results.
     * This is used for things like tree completion.
     *
     * @return the prefix length
     */
    public int getPrefixLength() {
        return prefixLength;
    }

    /**
     * Returns the text which was used for this auto completion
     * @return the text which was used for this auto completion
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the number of results
     * @return the number of results
     */
    public abstract int getNumResults();

    /**
     * Returns a selected result entry
     * @param idx the index of the desired result entry
     * @return the result entry
     * @see #getNumResults()
     */
    public abstract String getResult(int idx);

    /**
     * Returns the desired cursor position for the given result entry.
     * 
     * The default implementation returns {@link #DEFAULT_CURSOR_POS}
     * 
     * @param idx the index of the desired result entry
     * @return the cursor position
     * @see #getNumResults()
     * @see #getResult(int)
     * @see #DEFAULT_CURSOR_POS
     */
    public int getCursorPosForResult(int idx) {
        return DEFAULT_CURSOR_POS;
    }

    /**
     * Tries to refine the results. Refining can result in a different order
     * of results then a new query but is faster.
     *
     * If refining resulted in no results then an empty AutoCompletionResult
     * is returned.
     *
     * @param text The new text
     * @param cursorPos The new cursor position
     * @return the new refined AutoCompletionResult or null if refining was not possible
     */
    public AutoCompletionResult refine(String text, int cursorPos) {
        return null;
    }
}
