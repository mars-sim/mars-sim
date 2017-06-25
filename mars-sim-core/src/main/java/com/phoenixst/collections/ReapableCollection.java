/*
 *  $Id: ReapableCollection.java,v 1.14 2006/06/19 19:12:25 rconner Exp $
 *
 *  Copyright (C) 1994-2006 by Phoenix Software Technologists,
 *  Inc. and others.  All rights reserved.
 *
 *  THIS PROGRAM AND DOCUMENTATION IS PROVIDED UNDER THE TERMS OF THE
 *  COMMON PUBLIC LICENSE ("AGREEMENT") WHICH ACCOMPANIES IT.  ANY
 *  USE, REPRODUCTION OR DISTRIBUTION OF THE PROGRAM CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THE AGREEMENT.
 *
 *  The license text can also be found at
 *    http://opensource.org/licenses/cpl.php
 */

package com.phoenixst.collections;

import java.lang.ref.Reference;
import java.util.*;

import org.apache.log4j.Logger;


/**
 *  A reapable <code>Collection</code>.  At any time, elements of an
 *  instance of this class may be removed if there are no longer any
 *  references to them outside of the instance.  In particular, many
 *  methods like {@link #size()} and {@link #isEmpty()} may not give
 *  accurate answers, and that information is immediately out of date.
 *  Instances of this class are synchronized where necessary, but the
 *  user is <strong>required</strong> to synchronize externally during
 *  any iteration.
 *
 *  <P>This <code>Collection</code> does not permit <code>null</code>
 *  elements, nor will it allow itself to be added.
 *
 *  <P>Most of the methods in this class use reference equality rather
 *  than <code>.equals()</code>, given that the intent of this class
 *  is to track actual Object instances.  However, the methods {@link
 *  #removeAll(Collection)} and {@link #retainAll(Collection)} use the
 *  <code>contains()</code> method of the argument
 *  <code>Collection<code>.
 *
 *  @version    $Revision: 1.14 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class ReapableCollection
    implements Collection,
               Reapable
{

    /**
     *  The default logger.
     */
    private static final Logger LOGGER = Logger.getLogger( ReapableCollection.class );

    /**
     *  The minimum size of the internal Reference array.
     */
    private static final int MIN_CAPACITY = 16;

    /**
     *  The Reaper responsible for reaping this instance.
     */
    private final Reaper reaper;

    /**
     *  The array containing the References; any element may have a
     *  cleared referent, or may even be null.
     */
    Reference[] data = new Reference[ MIN_CAPACITY ];

    /**
     *  The size of the portion of the Reference array that is
     *  currently in use.
     */
    int size = 0;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>ReapableCollection</code> using the
     *  default {@link Reaper}.
     */
    public ReapableCollection()
    {
        this( RunnableReaper.DEFAULT_INSTANCE );
    }


    /**
     *  Creates a new <code>ReapableCollection</code> with the
     *  specified {@link Reaper}.  The <code>Reaper</code> must not
     *  create <code>PhantomReferences</code>.
     */
    public ReapableCollection( Reaper reaper )
    {
        super();
        this.reaper = reaper;
    }


    ////////////////////////////////////////
    // Maintenance
    ////////////////////////////////////////


    /**
     *  Applies the visitor to each uncleared Reference in this
     *  collection, and simultaneously collapses the uncleared refs
     *  to the front of the internal array.
     */
    private synchronized void visit( Visitor visitor )
    {
        // Compress valid references while applying the visitor.

        int dest = 0;

        for( int src = 0; src < size; src++ ) {

            Reference ref = data[src];
            data[src] = null;

            if( ref == null ) {
                continue;
            }

            // Keep a strong reference until the visitor is done with it.
            Object referent = ref.get();
            if( referent == null ) {
                continue;
            }

            if( visitor.visit( ref ) ) {
                visitor = NullVisitor.INSTANCE;
            }
            referent = null;
            // Store the reference if the visitor didn't clear it.
            if( ref.get() != null ) {
                data[dest++] = ref;
            }
        }

        if( size != dest && LOGGER.isDebugEnabled() ) {
            int percent = (int) ((100.0 * dest) / size);
            StringBuilder s = new StringBuilder( "Size " );
            s.append( size );
            s.append( " -> " );
            s.append( dest );
            s.append( " (" );
            s.append( percent );
            s.append( "%)" );
            LOGGER.debug( s );
        }

        size = dest;
    }


    /**
     *  Collapses the uncleared References to the front of the
     *  internal array, then possibly reallocates the array so that it
     *  can hold the specified number of additional elements.  This
     *  method should always be called within a synchronized block.
     */
    private void ensureCapacity( int numAdditional )
    {
        // Compress the current elements in-place
        visit( NullVisitor.INSTANCE );

        // Maybe change the capacity.  There are 3 Cases here, but the
        // first two are handled by the same code.
        //
        // 1) 4 * desired size <= capacity
        //      ==> trim capacity to the smallest power of 2 at least
        //          as large as the desired size, min of MIN_CAPACITY
        // 2) desired size > capacity
        //      ==> expand capacity to the smallest power of 2 at least
        //          as large as the desired size
        // 3) else
        //      ==> do nothing
        //
        // The factor of 4 for case #1 gives a little leeway for the
        // number of elements to fluctuate.  Basically, if the number
        // of active elements remains between 1/4 and the full
        // capacity, nothing is changed.

        int oldCapacity = data.length;
        int newSize = size + numAdditional;
        if( 4 * newSize > oldCapacity && newSize <= oldCapacity ) {
            return;
        }

        int newCapacity = Math.max( MIN_CAPACITY,
                                    2 * Integer.highestOneBit( newSize - 1 ) );
        Object oldData[] = data;
        data = new Reference[ newCapacity ];
        System.arraycopy( oldData, 0, data, 0, size );

        if( LOGGER.isDebugEnabled() ) {
            StringBuilder s = new StringBuilder( "Capacity " );
            if( oldCapacity < newCapacity ) {
                s.append( newCapacity / oldCapacity );
                s.append( "x" );
            } else {
                s.append( "/" );
                s.append( oldCapacity / newCapacity );
            }
            s.append( " -> " );
            s.append( newCapacity );
            LOGGER.debug( s );
        }
    }


    ////////////////////////////////////////
    // Collection
    ////////////////////////////////////////


    /**
     *  Returns at most the number of uncleared References currently
     *  in this collection.
     */
    public int size()
    {
        return size;
    }


    /**
     *  Returns <code>true</code> if this collection contains no
     *  elements; a <code>false</code> return value doesn't signify
     *  anything meaningful.
     */
    public boolean isEmpty()
    {
        return size == 0;
    }


    /**
     *  Adds the specified element to this collection, wrapping the
     *  element with a <code>Reference</code> created by the {@link
     *  Reaper} used to construct this collection.  This
     *  <code>Collection</code> does not permit <code>null</code>
     *  elements, nor will it allow itself to be added.
     */
    public boolean add( Object object )
    {
        if( object == null ) {
            throw new IllegalArgumentException( "Null elements are not permitted." );
        }
        if( object == this ) {
            throw new IllegalArgumentException( "A ReapableCollection cannot be added to itself." );
        }
        Reference ref = reaper.createReference( this, object );
        synchronized( this ) {
            if( size >= data.length ) {
                ensureCapacity( 1 );
            }
            data[size++] = ref;
        }
        return true;
    }


    /**
     *  If this collection contains a Reference for the specified
     *  object, the first such Reference found is cleared and
     *  <code>true</code> is returned.
     */
    public boolean remove( Object object )
    {
        if( object == null ) {
            return false;
        }
        RemoveVisitor visitor = new RemoveVisitor( object );
        visit( visitor );
        return visitor.found;
    }


    /**
     *  Returns true if this collection contains a Reference for the
     *  specified object.
     */
    public boolean contains( Object object )
    {
        if( object == null ) {
            return false;
        }
        ContainsVisitor visitor = new ContainsVisitor( object );
        visit( visitor );
        return visitor.found;
    }


    /**
     *  Returns an <code>Iterator</code> over the elements in this
     *  collection, which are the referents of uncleared References.
     *  The user <strong>must</strong> externally synchronize the
     *  <code>Iterator</code> returned by this method!
     */
    public Iterator iterator()
    {
        return new IteratorImpl();
    }


    public synchronized void clear()
    {
        data = new Reference[ MIN_CAPACITY ];
    }


    /**
     *  Adds all of the elements in the specified collection to this
     *  collection, wrapping each element with a
     *  <code>Reference</code> created by the {@link Reaper} used to
     *  construct this collection.  This <code>Collection</code> does
     *  not permit <code>null</code> elements, nor will it allow
     *  itself to be added.  If the argument collection is this
     *  collection, nothing happens and <code>false</code> is
     *  returned.
     */
    public boolean addAll( Collection collection )
    {
        if( collection == this ) {
            return false;
        }
        int otherSize = collection.size();
        if( otherSize == 0 ) {
            return false;
        }
        synchronized( this ) {
            if( size + otherSize > data.length ) {
                ensureCapacity( otherSize );
            }
            for( Iterator i = collection.iterator(); i.hasNext(); ) {
                Object object = i.next();
                if( object == null ) {
                    throw new IllegalArgumentException( "Null elements are not permitted." );
                }
                if( object == this ) {
                    throw new IllegalArgumentException( "A ReapableCollection cannot be added to itself." );
                }
                Reference ref = reaper.createReference( this, object );
                data[size++] = ref;
            }
        }
        return true;
    }


    /**
     *  Removes all this collection's elements that are also contained
     *  in the specified collection.  Unlike most other methods in
     *  this class, this method uses the definition of element
     *  equality defined by the argument collection.  This
     *  implementation is O(n*m), where <em>n</em> is the size of this
     *  collection and <em>m</em> is the cost of executing the
     *  <code>contains()</code> method of the argument collection.
     */
    public boolean removeAll( Collection collection )
    {
        if( collection == this ) {
            if( size == 0 ) {
                return false;
            }
            clear();
            return true;
        }
        if( collection.isEmpty() ) {
            return false;
        }
        RemoveAllVisitor visitor = new RemoveAllVisitor( collection );
        visit( visitor );
        return visitor.modified;
    }


    /**
     *  Retains only this collection's elements that are also
     *  contained in the specified collection.  Unlike most other
     *  methods in this class, this method uses the definition of
     *  element equality defined by the argument collection.  This
     *  implementation is O(n*m), where <em>n</em> is the size of this
     *  collection and <em>m</em> is the cost of executing the
     *  <code>contains()</code> method of the argument collection.
     */
    public boolean retainAll( Collection collection )
    {
        if( collection == this ) {
            return false;
        }
        if( collection.isEmpty() ) {
            clear();
            return true;
        }
        RetainAllVisitor visitor = new RetainAllVisitor( collection );
        visit( visitor );
        return visitor.modified;
    }


    /**
     *  Returns <code>true</code> if this collection contains
     *  References for all of the elements in the specified
     *  collection.  This implementation is O(n*m), where <em>n</em>
     *  is the size of this collection and <em>m</em> is the cost of
     *  iterating over the argument collection.
     */
    public boolean containsAll( Collection collection )
    {
        if( collection == this ) {
            return true;
        }
        for( Iterator i = collection.iterator(); i.hasNext(); ) {
            if( !contains( i.next() ) ) {
                return false;
            }
        }
        return true;
    }


    /**
     *  Returns an array containing all of the elements in this
     *  collection.  Note that this creates strong references to all
     *  elements, and will therefore prevent them from being garbage
     *  collected.
     */
    public Object[] toArray()
    {
        CollectVisitor visitor = new CollectVisitor();
        visit( visitor );
        return visitor.objects.toArray();
    }


    /**
     *  Returns an array containing all of the elements in this
     *  collection.  Note that this creates strong references to all
     *  elements, and will therefore prevent them from being garbage
     *  collected.
     */
    public Object[] toArray( Object[] array )
    {
        CollectVisitor visitor = new CollectVisitor();
        visit( visitor );
        return visitor.objects.toArray( array );
    }


    public String toString()
    {
        StringVisitor visitor = new StringVisitor();
        visit( visitor );
        return visitor.toString();
    }


    ////////////////////////////////////////
    // Reapable
    ////////////////////////////////////////


    public synchronized void reap()
    {
        ensureCapacity( 0 );
    }


    ////////////////////////////////////////
    // Private Visitor interface and classes
    ////////////////////////////////////////


    /**
     *  An interface for a simple visitor pattern, without the
     *  double-dispatch since it isn't necessary here (or even
     *  possible).
     */
    private interface Visitor
    {
        /**
         *  Visits the given reference.  If the visitor wants to
         *  remove the reference from the collection, it should clear
         *  it.  If the visitor wants the visiting process to no
         *  longer call it, it should return true.
         */
        public boolean visit( Reference ref );
    }


    private static class NullVisitor
        implements Visitor
    {
        static final Visitor INSTANCE = new NullVisitor();

        private NullVisitor()
        {
            super();
        }

        public boolean visit( Reference ref )
        {
            return false;
        }
    }


    private static class RemoveVisitor
        implements Visitor
    {
        private final Object object;
        boolean found = false;

        RemoveVisitor( Object object )
        {
            super();
            this.object = object;
        }

        public boolean visit( Reference ref )
        {
            if( ref.get() != object ) {
                return false;
            }
            found = true;
            ref.clear();
            return true;
        }
    }


    private static class ContainsVisitor
        implements Visitor
    {
        private final Object object;
        boolean found = false;

        ContainsVisitor( Object object )
        {
            super();
            this.object = object;
        }

        public boolean visit( Reference ref )
        {
            if( ref.get() != object ) {
                return false;
            }
            found = true;
            return true;
        }
    }


    private static class RemoveAllVisitor
        implements Visitor
    {
        private final Collection collection;
        boolean modified = false;

        RemoveAllVisitor( Collection collection )
        {
            super();
            this.collection = collection;
        }

        public boolean visit( Reference ref )
        {
            if( collection.contains( ref.get() ) ) {
                modified = true;
                ref.clear();
            }
            return false;
        }
    }


    private static class RetainAllVisitor
        implements Visitor
    {
        private final Collection collection;
        boolean modified = false;

        RetainAllVisitor( Collection collection )
        {
            super();
            this.collection = collection;
        }

        public boolean visit( Reference ref )
        {
            if( !collection.contains( ref.get() ) ) {
                modified = true;
                ref.clear();
            }
            return false;
        }
    }


    /**
     *  This creates strong references to everything!
     */
    private static class CollectVisitor
        implements Visitor
    {
        final List objects = new ArrayList();

        CollectVisitor()
        {
            super();
        }

        public boolean visit( Reference ref )
        {
            objects.add( ref.get() );
            return false;
        }
    }


    private static class StringVisitor
        implements Visitor
    {
        private final StringBuilder s = new StringBuilder();
        private boolean hasStarted = false;

        StringVisitor()
        {
            super();
            s.append( "[" );
        }

        public boolean visit( Reference ref )
        {
            if( hasStarted ) {
                s.append( ", " );
            } else {
                hasStarted = true;
            }
            s.append( ref.get() );
            return false;
        }

        public String toString()
        {
            s.append( "]" );
            return s.toString();
        }
    }


    ////////////////////////////////////////
    // Private Iterator implementation
    ////////////////////////////////////////


    private class IteratorImpl
        implements Iterator
    {
        // We can't just use indices to track the objects, since the
        // referent could go away if we don't keep a hard reference
        // after the call to hasNext().

        private int nextIndex = -1;
        private int currentIndex = 0;
        private boolean isCurrentValid = false;
        private Object next = null;

        IteratorImpl()
        {
            super();
        }

        public boolean hasNext()
        {
            if( next != null ) {
                return true;
            }
            nextIndex++;
            for( ; nextIndex < size; nextIndex++ ) {
                Reference ref = data[ nextIndex ];
                if( ref == null ) {
                    continue;
                }
                next = ref.get();
                if( next == null ) {
                    // referent is null, clear the entry
                    data[ nextIndex ] = null;
                    continue;
                }
                return true;
            }
            return false;
        }

        public Object next()
        {
            if( !hasNext() ) {
                throw new NoSuchElementException();
            }
            Object current = next;
            next = null;
            currentIndex = nextIndex;
            isCurrentValid = true;
            return current;
        }

        public void remove()
        {
            if( !isCurrentValid ) {
                throw new IllegalStateException();
            }
            isCurrentValid = false;
            Reference ref = data[ currentIndex ];
            data[ currentIndex ] = null;
            if( ref != null ) {
                ref.clear();
            }
        }
    }

}
