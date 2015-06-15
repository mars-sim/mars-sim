package com.jme3x.jfx.util;

import java.awt.Point;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.util.BufferUtils;
import com.jme3x.jfx.util.os.OperatingSystem;

/**
 * Set of methods for scrap work JFX.
 * 
 * @author Ronn
 */
public class JFXUtils {

	public static final String PROP_DISPLAY_UNDECORATED = "org.lwjgl.opengl.Window.undecorated";

	private static final Map<String, Point> OFFSET_MAPPING = new HashMap<>();

	static {
		OFFSET_MAPPING.put("Ubuntu 14.04 LTS (trusty)", new Point(10, 37));
		OFFSET_MAPPING.put("Ubuntu 14.04.1 LTS (trusty)", new Point(10, 37));
	}

	/**
	 * Getting the size of the window decorations in the system.
	 */
	public static final Point getWindowDecorationSize() {

		if("true".equalsIgnoreCase(System.getProperty(PROP_DISPLAY_UNDECORATED))) {
			return new Point(0, 0);
		}

		OperatingSystem system = new OperatingSystem();

		if(OFFSET_MAPPING.containsKey(system.getDistribution())) {
			return OFFSET_MAPPING.get(system.getDistribution());
		}

		return new Point(3, 25);
	}
	
	
    /**
     * Gets the triangle vertex data at the given triangle index 
     * and stores them into the v1, v2, v3 arguments. Works for 3-value components like position or normals
     * 
     * @param type buffer type to retrieve data from 
     * @param index The index of the triangle. 
     * Should be between 0 and {@link #getTriangleCount()}.
     * 
     * @param v1 Vector to contain first vertex data
     * @param v2 Vector to contain second vertex data
     * @param v3 Vector to contain third vertex data
     */
    public static void getTriangle(Mesh mesh, Type type, int index, Vector3f v1, Vector3f v2, Vector3f v3){
        VertexBuffer pb = mesh.getBuffer(type);
        IndexBuffer ib = mesh.getIndicesAsList();
        if (pb != null && pb.getFormat() == Format.Float && pb.getNumComponents() == 3){
            FloatBuffer fpb = (FloatBuffer) pb.getData();

            // acquire triangle's vertex indices
            int vertIndex = index * 3;
            int vert1 = ib.get(vertIndex);
            int vert2 = ib.get(vertIndex+1);
            int vert3 = ib.get(vertIndex+2);

            BufferUtils.populateFromBuffer(v1, fpb, vert1);
            BufferUtils.populateFromBuffer(v2, fpb, vert2);
            BufferUtils.populateFromBuffer(v3, fpb, vert3);
        }else{
            throw new UnsupportedOperationException(type + " buffer not set or "
                                                  + " has incompatible format");
        }
    }
    
    /**
     * Gets the triangle vertex data at the given triangle index 
     * and stores them into the v1, v2, v3 arguments. Works for 2-value components like texture coordinates
     * 
     * @param type buffer type to retrieve data from 
     * @param index The index of the triangle. 
     * Should be between 0 and {@link #getTriangleCount()}.
     * 
     * @param v1 Vector to contain first vertex data
     * @param v2 Vector to contain second vertex data
     * @param v3 Vector to contain third vertex data
     */

    public static void getTriangle(Mesh mesh, Type type, int index, Vector2f v1, Vector2f v2, Vector2f v3){
        VertexBuffer pb = mesh.getBuffer(type);
        IndexBuffer ib = mesh.getIndicesAsList();
        if (pb != null && pb.getFormat() == Format.Float && pb.getNumComponents() == 2){
            FloatBuffer fpb = (FloatBuffer) pb.getData();

            // acquire triangle's vertex indices
            int vertIndex = index * 3;
            int vert1 = ib.get(vertIndex);
            int vert2 = ib.get(vertIndex+1);
            int vert3 = ib.get(vertIndex+2);

            BufferUtils.populateFromBuffer(v1, fpb, vert1);
            BufferUtils.populateFromBuffer(v2, fpb, vert2);
            BufferUtils.populateFromBuffer(v3, fpb, vert3);
        }else{
            throw new UnsupportedOperationException(type + " buffer not set or "
                                                  + " has incompatible format");
        }
    }
    
	
	
}
