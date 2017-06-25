/*
 *  $Id: AndPredicate.java,v 1.10 2006/06/07 16:33:30 rconner Exp $
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

import org.apache.commons.collections.Predicate;


/**
 *  A <code>Predicate</code> which returns the logical short-circuit
 *  <em>and</em> of its operands.
 *
 *  <P>This is mostly equivalent to the class of the same name in
 *  Jakarta Commons-Collections 3.0.  This version exposes its
 *  constructor arguments through public accessor methods.  The
 *  deserialization process in this version checks for null fields.
 *  No equivalent exists in version 2.1.
 *
 *  @version    $Revision: 1.10 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public final class AndPredicate
    implements Predicate,
               Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The left operand.
     *
     *  @serial
     */
    private final Predicate left;

    /**
     *  The right operand.
     *
     *  @serial
     */
    private final Predicate right;


    /**
     *  Creates a new <code>AndPredicate</code>.
     */
    public AndPredicate( Predicate left, Predicate right )
    {
        super();
        this.left = left;
        this.right = right;
        if( left == null ) {
            throw new IllegalArgumentException( "Left Predicate is null." );
        }
        if( right == null ) {
            throw new IllegalArgumentException( "Right Predicate is null." );
        }
    }


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( left == null ) {
            throw new InvalidObjectException( "Left Predicate is null." );
        }
        if( right == null ) {
            throw new InvalidObjectException( "Right Predicate is null." );
        }
    }


    public boolean evaluate( Object object )
    {
        return left.evaluate( object )
            && right.evaluate( object );
    }


    /**
     *  Returns the left operand being used by this
     *  <code>AndPredicate</code>.
     */
    public Predicate getLeftOperand()
    {
        return left;
    }


    /**
     *  Returns the right operand being used by this
     *  <code>AndPredicate</code>.
     */
    public Predicate getRightOperand()
    {
        return right;
    }


    public boolean equals( Object object )
    {
        if( object == this ) {
            return true;
        }
        if( !(object instanceof AndPredicate) ) {
            return false;
        }
        AndPredicate pred = (AndPredicate) object;
        return left.equals( pred.left )
            && right.equals( pred.right );
    }


    public int hashCode()
    {
        // 17 & 37 are arbitrary, but non-zero and prime
        int result = 17;
        result = 37 * result + left.hashCode();
        result = 37 * result + right.hashCode();
        return result;
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "(" );
        s.append( left );
        s.append( " AND " );
        s.append( right );
        s.append( ")" );
        return s.toString();
    }

}
