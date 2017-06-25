/*
 *  $Id: DefaultOrientedForest.java,v 1.21 2006/06/07 21:08:23 rconner Exp $
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

import com.phoenixst.collections.OrderedPair;
import com.phoenixst.plexus.traversals.DepthFirstTraverser;
import com.phoenixst.plexus.util.DefaultObjectEdge;


/**
 *  A default implementation of the {@link Graph} and
 *  {@link OrientedForest} interfaces.
 *
 *  @version    $Revision: 1.21 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class DefaultOrientedForest extends DefaultGraph
    implements OrientedForest
{

    ////////////////////////////////////////
    // Constants
    ////////////////////////////////////////

    private static final long serialVersionUID = 2L;

    /**
     *  Argument to createEdge() that signifies the new edge should
     *  not be a forest edge.
     */
    static final Object NON_FOREST_STATE = new Object();


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>DefaultOrientedForest</code>.
     */
    public DefaultOrientedForest()
    {
        super();
    }


    /**
     *  Creates a new <code>DefaultOrientedForest</code> which is a
     *  copy of the specified <code>Graph</code>.
     */
    public DefaultOrientedForest( Graph graph )
    {
        super( graph );
    }


    ////////////////////////////////////////
    // DefaultGraph
    ////////////////////////////////////////


    protected Graph.Edge createEdge( Object object,
                                     Object tail,
                                     Object head,
                                     boolean isDirected,
                                     Object edgeState )
    {
        return new EdgeImpl( object, tail, head, isDirected, edgeState );
    }


    public Graph.Edge addEdge( Object object,
                               Object tail,
                               Object head,
                               boolean isDirected )
    {
        return addEdge( object, tail, head, isDirected, NON_FOREST_STATE );
    }


    ////////////////////////////////////////
    // OrientedForest
    ////////////////////////////////////////


    public Object getParent( Object node )
    {
        return getAdjacentNode( node, ParentPredicate.INSTANCE );
    }


    public Traverser childTraverser( Object node )
    {
        return traverser( node, ChildPredicate.INSTANCE );
    }


    public Graph.Edge getParentEdge( Object node )
    {
        return getIncidentEdge( node, ParentPredicate.INSTANCE );
    }


    public boolean isForestEdge( Graph.Edge edge )
    {
        return containsEdge( edge )
            && ((EdgeImpl) edge).isForest;
    }


    public Object getParentEndpoint( Graph.Edge edge )
    {
        if( !containsEdge( edge ) ) {
            throw new IllegalArgumentException( "Edge is not a forest edge: " + edge );
        }
        EdgeImpl edgeImpl = (EdgeImpl) edge;
        if ( !edgeImpl.isForest ) {
            throw new IllegalArgumentException( "Edge is not a forest edge: " + edge );
        }
        return edgeImpl.parent;
    }


    public Collection rootNodes()
    {
        // FIXME - allow a non-lazy version, user configurable?
        return nodes( new RootPredicate() );
    }


    public Object getRoot( Object node )
    {
        EdgeImpl edge = (EdgeImpl) getIncidentEdge( node, ParentPredicate.INSTANCE );
        while( edge != null ) {
            node = edge.parent;
            edge = (EdgeImpl) getIncidentEdge( node, ParentPredicate.INSTANCE );
        }
        return node;
    }


    public boolean isLeaf( Object node )
    {
        return getIncidentEdge( node, ChildPredicate.INSTANCE ) == null;
    }


    public boolean isAncestor( Object ancestor, Object descendant )
    {
        if( !containsNode( ancestor ) ) {
            throw new NoSuchNodeException( "Node is not in this graph: " + ancestor );
        }
        do {
            if( GraphUtils.equals( ancestor, descendant ) ) {
                return true;
            }
            EdgeImpl edge = (EdgeImpl) getIncidentEdge( descendant, ParentPredicate.INSTANCE );
            if( edge == null ) {
                break;
            }
            descendant = edge.parent;
        } while( true );

        return false;
    }


    public Object getLeastCommonAncestor( Object aNode, Object bNode )
    {
        return GraphUtils.getLeastCommonAncestor( this, aNode, bNode );
    }


    public int getDepth( Object node )
    {
        int depth = 0;
        EdgeImpl edge = (EdgeImpl) getIncidentEdge( node, ParentPredicate.INSTANCE );
        while( edge != null ) {
            node = edge.parent;
            edge = (EdgeImpl) getIncidentEdge( node, ParentPredicate.INSTANCE );
            depth++;
        }
        return depth;
    }


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


    ////////////////////////////////////////
    // Other public methods
    ////////////////////////////////////////


    /**
     *  Adds a new forest <code>Edge</code>.  The specified
     *  <code>parent</code> must be one of the specified endpoints.
     *  If the child endpoint already has a parent edge, it will be
     *  removed.
     */
    public Graph.Edge setParent( Object object,
                                 Object tail,
                                 Object head,
                                 boolean isDirected,
                                 Object parent )
    {
        Object child;
        if( GraphUtils.equals( parent, tail ) ) {
            child = head;
        } else if( GraphUtils.equals( parent, head ) ) {
            child = tail;
        } else {
            throw new IllegalArgumentException( "Parent must be one of the endpoints." );
        }

        Graph.Edge oldEdge = getIncidentEdge( child, ParentPredicate.INSTANCE );
        if( oldEdge != null ) {
            removeEdge( oldEdge );
        }

        return addEdge( object, tail, head, isDirected, parent );
    }


    ////////////////////////////////////////
    // Private classes
    ////////////////////////////////////////


    private class RootPredicate
        implements Predicate
    {
        RootPredicate()
        {
            super();
        }

        public boolean evaluate( Object object )
        {
            return getIncidentEdge( object, ParentPredicate.INSTANCE ) == null;
        }

        public String toString()
        {
            return "ROOT_NODE_PREDICATE";
        }
    }


    private static class ParentPredicate
        implements Predicate,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        static final ParentPredicate INSTANCE = new ParentPredicate();

        private ParentPredicate()
        {
            super();
        }

        private Object readResolve()
        {
            return INSTANCE;
        }

        public boolean evaluate( Object object )
        {
            OrderedPair pair = (OrderedPair) object;
            Object baseNode = pair.getFirst();
            EdgeImpl edge = (EdgeImpl) pair.getSecond();
            Object parent = edge.parent;
            return edge.isForest
                && baseNode != parent
                && (baseNode == null || !baseNode.equals( parent ));
        }

        public String toString()
        {
            return "PARENT_TRAVERSER_PREDICATE";
        }
    }


    private static class ChildPredicate
        implements Predicate,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        static final ChildPredicate INSTANCE = new ChildPredicate();

        private ChildPredicate()
        {
            super();
        }

        private Object readResolve()
        {
            return INSTANCE;
        }

        public boolean evaluate( Object object )
        {
            OrderedPair pair = (OrderedPair) object;
            Object baseNode = pair.getFirst();
            EdgeImpl edge = (EdgeImpl) pair.getSecond();
            Object parent = edge.parent;
            return edge.isForest
                && ((baseNode == null) ? (parent == null) : baseNode.equals( parent ));
        }

        public String toString()
        {
            return "CHILD_TRAVERSER_PREDICATE";
        }
    }


    private static class EdgeImpl extends DefaultObjectEdge
    {
        private static final long serialVersionUID = 2L;

        /**
         *  Whether or not this is a forest edge.
         *
         *  @serial
         */
        final boolean isForest;

        /**
         *  If this is a forest edge, the parent endpoint.
         *
         *  @serial
         */
        final Object parent;

        EdgeImpl( Object object, Object tail, Object head, boolean directed, Object edgeState )
        {
            super( object, tail, head, directed );
            if( edgeState == NON_FOREST_STATE ) {
                isForest = false;
                parent = null;
            } else {
                isForest = true;
                parent = edgeState;
            }
        }

        public String toString()
        {
            StringBuilder s = new StringBuilder();
            if( isForest && GraphUtils.equals( parent, getTail() ) ) {
                s.append( "^" );
            }
            s.append( "(" );
            s.append( getTail() );
            s.append( ")" );
            s.append( " -- (" );
            s.append( getUserObject() );
            s.append( isDirected() ? ") -> " : ") -- " );
            if( isForest && GraphUtils.equals( parent, getHead() ) ) {
                s.append( "^" );
            }
            s.append( "(" );
            s.append( getHead() );
            s.append( ")" );
            return s.toString();
        }
    }

}
