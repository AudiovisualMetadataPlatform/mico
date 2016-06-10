package eu.mico.platform.camel;

import org.apache.camel.RuntimeCamelException;

public class MICOCamelAnalysisException extends RuntimeCamelException {

	private String message;
	private String description;
	
	public MICOCamelAnalysisException(String message, String description){
		super("Detected error in MICO Camel route execution : \n" + message +" - "+description);
		this.message = message;
		this.description = description;
	}
	
	public String getErrorMessage(){
		return message;
	}
	
	public String getErrorDescription(){
		return description;
	}
}
