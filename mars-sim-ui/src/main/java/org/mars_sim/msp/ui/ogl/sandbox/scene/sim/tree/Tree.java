package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;

public class Tree {

	protected final static double PI180 = Math.PI / 180.0f;

	protected final static String PARAM_NODE_ID     = "id";
	protected final static String COLUMN_NODE_EDGES  = "edges";
	protected final static String COLUMN_NODE_DFS     = "dfs";
	protected final static String COLUMN_NODE_LOWLINK  = "lowlink";

	protected final static String PARAM_EDGE_SOURCE   = "source";
	protected final static String PARAM_EDGE_TARGET  = "target";

	protected final static Map<String,Class<?>> NECCESSARY_EDGE_COLUMNS = new HashMap<String,Class<?>>();
	protected final static Map<String,Class<?>> NECCESSARY_NODE_COLUMNS = new HashMap<String,Class<?>>();

	static {
		NECCESSARY_NODE_COLUMNS.put(PARAM_NODE_ID,  String.class);
		NECCESSARY_EDGE_COLUMNS.put(PARAM_EDGE_SOURCE, String.class);
		NECCESSARY_EDGE_COLUMNS.put(PARAM_EDGE_TARGET,  String.class);
	}

	protected Map<String,TreeNode> nodes = new HashMap<String,TreeNode>();
	protected List<TreeEdge> edges = new ArrayList<TreeEdge>();

	protected Map<String,Class<?>> schemaNodes = new HashMap<String,Class<?>>();
	protected Map<String,Class<?>> schemaEdges = new HashMap<String,Class<?>>();
	protected boolean directed = false;
	protected String id;

	/** constructor. */
	public Tree() {
		this("",false);
	}

	/** constructor. */
	public Tree(boolean direktita) {
		this("",direktita);
	}

	/** constructor. */
	public Tree(String id, boolean direktita) {
		addMinimumColumns();
		this.setDirected(direktita);
		this.setId(id);
	}

	/**
	 * constructor. uses the schema of the given
	 * tree for this new one.
	 * @param id {@link String}
	 * @param tree {@link Tree}
	 */
	public Tree(String id,Tree tree) {
		if (tree != null) {
			this.setDirected(tree.isDirected());
			this.setSchemaEdges(tree.getSchemaEdges());
			this.setSchemaNodes(tree.getSchemaNodes());
			this.setId(id);
		}
	}

	public void addEdgeColumn(String column,Class<?> type) {
		this.schemaEdges.put(column,type);
	}

	public void addEdge(TreeEdge edge) {
		this.edges.add(edge);
	}

	public TreeEdge addEdge(TreeNode node1, TreeNode node2) {
		TreeEdge edge = new TreeEdge(node1,node2);
		this.edges.add(edge);
		return edge;
	}

	public TreeEdge addEdgeToNode(TreeNode node1, TreeNode node2) {
		TreeEdge edge = addEdge(node1,node2);
		addEdgeToNode(node1,edge);
		addEdgeToNode(node2,edge);
		return edge;
	}

	public void addEdgeToNode(TreeNode node, TreeEdge edge) {
		List<TreeEdge> list = getEdgesFromNode(node);
		if (list == null) {
			list = new ArrayList<TreeEdge>();
		}
		if (!list.contains(edge)) {
			list.add(edge);
			node.setParam(Tree.COLUMN_NODE_EDGES,list);
		}
	}
	
	public TreeEdge addEdge(int x, int y) {
		return this.addEdge(Integer.toString(x),Integer.toString(y));
	}

	public TreeEdge addEdge(String id1, String id2) {
		TreeNode node1 = this.getNode(id1);
		TreeNode node2 = this.getNode(id2);
		return this.addEdge(node1,node2);
	}

