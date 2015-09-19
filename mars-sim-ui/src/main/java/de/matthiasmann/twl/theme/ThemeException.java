/*
 * Copyright (c) 2008-2012, Matthias Mann
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
package de.matthiasmann.twl.theme;

import java.io.IOException;
import java.net.URL;

/**
 * This exception is thrown when a theme file could not be parsed.
 * 
 * @author Matthias Mann
 */
public class ThemeException extends IOException {

    protected final Source source;
    
    public ThemeException(String msg, URL url, int lineNumber, int columnNumber, Throwable cause) {
        super(msg);
        this.source = new Source(url, lineNumber, columnNumber);
        initCause(cause);
    }
    
    void addIncludedBy(URL url, int lineNumber, int columnNumber) {
        Source head = source;
        while(head.includedBy != null) {
            head = head.includedBy;
        }
        head.includedBy = new Source(url, lineNumber, columnNumber);
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        String prefix = "\n           in ";
        for(Source src=source ; src!=null ; src=src.includedBy) {
            sb.append(prefix).append(src.url)
                    .append(" @").append(src.lineNumber)
                    .append(':').append(src.columnNumber);
            prefix = "\n  included by ";
        }
        return sb.toString();
    }

    /**
     * Returns the source URL of the XML file and the line/column number
     * where the exception originated.
     * @return the source
     */
    public Source getSource() {
        return source;
    }
    
    /**
     * Describes a position in an XML file
     */
    public static final class Source {
        protected final URL url;
        protected final int lineNumber;
        protected final int columnNumber;
        protected Source includedBy;

        Source(URL url, int lineNumber, int columnNumber) {
            this.url = url;
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }

        public URL getUrl() {
            return url;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public int getColumnNumber() {
            return columnNumber;
        }

        public Source getIncludedBy() {
            return includedBy;
        }
    }
}
