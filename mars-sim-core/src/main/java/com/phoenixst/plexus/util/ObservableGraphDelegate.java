/*
 *  $Id: ObservableGraphDelegate.java,v 1.20 2006/06/20 20:53:26 rconner Exp $
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

package com.phoenixst.plexus.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.*;

import com.phoenixst.plexus.*;


/**
 *  A helper class providing functionality to help implement {@link
 *  ObservableGraph}.
 *
 *  @version    $Revision: 1.20 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class ObservableGraphDelegate
{

    /**
     *  The default logger.
     */
    private static final Logger DEFAULT_LOGGER = Logger.getLogger( ObservableGraphDelegate.class );

    /**
     *  An empty listener array to use in toArray().
     */
    private static final GraphListener[] EMPTY_ARRAY = new GraphListener[0];

    /**
     *  The graph which is the source of the events.
     */
    private final Graph graph;

    /**
     *  The logger for this delegate.
     */
    private final Logger logger;

    /**
     *  The level at which to log.
     */
    private final Level level;

    /**
     *  The list of <code>GraphListeners</code>.
     */
    private final List<GraphListener> listeners = new CopyOnWriteArrayList<GraphListener>();


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>ObservableGraphDelegate</code>,
     *  which logs to a category corresponding to this class
     *  at the DEBUG level.
     */
    public ObservableGraphDelegate( Graph graph )
    {
        this( graph, DEFAULT_LOGGER, Level.DEBUG );
    }


    /**
     *  Creates a new <code>ObservableGraphDelegate</code>,
     *  which uses the specified logger at the DEBUG level.
     */
    public ObservableGraphDelegate( Graph graph, Logger logger )
    {
        this( graph, logger, Level.DEBUG );
    }


    /**
     *  Creates a new <code>ObservableGraphDelegate</code>.
     */
    public ObservableGraphDelegate( Graph graph, Logger logger, Level level )
    {
        super();
        this.graph = graph;
        this.logger = logger;
        this.level = level;
        if( graph == null ) {
            throw new IllegalArgumentException( "Graph is null." );
        }
        if( logger == null ) {
            throw new IllegalArgumentException( "Logger is null." );
        }
        if( level == null ) {
            throw new IllegalArgumentException( "Level is null." );
        }
    }


    ////////////////////////////////////////
    // listener maintenance methods
    ////////////////////////////////////////


    /**
     *  Adds the specified <code>GraphListener</code>.
     */
    public void addGraphListener( GraphListener listener )
    {
        if( listener == null ) {
            throw new IllegalArgumentException( "Listener is null" );
        }
        listeners.add( listener );
    }


    /**
     *  Removes a previously added <code>GraphListener</code>.
     */
    public void removeGraphListener( GraphListener listener )
    {
        if( listener == null ) {
            throw new IllegalArgumentException( "Listener is null" );
        }
        listeners.remove( listener );
    }


    /**
     *  Returns whether or not this delegate has any listeners.
     */
    public boolean hasListeners()
    {
        return !listeners.isEmpty();
    }


    /**
     *  Returns an array containing the currently registered
     *  <code>GraphListeners</code>.
     */
    public GraphListener[] getGraphListeners()
    {
        return listeners.toArray( EMPTY_ARRAY );
    }


    /**
     *  Removes all currently registered <code>GraphListeners</code>.
     */
    public void removeAllGraphListeners()
    {
        listeners.clear();
    }


    ////////////////////////////////////////
    // notification methods
    ////////////////////////////////////////


    /**
     *  Sends node added event to registered listeners.
     */
    public void fireNodeAdded( Object node )
    {
        if( listeners.isEmpty() ) {
            return;
        }
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Firing node added: " + node );
        }
        GraphEvent event = new GraphEvent( graph, node );
        for( GraphListener listener : listeners ) {
            try {
                listener.nodeAdded( event );
            } catch( Exception e ) {
                logger.error( "Exception thrown by GraphListener adding node: " + node, e );
            }
        }
    }


    /**
     *  Sends node removed event to registered listeners.
     */
    public void fireNodeRemoved( Object node )
    {
        if( listeners.isEmpty() ) {
            return;
        }
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Firing node removed: " + node );
        }
        GraphEvent event = new GraphEvent( graph, node );
        for( GraphListener listener : listeners ) {
            try {
                listener.nodeRemoved( event );
            } catch( Exception e ) {
                logger.error( "Exception thrown by GraphListener removing node: " + node, e );
            }
        }
    }


    /**
     *  Sends edge added event to registered listeners.
     */
    public void fireEdgeAdded( Graph.Edge edge )
    {
        if( listeners.isEmpty() ) {
            return;
        }
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Firing edge added: " + edge );
        }
        GraphEvent event = new GraphEvent( graph, edge );
        for( GraphListener listener : listeners ) {
            try {
                listener.edgeAdded( event );
            } catch( Exception e ) {
                logger.error( "Exception thrown by GraphListener adding edge: " + edge, e );
            }
        }
    }


    /**
     *  Sends edge removed event to registered listeners.
     */
    public void fireEdgeRemoved( Graph.Edge edge )
    {
        if( listeners.isEmpty() ) {
            return;
        }
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Firing edge removed: " + edge );
        }
        GraphEvent event = new GraphEvent( graph, edge );
        for( GraphListener listener : listeners ) {
            try {
                listener.edgeRemoved( event );
            } catch( Exception e ) {
                logger.error( "Exception thrown by GraphListener removing edge: " + edge, e );
            }
        }
    }

}
