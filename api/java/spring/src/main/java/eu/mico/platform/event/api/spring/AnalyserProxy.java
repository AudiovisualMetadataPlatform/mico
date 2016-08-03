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
import eu.mico.platform.persistence.model.Item;
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
     * ID that identifies the general functionality of this extractor.
     */
    private String extractorId;
    /**
     * ID that identifies the specific mode in which the extractor is running.
     */
    private String extractorModeId;
    /**
     * Extractor version .
     */
    private String extractorVersion;
    /**
     * The type of output produced by this service as symbolic identifier (e.g. MIME type).
     */
    private String provides;
    /**
     * The type of input required by this service as symbolic identifier (e.g. MIME type).
     */
    private String requires;
    /**
     * Implementation of the analysis task
     */
    private Analyser analyser;

    private Logger logger = LoggerFactory.getLogger(AnalyserProxy.class);

    public AnalyserProxy(String extractorId, String extractorModeId, String extractorVersion, String provides, String requires, Analyser analyser) {
        this.extractorId = extractorId;
        this.extractorModeId = extractorModeId;
        this.extractorVersion = extractorVersion;
        this.provides = provides;
        this.requires = requires;
        this.analyser = analyser;
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
    public void call(AnalysisResponse resp, Item item, java.util.List<eu.mico.platform.persistence.model.Resource> resourceList, java.util.Map<String,String> params) throws AnalysisException ,IOException {
        logger.info("Performing analyser method");
        analyser.call(resp, item, resourceList, params);
    }

	@Override
	public String getExtractorID() {
		return extractorId;
	}

	@Override
	public String getExtractorModeID() {
		return extractorModeId;
	}

	@Override
	public String getExtractorVersion() {
		return extractorVersion;
	}
}