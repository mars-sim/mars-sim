var React = require('react');
var Bootstrap = require('react-bootstrap');
var Tabs = Bootstrap.Tabs;
var Tab = Bootstrap.Tab;
var PersonBox = require('./personbox.jsx');
var VehicleBox = require('./vehiclebox.jsx');


module.exports = React.createClass({
  getInitialState() {
    return {
      key: 2
    };
  },

  handleSelect(key) {
    this.setState({key});
  },

  render() {
    return (
      <Tabs activeKey={this.state.key} onSelect={this.handleSelect} id="main-tab">
        <Tab eventKey={1} title="Persons">
            <PersonBox host={this.props.host} pageSize={10} />,
        </Tab>
        <Tab eventKey={2} title="Vehicles">
            <VehicleBox host={this.props.host} pageSize={10} />,
        </Tab>
        <Tab eventKey={3} title="Settlements">Tab 3 content</Tab>
      </Tabs>
    );
  }
});