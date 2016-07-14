var React = require('react');
var Link = require('react-router').Link;


// 
// This component takes a Entity json as input and renders it as a link
// to the appropriate details page using the RESTful URL.
//
module.exports = React.createClass({
 
  render: function() {
    // Check the entity is present
    if (this.props.entity != null) {
    	var link = '/' + this.props.type + 's/' + this.props.entity.id;
    	return (
        <Link to={link}>
          {this.props.entity.name}
        </Link>
    	);
    }
    else {
      return null;
    }
  }
});
