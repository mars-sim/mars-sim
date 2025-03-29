package com.mars_sim.tools.helpgenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mars_sim.core.building.construction.ConstructionStageInfo;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.tools.helpgenerator.HelpContext.ItemQuantity;

public class ConstructionGenerator extends TypeGenerator<ConstructionStageInfo> {

    public static final String TYPE_NAME = "construction";

    private Map<ConstructionStageInfo,List<ConstructionStageInfo>> dependentsBySource;

    protected ConstructionGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Construction Stage",
                "Stages available for constructing a new site",
                "construction");
        
        // Groups according to stage
        setGrouper("Stage", r-> r.getType().name());

        dependentsBySource = getConfig().getConstructionConfiguration().getAllConstructionStageInfoList()
                .stream()
                .filter(v -> v.getPrerequisiteStage() != null)
                .collect(Collectors.groupingBy(ConstructionStageInfo::getPrerequisiteStage));
    }

    /**
     * Add list of th Stages that follow this one.
     * @param r Construction stage for generation
     * @param output Destination of the content
     */
    @Override
    protected void addEntityProperties(ConstructionStageInfo r, Map<String,Object> scope) {
        var dependents = dependentsBySource.get(r);

        if (dependents == null) {
            dependents = Collections.emptyList();
        }
        scope.put("dependents", dependents);

        var hc = getParent();
        
        List<ItemQuantity>resources = new ArrayList<>();
        resources.addAll(r.getParts().entrySet().stream()
                            .map(e -> hc.createItemQuantity(
                                            ItemResourceUtil.findItemResourceName(e.getKey()),
                                            ItemType.PART,
                                            e.getValue()))
                            .toList());
        resources.addAll(r.getResources().entrySet().stream()
                            .map(e -> hc.createItemQuantity(
                                            ResourceUtil.findAmountResourceName(e.getKey()),
                                            ItemType.AMOUNT_RESOURCE,
                                            e.getValue()))
                            .toList());
        List<ItemQuantity>  sorted = resources.stream()
                    .sorted((o1, o2) -> o1.name().compareTo(o2.name())).toList();
        scope.put("resources", sorted);
    }

    @Override
    protected List<ConstructionStageInfo> getEntities() {
        return getConfig().getConstructionConfiguration().getAllConstructionStageInfoList();
    }

    @Override
    protected String getEntityName(ConstructionStageInfo v) {
        return v.getName();
    }
}
