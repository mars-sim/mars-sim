/*
 *  $Id: Graph.java,v 1.43 2006/06/20 16:41:23 rconner Exp $
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

package com.phoenixst.plexus;

import java.util.Collection;

import org.apache.commons.collections.Predicate;


/**
 *  The root interface of the graph hierarchy.
 *
 *  <P>See the <a href="{@docRoot}/overview-summary.html">Overview
 *  Summary</a> for details not included here.
 *
 *  <P>Nodes must contain unique (using {@link Object#equals
 *  Object.equals()}) user-provided objects.  This requirement allows
 *  nodes to be referenced unambiguously (when creating edges, for
 *  example).  The user-defined objects contained in {@link Edge}
 *  objects, however, are not subject to this requirement in the
 *  general case, although <code>Object.equals()</code> will be used
 *  for edge comparisons.
 *
 *  <P>Nothing in this interface prohibits a {@link Edge} from also
 *  being a node in the same <code>Graph</code>.  In other words,
 *  <code>Graph.Edges</code> can point to other
 *  <code>Graph.Edges</code>.  If a particular <code>Graph</code>
 *  implementation allows this, these two aspects of any particular
 *  <code>Graph.Edge</code> are independent.  Adding or removing an
 *  <code>Graph.Edge</code> as a node has no impact upon the object's
 *  existence as a <code>Graph.Edge</code>, and vice versa.
 *
 *  <P>All general-purpose implementations of this interface should
 *  provide two "standard" constructors: a void (no arguments)
 *  constructor, which creates an empty graph, and a constructor with
 *  a single argument of type <code>Graph</code>, which creates a new
 *  graph with the same elements as its argument.
 *
 *  @version    $Revision: 1.43 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface Graph
{

    /**
     *  Adds <code>node</code> to this <code>Graph</code> (optional
     *  operation).  Returns <code>true</code> if this
     *  <code>Graph</code> changed as a result of the call.  Returns
     *  <code>false</code> if this <code>Graph</code> already contains
     *  <code>node</code>.
     *
     *  <P>If a <code>Graph</code> refuses to add a particular node
     *  for any reason other than that it already contains the node,
     *  it <em>must</em> throw an exception (rather than returning
     *  <code>false</code>).  This preserves the invariant that a
     *  <code>Graph</code> always contains the specified node after
     *  this call returns.  <Code>Graph</Code> classes should clearly
     *  specify in their documentation any other restrictions on what
     *  nodes may be added.
     *
     *  @param node the node to be added to this <code>Graph</code>.
     *
     *  @return <code>true</code> if this <code>Graph</code> changed
     *  as a result of the call, <code>false</code> if this
     *  <code>Graph</code> already contains the specified node.
     *
     *  @throws ClassCastException if the class of <code>node</code>
     *  is of an inappropriate class for this <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if some aspect of
     *  <code>node</code> is inappropriate for this
     *  <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if <code>node</code> is
     *  <code>null</code> and this <code>Graph</code> does not not
     *  permit <code>null</code> nodes.
     *
     *  @throws UnsupportedOperationException if this method is not
     *  supported by this <code>Graph</code>.
     */
    public boolean addNode( Object node );


    /**
     *  Removes <code>node</code> from this <code>Graph</code>
     *  (optional operation).  This method will also remove all edges
     *  incident on <code>node</code>.
     *
     *  @param node the node to be removed from this
     *  <code>Graph</code>.
     *
     *  @return <code>true</code> if this <code>Graph</code> contained
     *  <code>node</code>.
     *
     *  @throws UnsupportedOperationException if this method is not
     *  supported by this <code>Graph</code>.
     */
    public boolean removeNode( Object node );


    /**
     *  Returns <code>true</code> if this <code>Graph</code> contains
     *  the specified node.
     *
     *  @param node the node whose presence in this <code>Graph</code>
     *  is to be tested.
     *
     *  @return <code>true</code> if this <code>Graph</code> contains
     *  the specified node.
     */
    public boolean containsNode( Object node );


    /**
     *  Adds the specified edge to the <code>Graph</code> (optional
     *  operation).  Returns the newly created <code>Graph.Edge</code>
     *  if this <code>Graph</code> changed as a result of the call.
     *  Returns <code>null</code> if this <code>Graph</code> does not
     *  allow duplicate edges and already contains the specified edge.
     *
     *  <P>If a <code>Graph</code> refuses to add a particular edge
     *  for any reason other than that it already contains the edge,
     *  it <em>must</em> throw an exception (rather than returning
     *  <code>null</code>).  This preserves the invariant that a
     *  <code>Graph</code> always contains the specified edge after
     *  this call returns.  <Code>Graph</Code> classes should clearly
     *  specify in their documentation any other restrictions on what
     *  edges may be added.
     *
     *  @param object the user-defined object to be contained in the
     *  new edge.
     *
     *  @param tail the first endpoint of the new edge.
     *
     *  @param head the second endpoint of the new edge.
     *
     *  @param isDirected whether the new edge is directed.
     *
     *  @return the newly created <code>Graph.Edge</code> if this
     *  <code>Graph</code> changed as a result of the call,
     *  <code>null</code> if this <code>Graph</code> does not allow
     *  duplicate edges and already contains the specified edge.
     *
     *  @throws ClassCastException if the class of
     *  <code>object</code>, <code>tail</code>, or <code>head</code>
     *  is of an inappropriate class for this <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if some aspect of
     *  <code>object</code>, <code>tail</code>, <code>head</code>, or
     *  <code>isDirected</code> is inappropriate for this
     *  <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if <code>object</code>,
     *  <code>tail</code>, or <code>head</code> is <code>null</code>
     *  and this <code>Graph</code> does not not permit
     *  <code>null</code> edges and/or nodes.
     *
     *  @throws NoSuchNodeException if <code>tail</code> or
     *  <code>head</code> is not present in this <code>Graph</code>.
     *
     *  @throws UnsupportedOperationException if this method is not
     *  supported by this <code>Graph</code>.
     */
    public Graph.Edge addEdge( Object object,
                               Object tail,
                               Object head,
                               boolean isDirected );


    /**
     *  Removes the specified <code>Graph.Edge</code> from this
     *  <code>Graph</code> (optional operation).
     *
     *  @param edge the <code>Graph.Edge</code> to be removed from
     *  this <code>Graph</code>.
     *
     *  @return <code>true</code> if this <code>Graph</code> contained
     *  the specified <code>Graph.Edge</code>.
     *
     *  @throws UnsupportedOperationException if this method is not
     *  supported by this <code>Graph</code>.
     */
    public boolean removeEdge( Graph.Edge edge );


    /**
     *  Returns <code>true</code> if this <code>Graph</code> contains
     *  the specified <code>Graph.Edge</code>.
     *
     *  @param edge the <code>Graph.Edge</code> whose presence in this
     *  <code>Graph</code> is to be tested.
     *
     *  @return <code>true</code> if this <code>Graph</code> contains
     *  the specified <code>Graph.Edge</code>.
     */
    public boolean containsEdge( Graph.Edge edge );


    /**
     *  Returns the degree of <code>node</code>, defined as the number
     *  of edges incident on <code>node</code>, with self-loops
     *  counted twice.  If this node has more than
     *  <code>Integer.MAX_VALUE</code> incident edges, returns
     *  <code>Integer.MAX_VALUE</code>.
     *
     *  @param node return the degree of this node.
     *
     *  @return the degree of <code>node</code>.
     *
     *  @throws ClassCastException if the class of <code>node</code>
     *  is of an inappropriate class for this <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if some aspect of
     *  <code>node</code> is inappropriate for this
     *  <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if <code>node</code> is
     *  <code>null</code> and this <code>Graph</code> does not not
     *  permit <code>null</code> nodes.
     *
     *  @throws NoSuchNodeException if <code>node</code> is not
     *  present in this <code>Graph</code>.
     */
    public int degree( Object node );


    /**
     *  Returns the degree of <code>node</code> for which the
     *  specified <code>Predicate</code> is satisfied, defined as the
     *  number of edges incident on <code>node</code> that pass the
     *  predicate, with self-loops counted only once.  The argument to
     *  the <code>Predicate.evaluate()</code> method is expected to be
     *  an {@link com.phoenixst.collections.OrderedPair}.  The first
     *  element of the <code>OrderedPair</code> is the specified
     *  <code>node</code>, the second is the <code>Graph.Edge</code>.
     *  If this node has more than <code>Integer.MAX_VALUE</code> such
     *  edges, returns <code>Integer.MAX_VALUE</code>.
     *
     *  @param node return the degree of this node for which the
     *  specified predicate is satisfied.
     *
     *  @param traverserPredicate the predicate which the counted
     *  <code>Graph.Edges</code> must satisfy.
     *
     *  @return the degree of <code>node</code> for which the
     *  specified predicate is satisfied.
     *
     *  @throws ClassCastException if the class of <code>node</code>
     *  is of an inappropriate class for this <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if some aspect of
     *  <code>node</code> is inappropriate for this
     *  <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if <code>node</code> is
     *  <code>null</code> and this <code>Graph</code> does not not
     *  permit <code>null</code> nodes.
     *
     *  @throws NoSuchNodeException if <code>node</code> is not
     *  present in this <code>Graph</code>.
     */
    public int degree( Object node,
                       Predicate traverserPredicate );


    /**
     *  Returns the nodes from this <code>Graph</code> that satisfy
     *  the specified <code>predicate</code>.
     *
     *  @param nodePredicate the predicate which the returned nodes
     *  must satisfy.
     *
     *  @return the nodes from this <code>Graph</code> that satisfy
     *  the specified <code>predicate</code>.
     */
    public Collection nodes( Predicate nodePredicate );


    /**
     *  Returns the <code>Graph.Edges</code> from this
     *  <code>Graph</code> that satisfy the specified
     *  <code>predicate</code>.
     *
     *  @param edgePredicate the predicate which the returned
     *  <code>Graph.Edges</code> must satisfy.
     *
     *  @return the <code>Graph.Edges</code> from this
     *  <code>Graph</code> that satisfy the specified
     *  <code>predicate</code>.
     */
    public Collection edges( Predicate edgePredicate );


    /**
     *  Returns the nodes adjacent to the specified <code>node</code>
     *  for which the specified <code>Predicate</code> is satisfied.
     *  The argument to the <code>Predicate.evaluate()</code> method
     *  is expected to be an {@link
     *  com.phoenixst.collections.OrderedPair}.  The first element of
     *  the <code>OrderedPair</code> is the specified
     *  <code>node</code>, the second is the <code>Graph.Edge</code>.
     *  It should be noted that removing a node from the returned
     *  <code>Collection</code> merely removes one instance of it
     *  being adjacent to the specified <code>node</code>.  In other
     *  words, a connecting <code>Graph.Edge</code> is removed.
     *
     *  @param node return the nodes adjacent to this node for which
     *  the specified predicate is satisfied.
     *
     *  @param traverserPredicate the predicate which the returned
     *  nodes and the traversed <code>Graph.Edges</code> must satisfy.
     *
     *  @return the nodes adjacent to the specified <code>node</code>
     *  for which the specified predicate is satisfied.
     *
     *  @throws ClassCastException if the class of <code>node</code>
     *  is of an inappropriate class for this <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if some aspect of
     *  <code>node</code> is inappropriate for this
     *  <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if <code>node</code> is
     *  <code>null</code> and this <code>Graph</code> does not not
     *  permit <code>null</code> nodes.
     *
     *  @throws NoSuchNodeException if <code>node</code> is not
     *  present in this <code>Graph</code>.
     */
    public Collection adjacentNodes( Object node,
                                     Predicate traverserPredicate );


    /**
     *  Returns the <code>Graph.Edges</code> incident on the specified
     *  <code>node</code> for which the specified
     *  <code>Predicate</code> is satisfied.  The argument to the
     *  <code>Predicate.evaluate()</code> method is expected to be an
     *  {@link com.phoenixst.collections.OrderedPair}.  The first
     *  element of the <code>OrderedPair</code> is the specified
     *  <code>node</code>, the second is the <code>Graph.Edge</code>.
     *
     *  @param node return the <code>Graph.Edges</code> incident on
     *  this node for which the specified predicate is satisfied.
     *
     *  @param traverserPredicate the predicate which the returned
     *  <code>Graph.Edges</code> must satisfy.
     *
     *  @return the <code>Graph.Edges</code> incident on the specified
     *  <code>node</code> for which the specified predicate is
     *  satisfied.
     *
     *  @throws ClassCastException if the class of <code>node</code>
     *  is of an inappropriate class for this <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if some aspect of
     *  <code>node</code> is inappropriate for this
     *  <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if <code>node</code> is
     *  <code>null</code> and this <code>Graph</code> does not not
     *  permit <code>null</code> nodes.
     *
     *  @throws NoSuchNodeException if <code>node</code> is not
     *  present in this <code>Graph</code>.
     */
    public Collection incidentEdges( Object node,
                                     Predicate traverserPredicate );


    /**
     *  Returns a node from this <code>Graph</code> that satisfies the
     *  specified <code>predicate</code>, or <code>null</code> if no
     *  such node exists.
     *
     *  @param nodePredicate the predicate which the returned node
     *  must satisfy.
     *
     *  @return a node from this <code>Graph</code> that satisfies the
     *  specified <code>predicate</code>, or <code>null</code> if no
     *  such node exists.
     */
    public Object getNode( Predicate nodePredicate );


    /**
     *  Returns a <code>Graph.Edge</code> from this <code>Graph</code>
     *  that satisfies the specified <code>predicate</code>, or
     *  <code>null</code> if no such <code>Graph.Edge</code> exists.
     *
     *  @param edgePredicate the predicate which the returned
     *  <code>Graph.Edge</code> must satisfy.
     *
     *  @return a <code>Graph.Edge</code> from this <code>Graph</code>
     *  that satisfies the specified <code>predicate</code>, or
     *  <code>null</code> if no such <code>Graph.Edge</code> exists.
     */
    public Graph.Edge getEdge( Predicate edgePredicate );


    /**
     *  Returns a node adjacent to the specified <code>node</code> for
     *  which the specified <code>Predicate</code> is satisfied.  The
     *  argument to the <code>Predicate.evaluate()</code> method is
     *  expected to be an {@link
     *  com.phoenixst.collections.OrderedPair}.  The first element of
     *  the <code>OrderedPair</code> is the specified
     *  <code>node</code>, the second is the <code>Graph.Edge</code>.
     *
     *  @param node traverse to a node adjacent to this node for which
     *  the specified predicate is satisfied.
     *
     *  @param traverserPredicate the predicate which the returned
     *  node and the traversed <code>Graph.Edge</code> must satisfy.
     *
     *  @return a node adjacent to the specified <code>node</code> for
     *  which the specified predicate is satisfied.
     *
     *  @throws ClassCastException if the class of <code>node</code>
     *  is of an inappropriate class for this <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if some aspect of
     *  <code>node</code> is inappropriate for this
     *  <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if <code>node</code> is
     *  <code>null</code> and this <code>Graph</code> does not not
     *  permit <code>null</code> nodes.
     *
     *  @throws NoSuchNodeException if <code>node</code> is not
     *  present in this <code>Graph</code>.
     */
    public Object getAdjacentNode( Object node,
                                   Predicate traverserPredicate );


    /**
     *  Returns a <code>Graph.Edge</code> incident on the specified
     *  <code>node</code> for which the specified
     *  <code>Predicate</code> is satisfied.  The argument to the
     *  <code>Predicate.evaluate()</code> method is expected to be an
     *  {@link com.phoenixst.collections.OrderedPair}.  The first
     *  element of the <code>OrderedPair</code> is the specified
     *  <code>node</code>, the second is the <code>Graph.Edge</code>.
     *
     *  @param node traverse to a <code>Graph.Edge</code> incident on
     *  this node for which the specified predicate is satisfied.
     *
     *  @param traverserPredicate the predicate which the returned
     *  <code>Graph.Edge</code> must satisfy.
     *
     *  @return a <code>Graph.Edge</code> incident on the specified
     *  <code>node</code> for which the specified predicate is
     *  satisfied.
     *
     *  @throws ClassCastException if the class of <code>node</code>
     *  is of an inappropriate class for this <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if some aspect of
     *  <code>node</code> is inappropriate for this
     *  <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if <code>node</code> is
     *  <code>null</code> and this <code>Graph</code> does not not
     *  permit <code>null</code> nodes.
     *
     *  @throws NoSuchNodeException if <code>node</code> is not
     *  present in this <code>Graph</code>.
     */
    public Graph.Edge getIncidentEdge( Object node,
                                       Predicate traverserPredicate );


    /**
     *  Returns a <code>Traverser</code> from <code>node</code> to all
     *  adjacent nodes for which the specified <code>Predicate</code>
     *  is satisfied.  The argument to the
     *  <code>Predicate.evaluate()</code> method is expected to be an
     *  {@link com.phoenixst.collections.OrderedPair}.  The first
     *  element of the <code>OrderedPair</code> is the specified
     *  <code>node</code>, the second is the <code>Graph.Edge</code>.
     *  The nodes returned by {@link Traverser#next()} are not
     *  necessarily distinct.  Self-loops are only traversed once.
     *  There are no guarantees concerning the order in which the
     *  nodes are returned (unless this <code>Graph</code> is an
     *  instance of some class that provides a guarantee).
     *
     *  @param node traverse over all nodes adjacent to this node for
     *  which the specified predicate is satisfied.
     *
     *  @param traverserPredicate the predicate which the returned
     *  nodes and their traversed <code>Graph.Edges</code> must
     *  satisfy.
     *
     *  @return a <code>Traverser</code> from <code>node</code> to all
     *  adjacent nodes for which the specified predicate is satisfied.
     *
     *  @throws ClassCastException if the class of <code>node</code>
     *  is of an inappropriate class for this <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if some aspect of
     *  <code>node</code> is inappropriate for this
     *  <code>Graph</code>.
     *
     *  @throws IllegalArgumentException if <code>node</code> is
     *  <code>null</code> and this <code>Graph</code> does not not
     *  permit <code>null</code> nodes.
     *
     *  @throws NoSuchNodeException if <code>node</code> is not
     *  present in this <code>Graph</code>.
     */
    public Traverser traverser( Object node,
                                Predicate traverserPredicate );


    /**
     *  An interface describing an edge in a {@link Graph}.  Please
     *  see {@link #equals(Object) equals( Object )} for important
     *  information on implementing this interface.
     */
    public interface Edge
    {

        /**
         *  Returns whether or not this <code>Graph.Edge</code> is
         *  directed.
         *
         *  @return whether or not this <code>Graph.Edge</code> is
         *  directed.
         */
        public boolean isDirected();


        /**
         *  Returns the user object contained in this
         *  <code>Graph.Edge</code>.
         *
         *  @return the user object contained in this
         *  <code>Graph.Edge</code>.
         */
        public Object getUserObject();


        /**
         *  Sets the user object contained in this
         *  <code>Graph.Edge</code>.
         *
         *  @param object the user object to replace the one in this
         *  <code>Graph.Edge</code>.
         *
         *  @throws ClassCastException if the class of
         *  <code>object</code> prevents it from being added to the
         *  {@link Graph} containing this <code>Graph.Edge</code>.
         *
         *  @throws IllegalArgumentException if some aspect of
         *  <code>object</code> prevents it from being added to the
         *  {@link Graph} containing this <code>Graph.Edge</code>.
         *
         *  @throws IllegalArgumentException if <code>object</code> is
         *  <code>null</code> and the {@link Graph} containing this
         *  <code>Graph.Edge</code> does not not permit
         *  <code>null</code> edges.
         *
         *  @throws UnsupportedOperationException if this method is
         *  not supported by this <code>Graph.Edge</code>.
         */
        public void setUserObject( Object object );


        /**
         *  Returns the node which is the tail of this
         *  <code>Graph.Edge</code>.
         *
         *  @return the node which is the tail of this
         *  <code>Graph.Edge</code>.
         */
        public Object getTail();


        /**
         *  Returns the node which is the head of this
         *  <code>Graph.Edge</code>.
         *
         *  @return the node which is the head of this
         *  <code>Graph.Edge</code>.
         */
        public Object getHead();


        /**
         *  Returns the node which is at the other end of this
         *  <code>Graph.Edge</code> than the specified node.
         *
         *  @param node the node which is the endpoint of this
         *  <code>Graph.Edge</code> not to return.
         *
         *  @return the node which is at the other end of this
         *  <code>Graph.Edge</code> than the specified node.
         *
         *  @throws IllegalArgumentException if this
         *  <code>Graph.Edge</code> is not incident on the specified
         *  node.
         */
        public Object getOtherEndpoint( Object node );


        /**
         *  Returns whether or not some other object is equal to this
         *  one.  It is vitally important that two
         *  <code>Graph.Edges</code> only be <code>.equals()</code>
         *  when they refer to the same actual edge in the graph.
         *  Which edge this is does not change when the contained
         *  user-defined object changes.  In a multigraph, the
         *  endpoints and contained user-defined object are generally
         *  not sufficiently distinguishing characteristics.
         *  Accepting the default implementation from
         *  <code>Object</code>, which uses reference equality, should
         *  be preferred unless <code>Graph.Edges</code> are lazily
         *  created on demand.
         *
         *  <P><b>Description copied from class: {@link
         *  Object}</b><br> {@inheritDoc}
         */
        public boolean equals( Object object );


        /**
         *  Returns the hash code for this <code>Graph.Edge</code>.
         *  Since it is mutable, the contained user-defined object
         *  should not be used when computing the hash code.
         *
         *  <P><b>Description copied from class: {@link
         *  Object}</b><br> {@inheritDoc}
         */
        public int hashCode();

    }

}
