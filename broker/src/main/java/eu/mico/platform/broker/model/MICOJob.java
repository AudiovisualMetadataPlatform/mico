package eu.mico.platform.broker.model;

public class MICOJob {

	private Integer wId;
	String itemURI;
	
	public MICOJob(Integer workflowId,String itemURI){
		this.wId=workflowId;
		this.itemURI=itemURI;
	}
	
	public Integer getWorkflowId(){
		return wId;
	}
	
	public String getItemURI(){
		return itemURI;
	}
	
	

	public boolean equals(Object j){

		return ((wId.intValue() == ((MICOJob)j).getWorkflowId().intValue() ) && itemURI.contentEquals(((MICOJob)j).getItemURI()) );
	}
	
	public int hashCode(){
		return wId.hashCode() + itemURI.hashCode();
	}
}
