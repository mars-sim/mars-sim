/**
 * Mars Simulation Project
 * AlwaysEmptySet.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.memory;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;

public class AlwaysEmptySet<T> implements Set<T> {

    @SuppressWarnings("rawtypes")
	public static final Set EMPTY_SET = new AlwaysEmptySet<>();

    private AlwaysEmptySet() {
    }

    @SuppressWarnings("unchecked")
	public static <T> Set<T> create() {
    	return EMPTY_SET;
    }

    public static <T> Callable<Set<T>> provider() {
        return new Callable<Set<T>>() {
            public Set<T> call() throws Exception {
                return create();
            }
        };
    }

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return true;
    }

    public boolean contains(Object o) {
        return false;
    }

    public Iterator<T> iterator() {
        return Collections.<T>emptySet().iterator();
    }

    public Object[] toArray() {
        return new Object[0];
    }

    @SuppressWarnings("unchecked")
	public <K> K[] toArray(K[] a) {
        return (K[]) Collections.emptySet().toArray();
    }

    public boolean add(T t) {
        return false;
    }

    public boolean remove(Object o) {
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        return false;
    }

    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    public boolean retainAll(Collection<?> c) {
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        return false;
    }

    public void clear() {
    }
}