	public void addMinimumColumns() {
		for (Entry<String,Class<?>> entry : NECCESSARY_NODE_COLUMNS.entrySet()) {
			addNodeColumn(entry.getKey(),entry.getValue());
		}
		for (Entry<String,Class<?>> entry : NECCESSARY_EDGE_COLUMNS.entrySet()) {
			addEdgeColumn(entry.getKey(),entry.getValue());
		}
	}

	public void addNodeColumn(String column,Class<?> type) {
		this.schemaNodes.put(column,type);
	}

	public void addNode(TreeNode node) {
		this.nodes.put(node.getId(),node);
	}

	public void addNode(String id, TreeNode node) {
		this.nodes.put(id,node);
	}

	/**
	 * creates a new {@link TreeNode}, inserts it into the tree
	 * and returns it.
	 * @return {@link TreeNode}
	 */
	public TreeNode addNode() {
		return addNode(Integer.toString(nodes.size()));
	}

	public TreeNode addNode(String id) {
		TreeNode node = new TreeNode(id);
		this.addNode(node);
		return node;
	}

	/**
	 * returns the value of the node's parameter
	 * {@link Tree#COLUMN_NODE_EDGES} for the given node.
	 * @param node {@link TreeNode}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<TreeEdge> getEdgesFromNode(TreeNode node) {
		return (List<TreeEdge>) node.getParam(Tree.COLUMN_NODE_EDGES);
	}

	/**
	 * returns those edges from the node parameter
	 * {@link Tree#COLUMN_NODE_EDGES} of the first
	 * node that also touch the second node.
	 * @param node1 {@link TreeNode}
	 * @param node2 {@link TreeNode}
	 * @return {@link List}<{@link TreeEdge}>
	 */
	public List<TreeEdge> getEdgesFromNode(TreeNode node1,TreeNode node2) {
		List<TreeEdge> list = new ArrayList<TreeEdge>();
		List<TreeEdge> edges = getEdges(node1);
		if (getEdges(node1) != null) {
			for (TreeEdge edge : edges) {
				if (edge.getTarget() == node2 || edge.getSource() == node2) {
					list.add(edge);
				}
			}
		}
		return list;
	}

	public List<TreeEdge> getEdges(TreeNode node) {
		List<TreeEdge> result = new ArrayList<TreeEdge>();
		for (TreeEdge edge : this.edges) {
			if (edge.getSource() == node) {
				result.add(edge);
			}
			if (!directed && edge.getTarget() == node) {
				result.add(edge);
			}
		}
		return result;
	}
	
	public List<TreeEdge> getEdges() {
		return edges;
	}

	/**
	 * gives a list of edges between node1 and node2.
	 * @param node1 {@link TreeNode}
	 * @param node2 {@link TreeNode}
	 * @return {@link Listo}<{@link TreeEdge}>
	 */
	public List<TreeEdge> getEdges(TreeNode node1,TreeNode node2) {
		List<TreeEdge> list = new ArrayList<TreeEdge>();
		for (TreeEdge edge : this.edges) {
			if (edge.getSource() == node1 && edge.getTarget() == node2) {
				list.add(edge);
			}
			if (!this.directed) {
				if (edge.getSource() == node2 && edge.getTarget() == node1) {
					list.add(edge);
				}
			}
		}
		return list;
	}

	public boolean hasEdge(TreeNode node1,TreeNode node2) {
		boolean found = false;
		int i = 0;
		while (i < this.edges.size() && !found) {
			TreeEdge edge = this.edges.get(i);
			if (edge.getSource() == node1 && edge.getTarget() == node2) {
				found = true;
			}
			i++;
		}
		if (!this.directed) {
			i = 0;
			while (i < this.edges.size() && !found) {
				TreeEdge edge = this.edges.get(i);
				if (edge.getSource() == node2 && edge.getTarget() == node1) {
					found = true;
				}
				i++;
			}
		}
		return found;
	}

	public TreeNode getNode(int id) {
		return this.getNode(Integer.toString(id));
	}

	public TreeNode getNode(String id) {
		return this.nodes.get(id);
	}

