/*
 *  $Id: DefaultTraverserFactory.java,v 1.14 2006/06/07 20:25:53 rconner Exp $
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

import java.io.*;

import org.apache.commons.collections.*;

import com.phoenixst.plexus.Graph;


/**
 *  A <code>Transformer</code> which when given a node, returns a
 *  {@link com.phoenixst.plexus.Traverser} over nodes adjacent to that
 *  node, specified by a <code>Graph</code> and a
 *  <code>Predicate</code>.
 *
 *  @version    $Revision: 1.14 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class DefaultTraverserFactory
    implements Transformer,
               Serializable
{

    private static final long serialVersionUID = 1L;


    ////////////////////////////////////////
    // Instance Fields
    ////////////////////////////////////////


    /**
     *  @serial
     */
    private final Graph graph;

    /**
     *  @serial
     */
    private final Predicate traverserPredicate;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>DefaultTraverserFactory</code>.
     */
    public DefaultTraverserFactory( Graph graph, Predicate traverserPredicate )
    {
        super();
        this.graph = graph;
        this.traverserPredicate = traverserPredicate;
        if( graph == null ) {
            throw new IllegalArgumentException( "Graph is null." );
        }
        if( traverserPredicate == null ) {
            throw new IllegalArgumentException( "Traverser predicate is null." );
        }
    }


    ////////////////////////////////////////
    // Serialization
    ////////////////////////////////////////


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( graph == null ) {
            throw new InvalidObjectException( "Graph is null." );
        }
        if( traverserPredicate == null ) {
            throw new InvalidObjectException( "Traverser predicate is null." );
        }
    }


    ////////////////////////////////////////
    // Transformer
    ////////////////////////////////////////


    public Object transform( Object node )
    {
        return graph.traverser( node, traverserPredicate );
    }


    ////////////////////////////////////////
    // Get Methods
    ////////////////////////////////////////


    /**
     *  Gets the <code>Graph</code> for this
     *  <code>DefaultTraverserFactory</code>.
     */
    public Graph getGraph()
    {
        return graph;
    }


    /**
     *  Gets the <code>Predicate</code> for this
     *  <code>DefaultTraverserFactory</code>.
     */
    public Predicate getTraverserPredicate()
    {
        return traverserPredicate;
    }

}
