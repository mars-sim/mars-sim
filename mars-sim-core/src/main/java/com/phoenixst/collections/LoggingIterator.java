/*
 *  $Id: LoggingIterator.java,v 1.9 2006/06/05 20:32:49 rconner Exp $
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

package com.phoenixst.collections;

import java.util.Iterator;

import org.apache.log4j.*;


/**
 *  An <code>Iterator</code> which wraps another to provide logging
 *  support.
 *
 *  @version    $Revision: 1.9 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class LoggingIterator
    implements Iterator
{

    /**
     *  The default logger.
     */
    private static final Logger DEFAULT_LOGGER = Logger.getLogger( LoggingIterator.class );

    /**
     *  The wrapped iterator.
     */
    private final Iterator delegate;

    /**
     *  The logger to use.
     */
    private final Logger logger;

    /**
     *  The level at which to log.
     */
    private final Level level;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>LoggingIterator</code> which
     *  logs to a category corresponding to this class at
     *  the DEBUG level.
     */
    public LoggingIterator( Iterator delegate )
    {
        this( delegate, DEFAULT_LOGGER, Level.DEBUG );
    }


    /**
     *  Creates a new <code>LoggingIterator</code> which uses
     *  the specified log at the DEBUG level.
     */
    public LoggingIterator( Iterator delegate, Logger logger )
    {
        this( delegate, logger, Level.DEBUG );
    }


    /**
     *  Creates a new <code>LoggingIterator</code>.
     */
    public LoggingIterator( Iterator delegate, Logger logger, Level level )
    {
        this.delegate = delegate;
        this.logger = logger;
        this.level = level;
        if( delegate == null ) {
            throw new IllegalArgumentException( "Delegate Iterator is null." );
        }
        if( logger == null ) {
            throw new IllegalArgumentException( "Logger is null." );
        }
        if( level == null ) {
            throw new IllegalArgumentException( "Level is null." );
        }
    }


    ////////////////////////////////////////
    // Protected methods
    ////////////////////////////////////////


    /**
     *  Provides access to internal state so it can be used
     *  by extensions of this class.
     */
    protected final Iterator getDelegate()
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
    // Iterator
    ////////////////////////////////////////


    public boolean hasNext()
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin hasNext()" );
            boolean hasNext = delegate.hasNext();
            logger.log( level, "End hasNext(), returns " + hasNext );
            return hasNext;
        }
        return delegate.hasNext();
    }


    public Object next()
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin next()" );
            Object next = delegate.next();
            logger.log( level, "End next(), returns " + next );
            return next;
        }
        return delegate.next();
    }


    public void remove()
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin remove()" );
            delegate.remove();
            logger.log( level, "End remove()" );
            return;
        }
        delegate.remove();
    }

}
