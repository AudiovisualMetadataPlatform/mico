var Form=function(extractorNodeId){
	
	this.thumbnail=null;
	this.closeBtn=null;
	
	this.init=function(extractorNodeId){
	  
		if ( (typeof Broker.brokerNodes[extractorNodeId] != 'undefined') && 
			 (typeof Broker.brokerNodes[extractorNodeId].extractorId != 'undefined') ){
			
		  var extractorId=Broker.brokerNodes[extractorNodeId].extractorId;
		  var extractorVersion=Broker.brokerNodes[extractorNodeId].extractorVersion;
		  var modeId=Broker.brokerNodes[extractorNodeId].modeId;
		  
		  console.log('generating form for the following extractor mode:');
		  var mode=Broker.getExtractorMode(extractorId,extractorVersion,modeId);
		  console.log(mode);
		  
		  
		  //create general form
		  
		  var thumbnail=document.createElement('form');
		  thumbnail.classList.add('thumbnail');
		  thumbnail.classList.add('col-md-4');	//aka, max 3 per row (12 tiles per row)
		  thumbnail.setAttribute('data-toggle','validator');  
		  
		  
		  //append name of the extractor mode and close button
		  
		  thumbnail.innerHTML=('<div class="caption" title="'+mode.description+'">'+
				  			   '<button type="button" class="close" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+
				  			   '<h3><small>'+extractorNodeId+'</small></h3><hr></div>');
		  
		  //setup reference to close button:
		  this.closeBtn=thumbnail.getElementsByTagName('button')[0];

		  
		  
		  //utility function for mimeType selection
		  
		  var btnGroup=function(mimeTypes){
			  
			  var grp=document.createElement('div');
			  grp.classList.add('btn-group');
			  grp.classList.add('btn-group-sm');
			  grp.classList.add('mimetype-checkbox');
			  
			  grp.setAttribute('data-toggle','buttons');  
			  
			  for (var j in mimeTypes){
				  var mimeBtn=document.createElement('label');
				  mimeBtn.classList.add('btn');
				  mimeBtn.classList.add('btn-default');
				  
				  mimeBtn.innerHTML='<input type="checkbox" autocomplete="off">'+
				                    '<span class="glyphicon glyphicon-ok"></span> '+
				                    mimeTypes[j];
				  
				  mimeBtn.classList.add('active');
				  mimeBtn.childNodes[0].checked=true;
				  
				  grp.appendChild(mimeBtn);
			  }
//			  grp.childNodes[0].classList.add('active');
//			  grp.childNodes[0].childNodes[0].checked=true;
			  
			  grp.onchange=function(e){
				  var valid=false;
				  
				  var selectedMimeTypes = (this.getElementsByTagName('input'));
				  
				  for(var i =0 ; i< selectedMimeTypes.length; i++)
				  {
					  valid=valid || selectedMimeTypes[i].checked;
				  }
				  if(! valid){
					  this.parentNode.classList.add('bg-danger');
					  $(e.target).click();
				  }else{
					  this.parentNode.classList.remove('bg-danger');
				  }
				  
				  ConfigurationGraph.updateActiveExtractorConfiguration();
			  };
			  
			  return grp;
		  };
		  
		  //utility function for parameter selection
		  var getParamInputGroup=function(param, paramIndex){
			  
			  var paramName=param.name;
			  var allowedValue=param.allowedValue;
			  var allowedRange=param.allowedRange;
			  
			  var moreThanOneOutputParam=(param.primitiveType.primitiveArray != null || 
						  				  param.primitiveType.enumerationArray.length>0);
			  
			  //regular expression for a single floating/int value
			  var regex=regExp.float;
			  
			  if(moreThanOneOutputParam){
				  
				  //regular expression for a comma separated list of floating/int values
				  regex = regExp.floatArray;
				  if(param.primitiveType.primitiveArray =='int[]'){

					  //regular expression for a comma separated list of int values
					  regex = regExp.intArray;
				  }
			  }
			  else if(param.primitiveType.primitive =='int'){
				  
				  //regular expression for single int value
				  regex = regExp.int;
			  };
			  

			  //setup output:
			  var out=null;
			  
			  
			  if(param.isSimpleFlag == true){
				  //if simple flag, override allowedValue with "enabled" and "disabled", plus force only one possibility
				  moreThanOneOutputParam=false;
				  allowedValue=["enabled","disabled"];
				  
			  }
			  
			  if(allowedValue.length>0){
				  
				  //if input list of values, we want an inlined from, with dropdown button + label
				  out=document.createElement('div');
				  out.classList.add('input-group');
				  
				  //-- displaying label
				  
				  var displayLabel=document.createElement('input');
				  displayLabel.classList.add('form-control');
				  displayLabel.id='value-param-'+paramIndex;
				  displayLabel.setAttribute('type','text');
				  displayLabel.setAttribute('placeholder',allowedValue[0]);
				  displayLabel.setAttribute('disabled','');
				  
				  //-- dropdown
				  var dropdown=document.createElement('div');
				  dropdown.classList.add('dropdown');
				  dropdown.classList.add('input-group-btn');
				  
				  var dropButton=document.createElement('button');
				  dropButton.id='dropdown-param-'+paramIndex;
				  
				  dropButton.className='btn btn-default btn-small dropdown-toggle';
				  dropButton.setAttribute('type','button');
				  dropButton.setAttribute('data-toggle','dropdown');
				  dropButton.setAttribute('aria-haspopup','true');
				  dropButton.setAttribute('aria-expanded','false');
				  dropButton.innerHTML=paramName+' <span class="caret"></span>';
				  
				  var dropList=document.createElement('ul');
				  dropList.className='dropdown-menu';
				  dropList.setAttribute('aria-labelledby',dropButton.id);
				  
				  if (moreThanOneOutputParam){
					  
					  dropList.classList.add('btn-group-vertical');
					  dropList.classList.add('btn-group-sm');
					  dropList.setAttribute('data-toggle','buttons'); 
					  
					  for(var v in allowedValue)
					  {
						  var li=document.createElement('label');
						  li.classList.add('btn');
						  li.classList.add('btn-default');
							  
						  li.innerHTML='<input type="checkbox" autocomplete="off">'+
						                    '<span class="glyphicon glyphicon-ok"></span> '+
						                    allowedValue[v];
							  
						  dropList.appendChild(li);
					  }
					  dropList.childNodes[0].classList.add('active');
					  dropList.childNodes[0].childNodes[0].checked=true;
					  
					  
					  dropList.onchange=function(e){
						  var valid=false;
							  
						  var selectedParams = (this.getElementsByTagName('input'));
						  var paramLabels    = (this.getElementsByTagName('label'));
							  
						  for(var i =0 ; i< selectedParams.length; i++)
						  {
							  valid=valid || selectedParams[i].checked;
						  }
						  if(! valid){
							  $(e.target).click();
						  }
							  
						  var newLabel=[];
						  for(var i =0 ; i< selectedParams.length; i++)
						  {
							  if(selectedParams[i].checked){
								  newLabel.push(paramLabels[i].textContent.substr(1));
							  }
						  }
							  
						  displayLabel.setAttribute('placeholder',newLabel);
							 
					  };
				  }
				  else{
					  for(var v in allowedValue)
					  {
						  var li=document.createElement('li');
						  li.innerHTML='<a>'+allowedValue[v]+'</a>';
						  li.addEventListener('click',function(e){  
							  displayLabel.setAttribute('placeholder',this.childNodes[0].innerHTML);
						  });
						  dropList.appendChild(li);
					  }
				  }
				  
				  
				  dropdown.appendChild(dropButton);
				  dropdown.appendChild(dropList);
				  
				  
				  
				  //--add both dropdown and label 
				  
				  out.appendChild(dropdown);
				  out.appendChild(displayLabel);
				  
			  }
			  else if(allowedRange.length>0)
			  {
				  //if input list of ranges				  		  

				  
				  var range2String=function(range){
					  
					  //create an easy access for the values of minIncl and maxIncl
					  for (var contentIdx in range.content ){
						  range[range.content[contentIdx].name]=range.content[contentIdx].value;
					  }
					  
					  var out = 'x';
					  if(range.maxIncl != undefined) 
					  {
						  out=out+' ≤ '+range.maxIncl;
						  if(range.minIncl != undefined){
							  out= range.minIncl + ' ≤ ' + out;
						  }
					  }
					  else{
						  out=out+' ≥ '+range.minIncl;
					  }
					  return out;
				  };

				  var validRangeDescription='value x subject to: ( '+ range2String(allowedRange[0])+' )';
				  for(var r=1; r < allowedRange.length ; r++){
					  validRangeDescription=validRangeDescription+' OR ( '+range2String(allowedRange[r])+' )';
				  }
				  
				  

					
				  
				//if only one value can be selected
				if(!moreThanOneOutputParam || true){
					
					//we want a inlined form with validation, displaying info about the required ranges
					
					//outern form-group
					out=document.createElement('div');
					out.classList.add('form-group');
					
					//inner input group
					var inputGroup=document.createElement('div');
					inputGroup.classList.add('input-group');
					inputGroup.innerHTML='<span class="input-group-addon">'+paramName+'</span>';
					
					//input field
					var paramInputField=document.createElement('input');
					paramInputField.classList.add('form-control');
					paramInputField.id='value-param-'+paramIndex;
					paramInputField.setAttribute('type',"text");
					//regular expression for comma separated list of loating values 
//					paramInputField.setAttribute('pattern', '^(\s*[-+]?[0-9]+\.[0-9]+\s*|\s*[-+]?\d+\s*)(\s*,\s*[-+]?[0-9]+\.[0-9]+\s*|\s*,\s*[-+]?\d+\s*)*$');
					paramInputField.setAttribute('data-error', validRangeDescription);
					paramInputField.setAttribute('data-range', 'validateRange');
					paramInputField.setAttribute('required','');
					
//					paramInputField.value=(allowedRange[0].minIncl!=null?allowedRange[0].minIncl:allowedRange[0].maxIncl);
					paramInputField.setAttribute('placeholder',moreThanOneOutputParam ? param.primitiveType.primitiveArray : param.primitiveType.primitive);
					
					paramInputField.regex=regex;
					paramInputField.moreThanOneOutputParam=moreThanOneOutputParam;
					

					//error div
					var errorField=document.createElement('div');
					errorField.classList.add('help-block');
					errorField.classList.add('with-errors');
					errorField.innerHTML=validRangeDescription;
					
					
					inputGroup.appendChild(paramInputField);
								
					out.appendChild(inputGroup);
					out.appendChild(errorField);
					
					var validateRange = function (el) {

						//validate syntax of the inptu field
						var valid=$(el)[0].regex.test($(el).val());

						//read the input field, and reshape from 'A, B,  C' to ['A','B','C']
						var value=$(el).val();
						value=value.replace(/\s/g, '');
						value=value.split(',');

						//validate every value in the newly obtained array, if the syntax( == valid) is correct							
						for(var v=0; valid && (v < value.length); v++){

							var currValid=false;

							for(var r=0; r < allowedRange.length ; r++){
								currValid=currValid || ((allowedRange[r].minIncl==null || parseFloat(value[v]) >= parseFloat(allowedRange[r].minIncl)) &&
										(allowedRange[r].maxIncl==null || parseFloat(value[v]) <= parseFloat(allowedRange[r].maxIncl)));
							}


							valid=valid && currValid;
						}

						//return the overall assesment ()
						return valid;
					};

					
					$(out).validator({
						custom: {
							range: validateRange
					    },
					    errors: {
					    	range: 'parameter out of defined range'
					    }
					});
					
				}
				else{
					//TODO: think about a better way of inputting more than one value
				}
				
			  }

			  return out;
		  };
		  
		  
		  //append list of inputs
		  
		  for(var i in mode.input){
			  
			  var d = document.createElement('div');
			  d.classList.add('extractor-input-data');
			  d.setAttribute('name',mode.input[i].dataType.syntacticType);
			  d.title=mode.input[i].semanticType.description;
			  
			  var t = document.createElement('div');
			  t.classList.add('extractor-data-description');
			  t.innerHTML='Input #'+(parseInt(i)+1)+': '+mode.input[i].dataType.syntacticType;
			  d.appendChild(t);		  
			  
			  
			  
			  d.appendChild(btnGroup(mode.input[i].dataType.mimeType));
			  
			  thumbnail.childNodes[0].appendChild(d);
			  
			  
		  }
		
		  //append separator
		  thumbnail.childNodes[0].appendChild(document.createElement('hr'));
		  
		  
		  //append list of outputs
		  
		  for(var i in mode.output){
			  
			  var d = document.createElement('div');
			  d.classList.add('extractor-output-data');
			  d.setAttribute('name',mode.output[i].dataType.syntacticType);
			  d.title=mode.output[i].semanticType.description;
			  
			  var t = document.createElement('div');
			  t.classList.add('extractor-data-description');
			  t.innerHTML='Output #'+(parseInt(i)+1)+': '+mode.output[i].dataType.syntacticType;
			  d.appendChild(t);		  
			  
			  
			  
			  d.appendChild(btnGroup(mode.output[i].dataType.mimeType));
			  
			  thumbnail.childNodes[0].appendChild(d);
	
		  }
		  
		  
		  document.getElementById('selectedExtractors').appendChild(thumbnail);
		  
		  //append separator
		  thumbnail.childNodes[0].appendChild(document.createElement('hr'));
		  
		  //if necessary, append section about the parameters:
		  if(mode.param.length > 0)
		  {

			  
			  for(var i in mode.param){
				  var param=mode.param[i];
				  
				  var d = document.createElement('div');
				  d.classList.add('extractor-param-data');
				  d.setAttribute('name',param.cmdLineSwitch);
				  d.title=param.description;
				  
				  var t = document.createElement('div');
				  t.classList.add('extractor-param-description');
				  t.innerHTML='Parameter #'+(parseInt(i)+1)+':';
				  d.appendChild(t);
				  
				  d.appendChild(getParamInputGroup(param, i));
				  
				  
				  thumbnail.childNodes[0].appendChild(d);
			  }
			  
			  //append separator
			  var hr=document.createElement('hr');
			  hr.style.marginTop='0px';
			  thumbnail.childNodes[0].appendChild(hr);
		  }

		  
		  this.thumbnail=thumbnail;
		  return thumbnail;
		}
		
		return null;
	};
	
	//returns a JS Object. Each attribute corresponds to the current selection of the respective syntacticType :)
	this.getSelectedInputMimeTypes = function(){
		
		var out={};		
		var inputs=this.thumbnail.getElementsByClassName('extractor-input-data');
		
		//for every input of the current extractor
		for (var i=0; i<inputs.length; i++){
			
			out[inputs[i].getAttribute('name')]=[];
			
			//retrieve selected mimeTypes
			var selectedMimeTypes = inputs[i].getElementsByClassName('active');
			
			for (var j=0; j<selectedMimeTypes.length; j++)
			{
			  out[inputs[i].getAttribute('name')].push(selectedMimeTypes[j].textContent.substr(1));
			}
			
		}		
		
		return out;
	};
	
	//returns a JS Object. Each attribute corresponds to the current selection of the respective syntacticType :)
	this.getSelectedOutputMimeTypes = function(){
		
		var out={};		
		var inputs=this.thumbnail.getElementsByClassName('extractor-output-data');
		
		//for every input of the current extractor
		for (var i=0; i<inputs.length; i++){
			
			out[inputs[i].getAttribute('name')]=[];
			
			//retrieve selected mimeTypes
			var selectedMimeTypes = inputs[i].getElementsByClassName('active');
			
			for (var j=0; j<selectedMimeTypes.length; j++)
			{
			  out[inputs[i].getAttribute('name')].push(selectedMimeTypes[j].textContent.substr(1));
			}
			
		}		
		
		return out;
	};
	
	this.getSelectedParameters = function(){
		var out={};
		var inputs=this.thumbnail.getElementsByClassName('extractor-param-data');
	}
	
	this.init(extractorNodeId);
};