package eu.mico.platform.broker.model;

public class MICOJob {

	private String wId;
	String itemURI;
	
	public MICOJob(String workflowId,String itemURI){
		this.wId=workflowId;
		this.itemURI=itemURI;
	}
	
	public String getWorkflowId(){
		return wId;
	}
	
	public String getItemURI(){
		return itemURI;
	}
	
	

	public boolean equals(Object j){

		return (wId.contentEquals(((MICOJob)j).getWorkflowId() ) && itemURI.contentEquals(((MICOJob)j).getItemURI()) );
	}
	
	public int hashCode(){
		return wId.hashCode() + itemURI.hashCode();
	}
}