	/**
	 * returns the first node with the given
	 * parameter / value pair.
	 * @param parameter {@link String}
	 * @param value {@link Object}
	 * @return {@link TreeNode}
	 */
	public TreeNode getNode(String parameter, Object value) {
		TreeNode result = null;
		Iterator<TreeNode> iter = this.nodes.values().iterator();
		while (iter.hasNext() && result == null) {
			TreeNode node = iter.next();
			String str = (String) node.getParam(parameter);
			if (str != null && str.compareTo((String) value) == 0) {
				result = node;
			}
		}
		return result;
	}

	public List<TreeNode> getNodes(String parameter, Object value) {
		List<TreeNode> result = new ArrayList<TreeNode>();
		Iterator<TreeNode> iter = this.nodes.values().iterator();
		while (iter.hasNext()) {
			TreeNode node = iter.next();
			if (((String) node.getParam(parameter)).compareTo((String) value) == 0) {
				result.add(node);
			}
		}
		return result;
	}

	public Map<String,TreeNode> getNodes() {
		return nodes;
	}

	/**
	 * the number of nodes in this tree.
	 * @return {@link Integer}
	 */
	public int getNodesCount() {
		return this.nodes.size();
	}
	
	public Map<String, Class<?>> getSchemaEdges() {
		return schemaEdges;
	}

	public Map<String, Class<?>> getSchemaNodes() {
		return schemaNodes;
	}

	public void setSchemaEdges(Map<String, Class<?>> schema) {
		this.schemaEdges = schema;
	}

	public void setSchemaNodes(Map<String, Class<?>> schema) {
		this.schemaNodes = schema;
	}
	
	public boolean isDirected() {
		return this.directed;
	}

