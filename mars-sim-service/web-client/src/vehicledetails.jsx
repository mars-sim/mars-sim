var React = require('react');
var ItemList = require('./itemlist.jsx');
var PersonList = require('./personlist.jsx');
var ResourceList = require('./resourcelist.jsx');
var LoadUnitDataMixin = require('./loadunitdatamixim.jsx');
var Bootstrap = require('react-bootstrap');
var Accordion = Bootstrap.Accordion;
var Badge = Bootstrap.Badge;
var Col = Bootstrap.Col;
var Grid = Bootstrap.Grid;
var Panel = Bootstrap.Panel;
var Row = Bootstrap.Row;

// Export this as VehicleDetails
module.exports = React.createClass({
  mixins: [LoadUnitDataMixin],

  unitPath: "vehicles",

  getInitialState: function() {
    return {details:{},
    		    activePanel: "",
    		    items:[],
			      persons:[],
			      resources:[]};
  },

  render: function() {
    var personsHeader = (<div>People <Badge>{this.state.details.numPersons}</Badge></div>);
  	var resourceHeader = (<div>Resources <Badge>{this.state.details.numResources}</Badge></div>);
    var itemHeader = (<div>Items <Badge>{this.state.details.numItems}</Badge></div>);
    return (
	  	<Panel header={this.state.details.name}>
	  	  <Grid>
    		  <Row>
      			<Col xs={12} sm={6} md={3}><strong>Type: </strong>{this.state.details.vehicleType}</Col>
      			<Col xs={12} sm={6} md={3}><strong>Status: </strong>{this.state.details.status}</Col>
      			<Col xs={12} sm={6} md={3}><strong>Speed: </strong>{parseFloat(this.state.details.speed).toFixed(2)} kmph</Col>
      			<Col xs={12} sm={6} md={3}><strong>Maintenance: </strong>{parseFloat(this.state.details.distanceLastMaintenance).toFixed(2)} km</Col>
    		  </Row>
    		  <Row>
    		    <Col xs={12}>
              <Accordion onSelect={this.handleDetailEvent}>
	    			    <Panel header={personsHeader} eventKey="persons">
                  <PersonList persons={this.state.persons} />
	    			    </Panel>
				        <Panel header={itemHeader} eventKey="items">
                  <ItemList items={this.state.items} />
				        </Panel>
				        <Panel header={resourceHeader} eventKey="resources">
				    	    <ResourceList resources={this.state.resources} />
				        </Panel>
			  	    </Accordion>
			  	  </Col>
    		  </Row>
		    </Grid>  	    	  
	  	</Panel>
  	);
  }
});