// Mixin to poll server
module.exports = {
  // Needs better handling 
  //serverHost: "http://localhost:8080",
  serverHost: "",

  getInitialState: function() {
    return {data: {items: [], pageNumber:1, totalSize:1},
      activePage:1};
  },
  componentDidMount: function() {
    this.loadDataFromServer();
    this.interval = setInterval(this.loadDataFromServer, 5000);
  },
  componentWillUnmount: function() {
    console.log("Stop auto load for " + this.unitPath);
    clearInterval(this.interval);
  },
  loadDataFromServer: function() {
    var fullURL =  this.serverHost + "/" + this.unitPath
                  + "?page=" + this.state.activePage
                  + "&size=" + 10;
    $.ajax({
      url: fullURL,
      dataType: 'json',
      cache: false,
      success: function(data) {
        console.log("Loaded unit page " + this.state.activePage + " from " + this.unitPath);
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(fullURL, status, err.toString());
      }.bind(this)
    });
  }
};