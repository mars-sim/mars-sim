/*
 *  $Id: Identifier.java,v 1.5 2005/10/03 15:11:54 rconner Exp $
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

package com.phoenixst.collections;


/**
 *  Basically, this is just an <code>Object</code> with a {@link
 *  #toString} value set by the constructor.  Instances of this class
 *  can be used instead of <code>Strings</code> for map and registry
 *  keys.  The reason for doing so is that this class uses reference
 *  equality for <code>.equals()</code> semantics, so that multiple
 *  users can use the same registry without fear of duplicating
 *  someone else's key.  Instances can also simply be used where it's
 *  helpful to have human readable <code>toString()</code> values for
 *  otherwise non-functional objects when <code>Strings</code> can't
 *  be used for some reason.
 *
 *  @version    $Revision: 1.5 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class Identifier
    implements java.io.Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The {@link #toString} value for this <code>Identifier</code>.
     *
     *  @serial
     */
    private final String name;


    /**
     *  Creates a new <code>Identifier</code> with the specified
     *  {@link #toString} value.
     */
    public Identifier( String name )
    {
        super();
        this.name = name;
    }


    public String toString()
    {
        return name;
    }

}
