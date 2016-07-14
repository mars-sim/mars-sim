var React = require('react');
var Bootstrap = require('react-bootstrap');
var Pagination = Bootstrap.Pagination;
var Panel = Bootstrap.Panel;
var Table = Bootstrap.Table;
var LoadUnitPageListMixin = require('./loadunitpagelistmixim.jsx');
var EntityLink = require('./entitylink.jsx');

var SettlementRow = React.createClass({
  render: function() {
  	return (
  		    <tr>
            <td>
              <EntityLink type="settlement" entity={this.props.settlement}/>
            </td>
            <td>{this.props.settlement.numParkedVehicles}</td>
            <td>{this.props.settlement.numPersons}</td>
            <td>{this.props.settlement.numBuildings}</td>
          </tr>);
  }
});

var SettlementList = React.createClass({
  render: function() {
  	var settlementRows = this.props.settlements.map(function(settlement) {
      return (
        <SettlementRow settlement={settlement} key={settlement.id}>
        </SettlementRow>
      );
    });
    return (
      <Table striped bordered condensed >
        <thead>
          <tr>
            <th>Name</th>
            <th>Vehicle Number</th>
            <th>Population</th>
            <th>Buildings</th>
          </tr>
        </thead>
        <tbody>
 			    {settlementRows}
        </tbody>
      </Table>
    );
  }
});



// Export this as SettlementBox
module.exports = React.createClass({
  mixins: [LoadUnitPageListMixin],

  unitPath: 'settlements',

  // Page change
  handleChangePage(eventKey) {
    this.setState({
      activePage: eventKey
    });
    this.loadDataFromServer();
  },
 
  render: function() {
  	var totalPages = Math.ceil(this.state.data.totalSize/10);
  	return (
	  	<Panel>
	  	  <SettlementList settlements={this.state.data.items} />
	  	  <Pagination
          bsSize="small"
          items={totalPages}
          activePage={this.state.activePage}
          onSelect={this.handleChangePage} />
	  	</Panel>
  	);
  }
});
