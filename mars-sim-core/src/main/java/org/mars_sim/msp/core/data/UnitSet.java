/*
 * Mars Simulation Project
 * UnitSet.java
 * @date 2021-10-28
 * @author Barry Evans
 */
package org.mars_sim.msp.core.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;

/**
 * This class holds a set of Unit classes. It has a customised serialization 
 * format that only stores the Unit identifiers. The Unit references are restored
 * after deserialisation via the reinit method.
 * @param <T> Unit subclass.
 */
public class UnitSet<T extends Unit> 
	implements Set<T>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	 
    // The list of UnitSet that have been deserialised and need initialising
	@SuppressWarnings("rawtypes")
	private static final List<UnitSet> INSTANCES = new ArrayList<>();
	
	// The set of Unit referneces
	private transient Set<T> units;
	
	// This is the transient unit identifiers only used in the readObject method 
	private transient int[] ids = null;

	
	public UnitSet() {
		super();
		
		this.units = new HashSet<>();
	}
    
	public void forEach(Consumer<? super T> action) {
		units.forEach(action);
	}

	public int size() {
		return units.size();
	}

	public boolean isEmpty() {
		return units.isEmpty();
	}

	public boolean contains(Object o) {
		return units.contains(o);
	}

	public Iterator<T> iterator() {
		return units.iterator();
	}

	public Object[] toArray() {
		return units.toArray();
	}

	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] a) {
		return units.toArray(a);
	}

	public boolean add(T e) {
		return units.add(e);
	}

	public boolean remove(Object o) {
		return units.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return units.containsAll(c);
	}

	public boolean addAll(Collection<? extends T> c) {
		return units.addAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return units.retainAll(c);
	}

	public boolean removeAll(Collection<?> c) {
		return units.removeAll(c);
	}

	@SuppressWarnings("hiding")
	public <T> T[] toArray(IntFunction<T[]> generator) {
		return units.toArray(generator);
	}

	public void clear() {
		units.clear();
	}

	public boolean equals(Object o) {
		return units.equals(o);
	}

	public int hashCode() {
		return units.hashCode();
	}

	public Spliterator<T> spliterator() {
		return units.spliterator();
	}

	public boolean removeIf(Predicate<? super T> filter) {
		return units.removeIf(filter);
	}

	public Stream<T> stream() {
		return units.stream();
	}

	public Stream<T> parallelStream() {
		return units.parallelStream();
	}
	
	/**
	 * Read the object which is represented as an array of ints of the Unit
	 * identifiers. This are held in a transient array until the reinit
	 * method is called.
	 * @param ois
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
    private void readObject(ObjectInputStream ois) 
      throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        ids = (int []) ois.readObject();
        
        registerInitialisation(this);
    }
    
	/**
	 * Serialise this UnitSet as an array of ints which are the Unit Identifiers
	 * of the set members
	 * @param oos
	 * @throws IOException
	 */
    private void writeObject(ObjectOutputStream oos) 
	      throws IOException {
        oos.defaultWriteObject();
        int [] heldIDs = new int[units.size()];
        int i = 0;
        for(Unit u : units) {
        	heldIDs[i++] = u.getIdentifier();
        }
        oos.writeObject(heldIDs);
	}

	/**
	 * Register an instance to the global post-serialisation initialisation list.
	 * @param i
	 */
	@SuppressWarnings("rawtypes")
	private static void registerInitialisation(UnitSet i) {
		INSTANCES.add(i);
	}
	
	/**
	 * Reinitialise any deserialised UnitSets; this turns the Unit ids into
	 * Unit references
	 * @param mgr
	 */
	@SuppressWarnings("rawtypes")
	public static void reinit(UnitManager mgr) {
		for (UnitSet i : INSTANCES) {
			i.reload(mgr);
		}
		INSTANCES.clear();
	}
    	
    /**
     * Convert the temp list of Unit identifiers into the Unit references
     * into the internal set.
     * @param mgr
     */
	@SuppressWarnings("unchecked")
	private void reload(UnitManager mgr) {
		if (units == null) {
			units = new HashSet<>();
			
			for(int id : ids) {
				T found = (T) mgr.getUnitByID(id);
				if (found == null) {
					throw new IllegalStateException("Can not find Unit for id=" + id);
				}
				units.add(found);
			}
			ids = null;
		}
	}
}
