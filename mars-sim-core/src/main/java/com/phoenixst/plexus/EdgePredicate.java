/*
 *  $Id: EdgePredicate.java,v 1.3 2004/07/22 15:47:02 rconner Exp $
 *
 *  Copyright (C) 1994-2004 by Phoenix Software Technologists,
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

package com.phoenixst.plexus;

import org.apache.commons.collections.Predicate;


/**
 *  A general interface for <code>Predicates</code> testing {@link
 *  Graph.Edge Graph.Edges} that can be piecewise defined.  These
 *  objects can be used by:
 *
 *  <UL>
 *    <LI>{@link Graph#edges(Predicate) Graph.edges( Predicate )}
 *    <LI>{@link Graph#getEdge(Predicate) Graph.getEdge( Predicate )}
 *  </UL>
 *
 *  @version    $Revision: 1.3 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface EdgePredicate extends Predicate
{

    /**
     *  Returns <code>true</code> if the specified object satisfies
     *  this <code>EdgePredicate</code>, and <code>false</code>
     *  otherwise.  The specified object is expected to be an instance
     *  of {@link Graph.Edge}.
     */
    public boolean evaluate( Object object );


    /**
     *  Gets the user object specification for this
     *  <code>EdgePredicate</code>.
     *
     *  <P>If the returned object is a <code>Predicate</code>, then
     *  that <code>Predicate</code> must evaluate to <code>true</code>
     *  when testing the contained user object for this
     *  <code>EdgePredicate</code> to be <code>true</code>.
     *
     *  <P>If the returned object is anything other than a
     *  <code>Predicate</code>, then the contained user object must be
     *  <code>.equals()</code> to the specified object (or it must be
     *  <code>null</code> if the specified object is
     *  <code>null</code>) for this <code>EdgePredicate</code> to be
     *  <code>true</code>.
     */
    public Object getUserObjectSpecification();


    /**
     *  Gets the first node specification for this
     *  <code>EdgePredicate</code>.
     *
     *  <P>If the returned object is a <code>Predicate</code>, then
     *  that <code>Predicate</code> must evaluate to <code>true</code>
     *  when testing the first node for this
     *  <code>EdgePredicate</code> to be <code>true</code>.
     *
     *  <P>If the returned object is anything other than a
     *  <code>Predicate</code>, then the first node must be
     *  <code>.equals()</code> to the specified object (or it must be
     *  <code>null</code> if the specified object is
     *  <code>null</code>) for this <code>EdgePredicate</code> to be
     *  <code>true</code>.
     */
    public Object getFirstNodeSpecification();


    /**
     *  Gets the second node specification for this
     *  <code>EdgePredicate</code>.
     *
     *  <P>If the returned object is a <code>Predicate</code>, then
     *  that <code>Predicate</code> must evaluate to <code>true</code>
     *  when testing the second node for this
     *  <code>EdgePredicate</code> to be <code>true</code>.
     *
     *  <P>If the returned object is anything other than a
     *  <code>Predicate</code>, then the second node must be
     *  <code>.equals()</code> to the specified object (or it must be
     *  <code>null</code> if the specified object is
     *  <code>null</code>) for this <code>EdgePredicate</code> to be
     *  <code>true</code>.
     */
    public Object getSecondNodeSpecification();


    /**
     *  Gets the direction flags for this <code>EdgePredicate</code>
     *  relative to the first node specification.  It may be any of
     *  the following values, or some combination made using
     *  bitwise-or:
     *
     *  <UL>
     *    <LI>{@link GraphUtils#UNDIRECTED_MASK}
     *    <LI>{@link GraphUtils#DIRECTED_OUT_MASK}
     *    <LI>{@link GraphUtils#DIRECTED_IN_MASK}
     *    <LI>{@link GraphUtils#DIRECTED_MASK}
     *    <LI>{@link GraphUtils#ANY_DIRECTION_MASK}
     *  </UL>
     */
    public int getDirectionFlags();

}
