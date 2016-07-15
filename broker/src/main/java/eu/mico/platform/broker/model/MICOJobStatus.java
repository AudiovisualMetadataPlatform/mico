package eu.mico.platform.broker.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//holds a 
public class MICOJobStatus implements Runnable{

	private static Logger log = LoggerFactory.getLogger(MICOJobStatus.class);
	private List<CamelJob> activeRoutes = new ArrayList<CamelJob>();	
	private Date created  = new Date();
	
	private String itemURI = null,
				   routeId = null,
				   notificationURI = null;
	
	private boolean isFinished = false;
	private boolean hasError  = false;
	private String error = "";
	
	public MICOJobStatus(String itemURI, String routeId, String notificationURI) {
		this.itemURI = itemURI;
		this.routeId = routeId;
		this.notificationURI = notificationURI;
	}

	@Override
	public void run() {
		//start all the required routes
		for (CamelJob r : activeRoutes){
			r.run();
		}
		
		//wait for all of them to finish
		for (CamelJob r : activeRoutes){
			try {
				r.join();
				hasError=hasError || r.hasError();
				
				if(r.hasError()){
					error = error + r.getErrorMessage() + "\n";
				}
				
			} catch (InterruptedException e) {
				hasError=true;
				isFinished=true;
				error = "Execution interrupted abruptly";
				break;
			}
		}
		
		//and as soon as you're done, call the completion handler
		handleCompletion();
		
	}
	
	public void addCamelJob(CamelJob j){
		activeRoutes.add(j);
	}

	public boolean isFinished() {
		return isFinished;
	}


	public boolean hasError() {
		return hasError;
	}
	
	private void handleCompletion(){
		isFinished=true;
		
		if(notificationURI != null){
			log.info("Sending notification to {}",notificationURI);

			try {
	        	HttpClient httpClient = new HttpClient();
	        	PostMethod postMethod = new PostMethod(notificationURI);
	        	
	        	postMethod.addParameter("itemURI", itemURI);
	        	postMethod.addParameter("routeId", routeId);
	        	postMethod.addParameter("isCompleted", Boolean.toString(isFinished));
	        	postMethod.addParameter("hasErrors", Boolean.toString(hasError));
	        	
	        	if(hasError){
	        		postMethod.addParameter("errorMessage",getErrorMessage());
	        	}
        	
				httpClient.executeMethod(postMethod);
				
				int responseStatusCode = postMethod.getStatusCode();
	        	if ( responseStatusCode != 200) {
	        		log.warn("Received unexpected error code ("+responseStatusCode+" - "+HttpStatus.getStatusText(responseStatusCode)+") after sending the notification");
	        	}
	        	
			} catch (IOException e) {
				log.warn("Unexpected exception caught while submitting the notification",e);
			}
		}
		log.info("A mico job terminated");
	}

	public Date getCreated() {
		return created;
	}

	public String getErrorMessage() {
		return error;
	}

	
	
}
