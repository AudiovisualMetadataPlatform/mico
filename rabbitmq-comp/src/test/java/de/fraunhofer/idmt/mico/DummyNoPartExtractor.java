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

package de.fraunhofer.idmt.mico;

import java.io.IOException;

import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.persistence.model.Item;

public class DummyNoPartExtractor extends DummyExtractor {

	private static Logger log = LoggerFactory.getLogger(DummyNoPartExtractor.class);
	
	public DummyNoPartExtractor(String source, String target) {
		super(source, target);
	}

	public DummyNoPartExtractor(String source, String target, String extractorID, String version, String mode) {
		super(source, target, extractorID, version, mode);
	}
	

	@Override
	public void call(AnalysisResponse resp,
            Item item,
            java.util.List<eu.mico.platform.persistence.model.Resource> resourceList,
            java.util.Map<String, String> params) throws AnalysisException,
            IOException {
        log.info("mock analysis NO-NEW-PART request for [{}] on queue {}",
                resourceList.get(0).getURI(), getQueueName());
        try {
			resp.sendFinish(item);
        } catch (RepositoryException e) {
            throw new AnalysisException("could not access triple store");
        }
		
	}


	
}
