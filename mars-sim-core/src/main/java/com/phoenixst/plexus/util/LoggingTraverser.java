/*
 *  $Id: LoggingTraverser.java,v 1.3 2006/06/05 20:32:49 rconner Exp $
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

import org.apache.log4j.*;

import com.phoenixst.collections.LoggingIterator;
import com.phoenixst.plexus.*;


/**
 *  A <code>Traverser</code> which wraps another to provide logging
 *  support.
 *
 *  @version    $Revision: 1.3 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class LoggingTraverser extends LoggingIterator
    implements Traverser
{

    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>LoggingTraverser</code> which
     *  logs to a category corresponding to this class at
     *  the DEBUG level.
     */
    public LoggingTraverser( Traverser delegate )
    {
        super( delegate );
    }


    /**
     *  Creates a new <code>LoggingTraverser</code> which uses
     *  the specified log at the DEBUG level.
     */
    public LoggingTraverser( Traverser delegate, Logger logger )
    {
        super( delegate, logger );
    }


    /**
     *  Creates a new <code>LoggingTraverser</code>.
     */
    public LoggingTraverser( Traverser delegate, Logger logger, Level level )
    {
        super( delegate, logger, level );
    }


    ////////////////////////////////////////
    // Traverser
    ////////////////////////////////////////


    public Graph.Edge getEdge()
    {
        if( getLogger().isEnabledFor( getLevel() ) ) {
            getLogger().log( getLevel(), "Begin getEdge()" );
            Graph.Edge edge = ((Traverser) getDelegate()).getEdge();
            getLogger().log( getLevel(), "End getEdge(), returns " + edge );
            return edge;
        }
        return ((Traverser) getDelegate()).getEdge();
    }


    public void removeEdge()
    {
        if( getLogger().isEnabledFor( getLevel() ) ) {
            getLogger().log( getLevel(), "Begin remove()" );
            ((Traverser) getDelegate()).removeEdge();
            getLogger().log( getLevel(), "End remove()" );
            return;
        }
        ((Traverser) getDelegate()).removeEdge();
    }

}
