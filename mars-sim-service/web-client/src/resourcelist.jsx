var React = require('react');
var Bootstrap = require('react-bootstrap');
var Badge = Bootstrap.Badge;
var ProgressBar = Bootstrap.ProgressBar;
var Table = Bootstrap.Table;


var ResourceRow = React.createClass({
  render: function() {
    var amount = this.props.resource.amount.toFixed(0);
    var capacity = this.props.resource.capacity;
    var percentage = (amount * 100) / capacity;
  	return (
  		    <tr>
            <td>{this.props.resource.name}</td>
            <td>{this.props.resource.phase}</td>
            <td><ProgressBar now={percentage} label={`${amount}`} /></td>
          </tr>);
  }
});

module.exports = React.createClass({
  render: function() {
  	var resourceRows = this.props.resources.map(function(resource) {
      return (
        <ResourceRow resource={resource} key={resource.name}>
        </ResourceRow>
      );
    });
    return (
      <Table striped bordered condensed >
        <thead>
          <tr>
          <th>Name</th>
          <th>Phase</th>
          <th>Amount</th>
          </tr>
        </thead>
        <tbody>
 			    {resourceRows}
        </tbody>
      </Table>
    );
  }
});