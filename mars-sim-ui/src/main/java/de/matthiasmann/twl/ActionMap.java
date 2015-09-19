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
package de.matthiasmann.twl;

import de.matthiasmann.twl.Event.Type;
import de.matthiasmann.twl.utils.ClassUtils;
import de.matthiasmann.twl.utils.HashEntry;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The action map class implements mappings from action names to methods using
 * reflection.
 *
 * @author Matthias Mann
 */
public class ActionMap {

    /**
     * Invocation flag
     *
     * Invoke the method on the first key pressed event.
     *
     * @see #addMapping(java.lang.String, java.lang.Object, java.lang.reflect.Method, java.lang.Object[], int)
     * @see #FLAG_ON_REPEAT
     */
    public static final int FLAG_ON_PRESSED = 1;

    /**
     * Invocation flag
     *
     * Invoke the method on a key release event.
     *
     * @see #addMapping(java.lang.String, java.lang.Object, java.lang.reflect.Method, java.lang.Object[], int)
     */
    public static final int FLAG_ON_RELEASE = 2;

    /**
     * Invocation flag
     *
     * Invoke the method also on a repeated key pressed event.
     *
     * @see #addMapping(java.lang.String, java.lang.Object, java.lang.reflect.Method, java.lang.Object[], int)
     * @see #FLAG_ON_PRESSED
     */
    public static final int FLAG_ON_REPEAT = 4;

    private Mapping[] mappings;
    private int numMappings;

    public ActionMap() {
        mappings = new Mapping[16];
    }

    /**
     * Invoke the mapping for the given action if one is defined and it's flags
     * match the passed event.
     * 
     * @param action the action name
     * @param event the event which caused the invocation
     * @return true if a mapping was found, false if no mapping was found.
     * @see #addMapping(java.lang.String, java.lang.Object, java.lang.reflect.Method, java.lang.Object[], int)
     * @throws NullPointerException when either action or event is null
     */
    public boolean invoke(String action, Event event) {
        Mapping mapping = HashEntry.get(mappings, action);
        if(mapping != null) {
            mapping.call(event);
            return true;
        }
        return false;
    }
    
    /**
     * Invoke the mapping for the given action if one is defined without
     * checking any flags.
     * 
     * @param action the action name
     * @return true if a mapping was found, false if no mapping was found.
     * @see #addMapping(java.lang.String, java.lang.Object, java.lang.reflect.Method, java.lang.Object[], int)
     * @throws NullPointerException when action is null
     */
    public boolean invokeDirect(String action) {
        Mapping mapping = HashEntry.get(mappings, action);
        if(mapping != null) {
            mapping.call();
            return true;
        }
        return false;
    }

