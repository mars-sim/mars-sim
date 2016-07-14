var React = require('react');
var Bootstrap = require('react-bootstrap');
var Badge = Bootstrap.Badge;
var ListGroup = Bootstrap.ListGroup;
var ListGroupItem = Bootstrap.ListGroupItem;


module.exports = React.createClass({
  render: function() {
  	var itemRows = this.props.items.map(function(item) {
      return (
        // Item name is unique so can be used as a key
        <ListGroupItem key={item.name}>
          {item.name}<Badge>{item.amount}</Badge>
        </ListGroupItem>
      );
    });
    return (
      <ListGroup>
        {itemRows}
      </ListGroup>
    );
  }
});