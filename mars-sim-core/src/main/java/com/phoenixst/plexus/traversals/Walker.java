/*
 *  $Id: Walker.java,v 1.9 2006/05/30 22:08:30 rconner Exp $
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

import java.util.NoSuchElementException;

import org.apache.commons.collections.*;

import com.phoenixst.plexus.*;
import com.phoenixst.plexus.util.*;


/**
 *  A <code>Traverser</code> which walks a graph, with no cycle
 *  detection.  Each iteration step moves from one node to an adjacent
 *  node.  The first node returned is the start node, and no
 *  <code>Edge</code> is traversed to reach it.
 *
 *  @version    $Revision: 1.9 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class Walker
    implements Traverser
{

    /**
     *  The possible states that this Walker can be in.
     */
    private enum State { INIT, FIRST_NODE, OTHER }

    /**
     *  If non-null, the graph being walked.
     */
    private final Graph graph;

    /**
     *  The transformer which, when given a node, returns an incident
     *  Edge.
     */
    private final Transformer incidentEdgeGetter;

    /**
     *  The current node.
     */
    private Object currentNode;

    /**
     *  The current edge.
     */
    private Graph.Edge currentEdge = null;

    /**
     *  The next edge.
     */
    private Graph.Edge nextEdge = null;

    /**
     *  The current state.
     */
    private State state = State.INIT;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>Walker</code>.
     */
    public Walker( Object startNode,
                   Graph graph,
                   Predicate traverserPredicate )
    {
        this( startNode,
              graph,
              new DefaultIncidentEdgeGetter( graph, traverserPredicate ) );
    }


    /**
     *  Creates a new unmodifiable <code>Walker</code>, which
     *  traverses the ancestors of the specified
     *  <code>startNode</code>.
     */
    public Walker( Object startNode,
                   OrientedForest forest )
    {
        this( startNode,
              null,
              new ParentEdgeGetter( forest ) );
    }


    /**
     *  Creates a new unmodifiable <code>Walker</code>.
     */
    public Walker( Object startNode,
                   Transformer incidentEdgeGetter )
    {
        this( startNode,
              null,
              incidentEdgeGetter );
    }


    /**
     *  Creates a new <code>Walker</code>.  If the <code>graph</code>
     *  argument is <code>null</code>, the <code>Walker</code> will be
     *  unmodifiable.
     */
    public Walker( Object startNode,
                   Graph graph,
                   Transformer incidentEdgeGetter )
    {
        super();
        this.graph = graph;
        this.incidentEdgeGetter = incidentEdgeGetter;

        if( incidentEdgeGetter == null ) {
            throw new IllegalArgumentException( "Incident Edge Getter is null." );
        }
        if( graph == null ) {
            // This is the only way to make sure that startNode is in
            // the graph in this case.
            incidentEdgeGetter.transform( startNode );
        } else if( !graph.containsNode( startNode ) ) {
            throw new NoSuchNodeException( "Graph does not contain start node: " + startNode );
        }

        currentNode = startNode;
    }


    ////////////////////////////////////////
    // Traverser
    ////////////////////////////////////////


    public boolean hasNext()
    {
        return state == State.INIT
            || nextEdge != null;
    }


    public Object next()
    {
        if( state == State.INIT ) {
            state = State.FIRST_NODE;
        } else {
            if( nextEdge == null ) {
                throw new NoSuchElementException();
            }
            state = State.OTHER;
            currentNode = nextEdge.getOtherEndpoint( currentNode );
        }
        currentEdge = nextEdge;
        nextEdge = (Graph.Edge) incidentEdgeGetter.transform( currentNode );
        return currentNode;
    }


    /**
     *  Removes from the underlying <code>Graph</code> the last node
     *  returned by {@link #next}, effectively terminating this
     *  iteration.
     *
     *  <P><b>Description copied from interface: {@link
     *  java.util.Iterator}</b><br> {@inheritDoc}
     */
    public void remove()
    {
        if( state == State.INIT ) {
            throw new IllegalStateException();
        }
        if( graph == null ) {
            throw new UnsupportedOperationException();
        }
        if( state == State.OTHER && currentEdge == null ) {
            throw new IllegalStateException();
        }
        graph.removeNode( currentNode );
        state = State.OTHER;
        currentEdge = null;
        nextEdge = null;
    }


    public Graph.Edge getEdge()
    {
        if( state == State.INIT || (state == State.OTHER && currentEdge == null) ) {
            throw new IllegalStateException();
        }
        return currentEdge;
    }


    public void removeEdge()
    {
        if( currentEdge == null ) {
            throw new IllegalStateException();
        }
        if( graph == null ) {
            throw new UnsupportedOperationException();
        }
        graph.removeEdge( currentEdge );
        currentEdge = null;
    }

}
