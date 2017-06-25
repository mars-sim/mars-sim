/*
 *  $Id: PredicateUtils.java,v 1.6 2005/10/03 15:11:54 rconner Exp $
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

import java.util.Collection;

import org.apache.commons.collections.Predicate;


/**
 *  This class contains static members related to
 *  <code>Predicates</code>.
 *
 *  <P>This is mostly equivalent to the class of the same name in
 *  Jakarta Commons-Collections 3.0, for those classes covered by this
 *  package.  No equivalent exists in version 2.1.
 *
 *  @version    $Revision: 1.6 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class PredicateUtils
{

    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Prevent instantiation.
     */
    private PredicateUtils()
    {
        super();
    }


    ////////////////////////////////////////
    // Methods
    ////////////////////////////////////////


    /**
     *  Returns a <code>Predicate</code> which always returns
     *  <code>true</code>.
     */
    public static Predicate truePredicate()
    {
        return TruePredicate.INSTANCE;
    }


    /**
     *  Returns a <code>Predicate</code> which always returns
     *  <code>false</code>.
     */
    public static Predicate falsePredicate()
    {
        return FalsePredicate.INSTANCE;
    }


    /**
     *  Returns a new <code>Predicate</code> which is the logical
     *  converse of the specified <code>Predicate</code>.
     */
    public static Predicate notPredicate( Predicate pred )
    {
        return new NotPredicate( pred );
    }


    /**
     *  Returns a new <code>Predicate</code> which is the logical
     *  <em>and</em> of the specified <code>Predicates</code>.
     */
    public static Predicate andPredicate( Predicate left, Predicate right )
    {
        return new AndPredicate( left, right );
    }


    /**
     *  Returns a new <code>Predicate</code> which is the logical
     *  <em>or</em> of the specified <code>Predicates</code>.
     */
    public static Predicate orPredicate( Predicate left, Predicate right )
    {
        return new OrPredicate( left, right );
    }


    /**
     *  Returns a new <code>Predicate</code> which is
     *  <code>true</code> if all of its operand predicates are
     *  <code>true</code>.
     */
    public static Predicate allPredicate( Predicate[] predicates )
    {
        return new AllPredicate( predicates );
    }


    /**
     *  Returns a new <code>Predicate</code> which is
     *  <code>true</code> if all of its operand predicates are
     *  <code>true</code>.
     */
    public static Predicate allPredicate( Collection predicates )
    {
        return new AllPredicate( predicates );
    }


    /**
     *  Returns a new <code>Predicate</code> which is
     *  <code>true</code> if any of its operand predicates are
     *  <code>true</code>.
     */
    public static Predicate anyPredicate( Predicate[] predicates )
    {
        return new AnyPredicate( predicates );
    }


    /**
     *  Returns a new <code>Predicate</code> which is
     *  <code>true</code> if any of its operand predicates are
     *  <code>true</code>.
     */
    public static Predicate anyPredicate( Collection predicates )
    {
        return new AnyPredicate( predicates );
    }


    /**
     *  Returns a new <code>Predicate</code> which is
     *  <code>true</code> if the evaluated object is
     *  <code>.equals()</code> to the specified object, or if both are
     *  <code>null</code>.
     */
    public static Predicate equalPredicate( Object testObject )
    {
        return new EqualPredicate( testObject );
    }


    /**
     *  Returns a new <code>Predicate</code> which is
     *  <code>true</code> if the evaluated object is the same object
     *  (by reference) as the specified object.
     */
    public static Predicate identityPredicate( Object testObject )
    {
        return new IdentityPredicate( testObject );
    }


    /**
     *  Returns a new <code>Predicate</code> which is
     *  <code>true</code> if the evaluated object is of the specified
     *  class.
     */
    public static Predicate instanceofPredicate( Class testClass )
    {
        return new InstanceofPredicate( testClass );
    }

}
