package org.mars_sim.msp.restws.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.restws.model.ScientificStudyDTO;

/**
 * This provides a mapper from a msp-core Person entity to the lightweight PersonSummary DTO.
 */
@Mapper(componentModel="spring",
		uses=UnitReferenceMapper.class)
public interface ScientificStudyMapper {
	@Mappings({
		@Mapping(source="primaryResearcher", target="primaryResearcher")
	})
	ScientificStudyDTO studyToScientificStudy(ScientificStudy study);
}
