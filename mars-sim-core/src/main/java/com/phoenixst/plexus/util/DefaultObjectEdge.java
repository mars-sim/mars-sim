/*
 *  $Id: DefaultObjectEdge.java,v 1.9 2005/10/03 15:20:46 rconner Exp $
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

package com.phoenixst.plexus.util;

import com.phoenixst.plexus.GraphUtils;


/**
 *  A default {@link com.phoenixst.plexus.Graph.Edge} implementation.
 *  The {@link #equals(Object) equals()} method is inherited from
 *  <code>Object</code> and uses reference equality.  This class
 *  should only be used by <code>Graphs</code> which create edges once
 *  and store them.
 *
 *  @version    $Revision: 1.9 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class DefaultObjectEdge extends DefaultEdge
{

    private static final long serialVersionUID = 2L;


    /**
     *  The user-defined object contained in this <code>Edge</code>.
     *
     *  @serial
     */
    private Object object;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>DefaultObjectEdge</code>.
     */
    public DefaultObjectEdge( Object object, Object tail, Object head, boolean directed )
    {
        super( tail, head, directed );
        this.object = object;
    }


    ////////////////////////////////////////
    // Edge methods
    ////////////////////////////////////////


    public Object getUserObject()
    {
        return object;
    }


    public void setUserObject( Object object )
    {
        this.object = object;
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    public String toString()
    {
        return GraphUtils.getTextValue( this, true ).toString();
    }

}
