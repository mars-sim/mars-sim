/*
 *  $Id: TraverserPredicate.java,v 1.4 2004/12/23 21:52:02 rconner Exp $
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
 *  A general interface for <code>Predicates</code> for traversals
 *  that can be piecewise defined.  These objects can be used by:
 *
 *  <UL>
 *    <LI>{@link Graph#degree(Object,Predicate) Graph.degree( node, Predicate )}
 *    <LI>{@link Graph#adjacentNodes(Object,Predicate) Graph.adjacentNodes( node, Predicate )}
 *    <LI>{@link Graph#incidentEdges(Object,Predicate) Graph.incidentEdges( node, Predicate )}
 *    <LI>{@link Graph#getAdjacentNode(Object,Predicate) Graph.getAdjacentNode( node, Predicate )}
 *    <LI>{@link Graph#getIncidentEdge(Object,Predicate) Graph.getIncidentEdge( node, Predicate )}
 *    <LI>{@link Graph#traverser(Object,Predicate) Graph.traverser( node, Predicate )}
 *  </UL>
 *
 *  @version    $Revision: 1.4 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface TraverserPredicate extends Predicate
{

    /**
     *  Returns <code>true</code> if the specified object satisfies
     *  this <code>TraverserPredicate</code>, and <code>false</code>
     *  otherwise.  The specified obect is expected to be an instance
     *  of {@link com.phoenixst.collections.OrderedPair}.  The first
     *  element of the <code>OrderedPair</code> is the node
     *  <strong>from</strong> which the {@link Graph.Edge} is being
     *  traversed, the second is the <code>Graph.Edge</code> itself.
     */
    public boolean evaluate( Object object );


    /**
     *  Gets the user object specification for this
     *  <code>TraverserPredicate</code>.
     *
     *  <P>If the returned object is a <code>Predicate</code>, then
     *  that <code>Predicate</code> must evaluate to <code>true</code>
     *  when testing the contained user object for this
     *  <code>TraverserPredicate</code> to be <code>true</code>.
     *
     *  <P>If the returned object is anything other than a
     *  <code>Predicate</code>, then the contained user object must be
     *  <code>.equals()</code> to the specified object (or it must be
     *  <code>null</code> if the specified object is
     *  <code>null</code>) for this <code>TraverserPredicate</code> to
     *  be <code>true</code>.
     */
    public Object getUserObjectSpecification();


    /**
     *  Gets the node specification for this
     *  <code>TraverserPredicate</code>.
     *
     *  <P>If the returned object is a <code>Predicate</code>, then
     *  that <code>Predicate</code> must evaluate to <code>true</code>
     *  when testing the node for this <code>TraverserPredicate</code>
     *  to be <code>true</code>.
     *
     *  <P>If the returned object is anything other than a
     *  <code>Predicate</code>, then the node must be
     *  <code>.equals()</code> to the specified object (or it must be
     *  <code>null</code> if the specified object is
     *  <code>null</code>) for this <code>TraverserPredicate</code> to
     *  be <code>true</code>.
     */
    public Object getNodeSpecification();


    /**
     *  Gets the direction flags for this
     *  <code>TraverserPredicate</code> relative to the node
     *  <strong>from</strong> which the {@link Graph.Edge} is being
     *  traversed.  It may be any of the following values, or some
     *  combination made using bitwise-or:
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
