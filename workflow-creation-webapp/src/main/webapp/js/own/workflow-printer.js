var WorkflowPrinter = function() {

	
	this.printXmlString = function () {
		return XmlPrettyPrint(XmlElement('routes', this.createPipelineStartingPoints() + 
			                                       this.createPipelineRoutes() +  
			                                       this.createAggregatorRoutes(),
			                                     { xmlns : 'http://camel.apache.org/schema/spring' })
			                  );
	}
	
	
	this.createPipelineRoutes = function() {
		var out = ''
		for ( var p in Workflow.pipelines) {
			var pString = '';

			pString = pString + this.createSimplePipeline(Workflow.pipelines[p]);
			var strCheck = this.createMulticast(Workflow.pipelines[p],
					Workflow.multicasts);
			if (strCheck.length > 0) {
				pString = pString + strCheck
			} else {
				pString = pString
						+ this.createAggregatorcast(Workflow.pipelines, p,
								Workflow.aggregators);
			}

			var from = XmlElement('from', '', {
				uri : 'direct:workflow-' + WORKFLOW_PREFIX + '-pipeline-' + p
			});

			if (pString != '') {
				out = out + XmlElement('route', from + pString, {
					id : 'workflow-' + WORKFLOW_PREFIX + '-pipeline-' + p
				})
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

	this.createSimplePipeline = function(pipe) {

		var extractors = '';
		for ( var node in pipe) {
			var brokerExtractor = Broker.brokerNodes[pipe[node].originalNode.title];
			if (brokerExtractor != undefined) {
				var extractorId = brokerExtractor.extractorId;
				var extractorVersion = brokerExtractor.extractorVersion;
				var modeId = brokerExtractor.modeId;
				var queueName = modeId;

				extractors = extractors
						+ XmlElement("to", '', {
							uri : "mico-comp:vbox1?serviceId=" + queueName
									+ "&amp;extractorId=" + extractorId
									+ "&amp;extractorVersion="
									+ extractorVersion + "&amp;modeId="
									+ modeId
						});
			}
		}
		if (extractors != '') {
			extractors = XmlElement('pipeline', extractors);
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
	
	this.createMulticast = function(pipe, multicasts) {
		var targets = '';
		if (pipe[pipe.length - 1].requireMulticast) {
			var multicast = multicasts[pipe[pipe.length - 1].isMulticastNumber]
			targets = targets
					+ this.createMulticastTarget(Workflow.pipelines, multicast);
			return XmlElement('multicast', targets);
		}
		return targets;
	}

	this.createMulticastTarget = function(pipelines, multicast) {

		var out = '';
		for ( var i in multicast.to) {
			var to = multicast.to[i];

			if (pipelines[to][0].requireAggregator == false) {
				out = out
						+ XmlElement('to', '', {
							uri : 'direct:workflow-' + WORKFLOW_PREFIX
									+ '-pipeline-' + to
						});
			} else {

				var aggregator = Workflow.aggregators[pipelines[to][0].needsAggregateNumber];
				to = pipelines[to][0].needsAggregateNumber;
				if (aggregator.isSimple == false) {
					for ( var j in aggregator.from) {
						if (aggregator.from[j] instanceof Array) {
							for ( var k = 0; k < aggregator.from[j].length; k++) {
								if (aggregator.from[j][k] == multicast.from) {
									to = to + '-' + j;
								}
							}
						}
					}
				}
				out = out
						+ XmlElement('to', '', {
							uri : 'direct:workflow-' + WORKFLOW_PREFIX
									+ '-aggregator-' + to
						});
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
	this.createAggregatorcast = function(pipelines, currPipeIdx, aggregators) {

		for ( var a in aggregators) {
			var aggregator = aggregators[a];
			var to = a;
			for ( var j in aggregator.from) {
				if (aggregator.from[j] instanceof Array) {
					for ( var k = 0; k < aggregator.from[j].length; k++) {
						if (aggregator.from[j][k] == currPipeIdx) {
							if (aggregator.from[j].length > 1) {
								to = to + '-' + j;
							}
							return XmlElement('to', '', {
								uri : 'direct:workflow-' + WORKFLOW_PREFIX
										+ '-aggregator-' + to
							});
						}
					}
				} else {
					if (aggregator.from[j] == currPipeIdx) {
						return XmlElement('to', '', {
							uri : 'direct:workflow-' + WORKFLOW_PREFIX
									+ '-aggregator-' + to
						});
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
	 * <route id="workflow-'+WORKFLOW_PREFIX+'-aggregator-'+IDENTIFIER" >
	 *   <from uri="direct:workflow-'+WORKFLOW_PREFIX+'-pipeline-'+IDENTIFIER"/>
	 *     <aggregate strategyRef="aggregatorStrategy" completionSize="1">
	 *       <correlationExpression> 
	 *         <simple>header.id</simple>
	 *       </correlationExpression> 
	 *     <to uri="direct:TARGET_PIPELINE"/>
	 *   </aggregate>
	 * </route>
	 * 
	 * for complex Aggregators:
	 * 
	 * one (or more)
	 * 
	 * <route id="workflow-'+WORKFLOW_PREFIX+'-aggregator-'+IDENTIFIER" >
	 *   <from uri="direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+IDENTIFIER+SUBIDENTIFIER"/>
	 *     <aggregate strategyRef="aggregatorStrategy" completionSize="1">
	 *       <correlationExpression>
	 *         <simple>header.id</simple>
	 *       </correlationExpression> 
	 *     <to uri="direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+IDENTIFIER"/>
	 * </aggregate> <route>
	 * 
	 * plus
	 * 
	 * <route id="workflow-'+WORKFLOW_PREFIX+'-aggregator-'+IDENTIFIER" >
	 *   <from uri="direct:workflow-'+WORKFLOW_PREFIX+'-aggregator-'+IDENTIFIER"/>
	 *     <aggregate strategyRef="aggregatorStrategy" completionPolicy="COMPLEX">
	 *     <correlationExpression> 
	 *       <simple>header.id</simple>  
	 *     </correlationExpression>
	 *     <to uri="direct:TARGET_PIPELINE"/>
	 *   </aggregate>
	 * <route>
	 * 
	 */
	this.createAggregatorRoutes = function() {
		var out = '\n';
		for ( var a in Workflow.aggregators) {
			var aggregator = Workflow.aggregators[a];
			if (aggregator.isSimple == true) {

				var from = XmlElement('from', '', {
					uri : 'direct:workflow-' + WORKFLOW_PREFIX + '-aggregator-'
							+ a
				});
				var correlationExpression = XmlElement('correlationExpression',
						'<simple>header.id</simple>');WORKFLOW_PREFIX
				var to = XmlElement('to', '', {
					uri : 'direct:workflow-' + WORKFLOW_PREFIX + '-pipeline-'
							+ aggregator.to
				});
				var aggregate = XmlElement('aggregate', correlationExpression
						+ to, {
					strategyRef : 'aggregatorStrategy',
					completionSize : '1'
				})

				out = out + XmlElement('route', from + aggregate, {
					id : 'workflow-' + WORKFLOW_PREFIX + '-aggregator-' + a
				});

			} else {

				// first simple sub-Aggregators:
				for ( var i in aggregator.from) {
					var currInput = aggregator.from[i];
					if (currInput.length > 1) {

						var from = XmlElement('from', '', {
							uri : 'direct:workflow-' + WORKFLOW_PREFIX
									+ '-aggregator-' + a + '-' + i
						});
						var correlationExpression = XmlElement(
								'correlationExpression',
								'<simple>header.id</simple>');
						var to = XmlElement('to', '', {
							uri : 'direct:workflow-' + WORKFLOW_PREFIX
									+ '-aggregator-' + a
						});
						var aggregate = XmlElement('aggregate',
								correlationExpression + to, {
									strategyRef : 'aggregatorStrategy',
									completionSize : '1'
								})

						out = out
								+ XmlElement('route', from + aggregate, {
									id : 'workflow-' + WORKFLOW_PREFIX
											+ '-aggregator-' + a + '-' + i
								});
					}
				}

				// then the complex one
				var from = XmlElement('from', '', {
					uri : 'direct:workflow-' + WORKFLOW_PREFIX + '-aggregator-'
							+ a
				});
				var correlationExpression = XmlElement('correlationExpression',
						'<simple>header.id</simple>');
				var to = XmlElement('to', '', {
					uri : 'direct:workflow-' + WORKFLOW_PREFIX + '-pipeline-'
							+ aggregator.to
				});
				var aggregate = XmlElement('aggregate', correlationExpression
						+ to, {
					strategyRef : 'aggregatorStrategy',
					completionPolicy : 'COMPLEX'
				})

				out = out + XmlElement('route', from + aggregate, {
					id : 'workflow-' + WORKFLOW_PREFIX + '-aggregator-' + a
				});
			}
		}
		return out + '\n' + XmlElement('bean', '', {
			id : "aggregatorStrategy",
			class : "org.apache.camel.processor.BodyInAggregatingStrategy"
		});
	}

	/*
	 * Example returned strings
	 * 
	 * <route
	 * id="workflow-'+WORKFLOW_PREFIX+'-starting-point-for-pipeline-'+IDENTIFIER"
	 * <from uri="direct:MIME_TYPE"/> <aggregate
	 * strategyRef="aggregatorStrategy" completionSize="1">
	 * <correlationExpression> <simple>header.id</simple>
	 * </correlationExpression> <to uri="direct:TARGET"/> </aggregate> </route>
	 * 
	 */
	this.createPipelineStartingPoints = function() {

		var out = '\n\n';

		for ( var p in Workflow.pipelines) {
			var pipe = Workflow.pipelines[p];

			if (pipe[0].id == ConfigurationGraph.USER_LABEL) {

				// if the mimetypes are going only to one extractor, connect it
				if (pipe[1] != undefined) {
					for ( var i in pipe[1].form.getSelectedInputMimeTypes()) {

						var mimeTypes = pipe[1].form
								.getSelectedInputMimeTypes()[i]
						for ( var j in mimeTypes) {
							var mimeType = mimeTypes[j]

							var from = XmlElement('from', '', {
								uri : 'direct:' + mimeType
							});
							var correlationExpression = XmlElement(
									'correlationExpression',
									'<simple>header.id</simple>');
							var to = XmlElement('to', '', {
								uri : 'direct:workflow-' + WORKFLOW_PREFIX
										+ '-pipeline-' + p
							});
							var aggregate = XmlElement('aggregate',
									correlationExpression + to, {
										strategyRef : 'aggregatorStrategy',
										completionSize : '1'
									})

							out = out
									+ XmlElement(
											'route',
											from + to,
											{
												id : 'workflow-'
														+ WORKFLOW_PREFIX
														+ '-starting-point-for-pipeline-'
														+ p + '-' + mimeType
											});
						}
					}
				} else {
					// otherwise, you need a multicast to the correct targets
					if (pipe[0].requireMulticast) {
						console.log(this.createMulticast(pipe, Workflow.multicasts))
					}
				}

				/*
				 * { //TODO: find the freaking aggregator =) var to=''; var
				 * found=false; var inputIndex=-1; var aggregatorIndex=-1;
				 * for(var a in Workflow.aggregators){ var
				 * aggregator=Workflow.aggregators[a]; to=a; for(var j in
				 * aggregator.from){ if(aggregator.from[j] instanceof Array){
				 * for(var k=0; k< aggregator.from[j].length && !found; k++){
				 * if(aggregator.from[j][k]==p){ found=true; inputIndex=j;
				 * aggregatorIndex=a; to=to+'-'+j; } } } else{
				 * if(aggregator.from[j]==p){ found=true; inputIndex=j;
				 * aggregatorIndex=a; } } if (found) break; } if(found) break; }
				 * 
				 * var
				 * pipe=Workflow.pipelines[Workflow.aggregators[aggregatorIndex].to]
				 * 
				 * var mimeTypes=pipe[0].form.getSelectedInputMimeTypes()
				 * mimeTypes=mimeTypes[Object.keys(mimeTypes)[inputIndex]]; for
				 * (var j in mimeTypes){ var mimeType=mimeTypes[j]
				 * 
				 * var from=XmlElement('from','',{uri: 'direct:'+mimeType}); var
				 * correlationExpression=XmlElement('correlationExpression','<simple>header.id</simple>');
				 * var to=XmlElement('to','',{uri:
				 * 'direct:workflow-'+WORKFLOW_PREFIX+'-pipeline-'+p}); var
				 * aggregate=XmlElement('aggregate',correlationExpression+to,{strategyRef:
				 * 'aggregatorStrategy', completionSize:'1'})
				 * 
				 * out=out+XmlElement('route',from+aggregate,{id:
				 * 'workflow-'+WORKFLOW_PREFIX+'-starting-point-for-pipeline-'+p+'-'+mimeType
				 * }); } }
				 */
			}

		}

		return out + '\n';
	};

};

WorkflowPrinter = new WorkflowPrinter();