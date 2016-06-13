package eu.mico.platform.broker.model;

import eu.mico.platform.camel.MICOCamelAnalysisException;
import eu.mico.platform.camel.MicoCamelContext;


public class CamelJob extends Thread implements Runnable{

	private boolean hasError  = false;
	private String itemURI = null,
			       partURI = null,
			       directURI = null;
	private MicoCamelContext camelContext;
	private String errorMessage;
	
	
	public CamelJob(String itemURI, String directURI, MicoCamelContext camelContext){
		this(itemURI,null,directURI,camelContext);
	}
	
	public CamelJob(String itemURI, String partURI, String directURI, MicoCamelContext camelContext){
		this.itemURI = itemURI;
		this.partURI = partURI;
		this.directURI = directURI;
		this.camelContext = camelContext;
	}

	@Override
	public void run() {
		
		try{
			if(partURI == null){
				camelContext.processItem(directURI,itemURI);
			}
			else{
				camelContext.processPart(directURI, itemURI, partURI);
			}
		}
		catch(MICOCamelAnalysisException e){
			hasError=true;
			errorMessage = e.getMessage();
		}		
	}

	public boolean hasError() {
		return hasError;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

}
