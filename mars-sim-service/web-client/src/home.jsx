var React = require('react');
var Bootstrap = require('react-bootstrap');
var Jumbotron = Bootstrap.Jumbotron;

module.exports = React.createClass({
  render: function() {
  	return (
  		<Jumbotron>
    		<h1>Mars Sim Project</h1>
    		<p>This is a simple web client using Bootstrap & React frameworks to access the simulation engine of the Mars Sim project.</p>
  		</Jumbotron>
	);
  }
 });
