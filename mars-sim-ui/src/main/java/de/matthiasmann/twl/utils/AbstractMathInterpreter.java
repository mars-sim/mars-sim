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
package de.matthiasmann.twl.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matthias Mann
 */
public abstract class AbstractMathInterpreter implements SimpleMathParser.Interpreter {

    public interface Function {
        public Object execute(Object ... args);
    }
    
    private final ArrayList<Object> stack;
    private final HashMap<String, Function> functions;

    public AbstractMathInterpreter() {
        this.stack = new ArrayList<Object>();
        this.functions = new HashMap<String, Function>();

        registerFunction("min", new FunctionMin());
        registerFunction("max", new FunctionMax());
    }

    public final void registerFunction(String name, Function function) {
        if(function == null) {
            throw new NullPointerException("function");
        }
        functions.put(name, function);
    }

    public Number execute(String str) throws ParseException {
        stack.clear();
        SimpleMathParser.interpret(str, this);
        if(stack.size() != 1) {
            throw new IllegalStateException("Expected one return value on the stack");
        }
        return popNumber();
    }

    public int[] executeIntArray(String str) throws ParseException {
        stack.clear();
        int count = SimpleMathParser.interpretArray(str, this);
        if(stack.size() != count) {
            throw new IllegalStateException("Expected " + count + " return values on the stack");
        }
        int[] result = new int[count];
        for(int i=count ; i-->0 ;) {
            result[i] = popNumber().intValue();
        }
        return result;
    }

    public<T> T executeCreateObject(String str, Class<T> type) throws ParseException {
        stack.clear();
        int count = SimpleMathParser.interpretArray(str, this);
        if(stack.size() != count) {
            throw new IllegalStateException("Expected " + count + " return values on the stack");
        }
        if(count == 1 && type.isInstance(stack.get(0))) {
            return type.cast(stack.get(0));
        }
        for(Constructor<?> c : type.getConstructors()) {
            Class<?>[] params = c.getParameterTypes();
            if(params.length == count) {
                boolean match = true;
                for(int i=0 ; i<count ; i++) {
                    if(!ClassUtils.isParamCompatible(params[i], stack.get(i))) {
                        match = false;
                        break;
                    }
                }
                if(match) {
                    try {
                        return type.cast(c.newInstance(stack.toArray(new Object[count])));
                    } catch (Exception ex) {
                        Logger.getLogger(AbstractMathInterpreter.class.getName()).log(
                                Level.SEVERE, "can't instantiate object", ex);
                    }
                }
            }
        }
        throw new IllegalArgumentException("Can't construct a " + type +
                " from expression: \"" + str + "\"");
    }
    
    protected void push(Object obj) {
        stack.add(obj);
    }

    protected Object pop() {
        int size = stack.size();
        if(size == 0) {
            throw new IllegalStateException("stack underflow");
        }
        return stack.remove(size-1);
    }

    protected Number popNumber() {
        Object obj = pop();
        if(obj instanceof Number) {
            return (Number)obj;
        }
        throw new IllegalStateException("expected number on stack - found: " +
                ((obj != null) ? obj.getClass() : "null"));
    }

    public void loadConst(Number n) {
        push(n);
    }

    public void add() {
        Number b = popNumber();
        Number a = popNumber();
        boolean isFloat = isFloat(a) || isFloat(b);
        if(isFloat) {
            push(a.floatValue() + b.floatValue());
        } else {
            push(a.intValue() + b.intValue());
        }
    }

    public void sub() {
        Number b = popNumber();
        Number a = popNumber();
        boolean isFloat = isFloat(a) || isFloat(b);
        if(isFloat) {
            push(a.floatValue() - b.floatValue());
        } else {
            push(a.intValue() - b.intValue());
        }
    }

    public void mul() {
        Number b = popNumber();
        Number a = popNumber();
        boolean isFloat = isFloat(a) || isFloat(b);
        if(isFloat) {
            push(a.floatValue() * b.floatValue());
        } else {
            push(a.intValue() * b.intValue());
        }
    }

    public void div() {
        Number b = popNumber();
        Number a = popNumber();
        boolean isFloat = isFloat(a) || isFloat(b);
        if(isFloat) {
            if(Math.abs(b.floatValue()) == 0) {
                throw new IllegalStateException("division by zero");
            }
            push(a.floatValue() / b.floatValue());
        } else {
            if(b.intValue() == 0) {
                throw new IllegalStateException("division by zero");
            }
            push(a.intValue() / b.intValue());
        }
    }

