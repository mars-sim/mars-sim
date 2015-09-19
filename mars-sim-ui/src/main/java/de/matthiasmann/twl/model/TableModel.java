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

/**
 *
 * @author Matthias Mann
 */
public interface TableModel extends TableColumnHeaderModel {

    public interface ChangeListener extends ColumnHeaderChangeListener {
        /**
         * New rows have been inserted. The existing rows starting at idx
         * have been shifted. The range idx to idx+count-1 (inclusive) are new.
         *
         * @param idx the first new row
         * @param count the number of inserted rows. Must be >= 1.
         */
        public void rowsInserted(int idx, int count);

        /**
         * Rows that were at the range idx to idx+count-1 (inclusive) have been removed.
         * Rows starting at idx+count have been shifted to idx.
         *
         * @param idx the first removed row
         * @param count the number of removed rows. Must be >= 1.
         */
        public void rowsDeleted(int idx, int count);

        /**
         * Rows in the range idx to idx+count-1 (inclusive) have been changed.
         *
         * @param idx the first changed row
         * @param count the number of changed rows. Must be >= 1.
         */
        public void rowsChanged(int idx, int count);

        /**
         * The specified cell has changed
         * @param row the row of the cell
         * @param column the column of the cell
         */
        public void cellChanged(int row, int column);

        /**
         * The complete table was recreated. There is no known relation between
         * old and new rows or columns. Also the number of rows and or columns
         * has changed.
         */
        public void allChanged();
    }

    public int getNumRows();

    public Object getCell(int row, int column);

    public Object getTooltipContent(int row, int column);
    
    public void addChangeListener(ChangeListener listener);

    public void removeChangeListener(ChangeListener listener);
}
