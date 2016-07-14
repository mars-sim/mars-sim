var React = require('react');
var ItemList = require('./itemlist.jsx');
var PersonList = require('./personlist.jsx');
var ResourceList = require('./resourcelist.jsx');
var VehicleList = require('./vehiclelist.jsx');
var LoadUnitDataMixin = require('./loadunitdatamixim.jsx');
var Bootstrap = require('react-bootstrap');
var Accordion = Bootstrap.Accordion;
var Badge = Bootstrap.Badge;
var Col = Bootstrap.Col;
var Grid = Bootstrap.Grid;
var Panel = Bootstrap.Panel;
var Row = Bootstrap.Row;
var Table = Bootstrap.Table;


var BuildingRow = React.createClass({
  render: function() {
    return (
          <tr>
            <td>{this.props.building.name}</td>
            <td>{this.props.building.buildingType}</td>
            <td>{this.props.building.powerMode}</td>
          </tr>);
  }
});

var BuildingList = React.createClass({
  render: function() {
    var buildingRows = this.props.buildings.map(function(building) {
      return (
        <BuildingRow building={building} key={building.id} />
      );
    });
    return (
      <Table striped bordered condensed >
        <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Power Mode</th>
            <th>Functions</th>
          </tr>
        </thead>
        <tbody>
          {buildingRows}
        </tbody>
      </Table>
    );
  }
});

// Export this as Settlement Details
module.exports = React.createClass({
  mixins: [LoadUnitDataMixin],

  unitPath: "settlements",

  getInitialState: function() {
    return {details:{},
    		    activePanel: "",
            buildings:[],
    		    items:[],
			      persons:[],
			      resources:[],
            vehicles:[]};
  },

  render: function() {
    var personsHeader = (<div>Population <Badge>{this.state.details.numPersons}</Badge></div>);
    var vehicleHeader = (<div>Vehicles <Badge>{this.state.details.numParkedVehicles}</Badge></div>);
    var resourceHeader = (<div>Resources <Badge>{this.state.details.numResources}</Badge></div>);
    var itemHeader = (<div>Items <Badge>{this.state.details.numItems}</Badge></div>);
    var buildingHeader = (<div>Buildings <Badge>{this.state.details.numBuildings}</Badge></div>);
    return (
	  	<Panel header={this.state.details.name}>
	  	  <Grid>
    		  <Row>
      			<Col xs={12} sm={6}><strong>Temperature: </strong>{this.state.details.temperature} &deg;C</Col>
      			<Col xs={12} sm={6}><strong>Air Pressure: </strong>{this.state.details.airPressure} psi</Col>
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
                <Panel header={vehicleHeader} eventKey="vehicles">
                  <VehicleList vehicles={this.state.vehicles} />
                </Panel>    
                <Panel header={buildingHeader} eventKey="buildings">
                  <BuildingList buildings={this.state.buildings} />
                </Panel>             
			  	    </Accordion>
			  	  </Col>
    		  </Row>
		    </Grid>  	    	  
	  	</Panel>
  	);
  }
});