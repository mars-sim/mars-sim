package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.xml.sax.SAXException;

public class CodecTreeGraphML {

	public static Tree read(InputStream is) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
			GraphMLHandler handler = new GraphMLHandler();
			try {
				saxParser.parse(is, handler);
			} catch (SAXException e) {
				Util.handle(e);
			} catch (IOException e) {
				Util.handle(e);
			}
			return handler.getArbo();
		} catch (ParserConfigurationException e1) {
			Util.handle(e1);
		} catch (SAXException e1) {
			Util.handle(e1);
		}
		return null;
	}

	/**
	 * @param tree {@link Tree}
	 * @return {@link String}
	 */
	public static String write(Tree tree, boolean includeSourceAndTarget) {
		return write(tree,includeSourceAndTarget,"UTF-8");
	}
	
	/**
	 * @param tree {@link Tree}
	 * @param codec {@link String}
	 * @return {@link String}
	 */
	public static String write(Tree tree, boolean includeSourceAndTarget, String codec) {
		StringBuffer s = new StringBuffer();
		s.append("<?xml version=\"1.0\" encoding=\"");
		s.append(codec);
		s.append("\"?>\n");
		s.append("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		s.append("<graph edgedefault=\"");
		if (!tree.isDirected()) {
			s.append("un");
		}
		s.append("directed\">\n\n");
		s.append("<!-- node column declarations -->\n");
		for (Entry<String,Class<?>> entry : tree.getSchemaNodes().entrySet()) {
			String name = entry.getKey();
			Class<?> c = entry.getValue();
			String className = c.getSimpleName();
			int punktpozicio = className.lastIndexOf(".");
			if (punktpozicio >= 0) {
				className = className.substring(punktpozicio + 1);
			}
			s.append("<key id=\"");
			s.append(name);
			s.append("\" for=\"node\" attr.name=\"");
			s.append(name);
			s.append("\" attr.type=\"");
			s.append(className);
			s.append("\"/>\n");
		}
		s.append("\n<!-- edge data column declarations -->\n");
		for (Entry<String,Class<?>> entry : tree.getSchemaEdges().entrySet()) {
			String name = entry.getKey();
			boolean show = true;
			if (name.compareTo(Tree.PARAM_EDGE_SOURCE) == 0 || name.compareTo(Tree.PARAM_EDGE_TARGET) == 0) {
				show = includeSourceAndTarget;
			}
			if (show) {
				Class<?> c = entry.getValue();
				String className = c.getSimpleName();
				int dotIndex = className.lastIndexOf(".");
				if (dotIndex >= 0) {
					className = className.substring(dotIndex + 1);
				}
				s.append("<key id=\"");
				s.append(name);
				s.append("\" for=\"edge\" attr.name=\"");
				s.append(name);
				s.append("\" attr.type=\"");
				s.append(className);
				s.append("\"/>\n");
			}
		}
		s.append("\n<!-- nodes of the tree -->\n");
		for (TreeNode node : tree.getNodes().values()) {
			s.append(nodeToGraphML(node,tree.getSchemaNodes()));
		}
		s.append("\n<!-- edges of the tree -->\n");
		for (TreeEdge edge : tree.getEdges()) {
			s.append(edgeToGraphML(edge,tree.getSchemaEdges(),includeSourceAndTarget));
		}
		s.append("\n</graph>\n");
		s.append("</graphml>\n");
		return s.toString();
	}
	
	public static String edgeToGraphML(
		TreeEdge edge,
		Map<String, Class<?>> columnsEdges,
		boolean includeSourceAndTarget
	) {
		StringBuffer s = new StringBuffer();
		s.append("<edge source=\"");
		s.append(edge.getSource().getId());
		s.append("\" target=\"");
		s.append(edge.getTarget().getId());
		s.append("\">");
		for (Entry<String,Class<?>> entry : columnsEdges.entrySet()) {
			String column = entry.getKey();
			boolean show = true;
			if (column.compareTo(Tree.PARAM_EDGE_SOURCE) == 0 || column.compareTo(Tree.PARAM_EDGE_TARGET) == 0) {
				show = includeSourceAndTarget;
			}
			if (show) {
				s.append("<data key=\"");
				s.append(column);
				s.append("\">");
				// TODO take care of parameter types other than string ...
				s.append(edge.getParam(column).toString().trim());
				s.append("</data>");
			}
		}
		s.append("</edge>\n");
		return s.toString();
	}

	public static String nodeToGraphML(
		TreeNode node,
		Map<String, Class<?>> columnsNodes
	) {
		StringBuffer s = new StringBuffer();
		s.append("<node id=\"");
		s.append(node.getId());
		s.append("\">");
		for (Entry<String,Class<?>> entry : columnsNodes.entrySet()) {
			String column = entry.getKey();
			if (column.compareTo(Tree.PARAM_NODE_ID) != 0) {
				s.append("<data key=\"");
				s.append(column);
				s.append("\">");
				s.append(node.getParam(column));
				s.append("</data>");
			}
		}
		s.append("</node>\n");
		return s.toString();
	}

	/**
	 * @param sourcePath {@link String}
	 * @return {@link Tree}
	 */
	public static Tree read(String sourcePath) {
		InputStream is = null;
		try {
			is = new FileInputStream(
				new File(
					sourcePath
				)
			);
		} catch (FileNotFoundException e) {
			Util.handle(e);
		}
		return CodecTreeGraphML.read(is);
	}
}
