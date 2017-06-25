/*
 *  $Id: RootedTree.java,v 1.3 2005/10/03 15:24:00 rconner Exp $
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

package com.phoenixst.plexus;


/**
 *  An {@link OrientedForest} which is restricted to the descendants
 *  of a single root node.
 *
 *  <P>The following methods behave as expected:
 *
 *  <UL>
 *    <LI>{@link #getRoot() getRoot()}
 *    <LI>{@link #setRoot(Object) setRoot( root )}
 *    <LI>{@link #isForestEdge(Graph.Edge) isForestEdge( edge )}
 *    <LI>{@link #getParentEndpoint(Graph.Edge) getParentEndpoint( edge )}
 *  </UL>
 *
 *  <P>The {@link #getRoot(Object) getRoot( node )} method will throw
 *  a <code>NoSuchNodeException</code> if given a node which is not a
 *  descendant of the root node.
 *
 *  <P>The following methods may throw a
 *  <code>NoSuchNodeException</code> if given a node which is not a
 *  descendant of the root node, depending upon whether or not it is
 *  computationally tenable to do so.  The behavior of any concrete
 *  implementations of these methods should be explicitly documented.
 *
 *  <UL>
 *    <LI>{@link #getParent(Object) getParent( node )}
 *    <LI>{@link #childTraverser(Object) childTraverser( node )}
 *    <LI>{@link #getParentEdge(Object) getParentEdge( node )}
 *    <LI>{@link #isLeaf(Object) isLeaf( node )}
 *    <LI>{@link #isAncestor(Object,Object) isAncestor( ancestor, descendant )}
 *    <LI>{@link #getLeastCommonAncestor(Object,Object) getLeastCommonAncestor( aNode, bNode )}
 *    <LI>{@link #getDepth(Object) getDepth( node )}
 *    <LI>{@link #getHeight(Object) getHeight( node )}
 *  </UL>
 *
 *  @version    $Revision: 1.3 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface RootedTree extends OrientedForest,
                                    Rooted
{

    /**
     *  Returns <code>true</code> if the specified node is a
     *  descendant of the root node.
     */
    public boolean isTreeNode( Object node );

}
