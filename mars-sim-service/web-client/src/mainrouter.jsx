var React = require('react');
var MainNav = require('./mainnav.jsx');
var EventTable = require('./eventtable.jsx');
var PersonTable = require('./persontable.jsx');
var PersonDetails = require('./persondetails.jsx');
var VehicleTable = require('./vehicletable.jsx');
var VehicleDetails = require('./vehicledetails.jsx');
var MissionTable = require('./missiontable.jsx');
var MissionDetails = require('./missiondetails.jsx');
var SettlementTable = require('./settlementtable.jsx');
var SettlementDetails = require('./settlementdetails.jsx');
var ReactRoute = require('react-router');
var Router = ReactRoute.Router;
var Route = ReactRoute.Route;
var IndexRoute = ReactRoute.IndexRoute;
var hashHistory = ReactRoute.hashHistory;
var Home = require('./home.jsx');


module.exports = React.createClass({

  render() {
    return (
      <Router history={hashHistory}>
        <Route path="/" component={MainNav}>

          <IndexRoute component={Home}/>

          <Route path="/vehicles/:id" component={VehicleDetails} />
          <Route path="/vehicles" component={VehicleTable} />
          <Route path="/missions/:id" component={MissionDetails} />
          <Route path="/missions" component={MissionTable} />
          <Route path="/settlements/:id" component={SettlementDetails} />
          <Route path="/settlements" component={SettlementTable} />
          <Route path="/persons/:id" component={PersonDetails} />
          <Route path="/persons" component={PersonTable} />
          <Route path="/events" component={EventTable} />
        </Route>
      </Router>
    );
  }
});