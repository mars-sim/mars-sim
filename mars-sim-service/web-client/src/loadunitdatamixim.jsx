
  // Mixin to poll server
module.exports = {
  // Needs better handling 
  //serverHost: "http://localhost:8080",
  serverHost: "",

  componentDidMount: function() {
    this.loadDataFromServer();
    this.interval = setInterval(this.loadDataFromServer, 5000);
  },

  componentWillUnmount: function() {
    console.log("Stop auto load for unit details:" + this.unitPath);
    clearInterval(this.interval);
  },

  // Loads unit details and then extra if needed
  loadDataFromServer: function() {
  	// Basic Unit details
    var fullURL =  this.serverHost + "/" + this.unitPath + "/" + this.props.params.id;
    $.ajax({
      url: fullURL,
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({details: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(fullURL, status, err.toString());
      }.bind(this)
    });

  	// Extra details
  	if (this.state.activePanel.length > 0) {
  		var extraURL =  fullURL + "/" + this.state.activePanel;
	    $.ajax({
	      url: extraURL,
	      dataType: 'json',
	      cache: false,
	      success: function(data) {
	        this.setState({[this.state.activePanel]: data});
	      }.bind(this),
	      error: function(xhr, status, err) {
	        console.error(extraURL, status, err.toString());
	      }.bind(this)
	    });
  	};
  },

  // Page change
  handleDetailEvent(eventKey) {
  	// If the active Panel is handled then it's a closed so stop refreshing
  	if (this.state.activePanel != eventKey) {
  		this.state.activePanel = eventKey;
    	this.loadDataFromServer();
  	} else {
  		this.state.activePanel = "";
  	}
  }
};