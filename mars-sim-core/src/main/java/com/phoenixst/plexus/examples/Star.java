/*
 *  $Id: Star.java,v 1.12 2006/06/07 19:56:36 rconner Exp $
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

package com.phoenixst.plexus.examples;

import java.io.*;

import com.phoenixst.plexus.GraphTransformer;
import com.phoenixst.plexus.operations.Join;


/**
 *  A star graph with one central node and <code>n</code> outer nodes.
 *
 *  @version    $Revision: 1.12 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class Star extends Join
{

    private static final long serialVersionUID = 3L;

    private transient int n;


    /**
     *  Creates a new <code>Star</code>.
     */
    public Star( int n )
    {
        super( new EmptyGraph( 1 ),
               new GraphTransformer( new EmptyGraph( n ),
                                     new IntegerOffsetTransformer( 1 ) ),
               true );
        this.n = n;
    }


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();

        // We could do a lot more checking in here, but why bother?
        // It's only an example after all.
        n = getRightOperand().nodes( null ).size();
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "Star( " );
        s.append( n );
        s.append( " )" );
        return s.toString();
    }

}
