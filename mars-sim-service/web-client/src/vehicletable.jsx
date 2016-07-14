var React = require('react');
var Bootstrap = require('react-bootstrap');
var Button = Bootstrap.Button;
var Pagination = Bootstrap.Pagination;
var Panel = Bootstrap.Panel;
var LoadUnitPageListMixin = require('./loadunitpagelistmixim.jsx');
var VehicleList = require('./vehiclelist.jsx');


// Export this as VehicleBox
module.exports = React.createClass({
  mixins: [LoadUnitPageListMixin],
  
  unitPath: "vehicles",

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
	  	  <VehicleList vehicles={this.state.data.items} />
	  	  <Pagination
          bsSize="small"
          items={totalPages}
          activePage={this.state.activePage}
          onSelect={this.handleChangePage} />
	  	</Panel>
  	);
  }
});
