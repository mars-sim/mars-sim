var React = require('react');
var ItemList = require('./itemlist.jsx');
var EntityLink = require('./entitylink.jsx');
var PersonList = require('./personlist.jsx');
var LoadUnitDataMixin = require('./loadunitdatamixim.jsx');
var Bootstrap = require('react-bootstrap');
var Accordion = Bootstrap.Accordion;
var Badge = Bootstrap.Badge;
var Col = Bootstrap.Col;
var Grid = Bootstrap.Grid;
var Panel = Bootstrap.Panel;
var Row = Bootstrap.Row;


// Export this as Person Details
module.exports = React.createClass({
  mixins: [LoadUnitDataMixin],

  unitPath: "missions",

  getInitialState: function() {
    return {details:{},
    		activePanel: "",
    		persons:[]};
  },

  render: function() {
  	// Identify any destination
  	var destinationRow = '';
    if (this.state.details.destinationSettlement != null) {
      destinationRow = <Row>
                				 <Col xs={12} md={6}>
                  			 	 <strong>Destination Settlement: </strong>
                           <EntityLink type="settlement"
                                       entity={this.state.details.destinationSettlement}/>
                				 </Col>
                       </Row>;
    }
    else if (this.state.details.destinationVehicle != null) {
      destinationRow = <Row>
                         <Col xs={12} md={6}>
                           <strong>Destination Vehicle: </strong>
                           <EntityLink type="vehicle"
                                       entity={this.state.details.destinationVehicle}/>
                         </Col>
                       </Row>;
    };

    var personsHeader = (<div>Persons <Badge>{this.state.details.numPersons}</Badge></div>);
  	return (
	  	<Panel header={this.state.details.name}>
	  	  <Grid>
    		<Row>
      		  <Col xs={12} sm={6}><strong>Type: </strong>{this.state.details.type}</Col>
    		</Row>
            <Row>
              <Col xs={12} md={6}>
                <strong>Base: </strong>
                <EntityLink type="settlement" entity={this.state.details.associatedSettlement}/>
              </Col>
              <Col xs={12} md={6}>
                <strong>Vehicle: </strong>
                <EntityLink type="vehicle" entity={this.state.details.vehicle}/>
              </Col>
            </Row>
            {destinationRow}
            <Row>
              <Col xs={12} md={6}><strong>Phase: </strong>{this.state.details.phaseDescription}</Col>
              <Col xs={12} md={6}><strong>Activity: </strong>{this.state.details.phase}</Col>
            </Row>
    		<Row>
    		  <Col xs={12}>
	    		<Accordion onSelect={this.handleDetailEvent}>
				  <Panel header={personsHeader} eventKey="persons">
				    <PersonList persons={this.state.persons} />
				  </Panel>
			  	</Accordion>
			  </Col>
    		</Row>
		  </Grid>  	    	  
	  	</Panel>
  	);
  }
});
