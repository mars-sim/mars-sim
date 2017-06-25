/*
 *  $Id: AbstractOrientedForest.java,v 1.12 2006/04/21 20:37:49 rconner Exp $
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

import com.phoenixst.plexus.traversals.DepthFirstTraverser;


/**
 *  This class provides a skeletal implementation of the
 *  <code>OrientedForest</code> interface, to minimize the effort
 *  required to implement this interface.
 *
 *  <P>All concrete extensions of this class must implement the
 *  following methods:
 *
 *  <UL>
 *    <LI>{@link #getParentEdge(Object) getParentEdge( node )}
 *    <LI>{@link #childTraverser(Object) childTraverser( node )}
 *    <LI>{@link #rootNodes() rootNodes()}
 *  </UL>
 *
 *  <P>The documentation for each non-abstract method in this class
 *  describes its implementation in detail.  Each of these methods may
 *  be overridden if there is a more efficient implementation.
 *
 *  @version    $Revision: 1.12 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public abstract class AbstractOrientedForest
    implements OrientedForest
{

    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>AbstractOrientedForest</code>.
     */
    protected AbstractOrientedForest()
    {
        super();
    }


    ////////////////////////////////////////
    // OrientedForest
    ////////////////////////////////////////


    /**
     *  Gets the parent of the specified node, or <code>null</code> if
     *  it doesn't have one.  If the graph may contain a
     *  <code>null</code> node, then {@link #getParentEdge(Object)
     *  getParentEdge( node )} must be used to distinguish the two cases.
     */
    public Object getParent( Object node )
    {
        Graph.Edge edge = getParentEdge( node );
        return (edge != null)
            ? edge.getOtherEndpoint( node )
            : null;
    }


    /**
     *  Gets whether or not the specified edge is a forest edge.
     */
    public boolean isForestEdge( Graph.Edge edge )
    {
        return edge.equals( getParentEdge( edge.getTail() ) )
            || edge.equals( getParentEdge( edge.getHead() ) );
    }


    /**
     *  Returns the parent endpoint of the specified forest edge.  If
     *  the specified edge is not a forest edge, throws an
     *  <code>IllegalArgumentException</code>.
     */
    public Object getParentEndpoint( Graph.Edge edge )
    {
        Object tail = edge.getTail();
        Object head = edge.getHead();
        if( edge.equals( getParentEdge( tail ) ) ) {
            return head;
        } else if( edge.equals( getParentEdge( head ) ) ) {
            return tail;
        } else {
            throw new IllegalArgumentException( "Edge is not a forest edge: " + edge );
        }
    }


    /**
     *  Gets the root of the subgraph containing the specified node.
     */
    public Object getRoot( Object node )
    {
        Graph.Edge edge = getParentEdge( node );
        while( edge != null ) {
            node = edge.getOtherEndpoint( node );
            edge = getParentEdge( node );
        }
        return node;
    }


    /**
     *  Returns <code>true</code> if the specified node has no
     *  children.
     */
    public boolean isLeaf( Object node )
    {
        return !childTraverser( node ).hasNext();
    }


    /**
     *  Returns <code>true</code> if <code>ancestor</code> is actually
     *  an ancestor of <code>descendant</code>.
     */
    public boolean isAncestor( Object ancestor, Object descendant )
    {
        // This method call is only here to test that ancestor is
        // actually in the graph.
        getParentEdge( ancestor );

        do {
            if( GraphUtils.equals( ancestor, descendant ) ) {
                return true;
            }
            Graph.Edge edge = getParentEdge( descendant );
            if( edge == null ) {
                break;
            }
            descendant = edge.getOtherEndpoint( descendant );
        } while( true );

        return false;
    }


    /**
     *  Returns the least common ancestor of the specified nodes, or
     *  <code>null</code> if none exists.  If the graph may contain a
     *  <code>null</code> node, then some other method must be used to
     *  distinguish the two cases.
     */
    public Object getLeastCommonAncestor( Object aNode, Object bNode )
    {
        return GraphUtils.getLeastCommonAncestor( this, aNode, bNode );
    }


    /**
     *  Gets the depth of the specified node.
     */
    public int getDepth( Object node )
    {
        int depth = 0;
        Graph.Edge edge = getParentEdge( node );
        while( edge != null ) {
            node = edge.getOtherEndpoint( node );
            edge = getParentEdge( node );
            depth++;
        }
        return depth;
    }


    /**
     *  Gets the height of the specified node.
     */
    public int getHeight( Object node )
    {
        int maxHeight = 0;
        int height = -1;
        for( DepthFirstTraverser t = new DepthFirstTraverser( node, this ); t.hasNext(); ) {
            t.next();
            if( t.isDescending() ) {
                height++;
                if( maxHeight < height ) {
                    maxHeight = height;
                }
            } else {
                height--;
            }
        }
        return maxHeight;
    }

}
