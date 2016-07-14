var React = require('react');
var Bootstrap = require('react-bootstrap');
var Table = Bootstrap.Table;
var EntityLink = require('./entitylink.jsx');


var VehicleRow = React.createClass({
  render: function() {
  	return (
  		    <tr>
            <td>
              <EntityLink type="vehicle" entity={this.props.vehicle}/>
            </td>
            <td>{this.props.vehicle.status}</td>
            <td>{this.props.vehicle.vehicleType}</td>
          </tr>);
  }
});

module.exports = React.createClass({
  render: function() {
  	var vehicleRows = this.props.vehicles.map(function(vehicle) {
      return (
        <VehicleRow vehicle={vehicle} key={vehicle.id}>
        </VehicleRow>
      );
    });
    return (
      <Table striped bordered condensed >
        <thead>
          <tr>
            <th>Name</th>
            <th>Status</th>
            <th>Type</th>
          </tr>
        </thead>
        <tbody>
 			    {vehicleRows}
        </tbody>
      </Table>
    );
  }
});