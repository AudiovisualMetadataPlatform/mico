package eu.mico.platform.camel;

import org.apache.camel.RuntimeCamelException;

public class MICOCamelAnalysisException extends RuntimeCamelException {

	public MICOCamelAnalysisException(String s){
		super("Detected error in MICO Camel route exectution : "+s);
	}
}
