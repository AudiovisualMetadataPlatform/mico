package eu.mico.platform.broker.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//holds a 
public class MICOJobStatus implements Runnable{

	private static Logger log = LoggerFactory.getLogger(MICOJobStatus.class);
	private List<CamelJob> activeRoutes = new ArrayList<CamelJob>();	
	private Date created  = new Date();
	
	private boolean isFinished = false;
	private boolean hasError  = false;
	private String error = "";

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
		log.info("A mico job terminated");
	}

	public Date getCreated() {
		return created;
	}

	public String getErrorMessage() {
		return error;
	}

	
	
}
