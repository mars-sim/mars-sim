var React = require('react');
var Bootstrap = require('react-bootstrap');
var Table = Bootstrap.Table;
var EntityLink = require('./entitylink.jsx');

var PersonRow = React.createClass({
  render: function() {
  	return (
  		    <tr>
            <td>
              <EntityLink type="person" entity={this.props.person}/>
            </td>
            <td>{this.props.person.gender}</td>
            <td>{this.props.person.task}</td>
          </tr>);
  }
});

module.exports = React.createClass({
  render: function() {
  	var personRows = this.props.persons.map(function(person) {
      return (
        <PersonRow person={person} key={person.id}>
        </PersonRow>
      );
    });
    return (
      <Table striped bordered condensed >
        <thead>
          <tr>
          <th>Name</th>
          <th>Gender</th>
          <th>Status</th>
          </tr>
        </thead>
        <tbody>
 			{personRows}
        </tbody>
      </Table>
    );
  }
});