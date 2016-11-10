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
