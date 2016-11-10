/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
