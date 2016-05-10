var Broker = new function() {
    
	this.retrievedAllExtractors=false;
	
	this.jsonExtractors = [];
	this.extractorURLs = [];
	this.brokerNodes = {};
	
	this.graphNodes = [];
	this.graphLinks = [];
    
    this.drawBrokerGraph = function () {
    	
    	this.jsonExtractors = [];
    	this.brokerNodes = {};
    	
    	//retrieve amount of available extractors via REST-API
    	var r=$.ajax({
    		
    		//general configuration
            url:  '/registration-service/info',
            mimeType : 'application/json',
            type: 'GET',
            
            //configure data received from the server
            cache: false,
            headers: { 
    			Accept : 'application/json',
    		},

           
            success: function(data){
                
            	//if at least one extractor is registered, retrieve it and go on
            	if(data.numberOfExtractors>0)
            	{
            		console.log('at least one extractor is present, proceed');
            		retrieveExtractors('/registration-service/find/extractors/');
            	}
            	//otherwise warn the user and proceed with dummy data
            	else{
            		alert('WARNING: No extractor is currently registered to the MICO platform!\nProceeding with dummy data');
                    Broker.extractorURLs.push('json/kaldiSample_audioDemux.json');
                    Broker.extractorURLs.push('json/kaldiSample_speakerDiarization.json');
                    Broker.extractorURLs.push('json/kaldiSample_speech2text.json');
                    Broker.extractorURLs.push('json/kaldiSample_ner.json');
                    retrieveDummyExtractors(0);
            	}

        		
            },
            error : function(e){
            	        	
                if(typeof e.responseText != 'undefined')
                {
            		console.error(e.responseText);
            		
                };
                
                alert('CRITICAL ERROR: Registration service API is not running!\nUnable to retrieve extractors, proceeding with dummy data');
                Broker.extractorURLs.push('json/routeConfSample_easy.json');
            	Broker.extractorURLs.push('json/routeConfSample.json');
            	
                //start retrieving extractors
                retrieveDummyExtractors(0);
            }      
        });
    	
    	
    	//retrieve json data for all available extractors via REST-API
    	var retrieveDummyExtractors = function(i){
    		
    		if(i<Broker.extractorURLs.length)
    		{
	    		var url=Broker.extractorURLs[i];
	    		
	    		r=$.getJSON( url,
	    				function(data)
	    				{
	    					Broker.jsonExtractors.push(data);
	    					return retrieveDummyExtractors(i+1);
	    				});
    		}
    		else{
    			Broker.retrievedAllExtractors=true;
    			Broker.computeGraphNodes(); //automatically triggers this.computeGraphNodes
    		}
    		
    	};
    	
    	var retrieveExtractors = function(url){
    		
	    	r=$.getJSON( url,
	    		function(data)
	    		{
	    			Broker.jsonExtractors=data.specs;
	    			Broker.retrievedAllExtractors=true;
	    			Broker.computeGraphNodes(); //automatically triggers this.computeGraphNodes
	    		});
    	};

    };
    
    this.computeGraphNodes= function(){
    	
    	if( Broker.retrievedAllExtractors == false )
    	{
    		throw new Error('Unable to retrieve MICO extractors data');
    	}
    	

    	//convert jsonExtractors to brokerNodes:
    	
    	for (var i in this.jsonExtractors)
    	{
    		var extractor=this.jsonExtractors[i];
    		
    		//for each extractor mode
    		for(var j in extractor.mode )
    		{
    			var mode=extractor.mode[j];
    			//console.log(mode)
    			
    			
    			
    			//--- 1. Store extractorMode id
    			var node = {};
    			
    			var modeId=mode.id + ' ('+extractor.id + ' v' + extractor.version+')' ;
    			
    			node[modeId]={};
    			node[modeId].className='extractor';
    			node[modeId].extractorId=extractor.id;
    			node[modeId].extractorVersion=extractor.version;
    			node[modeId].modeId=mode.id;
    			node[modeId].description=mode.description;
    			
    			this.brokerNodes[modeId]=(node[modeId]);
    			
    			//--- 2. Store syntacticType of every input
    			for (k in mode.input)
    			{
    				node = {};
    				//console.log(mode.input[k].dataType);
    				var inputType=mode.input[k].dataType.syntacticType;
    				
    				node[inputType]={};
    				node[inputType].className='binary-file';
    				node[inputType].description=mode.input[k].semanticType.name;
    				this.brokerNodes[inputType]=(node[inputType]);
    			}
    			
    			//--- 3. Store syntacticType of every output
    			for (k in mode.output)
    			{
    				node = {};
    				//console.log(mode.output[k].dataType);
    				var outputType=mode.output[k].dataType.syntacticType;
    				node[outputType]={};
    				node[outputType].className='binary-file';
    				node[outputType].description=mode.output[k].semanticType.description;
    				this.brokerNodes[outputType]=(node[outputType]);
    			}
    		}
    	}
    	
    	
    	//push brokerNodes to the graph ( as required by D3.js)
    	var nodeID=0;
    	for (var i in this.brokerNodes)
    	{
    		this.graphNodes.push({
    			
    			title : i,
    			description : Broker.brokerNodes[i].description,
    			modeId : Broker.brokerNodes[i].modeId,
    			className : Broker.brokerNodes[i].className,
    			selected : false,
    			form: new Form(i),
    			
    			x: Math.getRandomInt(0, OverviewGraph.svgWidth),	//initial x position
    			y: Math.getRandomInt(0, OverviewGraph.svgHeight) //initial y position   		
    			
    		});
    		
    		//also storing the related unique incremental index
    		this.brokerNodes[i].nodeIndex=nodeID++;
    	}
    	
    	//push links between nodes to the graph (as required by D3.js )
    	for (var i in this.jsonExtractors)
    	{
    		var extractor=this.jsonExtractors[i];
    		
    		//for each extractor mode
    		for(var j in extractor.mode )
    		{
    			var mode=extractor.mode[j];
    			
    			//--- 1. Compute graph id for the mode
    			var modeId=mode.id + ' ('+extractor.id + ' v' + extractor.version+')' ; 			

    			
    			//--- 2. Create a link from every input to the extractor mode
    			for (k in mode.input)
    			{
    				var inputType=mode.input[k].dataType.syntacticType;
    				
    				this.graphLinks.push({
    					source : this.brokerNodes[inputType].nodeIndex,
    					target : this.brokerNodes[modeId].nodeIndex
    				});
    				;
    			}
    			
    			//--- 3. Create a link from the extractor mode to every output
    			for (k in mode.output)
    			{
    				var outputType=mode.output[k].dataType.syntacticType;

    				this.graphLinks.push({
    					source : this.brokerNodes[modeId].nodeIndex,
    					target : this.brokerNodes[outputType].nodeIndex
    				});
    			}
    		}
    	}
    	
    	//let the svg render the graph
    	OverviewGraph.draw();
    	ConfigurationGraph.updateActiveExtractorConfiguration();
    	
    	//TODO: comment out for immediate validation of input fields!
//    	$('.form-group').validator('validate');
    	
    };
    
    this.getExtractorMode = function(extractorId, extractorVersion, modeId){
    	for (var i in this.jsonExtractors)
    	{
    		var e = this.jsonExtractors[i];
    		if ((e.id == extractorId) &&  (e.version == extractorVersion)){
    			for(var j in e.mode){
    				if(e.mode[j].id == modeId){
    					return e.mode[j];
    				}
    			}
    		}
    	}
    	return null;
    };
    
    

};