var React = require('react');
var Bootstrap = require('react-bootstrap');
var Pagination = Bootstrap.Pagination;
var Panel = Bootstrap.Panel;
var MissionList = require('./missionlist.jsx');
var LoadUnitPageListMixin = require('./loadunitpagelistmixim.jsx');


// Export this as MissionBox
module.exports = React.createClass({
  mixins: [LoadUnitPageListMixin],
  
  unitPath: "missions",

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
	  	  <MissionList missions={this.state.data.items} />
	  	  <Pagination
          bsSize="small"
          items={totalPages}
          activePage={this.state.activePage}
          onSelect={this.handleChangePage} />
	  	</Panel>
  	);
  }
});
