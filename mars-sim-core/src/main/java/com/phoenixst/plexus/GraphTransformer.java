/*
 *  $Id: GraphTransformer.java,v 1.30 2006/06/07 21:08:23 rconner Exp $
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

package com.phoenixst.plexus;

import java.io.*;

import com.phoenixst.collections.InvertibleTransformer;


/**
 *  A <code>Graph</code> which wraps another, transforming its nodes
 *  and edges in some way.
 *
 *  @version    $Revision: 1.30 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class GraphTransformer extends GraphWrapper
    implements Serializable
{

    private static final long serialVersionUID = 2L;

    /**
     *  The node transformer from this <code>Graph</code> to the
     *  wrapped one.
     *
     *  @serial
     */
    private InvertibleTransformer nodeTransformer;

    /**
     *  The edge transformer from this <code>Graph</code> to the
     *  wrapped one.
     *
     *  @serial
     */
    private InvertibleTransformer edgeTransformer;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Constructs a new <code>GraphTransformer</code> with no
     *  <code>Transformers</code> currently set.
     *
     *  @param delegate the <code>Graph</code> for which this is a
     *  transformed view.
     */
    public GraphTransformer( Graph delegate )
    {
        this( delegate, null, null );
    }


    /**
     *  Constructs a new <code>GraphTransformer</code> with the
     *  specified <code>Transformers</code>.
     *
     *  @param delegate the <code>Graph</code> for which this is a
     *  transformed view.
     *
     *  @param nodeTransformer the invertible node transformer from
     *  this <code>Graph</code> to the wrapped one.
     */
    public GraphTransformer( Graph delegate,
                             InvertibleTransformer nodeTransformer )
    {
        this( delegate, nodeTransformer, null );
    }


    /**
     *  Constructs a new <code>GraphTransformer</code> with the
     *  specified <code>Transformers</code>.
     *
     *  @param delegate the <code>Graph</code> for which this is a
     *  transformed view.
     *
     *  @param nodeTransformer the invertible node transformer from
     *  this <code>Graph</code> to the wrapped one.
     *
     *  @param edgeTransformer the invertible edge transformer from
     *  this <code>Graph</code> to the wrapped one.
     */
    public GraphTransformer( Graph delegate,
                             InvertibleTransformer nodeTransformer,
                             InvertibleTransformer edgeTransformer )
    {
        super( delegate );
        this.nodeTransformer = nodeTransformer;
        this.edgeTransformer = edgeTransformer;
    }


    ////////////////////////////////////////
    // Serialization
    ////////////////////////////////////////


    /**
     *  Serialize this <code>GraphWrapper</code>.
     *
     *  @serialData default, superclass delegate
     */
    private void writeObject( ObjectOutputStream out )
        throws IOException
    {
        out.defaultWriteObject();
        // Manually serialize superclass state
        out.writeObject( getDelegate() );
    }


    /**
     *  Deserialize this <code>GraphWrapper</code>.
     *
     *  @serialData default, superclass delegate
     */
    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        // Manually deserialize and init superclass state
        Graph delegate = (Graph) in.readObject();
        initialize( delegate );
    }


    ////////////////////////////////////////
    // Protected wrap/unwrap methods
    ////////////////////////////////////////


    protected Object wrapNode( Object node )
    {
        return (nodeTransformer != null)
            ? nodeTransformer.transform( node )
            : node;
    }


    protected Object unwrapNode( Object node )
    {
        return (nodeTransformer != null)
            ? nodeTransformer.untransform( node )
            : node;
    }


    protected Object wrapEdgeObject( Object edgeObject )
    {
        return (edgeTransformer != null)
            ? edgeTransformer.transform( edgeObject )
            : edgeObject;
    }


    protected Object unwrapEdgeObject( Object edgeObject )
    {
        return (edgeTransformer != null)
            ? edgeTransformer.untransform( edgeObject )
            : edgeObject;
    }


    ////////////////////////////////////////
    // Transformer get/set methods
    ////////////////////////////////////////


    /**
     *  Gets the node transformer.
     */
    public InvertibleTransformer getNodeTransformer()
    {
        return nodeTransformer;
    }


    /**
     *  Sets the node transformer.
     */
    public void setNodeTransformer( InvertibleTransformer nodeTransformer )
    {
        this.nodeTransformer = nodeTransformer;
    }


    /**
     *  Gets the edge transformer.
     */
    public InvertibleTransformer getEdgeTransformer()
    {
        return edgeTransformer;
    }


    /**
     *  Sets the edge transformer.
     */
    public void setEdgeTransformer( InvertibleTransformer edgeTransformer )
    {
        this.edgeTransformer = edgeTransformer;
    }

}
