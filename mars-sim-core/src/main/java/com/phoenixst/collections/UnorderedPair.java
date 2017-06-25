/*
 *  $Id: UnorderedPair.java,v 1.4 2005/08/31 22:55:55 rconner Exp $
 *
 *  Copyright (C) 1994-2005 by Phoenix Software Technologists,
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

import java.io.Serializable;
import java.util.*;


/**
 *  A simple mutable unordered pair implementation.  The individual
 *  elements may be changed, but the size of this implementation
 *  cannot.  For convenience, the elements are referred to as
 *  &quot;first&quot; and &quot;second&quot;, but this has no effect
 *  on {@link #equals(Object) .equals()}.
 *
 *  @version    $Revision: 1.4 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class UnorderedPair extends AbstractUnmodifiableCollection
    implements Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The first element.
     *
     *  @serial
     */
    private Object first;

    /**
     *  The second element.
     *
     *  @serial
     */
    private Object second;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    public UnorderedPair()
    {
        this( null, null );
    }


    public UnorderedPair( Object first, Object second )
    {
        super();
        this.first = first;
        this.second = second;
    }


    ////////////////////////////////////////
    // Convenience accessors
    ////////////////////////////////////////


    public Object getFirst()
    {
        return first;
    }


    public void setFirst( Object first )
    {
        this.first = first;
    }


    public Object getSecond()
    {
        return second;
    }


    public void setSecond( Object second )
    {
        this.second = second;
    }


    ////////////////////////////////////////
    // Collection
    ////////////////////////////////////////


    public int size()
    {
        return 2;
    }


    public boolean isEmpty()
    {
        return false;
    }


    public boolean contains( Object object )
    {
        if( object == null ) {
            return first == null || second == null;
        }
        return object.equals( first ) || object.equals( second );
    }


    public Iterator iterator()
    {
        return new PairIterator( this );
    }


    public Object[] toArray()
    {
        return new Object[] { first, second };
    }


    /**
     *  To conform to the contracts for <code>Set</code>,
     *  <code>List</code>, and other potential types of
     *  <code>Collections</code>, instances of this class can only be
     *  <code>.equals()</code> to other instances of this class.
     */
    public boolean equals( Object object )
    {
        if( object == this ) {
            return true;
        }
        if( !(object instanceof UnorderedPair) ) {
            return false;
        }
        UnorderedPair pair = (UnorderedPair) object;
        return (equals( first, pair.first ) && equals( second, pair.second ))
            || (equals( first, pair.second ) && equals( second, pair.first ));
    }


    public int hashCode()
    {
        return (first == null ? 0 : first.hashCode())
            ^ (second == null ? 0 : second.hashCode());
    }


    private static final boolean equals( Object a, Object b )
    {
        return (a == null) ? (b == null) : a.equals( b );
    }


    ////////////////////////////////////////
    // Private Iterator class
    ////////////////////////////////////////


    private static class PairIterator
        implements Iterator
    {
        private final UnorderedPair pair;
        private int i = 0;

        PairIterator( UnorderedPair pair )
        {
            super();
            this.pair = pair;
        }

        public boolean hasNext()
        {
            return i < 2;
        }

        public Object next()
        {
            if( i >= 2 ) {
                throw new NoSuchElementException();
            }
            i++;
            return i == 1 ? pair.getFirst() : pair.getSecond();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

}
