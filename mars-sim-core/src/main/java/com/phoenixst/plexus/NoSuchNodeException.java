/*
 *  $Id: NoSuchNodeException.java,v 1.5 2005/10/03 15:24:00 rconner Exp $
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


/**
 *  Thrown by a {@link Graph} method when an argument node is not
 *  found in the <code>Graph</code>, but the method cannot provide a
 *  meaningful result or perform its function without one.
 *
 *  @version    $Revision: 1.5 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class NoSuchNodeException extends RuntimeException
{

    private static final long serialVersionUID = 1L;


    /**
     *  Constructs a new <code>NoSuchNodeException</code> with
     *  <code>null</code> as its detail message and no cause.
     */
    public NoSuchNodeException()
    {
        super();
    }


    /**
     *  Constructs a new <code>NoSuchNodeException</code> with the
     *  specified detail message and no cause.
     *
     *  @param message the detail message.
     */
    public NoSuchNodeException( String message )
    {
        super( message );
    }


    /**
     *  Constructs a new <code>NoSuchNodeException</code> with the
     *  specified cause.
     *
     *  @param cause the cause.
     */
    public NoSuchNodeException( Throwable cause )
    {
        super( cause );
    }


    /**
     *  Constructs a new <code>NoSuchNodeException</code> with the
     *  specified detail message and cause.
     *
     *  @param message the detail message.
     *
     *  @param cause the cause.
     */
    public NoSuchNodeException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
