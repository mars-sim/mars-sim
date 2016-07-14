var React = require('react');
var Bootstrap = require('react-bootstrap');
var Pagination = Bootstrap.Pagination;
var Panel = Bootstrap.Panel;
var LoadUnitPageListMixin = require('./loadunitpagelistmixim.jsx');
var Table = Bootstrap.Table;
var EntityLink = require('./entitylink.jsx');

var EventRow = React.createClass({
  render: function() {
    return (
          <tr>
            <td>{this.props.event.name}</td>
            <td>
              <EntityLink type={this.props.event.sourceType}
                          entity={this.props.event.source}/>
            </td>
            <td>{this.props.event.description}</td>
            <td>{this.props.event.timestamp}</td>
          </tr>);
  }
});

EventList = React.createClass({
  render: function() {
    var eventRows = this.props.events.map(function(event) {
      return (
        // Why is this not id like unts ?
        <EventRow event={event} key={event.id}/>
      );
    });
    return (
      <Table striped bordered condensed >
        <thead>
          <tr>
            <th>Name</th>
            <th>Source</th>
            <th>Description</th>
            <th>Time</th>
          </tr>
        </thead>
        <tbody>
          {eventRows}
        </tbody>
      </Table>
    );
  }
});


// Export this as EventTable
module.exports = React.createClass({
  mixins: [LoadUnitPageListMixin],
  
  unitPath: "simulation/events",

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
	  	  <EventList events={this.state.data.items} />
	  	  <Pagination
          bsSize="small"
          ellipsis
          prev
          next
          first
          last
          boundaryLinks
          maxButtons={5}
          items={totalPages}
          activePage={this.state.activePage}
          onSelect={this.handleChangePage} />
	  	</Panel>
  	);
  }
});
