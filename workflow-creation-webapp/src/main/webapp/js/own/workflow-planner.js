var Workflow = function() {
	
	this.nodes={};	//nodes left to be processed
	this.pipelines=[[]];
	this.multicasts=[];
	this.aggregators=[];
	
	this.setup = function(){
		try{
			this.nodes=this.identifyNodeConnections();
			this.pipelines=this.initPipelines();
			this.pipelines=this.identifyPipelines();
			this.aggregators=this.defineAggregators();
			this.multicasts=this.defineMulticasts();
		}
		catch(e){
			this.nodes={};
			this.pipelines=[[]];
			if(e.name=="Invalid workflow configuration"){
				alert('---- '+e.name +'----\n'+ e.message);
			}
			throw e;
		}
	};
	
	this.identifyNodeConnections = function(){
		console.log('Identifying active extractors...');
		
		//check for validity
		if(ConfigurationGraph.svg.selectAll('.link.broken')[0].length > 0)
			throw { name: "Invalid workflow configuration", message: "Broken links are present. Unable to provide a routing"}
		
		$("[data-toggle=validator]").validator('validate');
		if(document.getElementsByClassName('form-group has-error').length > 0)
			throw { name: "Invalid workflow configuration", message: "Invalid parameter configurations are present. Unable to provide a routing"}
			
		var activeLinks=ConfigurationGraph.svg.selectAll('.link:not(.warning)').data();

		var activeNodes={};
		
		for (var i in activeLinks ){
			var link=activeLinks[i];
			link.source.connectionWeight=0;
			link.target.connectionWeight=0;
			
			//create one node for each mode, and for mico user
			if(activeNodes[link.source.id] == undefined){
				activeNodes[link.source.id]=jQuery.extend({}, link.source);
				activeNodes[link.source.id].sources={};
				activeNodes[link.source.id].targets={};
				activeNodes[link.source.id].outputSyntacticTypes={};
			}
			
			//create one node for mico system
			if(activeNodes[ConfigurationGraph.MICO_SYSTEM_LABEL] == undefined){
				if( link.target.id == ConfigurationGraph.MICO_SYSTEM_LABEL){
					activeNodes[link.target.id]=jQuery.extend({}, link.target);
					activeNodes[link.target.id].sources={};
					activeNodes[link.target.id].targets={};
					activeNodes[link.target.id].outputSyntacticTypes={};
				}
			}
			
		}
		
		for (var i in activeLinks )
		{
			var link=jQuery.extend(true, {}, activeLinks[i]);
			if(activeNodes[link.target.id].sources[link.source.id] != undefined ){
				link.source=activeNodes[link.target.id].sources[link.source.id];
			}
			if(activeNodes[link.source.id] != undefined){
				
				//flag the connection to source node
				activeNodes[link.target.id].sources[link.source.id]=link.source;
				if (activeNodes[link.target.id].sources[link.source.id].connectionWeight == undefined ){
					activeNodes[link.target.id].sources[link.source.id].connectionWeight = 1;
				}
				else{
					activeNodes[link.target.id].sources[link.source.id].connectionWeight = activeNodes[link.target.id].sources[link.source.id].connectionWeight +1;
				}
				
				//flag the connection to target node
				if(activeNodes[link.target.id]!=undefined){
					activeNodes[link.source.id].targets[link.target.id]=link.target;	
				}
				
				//flag the connection to output types
				if ( activeNodes[link.source.id].outputSyntacticTypes[link.syntacticType] == undefined ){
					activeNodes[link.source.id].outputSyntacticTypes[link.syntacticType]=[];
				}
				if(! arrayContains(activeNodes[link.source.id].outputSyntacticTypes[link.syntacticType],link.target.id)){
					activeNodes[link.source.id].outputSyntacticTypes[link.syntacticType].push(link.target.id);	
				}
			}
		}
		
//		delete activeNodes[ConfigurationGraph.MICO_SYSTEM_LABEL];
//		delete activeNodes[ConfigurationGraph.USER_LABEL];
		
		console.log(activeNodes)
		return activeNodes;
	};
	
	this.initPipelines=function(){
		
		console.log('Initializing pipelines')
		
		//create one pipeline per node, flagging need for multicast/aggregator
		var out=[]
		for(var i in this.nodes){
			
			this.nodes[i].requireMulticast=false;
			this.nodes[i].requireAggregator=false;
			
			//aggregator is straightforward
			if(Object.size(this.nodes[i].sources)>1){
				this.nodes[i].requireAggregator=true;
			} else if (Object.size(this.nodes[i].sources)==1){
				if(this.nodes[i].sources[Object.keys(this.nodes[i].sources)[0]].connectionWeight > 1){
					this.nodes[i].requireAggregator=true;
				}				
			}
			
			//while multicasts needs to manually exclude the mico system node from the targets
			if(this.nodes[i].id != ConfigurationGraph.USER_LABEL && this.nodes[i].id != ConfigurationGraph.MICO_SYSTEM_LABEL){
				var targets=this.nodes[i].targets;
				if(Object.size(targets)>1){
					delete targets[ConfigurationGraph.MICO_SYSTEM_LABEL]
				}
				if(Object.size(targets)>1){
					this.nodes[i].requireMulticast=true;
				}
			}
			
			//skip user node: we need one per each input point 
			if(this.nodes[i].id != ConfigurationGraph.USER_LABEL){
				out.push([this.nodes[i]])
			}
		}
		//plus one pipeline per every output syntactic type of mico user
		var newUserNode={};
		for(var i in this.nodes[ConfigurationGraph.USER_LABEL].outputSyntacticTypes){
			
			//make an exact copy of the node (without any reference) 
			newUserNode =jQuery.extend({} , this.nodes[ConfigurationGraph.USER_LABEL]);
			
			if(this.nodes[ConfigurationGraph.USER_LABEL].outputSyntacticTypes[i].length>1){
				newUserNode.requireMulticast=true;
			}
			newUserNode.targets={};
			
			for( var j in this.nodes[ConfigurationGraph.USER_LABEL].outputSyntacticTypes[i])
			{
				if( this.nodes[this.nodes[ConfigurationGraph.USER_LABEL].outputSyntacticTypes[i][j]] != undefined ){ 
					newUserNode.targets[this.nodes[ConfigurationGraph.USER_LABEL].outputSyntacticTypes[i][j]]=this.nodes[this.nodes[ConfigurationGraph.USER_LABEL].outputSyntacticTypes[i][j]];
				}
			}
			if(Object.size(newUserNode.targets)>0){
				newUserNode.outputSyntacticTypes={}
				newUserNode.outputSyntacticTypes[i] = jQuery.extend({} , this.nodes[ConfigurationGraph.USER_LABEL].outputSyntacticTypes[i]);
				out.push([newUserNode]);
			}
		}
		
		return (out);
	}
	
	this.identifyPipelines = function(){
		
		var pipelineNodes=[];
		var pipelineNodesIndexes=[];

		//if requires no multicasts nor aggregators, it's a pipeline element =) 
		for(var i in this.pipelines)
		{
			if( this.pipelines[i][0].requireMulticast==false &&
				this.pipelines[i][0].requireAggregator==false ){
				pipelineNodes.push(this.pipelines[i][0]);
				pipelineNodesIndexes.push(i);
			}
		}
		
		var out=this.pipelines;
		
		//remove from out elements corresponding to pipeline elements =)
		for( var i = pipelineNodesIndexes.length-1; i>=0; i--)
		{
			out.splice(pipelineNodesIndexes[i],1);
		}
		
		
		for (var n in pipelineNodes){
			var found=false;
			for(var p in out){
				var firstNode=out[p][0];
				var lastNode=out[p][out[p].length-1];
				
				if(found == false &&
				   firstNode.sources !=undefined &&	// -> user node
				   firstNode.requireAggregator == false &&				   
				   Object.keys(firstNode.sources)[0] == pipelineNodes[n].id &&
				   Object.keys(pipelineNodes[n].targets)[0] == firstNode.id 
				   ){
					found = true;
					out[p].unshift(pipelineNodes[n]);
				}
				
				if(found == false  &&
				   lastNode.requireMulticast == false &&
				   lastNode.targets !=undefined && // -> system node
				   Object.keys(lastNode.targets)[0] == pipelineNodes[n].id && 
				   Object.keys(pipelineNodes[n].sources)[0] == lastNode.id ){
					found = true;
					out[p].push(pipelineNodes[n]);
				}
			}
			if (! found){
				out.push([pipelineNodes[n]])
			};
		}
		
		console.log(out);
		return out;
	};
	
	this.defineMulticasts = function(){
		console.log('Identifying multicasts...');
		var multicastNodes = [];
		
		for(var i in this.pipelines)
		{
			if(this.pipelines[i][this.pipelines[i].length-1].requireMulticast == true){
				multicastNodes.push(this.pipelines[i][this.pipelines[i].length-1]);
			}
		}
		
		var out=[]
		for(var i in multicastNodes){
			
			var multicast={from : null, to: []};
						
			for(var p in this.pipelines){
				var pipe=this.pipelines[p];
				if(pipe[pipe.length-1].id == multicastNodes[i].id){
					multicast.from=p;
					multicastNodes[i].isMulticastNumber = out.length
				}else {
					for(var targetNodeId in multicastNodes[i].targets){
						if(pipe[0].id == targetNodeId){
							multicast.to.push(p)
						}
					}
				}
			}
			out.push(multicast);
			
		}
		console.log(out);
		return out;
	}
	
	//findJoin -> http://camel.apache.org/aggregator2.html
	this.defineAggregators = function(){
		console.log('Identifying aggregators...');
		var aggregateNodes = [];
		
		for(var i in this.pipelines)
		{
			if(this.pipelines[i][0].requireAggregator == true && this.pipelines[i][0].id != ConfigurationGraph.MICO_SYSTEM_LABEL){
				aggregateNodes.push(this.pipelines[i][0]);
			}
		}
		
		var out=[]
		for(var i in aggregateNodes){
			
			var aggregate={number: out.length, isSimple: true, from : [], to: null};
						
			for(var p in this.pipelines){
				var pipe=this.pipelines[p];
				if(pipe[0].id == aggregateNodes[i].id){
					aggregate.to=p;
					this.pipelines[p][0].needsAggregateNumber = out.length
				}else {
					for(var sourceNodeId in aggregateNodes[i].sources){
						if(pipe[pipe.length-1].id == sourceNodeId){
							aggregate.from.push(p)
						}
					}
				}
			}
			Array.prototype.push.apply(out, this.reshapeAggregator(aggregate,aggregateNodes[i]));
			
		}
		console.log(out);
		return out;
	}
	
	this.reshapeAggregator = function(aggregator, targetNode){
		
		var requiredInputSyntacticTypes=Object.keys(targetNode.form.getSelectedInputMimeTypes());

		//first, check if we are very lucky, aka
		
		//1. only one required syntactic type
		if(requiredInputSyntacticTypes.length == 1){
			return [aggregator];
		}
		
		//2. one input extractor for every different required syntactic type
		if(requiredInputSyntacticTypes.length == aggregator.from.length){
			aggregator.isSimple=false;
			return [aggregator];
		}
		
		//if not brace yourself =)
		aggregator.isSimple=false;
		
		var out=[];
		for(var i in requiredInputSyntacticTypes){
			var currType=requiredInputSyntacticTypes[i];
			
			//retrieve list of input types
			var responsibleInputs=[];
			for(var j in aggregator.from){
				var currPipe=this.pipelines[aggregator.from[j]];
				if (currPipe!=undefined){
						
					var currNode=currPipe[currPipe.length-1];
					
					//if  the pipeline ends with an extractor, look for a matching
					if(currNode.form != undefined){
						if(arrayContains(Object.keys(currNode.form.getSelectedOutputMimeTypes()),currType)){
							responsibleInputs.push({fromIndex: j, pipeNumber: aggregator.from[j], node: currNode});
						}
					}
				}
			}
			
			//if only one input for it, needs no change :)
			if(responsibleInputs.length > 1){
				
				var newFrom=[]
				for (var j = responsibleInputs.length-1; j>=0; j--){
					newFrom.push(responsibleInputs[j].pipeNumber)
					aggregator.from.splice(parseInt(responsibleInputs[j].fromIndex),1);
				}
//				var newSimpleAggregator={number: (out.length + aggregator.number + 1),
//					      				isSimple: true,
//					      				from : newFrom, 
//					      				to: aggregator.to}
				aggregator.from.push(newFrom);
			}
		}
		out.unshift(aggregator)
		
		
		return out;		
	};
	
	//generateConnections
	this.sendCamelRoute=function(){
		var xmlRoute=WorkflowPrinter.printXmlString()
		return xmlRoute;
	}
	
	
};

Workflow=new Workflow();