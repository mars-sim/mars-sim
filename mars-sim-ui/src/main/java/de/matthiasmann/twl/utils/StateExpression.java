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
package de.matthiasmann.twl.utils;

import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * A class to handle animation state expression
 * 
 * @author Matthias Mann
 */
public abstract class StateExpression {

    public abstract boolean evaluate(AnimationState as);

    public static StateExpression parse(String exp, boolean negate) throws ParseException {
        StringIterator si = new StringIterator(exp);
        StateExpression expr = parse(si);
        if(si.hasMore()) {
            si.unexpected();
        }
        expr.negate ^= negate;
        return expr;
    }

    private static StateExpression parse(StringIterator si) throws ParseException {
        ArrayList<StateExpression> children = new ArrayList<StateExpression>();
        char kind = ' ';
        
        for(;;) {
            if(!si.skipSpaces()) {
                si.unexpected();
            }
            char ch = si.peek();
            boolean negate = ch == '!';
            if(negate) {
                si.pos++;
                if(!si.skipSpaces()) {
                    si.unexpected();
                }
                ch = si.peek();
            }

            StateExpression child = null;
            if(Character.isJavaIdentifierStart(ch)) {
                child = new Check(StateKey.get(si.getIdent()));
            } else if(ch == '(') {
                si.pos++;
                child = parse(si);
                si.expect(')');
            } else if(ch == ')') {
                break;
            } else {
                si.unexpected();
            }

            child.negate = negate;
            children.add(child);

            if(!si.skipSpaces()) {
                break;
            }

            ch = si.peek();
            if("|+^".indexOf(ch) < 0) {
                break;
            }

            if(children.size() == 1) {
                kind = ch;
            } else if(kind != ch) {
                si.expect(kind);
            }
            si.pos++;
        }

        if(children.isEmpty()) {
            si.unexpected();
        }
        
        assert kind != ' ' || children.size() == 1;

        if(children.size() == 1) {
            return children.get(0);
        }
        
        return new Logic(kind, children.toArray(new StateExpression[children.size()]));
    }

    private static class StringIterator {
        final String str;
        int pos;

        StringIterator(String str) {
            this.str = str;
        }

        boolean hasMore() {
            return pos < str.length();
        }

        char peek() {
            return str.charAt(pos);
        }

        void expect(char what) throws ParseException {
            if(!hasMore() || peek() != what) {
                throw new ParseException("Expected '"+what+"' got " + describePosition(), pos);
            }
            pos++;
        }

        void unexpected() throws ParseException {
            throw new ParseException("Unexpected " + describePosition(), pos);
        }

        String describePosition() {
            if(pos >= str.length()) {
                return "end of expression";
            }
            return "'"+peek()+"' at " + (pos+1);
        }

        boolean skipSpaces() {
            while(hasMore() && Character.isWhitespace(peek())) {
                pos++;
            }
            return hasMore();
        }

        String getIdent() {
            int start = pos;
            while(hasMore() && Character.isJavaIdentifierPart(peek())) {
                pos++;
            }
            // intern string for faster HashMap lookup
            return str.substring(start, pos).intern();
        }
    }

    StateExpression() {
    }

    abstract void getUsedStateKeys(BitSet bs);
    
    boolean negate;

    public static class Logic extends StateExpression {
        final StateExpression[] children;
        final boolean and;
        final boolean xor;
        
        public Logic(char kind, StateExpression ... children) {
            if(kind != '|' && kind != '+' && kind != '^') {
                throw new IllegalArgumentException("kind");
            }
            this.children = children;
            this.and = kind == '+';
            this.xor = kind == '^';
        }

        @Override
        public boolean evaluate(AnimationState as) {
            boolean result = and ^ negate;
            for(StateExpression e : children) {
                boolean value = e.evaluate(as);
                if(xor) {
                    result ^= value;
                } else if(and != value) {
                    return result ^ true;
                }
            }
            return result;
        }

        @Override
        void getUsedStateKeys(BitSet bs) {
            for(StateExpression e : children) {
                e.getUsedStateKeys(bs);
            }
        }
    }
    
    public static class Check extends StateExpression {
        final StateKey state;

        public Check(StateKey state) {
            this.state = state;
        }

        @Override
        public boolean evaluate(AnimationState as) {
            return negate ^ (as != null && as.getAnimationState(state));
        }

        @Override
        void getUsedStateKeys(BitSet bs) {
            bs.set(state.getID());
        }
    }
}
