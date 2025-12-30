/*
 * Mars Simulation Project
 * BrowserWindow.java
 * @date 2025-12-30
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.browser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.interplanetary.transport.TransportManager;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.tool.MapSelector;

/**
 * Browser window for exploring entities. It uses dynamic loading to avoid long load times and excessive memory consumption.
 * The children fo collapsed nodes are removed to free memory.
 * Large child lists are grouped by name ranges to avoid overwhelming the user.
 */
public class BrowserWindow extends ContentPanel implements EntityManagerListener {
    public static final String NAME = "browser";
    public static final String ICON = "action/entitybrowser";

    // This values MUST match the Entity prefix used in message.properties
    private static final String PERSON = "Person";
    private static final String BUILDING = "Building";
    private static final String CONSTRUCTION = "ConstructionSite";
    private static final String VEHICLE = "Vehicle";
    private static final String ROBOT = "Robot";
    private static final String MISSION = "Mission";
    private static final String SCIENTIFIC_STUDY = "ScientificStudy";
    private static final String TRANSPORT = "TransportItem";
    private static final String[] ENTITY_TYPES = {BUILDING, CONSTRUCTION, MISSION, PERSON, ROBOT, SCIENTIFIC_STUDY, TRANSPORT, VEHICLE};
    private static final int MAX_ITEMS = 30;

    // Default configurations for name groupings
    private static record NameGrouping(String label, String filter) {}
    private static final NameGrouping[] NAME_GROUPINGS = {
        new NameGrouping("A-F", "^[A-Fa-f].*"),
        new NameGrouping("G-L", "^[G-Lg-l].*"),
        new NameGrouping("M-R", "^[M-Rm-r].*"),
        new NameGrouping("S-Z", "^[S-Zs-z].*")
    };

    private DefaultTreeModel entityModel;
    private Map<Entity,DefaultMutableTreeNode> entities;
    private JTree tree;
    private UIContext context;
    private ScientificStudyManager scienceMgr;
    private TransportManager transportMgr;

    public BrowserWindow(UIContext context) {
        super(NAME, "Entity Browser", Placement.LEFT);

        this.context = context;
        this.scienceMgr = context.getSimulation().getScientificStudyManager();
        this.transportMgr = context.getSimulation().getTransportManager();
        var unitManager = context.getSimulation().getUnitManager();
		
		// Get content pane
		JPanel mainPane = new JPanel(new BorderLayout());
		add(mainPane);

        entities = new HashMap<>();
        
        // Create the tree pane
        var root = new DefaultMutableTreeNode(Msg.getString("Settlement.plural"));
		entityModel = new DefaultTreeModel(root);

        // Inital load with Settlements
        unitManager.getSettlements().stream()
            .map(this::createSettlementNode)
            .forEach(root::add);
        
        buildUI(mainPane, entityModel);

        // List for new Settlements
        unitManager.addEntityManagerListener(UnitType.SETTLEMENT, this);

        var dims = new Dimension(250, 400);
        setMinimumSize(dims);
        setPreferredSize(dims);
    }

