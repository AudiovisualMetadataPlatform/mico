
var OverviewGraph = function() {
	
	this.dataNodes=null;
	this.dataLinks=null;
	
	this.svg = null;
	this.svgWidth = svgWidth;
	this.svgHeight = svgHeight;
	
	this.force = null;
	this.node = null;
	this.charge = -1000;
	this.gravity= .2;
	this.linkDistance=30;
	
	this.text   = null;
	this.circle = null;
	this.link   = null;
	
	this.numSelectedExtractors=0;
	
	this.draw = function(){
		
		this.dataNodes=Broker.graphNodes;
		this.dataLinks=Broker.graphLinks;

		//Create main svg container

		this.svg = d3.select('#overview-graph').append('svg')
		    .attr('width', this.svgWidth)
		    .attr('height', this.svgHeight);


		//define arrows for links
		this.svg.append('svg:defs').selectAll('marker')
		    .data(['end'])                 // Different link/path types can be defined here
		  .enter().append('svg:marker')    // This section adds in the arrows
		    .attr('id', String)
		    .attr('viewBox', '0 -5 10 10')
		    .attr('refX', 26)
		    .attr('refY', 0)
		    .attr('markerWidth', 6)
		    .attr('markerHeight', 6)
		    .attr('orient', 'auto')
		    .attr('fill', '#333')
		  .append('svg:path')
		    .attr('d', 'M0,-5L10,0L0,5');
		
		// Generate force layout

		this.force = d3.layout.force()
		    .size([this.svgWidth, this.svgHeight])
		    .nodes(this.dataNodes)
		    .links(this.dataLinks)
		    .linkDistance(this.linkDistance)
		    .charge( this.charge )
			.gravity( this.gravity );

		//visualize transitions at the beginning
		this.force.on('tick', function() {
		    
			OverviewGraph.node.each(function(d){
			    d.x=Math.max(21, Math.min(svgWidth - 21, d.x));
			    d.y=Math.max(21, Math.min(svgHeight - 21, d.y));
			});
			
		    OverviewGraph.text.attr('transform', function(d) { return 'translate(' + d.x + ',' + d.y + ')'; });
		    OverviewGraph.circle.attr('transform', function(d) { return 'translate(' + d.x + ',' + d.y + ')'; });
		    OverviewGraph.link.attr('x1', function(d) { return d.source.x; })
		      .attr('y1', function(d) { return d.source.y; })
		      .attr('x2', function(d) { return d.target.x; })
		      .attr('y2', function(d) { return d.target.y; });
		});
		
		// Add links to the svg, as lines

		this.link = this.svg.selectAll('.link')
		    .data(this.dataLinks)
		    .enter().append('line')
		    .attr('class', 'link')
		    .attr('marker-end', 'url(#end)');

		// Add nodes to the svg, as circles

		this.node = this.svg.selectAll('.node')
		    .data(this.dataNodes)
		    .enter().append('g')
		    .attr('class', 'node')
		    .on('click', function(d){ 
		    	
		    	if (d3.event != null && d3.event.defaultPrevented) return; // click suppressed if deriving from drag event
		    	
		    	console.log(d.title+' says: "stop touching me!!!"');
		    	
		    	if(d.className=='extractor'){
			    	if(d.selected ){
			    		OverviewGraph.numSelectedExtractors--;
			    	}
			    	else{
			    		OverviewGraph.numSelectedExtractors++;
			    	}
			    	d.selected=(!d.selected);
			    	
			    	d.form.thumbnail.parentNode.appendChild(d.form.thumbnail);			    	
			    	OverviewGraph.updateSelectedExtractors();
			    	OverviewGraph.updateAvalilableDataTypes();
			    	OverviewGraph.updateActiveLinks();
			    	ConfigurationGraph.updateActiveExtractorConfiguration();
		    	}
		    	
		    	});
		this.node.append('title')
		         .text( function(d) { 
		        	 if(d.className=='extractor') return d.description;
		        	 return ''; });

		this.circle = this.node.append('circle')
		    .attr('r', 20)
		    .call(this.force.drag()
		    		.on('dragstart', function(d){
		    			d3.select(this).classed('fixed', d.fixed = true);
		    		    d3.event.sourceEvent.stopPropagation();
		    		    d3.event.sourceEvent.preventDefault();
		    			}));

		// enable close button of the form
		this.node.each(function(d){
			if (d.form != null && d.form.thumbnail != null) {
				var node=this;
				d.form.closeBtn.addEventListener('click', function(e){
					
					  var e = document.createEvent('UIEvents');
					  e.initUIEvent('click', true, true, window, 1);
					  
					  if(!d.selected){
						  OverviewGraph.svg.selectAll('.extractor').each( function(d){ d3.select(this).node().dispatchEvent(e); });
					  }
					  d3.select(node).node().dispatchEvent(e);
				  });
			}
		});
		
		// and style them
		this.node.each(function(d){
		    if (d.className != null) {
		        d3.select(this).classed(d.className, true);
		    }
		});


		this.text = this.node.append('text')
		  .attr('x', 24)
		  .attr('dy', '.35em')
		  .text(function(d) { 
			  if(d.className=='extractor') return d.modeId;
			  return d.title; });

		// Fire it up

		this.force.start();
	};
	
	this.updateSelectedExtractors = function(){
		if(this.numSelectedExtractors>0){
			this.svg.selectAll('.extractor')
			  .each( function(d,i){
				  
				  if(d.selected){
					  this.classList.remove('disabled');
					  d.form.thumbnail.classList.remove('hidden');
					  
				  }else{
					  this.classList.add('disabled');
					  d.form.thumbnail.classList.add('hidden');
					  d3.select(this).classed('fixed', d.fixed = false);
				  }
			});
		}
		else{
			this.svg.selectAll('.extractor')
			  .each( function(d,i){
				  this.classList.remove('disabled');
				  d.form.thumbnail.classList.remove('hidden');
			});
		}
	};
	
	this.updateAvalilableDataTypes = function(){
		if(this.numSelectedExtractors>0){
			this.svg.selectAll('.binary-file')
			  .each( function(d,i){
				  
				//map between index as string as index as int
				  var connectedExtractors={}; 
				  
				  //1. look for extractors requiring this data type
				  for(j in OverviewGraph.dataLinks){
					  if(OverviewGraph.dataLinks[j].source.index == d.index) 
					  {
						  connectedExtractors[''+OverviewGraph.dataLinks[j].target.index]=OverviewGraph.dataLinks[j].target.index;
					  }
					  if(OverviewGraph.dataLinks[j].target.index == d.index){
						  connectedExtractors[''+OverviewGraph.dataLinks[j].source.index]=OverviewGraph.dataLinks[j].source.index;
					  }
				  }
				  
				  
				  //2. delete unselected connected extractors
				  for(j in connectedExtractors){
					  if(! OverviewGraph.dataNodes[connectedExtractors[j]].selected){
						delete connectedExtractors[j];  
					  }						  
				  }
				  
				  //3. if one or more remain, adjust the class accordingly
				  if(Object.size(connectedExtractors)>0){
					  this.classList.remove('disabled');
					  d.selected=true;
				  }else{
					  this.classList.add('disabled');
					  d3.select(this).classed('fixed', d.fixed = false);
					  d.selected=false;
				  }
			});
		}
		else{
			this.svg.selectAll('.binary-file')
			  .each( function(d,i){
				  this.classList.remove('disabled');
				  d.selected=false;
			});
		}
	};
	
	this.updateActiveLinks = function(){
		if(this.numSelectedExtractors>0){
			this.svg.selectAll('.link')
			  .each( function(d,i){
				  if(d.source.selected && d.target.selected){
					  this.classList.remove('disabled');
				  }else{
					  this.classList.add('disabled');
				  }
			});
		}
		else{
			this.svg.selectAll('.link')
			  .each( function(d,i){
				  this.classList.remove('disabled');
				  d.selected=false;
			});
		}
	};
};


OverviewGraph=new OverviewGraph();






