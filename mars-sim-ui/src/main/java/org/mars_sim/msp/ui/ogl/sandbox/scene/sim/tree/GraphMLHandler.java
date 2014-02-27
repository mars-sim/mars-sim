package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX Parser for GraphML data files.
 */
public class GraphMLHandler
extends DefaultHandler {

	public static final String ID         = "id";
	public static final String GRAPH      = "graph";
	public static final String EDGEDEF    = "edgedefault";
	public static final String DIRECTED   = "directed";
	public static final String UNDIRECTED = "undirected";

	public static final String KEY        = "key";
	public static final String FOR        = "for";
	public static final String ALL        = "all";
	public static final String ATTRNAME   = "attr.name";
	public static final String ATTRTYPE   = "attr.type";
	public static final String DEFAULT    = "default";

	public static final String NODE   = "node";
	public static final String EDGE   = "edge";
	public static final String SOURCE = "source";
	public static final String TARGET = "target";
	public static final String DATA   = "data";
	public static final String TYPE   = "type";

	public static final String INT = "int";
	public static final String INTEGER = "integer";
	public static final String LONG = "long";
	public static final String FLOAT = "float";
	public static final String DOUBLE = "double";
	public static final String REAL = "real";
	public static final String BOOLEAN = "boolean";
	public static final String STRING = "string";
	public static final String DATE = "date";

	protected static final String SRC = "source";
	protected static final String TRG = "target";
	protected static final String SRCID = SRC + "_" + ID;
	protected static final String TRGID = TRG + "_" + ID;

	protected Tree tree = new Tree();
	protected StringBuffer buffer = new StringBuffer();

	protected String m_id;
	protected String m_for;
	protected String m_name;
	protected String m_type;
	protected String m_dflt;
	protected String m_key;

	protected TreeElement element;
	protected List<TreeEdge> edges = new ArrayList<TreeEdge>();

	@Override
	public void startDocument() {
		this.tree.clear();
	}

	@Override
	public void startElement(
		String namespaceURI,
		String localName,
		String qName,
		Attributes atts
	) {
		// first clear the string buffer
		this.buffer.delete(0,this.buffer.length());

		// check directionality
		if (qName.compareTo(GRAPH) == 0) {
			String edef = atts.getValue(EDGEDEF);
			this.tree.setDirected(DIRECTED.equalsIgnoreCase(edef));
			this.tree.setId(atts.getValue(ID));
		} else if (qName.compareTo(KEY) == 0) {
			this.m_for = atts.getValue(FOR);
			this.m_id = atts.getValue(ID);
			this.m_name = atts.getValue(ATTRNAME);
			this.m_type = atts.getValue(ATTRTYPE);
		}
		else if (qName.compareTo(NODE) == 0) {
			String id = atts.getValue(ID);
			this.element = tree.addNode(id);
		}
		else if (qName.compareTo(EDGE) == 0) {
			String fonto_id = atts.getValue(SRC);
			String celo_id = atts.getValue(TRG);
			this.element = new TreeEdge();
			this.element.setParam(SRCID,fonto_id);
			this.element.setParam(TRGID,celo_id);
		} else if (qName.compareTo(DATA) == 0) {
			this.m_key = atts.getValue(KEY);
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) {
		if (qName.compareTo(DEFAULT) == 0) {
			// value is in the buffer
			this.m_dflt = this.buffer.toString();
		} else if (qName.compareTo(KEY) == 0) {
			// add column to schema declarations
			addToSchema();
		} else if (qName.compareTo(DATA) == 0) {
			// value is in the buffer
			String value = this.buffer.toString();
			String name = this.m_key;
			Class<?> type = (this.element instanceof TreeNode) ?
				tree.getSchemaNodes().get(name) :
				tree.getSchemaEdges().get(name)
			;
			try {
				Object val = parse(value,type);
				element.setParam(name,val);
			} catch (Exception e) {
				error(e);
			}
		} else if (qName.compareTo(NODE) == 0) {
			this.element = null;
		} else if (qName.compareTo(EDGE) == 0) {
			edges.add((TreeEdge) this.element);
			this.element = null;
		}
	}

	@Override
	public void endDocument() throws SAXException {
		for (TreeEdge eĝo : edges) {
			String fonto = eĝo.getParamString(SRCID);
			String celo = eĝo.getParamString(TRGID);
			eĝo.removeParam(SRCID);
			eĝo.removeParam(TRGID);
			TreeNode n1 = tree.getNode(fonto);
			TreeNode n2 = tree.getNode(celo);
			eĝo.setSource(n1);
			eĝo.setTarget(n2);
			if (n1 != null && n2 != null) {
				tree.addEdge(eĝo);
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		this.buffer.append(ch, start, length);
	}

	protected void addToSchema() {
		if (m_name == null || m_name.length() == 0) {
			error("Empty "+KEY+" name.");
		}
		if (m_type == null || m_type.length() == 0) {
			error("Empty "+KEY+" type.");
		}

		Class<?> type = parseType(m_type);
		if (m_for == null || m_for.compareTo(ALL) == 0) {
			tree.addNodeColumn(m_name, type);
			tree.addEdgeColumn(m_name, type);
		} else if ( m_for.equals(NODE) ) {
			tree.addNodeColumn(m_name, type);
		} else if ( m_for.equals(EDGE) ) {
			tree.addEdgeColumn(m_name, type);
		} else {
			error("Unrecognized \""+FOR+"\" value: "+ m_for);
		}
		m_dflt = null;
	}

	protected Class parseType(String type) {
		type = type.toLowerCase();
		if (type.equals(INT) || type.equals(INTEGER)) {
			return int.class;
		} else if ( type.equals(LONG) ) {
			return long.class;
		} else if ( type.equals(FLOAT) ) {
			return float.class;
		} else if ( type.equals(DOUBLE) || type.equals(REAL)) {
			return double.class;
		} else if ( type.equals(BOOLEAN) ) {
			return boolean.class;
		} else if ( type.equals(STRING) ) {
			return String.class;
		} else if ( type.equals(DATE) ) {
			return Date.class;
		} else {
			error("Unrecognized data type: "+type);
			return null;
		}
	}

	protected Object parse(String s, Class type) {
		if (type == int.class) {
			return Integer.parseInt(s);
		} else if (type == boolean.class) {
			return Boolean.parseBoolean(s);
		} else if (type == String.class) {
			return s;
		} else if (type == float.class) {
			return Float.parseFloat(s);
		} else if (type == double.class) {
			return Double.parseDouble(s);
		} else {
			return null;
		}
	}

	protected void error(String s) {
		throw new RuntimeException(s);
	}

	protected void error(Exception e) {
		throw new RuntimeException(e);
	}

	public Tree getArbo() {
		return this.tree;
	}
}
