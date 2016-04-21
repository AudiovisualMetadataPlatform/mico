var  ConfigurationGraph = function() {
	
	// Pre-initialized empty svg and related d3 structures
	this.svgWidth = svgWidth;
	this.svgHeight = svgHeight;
	this.charge=OverviewGraph.charge*2;
	this.linkDistance=OverviewGraph.linkDistance*4;
	
	this.svg = d3.select('#configuration-graph').append('svg')
    	.attr('width',  this.svgWidth)
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
	  .append('svg:path')
	    .attr('d', 'M0,-5L10,0L0,5');
	
	this.force = d3.layout.force()
    				.linkDistance(this.linkDistance)
    				.charge(this.charge)
    				.size([this.svgWidth, this.svgHeight]);
	
	this.nodes = this.force.nodes();
    this.links = this.force.links();

    //---functions for dynamic update and visualization
    
    this.addNode = function (node) {
    	
    	var id = node.id;
    	
    	var index=this.findNode(id);
    	if (typeof  index == 'undefined'){	//do not allow replicated nodes
    		this.nodes.push(node);
        	this.update();
    	}
    };

    this.removeNode = function (id) {
        var i = 0;
        var n = this.findNode(id);
        while (i < this.links.length) {
            if ((this.links[i]['source'] === n)||(this.links[i]['target'] == n)) this.links.splice(i,1);
            else i++;
        }
        var index = this.findNodeIndex(id);
        if(index !== undefined) {
        	this.nodes.splice(index, 1);
        	this.update();
        }
    };

    this.addLink = function (sourceId, targetId, syntacticType) {
        var sourceNode = this.findNode(sourceId);
        var targetNode = this.findNode(targetId);

        if((sourceNode !== undefined) && (targetNode !== undefined)) {
        	
        	var mimeTypeList=[];
        	
        	if( sourceId == this.USER_LABEL){
        		mimeTypeList=targetNode.form.getSelectedInputMimeTypes()[syntacticType];
        	}
        	else if( targetId == this.MICO_SYSTEM_LABEL){
        		mimeTypeList=sourceNode.form.getSelectedOutputMimeTypes()[syntacticType];
        	}
        	else{
        		mimeTypeList=arrayIntersection(sourceNode.form.getSelectedOutputMimeTypes()[syntacticType],
						                       targetNode.form.getSelectedInputMimeTypes()[syntacticType]);
        	}
        	
        	
//        	if(mimeTypeList.length>0)
        	{
	        	this.links.push({
	        		'source': sourceNode, 
	        		'target': targetNode,
	        		'syntacticType': syntacticType,
	        		'mimeTypeList': mimeTypeList,
	        		});
	        	this.update();
        	}
        	
        	//if no connection is available, show re-routing
        	if(mimeTypeList.length==0 && sourceId!=this.USER_LABEL && targetId!=this.MICO_SYSTEM_LABEL)
        	{
//        		this.addLink(this.USER_LABEL,targetId,syntacticType);
//        		this.addLink(sourceId,this.MICO_SYSTEM_LABEL,syntacticType);        		
        	}
        }
    };

    this.findNode = function (id) {
        for (var i=0; i < this.nodes.length; i++) {
            if (this.nodes[i].id === id)
                return this.nodes[i];
        };
    };

    this.findNodeIndex = function (id) {
        for (var i=0; i < this.nodes.length; i++) {
            if (this.nodes[i].id === id)
                return i;
        };
    };


    this.update = function () {

        var link = this.svg.selectAll('.link')
            .data(this.links);

        var linkEnter = link.enter().append('g')
        	.attr('class', 'link');
        
        linkEnter.append('line')
	            	.attr('marker-end', 'url(#end)');
        
        linkEnter.append('text')
              .attr('dy','.35em')
              .attr('text-anchor','middle')
              .text(function(d){
            	              	  
            	  linkEnter.append('title').
            	  		text( function(d) { return d.syntacticType; });
            	  
            	  if(d.mimeTypeList.length>0){                
            		  return d.mimeTypeList;
            	  }else{
            		  linkEnter.classed('warning', true);
            		  return 'WARNING: compatible but not connected';
            	  }
              }).call(function (s) {
  	            s.each(function(d) { d.bbox = this.getBBox(); });
  	        });
        
        linkEnter.insert('rect','text')
  	        .attr('width', function(d){return d.bbox.width;})
  	        .attr('height', function(d){return d.bbox.height;});

        link.exit().remove();

        var node = this.svg.selectAll('.node')
            .data(this.nodes, function(d) { return d.id;});

        var nodeEnter = node.enter().append('g')
            .attr('class', 'node')
            .call(	this.force.drag().on('dragstart', function(d){
    			d3.select(this).classed('fixed', d.fixed = true);
    		    d3.event.sourceEvent.stopPropagation();
    		    d3.event.sourceEvent.preventDefault();
    		}));
        
        nodeEnter.append('title')
        	.text( function(d) { return d.description; });

        nodeEnter.append('circle')
            .attr('r', 20);

        nodeEnter.append('text')
            .attr('x', 24)
            .attr('dy', '.35em')
            .text(function(d) {return d.id;});
	        
	        

        node.exit().remove();

        this.force.on('tick', function() {
        	
		  node.each(function(d){
			d.x=Math.max(21, Math.min(svgWidth - 21, d.x));
			d.y=Math.max(21, Math.min(svgHeight - 21, d.y));
		  });
        	
		  link.selectAll('line')
		  	  .attr('x1', function(d) { return d.source.x; })
              .attr('y1', function(d) { return d.source.y; })
              .attr('x2', function(d) { return d.target.x; })
              .attr('y2', function(d) { return d.target.y; });
		  
		  link.selectAll('text')
		  	  .attr('transform', function(d) { return 'translate(' + 0.5*(d.source.x+d.target.x) + ',' + 0.5*(d.source.y+d.target.y) + ')'; });
		  link.selectAll('rect')
	  	  	  .attr('transform', function(d) { return 'translate(' + 0.5*(d.source.x+d.target.x-this.getBBox().width) + ',' + 0.5*(d.source.y+d.target.y-this.getBBox().height) + ')'; });

          node.attr('transform', function(d) { return 'translate(' + d.x + ',' + d.y + ')'; });

          
        });
        
        
        // Restart the force layout.
        this.force.start();
    };
    this.update();
    
    
    //--- functions and variables connected to the overview graph:
    
    /* --- returns an array of Objects. Relevant fields are:
     * 
     * 	Object[i].syntacticType
     * 
     *  Object[i].providedBy[j].title
     *  Object[i].providedBy[j].form.getSelectedOutputMimeTypes()
     *  
     *  Object[i].consumedBy[j].title  
     *  Object[i].consumedBy[j].form.getSelectedOutputMimeTypes()  
     */
    
    this.previousNodes={};
    this.USER_LABEL='@--- MICO User ---@';
    this.MICO_SYSTEM_LABEL='@--- MICO System ---@';
    
    this.findActiveSyntacticLinks = function(){
    	
    	var activeDataNodes=OverviewGraph.svg.selectAll('.binary-file:not(.disabled)').data();
    	var activeLinks=OverviewGraph.svg.selectAll('.link:not(.disabled)').data();
    	
    	var sourceIndexes=[];
    	var targetIndexes=[];
    	for (var i in activeLinks){
    		sourceIndexes.push(activeLinks[i].source.title);
    		targetIndexes.push(activeLinks[i].target.title);
    	}
    	
    	var out=[];
    	for (var i in activeDataNodes){
    		var out_i = {};
    		
    		//setup the syntacticType
    		out_i.syntacticType=activeDataNodes[i].title;
    		
    		//setup providedBy and consumedBy. 
    		out_i.providedBy=[];
    		out_i.consumedBy=[];
    		for (var j in activeLinks){
    			if(activeLinks[j].target.title == out_i.syntacticType){
    				out_i.providedBy.push(activeLinks[j].source);
    			}
    			if(activeLinks[j].source.title == out_i.syntacticType){
    				out_i.consumedBy.push(activeLinks[j].target);
    			}
    		}
    		
    		//TODO: uncomment if necessary (?)
    		if (out_i.providedBy.length == 0) out_i.providedBy.push({modeId : this.USER_LABEL , description: 'MICO user, ingesting the input content' });
    		if (out_i.consumedBy.length == 0) out_i.consumedBy.push({modeId : this.MICO_SYSTEM_LABEL , description: 'MICO system, storing all the results produced by the current configuration' });
    		
    		out.push(out_i);
    	}
    	return out;
    	
    };
    
    this.updateActiveExtractorConfiguration = function(){
    	
    	var links=this.findActiveSyntacticLinks();
    	
    	//obtain currently relevant nodes
    	var currNodes={};    	
    	for(var i in links){
    		for(var j in links[i].providedBy){
    			var nodeId=links[i].providedBy[j].modeId;
    			currNodes[nodeId]={};
    			currNodes[nodeId].form=links[i].providedBy[j].form;
    			currNodes[nodeId].description=links[i].providedBy[j].description;
    			currNodes[nodeId].originalNode=links[i].providedBy[j];
    		}
    		for(var j in links[i].consumedBy){
    			var nodeId=links[i].consumedBy[j].modeId;
    			currNodes[nodeId]={};
    			currNodes[nodeId].form=links[i].consumedBy[j].form;
    			currNodes[nodeId].description=links[i].consumedBy[j].description;
    			currNodes[nodeId].originalNode=links[i].consumedBy[j];
    		}
    	}
    	
    	//erase nodes that were selected before but now are not
    	for(var i in currNodes){
    		delete this.previousNodes[i];
    	}
    	for(var i in this.previousNodes){
    		this.removeNode(i);
    	}
    	
//    	this.removeNode(this.USER_LABEL);
//    	this.removeNode(this.MICO_SYSTEM_LABEL);
    	
    	//remove all existing links
    	this.links.splice(0,this.links.length);
    	this.update();
    	
    	this.previousNodes=currNodes;
    	
    	//add to the graph the currently relevant nodes
    	for(var i in currNodes){
    		var node={};
    		node.id=i;
    		node.form=currNodes[i].form;
    		node.description=currNodes[i].description;
    		node.originalNode=currNodes[i].originalNode;
    		
    		this.addNode(node);
    	}
    	
    	//add to the graph the active links configuration as in the current MICO broker view
    	for(var i in links){
    		for(var j in links[i].providedBy){
    			for(var k in links[i].consumedBy){
    				this.addLink(links[i].providedBy[j].modeId,links[i].consumedBy[k].modeId, links[i].syntacticType);
        		}
    		}
    	}
    	
    	//move nodes after links
        this.domLinks=document.getElementById('configuration-graph').getElementsByClassName('link');
        this.domSvg=document.getElementById('configuration-graph').getElementsByTagName('svg')[0];
        for(var i=0; i<this.domLinks.length; i++) {
        	this.domSvg.insertBefore(this.domLinks[i],this.domSvg.childNodes[1]);
        }
        
        //verify correctness of the user input
        this.findBrokenLinks();
    };
    
    this.findBrokenLinks=function(){
    	
    	//repeat for every broken link
    	this.svg.selectAll('.link.warning')
    		.each( function(d,i){
    			
    			var syntacticType = d.syntacticType;
    			var targetNode    = ConfigurationGraph.findNode(d.target.id);
    			
    			//check if the target is receiving the current syntactic type from any other extractor
    			var found=false;
    	        for(var l=0; l<ConfigurationGraph.links.length; l++){
    	        	var link= ConfigurationGraph.links[l];
    	            found = found || ( link.target == targetNode &&				//same target node
    	            		           link.syntacticType ==  syntacticType &&  //same syntacticType
    	            		           link.mimeTypeList.length>0 );			//non-empty;
    	        };
    			
    	        if(!found){
    	        	
    	        	//change the text label background color    	        	
    	        	d3.select(this).classed('warning', false);
    	        	d3.select(this).classed('broken',true);
    	        	
    	        	//and update its content
    	        	this.getElementsByTagName('text')[0].textContent='ERROR: missing connection';	//label text
    	        	d.bbox=this.getElementsByTagName('text')[0].getBBox();							//label boundingbox
    	        	this.getElementsByTagName('rect')[0].setAttribute('width',d.bbox.width);		//backround rectangle width
    	        	this.getElementsByTagName('rect')[0].setAttribute('height',d.bbox.height);		//backround rectangle height
    	        	
    	        	
    	        	//erase the additional routing from source to mico system
    	        	var sourceNode     = ConfigurationGraph.findNode(d.source.id);
    	        	var micoSystemNode = ConfigurationGraph.findNode(ConfigurationGraph.MICO_SYSTEM_LABEL);
    	        	
    	        	 for(var l=0; l<ConfigurationGraph.links.length; l++){
    	        		 var link= ConfigurationGraph.links[l];
    	        		 if(link.source==sourceNode && link.target==micoSystemNode && link.syntacticType==syntacticType && link.mimeTypeList.length>0){
    	        			 ConfigurationGraph.links.splice(l,1);
    	        			 break;
    	        		 }
    	    	     };
    	    	     
    	    	     //update graph
    	    	     ConfigurationGraph.update();
    	        }
    	        
    		} );
    	
    };
};


ConfigurationGraph=new ConfigurationGraph();