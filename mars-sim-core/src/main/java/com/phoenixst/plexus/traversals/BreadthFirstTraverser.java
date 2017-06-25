/*
 *  $Id: BreadthFirstTraverser.java,v 1.6 2005/10/03 15:16:31 rconner Exp $
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

package com.phoenixst.plexus.traversals;

import java.util.*;

import org.apache.commons.collections.*;
import org.apache.log4j.Logger;

import com.phoenixst.plexus.*;
import com.phoenixst.plexus.util.*;


/**
 *  A breadth-first <code>Traverser</code> for a <code>Graph</code>,
 *  with no cycle detection.  The first node returned is the start
 *  node, and no <code>Edge</code> is traversed to reach it.  All of
 *  the caveats concerning the ordering of the operations
 *  <code>hasNext()</code>, <code>next()</code>, and
 *  <code>remove()</code> detailed by the {@link Traverser} class
 *  documentation apply here.
 *
 *  @version    $Revision: 1.6 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class BreadthFirstTraverser
    implements PruningTraverser
{

    /**
     *  The Logger.
     */
    private static final Logger LOGGER = Logger.getLogger( BreadthFirstTraverser.class );

    /**
     *  The factory for producing new Traversers.
     */
    private final Transformer traverserFactory;

    /**
     *  A FIFO queue of Traversers.  The next node to be returned is
     *  from the Traverser at the front of the queue.  As nodes are
     *  returned (and not pruned or removed), the above factory is
     *  used to create new Traversers which are added to the end of
     *  the queue (if the Traverser is not empty).
     */
    private final LinkedList traverserQueue = new LinkedList();

    /**
     *  The Traverser created from the last node returned by next().
     *  A value of <code>null</code> indicates that the last node or
     *  edge traversed has been removed or pruned.
     */
    private Traverser current = null;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>BreadthFirstTraverser</code>.
     */
    public BreadthFirstTraverser( Object startNode,
                                  Graph graph,
                                  Predicate traverserPredicate )
    {
        this( startNode,
              graph,
              new DefaultTraverserFactory( graph, traverserPredicate ) );
    }


    /**
     *  Creates a new <code>BreadthFirstTraverser</code>, which
     *  breadth-first traverses the descendants of the specified
     *  <code>startNode</code>.  The specified <code>startNode</code>
     *  cannot be removed by {@link #remove} when using this
     *  constructor.
     */
    public BreadthFirstTraverser( Object startNode,
                                  OrientedForest forest )
    {
        this( startNode,
              null,
              new ChildTraverserFactory( forest ) );
    }


    /**
     *  Creates a new <code>BreadthFirstTraverser</code>.  The
     *  specified <code>startNode</code> cannot be removed by {@link
     *  #remove} when using this constructor.
     */
    public BreadthFirstTraverser( Object startNode,
                                  Transformer traverserFactory )
    {
        this( startNode,
              null,
              traverserFactory );
    }


    /**
     *  Creates a new <code>BreadthFirstTraverser</code>.  If the
     *  <code>graph</code> argument is <code>null</code>, the
     *  specified <code>startNode</code> cannot be removed by {@link
     *  #remove}.
     */
    public BreadthFirstTraverser( Object startNode,
                                  Graph graph,
                                  Transformer traverserFactory )
    {
        super();
        this.traverserFactory = traverserFactory;

        if( traverserFactory == null ) {
            throw new IllegalArgumentException( "Traverser Factory is null." );
        }
        if( graph == null ) {
            // This is the only way to make sure that startNode is in
            // the graph in this case.
            traverserFactory.transform( startNode );
        } else if( !graph.containsNode( startNode ) ) {
            throw new NoSuchNodeException( "Graph does not contain start node: " + startNode );
        }

        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "Constructor: Adding trivial Traverser to " + startNode + " to end of queue." );
        }
        traverserQueue.addLast( new SingletonTraverser( graph, startNode, null ) );
    }


    ////////////////////////////////////////
    // Traverser
    ////////////////////////////////////////


    public boolean hasNext()
    {
        if( traverserQueue.isEmpty() ) {
            return false;
        }
        if( traverserQueue.size() > 1 ) {
            return true;
        }

        // Because it's quicker to check current than the one
        // on the queue.
        LOGGER.debug( "hasNext(): Calling hasNext() on last created Traverser." );
        if( current != null && current.hasNext() ) {
            return true;
        }

        LOGGER.debug( "  Calling hasNext() on first Traverser in queue." );
        return ((Traverser) traverserQueue.getFirst()).hasNext();
    }


    public Object next()
    {
        LOGGER.debug( "next():" );
        if( current != null && current.hasNext() ) {
            LOGGER.debug( "  Adding last created Traverser to end of queue." );
            traverserQueue.addLast( current );
        }
        current = null;

        while( !traverserQueue.isEmpty() ) {
            Traverser t = (Traverser) traverserQueue.getFirst();
            LOGGER.debug( "  Calling hasNext() on first Traverser in queue." );
            if( t.hasNext() ) {
                Object node = t.next();
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "  Creating new Traverser for " + node + "." );
                }
                current = (Traverser) traverserFactory.transform( node );
                return node;
            }
            LOGGER.debug( "  Removing first Traverser in queue." );
            traverserQueue.removeFirst();
        }

        throw new NoSuchElementException();
    }


    /**
     *  Removes from the underlying <code>Graph</code> the last node
     *  returned by {@link #next}.  This will prevent the exploration
     *  of those nodes that would have been reached through the
     *  removed node (unless they are reachable by another route).
     *  This method can be called only once per call to
     *  <code>next()</code>.  The behavior of this
     *  <code>Traverser</code> is unspecified if the underlying graph
     *  structure is modified while the traversal is in progress in
     *  any way other than by calling this method or {@link
     *  #removeEdge}.
     *
     *  @throws IllegalStateException if <code>next()</code> has not
     *  yet been called, or <code>remove()</code> or
     *  <code>removeEdge</code> has been called after the last call to
     *  <code>next()</code>.
     */
    public void remove()
    {
        if( current == null ) {
            throw new IllegalStateException();
        }
        LOGGER.debug( "remove(): Calling remove() on first Traverser in queue." );
        ((Traverser) traverserQueue.getFirst()).remove();
        current = null;
    }


    public Graph.Edge getEdge()
    {
        if( current == null ) {
            throw new IllegalStateException();
        }
        LOGGER.debug( "getEdge(): Calling getEdge() on first Traverser in queue." );
        return ((Traverser) traverserQueue.getFirst()).getEdge();
    }


    /**
     *  Removes from the underlying {@link Graph} the
     *  <code>Edge</code> that would be returned by {@link #getEdge
     *  getEdge()}.  This will prevent the exploration of those nodes
     *  that would have been reached through the removed
     *  <code>Edge</code> (unless they are reachable by another
     *  route).
     *
     *  <P><b>Description copied from interface: {@link
     *  Traverser}</b><br> {@inheritDoc}
     */
    public void removeEdge()
    {
        if( current == null ) {
            throw new IllegalStateException();
        }
        LOGGER.debug( "removeEdge(): Calling removeEdge() on first Traverser in queue." );
        ((Traverser) traverserQueue.getFirst()).removeEdge();
        current = null;
    }


    public void prune()
    {
        if( current == null ) {
            throw new IllegalStateException();
        }
        LOGGER.debug( "prune(): Setting last created Traverser to null." );
        current = null;
    }

}
