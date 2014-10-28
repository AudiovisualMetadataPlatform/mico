/**
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
package eu.mico.platform.event.api.spring;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.model.ContentItem;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Proxy for partial {@link eu.mico.platform.event.api.spring.Analyser} interface to fully comply with the {@link eu.mico.platform.event.api.AnalysisService} interface
 *
 * @author Kai Schlegel (kai.schlegel@googlemail.com)
 */
public class AnalyserProxy implements AnalysisService {

    /**
     * A unique ID (URI) that identifies this service and its functionality.
     */
    private URI id;
    /**
     * The type of output produced by this service as symbolic identifier (e.g. MIME type).
     */
    private String provides;
    /**
     * The type of input required by this service as symbolic identifier (e.g. MIME type).
     */
    private String requires;
    /**
     * The queue name that should be used by the messaging infrastructure for this service. If explicitly set,
     * this can be used to allow several services listen to the same queue and effectively implement a round-robin load
     * balancing. If queue name is not set, the event API will choose a random queue name.
     */
    private String queueName;
    /**
     * Implementation of the analysis task
     */
    private Analyser analyser;

    private Logger logger = LoggerFactory.getLogger(AnalyserProxy.class);

    public AnalyserProxy(URI id, String provides, String requires, String queueName, Analyser analyser) {
        this.id = id;
        this.provides = provides;
        this.requires = requires;
        this.queueName = queueName;
        this.analyser = analyser;
    }

    @Override
    public URI getServiceID() {
        return id;
    }

    @Override
    public String getProvides() {
        return provides;
    }

    @Override
    public String getRequires() {
        return requires;
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public void call(AnalysisResponse resp, ContentItem ci, URI uri) throws AnalysisException, IOException {
        logger.info("Performing analyser method");
        analyser.call(resp, ci, uri);
    }
}