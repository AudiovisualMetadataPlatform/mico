package eu.mico.platform.broker.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//holds a 
public class MICOJobStatus implements Runnable{

	private static Logger log = LoggerFactory.getLogger(MICOJobStatus.class);
	private List<CamelJob> activeRoutes = new ArrayList<CamelJob>();	
	
	private boolean isFinished = false;
	private boolean hasError  = false;

	@Override
	public void run() {
		//start all the required routes
		for (CamelJob r : activeRoutes){
			r.run();
			hasError=hasError || r.hasError();
		}
		
		//wait for all of them to finish
		for (CamelJob r : activeRoutes){
			try {
				r.join();
			} catch (InterruptedException e) {
				hasError=true;
				isFinished=true;
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

	
	
}
