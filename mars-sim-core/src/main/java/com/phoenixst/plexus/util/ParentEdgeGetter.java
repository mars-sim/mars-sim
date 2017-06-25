/*
 *  $Id: ParentEdgeGetter.java,v 1.9 2006/06/07 20:25:53 rconner Exp $
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

import org.apache.commons.collections.Transformer;

import com.phoenixst.plexus.OrientedForest;


/**
 *  A <code>Transformer</code> which when given a node, returns the
 *  parent <code>Edge</code> of that node, specified by an
 *  <code>OrientedForest</code>.
 *
 *  @version    $Revision: 1.9 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class ParentEdgeGetter
    implements Transformer,
               Serializable
{

    private static final long serialVersionUID = 2L;


    ////////////////////////////////////////
    // Instance Fields
    ////////////////////////////////////////


    /**
     *  @serial
     */
    private final OrientedForest forest;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>ParentEdgeGetter</code>.
     */
    public ParentEdgeGetter( OrientedForest forest )
    {
        super();
        this.forest = forest;
        if( forest == null ) {
            throw new IllegalArgumentException( "Forest is null." );
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
        if( forest == null ) {
            throw new InvalidObjectException( "Forest is null." );
        }
    }


    ////////////////////////////////////////
    // Transformer
    ////////////////////////////////////////


    public Object transform( Object node )
    {
        return forest.getParentEdge( node );
    }


    ////////////////////////////////////////
    // Get Methods
    ////////////////////////////////////////


    /**
     *  Gets the <code>OrientedForest</code> for this
     *  <code>ParentEdgeGetter</code>.
     */
    public OrientedForest getOrientedForest()
    {
        return forest;
    }

}
