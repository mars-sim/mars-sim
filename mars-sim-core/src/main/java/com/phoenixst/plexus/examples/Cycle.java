/*
 *  $Id: Cycle.java,v 1.20 2005/10/03 15:14:43 rconner Exp $
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

package com.phoenixst.plexus.examples;


/**
 *  A <code>Graph</code> containing a set of <code>Integer</code>
 *  nodes connected by a path of edges from the first node to the last
 *  one, and then back to the first, making a cycle.
 *
 *  @version    $Revision: 1.20 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class Cycle extends LoopGraph
{

    private static final long serialVersionUID = 2L;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>Cycle</code>.
     */
    public Cycle( int n )
    {
        super( n, 1 );
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "Cycle( " );
        s.append( getNodeSize() );
        s.append( " )" );
        return s.toString();
    }

}
