/*
 *  $Id: GraphEvent.java,v 1.9 2005/10/03 15:24:00 rconner Exp $
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

package com.phoenixst.plexus;

import java.util.EventObject;


/**
 *  An event that indicates a structural change in a {@link Graph}.
 *
 *  @version    $Revision: 1.9 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class GraphEvent extends EventObject
{

    private static final long serialVersionUID = 1L;


    /**
     *  The node or edge that was added or removed.
     *
     *  @serial
     */
    private final Object object;


    /**
     *  Constructs a new <code>GraphEvent</code>.
     */
    public GraphEvent( Graph source,
                       Object object )
    {
        super( source );
        this.object = object;
    }


    public Object getObject()
    {
        return object;
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( getClass().getName() );
        s.append( "[graph=" );
        s.append( source );
        s.append( ", " );
        s.append( "object=" );
        s.append( object );
        s.append( "]" );
        return s.toString();
    }

}
