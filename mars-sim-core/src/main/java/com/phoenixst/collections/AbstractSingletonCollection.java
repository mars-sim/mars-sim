/*
 *  $Id: AbstractSingletonCollection.java,v 1.11 2006/06/07 19:28:12 rconner Exp $
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

import java.util.*;

import org.apache.commons.collections.iterators.EmptyIterator;


/**
 *  A modifiable, lazy singleton <code>Collection</code> view.  This
 *  view may be empty at any given point in time.
 *
 *  @version    $Revision: 1.11 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public abstract class AbstractSingletonCollection
    implements Collection
{

    private final Object element;


    /**
     *  Creates a new <code>AbstractSingletonCollection</code>.
     */
    protected AbstractSingletonCollection( Object element )
    {
        super();
        this.element = element;
    }


    protected final Object getElement()
    {
        return element;
    }


    protected abstract boolean removeElement();


    public abstract boolean isEmpty();


    public int size()
    {
        return isEmpty() ? 0 : 1;
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean add( Object object )
    {
        throw new UnsupportedOperationException();
    }


    public boolean remove( Object object )
    {
        return (element == object || (element != null && element.equals( object ) ))
            && removeElement();
    }


    public boolean contains( Object object )
    {
        return (element == object || (element != null && element.equals( object ) ))
            && !isEmpty();
    }


    public Iterator iterator()
    {
        return isEmpty()
            ? EmptyIterator.INSTANCE
            : new IteratorImpl( this );
    }


    public boolean containsAll( Collection collection )
    {
        if( isEmpty() ) {
            return collection.isEmpty();
        }
        if( element == null ) {
            for( Iterator i = collection.iterator(); i.hasNext(); ) {
                if( i.next() != null ) {
                    return false;
                }
            }
        } else {
            for( Iterator i = collection.iterator(); i.hasNext(); ) {
                if( !element.equals( i.next() ) ) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean addAll( Collection collection )
    {
        throw new UnsupportedOperationException();
    }


    public boolean removeAll( Collection collection )
    {
        return collection.contains( element )
            && removeElement();
    }


    public boolean retainAll( Collection collection )
    {
        return !collection.contains( element )
            && removeElement();
    }


    public void clear()
    {
        removeElement();
    }


    public Object[] toArray()
    {
        return isEmpty()
            ? new Object[0]
            : new Object[] { element };
    }


    public Object[] toArray( Object[] array )
    {
        int size = size();
        if( size > 0 ) {
            if( array.length == 0 ) {
                array = (Object[]) java.lang.reflect.Array.newInstance( array.getClass().getComponentType(), 1 );
            }
            array[0] = element;
        }
        if( array.length > size ) {
            array[size] = null;
        }
        return array;
    }


    public String toString()
    {
        if( isEmpty() ) {
            return "[]";
        }
        StringBuilder s = new StringBuilder();
        s.append( "[" );
        s.append( element );
        s.append( "]" );
        return s.toString();
    }


    private static class IteratorImpl
        implements Iterator
    {
        private final AbstractSingletonCollection collection;
        private boolean hasNext = true;
        private boolean elementRemoved = false;

        IteratorImpl( AbstractSingletonCollection collection )
        {
            super();
            this.collection = collection;
        }

        public boolean hasNext()
        {
            return hasNext;
        }

        public Object next()
        {
            if( !hasNext ) {
                throw new NoSuchElementException();
            }
            hasNext = false;
            return collection.getElement();
        }

        public void remove()
        {
            if( hasNext || elementRemoved ) {
                throw new IllegalStateException();
            }
            collection.removeElement();
            elementRemoved = true;
        }
    }

}
