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

package eu.mico.platform.anno4j.model.fam;

import java.util.Collection;

import org.openrdf.annotations.Iri;
import org.openrdf.model.Resource;
import org.openrdf.repository.object.RDFObject;

import com.github.anno4j.model.impl.multiplicity.Choice;

import eu.mico.platform.anno4j.model.namespaces.FAM;

@Iri(FAM.TOPIC_CLASSIFICATION_ANNOTATION)
public interface TopicClassificationBody extends Choice, FAMBody {

    @Iri(FAM.CLASSIFICATION_SCHEME)
    RDFObject getClassificationScheme();
    
    @Iri(FAM.CLASSIFICATION_SCHEME)
    void setClassificationScheme(RDFObject scheme);

    void setClassificationScheme(Resource scheme);
    
    void setClassificationSchemeUri(String schemeUri);
    
    Collection<TopicBody> getTopics();
    
    void addTopic(TopicBody topic);

    
}
