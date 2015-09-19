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
package de.matthiasmann.twl.model;

import de.matthiasmann.twl.renderer.AnimationState.StateKey;

/**
 *
 * @author Matthias Mann
 */
public interface TableColumnHeaderModel {

    public interface ColumnHeaderChangeListener {
        /**
         * New columns have been inserted. The existing columns starting at idx
         * have been shifted. The range idx to idx+count-1 (inclusive) are new.
         *
         * @param idx the first new column
         * @param count the number of inserted columns. Must be >= 1.
         */
        public void columnInserted(int idx, int count);

        /**
         * Columns that were at the range idx to idx+count-1 (inclusive) have been removed.
         * Columns starting at idx+count have been shifted to idx.
         *
         * @param idx the first removed column
         * @param count the number of removed columns. Must be >= 1.
         */
        public void columnDeleted(int idx, int count);

        /**
         * A column header has changed it's text or state
         *
         * @param column the column index
         */
        public void columnHeaderChanged(int column);
    }

    public int getNumColumns();

    /**
     * A array of state keys for column headers. This array should not change.
     *
     * @return A StateKey array specifying column header states.
     * @see de.matthiasmann.twl.renderer.AnimationState
     */
    public StateKey[] getColumnHeaderStates();

    /**
     * The text of the specified column header.
     * @param column the column index
     * @return the column header test
     */
    public String getColumnHeaderText(int column);

    /**
     * Returns the value of the specified column header and state.
     *
     * @param column the column index
     * @param stateIdx the state index into the getColumnHeaderStates() array.
     * @return the value of the state
     * @see #getColumnHeaderStates()
     */
    public boolean getColumnHeaderState(int column, int stateIdx);
}
