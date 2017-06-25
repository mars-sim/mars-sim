/*
 *  $Id: CartesianProduct.java,v 1.2 2006/06/07 15:29:30 rconner Exp $
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

import java.io.*;
import java.util.*;

import org.apache.commons.collections.iterators.EmptyIterator;


/**
 *  A <code>Collection</code> whose elements are all of the ordered
 *  pairs (x, y), where x is from the first delegate collection and y
 *  is from the second.  Here, the elements are instances of {@link
 *  OrderedPair}.  This class has no public constructors, please use
 *  the provided factory methods.  If both delegate collections are
 *  sets, then this collection will be as well, except that it does
 *  not conform to the {@link Set} interface with regards to {@link
 *  Set#equals(java.lang.Object)} and {@link Set#hashCode()}.
 *
 *  @version    $Revision: 1.2 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public abstract class CartesianProduct extends AbstractUnmodifiableCollection
    implements Serializable
{

    private static final long serialVersionUID = 1L;


    /**
     *  The left operand.
     *
     *  @serial
     */
    final Collection left;

    /**
     *  The right operand.
     *
     *  @serial
     */
    final Collection right;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>CartesianProduct</code>.
     *
     *  @param left the first (left) collection operand for the
     *  product operation.
     *
     *  @param right the second (right) collection operand for the
     *  product operation.
     */
    CartesianProduct( Collection left, Collection right )
    {
        super();
        this.left = left;
        this.right = right;
        if( left == null ) {
            throw new IllegalArgumentException( "Left operand is null." );
        }
        if( right == null ) {
            throw new IllegalArgumentException( "Right operand is null." );
        }
    }


    ////////////////////////////////////////
    // Serialization
    ////////////////////////////////////////


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( left == null ) {
            throw new InvalidObjectException( "Left operand is null." );
        }
        if( right == null ) {
            throw new InvalidObjectException( "Right operand is null." );
        }
    }


    ////////////////////////////////////////
    // Static factory methods
    ////////////////////////////////////////


    /**
     *  Creates and returns a new <code>CartesianProduct</code> of the
     *  specified collections with the left one controlling the outer
     *  loop of its iterator.  This factory method is preferable when
     *  the left collection is more expensive to use than the right
     *  one.
     */
    public static final CartesianProduct leftProduct( Collection left, Collection right )
    {
        return new LeftProduct( left, right );
    }


    /**
     *  Creates and returns a new <code>CartesianProduct</code> of the
     *  specified collections with the right one controlling the outer
     *  loop of its iterator.  This factory method is preferable when
     *  the right collection is more expensive to use than the left
     *  one.
     */
    public static final CartesianProduct rightProduct( Collection left, Collection right )
    {
        return new RightProduct( left, right );
    }


    /**
     *  Returns an <code>Iterator</code> over the elements of the
     *  product of the specified collections with the left one
     *  controlling the outer loop.  This factory method is preferable
     *  when the left collection is more expensive to use than the
     *  right one.
     */
    public static final Iterator leftIterator( Collection left, Collection right )
    {
        if( left == null ) {
            throw new IllegalArgumentException( "Left operand is null." );
        }
        if( right == null ) {
            throw new IllegalArgumentException( "Right operand is null." );
        }
        Iterator leftIter = left.iterator();
        // We have to check for this case, because passing an empty
        // right collection to the Iterator constructor would result
        // in incorrect behavior for hasNext() as it is written.  The
        // alternative is a more complex Iterator implementation.
        if( !leftIter.hasNext() || right.isEmpty() ) {
            return EmptyIterator.INSTANCE;
        }
        return new LeftIterator( leftIter, right );
    }


    /**
     *  Returns an <code>Iterator</code> over the elements of the
     *  product of the specified collections with the right one
     *  controlling the outer loop.  This factory method is preferable
     *  when the right collection is more expensive to use than the
     *  left one.
     */
    public static final Iterator rightIterator( Collection left, Collection right )
    {
        if( left == null ) {
            throw new IllegalArgumentException( "Left operand is null." );
        }
        if( right == null ) {
            throw new IllegalArgumentException( "Right operand is null." );
        }
        Iterator rightIter = right.iterator();
        // We have to check for this case, because passing an empty
        // left collection to the Iterator constructor would result
        // in incorrect behavior for hasNext() as it is written.  The
        // alternative is a more complex Iterator implementation.
        if( !rightIter.hasNext() || left.isEmpty() ) {
            return EmptyIterator.INSTANCE;
        }
        return new RightIterator( left, rightIter );
    }


    ////////////////////////////////////////
    // Accessors
    ////////////////////////////////////////


    public final Collection getLeftOperand()
    {
        return left;
    }


    public final Collection getRightOperand()
    {
        return right;
    }


    ////////////////////////////////////////
    // Concrete CartesianProduct implementations
    ////////////////////////////////////////


    private static class LeftProduct extends CartesianProduct
    {
        private static final long serialVersionUID = 1L;

        LeftProduct( Collection left, Collection right )
        {
            super( left, right );
        }

        public boolean contains( Object object )
        {
            if( !(object instanceof OrderedPair) ) {
                return false;
            }
            OrderedPair pair = (OrderedPair) object;
            return right.contains( pair.getSecond() )
                && left.contains( pair.getFirst() );
        }

        public int size()
        {
            int rightSize = right.size();
            if( rightSize == 0 ) {
                return 0;
            }
            return rightSize * left.size();
        }

        public boolean isEmpty()
        {
            return right.isEmpty() || left.isEmpty();
        }

        public Iterator iterator()
        {
            return CartesianProduct.leftIterator( left, right );
        }
    }


    private static class RightProduct extends CartesianProduct
    {
        private static final long serialVersionUID = 1L;

        RightProduct( Collection left, Collection right )
        {
            super( left, right );
        }

        public boolean contains( Object object )
        {
            if( !(object instanceof OrderedPair) ) {
                return false;
            }
            OrderedPair pair = (OrderedPair) object;
            return left.contains( pair.getFirst() )
                && right.contains( pair.getSecond() );
        }

        public int size()
        {
            int leftSize = left.size();
            if( leftSize == 0 ) {
                return 0;
            }
            return leftSize * right.size();
        }

        public boolean isEmpty()
        {
            return left.isEmpty() || right.isEmpty();
        }

        public Iterator iterator()
        {
            return CartesianProduct.rightIterator( left, right );
        }
    }


    ////////////////////////////////////////
    // Concrete Iterator implementations
    ////////////////////////////////////////


    private static class LeftIterator
        implements Iterator
    {
        private final Iterator leftIter;
        private final Collection right;
        private Object leftObject;
        private Iterator rightIter = EmptyIterator.INSTANCE;

        LeftIterator( Iterator leftIter, Collection right )
        {
            this.leftIter = leftIter;
            this.right = right;
        }

        public boolean hasNext()
        {
            return rightIter.hasNext() || leftIter.hasNext();
        }

        public Object next()
        {
            if( !rightIter.hasNext() ) {
                leftObject = leftIter.next();
                rightIter = right.iterator();
            }
            return new OrderedPair( leftObject, rightIter.next() );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }


    private static class RightIterator
        implements Iterator
    {
        private final Collection left;
        private final Iterator rightIter;
        private Object rightObject;
        private Iterator leftIter = EmptyIterator.INSTANCE;

        RightIterator( Collection left, Iterator rightIter )
        {
            this.left = left;
            this.rightIter = rightIter;
        }

        public boolean hasNext()
        {
            return leftIter.hasNext() || rightIter.hasNext();
        }

        public Object next()
        {
            if( !leftIter.hasNext() ) {
                rightObject = rightIter.next();
                leftIter = left.iterator();
            }
            return new OrderedPair( leftIter.next(), rightObject );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

}
