/*
 *  $Id: PostOrderTraverser.java,v 1.6 2005/10/03 15:16:31 rconner Exp $
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
 *  A post-order depth-first <code>Traverser</code> for a
 *  <code>Graph</code>, with no cycle detection.  The last node
 *  returned is the start node, and no <code>Edge</code> is traversed
 *  to reach it.  All of the caveats concerning the ordering of the
 *  operations <code>hasNext()</code>, <code>next()</code>, and
 *  <code>remove()</code> detailed by the {@link Traverser} class
 *  documentation apply here.
 *
 *  @version    $Revision: 1.6 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class PostOrderTraverser
    implements Traverser
{

    /**
     *  The Logger.
     */
    private static final Logger LOGGER = Logger.getLogger( PostOrderTraverser.class );

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


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>PostOrderTraverser</code>.
     */
    public PostOrderTraverser( Object startNode,
                               Graph graph,
                               Predicate traverserPredicate )
    {
        this( startNode,
              graph,
              new DefaultTraverserFactory( graph, traverserPredicate ) );
    }


    /**
     *  Creates a new <code>PostOrderTraverser</code>, which
     *  depth-first traverses the descendants of the specified
     *  <code>startNode</code>.  The specified <code>startNode</code>
     *  cannot be removed by {@link #remove} when using this
     *  constructor.
     */
    public PostOrderTraverser( Object startNode,
                               OrientedForest forest )
    {
        this( startNode,
              null,
              new ChildTraverserFactory( forest ) );
    }


    /**
     *  Creates a new <code>PostOrderTraverser</code>.  The specified
     *  <code>startNode</code> cannot be removed by {@link #remove}
     *  when using this constructor.
     */
    public PostOrderTraverser( Object startNode,
                               Transformer traverserFactory )
    {
        this( startNode,
              null,
              traverserFactory );
    }


    /**
     *  Creates a new <code>PostOrderTraverser</code>.  If the
     *  <code>graph</code> argument is <code>null</code>, the
     *  specified <code>startNode</code> cannot be removed by {@link
     *  #remove}.
     */
    public PostOrderTraverser( Object startNode,
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
        if( !top.hasNext() ) {
            LOGGER.debug( "  Top Traverser has nothing left to return." );
            if( nodeStack.isEmpty() ) {
                throw new NoSuchElementException();
            }
            LOGGER.debug( "  Popping top Traverser and top node, returning the node." );
            traverserStack.pop();
            return nodeStack.pop();
        }

        while( true ) {
            Object node = top.next();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  Creating new Traverser for " + node + "." );
            }
            top = (Traverser) traverserFactory.transform( node );
            if( !top.hasNext() ) {
                LOGGER.debug( "  Node is a leaf, return it." );
                return node;
            }
            LOGGER.debug( "  Node is not a leaf, push it and push its Traverser." );
            nodeStack.push( node );
            traverserStack.push( top );
        }
    }


    public void remove()
    {
        if( traverserStack.isEmpty() ) {
            throw new IllegalStateException();
        }
        LOGGER.debug( "remove(): Calling remove() on top Traverser of stack." );
        ((Traverser) traverserStack.peek()).remove();
    }


    public Graph.Edge getEdge()
    {
        if( traverserStack.isEmpty() ) {
            throw new IllegalStateException();
        }
        LOGGER.debug( "getEdge(): Calling getEdge() on top Traverser of stack." );
        return ((Traverser) traverserStack.peek()).getEdge();
    }


    public void removeEdge()
    {
        if( traverserStack.isEmpty() ) {
            throw new IllegalStateException();
        }
        LOGGER.debug( "removeEdge(): Calling removeEdge() on top Traverser of stack." );
        ((Traverser) traverserStack.peek()).removeEdge();
    }

}
