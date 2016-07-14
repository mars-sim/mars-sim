var React = require('react');
var Bootstrap = require('react-bootstrap');
var Table = Bootstrap.Table;
var EntityLink = require('./entitylink.jsx');

var MissionRow = React.createClass({
  render: function() {
  	return (
          <tr>
            <td>
              <EntityLink type="mission" entity={this.props.mission}/>
            </td>
            <td>{this.props.mission.type}</td>
            <td>{this.props.mission.phase}</td>
            <td>{this.props.mission.numPeople}</td>
          </tr>);
  }
});

module.exports = React.createClass({
  render: function() {
  	var missionRows = this.props.missions.map(function(mission) {
      return (
        // Why is this not id like unts ?
        <MissionRow mission={mission} key={mission.id}>
        </MissionRow>
      );
    });
    return (
      <Table striped bordered condensed >
        <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Phase</th>
            <th>Members</th>
          </tr>
        </thead>
        <tbody>
 			    {missionRows}
        </tbody>
      </Table>
    );
  }
});
