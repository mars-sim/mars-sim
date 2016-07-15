package org.mars_sim.msp.restws.mapper;

import javax.annotation.Generated;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.restws.model.ScientificStudyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2016-07-14T17:06:44-0700",
    comments = "version: 1.0.0.Final, compiler: javac, environment: Java 1.8.0_92 (Oracle Corporation)"
)
@Component
public class ScientificStudyMapperImpl implements ScientificStudyMapper {

    @Autowired
    private UnitReferenceMapper unitReferenceMapper;

    @Override
    public ScientificStudyDTO studyToScientificStudy(ScientificStudy study) {
        if ( study == null ) {
            return null;
        }

        ScientificStudyDTO scientificStudyDTO = new ScientificStudyDTO();

        scientificStudyDTO.setPrimaryResearcher( unitReferenceMapper.unitToUnitReference( study.getPrimaryResearcher() ) );
        if ( study.getScience() != null ) {
            scientificStudyDTO.setScience( study.getScience().name() );
        }
        scientificStudyDTO.setPhase( study.getPhase() );
        scientificStudyDTO.setDifficultyLevel( study.getDifficultyLevel() );
        scientificStudyDTO.setCompletionState( study.getCompletionState() );

        return scientificStudyDTO;
    }
}
