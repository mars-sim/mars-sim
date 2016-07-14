var React = require('react');
var ReactRouterBootstrap = require('react-router-bootstrap');
var LinkContainer = ReactRouterBootstrap.LinkContainer;

var Bootstrap = require('react-bootstrap');
var Navbar = Bootstrap.Navbar;
var NavItem = Bootstrap.NavItem;
var Nav = Bootstrap.Nav;
var Label = Bootstrap.Label;

module.exports = React.createClass({
  render() {
    return (
      <div>
        <Navbar>
          <Navbar.Header>
              <Navbar.Brand>
                <a href="#">Mars Sim</a>
              </Navbar.Brand>
          </Navbar.Header>
          <Nav>
            <LinkContainer to='/persons'>
              <NavItem eventKey={1}>Person</NavItem>
            </LinkContainer>
            <LinkContainer to='/vehicles'>
              <NavItem eventKey={2}>Vehicles</NavItem>
            </LinkContainer>
            <LinkContainer to='/settlements'>
              <NavItem eventKey={3}>Settlements</NavItem>
            </LinkContainer>
            <LinkContainer to='/missions'>
              <NavItem eventKey={4}>Missions</NavItem>
            </LinkContainer>
            <LinkContainer to='/events'>
              <NavItem eventKey={5}>Events</NavItem>
            </LinkContainer>
          </Nav>
          <Nav pullRight>
            <NavItem eventKey={6}><Label>Current Time</Label></NavItem>
            <NavItem eventKey={7}><Label>Who am I</Label></NavItem>
          </Nav>
        </Navbar>  
        {this.props.children}
      </div>
    )
  }
});
