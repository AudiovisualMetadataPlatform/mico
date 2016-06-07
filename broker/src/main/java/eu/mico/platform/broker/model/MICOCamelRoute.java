package eu.mico.platform.broker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.lang3.StringEscapeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MICOCamelRoute {
	
	//provides read-only access to a specific extractor configuration described in a camel route
	public class ExtractorConfiguration {

		private String extractorId;
		private String modeId;
		private String version;
		private Map<String,String> params;

		public ExtractorConfiguration(String extractorId,String modeId,String version,Map<String,String> params){
			this.extractorId=extractorId;
			this.modeId=modeId;
			this.version=version;
			this.params=params;
		}

		public String getExtractorId() {
			return extractorId;
		}

		public String getModeId() {
			return modeId;
		}

		public String getVersion() {
			return version;
		}

		public Map<String,String> getParams() {
			return Collections.unmodifiableMap(params);
		}


	};
	
	//provides read-only access to a specific entry point described in a camel route
	public class EntryPoint {

		private String mimeType;
		private String syntacticType;
		private String directUri;
		

		public EntryPoint(String mimeType,String syntacticType, String directUri){
			this.mimeType=mimeType;
			this.syntacticType=syntacticType;
			this.directUri=directUri;
		}

		public String getMimeType() {
			return mimeType;
		}

		public String getSyntacticType() {
			return syntacticType;
		}

		public String getDirectUri() {
			return directUri;
		}

	};

	private static Logger log = LoggerFactory.getLogger(MICOCamelRoute.class);
	
	private ArrayList<ExtractorConfiguration> eConfig= new ArrayList<ExtractorConfiguration>();
	private ArrayList<EntryPoint> ePoints= new ArrayList<EntryPoint>();
	private String workflowId = null;
	
	
	/*
	 * Parse a camel route generated for the MICO system, and tries to identify
	 *  
     * 1. extractor configurations
     * 2. entry points of the pipelines
     *  
	 */
	public MICOCamelRoute parseCamelRoute(String xmlCamelRoute){
		log.info("Retrieving camel route configuration out of:\n{}",xmlCamelRoute);
		parseExtractorConfigurations(xmlCamelRoute);
		parseEntryPoints(xmlCamelRoute);
		return this;
	}

	/*
	 * Retrieves the identified entry points 
	 */
	public List<EntryPoint> getEntryPoints(){
		return new ArrayList<EntryPoint>(ePoints);
	}
	
	/*
	 * Retrieves the identified extractor configurations 
	 */
	public List<ExtractorConfiguration> getExtractorConfigurations(){
		return new ArrayList<ExtractorConfiguration>(eConfig);
	}
	
	/*
	 * Retrieves the workflow id 
	 */
	public String getWorkflowId(){
		return workflowId;
	}
	
	
	//------------------------- private members below this line  -------------------------
	
	private final static String EXTRACTOR_ID_PREFIX="extractorId=";
	private final static String EXTRACTOR_MODE_ID_PREFIX="modeId=";
	private final static String EXTRACTOR_VERSION_PREFIX="extractorVersion=";
	private final static String EXTRACTOR_PARAMETERS_PREFIX="parameters=";
	
	
	/*
	 * Creates one extractor configuration for each line matching this pattern.
	 * 
	 *  <to uri="mico-comp:vbox1?serviceId=TestExtractor&amp;
	 *           extractorId=mico-extractor-test&amp;
	 *           extractorVersion=1.0.0&amp;
	 *           modeId=TestExtractor&amp;
	 *           parameters={&quot;param1&quot;:&quot;valueParam1&quot;,&quot;param2&quot;:&quot;valueParam2&quot;}"/>
	 */
	
	private void parseExtractorConfigurations(String xmlCamelRoute){
		log.info("Retrieving route extractor configurations ... ");
		checkNonEmptyString("xmlCamelRoute",xmlCamelRoute);
		
    	//Split the route in separate lines
    	String[] lines = xmlCamelRoute.split("\n");
    	
    	//For every line
    	for(String line : lines){
    		//if the line describes a extractor
    		if(line.contains(EXTRACTOR_ID_PREFIX)){
    			
    			
    			//unescape html codes
    			line=StringEscapeUtils.unescapeHtml4(line);
    			log.debug("Parsing xml element {}",line);
    			
    			//split in the several chunks separated by '&'
    			String[] tokens=line.split("&");
    			
    			String extractorId = "";
    			String modeId  = "";
    			String version = "";
    			HashMap<String, String> params = new HashMap<String,String>();
    			
    			
    			
    			//for every token
    			for(String token : tokens){
    				
    				//extract the important information contained, if any
    				
    				if(token.startsWith(EXTRACTOR_ID_PREFIX)){
    					extractorId=token.substring(EXTRACTOR_ID_PREFIX.length());
    				}
    				else if (token.startsWith(EXTRACTOR_MODE_ID_PREFIX)){
    					modeId=token.substring(EXTRACTOR_MODE_ID_PREFIX.length());
    					if(modeId.contains("'/>")){
    						modeId=modeId.substring(0,modeId.length()-3);
    					}
    				}
    				else if(token.startsWith(EXTRACTOR_VERSION_PREFIX)){
    					version=token.substring(EXTRACTOR_VERSION_PREFIX.length());
    				}
    				else if(token.startsWith(EXTRACTOR_PARAMETERS_PREFIX)){
    					String stringParams=token.substring(EXTRACTOR_PARAMETERS_PREFIX.length());
    					
    					JsonFactory factory = new JsonFactory(); 
    	    		    ObjectMapper mapper = new ObjectMapper(factory); 
    	    		    TypeReference<HashMap<String,String>> typeRef 
    	    		            = new TypeReference<HashMap<String,String>>() {};

    	    		    
    					try {
    						params = mapper.readValue(stringParams, typeRef);
    					} catch (Exception e) {
    						log.error("Unable to parse extractor parameters.");
    						e.printStackTrace();
    						params = null;
    					} 
    				}
    			}
    			
    			//print some debug info
    			log.debug("extractorId = {}",extractorId);
    			log.debug("modeId = {}", modeId);
    			log.debug("version = {}",version);
    			log.debug("params = {}", params);
    			
    			//add the result of the parsing to the configuration member    			
    			addExtractorConfiguration(extractorId, modeId, version, params);
    		     
    		}
    	}
	}
	
	
	
	private final static String PIPELINE_STARTING_POINT_PATTERN="-starting-point-for";
	private final static String PIPELINE_ROUTE_STARTING_TOKEN="<route id='workflow-";
	private final static String DATA_MIME_TYPE_PREFIX="mimeType=";
	private final static String DATA_SYNTACTIC_TYPE_PREFIX="syntacticType=";

	
	/*
	 * Creates one entry point for each line matching this pattern, and sets the workflow id
	 * 
	 *  <route id='workflow-WORKFLOW_ID-starting-point-for-ELEMENT_ID-
	 *             mimeType=mico/test,
	 *             syntacticType=mico:inputTestType'>
	 */
	private void parseEntryPoints(String xmlCamelRoute) {
		log.info("Retrieving route entry points ... ");
		checkNonEmptyString("xmlCamelRoute",xmlCamelRoute);
		
    	//Split the route in separate lines
    	String[] lines = xmlCamelRoute.split("\n");
    	
    	//For every line
    	for(String line : lines){
    		//if the line describes a starting point
    		if(line.contains(PIPELINE_STARTING_POINT_PATTERN)){
    			
    			
    			//unescape html codes
    			line=StringEscapeUtils.unescapeHtml4(line);
    			log.debug("Parsing xml element {}",line);
    			
    			//retrieve mimeType location    			
    			String mimeType = "";
    			String syntacticType  = "";
    			String directUri = "";
    			
    			int mTypeIdx=line.indexOf(DATA_MIME_TYPE_PREFIX);
    			int sTypeIdx=line.indexOf(DATA_SYNTACTIC_TYPE_PREFIX);
    			int dUriIdx =line.indexOf(PIPELINE_ROUTE_STARTING_TOKEN);
    			
    			if(mTypeIdx!=-1 && sTypeIdx!=-1 && dUriIdx != -1){
    				mimeType=line.substring(mTypeIdx+DATA_MIME_TYPE_PREFIX.length(), sTypeIdx-1);
    				syntacticType=line.substring(sTypeIdx+DATA_SYNTACTIC_TYPE_PREFIX.length(),line.length()-2);
    				
    				String workflowId=line.substring(dUriIdx+PIPELINE_ROUTE_STARTING_TOKEN.length(),
    						                         line.indexOf(PIPELINE_STARTING_POINT_PATTERN));
    				this.workflowId=workflowId;
    				
    				directUri = "direct:workflow-1,mimeType=mico/test,syntacticType=C";
    				directUri = "direct:workflow-"+workflowId+","+
    							DATA_MIME_TYPE_PREFIX+mimeType+","+
    							DATA_SYNTACTIC_TYPE_PREFIX+syntacticType;
    			}
    			
    			//print some debug info
    			log.debug("mimeType = {}",mimeType);
    			log.debug("syntacticType = {}", syntacticType);
    			log.debug("directUri = {}", directUri);
    			
    			//add the result of the parsing to the configuration member    			
    			addEntryPoint(mimeType, syntacticType,directUri);
    		     
    		}
    	}
	}
	
	//add a new extractor configuration
	private void addExtractorConfiguration(String extractorId,String modeId,String version,Map<String,String> params) throws NullPointerException{
		
		checkNonEmptyString("extractorId",extractorId);
		checkNonEmptyString("modeId",modeId);
		checkNonEmptyString("version",version);		
		
		if( params!= null){
			eConfig.add( new ExtractorConfiguration(extractorId,modeId,version,new HashMap<String,String>(params)) );
		}
		else{
			eConfig.add( new ExtractorConfiguration(extractorId,modeId,version,new HashMap<String,String>()) );
		}
	};
	
	//add a new entry point
	private void addEntryPoint(String mimeType,String syntacticType, String directUri){

		checkNonEmptyString("mimeType",mimeType);
		checkNonEmptyString("syntacticType",syntacticType);
		checkNonEmptyString("directUri",directUri);
		ePoints.add( new EntryPoint(mimeType, syntacticType,directUri));
	};
	
	private void checkNonEmptyString(String s , String name) throws NullPointerException{
		if(s == null || s.isEmpty() ){
				log.error("String {} cannot be empty",name);
				throw new NullPointerException("Input String");
		}
	}
	
	
}
