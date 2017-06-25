/*
 *  $Id: LoggingGraph.java,v 1.13 2006/06/20 00:03:42 rconner Exp $
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
import org.apache.log4j.*;

import com.phoenixst.collections.LoggingCollection;
import com.phoenixst.plexus.util.*;


/**
 *  A <code>Graph</code> which wraps another to provide logging
 *  support.
 *
 *  @version    $Revision: 1.13 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class LoggingGraph
    implements ObservableGraph
{

    /**
     *  The default logger.
     */
    private static final Logger DEFAULT_LOGGER = Logger.getLogger( LoggingGraph.class );

    /**
     *  The wrapped graph.
     */
    private final Graph delegate;

    /**
     *  The logger to use.
     */
    private final Logger logger;

    /**
     *  The level at which to log.
     */
    private final Level level;

    /**
     *  The delegate to handle observable functionality.
     */
    private final ObservableGraphDelegate observableDelegate;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>LoggingGraph</code> which
     *  logs to a category corresponding to this class at
     *  the DEBUG level.
     */
    public LoggingGraph( Graph delegate )
    {
        this( delegate, DEFAULT_LOGGER, Level.DEBUG );
    }


    /**
     *  Creates a new <code>LoggingGraph</code> which uses
     *  the specified log at the DEBUG level.
     */
    public LoggingGraph( Graph delegate, Logger logger )
    {
        this( delegate, logger, Level.DEBUG );
    }


    /**
     *  Creates a new <code>LoggingGraph</code>.
     */
    public LoggingGraph( Graph delegate, Logger logger, Level level )
    {
        this.delegate = delegate;
        this.logger = logger;
        this.level = level;
        if( delegate == null ) {
            throw new IllegalArgumentException( "Delegate Graph is null." );
        }
        if( logger == null ) {
            throw new IllegalArgumentException( "Logger is null." );
        }
        if( level == null ) {
            throw new IllegalArgumentException( "Level is null." );
        }
        if( delegate instanceof ObservableGraph ) {
            observableDelegate = new ObservableGraphDelegate( this, logger, level );
            GraphListener delegateListener = new ForwardingGraphListener( observableDelegate );
            ((ObservableGraph) delegate).addGraphListener( delegateListener );
        } else {
            observableDelegate = null;
        }
    }


    ////////////////////////////////////////
    // Protected methods
    ////////////////////////////////////////


    /**
     *  Provides access to internal state so it can be used
     *  by extensions of this class.
     */
    protected final Graph getDelegate()
    {
        return delegate;
    }


    /**
     *  Provides access to internal state so it can be used
     *  by extensions of this class.
     */
    protected final Logger getLogger()
    {
        return logger;
    }


    /**
     *  Provides access to internal state so it can be used
     *  by extensions of this class.
     */
    protected final Level getLevel()
    {
        return level;
    }


    ////////////////////////////////////////
    // Graph
    ////////////////////////////////////////


    public boolean addNode( Object node )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( node );
            logger.log( level, "Begin addNode( " + argString + " )" );
            boolean modified = delegate.addNode( node );
            logger.log( level, "End addNode( " + argString + " ), returns " + modified );
            return modified;
        }
        return delegate.addNode( node );
    }


    public boolean removeNode( Object node )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( node );
            logger.log( level, "Begin removeNode( " + argString + " )" );
            boolean modified = delegate.removeNode( node );
            logger.log( level, "End removeNode( " + argString + " ), returns " + modified );
            return modified;
        }
        return delegate.removeNode( node );
    }


    public boolean containsNode( Object node )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( node );
            logger.log( level, "Begin containsNode( " + argString + " )" );
            boolean contains = delegate.containsNode( node );
            logger.log( level, "End containsNode( " + argString + " ), returns " + contains );
            return contains;
        }
        return delegate.containsNode( node );
    }


    public Graph.Edge addEdge( Object object,
                               Object tail,
                               Object head,
                               boolean isDirected )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = object + ", " + tail + ", " + head + ", " + isDirected;
            logger.log( level, "Begin addEdge( " + argString + " )" );
            Graph.Edge edge = delegate.addEdge( object, tail, head, isDirected );
            logger.log( level, "End addEdge( " + argString + " ), returns " + edge );
            return edge;
        }
        return delegate.addEdge( object, tail, head, isDirected );
    }


    public boolean removeEdge( Graph.Edge edge )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( edge );
            logger.log( level, "Begin removeEdge( " + argString + " )" );
            boolean modified = delegate.removeEdge( edge );
            logger.log( level, "End removeEdge( " + argString + " ), returns " + modified );
            return modified;
        }
        return delegate.removeEdge( edge );
    }


    public boolean containsEdge( Graph.Edge edge )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( edge );
            logger.log( level, "Begin containsEdge( " + argString + " )" );
            boolean contains = delegate.containsEdge( edge );
            logger.log( level, "End containsEdge( " + argString + " ), returns " + contains );
            return contains;
        }
        return delegate.containsEdge( edge );
    }


    public int degree( Object node )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( node );
            logger.log( level, "Begin degree( " + argString + " )" );
            int degree = delegate.degree( node );
            logger.log( level, "End degree( " + argString + " ), returns " + degree );
            return degree;
        }
        return delegate.degree( node );
    }


    public int degree( Object node,
                       Predicate traverserPredicate )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = node + ", " + traverserPredicate;
            logger.log( level, "Begin degree( " + argString + " )" );
            int degree = delegate.degree( node, traverserPredicate );
            logger.log( level, "End degree( " + argString + " ), returns " + degree );
            return degree;
        }
        return delegate.degree( node, traverserPredicate );
    }


    public Collection nodes( Predicate nodePredicate )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( nodePredicate );
            logger.log( level, "Begin nodes( " + argString + " )" );
            Collection nodes = delegate.nodes( nodePredicate );
            logger.log( level, "End nodes( " + argString + " ), returns " + nodes );
            return new LoggingCollection( nodes, logger, level );
        }
        // We still wrap the Collection since logging may actually
        // become enabled during its use.
        return new LoggingCollection( delegate.nodes( nodePredicate ),
                                      logger, level );
    }


    public Collection edges( Predicate edgePredicate )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( edgePredicate );
            logger.log( level, "Begin edges( " + argString + " )" );
            Collection edges = delegate.edges( edgePredicate );
            logger.log( level, "End edges( " + argString + " ), returns " + edges );
            return new LoggingCollection( edges, logger, level );
        }
        // We still wrap the Collection since logging may actually
        // become enabled during its use.
        return new LoggingCollection( delegate.edges( edgePredicate ),
                                      logger, level );
    }


    public Collection adjacentNodes( Object node,
                                     Predicate traverserPredicate )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = node + ", " + traverserPredicate;
            logger.log( level, "Begin adjacentNodes( " + argString + " )" );
            Collection adjNodes = delegate.adjacentNodes( node, traverserPredicate );
            logger.log( level, "End adjacentNodes( " + argString + " ), returns " + adjNodes );
            return new LoggingCollection( adjNodes, logger, level );
        }
        // We still wrap the Collection since logging may actually
        // become enabled during its use.
        return new LoggingCollection( delegate.adjacentNodes( node, traverserPredicate ),
                                      logger, level );
    }


    public Collection incidentEdges( Object node,
                                     Predicate traverserPredicate )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = node + ", " + traverserPredicate;
            logger.log( level, "Begin incidentEdges( " + argString + " )" );
            Collection incEdges = delegate.incidentEdges( node, traverserPredicate );
            logger.log( level, "End incidentEdges( " + argString + " ), returns " + incEdges );
            return new LoggingCollection( incEdges, logger, level );
        }
        // We still wrap the Collection since logging may actually
        // become enabled during its use.
        return new LoggingCollection( delegate.incidentEdges( node, traverserPredicate ),
                                      logger, level );
    }


    public Object getNode( Predicate nodePredicate )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( nodePredicate );
            logger.log( level, "Begin getNode( " + argString + " )" );
            Object node = delegate.getNode( nodePredicate );
            logger.log( level, "End getNode( " + argString + " ), returns " + node );
            return node;
        }
        return delegate.getNode( nodePredicate );
    }


    public Graph.Edge getEdge( Predicate edgePredicate )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( edgePredicate );
            logger.log( level, "Begin getEdge( " + argString + " )" );
            Graph.Edge edge = delegate.getEdge( edgePredicate );
            logger.log( level, "End getEdge( " + argString + " ), returns " + edge );
            return edge;
        }
        return delegate.getEdge( edgePredicate );
    }


    public Object getAdjacentNode( Object node,
                                   Predicate traverserPredicate )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = node + ", " + traverserPredicate;
            logger.log( level, "Begin getAdjacentNode( " + argString + " )" );
            Object adjNode = delegate.getAdjacentNode( node, traverserPredicate );
            logger.log( level, "End getAdjacentNode( " + argString + " ), returns " + adjNode );
            return adjNode;
        }
        return delegate.getAdjacentNode( node, traverserPredicate );
    }


    public Graph.Edge getIncidentEdge( Object node,
                                       Predicate traverserPredicate )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = node + ", " + traverserPredicate;
            logger.log( level, "Begin getIncidentEdge( " + argString + " )" );
            Graph.Edge incEdge = delegate.getIncidentEdge( node, traverserPredicate );
            logger.log( level, "End getIncidentEdge( " + argString + " ), returns " + incEdge );
            return incEdge;
        }
        return delegate.getIncidentEdge( node, traverserPredicate );
    }


    public Traverser traverser( Object node,
                                Predicate traverserPredicate )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = node + ", " + traverserPredicate;
            logger.log( level, "Begin traverser( " + argString + " )" );
            Traverser traverser = delegate.traverser( node, traverserPredicate );
            logger.log( level, "End traverser( " + argString + " ), returns " + traverser );
            return new LoggingTraverser( traverser, logger, level );
        }
        // We still wrap the Traverser since logging may actually
        // become enabled during its use.
        return new LoggingTraverser( delegate.traverser( node, traverserPredicate ),
                                     logger, level );
    }


    ////////////////////////////////////////
    // ObservableGraph
    ////////////////////////////////////////


    /**
     *  Adds the specified <code>GraphListener</code> which will be
     *  notified whenever this <code>ObservableGraph's</code>
     *  structure changes.  If the wrapped graph does not implement
     *  {@link ObservableGraph}, then this method with throw an
     *  <code>UnsupportedOperationException</code>.
     */
    public void addGraphListener( GraphListener listener )
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin addGraphListener( " + listener + " )" );
        }
        if( observableDelegate == null ) {
            throw new UnsupportedOperationException( "Wrapped graph is not observable." );
        }
        observableDelegate.addGraphListener( listener );
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "End addGraphListener( " + listener + " )" );
        }
    }


    /**
     *  Removes a previously added <code>GraphListener</code>.  If the
     *  wrapped graph does not implement {@link ObservableGraph}, then
     *  this method with throw an
     *  <code>UnsupportedOperationException</code>.
     */
    public void removeGraphListener( GraphListener listener )
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin removeGraphListener( " + listener + " )" );
        }
        if( observableDelegate == null ) {
            throw new UnsupportedOperationException( "Wrapped graph is not observable." );
        }
        observableDelegate.removeGraphListener( listener );
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "End removeGraphListener( " + listener + " )" );
        }
    }


    ////////////////////////////////////////
    // Object
    ////////////////////////////////////////


    public String toString()
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin toString()" );
            String string = delegate.toString();
            logger.log( level, "End toString(), returns " + string );
            return string;
        }
        return delegate.toString();
    }

}
