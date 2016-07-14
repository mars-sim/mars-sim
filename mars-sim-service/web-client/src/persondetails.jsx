var React = require('react');
var ItemList = require('./itemlist.jsx');
var ResourceList = require('./resourcelist.jsx');
var EntityLink = require('./entitylink.jsx');
var LoadUnitDataMixin = require('./loadunitdatamixim.jsx');
var Bootstrap = require('react-bootstrap');
var Accordion = Bootstrap.Accordion;
var Col = Bootstrap.Col;
var Grid = Bootstrap.Grid;
var OverlayTrigger = Bootstrap.OverlayTrigger;
var Panel = Bootstrap.Panel;
var Popover = Bootstrap.Popover;
var Row = Bootstrap.Row;

// Different Myers-Briggs indicators
var MBTI = [];
MBTI['E'] = 'Extraversion';
MBTI['S'] = 'Sensing';
MBTI['I'] = 'Introversion';
MBTI['N'] = 'Intuition';
MBTI['T'] = 'Thinking';
MBTI['F'] = 'Feeling';
MBTI['J'] = 'Judging';
MBTI['P'] = 'Perceiving';

// Popover tooltip to describe Personality
// Split into the individual indictors.
const PersonalityTooltip = React.createClass({
  render() {
    var title = "Myers-Briggs Type Indicator: " + this.props.type;
    var indicators = String(this.props.type).split('');
    let contents = <Popover title={title} id='MBTIpopup'>
                     <ul>
                       <li>{indicators[0]} - {MBTI[indicators[0]]}</li>
                       <li>{indicators[1]} - {MBTI[indicators[1]]}</li>
                       <li>{indicators[2]} - {MBTI[indicators[2]]}</li>
                       <li>{indicators[3]} - {MBTI[indicators[3]]}</li>
                     </ul>
                   </Popover>;

    return (
      <OverlayTrigger
        overlay={contents} placement="top"
        delayShow={300} delayHide={150}
      >
        <a href={this.props.href}>{this.props.type}</a>
      </OverlayTrigger>
    );
  }
});

// Export this as Person Details
module.exports = React.createClass({
  mixins: [LoadUnitDataMixin],

  unitPath: "persons",

  getInitialState: function() {
    return {details:{},
    		    activePanel: "",
    		    items:[],
			      resources:[]};
  },

  render: function() {
    // Create a meaningful task description
    var taskDesc = this.state.details.task;
    if (this.state.details.task != this.state.details.taskPhase) {
      taskDesc = this.state.details.task + " (" + this.state.details.taskPhase + ")";
    }

  	return (
	  	<Panel header={this.state.details.name}>
	  	  <Grid>
    		  <Row>
      			<Col xs={12} sm={6} md={3}><strong>Gender: </strong>{this.state.details.gender}</Col>
      			<Col xs={12} sm={6} md={3}><strong>Age: </strong>{this.state.details.age}</Col>
      			<Col xs={12} sm={6} md={3}><strong>Height: </strong>{(parseFloat(this.state.details.height)/100).toFixed(2)} m</Col>
      			<Col xs={12} sm={6} md={3}><strong>Mass: </strong>{parseFloat(this.state.details.mass).toFixed(2)} kg</Col>
    		  </Row>
          <Row>
            <Col xs={12} sm={6} md={3}><strong>Health: </strong>{this.state.details.healthSituation}</Col>
            <Col xs={12} sm={6} md={3}><strong>Personality: </strong><PersonalityTooltip type={this.state.details.personalityType}/></Col>
            <Col xs={12} sm={6} md={3}><strong>Stress: </strong>{this.state.details.stress}</Col>
            <Col xs={12} sm={6} md={3}><strong>Performance: </strong>{parseFloat(this.state.details.performance).toFixed(2) * 100} %</Col>
          </Row>
          <Row>
            <Col xs={12} md={6}>
              <strong>Settlement: </strong>
              <EntityLink type="settlement" entity={this.state.details.settlement}/>
            </Col>
            <Col xs={12} md={6}>
              <strong>Vehicle: </strong>
              <EntityLink type="vehicle" entity={this.state.details.vehicle}/>
            </Col>
          </Row>
          <Row>
            <Col xs={12} md={6}><strong>Task: </strong>{taskDesc}</Col>
            <Col xs={12} md={6}>
              <strong>Mission: </strong>
              <EntityLink type="mission" entity={this.state.details.mission}/>
            </Col>
          </Row>
    		  <Row>
    		  	<Col xs={12}>
	    		    <Accordion onSelect={this.handleDetailEvent}>
				        <Panel header="Items" eventKey="items">
				    	    <ItemList items={this.state.items} />
				        </Panel>
				        <Panel header="Resources" eventKey="resources">
				    	    <ResourceList resources={this.state.resources} />
				        </Panel>
			  	    </Accordion>
			  	  </Col>
    		  </Row>
		    </Grid>  	    	  
	  	</Panel>
  	);
  }
});