    /**
     * Add an action mapping for the specified action to the given public instance method.
     *
     * Parameters can be passed to the method to differentiate between different
     * actions using the same handler method.
     *
     * NOTE: if multiple methods are compatible to the given parameters then it's
     * undefined which method will be selected. No overload resolution is performed
     * besides a simple parameter compatibility check.
     *
     * @param action the action name
     * @param target the target object
     * @param methodName the method name
     * @param params parameters passed to the method
     * @param flags flags to control on which events the method should be invoked
     * @throws IllegalArgumentException if no matching method was found
     * @throws NullPointerException when {@code action}, {@code target} or {@code params} is null
     * @see ClassUtils#isParamsCompatible(java.lang.Class<?>[], java.lang.Object[])
     * @see #FLAG_ON_PRESSED
     * @see #FLAG_ON_RELEASE
     * @see #FLAG_ON_REPEAT
     */
    public void addMapping(String action, Object target, String methodName, Object[] params, int flags) throws IllegalArgumentException {
        if(action == null) {
            throw new NullPointerException("action");
        }
        for(Method m : target.getClass().getMethods()) {
            if(m.getName().equals(methodName) && !Modifier.isStatic(m.getModifiers())) {
                if(ClassUtils.isParamsCompatible(m.getParameterTypes(), params)) {
                    addMappingImpl(action, target, m, params, flags);
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Can't find matching method: " + methodName);
    }

    /**
     * Add an action mapping for the specified action to the given public static method.
     *
     * Parameters can be passed to the method to differentiate between different
     * actions using the same handler method.
     *
     * NOTE: if multiple methods are compatible to the given parameters then it's
     * undefined which method will be selected. No overload resolution is performed
     * besides a simple parameter compatibility check.
     *
     * @param action the action name
     * @param targetClass the target class
     * @param methodName the method name
     * @param params parameters passed to the method
     * @param flags flags to control on which events the method should be invoked
     * @throws NullPointerException when {@code action}, {@code targetClass} or {@code params} is null
     * @throws IllegalArgumentException if no matching method was found
     * @see ClassUtils#isParamsCompatible(java.lang.Class<?>[], java.lang.Object[])
     * @see #FLAG_ON_PRESSED
     * @see #FLAG_ON_RELEASE
     * @see #FLAG_ON_REPEAT
     */
    public void addMapping(String action, Class<?> targetClass, String methodName, Object[] params, int flags) throws IllegalArgumentException {
        if(action == null) {
            throw new NullPointerException("action");
        }
        for(Method m : targetClass.getMethods()) {
            if(m.getName().equals(methodName) && Modifier.isStatic(m.getModifiers())) {
                if(ClassUtils.isParamsCompatible(m.getParameterTypes(), params)) {
                    addMappingImpl(action, null, m, params, flags);
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Can't find matching method: " + methodName);
    }

    /**
     * Add an action mapping for the specified action to the given method.
     *
     * Parameters can be passed to the method to differentiate between different
     * actions using the same handler method.
     *
     * @param action the action name
     * @param target the target object. Can be null when the method is static
     * @param method the method to invoke
     * @param params the parameters to pass to the method
     * @param flags flags to control on which events the method should be invoked
     * @throws NullPointerException when {@code action}, {@code method} or {@code params} is null
     * @throws IllegalArgumentException <ul>
     *   <li>when the method is not public</li>
     *   <li>when the method does not belong to the target object</li>
     *   <li>when the parameters do not match the arguments</li>
     * </ul>
     * @see ClassUtils#isParamsCompatible(java.lang.Class<?>[], java.lang.Object[])
     * @see #FLAG_ON_PRESSED
     * @see #FLAG_ON_RELEASE
     * @see #FLAG_ON_REPEAT
     */
    public void addMapping(String action, Object target, Method method, Object[] params, int flags) {
        if(action == null) {
            throw new NullPointerException("action");
        }
        if(!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException("Method is not public");
        }
        if(target == null && !Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method is not static but target is null");
        }
        if(target != null && method.getDeclaringClass().isInstance(target)) {
            throw new IllegalArgumentException("method does not belong to target");
        }
        if(!ClassUtils.isParamsCompatible(method.getParameterTypes(), params)) {
            throw new IllegalArgumentException("Paramters don't match method");
        }
        addMappingImpl(action, target, method, params, flags);
    }

    /**
     * Add action mapping for all public methods of the specified class which
     * are annotated with the {@code Action} annotation.
     *
     * @param target the target class
     * @see Action
     */
    public void addMapping(Object target) {
        for(Method m : target.getClass().getMethods()) {
            Action action = m.getAnnotation(Action.class);
            if(action != null) {
                if(m.getParameterTypes().length > 0) {
                    throw new UnsupportedOperationException("automatic binding of actions not supported for methods with parameters");
                }
                String name = m.getName();
                if(action.name().length() > 0) {
                    name = action.name();
                }
                int flags =
                        (action.onPressed() ? FLAG_ON_PRESSED : 0) |
                        (action.onRelease() ? FLAG_ON_RELEASE : 0) |
                        (action.onRepeat() ? FLAG_ON_REPEAT : 0);
                addMappingImpl(name, target, m, null, flags);
            }
        }
    }

    protected void addMappingImpl(String action, Object target, Method method, Object[] params, int flags) {
        mappings = HashEntry.maybeResizeTable(mappings, numMappings++);
        HashEntry.insertEntry(mappings, new Mapping(action, target, method, params, flags));
    }

    /**
     * Annotation used for automatic handler registration
     *
     * @see #addMapping(java.lang.Object)
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Action {
        /**
         * Optional action name. If not specified then the method name is used
         * as action
         * @return the action name
         */
        String name() default "";
        /**
         * Invoke the method on first key press events
         * @return default true
         */
        boolean onPressed() default true;
        /**
         * Invoke the method on key release events
         * @return default false
         */
        boolean onRelease() default false;
        /**
         * Invoke the method also on repeated key press events
         * @return default false
         */
        boolean onRepeat() default true;
    }

    static class Mapping extends HashEntry<String, Mapping> {
        final Object target;
        final Method method;
        final Object[] params;
        final int flags;

        Mapping(String key, Object target, Method method, Object[] params, int flags) {
            super(key);
            this.target = target;
            this.method = method;
            this.params = params;
            this.flags = flags;
        }

        void call(Event e) {
            Type type = e.getType();
            if((type == Event.Type.KEY_RELEASED && ((flags & FLAG_ON_RELEASE) != 0)) ||
                    (type == Event.Type.KEY_PRESSED && ((flags & FLAG_ON_PRESSED) != 0) &&
                    (!e.isKeyRepeated() || ((flags & FLAG_ON_REPEAT) != 0)))) {
                call();
            }
        }
        
        void call() {
            try {
                method.invoke(target, params);
            } catch (Exception ex) {
                Logger.getLogger(ActionMap.class.getName()).log(Level.SEVERE,
                        "Exception while invoking action handler", ex);
            }
        }
    }
}
