/*
 *  $Id: PreOrderTraverser.java,v 1.6 2005/10/03 15:16:31 rconner Exp $
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
 *  A pre-order depth-first <code>Traverser</code> for a
 *  <code>Graph</code>, with no cycle detection.  The first node
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
public class PreOrderTraverser
    implements PruningTraverser
{

    /**
     *  The Logger.
     */
    private static final Logger LOGGER = Logger.getLogger( PreOrderTraverser.class );

    /**
     *  The factory for producing new Traversers.
     */
    private final Transformer traverserFactory;

    /**
     *  A stack of Traversers.  The next node to be returned is
     *  from the topmost Traverser which has something left to return.
     *  As nodes are returned, the above factory is used to create new
     *  Traversers which are pushed onto the stack, even if they are
     *  empty.
     */
    private final SimpleStack traverserStack = new SimpleStack();

    /**
     *  The Traverser which supplied the last node/edge returned by next().
     *  A value of <code>null</code> indicates that the last node or
     *  edge traversed has been removed or pruned.
     */
    private Traverser current = null;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>PreOrderTraverser</code>.
     */
    public PreOrderTraverser( Object startNode,
                              Graph graph,
                              Predicate traverserPredicate )
    {
        this( startNode,
              graph,
              new DefaultTraverserFactory( graph, traverserPredicate ) );
    }


    /**
     *  Creates a new <code>PreOrderTraverser</code>, which
     *  depth-first traverses the descendants of the specified
     *  <code>startNode</code>.  The specified <code>startNode</code>
     *  cannot be removed by {@link #remove} when using this
     *  constructor.
     */
    public PreOrderTraverser( Object startNode,
                              OrientedForest forest )
    {
        this( startNode,
              null,
              new ChildTraverserFactory( forest ) );
    }


    /**
     *  Creates a new <code>PreOrderTraverser</code>.  The specified
     *  <code>startNode</code> cannot be removed by {@link #remove}
     *  when using this constructor.
     */
    public PreOrderTraverser( Object startNode,
                              Transformer traverserFactory )
    {
        this( startNode,
              null,
              traverserFactory );
    }


    /**
     *  Creates a new <code>PreOrderTraverser</code>.  If the
     *  <code>graph</code> argument is <code>null</code>, the
     *  specified <code>startNode</code> cannot be removed by {@link
     *  #remove}.
     */
    public PreOrderTraverser( Object startNode,
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
            LOGGER.debug( "Consturctor: Pushing trivial Traverser to " + startNode + " onto stack." );
        }
        traverserStack.push( new SingletonTraverser( graph, startNode, null ) );
    }


    ////////////////////////////////////////
    // Traverser
    ////////////////////////////////////////


    public boolean hasNext()
    {
        LOGGER.debug( "hasNext(): Calling hasNext() on current Traverser." );
        if( current != null && current.hasNext() ) {
            return true;
        }
        LOGGER.debug( "  Calling hasNext() on Traversers in stack." );
        for( int i = 0, size = traverserStack.size(); i < size; i++ ) {
            if( ((Traverser) traverserStack.get( i )).hasNext() ) {
                return true;
            }
        }
        return false;
    }


    public Object next()
    {
        LOGGER.debug( "next():" );
        while( !traverserStack.isEmpty() ) {
            Traverser t = (Traverser) traverserStack.peek();
            LOGGER.debug( "  Calling hasNext() on top Traverser of stack." );
            if( t.hasNext() ) {
                current = t;
                Object node = current.next();
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "  Setting current Traverser to top of stack." );
                    LOGGER.debug( "  Pushing new Traverser for " + node + " onto stack." );
                }
                traverserStack.push( traverserFactory.transform( node ) );
                return node;
            }
            LOGGER.debug( "  Popping top Traverser off of stack." );
            traverserStack.pop();
        }
        current = null;
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
        LOGGER.debug( "remove(): Calling remove() on current Traverser." );
        current.remove();
        LOGGER.debug( "  Setting current Traverser to null and popping top Traverser off of stack." );
        current = null;
        traverserStack.pop();
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
        LOGGER.debug( "removeEdge(): Calling removeEdge() on current Traverser." );
        current.removeEdge();
        LOGGER.debug( "  Setting current Traverser to null and popping top Traverser off of stack." );
        current = null;
        traverserStack.pop();
    }


    public void prune()
    {
        if( current == null ) {
            throw new IllegalStateException();
        }
        LOGGER.debug( "prune(): Setting current Traverser to null and popping top Traverser off of stack." );
        current = null;
        traverserStack.pop();
    }

}
