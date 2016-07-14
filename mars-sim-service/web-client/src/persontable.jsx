var React = require('react');
var Bootstrap = require('react-bootstrap');
var PersonList = require('./personlist.jsx');
var Pagination = Bootstrap.Pagination;
var Panel = Bootstrap.Panel;
var LoadUnitPageListMixin = require('./loadunitpagelistmixim.jsx');


// Export this as PersonBox
module.exports = React.createClass({
  mixins: [LoadUnitPageListMixin],

  unitPath: "persons",

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
	  	  <PersonList persons={this.state.data.items} />
	  	  <Pagination
          bsSize="small"
          items={totalPages}
          activePage={this.state.activePage}
          onSelect={this.handleChangePage} />
	  	</Panel>
  	);
  }
});
