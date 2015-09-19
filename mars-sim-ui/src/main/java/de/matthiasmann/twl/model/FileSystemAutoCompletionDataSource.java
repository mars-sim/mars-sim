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

import de.matthiasmann.twl.model.FileSystemModel.FileFilter;
import java.util.ArrayList;

/**
 * Provides auto completion on a FileSystemModel
 * 
 * @author Matthias Mann
 */
public class FileSystemAutoCompletionDataSource implements AutoCompletionDataSource {

    final FileSystemModel fsm;
    final FileSystemModel.FileFilter fileFilter;

    public FileSystemAutoCompletionDataSource(FileSystemModel fsm, FileFilter fileFilter) {
        if(fsm == null) {
            throw new NullPointerException("fsm");
        }
        
        this.fsm = fsm;
        this.fileFilter = fileFilter;
    }

    public FileSystemModel getFileSystemModel() {
        return fsm;
    }

    public FileFilter getFileFilter() {
        return fileFilter;
    }

    public AutoCompletionResult collectSuggestions(String text, int cursorPos, AutoCompletionResult prev) {
        text = text.substring(0, cursorPos);
        int prefixLength = computePrefixLength(text);
        String prefix = text.substring(0, prefixLength);
        Object parent;

        if((prev instanceof Result) &&
                prev.getPrefixLength() == prefixLength &&
                prev.getText().startsWith(prefix)) {
            parent = ((Result)prev).parent;
        } else {
            parent = fsm.getFile(prefix);
        }

        if(parent == null) {
            return null;
        }

        Result result = new Result(text, prefixLength, parent);
        fsm.listFolder(parent, result);

        if(result.getNumResults() == 0) {
            return null;
        }

        return result;
    }
    
    int computePrefixLength(String text) {
        String separator = fsm.getSeparator();
        int prefixLength = text.lastIndexOf(separator) + separator.length();
        if(prefixLength < 0) {
            prefixLength = 0;
        }
        return prefixLength;
    }

    class Result extends AutoCompletionResult implements FileSystemModel.FileFilter {
        final Object parent;
        final String nameFilter;

        final ArrayList<String> results1 = new ArrayList<String>();
        final ArrayList<String> results2 = new ArrayList<String>();

        public Result(String text, int prefixLength, Object parent) {
            super(text, prefixLength);
            this.parent = parent;
            this.nameFilter = text.substring(prefixLength).toUpperCase();
        }

        public boolean accept(FileSystemModel fsm, Object file) {
            FileSystemModel.FileFilter ff = fileFilter;
            if(ff == null || ff.accept(fsm, file)) {
                int idx = getMatchIndex(fsm.getName(file));
                if(idx >= 0) {
                    addName(fsm.getPath(file), idx);
                }
            }
            return false;
        }

        private int getMatchIndex(String partName) {
            return partName.toUpperCase().indexOf(nameFilter);
        }
        private void addName(String fullName, int matchIdx) {
            if(matchIdx == 0) {
                results1.add(fullName);
            } else if(matchIdx > 0) {
                results2.add(fullName);
            }
        }

        private void addFiltedNames(ArrayList<String> results) {
            for(int i=0,n=results.size() ; i<n ; i++) {
                String fullName = results.get(i);
                int idx = getMatchIndex(fullName.substring(prefixLength));
                addName(fullName, idx);
            }
        }

        @Override
        public int getNumResults() {
            return results1.size() + results2.size();
        }

        @Override
        public String getResult(int idx) {
            int size1 = results1.size();
            if(idx >= size1) {
                return results2.get(idx - size1);
            } else {
                return results1.get(idx);
            }
        }

        boolean canRefine(String text) {
            return prefixLength == computePrefixLength(text) && text.startsWith(this.text);
        }

        @Override
        public AutoCompletionResult refine(String text, int cursorPos) {
            text = text.substring(0, cursorPos);
            if(canRefine(text)) {
                Result result = new Result(text, prefixLength, parent);
                result.addFiltedNames(results1);
                result.addFiltedNames(results2);
                return result;
            }
            return null;
        }
    }
}
