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
			
			if ( activeNodes[link.source.id].outputSyntacticTypes == undefined ){
				activeNodes[link.source.id].outputSyntacticTypes={};
			}
			if ( activeNodes[link.source.id].outputSyntacticTypes[link.syntacticType] == undefined ){
				activeNodes[link.source.id].outputSyntacticTypes[link.syntacticType]=[];
			}
			if(! arrayContains(activeNodes[link.source.id].outputSyntacticTypes[link.syntacticType],link.target.id)){
				activeNodes[link.source.id].outputSyntacticTypes[link.syntacticType].push(link.target.id);	//flag the connection to output types
			}
						
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
			
			//make an exact copy of the node (without any reference, tho) 
			newUserNode =jQuery.extend({} , this.nodes[ConfigurationGraph.USER_LABEL]);
			
			if(this.nodes[ConfigurationGraph.USER_LABEL].outputSyntacticTypes[i].length>1){
				newUserNode.requireMulticast=true;
			}
			newUserNode.targets={};
			for( var j in this.nodes[ConfigurationGraph.USER_LABEL].outputSyntacticTypes[i])
			{
				newUserNode.targets[this.nodes[ConfigurationGraph.USER_LABEL].outputSyntacticTypes[i][j]]=this.nodes[this.nodes[ConfigurationGraph.USER_LABEL].outputSyntacticTypes[i][j]];
			}
			out.push([newUserNode]);
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
		
		var out=''
		
		
		var createPipelineRoutes = function(){
			var out=''
			for (var p in Workflow.pipelines){
				var pString='';
				
				pString=pString+createSimplePipeline(Workflow.pipelines[p]);
				var strCheck=createMulticast(Workflow.pipelines[p],Workflow.multicasts);
				if(strCheck.length>0){
					pString=pString+strCheck
				}
				else{
					pString=pString+createAggregatorcast(Workflow.pipelines,p,Workflow.aggregators);
				}
				
				var from=XmlElement('from','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-pipeline-'+p});
				
				if(pString != ''){
					out=out+XmlElement('route',from+pString,{id: 'workflow-'+WORKFLOW_PREFIX+'-pipeline-'+p})
				}
			}
			return out;
		}
		
		
		/*
		 * Example returned string:
		 * 
		 * <pipeline>
         *   <to uri="mico-comp:vbox1?host=mico-platform&amp;user=mico&amp;password=mico&amp;serviceId=http://www.mico-project.org/services/audiodemux-queue-8kHz-mp4" />
         *   <to uri="mico-comp:vbox1?host=mico-platform&amp;user=mico&amp;password=mico&amp;serviceId=speakerdiarization" />
         *   <to uri="mico-comp:vbox1?host=mico-platform&ampserviceId=kaldicpp" />
         * </pipeline>
		 * 
		 * 
		 * using var XmlElement=function(name,content,attributes){
		 */
			
		var createSimplePipeline = function(pipe){
			
			var extractors='';
			for (var node in pipe)
			{
				var brokerExtractor=Broker.brokerNodes[pipe[node].originalNode.title];
				if(brokerExtractor!=undefined){
					var extractorId=brokerExtractor.extractorId;
					var extractorVersion=brokerExtractor.extractorVersion;
					var modeId=brokerExtractor.modeId;
					var queueName=modeId;
					  
					extractors=extractors+
							XmlElement("to",'',{uri:"mico-comp:vbox1?serviceId="+queueName+
													   "&amp;extractorId="+extractorId+
													   "&amp;extractorVersion="+extractorVersion+
													   "&amp;modeId="+modeId});
				}
			}
			if(extractors != '')  {
				extractors=XmlElement('pipeline',extractors);
			}
			return extractors
		}
		
		/*
		 * Example returned strings:
		 * 
		 * <multicast>
         *   <to uri="direct:pipeline-identifier-number-1" />	
         *   <to uri="direct:aggregator-identifier-number-2" />
         * </multicast>
         * 
		 */
		var createMulticast = function(pipe,multicasts){
			var targets='';
			if( pipe[pipe.length-1].requireMulticast ){
				var multicast=multicasts[pipe[pipe.length-1].isMulticastNumber]
				targets=targets+createMulticastTarget(Workflow.pipelines,multicast);
				return XmlElement('multicast',targets);
			}
			return targets;
		}
		
		var createMulticastTarget = function(pipelines,multicast){
			
			var out='';
			for (var i in multicast.to){
				var to=multicast.to[i];
				
				if(pipelines[to][0].requireAggregator == false){
					out=out+XmlElement('to','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-pipeline-'+to});
				}
				else{
					
					var aggregator=Workflow.aggregators[pipelines[to][0].needsAggregateNumber];
					to=pipelines[to][0].needsAggregateNumber;
					if(aggregator.isSimple == false){
						for(var j in aggregator.from){
							if(aggregator.from[j] instanceof Array){
								for(var k=0; k< aggregator.from[j].length; k++){
									if(aggregator.from[j][k]==multicast.from){
										to=to+'-'+j;
									}
								}
							}
						}
					}
					out=out+XmlElement('to','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+to});
				}
				
			}
			return out;
		}
		
		/*
		 * Example returned strings:
		 * 
		 * <to uri="direct:aggregator-identifier-number-2" />
         * 
		 */
		var createAggregatorcast = function(pipelines,currPipeIdx,aggregators){

			for(var a in aggregators){
				var aggregator=aggregators[a];
				var to=a;
				for(var j in aggregator.from){
					if(aggregator.from[j] instanceof Array){
						for(var k=0; k< aggregator.from[j].length; k++){
							if(aggregator.from[j][k]==currPipeIdx){
								if(aggregator.from[j].length>1){
									to=to+'-'+j;
								}
								return XmlElement('to','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+to});
							}
						}
					}
					else{
						if(aggregator.from[j]==currPipeIdx){
							return XmlElement('to','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+to});
						}
					}
				}
			}
			return '';
		}
		
		/*
		 * Example returned strings
		 * 
		 * for simple Aggregators:
		 * 
		 * <route id="workflow-'+WORKFLOW_PREFIX+'-aggregator-'+IDENTIFIER"
		 * 	 <from uri="direct:workflow-'+WORKFLOW_PREFIX+'-pipeline-'+IDENTIFIER"/>
	     *   <aggregate strategyRef="aggregatorStrategy" completionSize="1">
	     *      <correlationExpression>
	     *        <simple>header.id</simple>
	     *      </correlationExpression>
	     *      <to uri="direct:TARGET_PIPELINE"/>
	     *   </aggregate>
	     * </route>
	     * 
	     * for complex Aggregators:
	     *
	     * one (or more)
	     *
	     * <route id="workflow-'+WORKFLOW_PREFIX+'-aggregator-'+IDENTIFIER"
		 * 	 <from uri="direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+IDENTIFIER+SUBIDENTIFIER"/>
	     *   <aggregate strategyRef="aggregatorStrategy" completionSize="1">
	     *      <correlationExpression>
	     *        <simple>header.id</simple>
	     *      </correlationExpression>
	     *      <to uri="direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+IDENTIFIER"/>
	     *   </aggregate>
	     * <route>
	     * 
	     * plus
	     * 
	     * <route id="workflow-'+WORKFLOW_PREFIX+'-aggregator-'+IDENTIFIER"
		 * 	 <from uri="direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+IDENTIFIER"/>
	     *   <aggregate strategyRef="aggregatorStrategy" completionPolicy="COMPLEX">
	     *      <correlationExpression>
	     *        <simple>header.id</simple>
	     *      </correlationExpression>
	     *      <to uri="direct:TARGET_PIPELINE"/>
	     *   </aggregate>
	     * <route>
	     * 
	     */
		var createAggregatorRoutes = function(){
			var out='\n';
			for(var a in Workflow.aggregators){
				var aggregator=Workflow.aggregators[a];
				if(aggregator.isSimple == true){
					
					var from=XmlElement('from','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+a});
					var correlationExpression=XmlElement('correlationExpression','<simple>header.id</simple>');
					var to=XmlElement('to','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-pipeline-'+aggregator.to});
					var aggregate=XmlElement('aggregate',correlationExpression+to,{strategyRef: 'aggregatorStrategy', completionSize:'1'})
					
					out=out+XmlElement('route',from+aggregate,{id: 'workflow-'+WORKFLOW_PREFIX+'-aggregator-'+a });
					
				}else{
					
					//first simple sub-Aggregators:
					for(var i in aggregator.from){
						var currInput=aggregator.from[i];
						if(currInput.length>1){

							var from=XmlElement('from','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+a+'-'+i});
							var correlationExpression=XmlElement('correlationExpression','<simple>header.id</simple>');
							var to=XmlElement('to','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+a});
							var aggregate=XmlElement('aggregate',correlationExpression+to,{strategyRef: 'aggregatorStrategy', completionSize:'1'})
							
							out=out+XmlElement('route',from+aggregate,{id: 'workflow-'+WORKFLOW_PREFIX+'-aggregator-'+a+'-'+i});
						}
					}
					
					var from=XmlElement('from','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+a});
					var correlationExpression=XmlElement('correlationExpression','<simple>header.id</simple>');
					var to=XmlElement('to','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-pipeline-'+aggregator.to});
					var aggregate=XmlElement('aggregate',correlationExpression+to,{strategyRef: 'aggregatorStrategy', completionPolicy:'COMPLEX'})
					
					out=out+XmlElement('route',from+aggregate,{id: 'workflow-'+WORKFLOW_PREFIX+'-aggregator-'+a });
				}
			}
			return out+'\n'+XmlElement('bean','',{ id:"aggregatorStrategy", class: "org.apache.camel.processor.BodyInAggregatingStrategy"});
		}
		
		
		/*
		 * Example returned strings
		 * 
		 * <route id="workflow-'+WORKFLOW_PREFIX+'-starting-point-for-pipeline-'+IDENTIFIER"
		 * 	 <from uri="direct:MIME_TYPE"/>
	     *   <aggregate strategyRef="aggregatorStrategy" completionSize="1">
	     *      <correlationExpression>
	     *        <simple>header.id</simple>
	     *      </correlationExpression>
	     *      <to uri="direct:TARGET"/>
	     *   </aggregate>
	     * </route>
	     * 
	     */
		var createPipelineStartingPoints = function(){
			
			var out='\n\n';
			
			for (var p in Workflow.pipelines){
				var pipe=Workflow.pipelines[p];
				
				if(pipe[0].id == ConfigurationGraph.USER_LABEL){ 
					if(pipe[1] != undefined){
						for(var i in pipe[1].form.getSelectedInputMimeTypes()){
							
							var mimeTypes=pipe[1].form.getSelectedInputMimeTypes()[i]
							for (var j in mimeTypes){
								var mimeType=mimeTypes[j]
			
								var from=XmlElement('from','',{uri: 'direct:'+mimeType});
								var correlationExpression=XmlElement('correlationExpression','<simple>header.id</simple>');
								var to=XmlElement('to','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-pipeline-'+p});
								var aggregate=XmlElement('aggregate',correlationExpression+to,{strategyRef: 'aggregatorStrategy', completionSize:'1'})
								
								out=out+XmlElement('route',from+aggregate,{id: 'workflow-'+WORKFLOW_PREFIX+'-starting-point-for-pipeline-'+p+'-'+mimeType });
							}
						}
					}
					else{
						//TODO: find the freaking aggregator =)
						var to='';
						var found=false;
						var inputIndex=-1;
						var aggregatorIndex=-1;
						for(var a in Workflow.aggregators){
							var aggregator=Workflow.aggregators[a];
							to=a;
							for(var j in aggregator.from){
								if(aggregator.from[j] instanceof Array){
									for(var k=0; k< aggregator.from[j].length && !found; k++){
										if(aggregator.from[j][k]==p){
											found=true;
											inputIndex=j;
											aggregatorIndex=a;
											to=to+'-'+j;
										}
									}
								}
								else{
									if(aggregator.from[j]==p){
										found=true;
										inputIndex=j;
										aggregatorIndex=a;
									}
								}
								if (found) break;
							}
							if(found) break;
						}
										
						var pipe=Workflow.pipelines[Workflow.aggregators[aggregatorIndex].to]

						var mimeTypes=pipe[0].form.getSelectedInputMimeTypes()
						mimeTypes=mimeTypes[Object.keys(mimeTypes)[inputIndex]];
						for (var j in mimeTypes){
							var mimeType=mimeTypes[j]
				
							var from=XmlElement('from','',{uri: 'direct:'+mimeType});
							var correlationExpression=XmlElement('correlationExpression','<simple>header.id</simple>');
							var to=XmlElement('to','',{uri: 'direct:workflow-'+WORKFLOW_PREFIX+'-pipeline-'+p});
							var aggregate=XmlElement('aggregate',correlationExpression+to,{strategyRef: 'aggregatorStrategy', completionSize:'1'})
								
							out=out+XmlElement('route',from+aggregate,{id: 'workflow-'+WORKFLOW_PREFIX+'-starting-point-for-pipeline-'+p+'-'+mimeType });
						}
					}
				}
 
				
			}
			
			return out+'\n';
		}

		
		return XmlPrettyPrint(XmlElement('routes',createPipelineStartingPoints()+createPipelineRoutes()+createAggregatorRoutes(),{xmlns:'http://camel.apache.org/schema/spring'}));
	}
	
	
};

Workflow=new Workflow();