    private void buildUI(JPanel mainPane, DefaultTreeModel model) {
            
        // Button
        var buttonPanel = new JPanel();
        mainPane.add(buttonPanel, BorderLayout.NORTH);
        var details = new JButton(EntityLabel.DETAILS);
        details.setEnabled(false);
        details.addActionListener(e -> showDetails());
        buttonPanel.add(details);
        var locate = new JButton(EntityLabel.LOCATE);
        locate.setEnabled(false);
        locate.addActionListener(e -> showLocation());
        buttonPanel.add(locate);

        // Add the tree
        tree = new JTree(model);
        var scrollPane = new JScrollPane(tree);
        mainPane.add(scrollPane, BorderLayout.CENTER);

        // Control enabling buttons
        tree.addTreeSelectionListener(e -> {
            var selection = e.getNewLeadSelectionPath();
            boolean enable = false;
            if (selection != null) {
                var node = (DefaultMutableTreeNode) selection.getLastPathComponent();
                enable = node instanceof EntityNode;
            }
            details.setEnabled(enable);
            locate.setEnabled(enable);
        });

        // Double click to show details
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1 && e.getClickCount() == 2 && selPath != null) {
                    Object selectedNode = selPath.getLastPathComponent();
                    if (selectedNode instanceof EntityNode en) {
                        context.showDetails(en.getEntity());
                    }
                }
            }
        });

        // Dynamically add nodes
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                TreePath path = event.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                expandNode(node);
            }
            
            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                if (node.equals(model.getRoot())) {
                    return;
                }
                removeChildren(node);

                // Must be Type expansion node so add placeholder back
                node.add(new PlaceholderNode());
                model.nodeStructureChanged(node);
            }
        });
    }

    /**
     * User requests expansion of the given node.
     * @param node
     */
    private void expandNode(DefaultMutableTreeNode node) {
        // Already expanded
        if (node.getChildCount() > 1 && !(node.getChildAt(0) instanceof PlaceholderNode)) {
            return;
        }

        if (node instanceof TypeNode en) {
            expandTypeNode(en);
            entityModel.nodeStructureChanged(node);
        }
        else if (node instanceof EntityNode en && en.getEntity() instanceof Settlement) {
            expandSettlementNode(en);
            entityModel.nodeStructureChanged(node);
        }
    }

    /**
     * Remove the children of the given node recursively. This would typically be a TypeNode
     * that the user has collapsed.
     * @param node
     */
    private void removeChildren(DefaultMutableTreeNode node) {
        for (int i = node.getChildCount() - 1; i >= 0; i--) {
            var child = node.getChildAt(i);
            if (!child.isLeaf()) {
                removeChildren((DefaultMutableTreeNode) child);
            }
            if (child instanceof EntityNode en) {
                entities.remove(en.getEntity());
            }
            node.remove(i);
        }
        node.removeAllChildren();

    }

    /**
     * Show the details for the selected entity.
     */
    private void showDetails() {
        var selection = tree.getSelectionPath().getLastPathComponent();
        if (selection instanceof EntityNode en) {
            var entity = en.getEntity();
            context.showDetails(entity);
        }
    }

    /**
     * Show the location of the selected entity on the map tool.
     */
    private void showLocation() {
        var selection = tree.getSelectionPath().getLastPathComponent();
        if (selection instanceof EntityNode en) {
            var entity = en.getEntity();
            MapSelector.displayOnMap(context, entity);
        }
    }

    /**
     * Create a new TypeNode with the given type and parent settlement.
     * @param type The type of entities this node will represent.
     * @param parent The parent settlement for this type node.
     * @return A new TypeNode instance.
     */
    private DefaultMutableTreeNode createTopTypeNode(String type, Settlement parent) {
        var label = Msg.getString(type + ".plural");
        // No name filter for top level
        return new TypeNode(label, type, null, parent);
    }

    /**
     * User requests expansion of the given settlement node.
     * @param settlementNode
     */
    private void expandSettlementNode(EntityNode settlementNode) {
        // Remove placeholder
        removePlaceholder(settlementNode);

        var settlement = (Settlement) settlementNode.getEntity();
        for (var t : ENTITY_TYPES) {
            settlementNode.add(createTopTypeNode(t, settlement));
        }
    }

    /**
     * User requests expansion of the given type node. This will get the appropriate Entities from the Settlement
     * and either add them directly below or create sub-nodes if there are too many.
     * @param en
     */
    private void expandTypeNode(TypeNode en) {
        var settlement = en.getSettlement();

        // Remove placeholder
        removePlaceholder(en);

        // Find the source items
        Collection<? extends Entity> items = switch(en.getType()) {
            case PERSON -> settlement.getAllAssociatedPeople();
            case ROBOT -> settlement.getAllAssociatedRobots();
            case VEHICLE -> settlement.getAllAssociatedVehicles();
            case CONSTRUCTION -> settlement.getConstructionManager().getConstructionSites();
            case BUILDING -> settlement.getBuildingManager().getBuildingSet();
            case MISSION -> context.getSimulation().getMissionManager().getMissionsForSettlement(settlement);
            case SCIENTIFIC_STUDY -> scienceMgr.getAllStudies(settlement);
            case TRANSPORT -> transportMgr.getTransportItems().stream()
                                        .filter(ti -> ti.getSettlementName().equals(settlement.getName()))
                                        .toList();
            default -> Collections.emptyList();
        };
            
        // Check size and filter
        var nameFilter = en.getNameFilter();
        if (items.size() > MAX_ITEMS && nameFilter == null) {
            // Too many - create sub nodes by first letter
            for(var item : NAME_GROUPINGS) {
                en.add(new TypeNode(item.label, en.getType(), item.filter, settlement));
            }
            return;
        }

        // Convert Entiites to tree nodes
        items.stream()
            .filter(e -> nameFilter == null || e.getName().matches(nameFilter))
            .sorted(Comparator.comparing(Entity::getName))
            .map(this::createEntityNode)
            .forEach(en::add);
    }

    /**
     * Remove the placeholder child node from the given node. The Placeholder node is used to indicate
     * that the node can be expanded. It is never visible when expanded as it is removed during expansion.
     * @param node
     */
    private void removePlaceholder(DefaultMutableTreeNode node) {
        if (node.getChildCount() == 1 && node.getChildAt(0) instanceof PlaceholderNode) {
            node.remove(0);
        }
        else {
            throw new IllegalStateException("No placeholder to remove");
        }
    }

    /**
     * A new entity has been added.
     * @param newEntity
     */
    @Override
    public void entityAdded(Entity newEntity) {
        if (newEntity instanceof Settlement settlement) {   
            var settlementNode = createSettlementNode(settlement);
            var root = (DefaultMutableTreeNode) entityModel.getRoot();
            root.add(settlementNode);
            entityModel.nodeStructureChanged(root);
        }
    }

    /**
     * An entity has been removed.
     * @param removedEntity
     */
    @Override
    public void entityRemoved(Entity removedEntity) {       
        if (removedEntity instanceof Settlement settlement) {   
            var sNode = entities.remove(settlement);
            if (sNode != null) {
                var root = (DefaultMutableTreeNode) entityModel.getRoot();
                root.remove(sNode);
                entityModel.nodeStructureChanged(root);
            }
        }
    }

    /**
     * Create a settlement node for the given settlement. This is an Entity node with a
     * placeholder to trigger dynamic expansion
     * @param settlement
     * @return
     */
    private DefaultMutableTreeNode createSettlementNode(Settlement settlement) {
        var settlementNode = createEntityNode(settlement);
        settlementNode.add(new PlaceholderNode());
        return settlementNode;
    }

    /**
     * Create an entity node for the given entity. Register the nodes in the entity lookup
     * @param entity
     * @return
     */
    private DefaultMutableTreeNode createEntityNode(Entity entity) {
        var sNode = new EntityNode(entity);
        entities.put(entity, sNode);
        return sNode;
    }

    /**
     * This is a placeholder node used during dynamic loading.
     */
    private static class PlaceholderNode extends DefaultMutableTreeNode {
        public PlaceholderNode() {
            super("Loading...");
        }
    }

    /**
     * This is a tree node for a specific Entity.
     */
    private static class EntityNode extends DefaultMutableTreeNode {
        private Entity entity;

        public EntityNode(Entity entity) {
            super(entity.getName());
            this.entity = entity;
        }

        public Entity getEntity() {
            return entity;
        }
    }

    /**
     * This is an expansion node for a specific Entity type.
     * It can have an optional name filter that is used to restrict the children entires.
     */
    private static class TypeNode extends DefaultMutableTreeNode {
        private String type;
        private Settlement settlement;
        private String nameFilter;

        public TypeNode(String label, String type, String nameFilter, Settlement settlement) {
            super(label);
            this.type = type;
            this.nameFilter = nameFilter;
            this.settlement = settlement;

            add(new PlaceholderNode());
        }

        Settlement getSettlement() {
            return settlement;
        }

        String getType() {
            return type;
        }

        String getNameFilter() {
            return nameFilter;
        }
    }
}