	public void setDirected(boolean directed) {
		this.directed = directed;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public void addNodes(Collection<TreeNode> nodes) {
		for (TreeNode node : nodes) {
			addNode(node);
		}
	}

	public void addEdges(Collection<TreeEdge> edges) {
		if (edges != null) {
			for (TreeEdge edge : edges) {
				addEdge(edge);
			}
		}
	}

	public void clear() {
		this.edges.clear();
		this.nodes.clear();
		this.schemaEdges.clear();
		this.schemaNodes.clear();
		this.addMinimumColumns();
	}

	public Collection<TreeNode> getNodesCollection() {
		return this.nodes.values();
	}

	public Set<TreeNode> getNodesSet() {
		return new HashSet<TreeNode>(getNodesCollection());
	}
	
	public TreeNode[] getNodesArray() {
		TreeNode[] array = new TreeNode[this.nodes.values().size()];
		for (TreeNode node : this.nodes.values()) {
			int i = node.getIdInt();
			array[i] = node;
		}
		return array;
	}

	public TreeEdge[] getEdgesArray() {
		return this.edges.toArray(new TreeEdge[]{});
	}

	/**
	 * creates a random tree.
	 * @param directed {@link Boolean}
	 * @param nodes {@link Integer}
	 * @param edges {@link Integer}
	 * @return
	 */
	public static final Tree getRandomTree(
		final boolean directed,
		int nodes,
		int edges
	) {
		Tree tree = new Tree(directed);
		for (int i = 0; i < nodes; i++) {
			tree.addNode(i);
		}
		for (int i = 0; i < edges; i++) {
			TreeNode node1 = tree.getNode(Util.rnd(nodes));
			TreeNode node2 = tree.getNode(Util.rnd(nodes));
			tree.addEdge(node1,node2);
		}
		return tree;
	}

	/**
	 * gives every node of this tree an attribute
	 * with the given name and initializes it with
	 * random values between min and max.
	 * @param attributeName {@link String}
	 * @param minValue {@link Integer}
	 * @param maxValue {@link Integer}
	 */
	public void setEdgeAttributeRndInt(String attributeName, int minValue, int maxValue) {
		for (TreeEdge edge : this.edges) {
			int value = Util.rnd(minValue,maxValue);
			edge.setParam(attributeName,value);
		}
	}

	public void setEdgeAttributeInt(String attributeName, int value) {
		for (TreeEdge edge : this.edges) {
			edge.setParam(attributeName,value);
		}
	}

	private void addNode(int i) {
		this.addNode(Integer.toString(i));
	}

	/**
	 * @param node {@link TreeNode}
	 * @return {@link Boolean}
	 */
	public boolean contains(TreeNode node) {
		boolean result = false;
		Iterator<TreeNode> i = this.nodes.values().iterator();
		while (i.hasNext() && !result) {
			TreeNode n = i.next();
			result = n.equals(node);
		}
		return result;
	}

	/**
	 * sets the id of the given node to zero,
	 * interchanging with previous root node.
	 * @param node {@link TreeNode}
	 */
	public void setRoot(TreeNode node) {
		if (node != null && this.contains(node)) {
			String index = node.getId();
			TreeNode node2 = this.getNode(0);
			node.setId(0);
			node2.setId(index);
			this.nodes.remove(index);
			this.nodes.remove("0");
			this.nodes.put("0",node);
			this.nodes.put(index,node2);
		}
	}

	/**
	 * assumes that the node parameter {@link Tree#COLUMN_NODE_EDGES}
	 * is initialized by calling {@link Tree#setEdges()}.
	 * @param node {@link TreeNode}
	 * @return {@link List}<{@link TreeNode}>
	 */
	public List<TreeNode> getNeighbors(TreeNode node) {
		List<TreeNode> result = new ArrayList<TreeNode>();
		if (node != null && this.contains(node)) {
			List<TreeEdge> edges = getEdges(node);
			if (edges != null) {
				for (TreeEdge edge : edges) {
					TreeNode source = edge.getSource();
					TreeNode target = edge.getTarget();
					if (node.compareTo(source) == 0) {
						result.add(target);
					}
					if (!directed && node.compareTo(target) == 0) {
						result.add(source);
					}
				}
			}
		}
		return result;
	}

	/**
	 * @param parameter
	 * @param value
	 */
	public void setNodeAttributeInt(String parameter, int value) {
		for (TreeNode node : this.getNodesArray()) {
			node.setParam(parameter,value);
		}
	}

	/**
	 * iterates through the list of edges of this tree
	 * and puts the parameter {@link Tree#COLUMN_NODE_EDGES}
	 * to every node with adjacent edges.
	 */
	public void setEdges() {
		for (TreeNode node : this.getNodesArray()) {
			List<TreeEdge> edges = this.getEdges(node);
			node.setParam(Tree.COLUMN_NODE_EDGES,edges);
		}
	}

	/**
	 * gives back a list of connected components.
	 * @return {@link List}<{@link Tree}>
	 */
	public List<Tree> getConnectedComponents() {
		Tarjan tarjan = new Tarjan(this);
		return tarjan.getResult();
	}

	/**
	 * calculate difference in minutes between the given times.
	 * @param t1 {@link String} "hh:mm"
	 * @param t2 {@link String} "hh:mm"
	 * @return {@link Integer} t2 - t1
	 */
	private int getDeltaTime(String t1, String t2) {
		String[] t1s = t1.split(":");
		int t1h = Integer.parseInt(t1s[0]);
		int t1m = Integer.parseInt(t1s[1]);
		String[] t2s = t2.split(":");
		int t2h = Integer.parseInt(t2s[0]);
		int t2m = Integer.parseInt(t2s[1]);
		int minuten2 = (t2h * 60 + t2m);
		int minuten1 = (t1h * 60 - t1m);
		int ergebnis = minuten2 - minuten1;
		return ergebnis;
	}

	/**
	 * inspired by:
	 * http://de.wikipedia.org/wiki/Algorithmus_von_Tarjan_zur_Bestimmung_starker_Zusammenhangskomponenten
	 * @author stpa
	 * 10.11.2010
	 */
	class Tarjan {

		private Tree tree;
		private int maxDfs = 0;
		private Set<TreeNode> U;
		private Deque<TreeNode> S;
		private List<Tree> result;
		
		public Tarjan(Tree tree) {
			this.tree = tree;
			this.U = new HashSet<TreeNode>(tree.getNodes().values());
			this.S = new ArrayDeque<TreeNode>();
			result = new ArrayList<Tree>();
			while (U.size() > 0) {
				TreeNode v0 = U.iterator().next();
				tarjan(v0);
			}
		}

		public List<Tree> getResult() {
			return this.result;
		}
		
		private void tarjan(TreeNode v) {
			v.setParam(COLUMN_NODE_DFS,maxDfs);
			v.setParam(COLUMN_NODE_LOWLINK,maxDfs);
			maxDfs++;
			S.push(v);
			U.remove(v);
			List<TreeNode> neighbors = tree.getNeighbors(v);
			for (TreeNode neighbor : neighbors) {
				if (U.contains(neighbor)) {
					tarjan(neighbor);
					v.setParam(
						COLUMN_NODE_LOWLINK,
						Math.min(v.getParamInt(COLUMN_NODE_LOWLINK),neighbor.getParamInt(COLUMN_NODE_LOWLINK))
					);
				} else {
					v.setParam(
						COLUMN_NODE_LOWLINK,
						Math.min(v.getParamInt(COLUMN_NODE_LOWLINK),neighbor.getParamInt(COLUMN_NODE_DFS))
					);
				}
			}
			if (v.getParamInt(COLUMN_NODE_LOWLINK) == v.getParamInt(COLUMN_NODE_DFS)) {
				Tree littleTree = new Tree(Integer.toString(result.size()),tree);
				TreeNode node;
				do {
					node = S.pop();
					littleTree.addNode(node);
				} while (node != v);
				Set<TreeNode> nodes = littleTree.getNodesSet();
				for (TreeNode n : nodes) {
					List<TreeEdge> edges = tree.getEdges(n);
					littleTree.addEdges(edges);
				}
				result.add(littleTree);
			}
		}
	}
/*
	Eingabe: Graph G = (V, E)
	
	max_dfs := 0                     // Zähler für dfs
	U := V                           // Menge der unbesuchten Knoten
	S := {}                          // Stack zu Beginn leer
	while (es gibt ein v0 in U) do   // Solange es bis jetzt unerreichbare Knoten gibt
	  tarjan(v0)                     // Aufruf arbeitet alle von v0 erreichbaren Knoten ab
	end while
	
	procedure tarjan(v)
	v.dfs := max_dfs;          // Tiefensuchindex setzen
	v.lowlink := max_dfs;      // v.lowlink <= v.dfs
	max_dfs := max_dfs + 1;    // Zähler erhöhen
	S.push(v);                 // v auf Stack setzen
	U := U \ {v};              // v aus U entfernen
	forall (v, v') in E do     // benachbarte Knoten betrachten
	  if (v' in U)
	    tarjan(v');            // rekursiver Aufruf
	    v.lowlink := min(v.lowlink, v'.lowlink);
	  // Abfragen, ob v' im Stack ist. 
	  // Bei geschickter Realisierung in O(1).
	  // (z.B. Setzen eines Bits beim Knoten beim "push" und "pop") 
	  elseif (v' in S)
	    v.lowlink := min(v.lowlink, v'.dfs);
	  end if
	end for
	if (v.lowlink = v.dfs)     // Wurzel einer SZK
	  print "SZK:";
	  repeat
	    v' := S.pop;
	    print v';
	  until (v' = v);
	end if
*/

	/**
	 * @return {@link List}<{@link TreeNode}>
	 */
	public List<TreeNode> getNodesList() {
		List<TreeNode> result = new ArrayList<TreeNode>();
		result.addAll(this.nodes.values());
		return result;
	}
}
