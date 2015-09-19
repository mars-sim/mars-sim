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
package mines;

import de.matthiasmann.twl.model.AbstractTableModel;
import java.text.DateFormat;
import java.util.List;

/**
 *
 * @author Matthias Mann
 */
public class HighscoreTableModel extends AbstractTableModel {
    
    private final List<Highscores.Entry> entries;
    private final DateFormat dateFormat;

    public HighscoreTableModel(List<Highscores.Entry> entries) {
        this.entries = entries;
        this.dateFormat = DateFormat.getDateInstance();
    }
    
    public int getNumColumns() {
        return 3;
    }
    
    public String getColumnHeaderText(int column) {
        switch(column) {
            case 0: return "Date";
            case 1: return "Name";
            case 2: return "Time";
            default:
                throw new AssertionError();
        }
    }
    
    public int getNumRows() {
        return entries.size();
    }
    
    public Object getCell(int row, int column) {
        Highscores.Entry entry = entries.get(row);
        switch(column) {
            case 0: return dateFormat.format(entry.date);
            case 1: return entry.name;
            case 2: return String.format("%d:%02d", entry.time/60, entry.time%60);
            default:
                throw new AssertionError();
        }
    }
}
