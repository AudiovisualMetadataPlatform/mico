package eu.mico.platform.camel;

import org.apache.camel.RuntimeCamelException;

public class MICOCamelAnalysisException extends RuntimeCamelException {

	private static final long serialVersionUID = 3711484665837231316L;
	private String failingExtractor;
	private String message;
	private String description;
	
	public MICOCamelAnalysisException(String failingExtractor, String message, String description){
		super("Detected error in MICO Camel route execution for component "+failingExtractor+": \n" + message +" - "+description);
		this.failingExtractor = failingExtractor;
		this.message = message;
		this.description = description;
	}
	
	public String getErrorMessage(){
		return message;
	}
	
	public String getErrorDescription(){
		return description;
	}
	
	public String getFailingExtractor(){
		return failingExtractor;
	}
}
