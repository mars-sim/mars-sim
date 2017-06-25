/*
 *  $Id: DepthFirstTraverser.java,v 1.7 2005/10/03 15:16:31 rconner Exp $
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

import java.util.NoSuchElementException;

import org.apache.commons.collections.*;
import org.apache.log4j.Logger;

import com.phoenixst.collections.SimpleStack;
import com.phoenixst.plexus.*;
import com.phoenixst.plexus.util.*;


/**
 *  A depth-first <code>Traverser</code> for a <code>Graph</code>,
 *  with no cycle detection.  This <code>Traverser</code> hits each
 *  node twice (assuming it has not been removed), once on the way
 *  down and once on the way back up.  The first and last nodes
 *  returned are the start node, and no <code>Edge</code> is traversed
 *  to reach it.  All of the caveats concerning the ordering of the
 *  operations <code>hasNext()</code>, <code>next()</code>, and
 *  <code>remove()</code> detailed by the {@link Traverser} class
 *  documentation apply here.
 *
 *  @version    $Revision: 1.7 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class DepthFirstTraverser
    implements PruningTraverser
{

    /**
     *  The Logger.
     */
    private static final Logger LOGGER = Logger.getLogger( DepthFirstTraverser.class );

    /**
     *  The factory for producing new Traversers.
     */
    private final Transformer traverserFactory;

    /**
     *  A stack of Nodes.
     */
    private final SimpleStack nodeStack = new SimpleStack();

    /**
     *  A stack of Traversers.
     */
    private final SimpleStack traverserStack = new SimpleStack();

    /**
     *  The current Traverser, the one which can answer getEdge().
     */
    private Traverser current = null;

    /**
     *  Whether or not the last node returned by next() was on the way
     *  down or not.
     */
    private boolean descending = true;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>DepthFirstTraverser</code>.
     */
    public DepthFirstTraverser( Object startNode,
                                Graph graph,
                                Predicate traverserPredicate )
    {
        this( startNode,
              graph,
              new DefaultTraverserFactory( graph, traverserPredicate ) );
    }


    /**
     *  Creates a new <code>DepthFirstTraverser</code>, which
     *  depth-first traverses the descendants of the specified
     *  <code>startNode</code>.  The specified <code>startNode</code>
     *  cannot be removed by {@link #remove} when using this
     *  constructor.
     */
    public DepthFirstTraverser( Object startNode,
                                OrientedForest forest )
    {
        this( startNode,
              null,
              new ChildTraverserFactory( forest ) );
    }


    /**
     *  Creates a new <code>DepthFirstTraverser</code>.  The specified
     *  <code>startNode</code> cannot be removed by {@link #remove}
     *  when using this constructor.
     */
    public DepthFirstTraverser( Object startNode,
                                Transformer traverserFactory )
    {
        this( startNode,
              null,
              traverserFactory );
    }


    /**
     *  Creates a new <code>DepthFirstTraverser</code>.  If the
     *  <code>graph</code> argument is <code>null</code>, the
     *  specified <code>startNode</code> cannot be removed by {@link
     *  #remove}.
     */
    public DepthFirstTraverser( Object startNode,
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
            LOGGER.debug( "Constructor: Pushing trivial Traverser to " + startNode + " onto Traverser stack." );
        }
        traverserStack.push( new SingletonTraverser( graph, startNode, null ) );
    }


    ////////////////////////////////////////
    // Traverser
    ////////////////////////////////////////


    public boolean hasNext()
    {
        LOGGER.debug( "hasNext(): Calling isEmpty() on node stack OR hasNext() on top Traverser." );
        return !nodeStack.isEmpty()
            || ((Traverser) traverserStack.peek()).hasNext();
    }


    public Object next()
    {
        LOGGER.debug( "next():" );
        Traverser top = (Traverser) traverserStack.peek();
        LOGGER.debug( "  Calling hasNext() on top Traverser." );
        descending = top.hasNext();
        Object node;
        if( descending ) {
            LOGGER.debug( "  Descending, setting current Traverser to top of stack" );
            current = top;
            node = top.next();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  Creating new Traverser for " + node + "." );
                LOGGER.debug( "  Pushing node and its Traverser, return node." );
            }
            nodeStack.push( node );
            traverserStack.push( traverserFactory.transform( node ) );
        } else {
            LOGGER.debug( "  Ascending." );
            if( nodeStack.isEmpty() ) {
                throw new NoSuchElementException();
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  Popping node and Traverser." );
                LOGGER.debug( "  Setting current Traverser to top of stack." );
                LOGGER.debug( "  Return node." );
            }
            traverserStack.pop();
            current = (Traverser) traverserStack.peek();
            node = nodeStack.pop();
        }
        return node;
    }


    /**
     *  Removes from the underlying <code>Graph</code> the last node
     *  returned by {@link #next}.  If this method is called during
     *  descent, this will prevent the exploration of those nodes that
     *  would have been reached through the removed node (unless they
     *  are reachable by another route).  This method can be called
     *  only once per call to <code>next()</code>.  The behavior of
     *  this <code>Traverser</code> is unspecified if the underlying
     *  graph structure is modified while the traversal is in progress
     *  in any way other than by calling this method or {@link
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
        LOGGER.debug( "remove(): Calling remove() on current Traverser, setting current to null." );
        current.remove();
        current = null;
        if( descending ) {
            LOGGER.debug( "  Popping node and Traverser." );
            traverserStack.pop();
            nodeStack.pop();
        }
    }


    public Graph.Edge getEdge()
    {
        if( current == null ) {
            throw new IllegalStateException();
        }
        LOGGER.debug( "getEdge(): Calling getEdge() on current Traverser." );
        return current.getEdge();
    }


    /**
     *  Removes from the underlying {@link Graph} the
     *  <code>Edge</code> that would be returned by {@link #getEdge
     *  getEdge()}.  If this method is called during descent, this
     *  will prevent the exploration of those nodes that would have
     *  been reached through the removed <code>Edge</code> (unless
     *  they are reachable by another route).
     *
     *  <P><b>Description copied from interface: {@link
     *  Traverser}</b><br> {@inheritDoc}
     */
    public void removeEdge()
    {
        if( current == null ) {
            throw new IllegalStateException();
        }
        LOGGER.debug( "removeEdge(): Calling removeEdge() on current Traverser, setting current to null." );
        current.removeEdge();
        current = null;
        if( descending ) {
            LOGGER.debug( "  Popping node and Traverser." );
            traverserStack.pop();
            nodeStack.pop();
        }
    }


    public void prune()
    {
        if( current == null ) {
            throw new IllegalStateException();
        }
        LOGGER.debug( "prune(): Setting current to null." );
        current = null;
        if( descending ) {
            LOGGER.debug( "  Popping node and Traverser." );
            traverserStack.pop();
            nodeStack.pop();
        }
    }


    /**
     *  Returns <code>true</code> if the last node returned by {@link
     *  #next} is being traversed away from the start node,
     *  <code>false</code> if the traversal is on its way back out.
     */
    public boolean isDescending()
    {
        return descending;
    }

}
