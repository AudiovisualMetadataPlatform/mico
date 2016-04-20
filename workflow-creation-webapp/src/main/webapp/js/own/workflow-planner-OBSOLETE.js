var Workflow = function() {
	
	this.nodes={};	//nodes left to be processed
	this.pipelines=[[]];
	this.multicasts=[];
	this.aggregates=[];
	
	this.setup = function(){
		try{
			this.nodes=this.identifyNodeConnections();
			this.pipelines=this.identifyPipelines();
			this.multicasts=this.identifyMulticasts();
			this.aggregates=this.identifyAggregates();
		}
		catch(e){
			this.nodes={};
			this.pipelines=[[]];
			if(e.name=="Invalid workflow configuration"){
				alert('---- '+e.name +'----\n'+ e.message);
			}
			else throw e;
		}
	};
	
	this.identifyNodeConnections = function(){
		console.log('Identifying active extractors...');
		
		//check for validity
		if(ConfigurationGraph.svg.selectAll('.link.broken')[0].length > 0)
			throw { name: "Invalid workflow configuration", message: "Broken links are present. Unable to provide a routing"}
			
		var activeLinks=ConfigurationGraph.svg.selectAll('.link:not(.warning)').data();

		var activeNodes={};
		
		for (var i in activeLinks )
		{
			var link=activeLinks[i];
			if(activeNodes[link.source.id] == undefined){
				activeNodes[link.source.id]=link.source;
			}
			if ( activeNodes[link.source.id].targets == undefined ){
				activeNodes[link.source.id].targets={};
			}
			activeNodes[link.source.id].targets[link.target.id]=link.target;	//flag the connection to target nodes
			
			
			if(activeNodes[link.target.id] == undefined){
				activeNodes[link.target.id]=link.target;
			}
			if ( activeNodes[link.target.id].sources == undefined ){
				activeNodes[link.target.id].sources={};
			}
			activeNodes[link.target.id].sources[link.source.id]=link.source;	//flag the connection to source nodes
		}
		
//		delete activeNodes[ConfigurationGraph.MICO_SYSTEM_LABEL];
//		delete activeNodes[ConfigurationGraph.USER_LABEL];
		
		console.log(activeNodes)
		return activeNodes;
	};
	
	this.identifyPipelines = function(){
		console.log('Identifying pipelines...');
		
		var pipelineNodes=[];
		
		for(var i in this.nodes)
		{
			
			if(this.nodes[i].id != ConfigurationGraph.USER_LABEL &
			   Object.size(this.nodes[i].sources)<2 & 
			   Object.size(this.nodes[i].targets)<2
			   ){
				pipelineNodes.push(this.nodes[i]);
				delete this.nodes[i];
				
			}
		}
		
		for(var i=0; i<Object.size(this.nodes[ConfigurationGraph.USER_LABEL].targets); i++){
			pipelineNodes.push(this.nodes[ConfigurationGraph.USER_LABEL]);
		}
//		for(var i=0; i<Object.size(this.nodes[ConfigurationGraph.MICO_SYSTEM_LABEL].sources); i++){
//			pipelineNodes.push(this.nodes[ConfigurationGraph.MICO_SYSTEM_LABEL]);
//		}
//		delete this.nodes[ConfigurationGraph.MICO_SYSTEM_LABEL];
//		delete this.nodes[ConfigurationGraph.USER_LABEL];
		
		console.log(pipelineNodes)
		var out=[]
		for (var n in pipelineNodes){
			var found=false;
			for(var p in out){
				var firstNode=out[p][0];
				var lastNode=out[p][out[p].length-1];
				
				if(!found &&
				   firstNode.sources !=undefined &&	// -> user node 
				   Object.keys(firstNode.sources)[0] == pipelineNodes[n].id ){
					found = true;
					out[p].unshift(pipelineNodes[n]);
				}
				
				if(!found &&
				   lastNode.targets !=undefined && // -> system node 
				   Object.keys(lastNode.targets)[0] == pipelineNodes[n].id ){
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
	
	this.identifyMulticasts = function(){
		console.log('Identifying multicasts...');
		var multicastNodes = [];
		
		for(var i in this.nodes)
		{
			if(Object.size(this.nodes[i].sources)==1 && Object.size(this.nodes[i].targets)>1){
				multicastNodes.push(this.nodes[i]);
				delete this.nodes[i];
			}
		}
		
		var out=[]
		for(var i in multicastNodes){
			
			var multicast={from : null, to: []};
						
			for(var p in this.pipelines){
				var pipe=this.pipelines[p];
				if(pipe[pipe.length-1].id == Object.keys(multicastNodes[i].sources)[0]){
					multicast.from=p;
					multicastNodes[i].isMulticastNumber = out.length
					pipe.push(multicastNodes[i]);
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
		console.log(this.pipelines);
		return out;
	}
	
	//findJoin -> http://camel.apache.org/aggregator2.html
	this.identifyAggregates = function(){
		console.log('Identifying aggregates...');
		var aggregateNodes = [];
		
		for(var i in this.nodes)
		{
			if(this.nodes[i].targets != undefined && Object.size(this.nodes[i].targets) == 0){
				this.nodes[i].targets[ConfigurationGraph.MICO_SYSTEM_LABEL]=this.nodes[ConfigurationGraph.MICO_SYSTEM_LABEL]
			}
			if(Object.size(this.nodes[i].sources)>1 && Object.size(this.nodes[i].targets)==1){
				aggregateNodes.push(this.nodes[i]);
				delete this.nodes[i];
			}
		}
		
		var out=[]
		for(var i in aggregateNodes){
			
			var aggregate={number: out.length, isSimple: true, from : [], to: null};
						
			for(var p in this.pipelines){
				var pipe=this.pipelines[p];
				if(pipe[0].id == Object.keys(aggregateNodes[i].targets)[0]){
					aggregate.to=p;
					aggregateNodes[i].isAggregateNumber = out.length
					pipe.unshift(aggregateNodes[i]);
				}else {
					for(var sourceNodeId in aggregateNodes[i].sources){
						if(pipe[pipe.length-1].id == sourceNodeId){
							aggregate.from.push(p)
						}
					}
				}
			}
			if(aggregate.to==null){ //the aggregate is outputting to the mico system

				aggregateNodes[i].isAggregateNumber = out.length
				this.pipelines.push([aggregateNodes[i]])
			}
			Array.prototype.push.apply(out, this.reshapeAggregator(aggregate,aggregateNodes[i]));
			
		}
		console.log(out);
		console.log(this.pipelines);
		return out;
	}
	
	this.reshapeAggregator = function(aggregator, targetNode){
		
		var requiredInputSyntacticTypes=Object.keys(targetNode.form.getSelectedInputMimeTypes());

		//first, check if we are very lucky, aka
		
		console.log('Reshaping this aggregator:');
		console.log(aggregator);
		
		//1. only one required syntactic type
		if(requiredInputSyntacticTypes.length == 1){
			console.log('omg this is SO simple!')
			return aggregator;
		}
		
		//2. one input extractor for every different required syntactic type
		if(requiredInputSyntacticTypes.length == aggregator.from.length){
			console.log('omg we are SO lucky!')
			aggregator.isSimple=false;
			console.log('Turned into')
			console.log(aggregator);
			return aggregator;
		}
		
		console.log('please kill yourself')


		
	};
	//generateConnections
	
	
};

Workflow=new Workflow();




var attr={};
attr['xmlns']='http://www.springframework.org/schema/beans'
attr['xmlns:xsi']='http://www.w3.org/2001/XMLSchema-instance';
attr['xsi:schemaLocation']='\nhttp://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\n'+
     					     'http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd';

return XmlPrettyPrint(	XmlElement('beans', 
							XmlElement('camelContext',createPipelineStartingPoints()+createPipelineRoutes()+createAggregatorRoutes(),{xmlns:'http://camel.apache.org/schema/spring'})+'\n'+
							XmlElement('bean','',{ id:"aggregatorStrategy", class: "org.apache.camel.processor.BodyInAggregatingStrategy"}),
						attr))