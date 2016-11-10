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

package eu.mico.platform.event.impl;

import eu.mico.platform.event.api.AnalysisServiceBase;
import eu.mico.platform.event.api.VersionUtil;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import eu.mico.platform.event.api.AnalysisService;

public class AnalysisServiceUtil {

    private AnalysisServiceUtil(){
        // this class has no instance
    }

    public static URI getServiceID(AnalysisServiceBase service) {
        return new URIImpl("http://www.mico-project.org/services/"
                + getQueueName(service));
    }

    public static  String getQueueName(AnalysisServiceBase service) {
        return service.getExtractorID() 
                + "-" + VersionUtil.stripPatchVersion(service.getExtractorVersion())
                + "-" + service.getExtractorModeID();
    }
}
