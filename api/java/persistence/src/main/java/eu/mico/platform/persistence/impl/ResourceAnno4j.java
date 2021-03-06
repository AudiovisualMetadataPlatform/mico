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

package eu.mico.platform.persistence.impl;

import org.openrdf.idGenerator.IDGenerator;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mico.platform.anno4j.model.AssetMMM;
import eu.mico.platform.anno4j.model.ResourceMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Resource;

public abstract class ResourceAnno4j implements Resource {

    private Logger log = LoggerFactory.getLogger(getClass());
    
    private final ResourceMMM resourceMMM;
    protected final PersistenceService persistenceService;

    protected ResourceAnno4j(ResourceMMM resourceMMM, PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        this.resourceMMM = resourceMMM;
        
    }
    
    @Override
    public final URI getURI() {
        return (URI)resourceMMM.getResource();
    }

    @Override
    public final ResourceMMM getRDFObject() {
        return resourceMMM;
    }

    @Override
    public final String getSyntacticalType() {
        return resourceMMM.getSyntacticalType();
    }

    @Override
    public final void setSyntacticalType(String syntacticalType) throws RepositoryException {
        resourceMMM.setSyntacticalType(syntacticalType);
    }

    @Override
    public final String getSemanticType() {
        return resourceMMM.getSemanticType();
    }

    @Override
    public final void setSemanticType(String semanticType) throws RepositoryException {
        resourceMMM.setSemanticType(semanticType);
    }

    @Override
    public final Asset getAsset() throws RepositoryException {
        AssetMMM assetMMM = resourceMMM.getAsset();
        if (assetMMM == null) {
            assetMMM = createAndCommitAsset("");
            resourceMMM.setAsset(assetMMM);
        }
        return new AssetAnno4j(assetMMM, this.persistenceService.getStorage());
    }
    @Override
    public final Asset getAssetWithLocation(URI location) throws RepositoryException{
    	
    	if(location == null || location.toString().isEmpty()){
    		throw new IllegalArgumentException("Input location is null or empty");
    	}

        AssetMMM assetMMM = resourceMMM.getAsset();
        if (assetMMM == null) {
            assetMMM = createAndCommitAsset(location.toString());
            resourceMMM.setAsset(assetMMM);
        }
        if(! assetMMM.getLocation().contentEquals(location.toString()) ){
        	throw new IllegalArgumentException("Requested asset with location '"+location+
        			                           "', but the retrieved one has location '"+ assetMMM.getLocation()+"'");
        }
        return new AssetAnno4j(assetMMM, this.persistenceService.getStorage());
    }

    private AssetMMM createAndCommitAsset(String location) throws RepositoryException{
        AssetMMM assetMMM = null;

        //NOTE: Workaround for assetMMM.getLocation returning null (TODO: add Issue reference here)
        ObjectConnection itemCon = resourceMMM.getObjectConnection();
        ObjectConnection assetCon = null;
        org.openrdf.model.Resource assetResource = null;
        try {
            assetCon = itemCon.getRepository().getConnection();
            assetCon.begin();
            assetCon.setInsertContext(itemCon.getInsertContext());
            assetCon.setReadContexts(itemCon.getReadContexts());
            assetCon.setRemoveContexts(itemCon.getRemoveContexts());

            assetMMM = createObject(assetCon, null, AssetMMM.class);
            if(location == null || location.isEmpty()){
            	
                StringBuilder assetLocation = new StringBuilder()
                        .append(Asset.STORAGE_SERVICE_URN_PREFIX)
                        .append(this.getURI().getLocalName())
                        .append("/")
                        .append(((URI) assetMMM.getResource()).getLocalName());
                location=assetLocation.toString();
                
            }
            assetMMM.setLocation(location);
            assetCon.commit();
            log.trace("No Asset available for Resource {} - Created new Asset with id {} and location {}",
                    this.getURI(), assetMMM.getResourceAsString(), assetMMM.getLocation());
            assetResource = assetMMM.getResource();
        } finally {
            if(assetCon!= null){
                try {
                    assetCon.close();
                } catch (RepositoryException e){
                        /*ignore*/
                    e.printStackTrace();
                }
            }
        }
        assetMMM = AssetMMM.class.cast(itemCon.getObject(assetResource));
        assert assetMMM != null;

        return assetMMM;
    }

    @Override
    public final boolean hasAsset() throws RepositoryException {
        return resourceMMM.getAsset() != null;
    }
    
    protected <T extends RDFObject> T createObject(Class<T> clazz) throws RepositoryException{
        return createObject(null,clazz);
    }
    protected <T extends RDFObject> T createObject(URI resource, Class<T> clazz) throws RepositoryException{
        return createObject(resourceMMM.getObjectConnection(), resource, clazz);
    }
    protected <T extends RDFObject> T createObject(ObjectConnection con, URI resource, Class<T> clazz) throws RepositoryException{
        return con.addDesignation(con.getObjectFactory().createObject(
                resource == null ? IDGenerator.BLANK_RESOURCE : resource , clazz), clazz);
    }

}