    public void negate() {
        Number a = popNumber();
        if(isFloat(a)) {
            push(-a.floatValue());
        } else {
            push(-a.intValue());
        }
    }

    public void accessArray() {
        Number idx = popNumber();
        Object obj = pop();
        if(obj == null) {
            throw new IllegalStateException("null pointer");
        }
        if(!obj.getClass().isArray()) {
            throw new IllegalStateException("array expected");
        }
        try {
            push(Array.get(obj, idx.intValue()));
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalStateException("array index out of bounds", ex);
        }
    }

    public void accessField(String field) {
        Object obj = pop();
        if(obj == null) {
            throw new IllegalStateException("null pointer");
        }
        Object result = accessField(obj, field);
        push(result);
    }

    protected Object accessField(Object obj, String field) {
        Class<? extends Object> clazz = obj.getClass();
        try {
            if(clazz.isArray()) {
                if("length".equals(field)) {
                    return Array.getLength(obj);
                }
            } else {
                Method m = findGetter(clazz, field);
                if(m == null) {
                    for(Class<?> i : clazz.getInterfaces()) {
                        m = findGetter(i, field);
                        if(m != null) {
                            break;
                        }
                    }
                }
                if(m != null) {
                    return m.invoke(obj);
                }
            }
        } catch(Throwable ex) {
            throw new IllegalStateException("error accessing field '"+field+
                        "' of class '"+clazz+"'", ex);
        }
        throw new IllegalStateException("unknown field '"+field+
                    "' of class '"+clazz+"'");
    }

    private static Method findGetter(Class<?> clazz, String field) {
        for(Method m : clazz.getMethods()) {
            if(!Modifier.isStatic(m.getModifiers()) &&
                    m.getReturnType() != Void.TYPE &&
                    Modifier.isPublic(m.getDeclaringClass().getModifiers()) &&
                    m.getParameterTypes().length == 0 &&
                    (cmpName(m, field, "get") || cmpName(m, field, "is"))) {
                return m;
            }
        }
        return null;
    }
    
    private static boolean cmpName(Method m, String fieldName, String prefix) {
        String methodName = m.getName();
        int prefixLength = prefix.length();
        int fieldNameLength = fieldName.length();
        return methodName.length() == prefixLength + fieldNameLength &&
                methodName.startsWith(prefix) &&
                methodName.charAt(prefixLength) == Character.toUpperCase(fieldName.charAt(0)) &&
                methodName.regionMatches(prefixLength+1, fieldName, 1, fieldNameLength-1);
    }
    
    public void callFunction(String name, int args) {
        Object[] values = new Object[args];
        for(int i=args ; i-->0 ;) {
            values[i] = pop();
        }
        Function function = functions.get(name);
        if(function == null) {
            throw new IllegalArgumentException("Unknown function");
        }
        push(function.execute(values));
    }

    protected static boolean isFloat(Number n) {
        return !(n instanceof Integer);
    }

    public abstract static class NumberFunction implements Function {
        protected abstract Object execute(int ... values);
        protected abstract Object execute(float ... values);

        public Object execute(Object... args) {
            for(Object o : args) {
                if(!(o instanceof Integer)) {
                    float[] values = new float[args.length];
                    for(int i=0 ; i<values.length ; i++) {
                        values[i] = ((Number)args[i]).floatValue();
                    }
                    return execute(values);
                }
            }
            int[] values = new int[args.length];
            for(int i=0 ; i<values.length ; i++) {
                values[i] = ((Number)args[i]).intValue();
            }
            return execute(values);
        }
    }

    static class FunctionMin extends NumberFunction {
        @Override
        protected Object execute(int... values) {
            int result = values[0];
            for(int i=1 ; i<values.length ; i++) {
                result = Math.min(result, values[i]);
            }
            return result;
        }
        @Override
        protected Object execute(float... values) {
            float result = values[0];
            for(int i=1 ; i<values.length ; i++) {
                result = Math.min(result, values[i]);
            }
            return result;
        }
    }

    static class FunctionMax extends NumberFunction {
        @Override
        protected Object execute(int... values) {
            int result = values[0];
            for(int i=1 ; i<values.length ; i++) {
                result = Math.max(result, values[i]);
            }
            return result;
        }
        @Override
        protected Object execute(float... values) {
            float result = values[0];
            for(int i=1 ; i<values.length ; i++) {
                result = Math.max(result, values[i]);
            }
            return result;
        }
    }

}
