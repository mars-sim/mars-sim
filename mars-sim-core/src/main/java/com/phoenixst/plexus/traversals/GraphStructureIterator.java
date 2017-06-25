/*
 *  $Id: GraphStructureIterator.java,v 1.7 2006/06/21 20:21:32 rconner Exp $
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

package com.phoenixst.plexus.traversals;

import java.util.Iterator;

import org.apache.commons.collections.Transformer;

import com.phoenixst.collections.IteratorChain;
import com.phoenixst.plexus.*;
import com.phoenixst.plexus.util.*;


/**
 *  An <code>Iterator</code> over the nodes and edges of a
 *  <code>Graph</code> ordered such that the endpoints of a
 *  <code>Graph.Edge</code> are always seen before the edge
 *  itself.  This <code>Iterator</code> may be used to build
 *  a <code>Graph</code> with the same structure as some
 *  specified <code>Graph</code>, even if it has edges which
 *  point to other edges.
 *
 *  @version    $Revision: 1.7 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class GraphStructureIterator
    implements Iterator
{

    /**
     *  The Graph whose structure is being iterated over.
     */
    private final Graph graph;

    /**
     *  The current DepthFirstTraverser used to accomplish the
     *  topological sorting.
     */
    private final Traverser t;

    /**
     *  The last object returned by next();
     */
    private Object current = null;

    /**
     *  Whether or not the iteration has started;
     */
    private boolean hasStarted = false;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>GraphStructureIterator</code>.
     */
    public GraphStructureIterator( Graph graph )
    {
        super();
        this.graph = graph;
        t = new TopologicalSortTraverser( new IteratorChain( graph.nodes( null ).iterator(),
                                                             graph.edges( null ).iterator() ),
                                          new TraverserFactory( graph ) );
    }


    ////////////////////////////////////////
    // Iterator
    ////////////////////////////////////////


    public boolean hasNext()
    {
        return t.hasNext();
    }


    public Object next()
    {
        current = t.next();
        hasStarted = true;
        return current;
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }


    ////////////////////////////////////////
    // Public Methods
    ////////////////////////////////////////


    /**
     *  Returns whether or not the last object returned by
     *  {@link #next} is a node in the <code>Graph</code>.
     */
    public boolean isNode()
    {
        if( !hasStarted ) {
            throw new IllegalStateException();
        }
        return graph.containsNode( current );
    }


    /**
     *  Returns whether or not the last object returned by
     *  {@link #next} is a <code>Graph.Edge</code> in the
     *  <code>Graph</code>.
     */
    public boolean isEdge()
    {
        if( !hasStarted ) {
            throw new IllegalStateException();
        }
        return current instanceof Graph.Edge
            && graph.containsEdge( (Graph.Edge) current );
    }


    ////////////////////////////////////////
    // Private class
    ////////////////////////////////////////


    private static class TraverserFactory
        implements Transformer
    {
        private final Graph graph;

        TraverserFactory( Graph graph )
        {
            super();
            this.graph = graph;
        }

        public Object transform( Object object )
        {
            if( !(object instanceof Graph.Edge) ) {
                return GraphUtils.EMPTY_TRAVERSER;
            }
            Graph.Edge edge = (Graph.Edge) object;
            if( !graph.containsEdge( edge ) ) {
                return GraphUtils.EMPTY_TRAVERSER;
            }
            Object tail = edge.getTail();
            Object head = edge.getHead();
            Graph.Edge tailEdge = new DefaultSimpleEdge( edge, tail, true );
            Graph.Edge headEdge = new DefaultSimpleEdge( edge, head, true );
            return new TraverserChain( new SingletonTraverser( tail, tailEdge ),
                                       new SingletonTraverser( head, headEdge ) );
        }
    }

}
