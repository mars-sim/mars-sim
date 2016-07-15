package org.mars_sim.msp.restws.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Generated;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.task.TaskManager;
import org.mars_sim.msp.restws.model.PersonSummary;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2016-07-14T17:06:44-0700",
    comments = "version: 1.0.0.Final, compiler: javac, environment: Java 1.8.0_92 (Oracle Corporation)"
)
@Component
public class PersonSummaryMapperImpl implements PersonSummaryMapper {

    @Override
    public PersonSummary personToPersonSummary(Person person) {
        if ( person == null ) {
            return null;
        }

        PersonSummary personSummary = new PersonSummary();

        personSummary.setTask( personMindTaskManagerTaskName( person ) );
        personSummary.setId( person.getIdentifier() );
        personSummary.setName( person.getName() );
        if ( person.getGender() != null ) {
            personSummary.setGender( person.getGender().name() );
        }
        personSummary.setAge( person.getAge() );

        return personSummary;
    }

    @Override
    public List<PersonSummary> personsToPersonSummarys(Collection<Person> persons) {
        if ( persons == null ) {
            return null;
        }

        List<PersonSummary> list = new ArrayList<PersonSummary>();
        for ( Person person : persons ) {
            list.add( personToPersonSummary( person ) );
        }

        return list;
    }

    private String personMindTaskManagerTaskName(Person person) {

        if ( person == null ) {
            return null;
        }
        Mind mind = person.getMind();
        if ( mind == null ) {
            return null;
        }
        TaskManager taskManager = mind.getTaskManager();
        if ( taskManager == null ) {
            return null;
        }
        String taskName = taskManager.getTaskName();
        if ( taskName == null ) {
            return null;
        }
        return taskName;
    }
}
