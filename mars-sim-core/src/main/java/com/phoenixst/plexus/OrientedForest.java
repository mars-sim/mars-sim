/*
 *  $Id: OrientedForest.java,v 1.2 2004/10/11 18:26:32 rconner Exp $
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

import java.util.Collection;


/**
 *  A data structure with parent/child relationships.  Each node may
 *  have at most one adjacent node distinguished as being its
 *  "parent", and the data structure should be acyclic under the
 *  parent operation.  The following are definitions for other terms
 *  used here (and elsewhere):
 *
 *  <UL>
 *    <LI>If node <code>X</code> is the parent of node <code>Y</code>,
 *        then we also say that <code>Y</code> is a "child" of
 *        <code>X</code>.  A node may have multiple children.
 *    <LI>A node with no parent is a "root" node.  Every node in the
 *        graph is contained in a subgraph with a unique root, which
 *        can be found by following the chain of parents until it
 *        ends.
 *    <LI>A node with no children is a "leaf".  A non-leaf node is an
 *        "internal node".
 *    <LI>If node <code>X</code> is reachable through successive
 *        applications of the parent operation from node
 *        <code>Y</code>, then <code>X</code> is an "ancestor" of
 *        <code>Y</code>, and <code>Y</code> is a "descendant" of
 *        <code>X</code>.  Note that any node is both an ancestor and
 *        descendant of itself.
 *    <LI>Two nodes with the same parent are "siblings".
 *    <LI>The length of the path from a given node to its root is its
 *        "depth".  The depth of a root node is <code>0</code>.
 *    <LI>The length of the longest path from a given node to a leaf
 *        is its "height".  The height of a leaf is <code>0</code>.
 *  </UL>
 *
 *  <P>The word "oriented" was chosen instead of the more commonly
 *  used "directed" because the directedness of the edges need not
 *  have any bearing on the parent-child relationships.
 *
 *  @version    $Revision: 1.2 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface OrientedForest
{

    /**
     *  Gets the parent of the specified node, or <code>null</code> if
     *  it doesn't have one.  If <code>null</code> is a valid node,
     *  then {@link #getParentEdge} must be used to distinguish the
     *  two cases.
     */
    public Object getParent( Object node );


    /**
     *  Traverses over the children of the specified node.
     */
    public Traverser childTraverser( Object node );


    /**
     *  Gets the parent <code>Edge</code> of the specified node, or
     *  <code>null</code> if it doesn't have one.
     */
    public Graph.Edge getParentEdge( Object node );


    /**
     *  Gets whether or not the specified <code>Edge</code> is a
     *  forest edge.
     */
    public boolean isForestEdge( Graph.Edge edge );


    /**
     *  Returns the parent endpoint of the specified forest
     *  <code>Edge</code>.  If the specified <code>Edge</code> is not
     *  a forest edge, throws an
     *  <code>IllegalArgumentException</code>.
     */
    public Object getParentEndpoint( Graph.Edge edge );


    /**
     *  Returns the root nodes of this forest.
     */
    public Collection rootNodes();


    /**
     *  Gets the root of the subgraph containing the specified node.
     */
    public Object getRoot( Object node );


    /**
     *  Returns <code>true</code> if the specified node has no
     *  children.
     */
    public boolean isLeaf( Object node );


    /**
     *  Returns <code>true</code> if <code>ancestor</code> is actually
     *  an ancestor of <code>descendant</code>.
     */
    public boolean isAncestor( Object ancestor, Object descendant );


    /**
     *  Returns the least common ancestor of the specified nodes, or
     *  <code>null</code> if none exists.  If <code>null</code> is a
     *  valid node, then some other method must be used to distinguish
     *  the two cases.
     */
    public Object getLeastCommonAncestor( Object aNode, Object bNode );


    /**
     *  Gets the depth of the specified node.
     */
    public int getDepth( Object node );


    /**
     *  Gets the height of the specified node.
     */
    public int getHeight( Object node );

}
